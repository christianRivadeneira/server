package api.sys.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.math.BigDecimal;

public class City extends BaseModel<City> {
//inicio zona de reemplazo

    public String name;
    public int zoneId;
    public Integer oldCode;
    public String dbName;
    public boolean ctReal;
    public String daneCode;
    public Integer munId;
    public Integer pobId;
    public BigDecimal lat;
    public BigDecimal lon;
    public boolean neighCoords;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "zone_id",
            "old_code",
            "db_name",
            "ct_real",
            "dane_code",
            "mun_id",
            "pob_id",
            "lat",
            "lon",
            "neigh_coords"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, zoneId);
        q.setParam(3, oldCode);
        q.setParam(4, dbName);
        q.setParam(5, ctReal);
        q.setParam(6, daneCode);
        q.setParam(7, munId);
        q.setParam(8, pobId);
        q.setParam(9, lat);
        q.setParam(10, lon);
        q.setParam(11, neighCoords);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        zoneId = MySQLQuery.getAsInteger(row[1]);
        oldCode = MySQLQuery.getAsInteger(row[2]);
        dbName = MySQLQuery.getAsString(row[3]);
        ctReal = MySQLQuery.getAsBoolean(row[4]);
        daneCode = MySQLQuery.getAsString(row[5]);
        munId = MySQLQuery.getAsInteger(row[6]);
        pobId = MySQLQuery.getAsInteger(row[7]);
        lat = MySQLQuery.getAsBigDecimal(row[8], false);
        lon = MySQLQuery.getAsBigDecimal(row[9], false);
        neighCoords = MySQLQuery.getAsBoolean(row[10]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "city";
    }

    public static String getSelFlds(String alias) {
        return new City().getSelFldsForAlias(alias);
    }

    public static List<City> getList(MySQLQuery q, Connection conn) throws Exception {
        return new City().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new City().deleteById(id, conn);
    }
    
    public static City findById(int id, Connection conn) throws Exception {
        City obj = new City().select(id, conn);
        return obj;
    }

    public static List<City> getAll(Connection conn) throws Exception {
        return new City().getAllList(conn);
    }

//fin zona de reemplazo
}