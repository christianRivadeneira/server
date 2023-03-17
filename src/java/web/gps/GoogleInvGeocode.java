package web.gps;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "GoogleInvGeocode", urlPatterns = {"/GoogleInvGeocode"})
public class GoogleInvGeocode extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> req = MySQLQuery.scapedParams(request);
        String lat = req.get("lat");
        String lon = req.get("lon");
        String poolName = req.get("poolName");
        String tz = req.get("tz");
        String packageName = req.get("package");
        packageName = packageName != null ? packageName : "qualisys.co.com.montagas";
        if (poolName == null) {
            poolName = "sigmads";
            tz = null;
        }

        try (PrintWriter w = new PrintWriter(response.getOutputStream())) {
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");
            try (Connection con = MySQLCommon.getConnection(poolName, tz)) {

                String googleKey = new MySQLQuery("SELECT google_server_key FROM system_app WHERE package_name = '" + packageName + "'").getAsString(con);
                String url = "https://maps.googleapis.com/maps/api/geocode/json?key=" + googleKey + "&latlng=" + lat + "," + lon;
                HttpURLConnection googleConn = (HttpURLConnection) new URL(url).openConnection();
                googleConn.setConnectTimeout(10000);
                googleConn.setReadTimeout(10000);
                JsonObject res = Json.createReader(new BufferedReader(new InputStreamReader(new BufferedInputStream(googleConn.getInputStream()), "UTF-8"))).readObject();
                String status = res.getString("status");
                switch (status) {
                    case "OK":// indica que la respuesta contiene almenos un result válido.
                        JsonArray results = res.getJsonArray("results");

                        for (int i = 0; i < results.size(); i++) {
                            JsonObject o = results.getJsonObject(i);
                            JsonArray resTypes = o.getJsonArray("types");
                            for (int j = 0; j < resTypes.size(); j++) {
                                if (resTypes.getString(j).equals("street_address")) {

                                    String addr = o.getString("formatted_address");

                                    //String locType = o.getJsonObject("geometry").getString("location_type");
                                    //if (locType.equals("ROOFTOP")) {
                                    if (addr.indexOf(",") > 0) {
                                        addr = addr.substring(0, addr.indexOf(","));
                                    }
                                    //} else if (locType.equals("RANGE_INTERPOLATED")) {
                                    if (addr.indexOf(" a ") > 0) {
                                        addr = addr.substring(0, addr.indexOf(" a "));
                                    }
                                    //}

                                    try {
                                        boolean replaced = false;
                                        if (o.containsKey("address_components")) {
                                            JsonArray comps = o.getJsonArray("address_components");
                                            for (int k = 0; k < comps.size() && !replaced; k++) {
                                                JsonObject comp = comps.getJsonObject(k);
                                                if (comp.containsKey("types")) {
                                                    JsonArray types = comp.getJsonArray("types");
                                                    for (int l = 0; l < types.size() && !replaced; l++) {
                                                        if (types.getString(l).equals("route")) {
                                                            String shortName = comp.getString("short_name");
                                                            String longName = comp.getString("long_name");
                                                            addr = addr.replace(shortName, longName);
                                                            replaced = true;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } catch (Exception ex) {
                                    }

                                    addr = addr.replace("-", " - ");
                                    addr = addr.replace(" #", " # ");
                                    w.write(addr);
                                    return;
                                }
                            }
                        }
                        break;
                    case "NOT_FOUND"://indica que no se pudo geocodificar al menos a una de las ubicaciones especificadas en el origen, el destino o los waypoints de la solicitud.
                        throw new Exception("No se pudo Geocodificar " + status);
                    case "ZERO_RESULTS":// indica que no fue posible hallar una ruta entre el origen y el destino.
                        w.write("");
                        break;
                    case "MAX_WAYPOINTS_EXCEEDED":// indica se proporcionaron demasiados waypoint en la solicitud. La cantidad máxima de waypoints permitidos es 23, más el origen y el destino. (Si la solicitud no incluye una clave de API, la cantidad máxima permitida de waypoints será de 8. Los clientes de Google Maps API for Work pueden enviar solicitudes con hasta 23 waypoints).
                        throw new Exception("Se ha exedido el número de resultados " + status);
                    case "INVALID_REQUEST":// indica que la solicitud proporcionada no era válida. Las causas más comunes por las que se produce este estado incluyen un parámetro o valor de paránetro no válido.
                        throw new Exception("Solicitud no valida " + status);
                    case "OVER_QUERY_LIMIT":// indica que el servicio recibió demasiadas solicitudes desde tu aplicación dentro del período permitido.
                        w.write("");
                        break;
                    //         throw new Exception("Se ha exedido el numero de solicitudes " + status);
                    case "REQUEST_DENIED":// indica que el servicio no permitió que tu aplicación usara el servicio de indicaciones.
                        throw new Exception("Error en el servicio de Google " + status);
                    case "UNKNOWN_ERROR":// indica que no se pudo procesar una solicitud de indicaciones debido a un error en el servidor. La solicitud puede tener éxito si realizas un nuevo intento.
                        throw new Exception("Error en el servicio de Google " + status);
                    default:
                        break;
                }
            } catch (Exception ex) {
                response.sendError(500, ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
                Logger.getLogger(GoogleInvGeocode.class.getName()).log(Level.SEVERE, null, ex);
            } finally {

            }
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
        return "Geocoder";
    }
}
