package api.per.api.perExtraFlow;

import api.per.model.PerLicence;
import java.sql.Connection;
import java.util.Date;
import java.util.GregorianCalendar;
import utilities.MySQLQuery;

public class LicenseEventsGate {

    public static void updateEventsGate(Connection conn, PerLicence obj, PerLicence orig, boolean isNew, Integer extraReqId) throws Exception {

        boolean sameDay = new MySQLQuery("SELECT DATE(?1) = DATE(?2)")
                .setParam(1, obj.begDate)
                .setParam(2, obj.endDate).getAsBoolean(conn);

        boolean isBegDateEqualsCur = new MySQLQuery("SELECT DATE(?1) = CURDATE()")
                .setParam(1, (orig != null ? orig.begDate : obj.begDate))
                .getAsBoolean(conn);

        if (!obj.active && isBegDateEqualsCur) {
            if ((orig != null ? orig.begDate : obj.begDate).after(new Date())) {
                Object[][] dataHours = new MySQLQuery("SELECT exp_hour, profile_id "
                        + "FROM per_gate_event "
                        + "WHERE emp_id = " + obj.empId + " "
                        + "AND reg_hour IS NULL "
                        + "AND event_day = CURDATE() "
                        + "ORDER BY exp_hour ASC").getRecords(conn);

                if (dataHours != null && dataHours.length > 0) {
                    Object profId = dataHours[0][1];
                    Object beghour = dataHours[0][0];
                    Object endhour = "23:59:59";
                    new MySQLQuery("DELETE "
                            + "FROM per_gate_event "
                            + "WHERE emp_id = " + obj.empId + " "
                            + "AND reg_hour IS NULL "
                            + "AND event_day = CURDATE()").executeDelete(conn);//se elimina los eventos que no estan marcados
                    new MySQLQuery("INSERT INTO per_gate_event (emp_id, exp_hour, type, tolerance, event_day, warning, checked, miss_extra, profile_id, extra_request_id) "
                            + "SELECT "
                            + obj.empId + ", "
                            + "chk.hour, "
                            + "chk.`type`, "
                            + "if(chk.type = 'in', p.in_tolerance, p.out_tolerance), "
                            + "CURDATE(), "
                            + "0, 0, "
                            + "p.miss_extra_report, "
                            + "p.id, "
                            + (extraReqId != null ? extraReqId : "NULL") + " "
                            + "FROM per_gate_prof p "
                            + "INNER JOIN per_gate_prof_day d ON d.profile_id = p.id "
                            + "INNER JOIN per_gate_prof_day_chk chk ON chk.day_id = d.id "
                            + "WHERE p.id = " + profId + " AND d.week_day = '" + getDayName(new Date()) + "' "
                            + "AND chk.hour BETWEEN '" + beghour + "' AND '" + endhour + "'").executeInsert(conn);// se adicionan los eventos que deben ir sin permisos
                }
            } else if (!isNew) {
                throw new Exception("Atención, No se puede inactivar el permiso porque ya paso la fecha de inicio");
            }
        }

        isBegDateEqualsCur = new MySQLQuery("SELECT DATE(?1) = CURDATE()")
                .setParam(1, obj.begDate)
                .getAsBoolean(conn);

        if (obj.active && isBegDateEqualsCur) {

            boolean hasEvent = new MySQLQuery("SELECT COUNT(*) > 0 "
                    + "FROM per_gate_event e "
                    + "WHERE e.event_day = CURDATE() AND e.emp_id = " + obj.empId).getAsBoolean(conn);

            Integer ctrOfficeId = new MySQLQuery("SELECT pc.office_id "
                    + "FROM per_employee e  "
                    + "INNER JOIN per_contract pc ON pc.emp_id = e.id  "
                    + "WHERE e.id = " + obj.empId + "  "
                    + "AND (pc.active= 1 AND IF(pc.leave_date IS NULL, TRUE, CURDATE() <= pc.leave_date) AND  pc.beg_date <= CURDATE()) ").getAsInteger(conn);

            if (!hasEvent) {
                throw new Exception("No tiene horarios para modificar en porteria");
            }

            new MySQLQuery("DELETE per_gate_event.* "
                    + "FROM per_gate_event "
                    + "WHERE event_day = CURDATE() "
                    + "AND emp_id = " + obj.empId + " "
                    + "AND exp_hour > DATE_FORMAT(?1,'%H:%i:%s') "
                    + "AND exp_hour < " + (sameDay ? "DATE_FORMAT(?2,'%H:%i:%s')" : "'23:59:59'"))
                    .setParam(1, obj.begDate).setParam(2, obj.endDate).executeDelete(conn);

            if (sameDay) {
                //fin del permiso si es el mismo dia
                new MySQLQuery("INSERT INTO per_gate_event "
                        + "(`emp_id`, `profile_id`, `event_day`, `miss_extra`, `exp_hour`, `tolerance`, `reg_hour`, `type`, `warning`, `checked`, `notes`, `per_office_id`,`exp_office_id`, extra_request_id) "
                        + "SELECT e.emp_id, "
                        + "e.profile_id, "
                        + "e.event_day, "
                        + "e.miss_extra, "
                        + "DATE_FORMAT(?1,'%H:%i:%s'), "
                        + "p.in_tolerance, "
                        + "NULL, "
                        + "'in', "
                        + "0, "
                        + "0, "
                        + "NULL, "
                        + "e.per_office_id, "
                        + ctrOfficeId + ", "
                        + (extraReqId != null ? extraReqId : "NULL") + " "
                        + "FROM per_gate_event e "
                        + "INNER JOIN per_gate_prof p ON p.id = e.profile_id "
                        + "WHERE e.event_day = CURDATE() "
                        + "AND e.emp_id = " + obj.empId + " "
                        + "LIMIT 1").setParam(1, obj.endDate).executeInsert(conn);
            }
            //inicio del permiso
            new MySQLQuery("INSERT INTO per_gate_event "
                    + "(`emp_id`, `profile_id`, `event_day`, `miss_extra`, `exp_hour`, `tolerance`, `reg_hour`, `type`, `warning`, `checked`, `notes`, `per_office_id`,`exp_office_id`, extra_request_id) "
                    + "SELECT e.emp_id, "
                    + "e.profile_id, "
                    + "e.event_day, "
                    + "e.miss_extra, "
                    + "DATE_FORMAT(?1,'%H:%i:%s'), "
                    + "p.out_tolerance, "
                    + "NULL, "
                    + "'out', "
                    + "0, "
                    + "0, "
                    + "NULL, "
                    + "e.per_office_id, "
                    + ctrOfficeId + ", "
                    + (extraReqId != null ? extraReqId : "NULL") + " "
                    + "FROM per_gate_event e "
                    + "INNER JOIN per_gate_prof p ON p.id = e.profile_id "
                    + "WHERE e.event_day = CURDATE() "
                    + "AND e.emp_id = " + obj.empId + " "
                    + "LIMIT 1").setParam(1, obj.begDate).executeInsert(conn);
            //Borrar eventos contradictorios
            new MySQLQuery("DELETE FROM per_gate_event WHERE exp_hour IN "
                    + "(SELECT * FROM "
                    + "(SELECT exp_hour "//0
                    + "FROM per_gate_event "//1
                    + "WHERE event_day = CURDATE() AND emp_id = " + obj.empId + " "
                    + "GROUP BY exp_hour HAVING COUNT(*) > 1) "
                    + "AS l) AND event_day = CURDATE() AND emp_id = " + obj.empId).executeDelete(conn);
        }
    }

    public static String getDayName(Date dt) {
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

    public static String getDayLongName(String day) {
        switch (day) {
            case "lu":
                return "Lunes";
            case "ma":
                return "Martes";
            case "mi":
                return "Miércoles";
            case "ju":
                return "Jueves";
            case "vi":
                return "Viernes";
            case "sa":
                return "Sábado";
            case "do":
                return "Domingo";
            default:
                throw new RuntimeException("dia no soportado");
        }
    }
}
