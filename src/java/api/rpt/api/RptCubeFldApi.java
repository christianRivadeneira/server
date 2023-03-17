package api.rpt.api;

import api.BaseAPI;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import api.rpt.model.RptCubeFld;
import api.rpt.model.RptCubeTbl;
import utilities.MySQLQuery;

@Path("/rptCubeFld")
public class RptCubeFldApi extends BaseAPI {

    @POST
    public Response insert(RptCubeFld obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(RptCubeFld obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.update(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            RptCubeFld obj = new RptCubeFld().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);            
            RptCubeFld obj = new RptCubeFld().select(id, conn);
            RptCubeFld.delete(id, conn);
            MySQLQuery q = new MySQLQuery("UPDATE rpt_cube_fld SET place = place - 1 WHERE cube_id = ?1 AND place >= ?2");
            q.setParam(1, obj.cubeId);
            q.setParam(2, obj.place);
            q.executeUpdate(conn);
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
            return createResponse(RptCubeFld.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
