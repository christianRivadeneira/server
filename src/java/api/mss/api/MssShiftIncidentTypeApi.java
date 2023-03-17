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
import api.mss.model.MssShiftIncidentType;

@Path("/mssShiftIncidentType")
public class MssShiftIncidentTypeApi extends BaseAPI {

    @POST
    public Response insert(MssShiftIncidentType obj) {
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
    public Response update(MssShiftIncidentType obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssShiftIncidentType old = new MssShiftIncidentType().select(obj.id, conn);
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
            MssShiftIncidentType obj = new MssShiftIncidentType().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssShiftIncidentType.delete(id, conn);
            SysCrudLog.deleted(this, MssShiftIncidentType.class, id, conn);
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
            return createResponse(MssShiftIncidentType.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @GET
    @Path("/allActive")
    public Response getAllActive() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(MssShiftIncidentType.getAllActive(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
