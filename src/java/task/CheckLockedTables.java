//package task;
//
//import java.sql.Connection;
//import java.sql.Statement;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.Resource;
//import javax.ejb.ScheduleExpression;
//import javax.ejb.Singleton;
//import javax.ejb.Startup;
//import javax.ejb.Timeout;
//import javax.ejb.Timer;
//import javax.ejb.TimerConfig;
//import javax.ejb.TimerService;
//import service.MySQL.MySQLCommon;
//import utils.MySQLQuery;
//import web.quality.sendMail;
//
//@Startup
//@Singleton
//public class CheckLockedTables {
//
//    private static final Logger log = Logger.getLogger(CheckLockedTables.class.getName());
//
//    @Resource
//    private TimerService timerService;
//
//    @PostConstruct
//    public void createProgrammaticalTimer() {
//        log.log(Level.INFO, "Se inicializó el temporizador de detección de tablas bloqueadas.");
//        ScheduleExpression exp = new ScheduleExpression().minute("*/30");
//        timerService.createCalendarTimer(exp, new TimerConfig("", false));
//    }
//
//    @Timeout
//    public void handleTimer(final Timer timer) {
//        log.info("Inicio chequeo de tablas bloqueadas.");
//        Connection con = null;
//        Statement st = null;
//        int samples = 10;
//        try {
//            log.info("Inicio chequeo de tablas bloqueadas.");
//            con = MySQLCommon.getConnection("sigmads", null);
//            st = con.createStatement();
//
//            Map<String, Integer> tabs = new HashMap<>();
//
//            for (int i = 0; i < samples; i++) {
//                Object[][] data = new MySQLQuery("show open tables where in_use <> 0;").getRecords(con);
//                for (Object[] row : data) {
//                    String tableName = row[1].toString();
//                    if (tabs.containsKey(tableName)) {
//                        tabs.replace(tableName, tabs.get(tableName) + 1);
//                    } else {
//                        tabs.put(tableName, 0);
//                    }
//                }
//                Thread.sleep(1000);
//            }
//
//            boolean locked = false;
//            Set<Map.Entry<String, Integer>> es = tabs.entrySet();
//            for (Map.Entry<String, Integer> e : es) {
//                if (e.getValue() > samples / 2) {
//                    locked = true;
//                }
//            }
//            if (locked) {
//                sendMail.sendMail(st, "karol.mendoza@montagas.com.co", "Posible bloqueo de tablas", "Posible bloqueo de tablas", "Posible bloqueo de tablas");
//            }
//        } catch (Exception ex) {
//            Logger.getLogger(CheckLockedTables.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            MySQLCommon.closeConnection(con, st);
//        }
//    }
//}
