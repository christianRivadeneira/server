package api.bill.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import utilities.MySQLQuery;

public class BillOdorant extends BaseModel<BillOdorant> {
//inicio zona de reemplazo

    public String name;
    public BigDecimal min;
    public BigDecimal max;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "min",
            "max"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, min);
        q.setParam(3, max);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        min = MySQLQuery.getAsBigDecimal(row[1], false);
        max = MySQLQuery.getAsBigDecimal(row[2], false);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_odorant";
    }

    public static String getSelFlds(String alias) {
        return new BillOdorant().getSelFldsForAlias(alias);
    }

    public static List<BillOdorant> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillOdorant().getListFromQuery(q, conn);
    }

    public static List<BillOdorant> getList(Params p, Connection conn) throws Exception {
        return new BillOdorant().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillOdorant().deleteById(id, conn);
    }

    public static List<BillOdorant> getAll(Connection conn) throws Exception {
        return new BillOdorant().getAllList(conn);
    }

//fin zona de reemplazo
    public static Map<Integer, BillOdorant> getAsMap(Connection conn) throws Exception {
        Map<Integer, BillOdorant> m = new HashMap<>();
        List<BillOdorant> lst = new BillOdorant().getAllList(conn);
        for (int i = 0; i < lst.size(); i++) {
            BillOdorant o = lst.get(i);
            m.put(o.id, o);
        }
        return m;
    }

    /*
    public static List<BillOdorant> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/
}
