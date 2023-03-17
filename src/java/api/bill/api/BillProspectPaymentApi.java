package api.bill.api;

import api.BaseAPI;
import api.bill.model.BillProspect;
import api.bill.model.BillProspectPayment;
import api.bill.model.BillProspectService;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/billProspectPayment")
public class BillProspectPaymentApi extends BaseAPI {

    @POST
    public Response insert(BillProspectPayment obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            if (new BillProspect().select(new BillProspectService().select(obj.serviceId, conn).prospectId, conn).converted) {
                throw new Exception("El prospecto ya fue convertido.");
            }
            obj.insert(conn);
            useDefault(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(BillProspectPayment obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            if (new BillProspect().select(new BillProspectService().select(obj.serviceId, conn).prospectId, conn).converted) {
                throw new Exception("El prospecto ya fue convertido.");
            }
            BillProspectPayment old = new BillProspectPayment().select(obj.id, conn);
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
            BillProspectPayment obj = new BillProspectPayment().select(id, conn);
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
            BillProspectPayment obj = new BillProspectPayment().select(id, conn);
            if (new BillProspect().select(new BillProspectService().select(obj.serviceId, conn).prospectId, conn).converted) {
                throw new Exception("El prospecto ya fue convertido.");
            }
            BillProspectPayment.delete(id, conn);
            useDefault(conn);
            SysCrudLog.deleted(this, BillProspectPayment.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    /*@GET
    @Path("/grid")
    public Response getGrid() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult tbl = new GridResult();
            tbl.data = new MySQLQuery("").getRecords(conn);
            tbl.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 180, "Cupón"),
            };
            tbl.sortColIndex = 4;
            tbl.sortType = GridResult.SORT_ASC;
            return createResponse(tbl);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }*/
}
