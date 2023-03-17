package api.bill.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class BillPriceSpan extends BaseModel<BillPriceSpan> {
//inicio zona de reemplazo

    public int lstId;
    public int spanId;
    public BigDecimal price;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "lst_id",
            "span_id",
            "price"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, lstId);
        q.setParam(2, spanId);
        q.setParam(3, price);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        lstId = MySQLQuery.getAsInteger(row[0]);
        spanId = MySQLQuery.getAsInteger(row[1]);
        price = MySQLQuery.getAsBigDecimal(row[2], false);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_price_span";
    }

    public static String getSelFlds(String alias) {
        return new BillPriceSpan().getSelFldsForAlias(alias);
    }

    public static List<BillPriceSpan> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillPriceSpan().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillPriceSpan().deleteById(id, conn);
    }

    public static List<BillPriceSpan> getAll(Connection conn) throws Exception {
        return new BillPriceSpan().getAllList(conn);
    }

//fin zona de reemplazo
    public static String getPriceListName(Connection conn, int listId) throws Exception {
        return new MySQLQuery("select name from bill_price_list WHERE id = " + listId).getAsString(conn);
    }

    public static Map<Integer, String> getPriceListNames(Connection conn) throws Exception {
        Map<Integer, String> names = new HashMap<>();
        Object[][] listData = new MySQLQuery("select id, name from bill_price_list;").getRecords(conn);
        for (Object[] row : listData) {
            names.put(MySQLQuery.getAsInteger(row[0]), MySQLQuery.getAsString(row[1]));
        }
        return names;
    }

    public static Integer getListId(Connection conn, int spanId, int clientId) throws Exception {
        return new MySQLQuery("SELECT cl1.list_id FROM bill_client_list cl1 WHERE cl1.client_id = " + clientId + " AND cl1.span_id = (SELECT MAX(span_id) FROM bill_client_list cl WHERE cl.span_id <= " + spanId + " AND cl.client_id = " + clientId + ")").getAsInteger(conn);
    }

    public static BigDecimal getPrice(Connection conn, int spanId, int listId) throws Exception {
        BigDecimal p = new MySQLQuery("SELECT ps.price "
                + "FROM bill_price_span AS ps "
                + "WHERE ps.lst_id = " + listId + " AND ps.span_id = " + spanId).getAsBigDecimal(conn, false);

        if (p == null) {
            throw new Exception("No se ha definido el precio para la lista en el periodo.");
        }
        return p;
    }

    public static Map<Integer, BigDecimal> getPricesMap(Connection conn, int spanId) throws Exception {
        Object[][] lists = new MySQLQuery("SELECT DISTINCT cl.list_id FROM "
                + "bill_client_list cl  "
                + "INNER JOIN  "
                + "(SELECT "
                + "cl.client_id, "
                + "max(cl.span_id) as span_id "
                + "FROM "
                + "bill_client_list AS cl "
                + "INNER JOIN bill_client_tank AS c ON cl.client_id = c.id "
                + "WHERE "
                + "c.active = 1 AND cl.span_id <= " + spanId + " "
                + "GROUP BY "
                + "cl.client_id) AS l "
                + "ON cl.client_id = l.client_id AND cl.span_id = l.span_id;").getRecords(conn);

        Map<Integer, BigDecimal> prices = new HashMap<>();
        Object[][] pricesData = new MySQLQuery("SELECT ps.lst_id, ps.price "
                + "FROM bill_price_span AS ps "
                + "WHERE ps.span_id = " + spanId).getRecords(conn);

        for (Object[] priceRow : pricesData) {
            Object[] row = (Object[]) priceRow;
            prices.put(MySQLQuery.getAsInteger(row[0]), MySQLQuery.getAsBigDecimal(row[1], true));
        }
        for (Object[] listRow : lists) {
            int listId = MySQLQuery.getAsInteger(listRow[0]);
            if (!prices.containsKey(listId)) {
                throw new Exception("Las listas de precios no están completas.");
            }
        }
        return prices;
    }

}
