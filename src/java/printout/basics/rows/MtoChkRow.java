package printout.basics.rows;

import java.sql.Connection;
import utilities.MySQLQuery;


public class MtoChkRow {
//inicio zona de reemplazo
//AL ACTULIZAR ESTA CLASE CAMBIAR EL SELECT Y getFromRow

    public Integer id;
    public Integer grpId;
    public String name;
    public Integer place;
    public String type;
    public Boolean error;
    public Boolean mandatory;
    public Boolean flip;

    private static final String selFlds = "`grp_id`, "
            + "`name`, "
            + "`place`, "
            + "`type`, "
            + "`error`, "
            + "`mandatory`,"
            + "flip";

    private static final String setFlds = "mto_chk_row SET "
            + "`grp_id` = ?1, "
            + "`name` = ?2, "
            + "`place` = ?3, "
            + "`type` = ?4, "
            + "`error` = ?5, "
            + "`mandatory` = ?6,"
            + "flip = ?7";

    private void setFields(MtoChkRow obj, MySQLQuery q) {
        q.setParam(1, obj.grpId);
        q.setParam(2, obj.name);
        q.setParam(3, obj.place);
        q.setParam(4, obj.type);
        q.setParam(5, obj.error);
        q.setParam(6, obj.mandatory);
        q.setParam(7, obj.flip);
    }

    public MtoChkRow select(int id, Connection ep) throws Exception {
        return getFromRow(new MySQLQuery("SELECT " + selFlds + ", id FROM mto_chk_row WHERE id = " + id).getRecord(ep));
    }

//fin zona de reemplazo
    public static MtoChkRow getFromRow(Object[] row) throws Exception {
        MtoChkRow obj = new MtoChkRow();
        obj.grpId = (row[0] != null ? (Integer) row[0] : null);
        obj.name = (row[1] != null ? row[1].toString() : null);
        obj.place = (row[2] != null ? (Integer) row[2] : null);
        obj.type = (row[3] != null ? row[3].toString() : null);
        obj.error = (row[4] != null ? (Boolean) row[4] : null);
        obj.mandatory = (row[5] != null ? (Boolean) row[5] : null);
        obj.flip = (row[6] != null ? (Boolean) row[6] : null);
        obj.id = (Integer) row[7];//el ultimo indice siempre es el id
        return obj;
    }

    public static String getQueryRowByGrp(int grpId, Connection ep) throws Exception {
        return "SELECT " + selFlds + ", id FROM mto_chk_row WHERE grp_id = " + grpId + " ORDER BY place";
    }

    public static MtoChkRow[] getRowsByGrp(Object[][] data) throws Exception {
        MtoChkRow[] rta = new MtoChkRow[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = getFromRow(data[i]);
        }
        return rta;
    }

    public int insert(MtoChkRow obj, Connection ep) throws Exception {
        obj.place = new MySQLQuery("SELECT COALESCE(MAX(place), 0) + 1 FROM mto_chk_row WHERE grp_id = " + obj.grpId).getAsInteger(ep);
        MySQLQuery q = new MySQLQuery("INSERT INTO " + setFlds);
        setFields(obj, q);
        return q.executeInsert(ep);
    }

    public void update(MtoChkRow obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("UPDATE " + setFlds + " WHERE id = " + obj.id);
        setFields(obj, q);
        q.executeUpdate(ep);
    }

    public void delete(int id, Connection ep) throws Exception {
        new MySQLQuery("DELETE FROM mto_chk_row WHERE id = " + id).executeDelete(ep);
    }

    public String getEnumOptions(String fieldName) {
        if (fieldName.equals("type")) {
            return "nor=Normal&tit=Título&num=Número&txt=Texto";
        }
        return null;
    }

    public static String getLstState(MtoChkRow r, String colState) {
        if (r.error && colState.equals("stop")) {
            return "error";
        } else if (r.error && colState.equals("warn") || !r.error && colState.equals("stop")) {
            return "warn";
        } else {
            return "ok";
        }
    }
}
