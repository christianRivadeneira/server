package printout.basics;


import java.sql.Connection;
import java.util.Date;
import utilities.MySQLQuery;

public class MtoChkVersion {
//inicio zona de reemplazo

    public Integer id;
    public Integer typeId;
    public Date since;
    public Boolean active;

    private static final String selFlds = "`type_id`, "
            + "`since`, "
            + "`active`";

    private static final String setFlds = "mto_chk_version SET "
            + "`type_id` = ?1, "
            + "`since` = ?2, "
            + "`active` = ?3";

    private void setFields(MtoChkVersion obj, MySQLQuery q) {
        q.setParam(1, obj.typeId);
        q.setParam(2, obj.since);
        q.setParam(3, obj.active);
    }

    public MtoChkVersion select(int id, Connection ep) throws Exception {
        MtoChkVersion obj = new MtoChkVersion();
        MySQLQuery q = new MySQLQuery("SELECT " + selFlds + " FROM mto_chk_version WHERE id = " + id);
        Object[] row = q.getRecord(ep);
        obj.typeId = (row[0] != null ? (Integer) row[0] : null);
        obj.since = (row[1] != null ? (Date) row[1] : null);
        obj.active = (row[2] != null ? (Boolean) row[2] : null);
        obj.id = id;
        return obj;
    }

//fin zona de reemplazo
    public int insert(MtoChkVersion obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + setFlds);
        setFields(obj, q);
        return q.executeInsert(ep);
    }

    public void update(MtoChkVersion obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("UPDATE " + setFlds + " WHERE id = " + obj.id);
        setFields(obj, q);
        q.executeUpdate(ep);
    }

    public void delete(int id, Connection ep) throws Exception {
        new MySQLQuery("DELETE FROM mto_chk_version WHERE id = " + id).executeDelete(ep);
    }
}
