package web.system;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.servlet.http.HttpServlet;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@Singleton
@Startup
public class PurgeMySQLLogs extends HttpServlet {

    @Schedule(hour = "1", minute = "00")
    protected void processRequest() {
        try (Connection con = MySQLCommon.getConnection("sigmads", null)) {
            new MySQLQuery("purge binary logs before now()").executeUpdate(con);
        } catch (Exception e) {
            Logger.getLogger(PurgeMySQLLogs.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
