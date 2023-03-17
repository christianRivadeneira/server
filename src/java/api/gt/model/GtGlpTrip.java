package api.gt.model;

import api.BaseModel;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class GtGlpTrip extends BaseModel<GtGlpTrip> {

    //Fuera de la zona de reemplazo
    public String logNotes;
    //---------------------------------------------------

    //inicio zona de reemplazo

    public Date tripDate;
    public int typeId;
    public Integer vhId;
    public Integer driverId;
    public String plate;
    public String driver;
    public String authDoc;
    public String remPlate;
    public String prov;
    public String billNum;
    public String orderNum;
    public String cumplido;
    public BigDecimal capaFull;
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
    public Integer reqSteps;
    public String sealNum;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "trip_date",
            "type_id",
            "vh_id",
            "driver_id",
            "plate",
            "driver",
            "auth_doc",
            "rem_plate",
            "prov",
            "bill_num",
            "order_num",
            "cumplido",
            "capa_full",
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
            "seal_num"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, tripDate);
        q.setParam(2, typeId);
        q.setParam(3, vhId);
        q.setParam(4, driverId);
        q.setParam(5, plate);
        q.setParam(6, driver);
        q.setParam(7, authDoc);
        q.setParam(8, remPlate);
        q.setParam(9, prov);
        q.setParam(10, billNum);
        q.setParam(11, orderNum);
        q.setParam(12, cumplido);
        q.setParam(13, capaFull);
        q.setParam(14, ssign);
        q.setParam(15, esign);
        q.setParam(16, centerOrigId);
        q.setParam(17, centerDestId);
        q.setParam(18, enterpriseId);
        q.setParam(19, employeeId);
        q.setParam(20, cancel);
        q.setParam(21, cancelNotes);
        q.setParam(22, textData);
        q.setParam(23, steps);
        q.setParam(24, reqSteps);
        q.setParam(25, sealNum);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        tripDate = MySQLQuery.getAsDate(row[0]);
        typeId = MySQLQuery.getAsInteger(row[1]);
        vhId = MySQLQuery.getAsInteger(row[2]);
        driverId = MySQLQuery.getAsInteger(row[3]);
        plate = MySQLQuery.getAsString(row[4]);
        driver = MySQLQuery.getAsString(row[5]);
        authDoc = MySQLQuery.getAsString(row[6]);
        remPlate = MySQLQuery.getAsString(row[7]);
        prov = MySQLQuery.getAsString(row[8]);
        billNum = MySQLQuery.getAsString(row[9]);
        orderNum = MySQLQuery.getAsString(row[10]);
        cumplido = MySQLQuery.getAsString(row[11]);
        capaFull = MySQLQuery.getAsBigDecimal(row[12], false);
        ssign = (row[13] != null ? (byte[]) row[13] : null);
        esign = (row[14] != null ? (byte[]) row[14] : null);
        centerOrigId = MySQLQuery.getAsInteger(row[15]);
        centerDestId = MySQLQuery.getAsInteger(row[16]);
        enterpriseId = MySQLQuery.getAsInteger(row[17]);
        employeeId = MySQLQuery.getAsInteger(row[18]);
        cancel = MySQLQuery.getAsBoolean(row[19]);
        cancelNotes = MySQLQuery.getAsString(row[20]);
        textData = (row[21] != null ? (byte[]) row[21] : null);
        steps = MySQLQuery.getAsInteger(row[22]);
        reqSteps = MySQLQuery.getAsInteger(row[23]);
        sealNum = MySQLQuery.getAsString(row[24]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "gt_glp_trip";
    }

    public static String getSelFlds(String alias) {
        return new GtGlpTrip().getSelFldsForAlias(alias);
    }

    public static List<GtGlpTrip> getList(MySQLQuery q, Connection conn) throws Exception {
        return new GtGlpTrip().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new GtGlpTrip().deleteById(id, conn);
    }

    public static List<GtGlpTrip> getAll(Connection conn) throws Exception {
        return new GtGlpTrip().getAllList(conn);
    }

//fin zona de reemplazo
}
