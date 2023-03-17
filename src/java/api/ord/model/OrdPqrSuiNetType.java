package api.ord.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class OrdPqrSuiNetType extends BaseModel<OrdPqrSuiNetType> {
//inicio zona de reemplazo

    public String code;
    public String name;
    public String respType;
    public int respMinutes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "code",
            "name",
            "resp_type",
            "resp_minutes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, code);
        q.setParam(2, name);
        q.setParam(3, respType);
        q.setParam(4, respMinutes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        code = MySQLQuery.getAsString(row[0]);
        name = MySQLQuery.getAsString(row[1]);
        respType = MySQLQuery.getAsString(row[2]);
        respMinutes = MySQLQuery.getAsInteger(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_pqr_sui_net_type";
    }

    public static String getSelFlds(String alias) {
        return new OrdPqrSuiNetType().getSelFldsForAlias(alias);
    }

    public static List<OrdPqrSuiNetType> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdPqrSuiNetType().getListFromQuery(q, conn);
    }

    public static List<OrdPqrSuiNetType> getList(Params p, Connection conn) throws Exception {
        return new OrdPqrSuiNetType().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdPqrSuiNetType().deleteById(id, conn);
    }

    public static List<OrdPqrSuiNetType> getAll(Connection conn) throws Exception {
        return new OrdPqrSuiNetType().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<OrdPqrSuiNetType> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}
