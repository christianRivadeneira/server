package api.per.api;

import api.BaseAPI;
import api.GridResult;
import api.GridResultsPdf;
import api.MySQLCol;
import api.per.dto.EmpPaymentRequest;
import api.per.model.PerContract;
import api.per.model.PerEmployee;
import api.per.rpt.PersonalReport;
import java.awt.Color;
import java.io.File;
import java.sql.Connection;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;
import utilities.mysqlReport.MySQLReport;

@Path("/perEmployee")
public class PerEmployeeApi extends BaseAPI {

    public static int EMPLOYEE_PHOTO = 10;

    @GET
    @Path("/{empId}")
    public Response findById(@PathParam("empId") int empId) {
        try (Connection con = getConnection()) {
            MySQLQuery query = getPerEmployeeByEmpId(empId);
            PerEmployee employee = new PerEmployee().select(query, con);

            return createResponse(employee);
        } catch (Exception e) {
            e.printStackTrace();
            return createResponse(e);
        }
    }

    public MySQLQuery getPerEmployeeByEmpId(int empId) {
        String query = "SELECT "
                + PerEmployee.getSelFlds("pe")
                + "FROM per_employee pe "
                + "WHERE pe.emp_id = ?1 ";

        return new MySQLQuery(query)
                .setParam(1, empId);
    }

    @GET
    public Response findByDocument(@QueryParam("document") String document) {
        try (Connection con = getConnection()) {

            MySQLQuery employeeQuery = getPerEmployeeByDocumentQuery(document);
            PerEmployee employee = new PerEmployee().select(employeeQuery, con);

            if (employee == null) {
                throw new Exception("No hay empleados con ese número de documento.");
            }

            MySQLQuery contractQuery = getPerContractByEmpId(employee.id);
            PerContract contract = new PerContract().select(contractQuery, con);

            if (contract == null) {
                throw new Exception("El empleado no tiene contratos vigentes");
            }

            return createResponse(employee);
        } catch (Exception ex) {
            ex.printStackTrace();
            return createResponse(ex);
        }
    }

    private MySQLQuery getPerEmployeeByDocumentQuery(String document) {
        String query = "SELECT  "
                + PerEmployee.getSelFlds("pe")
                + "FROM per_employee pe "
                + "WHERE pe.document = ?1";

        return new MySQLQuery(query)
                .setParam(1, document);
    }

    private MySQLQuery getPerContractByEmpId(int empId) {
        String query = "SELECT "
                + PerContract.getSelFlds("pc")
                + "FROM per_contract pc "
                + "WHERE pc.emp_id = ?1 "
                + "AND pc.last = 1 "
                + "AND pc.active = 1 "
                + "AND pc.leave_date IS NULL ";

        return new MySQLQuery(query)
                .setParam(1, empId);
    }

