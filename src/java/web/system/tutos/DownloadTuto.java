package web.system.tutos;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import web.fileManager;
import static web.fileManager.copy;

@WebServlet(name = "DownloadTuto", urlPatterns = {"/DownloadTuto"})
public class DownloadTuto extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        String poolName = pars.get("poolName");
        String tz = pars.get("tz");
        String idBfile = pars.get("idBfile");
        String t = pars.get("t");

        try (Connection con = MySQLCommon.getConnection(poolName, tz)) {
            File tmp;
            if (idBfile != null && !idBfile.equals("null")) {
                fileManager.PathInfo pInfo = new fileManager.PathInfo(con);
                tmp = getFile(pInfo, Integer.valueOf(idBfile));
                if (tmp == null) {
                    throw new Exception("El archivo no exíste");
                }
            } else {
                String baseDir = new MySQLQuery("SELECT tutos_base_dir FROM sys_cfg LIMIT 1").getAsString(con);
                if (!baseDir.endsWith(File.separator)) {
                    baseDir = (baseDir + File.separator);
                }
                System.out.println(t);
                String type = new MySQLQuery("SELECT filetype FROM sys_tutorial WHERE filename = ?1").setParam(1, t).getAsString(con);
                tmp = new File(baseDir + t + "." + type);
            }
            copy(new BufferedInputStream(new FileInputStream(tmp)), response.getOutputStream());
            response.setStatus(200);
            response.getWriter().write("Video");
        } catch (Exception ex) {
            ex.printStackTrace();
            response.setStatus(500);
        }
    }

    private File getFile(fileManager.PathInfo info, int id, boolean newFormat) throws Exception {
        if (newFormat) {
            int fname = (int) Math.floor(id / 10000d);
            File folder = new File(info.path + fname + File.separator);
            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    throw new Exception("No se pudo crear el directorio: " + folder.getAbsolutePath());
                }
            }
            return new File(info.path + fname + File.separator + id + ".bin");
        } else {
            return new File(info.path + id + ".bin");
        }
    }

    private File getFile(fileManager.PathInfo pInfo, int id) throws Exception {
        File nf = getFile(pInfo, id, true);
        if (nf.exists()) {
            return nf;
        } else {
            File of = getFile(pInfo, id, false);
            if (of.exists()) {
                return of;
            }
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
