package web.vicky.clients;

import java.sql.Connection;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Startup;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@Singleton
@Startup
public class CloseSessionTimer {

    private static final Logger LOG = Logger.getLogger(CloseSessionTimer.class.getName());

    @PostConstruct
    public void reset() {
        try {
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Schedule(minute = "*/30", hour = "*")
    private void closeSessions() {
        try (Connection con = MySQLCommon.getConnection("sigmads", null)) {
            new MySQLQuery("UPDATE clie_session SET end_time = NOW() WHERE TIMESTAMPDIFF(MINUTE, last_activity, NOW()) > 30").executeUpdate(con);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}
