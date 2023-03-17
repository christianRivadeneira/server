package api.bill.api;

import api.BaseAPI;
import api.bill.model.BillInstance;
import api.bill.model.BillPriceIndex;
import api.bill.model.BillSpan;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import java.util.GregorianCalendar;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;

@Path("/billPriceIndex")
public class BillPriceIndexApi extends BaseAPI {

    @POST
    public Response insert(BillPriceIndex obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(obj.month);
            gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
            obj.month = gc.getTime();
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(BillPriceIndex obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(obj.month);
            gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
            obj.month = gc.getTime();
            verifyUse(obj, conn);
            useDefault(conn);
            BillPriceIndex old = new BillPriceIndex().select(obj.id, conn);
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
            BillPriceIndex obj = new BillPriceIndex().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillPriceIndex obj = new BillPriceIndex().select(id, conn);
            verifyUse(obj, conn);
            BillPriceIndex.delete(id, conn);
            SysCrudLog.deleted(this, BillPriceIndex.class, id, conn);
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
            return createResponse(BillPriceIndex.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private void verifyUse(BillPriceIndex obj, Connection conn) throws Exception {
        List<BillInstance> allBis = BillInstance.getAllNet(conn);
        for (int i = 0; i < allBis.size(); i++) {
            BillInstance bi = allBis.get(i);

            Boolean isBase = new MySQLQuery("SELECT COUNT(*)>0 "
                    + "FROM bill_market m "
                    + "INNER JOIN bill_instance i ON i.market_id = m.id "
                    + "WHERE m.base_month = DATE(?1) "
            ).setParam(1, obj.month).getAsBoolean(conn);

            if (isBase) {
                throw new Exception("No se puede modificar el indice de precios base");
            }
            
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(obj.month);
            gc.add(GregorianCalendar.MONTH, 1);            
            BillSpan usedSpan = BillSpan.getByMonth(bi.db, gc.getTime(), conn);
            if (usedSpan != null) {
                if (BillSpan.isSpanCaused(bi.db, usedSpan.id, conn)) {
                    throw new Exception("No se puede modificar, ya se causó un periodo con este índice\n"
                            + "Instancia: " + bi.name);
                }
            }
        }
    }
}
