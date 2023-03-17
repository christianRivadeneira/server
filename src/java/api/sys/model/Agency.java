package api.sys.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class Agency extends BaseModel<Agency> {
//inicio zona de reemplazo

    public int cityId;
    public int enterpriseId;
    public String accCode;
    public Integer centerId;
    public Integer locationId;
    public boolean active;
    public Integer sysCenterId;
    public boolean visible;
    public String name;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "city_id",
            "enterprise_id",
            "acc_code",
            "center_id",
            "location_id",
            "active",
            "sys_center_id",
            "visible",
            "name"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, cityId);
        q.setParam(2, enterpriseId);
        q.setParam(3, accCode);
        q.setParam(4, centerId);
        q.setParam(5, locationId);
        q.setParam(6, active);
        q.setParam(7, sysCenterId);
        q.setParam(8, visible);
        q.setParam(9, name);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        cityId = MySQLQuery.getAsInteger(row[0]);
        enterpriseId = MySQLQuery.getAsInteger(row[1]);
        accCode = MySQLQuery.getAsString(row[2]);
        centerId = MySQLQuery.getAsInteger(row[3]);
        locationId = MySQLQuery.getAsInteger(row[4]);
        active = MySQLQuery.getAsBoolean(row[5]);
        sysCenterId = MySQLQuery.getAsInteger(row[6]);
        visible = MySQLQuery.getAsBoolean(row[7]);
        name = MySQLQuery.getAsString(row[8]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "agency";
    }

    public static String getSelFlds(String alias) {
        return new Agency().getSelFldsForAlias(alias);
    }

    public static List<Agency> getList(MySQLQuery q, Connection conn) throws Exception {
        return new Agency().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new Agency().deleteById(id, conn);
    }

    public static List<Agency> getAll(Connection conn) throws Exception {
        return new Agency().getAllList(conn);
    }

//fin zona de reemplazo
    
    public static Agency getAgencyByParams(int cityId, int entId, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " + getSelFlds("") + ", id FROM agency WHERE visible AND city_id = " + cityId + " AND enterprise_id = " + entId);
        Object[] row = q.getRecord(ep);
        if (row != null) {
            Agency ag = new Agency();
            ag.setRow(row);
            return ag;
        } else {
            throw new Exception("La agencia no éxiste.");
        }
    }
}