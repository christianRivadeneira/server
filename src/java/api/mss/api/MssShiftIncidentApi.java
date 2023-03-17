package api.mss.api;

import api.BaseAPI;
import api.mss.dto.AppShiftIncident;
import api.mss.model.MssShiftIncident;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import utilities.ServerNow;
import web.quality.MailCfg;
import web.quality.SendMail;

@Path("/mssShiftIncident")
public class MssShiftIncidentApi extends BaseAPI {
    
    public static final String TZ = "GMT-05:00";

    @POST
    public Response insert(MssShiftIncident obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.regDt = new ServerNow();
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);

            try {
                if (obj.priority.equals("high")) {
                    Object[] data = new MySQLQuery("SELECT t.name, si.notes, DATE_FORMAT(si.reg_dt, '%d/%c/%Y %H:%i:%s') fec_reg , CONCAT(g.first_name, ' ', g.last_name), p.name "
                            + "FROM mss_shift_incident si "
                            + "INNER JOIN mss_shift_incident_type t ON t.id = si.shift_incident_type_id "
                            + "INNER JOIN mss_shift s ON s.id = si.shift_id "
                            + "INNER JOIN mss_guard g ON g.id = s.guard_id "
                            + "INNER JOIN mss_post p ON p.id = s.post_id "
                            + "WHERE si.id = ?1").setParam(1, obj.id).getRecord(conn);

                    MailCfg cfg = MailCfg.select(conn);
                    String html = SendMail.getHtmlMsg(conn, "Novedad de Turno",
                            "Se ha creado una novedad de turno con prioridad alta:<br/>"
                            + "<br/><b>Fecha de Registro:</b> " + MySQLQuery.getAsString(data[2])
                            + "<br/><b>Guarda:</b> " + MySQLQuery.getAsString(data[3])
                            + "<br/><b>Puesto:</b> " + MySQLQuery.getAsString(data[4])
                            + "<br/><b>Tipo de Novedad: </b>" + MySQLQuery.getAsString(data[0])
                            + "<br/><b>Notas:</b> " + MySQLQuery.getAsString(data[1])
                    );
                    SendMail.sendMail(cfg, new MySQLQuery("SELECT admin_mail FROM mss_cfg WHERE id = 1").getAsString(conn), "Novedad de Turno", html, TZ, null, null, null, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(MssShiftIncident obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssShiftIncident old = new MssShiftIncident().select(obj.id, conn);
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/incidentApp")
    public Response appUpdate(MssShiftIncident obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            MssShiftIncident newObj = new MssShiftIncident().select(obj.id, conn);
            newObj.notes = obj.notes;
            newObj.shiftIncidentTypeId = obj.shiftIncidentTypeId;

            MssShiftIncident old = new MssShiftIncident().select(obj.id, conn);
            newObj.update(conn);
            SysCrudLog.updated(this, newObj, old, conn);

            try {
                if (old.priority.equals("low") && newObj.priority.equals("high")) {
                    Object[] data = new MySQLQuery("SELECT t.name, si.notes, DATE_FORMAT(si.reg_dt, '%d/%c/%Y %H:%i:%s') fec_reg , CONCAT(g.first_name, ' ', g.last_name), p.name "
                            + "FROM mss_shift_incident si "
                            + "INNER JOIN mss_shift_incident_type t ON t.id = si.shift_incident_type_id "
                            + "INNER JOIN mss_shift s ON s.id = si.shift_id "
                            + "INNER JOIN mss_guard g ON g.id = s.guard_id "
                            + "INNER JOIN mss_post p ON p.id = s.post_id "
                            + "WHERE si.id = ?1").setParam(1, newObj.id).getRecord(conn);

                    MailCfg cfg = MailCfg.select(conn);
                    String html = SendMail.getHtmlMsg(conn, "Novedad de Turno",
                            "Se actualizó una novedad de turno con prioridad alta:<br/>"
                            + "<br/><b>Fecha de Registro:</b> " + MySQLQuery.getAsString(data[2])
                            + "<br/><b>Guarda:</b> " + MySQLQuery.getAsString(data[3])
                            + "<br/><b>Puesto:</b> " + MySQLQuery.getAsString(data[4])
                            + "<br/><b>Tipo de Novedad: </b>" + MySQLQuery.getAsString(data[0])
                            + "<br/><b>Notas:</b> " + MySQLQuery.getAsString(data[1])
                    );
                    SendMail.sendMail(cfg, new MySQLQuery("SELECT admin_mail FROM mss_cfg WHERE id = 1").getAsString(conn), "Novedad de Turno", html, "123123", null, null, null, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Response.ok(newObj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssShiftIncident obj = new MssShiftIncident().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/closeIncident")
    public Response closeIncident(MssShiftIncident obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MssShiftIncident old = new MssShiftIncident().select(obj.id, conn);
            obj.closeDt = now(conn);
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/incidentApp")
    public Response getIncident(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            Object[] row = new MySQLQuery("SELECT "
                    + " i.id, i.reg_dt, i.notes, t.name, t.id, i.priority "
                    + " FROM mss_shift_incident i"
                    + " INNER JOIN mss_shift_incident_type t ON t.id = i.shift_incident_type_id "
                    + " WHERE i.id = ?1"
            ).setParam(1, id).getRecord(conn);

            AppShiftIncident obj = new AppShiftIncident();
            obj.id = MySQLQuery.getAsInteger(row[0]);
            obj.regDt = MySQLQuery.getAsDate(row[1]);
            obj.notes = MySQLQuery.getAsString(row[2]);
            obj.type = MySQLQuery.getAsString(row[3]);
            obj.typeId = MySQLQuery.getAsInteger(row[4]);
            obj.priority = MySQLQuery.getAsString(row[5]);

            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssShiftIncident.delete(id, conn);
            SysCrudLog.deleted(this, MssShiftIncident.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/all")
    public Response all() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(MssShiftIncident.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/getAll")
    public Response getAll(@QueryParam("shiftId") String shiftId) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            Object[][] inProgData = new MySQLQuery("SELECT "
                    + " i.reg_dt, i.notes, t.name, i.id, i.priority "
                    + " FROM mss_shift_incident i"
                    + " INNER JOIN mss_shift_incident_type t ON t.id = i.shift_incident_type_id "
                    + " WHERE i.shift_id = ?1"
            ).setParam(1, shiftId).getRecords(conn);

            List<AppShiftIncident> shifts = new ArrayList<>();
            for (Object[] row : inProgData) {
                AppShiftIncident obj = new AppShiftIncident();
                obj.regDt = MySQLQuery.getAsDate(row[0]);
                obj.notes = MySQLQuery.getAsString(row[1]);
                obj.type = MySQLQuery.getAsString(row[2]);
                obj.id = MySQLQuery.getAsInteger(row[3]);
                obj.priority = MySQLQuery.getAsString(row[4]);
                shifts.add(obj);
            }
            return createResponse(shifts);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
