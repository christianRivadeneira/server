package api.bill.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class BillNetCheckType extends BaseModel<BillNetCheckType> {
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
        return "bill_inst_check_type";
    }

    public static String getSelFlds(String alias) {
        return new BillNetCheckType().getSelFldsForAlias(alias);
    }

    public static List<BillNetCheckType> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillNetCheckType().getListFromQuery(q, conn);
    }

    public static List<BillNetCheckType> getList(Params p, Connection conn) throws Exception {
        return new BillNetCheckType().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillNetCheckType().deleteById(id, conn);
    }

    public static List<BillNetCheckType> getAll(Connection conn) throws Exception {
        return new BillNetCheckType().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<BillNetCheckType> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}