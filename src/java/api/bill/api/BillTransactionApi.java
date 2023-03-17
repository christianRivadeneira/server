package api.bill.api;

import api.BaseAPI;
import api.bill.model.BillInstance;
import controller.billing.BillReportController;
import java.sql.Connection;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import utilities.mysqlReport.MySQLReport;

@Path("/billTransaction") /* Tabla de redes */
public class BillTransactionApi extends BaseAPI {

    @POST
    @Path("/rptCart")
    public Response getRptCart() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance inst = getBillInstance();
            useBillInstance(conn);
            MySQLReport r = BillReportController.getRptCart(inst, conn);
            useDefault(conn);
            return createResponse(r.write(conn), "cartera.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/rptCartAges")
    public Response getRptCartAges(@QueryParam("type") String type, @QueryParam("typeCli") String typeCli, 
            @QueryParam("minValue") boolean minValue, @QueryParam("months") boolean months) {
        try (Connection instConn = getConnection(); Connection gralConn = getConnection()) {
            getSession(instConn);
            BillInstance inst = getBillInstance();
            useBillInstance(instConn);
            MySQLReport r;
            if (inst.isNetInstance()) {
                r = BillReportController.getCartAgesNet(type, inst, instConn, gralConn);
            } else {
                r = BillReportController.getCartAgesTank(type, typeCli, minValue, months, inst, instConn, gralConn);
            }
            return createResponse(r.write(gralConn), "cartera.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
