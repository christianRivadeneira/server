package api.ess.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.ess.model.EssPqr;
import api.sys.model.SysCrudLog;
import java.io.File;
import java.sql.Connection;
import java.util.Date;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import metadata.model.GridRequest;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import utilities.Reports;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.Table;

@Path("/essPqr")
public class EssPqrApi extends BaseAPI {

    @POST
    public Response insert(EssPqr obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.regByEmpId = sl.employeeId;
            obj.begDate = new Date();
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(EssPqr obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            EssPqr old = new EssPqr().select(obj.id, conn);
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/close")
    public Response close(EssPqr obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            EssPqr old = new EssPqr().select(obj.id, conn);
            obj.endDate = new Date();
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
            EssPqr obj = new EssPqr().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            EssPqr.delete(id, conn);
            SysCrudLog.deleted(this, EssPqr.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private GridResult getPqrsOpen(Connection conn) throws Exception {
        GridResult r = new GridResult();
        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_KEY),
            new MySQLCol(MySQLCol.TYPE_KEY),
            new MySQLCol(MySQLCol.TYPE_INTEGER, 20, "Serial"),
            new MySQLCol(MySQLCol.TYPE_ENUM, 50, "Origen", new String[][]{new String[]{"build", "Edificio"}, new String[]{"unit", "Apartamento"},}),
            new MySQLCol(MySQLCol.TYPE_TEXT, 50, "Tipo"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 50, "Inicio"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 75, "Solicitó"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 75, "De"),
            new MySQLCol(MySQLCol.TYPE_INTEGER, 75, "Dias Restantes")
        };

        r.cols[2].toString = true;

        r.data = new MySQLQuery("SELECT "
                + "pqr.id, "
                + "pqr.type, "
                + "pqr.id, "
                + "pqr.type, "
                + "t.name, "
                + "pqr.beg_date, "
                + "CONCAT(per.first_name, ' ', per.last_name), "
                + "IFNULL(b.name, CONCAT(b1.name, ' Torre ', u.tower, ' Apto ', u.code)),"
                + "t.days - datediff(NOW(), pqr.beg_date)"
                + "FROM "
                + "ess_pqr pqr "
                + "INNER JOIN ess_person per ON per.id = pqr.req_by_id "
                + "INNER JOIN ess_pqr_type t ON t.id = pqr.type_id "
                + "LEFT JOIN ess_building b ON b.id = pqr.build_id "
                + "LEFT JOIN ess_unit u ON u.id = pqr.unit_id "
                + "LEFT JOIN ess_building b1 ON b1.id = u.build_id "
                + "WHERE "
                + "pqr.end_date IS NULL").getRecords(conn);

        r.sortType = GridResult.SORT_DESC;
        r.sortColIndex = 3;
        return r;
    }

    @POST
    @Path("/open")
    public Response gridOpen(GridRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(getPqrsOpen(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Produces(value = "application/vnd.ms-excel")
    @Path("/open")
    public Response exportOpen(GridRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult r = getPqrsOpen(conn);
            MySQLReport rep = new MySQLReport("Pqrs Abiertas", null, "hoja1", now(conn));
            rep.setVerticalFreeze(5);
            rep.setZoomFactor(80);
            rep.setMultiRowTitles(true);
            rep.getTables().add(new Table("Pqrs Abiertas"));
            rep = MySQLReport.getReport(rep, r.cols, r.data);
            return createResponse(rep.write(conn), "PqrsAbiertas.xls");

        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private GridResult getPqrsClose(Connection conn, GridRequest req) throws Exception {
        GridResult r = new GridResult();
        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_KEY),
            new MySQLCol(MySQLCol.TYPE_KEY),
            new MySQLCol(MySQLCol.TYPE_INTEGER, 20, "Serial"),
            new MySQLCol(MySQLCol.TYPE_ENUM, 50, "Origen", new String[][]{new String[]{"build", "Edificio"}, new String[]{"unit", "Apartamento"},}),
            new MySQLCol(MySQLCol.TYPE_TEXT, 50, "Tipo"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 50, "Inicio"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 50, "Cierre"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 75, "Solicitó"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 75, "De"),};

        r.cols[2].toString = true;

        MySQLQuery q = new MySQLQuery("SELECT "
                + "pqr.id, "
                + "pqr.type, "
                + "pqr.id, "
                + "pqr.type, "
                + "t.name, "
                + "pqr.beg_date, "
                + "pqr.end_date, "
                + "CONCAT(per.first_name, ' ', per.last_name), "
                + "IFNULL(b.name, CONCAT(b1.name, ' Torre ', u.tower, ' Apto ', u.code)),"
                + "t.days - datediff(NOW(), pqr.beg_date)"
                + "FROM "
                + "ess_pqr pqr "
                + "INNER JOIN ess_person per ON per.id = pqr.req_by_id "
                + "INNER JOIN ess_pqr_type t ON t.id = pqr.type_id "
                + "LEFT JOIN ess_building b ON b.id = pqr.build_id "
                + "LEFT JOIN ess_unit u ON u.id = pqr.unit_id "
                + "LEFT JOIN ess_building b1 ON b1.id = u.build_id "
                + "WHERE "
                + "pqr.end_date IS NOT NULL AND YEAR(pqr.end_date) = YEAR(?1) AND MONTH(pqr.end_date) = MONTH(?1)").setParam(1, req.dates.get(0));
        System.out.println(q.getParametrizedQuery());
        r.data = q.getRecords(conn);

        r.sortType = GridResult.SORT_DESC;
        r.sortColIndex = 3;
        return r;

    }

    @POST
    @Path("/closed")
    public Response closed(GridRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(getPqrsClose(conn, req));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Produces(value = "application/vnd.ms-excel")
    @Path("/closed")
    public Response exportClosed(GridRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult r = getPqrsClose(conn, req);
            MySQLReport rep = new MySQLReport("Pqrs Cerradas", null, "hoja1", now(conn));
            rep.setVerticalFreeze(5);
            rep.setZoomFactor(80);
            rep.setMultiRowTitles(true);
            rep.getTables().add(new Table("Pqrs Cerradas"));
            rep = MySQLReport.getReport(rep, r.cols, r.data);
            return createResponse(rep.write(conn), "PqrsCerradas.xls");

        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
