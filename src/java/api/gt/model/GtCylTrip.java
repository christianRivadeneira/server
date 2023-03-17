package api.gt.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class GtCylTrip extends BaseModel<GtCylTrip> {
//inicio zona de reemplazo

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
    public Boolean imported;
    public boolean hasSsign;
    public boolean hasEsign;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "trip_date",
            "type_id",
            "vh_id",
            "driver_id",
            "next_trip_id",
            "plate",
            "driver",
            "auth_doc",
            "cdt",
            "sdt",
            "edt",
            "ddt",
            "try_num",
            "blocked",
            "ssign",
            "esign",
            "center_orig_id",
            "center_dest_id",
            "enterprise_id",
            "employee_id",
            "cancel",
            "cancel_notes",
            "text_data",
            "steps",
            "req_steps",
            "org_trip_id",
            "liq_id",
            "show_in_liq",
            "factory_id",
            "imported",
            "has_ssign",
            "has_esign"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, tripDate);
        q.setParam(2, typeId);
        q.setParam(3, vhId);
        q.setParam(4, driverId);
        q.setParam(5, nextTripId);
        q.setParam(6, plate);
        q.setParam(7, driver);
        q.setParam(8, authDoc);
        q.setParam(9, cdt);
        q.setParam(10, sdt);
        q.setParam(11, edt);
        q.setParam(12, ddt);
        q.setParam(13, tryNum);
        q.setParam(14, blocked);
        q.setParam(15, ssign);
        q.setParam(16, esign);
        q.setParam(17, centerOrigId);
        q.setParam(18, centerDestId);
        q.setParam(19, enterpriseId);
        q.setParam(20, employeeId);
        q.setParam(21, cancel);
        q.setParam(22, cancelNotes);
        q.setParam(23, textData);
        q.setParam(24, steps);
        q.setParam(25, reqSteps);
        q.setParam(26, orgTripId);
        q.setParam(27, liqId);
        q.setParam(28, showInLiq);
        q.setParam(29, factoryId);
        q.setParam(30, imported);
        q.setParam(31, hasSsign);
        q.setParam(32, hasEsign);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        tripDate = MySQLQuery.getAsDate(row[0]);
        typeId = MySQLQuery.getAsInteger(row[1]);
        vhId = MySQLQuery.getAsInteger(row[2]);
        driverId = MySQLQuery.getAsInteger(row[3]);
        nextTripId = MySQLQuery.getAsInteger(row[4]);
        plate = MySQLQuery.getAsString(row[5]);
        driver = MySQLQuery.getAsString(row[6]);
        authDoc = MySQLQuery.getAsString(row[7]);
        cdt = MySQLQuery.getAsDate(row[8]);
        sdt = MySQLQuery.getAsDate(row[9]);
        edt = MySQLQuery.getAsDate(row[10]);
        ddt = MySQLQuery.getAsDate(row[11]);
        tryNum = MySQLQuery.getAsInteger(row[12]);
        blocked = MySQLQuery.getAsBoolean(row[13]);
        ssign = (row[14] != null ? (byte[]) row[14] : null);
        esign = (row[15] != null ? (byte[]) row[15] : null);
        centerOrigId = MySQLQuery.getAsInteger(row[16]);
        centerDestId = MySQLQuery.getAsInteger(row[17]);
        enterpriseId = MySQLQuery.getAsInteger(row[18]);
        employeeId = MySQLQuery.getAsInteger(row[19]);
        cancel = MySQLQuery.getAsBoolean(row[20]);
        cancelNotes = MySQLQuery.getAsString(row[21]);
        textData = (row[22] != null ? (byte[]) row[22] : null);
        steps = MySQLQuery.getAsInteger(row[23]);
        reqSteps = MySQLQuery.getAsInteger(row[24]);
        orgTripId = MySQLQuery.getAsInteger(row[25]);
        liqId = MySQLQuery.getAsInteger(row[26]);
        showInLiq = MySQLQuery.getAsBoolean(row[27]);
        factoryId = MySQLQuery.getAsInteger(row[28]);
        imported = MySQLQuery.getAsBoolean(row[29]);
        hasSsign = MySQLQuery.getAsBoolean(row[30]);
        hasEsign = MySQLQuery.getAsBoolean(row[31]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "gt_cyl_trip";
    }

    public static String getSelFlds(String alias) {
        return new GtCylTrip().getSelFldsForAlias(alias);
    }

    public static List<GtCylTrip> getList(MySQLQuery q, Connection conn) throws Exception {
        return new GtCylTrip().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new GtCylTrip().deleteById(id, conn);
    }

    public static List<GtCylTrip> getAll(Connection conn) throws Exception {
        return new GtCylTrip().getAllList(conn);
    }

//fin zona de reemplazo

}
