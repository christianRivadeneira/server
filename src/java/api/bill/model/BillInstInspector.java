package api.bill.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class BillInstInspector extends BaseModel<BillInstInspector> {
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
        return "bill_inst_inspector";
    }

    public static String getSelFlds(String alias) {
        return new BillInstInspector().getSelFldsForAlias(alias);
    }

    public static List<BillInstInspector> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillInstInspector().getListFromQuery(q, conn);
    }

    public static List<BillInstInspector> getList(Params p, Connection conn) throws Exception {
        return new BillInstInspector().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillInstInspector().deleteById(id, conn);
    }

    public static List<BillInstInspector> getAll(Connection conn) throws Exception {
        return new BillInstInspector().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<BillInstInspector> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}