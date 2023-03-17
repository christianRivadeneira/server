package web.ordering;

import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
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
import utilities.MySQLQuery;
import web.push.GCMUtils;

@MultipartConfig
@WebServlet(name = "OrderResponse", urlPatterns = {"/OrderResponse"})
public class OrderResponse extends HttpServlet {

    //private static final Logger LOG = Logger.getLogger(CloseSessionTimer.class.getName());
    private static final String STATUS_OK = "OK";
    private static final String STATUS_ERROR = "ERROR";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {

            String poolName = request.getParameter("poolName");
            JsonObjectBuilder out = Json.createObjectBuilder();

            conn = MySQLCommon.getConnection(poolName, null);
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");
            JsonObject req = MySQLQuery.scapeJsonObj(request);

            try {
                String header = req.getString("header");
                Integer orderId = req.containsKey("orderId") ? req.getInt("orderId") : null;
                Integer clientId = req.containsKey("clientId") ? req.getInt("clientId") : null;
                Integer vehId = req.containsKey("vehId") ? req.getInt("vehId") : null;
                Integer driverId = req.containsKey("driverId") ? req.getInt("driverId") : null;

                Integer appId = new MySQLQuery("SELECT id FROM system_app WHERE package_name = 'com.qualisys.mimon'").getAsInteger(conn);
                if (appId == null) {
                    throw new Exception("Aplicativo de Clientes sin entrada en Base de Datos");
                }

                switch (header) {
                    case "accepted":
                        Double driverLat = Double.parseDouble(req.getString("driverLat"));
                        Double driverLon = Double.parseDouble(req.getString("driverLon"));
                        String driverName = req.getString("driverName");
                        String vehicle = req.getString("vehicle");
                        String phone = req.getString("phone");

                        Integer indexId = new MySQLQuery("SELECT index_id FROM ord_cyl_order WHERE id = " + orderId).getAsInteger(conn);
                        String clieDoc = new MySQLQuery("SELECT document FROM ord_contract_index WHERE id = " + indexId).getAsString(conn);

                        boolean cancelled = new MySQLQuery("SELECT COUNT(*)>0 FROM ord_cyl_order "
                                + "WHERE id = " + orderId + " "
                                + "AND cancel_cause_id IS NOT NULL").getAsBoolean(conn);

                        if (!cancelled) {
                            int enterpriseId = new MySQLQuery("SELECT e.id "
                                    + "FROM vehicle v "
                                    + "INNER JOIN agency a ON v.agency_id = a.id "
                                    + "INNER JOIN enterprise e ON a.enterprise_id = e.id "
                                    + "WHERE v.id = " + vehId).getAsInteger(conn);

                            new MySQLQuery("UPDATE ord_cyl_order "
                                    + "SET enterprise_id = " + enterpriseId + ", "
                                    + "driver_id = " + driverId + ", "
                                    + "vehicle_id = " + vehId + ", "
                                    + "assig_hour = CURTIME() "
                                    + "WHERE id = " + orderId).executeUpdate(conn);

                            //Enviar push a cliente
                            JsonObjectBuilder sob = Json.createObjectBuilder();
                            sob.add("type", "accepted");
                            sob.add("orderId", orderId);
                            sob.add("driverId", driverId);
                            sob.add("driverLat", driverLat);
                            sob.add("driverLon", driverLon);
                            sob.add("driverName", driverName);
                            sob.add("vehicle", vehicle);
                            sob.add("phone", phone);
                            sob.add("subject", "Pedido Confirmado");
                            sob.add("brief", "Tu pedido ya está en camino");

                            if (clientId != null) {
                                GCMUtils.sendToApp(appId, sob.build(), poolName, null, String.valueOf(clientId));
                            }
                            
                            if (indexId != null) {
                                out.add("indexId", indexId);
                                out.add("clieDoc", clieDoc);
                            }
                            out.add("msg", "Pedido Confirmado");
                            out.add("status", STATUS_OK);
                        } else {
                            out.add("msg", "Lo sentimos. El pedido fue cancelado o asignado a otro conductor");
                            out.add("status", STATUS_ERROR);
                        }

                        break;

                    case "arrive":

                        JsonObjectBuilder aob = Json.createObjectBuilder();
                        aob.add("subject", "¡Atención!");
                        aob.add("brief", "Tu pedido ha llegado");
                        aob.add("type", "arrival");

                        GCMUtils.sendToApp(appId, aob.build(), poolName, null, String.valueOf(clientId));
                        out.add("status", STATUS_OK);

                        break;

                    case "refuse":

                        boolean inOrder = req.getBoolean("inOrder");
                        int eventId = req.getInt("eventId");

                        if (inOrder) {
                            new MySQLQuery("UPDATE ord_cyl_order SET "
                                    + "enterprise_id = NULL, "
                                    + "driver_id = NULL, "
                                    + "vehicle_id = NULL, "
                                    + "assig_hour = NULL "
                                    + "WHERE id = " + orderId).executeUpdate(conn);

                            new MySQLQuery("INSERT INTO ord_driver_order_hist "
                                    + "SET driver_id = " + driverId + ", "
                                    + "vehicle_id = " + vehId + ", "
                                    + "order_id = " + orderId + ", "
                                    + "event_id = " + eventId).executeInsert(conn);

                            JsonObjectBuilder rob = Json.createObjectBuilder();
                            rob.add("subject", "¡Atención!");
                            rob.add("brief", "El vendedor ha cancelado");
                            rob.add("message", "Lo sentimos. Ha ocurrido un inconveniente y su pedido será asignado desde la oficina de Atención al Cliente.");
                            rob.add("type", "refuse");

                            if (clientId != null) {
                                GCMUtils.sendToApp(appId, rob.build(), poolName, null, String.valueOf(clientId));
                            }
                        }
                        new MySQLQuery("INSERT INTO ord_veh_event SET vehicle_id = " + vehId + ", event_type_id = " + eventId + ", ev_date = NOW()").executeInsert(conn);
                        out.add("status", STATUS_OK);
                        break;

                    case "confirm":
                        String cylTypes = req.getString("cylTypes");
                        String cylCount = req.getString("cylCount");
                        String driverNameC = req.getString("driverName");
                        String vehicleC = req.getString("vehicle");
                        String phoneC = req.getString("phone");

                        new MySQLQuery("UPDATE ord_cyl_order SET "
                                + "confirm_hour = CURTIME() "
                                + "WHERE id = " + orderId).executeUpdate(conn);

                        new MySQLQuery("INSERT INTO ord_driver_order_hist SET "
                                + "driver_id = " + driverId + ", "
                                + "vehicle_id = " + vehId + ","
                                + "order_id = " + orderId).executeInsert(conn);

                        JsonObjectBuilder rob = Json.createObjectBuilder();
                        rob.add("subject", "Pedido Registrado");
                        rob.add("brief", "Por favor confirma tu pedido");
                        rob.add("type", "confirm");
                        rob.add("orderId", orderId);
                        rob.add("cylTypes", cylTypes);
                        rob.add("cylCount", cylCount);
                        rob.add("driverName", driverNameC);
                        rob.add("vehicle", vehicleC);
                        rob.add("phone", phoneC);

                        if (clientId != null) {
                            GCMUtils.sendToApp(appId, rob.build(), poolName, null, String.valueOf(clientId));
                        }
                        break;

                    case "cancel":

                        Integer cancelId = req.getInt("cancelId");

                        new MySQLQuery("UPDATE ord_cyl_order SET cancel_cause_id = " + cancelId + " WHERE id = " + orderId).executeUpdate(conn);
                        new MySQLQuery("INSERT INTO ord_driver_order_hist SET "
                                + "driver_id = " + driverId + ","
                                + "vehicle_id = " + vehId + ","
                                + "order_id = " + orderId + ","
                                + "cancel_cause_id = " + cancelId).executeInsert(conn);
                        //Enviar push de cancelación al usuario
                        break;

                    default:
                        throw new Exception("Opción no válida");
                }
            } catch (Exception ex) {
                Logger.getLogger(OrderResponse.class.getName()).log(Level.SEVERE, null, ex);
                out.add("msg", ex.getMessage());
                out.add("status", STATUS_ERROR);
            } finally {
                w.writeObject(out.build());
            }
        } catch (Exception ex) {
            Logger.getLogger(OrderResponse.class.getName()).log(Level.SEVERE, null, ex);
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

}
