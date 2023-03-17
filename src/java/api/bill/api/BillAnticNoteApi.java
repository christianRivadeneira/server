package api.bill.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.bill.model.BillAnticNote;
import api.bill.model.BillAnticNoteRequest;
import api.bill.model.BillBuilding;
import api.bill.model.BillClientTank;
import api.bill.model.BillInstance;
import api.bill.model.BillSpan;
import api.bill.model.BillTransaction;
import api.bill.writers.antic.AnticWriter;
import api.sys.model.City;
import api.sys.model.Employee;
import api.sys.model.SysCrudLog;
import java.io.File;
import java.sql.Connection;
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

@Path("/billAnticNote")
public class BillAnticNoteApi extends BaseAPI {

    @POST
    public Response insert(BillAnticNoteRequest req) {
        try (Connection billConn = getConnection()) {
            SessionLogin sess = getSession(billConn);
            try {
                billConn.setAutoCommit(false);
                useBillInstance(billConn);
                BillAnticNote n = BillAnticNote.createNote(req, sess, getBillInstance(), billConn);
                billConn.commit();
                return createResponse(n);
            } catch (Exception ex) {
                billConn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(BillAnticNote obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillAnticNote old = new BillAnticNote().select(obj.id, conn);
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
            BillAnticNote obj = new BillAnticNote().select(id, conn);
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
            try {
                conn.setAutoCommit(false);
                BillAnticNote n = new BillAnticNote().select(id, conn);
                BillAnticNote.cancel(this, n, false, conn);
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

    @POST
    @Path("/print")
    public Response print(@QueryParam("id") int id) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance inst = getBillInstance();
            useBillInstance(conn);
            BillAnticNote note = new BillAnticNote().select(id, conn);
            BillClientTank client = new BillClientTank().select(note.clientTankId, conn);
            BillBuilding build = client.buildingId != null ? new BillBuilding().select(client.buildingId, conn) : null;
            BillSpan span = new BillSpan().select(note.billSpanId, conn);
            List<BillTransaction> deta = BillTransaction.getByDoc(note.id, "pag_antic", conn);
            useDefault(conn);
            Employee e = new Employee().select(note.creUsuId, conn);
            City city = new City().select(inst.cityId, conn);
            AnticWriter w = new AnticWriter(inst);
            w.drawNote(note, client, build, span, e, city, deta, conn);
            File f = w.endDocument();
            return createResponse(f, "nota.pdf");
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
            GridResult tbl = new GridResult();
            tbl.sortColIndex = 1;
            tbl.sortType = GridResult.SORT_DESC;
            tbl.data = new MySQLQuery("SELECT "
                    + "n.id, "//0
                    + "DATE_FORMAT(`cons_month`, 'Consumo de %M %Y'),"//1
                    + "n.when_notes, "//2
                    + "n.serial, "//3
                    + "n.label, "//4
                    + "t.`value`, "//5
                    + "n.active "//6
                    + "FROM "
                    + "bill_antic_note AS n "
                    + "LEFT JOIN bill_transaction AS t ON n.id = t.doc_id AND t.doc_type = 'pag_antic' "
                    + "INNER JOIN bill_span AS s ON s.id = n.bill_span_id "
                    + "WHERE "
                    + "n.client_tank_id = " + clientId + " ").getRecords(conn);;
            tbl.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 150, "Periodo"),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 75, "Creación"),
                new MySQLCol(MySQLCol.TYPE_INTEGER, 100, "Número"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 150, "Etiqueta"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 90, "Valor"),
                new MySQLCol(MySQLCol.TYPE_BOOLEAN, 50, "Activa")
            };
            return createResponse(tbl);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
