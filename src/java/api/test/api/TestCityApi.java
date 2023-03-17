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
import api.test.model.TestCity;
import utilities.MySQLQuery;

@Path("/testCity")
public class TestCityApi extends BaseAPI {

    @POST
    public Response insert(TestCity obj) {
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
    public Response update(TestCity obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            TestCity old = new TestCity().select(obj.id, conn);
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
            TestCity obj = new TestCity().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            TestCity.delete(id, conn);
            SysCrudLog.deleted(this, TestCity.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getAll")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            //SessionLogin sl = getSession(conn);
            return createResponse(TestCity.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @GET
    @Path("/getByState")
    public Response getByState(@QueryParam("stateId") int stateId) {
        try (Connection conn = getConnection()) {
            return createResponse(TestCity.getList(new MySQLQuery("SELECT " + TestCity.getSelFlds("s") + " FROM test_city s WHERE s.state_id = " + stateId), conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
