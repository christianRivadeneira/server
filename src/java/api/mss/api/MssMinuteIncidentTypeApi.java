package api.mss.api;

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
import api.mss.model.MssMinuteIncidentType;
import java.util.List;

@Path("/mssMinuteIncidentType")
public class MssMinuteIncidentTypeApi extends BaseAPI {

    @POST
    public Response insert(MssMinuteIncidentType obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(MssMinuteIncidentType obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssMinuteIncidentType old = new MssMinuteIncidentType().select(obj.id, conn);
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
            MssMinuteIncidentType obj = new MssMinuteIncidentType().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssMinuteIncidentType.delete(id, conn);
            SysCrudLog.deleted(this, MssMinuteIncidentType.class, id, conn);
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
            return createResponse(MssMinuteIncidentType.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @GET
    @Path("/allActive")
    public Response getAllActive() {
        try (Connection conn = getConnection()) {
            getSession(conn);            
            List<MssMinuteIncidentType> listInfo = new MssMinuteIncidentType().getAllActive(conn);            
            return createResponse(listInfo);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
