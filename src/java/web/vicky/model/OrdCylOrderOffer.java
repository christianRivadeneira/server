package web.vicky.model;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import utilities.MySQLQuery;
import web.push.GCMUtils;

/**
 * El generador coloca el acceptDevice en Integer, pero al ser unsigned debería
 * ser Long
 */
public class OrdCylOrderOffer {
//inicio zona de reemplazo

    public int id;
    public int orderId;
    public int empId;
    public int vhId;
    public Integer backoffCauseId;
    public Date offerDt;
    public Date ackDt;
    public Long acceptDevice;
    public Date acceptDt;
    public Date rejectDt;
    public Date timeoutDt;
    public Date confirmDt;
    public Date cancelDt;
    public Date backoffDt;
    public Date arriveDt;
    public BigDecimal lat;
    public BigDecimal lon;
    public int round;
    public Boolean appRunning;
    public String ackType;
    public String log;

    /**
     * El generador coloca el acceptDevice en Integer, pero al ser unsigned
     * debería ser Long
     */
    private static final String SEL_FLDS = "`order_id`, "
            + "`emp_id`, "
            + "`vh_id`, "
            + "`backoff_cause_id`, "
            + "`offer_dt`, "
            + "`ack_dt`, "
            + "`accept_device`, "
            + "`accept_dt`, "
            + "`reject_dt`, "
            + "`timeout_dt`, "
            + "`confirm_dt`, "
            + "`cancel_dt`, "
            + "`backoff_dt`, "
            + "`arrive_dt`, "
            + "`lat`, "
            + "`lon`, "
            + "`round`, "
            + "`app_running`, "
            + "`ack_type`, "
            + "`log`";

    private static final String SET_FLDS = "ord_cyl_order_offer SET "
            + "`order_id` = ?1, "
            + "`emp_id` = ?2, "
            + "`vh_id` = ?3, "
            + "`backoff_cause_id` = ?4, "
            + "`offer_dt` = ?5, "
            + "`ack_dt` = ?6, "
            + "`accept_device` = ?7, "
            + "`accept_dt` = ?8, "
            + "`reject_dt` = ?9, "
            + "`timeout_dt` = ?10, "
            + "`confirm_dt` = ?11, "
            + "`cancel_dt` = ?12, "
            + "`backoff_dt` = ?13, "
            + "`arrive_dt` = ?14, "
            + "`lat` = ?15, "
            + "`lon` = ?16, "
            + "`round` = ?17, "
            + "`app_running` = ?18, "
            + "`ack_type` = ?19, "
            + "`log` = ?20";

    private static void setFields(OrdCylOrderOffer obj, MySQLQuery q) {
        q.setParam(1, obj.orderId);
        q.setParam(2, obj.empId);
        q.setParam(3, obj.vhId);
        q.setParam(4, obj.backoffCauseId);
        q.setParam(5, obj.offerDt);
        q.setParam(6, obj.ackDt);
        q.setParam(7, obj.acceptDevice);
        q.setParam(8, obj.acceptDt);
        q.setParam(9, obj.rejectDt);
        q.setParam(10, obj.timeoutDt);
        q.setParam(11, obj.confirmDt);
        q.setParam(12, obj.cancelDt);
        q.setParam(13, obj.backoffDt);
        q.setParam(14, obj.arriveDt);
        q.setParam(15, obj.lat);
        q.setParam(16, obj.lon);
        q.setParam(17, obj.round);
        q.setParam(18, obj.appRunning);
        q.setParam(19, obj.ackType);
        q.setParam(20, obj.log);

    }

    public static OrdCylOrderOffer getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        OrdCylOrderOffer obj = new OrdCylOrderOffer();
        obj.orderId = MySQLQuery.getAsInteger(row[0]);
        obj.empId = MySQLQuery.getAsInteger(row[1]);
        obj.vhId = MySQLQuery.getAsInteger(row[2]);
        obj.backoffCauseId = MySQLQuery.getAsInteger(row[3]);
        obj.offerDt = MySQLQuery.getAsDate(row[4]);
        obj.ackDt = MySQLQuery.getAsDate(row[5]);
        obj.acceptDevice = MySQLQuery.getAsLong(row[6]);
        obj.acceptDt = MySQLQuery.getAsDate(row[7]);
        obj.rejectDt = MySQLQuery.getAsDate(row[8]);
        obj.timeoutDt = MySQLQuery.getAsDate(row[9]);
        obj.confirmDt = MySQLQuery.getAsDate(row[10]);
        obj.cancelDt = MySQLQuery.getAsDate(row[11]);
        obj.backoffDt = MySQLQuery.getAsDate(row[12]);
        obj.arriveDt = MySQLQuery.getAsDate(row[13]);
        obj.lat = MySQLQuery.getAsBigDecimal(row[14], false);
        obj.lon = MySQLQuery.getAsBigDecimal(row[15], false);
        obj.round = MySQLQuery.getAsInteger(row[16]);
        obj.appRunning = MySQLQuery.getAsBoolean(row[17]);
        obj.ackType = MySQLQuery.getAsString(row[18]);
        obj.log = MySQLQuery.getAsString(row[19]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public static OrdCylOrderOffer[] getByOrderId(int orderId, Connection ep) throws Exception {
        Object[][] data = new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM ord_cyl_order_offer WHERE order_id = ?1 AND offer_dt IS NOT NULL").setParam(1, orderId).getRecords(ep);
        OrdCylOrderOffer[] rta = new OrdCylOrderOffer[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = getFromRow(data[i]);
        }
        return rta;
    }

    public static OrdCylOrderOffer select(int id, Connection ep) throws Exception {
        return OrdCylOrderOffer.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(ep));
    }

