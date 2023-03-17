package web.system.tutos;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "DeleteTuto", urlPatterns = {"/deleteTuto"})
public class DeleteTuto extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        String poolName = pars.get("poolName");
        String tz = pars.get("tz");
        String sessionId = pars.get("sessionId");
        String id = pars.get("id");

        try (Connection con = MySQLCommon.getConnection(poolName, tz)) {
            SessionLogin.validate(sessionId, con);
            String baseDir = new MySQLQuery("SELECT tutos_base_dir FROM sys_cfg LIMIT 1").getAsString(con);
            if (baseDir != null && !baseDir.isEmpty()) {
                if (!baseDir.endsWith(File.separator)) {
                    baseDir = (baseDir + File.separator);
                }
                String name = new MySQLQuery("SELECT filename FROM sys_tutorial t WHERE id = " + id).getAsString(con);
                if (name != null && !name.isEmpty()) {
                    String type = new MySQLQuery("SELECT filetype FROM sys_tutorial t WHERE id = " + id).getAsString(con);
                    if (type != null && !type.isEmpty()) {
                        new File(baseDir + name + "." + type).delete();
                    }
                }
            }
            new MySQLQuery("DELETE FROM sys_tutorial WHERE id = " + id).executeDelete(con);
            response.setStatus(200);
        } catch (Exception ex) {
            sendError(response, ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";

    }

    private void sendError(HttpServletResponse resp, Exception ex) throws IOException {
        Logger.getLogger(DeleteTuto.class
                .getName()).log(Level.SEVERE, null, ex);
        resp.setStatus(500);
        if (ex.getMessage() != null) {
            resp.getOutputStream().write(ex.getMessage().getBytes("UTF8"));
        } else {
            resp.getOutputStream().write(ex.getClass().getName().getBytes("UTF8"));
        }
    }
}
