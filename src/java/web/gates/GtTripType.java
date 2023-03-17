package web.gates;

import java.sql.Connection;
import utilities.MySQLQuery;

public class GtTripType {

    public Integer id;
    public Boolean c;
    public Boolean s;
    public Boolean e;
    public Boolean n;
    public Boolean d;
    public String type;
    public String name;
    public Boolean sell;
    public Boolean pul;
    public Boolean sameState;
    public Boolean active;
    public Boolean sameCenter;
    public Boolean glpCSingle;
    public Boolean glpSSingle;
    public Boolean glpESingle;
    public Boolean glpDSingle;
    public Integer steps;
    public Boolean scnCyls;
    public Boolean factory;
    public Integer invMvTypeId;

    private static final String selFlds = "`c`, "
            + "`s`, "
            + "`e`, "
            + "`n`, "
            + "`d`, "
            + "`type`, "
            + "`name`, "
            + "`sell`, "
            + "`pul`, "
            + "`same_state`, "
            + "`active`, "
            + "`same_center`, "
            + "`glp_c_single`, "
            + "`glp_s_single`, "
            + "`glp_e_single`, "
            + "`glp_d_single`, "
            + "`steps`, "
            + "`scn_cyls`, "
            + "`factory`, "
            + "`inv_mv_type_id`";

    private static final String setFlds = "gt_trip_type SET "
            + "`c` = ?1, "
            + "`s` = ?2, "
            + "`e` = ?3, "
            + "`n` = ?4, "
            + "`d` = ?5, "
            + "`type` = ?6, "
            + "`name` = ?7, "
            + "`sell` = ?8, "
            + "`pul` = ?9, "
            + "`same_state` = ?10, "
            + "`active` = ?11, "
            + "`same_center` = ?12, "
            + "`glp_c_single` = ?13, "
            + "`glp_s_single` = ?14, "
            + "`glp_e_single` = ?15, "
            + "`glp_d_single` = ?16, "
            + "`spteps` = ?17, "
            + "`scn_cyls` = ?18, "
            + "`factory` = ?19, "
            + "`inv_mv_type_id` = ?20";

    private void setFields(GtTripType obj, MySQLQuery q) {
        q.setParam(1, obj.c);
        q.setParam(2, obj.s);
        q.setParam(3, obj.e);
        q.setParam(4, obj.n);
        q.setParam(5, obj.d);
        q.setParam(6, obj.type);
        q.setParam(7, obj.name);
        q.setParam(8, obj.sell);
        q.setParam(9, obj.pul);
        q.setParam(10, obj.sameState);
        q.setParam(11, obj.active);
        q.setParam(12, obj.sameCenter);
        q.setParam(13, obj.glpCSingle);
        q.setParam(14, obj.glpSSingle);
        q.setParam(15, obj.glpESingle);
        q.setParam(16, obj.glpDSingle);
        q.setParam(17, obj.steps);
        q.setParam(18, obj.scnCyls);
        q.setParam(19, obj.factory);
        q.setParam(20, obj.invMvTypeId);
    }

    public GtTripType select(int id, Connection ep) throws Exception {
        GtTripType obj = new GtTripType();
        MySQLQuery q = new MySQLQuery("SELECT " + selFlds + " FROM gt_trip_type WHERE id = " + id);
        Object[] row = q.getRecord(ep);
        obj.c = (row[0] != null ? (Boolean) row[0] : null);
        obj.s = (row[1] != null ? (Boolean) row[1] : null);
        obj.e = (row[2] != null ? (Boolean) row[2] : null);
        obj.n = (row[3] != null ? (Boolean) row[3] : null);
        obj.d = (row[4] != null ? (Boolean) row[4] : null);
        obj.type = (row[5] != null ? row[5].toString() : null);
        obj.name = (row[6] != null ? row[6].toString() : null);
        obj.sell = (row[7] != null ? (Boolean) row[7] : null);
        obj.pul = (row[8] != null ? (Boolean) row[8] : null);
        obj.sameState = (row[9] != null ? (Boolean) row[9] : null);
        obj.active = (row[10] != null ? (Boolean) row[10] : null);
        obj.sameCenter = (row[11] != null ? (Boolean) row[11] : null);
        obj.glpCSingle = (row[12] != null ? (Boolean) row[12] : null);
        obj.glpSSingle = (row[13] != null ? (Boolean) row[13] : null);
        obj.glpESingle = (row[14] != null ? (Boolean) row[14] : null);
        obj.glpDSingle = (row[15] != null ? (Boolean) row[15] : null);
        obj.steps = (row[16] != null ? (Integer) row[16] : null);
        obj.scnCyls = (row[17] != null ? (Boolean) row[17] : null);
        obj.factory = (row[18] != null ? (Boolean) row[18] : null);
        obj.invMvTypeId = (MySQLQuery.getAsInteger(row[19]));

        obj.id = id;
        return obj;
    }

//fin zona de reemplazo
    public String getEnumOptions(String fieldName) {
        if (fieldName.equals("type")) {
            return "cyls=Cilindros&glp=Glp";
        }
        return null;
    }

    public static Object[][] getOptions(String type, Connection ep) throws Exception {
        boolean showOutFactory = new MySQLQuery("SELECT show_out_factory FROM gt_cfg WHERE id = 1").getAsBoolean(ep);
        return new MySQLQuery("SELECT id, name "
                + "FROM gt_trip_type WHERE type = '" + type + "' AND active = 1 "
                + (showOutFactory ? "" : "AND factory = 0 ")
                + "ORDER BY name").getRecords(ep);
    }

    public static boolean getSingleByType(GtTripType tripType, String evType) {
        if (evType.equals("c")) {
            return tripType.glpCSingle;
        } else if (evType.equals("s")) {
            return tripType.glpSSingle;
        } else if (evType.equals("e")) {
            return tripType.glpESingle;
        } else if (evType.equals("d")) {
            return tripType.glpDSingle;
        }
        throw new RuntimeException("Tipo desconocido " + evType);
    }

    public static Boolean getNeedsVal(GtTripType tripType, String type) {
        if (type.equals("c")) {
            return false;
        } else if (type.equals("s")) {
            return tripType.c;
        } else if (type.equals("e")) {
            return tripType.s;
        } else if (type.equals("d")) {
            return tripType.e;
        }
        throw new RuntimeException("Tipo no reconocido: " + type);
    }
}
