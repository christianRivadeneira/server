package api.per.api;

import api.BaseAPI;
import api.per.dto.ExtrasRequest;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import api.per.model.PerExtra;
import api.per.rpt.PersonalReport;
import utilities.mysqlReport.MySQLReport;

@Path("/perExtra")
public class PerExtraApi extends BaseAPI {

    @POST
    public Response insert(PerExtra obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(PerExtra obj) {
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
            PerExtra obj = new PerExtra().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            PerExtra.delete(id, conn);
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
            return createResponse(PerExtra.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
        
    @POST
    @Path("/detailsExtras")
    public Response getDetailsExtras(ExtrasRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = PersonalReport.getDetailsExtras(req.employeeId, req.payMonth, req.authOffices, req.invertNames, req.rdbDet, req.rdbTot,
                    req.type, req.part, req.sbArea, req.pos, req.cityId, req.officeId, req.nameEmployee, req.saraRoundExtrasMinutes, req.manualOnly, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
