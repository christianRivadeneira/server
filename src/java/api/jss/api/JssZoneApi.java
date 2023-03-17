package api.jss.api;

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
import api.jss.model.JssZone;
import utilities.MySQLQuery;

@Path("/jssZone")
public class JssZoneApi extends BaseAPI {

    @POST
    public Response insert(JssZone obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(JssZone obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            JssZone old = new JssZone().select(obj.id, conn);
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            JssZone obj = new JssZone().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            System.err.println("****************************************************");

            if (new MySQLQuery("SELECT COUNT(*) > 0 FROM jss_client_zone cz WHERE cz.zone_id = ?1").setParam(1, id).getAsBoolean(conn)) {
                throw new Exception("No se puede eliminar porque tiene clientes asignados");
            }
            SessionLogin sl = getSession(conn);
            JssZone.delete(id, conn);
            SysCrudLog.deleted(this, JssZone.class, id, conn);
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
            return createResponse(JssZone.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/allZone")
    public Response getAllZone() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(JssZone.getAll(conn, sl.employeeId));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
