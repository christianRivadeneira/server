package api.ord.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class OrdPqrAnulCause extends BaseModel<OrdPqrAnulCause> {
//inicio zona de reemplazo

    public String type;
    public String description;
    public boolean cancelRepair;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "type",
            "description",
            "cancel_repair"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, type);
        q.setParam(2, description);
        q.setParam(3, cancelRepair);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        type = MySQLQuery.getAsString(row[0]);
        description = MySQLQuery.getAsString(row[1]);
        cancelRepair = MySQLQuery.getAsBoolean(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_pqr_anul_cause";
    }

    public static String getSelFlds(String alias) {
        return new OrdPqrAnulCause().getSelFldsForAlias(alias);
    }

    public static List<OrdPqrAnulCause> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdPqrAnulCause().getListFromQuery(q, conn);
    }

    public static List<OrdPqrAnulCause> getList(Params p, Connection conn) throws Exception {
        return new OrdPqrAnulCause().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdPqrAnulCause().deleteById(id, conn);
    }

    public static List<OrdPqrAnulCause> getAll(Connection conn) throws Exception {
        return new OrdPqrAnulCause().getAllList(conn);
    }

//fin zona de reemplazo
    
    public static List<OrdPqrAnulCause> getPqrAnulCauses(String type, Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " + getSelFlds("") + " FROM ord_pqr_anul_cause WHERE type = ?1 ORDER BY id ASC").setParam(1, type);
        return new OrdPqrAnulCause().getListFromQuery(q, conn);
    }

}