package api.ord.api;

import api.BaseAPI;
import api.ord.dto.PqrReqReport;
import api.ord.model.OrdPqrRequest;
import api.ord.rpt.OrdPqrsReports;
import api.sys.model.SysCrudLog;
import java.io.File;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.Reports;
import utilities.mysqlReport.MySQLReport;

@Path("/ordPqrRequest")
public class OrdPqrRequestApi extends BaseAPI {

    @POST
    public Response insert(OrdPqrRequest obj) {
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
    public Response update(OrdPqrRequest obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            OrdPqrRequest old = new OrdPqrRequest().select(obj.id, conn);
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
            OrdPqrRequest obj = new OrdPqrRequest().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            OrdPqrRequest.delete(id, conn);
            SysCrudLog.deleted(this, OrdPqrRequest.class, id, conn);
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
            return createResponse(OrdPqrRequest.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    //    REPORTES-----------------------------------------------------------------------
//     --------------------------------------------------------------------------------
    @POST
    @Path("/criticRequest")
    public Response criticRequest(PqrReqReport obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MySQLReport rep = OrdPqrsReports.criticRequest(obj.year, obj.month, obj.state, conn);
            File file = Reports.createReportFile("critic_fac", "xls");
            useDefault(conn);
            return createResponse(rep.write(conn), "critic_fac.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
