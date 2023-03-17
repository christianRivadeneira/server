package api.jss.api;

import api.BaseAPI;
import api.MultiPartRequest;
import api.jss.dto.VisitDto;
import api.jss.model.JssAlarmClient;
import api.jss.model.JssSpan;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import api.jss.model.JssVisit;
import api.mss.model.MssGuard;
import static api.mss.model.MssGuard.getFromEmployee;
import java.util.Calendar;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import utilities.MySQLQuery;
import web.fileManager;

@Path("/jssVisit")
public class JssVisitApi extends BaseAPI {

    @POST
    public Response insert(JssVisit obj) {
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
    public Response update(JssVisit obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            JssVisit old = new JssVisit().select(obj.id, conn);
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
            JssVisit obj = new JssVisit().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            JssVisit.delete(id, conn);
            SysCrudLog.deleted(this, JssVisit.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/all")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(JssVisit.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/visitByFilter")
    public Response getShiftsByFilter(
            @QueryParam("clientId") Integer clientId,
            @QueryParam("day") Integer day,
            @QueryParam("month") Integer month,
            @QueryParam("week") Integer week,
            @QueryParam("year") Integer year) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            Integer guardId = getFromEmployee(sl.employeeId, MssGuard.TYPE_AGENT, conn);
            if (guardId == null) {
                throw new Exception("No se encuentra registrado como agente");
            }

            String query = "SELECT j.client_id, j.agent_id, j.span_id, j.beg_dt, j.end_dt, j.notes, j.id FROM jss_visit j WHERE j.client_id = ?1 AND j.agent_id = ?2 ";

            MySQLQuery mq = null;

            if (day == null && month == null && year == null && week == null) {
                mq = new MySQLQuery(query
                        + " AND (DATE(j.beg_dt) = CURDATE() "
                        + " OR DATE(j.beg_dt) = CURDATE()) ");
            } else if (day != null && month != null && year != null) {
                mq = new MySQLQuery(query
                        + " AND MONTH(j.beg_dt) = ?3 "
                        + " AND DAY(j.beg_dt) = ?4 "
                        + " AND YEAR(j.beg_dt) = ?5 "
                        + " ORDER BY j.beg_dt ASC");
                mq.setParam(3, month).setParam(4, day).setParam(5, year);
            } else if (week != null) {
                mq = new MySQLQuery(query
                        + " AND WEEK(j.beg_dt)= ?3 "
                        + " ORDER BY j.beg_dt ASC"
                );
                mq.setParam(3, week);
            } else if (month != null) {
                mq = new MySQLQuery(query
                        + " AND MONTH(j.beg_dt)= ?3 "
                        + " ORDER BY j.beg_dt ASC"
                );
                mq.setParam(3, month);
            }

            mq.setParam(1, clientId).setParam(2, guardId);
            List<JssVisit> list = JssVisit.getList(mq, conn);

            return createResponse(list);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/registerVisit")
    public Response registerVisit(VisitDto visitDto) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            JssAlarmClient client = new JssAlarmClient().select(visitDto.clientId, conn);

            if (!client.code.equals(visitDto.code)) {
                throw new Exception("El código no coincide con el cliente");
            }

            JssVisit obj = new JssVisit();

            MssGuard agent = MssGuard.getAgent(sl.employeeId, conn);
            JssSpan jssSpan = JssSpan.selectNow(conn);

            if (jssSpan == null) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, 1);
                jssSpan = new JssSpan();
                jssSpan.begDt = cal.getTime();
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                jssSpan.endDt = cal.getTime();
                jssSpan.insert(conn);
            }

            obj.agentId = agent.id;
            obj.begDt = now(conn);
            obj.clientId = visitDto.clientId;
            obj.spanId = jssSpan.id;

            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @PUT
    @Path("/updateEnd")
    public Response updateEnd(VisitDto visitDto) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            JssVisit obj = new JssVisit().select(visitDto.id, conn);
            obj.endDt = now(conn);

            JssAlarmClient client = new JssAlarmClient().select(obj.clientId, conn);

            if (!client.code.equals(visitDto.code)) {
                throw new Exception("El código no coincide con el cliente");
            }
            JssVisit old = new JssVisit().select(obj.id, conn);
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @PUT
    @Path("/updateNotes")
    public Response updateNotes(VisitDto visitDto) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            JssVisit obj = new JssVisit().select(visitDto.id, conn);
            obj.notes = visitDto.notes;
            JssVisit old = new JssVisit().select(obj.id, conn);
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @GET
    @Path("/pqrsVisits")
    public Response getPqrsVisits() {
        try (Connection conn = getConnection()) {
            return createResponse(JssVisit.getPqrsVisits(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/savePhoto")
    public Response savePhoto(@Context HttpServletRequest request) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);
            try {
                int id = MySQLQuery.getAsInteger(mr.params.get("id"));
                
                if (mr.getFile() != null && mr.getFile().file != null) {
                    fileManager.upload(
                            sl.employeeId,
                            id, //ownerId
                            null,//ownerType, 
                            "jss_visit", //tableName
                            "visita.jpg", //fileName, 
                            "Foto de punto", //desc, 
                            true, //unique
                            true,//override
                            null,//shrinkType
                            pi, mr.getFile().file, conn
                    );
                }
                return Response.ok().build();
            } finally {
                mr.deleteFiles();
            }            
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }


}
