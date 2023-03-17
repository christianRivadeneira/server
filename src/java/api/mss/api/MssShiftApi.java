package api.mss.api;

import api.BaseAPI;
import api.GridResult;
import api.MultiPartRequest;
import api.MySQLCol;
import api.mss.beans.Mosaic;
import api.mss.dto.MssShiftEvent;
import api.mss.model.MssClient;
import api.mss.model.MssGuard;
import static api.mss.model.MssGuard.getFromEmployee;
import api.mss.model.MssPoint;
import api.mss.model.MssPost;
import api.mss.dto.MssShiftApp;
import api.mss.dto.PostApp;
import api.mss.dto.ReviewInfo;
import api.mss.dto.MssPointDto;
import api.mss.model.MssCfg;
import api.mss.model.MssScheduleCode;
import api.mss.model.MssShift;
import static api.mto.tasks.MtoTripTasks.distance;
import api.sys.model.Employee;
import api.sys.model.SysAppProfile;
import api.sys.model.SysAppProfileEmp;
import api.sys.model.SysCenter;
import api.sys.model.SysCrudLog;
import java.awt.Color;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import metadata.model.GridRequest;
import model.system.SessionLogin;
import utilities.Dates;
import utilities.MySQLQuery;
import utilities.Strings;
import utilities.cast;
import utilities.importer.Importer;
import utilities.importer.ImporterCol;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.Table;
import utilities.xlsReader.XlsReader;
import web.MD5;
import web.fileManager;
import web.quality.MailCfg;
import web.quality.SendMail;

@Path("/mssShift")
public class MssShiftApi extends BaseAPI {

