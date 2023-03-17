/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web.discount;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.MySQLPreparedInsert;
import utilities.MySQLQuery;
import web.fileManager;

@MultipartConfig
@WebServlet(name = "SyncMinasNifs", urlPatterns = {"/SyncMinasNifs"})
public class SyncMinasNifs extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try {
            File tmpZip = File.createTempFile("syncminas", ".gzip");
            try (FileOutputStream fosZip = new FileOutputStream(tmpZip)) {
                fileManager.copy(request.getPart("file").getInputStream(), new BufferedOutputStream(fosZip));
                File tmpCsv = File.createTempFile("syncminas", ".csv");
                try (FileInputStream fis = new FileInputStream(tmpZip); GZIPInputStream gin = new GZIPInputStream(fis); FileOutputStream fosCsv = new FileOutputStream(tmpCsv)) {
                    fileManager.copy(gin, fosCsv);
                    try (Connection conn = MySQLCommon.getConnection("sigmads", null); BufferedReader br = new BufferedReader(new FileReader(tmpCsv))) {
                        SessionLogin.validate(MySQLQuery.scape(request.getParameter("sessionId")), conn);
                        new MySQLQuery("TRUNCATE dto_minas_cyl").executeUpdate(conn);
                        MySQLPreparedInsert pi = new MySQLPreparedInsert("INSERT INTO dto_minas_cyl (`y`, `f`, `s`) VALUES (?1, ?2, ?3)", false, conn);

                        String line = br.readLine();
                        while ((line = br.readLine()) != null) {
                            String[] parts = line.split(",");
                            String nif = parts[1].replaceAll("[ ]+", "");
                            String status = parts[3];
                            if (status.equals("A")) {
                                Integer y = null;
                                Integer f = null;
                                Integer s = null;
                                if (nif.matches("[0-9]+")) {
                                    String fs = nif.substring(2, nif.length() - 6);
                                    y = Integer.valueOf(nif.substring(0, 2));
                                    f = Integer.valueOf(fs);
                                    s = Integer.valueOf(nif.substring(nif.length() - 6));
                                } else if (nif.matches("[0-9\\-]+")) {
                                    String[] nifParts = nif.split("-");
                                    y = Integer.valueOf(nifParts[0]);
                                    f = Integer.valueOf(nifParts[1]);
                                    s = Integer.valueOf(nifParts[2]);
                                } else {
                                    throw new Exception("El NIF solo debe tener números y guiones. " + nif);
                                }
                                pi.setParameter(1, y);
                                pi.setParameter(2, f);
                                pi.setParameter(3, s);
                                pi.addBatch();
                            }
                        }
                        pi.executeBatch();
                        pi.printStats("Inserts");
                        tmpZip.delete();
                        tmpCsv.delete();
                        new MySQLQuery("UPDATE trk_cyl c SET c.minas_reported = (SELECT COUNT(*) > 0 FROM dto_minas_cyl mc WHERE mc.`y` = c.nif_y AND mc.f = c.nif_f AND mc.s = c.nif_s)").executeUpdate(conn);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(SyncMinasNifs.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500, ex.getMessage());
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
