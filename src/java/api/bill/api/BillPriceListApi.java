package api.bill.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import api.bill.model.BillPriceList;
import utilities.MySQLQuery;

@Path("/billPriceList")
public class BillPriceListApi extends BaseAPI {

    @POST
    public Response insert(BillPriceList obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            obj.insert(conn);
            if (obj.defaultOpt) {
                new MySQLQuery("UPDATE bill_price_list SET `default_opt` = (id = " + obj.id + ")").executeUpdate(conn);
            }
            useDefault(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(BillPriceList obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillPriceList old = new BillPriceList().select(obj.id, conn);

            if (!obj.active) {
                if (new MySQLQuery("SELECT " + obj.id + " IN (" + BillPriceList.getUsedLists(conn, null) + ")").getAsBoolean(conn)) {
                    throw new Exception("No se puede desactivar una lista de precios en uso.");
                }
                if (obj.defaultOpt) {
                    obj.defaultOpt = false;
                }
            }

            obj.update(conn);
            if (obj.defaultOpt) {
                new MySQLQuery("UPDATE bill_price_list SET `default_opt` = (id = " + obj.id + ")").executeUpdate(conn);
            }
            useDefault(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillPriceList obj = new BillPriceList().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillPriceList.delete(id, conn);
            SysCrudLog.deleted(this, BillPriceList.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/grid")
    public Response getGrid(@QueryParam("active") boolean active) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            GridResult gr = new GridResult();
            gr.data = new MySQLQuery("SELECT id, `name`, `default_opt` FROM bill_price_list WHERE active = " + (active ? "1" : "0")).getRecords(conn);
            gr.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 60, "Nombre"),
                new MySQLCol(MySQLCol.TYPE_BOOLEAN, 15, "Por Defecto")
            };
            return createResponse(gr);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
