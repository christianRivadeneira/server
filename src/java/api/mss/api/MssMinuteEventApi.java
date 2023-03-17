package api.mss.api;

import api.BaseAPI;
import api.mss.dto.MssAppRowDto;
import api.mss.dto.MssEventCreationRequestDto;
import api.mss.dto.MssEventFldDto;
import api.mss.model.MssGuard;
import static api.mss.model.MssGuard.getFromEmployee;
import api.mss.model.MssMinute;
import api.mss.model.MssMinuteEvent;
import api.mss.model.MssMinuteField;
import api.mss.model.MssMinuteIncident;
import api.mss.model.MssMinuteIncidentType;
import api.mss.model.MssMinuteMail;
import api.mss.model.MssMinuteType;
import api.mss.model.MssMinuteValue;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
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
import web.quality.SendMail;

@Path("/mssMinuteEvent")
public class MssMinuteEventApi extends BaseAPI {

    @POST
    public Response insert(MssMinuteEvent obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(MssMinuteEvent obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssMinuteEvent old = new MssMinuteEvent().select(obj.id, conn);
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssMinuteEvent obj = new MssMinuteEvent().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssMinuteEvent.delete(id, conn);
            SysCrudLog.deleted(this, MssMinuteEvent.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/creation")
    public Response creation(MssEventCreationRequestDto req) {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                SessionLogin sl = getSession(conn);

                Integer guardId = getFromEmployee(sl.employeeId, MssGuard.TYPE_GUARD, conn);

                if (guardId == null) {
                    throw new Exception("No se encuentra registrado como guarda de seguridad");
                }
                Integer minuteId = new MySQLQuery("SELECT m.id "
                        + "FROM mss_minute m WHERE m.dt = CURDATE() "
                        + "AND m.post_id = ?2 "
                        + "AND m.type_id =?3").setParam(2, req.postId).setParam(3, req.typeId).getAsInteger(conn);
                if (minuteId == null) {
                    MssMinute minu = new MssMinute();
                    minu.postId = req.postId;
                    minu.typeId = req.typeId;
                    minu.dt = new ServerNow();
                    minuteId = minu.insert(conn);
                }

                MssMinuteEvent ev = new MssMinuteEvent();
                ev.regDate = new ServerNow();
                ev.type = req.eventType;
                ev.notes = req.notes;
                ev.guardId = guardId;
                ev.minuteId = minuteId;
                int eventId = ev.insert(conn);
                
                for (int i = 0; i < req.flds.size(); i++) {
                    MssMinuteValue val = new MssMinuteValue();
                    val.minuteEventId = eventId;
                    val.minuteFieldId = req.flds.get(i).fldId;
                    val.value = req.flds.get(i).value;
                    val.insert(conn);
                }

                if (req.incidentNotes != null) {
                    MssMinuteIncident inc = new MssMinuteIncident();
                    inc.notes = req.incidentNotes;
                    inc.typeId = req.incidentTypeId;
                    inc.eventId = eventId;
                    inc.insert(conn);
                    List<MssMinuteMail> mails = MssMinuteMail.getByIncidentType(req.incidentTypeId, conn);

                    MssMinuteIncidentType t = new MssMinuteIncidentType().select(req.incidentTypeId, conn);

                    StringBuilder sb = new StringBuilder();
                    MssMinuteType mt = new MssMinuteType().select(req.typeId, conn);

                    sb.append("Se registró una novedad de tipo ").append(t.name).append(" en la minuta tipo ").append(mt.name).append("<br>");
                    sb.append("<b>Información:</b><br>");
                    for (int i = 0; i < req.flds.size(); i++) {
                        MssEventFldDto fldDto = req.flds.get(i);
                        MssMinuteField fld = new MssMinuteField().select(fldDto.fldId, conn);
                        sb.append("<b>").append(fld.name).append(":</b> ").append(fldDto.value).append("<br>");
                    }
                    sb.append("<b>Observaciones:</b><br>");
                    sb.append(inc.notes);

                    for (int i = 0; i < mails.size(); i++) {
                        MssMinuteMail mail = mails.get(i);
                        SendMail.sendMail(conn, mail.email, "Novedad de minuta", sb.toString(), sb.toString());
                    }
                }

                conn.commit();
                return Response.ok().build();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
  
    @GET
    @Path("/appDto")
    public Response getAppDto(@QueryParam("postId") int postId, @QueryParam("minuteTypeId") int minuteTypeId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(MssAppRowDto.getByPost(postId, minuteTypeId, conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
}
