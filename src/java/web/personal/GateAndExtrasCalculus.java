package web.personal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import service.MySQL.MySQLCommon;
import utilities.Dates;
import utilities.MySQLQuery;
import utilities.SysTask;
import web.quality.SendMail;

/**
 * Debe llamarse el día en que se crean los eventos de portería, genera las
 * extras del día anterior.
 */
@Singleton
@Startup
public class GateAndExtrasCalculus {

    @Schedule(hour = "3", minute = "5")
    protected void processRequest() {
        System.out.println("EXTRAS Y EVENTOS DE PORTERÍA");
        try {
            try (Connection conn = MySQLCommon.getConnection("sigmads", null)) {

                if (!new MySQLQuery("SELECT send_th_mail FROM sys_cfg WHERE id = 1").getAsBoolean(conn)) {
                    return;
                }

                Exception evEx = null;
                Exception exEx = null;
                Exception fpEx = null;
                SysTask t = new SysTask(GateAndExtrasCalculus.class, 1, conn);
                Date today = Dates.trimDate(new GregorianCalendar().getTime());
                //EVENTOS DE PORTERÍA
                try {
                    GateEvents.generate(conn, today);
                } catch (Exception ex) {
                    Logger.getLogger(GateAndExtrasCalculus.class.getName()).log(Level.SEVERE, null, ex);
                    evEx = ex;
                }

                try {
                    if (new MySQLQuery("SELECT workday FROM per_cfg WHERE id = 1").getAsBoolean(conn)) {
                        ExtrasClc.generate(conn);
                    } else {
                        extras(today, conn);
                    }
                } catch (Exception ex) {
                    exEx = ex;
                }

                try {
                    removeUnusedFprints(conn);
                } catch (Exception ex) {
                    fpEx = ex;
                }

                t.success(conn);
                if (evEx != null || exEx != null || fpEx != null) {
                    try {
                        StringBuilder sb = new StringBuilder();
                        stackTrace(evEx, sb, "Eventos");
                        stackTrace(exEx, sb, "Extras");
                        stackTrace(fpEx, sb, "Huellas");
                        String msg = sb.toString();
                        SendMail.sendMail(conn, "karol.mendoza@montagas.com.co", "Error en Extras", msg, msg);
                    } catch (Exception ex1) {
                        Logger.getLogger(GateAndExtrasCalculus.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(GateAndExtrasCalculus.class.getName()).log(Level.SEVERE, null, ex);
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

    private void removeUnusedFprints(Connection conn) throws Exception {
        Boolean removeF = new MySQLQuery("SELECT cfg.remove_fprints FROM per_cfg cfg").getAsBoolean(conn);
        if (removeF) {//solo si esta activada la variable 
            //empleados que no han pasado por porterías en los últimos 120 días.
            Object[][] empData = new MySQLQuery("SELECT "
                    + "emp_id "
                    + "FROM "
                    + "per_gate_event "
                    + "WHERE reg_hour is not null "
                    + "GROUP BY emp_id "
                    + "HAVING max(event_day) < DATE_SUB(now(),INTERVAL 120 day) AND "
                    + "(SELECT MAX(assigned) "
                    + "FROM per_emp_prof ep "
                    + "WHERE ep.emp_id = emp_id) < DATE_SUB(now(),INTERVAL 20 day)").getRecords(conn);

            if (empData.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < empData.length; i++) {
                    Object[] row = empData[i];
                    sb.append(row[0]);
                    if (i + 1 < empData.length) {
                        sb.append(",");
                    }
                }
                new MySQLQuery("DELETE FROM per_emp_prof WHERE emp_id IN (" + sb.toString() + ")").executeUpdate(conn);
                new MySQLQuery("DELETE FROM per_fprint WHERE emp_id IN (" + sb.toString() + ")").executeUpdate(conn);
                new MySQLQuery("UPDATE per_gate_prof SET active = 0 where id not in (select distinct prof_id from per_emp_prof);").executeUpdate(conn);
            }
        }
    }

    public static void extras(Date today, Connection conn) throws Exception {
        today = Dates.trimDate(today);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(today);

        //fecha de ayer            
        gc.add(GregorianCalendar.DAY_OF_MONTH, -1);
        int dayOfWeek = gc.get(GregorianCalendar.DAY_OF_WEEK);
        Date yester = Dates.trimDate(gc.getTime());

        //fecha de pago, siempre se paga en la primera quincena del mes siguiente
        gc = new GregorianCalendar();
        gc.setTime(yester);
        gc.add(GregorianCalendar.MONTH, 1);
        gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
        Date payDate = gc.getTime();

        //buscar si ya hay horas extras registradas
        MySQLQuery q = new MySQLQuery("SELECT COUNT(*) > 0 FROM `per_extra` WHERE reg_date = ?1 AND input_type = 'gate'");
        q.setParam(1, yester);
        Boolean extras = q.getAsBoolean(conn);
        extras = (extras == null ? false : extras);
        if (extras) {
            throw new Exception("Ya se registraron extras de portería.");
        }

        //CASO ESPECIAL REMBERTO
        //empleados que dejaron eventos sin hora registrada
        MySQLQuery openQ = new MySQLQuery("SELECT DISTINCT emp_id FROM per_gate_event WHERE event_day = ?1 AND reg_hour IS NULL;");
        openQ.setParam(1, yester);
        Object[][] openeds = openQ.getRecords(conn);
        for (int i = 0; i < openeds.length; i++) {
            MySQLQuery lastEventQ = new MySQLQuery("select id, exp_hour, reg_hour from per_gate_event where emp_id = ?1 AND event_day = ?2 ORDER BY exp_hour DESC LIMIT 0,1");
            lastEventQ.setParam(1, openeds[i][0]);
            lastEventQ.setParam(2, yester);

            Object[] evRow = lastEventQ.getRecord(conn);
            if (evRow[2] == null) {
                MySQLQuery gateSpanQ = new MySQLQuery("SELECT id, beg_hour FROM per_gate_span s WHERE s.emp_id = ?1 AND s.event_day = ?2 AND type = 'full' AND end_hour IS NULL");
                gateSpanQ.setParam(1, openeds[i][0]);
                gateSpanQ.setParam(2, today);
                Object[][] gateEvs = gateSpanQ.getRecords(conn);
                if (gateEvs.length == 1) {
                    int eventId = MySQLQuery.getAsInteger(evRow[0]);
                    int spanId = MySQLQuery.getAsInteger(gateEvs[0][0]);
                    new MySQLQuery("UPDATE per_gate_event SET reg_hour = '23:59:59' WHERE id = " + eventId).executeUpdate(conn);
                    new MySQLQuery("UPDATE per_gate_span SET end_hour = beg_hour WHERE id = " + spanId).executeUpdate(conn);
                    new MySQLQuery("UPDATE per_gate_span SET beg_hour = '00:00:00' WHERE id = " + spanId).executeUpdate(conn);
                }
            }
        }

        //CASO ALICIA FLOREZ
        //empleados que dejaron jonadas adicionales sin hora final
        MySQLQuery openQ1 = new MySQLQuery("SELECT DISTINCT emp_id FROM per_gate_span WHERE event_day = ?1 AND end_hour IS NULL AND type = 'full' ORDER BY beg_hour DESC LIMIT 0, 1");
        openQ1.setParam(1, yester);
        Object[][] openeds1 = openQ1.getRecords(conn);
        for (int i = 0; i < openeds1.length; i++) {
            //ultima jornada adicional de un empleado en una fecha
            MySQLQuery lastEventQ1 = new MySQLQuery("SELECT id, beg_hour, end_hour from per_gate_span where emp_id = ?1 AND event_day = ?2 ORDER BY beg_hour DESC LIMIT 0,1");
            lastEventQ1.setParam(1, openeds1[i][0]);
            lastEventQ1.setParam(2, yester);
            Object[] evRow = lastEventQ1.getRecord(conn);
            if (evRow[2] == null) {
                //evento en portería de la fecha de hoy
                MySQLQuery gateSpanQ1 = new MySQLQuery("SELECT id, beg_hour FROM per_gate_span s WHERE s.emp_id = ?1 AND s.event_day = ?2 AND type = 'full' AND end_hour IS NULL");
                gateSpanQ1.setParam(2, today);
                gateSpanQ1.setParam(1, openeds1[i][0]);
                Object[][] gateEvs = gateSpanQ1.getRecords(conn);
                if (gateEvs.length == 1) {
                    int eventId = (Integer) evRow[0];
                    int spanId = (Integer) gateEvs[0][0];
                    new MySQLQuery("UPDATE per_gate_span SET end_hour = '23:59:59' WHERE id = " + eventId).executeUpdate(conn);
                    new MySQLQuery("UPDATE per_gate_span SET end_hour = beg_hour WHERE id = " + spanId).executeUpdate(conn);
                    new MySQLQuery("UPDATE per_gate_span SET beg_hour = '00:00:00' WHERE id = " + spanId).executeUpdate(conn);
                }
            }
        }

        //dominical ú ordinaria, preguntar si es domingo, buscar en la lista de festivos
        boolean dom = (dayOfWeek == GregorianCalendar.SUNDAY);
        if (!dom) {
            q = new MySQLQuery("SELECT COUNT(*) > 0 FROM per_holiday WHERE holi_date = ?1");
            q.setParam(1, yester);
            Boolean r = q.getAsBoolean(conn);
            if ((r == null ? false : r)) {
                dom = true;
            }
        }

        //diurna o nocturna
        q = new MySQLQuery("SELECT per_cfg.diu_begin, per_cfg.diu_end, extra_lim FROM per_cfg WHERE per_cfg.id = 1");
        Object[] cfg = q.getRecord(conn);
        int[][] dayParts = new int[3][2];
        dayParts[0][0] = 0;
        dayParts[0][1] = toSecs((Date) cfg[0]);

        dayParts[1][0] = toSecs((Date) cfg[0]);
        dayParts[1][1] = toSecs((Date) cfg[1]);

        dayParts[2][0] = toSecs((Date) cfg[1]);
        dayParts[2][1] = 86400;

        //EXTRAS DE JORNADAS ADICIONALES
        q = new MySQLQuery("SELECT `id`, `emp_id`, `beg_hour`, `end_hour` FROM per_gate_span WHERE `type` = 'full' AND `event_day` = ?1 AND end_hour IS NOT NULL");
        q.setParam(1, yester);
        Object[][] extrs = q.getRecords(conn);

        for (int i = 0; i < extrs.length; i++) {
            Object[] extr = extrs[i];
            int empId = (Integer) extr[1];
            MySQLQuery contractQ = new MySQLQuery("SELECT c.employeer_id, c.pos_id FROM per_contract AS c WHERE c.emp_id = ?1 AND c.last = 1");
            contractQ.setParam(1, empId);
            Object[][] cts = contractQ.getRecords(conn);
            if (cts.length > 0) {
                int[] span = new int[]{roundSecs(toSecs((Date) extr[2]), RoundingMode.HALF_EVEN), roundSecs(toSecs((Date) extr[3]), RoundingMode.HALF_EVEN)};
                for (int j = 0; j < dayParts.length; j++) {
                    int[] inter = intersec(dayParts[j], span);
                    if (inter[1] - inter[0] > 0) {
                        String evType;
                        if (j == 1) {//diurna
                            if (dom) {
                                evType = "ExDiuDom";
                            } else {
                                evType = "ExDiuSem";
                            }
                        } else//nocturna
                        if (dom) {
                            evType = "ExNocDom";
                        } else {
                            evType = "ExNocSem";
                        }

                        MySQLQuery insq = new MySQLQuery("INSERT INTO `per_extra` SET "
                                + "`emp_id` = " + empId + ", "//0
                                + "`pos_id` = " + cts[0][1] + ", "//1
                                + "`employeer_id` = " + cts[0][0] + ", "//2
                                + "`pay_month` = ?1, "//3                                    
                                + "`ev_date` = ?2, "//5
                                + "`beg_time` = ?3, "//6
                                + "`end_time` = ?4, "//7
                                + "`ev_type` = '" + evType + "', "//8
                                + "`reg_by_id` = 1, "//9
                                + "`reg_type` = 'prog', "//10
                                + "`reg_date` = ?5, "//11
                                + "`checked` = 0, "//12                                    
                                + "`input_type` = 'gate', "//14
                                //+ "`gate_event_id` = , "//15
                                + "`gate_span_id` = " + extr[0] + ", "//16
                                + "`approved_time` = ?6, "//17
                                + "`active` = 1");
                        insq.setParam(1, payDate);
                        insq.setParam(2, yester);
                        insq.setParam(3, secsToDate(inter[0]));
                        insq.setParam(4, secsToDate(inter[1]));
                        insq.setParam(5, yester);
                        insq.setParam(6, inter[1] - inter[0]);
                        insq.executeUpdate(conn);
                    }
                }
            }
        }

        //EXTRAS DE JORNADAS NORMALES         
        q = new MySQLQuery(""
                + "SELECT emp_id, exp_hour, reg_hour, type FROM per_gate_event e WHERE e.event_day = ?1 AND reg_hour IS NOT NULL AND type = 'out' AND TIME_TO_SEC(TIMEDIFF(reg_hour, exp_hour))/60 > " + cfg[2] + " "
                + " UNION "
                + "SELECT emp_id, reg_hour, exp_hour, type FROM per_gate_event e WHERE e.event_day = ?1 AND reg_hour IS NOT NULL AND type = 'in' AND TIME_TO_SEC(TIMEDIFF(exp_hour, reg_hour))/60 > " + cfg[2] + " ");

        q.setParam(1, yester);
        extrs = q.getRecords(conn);

        for (Object[] extr : extrs) {
            int empId = (Integer) extr[0];
            MySQLQuery contractQ = new MySQLQuery("SELECT c.employeer_id, c.pos_id FROM per_contract AS c WHERE c.emp_id = ?1 AND c.last = 1");
            contractQ.setParam(1, empId);
            Object[][] cts = contractQ.getRecords(conn);
            if (cts.length > 0) {
                int[] span = new int[]{roundSecs(toSecs((Date) extr[1]), RoundingMode.HALF_EVEN), roundSecs(toSecs((Date) extr[2]), RoundingMode.HALF_EVEN)};
                if (span[1] - span[0] > (Integer) cfg[2]) {//si pasa el límite de extras
                    for (int j = 0; j < dayParts.length; j++) {
                        int[] inter = intersec(dayParts[j], span);
                        if (inter[1] - inter[0] > 0) {
                            String evType;
                            if (j == 1) {//diurna
                                if (dom) {
                                    evType = "ExDiuDom";
                                } else {
                                    evType = "ExDiuSem";
                                }
                            } else//nocturna
                            if (dom) {
                                evType = "ExNocDom";
                            } else {
                                evType = "ExNocSem";
                            }
                            MySQLQuery evIdQ = new MySQLQuery("SELECT id FROM per_gate_event WHERE emp_id = ?1 AND exp_hour = ?2 AND event_day = ?3");
                            evIdQ.setParam(1, empId);
                            if (extr[3].equals("out")) {
                                evIdQ.setParam(2, extr[1]);
                            } else {
                                evIdQ.setParam(2, extr[2]);
                            }
                            evIdQ.setParam(3, yester);

                            MySQLQuery insq = new MySQLQuery("INSERT INTO `per_extra` SET "
                                    + "`emp_id` = " + empId + ", "//0
                                    + "`pos_id` = " + cts[0][1] + ", "//1
                                    + "`employeer_id` = " + cts[0][0] + ", "//2
                                    + "`pay_month` = ?1, "//3
                                    + "`ev_date` = ?2, "//5
                                    + "`beg_time` = ?3, "//6
                                    + "`end_time` = ?4, "//7
                                    + "`ev_type` = '" + evType + "', "//8
                                    + "`reg_by_id` = 1, "//9
                                    + "`reg_type` = 'bill', "//10
                                    + "`reg_date` = ?5, "//11
                                    + "`checked` = 0, "//12
                                    + "`input_type` = 'gate', "//14
                                    + "`gate_event_id` = " + evIdQ.getAsInteger(conn) + ", "//16
                                    + "`approved_time` = ?6, "//17
                                    + "`active` = 1");
                            insq.setParam(1, payDate);
                            insq.setParam(2, yester);
                            insq.setParam(3, secsToDate(inter[0]));
                            insq.setParam(4, secsToDate(inter[1]));
                            insq.setParam(5, yester);
                            insq.setParam(6, inter[1] - inter[0]);
                            insq.executeUpdate(conn);
                        }//si hubo intersección
                    }//por las partes de día
                }//si la diferencia el superior al parametro
            }//si tiene contrato
        } /////FIN EXTRAS    
    }

    private String getDayName(Date dt) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(dt);
        switch (gc.get(GregorianCalendar.DAY_OF_WEEK)) {
            case GregorianCalendar.MONDAY:
                return "lu";
            case GregorianCalendar.TUESDAY:
                return "ma";
            case GregorianCalendar.WEDNESDAY:
                return "mi";
            case GregorianCalendar.THURSDAY:
                return "ju";
            case GregorianCalendar.FRIDAY:
                return "vi";
            case GregorianCalendar.SATURDAY:
                return "sa";
            case GregorianCalendar.SUNDAY:
                return "do";
            default:
                throw new RuntimeException("dia no soportado");
        }
    }

    private static int toSecs(Date dt) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(dt);
        return (gc.get(GregorianCalendar.HOUR_OF_DAY) * 3600) + (gc.get(GregorianCalendar.MINUTE) * 60) + (gc.get(GregorianCalendar.SECOND));
    }

    private static int[] intersec(int[] i1, int[] i2) {
        return new int[]{Math.max(i1[0], i2[0]), Math.min(i1[1], i2[1])};
    }

    private static Date secsToDate(int secs) {
        int res = secs % 3600;
        int hrs = (secs - res) / 3600;
        int min = res / 60;
        return new GregorianCalendar(1970, 0, 01, hrs, min, 0).getTime();
    }

    private static int roundSecs(int secs, RoundingMode r) {
        return (new BigDecimal(secs / 60d).setScale(0, r).intValue()) * 60;
    }
}
