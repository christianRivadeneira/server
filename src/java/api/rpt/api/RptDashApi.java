package api.rpt.api;

import api.BaseAPI;
import api.rpt.model.RptDash;
import api.rpt.model.RptPerm;
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

@Path("/rptDash")
public class RptDashApi extends BaseAPI {

    @POST
    public Response insert(RptDash obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            RptPerm perm = new RptPerm();
            perm.dashId = obj.id;
            perm.empId = sl.employeeId;
            perm.canShare = true;
            perm.type = "edit";
            perm.active = true;
            perm.insert(conn);

            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(RptDash obj) {
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
            RptDash obj = new RptDash().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            RptDash.delete(id, conn);
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
            return createResponse(RptDash.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getRecent")
    public Response getRecent(@QueryParam("limit") int limit) {
        try (Connection con = getConnection()) {
            SessionLogin sl = getSession(con);
            MySQLQuery q;
            if (sl.employeeId == 1) {
                q = new MySQLQuery("SELECT "
                        + RptDash.getSelFlds("d")
                        + "FROM "
                        + "rpt_dash d "
                        + "LEFT JOIN rpt_perm p ON d.id = p.dash_id AND p.active AND p.emp_id = ?1 "
                        + "ORDER BY COALESCE(p.last_view, '1970-01-01') DESC LIMIT " + limit).setParam(1, sl.employeeId);

            } else {
                q = new MySQLQuery("SELECT "
                        + RptDash.getSelFlds("d")
                        + "FROM "
                        + "rpt_dash d "
                        + "INNER JOIN rpt_perm p ON d.id = p.dash_id AND p.active AND p.emp_id = ?1 "
                        + "ORDER BY p.last_view DESC LIMIT " + limit).setParam(1, sl.employeeId);

            }
            return Response.ok(RptDash.getList(q, con)).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
