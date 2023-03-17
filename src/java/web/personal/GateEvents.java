package web.personal;

import java.sql.Connection;
import java.util.Date;
import java.util.GregorianCalendar;
import utilities.MySQLQuery;

public class GateEvents {

    public static void generate(Connection conn, Date today) throws Exception {
        System.out.println("gates events");
        MySQLQuery q = new MySQLQuery("SELECT COUNT(*) > 0 FROM per_holiday WHERE holi_date = ?1");
        q.setParam(1, today);
        Boolean holy = q.getAsBoolean(conn);
        holy = (holy == null ? false : holy);
        if (!holy) {
            //empleados que ya tienen eventos de portería para hoy
            String emps = new MySQLQuery("select group_concat(cast(e.emp_id as char)) from per_gate_event e where e.event_day = ?1").setParam(1, today).getAsString(conn);

            if (new MySQLQuery("SELECT has_replace FROM per_cfg ").getAsBoolean(conn)) {
                //empleados que tienen perfiles provisionales
                String empsProv = new MySQLQuery("SELECT GROUP_CONCAT(CAST(emp.id as char)) "
                        + "FROM per_gate_prof AS prof "
                        + "INNER JOIN per_gate_prof_day d ON d.profile_id = prof.id "
                        + "INNER JOIN per_gate_prof_day_chk dc ON dc.day_id = d.id "
                        + "INNER JOIN per_emp_prof AS ep ON ep.prof_id = prof.id "
                        + "INNER JOIN per_employee AS emp ON ep.emp_id = emp.id "
                        + "LEFT JOIN per_vacation AS vac ON CURDATE() BETWEEN vac.date_beg AND vac.date_end AND vac.employee_id = emp.id "
                        + "WHERE (bad_fingerprints OR (SELECT COUNT(*) > 0 FROM per_fprint WHERE emp_id = emp.id)) "
                        + "AND prof.`type` = 'prov' AND prof.active "
                        + "AND d.week_day = '" + getDayName(today) + "' "
                        + "AND CURDATE() BETWEEN ep.beg_date AND ep.end_date "
                        + "AND vac.id IS NULL").getAsString(conn);
                //Inserta eventos de portería de perfiles provisionales
                new MySQLQuery("INSERT INTO per_gate_event (emp_id, exp_hour, type, tolerance, event_day, warning, checked, miss_extra, profile_id) "
                        + "(SELECT "
                        + "emp.id, "
                        + "dc.`hour`, "
                        + "dc.type, "
                        + "IF(dc.type = 'in', prof.in_tolerance, prof.out_tolerance), "
                        + "CURDATE(), "
                        + "0, "
                        + "0, "
                        + "miss_extra_report, "
                        + "prof.id "
                        + "FROM per_gate_prof AS prof "
                        + "INNER JOIN per_gate_prof_day d ON d.profile_id = prof.id "
                        + "INNER JOIN per_gate_prof_day_chk dc ON dc.day_id = d.id "
                        + "INNER JOIN per_emp_prof AS ep ON ep.prof_id = prof.id "
                        + "INNER JOIN per_employee AS emp ON ep.emp_id = emp.id "
                        + "LEFT JOIN per_vacation AS vac ON CURDATE() BETWEEN vac.date_beg AND vac.date_end AND vac.employee_id = emp.id "
                        + "WHERE (bad_fingerprints OR (SELECT COUNT(*) > 0 FROM per_fprint WHERE emp_id = emp.id)) "
                        + "AND prof.`type` = 'prov' AND prof.active "
                        + "AND d.week_day = '" + getDayName(today) + "' "
                        + (emps != null ? "AND emp.id NOT IN (" + emps + ") " : "")
                        + "AND CURDATE() BETWEEN ep.beg_date AND ep.end_date "
                        + "AND vac.id IS NULL"
                        + ")").executeUpdate(conn);
                emps = (emps != null ? (emps + (empsProv != null ? "," + empsProv : "")) : (empsProv != null ? empsProv : null));
            }

            //Inserta eventos de portería de perfiles normales
            new MySQLQuery("INSERT INTO per_gate_event (emp_id, exp_hour, type, tolerance, event_day, warning, checked, miss_extra, profile_id) "
                    + "(SELECT "
                    + "emp.id, "//0
                    + "chk.`hour`, "//2
                    + "chk.type, "//3
                    + "if(chk.type = 'in', prof.in_tolerance, prof.out_tolerance), "//4
                    + "CURDATE(), "//5
                    + "0, "//6
                    + "0, "
                    + "miss_extra_report, "//7
                    + "prof.id "
                    + "FROM per_gate_prof AS prof "
                    + "INNER JOIN per_emp_prof AS ep ON ep.prof_id = prof.id "//8
                    + "INNER JOIN per_employee AS emp ON ep.emp_id = emp.id "//11
                    + "INNER JOIN per_gate_prof_day AS `day` ON `day`.profile_id = prof.id "//9
                    + "INNER JOIN per_gate_prof_day_chk AS chk ON chk.day_id = `day`.id "//10     
                    + "LEFT JOIN per_vacation AS vac ON CURDATE() BETWEEN vac.date_beg AND vac.date_end AND vac.employee_id = emp.id "//8
                    + "WHERE "
                    + "(bad_fingerprints OR (SELECT COUNT(*) > 0 FROM per_fprint WHERE emp_id = emp.id)) AND "
                    + "vac.id IS NULL AND prof.cond_beg IS NULL AND `day`.week_day = '" + getDayName(today) + "' "
                    + "AND prof.`type` <> 'prog' AND prof.`type` <> 'prov' "
                    + (emps != null ? "AND emp.id NOT IN (" + emps + ")" : "")
                    + ")").executeUpdate(conn);

            //Inserta eventos de portería para perfiles programados
            new MySQLQuery("INSERT INTO per_gate_event (emp_id, exp_hour, type, tolerance, event_day, warning, checked, miss_extra, profile_id) "
                    + "(SELECT e.id, chk.`hour`, chk.type, if(chk.type = 'in', p.in_tolerance, p.out_tolerance), CURDATE(), 0, 0, miss_extra_report, p.id "
                    + "FROM per_gate_prog pg "
                    + "INNER JOIN per_gate_prof p ON pg.profile_id = p.id "
                    + "INNER JOIN per_employee e ON e.id = pg.emp_id "
                    + "INNER JOIN per_gate_prof_day d ON d.profile_id = p.id "
                    + "INNER JOIN per_gate_prof_day_chk chk ON chk.day_id = d.id "
                    + "LEFT JOIN per_vacation AS vac ON CURDATE() BETWEEN vac.date_beg AND vac.date_end AND vac.employee_id = e.id "
                    + "WHERE (bad_fingerprints OR (SELECT COUNT(*) > 0 FROM per_fprint WHERE emp_id = e.id)) "
                    + "AND vac.id IS NULL AND p.cond_beg IS NULL "
                    + "AND p.`type` = 'prog' AND pg.date = CURDATE() "
                    + "AND d.week_day = '" + getDayName(today) + "' "
                    + (emps != null ? "AND e.id NOT IN (" + emps + ")" : "")
                    + ")").executeUpdate(conn);

            //Elimina eventos de permisos para del dia
            new MySQLQuery("DELETE e.* "
                    + "FROM per_licence l "
                    + "INNER JOIN per_gate_event e ON e.emp_id = l.emp_id "
                    + "WHERE e.event_day = CURDATE() "
                    + "AND e.exp_hour > l.beg_date "
                    + "AND e.exp_hour < l.end_date "
                    + "AND l.active = 1").executeDelete(conn);

            //Inserta eventos de entrada de permisos
            new MySQLQuery("INSERT INTO per_gate_event "
                    + "(`emp_id`, `profile_id`, `event_day`, `miss_extra`, `exp_hour`, `tolerance`, `reg_hour`, `type`, `warning`, `checked`, `notes`, `per_office_id`) "
                    + "SELECT e.emp_id, "
                    + "e.profile_id, "
                    + "e.event_day, "
                    + "e.miss_extra, "
                    + "TIME(l.end_date), "
                    + "e.tolerance, "
                    + "NULL, "
                    + "'in', "
                    + "0, "
                    + "0, "
                    + "NULL, "
                    + "e.per_office_id "
                    + "FROM per_licence l "
                    + "INNER JOIN per_gate_event e ON e.emp_id = l.emp_id "
                    + "WHERE CURDATE() =  DATE(l.end_date) "
                    + "AND e.event_day = CURDATE() "
                    + "AND e.`type` = 'in' "
                    + "AND l.active = 1 "
                    + "GROUP BY e.emp_id").executeUpdate(conn);

            //Inserta eventos de salida de permisos
            new MySQLQuery("INSERT INTO per_gate_event "
                    + "(`emp_id`, `profile_id`, `event_day`, `miss_extra`, `exp_hour`, `tolerance`, `reg_hour`, `type`, `warning`, `checked`, `notes`, `per_office_id`) "
                    + "SELECT e.emp_id, "
                    + "e.profile_id, "
                    + "e.event_day, "
                    + "e.miss_extra, "
                    + "TIME(l.beg_date), "
                    + "e.tolerance, "
                    + "NULL, "
                    + "'out', "
                    + "0, "
                    + "0, "
                    + "NULL, "
                    + "e.per_office_id "
                    + "FROM per_licence l "
                    + "INNER JOIN per_gate_event e ON e.emp_id = l.emp_id "
                    + "WHERE CURDATE() =  DATE(l.beg_date) "
                    + "AND e.event_day = CURDATE() "
                    + "AND e.`type` = 'out' "
                    + "AND l.active = 1 "
                    + "GROUP BY e.emp_id").executeUpdate(conn);

            //Borrar eventos contradictorios
            String ids = new MySQLQuery("(SELECT group_concat(cast(id as char)) "
                    + "FROM per_gate_event "
                    + "WHERE event_day = CURDATE() "
                    + "GROUP BY emp_id, exp_hour HAVING COUNT(*) > 1);").getAsString(conn);

            if (ids != null && !ids.isEmpty()) {
                new MySQLQuery("DELETE FROM per_gate_event WHERE id IN (" + ids + ")").executeDelete(conn);
            }
        }
    }

    private static String getDayName(Date dt) {
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
}