    @POST
    @Path("/hv")
    public Response getHv(@QueryParam("perEmpId") int perEmpId, @QueryParam("limit") boolean limit) {
        try (Connection conn = getConnection()) {
            return createResponse(getHv(perEmpId, limit, conn), "hv.pdf");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/empPayment")
    public Response getEmpPayment(EmpPaymentRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = PersonalReport.getEmpPayment(req.payMonth, req.qna1, req.invertNames, req.authOffices, req.enterpriseId, req.cityId, req.employeerId,
                    req.sbareaId, req.salesman, req.areaId, req.posId, req.officeId, req.employeerName, req.enterpriseName, req.cityName,
                    req.areaName, req.sbAreaName, req.titleArea, req.titleSbArea, conn);
            return createResponse(rep.write(conn), "excel.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/empPaymentClc")
    public Response getEmpPaymentClc(EmpPaymentRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = PersonalReport.getEmpPaymentClc(req.payMonth, req.qna1, req.invertNames, req.authOffices, req.enterpriseId, req.cityId, req.employeerId,
                    req.sbareaId, req.salesman, req.areaId, req.posId, req.officeId, req.employeerName, req.enterpriseName, req.cityName,
                    req.areaName, req.sbAreaName, req.titleArea, req.titleSbArea, conn);
            return createResponse(rep.write(conn), "excel.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public static File getHv(int perEmpId, boolean limit, Connection conn) throws Exception {

        File f = File.createTempFile("hojaDeVida", ".pdf");

        Color colorBackground = new Color(255, 248, 248);
        Color colorBorder = new Color(250, 69, 30);

        GridResultsPdf pdf = new GridResultsPdf(f, colorBackground, colorBorder);

        pdf.addDocumentTitle("Hoja de Vida");

        Integer empId = new MySQLQuery("SELECT id FROM employee e WHERE e.per_employee_id = ?1").setParam(1, perEmpId).getAsInteger(conn);

        pdf.addPhotho(conn, perEmpId, EMPLOYEE_PHOTO);

        GridResult r = new GridResult();

        r.data = new MySQLQuery("SELECT pe.document, pe.first_name, pe.last_name, pe.num_hist, pe.b_date, pe.b_city, pe.phones, pe.mail, pe.address, pe.neigh, pe.cur_city, pe.gender, pe.sc_level, pp.name "
                + "FROM per_employee pe "
                + "LEFT JOIN per_profession pp ON pp.id = pe.profession_id "
                + "WHERE pe.id = ?1").setParam(1, perEmpId).getRecords(conn);

        if (r.data != null && r.data.length > 0) {
            r.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Documento"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Nombres"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Apellidos"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Núm. Historia"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Nacimiento"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Nacio en"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Teléfono"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Correo"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Dirección"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Barrio"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Ciudad"),
                new MySQLCol(MySQLCol.TYPE_ENUM, 20, "Género", new PerEmployee().getEnumOptionsAsMatrix("gender")),
                new MySQLCol(MySQLCol.TYPE_ENUM, 20, "Escolaridad", new PerEmployee().getEnumOptionsAsMatrix("sc_level")),
                new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Profesión")};

            pdf.addVerticalGrid("Datos del Empleado", r);
        }

        r = new GridResult();
        r.data = new MySQLQuery("SELECT ctr.beg_date, ctr.end_date, per_employeer.name, ctr.leave_date, pos.name, sb.name "
                + "FROM per_contract ctr "
                + "INNER JOIN per_pos pos on pos.id = ctr.pos_id "
                + "INNER JOIN per_sbarea AS sb ON sb.id = pos.sarea_id "
                + "LEFT JOIN per_employeer ON per_employeer.id = ctr.employeer_id "
                + "WHERE ctr.emp_id = ?1 ORDER BY ctr.beg_date DESC "
                + (limit ? " LIMIT 15" : "")).setParam(1, perEmpId).getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 10, "Inicio"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 10, "Fin"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 25, "Empleador"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 10, "Retiro"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 25, "Cargo"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 25, "Área")
        };

        pdf.addGrid("Contratos", r);

        r = new GridResult();
        r.data = new MySQLQuery("SELECT v.internal, v.plate, dv.`begin`, dv.`end`, vt.`name` "
                + "FROM driver_vehicle AS dv "
                + "INNER JOIN vehicle AS v ON dv.vehicle_id = v.id AND dv.driver_id = ?1 "
                + "INNER JOIN vehicle_type AS vt ON v.vehicle_type_id = vt.id "
                + "INNER JOIN employee AS emp ON emp.id = dv.driver_id "
                + (limit ? " LIMIT 15" : "")).setParam(1, empId).getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Interno"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Placa"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 20, "Inicio"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 20, "Fin"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Tipo")
        };

        pdf.addGrid("Asignación de Vehículos", r);

        r = new GridResult();
        r.data = new MySQLQuery("SELECT CONCAT(e.first_name,' ',e.last_name), a.rev_date, a.detail, IF(a.check_date IS NOT NULL, 'Si', 'No') "
                + "FROM per_chk_advice a "
                + "INNER JOIN per_employee e ON e.id = a.emp_id "
                + (limit ? " LIMIT 15" : "")).getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Empleado"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 10, "Seguimiento"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Detalle"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 10, "Ejecutada")};

        pdf.addGrid("Novedad Clínica", r);