    @POST
    public Response insert(MssShift obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/shift")
    public Response insertShift(MssShift obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            Date now = Dates.trimDate(now(conn));
            Date prog = Dates.trimDate(obj.expBeg);

            if (prog.before(now)) {
                throw new Exception("No se pueden programar turnos para el pasado");
            }

            MssScheduleCode sc = new MssScheduleCode().select(Integer.valueOf(obj.scCode), conn);

            if (getCointainsKeys(sc.code)) {
                throw new Exception("Todo código entre llaves [] sera ignorado");
            }

            MySQLQuery q = new MySQLQuery("INSERT INTO mss_shift (`guard_id`, `post_id`,`active`,`exp_beg`, `exp_end`,"
                    + "`sc_code`,`chk_status`, `reg_chk`,`notified`,`make_round` ) "
                    + "(SELECT ?2, ?3, 1, "
                    + "DATE_ADD(DATE(?4),INTERVAL TIME_TO_SEC(sc.beg_time) SECOND), "
                    + "DATE_ADD(DATE(?4),INTERVAL TIME_TO_SEC(sc.end_time) SECOND), "
                    + "sc.code, IF(RAND()<0.2,'pending','skip'), NOW(), 0, 0 "
                    + "FROM mss_schedule_code sc WHERE UPPER(sc.code) = ?1 AND sc.beg_time <> sc.end_time ) ");
            q.setParam(1, sc.code);
            q.setParam(2, obj.guardId);
            q.setParam(3, obj.postId);
            q.setParam(4, obj.expBeg);
            q.executeUpdate(conn);

            new MySQLQuery("UPDATE mss_shift SET exp_end = DATE_ADD(exp_end, INTERVAL 1 DAY) WHERE exp_beg > exp_end").executeUpdate(conn);

            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(MssShift obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssShift old = new MssShift().select(obj.id, conn);
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/check")
    public Response check(MssShift obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            String notes = obj.revNotes;
            MssShift old = new MssShift().select(obj.id, conn);
            obj = new MssShift().select(obj.id, conn);
            obj.revDt = MySQLQuery.now(conn);
            obj.revNotes = notes;
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssShift obj = new MssShift().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssShift.delete(id, conn);
            SysCrudLog.deleted(this, MssShift.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/shiftsByFilter")
    public Response getShiftsByFilter(
            @QueryParam("day") Integer day,
            @QueryParam("month") Integer month,
            @QueryParam("week") Integer week,
            @QueryParam("year") Integer year) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            Integer guardId = getFromEmployee(sl.employeeId, MssGuard.TYPE_GUARD, conn);
            if (guardId == null) {
                throw new Exception("No se encuentra registrado como guarda de seguridad");
            }
            Object[][] inProgData;
            String shiftQuery = "SELECT f.id, p.client_id, "
                    + " p.id, p.name, "
                    + " f.exp_beg, f.exp_end, "
                    + " f.reg_beg, f.reg_end "
                    + " FROM mss_shift f "
                    + " INNER JOIN mss_post p ON p.id = f.post_id "
                    + " WHERE f.active AND f.guard_id = ?1 ";

            MySQLQuery mq = null;

            if (day == null && month == null && year == null && week == null) {
                mq = new MySQLQuery(shiftQuery
                        + " AND (DATE(f.exp_beg) = CURDATE() "
                        + " OR DATE(f.exp_end) = CURDATE()) ");
            } else if (day != null && month != null && year != null) {
                mq = new MySQLQuery(shiftQuery
                        + " AND MONTH(f.exp_beg) = ?2 "
                        + " AND DAY(f.exp_beg) = ?3 "
                        + " AND YEAR(f.exp_beg) = ?4 "
                        + " ORDER BY exp_beg ASC");
                mq.setParam(2, month).setParam(3, day).setParam(4, year);
            } else if (week != null) {
                mq = new MySQLQuery(shiftQuery
                        + " AND WEEK(f.exp_beg)= ?2 "
                        + " ORDER BY exp_beg ASC"
                );
                mq.setParam(2, week);
            } else if (month != null) {
                mq = new MySQLQuery(shiftQuery
                        + " AND MONTH(f.exp_beg)= ?2 "
                        + " ORDER BY exp_beg ASC"
                );
                mq.setParam(2, month);
            }

            inProgData = mq.setParam(1, guardId).getRecords(conn);

            String red = "#D32F2F";
            String green = "#388E3C";
            String blue = "#21559D";
            String orange = "#ff8800";

            Date curdate = MySQLQuery.now(conn);
            List<MssShiftApp> shifts = new ArrayList<>();

            if (inProgData != null) {
                for (Object[] row : inProgData) {
                    MssShiftApp obj = new MssShiftApp();
                    obj.id = MySQLQuery.getAsInteger(row[0]);
                    obj.clientId = MySQLQuery.getAsInteger(row[1]);
                    obj.postId = MySQLQuery.getAsInteger(row[2]);
                    obj.postName = MySQLQuery.getAsString(row[3]);
                    obj.expBeg = MySQLQuery.getAsDate(row[4]);
                    obj.expEnd = MySQLQuery.getAsDate(row[5]);
                    obj.regBeg = MySQLQuery.getAsDate(row[6]);
                    obj.regEnd = MySQLQuery.getAsDate(row[7]);

                    if (obj.regEnd == null && obj.regBeg != null) {
                        obj.state = "Sin marcación de salida";
                        obj.hexColor = orange;
                    } else if (obj.regEnd != null && obj.regBeg != null) {
                        obj.state = "Finalizado";
                        obj.hexColor = green;
                    } else if (obj.regEnd != null && obj.regBeg == null) {
                        obj.state = "Sin marcación de entrada";
                        obj.hexColor = orange;
                    } else if (obj.regEnd == null && obj.regBeg == null) {
                        if (obj.expEnd.getTime() > curdate.getTime()) {
                            obj.state = "Pendiente";
                            obj.hexColor = blue;
                        } else {
                            obj.state = "No se realizó";
                            obj.hexColor = red;
                        }
                    }

                    shifts.add(obj);
                }
            }

            return createResponse(shifts);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/pendingShift")
    public Response pendingShift() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            ReviewInfo pendingRound = MssShift.getPendingShift(conn);
            return Response.ok(pendingRound).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/shiftStatus")
    public Response shiftStatus(@QueryParam("id") int id, @QueryParam("status") String status) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            if (!status.equals("ok") && !status.equals("fail")) {
                throw new Exception("Estado no esperado, Contacte a sistemas");
            }
            MssShift obj = new MssShift().select(id, conn);
            MssShift old = new MssShift().select(id, conn);
            obj.chkStatus = (status.equals("ok") ? "ok" : "fail");
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);

            ReviewInfo pendingRound = MssShift.getPendingShift(conn);

            return Response.ok(pendingRound).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private boolean isOverTime(MssCfg cfg, Date currDate, MssShift previuMssShift) {
        boolean overTime = false;
        if (cfg.maxWorkingHours > 0) {//lo maximo que trabaja un guarda
            GregorianCalendar gcCurr = new GregorianCalendar();
            GregorianCalendar gcLast = new GregorianCalendar();
            gcCurr.setTime(currDate);
            gcLast.setTime(previuMssShift.regBeg);

            int currHour = gcCurr.get(GregorianCalendar.HOUR_OF_DAY);
            int currMin = gcCurr.get(GregorianCalendar.MINUTE);
            int lastHour = gcLast.get(GregorianCalendar.HOUR_OF_DAY);
            int lastMin = gcLast.get(GregorianCalendar.MINUTE);

            int curTime = (currMin + (currHour * 60));
            int lasTime = (lastMin + (lastHour * 60));

            if (gcCurr.get(GregorianCalendar.DAY_OF_YEAR) > gcLast.get(GregorianCalendar.DAY_OF_YEAR)) {
                curTime = curTime + (24 * 60);
            }

            int workHours = curTime - lasTime;
            overTime = workHours > cfg.maxWorkingHours * 60;
        }
        return overTime;
    }

    private MssShift getPreviousEventualShift(Connection conn, int guardId) throws Exception {
        //verificar si tengo turnos eventuales antiguos del dia actual o anterior de tipo eventual osea q no tiene sc_code
        MySQLQuery qPreviousShift = new MySQLQuery("SELECT " + MssShift.getSelFlds("s") + " FROM mss_shift s "
                + "WHERE s.sc_code IS NULL AND s.reg_end IS NULL AND s.reg_beg IS NOT NULL "
                + "AND (CURDATE() = DATE(s.reg_beg) OR DATE_SUB(CURDATE(),INTERVAL 1 DAY) = DATE(s.reg_beg)) AND s.guard_id = ?1 "
                + "ORDER BY s.id DESC LIMIT 1 ").setParam(1, guardId);

        return new MssShift().select(qPreviousShift, conn);
    }

    private MssShift getPreviousProgShift(Connection conn, int guardId, int postId) throws Exception {
        return new MssShift().select(new MySQLQuery("SELECT " + MssShift.getSelFlds("s") + " "
                + "FROM mss_shift s "
                + "WHERE "
                + "s.guard_id = ?1 AND s.post_id = ?2 AND s.active AND s.rev_dt IS NULL "
                + "AND (s.reg_beg IS NULL OR s.reg_end IS NULL) "
                + "AND (DATE(s.exp_beg) = CURDATE() OR DATE(s.exp_end) = CURDATE()) "
                + "ORDER BY ABS(TIME_TO_SEC(TIMEDIFF(NOW() , IF(s.reg_beg IS NOT NULL, s.exp_end, s.exp_beg)))/60) ASC "
                + "LIMIT 1").setParam(1, guardId).setParam(2, postId), conn);
    }

    @POST
    @Path("/regEvent")
    public Response regEvent(@Context HttpServletRequest request) {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                SessionLogin sl = getSession(conn);

                MssCfg cfg = new MssCfg().select(1, conn);

                Integer guardId = getFromEmployee(sl.employeeId, MssGuard.TYPE_GUARD, conn);
                if (guardId == null) {
                    throw new Exception("No se encuentra registrado como guarda de seguridad");
                }

                fileManager.PathInfo pi = new fileManager.PathInfo(conn);
                MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);
                try {
                    Date currDate = MySQLQuery.now(conn);

                    String code = mr.params.get("postCode");
                    boolean makeRound = MySQLQuery.getAsBoolean(mr.params.get("makeRound"));
                    Integer postId = MySQLQuery.getAsInteger(mr.params.get("postId"));
                    BigDecimal lat = MySQLQuery.getAsBigDecimal(mr.params.get("lat"), true);
                    BigDecimal lon = MySQLQuery.getAsBigDecimal(mr.params.get("lon"), true);

                    System.out.println("lat: " + lat);
                    System.out.println("lon: " + lon);

                    if (postId != null) {
                        boolean hasShift = new MySQLQuery("SELECT COUNT(*) > 0 FROM mss_shift s WHERE s.post_id = ?1 AND DATE(s.reg_beg) = CURDATE() AND s.reg_end IS NULL").setParam(1, postId).getAsBoolean(conn);
                        if (hasShift) {
                            throw new Exception("Este puesto ya tiene registrado un ingreso de guarda");
                        }
                    }

                    MssPoint curPoint = getCurrentPoint(guardId, postId, code, lat, lon, cfg, conn);

                    if (curPoint != null) {
                        //throw new Exception("Error");
                    }
                    MssPost post = new MssPost().select(curPoint.postId, conn);

                    if (!curPoint.isCheck) {//verificar si no tiene punto
                        if (MssPoint.hasCheckPoint(curPoint.postId, conn)) {
                            throw new Exception("Punto invalido, debe escanear el punto de check in/out");
                        }
                        throw new Exception("Este puesto no tiene establecido un punto de check in/out "
                                + "Comuniquese con el area encargada");

                    }

                    MssShift shift;

                    boolean newEventualShift = false;

                    MssShift previousEventualShift = getPreviousEventualShift(conn, guardId);
                    MssShift previousProgShift = getPreviousProgShift(conn, guardId, post.id);

                    if (cfg.allowEventualShift && previousProgShift == null) {
                        if (previousEventualShift != null) {
                            MySQLQuery qPreviousPoint = new MySQLQuery("SELECT " + MssPoint.getSelFlds("p") + " "
                                    + "FROM mss_post ps "
                                    + "INNER JOIN mss_point p ON p.post_id = ps.id AND p.is_check = 1 "
                                    + "WHERE ps.id = ?1").setParam(1, previousEventualShift.postId);
                            MssPoint previousPoint = new MssPoint().select(qPreviousPoint, conn);

                            boolean overTime = isOverTime(cfg, currDate, previousEventualShift);
                            if (previousPoint.id != curPoint.id || overTime) {//creo un nuevo turno
                                shift = MssShift.createInstantShift(conn, guardId, post.id, currDate, makeRound, cfg);
                                newEventualShift = true;
                            } else {//si es el mismo punto y si no exede la jornada laboral
                                previousEventualShift.expEnd = currDate;
                                previousEventualShift.update(conn);
                                shift = previousEventualShift;
                            }
                        } else {//creo un nuevo turno
                            shift = MssShift.createInstantShift(conn, guardId, post.id, currDate, makeRound, cfg);
                            newEventualShift = true;
                        }
                    } else {
                        shift = previousProgShift;
                    }

                    if (shift == null) {
                        throw new Exception("No se encontró turnos disponibles para el día de hoy.");
                    }

                    MssShiftEvent d = new MssShiftEvent();
                    d.postName = post.name;

                    String red = "#D32F2F";
                    String green = "#388E3C";
                    String blue = "#21559D";
                    String orange = "#ff8800";

                    System.out.println("lat point: " + curPoint.lat.doubleValue());
                    System.out.println("lon point: " + curPoint.lon.doubleValue());
                    double distance = distance(curPoint.lat.doubleValue(), curPoint.lon.doubleValue(), lat.doubleValue(), lon.doubleValue(), "K") * 1000;
                    if (distance > cfg.pointRadiusTolerance) {
                        d.hexColor = red;
                        d.msg = "No se encuentra ubicado en el punto de check in/out del puesto";
                        return Response.ok(d).build();
                    }

                    boolean in = MySQLQuery.getAsDate(shift.regBeg) == null;

                    if (shift.regBeg != null) { //------------------------------------------------------------ Descomentar al acabar las pruebas
                        long t1 = currDate.getTime();
                        long t2 = (MySQLQuery.getAsDate(shift.regBeg)).getTime();
                        if (t1 - t2 < 180000) {//10 minutos de diferencia con el ultimo registro // 23-12-2020 posiblemente necesite un cfg andres solarte
                            d.hexColor = red;
                            d.msg = "Ya registró su marcacion en este puesto.\nIntente mas tarde.";
                            return Response.ok(d).build();
                        }
                    }

                    d.expected = (in ? "Ingreso Esperado:\t\t\t\t" : "Salida Esperada:\t\t\t") + Dates.getDateTimeFormat().format(in ? shift.expBeg : shift.expEnd);
                    d.registered = (in ? "Ingreso Registrado:\t\t" : "Salida Registrada:\t\t") + Dates.getDateTimeFormat().format(currDate);

                    int segDiff = timeToSeconds(in ? shift.expBeg : shift.expEnd) - timeToSeconds(currDate);
                    int minutsDiff = Math.abs(segDiff / 60);//por si se necesita usar                              

                    if (shift.scCode == null) {
                        d.hexColor = blue;
                        if (in) {
                            shift.regBeg = currDate;
                            d.msg = "Marcacion correcta en inicio de turno";

                        } else {
                            shift.expEnd = currDate;
                            shift.regEnd = currDate;
                            d.msg = "Marcacion correcta en salida de turno";
                        }
                    } else {
                        if (in) {//entrada
                            shift.regBeg = currDate;
                            shift.chkStatus = Math.random() < 0.3 ? "pending" : "skip";
                            if (segDiff < 0) {//tarde
                                d.msg = "Llega " + Math.abs(segDiff / 60) + " Minutos Tarde";
                                d.hexColor = orange;
                            } else {//temprano
                                d.msg = "Llega " + Math.abs(segDiff / 60) + " Minutos Temprano";
                                d.hexColor = green;
                            }
                        } else {//salida
                            shift.regEnd = currDate;
                            if (segDiff < 0) {//tarde
                                d.msg = "Se Marcha " + Math.abs(segDiff / 60) + " Minutos Tarde";
                                d.hexColor = green;
                            } else {//temprano
                                d.msg = "Se Marcha " + Math.abs(segDiff / 60) + " Minutos Temprano";
                                d.hexColor = orange;
                            }
                        }
                    }

                    shift.inTolerance = cfg.inTolerance;
                    shift.anticipation = cfg.anticipation;
                    shift.makeRound = makeRound;

                    shift.update(conn);

                    if (newEventualShift) {
                        MailCfg cfgMail = MailCfg.select(conn);
                        String html = SendMail.getHtmlMsg(conn, "Nuevo Turno Eventual desde App",
                                "Se ha registrado un nuevo turno eventual con los siguientes datos:<br/>"
                                + "<br/><b>Fecha de Ingeso:</b> " + Dates.getDateTimeFormat().format(currDate)
                                + "<br/><b>Guarda:</b> " + new MySQLQuery("SELECT CONCAT(first_name, ' ', last_name, ' - doc: ', document) FROM mss_guard WHERE id = ?1").setParam(1, guardId).getAsString(conn)
                                + "<br/><b>Puesto:</b> " + post.name
                        );
                        SendMail.sendMail(cfgMail, new MySQLQuery("SELECT admin_mail FROM mss_cfg WHERE id = 1").getAsString(conn), "Nuevo Turno Eventual", html, "123123", null, null, null, null);
                    }

                    if (mr.files.containsKey("photoFile")) {
                        fileManager.upload(
                                sl.employeeId,
                                shift.id, //ownerId
                                null,//ownerType, 
                                "mss_shift", //tableName
                                (in ? "foto_entrada" : "foto_salida") + ".jpg", //fileName, 
                                "Foto " + (in ? "entrada" : "salida"), //desc, 
                                false, //unique
                                null,//shrinkType
                                pi,
                                mr.files.get("photoFile").file,
                                conn
                        );
                    }

                    if (mr.files.containsKey("qrFile")) {
                        fileManager.upload(
                                sl.employeeId,
                                shift.id, //ownerId
                                null,//ownerType, 
                                "mss_shift", //tableName
                                (in ? "qr_entrada" : "qr_salida") + ".jpg", //fileName, 
                                "QR " + (in ? "entrada" : "salida"), //desc, 
                                false, //unique
                                null,//shrinkType
                                pi,
                                mr.files.get("qrFile").file,
                                conn
                        );
                    }

                    conn.commit();
                    return Response.ok(d).build();
                } finally {
                    mr.deleteFiles();
                }
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/shiftInfo")
    public Response getShiftsTurn() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            Integer guardId = getFromEmployee(sl.employeeId, MssGuard.TYPE_GUARD, conn);
            if (guardId == null) {
                throw new Exception("No se encuentra registrado como guarda de seguridad");
            }

            Object[] record = new MySQLQuery("SELECT  "
                    + " s.id, p.client_id, "
                    + " p.id, p.name, "
                    + " s.exp_beg, s.exp_end, "
                    + " s.reg_beg, s.reg_end, "
                    + " s.make_round "
                    + " FROM mss_shift s "
                    + " INNER JOIN mss_post p ON p.id = s.post_id  "
                    + " WHERE s.active  "
                    + " AND (s.reg_beg IS NOT NULL AND s.reg_end IS NULL) "
                    + " AND (DATE(s.exp_beg) = CURDATE() OR DATE(s.exp_end) = CURDATE()) "
                    + " AND s.guard_id = ?1 "
                    + " ORDER BY s.exp_beg ASC LIMIT 1 ").setParam(1, guardId).getRecord(conn);

            if (record == null || record.length == 0) {
                return createResponse(null);
            }

            MssShiftApp obj = new MssShiftApp();
            obj.id = MySQLQuery.getAsInteger(record[0]);
            obj.clientId = MySQLQuery.getAsInteger(record[1]);
            obj.postId = MySQLQuery.getAsInteger(record[2]);
            obj.postName = MySQLQuery.getAsString(record[3]);
            obj.expBeg = MySQLQuery.getAsDate(record[4]);
            obj.expEnd = MySQLQuery.getAsDate(record[5]);
            obj.regBeg = MySQLQuery.getAsDate(record[6]);
            obj.regEnd = MySQLQuery.getAsDate(record[7]);
            obj.makeRound = MySQLQuery.getAsBoolean(record[8]);

            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("mosaic")
    public Response getMosaic(@QueryParam("id") int shiftId) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            List<Integer> bFileIds = new ArrayList<>();

            Integer empPhotoId = new MySQLQuery("SELECT f.id FROM "
                    + "mss_shift s "
                    + "INNER JOIN mss_guard g ON s.guard_id = g.id "
                    + "INNER JOIN bfile f ON f.owner_id = g.id AND f.`table` = 'mss_guard' "
                    + "WHERE s.id = ?1 LIMIT 1").setParam(1, shiftId).getAsInteger(conn);

            Object[][] postPhotoData = new MySQLQuery("SELECT f.id FROM "
                    + "mss_shift s "
                    + "INNER JOIN mss_point p ON p.post_id = s.post_id AND p.is_check "
                    + "INNER JOIN bfile f ON f.owner_id = p.id AND f.`table` = 'mss_point' "
                    + "WHERE s.id = ?1").setParam(1, shiftId).getRecords(conn);

            Object[][] shiftPhotoData = new MySQLQuery("SELECT f.id FROM "
                    + "mss_shift s "
                    + "INNER JOIN bfile f ON f.owner_id = s.id AND f.`table` = 'mss_shift' "
                    + "WHERE s.id = ?1 "
                    + "ORDER BY f.created ASC").setParam(1, shiftId).getRecords(conn);

            if (empPhotoId != null) {
                bFileIds.add(empPhotoId);
            }

            for (Object[] postPhotoRow : postPhotoData) {
                bFileIds.add(cast.asInt(postPhotoRow, 0));
            }

            for (Object[] shiftPhotoRow : shiftPhotoData) {
                bFileIds.add(cast.asInt(shiftPhotoRow, 0));
            }

            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            return createResponse(Mosaic.mosaic(bFileIds, pi, 1920, 2, Color.white), "evidencia.jpg");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private static int timeToSeconds(Date dt) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(dt);
        return (gc.get(GregorianCalendar.HOUR_OF_DAY) * 3600) + (gc.get(GregorianCalendar.MINUTE) * 60) + gc.get(GregorianCalendar.SECOND);
    }

    @POST
    @Path("/importShift")
    public Response importShift(@Context HttpServletRequest request) throws Exception {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                getSession(conn);

                fileManager.PathInfo pi = new fileManager.PathInfo(conn);
                MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);

                if (!mr.getFile().isXls()) {
                    throw new Exception("El archivo no tiene el formato .xls");
                }

                Object[][] data = XlsReader.readExcel(mr.getFile().file.getPath(), 0).getData();

                if (data == null || data.length < 2) {
                    throw new Exception("El archivo no contiene registros");
                }

                GregorianCalendar gc = new GregorianCalendar();;

                if (data[1] != null && data[1].length > 1
                        && MySQLQuery.getAsString(data[1][0]).toUpperCase().equals("DESDE")) {
                    try {
                        String[] datePart = MySQLQuery.getAsString(data[1][1]).split("-");
                        gc.set(GregorianCalendar.YEAR, Integer.valueOf(datePart[0]));
                        gc.set(GregorianCalendar.MONTH, Integer.valueOf(datePart[1]) - 1);
                    } catch (NumberFormatException ex) {
                        throw new Exception("El archivo no contiene las fechas de importación en formato correcto");
                    }
                } else {
                    throw new Exception("El archivo no contiene la fecha de importación");
                }

                MD5 md = MD5.getInstance();

                SysAppProfile guardProfile = SysAppProfile.getProfileByName("com.qualisys.minutas", "Guardas", conn);
                if (guardProfile == null) {
                    throw new Exception("No se ha creado el perfil para guardas. Comuniquese con sistemas.");
                }

                List<ImporterCol> cols = new ArrayList<>();
                cols.add(new ImporterCol("C.C. Empleado", ImporterCol.TYPE_DATE, false));
                cols.add(new ImporterCol("NOMBRE EMPLEADO", ImporterCol.TYPE_TEXT, false));
                cols.add(new ImporterCol("UBICACION", ImporterCol.TYPE_TEXT, false));
                cols.add(new ImporterCol("DESCRIPCION UBICACION", ImporterCol.TYPE_TEXT, false));
                cols.add(new ImporterCol("PUESTO", ImporterCol.TYPE_TEXT, false));
                cols.add(new ImporterCol("DESCRIPCION PUESTO", ImporterCol.TYPE_TEXT, false));

                for (int i = 1; i <= 31; i++) {
                    cols.add(new ImporterCol(i + "", ImporterCol.TYPE_TEXT, true));
                }

                cols.add(new ImporterCol("CENTRO DE OPERACION", ImporterCol.TYPE_TEXT, false));
                cols.add(new ImporterCol("DESCRIPCION CENTRO DE OPERACION", ImporterCol.TYPE_TEXT, false));
                cols.add(new ImporterCol("CENTRO DE COSTOS MAYOR(UBICACION)", ImporterCol.TYPE_TEXT, true));
                cols.add(new ImporterCol("DESCRIPCION CENTRO DE COSTOS MAYOR(UBICACION)", ImporterCol.TYPE_TEXT, true));
                cols.add(new ImporterCol("CENTRO DE COSTOS", ImporterCol.TYPE_TEXT, false));
                cols.add(new ImporterCol("DESCRIPCION CENTRO DE COSTOS", ImporterCol.TYPE_TEXT, false));
                cols.add(new ImporterCol("UNIDAD DE NEGOCIO", ImporterCol.TYPE_TEXT, false));
                cols.add(new ImporterCol("DESCRIPCION UNIDAD DE NEGOCIO", ImporterCol.TYPE_TEXT, false));
                cols.add(new ImporterCol("PROYECTO", ImporterCol.TYPE_TEXT, false));
                cols.add(new ImporterCol("DESCRIPCION PROYECTO", ImporterCol.TYPE_TEXT, false));
                cols.add(new ImporterCol("CLIENTE", ImporterCol.TYPE_TEXT, true));
                cols.add(new ImporterCol("RAZON SOCIAL CLIENTE", ImporterCol.TYPE_TEXT, true));

                Importer importer = new Importer(data, cols);

                for (int i = importer.headRow + 1; i < data.length; i++) {
                    importer.row = data[i];
                    Object[] row = importer.row;

                    String doc = clearString(row[0]);
                    String name = clearString(row[1]);
                    name = name.toUpperCase().replaceAll("DE LA ", "DE_LA_")
                            .replaceAll("DE LOS ", "DE_LOS_").replaceAll("DEL ", "DEL_")
                            .replaceAll("DE ", "DE_").trim();
                    String nameParts[] = name.split(" ");
                    String firstName;
                    String lastName;
                    switch (nameParts.length) {
                        case 1:
                            throw new Exception("El nombre del guarda en la fila " + (i + 1) + " esta incompleto");
                        case 2:
                            lastName = nameParts[0];
                            firstName = nameParts[1];
                            break;
                        case 3:
                            lastName = nameParts[0] + " " + nameParts[1];
                            firstName = nameParts[2];
                            break;
                        default:
                            lastName = nameParts[0] + " " + nameParts[1];
                            firstName = nameParts[2] + " " + nameParts[3]
                                    + ((nameParts.length > 4 ? " " + nameParts[4] : ""));
                            break;
                    }

                    Employee emp = Employee.getByDoc(doc, conn);

                    if (emp == null) {
                        emp = new Employee();
                        emp.document = doc;
                        emp.firstName = Strings.toTitleType(firstName.replaceAll("_", " ")).trim();
                        emp.lastName = Strings.toTitleType(lastName.replaceAll("_", " ")).trim();
                        emp.login = doc;
                        emp.password = md.hashData(emp.document.getBytes());;
                        emp.active = true;
                        emp.insert(conn);

                    }

                    if (!SysAppProfileEmp.hasProfileByEmp(emp.id, guardProfile.id, conn)) {
                        SysAppProfileEmp pemp = new SysAppProfileEmp();
                        pemp.empId = emp.id;
                        pemp.appProfileId = guardProfile.id;
                        pemp.insert(conn);
                    }

                    MssGuard guard = MssGuard.getByDoc(doc, "guard", conn);

                    if (guard == null) {
                        guard = new MssGuard();
                        guard.document = doc;
                        guard.firstName = emp.firstName;
                        guard.lastName = emp.lastName;
                        guard.empId = emp.id;
                        guard.supervisor = false;
                        guard.type = "guard";
                        guard.insert(conn);
                    }

                    //Sys_center
                    String centerCode = clearString(row[37]);
                    String centerName = Strings.toTitleType(clearString(row[38]));
                    SysCenter center = SysCenter.getSysCenterByCode(centerCode, conn);

                    if (center == null) {

                        Integer typeId = new MySQLQuery("SELECT id FROM sys_center_type WHERE name like '%Otro%'").getAsInteger(conn);
                        if (typeId == null) {
                            throw new Exception("No se ha creado un tipo de centro operativo");
                        }
                        center = new SysCenter();
                        center.name = centerName;
                        center.code = centerCode;
                        center.active = true;
                        center.typeId = typeId;
                        center.insert(conn);
                    }

                    String cliCode = clearString(row[2]);
                    String cliName = Strings.toTitleType(clearString(row[3]));
                    String cliNit = row.length > 47 ? clearString(row[47]) : null;

                    MssClient cli = MssClient.getClientByCode(cliCode, conn);
                    if (cli == null) {
                        cli = new MssClient();
                        cli.code = cliCode;
                        cli.name = cliName;
                        cli.nit = cliNit;
                        cli.active = true;
                        cli.insert(conn);
                    }

                    String postName = Strings.toTitleType(clearString(row[5]));
                    String postCode = Strings.toTitleType(clearString(row[4]));
                    String postNotes = Strings.toTitleType(clearString(row[44]) + "-" + clearString(row[45]));

                    MssPost post = MssPost.getByCode(postCode, conn);
                    if (post == null) {
                        post = new MssPost();
                        post.code = postCode;
                        post.name = postName;
                        post.clientId = cli.id;
                        post.active = true;
                        post.sysCenterId = center.id;
                        post.notes = postNotes;
                        post.insert(conn);
                    }

                    Date today = Dates.trimDate(MySQLQuery.now(conn));

                    int limit = gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);

                    List<String> scannedCodes = new ArrayList<>();

                    for (int j = 6; j <= limit + 5; j++) {
                        String scheduleCode = clearString(row[j]);
                        gc.set(GregorianCalendar.DAY_OF_MONTH, j - 5);
                        Date scDate = Dates.trimDate(gc.getTime());

                        if (!scannedCodes.contains(scheduleCode) && !MySQLQuery.isEmpty(scheduleCode)
                                && !getCointainsKeys(scheduleCode) && scDate.compareTo(today) > 0) {
                            MySQLQuery q = new MySQLQuery("SELECT COUNT(*) > 0 "
                                    + "FROM mss_schedule_code sc WHERE UPPER(sc.code) = ?1").setParam(1, scheduleCode.toUpperCase());
                            if (!q.getAsBoolean(conn)) {
                                throw new Exception("Código " + scheduleCode + " no encontrado, primero debe importar el Excel de jornadas");
                            } else {
                                scannedCodes.add(scheduleCode);
                            }
                        }
                    }

                    for (int j = 6; j <= limit + 5; j++) {
                        String scheduleCode = clearString(row[j]);
                        gc.set(GregorianCalendar.DAY_OF_MONTH, j - 5);
                        Date scDate = Dates.trimDate(gc.getTime());
                        if (scDate.compareTo(today) >= 0) {

                            if (MySQLQuery.isEmpty(scheduleCode) || getCointainsKeys(scheduleCode)) {
                                MySQLQuery q = new MySQLQuery("DELETE FROM mss_shift "
                                        + "WHERE guard_id = ?2 AND post_id = ?3 AND "
                                        + "DATE(exp_beg) = ?4 AND reg_beg IS NULL AND reg_end IS NULL");
                                q.setParam(2, guard.id);
                                q.setParam(3, post.id);
                                q.setParam(4, scDate);
                                q.executeDelete(conn);
                            } else {
                                MySQLQuery q = new MySQLQuery("DELETE FROM mss_shift "
                                        + "WHERE guard_id = ?2 AND post_id = ?3 AND "
                                        + "sc_code = ?1 AND DATE(exp_beg) = ?4 AND reg_beg IS NULL AND reg_end IS NULL");
                                q.setParam(1, scheduleCode.toUpperCase());
                                q.setParam(2, guard.id);
                                q.setParam(3, post.id);
                                q.setParam(4, scDate);
                                q.executeDelete(conn);

                                q = new MySQLQuery("INSERT INTO mss_shift (`guard_id`, `post_id`,`active`,`exp_beg`, `exp_end`,"
                                        + "`sc_code`,`chk_status`, `reg_chk`,`notified`,`make_round` ) "
                                        + "(SELECT ?2, ?3, 1, "
                                        + "DATE_ADD(?4,INTERVAL TIME_TO_SEC(sc.beg_time) SECOND), "
                                        + "DATE_ADD(?4,INTERVAL TIME_TO_SEC(sc.end_time) SECOND), "
                                        + "sc.code, IF(RAND()<0.2,'pending','skip'), NOW(), 0, 0 "
                                        + "FROM mss_schedule_code sc WHERE UPPER(sc.code) = ?1 AND sc.beg_time <> sc.end_time ) ");
                                q.setParam(1, scheduleCode);
                                q.setParam(2, guard.id);
                                q.setParam(3, post.id);
                                q.setParam(4, scDate);
                                q.executeUpdate(conn);
                            }
                        }
                    }
                    new MySQLQuery("UPDATE mss_shift SET exp_end = DATE_ADD(exp_end, INTERVAL 1 DAY) WHERE exp_beg > exp_end").executeUpdate(conn);

                }
                mr.deleteFiles();
                conn.commit();
                return createResponse("ok");
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }

        } catch (Exception ex) {

            return createResponse(ex);
        }
    }

    private boolean getCointainsKeys(String scheduleCode) {
        return (scheduleCode.contains("[") && scheduleCode.contains("]"));
    }

    private String clearString(Object obj) {
        if (obj == null) {
            return null;
        }
        String regex = "\\s+";
        return MySQLQuery.getAsString(obj).replaceAll(regex, " ").trim();
    }

    private class RowImport implements Comparable<RowImport> {

        public int regNum;
        public Date date;
        public Integer unitId;
        public String notes;

        public Comparator<RowImport> provComparator = new Comparator<RowImport>() {
            @Override
            public int compare(RowImport o1, RowImport o2) {
                int obj1 = o1.regNum;
                int obj2 = o2.regNum;
                return obj2 - obj1;
            }
        };

        @Override
        public int compareTo(RowImport o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @POST
    @Path("/unchecked")
    public Response getUnchecked(GridRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult tbl = getUncheckedGrid(conn);
            return createResponse(tbl);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Produces(value = "application/vnd.ms-excel")
    @Path("/unchecked")
    public Response exportUnchecked(GridRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult r = getUncheckedGrid(conn);
            MySQLReport rep = new MySQLReport("Turnos con Inconsistencias", null, "hoja1", now(conn));
            rep.setVerticalFreeze(5);
            rep.setZoomFactor(80);
            rep.setShowNumbers(true);
            rep.setMultiRowTitles(true);
            rep.getTables().add(new Table("Turnos con Inconsistencias"));

            rep = MySQLReport.getReport(rep, r.cols, r.data);
            return createResponse(rep.write(conn), "turnos_inconsistentes.xls");

        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private GridResult getUncheckedGrid(Connection conn) throws Exception {

        GridResult gr = new GridResult();

        gr.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_KEY),
            new MySQLCol(MySQLCol.TYPE_TEXT, 50, "Cliente"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 25, "Puesto"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 50, "Guarda"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_A, 60, "Inicio Esperado"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_A, 60, "Inicio Registrado"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_A, 60, "Fin Esperado"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_A, 60, "Fin Registrado"),};
        gr.cols[2].toString = true;

        gr.data = new MySQLQuery("SELECT "
                + " s.id, c.code, p.code, "
                + " g.document, "
                + "s.exp_beg, s.reg_beg, "
                + "s.exp_end, s.reg_end "
                + " FROM mss_shift s "
                + " INNER JOIN mss_post p ON p.id = s.post_id "
                + " INNER JOIN mss_client c ON c.id = p.client_id "
                + " INNER JOIN mss_guard g ON g.id = s.guard_id "
                + " WHERE "
                + " s.rev_dt IS NULL AND p.begin_dt <= DATE(s.exp_beg) AND p.active AND s.active AND ( "
                + " (s.exp_beg < NOW() AND s.reg_beg IS NULL) OR "
                + " (s.exp_end < NOW() AND s.reg_end IS NULL) OR "
                + " (TO_SECONDS(s.reg_end) - TO_SECONDS(s.exp_end)) < 0 OR "
                + " (TO_SECONDS(s.reg_beg) NOT BETWEEN (TO_SECONDS(DATE_SUB( DATE_SUB(s.exp_beg, INTERVAL (s.anticipation *60) SECOND) , INTERVAL (s.in_tolerance * 60) SECOND))) AND "
                + " (TO_SECONDS(DATE_ADD( DATE_SUB(s.exp_beg, INTERVAL (s.anticipation *60) SECOND) , INTERVAL (s.in_tolerance * 60) SECOND))))) "
        ).getRecords(conn);

        //para entrada: var anticipacion 15, var tolerancia 2 = 13 - 17 // guardas dos campos en la shift
        //para salida no debe salir antes de lo que dice la salida esperada            
        gr.sortType = GridResult.SORT_DESC;
        gr.sortColIndex = 3;
        return gr;
    }

    @POST
    @Path("/checkPostsByPoint")
    public Response checkPostByPoints(MssPointDto dto) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssCfg cfg = new MssCfg().select(1, conn);
            Integer guardId = getFromEmployee(sl.employeeId, MssGuard.TYPE_GUARD, conn);
            if (guardId == null) {
                throw new Exception("No se encuentra registrado como guarda de seguridad");
            }
            MssPoint currentPoint = getCurrentPoint(guardId, null, dto.code, dto.lat, dto.lon, cfg, conn);
            List<PostApp> rta = new ArrayList<>();
            if (currentPoint == null) {
                MySQLQuery qPost = new MySQLQuery("SELECT " + MssPost.getSelFlds("ps") + " , (IFNULL(p.lat," + dto.lat + " ) - (" + dto.lat + ")) AS difLat, (IFNULL(p.lon, " + dto.lon + ") - (" + dto.lon + ")) AS difLon, ps.id, p.lat, p.lon "
                        + "FROM mss_point p "
                        + "INNER JOIN mss_post ps ON ps.id = p.post_id "
                        + "WHERE p.code = ?1 "
                        + "GROUP BY ps.id "
                        + "ORDER BY difLat DESC, difLon DESC; ").setParam(1, dto.code);
                Object[][] data = qPost.getRecords(conn);
                List<MssPost> listPost = MssPost.getList(data);
                int intValues = MssPost.getIntValues();
                for (int i = 0; i < listPost.size(); i++) {
                    MssPost post = listPost.get(i);
                    BigDecimal latitud = MySQLQuery.getAsBigDecimal(data[i][intValues + 4], true);
                    BigDecimal longitud = MySQLQuery.getAsBigDecimal(data[i][intValues + 5], true);
                    double distance = distance(latitud.doubleValue(), longitud.doubleValue(), dto.lat.doubleValue(), dto.lon.doubleValue(), "K") * 1000;
                    if (distance <= cfg.pointRadiusTolerance) {
                        PostApp obj = new PostApp();
                        obj.postId = post.id;
                        obj.postName = post.name;
                        rta.add(obj);
                    }

                }
            }
            return createResponse(rta);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private MssPoint getCurrentPoint(int guardId, Integer postId, String code, BigDecimal lat, BigDecimal lon, MssCfg cfg, Connection conn) throws Exception {
        List<MssPoint> points = MssPoint.getPointsByQrCode(code, conn);
        Date currDate = now(conn);

        if (MySQLQuery.isEmpty(points)) {
            throw new Exception("El código no corresponde a un puesto");
        }

        MySQLQuery qPost = new MySQLQuery("SELECT p.id, s.id "
                + "FROM mss_point p "
                + "INNER JOIN mss_post ps ON ps.id = p.post_id "
                + "INNER JOIN mss_shift s ON s.post_id = ps.id  "
                + "WHERE p.code = ?1 "
                + "AND s.guard_id = ?2 "
                + "AND (CURDATE() = DATE(s.exp_beg) OR CURDATE() = DATE(s.exp_end)) "
                + "AND s.sc_code IS NOT NULL " //para turnos por importador o manuales
                + "LIMIT 1")
                .setParam(1, code).setParam(2, guardId);

        Object[] data = qPost.getRecord(conn);

        MssPoint curPoint = null;
        if (postId != null && data != null && data.length > 0) {
            curPoint = new MssPoint().select(MySQLQuery.getAsInteger(data[0]), conn);
            MssShift mssShift = new MssShift().select(MySQLQuery.getAsInteger(data[1]), conn);

            mssShift.postId = postId;
            mssShift.update(conn);
        }

        if (curPoint == null || cfg.allowEventualShift) {
            if (postId == null) {
                //verificar si tengo puntos anteriores                
                MssShift previousEventualShift = getPreviousEventualShift(conn, guardId);
                if (previousEventualShift != null) {
                    boolean overTime = isOverTime(cfg, currDate, previousEventualShift);
                    if (!overTime) {
                        postId = previousEventualShift.postId;
                    }
                }
            }

            //traer punto y puesto segun codigo QR|
            MySQLQuery qPoint = new MySQLQuery("SELECT " + MssPoint.getSelFlds("p") + " , (IFNULL(p.lat," + lat + " ) - (" + lat + ")) AS difLat, (IFNULL(p.lon, " + lon + ") - (" + lon + ")) AS difLon, p.id "
                    + "FROM mss_point p WHERE p.code = ?1 " + (postId != null ? "AND p.post_id = ?2 " : "")
                    + "ORDER BY difLat DESC, difLon DESC; ").setParam(1, code);
            if (postId != null) {
                qPoint.setParam(2, postId);
            }
            List<MssPoint> listPoints = MssPoint.getList(qPoint, conn);
            if (listPoints.size() == 1) {
                curPoint = listPoints.get(0);
            } else {
                return null;
            }

        } else {
            throw new Exception("No se encontró turnos disponibles para el día de hoy.");
        }
        return curPoint;
    }
}
