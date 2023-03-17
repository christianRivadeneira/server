package web;

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
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@WebServlet(name = "GetAppVersion", urlPatterns = {"/GetAppVersion"})
public class GetAppVersion extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain; charset=utf-8");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> pars = MySQLQuery.scapedParams(request);
        String appName = pars.get("appName");
        String version = pars.get("version");
        String poolName = pars.get("poolName");
        String tz = pars.get("tz");
        poolName = poolName != null ? poolName : "sigmads";

        try (Connection con = MySQLCommon.getConnection(poolName, tz)) {
            Boolean okVersion = new MySQLQuery("SELECT version = '" + version + "' FROM system_app WHERE package_name = '" + appName + "'").getAsBoolean(con);
            response.getWriter().write(okVersion == null || !okVersion ? "download" : "ok");
        } catch (Exception e) {
            Logger.getLogger(GetAppVersion.class.getName()).log(Level.SEVERE, null, e);
            response.getWriter().write("error");
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
        return "App Version";
    }

}
