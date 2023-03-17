package web.gates;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import utilities.MySQLQuery;

public class GtGlpInv {

    public Integer id;
    public Integer tripId;
    public Date invDate;
    public String type;
    public BigDecimal tmpB;
    public BigDecimal tmpE;
    public BigDecimal rgB;
    public BigDecimal rgE;
    public BigDecimal psiB;
    public BigDecimal psiE;
    public BigDecimal d1B;
    public BigDecimal d1E;
    public BigDecimal d2B;
    public BigDecimal d2E;
    public Integer minutesBegin;
    public Integer minutesEnd;
    public String notes;
    public boolean single;

    private static final String selFlds = "`trip_id`, "
            + "`inv_date`, "
            + "`type`, "
            + "`tmp_b`, "
            + "`tmp_e`, "
            + "`rg_b`, "
            + "`rg_e`, "
            + "`psi_b`, "
            + "`psi_e`, "
            + "`d1_b`, "
            + "`d1_e`, "
            + "`d2_b`, "
            + "`d2_e`, "
            + "`minutes_begin`, "
            + "`minutes_end`, "
            + "`notes`,"
            + "`single`";

    private static final String setFlds = "gt_glp_inv SET "
            + "`trip_id` = ?1, "
            + "`inv_date` = ?2, "
            + "`type` = ?3, "
            + "`tmp_b` = ?4, "
            + "`tmp_e` = ?5, "
            + "`rg_b` = ?6, "
            + "`rg_e` = ?7, "
            + "`psi_b` = ?8, "
            + "`psi_e` = ?9, "
            + "`d1_b` = ?10, "
            + "`d1_e` = ?11, "
            + "`d2_b` = ?12, "
            + "`d2_e` = ?13, "
            + "`minutes_begin` = ?14, "
            + "`minutes_end` = ?15, "
            + "`notes` = ?16, "
            + "`single` = ?17";

    private void setFields(GtGlpInv obj, MySQLQuery q) {
        q.setParam(1, obj.tripId);
        q.setParam(2, obj.invDate);
        q.setParam(3, obj.type);
        q.setParam(4, obj.tmpB);
        q.setParam(5, obj.tmpE);
        q.setParam(6, obj.rgB);
        q.setParam(7, obj.rgE);
        q.setParam(8, obj.psiB);
        q.setParam(9, obj.psiE);
        q.setParam(10, obj.d1B);
        q.setParam(11, obj.d1E);
        q.setParam(12, obj.d2B);
        q.setParam(13, obj.d2E);
        q.setParam(14, obj.minutesBegin);
        q.setParam(15, obj.minutesEnd);
        q.setParam(16, obj.notes);
        q.setParam(17, obj.single);
    }

    public GtGlpInv select(int id, Connection ep) throws Exception {
        GtGlpInv obj = new GtGlpInv();
        MySQLQuery q = new MySQLQuery("SELECT " + selFlds + " FROM gt_glp_inv WHERE id = " + id);
        Object[] row = q.getRecord(ep);
        obj.tripId = (row[0] != null ? (Integer) row[0] : null);
        obj.invDate = (row[1] != null ? (Date) row[1] : null);
        obj.type = (row[2] != null ? row[2].toString() : null);
        obj.tmpB = (row[3] != null ? (BigDecimal) row[3] : null);
        obj.tmpE = (row[4] != null ? (BigDecimal) row[4] : null);
        obj.rgB = (row[5] != null ? (BigDecimal) row[5] : null);
        obj.rgE = (row[6] != null ? (BigDecimal) row[6] : null);
        obj.psiB = (row[7] != null ? (BigDecimal) row[7] : null);
        obj.psiE = (row[8] != null ? (BigDecimal) row[8] : null);
        obj.d1B = (row[9] != null ? (BigDecimal) row[9] : null);
        obj.d1E = (row[10] != null ? (BigDecimal) row[10] : null);
        obj.d2B = (row[11] != null ? (BigDecimal) row[11] : null);
        obj.d2E = (row[12] != null ? (BigDecimal) row[12] : null);
        obj.minutesBegin = (row[13] != null ? (Integer) row[13] : null);
        obj.minutesEnd = (row[14] != null ? (Integer) row[14] : null);
        obj.notes = (row[15] != null ? row[15].toString() : null);
        obj.single = (row[16] != null ? (Boolean) row[16] : null);
        obj.id = id;
        return obj;
    }

    public int insert(GtGlpInv obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + setFlds);
        setFields(obj, q);
        return q.executeInsert(ep);
    }

    public void update(GtGlpInv obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("UPDATE " + setFlds + " WHERE id = " + obj.id);
        setFields(obj, q);
        q.executeUpdate(ep);
    }

    public void delete(int id, Connection ep) throws Exception {
        new MySQLQuery("DELETE FROM gt_glp_inv WHERE id = " + id).executeDelete(ep);
    }

    public String getEnumOptions(String fieldName) {
        if (fieldName.equals("type")) {
            return "c=Cargue&s=Salida&e=Entrada&d=Descargue";
        }
        return null;
    }

    public static String getTypes(String name) {
        if (name.equals("c")) {
            return "Cargue";
        } else if (name.equals("d")) {
            return "Descargue";
        } else if (name.equals("e")) {
            return "Entrada";
        } else if (name.equals("s")) {
            return "Salida";
        }
        return null;
    }
    
}
