package utilities.logs;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import utilities.MySQLQuery;

public abstract class Log {

    public abstract String getTableName();
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void createSystemLogNs(LogType type, String notes, int empId, Connection ep) throws Exception {
        createLogNs(null, type, notes, empId, ep);
    }

    public void createLogNs(Integer ownerId, LogType type, String notes, int empId, Connection ep) throws Exception {
        if (notes == null || notes.trim().isEmpty()) {
            return;
        }
        new MySQLQuery(getLogQueryNs(ownerId, type, notes, empId, ep)).executeInsert(ep);
    }

    public String getLogQueryNs(Integer ownerId, LogType type, String notes, int empId, Connection ep) throws Exception {
        return getLogQueryNs(ownerId, type, notes, null, empId, ep);
    }

    public String getLogQueryNs(Integer ownerId, LogType type, String notes, Date d, int empId, Connection ep) throws Exception {
        return "INSERT INTO " + getTableName() + " SET "
                + "`owner_id` = " + ownerId + ", "
                + "`owner_type` = " + type.id + ", "
                + "`employee_id` =  " + empId + ", "
                + (d == null ? "`log_date` = NOW(), " : "`log_date` = '" + sdf.format(d) + "', ")
                + "`notes` = \"" + notes.replaceAll("[\"']", "") + "\"";
    }
}
