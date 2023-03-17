package api.mto.api;

import api.BaseAPI;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import api.mto.model.MtoCfg;

@Path("/mtoCfg")
public class MtoCfgApi extends BaseAPI {

    @PUT
    public Response update(MtoCfg obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MtoCfg old = new MtoCfg().select(obj.id, conn);
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
            MtoCfg obj = new MtoCfg().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }


    @GET
    @Path("/all")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(MtoCfg.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
