package web.system.launcher;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
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
import web.fileManager;

@WebServlet(urlPatterns = {"/GetInstallerExe"})
public class GetInstallerExe extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Content-disposition", "attachment; filename=" + "Qualisys_setup.exe");

        Map<String, String> pars = MySQLQuery.scapedParams(request);
        String poolName = "sigmads";
        if (pars.containsKey("poolName")) {
            poolName = pars.get("poolName");
        }

        try (Connection con = MySQLCommon.getConnection(poolName, null); OutputStream os = response.getOutputStream()) {
            String path = new MySQLQuery("SELECT sigma_path FROM sys_cfg").getAsString(con);
            String sUrls = new MySQLQuery("SELECT url_exe_launcher FROM sys_cfg").getAsString(con);

            if (sUrls == null || sUrls.isEmpty()) {
                throw new Exception("No se encontró la configuración de las URL.");
            }

            if (path == null || path.isEmpty()) {
                path = getServletContext().getRealPath("/");
            }

            byte[] bytes = Files.readAllBytes(new File(path + File.separator + "installer" + File.separator + "Qualisys Setup.exe").toPath());
            int repBeg = getIndexToReplace(bytes, "W]$t*F6vNNs+3t}A$[HfP'.X\\g[yB_g,a(xKM].,26cPA^6]".getBytes());
            String[] urls = sUrls.split(",");

            String data = poolName + "\n";
            for (String url : urls) {
                data += (url + "\n");
            }
            insert(bytes, data, repBeg);
            fileManager.copy(new ByteArrayInputStream(bytes), os);
        } catch (Exception ex) {
            Logger.getLogger(GetInstallerExe.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void insert(byte[] bin, String data, int pos) throws Exception {
        byte[] rep = pad(data);
        System.arraycopy(rep, 0, bin, pos, rep.length);
    }

    public byte[] pad(String data) throws Exception {
        byte[] rta = new byte[1024];        
        byte[] bdata = data.getBytes("US-ASCII");
        for (int i = bdata.length; i < rta.length; i++) {
            rta[i] = 32;            
        }
        System.arraycopy(bdata, 0, rta, 0, bdata.length);
        return rta;
    }

    public static int getIndexToReplace(byte[] raw, byte[] input) throws Exception {
        for (int i = 0; i < raw.length - input.length; i++) {
            boolean match = true;
            for (int j = 0; j < input.length && match; j++) {
                if (raw[j + i] != input[j]) {
                    match = false;
                }
            }
            if (match) {
                return i;
            }
        }
        throw new Exception("No se encontró el reemplazo");
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
