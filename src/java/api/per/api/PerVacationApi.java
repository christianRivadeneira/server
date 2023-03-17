package api.per.api;

import api.BaseAPI;
import api.per.dto.VacationRequest;
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
import api.per.model.PerVacation;
import api.per.rpt.PersonalReport;
import utilities.mysqlReport.MySQLReport;

@Path("/perVacation")
public class PerVacationApi extends BaseAPI {

    @POST
    public Response insert(PerVacation obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(PerVacation obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            PerVacation old = new PerVacation().select(obj.id, conn);
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            PerVacation obj = new PerVacation().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            PerVacation.delete(id, conn);
            SysCrudLog.deleted(this, PerVacation.class, id, conn);
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
            return createResponse(PerVacation.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/repVacations")
    public Response getRepVacations(VacationRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = PersonalReport.getRepVacations(req.begDt, req.endDt,  req.invertNames, req.authOffices, conn);
            return createResponse(rep.write(conn), "excel.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
