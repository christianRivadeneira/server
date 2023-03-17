package utilities;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import service.MySQL.MySQLCommon;

@Singleton
@Startup
public class CleanFailedQueries {

    @Schedule(dayOfWeek = "2", hour = "21", minute = "5")
    protected void processRequest() {
        try (Connection conn = MySQLCommon.getConnection("sigmads", null)) {
            new MySQLQuery("DELETE FROM sys_failed_query WHERE dt < DATE_SUB(NOW(), INTERVAL 3 MONTH)").executeDelete(conn);
        } catch (Exception e) {
            Logger.getLogger(CleanFailedQueries.class.getName()).log(Level.SEVERE, null, e);
        }
    }

}
