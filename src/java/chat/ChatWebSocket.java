package chat;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@Startup
@Singleton
@ServerEndpoint(value = "/ChatWebSocket", encoders = {MessageEncDec.class}, decoders = {MessageEncDec.class})

public class ChatWebSocket {

    private final Map<String, List<ChatSession>> sessions = new HashMap<String, List<ChatSession>>();

    @Schedule(minute = "*/1", second = "0", hour = "*")
    public void clearIdle() {
        if (!isEnabled()) {
            return;
        }
        try {
            String[] poolNames = getPoolNames();
            for (String poolName : poolNames) {
                ChatSession[] sess = getSessions(poolName);
                List<ChatSession> toRemove = new ArrayList();
                for (ChatSession cs : sess) {
                    if (cs.isIdle()) {
                        toRemove.add(cs);
                    } else {
                        try {
                            Message m = new Message();
                            m.addUTF("PING");
                            cs.pingSent();
                            cs.getSession().getAsyncRemote().sendText(m.toJsonString());
                        } catch (Exception ex) {
                            Logger.getLogger(ChatWebSocket.class.getName()).log(Level.INFO, null, ex);
                        }
                    }
                }
                if (!toRemove.isEmpty()) {
                    //System.out.println("Removing: " + toRemove.size());
                    try (Connection conn = MySQLCommon.getConnection(poolName, null)) {
                        for (ChatSession cs : toRemove) {
                            MySQLCommon.setTz(conn, poolName, cs.getTz());
                            removeSession(cs, conn);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(ChatWebSocket.class.getName()).log(Level.INFO, null, ex);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ChatWebSocket.class.getName()).log(Level.INFO, null, ex);
        }
    }

    private boolean isEnabled() {
        try (Connection con = MySQLCommon.getConnection("sigmads", null)) {
            return new MySQLQuery("select chat_active from sys_cfg").getAsBoolean(con);
        } catch (Exception ex) {
            Logger.getLogger(ChatWebSocket.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    private ChatSession[] getSessions(String poolName) {
        synchronized (sessions) {
            return sessions.get(poolName).toArray(new ChatSession[0]);
        }
    }

    private String[] getPoolNames() {
        synchronized (sessions) {
            return sessions.keySet().toArray(new String[0]);
        }
    }

    private synchronized void sendUserUpdate(String poolName, Connection conn, User u) throws Exception {
        ChatSession[] sess = getSessions(poolName);
        for (ChatSession cs : sess) {
            if (cs.getSession().isOpen() && cs.getEmpId() != u.id) {
                u.setThreadOwnerId(cs.getEmpId(), conn);
                try {
                    Message m = new Message();
                    m.addUTF("USER");
                    u.addToMessage(m);
                    cs.getSession().getAsyncRemote().sendText(m.toJsonString());
                } catch (Exception ex) {
                    Logger.getLogger(ChatWebSocket.class.getName()).log(Level.INFO, null, ex);
                }
            }
        }
    }

    private void removeSession(ChatSession cs, Connection conn) {
        try {
            cs.closeWebSocketSession();
            synchronized (sessions) {
                sessions.get(cs.getPoolName()).remove(cs);
            }
            sendUserUpdate(cs.getPoolName(), conn, new User(cs.getEmpId(), null, hasSessions(cs.getPoolName(), cs.getEmpId()), conn));
        } catch (Exception ex) {
            Logger.getLogger(ChatWebSocket.class.getName()).log(Level.INFO, null, ex);
        }
    }

    private ChatSession findSession(Session sess) {
        return findSession(null, sess);
    }

    private ChatSession findSession(String poolName, Session s) {
        String[] poolNames = getPoolNames();
        for (String iPoolName : poolNames) {
            if (poolName == null || poolName.equals(iPoolName)) {
                ChatSession[] sess = getSessions(iPoolName);
                for (ChatSession cs : sess) {
                    if (cs.getSession().equals(s)) {
                        return cs;
                    }
                }
            }
        }
        return null;
    }

    private ChatSession[] findSessions(String poolName, int empId) {
        List<ChatSession> rta = new ArrayList<>();
        ChatSession[] sess = getSessions(poolName);
        for (ChatSession cs : sess) {
            if (cs.getEmpId() == empId) {
                rta.add(cs);
            }
        }
        return rta.toArray(new ChatSession[0]);
    }

    private boolean hasSessions(String poolName, int id) {
        ChatSession[] sess = getSessions(poolName);
        for (ChatSession cs : sess) {
            if (cs.getEmpId() == id) {
                return true;
            }
        }
        return false;
    }

    @OnMessage
    public void command(Message im, Session session) {
        try {
            if (!isEnabled()) {
                return;
            }
            Connection conn = null;
            String c = im.getUTF();
            if (c.equals("ID") || c.equals("LOGIN")) {
                int fromId = im.getInt();
                String poolName = im.getUTF();
                String tz = im.getUTF();

                synchronized (sessions) {
                    List<ChatSession> sess;
                    if (sessions.containsKey(poolName)) {
                        sess = sessions.get(poolName);
                    } else {
                        sess = new ArrayList<ChatSession>();
                        sessions.put(poolName, sess);
                    }
                    sess.add(new ChatSession(session, poolName, tz, fromId));
                }
                try {
                    conn = MySQLCommon.getConnection(poolName, tz);
                    sendUserUpdate(poolName, conn, new User(fromId, null, true, conn));
                } catch (Exception ex) {
                    Logger.getLogger(ChatWebSocket.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    MySQLCommon.closeConnection(conn);
                }
                return;
            }
            try {
                ChatSession cs = findSession(session);
                cs.pingReceived();
                conn = MySQLCommon.getConnection(cs.getPoolName(), cs.getTz());
                switch (c) {
                    case "MSG":
                        int toId = im.getInt();
                        int attId = im.getInt();
                        String msg = im.getUTF();
                        long token = im.getLong();

                        MySQLQuery insertQ = new MySQLQuery("INSERT INTO sys_chat_msg SET dt = NOW(), from_id = " + cs.getEmpId() + ", to_id = " + toId + ", content = ?1, attach_id = ?2");
                        insertQ.setParam(1, msg);
                        insertQ.setParam(2, attId);
                        int msgId = insertQ.executeInsert(conn);
                        if (attId != -1) {
                            new MySQLQuery("UPDATE bfile SET owner_id = " + msgId + " WHERE id = " + attId).executeUpdate(conn);
                        }

                        ChatSession[] toThreads = findSessions(cs.getPoolName(), toId);
                        if (toThreads.length > 0) {
                            User u = new User(cs.getEmpId(), toId, true, conn);
                            for (ChatSession csTo : toThreads) {
                                try {
                                    Message m = new Message();
                                    m.addUTF("USER");
                                    u.addToMessage(m);
                                    csTo.getSession().getAsyncRemote().sendText(m.toJsonString());
                                } catch (Exception ex) {
                                    Logger.getLogger(ChatWebSocket.class.getName()).log(Level.INFO, null, ex);
                                }
                            }
                        }
                        sendConfirm(token, cs.getSession());
                        break;
                    case "USERS":
                        Object[][] users = new MySQLQuery("SELECT e.id FROM employee e WHERE e.login IS NOT NULL AND e.active = 1 AND e.id <> " + cs.getEmpId()).getRecords(conn);
                        Message m = new Message();
                        m.addUTF("USERS");
                        m.addInt(users.length);

                        for (Object[] userRow : users) {
                            Integer rowId = MySQLQuery.getAsInteger(userRow[0]);
                            new User(rowId, cs.getEmpId(), hasSessions(cs.getPoolName(), rowId), conn).addToMessage(m);
                        }
                        cs.getSession().getAsyncRemote().sendText(m.toJsonString());

                        break;
                    case "LOGOUT":
                        removeSession(cs, conn);
                        break;
                    case "READ":
                        int fromIdRead = im.getInt();
                        Object[][] msgData = new MySQLQuery("SELECT DISTINCT * FROM ("
                                + "(SELECT content, dt, attach_id, false FROM sys_chat_msg WHERE seen = 0 AND from_id = " + fromIdRead + " AND to_id = " + cs.getEmpId() + " ORDER BY dt) "
                                + "UNION "
                                + "(SELECT content, dt, attach_id, false FROM sys_chat_msg WHERE from_id = " + fromIdRead + " AND to_id = " + cs.getEmpId() + " ORDER BY dt DESC LIMIT 0, 10) "
                                + "UNION "
                                + "(SELECT content, dt, attach_id, true  FROM sys_chat_msg WHERE from_id = " + cs.getEmpId() + " AND to_id = " + fromIdRead + " ORDER BY dt DESC LIMIT 0, 10) "
                                + ") AS l ORDER BY dt ASC "
                        ).getRecords(conn);
                        Message om = new Message();
                        om.addUTF("MSGS");
                        om.addInt(msgData.length);
                        om.addInt(fromIdRead);

                        for (Object[] msgRow : msgData) {
                            String content = MySQLQuery.getAsString(msgRow[0]);
                            Date dt = MySQLQuery.getAsDate(msgRow[1]);
                            int attachId = MySQLQuery.getAsInteger(msgRow[2]);
                            boolean mine = MySQLQuery.getAsBoolean(msgRow[3]);

                            om.addUTF(content);
                            om.addDate(dt);
                            om.addInt(attachId);
                            om.addBoolean(mine);
                        }
                        session.getAsyncRemote().sendText(om.toJsonString());
                        new MySQLQuery("UPDATE sys_chat_msg SET seen = 1 WHERE from_id = " + fromIdRead + " AND to_id = " + cs.getEmpId() + " ").executeUpdate(conn);

                        break;
                    case "INFO":
                        Message infoMsg = new Message();
                        infoMsg.addUTF("INFO");
                        infoMsg.addUTF(getInfo());
                        session.getAsyncRemote().sendText(infoMsg.toJsonString());
                        break;
                    case "PING":
                        //no se contesta, sino se forma un ciclo infinito
                        break;
                    default:
                        System.out.println("Comando Desconocido: " + c);
                        break;

                }
            } catch (Exception ex) {
                Logger.getLogger(ChatWebSocket.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                MySQLCommon.closeConnection(conn);
            }
        } catch (Exception ex) {
            Logger.getLogger(ChatWebSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String getInfo() {
        StringBuilder sb = new StringBuilder();
        String[] poolNames = getPoolNames();
        for (String poolName : poolNames) {
            sb.append(poolName).append(": ").append(getSessions(poolName).length);
        }
        return sb.toString();
    }

    private void sendConfirm(long token, Session sess) throws IOException {
        Message msg = new Message();
        msg.addUTF("CONF");
        msg.addLong(token);
        sess.getAsyncRemote().sendText(msg.toJsonString());
    }

    @OnOpen
    public void onOpen(Session peer) {
        //System.out.println("onOpen");
    }

    @OnClose
    public void onClose(Session peer) {
        //System.out.println("onClose");
    }

    @OnError
    public void onError(Throwable t) {
        //System.out.println("onClose");
    }

}
