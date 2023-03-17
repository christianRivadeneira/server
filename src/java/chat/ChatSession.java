package chat;

import java.io.IOException;
import java.sql.Connection;
import javax.websocket.Session;
import service.MySQL.MySQLCommon;

public class ChatSession {

    private final Session sess;
    private final int empId;
    private int pings = 0;
    private final String poolName;
    private final String tz;

    public ChatSession(Session sess, String poolName, String tz, int id) {
        this.sess = sess;
        this.empId = id;
        this.poolName = poolName;
        this.tz = tz;
    }

    public void pingSent() {
        pings++;
    }

    public void pingReceived() {
        pings = 0;
    }

    public Session getSession() {
        return sess;
    }

    public boolean isIdle() {
        return !sess.isOpen() || pings > 2;
    }

    public void closeWebSocketSession() throws IOException {
        sess.close();
    }

    public String getPoolName() {
        return poolName;
    }

    public String getTz() {
        return tz;
    }

    public Connection getConnection() throws Exception {
        return MySQLCommon.getConnection(poolName, tz);
    }

    public int getEmpId() {
        return empId;
    }
}
