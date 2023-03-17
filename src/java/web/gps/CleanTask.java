package web.gps;

import java.sql.Connection;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Startup;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import utilities.SysTask;

@Singleton
@Startup
public class CleanTask {

    private static final Logger LOG = Logger.getLogger(CleanTask.class.getName());

    @PostConstruct
    public void reset() {
        try {
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Schedule(hour = "2", minute = "1")
    private void clean() {
        try (Connection con = MySQLCommon.getConnection("sigmads", null)) {
            SysTask t = new SysTask(CleanTask.class, 1, con);
            try {
                GregorianCalendar gc = new GregorianCalendar();
                gc.set(GregorianCalendar.HOUR_OF_DAY, 23);
                gc.set(GregorianCalendar.MINUTE, 59);
                gc.set(GregorianCalendar.SECOND, 59);
                gc.set(GregorianCalendar.MILLISECOND, 0);
                Date end = gc.getTime();

                gc.add(GregorianCalendar.MONTH, -2);
                gc.set(GregorianCalendar.HOUR_OF_DAY, 0);
                gc.set(GregorianCalendar.MINUTE, 0);
                gc.set(GregorianCalendar.SECOND, 0);
                Date beg = gc.getTime();

                Integer count = new MySQLQuery("SELECT COUNT(*) FROM gps_coordinate WHERE date NOT BETWEEN ?1 AND ?2").setParam(1, beg).setParam(2, end).getAsInteger(con);

                int blocks = (int) Math.ceil(count / 50000d);
                for (int i = 0; i < blocks; i++) {
                    new MySQLQuery("DELETE FROM gps_coordinate WHERE date NOT BETWEEN ?1 AND ?2 LIMIT 50000").setParam(1, beg).setParam(2, end).executeUpdate(con);
                }

                GregorianCalendar gcOpt = new GregorianCalendar();
                gcOpt.add(GregorianCalendar.DAY_OF_MONTH, -1);
                ClearGpsData.optimize(gcOpt.getTime(), 40);

                gcOpt.add(GregorianCalendar.DAY_OF_MONTH, -30);
                ClearGpsData.optimize(gcOpt.getTime(), 100);

                t.success(con);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, null, ex);
                t.error(ex, con);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}
