package web.tanks;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import utilities.SysTask;
import web.quality.SendMail;

/**
 */
@Singleton
@Startup
public class EstScheduleTask {

    @Schedule(hour = "1", minute = "5")
    protected void processRequest() {
        System.out.println("TAREA PROGRAMADA ESTACIONARIOS");
        try {
            try (Connection conn = MySQLCommon.getConnection("sigmads", null)) {
                Boolean schedTask = new MySQLQuery("SELECT sched_task "
                        + "FROM est_cfg WHERE id = 1").getAsBoolean(conn);
                if (schedTask == null || !schedTask) {
                    return;
                }

                Exception inaEx = null;
                SysTask t = new SysTask(EstScheduleTask.class, 1, conn);
                //INACTIVAR NOVEDADES 
                try {
                    inactiveNovs(conn);
                    updateSchedules(conn, "sigmads");
                } catch (Exception ex) {
                    Logger.getLogger(EstScheduleTask.class.getName()).log(Level.SEVERE, null, ex);
                    inaEx = ex;
                }

                t.success(conn);
                if (inaEx != null) {
                    try {
                        StringBuilder sb = new StringBuilder();
                        stackTrace(inaEx, sb, "Inactivar Novedades");
                        String msg = sb.toString();
                        SendMail.sendMail(conn, "karol.mendoza@montagas.com.co", "Error en Tarea Prog. Estacionarios", msg, msg);
                    } catch (Exception ex1) {
                        Logger.getLogger(EstScheduleTask.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(EstScheduleTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void stackTrace(Exception ex, StringBuilder sb, String name) {
        sb.append("--------------------");
        sb.append("\r\n");
        sb.append(name);
        sb.append("\r\n");
        sb.append(ex != null ? ex.getClass().getName() : " ");
        sb.append("\r\n");
        if (ex != null && ex.getMessage() != null) {
            sb.append(ex.getMessage());
            sb.append("\r\n");
        }

        if (ex != null) {
            StackTraceElement[] traces = ex.getStackTrace();
            for (int i = 0; i < Math.min(10, traces.length); i++) {
                sb.append(traces[i].toString());
                sb.append("\r\n");
            }
        }
    }

    private void inactiveNovs(Connection conn) throws Exception {
        Object[][] objs = new MySQLQuery("SELECT n.id, "
                + "n.num_visit, "
                + "COUNT(v.id) AS tot "
                + "FROM est_sede_nov n "
                + "LEFT JOIN est_sale v ON n.clie_tank_id = v.client_id AND v.sale_date BETWEEN n.dt AND CURDATE() "
                + "WHERE n.active = 1 "
                + "GROUP BY n.id "
                + "HAVING (tot >= n.num_visit)").getRecords(conn);

        if (objs != null && objs.length > 0) {
            String upd = "UPDATE est_sede_nov SET active = 0 WHERE id IN (";

            for (int i = 0; i < objs.length; i++) {
                upd += objs[i][0] + (i == objs.length - 1 ? ")" : ",");
            }
            new MySQLQuery(upd).executeUpdate(conn);
        }
    }

    private void updateSchedules(Connection conn, String poolName) throws Exception {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        Object[][] objs = new MySQLQuery("SELECT "
                + "p.prog_date, "
                + "p.vh_id "
                + "FROM est_prog p "
                + "WHERE p.prog_date = CURDATE()").getRecords(conn);

        if (objs != null && objs.length > 0) {
            for (Object[] row : objs) {
                EstScheduleServlet.insertScheduledPath(poolName, sf.format(row[0]), MySQLQuery.getAsInteger(row[1]));
            }
        }
    }
}
