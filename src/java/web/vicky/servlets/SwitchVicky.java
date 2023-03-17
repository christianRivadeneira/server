package web.vicky.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import web.vicky.model.VickyCfg;

@WebServlet(name = "SwitchVicky", urlPatterns = {"/vicky/SwitchVicky"})
public class SwitchVicky extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> req = MySQLQuery.scapedParams(request);
        String key = req.get("key");
        try (PrintWriter pw = new PrintWriter(response.getOutputStream())) {
            if (key.equals("Fsk129azF")) {
                try (Connection conn = MySQLCommon.getConnection("sigmads", null)) {
                    VickyCfg cfg = VickyCfg.select(1, conn);
                    cfg.enabled = !cfg.enabled;
                    new MySQLQuery(VickyCfg.getUpdateQuery(cfg)).executeUpdate(conn);
                    if (cfg.enabled) {
                        pw.write("Vicky was enabled!");
                    } else {
                        pw.write("Vicky was disabled!");
                    }
                }
            } else {
                pw.write("Incorrect Key!");
            }
        } catch (Exception ex) {
            Logger.getLogger(SwitchVicky.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500);
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
