package api.test.api;

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
import api.test.model.TestState;
import utilities.MySQLQuery;

@Path("/testState")
public class TestStateApi extends BaseAPI {

    @POST
    public Response insert(TestState obj) {
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
    public Response update(TestState obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            TestState old = new TestState().select(obj.id, conn);
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
            TestState obj = new TestState().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            TestState.delete(id, conn);
            SysCrudLog.deleted(this, TestState.class, id, conn);
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
            return createResponse(TestState.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @GET
    @Path("/getByCountry")
    public Response getByCountry(@QueryParam("countryId") int countryId) {
        try (Connection conn = getConnection()) {
            return createResponse(TestState.getList(new MySQLQuery("SELECT " + TestState.getSelFlds("s") + " FROM test_state s WHERE s.country_id = " + countryId), conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
