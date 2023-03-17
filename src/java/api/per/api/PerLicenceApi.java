package api.per.api;

import api.per.api.perExtraFlow.LicenseEventsGate;
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
import api.per.model.PerLicence;
import api.per.model.PerLog;

@Path("/perLicence")
public class PerLicenceApi extends BaseAPI {

    @POST
    public Response insert(PerLicence obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            conn.setAutoCommit(false);
            obj.id = obj.insert(conn);
            LicenseEventsGate.updateEventsGate(conn, obj, null, true, null);
            String logs = obj.getLogs(null, obj, conn);
            PerLog.createLog(obj.id, PerLog.PER_LICENCE, logs, sl.employeeId, conn);
            conn.setAutoCommit(true);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(PerLicence obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            PerLicence orig = new PerLicence().select(obj.id, conn);
            conn.setAutoCommit(false);
            obj.update(conn);
            LicenseEventsGate.updateEventsGate(conn, obj, null, true, null);
            String logs = obj.getLogs(orig, obj, conn);
            PerLog.createLog(obj.id, PerLog.PER_LICENCE, logs, sl.employeeId, conn);
            conn.setAutoCommit(true);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            PerLicence obj = new PerLicence().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            PerLicence.delete(id, conn);
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
            return createResponse(PerLicence.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
