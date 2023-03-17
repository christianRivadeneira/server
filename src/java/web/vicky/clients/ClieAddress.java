package web.vicky.clients;

import java.sql.Connection;
import utilities.MySQLQuery;

public class ClieAddress {
//inicio zona de reemplazo

    public int id;
    public String address;
    public int usrId;
    public int cityId;
    public Integer neighId;
    public String notes;

    private static final String SEL_FLDS = "`address`, "
            + "`usr_id`, "
            + "`city_id`, "
            + "`neigh_id`, "
            + "`notes`";

    private static final String SET_FLDS = "clie_address SET "
            + "`address` = ?1, "
            + "`usr_id` = ?2, "
            + "`city_id` = ?3, "
            + "`neigh_id` = ?4, "
            + "`notes` = ?5";

    private static void setFields(ClieAddress obj, MySQLQuery q) {
        q.setParam(1, obj.address);
        q.setParam(2, obj.usrId);
        q.setParam(3, obj.cityId);
        q.setParam(4, obj.neighId);
        q.setParam(5, obj.notes);

    }

    public static ClieAddress getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        ClieAddress obj = new ClieAddress();
        obj.address = MySQLQuery.getAsString(row[0]);
        obj.usrId = MySQLQuery.getAsInteger(row[1]);
        obj.cityId = MySQLQuery.getAsInteger(row[2]);
        obj.neighId = MySQLQuery.getAsInteger(row[3]);
        obj.notes = MySQLQuery.getAsString(row[4]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public ClieAddress() {

    }

    public ClieAddress(int id, String address, int usrId, String notes) {
        this.id = id;
        this.address = address;
        this.usrId = usrId;
        this.notes = notes;
    }

    public static ClieAddress[] getByUsr(int usrId, Connection ep) throws Exception {
        Object[][] data = new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM clie_address WHERE usrId = " + usrId).getRecords(ep);
        ClieAddress[] rta = new ClieAddress[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = getFromRow(data[i]);
        }
        return rta;
    }

    public static ClieAddress select(int id, Connection conn) throws Exception {
        return getFromRow(new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM clie_address WHERE id = " + id).getRecord(conn));
    }

    public static int insert(ClieAddress pobj, Connection ep) throws Exception {
        ClieAddress obj = (ClieAddress) pobj;
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.executeInsert(ep);
    }

    public static void update(ClieAddress pobj, Connection ep) throws Exception {
        ClieAddress obj = (ClieAddress) pobj;
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        q.executeUpdate(ep);
    }

    public static void delete(int id, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("DELETE FROM clie_address WHERE id = " + id);
        q.executeDelete(ep);
    }

}
