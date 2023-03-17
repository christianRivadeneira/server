package api.bill.api;

import api.BaseAPI;
import api.bill.model.BillAnticNote;
import api.bill.model.BillAnticNoteRequest;
import api.bill.model.BillAnticNoteType;
import api.bill.model.BillInstance;
import api.bill.model.BillMarket;
import api.bill.model.BillPriceIndex;
import api.bill.model.BillServiceFail;
import api.bill.model.BillSpan;
import api.bill.model.BillTransaction;
import api.sys.model.SysCrudLog;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DecimalFormat;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;

@Path("/billServiceFail")
public class BillServiceFailApi extends BaseAPI {

    private double getHrs(BillServiceFail obj) {
        return (double) ((obj.endDt.getTime() - obj.begDt.getTime()) / 3600000d);
    }

    private BigDecimal getAvgCons(int clientId, Connection billConn) throws Exception {
        return new MySQLQuery("SELECT SUM(cons)/8760.0 FROM "
                + "(SELECT c.m3_subs + c.m3_no_subs as cons "
                + "FROM bill_clie_cau c WHERE c.client_id = ?1 "
                + "ORDER BY c.span_id DESC LIMIT 12) AS l1;").setParam(1, clientId).getAsBigDecimal(billConn, true);        
    }

    private BigDecimal getCregCost(BillInstance inst, BillSpan cons, Connection conn) throws Exception {
        BillSpan reca = new BillSpan().select(cons.id - 1, conn);
        inst.useDefault(conn);
        BillMarket market = new BillMarket().select(inst.marketId, conn);
        BigDecimal baseIpp = BillPriceIndex.getByMonth(market.failCostBaseMonth, conn).ipp;
        BigDecimal curIpp = BillPriceIndex.getByMonth(reca.consMonth, conn).ipp;
        inst.useInstance(conn);
        return new BigDecimal(market.failCost.doubleValue() * (curIpp.doubleValue() / baseIpp.doubleValue()));
    }

    private BigDecimal getCost(BigDecimal avgCons, BigDecimal cregCost, BillServiceFail obj) throws Exception {
        return avgCons.multiply(cregCost).multiply(new BigDecimal(getHrs(obj)));
    }

    @POST
    public Response insert(BillServiceFail obj) {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                SessionLogin sl = getSession(conn);

                if (obj.begDt.getTime() > obj.endDt.getTime()) {
                    throw new Exception("El fin debe ser menor que el inicio");
                }

                BillAnticNoteType nType = BillAnticNoteType.getSrvFailype(conn);
                if (nType == null) {
                    throw new Exception("El tipo de nota para interrupciones de servicio no está definido.");
                }

                useBillInstance(conn);
                BillInstance inst = getBillInstance();
                BillSpan cons = BillSpan.getByClient("cons", obj.clientId, inst, conn);

                obj.spanId = cons.id;

                DecimalFormat df = new DecimalFormat("#,##0.00");

                obj.avgCons = getAvgCons(obj.clientId, conn);
                obj.cregCost = getCregCost(inst, cons, conn);
                obj.cost = getCost(obj.avgCons, obj.cregCost, obj);
                obj.causalType = obj.causalType;
                obj.insert(conn);

                BillAnticNoteRequest r = new BillAnticNoteRequest();
                r.value = obj.cost;
                r.note = new BillAnticNote();
                r.note.billSpanId = cons.id;
                r.note.clientTankId = obj.clientId;
                r.note.srvFailId = obj.id;
                r.note.typeId = nType.id;
                r.note.descNotes = nType.name;
                r.note.label = nType.name + " " + df.format(getHrs(obj)) + " hrs";
                BillAnticNote.createNote(r, sl, inst, conn);
                useDefault(conn);
                SysCrudLog.created(this, obj, conn);
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

    @PUT
    public Response update(BillServiceFail obj) {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                getSession(conn);

                if (obj.begDt.getTime() > obj.endDt.getTime()) {
                    throw new Exception("El fin debe ser menor que el inicio");
                }

                useBillInstance(conn);
                BillInstance inst = getBillInstance();
                BillSpan cons = BillSpan.getByClient("cons", obj.clientId, inst, conn);

                if (obj.spanId != cons.id) {
                    throw new Exception("La nota ya se debitó");
                }

                BillServiceFail old = new BillServiceFail().select(obj.id, conn);
                obj.avgCons = getAvgCons(obj.clientId, conn);
                obj.cregCost = getCregCost(inst, cons, conn);
                obj.cost = getCost(obj.avgCons, obj.cregCost, obj);

                obj.update(conn);
                useDefault(conn);
                BillAnticNoteType nType = BillAnticNoteType.getSrvFailype(conn);
                SysCrudLog.updated(this, obj, old, conn);

                useBillInstance(conn);
                DecimalFormat df = new DecimalFormat("#,##0.00");
                BillAnticNote n = BillAnticNote.getBySrvFail(obj.id, conn);
                BillTransaction t = BillTransaction.getByDoc(n.id, "pag_antic", conn).get(0);
                t.value = obj.cost;

                n.label = nType.name + " " + df.format(getHrs(obj)) + " hrs";
                useDefault(conn);
                conn.commit();
                conn.commit();
                return createResponse(obj);
            } catch (Exception ex) {
                conn.rollback();
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillServiceFail obj = new BillServiceFail().select(id, conn);
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
            BillServiceFail.delete(id, conn);
            useDefault(conn);
            SysCrudLog.deleted(this, BillServiceFail.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
