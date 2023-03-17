package api.mto.api;

import api.BaseAPI;
import api.mto.model.MtoRoute;
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
import utilities.apiClient.BooleanResponse;

@Path("/mtoRoute")
public class MtoRouteApi extends BaseAPI {

    @POST
    public Response insert(MtoRoute obj) {
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
    public Response update(MtoRoute obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MtoRoute old = new MtoRoute().select(obj.id, conn);
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
            MtoRoute obj = new MtoRoute().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MtoRoute.delete(id, conn);
            SysCrudLog.deleted(this, MtoRoute.class, id, conn);
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
            return createResponse(MtoRoute.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/hasPoints")
    public Response hasPoints(@QueryParam("id") int routeId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(new BooleanResponse(MtoRoute.hasPoints(routeId, conn)));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
