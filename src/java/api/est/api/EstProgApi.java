package api.est.api;

import api.BaseAPI;
import api.est.dto.ValidateScheduleDTO;
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
import api.est.model.EstProg;

@Path("/estProg")
public class EstProgApi extends BaseAPI {

    @POST
    public Response insert(EstProg obj) {
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
    public Response update(EstProg obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            EstProg old = new EstProg().select(obj.id, conn);
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
            EstProg obj = new EstProg().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            EstProg.delete(id, conn);
            SysCrudLog.deleted(this, EstProg.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getAll")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(EstProg.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/validateSchedule")
    public Response validateSchedule(ValidateScheduleDTO dto) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            EstProg.validateSchedule(dto.clientId, dto.week, dto.day, dto.date, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
