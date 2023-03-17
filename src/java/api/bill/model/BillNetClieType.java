package api.bill.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class BillNetClieType extends BaseModel<BillNetClieType> {
//inicio zona de reemplazo

    public String name;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        active = MySQLQuery.getAsBoolean(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_net_clie_type";
    }

    public static String getSelFlds(String alias) {
        return new BillNetClieType().getSelFldsForAlias(alias);
    }

    public static List<BillNetClieType> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillNetClieType().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillNetClieType().deleteById(id, conn);
    }

    public static List<BillNetClieType> getAll(Connection conn) throws Exception {
        return new BillNetClieType().getAllList(conn);
    }

//fin zona de reemplazo
}