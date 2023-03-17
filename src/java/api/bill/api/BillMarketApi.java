package api.bill.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.bill.model.BillMarket;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import java.util.GregorianCalendar;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;

@Path("/billMarket")
public class BillMarketApi extends BaseAPI {

    @POST
    public Response insert(BillMarket obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(obj.baseMonth);
            gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
            obj.baseMonth = gc.getTime();
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(BillMarket obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(obj.baseMonth);
            gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
            obj.baseMonth = gc.getTime();
            BillMarket old = new BillMarket().select(obj.id, conn);
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
            BillMarket obj = new BillMarket().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillMarket.delete(id, conn);
            SysCrudLog.deleted(this, BillMarket.class, id, conn);
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
            return createResponse(BillMarket.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @GET
    @Path("/table")
    public Response table() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult tbl = new GridResult();
            tbl.data = new MySQLQuery("SELECT m.id, m.code, m.name FROM bill_market m").getRecords(conn);
            tbl.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 10, "Código SUI"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 15, "Nombre")
            };
            return createResponse(tbl);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
