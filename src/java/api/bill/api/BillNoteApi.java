package api.bill.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.bill.model.BillBuilding;
import api.bill.model.BillClientTank;
import api.bill.model.BillInstance;
import api.bill.model.BillNote;
import api.bill.model.BillNoteRequest;
import api.bill.model.BillSpan;
import api.bill.model.BillTransaction;
import api.bill.writers.note.NoteWriter;
import api.sys.model.City;
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
import model.billing.BillBank;
import model.system.SessionLogin;
import utilities.MySQLQuery;

@Path("/billNote")
public class BillNoteApi extends BaseAPI {

    @POST
    public Response insert(BillNoteRequest req) {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                SessionLogin sess = getSession(conn);
                BillNote obj = BillNoteRequest.createNote(req, sess, this, conn);
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
    public Response update(BillNote obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillNote orig = new BillNote().select(obj.id, conn);
            BillNote copy = orig.duplicate();
            copy.descNotes = obj.descNotes;
            copy.update(conn);
            useDefault(conn);
            SysCrudLog.updated(this, copy, orig, conn);
            return Response.ok(copy).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            return createResponse(new BillNote().select(id, conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection(); Connection gral = getConnection()) {
            try {
                SessionLogin sl = getSession(conn);
                conn.setAutoCommit(false);
                useBillInstance(conn);
                BillNote note = new BillNote().select(id, conn);
                BillNote.cancel(this, note, false, conn);
                conn.commit();
                return createResponse();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/grid")
    public Response getGrid(@QueryParam("clientId") Integer clientId, @QueryParam("type") String type) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            GridResult tbl = new GridResult();

            if (type.equals("rebill")) {
                tbl.cols = new MySQLCol[]{
                    new MySQLCol(MySQLCol.TYPE_KEY),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 150, "Periodo Refacturación"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 150, "Periodo Refacturado"),
                    new MySQLCol(MySQLCol.TYPE_BOOLEAN, 30, "Activa")
                };
                tbl.sortColIndex = 0;
                tbl.sortType = GridResult.SORT_DESC;
                tbl.data = new MySQLQuery("SELECT n.id, DATE_FORMAT(reb.cons_month, 'Consumos de %M/%Y'), DATE_FORMAT(cons.cons_month, 'Consumos de %M/%Y'), n.active "
                        + "FROM "
                        + "bill_clie_rebill n "
                        + "INNER JOIN bill_span cons ON cons.id = n.error_span_id "
                        + "INNER JOIN bill_span reb ON reb.id = n.rebill_span_id "
                        + "WHERE n.client_id = ?1").setParam(1, clientId).getRecords(conn);
            } else {
                tbl.cols = new MySQLCol[]{
                    new MySQLCol(MySQLCol.TYPE_KEY),
                    new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 90, "Creación"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 150, "Periodo"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 150, "Usuario"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 200, "Descripción")
                };
                tbl.sortColIndex = 0;
                tbl.sortType = GridResult.SORT_DESC;
                tbl.data = new MySQLQuery("SELECT n.id, n.when_notes, DATE_FORMAT(s.cons_month, 'Consumos de %M/%Y'), (SELECT CONCAT(e.first_name, ' ', e.last_name) FROM bill_transaction t "
                        + "INNER JOIN sigma.employee e ON t.cre_usu_id = e.id "
                        + "WHERE t.doc_id = n.id AND t.doc_type = 'not' LIMIT 1), n.desc_notes  FROM "
                        + "bill_note n "
                        + "INNER JOIN bill_span s ON s.id = n.bill_span_id "
                        + "WHERE n.client_tank_id = ?1 AND n.type_notes = ?2;").setParam(1, clientId).setParam(2, type).getRecords(conn);
            }
            return createResponse(tbl);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/print")
    public Response print(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance inst = getBillInstance();
            NoteWriter w = new NoteWriter(inst, conn);
            City city = new City().select(inst.cityId, conn);

            useBillInstance(conn);
            BillNote note = new BillNote().select(id, conn);
            BillClientTank client = new BillClientTank().select(note.clientTankId, conn);

            BillBuilding build = null;
            if (inst.isTankInstance()) {
                build = new BillBuilding().select(client.buildingId, conn);
            }
            BillSpan span = new BillSpan().select(note.billSpanId, conn);
            BillBank bank = null;
            if (note.bank != null) {
                bank = new BillBank().select(note.bank, conn);
            }
            List<BillTransaction> lst = BillTransaction.getByDoc(note.id, "not", conn);

            w.beginDocument();
            w.addNote(note, client, build, span, bank, city, lst);
            return createResponse(w.endDocument(), "nota.pdf");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
