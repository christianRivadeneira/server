package api.ess.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class EssBuildAdmin extends BaseModel<EssBuildAdmin> {
//inicio zona de reemplazo

    public int buildId;
    public int personId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "build_id",
            "person_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, buildId);
        q.setParam(2, personId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        buildId = MySQLQuery.getAsInteger(row[0]);
        personId = MySQLQuery.getAsInteger(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ess_build_admin";
    }

    public static String getSelFlds(String alias) {
        return new EssBuildAdmin().getSelFldsForAlias(alias);
    }

    public static List<EssBuildAdmin> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EssBuildAdmin().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EssBuildAdmin().deleteById(id, conn);
    }

    public static List<EssBuildAdmin> getAll(Connection conn) throws Exception {
        return new EssBuildAdmin().getAllList(conn);
    }

//fin zona de reemplazo
}
