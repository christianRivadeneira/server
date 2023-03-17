package web.push;

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
import utilities.Dates;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "getLogs", urlPatterns = {"/getLogs"})
public class getLogs extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject req = (JsonObject) MySQLQuery.scapeJsonObj(request);
        String sessionId = req.getString("sessionId");
        String empId = req.getString("empId");
        String type = req.getString("type");
        String poolName = req.getString("poolName");
        String tz = req.getString("tz");
        String pack = req.containsKey("package") ? req.getString("package") : "com.glp.subsidiosonline";
        boolean hidden = req.containsKey("hidden") ? req.getBoolean("hidden") : false;
        Connection con = null;
        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            JsonObjectBuilder rta = Json.createObjectBuilder();
            try {
                con = MySQLCommon.getConnection(poolName, tz);
                int appId = new MySQLQuery("SELECT id FROM system_app WHERE package_name = '" + pack + "'").getAsInteger(con);
                SessionLogin si = SessionLogin.validate(sessionId, con);
                JsonObjectBuilder ob = Json.createObjectBuilder();
                ob.add("type", type);
                ob.add("user", si.firstName + " " + si.lastName);
                ob.add("dt", Dates.getCheckFormat().format(new Date()));
                if (hidden) {
                    ob.addNull("hidden");
                }
                GCMUtils.sendToApp(appId, ob.build(), poolName, tz, empId);
                rta.add("result", "ok");
            } catch (Exception ex) {
                Logger.getLogger(getLogs.class.getName()).log(Level.SEVERE, null, ex);
                rta.add("result", "error");
                rta.add("msg", ex.getMessage());
            } finally {
                w.writeObject(rta.build());
                MySQLCommon.closeConnection(con);
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "get logs";
    }// </editor-fold>

}
