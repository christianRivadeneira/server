package api.per.model;

import java.sql.Connection;
import java.util.Date;
import utilities.Dates;
import utilities.MySQLQuery;

public class PerGateSpan {

    //Para efectos de la api. Por fuera de la zona de reemplazo
    public boolean hasProfile;
    public boolean holy;
    public boolean vaca;
    public boolean hasEvents;
    //Para efectos de la api. Por fuera de la zona de reemplazo

    //inicio zona de reemplazo
    public Integer id;
    public Integer empId;
    public Date eventDay;
    public Date begHour;
    public Date endHour;
    public String type;
    public String notes;
    public Integer expOfficeId;

    private static final String selFlds
            = "`emp_id`, "
            + "`event_day`, "
            + "`beg_hour`, "
            + "`end_hour`, "
            + "`type`, "
            + "`notes`, "
            + "`exp_office_id`";

    private static final String setFlds = "per_gate_span SET "
            + "`emp_id` = ?1, "
            + "`event_day` = ?2, "
            + "`beg_hour` = ?3, "
            + "`end_hour` = ?4, "
            + "`type` = ?5, "
            + "`notes` = ?6, "
            + "`exp_office_id` = ?7";

    private static final String toStrFlds = ")";

    private void setFields(PerGateSpan obj, MySQLQuery q) {
        q.setParam(1, obj.empId);
        q.setParam(2, obj.eventDay);
        q.setParam(3, obj.begHour);
        q.setParam(4, obj.endHour);
        q.setParam(5, obj.type);
        q.setParam(6, obj.notes);
        q.setParam(7, obj.expOfficeId);

    }

    public PerGateSpan select(int id, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " + selFlds + ", id FROM per_gate_span WHERE id = " + id);
        return getFromRow(q.getRecord(ep));
    }

    public static PerGateSpan select(int empId, String type, Date evDate, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " + selFlds + ", id FROM per_gate_span WHERE emp_id = " + empId + " AND event_day = ?1 AND type = '" + type + "' AND end_hour IS NULL");
        q.setParam(1, Dates.trimDate(evDate));
        Object[] row = q.getRecord(ep);
        if (row != null) {
            return getFromRow(row);
        } else {
            return null;
        }
    }

    public static PerGateSpan getFromRow(Object[] row) {
        PerGateSpan obj = new PerGateSpan();
        obj.empId = (row[0] != null ? (Integer) row[0] : null);
        obj.eventDay = (row[1] != null ? (Date) row[1] : null);
        obj.begHour = (row[2] != null ? (Date) row[2] : null);
        obj.endHour = (row[3] != null ? (Date) row[3] : null);
        obj.type = (row[4] != null ? row[4].toString() : null);
        obj.notes = (row[5] != null ? row[5].toString() : null);
        obj.expOfficeId = MySQLQuery.getAsInteger(row[6]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public int insert(PerGateSpan obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + setFlds);
        setFields(obj, q);
        return q.executeInsert(ep);
    }

    public String getInsertString(PerGateSpan obj) throws Exception {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + setFlds);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public void update(PerGateSpan obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("UPDATE " + setFlds + " WHERE id = " + obj.id);
        setFields(obj, q);
        q.executeUpdate(ep);
    }

    public String getUpdateString(PerGateSpan obj) throws Exception {
        MySQLQuery q = new MySQLQuery("UPDATE " + setFlds + " WHERE id = " + obj.id);
        setFields(obj, q);
        return q.getQuery();
    }

    public void delete(int id, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("DELETE FROM per_gate_span WHERE id = " + id);
        q.executeDelete(ep);
    }

    public String toString(int id, Connection ep) throws Exception {
        return new MySQLQuery("SELECT " + toStrFlds + " FROM per_gate_span WHERE id = " + id).getRecord(ep)[0].toString();
    }
}
