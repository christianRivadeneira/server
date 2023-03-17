package web.marketing.pvs;

import utilities.MySQLQuery;
import java.sql.Connection;

public class TrkPVSaleWarning {
//inicio zona de reemplazo

    public int id;
    public int pvSaleId;
    public String warning;

    private static final String SEL_FLDS = "`sale_id`, "
            + "`warning`";

    private static final String SET_FLDS = "trk_pv_sale_warning SET "
            + "`pv_sale_id` = ?1, "
            + "`warning` = ?2";

    private static void setFields(TrkPVSaleWarning obj, MySQLQuery q) {
        q.setParam(1, obj.pvSaleId);
        q.setParam(2, obj.warning);
    }

    public static TrkPVSaleWarning getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        TrkPVSaleWarning obj = new TrkPVSaleWarning();
        obj.pvSaleId = MySQLQuery.getAsInteger(row[0]);
        obj.warning = MySQLQuery.getAsString(row[1]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

    public TrkPVSaleWarning select(int id, Connection conn) throws Exception {
        return TrkPVSaleWarning.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(conn));
    }

    public int insert(TrkPVSaleWarning obj, Connection conn) throws Exception {
        int nId = new MySQLQuery(TrkPVSaleWarning.getInsertQuery(obj)).executeInsert(conn);
        obj.id = nId;
        return nId;
    }

    public void update(TrkPVSaleWarning pobj, Connection conn) throws Exception {
        new MySQLQuery(TrkPVSaleWarning.getUpdateQuery(pobj)).executeUpdate(conn);
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM trk_sale_warning WHERE id = " + id;
    }

    public static String getInsertQuery(TrkPVSaleWarning obj) {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public static String getUpdateQuery(TrkPVSaleWarning obj) {
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public void delete(int id, Connection conn) throws Exception {
        new MySQLQuery("DELETE FROM trk_sale_warning WHERE id = " + id).executeDelete(conn);
    }
}
