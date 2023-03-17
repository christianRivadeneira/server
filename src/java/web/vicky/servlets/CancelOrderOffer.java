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
import web.vicky.beans.CheckOrderStatus;

@WebServlet(name = "CancelOrderOffer", urlPatterns = {"/vicky/CancelOrderOffer"})
public class CancelOrderOffer extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        Integer orderId = MySQLQuery.getAsInteger(pars.get("orderId"));
        Integer causeId = MySQLQuery.getAsInteger(pars.get("causeId"));
        Integer empId = MySQLQuery.getAsInteger(pars.get("empId"));
        Boolean offers = MySQLQuery.getAsBoolean(pars.get("offers"));

        try (PrintWriter pw = new PrintWriter(response.getOutputStream())) {
            try (Connection conn = MySQLCommon.getConnection("sigmads", null)) {
                CheckOrderStatus.cancelOrderOffer(orderId, causeId, empId, (offers != null ? offers : true), conn);
            }
            pw.write("ok");
        } catch (Exception ex) {
            Logger.getLogger(CancelOrderOffer.class.getName()).log(Level.SEVERE, null, ex);
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
