package web.vicky.servlets;

import controller.system.LoginController;
import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
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
import model.Employee;
import model.menu.Credential;
import service.MySQL.MySQLCommon;
import service.MySQL.MySQLSelect;
import utilities.MySQLQuery;
import web.ShortException;
import web.vicky.clients.LocationInfo;
import web.marketing.cylSales.Contract;
import web.marketing.cylSales.CylSales;
import web.vicky.beans.CheckOrderStatus;
import web.vicky.clients.GpsPolygon;
import web.vicky.model.ScheduleCheck;

@MultipartConfig
@WebServlet(name = "VirtualLogin", urlPatterns = {"/VirtualLogin"})
public class VirtualLogin extends HttpServlet {

    private static final String STATUS_OK = "OK";
    private static final String STATUS_ERROR = "ERROR";

    @Inject
    private CheckOrderStatus statusChecker;

    private int getVersion(String s) {
        return Double.valueOf(s).intValue();
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            JsonObjectBuilder ob = Json.createObjectBuilder();

            conn = MySQLCommon.getConnection("sigmads", null);
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");

            JsonObject req = MySQLQuery.scapeJsonObj(request);
            String reqType = req.getString("request");

            try {
                switch (reqType) {
                    case "getCylTypes": {
                        Object dataCyl[][] = new MySQLQuery("SELECT id, name FROM cylinder_type WHERE id IN (10,8,5,6) ORDER BY CAST(name AS SIGNED)").getRecords(conn);
                        JsonArrayBuilder jab = Json.createArrayBuilder();
                        for (Object[] data : dataCyl) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("name", MySQLQuery.getAsString(data[1]) + " lb");
                            jab.add(row);
                        }
                        ob.add("cylTypes", jab);
                        ob.add("status", STATUS_OK);
                        break;
                    }
                    case "getNeighs": {
                        Object[][] data = new MySQLQuery(" SELECT  n.id, concat(n.name,' - ',s.name)  "
                                + " FROM neigh n "
                                + " INNER JOIN sector s ON n.sector_id=s.id "
                                + " INNER JOIN city c ON s.city_id =c.id "
                                + " WHERE c.id = 3 "
                                + " ORDER BY 1 ").getRecords(conn);

                        JsonArrayBuilder ab = Json.createArrayBuilder();
                        int i = 0;
                        for (Object[] row : data) {
                            JsonObjectBuilder job = Json.createObjectBuilder()
                                    .add("neighId", MySQLQuery.getAsInteger(row[0]))
                                    .add("neighName", MySQLQuery.getAsString(row[1]));
                            ab.add(job);
                        }
                        ob.add("subs", ab);
                        ob.add("status", STATUS_OK);

                        break;
                    }
// resolver consulta y acomodar estados
                    case "getOrderHist": {
                        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
                        SimpleDateFormat sh = new SimpleDateFormat("HH:mm:ss");
                        int year = req.getInt("year");
                        int month = req.getInt("month");
                        int empId = req.getInt("empId");
                        boolean unatt = req.getBoolean("unatt");
                        Object dataOrders[][] = new MySQLQuery("SELECT "
                                + "i.document, "
                                + "i.first_name, "
                                + "i.last_name, "
                                + "i.address, "
                                + "i.phones, "
                                + "o.day, "
                                + "o.taken_hour,"
                                + "IF(o.confirm_hour IS NOT NULL AND o.cancel_cause_id IS NULL, 'Atendido' "
                                + ", IF(o.day < CURDATE()AND o.confirm_hour IS NULL AND o.cancel_cause_id IS NULL,'No Atendido',  "
                                + "IF(o.cancel_cause_id is not null, 'Cancelado', 'Pendiente'))) "
                                + "FROM ord_cyl_order o "
                                + "INNER JOIN ord_contract_index i ON i.id = o.index_id "
                                + "WHERE YEAR(o.day) = " + year + " "
                                + "AND MONTH(o.day) = " + month + " "
                                + "AND o.taken_by_id = " + empId + " "
                                + (unatt ? "AND o.day < CURDATE() AND o.confirm_hour IS NULL " : "")
                                + "ORDER BY o.day DESC ").getRecords(conn);
                        JsonArrayBuilder jab = Json.createArrayBuilder();
                        for (Object[] data : dataOrders) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("document", MySQLQuery.getAsString(data[0]));
                            row.add("firstName", MySQLQuery.getAsString(data[1]));
                            row.add("lastName", MySQLQuery.getAsString(data[2]));
                            row.add("address", MySQLQuery.getAsString(data[3]));
                            row.add("phones", MySQLQuery.getAsString(data[4]));
                            row.add("date", sf.format(MySQLQuery.getAsDate(data[5])));
                            row.add("thour", sh.format(MySQLQuery.getAsDate(data[6])));
                            row.add("state", MySQLQuery.getAsString(data[7]));
                            jab.add(row);
                        }
                        ob.add("orders", jab);
                        ob.add("status", STATUS_OK);
                        break;
                    }

                    case "getSummary": {
                        int year = req.getInt("year");
                        int month = req.getInt("month");
                        int empId = req.getInt("empId");
                        Integer attended = new MySQLQuery("select count(*)"
                                + " from ord_cyl_order where confirm_hour is "
                                + " not null and cancel_cause_id is null "
                                + " AND taken_by_id =" + empId + " "
                                + " AND YEAR(day) = " + year + " "
                                + " AND MONTH(day) = " + month + " ").getAsInteger(conn);
                        Integer unattended = new MySQLQuery("select count(*) "
                                + " from ord_cyl_order where day < CURDATE() and "
                                + " confirm_hour is null "
                                + " and cancel_cause_id is null "
                                + " AND taken_by_id =" + empId + " "
                                + " AND YEAR(day) = " + year + " "
                                + " AND MONTH(day) = " + month + " ").getAsInteger(conn);
                        Integer canceled = new MySQLQuery("select count(*) from "
                                + "ord_cyl_order where cancel_cause_id is not null "
                                + " AND taken_by_id =" + empId + " "
                                + " AND YEAR(day) = " + year + " "
                                + " AND MONTH(day) = " + month + " ").getAsInteger(conn);
                        ob.add("attended", attended);
                        ob.add("unattended", unattended);
                        ob.add("canceled", canceled);
                        ob.add("status", STATUS_OK);
                        break;
                    }

                    case "checkVersion": {
                        String packageName = req.getString("packageName");
                        String usrVersion = req.getString("version");
                        Object[] appRow = new MySQLQuery("select s.version, s.mandatory from system_app s where s.package_name = ?1;").setParam(1, packageName).getRecord(conn);
                        String minVersion = MySQLQuery.getAsString(appRow[0]);
                        boolean mandatory = MySQLQuery.getAsBoolean(appRow[1]);

                        if (getVersion(minVersion) <= getVersion(usrVersion)) {
                            ob.add("update", "no");
                        } else {
                            ob.add("update", mandatory ? "mandatory" : "optional");
                        }
                        ob.add("status", STATUS_OK);
                        break;
                    }

                    case "searchDoc": {
                        String doc = req.containsKey("doc") ? req.getString("doc") : null;
                        String where = "";
                        if (doc != null) {
                            doc = doc.trim();
                            doc = doc.replaceAll("[^0-9]", "");
                            where = "(oci.document LIKE '%" + doc + "%' )";//BUSCA POR DOCUMENTO
                        } else {
                            throw new Exception("Escriba nombre o dirección");
                        }

                        Object[][] data = new MySQLQuery("SELECT "
                                + "oci.id AS id, "//0
                                + "oci.document AS doc, "//1
                                + "oci.first_name AS fn, "//2
                                + "oci.last_name AS ln, "//3
                                + "oci.address AS ad, "//4
                                + "oci.phones AS tel, "//5
                                + "oci.email AS eml, "//6
                                + "IF(oci.`type` = 'brand', 1, 0), "//7
                                + "n.name ,"//8
                                + "n.id "
                                + "FROM ord_contract_index AS oci "
                                + "LEFT JOIN neigh AS n ON n.id = oci.neigh_id "
                                + "WHERE oci.active = 1 AND " + where + " ORDER BY oci.`type` LIMIT 100 ").getRecords(conn);

                        JsonArrayBuilder ab = Json.createArrayBuilder();
                        int i = 0;
                        for (Object[] row : data) {
                            JsonObjectBuilder job = Json.createObjectBuilder()
                                    .add("ctrId", MySQLQuery.getAsInteger(row[0]))
                                    .add("document", (row[1] != null ? MySQLQuery.getAsString(row[1]) : ""))
                                    .add("first_name", (row[2] != null ? (MySQLQuery.getAsString(row[2]).equals("null") ? "Sin " : MySQLQuery.getAsString(row[2])) : ""))
                                    .add("last_name", (row[3] != null ? (MySQLQuery.getAsString(row[3]).equals("null") ? "Información" : MySQLQuery.getAsString(row[3])) : ""))
                                    .add("address", (row[4] != null ? (MySQLQuery.getAsString(row[4]).equals("null") ? "Sin Información" : MySQLQuery.getAsString(row[4])) : ""))
                                    .add("phones", (row[5] != null ? MySQLQuery.getAsString(row[5]) : ""))
                                    .add("mail", (row[6] != null ? MySQLQuery.getAsString(row[6]) : ""))
                                    .add("brand", (row[7] != null ? MySQLQuery.getAsBoolean(row[7]) : false))
                                    .add("neigh", row[8] != null ? MySQLQuery.getAsString(row[8]) : "")
                                    .add("nId", row[9] != null ? MySQLQuery.getAsInteger(row[9]) : 0);
                            ab.add(job);
                        }
                        ob.add("ctrs", ab);
                        ob.add("status", STATUS_OK);

                        break;
                    }

                    case "login": {
                        String user = req.getString("user");
                        String pass = req.getString("password");
                        String pack = req.getString("packageName");
                        String type = req.getString("type");
                        String extras = req.getString("extras");
                        String phone = req.getString("phone");
                        boolean returnToken = req.getBoolean("returnToken");
                        boolean sign = true;

                        Credential cred = LoginController.getByCredentials(getServletContext(), user, pass, type, extras, phone, pack, request, returnToken, null, "sigmads", null, sign);
                        Employee emp = new Employee().select(cred.getEmployeeId(), conn);

                        if (emp.virtual) {
                            ob.add("empId", emp.id);
                            if (cred.getDaysLeftPasswordExpiration() != null) {
                                ob.add("daysLeftPasswordExpiration", cred.getDaysLeftPasswordExpiration());
                            }
                            ob.add("sessionId", cred.getSessionId());
                            ob.add("name", emp.firstName + " " + emp.lastName);
                            ob.add("token", "");
                            ob.add("projectNum", cred.getProjectNum());
                            ob.add("status", STATUS_OK);

                        } else {
                            ob.add("status", STATUS_ERROR);
                            ob.add("msg", "El Usuario no es Tendero.");
                        }
                        break;
                    }
                    case "createOrder": {
                        int empId = req.getInt("empId");
                        if (new MySQLQuery("SELECT COUNT(*) = 0 FROM pv_training WHERE emp_id = " + empId).getAsBoolean(conn)) {
                            double lat = req.getJsonNumber("lat").doubleValue();
                            double lon = req.getJsonNumber("lon").doubleValue();
                            boolean isSub = req.getBoolean("isSuburb");
                            int subId = req.getInt("subId");

                            JsonArray cyls = req.getJsonArray("cyls");

                            LocationInfo li = LocationInfo.getInfo(lat, lon, conn);
                            ScheduleCheck sched = ScheduleCheck.validateSchedule(li.officeId, conn);
                            ob.add("open", sched.open);

                            if (sched.open) {
                                Integer neighId;
                                if (isSub) {
                                    neighId = subId;
                                } else {
                                    neighId = GpsPolygon.hitNeigh(lat, lon, li.cityId);
                                }
                                if (neighId == null) {
                                    neighId = new MySQLQuery("SELECT id FROM neigh WHERE sector_id = " + li.sectorId + " AND name='otro'").getAsInteger(conn);
                                }
                                if (neighId == null) {
                                    throw new ShortException("Pedido fallido. Su barrio no está referenciado. Comuníquese con la oficina de Atención al Cliente Montagas.");
                                }

                                Contract ctr = Contract.searchContract(conn, req.getInt("ctrId"));
                                if (ctr == null) {
                                    String phone = (req.containsKey("phones") ? req.getString("phones") : null);
                                    int indId = CylSales.insertContract(null, req.getString("clieDoc"), req.getString("clieFName"), req.getString("clieLName"), phone, 3, req.getString("clieAddress"), "", 1, conn, neighId);
                                    ctr = Contract.searchContract(conn, indId);
                                } else if (ctr.firstName.equals("null") || ctr.firstName.equals("Sin ") || ctr.address.contains("null") || ctr.address.contains("Sin Info")) {
                                    String fName = req.getString("clieFName");
                                    String lName = req.getString("clieLName");
                                    String address = req.getString("clieAddress");
                                    String phone = (req.containsKey("phones") ? req.getString("phones") : null);

                                    new MySQLQuery("UPDATE ord_contract_index SET "
                                            + "first_name = '" + fName + "', last_name = '" + lName + "', address = '" + address + "' "
                                            + (phone != null ? ", phones = '" + phone + "' " : " ")
                                            + ", neigh_id = " + neighId + " "
                                            + "WHERE id = " + ctr.indexId).executeUpdate(conn);
                                    new MySQLQuery("UPDATE ord_contract SET "
                                            + "first_name = '" + fName + "', last_name = '" + lName + "', address = '" + address + "' "
                                            + (phone != null ? ", phones = '" + phone + "' " : " ")
                                            + ", neigh_id = " + neighId + " "
                                            + "WHERE id = (SELECT contract_id FROM ord_contract_index WHERE id = " + ctr.indexId + ")").executeUpdate(conn);
                                } else {
                                    String mail = (req.containsKey("mail") ? req.getString("mail") : null);
                                    String phone = (req.containsKey("phones") ? req.getString("phones") : null);
                                    String address = req.getString("clieAddress");
                                    if (!Objects.equals(ctr.phones, phone) || !Objects.equals(ctr.email, mail) || !Objects.equals(ctr.address, address)) {
                                        Contract.updateContract(conn, ctr, phone, mail, address, neighId);
                                    }
                                }
                                Integer id = new MySQLQuery("SELECT id FROM ord_cyl_order WHERE index_id = " + ctr.indexId + " AND day = CURDATE() AND driver_id IS NULL AND cancelled_by IS NULL ").getAsInteger(conn);
                                boolean ordByViki = new MySQLQuery("SELECT sales_app FROM ord_office WHERE id = " + li.officeId).getAsBoolean(conn);
                                boolean vikiIsEnable = new MySQLQuery("SELECT enabled FROM vicky_cfg WHERE id = 1").getAsBoolean(conn);

                                if (id == null) {
                                    id = new MySQLQuery("INSERT INTO ord_cyl_order_seq SET dummy = 0").executeInsert(conn);                                    
                                    new MySQLQuery("INSERT into ord_cyl_order  set "
                                            + " id = " + id + ","
                                            + " day = CURDATE(),"
                                            + " office_id = " + li.officeId + ","
                                            + " taken_by_id = " + empId + ","
                                            + " neigh_id = " + neighId + ","
                                            + " index_id = " + ctr.indexId + ","
                                            + " taken_hour = CURTIME(), "
                                            + " channel_id = 5, "
                                            + " dist = 1, "
                                            + " wait_to_app = " + (ordByViki && vikiIsEnable)).executeUpdate(conn);

                                    JsonObject aux;
                                    for (int i = 0; i < cyls.size(); i++) {
                                        aux = cyls.getJsonObject(i);
                                        int typeId = aux.getInt("cylId");
                                        int amount = aux.getInt("cylCount");

                                        new MySQLQuery("INSERT INTO ord_cyl_type_order SET "
                                                + "order_id = " + id + ", "
                                                + "cylinder_type_id = " + typeId + ", "
                                                + "amount = " + amount).executeInsert(conn);
                                    }
                                } else {
                                    new MySQLQuery("UPDATE ord_cyl_order SET wait_to_app = " + (ordByViki && vikiIsEnable) + " WHERE id = " + id).executeUpdate(conn);
                                }
                                ob.add("orderId", id);
                                statusChecker.scheduleCheck(id);
                            } else {
                                ob.add("openTitle", sched.title);
                                ob.add("openSub", sched.subtitle);
                                ob.add("openMsg", sched.msg);
                            }
                        }
                        ob.add("status", STATUS_OK);
                        break;
                    }
                    case "logout": {
                        String sessionId = req.getString("sessionId");
                        closeSession(sessionId, conn, request);
                        ob.add("status", STATUS_OK);
                        ob.add("msg", "Se ha cerrado la sesión.");
                        break;
                    }

                    default:
                        throw new Exception("Unknown header: " + reqType);
                }
            } catch (Exception ex) {
                Logger.getLogger(VirtualLogin.class.getName()).log(Level.SEVERE, null, ex);
                ob.add("status", STATUS_ERROR);
                ob.add("msg", ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
            } finally {
                w.writeObject(ob.build());
            }
        } catch (Exception ex) {
            Logger.getLogger(VirtualLogin.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500, ex.getMessage());
        } finally {
            MySQLSelect.tryClose(conn);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

    public static void closeSession(String sessionId, Connection conn, HttpServletRequest req) throws Exception {
        new MySQLQuery("UPDATE clie_session SET end_time = NOW() WHERE session_id = ?1;").setParam(1, sessionId).executeUpdate(conn);

    }

}
