package web.platform;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@Startup
@Singleton
public class AutoCloseSessions {

    @Schedule(hour = "0", second = "0", minute = "0", dayOfMonth = "*")
    public void clearIdle() {
        try (Connection con = MySQLCommon.getConnection("sigmads", null)) {
            Integer minutes = new MySQLQuery("SELECT idle_minutes FROM sys_cfg").getAsInteger(con);
            if (minutes != null && minutes != 0) {
                new MySQLQuery("UPDATE session_login s "
                        + "set s.end_time = now() "
                        + "WHERE "
                        + "TIMESTAMPDIFF(MINUTE, s.last_req, now()) > " + (18 * 60) + " AND s.`type` = 'pc' AND s.end_time IS NULL").executeUpdate(con);
            }
        } catch (Exception ex) {
            Logger.getLogger(AutoCloseSessions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
