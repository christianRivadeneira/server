package api.hlp.api;

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
import api.hlp.model.HlpSpanRequest;
import api.hlp.model.OpenRequestSpan;
import java.util.List;
import javax.ws.rs.PathParam;
import utilities.MySQLQuery;

@Path("/hlpSpanRequest")
public class HlpSpanRequestApi extends BaseAPI {

    @POST
    public Response insert(HlpSpanRequest obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(HlpSpanRequest obj) {
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
            HlpSpanRequest obj = new HlpSpanRequest().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            HlpSpanRequest.delete(id, conn);
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
            return createResponse(HlpSpanRequest.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/perEmployee/{perEmployeeId}")
    public Response getEmployeeOpenedSpans(@PathParam("perEmployeeId") int perEmployeeId){
        try(Connection con = getConnection()) {
            String query = "SELECT " +
                "	s.id, " +
                "	r.subject, " +
                "	s.reg_date " +
                "FROM hlp_span_request s " +
                "INNER JOIN hlp_request r ON s.case_id = r.id " +
                "WHERE s.incharge_id = ?1 " +
                "AND s.end_date IS NULL " +
                "ORDER BY s.reg_date DESC ";

            MySQLQuery q = new MySQLQuery(query).setParam(1, perEmployeeId);

            List<OpenRequestSpan> list = OpenRequestSpan.getList(q.getRecords(con));
            return createResponse(list);
        } catch (Exception e) {
            e.printStackTrace();
            return createResponse(e);
        }
    }
}
