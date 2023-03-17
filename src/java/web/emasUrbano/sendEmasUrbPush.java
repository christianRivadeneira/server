package web.emasUrbano;

import java.io.IOException;
import java.sql.Connection;
import java.util.Date;
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
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import service.MySQL.MySQLSelect;
import utilities.Dates;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "sendEmasUrbPush", urlPatterns = {"/sendEmasUrbPush"})
public class sendEmasUrbPush extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            JsonObjectBuilder obs = Json.createObjectBuilder();
            JsonObject req = MySQLQuery.scapeJsonObj(request);

            String sessionId = req.getString("sessionId");
            SessionLogin.validate(sessionId);
            
            String type = req.getString("type");
            String neighIds = req.containsKey("neighIds") ? req.getString("neighIds") : null;
            String zoneId = req.containsKey("zoneId") ? req.getString("zoneId") : null;
            String subject = req.getString("subject");
            String brief = req.getString("brief");
            String message = req.getString("message");
            String poolName = req.getString("poolName");
            String tz = null;

            try {
                conn = MySQLCommon.getConnection(poolName, tz);
                int appId = new MySQLQuery("SELECT id FROM system_app WHERE package_name = 'com.qualisys.emasurbano'").getAsInteger(conn);
                JsonObjectBuilder ob = Json.createObjectBuilder();
                ob.add("subject", subject);
                ob.add("brief", brief);
                ob.add("message", message);
                ob.add("user", "Emas Pasto");
                ob.add("dt", Dates.getCheckFormat().format(new Date()));
                FCMEmas.sendToUrbApp(appId, ob.build(), poolName, tz, neighIds, zoneId, type);
                obs.add("status", "OK");
            } catch (Exception ex) {
                Logger.getLogger(sendEmasUrbPush.class.getName()).log(Level.SEVERE, null, ex);
                obs.add("status", "ERROR");
                String msg = ex.getMessage();
                if (ex.getMessage() != null && !msg.isEmpty()) {
                    obs.add("errorMsg", msg);
                } else {
                    obs.add("errorMsg", "Error desconocido.");
                }
            } finally {
                w.writeObject(obs.build());
            }

        } catch (Exception ex) {
            Logger.getLogger(sendEmasUrbPush.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySQLSelect.tryClose(conn);
        }
    }
    
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);

        } catch (Exception ex) {
            Logger.getLogger(sendEmasUrbPush.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);

        } catch (Exception ex) {
            Logger.getLogger(sendEmasUrbPush.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "sendEmasUrbPush";
    }
}
