package web.gates.cylTrip;

import java.sql.Connection;
import java.util.Date;
import utilities.MySQLQuery;

public class GtCylTrip {
//inicio zona de reemplazo

     public int id;
    public Date tripDate;
    public int typeId;
    public Integer vhId;
    public Integer driverId;
    public Integer nextTripId;
    public String plate;
    public String driver;
    public String authDoc;
    public Date cdt;
    public Date sdt;
    public Date edt;
    public Date ddt;
    public int tryNum;
    public boolean blocked;
    public byte[] ssign;
    public byte[] esign;
    public int centerOrigId;
    public Integer centerDestId;
    public int enterpriseId;
    public int employeeId;
    public boolean cancel;
    public String cancelNotes;
    public byte[] textData;
    public int steps;
    public int reqSteps;
    public Integer orgTripId;
    public Integer liqId;
    public boolean showInLiq;
    public Integer factoryId;
    public boolean hasSsign;
    public boolean hasEsign;

    private static final String SEL_FLDS = "`trip_date`, "
            + "`type_id`, "
            + "`vh_id`, "
            + "`driver_id`, "
            + "`next_trip_id`, "
            + "`plate`, "
            + "`driver`, "
            + "`auth_doc`, "
            + "`cdt`, "
            + "`sdt`, "
            + "`edt`, "
            + "`ddt`, "
            + "`try_num`, "
            + "`blocked`, "
            + "null, "
            + "null, "
            + "`center_orig_id`, "
            + "`center_dest_id`, "
            + "`enterprise_id`, "
            + "`employee_id`, "
            + "`cancel`, "
            + "`cancel_notes`, "
            + "null, "
            + "`steps`, "
            + "`req_steps`, "
            + "`org_trip_id`, "
            + "`liq_id`, "
            + "`show_in_liq`, "
            + "`factory_id`, "
            + "`has_ssign`, "
            + "`has_esign`";

    private static final String SET_FLDS = "gt_cyl_trip SET "
            + "`trip_date` = ?1, "
            + "`type_id` = ?2, "
            + "`vh_id` = ?3, "
            + "`driver_id` = ?4, "
            + "`next_trip_id` = ?5, "
            + "`plate` = ?6, "
            + "`driver` = ?7, "
            + "`auth_doc` = ?8, "
            + "`cdt` = ?9, "
            + "`sdt` = ?10, "
            + "`edt` = ?11, "
            + "`ddt` = ?12, "
            + "`try_num` = ?13, "
            + "`blocked` = ?14, "
            + "`ssign` = ?15, "
            + "`esign` = ?16, "
            + "`center_orig_id` = ?17, "
            + "`center_dest_id` = ?18, "
            + "`enterprise_id` = ?19, "
            + "`employee_id` = ?20, "
            + "`cancel` = ?21, "
            + "`cancel_notes` = ?22, "
            + "`text_data` = ?23, "
            + "`steps` = ?24, "
            + "`req_steps` = ?25, "
            + "`org_trip_id` = ?26, "
            + "`liq_id` = ?27, "
            + "`show_in_liq` = ?28, "            
            + "`factory_id` = ?29, "
            + "`has_ssign` = ?30, "
            + "`has_esign` = ?31";

    private static void setFields(GtCylTrip obj, MySQLQuery q) {
        q.setParam(1, obj.tripDate);
        q.setParam(2, obj.typeId);
        q.setParam(3, obj.vhId);
        q.setParam(4, obj.driverId);
        q.setParam(5, obj.nextTripId);
        q.setParam(6, obj.plate);
        q.setParam(7, obj.driver);
        q.setParam(8, obj.authDoc);
        q.setParam(9, obj.cdt);
        q.setParam(10, obj.sdt);
        q.setParam(11, obj.edt);
        q.setParam(12, obj.ddt);
        q.setParam(13, obj.tryNum);
        q.setParam(14, obj.blocked);
        q.setParam(15, obj.ssign);
        q.setParam(16, obj.esign);
        q.setParam(17, obj.centerOrigId);
        q.setParam(18, obj.centerDestId);
        q.setParam(19, obj.enterpriseId);
        q.setParam(20, obj.employeeId);
        q.setParam(21, obj.cancel);
        q.setParam(22, obj.cancelNotes);
        q.setParam(23, obj.textData);
        q.setParam(24, obj.steps);
        q.setParam(25, obj.reqSteps);
        q.setParam(26, obj.orgTripId);
        q.setParam(27, obj.liqId);
        q.setParam(28, obj.showInLiq);
        q.setParam(29, obj.factoryId);
        q.setParam(30, obj.hasSsign);
        q.setParam(31, obj.hasEsign);
    }

