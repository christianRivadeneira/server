package web.system.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import static web.GetRepoLibs.getMD5Checksum;
import web.fileManager;

@WebServlet(urlPatterns = {"/GetUpdaterFile"})
public class GetUpdaterFile extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        try (Connection con = MySQLCommon.getConnection(pars.get("poolName"), null)) {
            String path = new MySQLQuery("SELECT sigma_path FROM sys_cfg").getAsString(con);
            if (path == null || path.isEmpty()) {
                path = getServletContext().getRealPath("/");
            }
            File file = new File(path + File.separator + "updater" + File.separator + "QualisysUpdater.exe");
            if (pars.containsKey("md5")) {
                try (PrintWriter out = response.getWriter()) {
                    out.write("QualisysUpdater.exe\t" + file.length() + "\t");
                    out.write(getMD5Checksum(file));
                }
            } else {
                try (GZIPOutputStream gos = new GZIPOutputStream(response.getOutputStream())) {
                    fileManager.copy(new FileInputStream(file), gos);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(GetUpdaterFile.class.getName()).log(Level.SEVERE, null, ex);
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
