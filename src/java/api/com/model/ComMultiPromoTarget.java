package api.com.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class ComMultiPromoTarget extends BaseModel<ComMultiPromoTarget> {
//inicio zona de reemplazo

    public int promoId;
    public Integer indexId;
    public Integer tankClientId;
    public int saleCount;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "promo_id",
            "index_id",
            "tank_client_id",
            "sale_count"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, promoId);
        q.setParam(2, indexId);
        q.setParam(3, tankClientId);
        q.setParam(4, saleCount);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        promoId = MySQLQuery.getAsInteger(row[0]);
        indexId = MySQLQuery.getAsInteger(row[1]);
        tankClientId = MySQLQuery.getAsInteger(row[2]);
        saleCount = MySQLQuery.getAsInteger(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "com_multi_promo_target";
    }

    public static String getSelFlds(String alias) {
        return new ComMultiPromoTarget().getSelFldsForAlias(alias);
    }

    public static List<ComMultiPromoTarget> getList(MySQLQuery q, Connection conn) throws Exception {
        return new ComMultiPromoTarget().getListFromQuery(q, conn);
    }

    public static List<ComMultiPromoTarget> getList(Params p, Connection conn) throws Exception {
        return new ComMultiPromoTarget().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new ComMultiPromoTarget().deleteById(id, conn);
    }

    public static List<ComMultiPromoTarget> getAll(Connection conn) throws Exception {
        return new ComMultiPromoTarget().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<ComInstPromoTarget> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}