package api.est.model;

import java.sql.Connection;
import java.util.Date;
import utilities.logs.Log;
import utilities.logs.LogType;

public class EstLog extends Log {

    public static final LogType PRICE_LIST = new LogType(1);
    public static final LogType MODIFIED_BILL = new LogType(2);
    public static final LogType EST_CLIENT = new LogType(3);
    public static final LogType ALERT_CONSUM = new LogType(4);
    public static final LogType PRICE_TYPE = new LogType(5);
    public static final LogType RES_BILL = new LogType(6);
    public static final LogType RES_REM = new LogType(7);
    public static final LogType CATEG_TYPE = new LogType(8);
    public static final LogType TANK_CATEG = new LogType(9);
    public static final LogType EST_OFFICE = new LogType(10);
    public static final LogType EST_CAR = new LogType(11);
    public static final LogType EST_STORE = new LogType(12);
    public static final LogType EST_TANK_NORM = new LogType(13);
    public static final LogType EST_SALE = new LogType(14,"SELECT CONCAT('Factura ',bill_num,'  Fecha ',  DATE_FORMAT(sale_date, '%d/%m/%Y')) FROM est_sale WHERE id = ?1", "");
    public static final LogType EST_PATH = new LogType(15);
    public static final LogType EST_EXT_CLIE = new LogType(16);
    public static final LogType EST_EXEC_REG = new LogType(17);
    public static final LogType EST_SYS_CENTER = new LogType(18);
    public static final LogType EST_PROG = new LogType(19);
    public static final LogType EST_SCHEDULE = new LogType(20);  
    public static final LogType EST_CLIE_NOV = new LogType(21);  //último creado 
    

    private static final EstLog LOG = new EstLog();

    public static void createSystemLog(LogType type, String notes, int empId, Connection ep) throws Exception {
        LOG.createLogNs(null, type, notes, empId, ep);
    }

    public static void createLog(Integer ownerId, LogType type, String notes, int empId, Connection ep) throws Exception {
        LOG.createLogNs(ownerId, type, notes, empId, ep);
    }

    public static String getLogQuery(Integer ownerId, LogType type, String notes, int empId, Connection ep) throws Exception {
        return LOG.getLogQueryNs(ownerId, type, notes, empId, ep);
    }

    public static String getLogQuery(Integer ownerId, LogType type, String notes, Date d, int empId, Connection ep) throws Exception {
        return LOG.getLogQueryNs(ownerId, type, notes, d, empId, ep);
    }

    @Override
    public String getTableName() {
        return "est_log";
    }
}
