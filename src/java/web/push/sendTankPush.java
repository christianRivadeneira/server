package web.push;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.Dates;
import utilities.MySQLQuery;

@WebServlet(name = "sendTankPush", urlPatterns = {"/sendTankPush"})
public class sendTankPush extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        JsonObject req1;
        try (JsonReader r = Json.createReader(request.getInputStream())) {
            req1 = r.readObject();
        }

        JsonObject req = (JsonObject) MySQLQuery.scapeJsonObj(req1);
        String sessionId = req.getString("sessionId");
        String employeeIds = req.containsKey("employeeIds") ? req.getString("employeeIds") : null;
        String execId = req.containsKey("execId") ? req.getString("execId") : null;
        String subject = req.getString("subject");
        String brief = req.getString("brief");
        String message = req.getString("message");
        String poolName = req.getString("poolName");
        String tz = req.getString("tz");
        boolean hidden = req.containsKey("hidden") ? req.getBoolean("hidden") : false;
        Connection con = null;
        try (PrintWriter out = response.getWriter()) {
            try {
                con = MySQLCommon.getConnection(poolName, tz);
                int appId = new MySQLQuery("SELECT id FROM system_app WHERE package_name = 'com.qualisys.tanks'").getAsInteger(con);
                SessionLogin si = SessionLogin.validate(sessionId, con);
                JsonObjectBuilder ob = Json.createObjectBuilder();
                ob.add("subject", subject);
                ob.add("brief", brief);
                ob.add("message", message);
                ob.add("user", si.firstName + " " + si.lastName);
                ob.add("dt", Dates.getCheckFormat().format(new Date()));
                if (req.containsKey("type")) {
                    ob.add("type", req.getString("type"));
                }
                if (hidden) {
                    ob.addNull("hidden");
                }
                GCMUtils.sendToApp(appId, ob.build(), poolName, tz, employeeIds);

                if (execId != null) {
                    JsonObjectBuilder ob1 = Json.createObjectBuilder();
                    ob1.add("type", "winnerPromo");
                    ob1.add("subject", subject);
                    ob1.add("brief", brief);
                    ob1.add("message", message);
                    ob1.add("user", si.firstName + " " + si.lastName);
                    ob1.add("dt", Dates.getCheckFormat().format(new Date()));

                    Integer empId = new MySQLQuery("SELECT id FROM employee WHERE active AND per_employee_id = " + execId).getAsInteger(con);
                    if (empId != null) {
                        GCMUtils.sendToAppManagers(ob1.build(), String.valueOf(empId), con);
                    }
                }
                out.write("ok");
            } catch (Exception ex) {
                Logger.getLogger(sendTankPush.class.getName()).log(Level.SEVERE, null, ex);
                out.write("error");
            } finally {
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
        return "Short description";
    }// </editor-fold>

}
