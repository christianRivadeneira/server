package api.ess.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class EssUnit extends BaseModel<EssUnit> {

    private static List<EssUnit> getByBuild(int buildingId, Connection conn) throws Exception {
        return getList(new MySQLQuery("SELECT " + getSelFlds("") + " FROM ess_unit WHERE build_id = ?1").setParam(1, buildingId), conn);

    }
//inicio zona de reemplazo

    public int buildId;
    public String code;
    public int tower;
    public String phone;
    public boolean active;
    public String cache;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "build_id",
            "code",
            "tower",
            "phone",
            "active",
            "cache"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, buildId);
        q.setParam(2, code);
        q.setParam(3, tower);
        q.setParam(4, phone);
        q.setParam(5, active);
        q.setParam(6, cache);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        buildId = MySQLQuery.getAsInteger(row[0]);
        code = MySQLQuery.getAsString(row[1]);
        tower = MySQLQuery.getAsInteger(row[2]);
        phone = MySQLQuery.getAsString(row[3]);
        active = MySQLQuery.getAsBoolean(row[4]);
        cache = MySQLQuery.getAsString(row[5]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ess_unit";
    }

    public static String getSelFlds(String alias) {
        return new EssUnit().getSelFldsForAlias(alias);
    }

    public static List<EssUnit> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EssUnit().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EssUnit().deleteById(id, conn);
    }

    public static List<EssUnit> getAll(Connection conn) throws Exception {
        return new EssUnit().getAllList(conn);
    }

//fin zona de reemplazo
    public static void updateCache(int unitId, Connection conn) throws Exception {
        EssUnit u = new EssUnit().select(unitId, conn);
        String cache = new MySQLQuery("SELECT GROUP_CONCAT(concat(first_name, ' ',last_name) SEPARATOR ' ') FROM \n"
                + "ess_person_unit pu\n"
                + "INNER JOIN ess_person p ON pu.person_id = p.id\n"
                + "WHERE pu.unit_id = ?1").setParam(1, unitId).getAsString(conn);
        u.cache = (cache != null ? cache : "") + " " + u.code + " " + u.tower;
        u.update(conn);
    }

    public static void updateCache(Connection conn) throws Exception {
        List<EssBuilding> builds = EssBuilding.getAll(conn);
        for (int i = 0; i < builds.size(); i++) {
            EssBuilding b = builds.get(i);
            List<EssUnit> units = EssUnit.getByBuild(b.id, conn);
            for (int j = 0; j < units.size(); j++) {
                EssUnit u = units.get(j);
                updateCache(u.id, conn);
            }
        }
    }
}
