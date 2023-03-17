package web.vicky.servlets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.Date;
import java.util.GregorianCalendar;
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
import utilities.DesEncrypter;
import utilities.MySQLQuery;
import static web.vicky.beans.CheckOrderStatus.sendToClient;
import web.vicky.clients.ClieAddress;
import web.vicky.clients.ClieUsr;
import web.vicky.clients.LocationInfo;
import web.vicky.clients.SendRecoveryMail;

@MultipartConfig
@WebServlet(name = "ClientLogin", urlPatterns = {"/ClientLogin"})
public class ClientLogin extends HttpServlet {

    private static final String STATUS_OK = "OK";
    private static final String STATUS_ERROR = "ERROR";

    public void sendLoginInfo(JsonObjectBuilder ob, ClieUsr usr, String sessionId, String packageName, Connection conn) throws Exception {
        Object[] data = new MySQLQuery("SELECT "
                + "c.id, "
                + "ct.name "
                + "FROM ord_contract_index c "
                + "INNER JOIN city ct ON c.city_id = ct.id "
                + "WHERE c.contract_id = " + usr.id + " "
                + "AND type = 'app' "
                + "AND active").getRecord(conn);

        Integer indexId = MySQLQuery.getAsInteger(data[0]);

        boolean order = new MySQLQuery("SELECT COUNT(*) > 0 "
                + "FROM ord_cyl_order o  "
                + "LEFT JOIN ord_cyl_order_offer of on o.id = of.order_id and o.driver_id = of.emp_id and of.accept_dt is not null and of.cancel_dt is null and of.backoff_dt is null "
                + "WHERE "
                + "o.index_id = " + indexId + " "
                + "AND o.cancel_cause_id IS NULL  "
                + "AND o.confirm_hour IS NULL AND ("
                + "(o.wait_to_app AND o.driver_id IS NULL) OR "
                + "(o.driver_id IS NOT NULL AND of.arrive_dt IS NULL) OR "
                + "(o.driver_id IS NOT NULL AND of.arrive_dt IS NOT NULL AND o.clie_confirmed = 0) "
                + ")").getAsBoolean(conn);

        ob.add("status", STATUS_OK);
        ob.add("indexId", indexId);
        ob.add("sessionId", sessionId);
        ob.add("usrId", usr.id);
        ob.add("name", usr.firstName + " " + usr.lastName);
        ob.add("address", usr.address);
        ob.add("city", MySQLQuery.getAsString(data[1]));
        ob.add("order", order);
    }

    public void updateToken(String token, int usrId, Connection conn) throws Exception {
        Object[] tokenRow = new MySQLQuery("SELECT id, usr_id FROM clie_gcm_token c WHERE c.token = ?1;").setParam(1, token).getRecord(conn);
        if (tokenRow != null && tokenRow.length > 0) {
            int tokenUsrId = MySQLQuery.getAsInteger(tokenRow[1]);
            if (tokenUsrId != usrId) {
                int tokenId = MySQLQuery.getAsInteger(tokenRow[0]);
                new MySQLQuery("UPDATE clie_gcm_token SET usr_id = " + usrId + " WHERE id = " + tokenId + "").executeUpdate(conn);
            }
        } else {
            new MySQLQuery("INSERT INTO clie_gcm_token  SET "
                    + " usr_id=" + usrId + ", "
                    + " token= ?1;"
            ).setParam(1, token).executeInsert(conn);
        }
    }

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
            String packageName = req.containsKey("packageName") ? req.getString("packageName") : "";

