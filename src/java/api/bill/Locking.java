package api.bill;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

public class Locking {

    public static final synchronized void lock(int instId) throws Exception {
        try (Connection conn = MySQLCommon.getDefaultConnection()) {
            if (new MySQLQuery("SELECT locked FROM sigma.bill_instance i WHERE i.id = ?1").setParam(1, instId).getAsBoolean(conn)) {
                throw new Exception("En proceso de cierre o importación.\nIntente nuevamente más tarde.");
            }
            new MySQLQuery("UPDATE sigma.bill_instance SET locked = 1 WHERE id = ?1").setParam(1, instId).executeUpdate(conn);
        }
    }

    public static synchronized void unlock(int instId) {
        try (Connection conn = MySQLCommon.getDefaultConnection()) {
            new MySQLQuery("UPDATE sigma.bill_instance SET locked = 0 WHERE id = ?1").setParam(1, instId).executeUpdate(conn);
        } catch (Exception ex) {
            Logger.getLogger(Locking.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
