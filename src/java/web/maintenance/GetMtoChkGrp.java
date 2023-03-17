package web.maintenance;

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
import printout.basics.groups.MtoChkGrp;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "GetMtoChkGrp", urlPatterns = {"/GetMtoChkGrp"})
public class GetMtoChkGrp extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        JsonObject pars = MySQLQuery.scapeJsonObj(request);

        try {
            JsonObjectBuilder ob = Json.createObjectBuilder();
            try (Connection conn = MySQLCommon.getConnection(pars.getString("poolName"), pars.getString("tz"))) {
                try {
                    ob.add("data", MtoChkGrp.getJsonGrps(pars.getInt("version"), pars.getInt("vehicle"), conn));
                    ob.add("status", "ok");
                    try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
                        w.writeObject(ob.build());
                    }
                } catch (Exception ex) {
                    throw ex;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(GetMtoChkGrp.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500, ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
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
