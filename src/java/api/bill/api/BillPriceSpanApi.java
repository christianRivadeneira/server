package api.bill.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.bill.model.BillPriceList;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import api.bill.model.BillPriceSpan;
import api.bill.model.BillSpan;
import java.text.DecimalFormat;
import utilities.MySQLQuery;

@Path("/billPriceSpan")
public class BillPriceSpanApi extends BaseAPI {

    @POST
    public Response insert(BillPriceSpan obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            obj.spanId = BillSpan.getByState("cons", conn).id;
            if (!BillSpan.isPricesListOpen(new BillSpan().select(obj.spanId, conn), getBillInstance(), conn)) {
                throw new Exception("El periodo ya se ha causado");
            }
            obj.insert(conn);
            BillSpan span = new BillSpan().select(obj.spanId, conn);
            BillPriceList lst = new BillPriceList().select(obj.lstId, conn);
            useDefault(conn);
            SysCrudLog.created(this, span, "Se definió el precio para la lista " + lst.name, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(BillPriceSpan obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillSpan s = new BillSpan().select(obj.spanId, conn);
            if (!BillSpan.isPricesListOpen(s, getBillInstance(), conn)) {
                throw new Exception("El periodo ya se ha causado");
            }
            BillPriceSpan old = new BillPriceSpan().select(obj.id, conn);
            obj.update(conn);
            BillSpan span = new BillSpan().select(obj.spanId, conn);
            BillPriceList lst = new BillPriceList().select(obj.lstId, conn);
            useDefault(conn);
            SysCrudLog.updated(this, span, "Cambió el precio para la lista " + lst.name + " era " + new DecimalFormat("#,###.0000").format(old.price), conn);
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
            BillPriceSpan obj = new BillPriceSpan().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

   /* @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillPriceSpan.delete(id, conn);
            SysCrudLog.deleted(this, BillPriceSpan.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }*/

    @GET
    @Path("/grid")
    public Response grid(@QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            GridResult gr = new GridResult();
            //BillSpan cons = BillSpan.getByState("cons", conn);
            String lists = BillPriceList.getUsedLists(conn, spanId);
            gr.data = new MySQLQuery("SELECT "
                    + "bps.id,"
                    + "lst.id,"
                    + "lst.`name`,"
                    + "bps.price "
                    + "FROM bill_price_list AS lst "
                    + "LEFT JOIN bill_price_span AS bps ON bps.lst_id = lst.id AND bps.span_id = " + spanId + " "
                    + "WHERE lst.id IN (" + lists + ")").getRecords(conn);
            gr.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 70, "Lista"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 30, "Precio")
            };
            return createResponse(gr);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    /*
    public Response a(@QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);

            BillPriceSpan.getPricesMap(conn, spanId);
            return createResponse(gr);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }*/

//    public static Map<Integer, BigDecimal> getPricesMap(int spanId, EndPoints ep) throws Exception {
//        Object[][] lists = new MySQLQuery("SELECT DISTINCT cl.list_id FROM "
//                + "bill_client_list cl  "
//                + "INNER JOIN  "
//                + "(SELECT "
//                + "cl.client_id, "
//                + "max(cl.span_id) as span_id "
//                + "FROM "
//                + "bill_client_list AS cl "
//                + "INNER JOIN bill_client_tank AS c ON cl.client_id = c.id "
//                + "WHERE "
//                + "c.active = 1 AND cl.span_id <= " + spanId + " "
//                + "GROUP BY "
//                + "cl.client_id) AS l "
//                + "ON cl.client_id = l.client_id AND cl.span_id = l.span_id;").getRecords(ep, ep.getPoolNameByInst());
//
//        Map<Integer, BigDecimal> prices = new HashMap<>();
//        Object[][] pricesData = new MySQLQuery("SELECT ps.lst_id, ps.price "
//                + "FROM bill_price_span AS ps "
//                + "WHERE ps.span_id = " + spanId).getRecords(ep, ep.getPoolNameByInst());
//
//        for (Object[] priceRow : pricesData) {
//            Object[] row = (Object[]) priceRow;
//            prices.put(MySQLQuery.getAsInteger(row[0]), MySQLQuery.getAsBigDecimal(row[1], true));
//        }
//        for (Object[] listRow : lists) {
//            int listId = MySQLQuery.getAsInteger(listRow[0]);
//            if (!prices.containsKey(listId)) {
//                throw new Exception("Las listas de precios no están completas.");
//            }
//        }
//        return prices;
//    }
//
//    public static Map<Integer, BigDecimal> getPricesMap(int spanId, String city, EndPoints ep) throws Exception {
//        Object[][] lists = new MySQLQuery("SELECT DISTINCT cl.list_id FROM "
//                + "bill_client_list cl  "
//                + "INNER JOIN  "
//                + "(SELECT "
//                + "cl.client_id, "
//                + "max(cl.span_id) as span_id "
//                + "FROM "
//                + "bill_client_list AS cl "
//                + "INNER JOIN bill_client_tank AS c ON cl.client_id = c.id "
//                + "WHERE "
//                + "c.active = 1 AND cl.span_id <= " + spanId + " "
//                + "GROUP BY "
//                + "cl.client_id) AS l "
//                + "ON cl.client_id = l.client_id AND cl.span_id = l.span_id;").getRecords(ep, city);
//
//        Map<Integer, BigDecimal> prices = new HashMap<>();
//        Object[][] pricesData = new MySQLQuery("SELECT ps.lst_id, ps.price "
//                + "FROM bill_price_span AS ps "
//                + "WHERE ps.span_id = " + spanId).getRecords(ep, city);
//
//        for (Object[] priceRow : pricesData) {
//            Object[] row = (Object[]) priceRow;
//            prices.put(MySQLQuery.getAsInteger(row[0]), MySQLQuery.getAsBigDecimal(row[1], true));
//        }
//        for (Object[] listRow : lists) {
//            int listId = MySQLQuery.getAsInteger(listRow[0]);
//            if (!prices.containsKey(listId)) {
//                throw new Exception("Las listas de precios no están completas.");
//            }
//        }
//        return prices;
//    }
}
