package web.push;

import java.sql.Connection;
import utilities.MySQLQuery;

public class SysGcmToken {
//inicio zona de reemplazo

    public int id;
    public int appId;
    public String imei;
    public int empId;
    public String token;

    private static final String SEL_FLDS = "`app_id`, "
            + "`imei`, "
            + "`emp_id`, "
            + "`token`";

    private static final String SET_FLDS = "sys_gcm_token SET "
            + "`app_id` = ?1, "
            + "`imei` = ?2, "
            + "`emp_id` = ?3, "
            + "`token` = ?4";

    private static void setFields(SysGcmToken obj, MySQLQuery q) {
        q.setParam(1, obj.appId);
        q.setParam(2, obj.imei);
        q.setParam(3, obj.empId);
        q.setParam(4, obj.token);

    }

    public static SysGcmToken getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        SysGcmToken obj = new SysGcmToken();
        obj.appId = MySQLQuery.getAsInteger(row[0]);
        obj.imei = MySQLQuery.getAsString(row[1]);
        obj.empId = MySQLQuery.getAsInteger(row[2]);
        obj.token = MySQLQuery.getAsString(row[3]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public SysGcmToken select(int id, Connection ep) throws Exception {
        return SysGcmToken.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(ep));
    }

    public static SysGcmToken getByImei(int appId, String imei, Connection ep) throws Exception {
        return SysGcmToken.getFromRow(new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM sys_gcm_token WHERE imei = '" + imei + "' AND app_id = " + appId).getRecord(ep));
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM sys_gcm_token WHERE id = " + id;
    }

    public int insert(SysGcmToken pobj, Connection ep) throws Exception {
        return new MySQLQuery(SysGcmToken.getInsertQuery((SysGcmToken) pobj)).executeInsert(ep);
    }

    public static String getInsertQuery(SysGcmToken obj) {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public void update(SysGcmToken pobj, Connection ep) throws Exception {
        new MySQLQuery(SysGcmToken.getUpdateQuery((SysGcmToken) pobj)).executeUpdate(ep);
    }

    public static String getUpdateQuery(SysGcmToken obj) {
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public void delete(int id, Connection ep) throws Exception {
        new MySQLQuery("DELETE FROM sys_gcm_token WHERE id = " + id).executeDelete(ep);
    }
}
