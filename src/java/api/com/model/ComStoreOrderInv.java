package api.com.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class ComStoreOrderInv extends BaseModel<ComStoreOrderInv> {
//inicio zona de reemplazo

    public int orderId;
    public int cylTypeId;
    public int amount;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "order_id",
            "cyl_type_id",
            "amount"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, orderId);
        q.setParam(2, cylTypeId);
        q.setParam(3, amount);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        orderId = MySQLQuery.getAsInteger(row[0]);
        cylTypeId = MySQLQuery.getAsInteger(row[1]);
        amount = MySQLQuery.getAsInteger(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "com_store_order_inv";
    }

    public static String getSelFlds(String alias) {
        return new ComStoreOrderInv().getSelFldsForAlias(alias);
    }

    public static List<ComStoreOrderInv> getList(MySQLQuery q, Connection conn) throws Exception {
        return new ComStoreOrderInv().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new ComStoreOrderInv().deleteById(id, conn);
    }

    public static List<ComStoreOrderInv> getAll(Connection conn) throws Exception {
        return new ComStoreOrderInv().getAllList(conn);
    }

//fin zona de reemplazo

}
