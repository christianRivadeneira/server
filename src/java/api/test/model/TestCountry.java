package api.test.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class TestCountry extends BaseModel<TestCountry> {
//inicio zona de reemplazo

    public String name;
    public boolean active;
    public String president;
    public Integer population;
    public String capital;
    public int area;
    public int densidad;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "active",
            "president",
            "population",
            "capital",
            "area",
            "densidad"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, active);
        q.setParam(3, president);
        q.setParam(4, population);
        q.setParam(5, capital);
        q.setParam(6, area);
        q.setParam(7, densidad);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        active = MySQLQuery.getAsBoolean(row[1]);
        president = MySQLQuery.getAsString(row[2]);
        population = MySQLQuery.getAsInteger(row[3]);
        capital = MySQLQuery.getAsString(row[4]);
        area = MySQLQuery.getAsInteger(row[5]);
        densidad = MySQLQuery.getAsInteger(row[6]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "test_country";
    }

    public static String getSelFlds(String alias) {
        return new TestCountry().getSelFldsForAlias(alias);
    }

    public static List<TestCountry> getList(MySQLQuery q, Connection conn) throws Exception {
        return new TestCountry().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new TestCountry().deleteById(id, conn);
    }

    public static List<TestCountry> getAll(Connection conn) throws Exception {
        return new TestCountry().getAllList(conn);
    }

//fin zona de reemplazo
}