    public static GtCylTrip getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        GtCylTrip obj = new GtCylTrip();
        obj.tripDate = MySQLQuery.getAsDate(row[0]);
        obj.typeId = MySQLQuery.getAsInteger(row[1]);
        obj.vhId = MySQLQuery.getAsInteger(row[2]);
        obj.driverId = MySQLQuery.getAsInteger(row[3]);
        obj.nextTripId = MySQLQuery.getAsInteger(row[4]);
        obj.plate = MySQLQuery.getAsString(row[5]);
        obj.driver = MySQLQuery.getAsString(row[6]);
        obj.authDoc = MySQLQuery.getAsString(row[7]);
        obj.cdt = MySQLQuery.getAsDate(row[8]);
        obj.sdt = MySQLQuery.getAsDate(row[9]);
        obj.edt = MySQLQuery.getAsDate(row[10]);
        obj.ddt = MySQLQuery.getAsDate(row[11]);
        obj.tryNum = MySQLQuery.getAsInteger(row[12]);
        obj.blocked = MySQLQuery.getAsBoolean(row[13]);
        obj.ssign = (row[14] != null ? (byte[]) row[14] : null);
        obj.esign = (row[15] != null ? (byte[]) row[15] : null);
        obj.centerOrigId = MySQLQuery.getAsInteger(row[16]);
        obj.centerDestId = MySQLQuery.getAsInteger(row[17]);
        obj.enterpriseId = MySQLQuery.getAsInteger(row[18]);
        obj.employeeId = MySQLQuery.getAsInteger(row[19]);
        obj.cancel = MySQLQuery.getAsBoolean(row[20]);
        obj.cancelNotes = MySQLQuery.getAsString(row[21]);
        obj.textData = (row[22] != null ? (byte[]) row[22] : null);
        obj.steps = MySQLQuery.getAsInteger(row[23]);
        obj.reqSteps = MySQLQuery.getAsInteger(row[24]);
        obj.orgTripId = MySQLQuery.getAsInteger(row[25]);
        obj.liqId = MySQLQuery.getAsInteger(row[26]);
        obj.showInLiq = MySQLQuery.getAsBoolean(row[27]);
        obj.factoryId = MySQLQuery.getAsInteger(row[28]);
        obj.hasSsign = MySQLQuery.getAsBoolean(row[29]);
        obj.hasEsign = MySQLQuery.getAsBoolean(row[30]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    /////////////////////////////////////////////////////
    public GtCylTrip select(int id, Connection ep) throws Exception {
        return GtCylTrip.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(ep));
    }

    public static Integer getTripIdpByLiquidation(int liqId, Connection ep) throws Exception {
        return new MySQLQuery("SELECT id FROM gt_cyl_trip WHERE liq_id = " + liqId + " ").getAsInteger(ep);
    }

    public int insert(GtCylTrip obj, Connection ep) throws Exception {
        obj.showInLiq = true;
        int nId = new MySQLQuery(GtCylTrip.getInsertQuery(obj)).executeInsert(ep);
        obj.id = nId;
        return nId;
    }

    public void update(GtCylTrip pobj, Connection ep) throws Exception {
        new MySQLQuery(GtCylTrip.getUpdateQuery((GtCylTrip) pobj)).executeUpdate(ep);
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM gt_cyl_trip WHERE id = " + id;
    }

    public static String getInsertQuery(GtCylTrip obj) {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public static String getUpdateQuery(GtCylTrip obj) {
        obj.steps = (obj.cdt != null ? 1 : 0) + (obj.sdt != null ? 1 : 0) + (obj.edt != null ? 1 : 0) + (obj.ddt != null ? 1 : 0);
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public void delete(int id, Connection ep) throws Exception {
        new MySQLQuery("DELETE FROM gt_cyl_trip WHERE id = " + id).executeDelete(ep);
    }

    public String getState(String state) {
        switch (state) {
            case "l":
                return "Llenos";
            case "v":
                return "Vacíos";
            case "f":
                return "Fugas";
            default:
                break;
        }
        return null;
    }

}
