package api.bill.api;

import api.BaseAPI;
import api.bill.model.BillInstCheckType;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/billInstCheckType")
public class BillInstCheckTypeApi extends BaseAPI {

    @POST
    public Response insert(BillInstCheckType obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(BillInstCheckType obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstCheckType old = new BillInstCheckType().select(obj.id, conn);
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
            getSession(conn);
            BillInstCheckType obj = new BillInstCheckType().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstCheckType.delete(id, conn);
            SysCrudLog.deleted(this, BillInstCheckType.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}