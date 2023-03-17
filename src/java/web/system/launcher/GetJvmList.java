package web.system.launcher;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
import static web.GetRepoLibs.getMD5Checksum;

@WebServlet(urlPatterns = {"/GetJvmList"})
public class GetJvmList extends HttpServlet {

    public static void browse(File f, List<File> files) {
        if (f.isFile()) {
            files.add(f);
        }
        File[] children = f.listFiles();
        if (children != null) {
            for (File child : children) {
                browse(child, files);
            }
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        try (PrintWriter out = response.getWriter(); Connection con = MySQLCommon.getConnection(pars.get("poolName"), null)) {
            String path = new MySQLQuery("SELECT jvm_path FROM sys_cfg").getAsString(con);
            File root = new File(path);
            List<File> files = new ArrayList<>();
            browse(root, files);
            String rootPath = root.getAbsolutePath();
            for (File file : files) {
                writeFile(out, file, rootPath);
            }

        } catch (Exception ex) {
            Logger.getLogger(GetJvmList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void writeFile(PrintWriter out, File file, String root) throws Exception {
        String path = file.getAbsolutePath();
        if (!path.startsWith(root)) {
            throw new RuntimeException("El archivo debe estar en la raíz");
        }
        out.write(path.substring(root.length() + 1, path.length()));
        out.write("\t");
        out.write(file.length() + "");
        out.write("\t");
        out.write(getMD5Checksum(file));
        out.write("\n");
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
