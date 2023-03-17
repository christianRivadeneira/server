package api.bill.api;

import api.BaseAPI;
import api.Params;
import api.bill.model.BillMeasure;
import api.bill.model.BillOdorant;
import api.bill.model.BillSpan;
import api.ord.model.OrdPqrRequest;
import api.sys.model.SysCrudLog;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import utilities.ServerNow;

@Path("/billMeasure")
public class BillMeasureApi extends BaseAPI {

    @PUT
    public Response update(BillMeasure obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillMeasure old = new BillMeasure().select(obj.id, conn);
            obj.update(conn);
            useDefault(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("byClientSpan")
    public Response getByClientSpan(@QueryParam("clientId") int clientId, @QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillMeasure obj = new BillMeasure().select(new Params("clientId", clientId).param("spanId", spanId), conn);
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
            BillMeasure obj = new BillMeasure().select(id, conn);
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
            BillMeasure.delete(id, conn);
            useDefault(conn);
            SysCrudLog.deleted(this, BillMeasure.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/current")
    public Response getCurrent() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            return createResponse(BillMeasure.getBySpan(BillSpan.getByState("cons", conn).id, conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/set")
    public Response set(List<BillMeasure> lst) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        try (Connection billConn = getConnection(); Connection sigmaConn = getConnection()) {
            useBillInstance(billConn);
            SessionLogin s = getSession(sigmaConn);
            Map<Integer, BillOdorant> ods = BillOdorant.getAsMap(sigmaConn);

            for (int i = 0; i < lst.size(); i++) {
                BillMeasure m = lst.get(i);
                BillMeasure old = new BillMeasure().select(m.id, billConn);

                m.odorantOk = true;
                m.pressureOk = true;

                String notes = "";
                if (m.pressure == null) {
                    m.pressureOk = false;
                    notes += "No se registró presión. ";
                } else {
                    if (m.pressure.compareTo(new BigDecimal(23)) < 0) {
                        m.pressureOk = false;
                        notes += "La presión registrada (" + df.format(m.pressure) + ") es inferior a 23 mbar. ";
                    }
                    if (m.pressure.compareTo(new BigDecimal(35)) > 0) {
                        m.pressureOk = false;
                        notes += "La presión registrada (" + df.format(m.pressure) + ") es superior a 35 mbar. ";
                    }
                }

                BillOdorant o = ods.get(m.odorantId);
                if (m.odorantAmount == null) {
                    notes += "No se registró el nivel de " + o.name;
                    m.odorantOk = false;
                } else {
                    if (m.odorantAmount.compareTo(o.min) < 0) {
                        notes += "El nivel de " + o.name + " registrado (" + df.format(m.odorantAmount) + ") es inferior a " + df.format(o.min) + " mg/m³. ";
                        m.odorantOk = false;
                    }

                    if (m.odorantAmount.compareTo(o.max) > 0) {
                        notes += "El nivel de " + o.name + " registrado (" + df.format(m.odorantAmount) + ") es superior a " + df.format(o.min) + " mg/m³. ";
                        m.odorantOk = false;
                    }
                }

                m.update(billConn);
                SysCrudLog.updated(this, m, old, sigmaConn);

                OrdPqrRequest r = OrdPqrRequest.getByMeasure(m.id, getBillInstId(), sigmaConn);
                if (!m.odorantOk || !m.pressureOk) {
                    if (r == null) {
                        r = new OrdPqrRequest();
                        r.billMeasureId = m.id;
                        r.clientTankId = new MySQLQuery("SELECT c.id "
                                + " FROM sigma.ord_pqr_client_tank c "
                                + " WHERE c.mirror_id = ?1 AND c.bill_instance_id = ?2")
                                .setParam(1, m.clientId).setParam(2, getBillInstId()).getAsInteger(sigmaConn);
                        r.createdId = s.employeeId;
                        r.creationDate = new ServerNow();
                        r.instanceId = getBillInstId();
                        r.spanId = m.spanId;
                        r.notes = notes;
                        r.billReqType = "measure";
                        r.insert(sigmaConn);
                    }
                } else {
                    if (r != null) {
                        OrdPqrRequest.delete(r.id, sigmaConn);
                    }
                }
            }
            return createResponse(BillMeasure.getBySpan(BillSpan.getByState("cons", billConn).id, billConn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
