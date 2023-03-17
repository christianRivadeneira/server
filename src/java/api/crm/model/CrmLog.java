package api.crm.model;

import java.sql.Connection;
import java.util.Date;
import utilities.logs.Log;
import utilities.logs.LogType;

public class CrmLog extends Log {

    public static final LogType CLIENT = new LogType(1);
    public static final LogType CL_TASK = new LogType(2);
    public static final LogType CL_PROJECT = new LogType(3);
    public static final LogType CRM_GPRS = new LogType(4);
    public static final LogType CRM_TYPE_TASK = new LogType(5);
    public static final LogType CRM_CFG = new LogType(6);

    private static final CrmLog LOG = new CrmLog();

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
        return "crm_log";
    }
}
