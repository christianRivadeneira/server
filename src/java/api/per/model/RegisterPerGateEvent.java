package api.per.model;

import java.awt.Color;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.swing.ImageIcon;
import utilities.Dates;
import utilities.MySQLQuery;

public class RegisterPerGateEvent {

    public String name;
    public String event;
    public String current;
    public String diff;
    public Boolean warn = false;
    public Boolean alreadyRegistered = false;
    public String coloEvent = Integer.toHexString(Color.BLACK.getRGB()).substring(2);
    public String colorDiff = Integer.toHexString(Color.BLACK.getRGB()).substring(2);
    public String colorCurr = Integer.toHexString(Color.BLACK.getRGB()).substring(2);
    public ImageIcon photo;
    public boolean hasProfile;
    public boolean holy;
    public boolean vaca;
    public boolean hasEvents;
    public int profileId;
    public int missExtra;
    public Integer expOfficeId;
    public Integer photoId;
    public String gender;

    public static RegisterPerGateEvent registerEvent(int empId, int ownerType, boolean hasReplace, Integer regOfficeId, boolean nearestEvent, Integer tempPrintId, boolean normalEvent, String regOfficeName, Date regTime, Connection con) throws Exception {
        //MIS DATOS GENERALES
        String time = regTime != null ? "'" + Dates.getSQLTimeFormat().format(regTime) + "'" : null;
        Date now = getTimePrepared(con);

        RegisterPerGateEvent d = new RegisterPerGateEvent();
        Object[] empRow = new MySQLQuery("SELECT "
                + "CONCAT(e.first_name, ' ',e.last_name), "
                + "e.last_check_in, "
                + "e.gender, "
                + "ctr.office_id, "
                + "IF(ctr.office_id IS NOT NULL,(SELECT o.name FROM per_office AS o WHERE o.id = ctr.office_id),'') "
                + "FROM per_employee AS e "
                + "LEFT JOIN per_contract AS ctr ON ctr.emp_id = e.id AND (ctr.active = 1 AND IF(ctr.leave_date IS NULL, TRUE, CURDATE() <= ctr.leave_date) AND ctr.beg_date <= CURDATE()) "
                + "WHERE e.id = " + empId + "").getRecord(con);//0        
        //MI FOTO
        d.photoId = new MySQLQuery("SELECT id FROM bfile WHERE owner_type = " + ownerType + " AND owner_id = " + empId + " LIMIT 1").getAsInteger(con);//1
        //EL PERFIL QUE ME CORRESPONDE SEGÚN LA HORA DEL DÍA EN QUE LLEGO
        Object[][] profData = new MySQLQuery("SELECT "
                + "p.id,"
                + "p.miss_extra_report "
                + "FROM "
                + "per_emp_prof AS ep "
                + "INNER JOIN per_gate_prof AS p ON p.id = ep.prof_id "
                + "LEFT JOIN per_gate_prof_day pd ON pd.profile_id = p.id AND pd.week_day = '" + getDayName() + "' "
                + "LEFT JOIN per_gate_prog AS prog ON prog.date = "
                + (regTime != null ? time : " CURTIME() ")
                + "AND prog.emp_id = ep.emp_id AND prog.profile_id = p.id "
                + "WHERE ep.emp_id = " + empId + " "
                + "AND (SELECT COUNT(*) > 0 FROM per_gate_prof_day_chk chk WHERE chk.day_id = pd.id) "
                + "AND ("
                + "(p.`type` = 'norm' "
                + "AND p.cond_beg IS NULL) " //es un perfil normal
                + "OR "
                + "(p.`type` = 'cond' AND p.cond_beg IS NOT NULL AND ("
                + (regTime != null ? time : " CURTIME()")
                + "BETWEEN p.cond_beg AND p.cond_end)) "//condicional
                + "OR "
                + (hasReplace ? "(p.`type` = 'prov' AND p.active AND ("
                        + (regTime != null ? "'" + Dates.getSQLDateFormat().format(regTime) + "'" : " CURDATE()")
                        + " BETWEEN ep.beg_date AND ep.end_date)) OR " : "")//provisional
                + "(p.`type` = 'prog' AND p.cond_beg IS NULL AND prog.id IS NOT NULL)) "//es un perfil programado
                + "ORDER BY p.`type` = 'prov' DESC").getRecords(con);//es prioritario el perfil provisional

        Integer profileId = null;
        Integer missExtra = 0;

        if (profData != null && profData.length > 0) {
            if (profData.length == 1) {
                Object[] row = profData[0];
                profileId = row != null ? MySQLQuery.getAsInteger(row[0]) : null;
                missExtra = row != null ? MySQLQuery.getAsInteger(row[1]) : 0;
            } else {
                throw new Exception("El empleado tiene más de un perfíl condicional que inicia en este momento."
                        + "\nDeben verificarse los perfiles asignados en talento humano");
            }
        }

        //SE PREGUNTA PARA REPORTAR COMO IRREGULARIDAD LLEGADA TEMPRANO O SALIDA TARDE
        Integer extra_lim = new MySQLQuery("SELECT extra_lim FROM per_cfg WHERE per_cfg.id = 1").getAsInteger(con);//3
        //TIENE PERFILES ASIGNADOS
        d.hasProfile = new MySQLQuery("SELECT COUNT(*)>0 FROM per_emp_prof ep WHERE ep.emp_id = " + empId + " AND ep.active").getAsBoolean(con);//4
        //VER SI HOY ES FESTIVO
        d.holy = new MySQLQuery("SELECT COUNT(*) > 0 FROM per_holiday WHERE holi_date = CURDATE()").getAsBoolean(con);//5
        //VER SI ESTOY DE VACACIONES
        d.vaca = new MySQLQuery("SELECT COUNT(*) > 0 FROM per_vacation WHERE CURDATE() BETWEEN date_beg AND date_end AND employee_id = " + empId).getAsBoolean(con);//6
        //curDate
        Date currDate = (regTime != null ? regTime : now);//7
        //VER SI YA TENGO GENERADA LA PLANILLA DEL DÍA
        d.hasEvents = new MySQLQuery("SELECT COUNT(*) > 0 FROM per_gate_event AS ev WHERE ev.event_day = CURDATE() AND ev.emp_id = " + empId).getAsBoolean(con);//8
        d.name = MySQLQuery.getAsString(empRow[0]);
        d.gender = MySQLQuery.getAsString(empRow[2]);
        if (!d.hasProfile) {
            d.event = "El empleado no tiene perfíl asignado.";
            d.coloEvent = Integer.toHexString(Color.RED.getRGB()).substring(2);
        } else {
            //CREANDO EVENTOS DEL DÍA
            d.expOfficeId = MySQLQuery.getAsInteger(empRow[3]);
            createEvents(d.holy, d.vaca, d.hasEvents, profileId, empId, missExtra, con, regOfficeId, d.expOfficeId);//Llama a la función que valida eventos con permisos

            //OBTENER DE LA LISTA DE EVENTOS DEL DÍA EL EVENTO QUE CORRESPONDE CON ESTE CHEQUEO            
            Object[] evRow;
            if (nearestEvent) {// evento mas proximo                
                evRow = new MySQLQuery("SELECT id, exp_hour, tolerance, type, miss_extra, "
                        + "ABS(TIME_TO_SEC(TIMEDIFF( " + (regTime != null ? time : " CURTIME()") + ", "
                        + "e.exp_hour))/60) AS dif "
                        + "FROM per_gate_event e "
                        + "WHERE e.emp_id = " + empId + " AND e.event_day = CURDATE() AND reg_hour IS NULL "
                        + "AND IFNULL(e.exp_hour >= (SELECT MAX(ev2.reg_hour) FROM per_gate_event ev2  WHERE ev2.emp_id = " + empId + " AND ev2.event_day = CURDATE() AND ev2.reg_hour IS NOT NULL), TRUE) "
                        + "ORDER BY dif ASC "
                        + "LIMIT 0, 1").getRecord(con);
            } else {
                evRow = new MySQLQuery("SELECT id, exp_hour, tolerance, type, miss_extra FROM per_gate_event e WHERE e.emp_id = " + empId + " AND e.event_day = CURDATE() AND reg_hour IS NULL ORDER BY exp_hour ASC LIMIT 0, 1").getRecord(con);
            }

            if (empRow[1] != null) { //------------------------------------------------------------ Descomentar al acabar las pruebas
                long t1 = currDate.getTime();
                long t2 = (MySQLQuery.getAsDate(empRow[1])).getTime();
                if (t1 - t2 < 180000) {//3 minutos de diferencia con el ultimo registro
                    d.event = "El empleado ya se registró.";
                    d.coloEvent = Integer.toHexString(Color.RED.getRGB()).substring(2);
                    d.alreadyRegistered = true;
                    return d;
                }
            }
            if (tempPrintId != null) {
                new MySQLQuery("UPDATE per_fprint SET last = (id = " + tempPrintId + ") WHERE emp_id = " + empId).executeUpdate(con);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            if (normalEvent) {//evento normal según perfil de portería.
                if (evRow != null) {
                    Boolean evRowmissExtra = MySQLQuery.getAsBoolean(evRow[4]);
                    int segDiff = timeToSeconds((Date) evRow[1]) - timeToSeconds(currDate);
                    d.warn = false;
                    d.event = ((evRow[3].equals("in")) ? "Ingreso de las " : "Salida de las ") + sdf.format(evRow[1]);
                    if ((evRow[3].equals("in"))) {//entrada
                        if (segDiff < 0) {//tarde
                            d.diff = "Llega " + Math.abs(segDiff / 60) + " Minutos Tarde";
                            d.warn = Math.abs(segDiff) > ((Integer) evRow[2] * 60);
                        } else {//temprano
                            d.diff = "Llega " + Math.abs(segDiff / 60) + " Minutos Temprano";
                            d.warn = Math.abs(segDiff) > (extra_lim * 60) && (!evRowmissExtra);
                        }
                    } else {//salida
                        if (segDiff < 0) {//tarde
                            d.diff = "Se Marcha " + Math.abs(segDiff / 60) + " Minutos Tarde";
                            d.warn = Math.abs(segDiff) > (extra_lim * 60) && (!evRowmissExtra);
                        } else {//temprano
                            d.diff = "Se Marcha " + Math.abs(segDiff / 60) + " Minutos Temprano";
                            d.warn = Math.abs(segDiff) > ((Integer) evRow[2] * 60);
                        }
                    }

                    String otherOffice = "";
                    if (!d.expOfficeId.equals(regOfficeId)) {
                        d.warn = true;
                        otherOffice = "\nRegistró en " + regOfficeName + ". "
                                + "\nOficina Asignada: " + (empRow[4] != null ? MySQLQuery.getAsString(empRow[4]) : "[Desconocida] Id:" + MySQLQuery.getAsInteger(empRow[3]));
                    }
                    d.current = "Son las " + sdf.format(currDate);
                    if (d.warn) {
                        d.colorDiff = Integer.toHexString(Color.RED.getRGB()).substring(2);
                        new MySQLQuery("UPDATE per_gate_event SET reg_hour = ?1 , warning = 1,  per_office_id = " + regOfficeId + ",  exp_office_id = " + d.expOfficeId + ", "
                                + "notes = '" + (d.diff + (otherOffice != null ? " " + otherOffice : "")) + "' "
                                + "WHERE id = " + evRow[0])
                                .setParam(1, regTime != null ? regTime : now)
                                .executeUpdate(con);
                    } else {
                        new MySQLQuery("UPDATE per_gate_event SET reg_hour = ?1 , "
                                + "warning = 0 , "
                                + "per_office_id = " + regOfficeId + ",  "
                                + "exp_office_id = " + d.expOfficeId + " WHERE id = " + evRow[0])
                                .setParam(1, regTime != null ? regTime : now)
                                .executeUpdate(con);
                    }

                    new MySQLQuery("UPDATE per_employee SET last_check_in = ?1 WHERE id = " + empId).setParam(1, (regTime != null ? regTime : currDate)).executeUpdate(con);
                } else {
                    span(con, "full", currDate, regTime, empId, d);//jornada adicional completa
                }
            } else {
                span(con, "short", currDate, regTime, empId, d);//salida breve
            }
        }
        return d;
    }

    private static Date getTimePrepared(Connection con) throws Exception {
        String timeZone = new MySQLQuery("SELECT time_zone FROM sys_cfg WHERE id = 1").getAsString(con);
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        TimeZone localTimeZone = TimeZone.getDefault();

        int timeZoneOffset = tz.getOffset(System.currentTimeMillis());
        int localTimeZoneOffset = localTimeZone.getOffset(System.currentTimeMillis());
        int difference = Math.abs(timeZoneOffset - localTimeZoneOffset);

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(System.currentTimeMillis() - difference);
        return gc.getTime();
    }

    private static void span(Connection ep, String type, Date currDate, Date regTime, Integer empId, RegisterPerGateEvent data) throws Exception {
        Date now = getTimePrepared(ep);

        String time = regTime != null ? "'" + Dates.getSQLTimeFormat().format(regTime) + "'" : null;
        PerGateSpan currSp = PerGateSpan.select(empId, type, currDate, ep);
        if (currSp == null) {
            PerGateSpan sp = new PerGateSpan();
            sp.begHour = currDate;
            sp.expOfficeId = data.expOfficeId;
            sp.empId = empId;
            sp.eventDay = Dates.trimDate(currDate);
            sp.type = type;
            new MySQLQuery(sp.getInsertString(sp)).executeInsert(ep);
        } else {
            new MySQLQuery("UPDATE per_gate_span "
                    + "SET end_hour = '" + (regTime != null ? time : new SimpleDateFormat("HH:mm:ss").format(now)) + "' "
                    + "WHERE id = " + currSp.id).executeUpdate(ep);
        }
        data.event = (currSp == null ? "Inicio de " : "Fin de ") + ((type.equals("full")) ? "Jornada Adicional" : "Salida Breve");
        setLastCheck(currDate, empId, ep);
    }

    private static void setLastCheck(Date currDate, Integer empId, Connection ep) throws Exception {
        new MySQLQuery("UPDATE per_employee SET last_check_in = ?1 WHERE id = " + empId).setParam(1, currDate).executeUpdate(ep);
    }

    private static int timeToSeconds(Date dt) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(dt);
        return (gc.get(GregorianCalendar.HOUR_OF_DAY) * 3600) + (gc.get(GregorianCalendar.MINUTE) * 60) + gc.get(GregorianCalendar.SECOND);
    }

