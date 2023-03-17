package web.helpdesk;

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
import service.MySQL.MySQLCommon;
import service.MySQL.MySQLSelect;
import utilities.Dates;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "sendHlpPush", urlPatterns = {"/sendHlpPush"})
public class sendHlpPush extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        Connection conn = null;
        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {

            JsonObjectBuilder obs = Json.createObjectBuilder();
            JsonObject req = MySQLQuery.scapeJsonObj(request);

            String subject = req.getString("subject");
            String message = req.getString("message");
            String poolName = req.getString("poolName");
            String tz = req.getString("tz");
            String perEmployee = req.getString("per_employee");

            try {
                conn = MySQLCommon.getConnection(poolName, tz);
                
                String employee = new MySQLQuery("SELECT id FROM employee WHERE per_employee_id = " + perEmployee + " AND active ").getAsString(conn);
                int appId = new MySQLQuery("SELECT id FROM system_app WHERE package_name = 'com.qualisys.helpdesk'").getAsInteger(conn);
               
                JsonObjectBuilder ob = Json.createObjectBuilder();
                ob.add("subject", subject);
                ob.add("message", message);
                ob.add("user", "HelpDesk");
                ob.add("dt", Dates.getCheckFormat().format(new Date()));
                GCMHlp.sendToHlpApp(appId, ob.build(), poolName, tz, employee);
                obs.add("status", "OK");

            } catch (Exception ex) {
                Logger.getLogger(sendHlpPush.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(sendHlpPush.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySQLSelect.tryClose(conn);
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
