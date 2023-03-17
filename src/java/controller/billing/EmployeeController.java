package controller.billing;

import java.sql.Connection;
import utilities.MySQLQuery;
import web.billing.BillingServlet;

public class EmployeeController {

    public static String getEmployeeName(Object employeeId) throws Exception {
        try (Connection gralConn = BillingServlet.getConnection()) {
            return getEmployeeName(employeeId, gralConn);
        }
    }

    public static String getEmployeeName(Object employeeId, Connection conn) throws Exception {
        if (employeeId == null) {
            return null;
        }
        String name = new MySQLQuery("SELECT CONCAT(first_name, ' ', last_name) FROM employee WHERE id = " + MySQLQuery.getAsInteger(employeeId)).getAsString(conn);
        return name != null ? name : "";
    }
    
    public static String getEmployeeShortName(Object employeeId, Connection conn) throws Exception {
        if (employeeId == null) {
            return null;
        }
        String name = new MySQLQuery("SELECT short_name FROM employee WHERE id = " + MySQLQuery.getAsInteger(employeeId)).getAsString(conn);
        return name != null ? name : "";
    }

}
