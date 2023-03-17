package api.bill.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.bill.model.BillBuildFactor;
import api.bill.model.BillSpan;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;

@Path("/billBuildFactor")  
public class BillBuildFactorApi extends BaseAPI {

    @POST
    public Response insert(BillBuildFactor obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillSpan cons = BillSpan.getByBuilding("cons", obj.buildId, getBillInstance(), conn);
            if (cons.id != obj.billSpanId) {
                throw new Exception("No se puede crear en el periodo");
            }
            obj.insert(conn);
            useDefault(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(BillBuildFactor obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillBuildFactor old = new BillBuildFactor().select(obj.id, conn);
            BillSpan span = BillSpan.getByBuilding("cons", obj.buildId, getBillInstance(), conn);
            if (span.id != obj.billSpanId) {
                throw new Exception("Ya se causó.");
            }
            obj.update(conn);
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
            BillBuildFactor obj = new BillBuildFactor().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillBuildFactor obj = new BillBuildFactor().select(id, conn);
            BillSpan span = BillSpan.getByBuilding("cons", obj.buildId, getBillInstance(), conn);
            if (span.id != obj.billSpanId) {
                throw new Exception("Ya se causó.");
            }
            BillBuildFactor.delete(obj.id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/grid")
    public Response getGrid(@QueryParam("buildId") int buildId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);

            GridResult gr = new GridResult();
            gr.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 80, "Desde"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 40, "Factor")
            };

            List<BillBuildFactor> facs = BillBuildFactor.getList(new MySQLQuery("SELECT " + BillBuildFactor.getSelFlds("f") + " FROM bill_build_factor f WHERE f.build_id = ?1").setParam(1, buildId), conn);

            Object[][] data = new Object[facs.size()][3];
            for (int i = 0; i < facs.size(); i++) {
                BillBuildFactor f = facs.get(i);
                BillSpan span = new BillSpan().select(f.billSpanId, conn);
                data[i][0] = f.id;
                data[i][1] = span.getConsLabel();
                data[i][2] = f.factor;
            }
            gr.data = data;
            return createResponse(gr);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
