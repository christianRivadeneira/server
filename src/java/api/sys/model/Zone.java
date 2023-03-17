package api.sys.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.math.BigDecimal;

public class Zone extends BaseModel<Zone> {
//inicio zona de reemplazo

    public String name;
    public Integer percRul;
    public Integer percUrb;
    public boolean active;    
    public BigDecimal lat;
    public BigDecimal lon;
    public Integer polyColor;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "perc_rul",
            "perc_urb",
            "active",
            "lat",
            "lon",
            "poly_color"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, percRul);
        q.setParam(3, percUrb);
        q.setParam(4, active);
        q.setParam(5, lat);
        q.setParam(6, lon);
        q.setParam(7, polyColor);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        percRul = MySQLQuery.getAsInteger(row[1]);
        percUrb = MySQLQuery.getAsInteger(row[2]);
        active = MySQLQuery.getAsBoolean(row[3]);
        lat = MySQLQuery.getAsBigDecimal(row[4], false);
        lon = MySQLQuery.getAsBigDecimal(row[5], false);
        polyColor = MySQLQuery.getAsInteger(row[6]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "zone";
    }

    public static String getSelFlds(String alias) {
        return new Zone().getSelFldsForAlias(alias);
    }

    public static List<Zone> getList(MySQLQuery q, Connection conn) throws Exception {
        return new Zone().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new Zone().deleteById(id, conn);
    }
    
    public static Zone findById(int id, Connection conn) throws Exception {
        Zone obj = new Zone().select(id, conn);
        return obj;
    }

    public static List<Zone> getAll(Connection conn) throws Exception {
        return new Zone().getAllList(conn);
    }

//fin zona de reemplazo
}