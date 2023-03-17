package utilities;

import java.sql.Connection;
import java.util.Date;

public final class SysTask {

    private String db;

//inicio zona de reemplazo
    public int id;
    public String className;
    public int employeeId;
    public Date begin;
    public Date end;
    public String exClass;
    public String exMsg;

    public SysTask() {

    }

    public SysTask(Class c, String extra, int empId, String db, Connection conn) throws Exception {
        begin = new Date();
        className = c.getName() + (extra != null ? " " + extra : "");
        employeeId = empId;
        this.db = db;
        id = insert(this, conn);
    }

    public void success(Connection conn) throws Exception {
        end = new Date();
        update(this, conn);
    }

    public void error(Exception ex, Connection conn) throws Exception {
        end = new Date();
        exClass = ex.getClass().getName();
        StringBuilder sb = new StringBuilder();
        if (ex.getMessage() != null) {
            sb.append(ex.getMessage());
            sb.append("\r\n");
        }
        StackTraceElement[] traces = ex.getStackTrace();
        for (StackTraceElement trace : traces) {
            sb.append(trace.toString());
            sb.append("\r\n");
            if (sb.length() > 4095) {
                break;
            }
        }
        sb.setLength(4090);
        sb.trimToSize();
        exMsg = sb.toString();
        update(this, conn);
    }

    public SysTask(Class c, int empId, String db, Connection conn) throws Exception {
        this(c, null, empId, db, conn);
    }

    public SysTask(Class c, int empId, Connection conn) throws Exception {
        this(c, null, empId, null, conn);
    }

    public SysTask(Class c, String extra, int empId, Connection conn) throws Exception {
        this(c, extra, empId, null, conn);
    }

    private static final String SET_FLDS = "sys_task SET "
            + "`class` = ?1, "
            + "`employee_id` = ?2, "
            + "`begin` = ?3, "
            + "`end` = ?4, "
            + "`ex_class` = ?5, "
            + "`ex_msg` = ?6";

    private static void setFields(SysTask obj, MySQLQuery q) {
        q.setParam(1, obj.className);
        q.setParam(2, obj.employeeId);
        q.setParam(3, obj.begin);
        q.setParam(4, obj.end);
        q.setParam(5, obj.exClass);
        q.setParam(6, obj.exMsg);

    }

    private int insert(SysTask pobj, Connection ep) throws Exception {
        SysTask obj = (SysTask) pobj;
        int nId = new MySQLQuery(SysTask.getInsertQuery(obj, db)).executeInsert(ep);
        obj.id = nId;
        return nId;
    }

    private void update(SysTask pobj, Connection ep) throws Exception {
        new MySQLQuery(SysTask.getUpdateQuery((SysTask) pobj, db)).executeUpdate(ep);
    }

    private static String getInsertQuery(SysTask obj, String db) {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + (db != null ? db + "." : "") + SET_FLDS);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    private static String getUpdateQuery(SysTask obj, String db) {
        MySQLQuery q = new MySQLQuery("UPDATE " + (db != null ? db + "." : "") + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }
}
