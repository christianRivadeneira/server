package api.per.api;

import api.BaseAPI;
import api.per.dto.ExtrasRequest;
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
import api.per.model.PerSurcharge;
import api.per.rpt.PersonalReport;
import utilities.mysqlReport.MySQLReport;

@Path("/perSurcharge")
public class PerSurchargeApi extends BaseAPI {

    @POST
    public Response insert(PerSurcharge obj) {
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
    public Response update(PerSurcharge obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            PerSurcharge old = new PerSurcharge().select(obj.id, conn);
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
            PerSurcharge obj = new PerSurcharge().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            PerSurcharge.delete(id, conn);
            SysCrudLog.deleted(this, PerSurcharge.class, id, conn);
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
            return createResponse(PerSurcharge.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/detailsSurcharges")
    public Response getDetailsSurcharges(ExtrasRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = PersonalReport.getDetailsSurcharges(req.employeeId, req.payMonth, req.authOffices, req.invertNames, req.rdbDet, req.rdbTot,
                    req.part, req.sbArea, req.pos, req.cityId, req.officeId, req.nameEmployee, req.saraRoundExtrasMinutes, req.manualOnly, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
