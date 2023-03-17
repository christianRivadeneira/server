package api.ord.api;

import api.BaseAPI;
import api.ord.dto.DetailsPollRequest;
import api.ord.model.OrdContractIndex;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import api.ord.model.OrdPoll;
import api.ord.model.OrdTankClient;
import java.text.SimpleDateFormat;
import utilities.MySQLQuery;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.Table;

@Path("/ordPoll")
public class OrdPollApi extends BaseAPI {

    @POST
    public Response insert(OrdPoll obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(OrdPoll obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.update(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            OrdPoll obj = new OrdPoll().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            OrdPoll.delete(id, conn);
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
            return createResponse(OrdPoll.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/detailRepairPoll")
    public Response getDetailRepairPoll(DetailsPollRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");
            String str = "SELECT "
                    + (req.allOffices ? "of.sname, " : "")
                    + "p.serial, "
                    + (req.enterprise ? "e.`short_name`, " : "")//2
                    + "p.regist_date, "//3
                    + "p.regist_hour, "//4
                    + "COALESCE(em.short_name, CONCAT(em.first_name, ' ' ,em.last_name)), "//5
                    + "p.confirm_date, "//6
                    + "IF(i.id IS NOT NULL, CONCAT(i.first_name, ' ', i.last_name), IF(b.id IS NOT NULL, b.name, CONCAT(client.first_name, ' ', COALESCE(client.last_name, '')))), "//7
                    + " CASE "//direccion 
                    + "WHEN p.index_id IS NOT NULL THEN CONCAT(i.address, IF(ne.`name` IS NOT NULL, CONCAT(' ', ne.`name`), ''), ' [', c.name,']') "
                    + "WHEN p.build_id IS NOT NULL THEN CONCAT(b.address,' [', c.name,']') "
                    + "WHEN p.client_id IS NOT NULL AND client.build_ord_id IS NOT NULL THEN CONCAT(build.address,' ',build.name,' [', c.name,']')  "
                    + "ELSE CONCAT(n.name,' ',client.address)  "
                    + "END ,"
                    + "r.description, "//9
                    + (req.subreason ? " subr.description ," : " ")
                    + "CONCAT(t.first_name, ' ', t.last_name) "//10
                    + (req.showPqrNotes ? ",p.notes " : " ")
                    + ", IF(p.satis_poll_id IS NOT NULL,'Si','No'), "
                    + "np.name, "
                    + "poll.notes "
                    + "FROM "
                    + "ord_repairs AS p "
                    + "INNER JOIN ord_pqr_reason AS r ON r.id = p.reason_id "
                    + "INNER JOIN ord_technician AS t ON t.id = p.technician_id "
                    + "LEFT JOIN ord_contract_index as i ON i.id = p.index_id "//cliente cilindros
                    + "LEFT JOIN neigh as ne ON ne.id = i.neigh_id "//bario del cliente cilindros
                    + "LEFT JOIN ord_pqr_client_tank as client ON client.id = p.client_id "//cliente facturación
                    + "LEFT JOIN ord_tank_client as build ON build.id = client.build_ord_id "//edificio del un cliente facturación
                    + "LEFT JOIN ord_tank_client as b ON b.id = p.build_id "//pqr a nombre del edificio
                    + (req.enterprise ? "LEFT JOIN enterprise AS e ON p.enterprise_id = e.id " : "")
                    + "INNER JOIN employee em ON em.id = p.regist_by  "
                    + (req.allOffices ? "INNER JOIN ord_office AS of ON of.id = p.office_id " : "")
                    + (req.subreason ? "LEFT JOIN ord_pqr_subreason subr ON subr.id = p.subreason_id " : "")
                    + "LEFT JOIN neigh AS n ON n.id = client.neigh_id " //barrio redes 
                    + "LEFT JOIN city c ON c.id = IF(i.id IS NOT NULL, i.city_id, IF(b.id IS NOT NULL, b.city_id, build.city_id )) "
                    + "LEFT JOIN ord_no_poll np ON np.id = p.no_poll_id "
                    + "LEFT JOIN ord_poll poll ON poll.id = p.satis_poll_id "
                    + "WHERE "
                    + req.officeCond + " AND "
                    + "p.pqr_poll_id IS NOT NULL AND "
                    + "p.anul_cause_id IS NULL AND "
                    + "(p.satis_poll_id IS NOT NULL OR p.no_poll_id IS NOT NULL) AND "
                    + "p.confirm_date BETWEEN ?1 AND ?2 ";

            MySQLQuery q = new MySQLQuery(str);
            q.setParam(1, req.begin);
            q.setParam(2, req.end);
            Object[][] data = q.getRecords(conn);

            MySQLReport rep = new MySQLReport("Reporte Detallado Encuestas Asistencias Técnicas", "", "Encuestas", MySQLQuery.now(conn));
            rep.getSubTitles().add("Periodo " + dt.format(req.begin) + " - " + dt.format(req.end));

            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
            rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//1          
            rep.getFormats().add(new CellFormat(MySQLReportWriter.ENUM, MySQLReportWriter.LEFT, new OrdTankClient().getEnumOptions("type")));//2   
            rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "HH:mm"));//3   

            rep.setZoomFactor(85);
            rep.setShowNumbers(true);
            rep.getFormats().get(0).setWrap(true);
            rep.setVerticalFreeze(5);
            Table tb = new Table("Encuestas Asistencias Técnicas");
            if (req.allOffices) {
                tb.getColumns().add(new Column("Ofi", 15, 0));
            }
            tb.getColumns().add(new Column("Solicitud", 20, 0));
            if (req.enterprise) {
                tb.getColumns().add(new Column("Emp", 15, 0));
            }
            tb.getColumns().add(new Column("Cap", 15, 1));
            tb.getColumns().add(new Column("Hora", 15, 3));
            tb.getColumns().add(new Column("Capturó", 25, 0));
            tb.getColumns().add(new Column("Conf", 15, 1));
            tb.getColumns().add(new Column("Cliente", 25, 0));
            tb.getColumns().add(new Column("Dirección", 30, 0));
            tb.getColumns().add(new Column("Motivo", 30, 0));
            if (req.subreason) {
                tb.getColumns().add(new Column("Dlle. Motivo", 40, 0));
            }
            tb.getColumns().add(new Column("Técnico", 40, 0));
            if (req.showPqrNotes) {
                tb.getColumns().add(new Column("Notas", 40, 0));
            }
            tb.getColumns().add(new Column("Enc", 10, 0));
            tb.getColumns().add(new Column("Motivo no Encuesta", 40, 0));
            tb.getColumns().add(new Column("Informe Final", 40, 0));

            tb.setData(data);
            if (tb.getData().length > 0) {
                rep.getTables().add(tb);
            }
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/detailCylOrderPoll")
    public Response getDetailCylOrderPoll(DetailsPollRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");
            String str = "SELECT "
                    + (req.allOffices ? "of.sname, " : "")
                    + "o.id, "//0
                    + "o.taken_hour, "//1
                    + "i.type, "//2
                    + "i.document, "//3
                    + "CONCAT(i.first_name, ' ', i.last_name), "//4
                    + "CONCAT(i.address, IF(neigh.`name` IS NOT NULL, CONCAT(' ', neigh.`name`), '')), "//5
                    + "sector.`name`, "//6
                    + "(SELECT COUNT(*) > 0 FROM ord_cyl_order_nov WHERE cyl_order_id = o.id), "
                    + "IF(o.poll_id IS NOT NULL,'Si','No'), "
                    + "np.name, "
                    + "p.notes "
                    + "FROM "
                    + "ord_cyl_order AS o "
                    + "INNER JOIN employee AS e ON e.id = o.taken_by_id "
                    + "INNER JOIN ord_contract_index AS i ON i.id = o.index_id "
                    + "LEFT JOIN neigh ON neigh.id = o.neigh_id "
                    + "INNER JOIN sector ON sector.id = neigh.sector_id "
                    + "LEFT JOIN ord_no_poll np ON np.id = o.no_poll_id "
                    + "LEFT JOIN ord_poll p ON p.id = o.poll_id "
                    + (req.allOffices ? "INNER JOIN ord_office AS of ON of.id = o.office_id " : "")
                    + "WHERE "
                    + (req.pollOtherOperator || req.pollResp ? "" : "o.taken_by_id = " + req.empId + " AND ")
                    + req.officeCond + " AND "
                    + "o.`day` BETWEEN ?1 AND ?2 AND "
                    + "o.cancel_cause_id IS NULL AND "
                    + "o.vehicle_id IS NOT NULL AND "
                    + "o.confirm_hour IS NOT NULL AND "
                    + "(o.poll_id IS NOT NULL OR o.no_poll_id IS NOT NULL) ";

            MySQLQuery q = new MySQLQuery(str);
            q.setParam(1, req.begin);
            q.setParam(2, req.end);
            Object[][] data = q.getRecords(conn);

            MySQLReport rep = new MySQLReport("Reporte Detallado Encuestas Pedidos Cilindros", "", "Encuestas", MySQLQuery.now(conn));
            rep.getSubTitles().add("Periodo " + dt.format(req.begin) + " - " + dt.format(req.end));

            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
            rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//1          
            rep.getFormats().add(new CellFormat(MySQLReportWriter.ENUM, MySQLReportWriter.LEFT, new OrdContractIndex().getEnumOptions("contract_type")));//2   
            rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "HH:mm"));//3   

            rep.setZoomFactor(85);
            rep.setShowNumbers(true);
            rep.getFormats().get(0).setWrap(true);
            rep.setVerticalFreeze(5);
            Table tb = new Table("Encuestas Pedidos Cilindros");
            if (req.allOffices) {
                tb.getColumns().add(new Column("Ofi", 15, 0));
            }
            tb.getColumns().add(new Column("Número", 20, 0));
            tb.getColumns().add(new Column("Cap", 15, 3));
            tb.getColumns().add(new Column("Clie", 15, 2));
            tb.getColumns().add(new Column("Documento", 15, 0));
            tb.getColumns().add(new Column("Nombres", 25, 0));
            tb.getColumns().add(new Column("Dirección", 40, 0));
            tb.getColumns().add(new Column("Sector", 25, 0));
            tb.getColumns().add(new Column("Nov", 10, 0));
            tb.getColumns().add(new Column("Enc", 10, 0));
            tb.getColumns().add(new Column("Motivo no Encuesta", 40, 0));
            tb.getColumns().add(new Column("Informe Final", 40, 0));

            tb.setData(data);
            if (tb.getData().length > 0) {
                rep.getTables().add(tb);
            }
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/detailExecVisit")
    public Response getDetailExecVisit(DetailsPollRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");
            String str = "SELECT "
                    + "v.regist_date, "
                    + "IF(v.regist_by IS NOT NULL, CONCAT(er.first_name, ' ', er.last_name),''), "
                    + "IF(v.vehicle_id IS NOT NULL , 'Ventas', 'Visita'), "
                    + "IF(v.vehicle_id IS NOT NULL , 'Carrotanque', IF(v.manager_id IS NOT NULL, 'Gestor', 'Ejecutivo Cta')) , "
                    + "IF(v.vehicle_id IS NOT NULL , CONCAT(ev.first_name,' ',ev.last_name), IF(v.manager_id IS NOT NULL, CONCAT(em.first_name,' ',em.last_name), CONCAT(es.first_name,' ',es.last_name))) , "
                    + "CONCAT(vh.internal,' ',e.short_name,' - ' ,vh.plate), "
                    + "c.name, "
                    + "CONCAT(c.address,' [', ct.name,']')  , "
                    + "c.phones, "
                    + "pob.name, "
                    + "v.visit_date,"
                    + "c.type, "
                    + "IF(v.poll_id IS NOT NULL,'Si','No'), "
                    + "np.name, "
                    + "p.notes "
                    + "FROM "
                    + "ord_tank_visit AS v "
                    + "LEFT JOIN com_service_manager m ON m.id = v.manager_id "
                    + "LEFT JOIN employee em ON em.id = m.emp_id "
                    + "LEFT JOIN per_employee es ON es.id = v.supervisor_id "
                    + "LEFT JOIN vehicle AS vh ON vh.id = v.vehicle_id "
                    + "LEFT JOIN employee ev ON ev.id = v.driver_id "
                    + "LEFT JOIN employee er ON er.id = v.regist_by "
                    + "LEFT JOIN agency AS a ON vh.agency_id = a.id "
                    + "LEFT JOIN enterprise AS e ON e.id = a.enterprise_id "
                    + "INNER JOIN ord_tank_client AS c ON c.id = v.client_id "
                    + "INNER JOIN dane_poblado AS pob ON c.dane_pob = pob.id "
                    + "INNER JOIN city ct ON ct.id = c.city_id "
                    + "LEFT JOIN ord_no_poll np ON np.id = v.no_poll_id "
                    + "LEFT JOIN ord_poll p ON p.id = v.poll_id "
                    + "WHERE "
                    + "v.vehicle_id IS NULL AND  "
                    + "v.visit_date BETWEEN ?1 AND ?2 "
                    + "AND (v.poll_id IS NOT NULL OR v.no_poll_id IS NOT NULL) AND "
                    + "c.city_id IN (" + req.citiesList + ") ";

            MySQLQuery q = new MySQLQuery(str);
            q.setParam(1, req.begin);
            q.setParam(2, req.end);
            Object[][] data = q.getRecords(conn);

            MySQLReport rep = new MySQLReport("Reporte Detallado Encuestas Estacionarios", "", "encuestas", MySQLQuery.now(conn));
            rep.getSubTitles().add("Periodo " + dt.format(req.begin) + " - " + dt.format(req.end));

            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
            rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//1          
            rep.getFormats().add(new CellFormat(MySQLReportWriter.ENUM, MySQLReportWriter.LEFT, new OrdTankClient().getEnumOptions("type")));//2            

            rep.setZoomFactor(85);
            rep.setShowNumbers(true);
            rep.getFormats().get(0).setWrap(true);
            rep.setVerticalFreeze(5);
            Table tb = new Table("Encuestas Estacionarios");
            tb.getColumns().add(new Column("Creación", 15, 1));
            tb.getColumns().add(new Column("Capturó", 15, 0));
            tb.getColumns().add(new Column("Evento", 15, 0));
            tb.getColumns().add(new Column("Tipo", 18, 0));
            tb.getColumns().add(new Column("Empleado", 40, 0));
            tb.getColumns().add(new Column("Carrotanque", 20, 0));
            tb.getColumns().add(new Column("Cliente", 40, 0));
            tb.getColumns().add(new Column("Dirección", 30, 2));
            tb.getColumns().add(new Column("Teléfono", 25, 0));
            tb.getColumns().add(new Column("Ciudad", 30, 0));
            tb.getColumns().add(new Column("Fecha", 15, 1));
            tb.getColumns().add(new Column("Tipo", 15, 2));
            tb.getColumns().add(new Column("Enc", 10, 0));
            tb.getColumns().add(new Column("Motivo no Encuesta", 40, 0));
            tb.getColumns().add(new Column("Informe Final", 40, 0));

            tb.setData(data);
            if (tb.getData().length > 0) {
                rep.getTables().add(tb);
            }
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/detailTankVisit")
    public Response getDetailTankVisit(DetailsPollRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");
            String str = "SELECT "
                    + "v.regist_date, "
                    + "IF(v.regist_by IS NOT NULL, CONCAT(er.first_name, ' ', er.last_name),''), "
                    + "IF(v.vehicle_id IS NOT NULL , 'Ventas', 'Visita'), "
                    + "IF(v.vehicle_id IS NOT NULL , 'Carrotanque', IF(v.manager_id IS NOT NULL, 'Gestor', 'Ejecutivo Cta')) , "
                    + "IF(v.vehicle_id IS NOT NULL , CONCAT(ev.first_name,' ',ev.last_name), IF(v.manager_id IS NOT NULL, CONCAT(em.first_name,' ',em.last_name), CONCAT(es.first_name,' ',es.last_name))) , "
                    + "CONCAT(vh.internal,' ',e.short_name,' - ' ,vh.plate), "
                    + "c.name, "
                    + "CONCAT(c.address,' [', ct.name,']')  , "
                    + "c.phones, "
                    + "pob.name, "
                    + "v.visit_date,"
                    + "c.type, "
                    + "IF(v.poll_id IS NOT NULL,'Si','No'), "
                    + "np.name, "
                    + "p.notes "
                    + "FROM "
                    + "ord_tank_visit AS v "
                    + "LEFT JOIN com_service_manager m ON m.id = v.manager_id "
                    + "LEFT JOIN employee em ON em.id = m.emp_id "
                    + "LEFT JOIN per_employee es ON es.id = v.supervisor_id "
                    + "LEFT JOIN vehicle AS vh ON vh.id = v.vehicle_id "
                    + "LEFT JOIN employee ev ON ev.id = v.driver_id "
                    + "LEFT JOIN employee er ON er.id = v.regist_by "
                    + "LEFT JOIN agency AS a ON vh.agency_id = a.id "
                    + "LEFT JOIN enterprise AS e ON e.id = a.enterprise_id "
                    + "INNER JOIN ord_tank_client AS c ON c.id = v.client_id "
                    + "INNER JOIN dane_poblado AS pob ON c.dane_pob = pob.id "
                    + "INNER JOIN city ct ON ct.id = c.city_id "
                    + "LEFT JOIN ord_no_poll np ON np.id = v.no_poll_id "
                    + "LEFT JOIN ord_poll p ON p.id = v.poll_id "
                    + "WHERE "
                    + "v.vehicle_id IS NOT NULL AND  "
                    + "v.visit_date BETWEEN ?1 AND ?2 "
                    + "AND (v.poll_id IS NOT NULL OR v.no_poll_id IS NOT NULL) AND "
                    + "c.city_id IN (" + req.citiesList + ") ";

            MySQLQuery q = new MySQLQuery(str);
            q.setParam(1, req.begin);
            q.setParam(2, req.end);
            Object[][] data = q.getRecords(conn);

            MySQLReport rep = new MySQLReport("Reporte Detallado Encuestas Estacionarios", "", "encuestas", MySQLQuery.now(conn));
            rep.getSubTitles().add("Periodo " + dt.format(req.begin) + " - " + dt.format(req.end));

            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
            rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//1          
            rep.getFormats().add(new CellFormat(MySQLReportWriter.ENUM, MySQLReportWriter.LEFT, new OrdTankClient().getEnumOptions("type")));//2            

            rep.setZoomFactor(85);
            rep.setShowNumbers(true);
            rep.getFormats().get(0).setWrap(true);
            rep.setVerticalFreeze(5);
            Table tb = new Table("Encuestas Estacionarios");
            tb.getColumns().add(new Column("Creación", 15, 1));
            tb.getColumns().add(new Column("Capturó", 15, 0));
            tb.getColumns().add(new Column("Evento", 15, 0));
            tb.getColumns().add(new Column("Tipo", 18, 0));
            tb.getColumns().add(new Column("Empleado", 40, 0));
            tb.getColumns().add(new Column("Carrotanque", 20, 0));
            tb.getColumns().add(new Column("Cliente", 40, 0));
            tb.getColumns().add(new Column("Dirección", 30, 2));
            tb.getColumns().add(new Column("Teléfono", 25, 0));
            tb.getColumns().add(new Column("Ciudad", 30, 0));
            tb.getColumns().add(new Column("Fecha", 15, 1));
            tb.getColumns().add(new Column("Tipo", 15, 2));
            tb.getColumns().add(new Column("Enc", 10, 0));
            tb.getColumns().add(new Column("Motivo no Encuesta", 40, 0));
            tb.getColumns().add(new Column("Informe Final", 40, 0));

            tb.setData(data);
            if (tb.getData().length > 0) {
                rep.getTables().add(tb);
            }
            return createResponse(rep.write(conn), "exp_cts_como.xls");            
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/detailStoreVisit")
    public Response getDetailStoreVisit(DetailsPollRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");
            String str = "SELECT "
                    + "v.regist_date, "
                    + "IF(v.regist_by IS NOT NULL, CONCAT(er.first_name, ' ', er.last_name),''), "
                    + "CONCAT(e.first_name, ' ', e.last_name), "
                    + "CONCAT(s.first_name, ' ', s.last_name), "
                    + "s.address, "
                    + "IF(v.poll_id IS NOT NULL,'Si','No'), "
                    + "np.name, "
                    + "p.notes "
                    + "FROM inv_store_visit v "
                    + "INNER JOIN inv_store s ON s.id = v.store_id "
                    + "INNER JOIN com_service_manager m ON v.manager_id = m.id "
                    + "INNER JOIN employee e ON e.id = m.emp_id "
                    + "LEFT JOIN employee er ON er.id = v.regist_by "
                    + "LEFT JOIN ord_no_poll np ON np.id = v.no_poll_id "
                    + "LEFT JOIN ord_poll p ON p.id = v.poll_id "
                    + "WHERE v.visit_date BETWEEN ?1 AND ?2 "
                    + "AND s.city_id IN (" + req.citiesList + ") "
                    + "AND (v.poll_id IS NOT NULL OR v.no_poll_id IS NOT NULL)";

            MySQLQuery q = new MySQLQuery(str);
            q.setParam(1, req.begin);
            q.setParam(2, req.end);
            Object[][] data = q.getRecords(conn);

            MySQLReport rep = new MySQLReport("Reporte Detallado Encuestas Almacenes", "", "Encuestas", MySQLQuery.now(conn));
            rep.getSubTitles().add("Periodo " + dt.format(req.begin) + " - " + dt.format(req.end));

            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
            rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//1          
            rep.getFormats().add(new CellFormat(MySQLReportWriter.ENUM, MySQLReportWriter.LEFT, new OrdTankClient().getEnumOptions("type")));//2            

            rep.setZoomFactor(85);
            rep.setShowNumbers(true);
            rep.getFormats().get(0).setWrap(true);
            rep.setVerticalFreeze(5);
            Table tb = new Table("Encuestas Almacenes");
            tb.getColumns().add(new Column("Creación", 20, 1));
            tb.getColumns().add(new Column("Capturó", 40, 0));
            tb.getColumns().add(new Column("Gestor", 40, 0));
            tb.getColumns().add(new Column("Almacén", 40, 0));
            tb.getColumns().add(new Column("Dirección", 40, 0));
            tb.getColumns().add(new Column("Enc", 10, 0));
            tb.getColumns().add(new Column("Motivo no Encuesta", 40, 0));
            tb.getColumns().add(new Column("Informe Final", 40, 0));

            tb.setData(data);
            if (tb.getData().length > 0) {
                rep.getTables().add(tb);
            }
            return createResponse(rep.write(conn), "exp_cts_como.xls");            
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
