package api.ess.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.ess.dto.UncheckPanicEvent;
import api.ess.model.EssPanicEvent;
import api.sys.model.SysCrudLog;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.Table;

@Path("/essPanicEvent")
public class EssPanicEventApi extends BaseAPI {

    @POST
    public Response insert() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            EssPanicEvent obj = new EssPanicEvent();
            obj.regDt = new Date();
            obj.empId = sl.employeeId;
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            EssPanicEvent obj = new EssPanicEvent().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(EssPanicEvent obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            EssPanicEvent old = new EssPanicEvent().select(obj.id, conn);
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/closeEvent")
    public Response closeEvent(EssPanicEvent obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            EssPanicEvent old = new EssPanicEvent().select(obj.id, conn);
            old.closeDt = new Date();
            old.closeById = sl.employeeId;
            old.notes = obj.notes;
            old.update(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/checkEvent")
    public Response checkEvent(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            EssPanicEvent obj = new EssPanicEvent().select(id, conn);
            obj.checkDt = new Date();
            obj.checkedById = sl.employeeId;
            obj.update(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            EssPanicEvent.delete(id, conn);
            SysCrudLog.deleted(this, EssPanicEvent.class, id, conn);
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
            return createResponse(EssPanicEvent.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/uncheckEvents")
    public Response uncheckEvents() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            List<UncheckPanicEvent> rta = new ArrayList<>();
            Object[][] data = new MySQLQuery("SELECT "
                    + " ev.id, "
                    + " p.id, "
                    + " u.id, "
                    + " GROUP_CONCAT(b.name SEPARATOR ' - '), "
                    + " GROUP_CONCAT(u.code SEPARATOR ' - '), "
                    + " CONCAT(p.first_name, IFNULL(CONCAT(' ',p.last_name), ' ')), "
                    + " p.phone, "
                    + " ev.reg_dt "
                    + " FROM ess_panic_event ev "
                    + " INNER JOIN ess_person p ON p.emp_id = ev.emp_id "
                    + " INNER JOIN ess_person_unit pu ON pu.person_id = p.id "
                    + " INNER JOIN ess_unit u ON u.id = pu.unit_id "
                    + " INNER JOIN ess_building b ON b.id = u.build_id "
                    + " WHERE ev.check_dt IS NULL "
                    + " GROUP BY ev.id").getRecords(conn);

            if (data != null) {
                for (Object[] obj : data) {
                    UncheckPanicEvent ev = new UncheckPanicEvent();
                    ev.eventId = MySQLQuery.getAsInteger(obj[0]);
                    ev.personId = MySQLQuery.getAsInteger(obj[1]);
                    ev.unitId = MySQLQuery.getAsInteger(obj[2]);
                    ev.buildingName = MySQLQuery.getAsString(obj[3]);
                    ev.apto = MySQLQuery.getAsString(obj[4]);
                    ev.personName = MySQLQuery.getAsString(obj[5]);
                    ev.phone = MySQLQuery.getAsString(obj[6]);
                    ev.regDate = MySQLQuery.getAsDate(obj[7]);
                    rta.add(ev);
                }
            }

            return createResponse(rta);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/openEvent")
    public Response uncheckEvents(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Object[] data = new MySQLQuery("SELECT "
                    + " ev.id, "
                    + " p.id, "
                    + " u.id, "
                    + " GROUP_CONCAT(b.name SEPARATOR ' - '), "
                    + " GROUP_CONCAT(u.code SEPARATOR ' - '), "
                    + " CONCAT(p.first_name, IFNULL(CONCAT(' ',p.last_name), ' ')), "
                    + " p.phone, "
                    + " ev.reg_dt "
                    + " FROM ess_panic_event ev "
                    + " INNER JOIN ess_person p ON p.emp_id = ev.emp_id "
                    + " INNER JOIN ess_person_unit pu ON pu.person_id = p.id "
                    + " INNER JOIN ess_unit u ON u.id = pu.unit_id "
                    + " INNER JOIN ess_building b ON b.id = u.build_id "
                    + " WHERE ev.check_dt IS NOT NULL "
                    + " AND ev.close_dt IS NULL "
                    + " AND ev.id = ?1 "
                    + " GROUP BY ev.id")
                    .setParam(1, id)
                    .getRecord(conn);

            if (data != null) {
                UncheckPanicEvent ev = new UncheckPanicEvent();
                ev.eventId = MySQLQuery.getAsInteger(data[0]);
                ev.personId = MySQLQuery.getAsInteger(data[1]);
                ev.unitId = MySQLQuery.getAsInteger(data[2]);
                ev.buildingName = MySQLQuery.getAsString(data[3]);
                ev.apto = MySQLQuery.getAsString(data[4]);
                ev.personName = MySQLQuery.getAsString(data[5]);
                ev.phone = MySQLQuery.getAsString(data[6]);
                ev.regDate = MySQLQuery.getAsDate(data[7]);
                return createResponse(ev);
            } else {
                throw new Exception("No se encontró un evento no checkeado con ese id");
            }

        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public GridResult getEventsOpen(Connection conn) throws Exception {
        GridResult r = new GridResult();
        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_KEY),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 50, "Inicio"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 75, "Solicitó"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 75, "Edificio"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 75, "Apartamento"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 75, "Teléfono"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 50, "Atendidó"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 75, "Operario"),};

        r.data = new MySQLQuery("SELECT "
                + " p.id, "
                + " p.reg_dt, CONCAT(sp.first_name,' ', IFNULL(sp.last_name,'')), "
                + " b.name,"
                + " CONCAT(u.tower, ' - ',u.code),"
                + " u.phone,"
                + " p.check_dt, CONCAT(eope.first_name,' ', IFNULL(eope.last_name,''))"
                + " FROM ess_panic_event p"
                + " INNER JOIN employee eope ON eope.id = p.checked_by_id"
                + " INNER JOIN ess_person sp ON sp.emp_id = p.emp_id"
                + " INNER JOIN ess_person_unit pu ON pu.person_id = sp.id"
                + " INNER JOIN ess_unit u ON u.id = pu.unit_id"
                + " INNER JOIN ess_building b ON b.id = u.build_id"
                + " WHERE p.close_dt IS NULL"
        ).getRecords(conn);
        r.sortType = GridResult.SORT_DESC;
        r.sortColIndex = 1;
        return r;
    }

    @POST
    @Path("/open")
    public Response gridOpen(GridRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(getEventsOpen(conn));
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
            GridResult r = getEventsOpen(conn);
            MySQLReport rep = new MySQLReport("Emergencias Abiertas", null, "hoja1", now(conn));
            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
            rep.setVerticalFreeze(5);
            rep.setZoomFactor(80);            
            rep.setMultiRowTitles(true);
            rep.getTables().add(new Table("Emergencias Abiertas"));
            rep = MySQLReport.getReport(rep, r.cols, r.data);
            return createResponse(rep.write(conn), "EmergenciasAbiertas.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public GridResult getEventsClosed(Connection conn) throws Exception {
        GridResult r = new GridResult();
        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_KEY),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 50, "Inicio"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 75, "Solicitó"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 75, "Edificio"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 75, "Apartamento"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 75, "Teléfono"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 50, "Finalizadó"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 75, "Operario"),};

        r.data = new MySQLQuery("SELECT "
                + " p.id, "
                + " p.reg_dt, CONCAT(sp.first_name,' ', IFNULL(sp.last_name,'')), "
                + " b.name,"
                + " CONCAT(u.tower, ' - ',u.code),"
                + " u.phone,"
                + " p.check_dt, CONCAT(eope.first_name,' ', IFNULL(eope.last_name,''))"
                + " FROM ess_panic_event p"
                + " INNER JOIN employee eope ON eope.id = p.checked_by_id"
                + " INNER JOIN ess_person sp ON sp.emp_id = p.emp_id"
                + " INNER JOIN ess_person_unit pu ON pu.person_id = sp.id"
                + " INNER JOIN ess_unit u ON u.id = pu.unit_id"
                + " INNER JOIN ess_building b ON b.id = u.build_id"
                + " WHERE p.close_dt IS NOT NULL"
        ).getRecords(conn);

        r.sortType = GridResult.SORT_DESC;
        r.sortColIndex = 1;
        return r;
    }

    @POST
    @Path("/closed")
    public Response gridClosed(GridRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(getEventsClosed(conn));
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
            GridResult r = getEventsClosed(conn);
            MySQLReport rep = new MySQLReport("Emergencias Cerradas", null, "hoja1", now(conn));
            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
            rep.setVerticalFreeze(5);
            rep.setZoomFactor(80);
            rep.setMultiRowTitles(true);
            rep.getTables().add(new Table("Emergencias Cerradas"));
            rep = MySQLReport.getReport(rep, r.cols, r.data);
            return createResponse(rep.write(conn), "EmergenciasCerradas.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
