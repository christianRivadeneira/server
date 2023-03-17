package printout.basics;

import java.sql.Connection;
import java.util.Date;
import utilities.MySQLQuery;

public class MtoCall {
//inicio zona de reemplazo

    public Integer id;
    public Date fromDate;
    public Date toDate;
    public Integer revision;

    private static final String selFlds = "`from_date`, "
            + "`to_date`, "
            + "`revision`";

    private static final String setFlds = "mto_call SET "
            + "`from_date` = ?1, "
            + "`to_date` = ?2, "
            + "`revision` = ?3";

    private void setFields(MtoCall obj, MySQLQuery q) {
        q.setParam(1, obj.fromDate);
        q.setParam(2, obj.toDate);
        q.setParam(3, obj.revision);
    }

    public MtoCall select(int id, Connection ep) throws Exception {
        MtoCall obj = new MtoCall();
        MySQLQuery q = new MySQLQuery("SELECT " + selFlds + " FROM mto_call WHERE id = " + id);
        Object[] row = q.getRecord(ep);
        obj.fromDate = (row[0] != null ? (Date) row[0] : null);
        obj.toDate = (row[1] != null ? (Date) row[1] : null);
        obj.revision = (row[2] != null ? (Integer) row[2] : null);
        obj.id = id;
        return obj;
    }

    //fin zona de reemplazo
    public int insert(MtoCall obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + setFlds);
        setFields(obj, q);
        return q.executeInsert(ep);
    }

    public void update(MtoCall obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("UPDATE " + setFlds + " WHERE id = " + obj.id);
        setFields(obj, q);
        q.executeUpdate(ep);
    }

    public void delete(int id, Connection ep) throws Exception {
        new MySQLQuery("DELETE FROM mto_call WHERE id = " + id).executeDelete(ep);
    }
}