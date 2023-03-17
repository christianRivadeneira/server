package api.mss.api;

import api.BaseAPI;
import api.mss.model.MssMinuteType;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;

@Path("/mssMinuteType")
public class MssMinuteTypeApi extends BaseAPI {

    @POST
    public Response insert(MssMinuteType obj) {
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
    public Response update(MssMinuteType obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssMinuteType old = new MssMinuteType().select(obj.id, conn);
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
            MssMinuteType obj = new MssMinuteType().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssMinuteType.delete(id, conn);
            SysCrudLog.deleted(this, MssMinuteType.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/all")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(MssMinuteType.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/byClient")
    public Response getAllCliente(@QueryParam("clientId") int clientId) {
        try (Connection conn = getConnection()) {
            getSession(conn);            
            List<MssMinuteType> listInfo = new MssMinuteType().getByClient(clientId,conn);            
            return createResponse(listInfo);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
