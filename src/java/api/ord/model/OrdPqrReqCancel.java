package api.ord.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class OrdPqrReqCancel extends BaseModel<OrdPqrReqCancel> {
//inicio zona de reemplazo

    public String name;
    public Boolean active;
    public String type;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "active",
            "type"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, active);
        q.setParam(3, type);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        active = MySQLQuery.getAsBoolean(row[1]);
        type = MySQLQuery.getAsString(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_pqr_req_cancel";
    }

    public static String getSelFlds(String alias) {
        return new OrdPqrReqCancel().getSelFldsForAlias(alias);
    }

    public static List<OrdPqrReqCancel> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdPqrReqCancel().getListFromQuery(q, conn);
    }

    public static List<OrdPqrReqCancel> getList(Params p, Connection conn) throws Exception {
        return new OrdPqrReqCancel().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdPqrReqCancel().deleteById(id, conn);
    }

    public static List<OrdPqrReqCancel> getAll(Connection conn) throws Exception {
        return new OrdPqrReqCancel().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<OrdPqrReqCancel> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}
