package api.smb.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class Cause extends BaseModel<Cause> {
//inicio zona de reemplazo

    public String description;
    public String kind;
    public boolean defaultOp;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "description",
            "kind",
            "default_op"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, description);
        q.setParam(2, kind);
        q.setParam(3, defaultOp);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        description = MySQLQuery.getAsString(row[0]);
        kind = MySQLQuery.getAsString(row[1]);
        defaultOp = MySQLQuery.getAsBoolean(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "cause";
    }

    public static String getSelFlds(String alias) {
        return new Cause().getSelFldsForAlias(alias);
    }

    public static List<Cause> getList(MySQLQuery q, Connection conn) throws Exception {
        return new Cause().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new Cause().deleteById(id, conn);
    }

    public static List<Cause> getAll(Connection conn) throws Exception {
        return new Cause().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<Cause> getByType(Connection conn, String type) throws Exception {        
        Params p = new Params("kind", type);
        p.sort("description", Params.ASC);        
        return  new Cause().getListFromParams(p, conn);
    }

}
