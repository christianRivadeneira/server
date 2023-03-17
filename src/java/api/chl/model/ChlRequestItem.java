package api.chl.model;

import api.BaseModel;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class ChlRequestItem extends BaseModel<ChlRequestItem> {
//inicio zona de reemplazo

    public Integer itemId;
    public int amount;
    public String name;
    public Integer requestId;
    public String notes;
    public BigDecimal price;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "item_id",
            "amount",
            "name",
            "request_id",
            "notes",
            "price"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, itemId);
        q.setParam(2, amount);
        q.setParam(3, name);
        q.setParam(4, requestId);
        q.setParam(5, notes);
        q.setParam(6, price);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        itemId = MySQLQuery.getAsInteger(row[0]);
        amount = MySQLQuery.getAsInteger(row[1]);
        name = MySQLQuery.getAsString(row[2]);
        requestId = MySQLQuery.getAsInteger(row[3]);
        notes = MySQLQuery.getAsString(row[4]);
        price = MySQLQuery.getAsBigDecimal(row[5],true);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "chl_request_item";
    }

    public static String getSelFlds(String alias) {
        return new ChlRequestItem().getSelFldsForAlias(alias);
    }

    public static List<ChlRequestItem> getList(MySQLQuery q, Connection conn) throws Exception {
        return new ChlRequestItem().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new ChlRequestItem().deleteById(id, conn);
    }

    public static List<ChlRequestItem> getAll(Connection conn) throws Exception {
        return new ChlRequestItem().getAllList(conn);
    }

//fin zona de reemplazo

}
