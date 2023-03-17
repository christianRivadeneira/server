package web.helpdesk;

import java.io.IOException;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import service.MySQL.MySQLSelect;
import utilities.Dates;
import utilities.JsonUtils;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "HelpDeskV2", urlPatterns = {"/HelpDeskV2"})
public class HelpDeskV2 extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
        boolean isNormUser;
        boolean isSupport;

        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            JsonObjectBuilder ob = Json.createObjectBuilder();
            JsonObject req = MySQLQuery.scapeJsonObj(request);
            String poolName = req.getString("pool_name");
            String tz = JsonUtils.getString(req, "tz");
            conn = MySQLCommon.getConnection((poolName != null ? poolName : "sigmads"), (tz != null ? tz : null));

            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");

            isNormUser = req.getString("is_normal").equals("true");
            isSupport = req.getString("is_support").equals("true");

            String msg = "consulta realizada con exito";
            String header = req.getString("header");
            try {
                switch (header) {
                    case "countCases":
                        ob.add("countCases", countCases(conn, req, isNormUser, isSupport));
                        break;
                    case "listRequest":
                        ob.add("listRequest", listRequest(conn, req, isNormUser, isSupport));
                        break;
                    case "findByIdRequest":
                        ob.add("findByIdRequest", findByIdRequest(conn, req));
                        break;
                    case "crud":
                        ob.add("crud", crud(conn, req, poolName, tz));
                        break;
                    case "typeRequest":
                        ob.add("typeRequest", typeRequest(conn, req, isNormUser));
                        break;
                    case "employeeSolicited":
                        ob.add("employeeSolicited", employeeSolicited(conn, req));
                        break;
                    case "employeeInCharge":
                        ob.add("employeeInCharge", employeeInCharge(conn, req));
                        break;
                    case "reviewState":
                        ob.add("reviewState", reviewState(conn, req));
                        break;
                    case "approveRefuse":
                        ob.add("approveRefuse", approveRefuse(conn, req));
                        break;
                    case "requestByState":
                        ob.add("listRequest", requestByState(conn, req));
                        break;
                    case "reasign":
                        ob.add("reasign", reasing(conn, req, poolName, tz));
                        break;
                    case "sendReview":
                        ob.add("sendReview", sendReview(conn, req));
                        break;
                    case "returnReview":
                        ob.add("returnReview", returnReview(conn, req));
                        break;
                    case "closeCase":
                        ob.add("closeCase", closeCase(conn, req));
                        break;

                    case "cancelSuscrib":
                        ob.add("cancelSuscrib", cancelSuscrib(conn, req));
                        break;

                    case "registToken":
                        ob.add("status", registToken(conn, req));
                        break;
                    default:
                        msg = "no se encontro header";
                        break;
                }
                ob.add("msg", msg);
                ob.add("result", "OK");
            } catch (Exception ex) {
                Logger.getLogger(HelpDeskV2.class.getName()).log(Level.SEVERE, null, ex);
                ob.add("result", "ERROR");
                String m = ex.getMessage();
                if (m != null && !m.isEmpty()) {
                    ob.add("errorMsg", m);
                } else {
                    ob.add("errorMsg", "Error desconocido.");
                }
            } finally {
                w.writeObject(ob.build());
            }
        } catch (Exception ex) {
            Logger.getLogger(HelpDeskV2.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySQLSelect.tryClose(conn);
        }

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

    private JsonArrayBuilder requestByState(Connection conn, JsonObject req) throws Exception {

        int idEmployee = req.getInt("employee_id");
        String state = req.getString("state");
        if (req.get("state") != null) {
            state = req.getString("state");
            state = state == null || state.isEmpty() ? "" : " AND r.state = '" + state + "' ";
        }
        String query = "SELECT "
                + "r.id , r.subject, r.notes,r.state,CONCAT(e.first_name , ' ' ,e.last_name), e.id "
                + "FROM hlp_request r "
                + "INNER JOIN hlp_type t ON t.id = r.type_id AND t.is_administrative = 1 "
                + "LEFT JOIN per_employee pe ON pe.id = r.in_charge "
                + "INNER JOIN employee e ON e.id = r.employee_id AND e.id = " + idEmployee + " "
                + " "
                + state
                + " ORDER BY r.id DESC";
        Object[][] res = new MySQLQuery(query).getRecords(conn);
        JsonArrayBuilder jsonCase = Json.createArrayBuilder();

        for (Object[] data : res) {
            JsonObjectBuilder row = Json.createObjectBuilder();
            row.add("id", MySQLQuery.getAsInteger(data[0]));
            row.add("subject", MySQLQuery.getAsString(data[1]));
            row.add("notes", MySQLQuery.getAsString(data[2]));
            row.add("state", MySQLQuery.getAsString(data[3]));
            row.add("employeeSolicited", MySQLQuery.getAsString(data[4]));
            row.add("employeeSolicitedId", MySQLQuery.getAsInteger(data[5]));
            jsonCase.add(row);
        }
        return jsonCase;
    }

    private JsonArrayBuilder typeRequest(Connection conn, JsonObject req, boolean isNormal) throws Exception {
        int employeeId = req.getInt("employee_id");
        String query = "";

        if (isNormal) {
            query = "SELECT t.id, t.name "
                    + "FROM hlp_type t "
                    + "WHERE show_in_desktop ";
        } else {
            query = "SELECT DISTINCT t.id, name "
                    + "FROM hlp_type t "
                    + "INNER JOIN per_employee emp ON emp.id = t.emp_id AND emp.active "
                    + "INNER JOIN employee e ON e.per_employee_id = emp.id AND e.active "
                    + "LEFT JOIN hlp_sup_emp inc ON inc.type_id = t.id "
                    + "WHERE e.id = " + employeeId + " OR (inc.employee_id = " + employeeId + " AND inc.active) "
                    + "ORDER BY name ASC";
        }
        
        Object[][] res = new MySQLQuery(query).getRecords(conn);
        JsonArrayBuilder jsonCase = Json.createArrayBuilder();
        for (Object[] data : res) {
            JsonObjectBuilder row = Json.createObjectBuilder();
            row.add("id", MySQLQuery.getAsInteger(data[0]));
            row.add("name", MySQLQuery.getAsString(data[1]));
            jsonCase.add(row);
        }
        return jsonCase;
    }

    private JsonArrayBuilder employeeInCharge(Connection conn, JsonObject req) throws Exception {
        int typeRequest = req.getInt("typeRequest");
        String query = "SELECT p.id, CONCAT(p.first_name, ' ',p.last_name) "
                + "FROM hlp_sup_emp h "
                + "INNER JOIN hlp_type t ON t.id = h.type_id  AND t.id = " + typeRequest + " "
                + "INNER JOIN per_employee p ON p.id = h.emp_id "
                + "INNER JOIN employee e ON e.per_employee_id = p.id AND e.active = 1 "
                + "INNER JOIN per_contract pc ON pc.emp_id = p.id AND pc.`last` = 1 "
                + "ORDER BY p.first_name ASC ";
        Object[][] res = new MySQLQuery(query).getRecords(conn);
        JsonArrayBuilder jsonCase = Json.createArrayBuilder();

        for (Object[] data : res) {
            JsonObjectBuilder row = Json.createObjectBuilder();
            row.add("id", MySQLQuery.getAsInteger(data[0]));
            row.add("name", MySQLQuery.getAsString(data[1]));
            jsonCase.add(row);
        }
        return jsonCase;
    }

    private JsonArrayBuilder reviewState(Connection conn, JsonObject req) throws Exception {
        String query = "SELECT r.id, r.name, r.active FROM hlp_review_state r WHERE  r.active = 1 ORDER BY r.name ASC ";
        Object[][] res = new MySQLQuery(query).getRecords(conn);
        JsonArrayBuilder jsonCase = Json.createArrayBuilder();
        for (Object[] data : res) {
            JsonObjectBuilder row = Json.createObjectBuilder();
            row.add("id", MySQLQuery.getAsInteger(data[0]));
            row.add("name", MySQLQuery.getAsString(data[1]));
            row.add("active", MySQLQuery.getAsString(data[2]));
            jsonCase.add(row);
        }
        return jsonCase;
    }

    private JsonArrayBuilder employeeSolicited(Connection conn, JsonObject req) throws Exception {
        String keysSync = req.getString("keys");
        String[] keys = keysSync.split(":");
        String typeUser = req.getString(keys[0]);
        String query = null;
        switch (typeUser) {
            case "emp":
                query = "SELECT emp.id , CONCAT(emp.first_name, ' ',emp.last_name) "
                        + "FROM employee emp "
                        + "WHERE emp.active = 1 "
                        + "ORDER BY emp.first_name ASC ";
                break;

            case "thi":
                query = "SELECT t.id , CONCAT(t.first_name, ' ',t.last_name) "
                        + "FROM hlp_per_third t "
                        + "ORDER BY t.first_name ASC ";
                break;
            case "cli":
                query = "SELECT id, name  FROM crm_client WHERE active = 1 AND type LIKE 'client' ORDER BY name  ASC ";
                break;
        }
        Object[][] res = new MySQLQuery(query).getRecords(conn);
        JsonArrayBuilder jsonCase = Json.createArrayBuilder();
        for (Object[] data : res) {
            JsonObjectBuilder row = Json.createObjectBuilder();
            row.add("id", MySQLQuery.getAsInteger(data[0]));
            row.add("name", MySQLQuery.getAsString(data[1]));
            row.add("typeUser", typeUser);
            jsonCase.add(row);
        }

        return jsonCase;
    }

    private JsonArrayBuilder crud(Connection conn, JsonObject req, String poolName, String tz) throws Exception {
        String typeCrud = req.getString("typeCrud");

        int idEmployee = req.getInt("employee_id");
        String subject = req.getString("subject");
        String notes = req.getString("notes");
        String state = req.getString("state");
        String priority = req.getString("priority");
        int inCharge = req.getInt("employeeInCharge");
        String userType = req.getString("typeUser");
        String expiDateString = req.getString("expirateDate");
        int idTypeUser = req.getInt("employeeSolicited");
        int idTypeRequest = req.getInt("typeRequest");
        Date expiDate = (expiDateString.equals("") ? null : formatDate(expiDateString));
        JsonArrayBuilder jsonCase = Json.createArrayBuilder();

        Integer inChargeEmp = new MySQLQuery("SELECT id FROM employee WHERE per_employee_id = " + inCharge + " AND active ").getAsInteger(conn);

        switch (typeCrud) {
            case "update":
                int idRequest = req.getInt("requestId");
                String solution = req.getString("solution");

                String typeUserQ = (userType.equals("emp") ? "r.employee_id = " : userType.equals("thi") ? "r.per_third_id = " : userType.equals("cli") ? "r.crm_cli_id = " : null);
                String query = "UPDATE hlp_request r "
                        + "INNER JOIN hlp_type t ON t.id = r.type_id "
                        + ""
                        + "SET r.subject = '" + subject + "' ,  "
                        + "r.notes = '" + notes + "', "
                        + "r.state = '" + state + "' , "
                        + "r.priority = '" + priority + "' , "
                        + "r.employee_id = null , r.per_third_id = null , r.crm_cli_id = null , "
                        + typeUserQ + idTypeUser + " , "
                        + "r.in_charge = " + inCharge + " , "
                        + "r.in_charge_emp = " + inChargeEmp + " , "
                        + "r.user_type = '" + userType + "', "
                        + "r.solution = '" + solution + "', "
                        + "r.type_id = " + idTypeRequest + ", "
                        + "r.expirate_date = ?1 "
                        + "WHERE r.id = " + idRequest;
                new MySQLQuery(query).setParam(1, expiDate).executeUpdate(conn);

                //actualizar suscriptor
                updateSuscriptor(true, idRequest, idEmployee, inCharge, conn);

                //crear el registro de flujo
                Integer idEmployeeNextPerson = idEmployeeByPerEmployee(inCharge, conn);
                Integer idEmployeePreviosPerson = userType.equals("emp") ? idTypeUser : null;
                insertFlowCheck("Reasignado", idRequest, idEmployee, null, state, idEmployeePreviosPerson, idEmployeeNextPerson, conn);

                if (state.equals("new")) {
                    sendPush(subject, MySQLQuery.getAsString(idRequest), MySQLQuery.getAsString(inChargeEmp), notes, conn, poolName, tz);
                }

                break;

            case "insert":
                typeUserQ = (userType.equals("emp") ? "employee_id " : userType.equals("thi") ? "per_third_id " : userType.equals("cli") ? "crm_cli_id " : null);
                query = "INSERT INTO hlp_request "
                        + "(subject, notes, state, " + typeUserQ + ", priority, "
                        + " in_charge, in_charge_emp, type_id, user_type, created_by ,  reg_date, beg_date,expirate_date) "
                        + "VALUES "
                        + "('" + subject + "','" + notes + "','" + state + "'," + idTypeUser + ",'" + priority + "', "
                        + inCharge + "," + inChargeEmp + "," + idTypeRequest + ",'" + userType + "' , " + idEmployee + ", ?1 , ?2, ?3 );";
                idRequest = new MySQLQuery(query)
                        .setParam(1, new Date())
                        .setParam(2, new Date())
                        .setParam(3, expiDate)
                        .executeInsert(conn);

                //actualizar suscriptor
                updateSuscriptor(true, idRequest, idEmployee, inCharge, conn);

                //crear el registro de flujo
                idEmployeeNextPerson = idEmployeeByPerEmployee(inCharge, conn);
                idEmployeePreviosPerson = userType.equals("emp") ? idTypeUser : null;
                insertFlowCheck("Apertura", idRequest, idEmployee, null, "new", idEmployeePreviosPerson, idEmployeeNextPerson, conn);
                insertFlowCheck("Asignacion", idRequest, idEmployee, null, state, idEmployeePreviosPerson, idEmployeeNextPerson, conn);

                jsonCase = Json.createArrayBuilder();
                JsonObjectBuilder row = Json.createObjectBuilder();
                row.add("id", idRequest);
                jsonCase.add(row);

                if (!state.equals("plan")) {
                    sendPush(subject, MySQLQuery.getAsString(idRequest), MySQLQuery.getAsString(inChargeEmp), notes, conn, poolName, tz);
                }
                break;
        }
        return jsonCase;
    }

    public void sendPush(String subject, String idRequest, String inChargeEmp, String notes, Connection conn, String poolName, String tz) throws Exception {
        int appId = new MySQLQuery("SELECT id FROM system_app WHERE package_name = 'com.qualisys.helpdesk'").getAsInteger(conn);
        JsonObjectBuilder ob = Json.createObjectBuilder();
        ob.add("subject", subject);
        ob.add("message", "Le fue asignado el caso No. " + idRequest + " con las siguientes caracteristicas:\n\n" + notes);
        ob.add("user", "MESA DE AYUDA");
        ob.add("dt", Dates.getCheckFormat().format(new Date()));

        GCMHlp.sendToHlpApp(appId, ob.build(), poolName, tz, MySQLQuery.getAsString(inChargeEmp));

    }

    private JsonArrayBuilder findByIdRequest(Connection conn, JsonObject req) throws Exception {
        int idRequest = req.getInt("requestId");

        String query = "SELECT "
                + "r.id , r.subject, IFNULL(r.notes,''), r.state, "
                + ""
                + "CASE r.user_type "
                + "WHEN 'emp' THEN CONCAT(e.first_name,' ',e.last_name) "
                + "WHEN 'thi' THEN CONCAT(pt.first_name,' ',pt.last_name) "
                + "WHEN 'cli' THEN COALESCE(cr.short_name, cr.name) END , "
                + " "
                + "CASE r.user_type "
                + "WHEN 'emp' THEN e.id "
                + "WHEN 'thi' THEN pt.id "
                + "WHEN 'cli' THEN cr.id END , "
                + ""
                + "r.priority, CONCAT(pe.first_name, ' ',pe.last_name),pe.id, t.name, t.id, r.user_type , "
                + "IF(r.expirate_date IS NULL ,'', r.expirate_date), "
                + "CONCAT(creator.first_name , ' ' ,creator.last_name),creator.id, "
                + "IF(r.solution IS NULL,'', r.solution),"
                + "r.expirate_date, "
                + "r.in_charge_emp, "
                + "r.reg_date "
                + "FROM hlp_request r "
                + "INNER JOIN hlp_type t ON t.id = r.type_id "
                + "LEFT JOIN per_employee pe ON pe.id = r.in_charge "
                + "LEFT JOIN employee e ON e.id = r.employee_id "
                + "LEFT JOIN hlp_per_third pt ON pt.id = r.per_third_id "
                + "LEFT JOIN crm_client cr ON cr.id = r.crm_cli_id "
                + "LEFT JOIN employee emcrm ON emcrm.id = cr.sales_employee_id "
                + "INNER JOIN employee creator ON creator.id = r.created_by "
                + "WHERE r.id = " + idRequest;

        Object[][] res = new MySQLQuery(query).getRecords(conn);
        JsonArrayBuilder jsonCase = Json.createArrayBuilder();

        for (Object[] data : res) {
            JsonObjectBuilder row = Json.createObjectBuilder();
            row.add("id", MySQLQuery.getAsInteger(data[0]));
            row.add("subject", MySQLQuery.getAsString(data[1]));
            row.add("notes", MySQLQuery.getAsString(data[2]));
            row.add("state", MySQLQuery.getAsString(data[3]));
            row.add("employeeSolicited", MySQLQuery.getAsString(data[4]));
            row.add("employeeSolicitedId", MySQLQuery.getAsInteger(data[5]));
            row.add("priority", data[6] != null ? MySQLQuery.getAsString(data[6]) : "med");
            row.add("inCharge", data[7] != null ? MySQLQuery.getAsString(data[7]) : "Sin Encargado");
            addInt(row, "inChargeId", MySQLQuery.getAsInteger(data[8]));
            row.add("type", MySQLQuery.getAsString(data[9]));
            row.add("typeId", MySQLQuery.getAsInteger(data[10]));
            row.add("typeUser", MySQLQuery.getAsString(data[11]));
            row.add("expirateDate", MySQLQuery.getAsString(data[12]));
            row.add("creator", MySQLQuery.getAsString(data[13]));
            row.add("creatorId", MySQLQuery.getAsInteger(data[14]));
            row.add("solution", MySQLQuery.getAsString(data[15]));
            row.add("expirateDate", data[16] != null ? MySQLQuery.getAsString(data[16]) : "NULL");
            addInt(row, "inChargeEmp", MySQLQuery.getAsInteger(data[17]));
            row.add("regDate", MySQLQuery.getAsString(data[18]));
            jsonCase.add(row);
        }
        return jsonCase;
    }

    private JsonArrayBuilder countCases(Connection conn, JsonObject req, boolean isNormal, boolean isSupport) throws Exception {
        int employeeId = req.getInt("employee_id");

        Object[][] dataTypes = new MySQLQuery("SELECT t.id, e.id  "
                + "FROM hlp_type t INNER JOIN employee e ON e.per_employee_id = t.emp_id  "
                + (!isNormal && !isSupport ? "WHERE e.id = " + employeeId : "")).getRecords(conn);
        String columns = " COUNT(*) ";
        String typeUser = "(r.employee_id IS NOT NULL OR r.per_third_id IS NOT NULL OR r.crm_cli_id IS NOT NULL) ";
        List<String> othersTypes = new ArrayList<>(Arrays.asList("suscribed", "alerts", "running", "cases", "clos"));

        //CASOS PENDIENTES
        Object[][] res = getData(dataTypes, columns, "cases", othersTypes, employeeId, null, typeUser, conn, (!isNormal ? "pend" : null), isNormal, isSupport);

        JsonArrayBuilder jsonCase = Json.createArrayBuilder();
        int count = 0;
        for (Object[] data : res) {
            count += MySQLQuery.getAsInteger(data[0]);
        }
        if (!isNormal) {
            res = getData(dataTypes, columns, "cases", othersTypes, employeeId, null, typeUser, conn, (!isNormal ? "entry" : null), isNormal, isSupport);
            for (Object[] data : res) {
                count += MySQLQuery.getAsInteger(data[0]);
            }
            res = getData(dataTypes, columns, "cases", othersTypes, employeeId, null, typeUser, conn, (!isNormal ? "prog" : null), isNormal, isSupport);
            for (Object[] data : res) {
                count += MySQLQuery.getAsInteger(data[0]);
            }
        }

        JsonObjectBuilder rowCases = Json.createObjectBuilder();
        rowCases.add("count", count);
        rowCases.add("type", "TAG_PNL_CASES");
        jsonCase.add(rowCases);

        //CASOS CERRADOS
        res = getData(dataTypes, columns, "clos", othersTypes, employeeId, null, typeUser, conn, null, isNormal, isSupport);
        count = 0;
        for (Object[] data : res) {
            count += MySQLQuery.getAsInteger(data[0]);
        }

        JsonObjectBuilder rowClos = Json.createObjectBuilder();
        rowClos.add("count", count);
        rowClos.add("type", "TAG_PNL_CLOSES");
        jsonCase.add(rowClos);

        //CASOS SUSCRITO
        res = getData(dataTypes, columns, "suscribed", othersTypes, employeeId, null, typeUser, conn, null, isNormal, isSupport);
        count = 0;
        for (Object[] data : res) {
            count += MySQLQuery.getAsInteger(data[0]);
        }

        JsonObjectBuilder rowSus = Json.createObjectBuilder();
        rowSus.add("count", count);
        rowSus.add("type", "TAG_PNL_SUBSCRIBED");
        jsonCase.add(rowSus);

        //CASOS ALERTAS
        res = getData(dataTypes, columns, "alerts", othersTypes, employeeId, null, typeUser, conn, null, isNormal, isSupport);
        count = 0;
        for (Object[] data : res) {
            count += MySQLQuery.getAsInteger(data[0]);
        }

        JsonObjectBuilder rowAlerts = Json.createObjectBuilder();
        rowAlerts.add("count", count);
        rowAlerts.add("type", "TAG_PNL_ALERTS");
        jsonCase.add(rowAlerts);

        return jsonCase;
    }

    private JsonArrayBuilder listRequest(Connection conn, JsonObject req, boolean isNormal, boolean isSupport) throws Exception {
        int employeeId = req.getInt("employee_id");
        String filterType = req.getString("filterType");

        Object[][] dataTypes = new MySQLQuery("SELECT t.id, e.id  "
                + "FROM hlp_type t INNER JOIN employee e ON e.per_employee_id = t.emp_id  "
                + (!isNormal && !isSupport ? "WHERE e.id = " + employeeId : "")).getRecords(conn);
        String columns = "r.id , r.subject, IFNULL(r.notes,''),r.state, IF(emp.id IS NULL, 'Sin Encargado', CONCAT(emp.first_name , ' ' ,emp.last_name)), created_by, r.type_id, r.in_charge ";
        String orderBy = " ORDER BY r.id DESC ";
        String typeUser = "(r.employee_id IS NOT NULL OR r.per_third_id IS NOT NULL OR r.crm_cli_id IS NOT NULL) ";
        List<String> othersTypes = new ArrayList<>(Arrays.asList("suscribed", "alerts", "running", "cases", "clos"));
        String state = "";
        if (req.get("state") != null) {
            state = req.getString("state");
            state = state == null ? "" : state;
            if (state.equals("all") || !othersTypes.contains(state)) {
                state = state.equals("all") ? " AND r.state <> 'clos' " : " AND r.state = '" + state + "' ";
            }
        }

        JsonArrayBuilder jsonCase = Json.createArrayBuilder();
        Object[][] res = getData(dataTypes, columns, state, othersTypes, employeeId, orderBy, typeUser, conn, filterType, isNormal, isSupport);
        for (Object[] data : res) {
            JsonObjectBuilder row = Json.createObjectBuilder();
            row.add("id", MySQLQuery.getAsInteger(data[0]));
            row.add("subject", MySQLQuery.getAsString(data[1]));
            row.add("notes", MySQLQuery.getAsString(data[2]));
            row.add("state", MySQLQuery.getAsString(data[3]));
            row.add("creator", MySQLQuery.getAsString(data[4]));
            addInt(row, "creatorId", MySQLQuery.getAsInteger(data[5]));
            row.add("typeRequest", MySQLQuery.getAsInteger(data[6]));
            addInt(row, "inChargeId", MySQLQuery.getAsInteger(data[7]));
            jsonCase.add(row);
        }

        return jsonCase;
    }

    private JsonArrayBuilder approveRefuse(Connection conn, JsonObject req) throws Exception {
        String keysSync = req.getString("keys");
        String[] keys = keysSync.split(":");
        int caseId = req.getInt(keys[0]);
        int employeeId = req.getInt(keys[1]);
        int previousInchargeId = req.getInt(keys[2]);
        String state = req.getString(keys[3]);
        String notes = req.getString(keys[4]);

        String q = "";
        if (state.equals("reje")) {
            Object[] objSpan = new MySQLQuery("SELECT id, `end_date` FROM hlp_span_request sr WHERE sr.case_id = " + caseId + " ORDER BY sr.id DESC LIMIT 1 ").getRecord(conn);

            if (objSpan != null && objSpan[1] == null) {
                q = "UPDATE hlp_span_request "
                        + "SET incharge_id = " + previousInchargeId + " , "
                        + "last = 1 , "
                        + "end_date = NOW() "
                        + "WHERE id = " + MySQLQuery.getAsInteger(objSpan[0]) + " ";
                new MySQLQuery(q).executeUpdate(conn);//updateSpan

                q = "INSERT INTO hlp_span_request "
                        + "SET case_id =" + caseId + " , "
                        + "last = 1 ,"
                        + "reg_date = NOW() ,"
                        + "incharge_id = " + previousInchargeId + "  "
                        + "";
                new MySQLQuery(q).executeInsert(conn);//insertSpan
            } else {
                q = "INSERT INTO hlp_span_request "
                        + "SET case_id =" + caseId + " , "
                        + "last = 1 ,"
                        + "reg_date = NOW() ,"
                        + "incharge_id = " + previousInchargeId + "  "
                        + "";
                new MySQLQuery(q).executeInsert(conn);//insertSpan
            }
        }

        q = "INSERT INTO hlp_flow_check "
                + "SET step = '" + (state.equals("reje") ? getStep("reje") : getStep("acep")) + "', "
                + "case_id = " + caseId + " , "
                + "per_current = " + employeeId + " , "
                + "reg_date = NOW() ,"
                + "state = '" + state + "' , "
                + "notes = '" + notes + "' ";
        new MySQLQuery(q).executeInsert(conn);//insertFlow

        q = "UPDATE hlp_request "
                + "SET state = '" + (state.equals("reje") ? "reje" : "clos") + "' "
                + (state.equals("reje") ? ", running = 1 " : "")
                + (state.equals("clos") ? ", close_date = NOW(), end_date = NOW() " : "")
                + "WHERE id = " + caseId + "";
        new MySQLQuery(q).executeUpdate(conn);

        JsonArrayBuilder jsonCase = Json.createArrayBuilder();
        return jsonCase;
    }

    private JsonArrayBuilder sendReview(Connection conn, JsonObject req) throws Exception {
        String keysSync = req.getString("keys");
        String[] keys = keysSync.split(":");
        int caseId = req.getInt(keys[0]);
        int employeeId = req.getInt(keys[1]);
        int previousInchargeId = req.getInt(keys[2]);
        int nextInChargeId = req.getInt(keys[3]);
        int reviewStateId = req.getInt(keys[4]);
        String notes = req.getString(keys[5]);
        Integer nextInChargeIdEmp = idEmployeeByPerEmployee(nextInChargeId, conn);

        Object[] objSpan = new MySQLQuery(" SELECT id, `end_date` "
                + "FROM hlp_span_request sr WHERE sr.case_id = " + caseId + " "
                + "ORDER BY sr.id DESC LIMIT 1 ").getRecord(conn);
        String q = "";
        if (objSpan != null && objSpan[1] == null) {
            q = "UPDATE hlp_span_request "
                    + "SET incharge_id = " + previousInchargeId + " , "
                    + "last = 1 , "
                    + "review_state_id = " + reviewStateId + " , "
                    + "end_date = NOW() "
                    + "WHERE id = " + MySQLQuery.getAsInteger(objSpan[0]);
            new MySQLQuery(q).executeUpdate(conn);//insertSpan
        } else {
            q = "INSERT INTO hlp_span_request "
                    + "SET case_id =" + caseId + " , "
                    + "last = 1 ,"
                    + "reg_date = NOW() ,"
                    + "end_date = NOW(), "
                    + "incharge_id = " + previousInchargeId + " , "
                    + "review_state_id = " + reviewStateId + " "
                    + "";
            new MySQLQuery(q).executeInsert(conn);//updateSpan
        }

        String nameState = new MySQLQuery("SELECT r.name FROM hlp_review_state r WHERE  r.id = " + reviewStateId).getAsString(conn);

        q = "INSERT INTO hlp_flow_check "
                + "SET step = '" + getStep("revi") + "', "
                + "previous_person = " + idEmployeeByPerEmployee(previousInchargeId, conn) + ", "
                + "next_person = " + idEmployeeByPerEmployee(nextInChargeId, conn) + ", "
                + "case_id = " + caseId + " , "
                + "per_current = " + employeeId + " , "
                + "reg_date = NOW() ,"
                + "state = 'revi' , "
                + "notes = '" + nameState + " - " + (notes) + "' ";
        new MySQLQuery(q).executeInsert(conn);//insertFlow

        q = "UPDATE hlp_request "
                + "SET state = 'revi' , "
                + "in_charge_emp = " + nextInChargeIdEmp + ","
                + "in_charge = " + nextInChargeId + ","
                + "running = 0 "
                + "WHERE id = " + caseId;
        new MySQLQuery(q).executeUpdate(conn);

        //actualizar suscripciones
        updateSuscriptor(true, caseId, idEmployeeByPerEmployee(previousInchargeId, conn), nextInChargeId, conn);

        JsonArrayBuilder jsonCase = Json.createArrayBuilder();
        return jsonCase;
    }

    private JsonArrayBuilder returnReview(Connection conn, JsonObject req) throws Exception {
        String keysSync = req.getString("keys");
        String[] keys = keysSync.split(":");
        int caseId = req.getInt(keys[0]);
        int employeeId = req.getInt(keys[1]);
        int previousInchargeId = req.getInt(keys[2]);
        String notes = req.getString(keys[3]);
        Integer nextInChargeId = new MySQLQuery("SELECT incharge_id FROM hlp_span_request WHERE case_id = " + caseId + " AND `last` ORDER BY id DESC LIMIT 1 ").getAsInteger(conn);
        Integer nextInChargeIdEmp = idEmployeeByPerEmployee(nextInChargeId, conn);

        Object[] objSpan = new MySQLQuery(" SELECT id, `end_date` "
                + "FROM hlp_span_request sr WHERE sr.case_id = " + caseId + " "
                + "ORDER BY sr.id DESC LIMIT 1 ").getRecord(conn);
        String q = "";
        if (objSpan != null && objSpan[1] == null) {
            q = "UPDATE hlp_span_request SET end_date = NOW() WHERE id = " + MySQLQuery.getAsInteger(objSpan[0]);
            new MySQLQuery(q).executeUpdate(conn);//insertSpan
        } else {
            q = "INSERT INTO hlp_span_request "
                    + "SET case_id =" + caseId + " , "
                    + "last = 1 ,"
                    + "reg_date = NOW() ,"
                    + "end_date = NOW(), "
                    + "incharge_id = " + previousInchargeId + "  "
                    + "";
            new MySQLQuery(q).executeInsert(conn);//updateSpan
        }

        q = "INSERT INTO hlp_flow_check "
                + "SET step = '" + getStep("reviReturn") + "', "
                + "previous_person = " + idEmployeeByPerEmployee(previousInchargeId, conn) + ", "
                + "next_person = " + idEmployeeByPerEmployee(nextInChargeId, conn) + ", "
                + "case_id = " + caseId + " , "
                + "per_current = " + employeeId + " , "
                + "reg_date = NOW() , "
                + "notes = '" + notes + "', "
                + "state = 'prog' ";
        new MySQLQuery(q).executeInsert(conn);//insertFlow

        q = "UPDATE hlp_request "
                + "SET state = 'prog' , "
                + "in_charge_emp = " + nextInChargeIdEmp + ","
                + "in_charge = " + nextInChargeId + ","
                + "running = 0 "
                + "WHERE id = " + caseId;
        new MySQLQuery(q).executeUpdate(conn);

        //actualizar suscripciones
        updateSuscriptor(true, caseId, idEmployeeByPerEmployee(previousInchargeId, conn), nextInChargeId, conn);

        JsonArrayBuilder jsonCase = Json.createArrayBuilder();
        return jsonCase;
    }

    private JsonArrayBuilder reasing(Connection conn, JsonObject req, String poolName, String tz) throws Exception {
        String keysSync = req.getString("keys");
        String[] keys = keysSync.split(":");
        int idRequest = req.getInt(keys[0]);
        int idEmployee = req.getInt(keys[1]);
        int inCharge = req.getInt(keys[2]);
        Boolean suscripted = req.getBoolean(keys[3]);

        Integer inChargeEmp = new MySQLQuery("SELECT id FROM employee WHERE per_employee_id = " + inCharge + " AND active ").getAsInteger(conn);
        //actualizar el request con el nuevo encargado
        String query = "UPDATE hlp_request r "
                + "SET r.in_charge = " + inCharge + ", r.in_charge_emp = " + inChargeEmp + " "
                + "WHERE r.id = " + idRequest;
        new MySQLQuery(query).executeUpdate(conn);

        //actualizar suscripciones
        updateSuscriptor(suscripted, idRequest, idEmployee, inCharge, conn);

        //crear el registro de flujo
        Integer idEmployeeNextPerson = idEmployeeByPerEmployee(inCharge, conn);
        insertFlowCheck("Reasignado", idRequest, idEmployee, null, null, null, idEmployeeNextPerson, conn);

        Object[] row = new MySQLQuery("SELECT subject, notes FROM hlp_request r WHERE r.id = " + idRequest).getRecord(conn);

        sendPush(MySQLQuery.getAsString(row[0]), MySQLQuery.getAsString(idRequest), MySQLQuery.getAsString(inChargeEmp), MySQLQuery.getAsString(row[1]), conn, poolName, tz);

        JsonArrayBuilder jsonCase = Json.createArrayBuilder();
        return jsonCase;
    }

    private JsonArrayBuilder closeCase(Connection conn, JsonObject req) throws Exception {
        String keysSync = req.getString("keys");
        String[] keys = keysSync.split(":");
        int idRequest = req.getInt(keys[0]);
        int idEmployee = req.getInt(keys[1]);

        String query = "UPDATE hlp_request r "
                + "SET r.state = 'clos', r.close_date = NOW(), r.end_date = NOW() "
                + "WHERE r.id = " + idRequest;
        new MySQLQuery(query).executeUpdate(conn);

        //crear el registro de flujo
        insertFlowCheck("Cerrado", idRequest, idEmployee, null, "clos", null, null, conn);

        JsonArrayBuilder jsonCase = Json.createArrayBuilder();
        return jsonCase;
    }

    private JsonArrayBuilder cancelSuscrib(Connection conn, JsonObject req) throws Exception {
        String keysSync = req.getString("keys");
        String[] keys = keysSync.split(":");
        int idEmployee = req.getInt(keys[0]);
        int idRequest = req.getInt(keys[1]);
        String query = "UPDATE hlp_suscriptor h "
                + "SET h.active = 0 "
                + "WHERE h.case_id = " + idRequest + " AND h.sup_emp_id = " + idEmployee + " ";
        new MySQLQuery(query).executeUpdate(conn);
        JsonArrayBuilder jsonCase = Json.createArrayBuilder();
        return jsonCase;
    }

    private Date formatDate(String dateString) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return format.parse(dateString);
        } catch (ParseException ex) {
            return null;
        }
    }

    private void updateSuscriptor(boolean suscripted, Integer idRequest, Integer idEmployee, Integer inCharge, Connection conn) throws Exception {
        //preguntar si esta suscrito a esta solicitud
        String query = "SELECT EXISTS(SELECT id FROM hlp_suscriptor WHERE case_id = " + idRequest + " AND sup_emp_id = " + idEmployee + ");";
        Integer hasSuscripted = MySQLQuery.getAsInteger(new MySQLQuery(query).getRecord(conn)[0]);
        if (suscripted) {
            //crear o actualizar el registro si quiere estar suscrito
            if (hasSuscripted == 1) {
                updateSuscriptor(1, idRequest, idEmployee, conn);
            } else {
                insertSuscriptor(idRequest, idEmployee, conn);
            }
        } else //cambiar el estado del suscrito a no suscrito
        if (hasSuscripted == 1) {
            updateSuscriptor(0, idRequest, idEmployee, conn);
        }
        //si el encargado es el que solicito entonces el estado de suscrito se vuelve inactivo (0)
        updateSuscriptorSolicited(idRequest, inCharge, conn);
    }

    private void updateSuscriptorSolicited(int idRequest, int inCharge, Connection conn) throws Exception {
        String query = "UPDATE hlp_suscriptor hlps "
                + "SET active = 0 "
                + "WHERE hlps.case_id = " + idRequest + " AND "
                + "hlps.sup_emp_id IN (SELECT e.id FROM employee e WHERE e.per_employee_id = " + inCharge + " )";
        new MySQLQuery(query).executeUpdate(conn);
    }

    private void updateSuscriptor(Integer active, Integer idRequest, Integer idEmployee, Connection conn) throws Exception {
        String query = "UPDATE hlp_suscriptor hlps "
                + "SET active = " + active + " "
                + "WHERE hlps.case_id = " + idRequest + " AND "
                + "hlps.sup_emp_id = " + idEmployee;
        new MySQLQuery(query).executeUpdate(conn);
    }

    private void insertSuscriptor(Integer idRequest, Integer idEmployee, Connection conn) throws Exception {
        String query = "INSERT INTO hlp_suscriptor "
                + "(case_id, sup_emp_id, active) "
                + "VALUES (" + idRequest + ", " + idEmployee + ", 1);";
        new MySQLQuery(query).executeInsert(conn);
    }

    private void insertFlowCheck(String step, Integer caseId, Integer perCurrent, String notes, String state, Integer previusPerson, Integer nextPerson, Connection conn) throws Exception {
        if (step.equals("Reasignado") && (previusPerson == null || state == null)) {
            String queryIdPerson = "SELECT f.next_person , f.state FROM hlp_flow_check f WHERE f.case_id = " + caseId + " ORDER BY f.id DESC LIMIT 1";
            Object[] obj = new MySQLQuery(queryIdPerson).getRecord(conn);
            if (obj != null && obj.length > 0) {
                previusPerson = previusPerson == null ? MySQLQuery.getAsInteger(obj[0]) : previusPerson;
                state = (state == null ? MySQLQuery.getAsString(obj[1]) : state);
            } else {
                state = "prog";
            }
        }
        String query = "INSERT INTO hlp_flow_check (step , case_id , per_current , reg_date , notes , state , previous_person , next_person) "
                + "VALUES ('" + step + "' , " + caseId + " , " + perCurrent + ", ?1 , " + (notes == null ? notes : "'" + notes + "'") + " , '" + state + "' , " + previusPerson + " , " + nextPerson + ")";
        new MySQLQuery(query).setParam(1, new Date()).executeInsert(conn);
    }

    private Integer idEmployeeByPerEmployee(Integer idPerEmployee, Connection conn) throws Exception {
        String query = "SELECT e.id FROM employee e INNER JOIN per_employee p ON p.id = e.per_employee_id AND p.id = " + idPerEmployee + " LIMIT 1 ";
        Object[] obj = new MySQLQuery(query).getRecord(conn);
        return obj.length > 0 ? MySQLQuery.getAsInteger(obj[0]) : null;
    }

    public void addInt(JsonObjectBuilder ob, String field, Integer value) {
        if (value == null) {
            ob.addNull(field);
        } else {
            ob.add(field, value);
        }
    }

    public Object[][] getData(Object[][] dataTypes, String columns, String state,
            List<String> othersTypes, Integer employeeId, String orderBy, String typeUser,
            Connection conn, String filterType, boolean isNormal, boolean isSupport) throws Exception {

        List<Object[]> listRows = null;
        listRows = new ArrayList<>();
        for (Object[] rowType : dataTypes) {
            String query = "SELECT " + columns + " "
                    + "FROM hlp_request r "
                    + "INNER JOIN hlp_type t ON t.id = r.type_id "
                    + "LEFT JOIN employee emp ON emp.id = r.in_charge_emp "
                    + (state.equals(othersTypes.get(0)) ? "INNER JOIN hlp_suscriptor hlps ON hlps.case_id = r.id " : " ")
                    + "WHERE t.id = " + rowType[0] + " ";

            String filter = "";
            Object[] hlpCfg = new MySQLQuery("SELECT c.has_play_pause, c.expire_alert FROM hlp_cfg c ").getRecord(conn);

            if (hlpCfg == null || hlpCfg.length == 0) {
                throw new Exception("No se ha definido cfg de Mesa de Ayuda ");
            }

            if (isNormal) {
                filter += " AND r.state = 'plan' AND r.created_by = " + employeeId + " ";
                filterType = null;
            } else if (isSupport) {
                filter += " AND r.state <> 'plan' AND r.in_charge_emp = " + employeeId + " ";
            }

            if (state.equals(othersTypes.get(0))) {// suscritos
                query += " AND r.state <> 'clos' AND r.state <> 'entry'  AND hlps.active = 1 AND hlps.sup_emp_id = " + employeeId + " ";
            } else if (state.equals(othersTypes.get(1))) {//alerts             
                query += "AND (r.state LIKE 'wait' OR r.state LIKE 'prog' OR r.state LIKE 'solv' ) "
                        + "AND (TIMESTAMPDIFF(HOUR,CURRENT_TIMESTAMP() ,r.expirate_date)) <= " + hlpCfg[1] + " ";

                if (isSupport) {
                    query += "AND (r.created_by = " + employeeId + " OR r.in_charge_emp = " + employeeId + ") ";
                }
            } else if (state.equals(othersTypes.get(2))) {//running
                query += " AND r.running = 1 ";
                query += filter;
            } else if (state.equals(othersTypes.get(3))) {//cases
                if (filterType != null) {
                    switch (filterType) {
                        case "pend":
                            query += "AND (r.state <> 'plan' AND r.state <> 'canc' AND r.state <> 'clos' AND r.state <> 'new' AND r.state <> 'retu' AND r.state <> 'entry' " + (MySQLQuery.getAsBoolean(hlpCfg[0]) ? " " : " AND r.state <> 'prog' ") + " )";
                            query += (MySQLQuery.getAsBoolean(hlpCfg[0]) ? " AND ( r.running=0 OR r.running IS NULL) " : " ");
                            break;
                        case "entry":
                            query += "AND (r.state = 'new' OR r.state = 'retu' OR r.state='entry') ";
                            break;
                        case "prog":
                            query += "AND (r.state LIKE 'prog' OR r.state LIKE 'revi' OR r.state LIKE 'reje') ";
                            query += (MySQLQuery.getAsBoolean(hlpCfg[0]) ? " AND (r.running = 1 OR r.running IS NULL) " : " ");
                            break;
                    }
                }
                query += filter;
            } else if (state.equals(othersTypes.get(4))) {//clos
                query += " AND r.state = 'clos' ";
                query += " AND MONTH(CURDATE()) = MONTH(r.close_date) AND YEAR(CURDATE()) = YEAR(r.close_date) ";
                if (isNormal) {
                    query += " AND r.created_by = " + employeeId + " ";
                } else if (isSupport) {
                    query += " AND r.in_charge_emp = " + employeeId + " ";
                }
            }
            query += " AND " + typeUser;
            if (orderBy != null) {
                query += orderBy;
            }
            Object[][] data = new MySQLQuery(query).getRecords(conn);
            for (Object[] row : data) {
                listRows.add(row);
            }
        }
        return listRows.toArray(new Object[0][]);
    }

    public String getStep(String fieldName) {
        switch (fieldName) {
            case "aper":
                return "Apertura";
            case "asig":
                return "Asignación";
            case "apro":
                return "Envio a Aprobación";
            case "acep":
                return "Solución Aceptada";
            case "reje":
                return "Solución Rechazada";
            case "retu":
                return "Devuelto";
            case "wait":
                return "Puesto en Espera";
            case "clos":
                return "Cerrado";
            case "canc":
                return "Cancelado";
            case "solv":
                return "Solucionado";
            case "aper_asig":
                return "Apertura y Asignación";
            case "aper_solv":
                return "Apertura con Solucion";
            case "reop":
                return "Reapertura";
            case "revi":
                return "En Revisión";
            case "reviReturn":
                return "Revisión Devuelta";
            case "reAsign":
                return "Reasignado";

        }
        return null;
    }

    private String registToken(Connection conn, JsonObject req) throws Exception {

        String token = JsonUtils.getString(req, "token");
        String imei = JsonUtils.getString(req, "imei");
        String employee = JsonUtils.getString(req, "employee");
        Integer appId = new MySQLQuery("SELECT id FROM system_app WHERE package_name = 'com.qualisys.helpdesk'").getAsInteger(conn);

        Object[] tokenRow = new MySQLQuery("SELECT id, imei FROM sys_gcm_token c WHERE c.token = '" + token + "' AND app_id = " + appId).getRecord(conn);
        if (tokenRow != null && tokenRow.length > 0) {
            String imeiId = MySQLQuery.getAsString(tokenRow[1]);
            if (imeiId.equals(imei)) {
                int id = MySQLQuery.getAsInteger(tokenRow[0]);
                new MySQLQuery("UPDATE sys_gcm_token "
                        + "SET emp_id = " + employee + " "
                        + "WHERE id = " + id + " ").executeUpdate(conn);
            }
        } else {
            new MySQLQuery("INSERT INTO sys_gcm_token "
                    + "SET  imei=" + imei + ", emp_id=" + employee + ", token= '" + token + "', app_id = " + appId).executeInsert(conn);
        }

        return "OK";
    }
}