        r = new GridResult();
        r.data = new MySQLQuery("SELECT a.reg_date, c.`name`, a.days, a.ext_days, a.loaded_days, sum(dp.ded_days) "
                + "FROM per_accident AS a "
                + "INNER JOIN per_cause AS c ON c.id = a.cause_id "
                + "LEFT JOIN per_ded_plan AS dp ON a.id = dp.cause_id "
                + "WHERE a.active = 1 AND a.emp_id = ?1 "
                + "GROUP BY a.id ORDER BY a.reg_date DESC"
                + (limit ? " LIMIT 15" : "")).setParam(1, perEmpId).getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 10, "Fecha"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 40, "Causal"),
            new MySQLCol(MySQLCol.TYPE_INTEGER, 10, "Incapacidad"),
            new MySQLCol(MySQLCol.TYPE_INTEGER, 10, "Prorroga"),
            new MySQLCol(MySQLCol.TYPE_INTEGER, 10, "Cargados"),
            new MySQLCol(MySQLCol.TYPE_INTEGER, 10, "Deducción")
        };

        pdf.addGrid("Accidentes", r);

        r = new GridResult();
        r.data = new MySQLQuery("SELECT sl.reg_date, sl.end_date, c.`name`, sl.days, sl.ext_days, sum(dp.ded_days) "
                + "FROM per_sick_leave AS sl "
                + "INNER JOIN per_cause AS c ON c.id = sl.cause_id "
                + "LEFT JOIN per_ded_plan AS dp ON sl.id = dp.cause_id "
                + "WHERE sl.active = 1 AND sl.emp_id = ?1 "
                + "GROUP BY sl.id ORDER BY sl.reg_date DESC"
                + (limit ? " LIMIT 15" : "")).setParam(1, perEmpId).getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 10, "Registro"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 10, "Fin"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 40, "Causal"),
            new MySQLCol(MySQLCol.TYPE_INTEGER, 10, "Dias"),
            new MySQLCol(MySQLCol.TYPE_INTEGER, 10, "Prorroga"),
            new MySQLCol(MySQLCol.TYPE_INTEGER, 10, "Deducción")
        };

        pdf.addGrid("Incapacidades", r);

        r = new GridResult();
        r.data = new MySQLQuery("SELECT li.beg_date, li.end_date, c.`name`, sum(dp.ded_days) "
                + "FROM per_licence AS li "
                + "INNER JOIN per_cause AS c ON c.id = li.cause_id "
                + "LEFT JOIN per_ded_plan AS dp ON li.id = dp.cause_id "
                + "WHERE li.active = 1 AND li.emp_id = ?1 "
                + "GROUP BY li.id ORDER BY li.beg_date DESC"
                + (limit ? " LIMIT 15" : "")).setParam(1, perEmpId).getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 10, "Inicio"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 10, "Fin"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 40, "Causal"),
            new MySQLCol(MySQLCol.TYPE_INTEGER, 10, "Deducción")
        };

        pdf.addGrid("Permisos", r);

        r = new GridResult();
        r.data = new MySQLQuery("SELECT pw.reg_date, c.name, IF(pw.active, 'Si', 'No') "
                + "FROM per_warning pw "
                + "INNER JOIN per_cause AS c ON c.id = pw.cause_id "
                + "WHERE pw.active = 1 AND pw.emp_id = ?1 "
                + (limit ? " LIMIT 15" : "")).setParam(1, perEmpId).getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 10, "Registro"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 40, "Causal"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 10, "Deducción")
        };

        pdf.addGrid("Llamados de Atención", r);

        r = new GridResult();
        r.data = new MySQLQuery("SELECT reg_date, c.name, days, sum(dp.ded_days), p.begin_date,p.end_date "
                + "FROM per_penalty p "
                + "INNER JOIN per_cause c ON p.cause_id = c.id "
                + "LEFT JOIN per_ded_plan AS dp ON p.id = dp.cause_id "
                + "WHERE p.active = 1 AND p.emp_id= ?1 "
                + "GROUP BY p.id ORDER BY reg_date DESC"
                + (limit ? " LIMIT 15" : "")).setParam(1, perEmpId).getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 10, "Registro"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 40, "Causal"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 10, "Dias"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 10, "Deducción"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 10, "Inicio"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 10, "Fin")
        };

        pdf.addGrid("Sanciones", r);

        r = new GridResult();
        r.data = new MySQLQuery("SELECT a.reg_date, c.`name`,a.eval_date, IF(a.done, 'Si', 'No') "
                + "FROM per_agreement AS a "
                + "INNER JOIN per_cause AS c ON c.id = a.cause_id "
                + "WHERE a.active = 1 AND emp_id = ?1 "
                + "ORDER BY reg_date DESC"
                + (limit ? " LIMIT 15" : "")).setParam(1, perEmpId).getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 10, "Registro"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 40, "Causal"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 10, "Evaluación"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 10, "Cumplió")
        };

        pdf.addGrid("Compromisos", r);

        r = new GridResult();
        r.data = new MySQLQuery("SELECT pn.note_date, pn.rev_date, per_cause.name, pn.description "
                + "FROM per_emp_note pn "
                + "INNER JOIN per_cause ON per_cause.id = pn.cause_id "
                + "WHERE pn.emp_id = ?1 "
                + (limit ? " LIMIT 15" : "")).setParam(1, perEmpId).getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 10, "Registro"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 10, "Revisada"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 40, "Causal"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 40, "Detalle")
        };

        pdf.addGrid("Novedades Laborales", r);

        pdf.close();
        return f;
    }

}
