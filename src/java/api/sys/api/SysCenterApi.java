package api.sys.api;

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
import api.sys.model.SysCenter;
import api.sys.model.SysCenterType;
import utilities.MySQLQuery;

@Path("/sysCenter")
public class SysCenterApi extends BaseAPI {

    @POST
    public Response insert(SysCenter obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            if (obj.typeId == 0) {
                Integer types = new MySQLQuery("SELECT COUNT(*) FROM sys_center_type").getAsInteger(conn);
                if (types == 0) {
                    SysCenterType ct = new SysCenterType();
                    ct.initials = "O";
                    ct.name = "Otro";
                    ct.insert(conn);
                } else {
                    Integer typeId = new MySQLQuery("SELECT id FROM sys_center_type LIMIT 1").getAsInteger(conn);
                    obj.typeId = typeId;
                }
            }
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(SysCenter obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            SysCenter old = new SysCenter().select(obj.id, conn);
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
            SysCenter obj = new SysCenter().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            SysCenter.delete(id, conn);
            SysCrudLog.deleted(this, SysCenter.class, id, conn);
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
            return createResponse(SysCenter.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
