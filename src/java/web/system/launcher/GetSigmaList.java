package web.system.launcher;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
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
import static web.system.launcher.GetJvmList.browse;

@WebServlet(urlPatterns = {"/GetSigmaList"})
public class GetSigmaList extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        try (PrintWriter out = response.getWriter(); Connection con = MySQLCommon.getConnection(pars.get("poolName"), null)) {
            String path = new MySQLQuery("SELECT sigma_path FROM sys_cfg").getAsString(con);
            if (path == null || path.isEmpty()) {
                path = getServletContext().getRealPath("/");
            }
            File root = new File(path);
            List<File> files = new ArrayList<>();
            browse(root, files);
            String rootPath = root.getAbsolutePath();
            for (File file : files) {
                writeFile(out, file, rootPath);
            }

        } catch (Exception ex) {
            Logger.getLogger(GetSigmaList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void writeFile(PrintWriter out, File file, String root) throws Exception {
        String path = file.getAbsolutePath();
        if (!path.startsWith(root)) {
            throw new RuntimeException("El archivo debe estar en la raíz");
        }
        if (path.endsWith(".jar")) {
            out.write(path.substring(root.length() + 1, path.length()));
            out.write("\t");
            out.write(file.length() + "");
            out.write("\t");
            out.write(getMD5Checksum(file));
            out.write("\n");
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
