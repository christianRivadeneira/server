package api.sys.model;

import api.BaseModel;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class Neigh extends BaseModel<Neigh> {
//inicio zona de reemplazo

    public String name;
    public int sectorId;
    public Integer oldCode;
    public String type;
    public BigDecimal lat;
    public BigDecimal lon;
    public Integer urbZoneId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "sector_id",
            "old_code",
            "type",
            "lat",
            "lon",
            "urb_zone_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, sectorId);
        q.setParam(3, oldCode);
        q.setParam(4, type);
        q.setParam(5, lat);
        q.setParam(6, lon);
        q.setParam(7, urbZoneId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        sectorId = MySQLQuery.getAsInteger(row[1]);
        oldCode = MySQLQuery.getAsInteger(row[2]);
        type = MySQLQuery.getAsString(row[3]);
        lat = MySQLQuery.getAsBigDecimal(row[4], false);
        lon = MySQLQuery.getAsBigDecimal(row[5], false);
        urbZoneId = MySQLQuery.getAsInteger(row[6]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "neigh";
    }

    public static String getSelFlds(String alias) {
        return new Neigh().getSelFldsForAlias(alias);
    }

    public static List<Neigh> getList(MySQLQuery q, Connection conn) throws Exception {
        return new Neigh().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new Neigh().deleteById(id, conn);
    }

    public static List<Neigh> getAll(Connection conn) throws Exception {
        return new Neigh().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<Neigh> getNeighsBySector(int sectorId, Connection ep) throws Exception {
        MySQLQuery mq = new MySQLQuery("SELECT " + getSelFlds("") + " FROM neigh WHERE sector_id = " + sectorId + " ORDER BY name ASC");
        List<Neigh> list = getList(mq, ep);
        return list;
    }
}
