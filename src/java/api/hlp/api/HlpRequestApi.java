package api.hlp.api;

import api.BaseAPI;
import api.hlp.model.ClosedSpansRequest;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import api.hlp.model.HlpRequest;
import api.hlp.model.HlpSpanRequest;
import java.util.Date;
import java.util.List;
import javax.ws.rs.PathParam;
import utilities.MySQLQuery;
import web.ShortException;

@Path("/hlpRequest")
public class HlpRequestApi extends BaseAPI {

    @POST
    public Response insert(HlpRequest obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(HlpRequest obj) {
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
            HlpRequest obj = new HlpRequest().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            HlpRequest.delete(id, conn);
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
            return createResponse(HlpRequest.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/perEmployee/{perEmployeeId}")
    public Response getEmployeeClosedSpansRequests(@PathParam("perEmployeeId") int perEmployeeId){
        try(Connection con = getConnection()) {
            String query = "SELECT " +
                "	r.id, " +
                "	r.subject, " +
                "   r.reg_date " +
                "FROM hlp_request r " +
                "INNER JOIN hlp_span_request s ON s.case_id = r.id " +
                "WHERE s.incharge_id = ?1 " +
                "AND s.end_date IS NOT NULL " +
                "GROUP BY r.id " +
                "ORDER BY MAX(s.end_date) DESC LIMIT 6 ";

            MySQLQuery q = new MySQLQuery(query).setParam(1, perEmployeeId);

            List<ClosedSpansRequest> list = ClosedSpansRequest.getList(q.getRecords(con));

            return createResponse(list);
        } catch (Exception e) {
            e.printStackTrace();
            return createResponse(e);
        }
    }

    @PUT
    @Path("/closeSpan/{spanId}")
    public Response getCloseSpanRequest(@PathParam("spanId") int spanId){
        try(Connection con = getConnection()) {
            HlpSpanRequest spanRequest = new HlpSpanRequest().select(spanId, con);
            spanRequest.endDate = new Date();
            spanRequest.update(con);

            String query = "SELECT " +
                HlpRequest.getSelFlds("hr") +
                "FROM hlp_request hr " +
                "WHERE hr.id = ( " +
                "	SELECT hsr.case_id " +
                "	FROM hlp_span_request hsr " +
                "	WHERE hsr.id = ?1 " +
                ") ";

            MySQLQuery q = new MySQLQuery(query).setParam(1, spanId);
            HlpRequest request = new HlpRequest().select(q, con);
            request.running = false;
            request.update(con);

            return Response.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return createResponse(e);
        }
    }

    @PUT
    @Path("/{requestId}/openSpan/")
    public Response openSpanRequest(@PathParam("requestId") int requestId){
        try(Connection con = getConnection()) {
            String requestQuery = "SELECT " +
                HlpRequest.getSelFlds("hr") +
                "FROM hlp_request hr " +
                "WHERE hr.id = ?1 ";

            MySQLQuery q = new MySQLQuery(requestQuery).setParam(1, requestId);
            HlpRequest request = new HlpRequest().select(q, con);
            
            if(request.running){
                throw new ShortException("La solicitud ya se encuentra en ejecución");
            }

            request.running = true;
            request.update(con);

            HlpSpanRequest span = new HlpSpanRequest();
            span.caseId = request.id;
            span.inchargeId = request.inCharge;
            span.regDate = new Date();
            span.insert(con);

            return Response.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return createResponse(e);
        }
    }

}
