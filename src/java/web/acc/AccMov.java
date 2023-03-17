package web.acc;

import java.math.BigDecimal;
import java.sql.Connection;
import utilities.MySQLQuery;

public class AccMov {
//inicio zona de reemplazo

    public int id;
    public int docId;
    public int accId;
    public String type;
    public BigDecimal value;

    public AccMov() {

    }

    public AccMov(int docId, int accId, String type, BigDecimal value) {
        this.docId = docId;
        this.accId = accId;
        this.type = type;
        this.value = (type.equals("deb") ? value : value.multiply(new BigDecimal(-1)));
    }

    private static final String SEL_FLDS = "`doc_id`, "
            + "`acc_id`, "
            + "`type`, "
            + "`value`";

    private static final String SET_FLDS = "acc_mov SET "
            + "`doc_id` = ?1, "
            + "`acc_id` = ?2, "
            + "`type` = ?3, "
            + "`value` = ?4";

    private static void setFields(AccMov obj, MySQLQuery q) {
        q.setParam(1, obj.docId);
        q.setParam(2, obj.accId);
        q.setParam(3, obj.type);
        q.setParam(4, obj.value);

    }

    public static AccMov getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        AccMov obj = new AccMov();
        obj.docId = MySQLQuery.getAsInteger(row[0]);
        obj.accId = MySQLQuery.getAsInteger(row[1]);
        obj.type = MySQLQuery.getAsString(row[2]);
        obj.value = MySQLQuery.getAsBigDecimal(row[3], false);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo

    /*
    public static AccMov[] getAll(Connection ep) throws Exception {
        Object[][] data = new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM acc_mov").getRecords(ep);
        AccMov[] rta = new AccMov[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = getFromRow(data[i]);            
        }
        return rta;
    }*/
    public static AccMov select(int id, Connection ep) throws Exception {
        return AccMov.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(ep));
    }

    public static int insert(AccMov pobj, Connection ep) throws Exception {
        AccMov obj = (AccMov) pobj;
        int nId = new MySQLQuery(AccMov.getInsertQuery(obj)).executeInsert(ep);
        obj.id = nId;
        return nId;
    }

    public void update(AccMov pobj, Connection ep) throws Exception {
        new MySQLQuery(AccMov.getUpdateQuery((AccMov) pobj)).executeUpdate(ep);
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM acc_mov WHERE id = " + id;
    }

    public static String getInsertQuery(AccMov obj) {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public static String getUpdateQuery(AccMov obj) {
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public void delete(int id, Connection ep) throws Exception {
        new MySQLQuery("DELETE FROM acc_mov WHERE id = " + id).executeDelete(ep);
    }

}
