package web.vicky.servlets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
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
import service.MySQL.MySQLCommon;
import service.MySQL.MySQLSelect;
import utilities.DesEncrypter;
import utilities.MySQLQuery;
import web.ShortException;
import web.vicky.beans.CheckOrderStatus;
import web.vicky.clients.GpsPolygon;
import web.vicky.clients.LocationInfo;
import web.vicky.model.ScheduleCheck;

@MultipartConfig
@WebServlet(name = "/MimonRequest", urlPatterns = {"/MimonRequest"})
public class MimonRequest extends HttpServlet {

    private static final String STATUS_OK = "OK";
    private static final String STATUS_ERROR = "ERROR";

    @Inject
    private CheckOrderStatus statusChecker;

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

                    case "getCancelCauses": {
                        Object dataCancel[][] = new MySQLQuery("SELECT id, description FROM ord_cancel_cause WHERE type = 'clie'").getRecords(conn);
                        JsonArrayBuilder jab = Json.createArrayBuilder();
                        for (Object[] data : dataCancel) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("name", (data[1] != null ? MySQLQuery.getAsString(data[1]) : ""));
                            jab.add(row);
                        }
                        ob.add("status", STATUS_OK);
                        ob.add("causes", jab);
                        break;
                    }

                    case "getComplainCauses": {
                        String type = req.getString("type");
                        Object dataCancel[][] = new MySQLQuery("SELECT id, name FROM ord_complain_cause WHERE type = ?1 AND active ORDER BY place").setParam(1, type).getRecords(conn);
                        JsonArrayBuilder jab = Json.createArrayBuilder();
                        for (Object[] data : dataCancel) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("name", (data[1] != null ? MySQLQuery.getAsString(data[1]) : ""));
                            jab.add(row);
                        }
                        ob.add("status", STATUS_OK);
                        ob.add("causes", jab);
                        break;
                    }

                    case "getDriverPos": {
                        int driverId = req.getInt("driverId");
                        Object[] coords = new MySQLQuery("SELECT c.latitude, c.longitude "
                                + "FROM gps_last_coord c "
                                + "WHERE c.employee_id = " + driverId + " "
                                + "ORDER BY c.date desc limit 1;").getRecord(conn);
                        ob.add("lat", MySQLQuery.getAsDouble(coords[0]));
                        ob.add("lon", MySQLQuery.getAsDouble(coords[1]));
                        ob.add("status", STATUS_OK);
                        break;
                    }

                    case "confirmOrder": {
                        int orderId = req.getInt("orderId");
                        int rate = req.getInt("rate");
                        Integer compId = null;

                        if (req.containsKey("compId")) {
                            compId = req.getInt("compId");
                        }
                        MySQLQuery mq = new MySQLQuery("UPDATE ord_cyl_order "
                                + "SET delivered = 1, "
                                + "called = 1, "
                                + "clie_confirmed = 1, "
                                + (compId != null ? "complain_id = " + compId + ", " : "")
                                + "rate = " + rate + " "
                                + "WHERE id = " + orderId + " ");
                        mq.executeUpdate(conn);

                        /*//Entrega de bonos con confirmación del cliente, 4/09/2017
                        Object[] saleRow = new MySQLQuery("SELECT s.id, s.emp_id FROM "
                                + "ord_ord_sale mm "
                                + "INNER JOIN trk_sale s on mm.trk_sale_id = s.id "
                                + "WHERE mm.cyl_order_id = " + orderId).getRecord(conn);

                        if (saleRow != null) {
                            Integer saleId = MySQLQuery.getAsInteger(saleRow[0]);
                            Integer empId = MySQLQuery.getAsInteger(saleRow[1]);
                            //CliePromo.checkPromoTicket(orderId, saleId, empId, conn);
                        }*/
                        ob.add("status", STATUS_OK);
                        ob.add("msg1", "Gracias");
                        ob.add("msg2", "Gracias por compartir su opinión");
                        break;
                    }

                    case "complainOrder": {
                        int orderId = req.getInt("orderId");
                        int compId = req.getInt("compId");
                        MySQLQuery mq = new MySQLQuery("UPDATE ord_cyl_order SET "
                                + "complain_id = " + compId + " "
                                + "WHERE id = " + orderId + " ");
                        mq.executeUpdate(conn);
                        ob.add("msg1", "Lo sentimos");
                        ob.add("msg2", "Lo contactaremos pronto para solucionar el inconveniente");
                        ob.add("status", STATUS_OK);
                        break;
                    }

                    case "createOrder": {
                        double lat = req.getJsonNumber("lat").doubleValue();
                        double lon = req.getJsonNumber("lon").doubleValue();
                        int userId = req.getInt("usrId");
                        int indexId = req.getInt("indexId");

                        JsonArray cyls = req.getJsonArray("cyls");
                        String address = req.getString("address");
                        String landMark = (req.containsKey("landmark") && !req.isNull("landmark") ? req.getString("landmark") : "");

                        LocationInfo li = LocationInfo.getInfo(lat, lon, conn);
                        ScheduleCheck sched = ScheduleCheck.validateSchedule(li.officeId, conn);
                        ob.add("open", sched.open);

                        if (sched.open) {
                            Integer neighId = GpsPolygon.hitNeigh(lat, lon, li.cityId);
                            if (neighId == null) {
                                neighId = new MySQLQuery("SELECT id FROM neigh WHERE sector_id = " + li.sectorId + " AND name='otro'").getAsInteger(conn);
                            }
                            
                            if (neighId == null) {
                                throw new ShortException("Pedido fallido. Su barrio no está referenciado. Comuníquese con la oficina de Atención al Cliente Montagas.");
                            }

                            if (!landMark.equals("")) {
                                new MySQLQuery("UPDATE clie_usr SET reference = ?1 WHERE id =" + userId).setParam(1, landMark).executeUpdate(conn);
                            }

                            new MySQLQuery("UPDATE ord_contract_index SET neigh_id = " + neighId + ", address = ?3, lat = ?1, lon = ?2 WHERE id =" + indexId).setParam(1, lat).setParam(2, lon).setParam(3, address).executeUpdate(conn);
                            new MySQLQuery("UPDATE clie_usr SET address = ?1 WHERE id =" + userId).setParam(1, address).executeUpdate(conn);

                            Integer id = new MySQLQuery("SELECT id FROM ord_cyl_order WHERE index_id = " + indexId + " AND day = CURDATE() AND driver_id IS NULL AND cancelled_by IS NULL ").getAsInteger(conn);
                            boolean ordByViki = new MySQLQuery("SELECT sales_app FROM ord_office WHERE id = " + li.officeId).getAsBoolean(conn);
                            boolean vikiIsEnable = new MySQLQuery("SELECT enabled FROM vicky_cfg WHERE id = 1").getAsBoolean(conn);

                            if (id == null) {                                
                                id = new MySQLQuery("INSERT INTO ord_cyl_order_seq SET dummy = 0").executeInsert(conn);
                                new MySQLQuery("INSERT into ord_cyl_order  set "
                                        + " id = " + id + ","
                                        + " day = CURDATE(),"
                                        + " office_id = " + li.officeId + ","
                                        + " taken_by_id = 1,"
                                        + " neigh_id = " + neighId + ","
                                        + " index_id = " + indexId + ","
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
                        ob.add("status", STATUS_OK);
                        break;
                    }

                    case "history": {
                        int indexId = req.getInt("indexId");
                        Object[][] data = new MySQLQuery("SELECT "
                                + "o.id, "//0
                                + "o.day, "//1
                                + "TIME(of.confirm_dt), "//2
                                + "(SELECT GROUP_CONCAT(t.name, ' x ', c.amount) FROM cylinder_type t INNER JOIN ord_cyl_type_order c ON c.cylinder_type_id = t.id WHERE c.order_id = o.id), "//3
                                + "CONCAT(e.first_name, ' ', e.last_name), "//4
                                + "o.rate, "//5
                                + "(SELECT b.id FROM bfile b WHERE b.owner_id = pe.id AND b.owner_type = 10), "//6
                                + "v.plate, "//7
                                + "v.internal "//8
                                + "FROM ord_cyl_order o "
                                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                                + "INNER JOIN ord_cyl_order_offer of ON of.order_id = o.id AND of.confirm_dt IS NOT NULL "
                                + "INNER JOIN employee e ON of.emp_id = e.id "
                                + "INNER JOIN vehicle v ON of.vh_id = v.id "
                                + "LEFT JOIN per_employee pe ON e.per_employee_id = pe.id "
                                + "WHERE i.id = " + indexId + " "
                                + "AND o.cancel_cause_id IS NULL "
                                + "AND o.confirmed_by_id IS NOT NULL "
                                + "ORDER BY o.id DESC "
                                + "LIMIT 5").getRecords(conn);

                        JsonArrayBuilder ab = Json.createArrayBuilder();
                        for (Object[] row : data) {
                            JsonObjectBuilder job = Json.createObjectBuilder()
                                    .add("orderId", MySQLQuery.getAsInteger(row[0]))
                                    .add("orderDay", MySQLQuery.getAsString(row[1]))
                                    .add("orderConfirm", MySQLQuery.getAsString(row[2]))
                                    .add("orderCyls", MySQLQuery.getAsString(row[3]))
                                    .add("orderSman", MySQLQuery.getAsString(row[4]))
                                    .add("orderRate", MySQLQuery.getAsInteger(row[5]))
                                    .add("plate", MySQLQuery.getAsString(row[7]))
                                    .add("internal", MySQLQuery.getAsString(row[8]));

                            if (row[6] != null) {
                                job.add("photoId", MySQLQuery.getAsInteger(row[6]));
                            }
                            ab.add(job);
                        }
                        ob.add("status", STATUS_OK);
                        ob.add("orders", ab);

                        break;
                    }

                    case "tickHist": {
                        int indexId = req.getInt("indexId");
                        Object[][] data = new MySQLQuery("SELECT "
                                + "t.id, "
                                + "t.`status`"
                                + "FROM clie_ticket t "
                                + "WHERE t.index_id = " + indexId).getRecords(conn);

                        Object[][] defData = new Object[data.length][5];
                        for (int i = 0; i < data.length; i++) {
                            defData[i][0] = data[i][0];
                            defData[i][1] = data[i][1];

                            int tckId = MySQLQuery.getAsInteger(data[i][0]);
                            Object[] row = new MySQLQuery("SELECT subject, date FROM clie_msg_ticket WHERE ticket_id = " + tckId + " ORDER BY id ASC LIMIT 1").getRecord(conn);
                            defData[i][2] = row[0];
                            defData[i][3] = row[1];
                            defData[i][4] = new MySQLQuery("SELECT sent_to = 'cli' FROM clie_msg_ticket WHERE ticket_id = " + tckId + " ORDER BY id DESC LIMIT 1").getAsInteger(conn);

                        }

                        JsonArrayBuilder ab = Json.createArrayBuilder();
                        for (Object[] row : defData) {
                            JsonObjectBuilder job = Json.createObjectBuilder()
                                    .add("ticketId", MySQLQuery.getAsInteger(row[0]))
                                    .add("ticketDay", MySQLQuery.getAsString(row[3]))
                                    .add("ticketSubject", MySQLQuery.getAsString(row[2]))
                                    .add("ticketStatus", MySQLQuery.getAsString(row[1]))
                                    .add("msgPend", MySQLQuery.getAsBoolean(row[4]));
                            ab.add(job);
                        }
                        ob.add("status", STATUS_OK);
                        ob.add("tickets", ab);

                        break;
                    }

                    case "getMsgs": {
                        int ticketId = req.getInt("ticketId");
                        Object[][] data = new MySQLQuery("SELECT "
                                + "m.id, "
                                + "m.subject, "
                                + "m.msg, "
                                + "m.sent_to, "
                                + "m.`date` "
                                + "FROM clie_msg_ticket m "
                                + "WHERE m.ticket_id = " + ticketId).getRecords(conn);

                        JsonArrayBuilder ab = Json.createArrayBuilder();
                        for (Object[] row : data) {
                            JsonObjectBuilder job = Json.createObjectBuilder()
                                    .add("msgId", MySQLQuery.getAsInteger(row[0]))
                                    .add("msgSubject", row[1] != null ? MySQLQuery.getAsString(row[1]) : "")
                                    .add("msg", MySQLQuery.getAsString(row[2]))
                                    .add("msgSentTo", MySQLQuery.getAsString(row[3]))
                                    .add("msgDate", MySQLQuery.getAsString(row[4]));
                            ab.add(job);
                        }
                        ob.add("status", STATUS_OK);
                        ob.add("msgs", ab);
                        break;
                    }

                    case "putMsg": {
                        new MySQLQuery("INSERT INTO clie_msg_ticket "
                                + "SET msg = ?1, "
                                + "sent_to = 'sac', "
                                + "date = NOW(), "
                                + "ticket_id = " + req.getInt("ticketId")).setParam(1, req.getString("msg")).executeInsert(conn);

                        ob.add("status", STATUS_OK);
                        break;
                    }

                    case "createTicket": {
                        int ticketId = new MySQLQuery("INSERT INTO clie_ticket "
                                + "SET index_id = " + req.getInt("indexId") + ", "
                                + "status = 'op'").executeInsert(conn);

                        new MySQLQuery("INSERT INTO clie_msg_ticket "
                                + "SET msg = ?1, "
                                + "subject = ?2, "
                                + "sent_to = 'sac', "
                                + "date = NOW(), "
                                + "ticket_id = " + ticketId).setParam(1, req.getString("msg")).setParam(2, req.getString("subject")).executeInsert(conn);

                        ob.add("status", STATUS_OK);
                        break;
                    }

                    default:
                        throw new Exception("Unknown header: " + reqType);
                }
            } catch (Exception ex) {
                Logger.getLogger(MimonRequest.class.getName()).log(Level.SEVERE, null, ex);
                ob.add("status", STATUS_ERROR);
                ob.add("msg", ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
            } finally {
                w.writeObject(ob.build());
            }
        } catch (Exception ex) {
            Logger.getLogger(MimonRequest.class.getName()).log(Level.SEVERE, null, ex);
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

    public static String openSession(int usrId, String imei, Connection conn, HttpServletRequest req) throws Exception {
        int id = new MySQLQuery("INSERT INTO clie_session SET begin_time = NOW(), usr_id = " + usrId + ", session_id = '', server_ip = ?3, user_ip = ?1, imei = ?2, last_activity = NOW()").setParam(1, req.getRemoteAddr()).setParam(2, imei).setParam(3, req.getServerName()).executeInsert(conn);
        DesEncrypter enc = new DesEncrypter("9An7ver");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        enc.encrypt(new ByteArrayInputStream((id + "").getBytes()), baos);
        String sessId = enc.hexStringFromBytes(baos.toByteArray());
        new MySQLQuery("UPDATE clie_session SET session_id = ?1 WHERE id = " + id).setParam(1, sessId).executeUpdate(conn);
        new MySQLQuery("UPDATE clie_session SET end_time = NOW() WHERE id <> " + id + " AND usr_id = " + usrId).executeUpdate(conn);

        return sessId;
    }

    public static void closeSession(String sessionId, Connection conn, HttpServletRequest req) throws Exception {

        new MySQLQuery("UPDATE clie_session SET end_time = NOW() WHERE session_id = ?1;").setParam(1, sessionId).executeUpdate(conn);

    }
}
