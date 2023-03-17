package api.ess.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class EssPkgType extends BaseModel<EssPkgType> {
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
        return "ess_pkg_type";
    }

    public static String getSelFlds(String alias) {
        return new EssPkgType().getSelFldsForAlias(alias);
    }

    public static List<EssPkgType> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EssPkgType().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EssPkgType().deleteById(id, conn);
    }

    public static List<EssPkgType> getAll(Connection conn) throws Exception {
        return new EssPkgType().getAllList(conn);
    }

//fin zona de reemplazo
}
