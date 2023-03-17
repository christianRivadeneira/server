package web;

import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@WebServlet(name = "GetDBStructure", urlPatterns = {"/GetDBStructure"})
public class GetDBStructure extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> args = MySQLQuery.scapedParams(request);
        String poolName = args.get("poolName");
        String key = args.get("key");
        String sessionId = args.get("sessionId");
        String tz = args.get("tz");

        try (Connection conn = MySQLCommon.getConnection(poolName != null ? poolName : "sigmads", tz)) {
            if (sessionId == null && key == null) {
                throw new Exception("Debe indicar session o key");
            }
            if (sessionId != null) {
                SessionLogin.validate(sessionId, conn);
            } else {
                if (!key.equals("Mr217aa3")) {
                    throw new Exception("Clave incorrecta. Comuníquese con el administrador");
                }
            }

            String dbs = "";
            Object[][] dbsData = new MySQLQuery("SHOW DATABASES;").getRecords(conn);
            for (Object[] row : dbsData) {
                String db = row[0].toString();
                if (!db.equals("information_schema") && !db.equals("mysql") && !db.equals("performance_schema") && !db.equals("phpmyadmin")) {
                    dbs += db + " ";
                }
            }
            String dbUrl = new MySQLQuery("SELECT db_url FROM sys_cfg").getAsString(conn);

            int i1 = dbUrl.indexOf("//");
            int i2 = dbUrl.indexOf(":", i1);
            int i3 = dbUrl.indexOf("/", i2);
            int i4 = dbUrl.indexOf("?", i3);
            final String host = dbUrl.substring(i1 + 2, i2);
            final String port = dbUrl.substring(i2 + 1, i3);
            String params = dbUrl.substring(i4 + 1);
            String[] pars = params.split("&");
            final String user = pars[0].split("=")[1];
            final String pass = pars[1].split("=")[1];

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_H-m-s");
            response.setHeader("Content-Disposition", "attachment; filename=" + df.format(new Date()) + ".zip");

            Process exec = Runtime.getRuntime().exec(
                    "mysqldump --host=" + host + " --port=" + port + " "
                    + " --user=" + user + " --password=" + pass + " "
                    + " --skip-comments --default-character-set=latin1 --hex-blob --no-data --add-drop-database --databases " + dbs);

            try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
                ZipEntry ze = new ZipEntry("all.sql");
                zos.putNextEntry(ze);
                fileManager.copy(exec.getInputStream(), zos, true, false);
                zos.closeEntry();
            }
        } catch (Exception e) {
            Logger.getLogger(GetDBStructure.class.getName()).log(Level.SEVERE, null, e);
            response.sendError(500, e.getMessage());
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
        return "Estructura BD";
    }
}
