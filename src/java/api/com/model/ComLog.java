package api.com.model;

import utilities.logs.Log;
import utilities.logs.LogType;
import java.sql.Connection;
import java.util.Date;

public class ComLog extends Log {

    public static final LogType MANAGER = new LogType(1, "SELECT CONCAT(e.first_name, ' ', e.last_name) FROM com_service_manager m INNER JOIN employee e ON e.id = m.emp_id WHERE m.id = ?1", "Gestor");
    public static final LogType PROMO = new LogType(2, "SELECT name FROM com_promo p WHERE p.id = ?1", "Promoción");
    public static final LogType STORE_ORDER = new LogType(3, "SELECT o.id FROM com_store_order o WHERE o.id = ?1", "Pedido");//último creado 

    private static final ComLog LOG = new ComLog();

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
        return "com_log";
    }
}
