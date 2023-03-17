package api.sys.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class Login extends BaseModel<Login> {
//inicio zona de reemplazo

    public int profileId;
    public int employeeId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "profile_id",
            "employee_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, profileId);
        q.setParam(2, employeeId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        profileId = MySQLQuery.getAsInteger(row[0]);
        employeeId = MySQLQuery.getAsInteger(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "login";
    }

    public static String getSelFlds(String alias) {
        return new Login().getSelFldsForAlias(alias);
    }

    public static List<Login> getList(MySQLQuery q, Connection conn) throws Exception {
        return new Login().getListFromQuery(q, conn);
    }

    public static List<Login> getList(Params p, Connection conn) throws Exception {
        return new Login().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new Login().deleteById(id, conn);
    }

    public static List<Login> getAll(Connection conn) throws Exception {
        return new Login().getAllList(conn);
    }

//fin zona de reemplazo
}
