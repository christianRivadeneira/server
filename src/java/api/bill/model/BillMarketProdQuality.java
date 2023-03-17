package api.bill.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class BillMarketProdQuality extends BaseModel<BillMarketProdQuality> {
//inicio zona de reemplazo

    public int marketId;
    public String label;
    public String value;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "market_id",
            "label",
            "value"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, marketId);
        q.setParam(2, label);
        q.setParam(3, value);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        marketId = MySQLQuery.getAsInteger(row[0]);
        label = MySQLQuery.getAsString(row[1]);
        value = MySQLQuery.getAsString(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_market_prod_quality";
    }

    public static String getSelFlds(String alias) {
        return new BillMarketProdQuality().getSelFldsForAlias(alias);
    }

    public static List<BillMarketProdQuality> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillMarketProdQuality().getListFromQuery(q, conn);
    }

    public static List<BillMarketProdQuality> getList(Params p, Connection conn) throws Exception {
        return new BillMarketProdQuality().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillMarketProdQuality().deleteById(id, conn);
    }

    public static List<BillMarketProdQuality> getAll(Connection conn) throws Exception {
        return new BillMarketProdQuality().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<BillMarketProdQuality> getByMarket(int marketId, Connection conn) throws Exception {
        return getList(new MySQLQuery(
                "SELECT " + getSelFlds("i") + " FROM sigma.bill_market_prod_quality i "
                + "WHERE i.market_id = ?1 ORDER BY i.label").setParam(1, marketId), conn);
    }

}
