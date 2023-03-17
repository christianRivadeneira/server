package api.bill.api;

import api.BaseAPI;
import api.bill.model.BillSchedServiceFail;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/billSchedServiceFail")
public class BillSchedServiceFailApi extends BaseAPI {

    @POST
    public Response insert(BillSchedServiceFail obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            obj.insert(conn);
            useDefault(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(BillSchedServiceFail obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillSchedServiceFail old = new BillSchedServiceFail().select(obj.id, conn);
            obj.update(conn);
            useDefault(conn);
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
            useBillInstance(conn);
            BillSchedServiceFail obj = new BillSchedServiceFail().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillSchedServiceFail.delete(id, conn);
            useDefault(conn);
            SysCrudLog.deleted(this, BillSchedServiceFail.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
