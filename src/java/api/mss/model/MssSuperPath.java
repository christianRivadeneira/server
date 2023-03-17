package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MssSuperPath extends BaseModel<MssSuperPath> {
//inicio zona de reemplazo

    public String name;
    public String code;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "code",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, code);
        q.setParam(3, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        code = MySQLQuery.getAsString(row[1]);
        active = MySQLQuery.getAsBoolean(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_super_path";
    }

    public static String getSelFlds(String alias) {
        return new MssSuperPath().getSelFldsForAlias(alias);
    }

    public static List<MssSuperPath> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssSuperPath().getListFromQuery(q, conn);
    }

    public static List<MssSuperPath> getList(Params p, Connection conn) throws Exception {
        return new MssSuperPath().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssSuperPath().deleteById(id, conn);
    }

    public static List<MssSuperPath> getAll(Connection conn) throws Exception {
        return new MssSuperPath().getAllList(conn);
    }

//fin zona de reemplazo
}
