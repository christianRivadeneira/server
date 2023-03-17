package web.gps;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "GoogleGeocode", urlPatterns = {"/GoogleGeocode"})
public class GoogleGeocode extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String address = request.getParameter("address");
        String poolName = request.getParameter("poolName");
        String tz = request.getParameter("tz");
        if (poolName == null) {
            poolName = "sigmads";
            tz = null;
        }

        JsonObject req;
        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            JsonObjectBuilder root = Json.createObjectBuilder();

            JsonArrayBuilder jar = Json.createArrayBuilder();
            JsonObjectBuilder error = Json.createObjectBuilder();

            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");
            try (Connection con = MySQLCommon.getConnection(poolName, tz)) {

                String googleKey = new MySQLQuery("SELECT geocoder_key FROM com_cfg WHERE id = 1").getAsString(con);
                HttpURLConnection googleConn = (HttpURLConnection) new URL("https://maps.googleapis.com/maps/api/geocode/json?sensor=false&address=" + address + "&components=country:Colombia&region=co&key=" + googleKey).openConnection();
                googleConn.setConnectTimeout(10000);
                googleConn.setReadTimeout(10000);
                googleConn.addRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
                req = Json.createReader(new BufferedReader(new InputStreamReader(new BufferedInputStream(googleConn.getInputStream()), "UTF-8"))).readObject();
                String status = req.getString("status");
                switch (status) {
                    case "OK":// indica que la respuesta contiene almenos un result válido.
                        JsonArray results = req.getJsonArray("results");
                        for (int i = 0; i < results.size(); i++) {
                            JsonObject o = results.getJsonObject(i);
//                            System.out.println("" + o.getString("formatted_address"));
//                            System.out.println("" + o.getJsonObject("geometry").getJsonObject("location").getJsonNumber("lat"));
//                            System.out.println("" + o.getJsonObject("geometry").getJsonObject("location").getJsonNumber("lng"));

                            JsonObjectBuilder obj = Json.createObjectBuilder();
                            obj.add("address", o.getString("formatted_address"));
                            obj.add("lat", o.getJsonObject("geometry").getJsonObject("location").getJsonNumber("lat"));
                            obj.add("lon", o.getJsonObject("geometry").getJsonObject("location").getJsonNumber("lng"));
                            jar.add(obj.build());
                        }
                        break;
                    case "NOT_FOUND"://indica que no se pudo geocodificar al menos a una de las ubicaciones especificadas en el origen, el destino o los waypoints de la solicitud.
                        error.add("error", "No se pudo Geocodificar " + status);
                        break;
                    case "ZERO_RESULTS":// indica que no fue posible hallar una ruta entre el origen y el destino.
                        error.add("error", "No se hayaron resultados " + status);
                        break;
                    case "MAX_WAYPOINTS_EXCEEDED":// indica se proporcionaron demasiados waypoint en la solicitud. La cantidad máxima de waypoints permitidos es 23, más el origen y el destino. (Si la solicitud no incluye una clave de API, la cantidad máxima permitida de waypoints será de 8. Los clientes de Google Maps API for Work pueden enviar solicitudes con hasta 23 waypoints).
                        error.add("error", "Se ha exedido el número de resultados " + status);
                        break;
                    case "INVALID_REQUEST":// indica que la solicitud proporcionada no era válida. Las causas más comunes por las que se produce este estado incluyen un parámetro o valor de paránetro no válido.
                        error.add("error", "Solicitud no valida " + status);
                        break;
                    case "OVER_QUERY_LIMIT":// indica que el servicio recibió demasiadas solicitudes desde tu aplicación dentro del período permitido.
                        error.add("error", "Se ha excedido el numero de solicitudes " + status);
                        break;
                    case "REQUEST_DENIED":// indica que el servicio no permitió que tu aplicación usara el servicio de indicaciones.
                        error.add("error", "Error en el servicio de Google " + status);
                        break;
                    case "UNKNOWN_ERROR":// indica que no se pudo procesar una solicitud de indicaciones debido a un error en el servidor. La solicitud puede tener éxito si realizas un nuevo intento.
                        error.add("error", "Error en el servicio de Google " + status);
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                error.add("error", "Error Desconocido");
                Logger.getLogger(GoogleGeocode.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                root.add("data", jar.build());
                root.add("error", error.build());
                w.writeObject(root.build());
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
