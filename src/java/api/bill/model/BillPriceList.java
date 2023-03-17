package api.bill.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class BillPriceList extends BaseModel<BillPriceList> {
//inicio zona de reemplazo

    public String name;
    public boolean defaultOpt;
    public boolean active;
    public int maxFinanPayments;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "default_opt",
            "active",
            "max_finan_payments"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, defaultOpt);
        q.setParam(3, active);
        q.setParam(4, maxFinanPayments);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        defaultOpt = MySQLQuery.getAsBoolean(row[1]);
        active = MySQLQuery.getAsBoolean(row[2]);
        maxFinanPayments = MySQLQuery.getAsInteger(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_price_list";
    }

    public static String getSelFlds(String alias) {
        return new BillPriceList().getSelFldsForAlias(alias);
    }

    public static List<BillPriceList> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillPriceList().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillPriceList().deleteById(id, conn);
    }

    public static List<BillPriceList> getAll(Connection conn) throws Exception {
        return new BillPriceList().getAllList(conn);
    }

//fin zona de reemplazo
    public static String getUsedLists(Connection conn, Integer spanId) throws Exception {
        return new MySQLQuery("SELECT GROUP_CONCAT(CAST(list_id AS CHAR)) FROM  "
                + "(SELECT DISTINCT cl.list_id FROM "
                + "bill_client_list cl  "
                + "INNER JOIN  "
                + "(SELECT "
                + "cl.client_id, "
                + "max(cl.span_id) as span_id "
                + "FROM "
                + "bill_client_list AS cl "
                + "INNER JOIN bill_client_tank AS c ON cl.client_id = c.id "
                + "WHERE "
                + "c.active = 1 "
                + (spanId != null ? "AND cl.span_id <= " + spanId + " " : "")
                + "GROUP BY "
                + "cl.client_id) AS l "
                + "ON cl.client_id = l.client_id AND cl.span_id = l.span_id) AS l").getAsString(conn);
    }

}
