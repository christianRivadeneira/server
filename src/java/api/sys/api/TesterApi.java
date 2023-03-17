package api.sys.api;

import api.BaseAPI;
import api.sys.model.Person;
import java.io.File;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import utilities.apiClient.BooleanResponse;
import utilities.apiClient.StringResponse;

@Path("/tester")
public class TesterApi extends BaseAPI {

    @POST
    @Path("/echoMixedList")
    public Response echoMixedList(List<Object> lst) {
        for (int i = 0; i < lst.size(); i++) {
            Object get = lst.get(i);
            if (get != null) {
                System.out.println(get + " " + get.getClass().toString());
            } else {
                System.out.println("null");
            }
        }
        return Response.ok(lst).build();
    }

    @POST
    @Path("/echoDates")
    public Response echoDates(List<Date> ds) {
        return createResponse(ds);
    }

    @POST
    @Path("/echoList")
    public Response echoList(List<Person> nums) {
        return Response.ok(nums).build();
    }

    @POST
    @Path("/echoObject")
    public Response echoObject(Person person) {
        return Response.ok(person).build();
    }

    @GET
    @Path("/isShowingQueries")
    public Response isShowingQueries() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(new BooleanResponse(MySQLQuery.PRINT_QUERIES));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/hideQueries")
    public Response hideQueries() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            if (sl.employeeId == 1) {
                MySQLQuery.PRINT_QUERIES = false;
            }
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/showQueries")
    public Response showQueries() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            if (sl.employeeId == 1) {
                MySQLQuery.PRINT_QUERIES = true;
            }
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/apiCallTest")
    public Response apiCallTest(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {

            File f = new File("D:\\2020-05-29 10_57_24-Window.png");

            return createResponse(new StringResponse(null, id + ""));
            //return createResponse(f, "img.png");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
