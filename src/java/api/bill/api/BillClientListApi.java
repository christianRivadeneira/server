package api.bill.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import api.bill.model.BillClientList;
import api.bill.model.BillClientTank;
import api.bill.model.BillPriceList;
import api.bill.model.BillSpan;
import utilities.MySQLQuery;

@Path("/billClientList")
public class BillClientListApi extends BaseAPI {

    @POST
    public Response insert(BillClientList obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            obj.insert(conn);
            BillSpan cons = BillSpan.getByClient("cons", obj.clientId, getBillInstance(), conn);            
            if (cons.id != obj.spanId) {
                throw new Exception("No se puede crear en el periodo indicado");
            }

            BillPriceList lst = new BillPriceList().select(obj.listId, conn);
            BillClientTank c = new BillClientTank().select(obj.clientId, conn);
            useDefault(conn);
            SysCrudLog.created(this, c, "Se adicionó la lista de precios " + lst.name + " desde el periodo " + cons.getConsLabel(), conn);
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
            BillClientList obj = new BillClientList().select(id, conn);

            BillSpan cons = BillSpan.getByClient("cons", obj.clientId, getBillInstance(), conn);
            if (obj.spanId != cons.id) {
                throw new Exception("Ya fue causado");
            }

            BillPriceList lst = new BillPriceList().select(obj.listId, conn);
            BillClientTank c = new BillClientTank().select(obj.clientId, conn);
            new MySQLQuery("DELETE FROM bill_client_list WHERE id = " + id).executeDelete(conn);
            useDefault(conn);
            SysCrudLog.created(this, c, "Se removió la lista de precios " + lst.name + " desde el periodo " + cons.getConsLabel(), conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/grid")
    public Response getGrid(@QueryParam("clientId") int clientId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            GridResult gr = new GridResult();

            gr.data = new MySQLQuery("SELECT cl.id, s.id, DATE_FORMAT(s.cons_month, 'Consumos de %M/%Y'), l.name  "
                    + "FROM bill_client_list cl "
                    + "INNER JOIN bill_span s ON s.id = cl.span_id "
                    + "INNER JOIN bill_price_list l ON l.id = cl.list_id "
                    + "WHERE cl.client_id = ?1").setParam(1, clientId).getRecords(conn);
            gr.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 10, "Periodo"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 10, "Lista")
            };
            return createResponse(gr);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
