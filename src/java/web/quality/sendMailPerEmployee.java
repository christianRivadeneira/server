package web.quality;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import web.fileManager;
import static web.quality.SendMail.getHtmlMsg;

@MultipartConfig
@WebServlet(name = "sendMailPerEmployee", urlPatterns = {"/quality/sendMailPerEmployee"})
public class sendMailPerEmployee extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        String sessionId = pars.get("sessionId");
        String poolName = pars.get("poolName");
        String tz = pars.get("tz");
        String sub = pars.get("sub");
        String msg = pars.get("msg");
        String dest = pars.get("dest");
        String copy = pars.get("copy");
        String type = pars.get("type");
        String sProcId = pars.get("procId");
        String sOfficeId = pars.get("officeId");
        String email = pars.get("addrs");
        String fileName = pars.get("fileName");
        String module = pars.get("modId");
        try (Connection con = MySQLCommon.getConnection(poolName, tz);) {
            SessionLogin.validate(sessionId, con);
            InputStream is = fileName != null ? request.getPart("file").getInputStream() : null;
            File destino = is != null ? File.createTempFile("tmp", ".bin") : null;
            Integer procId = !type.isEmpty() ? Integer.valueOf(sProcId) : null;
            Integer officeId = !type.isEmpty() ? Integer.valueOf(sOfficeId) : null;
            Integer modId = !module.isEmpty() ? Integer.valueOf(module) : null;
            if (is != null) {
                try (FileOutputStream fos = new FileOutputStream(destino)) {
                    fileManager.copy(is, new BufferedOutputStream(fos));
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = is.read(buf)) > 0) {
                        fos.write(buf, 0, len);
                    }
                } catch (IOException e) {
                    Logger.getLogger(sendMailPerEmployee.class.getName()).log(Level.SEVERE, null, e);
                }
            }
            request.getInputStream().close();
            if (dest != null && !dest.isEmpty()) {
                try {
                    SendMail.sendMail("per_employee", sub, msg, dest, copy.equals("1"), type, procId, officeId, poolName, tz, new String[]{fileName}, new File[]{destino}, modId);
                    response.setStatus(200);
                } catch (Exception ex) {
                    Logger.getLogger(sendMailPerEmployee.class.getName()).log(Level.SEVERE, null, ex);
                    StackTraceElement[] stack = ex.getStackTrace();
                    StringBuilder sb = new StringBuilder(ex.getClass().toString()).append("\r\n");
                    sb.append(ex.getMessage()).append("\r\n");
                    for (StackTraceElement se : stack) {
                        sb.append(se.toString()).append("\r\n");
                    }
                    response.sendError(500, sb.toString());
                }
            } else {
                SendMail.sendMail(con, email, sub, getHtmlMsg(con, sub, msg), msg, new String[]{fileName}, new File[]{destino});
            }
        } catch (Exception e) {
            Logger.getLogger(sendMailPerEmployee.class.getName()).log(Level.SEVERE, null, e);
            response.sendError(500);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "";
    }
}
