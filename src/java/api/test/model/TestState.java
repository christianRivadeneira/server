package api.test.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class TestState extends BaseModel<TestState> {
//inicio zona de reemplazo

    public int countryId;
    public String name;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "country_id",
            "name",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, countryId);
        q.setParam(2, name);
        q.setParam(3, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        countryId = MySQLQuery.getAsInteger(row[0]);
        name = MySQLQuery.getAsString(row[1]);
        active = MySQLQuery.getAsBoolean(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "test_state";
    }

    public static String getSelFlds(String alias) {
        return new TestState().getSelFldsForAlias(alias);
    }

    public static List<TestState> getList(MySQLQuery q, Connection conn) throws Exception {
        return new TestState().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new TestState().deleteById(id, conn);
    }

    public static List<TestState> getAll(Connection conn) throws Exception {
        return new TestState().getAllList(conn);
    }

//fin zona de reemplazo
}