    public static void createEvents(boolean holy, boolean vaca, boolean hasEvents, Integer profileId, Integer empId, Integer missExtra, Connection ep, Integer officeId, Integer expOfficeId) throws Exception {
        if (profileId == null) {
            return;
        }
        if (!vaca && !holy) {
            if (!hasEvents) {
                //crea los eventos del día
                new MySQLQuery("INSERT INTO per_gate_event (emp_id, exp_hour, type, tolerance, event_day, warning, checked, miss_extra, profile_id,per_office_id, exp_office_id) "
                        + "(SELECT "
                        + empId + ", "//0
                        + "chk.`hour`, "//1
                        + "chk.type, "//2
                        + "IF(chk.type = 'in', prof.in_tolerance, prof.out_tolerance), "//3
                        + "CURDATE(), "//4
                        + "0, "//5
                        + "0, "//6
                        + missExtra + ", "//7
                        + profileId + ", "//8
                        + (officeId != null ? officeId : "NULL") + ", "//9
                        + (expOfficeId != null ? expOfficeId : "NULL ") + " "//10
                        + "FROM per_gate_prof AS prof "
                        + "INNER JOIN per_gate_prof_day AS `day` ON `day`.profile_id = prof.id "
                        + "INNER JOIN per_gate_prof_day_chk AS chk ON chk.day_id = `day`.id "
                        + "LEFT JOIN per_gate_event ev ON ev.emp_id = " + empId + " AND ev.exp_hour = chk.`hour` AND ev.type = chk.type AND ev.event_day = CURDATE() "
                        + "WHERE ev.id IS NULL AND prof.id = " + profileId + " AND `day`.week_day = '" + getDayName() + "')").executeInsert(ep);
                //Borra eventos dentro del rango de los permisos
                new MySQLQuery("DELETE per_gate_event.* "
                        + "FROM per_gate_event "
                        + "INNER JOIN per_licence ON per_gate_event.emp_id = per_licence.emp_id "
                        + "WHERE per_licence.beg_date < TIMESTAMP(event_day,exp_hour) AND "//3
                        + "TIMESTAMP(event_day,exp_hour) < per_licence.end_date AND per_gate_event.event_day = CURDATE() "
                        + "AND active = 1 AND per_gate_event.emp_id = " + empId).executeDelete(ep);

                //Crea el evento de inicio de permiso.
                new MySQLQuery("INSERT INTO per_gate_event (emp_id, exp_hour, type, tolerance, event_day, warning, checked, miss_extra, profile_id,per_office_id, exp_office_id) "
                        + " "//0
                        + "(SELECT "//1
                        + "lic.emp_id, "//2
                        + "TIME(lic.beg_date), "//4
                        + "'out', "//5
                        + "prof.out_tolerance, "//6
                        + "CURDATE(), "//7
                        + "0, "//8
                        + "0, "//9
                        + "miss_extra_report, "//10
                        + "prof.id,"
                        + "" + (officeId != null ? officeId : "NULL") + ", "//11
                        + "" + (expOfficeId != null ? expOfficeId : "NULL ") + " "//12
                        + "FROM per_licence AS lic "
                        + "INNER JOIN per_employee AS emp ON lic.emp_id = emp.id "
                        + "INNER JOIN per_emp_prof AS ep ON emp.id = ep.emp_id "
                        + "INNER JOIN per_gate_prof AS prof ON ep.prof_id = prof.id "
                        + "WHERE DATE(lic.beg_date) = CURDATE() "
                        + "AND lic.emp_id = " + empId + " "
                        + "AND prof.id = " + profileId + " "
                        + "AND lic.active = 1 "
                        + "AND (SELECT COUNT(*) = 0 "
                        + "FROM per_gate_event e "
                        + "WHERE e.emp_id = lic.emp_id AND e.event_day = CURDATE() "
                        + "AND e.exp_hour = TIME(lic.beg_date) AND e.type like 'out'))").executeUpdate(ep);

                //Crea el evento de fin de permiso
                new MySQLQuery("INSERT INTO per_gate_event (emp_id, exp_hour, type, tolerance, event_day, warning, checked, miss_extra, profile_id,per_office_id,exp_office_id) "
                        + " "
                        + "(SELECT "
                        + "lic.emp_id, "
                        + "TIME(lic.end_date), "
                        + "'in', "
                        + "prof.in_tolerance, "
                        + "CURDATE(), "
                        + "0, "
                        + "0, "
                        + "miss_extra_report, "
                        + "prof.id, "
                        + (officeId != null ? officeId : "NULL") + ", "
                        + (expOfficeId != null ? expOfficeId : "NULL ") + " "
                        + "FROM per_licence AS lic "
                        + "INNER JOIN per_employee AS emp ON lic.emp_id = emp.id "
                        + "INNER JOIN per_emp_prof AS ep ON emp.id = ep.emp_id "
                        + "INNER JOIN per_gate_prof AS prof ON ep.prof_id = prof.id "
                        + "WHERE DATE(lic.end_date) = CURDATE() "
                        + "AND lic.emp_id = " + empId + " "
                        + "AND prof.id = " + profileId + " "
                        + "AND lic.active = 1 "
                        + "AND (SELECT COUNT(*) = 0 "
                        + "FROM per_gate_event e "
                        + "WHERE e.emp_id = lic.emp_id AND e.event_day = CURDATE() "
                        + "AND e.exp_hour = TIME(lic.end_date) AND e.type like 'in'))").executeUpdate(ep);

                //Borrar eventos contradictorios
                new MySQLQuery("DELETE FROM per_gate_event WHERE exp_hour IN "
                        + "(SELECT * FROM "
                        + "(SELECT exp_hour "//0
                        + "FROM per_gate_event "//1
                        + "WHERE event_day = CURDATE() AND emp_id = " + empId + " "
                        + "GROUP BY exp_hour HAVING COUNT(*) > 1) "
                        + "AS l) AND event_day = CURDATE() AND emp_id = " + empId).executeDelete(ep);
            }
        }
    }

    private static String getDayName() throws Exception {
        GregorianCalendar gc = new GregorianCalendar();
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
