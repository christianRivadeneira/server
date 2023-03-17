package web.marketing.pvs;

import java.io.IOException;
import java.sql.Connection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@WebServlet(name = "launcher", urlPatterns = {"/Launcher"})
public class LaunchBiableSync extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (Connection conn = MySQLCommon.getConnection("sigmads", null)) {
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");

            System.out.println("Called for URL");
            Map<String, String> req = MySQLQuery.scapedParams(request);
            String sessionId = req.get("sessionId");
            if (sessionId == null) {
                throw new Exception("Falta parámetro sesión");
            }
            SessionLogin.validate(sessionId, conn, null);

            GetBiableInfo.syncBiableInfo();
            response.setStatus(200);
            response.getWriter().write("Sincronización exitosa");
        } catch (Exception e) {
            sendError(response, e);
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
        return "Sync";
    }

    private void sendError(HttpServletResponse resp, Exception ex) throws IOException {
        Logger.getLogger(LaunchBiableSync.class.getName()).log(Level.SEVERE, null, ex);
        resp.setStatus(500);
        if (ex.getMessage() != null) {
            resp.getOutputStream().write(ex.getMessage().getBytes("UTF8"));
        } else {
            resp.getOutputStream().write(ex.getClass().getName().getBytes("UTF8"));
        }
    }
}
