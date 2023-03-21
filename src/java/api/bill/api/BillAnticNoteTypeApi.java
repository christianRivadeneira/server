package api.bill.api;

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
import api.bill.model.BillAnticNoteType;

@Path("/billAnticNoteType")
public class BillAnticNoteTypeApi extends BaseAPI {

    @POST
    public Response insert(BillAnticNoteType obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);            
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(BillAnticNoteType obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillAnticNoteType old = new BillAnticNoteType().select(obj.id, conn);
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
            getSession(conn);
            BillAnticNoteType obj = new BillAnticNoteType().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            BillAnticNoteType.delete(id, conn);
            SysCrudLog.deleted(this, BillAnticNoteType.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getAll")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(BillAnticNoteType.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}