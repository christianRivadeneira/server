package web;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
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

@WebServlet(name = "enterpriseLogo", urlPatterns = {"/enterpriseLogo"})
public class enterpriseLogo extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        String poolName = pars.get("poolName");
        String i = pars.get("i");
        try (Connection con = MySQLCommon.getConnection(poolName, null);) {
            File f = getEnterpriseLogo(i, con);
            if (f != null) {
                response.setContentLength((int) f.length());
                FileInputStream fis = new FileInputStream(f);
                fileManager.copy(fis, response.getOutputStream());
            } else {
                writeEmptyImage(response.getOutputStream());
            }
        } catch (Exception ex) {
            Logger.getLogger(enterpriseLogo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void writeEmptyImage(OutputStream os) throws IOException {
        //png transparente de 1x1
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 1, 0, 0, 0, 1, 1, 3, 0, 0, 0, 37, -37, 86, -54, 0, 0, 0, 3, 80, 76, 84, 69, 0, 0, 0, -89, 122, 61, -38, 0, 0, 0, 1, 116, 82, 78, 83, 0, 64, -26, -40, 102, 0, 0, 0, 10, 73, 68, 65, 84, 8, -41, 99, 96, 0, 0, 0, 2, 0, 1, -30, 33, -68, 51, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126});
        fileManager.copy(bais, os);
    }

    public static File getEnterpriseLogo(String ownerId, Connection con) throws Exception {
        Integer id = new MySQLQuery("SELECT id FROM bfile WHERE owner_type = 29 AND owner_id = " + ownerId).getAsInteger(con);
        if (id != null && id > 0) {            
            return new fileManager.PathInfo(con).getExistingFile(id);
        }
        return null;
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
