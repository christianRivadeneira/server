package api.crm.api;

import api.BaseAPI;
import api.GridResult;
import api.GridResultsPdf;
import api.MySQLCol;
import api.crm.model.CrmClient;
import api.crm.model.CrmProject;
import api.crm.model.CrmTask;
import api.crm.model.FullProspect;
import api.est.model.EstMto;
import java.awt.Color;
import java.io.File;
import java.sql.Connection;
import java.util.Date;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;
import utilities.apiClient.IntegerResponse;

@Path("/CrmClientApi")
public class CrmClientApi extends BaseAPI {

    public static final int MODULE_ID = 852;

    @POST
    @Path("/clientMethods")
    public Response clientMethods(FullProspect fp) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLQuery log = new MySQLQuery("INSERT INTO crm_log SET "
                    + "`owner_id` = ?1, "
                    + "`owner_type` = ?2, "
                    + "`employee_id` = ?3, "
                    + "`log_date` = NOW(), "
                    + "`notes` = ?4");
            log.setParam(1, MySQLQuery.getAsInteger(fp.clientId));
            log.setParam(2, MySQLQuery.getAsInteger(fp.ownerType));
            log.setParam(3, MySQLQuery.getAsInteger(fp.employeeId));
            log.setParam(4, fp.notesLog.replaceAll("[\"']", ""));
            log.executeInsert(conn);
            Integer idProspect = null;
            if (fp.withEst) {
                Integer regExecId = new MySQLQuery("SELECT p.id "
                        + "FROM per_employee p "
                        + "INNER JOIN est_exec_reg e ON e.per_emp_id = p.id "
                        + "WHERE p.active AND e.active AND p.emp_id = ?1 LIMIT 1").setParam(1, fp.salesEmployeeId).getAsInteger(conn);
                MySQLQuery insertEstProspect = new MySQLQuery("INSERT INTO est_prospect SET "
                        + "`reg_dt` = ?1, "
                        + "`afil_dt` = ?2, "
                        + "`document` = ?3, "
                        + "`name` = ?4, "
                        + "`capacity` = ?5, "
                        + "`cons_planned` = ?6, "
                        + "`reg_exec_id` = ?7, "
                        + "`client_id` = ?8, "
                        + "`pros_ent_id` = ?9, "
                        + "`address` = ?10, "
                        + "`phone` = ?11, "
                        + "`mail` = ?12, "
                        + "`contact` = ?13, "
                        + "`category_id` = ?14 "
                );

                insertEstProspect.setParam(1, new Date());
                insertEstProspect.setParam(2, null);
                insertEstProspect.setParam(3, fp.document);
                insertEstProspect.setParam(4, fp.name);
                insertEstProspect.setParam(5, "0");
                insertEstProspect.setParam(6, 0);
                insertEstProspect.setParam(7, regExecId);
                insertEstProspect.setParam(8, null);
                insertEstProspect.setParam(9, null);
                insertEstProspect.setParam(10, fp.address);
                insertEstProspect.setParam(11, fp.phone);
                insertEstProspect.setParam(12, fp.mail);
                insertEstProspect.setParam(13, fp.contact);
                insertEstProspect.setParam(14, fp.categoryId);
                idProspect = insertEstProspect.executeInsert(conn);
            }
            MySQLQuery updateCliente = new MySQLQuery("UPDATE crm_client SET type = 'client', state = 'opor_cli' , begin_date= ?1, prospect_id = ?2 WHERE id = ?3");
            updateCliente.setParam(1, new Date());
            updateCliente.setParam(2, idProspect);
            updateCliente.setParam(3, MySQLQuery.getAsInteger(fp.clientId));
            updateCliente.executeUpdate(conn);

            MySQLQuery insertProspect = new MySQLQuery("INSERT INTO crm_prospec_flow SET "
                    + "`client_id` = ?1, "
                    + "`state` = ?2, "
                    + "`reg_date` = ?3, "
                    + "`notes` = ?4");
            insertProspect.setParam(1, MySQLQuery.getAsInteger(fp.clientId));
            insertProspect.setParam(2, fp.state);
            insertProspect.setParam(3, new Date());
            insertProspect.setParam(4, fp.notes);
            insertProspect.executeInsert(conn);

            return Response.ok().build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getCountCrm")
    public Response getCountProspects(
            @QueryParam("empId") int empId,
            @QueryParam("type") String type,
            @QueryParam("isAdmin") boolean isAdmin) {
        try (Connection conn = getConnection()) {

            String str = "SELECT COUNT(*)  "
                    + "FROM crm_client c "
                    + "LEFT JOIN est_prospect p ON p.id = c.prospect_id "
                    + "LEFT JOIN ord_tank_client o ON o.id = p.client_id ";
            if (isAdmin) {
                str += "WHERE c.type = ?1 AND o.id IS NULL AND "
                        + "c.sales_employee_id IS NULL "
                        + "AND c.created_by = " + empId + " "
                        + "AND c.active "
                        + "AND IFNULL(p.active,true) AND IFNULL(o.active,true) ";

            } else {
                str += "WHERE c.sales_employee_id = ?1 AND c.type = ?2 AND o.id IS NULL "
                        + "AND c.active "
                        + "AND IFNULL(p.active,true) AND IFNULL(o.active,true) ";
            }

            Integer count;
            if (isAdmin) {
                count = new MySQLQuery(str).setParam(1, type).getAsInteger(conn);
            } else {
                count = new MySQLQuery(str).setParam(1, empId).setParam(2, type).getAsInteger(conn);
            }

            count = (count != null ? count : 0);

            return createResponse(new IntegerResponse(count));
        } catch (Exception ex) {
            ex.printStackTrace();
            return createResponse(ex);
        }
    }

