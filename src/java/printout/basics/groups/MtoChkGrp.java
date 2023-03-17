package printout.basics.groups;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import utilities.JsonUtils;
import utilities.MySQLQuery;

public class MtoChkGrp {
//inicio zona de reemplazo

    public Integer id;
    public Integer versionId;
    public String name;
    public Integer place;
    public String notes;
    public String subquery;
    public boolean isSuperGrp;
    public Integer superGrpId;

    private static final String SEL_FLDS = "`version_id`, "
            + "`name`, "
            + "`place`, "
            + "`notes`, "
            + "`subquery`, "
            + "`is_super_grp`, "
            + "`super_grp_id`";

    private static final String SET_FLDS = "mto_chk_grp SET "
            + "`version_id` = ?1, "
            + "`name` = ?2, "
            + "`place` = ?3, "
            + "`notes` = ?4, "
            + "`subquery` = ?5, "
            + "`is_super_grp` = ?6, "
            + "`super_grp_id` = ?7";

    private static void setFields(MtoChkGrp obj, MySQLQuery q) {
        q.setParam(1, obj.versionId);
        q.setParam(2, obj.name);
        q.setParam(3, obj.place);
        q.setParam(4, obj.notes);
        q.setParam(5, obj.subquery);
        q.setParam(6, obj.isSuperGrp);
        q.setParam(7, obj.superGrpId);
    }

    public static MtoChkGrp getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        MtoChkGrp obj = new MtoChkGrp();
        obj.versionId = (row[0] != null ? (Integer) row[0] : null);
        obj.name = (row[1] != null ? row[1].toString() : null);
        obj.place = (row[2] != null ? (Integer) row[2] : null);
        obj.notes = (row[3] != null ? MySQLQuery.getAsString(row[3]) : null);
        obj.subquery = MySQLQuery.getAsString(row[4]);
        obj.isSuperGrp = MySQLQuery.getAsBoolean(row[5]);
        obj.superGrpId = MySQLQuery.getAsInteger(row[6]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    private static final String TO_STR_FLDS = "`name`";

    public static MtoChkGrp[] getGrpsByVersion(int versionId, Connection ep) throws Exception {
        Object[][] data = new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM mto_chk_grp WHERE version_id = " + versionId + " ORDER BY place").getRecords(ep);
        MtoChkGrp[] rta = new MtoChkGrp[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = getFromRow(data[i]);
        }
        return rta;
    }

    public int insert(MtoChkGrp obj, Connection ep) throws Exception {
        obj.place = new MySQLQuery("SELECT COALESCE(MAX(place), 0) + 1 FROM mto_chk_grp WHERE version_id = " + obj.versionId).getAsInteger(ep);
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.executeInsert(ep);
    }

    public void update(MtoChkGrp obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        q.executeUpdate(ep);
    }

    public void delete(int id, Connection ep) throws Exception {
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM mto_chk_grp WHERE version_id = " + id + " ORDER BY place";
    }

    public static MtoChkGrp[] getGrpsSubQ(Integer version, Integer vehicle, Connection conn) throws IOException, Exception {
        List<MtoChkGrp> list = new ArrayList<>();
        JsonArray data = getJsonGrps(version, vehicle, conn).build();
        JsonObject row;

        for (int i = 0; i < data.size(); i++) {
            MtoChkGrp grp = new MtoChkGrp();
            row = data.getJsonObject(i);

            grp.versionId = row.getInt("version");
            grp.name = row.getString("name");
            grp.place = row.getInt("place");
            grp.notes = JsonUtils.getString(row, "notes");
            grp.subquery = JsonUtils.getString(row, "subquery");
            grp.isSuperGrp = JsonUtils.getInt(row, "isSuperGrp").equals(1);
            grp.superGrpId = JsonUtils.getInt(row, "superGrpId");
            grp.id = row.getInt("id");
            list.add(grp);
        }

        return list.toArray(new MtoChkGrp[list.size()]);
    }

    public static JsonArrayBuilder getJsonGrps(Integer version, Integer vehicle, Connection conn) throws Exception {
        Object[][] data = new MySQLQuery(MtoChkGrp.getSelectQuery(version)).getRecords(conn);

        JsonArrayBuilder jsonArrayGrps = Json.createArrayBuilder();

        for (Object[] grp : data) {
            boolean addGroup = true;
            if (grp[4] != null) {//[4] campo subquery
                String query = MySQLQuery.getAsString(grp[4]);
                if (!query.toLowerCase().contains("select")) {
                    throw new Exception("El subquery definido en el grupo [" + grp[1].toString().toUpperCase() + "] no es valido");
                }

                if (query.contains("@vh")) {
                    query = query.replaceAll("@vh", MySQLQuery.getAsString(vehicle));
                }
                Boolean rta = new MySQLQuery(MySQLQuery.getAsString(query)).getAsBoolean(conn);
                addGroup = (rta != null ? rta : false);
            }

            if (addGroup) {
                JsonObjectBuilder row = Json.createObjectBuilder();
                row.add("version", MySQLQuery.getAsInteger(grp[0]));
                row.add("name", MySQLQuery.getAsString(grp[1]));
                row.add("place", MySQLQuery.getAsInteger(grp[2]));
                JsonUtils.addString(row, "notes", grp[3]);
                JsonUtils.addString(row, "subquery", (grp[4]));
                JsonUtils.addInt(row, "isSuperGrp", (grp[5]));
                JsonUtils.addInt(row, "superGrpId", (grp[6]));
                row.add("id", MySQLQuery.getAsInteger(grp[7]));
                jsonArrayGrps.add(row);
            }
        }
        return jsonArrayGrps;
    }
}
