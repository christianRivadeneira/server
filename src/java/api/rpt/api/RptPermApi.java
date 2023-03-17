package api.rpt.api;

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
import api.rpt.model.RptPerm;

@Path("/rptPerm")
public class RptPermApi extends BaseAPI {

    @POST
    public Response insert(RptPerm obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
        
    @POST
    @Path("/insertFromEmployee")
    public Response insertFromEmployee(RptPerm obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);            
            obj.type = "edit";
            obj.active = true;            
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(RptPerm obj) {
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
            RptPerm obj = new RptPerm().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            RptPerm.delete(id, conn);
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
            return createResponse(RptPerm.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getByRpt")
    public Response getByRpt(@QueryParam("dashId") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(RptPerm.getByRpt(id, sl.employeeId, conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getByDash")
    public Response getByDash(@QueryParam("dashId") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            RptPerm p = RptPerm.getByDash(id, sl.employeeId, conn);
            return createResponse(p);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