    @POST
    @Path("/hv")
    public Response getHv(@QueryParam("clientId") int clientId, @QueryParam("limit") boolean limit) {
        try (Connection conn = getConnection()) {
            return createResponse(getHv(clientId, limit, conn), "hv.pdf");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public static File getHv(int clientId, boolean limit, Connection conn) throws Exception {

        File f = File.createTempFile("hojaDeVida", ".pdf");

        Color colorBackground = new Color(255, 248, 248);
        Color colorBorder = new Color(250, 69, 30);

        GridResultsPdf pdf = new GridResultsPdf(f, colorBackground, colorBorder);

        pdf.addDocumentTitle("Hoja de Vida");

        GridResult r = new GridResult();

        r.data = new MySQLQuery("SELECT "
                + "c.name, ch.name, ct.name, "
                + "CONCAT(e.first_name, ' ', e.last_name), c.main_contact, "
                + "c.phone, c.begin_date, c.state "
                + "FROM crm_client c "
                + "LEFT JOIN crm_chanel ch ON ch.id = c.chanel_id "
                + "LEFT JOIN city ct ON c.city_id = ct.id "
                + "LEFT JOIN employee e ON e.id = c.sales_employee_id "
                + "WHERE "
                + "c.type = 'prospect' AND "
                + "c.id = ?1").setParam(1, clientId).getRecords(conn);

        if (r.data != null && r.data.length > 0) {
            r.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Nombre"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Canal"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Ciudad"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Responsable"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Contacto"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Teléfono"),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 30, "Registro"),
                new MySQLCol(MySQLCol.TYPE_ENUM, 20, "Estado", new CrmClient().getEnumOptionsAsMatrix("state"))};

            pdf.addVerticalGrid("Datos Prospecto", r);
        }

        r = new GridResult();
        r.data = new MySQLQuery("SELECT "
                + "cl.`name`, cl.short_name, cl.document, "
                + "(SELECT name FROM crm_chanel WHERE id = cl.chanel_id), ct.`name`, "
                + " CONCAT(e.first_name,' ',e.last_name), cl.main_contact, cl.phone, "
                + " (SELECT pf.reg_date FROM crm_prospec_flow AS pf WHERE pf.client_id = cl.id AND state = 'opor_cli' ORDER BY pf.reg_date DESC LIMIT 1), cl.state "
                + " FROM crm_client cl "
                + " LEFT JOIN city ct ON cl.city_id = ct.id "
                + " LEFT JOIN employee AS e ON e.id = cl.sales_employee_id "
                + " LEFT JOIN est_prospect p ON cl.prospect_id = p.id "
                + " LEFT JOIN ord_tank_client tc ON tc.id = p.client_id "
                + " WHERE cl.type = 'client' AND cl.id = ?1").setParam(1, clientId).getRecords(conn);

        if (r.data != null && r.data.length > 0) {
            r.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Nombre"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Nombre Corto"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Documento"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Canal"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Ciudad"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Responsable"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Contacto"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Teléfono"),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 30, "Conversión"),
                new MySQLCol(MySQLCol.TYPE_ENUM, 30, "Estado", new CrmClient().getEnumOptionsAsMatrix("state"))};

            pdf.addVerticalGrid("Datos Cliente", r);
        }

        r = new GridResult();
        r.data = new MySQLQuery("SELECT ct.desc_short,p.name, tt.name, ct.prog_date, ct.rem_date, ct.ejec_date, IF(ct.satisfactory, 'Si', 'No') "
                + "FROM crm_task AS ct "
                + "INNER JOIN crm_type_task AS tt ON tt.id = ct.type_task_id "
                + "LEFT JOIN crm_project p ON ct.project_id = p.id "
                + "WHERE tt.module = ?1 AND ct.client_id = ?2 ORDER BY ct.prog_date "
                + (limit ? " LIMIT 15" : "")).setParam(1, MODULE_ID).setParam(2, clientId).getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Actividad"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Proyecto"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 15, "Tipo"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 10, "Programada"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_A, 15, "Recordar"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 10, "Ejecutada"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 10, "Satisfactoria")
        };

        pdf.addGrid("Actividades", r);

        r = new GridResult();
        r.data = new MySQLQuery("SELECT p.name, em.short_name, p.state, p.dt_initial "
                + "FROM crm_project p "
                + "LEFT JOIN per_employee e ON e.id = p.resp_id "
                + "LEFT JOIN employee em ON em.id = e.emp_id "
                + "WHERE p.client_id = ?1 "
                + (limit ? " LIMIT 15" : "")).setParam(1, clientId).getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_TEXT, 40, "Nombre"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 40, "Responsable"),
            new MySQLCol(MySQLCol.TYPE_ENUM, 15, "Estado", new CrmProject().getEnumOptionsAsMatrix("state")),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 15, "Inicio")
        };

        pdf.addGrid("Proyectos", r);

        r = new GridResult();
        r.data = new MySQLQuery("SELECT pf.reg_date, pf.state, pf.notes "
                + "FROM crm_prospec_flow AS pf WHERE pf.client_id = ?1 ORDER BY pf.reg_date DESC"
                + (limit ? " LIMIT 15" : "")).setParam(1, clientId).getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 20, "Registro"),
            new MySQLCol(MySQLCol.TYPE_ENUM, 20, "Estado", new CrmClient().getEnumOptionsAsMatrix("state")),
            new MySQLCol(MySQLCol.TYPE_TEXT, 60, "Notas")};

        pdf.addGrid("Flujo del cliente o prospecto", r);
        pdf.close();
        return f;
    }

}
