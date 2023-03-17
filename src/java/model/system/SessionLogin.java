package model.system;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

public class SessionLogin {

    public String document;
    public String firstName;
    public String lastName;

//inicio zona de reemplazo
    public int id;
    public int employeeId;
    public Date beginTime;
    public Date lastReq;
    public Date endTime;
    public String sessionId;
    public String userIp;
    public String serverIp;
    public String type;
    public String extras;
    public String phone;
    public Integer appId;

    private static final String SEL_FLDS = "`employee_id`, "
            + "`begin_time`, "
            + "`last_req`, "
            + "`end_time`, "
            + "`session_id`, "
            + "`user_ip`, "
            + "`server_ip`, "
            + "`type`, "
            + "`extras`, "
            + "`phone`, "
            + "`app_id`";

    private static final String SET_FLDS = "session_login SET "
            + "`employee_id` = ?1, "
            + "`begin_time` = ?2, "
            + "`last_req` = ?3, "
            + "`end_time` = ?4, "
            + "`session_id` = ?5, "
            + "`user_ip` = ?6, "
            + "`server_ip` = ?7, "
            + "`type` = ?8, "
            + "`extras` = ?9, "
            + "`phone` = ?10, "
            + "`app_id` = ?11";

    private static void setFields(SessionLogin obj, MySQLQuery q) {
        q.setParam(1, obj.employeeId);
        q.setParam(2, obj.beginTime);
        q.setParam(3, obj.lastReq);
        q.setParam(4, obj.endTime);
        q.setParam(5, obj.sessionId);
        q.setParam(6, obj.userIp);
        q.setParam(7, obj.serverIp);
        q.setParam(8, obj.type);
        q.setParam(9, obj.extras);
        q.setParam(10, obj.phone);
        q.setParam(11, obj.appId);

    }

