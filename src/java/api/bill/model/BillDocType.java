package api.bill.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class BillDocType extends BaseModel<BillDocType> {
//inicio zona de reemplazo

    public String name;
    public String type;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "type"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, type);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        type = MySQLQuery.getAsString(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_doc_type";
    }

    public static String getSelFlds(String alias) {
        return new BillDocType().getSelFldsForAlias(alias);
    }

    public static List<BillDocType> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillDocType().getListFromQuery(q, conn);
    }

    public static List<BillDocType> getList(Params p, Connection conn) throws Exception {
        return new BillDocType().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillDocType().deleteById(id, conn);
    }

    public static List<BillDocType> getAll(Connection conn) throws Exception {
        return new BillDocType().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<BillDocType> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}
