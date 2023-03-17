package web.gps;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import utilities.MySQLQuery;

public class GpsLastCoord {
//inicio zona de reemplazo

    public int id;
    public BigDecimal latitude;
    public BigDecimal longitude;
    public int employeeId;
    public Date date;
    public Integer appId;
    public Integer sessionId;

    private static final String SEL_FLDS = "`latitude`, "
            + "`longitude`, "
            + "`employee_id`, "
            + "`date`, "
            + "`app_id`, "
            + "`session_id`";

    private static final String SET_FLDS = "gps_last_coord SET "
            + "`latitude` = ?1, "
            + "`longitude` = ?2, "
            + "`employee_id` = ?3, "
            + "`date` = ?4, "
            + "`app_id` = ?5, "
            + "`session_id` = ?6";

    private static void setFields(GpsLastCoord obj, MySQLQuery q) {
        q.setParam(1, obj.latitude);
        q.setParam(2, obj.longitude);
        q.setParam(3, obj.employeeId);
        q.setParam(4, obj.date);
        q.setParam(5, obj.appId);
        q.setParam(6, obj.sessionId);

    }

    public static GpsLastCoord getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        GpsLastCoord obj = new GpsLastCoord();
        obj.latitude = MySQLQuery.getAsBigDecimal(row[0], false);
        obj.longitude = MySQLQuery.getAsBigDecimal(row[1], false);
        obj.employeeId = MySQLQuery.getAsInteger(row[2]);
        obj.date = MySQLQuery.getAsDate(row[3]);
        obj.appId = MySQLQuery.getAsInteger(row[4]);
        obj.sessionId = MySQLQuery.getAsInteger(row[5]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public GpsLastCoord select(int id, Connection ep) throws Exception {
        return GpsLastCoord.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(ep));
    }

    public GpsLastCoord selectByEmployeeId(int empId, Connection ep) throws Exception {
        return GpsLastCoord.getFromRow(new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM gps_last_coord WHERE employee_id = " + empId).getRecord(ep));
    }

    public int insert(GpsLastCoord pobj, Connection ep) throws Exception {
        GpsLastCoord obj = (GpsLastCoord) pobj;
        int nId = new MySQLQuery(GpsLastCoord.getInsertQuery(obj)).executeInsert(ep);
        obj.id = nId;
        return nId;
    }

    public void update(GpsLastCoord pobj, Connection ep) throws Exception {
        new MySQLQuery(GpsLastCoord.getUpdateQuery((GpsLastCoord) pobj)).executeUpdate(ep);
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM gps_last_coord WHERE id = " + id;
    }

    public static String getInsertQuery(GpsLastCoord obj) {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public static String getUpdateQuery(GpsLastCoord obj) {
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public static void sync(GpsCoordinate coord, Connection ep) throws Exception {
        //" AND `app_id` = " + coord.appId + " AND `session_id` = " + coord.sessionId
        new MySQLQuery("DELETE FROM gps_last_coord WHERE `employee_id` = " + coord.employeeId).executeDelete(ep);
        GpsLastCoord lc = new GpsLastCoord();
        lc.appId = coord.appId;
        lc.date = coord.date;
        lc.employeeId = coord.employeeId;
        lc.latitude = coord.latitude;
        lc.longitude = coord.longitude;
        lc.sessionId = coord.sessionId;
        lc.insert(lc, ep);
    }
}
