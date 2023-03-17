package web.ordering;

import java.io.IOException;
import java.sql.Connection;
import java.util.Map;
import java.util.Timer;
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
@WebServlet(name = "SendPQRPush", urlPatterns = {"/SendPQRPush"})
public class SendPQRPush extends HttpServlet {

    private static final String STATUS_ERROR = "ERROR";
    private static final String STATUS_OK = "OK";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection con = null;

        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {

            Map<String, String> requ = MySQLQuery.scapedParams(request);
            String poolName = requ.get("poolName");
            poolName = (poolName != null ? poolName : "sigmads");

            con = MySQLCommon.getConnection(poolName, null);
            JsonObjectBuilder out = Json.createObjectBuilder();

            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");
            JsonObject req = MySQLQuery.scapeJsonObj(request);

            String header = req.getString("header");

            try {
                int appId = new MySQLQuery("SELECT id FROM system_app WHERE package_name = 'com.qualisys.readings'").getAsInteger(con);
                switch (header) {
                    case "push":
                        int empId = req.getInt("empId");
                        JsonObjectBuilder cob = Json.createObjectBuilder();
                        cob.add("type", req.getString("type"));
                        cob.add("main", req.getString("main"));
                        cob.add("sec", req.getString("sec"));
                        cob.add("third", req.getString("third"));
                        
                        GCMUtils.sendToAppReadings(appId, cob.build(), poolName, null, String.valueOf(empId));
                        out.add("status", STATUS_OK);
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                Logger.getLogger(SendPQRPush.class.getName()).log(Level.SEVERE, null, ex);
                out.add("status", STATUS_ERROR);
            } finally {
                w.writeObject(out.build());
            }
        } catch (Exception ex) {
            Logger.getLogger(SendPQRPush.class.getName()).log(Level.SEVERE, null, ex);
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
}
