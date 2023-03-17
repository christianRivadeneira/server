package web.closeSession;

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
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "CloseSessions", urlPatterns = {"/CloseSessions"})
public class CloseSessions extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            JsonObjectBuilder ob = Json.createObjectBuilder();
            JsonObject req = MySQLQuery.scapeJsonObj(request);
            String header = req.getString("header");
            try (Connection conn = MySQLCommon.getConnection("sigmads", null)) {
                SessionLogin.validate(req.getString("sessionId"), conn, null);
                switch (header) {
                    case "closeSessions": {
                        Integer empId = req.getInt("empId");
                        boolean uniMovil = req.getBoolean("uniMovil");
                        boolean uniDesktop = req.getBoolean("uniDesktop");
                        if (uniDesktop) {
                            new MySQLQuery("UPDATE session_login SET end_time = now() WHERE type = 'pc' AND employee_id = " + empId + " AND end_time IS NULL").executeUpdate(conn);
                        }
                        if (uniMovil) {
                            new MySQLQuery("UPDATE session_login SET end_time = now() WHERE type = 'android' AND employee_id = " + empId + " AND end_time IS NULL").executeUpdate(conn);
                        }
                        break;
                    }
                    default:
                        break;
                }
                ob.add("status", "OK");
            } catch (Exception ex) {
                Logger.getLogger(CloseSessions.class.getName()).log(Level.SEVERE, null, ex);
                ob.add("status", "ERROR");
                String msg = ex.getMessage();
                if (ex.getMessage() != null && !msg.isEmpty()) {
                    ob.add("errorMsg", msg);
                } else {
                    ob.add("errorMsg", "Error desconocido.");
                }
            } finally {
                w.writeObject(ob.build());
            }
        } catch (Exception ex) {
            Logger.getLogger(CloseSessions.class.getName()).log(Level.SEVERE, null, ex);
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
        return "SessionClose";
    }

}
