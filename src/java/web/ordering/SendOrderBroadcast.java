package web.ordering;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
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
import utilities.MySQLQuery;
import web.push.GCMUtils;

@MultipartConfig
@WebServlet(name = "SendOrderBroadcast", urlPatterns = {"/SendOrderBroadcast"})
public class SendOrderBroadcast extends HttpServlet {

    Timer timer = new Timer();
    private static final String STATUS_ERROR = "ERROR";
    private static final String STATUS_OK = "OK";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection con = null;

        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {

            String poolName = request.getParameter("poolName");

            con = MySQLCommon.getConnection(poolName, null);
            JsonObjectBuilder out = Json.createObjectBuilder();

            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");
            JsonObject req = MySQLQuery.scapeJsonObj(request);

            String header = req.getString("header");
            boolean hidden = req.containsKey("hidden") ? req.getBoolean("hidden") : false;
            int orderId = req.getInt("orderId");

            try {
                int appId = new MySQLQuery("SELECT id FROM system_app WHERE package_name = 'com.glp.subsidiosonline'").getAsInteger(con);
                switch (header) {
                    case "request":
                        Integer clientId = req.containsKey("clientId") ? req.getInt("clientId") : null;
                        String address = req.getString("address");
                        double clientLat = Double.parseDouble(req.getString("lat"));
                        double clientLon = Double.parseDouble(req.getString("lon"));
                        String neighName = req.getString("neighName");
                        String sectorName = req.getString("sectorName");
                        JsonArray cyls = req.getJsonArray("cyls");
                        String subject = req.getString("subject");
                        String brief = req.getString("brief");
                        Integer drvId = (req.containsKey("drvId") ? req.getInt("drvId") : null);
                        int userTz = req.getInt("timeZone");

                        int offset = TimeZone.getDefault().getOffset(new Date().getTime());
                        int serverTz = offset / (1000 * 60 * 60);
                        int dif = userTz - serverTz;

                        if (dif < 0) {
                            dif = dif * -1;
                        }

                        DriverCoords nearest = null;
                        DriverCoords secondNearest = null;
                        if (drvId == null) {
                            Object[][] coordsData = new MySQLQuery("SELECT "
                                    + "latitude,"
                                    + "longitude,"
                                    + "employee_id,"
                                    + "`date` "
                                    + "FROM gps_coordinate "
                                    + "WHERE `date` > DATE_SUB(CURTIME(),INTERVAL '" + dif + ":10' HOUR_MINUTE) "
                                    + "AND latitude != 0").getRecords(con);

                            List<DriverCoords> lastCoords = new ArrayList<>();
                            for (int i = 0; i < coordsData.length; i++) {
                                DriverCoords driverCoord = DriverCoords.getFromRow(coordsData[i]);
                                boolean exist = false;
                                for (int j = 0; j < lastCoords.size(); j++) {
                                    DriverCoords auxItem = lastCoords.get(j);
                                    if (driverCoord.empId == auxItem.empId) {
                                        exist = true;
                                        if (driverCoord.date.after(auxItem.date)) {
                                            lastCoords.set(j, driverCoord);
                                            break;
                                        }
                                    }
                                }
                                if (!exist) {
                                    lastCoords.add(driverCoord);
                                }
                            }

                            for (int i = 0; i < lastCoords.size(); i++) {
                                DriverCoords item = lastCoords.get(i);
                                double dist = distFrom(clientLat, clientLon, item.lat, item.lon);

                                if (dist > 1.5) {
                                    lastCoords.remove(i);
                                    i--;
                                } else {
                                    boolean onFrec = new MySQLQuery("SELECT COUNT(*)>0 "
                                            + "FROM ord_veh_event e "
                                            + "INNER JOIN ord_veh_event_type et ON e.event_type_id = et.id "
                                            + "INNER JOIN driver_vehicle dv ON dv.vehicle_id = e.vehicle_id "
                                            + "INNER JOIN dto_salesman ds ON ds.driver_id = dv.driver_id "
                                            + "INNER JOIN vehicle v ON dv.vehicle_id = v.id "
                                            + "WHERE dv.driver_id = " + item.empId + " "
                                            + "AND v.active = 1 "
                                            + "AND v.visible = 1 "
                                            + "AND et.short_name = 'EF'"
                                            + "AND e.id = (SELECT MAX(id) FROM ord_veh_event WHERE vehicle_id = v.id)").getAsBoolean(con);

                                    boolean inOrder = new MySQLQuery("SELECT COUNT(*)>0 "
                                            + "FROM ord_cyl_order "
                                            + "WHERE driver_id = " + item.empId + " "
                                            + "AND `day` = CURDATE() "
                                            + "AND cancel_cause_id IS NULL "
                                            + "AND confirm_hour IS NULL").getAsBoolean(con);

                                    if (!onFrec || inOrder) {
                                        lastCoords.remove(i);
                                        i--;
                                    }

                                }
                            }

                            if (lastCoords.size() > 1) {
                                nearest = lastCoords.get(0);
                                secondNearest = lastCoords.get(1);
                                double minD = distFrom(clientLat, clientLon, nearest.lat, nearest.lon);
                                for (int i = 1; i < lastCoords.size(); i++) {
                                    double currentD = distFrom(clientLat, clientLon, lastCoords.get(i).lat, lastCoords.get(i).lon);
                                    if (minD > currentD) {
                                        minD = currentD;
                                        secondNearest = nearest;
                                        nearest = lastCoords.get(i);
                                    }
                                }
                            } else if (lastCoords.size() == 1) {
                                nearest = lastCoords.get(0);
                            }
                        }

                        JsonObjectBuilder ob = Json.createObjectBuilder();
                        ob.add("orderId", orderId);
                        if (clientId != null) {
                            ob.add("clientId", clientId);
                        }
                        ob.add("clientLat", clientLat);
                        ob.add("clientLon", clientLon);
                        ob.add("address", address);
                        ob.add("sectorName", sectorName);
                        ob.add("neighName", neighName);
                        ob.add("cyls", cyls);
                        ob.add("subject", subject);
                        ob.add("brief", brief);
                        ob.add("dt", Dates.getCheckFormat().format(new Date()));
                        ob.add("type", "order");

                        if (hidden) {
                            ob.addNull("hidden");
                        }

                        JsonObject jo = ob.build();

                        if (drvId != null || nearest != null) {
                            GCMUtils.sendToApp(appId, jo, poolName, null, String.valueOf((drvId != null ? drvId : nearest.empId)));
                        }
                        if (secondNearest != null) {
                            startTimer(poolName, orderId, jo, secondNearest.empId);
                        }
                        out.add("status", STATUS_OK);
                        break;

                    case "cancel":

                        int cancelId = req.getInt("cancelId");
                        int driverId = req.getInt("driverId");
                        String cause = req.getString("cause");

                        //Insertar log para el historico de pedidos del cliente
                        //PENDIENTE 
                        new MySQLQuery("UPDATE ord_cyl_order SET "
                                + "cancel_cause_id = ?1 "
                                + "WHERE id = ?2")
                                .setParam(1, cancelId)
                                .setParam(2, orderId).executeUpdate(con);

                        JsonObjectBuilder cob = Json.createObjectBuilder();
                        cob.add("type", "cancelOrder");
                        cob.add("subject", "¡Atención!");
                        cob.add("brief", "El cliente ha cancelado");
                        cob.add("cause", cause);

                        GCMUtils.sendToApp(appId, cob.build(), poolName, null, String.valueOf(driverId));
                        out.add("status", STATUS_OK);
                        break;
                }
            } catch (Exception ex) {
                Logger.getLogger(SendOrderBroadcast.class.getName()).log(Level.SEVERE, null, ex);
                out.add("status", STATUS_ERROR);
            } finally {
                w.writeObject(out.build());
            }
        } catch (Exception ex) {
            Logger.getLogger(SendOrderBroadcast.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySQLSelect.tryClose(con);
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

    private static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371.0; // miles 3958.75 (or 6371.0 kilometers)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;
        return dist;
    }

    public void startTimer(final String poolName, final int orderId, final JsonObject ob, final int empId) throws Exception {

        final Connection con = MySQLCommon.getConnection(poolName, null);

        TimerTask task = new TimerTask() {

            int s = 0;
            boolean firstDriverConsulted = false;

            @Override
            public boolean cancel() {
                MySQLCommon.closeConnection(con);
                timer = new Timer();
                return super.cancel();
            }

            @Override
            public void run() {
                if (s == 65) {
                    try {

                        boolean orderAccepted = new MySQLQuery("SELECT COUNT(*)>0 FROM ord_cyl_order "
                                + "WHERE id = ?1 "
                                + "AND driver_id IS NOT NULL "
                                + "AND vehicle_id IS NOT NULL").setParam(1, orderId).getAsBoolean(con);

                        if (orderAccepted) {
                            timer.cancel();
                            timer.purge();
                        } else if (!orderAccepted && !firstDriverConsulted) {
                            GCMUtils.sendToApp(11, ob, poolName, null, String.valueOf(empId));
                            s = 0;
                        } else if (!orderAccepted && firstDriverConsulted) {
                            timer.cancel();
                            timer.purge();
                        }

                        firstDriverConsulted = true;

                    } catch (Exception ex) {
                        Logger.getLogger(SendOrderBroadcast.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                s++;
            }
        };
        timer.schedule(task, 0, 1000);

    }
}

class DriverCoords {

    double lat;
    double lon;
    int empId;
    Date date;

    public static DriverCoords getFromRow(Object[] row) {
        DriverCoords item = new DriverCoords();
        item.lat = MySQLQuery.getAsDouble(row[0]);
        item.lon = MySQLQuery.getAsDouble(row[1]);
        item.empId = MySQLQuery.getAsInteger(row[2]);
        item.date = MySQLQuery.getAsDate(row[3]);

        return item;
    }

}
