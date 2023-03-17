package api.jss.api;

import api.BaseAPI;
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
import api.jss.model.JssAgentZone;
import api.jss.model.JssSpan;
import api.mss.model.MssGuard;
import java.util.Calendar;

@Path("/jssAgentZone")
public class JssAgentZoneApi extends BaseAPI {

    @POST
    public Response insert(JssAgentZone obj) {
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
    public Response update(JssAgentZone obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            JssAgentZone old = new JssAgentZone().select(obj.id, conn);
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/updateZone")
    public Response updateZone(@QueryParam("zoneId") int zoneId) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            JssSpan span = JssSpan.selectNow(conn);
            if (span == null) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, 1);
                span = new JssSpan();
                span.begDt = cal.getTime();
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                span.endDt = cal.getTime();
                span.insert(conn);
            }

            MssGuard agent = MssGuard.getAgent(sl.employeeId, conn);
            JssAgentZone obj = JssAgentZone.getByAgentId(conn, agent.id, span.id);
            JssAgentZone old = JssAgentZone.getByAgentId(conn, agent.id, span.id);            

            if (obj == null) {
                obj = new JssAgentZone();
                obj.agentId = agent.id;
                obj.spanId = span.id;
                obj.zoneId = zoneId;
                obj.insert(conn);
                SysCrudLog.created(this, obj, conn);
            } else if (obj.zoneId != zoneId) {
                obj.zoneId = zoneId;
                obj.update(conn);
                SysCrudLog.updated(this, obj, old, conn);
            }

            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            JssAgentZone obj = new JssAgentZone().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            JssAgentZone.delete(id, conn);
            SysCrudLog.deleted(this, JssAgentZone.class, id, conn);
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
            return createResponse(JssAgentZone.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }


    /*@GET
    @Path("/grid")
    public Response getGrid() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult tbl = new GridResult();
            tbl.data = new MySQLQuery("").getRecords(conn);
            tbl.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 180, "Cupón"),
            };
            tbl.sortColIndex = 4;
            tbl.sortType = GridResult.SORT_ASC;
            return createResponse(tbl);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }*/
}