    public static SessionLogin getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        SessionLogin obj = new SessionLogin();
        obj.employeeId = MySQLQuery.getAsInteger(row[0]);
        obj.beginTime = MySQLQuery.getAsDate(row[1]);
        obj.lastReq = MySQLQuery.getAsDate(row[2]);
        obj.endTime = MySQLQuery.getAsDate(row[3]);
        obj.sessionId = MySQLQuery.getAsString(row[4]);
        obj.userIp = MySQLQuery.getAsString(row[5]);
        obj.serverIp = MySQLQuery.getAsString(row[6]);
        obj.type = MySQLQuery.getAsString(row[7]);
        obj.extras = MySQLQuery.getAsString(row[8]);
        obj.phone = MySQLQuery.getAsString(row[9]);
        obj.appId = MySQLQuery.getAsInteger(row[10]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public static SessionLogin validate(String sessionId) throws Exception {
        return validate(sessionId, "sigmads", null);
    }

    public static SessionLogin validate(String sessionId, String poolName, String tz) throws Exception {
        try (Connection conn = MySQLCommon.getConnection(poolName, tz)) {
            return validate(sessionId, conn, null);
        }
    }

    public static SessionLogin validate(String sessionId, EntityManager em) throws Exception, ClosedSessionException {
        try {
            if (sessionId == null || sessionId.isEmpty()) {
                throw new Exception("Error inesperado");
            }
            em.getTransaction().begin();
            List<Object[]> data = em.createNativeQuery("SELECT " + SEL_FLDS + ", id FROM session_login WHERE session_id = \"" + sessionId + "\";").getResultList();
            if (data == null || (data.isEmpty() || data.get(0).length == 0)) {
                throw new Exception("Error inesperado");
            }
            SessionLogin sl = SessionLogin.getFromRow(data.get(0));
            if (sl.endTime != null) {
                throw new ClosedSessionException();
            }

            Object[] empRow = ((List<Object[]>) em.createNativeQuery("SELECT document, first_name, last_name FROM employee WHERE id = " + sl.employeeId).getResultList()).get(0);
            sl.document = MySQLQuery.getAsString(empRow[0]);
            sl.firstName = MySQLQuery.getAsString(empRow[1]);
            sl.lastName = MySQLQuery.getAsString(empRow[2]);

            em.createNativeQuery("UPDATE session_login SET last_req = NOW() WHERE id = " + sl.id).executeUpdate();
            em.getTransaction().commit();
            return sl;
        } catch (Exception ex) {
            em.getTransaction().rollback();
            throw ex;
        }
    }

     
    public static SessionLogin validate(int sessionId, Connection conn, String dbName) throws AuthenticationException, ClosedSessionException {
        SessionLogin sl = null;
        try {
            sl = SessionLogin.getFromRow(new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM " + (dbName != null ? dbName + "." : "") + "session_login WHERE id = ?1;").setParam(1, sessionId).getRecord(conn));
        } catch (Exception ex) {
            throw new AuthenticationException(ex);
        }
        if (sl == null) {
            throw new AuthenticationException("Error inesperado");
        } else if (sl.endTime != null) {
            throw new ClosedSessionException();
        }
        try {
            Object[] empRow = new MySQLQuery("SELECT document, first_name, last_name FROM " + (dbName != null ? dbName + "." : "") + "employee WHERE id = " + sl.employeeId).getRecord(conn);
            sl.document = MySQLQuery.getAsString(empRow[0]);
            sl.firstName = MySQLQuery.getAsString(empRow[1]);
            sl.lastName = MySQLQuery.getAsString(empRow[2]);
            new MySQLQuery("UPDATE " + (dbName != null ? dbName + "." : "") + "session_login SET last_req = NOW() WHERE id = " + sl.id).executeUpdate(conn);
        } catch (Exception ex) {
            throw new AuthenticationException(ex);
        }
        return sl;
    }
    
    
    /**
     *
     * @param sessionId
     * @param conn
     * @param dbName Solo se usa cuando se llama desde facturación, debe ser el
     * nombre la BD sigma, puede ser NULL
     * @return
     * @throws Exception
     */
    public static SessionLogin validate(String sessionId, Connection conn, String dbName) throws AuthenticationException, ClosedSessionException {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new AuthenticationException("Error inesperado");
        }
        SessionLogin sl = null;
        try {
            sl = SessionLogin.getFromRow(new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM " + (dbName != null ? dbName + "." : "") + "session_login WHERE session_id = ?1;").setParam(1, sessionId).getRecord(conn));
        } catch (Exception ex) {
            throw new AuthenticationException(ex);
        }
        if (sl == null) {
            throw new AuthenticationException("Error inesperado");
        } else if (sl.endTime != null) {
            throw new ClosedSessionException();
        }
        try {
            Object[] empRow = new MySQLQuery("SELECT document, first_name, last_name FROM " + (dbName != null ? dbName + "." : "") + "employee WHERE id = " + sl.employeeId).getRecord(conn);
            sl.document = MySQLQuery.getAsString(empRow[0]);
            sl.firstName = MySQLQuery.getAsString(empRow[1]);
            sl.lastName = MySQLQuery.getAsString(empRow[2]);
            new MySQLQuery("UPDATE " + (dbName != null ? dbName + "." : "") + "session_login SET last_req = NOW() WHERE id = " + sl.id).executeUpdate(conn);
        } catch (Exception ex) {
            throw new AuthenticationException(ex);
        }
        return sl;
    }

    /**
     *
     * @param sessionId
     * @param conn
     * @return
     * @throws Exception
     */
    public static SessionLogin validate(String sessionId, Connection conn) throws Exception {
        return validate(sessionId, conn, null);
    }

    /*public SessionLogin select(int id, Connection ep) throws Exception {
        return SessionLogin.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(ep));
    }*/
    public int insert(SessionLogin pobj, Connection ep) throws Exception {
        SessionLogin obj = (SessionLogin) pobj;
        int nId = new MySQLQuery(SessionLogin.getInsertQuery(obj)).executeInsert(ep);
        obj.id = nId;
        return nId;
    }

    public void update(SessionLogin pobj, Connection ep) throws Exception {
        new MySQLQuery(SessionLogin.getUpdateQuery((SessionLogin) pobj)).executeUpdate(ep);
    }

    public static String getSelectQueryBySessionId(String id) {
        return "SELECT " + SEL_FLDS + ", id FROM session_login WHERE session_id = '" + id + "'";
    }

    public static String getInsertQuery(SessionLogin obj) {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public static String getUpdateQuery(SessionLogin obj) {
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

}
