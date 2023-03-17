package api.bill.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.bill.model.BillClieDoc;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import metadata.model.GridRequest;
import utilities.MySQLQuery;

@Path("/billClieDoc")
public class BillClieDocApi extends BaseAPI {

    @POST
    public Response insert(BillClieDoc obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            obj.instId = getBillInstId();
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(BillClieDoc obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillClieDoc old = new BillClieDoc().select(obj.id, conn);
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillClieDoc obj = new BillClieDoc().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillClieDoc.delete(id, conn);
            SysCrudLog.deleted(this, BillClieDoc.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/grid")
    public Response getGrid(GridRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult tbl = new GridResult();
            tbl.data = new MySQLQuery("SELECT cd.id, t.name, count(bf.id) > 0 FROM "
                    + "bill_clie_doc cd "
                    + "INNER JOIN bill_doc_type t ON t.id = cd.type_id "
                    + "LEFT JOIN bfile bf ON bf.owner_id = cd.id AND bf.table = 'bill_clie_doc' "
                    + "WHERE cd.client_id = ?1 AND cd.inst_id = ?2 "
                    + "GROUP BY cd.id").setParam(1, req.ints.get(0)).setParam(2, getBillInstId()).getRecords(conn);
            tbl.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 180, "Tipo"),
                new MySQLCol(MySQLCol.TYPE_BOOLEAN, 40, "Adjunto"),
            };
            return createResponse(tbl);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
