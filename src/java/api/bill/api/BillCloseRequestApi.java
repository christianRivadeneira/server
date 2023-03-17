package api.bill.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import api.bill.model.BillCloseRequest;
import java.io.File;
import metadata.model.GridRequest;
import utilities.MySQLQuery;
import web.fileManager;

@Path("/billCloseRequest")
public class BillCloseRequestApi extends BaseAPI {

    @POST
    public Response insert(BillCloseRequest obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(BillCloseRequest obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            BillCloseRequest old = new BillCloseRequest().select(obj.id, conn);
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            BillCloseRequest obj = new BillCloseRequest().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            BillCloseRequest.delete(id, conn);
            SysCrudLog.deleted(this, BillCloseRequest.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getAll")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(BillCloseRequest.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/backupsGrid")
    public Response getBackupsGrid(GridRequest req) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            GridResult gr = new GridResult();
            gr.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_A, 50, "Fecha"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 50, "Instancia"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 75, "Empleado")
            };
            gr.sortColIndex = 0;
            gr.sortType = GridResult.SORT_DESC;
            gr.data = new MySQLQuery("SELECT b.id, r.beg_dt, i.name, concat(e.first_name, ' ', e.last_name) FROM  "
                    + "bill_close_request r "
                    + "INNER JOIN bfile b ON r.id = b.owner_id AND b.owner_type = 140 "
                    + "INNER JOIN bill_instance i ON i.id = r.inst_id "
                    + "INNER JOIN employee e ON r.emp_id = e.id "
                    + "WHERE r.`status` = 'success' AND year(r.beg_dt) = ?1").setParam(1, req.ints.get(0)).getRecords(conn);
            return createResponse(gr);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/backup")
    public Response getBackup(@QueryParam("bFileId") int bFileId) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            File f = pi.getExistingFile(bFileId);
            return createResponse(f, "bk.sql");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
