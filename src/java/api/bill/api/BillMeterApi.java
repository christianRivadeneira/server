package api.bill.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.bill.model.BillClientTank;
import api.bill.model.BillInstance;
import api.bill.model.BillMeter;
import api.bill.model.BillSpan;
import api.sys.model.SysCrudLog;
import java.math.BigDecimal;
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

@Path("/billMeter")
public class BillMeterApi extends BaseAPI {

    public static BillMeter insert(BillMeter obj, Connection conn, BaseAPI caller) throws Exception {
        caller.useBillInstance(conn);
        BillInstance inst = caller.getBillInstance();
        BillSpan cons = BillSpan.getByClient("cons", obj.clientId, inst, conn);
        if (cons.id != obj.startSpanId) {
            throw new Exception("No se puede crear en el periodo");
        }

        if (new MySQLQuery("SELECT COUNT(*) > 0 FROM "
                + "(SELECT "
                + "(SELECT `number` FROM bill_meter WHERE client_id = c.id ORDER BY start_span_id DESC LIMIT 1) AS m "
                + "FROM bill_client_tank c WHERE c.active) as l  WHERE l.m = ?1")
                .setParam(1, obj.number)
                .getAsBoolean(conn)) {
            throw new Exception("El medidor ya existe.");
        }

        obj.insert(conn);
        BillClientTank.updateCache(obj.clientId, conn);
        caller.useDefault(conn);
        SysCrudLog.created(caller, obj, conn);
        return obj;
    }

    @POST
    public Response insert(BillMeter obj) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            getSession(conn);
            try {
                obj = insert(obj, conn, this);
                conn.commit();
                return createResponse(obj);
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public boolean changed(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return false;
        } else if (a != null && b != null) {
            return a.compareTo(b) != 0;
        } else {
            return true;
        }
    }

    @PUT
    public Response update(BillMeter obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillMeter old = new BillMeter().select(obj.id, conn);
            BillSpan cons = BillSpan.getByClient("cons", obj.clientId, getBillInstance(), conn);
            if (cons.id != obj.startSpanId) {
                if (changed(old.startReading, obj.startReading)) {
                    throw new Exception("No se puede cambiar la lectura inicial, ya se causó.");
                }

                if (changed(old.factor, obj.factor)) {
                    throw new Exception("No se puede cambiar el factor, ya se causó.");
                }
            }

            if (new MySQLQuery("SELECT COUNT(*) > 0 FROM "
                    + "(SELECT "
                    + "(SELECT `number` FROM bill_meter WHERE id <> ?2 AND client_id = c.id ORDER BY start_span_id DESC LIMIT 1) AS m "
                    + "FROM bill_client_tank c WHERE c.active) as l  WHERE l.m = ?1")
                    .setParam(1, obj.number).setParam(2, obj.id)
                    .getAsBoolean(conn)) {
                throw new Exception("El medidor ya existe.");
            }

            obj.update(conn);
            BillClientTank.updateCache(obj.clientId, conn);
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
            BillMeter obj = new BillMeter().select(id, conn);
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
            BillMeter obj = new BillMeter().select(id, conn);
            BillSpan span = BillSpan.getByClient("cons", obj.clientId, getBillInstance(), conn);
            if (span.id != obj.startSpanId) {
                throw new Exception("Ya se causó.");
            }

            //20/10/2020 se quita esta validación por petición de mg
            /*if (new MySQLQuery("SELECT COUNT(*) = 1 FROM bill_meter WHERE client_id = ?1").setParam(1, obj.clientId).getAsBoolean(conn)) {
                throw new Exception("El cliente debe tener un medidor");
            }*/

            BillClientTank.updateCache(obj.clientId, conn);
            BillMeter.delete(obj.id, conn);
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
            gr.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 80, "Desde"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 80, "Medidor"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 40, "Factor")
            };

            List<BillMeter> facs = BillMeter.getList(new MySQLQuery("SELECT " + BillMeter.getSelFlds("m") + " FROM bill_meter m WHERE m.client_id = ?1").setParam(1, clientId), conn);

            Object[][] data = new Object[facs.size()][4];
            for (int i = 0; i < facs.size(); i++) {
                BillMeter f = facs.get(i);
                BillSpan span = new BillSpan().select(f.startSpanId, conn);
                data[i][0] = f.id;
                data[i][1] = span.getConsLabel();
                data[i][2] = f.number;
                data[i][3] = f.factor;
            }
            gr.data = data;
            return createResponse(gr);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/current")
    public Response getCurrent(@QueryParam("clientId") int clientId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillMeter m = new BillMeter().select(new MySQLQuery("SELECT " + BillMeter.getSelFlds("m") + " FROM bill_meter m WHERE m.client_id = ?1 ORDER BY m.start_span_id DESC LIMIT 1").setParam(1, clientId), conn);
            return createResponse(m);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
