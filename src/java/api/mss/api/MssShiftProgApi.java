package api.mss.api;

import api.BaseAPI;
import api.mss.model.MssShiftProg;
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

@Path("/mssShiftProg")
public class MssShiftProgApi extends BaseAPI {

    @POST
    public Response insert(MssShiftProg obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            if (obj.beg.compareTo(obj.end) > 0 && !obj.endNextDay) {
                throw new Exception("El fin debe ser posterior al inicio");
            } else if (obj.beg.compareTo(obj.end) < 0 && obj.endNextDay) {
                throw new Exception("El inicio debe ser posterior al fin");
            }
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(MssShiftProg obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            if (obj.beg.compareTo(obj.end) > 0 && !obj.endNextDay) {
                throw new Exception("El fin debe ser posterior al inicio");
            } else if (obj.beg.compareTo(obj.end) < 0 && obj.endNextDay) {
                throw new Exception("El inicio debe ser posterior al fin");
            }
            MssShiftProg old = new MssShiftProg().select(obj.id, conn);
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
            MssShiftProg obj = new MssShiftProg().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssShiftProg.delete(id, conn);
            SysCrudLog.deleted(this, MssShiftProg.class, id, conn);
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
            return createResponse(MssShiftProg.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
