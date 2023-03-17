package api.test.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class TestPerson extends BaseModel<TestPerson> {
//inicio zona de reemplazo

    public String name;
    public int cityId;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "city_id",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, cityId);
        q.setParam(3, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        cityId = MySQLQuery.getAsInteger(row[1]);
        active = MySQLQuery.getAsBoolean(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "test_person";
    }

    public static String getSelFlds(String alias) {
        return new TestPerson().getSelFldsForAlias(alias);
    }

    public static List<TestPerson> getList(MySQLQuery q, Connection conn) throws Exception {
        return new TestPerson().getListFromQuery(q, conn);
    }

    public static List<TestPerson> getList(Params p, Connection conn) throws Exception {
        return new TestPerson().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new TestPerson().deleteById(id, conn);
    }

    public static List<TestPerson> getAll(Connection conn) throws Exception {
        return new TestPerson().getAllList(conn);
    }

//fin zona de reemplazo
}
