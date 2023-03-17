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

@WebServlet(name = "sendMimonPush", urlPatterns = {"/sendMimonPush"})
public class sendMimonPush extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        MySQLQuery.scapeJsonObj(request);
        JsonObject req1;
        try (JsonReader r = Json.createReader(request.getInputStream())) {
            req1 = r.readObject();
        }

        JsonObject req = (JsonObject) MySQLQuery.scapeJsonObj(req1);
        String sessionId = req.getString("sessionId");
        String cliIds = req.containsKey("cliIds") ? req.getString("cliIds") : null;
        int ticketId = req.getInt("ticketId");
        String message = req.getString("message");
        String poolName = req.getString("poolName");
        String tz = req.getString("tz");
        boolean hidden = req.containsKey("hidden") ? req.getBoolean("hidden") : false;
        Connection con = null;
        try (PrintWriter out = response.getWriter()) {
            try {
                con = MySQLCommon.getConnection(poolName, tz);
                int appId = new MySQLQuery("SELECT id FROM system_app WHERE package_name = 'qualisys.co.com.montagas'").getAsInteger(con);
                Object[] row = new MySQLQuery("SELECT "
                        + "mt.subject, "
                        + "t.`status`, "
                        + "(SELECT `date` FROM clie_msg_ticket WHERE ticket_id = t.id ORDER BY id DESC LIMIT 1) "
                        + "FROM clie_msg_ticket mt "
                        + "INNER JOIN clie_ticket t ON mt.ticket_id = t.id "
                        + "WHERE t.id = " + ticketId + " "
                        + "ORDER BY mt.id ASC LIMIT 1").getRecord(con);

                SessionLogin si = SessionLogin.validate(sessionId, con);
                JsonObjectBuilder ob = Json.createObjectBuilder();
                ob.add("ticketId", ticketId);
                ob.add("msg", message);
                ob.add("user", si.firstName + " " + si.lastName);
                ob.add("type", "ticket");
                ob.add("ticketDay", MySQLQuery.getAsString(row[2]));
                ob.add("ticketSubject", MySQLQuery.getAsString(row[0]));
                ob.add("ticketStatus", MySQLQuery.getAsString(row[1]));
                ob.add("msgPend", 1);

                ob.add("dt", Dates.getCheckFormat().format(new Date()));
                if (hidden) {
                    ob.addNull("hidden");
                }
                FCMClient.sendToCliApp(appId, ob.build(), poolName, tz, cliIds);
                out.write("ok");
            } catch (Exception ex) {
                Logger.getLogger(sendSalesPush.class.getName()).log(Level.SEVERE, null, ex);
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
