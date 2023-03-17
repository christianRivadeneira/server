package api.sys.api;

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
import api.sys.model.SysColorCfg;
import utilities.MySQLQuery;

@Path("/sysColorCfg")
public class SysColorCfgApi extends BaseAPI {

    @POST
    public Response insert(SysColorCfg obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(SysColorCfg obj) {
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
            SysColorCfg obj = new SysColorCfg().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getByElement")
    public Response getByElement(@QueryParam("element") String element) {
        try (Connection conn = getConnection()) {
            MySQLQuery q = new MySQLQuery("SELECT " + SysColorCfg.getSelFlds("") + " "
                    + "FROM sys_color_cfg WHERE "
                    + "element = '" + element + "'");
            SysColorCfg obj = new SysColorCfg().select(q, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            SysColorCfg.delete(id, conn);
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
            return createResponse(SysColorCfg.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
