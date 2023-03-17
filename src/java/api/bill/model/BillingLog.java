package api.bill.model;

import java.sql.Connection;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import utilities.logs.LogType;

public class BillingLog {

    public static final LogType BILL_CLIENT = new LogType(1);
    public static final LogType BILL_CLIENT_NOTE = new LogType(2);
    public static final LogType BILL_DTO = new LogType(3);
    public static final LogType BILL_SPAN = new LogType(4);
    public static final LogType BILL_CLIENT_INSPECTION = new LogType(5);
    public static final LogType BILL_PRICE_LIST = new LogType(6);
    public static final LogType BILL_USR_SERVICE = new LogType(7);
    public static final LogType BILL_FACTOR = new LogType(8);
    public static final LogType BILL_READING = new LogType(9);
    public static final LogType BILL_SUSP = new LogType(10);
    public static final LogType BILL_CFG = new LogType(11);
    public static final LogType BILL_BUILDING = new LogType(12);
    public static final LogType BILL_IMPORT_ASO = new LogType(13);
    public static final LogType BILL_MARKET = new LogType(14);//ultimo

    public static void createLogQuery(Integer instId, int ownerId, LogType type, String notes, SessionLogin sess, Connection con) throws Exception {
        new MySQLQuery("INSERT INTO sigma.billing_log SET "
                + "`owner_id` = " + ownerId + ", "
                + "`owner_type` = " + type.id + ", "
                + "`employee_id` =  " + sess.employeeId + ", "
                + "`log_date` = NOW(), "
                + "`billing_city_id` = ?1, "
                + "`notes` = ?2").setParam(1, instId).setParam(2, notes).executeInsert(con);
    }
}
