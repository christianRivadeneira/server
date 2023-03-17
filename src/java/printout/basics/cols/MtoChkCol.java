package printout.basics.cols;

import java.sql.Connection;
import utilities.MySQLQuery;

public class MtoChkCol {
//inicio zona de reemplazo

    public Integer id;
    public Integer grpId;
    public String name;
    public String shortName;
    public String state;
    public String flipState;
    public Integer place;

    private static final String selFlds = "`grp_id`, "
            + "`name`, "
            + "`short_name`, "
            + "`state`, "
            + "`flip_state`, "
            + "`place`";

    private static final String setFlds = "mto_chk_col SET "
            + "`grp_id` = ?1, "
            + "`name` = ?2, "
            + "`short_name` = ?3, "
            + "`state` = ?4, "
            + "`flip_state` = ?5, "
            + "`place` = ?6";

    private void setFields(MtoChkCol obj, MySQLQuery q) {
        q.setParam(1, obj.grpId);
        q.setParam(2, obj.name);
        q.setParam(3, obj.shortName);
        q.setParam(4, obj.state);
        q.setParam(5, obj.flipState);
        q.setParam(6, obj.place);

    }

//fin zona de reemplazo
    public MtoChkCol select(int id, Connection ep) throws Exception {
        return getFromRow(new MySQLQuery("SELECT " + selFlds + ", id FROM mto_chk_col WHERE id = " + id).getRecord(ep));
    }

    public static MtoChkCol getFromRow(Object[] row) throws Exception {
        MtoChkCol obj = new MtoChkCol();
        obj.grpId = (row[0] != null ? (Integer) row[0] : null);
        obj.name = (row[1] != null ? row[1].toString() : null);
        obj.shortName = (row[2] != null ? row[2].toString() : null);
        obj.state = (row[3] != null ? (String) row[3] : null);
        obj.flipState = (row[4] != null ? (String) row[4] : null);
        obj.place = (row[5] != null ? (Integer) row[5] : null);
        obj.id = (Integer) row[6];
        return obj;
    }

//fin zona de reemplazo
    public static String getQueryColByGrp(int grpId, Connection ep) {
        return "SELECT " + selFlds + ", id FROM mto_chk_col WHERE grp_id = " + grpId + " ORDER BY place";
    }

    public static MtoChkCol[] getColsByGrp(Object[][] data) throws Exception {
        MtoChkCol[] rta = new MtoChkCol[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = getFromRow(data[i]);
        }
        return rta;
    }

    public int insert(MtoChkCol obj, Connection ep) throws Exception {
        obj.place = new MySQLQuery("SELECT COALESCE(MAX(place), 0) + 1 FROM mto_chk_col WHERE grp_id = " + obj.grpId).getAsInteger(ep);
        MySQLQuery q = new MySQLQuery("INSERT INTO " + setFlds);
        setFields(obj, q);
        int insId = q.executeInsert(ep);
        updateFlip(ep);
        return insId;

    }

    public void update(MtoChkCol obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("UPDATE " + setFlds + " WHERE id = " + obj.id);
        setFields(obj, q);
        q.executeUpdate(ep);
        updateFlip(ep);

    }

    public void delete(int id, Connection ep) throws Exception {
        new MySQLQuery("DELETE FROM mto_chk_col WHERE id = " + id).executeDelete(ep);
        updateFlip(ep);
    }

    private static void updateFlip(Connection ep) throws Exception {
        new MySQLQuery("UPDATE mto_chk_col, ( "
                + "SELECT c1.id, c2.state FROM mto_chk_col c1 "
                + "INNER JOIN mto_chk_col c2 ON c1.grp_id = c2.grp_id AND c2.place = (SELECT COUNT(*) FROM mto_chk_col WHERE grp_id = c1.grp_id) + 1 - c1.place) AS l "
                + "SET mto_chk_col.flip_state = l.state WHERE l.id = mto_chk_col.id").executeUpdate(ep);
    }

    public String toString(int id, Connection ep) throws Exception {
        return null;
    }

    public String getEnumOptions(String fieldName) {
        if (fieldName.equals("state")) {
            return "ok=Sin Problemas&warn=Advertencia&stop=Detener el Vehículo";
        }
        return null;
    }
}
