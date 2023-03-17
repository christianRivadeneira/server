package chat;

import java.sql.Connection;
import java.util.Date;
import java.util.GregorianCalendar;
import utilities.Dates;
import utilities.MySQLQuery;

public class User {

    public int id;
    public String name;
    public String job;
    public int messagesFrom;
    public boolean connected;
    public Date lastMsg;

    public User(int id, Integer threadOwnerId, boolean connected, Connection conn) throws Exception {
        Object[] userRow = new MySQLQuery("SELECT "
                + "e.id, "
                + "CONCAT(e.first_name,' ', e.last_name), "
                + "pp.`name` "
                + "FROM "
                + "employee AS e "
                + "LEFT JOIN per_employee AS pe ON e.per_employee_id = pe.id "
                + "LEFT JOIN per_contract AS pc ON pc.emp_id = pe.id AND pc.last = 1 "
                + "LEFT JOIN per_pos AS pp ON pc.pos_id = pp.id "
                + "WHERE e.login IS NOT NULL AND e.active = 1 AND e.id = " + id).getRecord(conn);
        this.id = MySQLQuery.getAsInteger(userRow[0]);
        this.name = MySQLQuery.getAsString(userRow[1]);
        this.job = userRow[2] != null ? MySQLQuery.getAsString(userRow[2]) : "";
        this.connected = connected;
        setThreadOwnerId(threadOwnerId, conn);
    }

    public final void setThreadOwnerId(Integer threadOwnerId, Connection conn) throws Exception {
        if (threadOwnerId != null) {
            lastMsg = new MySQLQuery("SELECT DATE(MAX(dt)) FROM sys_chat_msg WHERE (from_id = " + threadOwnerId + " AND to_id = " + id + ") OR (to_id = " + threadOwnerId + " AND from_id = " + id + ")").getAsDate(conn);
            messagesFrom = new MySQLQuery("SELECT COUNT(*) FROM sys_chat_msg m WHERE m.to_id = " + threadOwnerId + " AND m.from_id = " + id + " AND m.seen = 0 ").getAsInteger(conn);
        } else {
            lastMsg = Dates.trimDate(new GregorianCalendar(1970, 0, 1).getTime());
            messagesFrom = 0;
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getJob() {
        return job;
    }

    public int getMessages() {
        return messagesFrom;
    }

    public boolean isConnected() {
        return connected;
    }

    public void addToMessage(Message m) {
        m.addInt(id);
        m.addUTF(name);
        m.addUTF(job);
        m.addInt(messagesFrom);
        m.addDate(lastMsg != null ? lastMsg : Dates.trimDate(new GregorianCalendar(1970, 0, 1).getTime()));
        m.addBoolean(connected);
    }
}
