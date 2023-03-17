package api.sys.api;

import api.BaseAPI;
import api.sys.dto.MinVersionRequest;
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
import api.sys.model.SystemApp;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@Path("/systemApp")
public class SystemAppApi extends BaseAPI {

    @POST
    public Response insert(SystemApp obj) {
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
    public Response update(SystemApp obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            SystemApp old = new SystemApp().select(obj.id, conn);
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
            SystemApp obj = new SystemApp().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            SystemApp.delete(id, conn);
            SysCrudLog.deleted(this, SystemApp.class, id, conn);
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
            return createResponse(SystemApp.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/verifyMinVersion")
    public Response verifyMinVersion(MinVersionRequest obj) {
        try (Connection conn = MySQLCommon.getConnection(obj.poolName, obj.tz)) {
            SystemApp.verifyMinVersion(obj.appId, obj.packageName, obj.version, conn);
            return Response.ok().build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
