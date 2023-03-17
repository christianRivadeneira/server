package model.billing;

import java.sql.Connection;
import utilities.MySQLQuery;
import java.util.Date;
import java.util.List;

public class BillAlertReadings {
//inicio zona de reemplazo

    public int id;
    public Date regDate;
    public int spanId;
    public Integer cityId;
    public Integer billInstanceId;
    public Integer buildingId;
    public int clientId;
    public boolean noMeter;
    public boolean discon;
    public boolean emailSent;
    public Integer reviewedBy;
    public Date reviewDate;
    public String notes;
    public String motive;

    private static final String SEL_FLDS = "`reg_date`, "
            + "`span_id`, "
            + "`city_id`, "
            + "`bill_instance_id`, "
            + "`building_id`, "
            + "`client_id`, "
            + "`no_meter`, "
            + "`discon`, "
            + "`email_sent`, "
            + "`reviewed_by`, "
            + "`review_date`, "
            + "`notes`, "
            + "`motive`";

    private static final String SET_FLDS = "bill_alert_readings SET "
            + "`reg_date` = ?1, "
            + "`span_id` = ?2, "
            + "`city_id` = ?3, "
            + "`bill_instance_id` = ?4, "
            + "`building_id` = ?5, "
            + "`client_id` = ?6, "
            + "`no_meter` = ?7, "
            + "`discon` = ?8, "
            + "`email_sent` = ?9, "
            + "`reviewed_by` = ?10, "
            + "`review_date` = ?11, "
            + "`notes` = ?12, "
            + "`motive` = ?13";

    private static void setFields(BillAlertReadings obj, MySQLQuery q) {
        q.setParam(1, obj.regDate);
        q.setParam(2, obj.spanId);
        q.setParam(3, obj.cityId);
        q.setParam(4, obj.billInstanceId);
        q.setParam(5, obj.buildingId);
        q.setParam(6, obj.clientId);
        q.setParam(7, obj.noMeter);
        q.setParam(8, obj.discon);
        q.setParam(9, obj.emailSent);
        q.setParam(10, obj.reviewedBy);
        q.setParam(11, obj.reviewDate);
        q.setParam(12, obj.notes);
        q.setParam(13, obj.motive);

    }

    public static BillAlertReadings getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        BillAlertReadings obj = new BillAlertReadings();
        obj.regDate = MySQLQuery.getAsDate(row[0]);
        obj.spanId = MySQLQuery.getAsInteger(row[1]);
        obj.cityId = MySQLQuery.getAsInteger(row[2]);
        obj.billInstanceId = MySQLQuery.getAsInteger(row[3]);
        obj.buildingId = MySQLQuery.getAsInteger(row[4]);
        obj.clientId = MySQLQuery.getAsInteger(row[5]);
        obj.noMeter = MySQLQuery.getAsBoolean(row[6]);
        obj.discon = MySQLQuery.getAsBoolean(row[7]);
        obj.emailSent = MySQLQuery.getAsBoolean(row[8]);
        obj.reviewedBy = MySQLQuery.getAsInteger(row[9]);
        obj.reviewDate = MySQLQuery.getAsDate(row[10]);
        obj.notes = MySQLQuery.getAsString(row[11]);
        obj.motive = MySQLQuery.getAsString(row[12]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    private static final String TO_STR_FLDS = ")";

    public BillAlertReadings select(int id, Connection conn) throws Exception {
        return BillAlertReadings.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(conn));
    }

    public int insert(BillAlertReadings pobj, Connection conn) throws Exception {
        BillAlertReadings obj = (BillAlertReadings) pobj;
        int nId = new MySQLQuery(BillAlertReadings.getInsertQuery(obj)).executeInsert(conn);
        obj.id = nId;
        return nId;
    }

    public void update(BillAlertReadings pobj, Connection conn) throws Exception {
        new MySQLQuery(BillAlertReadings.getUpdateQuery((BillAlertReadings) pobj)).executeUpdate(conn);
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM bill_alert_readings WHERE id = " + id;
    }

    public static String getInsertQuery(BillAlertReadings obj) {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.getQuery();
    }

    public static String getMultiInsertQuery(List<BillAlertReadings> objs) {
        StringBuilder fields = new StringBuilder();
        for (int i = 0; i < SEL_FLDS.split(",").length; i++) {
            fields.append("?").append((i + 1)).append(", ");
        }
        fields.deleteCharAt(fields.length() - 2);
        String values = fields.toString();

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO sigma.bill_alert_readings (" + SEL_FLDS + ") VALUES");
        for (int i = 0; i < objs.size(); i++) {
            BillAlertReadings obj = objs.get(i);
            MySQLQuery q = new MySQLQuery("( " + values + " )");
            setFields(obj, q);
            sb.append(q.getParametrizedQuery()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public static String getUpdateQuery(BillAlertReadings obj) {
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        return q.getQuery();
    }

    public static String getEnumOptions(String fieldName) {
        if (fieldName.equals("motive")) {
            return ""
                    + "con_sm=Registra Consumo sin Medidor&"
                    + "con_ss=Registra Consumo sin Servicio&"
                    + "ss_con=Registra sin Servicio Teniendo Consumo&"
                    + "sm_con=Registra sin Medidor Teniendo Consumo ";
        }
        return null;
    }

}
