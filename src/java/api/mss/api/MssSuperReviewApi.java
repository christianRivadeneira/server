package api.mss.api;

import api.BaseAPI;
import static api.mss.model.MssGuard.getSuperIdFromEmployee;
import api.mss.model.MssSuperReview;
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
import utilities.MySQLQuery;
import utilities.ServerNow;

@Path("/mssSuperReview")
public class MssSuperReviewApi extends BaseAPI {

    @POST
    public Response insert(MssSuperReview obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            obj.superId = getSuperIdFromEmployee(sl.employeeId, conn);
            obj.regDate = new ServerNow();
            obj.feedback = MySQLQuery.isEmpty(obj.feedback) ? null : obj.feedback;
            obj.notes = MySQLQuery.isEmpty(obj.notes) ? null : obj.notes;
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(MssSuperReview obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssSuperReview old = new MssSuperReview().select(obj.id, conn);
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
            MssSuperReview obj = new MssSuperReview().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssSuperReview.delete(id, conn);
            SysCrudLog.deleted(this, MssSuperReview.class, id, conn);
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
            return createResponse(MssSuperReview.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
