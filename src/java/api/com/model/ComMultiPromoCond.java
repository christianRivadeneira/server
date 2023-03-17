package api.com.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class ComMultiPromoCond extends BaseModel<ComMultiPromoCond> {
//inicio zona de reemplazo

    public int promoId;
    public int value;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "promo_id",
            "value"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, promoId);
        q.setParam(2, value);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        promoId = MySQLQuery.getAsInteger(row[0]);
        value = MySQLQuery.getAsInteger(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "com_multi_promo_cond";
    }

    public static String getSelFlds(String alias) {
        return new ComMultiPromoCond().getSelFldsForAlias(alias);
    }

    public static List<ComMultiPromoCond> getList(MySQLQuery q, Connection conn) throws Exception {
        return new ComMultiPromoCond().getListFromQuery(q, conn);
    }

    public static List<ComMultiPromoCond> getList(Params p, Connection conn) throws Exception {
        return new ComMultiPromoCond().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new ComMultiPromoCond().deleteById(id, conn);
    }

    public static List<ComMultiPromoCond> getAll(Connection conn) throws Exception {
        return new ComMultiPromoCond().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<ComInstPromoCond> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}