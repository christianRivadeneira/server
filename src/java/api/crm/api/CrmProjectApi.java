package api.crm.api;

import api.BaseAPI;
import api.crm.model.CrmProject;
import api.sys.model.SimpleComboData;
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
import utilities.MySQLQuery;

@Path("/crmProject")
public class CrmProjectApi extends BaseAPI {

    @POST
    public Response insert(CrmProject obj) {
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
    public Response update(CrmProject obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            CrmProject old = new CrmProject().select(obj.id, conn);
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
            CrmProject obj = new CrmProject().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            CrmProject.delete(id, conn);
            SysCrudLog.deleted(this, CrmProject.class, id, conn);
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
            return createResponse(CrmProject.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/cmbRecent")
    public Response getcmbRecent() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(new SimpleComboData(new MySQLQuery("SELECT p.id, CONCAT(c.name, ' - ', p.name) FROM crm_project p "
                    + "INNER JOIN crm_client c ON p.client_id = c.id "
                    + "left JOIN hlp_request r ON r.project_id = p.id "
                    + "left JOIN hlp_span_request s ON s.case_id = r.id "
                    + "WHERE c.active AND  p.state = 'active' AND c.`type` = 'client' "
                    + "GROUP BY p.id "
                    + "ORDER BY MAX(IFNULL(s.reg_date, '1970-01-01')) DESC ").getRecords(conn)));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