    public static int insert(OrdCylOrderOffer pobj, Connection ep) throws Exception {
        OrdCylOrderOffer obj = (OrdCylOrderOffer) pobj;
        int nId = new MySQLQuery(OrdCylOrderOffer.getInsertQuery(obj)).executeInsert(ep);
        obj.id = nId;
        return nId;
    }

    public static void update(OrdCylOrderOffer pobj, Connection ep) throws Exception {
        new MySQLQuery(OrdCylOrderOffer.getUpdateQuery((OrdCylOrderOffer) pobj)).executeUpdate(ep);
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM ord_cyl_order_offer WHERE id = " + id;
    }

    public static String getInsertQuery(OrdCylOrderOffer obj) {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public static String getUpdateQuery(OrdCylOrderOffer obj) {
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public static void delete(int id, Connection ep) throws Exception {
        new MySQLQuery("DELETE FROM ord_cyl_order_offer WHERE id = " + id).executeDelete(ep);
    }

    public static boolean isDriverFree(int empId, VickyCfg cfg, Connection conn) throws Exception {
        new MySQLQuery("update ord_cyl_order_offer SET timeout_dt = NOW() "
                + "WHERE emp_id = " + empId + " AND offer_dt IS NOT NULL AND ack_dt IS NOT NULL AND accept_dt IS NULL AND reject_dt IS NULL AND timeout_dt IS NULL "
                + "AND TIMESTAMPDIFF(SECOND, offer_dt, now()) > " + (cfg.roundTimeSecs * 3)).executeUpdate(conn);

        return new MySQLQuery("SELECT COUNT(*) = 0 FROM ord_cyl_order WHERE driver_id = ?1 AND cancel_cause_id IS NULL AND confirmed_by_id IS NULL AND day = curdate();").setParam(1, empId).getAsBoolean(conn);
        
        /*
        
        //contador de ofertas pendientes
        return new MySQLQuery("SELECT COUNT(*) = 0 "
                + "FROM ord_cyl_order_offer "
                + "WHERE emp_id = ?1 AND "
                + "reject_dt  IS NULL AND "
                + "timeout_dt IS NULL AND "
                + "confirm_dt IS NULL AND "
                + "cancel_dt  IS NULL AND "
                + "backoff_dt IS NULL AND "
                + "(ack_type = 'ok' OR app_running = 1 OR TIMESTAMPDIFF(SECOND, offer_dt, now()) < " + cfg.roundTimeSecs + ")").setParam(1, empId).getAsBoolean(conn);*/
    }

    public static void cancelOffers(int orderId, VickyCfg cfg, boolean offer, Connection conn) throws Exception {
        cancelOffers(orderId, cfg, null, offer, conn);
    }

    public static void cancelOffers(int orderId, VickyCfg cfg, Integer skipEmpId, boolean offer, Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT group_concat(cast(id as char)), group_concat(cast(emp_id as char)) "
                + "FROM ord_cyl_order_offer "
                + "WHERE timeout_dt IS NULL "
                + "AND order_id = ?1 "
                + (skipEmpId != null ? "AND emp_id <> " + skipEmpId + " " : "")
        );
        Object[] offerRow = q.setParam(1, orderId).getRecord(conn);

        if (offerRow != null && offerRow.length > 0) {
            String ids = offerRow[0].toString();
            String emps = offerRow[1].toString();

            new MySQLQuery("UPDATE ord_cyl_order_offer SET timeout_dt = NOW() "
                    + "WHERE "
                    + "id IN (" + ids + ")").setParam(1, orderId).executeUpdate(conn);

            JsonObjectBuilder ob = Json.createObjectBuilder();
            ob.add("orderId", orderId);
            ob.add("type", "offerExp");
            ob.add("isOffer", offer ? "1" : "0");
            JsonObject json = ob.build();
            GCMUtils.sendToAppAsync(cfg.appId, json, emps);
        }
    }

    public static OrdCylOrderOffer[] getAll(Connection ep) throws Exception {
        Object[][] data = new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM ord_cyl_order_offer").getRecords(ep);
        OrdCylOrderOffer[] rta = new OrdCylOrderOffer[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = getFromRow(data[i]);
        }
        return rta;
    }

}
