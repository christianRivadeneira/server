package web.push;

import api.crm.model.CrmTask;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import utilities.Dates;
import utilities.MySQLQuery;

@Stateless
public class TimerPushBean {

    @Resource
    TimerService timerService;

    public void createTimer(CrmTask task) {

        Iterator<Timer> it = timerService.getAllTimers().iterator();

        while (it.hasNext()) {
            Timer t = it.next();
            if (t.getInfo() != null && t.getInfo() instanceof CrmTask) {
                if (task.id == ((CrmTask) t.getInfo()).id) {
                    t.cancel();
                    timerService.getAllTimers().remove(t);
                }
            }
        }
        if (task.remDate != null) {
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(task.remDate);
            ScheduleExpression sh = new ScheduleExpression();
            sh.second(gc.get(GregorianCalendar.SECOND));
            sh.minute(gc.get(GregorianCalendar.MINUTE));
            sh.hour(gc.get(GregorianCalendar.HOUR_OF_DAY));
            sh.dayOfMonth(gc.get(GregorianCalendar.DAY_OF_MONTH));
            sh.year(gc.get(GregorianCalendar.YEAR));
            timerService.createCalendarTimer(sh, new TimerConfig(task, true));
        }
    }

    @Timeout
    public void execute(Timer timer) throws SQLException {
        Connection conn = null;
        try {
//            System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX ENTROOOOOOO");
//            CrmTask obj = ((CrmTask) timer.getInfo());
//            Boolean existTask = new MySQLQuery("SELECT COUNT(*)>0 FROM crm_task WHERE id  = " + obj.id).getAsBoolean(conn);
//            if (existTask == null || !existTask) {
//                return;
//            }
//            String clientName = new MySQLQuery("SELECT name FROM crm_client c WHERE c.id = " + obj.clientId).getAsString(conn);
//            String creatorName = new MySQLQuery("SELECT CONCAT(first_name, ' ', last_name ) FROM employee where id = " + obj.creatorId).getAsString(conn);
//            String execMsg = "El día de hoy tiene pendiente una tarea del cliente: " + clientName;
//
//            JsonObjectBuilder rob = Json.createObjectBuilder();
//            rob.add("subject", "Recordatorio de Actividad");
//            rob.add("brief", obj.descShort);
//            rob.add("type", "reminderActivity");
//            rob.add("message", execMsg + ":\n\n" + obj.descShort);
//            rob.add("user", creatorName);
//            rob.add("dt", Dates.getCheckFormat().format(new Date()));
//
//            GCMUtils.sendToAppManagers(rob.build(), String.valueOf(obj.respId), conn);

        } catch (Exception e) {
            Logger.getLogger(TimerPushBean.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            conn.close();
        }
    }
}