            try {
                switch (reqType) {
                    case "checkVersion": {
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

                    case "requestOrderUpdate": {
                        int indexId = req.getInt("indexId");

                        MySQLQuery q = new MySQLQuery("SELECT o.id,  "
                                + "COUNT(t.id) > 0 AND o.driver_id IS NULL AS search, "
                                + "o.driver_id IS NOT NULL AND of.arrive_dt IS NULL AS path, "
                                + "o.driver_id IS NOT NULL AND of.arrive_dt IS NOT NULL AND o.clie_confirmed = 0 AS arrived,"
                                + "o.driver_id,"
                                + "o.vehicle_id "
                                + "FROM ord_cyl_order o  "
                                + "LEFT JOIN ord_cyl_order_offer of on o.id = of.order_id and o.driver_id = of.emp_id and of.accept_dt is not null and of.cancel_dt is null and of.backoff_dt is null "
                                + "LEFT JOIN ord_cyl_order_timer t ON t.order_id = o.id and t.handle is not null "
                                + "WHERE "
                                + "o.index_id = " + indexId + " "
                                + "AND o.cancel_cause_id IS NULL  "
                                + "AND o.confirm_hour IS NULL AND ("
                                + "(o.wait_to_app AND o.driver_id IS NULL) OR "
                                + "(o.driver_id IS NOT NULL AND of.arrive_dt IS NULL) OR "
                                + "(o.driver_id IS NOT NULL AND of.arrive_dt IS NOT NULL AND o.clie_confirmed = 0) "
                                + ") ORDER BY o.day DESC, o.taken_hour DESC");

                        Object[] statusRow = q.getRecord(conn);

                        int orderId = MySQLQuery.getAsInteger(statusRow[0]);
                        Integer empId = MySQLQuery.getAsInteger(statusRow[4]);
                        Integer vhId = MySQLQuery.getAsInteger(statusRow[5]);
                        Double lat = null;
                        Double lon = null;
                        if (empId != null) {
                            Object[] coordsRecord = new MySQLQuery("SELECT `latitude`, `longitude` FROM `gps_last_coord` WHERE `employee_id` = " + empId).getRecord(conn);
                            lat = MySQLQuery.getAsDouble(coordsRecord[0]);
                            lon = MySQLQuery.getAsDouble(coordsRecord[1]);
                        }

                        if (MySQLQuery.getAsBoolean(statusRow[1])) {
                            //searching
                            sendToClient(orderId, empId, vhId, lat, lon, "searching", null, null, null, conn);
                        } else if (MySQLQuery.getAsBoolean(statusRow[2])) {
                            //path
                            sendToClient(orderId, empId, vhId, lat, lon, "orderAccepted", null, null, null, conn);
                        } else if (MySQLQuery.getAsBoolean(statusRow[3])) {
                            //arrived
                            sendToClient(orderId, empId, vhId, null, null, "orderArrived", null, null, null, conn);
                        } else {
                            System.out.println("Opción inesperada");
                        }

                        ob.add("status", STATUS_OK);
                        break;
                    }
                    case "register": {
                        String mail = req.getString("mail");
                        double lat = req.getJsonNumber("lat").doubleValue();
                        double lon = req.getJsonNumber("lon").doubleValue();

                        if (new MySQLQuery("SELECT COUNT(*) > 0 FROM clie_usr WHERE mail = ?1;").setParam(1, mail).getAsBoolean(conn)) {
                            ob.add("status", STATUS_ERROR);
                            ob.add("msg", "El correo que ingresó ya está registrado.");
                        } else {
                            LocationInfo li = LocationInfo.getInfo(lat, lon, conn);
                            ClieUsr usr = new ClieUsr();
                            usr.firstName = req.getString("firstName");
                            usr.lastName = req.getString("lastName");
                            usr.mail = mail;
                            usr.password = req.getString("password");
                            usr.phone = req.getString("phone");
                            usr.document = "pendiente";
                            usr.address = "pendiente";
                            usr.landmark = "";
                            usr.registerDate = new Date();
                            usr.id = ClieUsr.insert(usr, conn);

                            new MySQLQuery("INSERT INTO ord_contract_index  SET "
                                    + " document=?1,"
                                    + " address=?2,"
                                    + " phones=?3,"
                                    + " first_name=?4,"
                                    + " last_name=?5,"
                                    + " contract_id=" + usr.id + ","
                                    + " city_id=" + li.cityId + ","
                                    + " active=1,"
                                    + " email=?6,"
                                    + " lat=" + lat + ","
                                    + " lon=" + lon + ","
                                    + " auth_advertising=1,"
                                    + " type='app'"
                            ).setParam(1, usr.document).setParam(2, usr.address).setParam(3, usr.phone).setParam(4, usr.firstName).setParam(5, usr.lastName).setParam(6, usr.mail).executeInsert(conn);
                            ob.add("status", STATUS_OK);

                        }
                        break;
                    }

                    case "login": {
                        String mail = req.getString("mail");
                        String password = req.getString("password");
                        //String packageName = req.getString("packageName");

                        String token = req.getString("token");
                        ClieUsr usr = ClieUsr.getFromMail(mail, conn);
                        updateToken(token, usr.id, conn);

                        if (usr.password.toUpperCase().equals(password.toUpperCase())) {
                            String sessionId = openSession(usr.id, conn, request);
                            sendLoginInfo(ob, usr, sessionId, packageName, conn);
                        } else {
                            ob.add("status", STATUS_ERROR);
                            ob.add("msg", "Contraseña incorrecta.");
                        }
                        break;
                    }

                    case "sessionInfo": {
                        String sessionId = req.getString("sessionId");
                        //String packageName = req.getString("packageName");
                        String token = req.getString("token");

                        Object[] sessRow = new MySQLQuery("SELECT usr_id FROM clie_session WHERE session_id = ?1;").setParam(1, sessionId).getRecord(conn);
                        if (sessRow != null) {
                            int usrId = MySQLQuery.getAsInteger(sessRow[0]);
                            ClieUsr usr = ClieUsr.select(usrId, conn);
                            updateToken(token, usr.id, conn);
                            sendLoginInfo(ob, usr, sessionId, packageName, conn);
                        } else {
                            ob.add("status", STATUS_ERROR);
                            ob.add("msg", "Contraseña incorrecta.");
                        }
                        break;
                    }

                    case "generatePIN": {
                        String mail = req.getString("mail");
                        ClieUsr usr = ClieUsr.getFromMail(mail, conn);
                        String randomKey = "";
                        while (randomKey.length() < 5) {
                            char c = (char) Math.floor(Math.random() * (1 - (255 + 1)) + (255));
                            if (((c >= '1' && c <= '9') || (c >= 'A' && c <= 'Z')) && c != 'O') {
                                randomKey += c;
                            }
                        }
                        GregorianCalendar gc = new GregorianCalendar();
                        gc.setTime(new Date());
                        gc.add(GregorianCalendar.HOUR_OF_DAY, 1);
                        usr.recoveryPin = randomKey;
                        usr.recoveryExp = gc.getTime();
                        usr.recoveryAccepted = false;
                        ClieUsr.update(usr, conn);
                        SendRecoveryMail.sendMail(usr, conn);
                        ob.add("status", STATUS_OK);
                        break;
                    }

                    case "confirmPIN": {
                        String mail = req.getString("mail");
                        String recPIN = req.getString("pin").toUpperCase();
                        ClieUsr usr = ClieUsr.getFromMail(mail, conn);
                        if (usr.recoveryPin == null) {
                            throw new Exception("No se ha iniciado el proceso de recuperación");
                        } else if (usr.recoveryExp.compareTo(new Date()) < 0) {
                            throw new Exception("El PIN ha expirado.");
                        } else if (!usr.recoveryPin.equals(recPIN)) {
                            Thread.sleep(3000);
                            throw new Exception("El PIN no coincide.");
                        } else {
                            usr.recoveryAccepted = true;
                            ClieUsr.update(usr, conn);
                            ob.add("status", STATUS_OK);
                        }
                        break;
                    }
                    case "passChange": {
                        String mail = req.getString("mail");
                        String newPassword = req.getString("newPassword");
                        ClieUsr usr = ClieUsr.getFromMail(mail, conn);
                        if (usr.recoveryPin == null) {
                            throw new Exception("No se ha iniciado el proceso de recuperación");
                        } else if (!usr.recoveryAccepted) {
                            throw new Exception("El PIN no ha sido confirmado.");
                        } else if (usr.recoveryExp.compareTo(new Date()) < 0) {
                            throw new Exception("El PIN no ha expirado.");
                        }
                        usr.password = newPassword;
                        ClieUsr.update(usr, conn);
                        ob.add("status", STATUS_OK);
                        ob.add("sessionId", openSession(usr.id, conn, request));
                        break;
                    }

                    case "logout": {
                        String sessionId = req.getString("sessionId");
                        closeSession(sessionId, conn, request);
                        ob.add("status", STATUS_OK);
                        ob.add("msg", "Se ha cerrado la sesión.");
                        break;
                    }
                    case "insertAddress": {
                        String address = req.getString("address");
                        int usrId = req.getInt("usrId");
                        String notes = req.getString("notes");
                        ClieAddress usr = new ClieAddress();
                        usr.address = address;
                        usr.usrId = usrId;
                        usr.notes = notes;
                        ClieAddress.insert(usr, conn);
                        ob.add("status", STATUS_OK);
                        break;
                    }

                    case "getAddresses": {
                        int usrId = req.getInt("usrId");
                        JsonArrayBuilder dataAddress = Json.createArrayBuilder();
                        ClieAddress[] adds = ClieAddress.getByUsr(usrId, conn);

                        for (ClieAddress add : adds) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("id", add.id);
                            row.add("address", add.address);
                            row.add("notas", add.notes);
                            dataAddress.add(row);
                        }
                        ob.add("status", STATUS_OK);
                        ob.add("address", dataAddress);
                        break;
                    }
                    case "deleteAddress": {
                        ClieAddress.delete(req.getInt("id"), conn);

                        ob.add("status", STATUS_OK);
                        ob.add("msg", "Direccion eliminada con exito.");

                        break;
                    }
                    case "updateAddress": {
                        String address = req.getString("address");
                        int usrId = req.getInt("usrId");
                        String notas = req.getString("notas");
                        int id = req.getInt("id");

                        ClieAddress usr = new ClieAddress(id, address, usrId, notas);
                        ClieAddress.update(usr, conn);

                        ob.add("status", STATUS_OK);
                        ob.add("msg", "Direccion actualizada con exito.");

                        break;
                    }

                    default:
                        throw new Exception("Unknown header: " + reqType);
                }
            } catch (Exception ex) {
                Logger.getLogger(ClientLogin.class.getName()).log(Level.SEVERE, packageName, ex);
                ob.add("status", STATUS_ERROR);
                ob.add("msg", ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
            } finally {
                w.writeObject(ob.build());
            }
        } catch (Exception ex) {
            Logger.getLogger(ClientLogin.class.getName()).log(Level.SEVERE, null, ex);
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

    public static String openSession(int usrId, Connection conn, HttpServletRequest req) throws Exception {
        int id = new MySQLQuery("INSERT INTO clie_session SET begin_time = NOW(), usr_id = " + usrId + ", session_id = '', server_ip = ?1, user_ip = ?2, last_activity = NOW()").setParam(1, req.getServerName()).setParam(2, req.getRemoteAddr()).executeInsert(conn);
        DesEncrypter enc = new DesEncrypter("9An7Ver");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        enc.encrypt(new ByteArrayInputStream((id + "").getBytes()), baos);
        String sessId = enc.hexStringFromBytes(baos.toByteArray());
        new MySQLQuery("UPDATE clie_session SET session_id = ?1 WHERE id = " + id).setParam(1, sessId).executeUpdate(conn);
        //new MySQLQuery("UPDATE clie_session SET end_time = NOW() WHERE id <> " + id + " AND usr_id = " + usrId).executeUpdate(conn);
        return sessId;
    }

    public static void closeSession(String sessionId, Connection conn, HttpServletRequest req) throws Exception {
        new MySQLQuery("UPDATE clie_session SET end_time = NOW() WHERE session_id = ?1;").setParam(1, sessionId).executeUpdate(conn);

    }

}
