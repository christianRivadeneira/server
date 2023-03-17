package web.system.tutos;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.Map;
import java.util.Random;
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
import static web.fileManager.copy;

@MultipartConfig
@WebServlet(name = "UploadTuto", urlPatterns = {"/uploadTuto"})
public class UploadTuto extends HttpServlet {

    private static String randString(int len) {

        Random random = new Random();
        StringBuilder buffer = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            int leftLimit;
            int rightLimit;

            double r = Math.random();
            if (r < 1 / 3d) {
                leftLimit = 48;// 0
                rightLimit = 57;// 9
            } else {
                leftLimit = 65;// A
                rightLimit = 90;// Z
            }
            int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        String poolName = pars.get("poolName");
        String tz = pars.get("tz");
        String sessionId = pars.get("sessionId");
        String id = pars.get("id");
        String type = pars.get("type");

        try (Connection con = MySQLCommon.getConnection(poolName, tz)) {
            SessionLogin.validate(sessionId, con);
            String baseDir = new MySQLQuery("SELECT tutos_base_dir FROM sys_cfg LIMIT 1").getAsString(con);
            if (!baseDir.endsWith(File.separator)) {
                baseDir = (baseDir + File.separator);
            }
            String name = randString(12);
            File tmp = new File(baseDir + name + "." + type);
            copy(request.getPart("data").getInputStream(), new BufferedOutputStream(new FileOutputStream(tmp)));
            new MySQLQuery("UPDATE sys_tutorial t SET filename = ?1, filetype = ?2 WHERE id = " + id).setParam(1, name).setParam(2, type).executeUpdate(con);
            response.setStatus(200);
            response.getWriter().write("Video");
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
        Logger.getLogger(UploadTuto.class
                .getName()).log(Level.SEVERE, null, ex);
        resp.setStatus(500);
        if (ex.getMessage() != null) {
            resp.getOutputStream().write(ex.getMessage().getBytes("UTF8"));
        } else {
            resp.getOutputStream().write(ex.getClass().getName().getBytes("UTF8"));
        }
    }
}
