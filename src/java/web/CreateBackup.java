package web;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.pdfbox.io.IOUtils;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import utilities.SysTask;

@WebServlet(name = "CreateBackup", urlPatterns = {"/CreateBackup"})
public class CreateBackup extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> args = MySQLQuery.scapedParams(request);
        long l = System.currentTimeMillis();
        response.setContentType("text/html;charset=UTF-8");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

        String backupPath = args.get("path");
        String poolName = args.get("poolName");
        String tz = args.get("tz");

        backupPath = backupPath + (backupPath.endsWith(File.separator) ? "" : File.separator);
        if (poolName == null) {
            poolName = "sigmads";
            tz = null;
        }

        SysTask task = null;
        try (Connection conn = MySQLCommon.getConnection(poolName, tz)) {
            PrintWriter out = response.getWriter();
            String host;
            String port;
            String user;
            String pass;
            String dbs = "--databases ";
            Object[][] dbsData;

            try {
                task = new SysTask(CreateBackup.class, 1, conn);
                String dbUrl = new MySQLQuery("SELECT db_url FROM sys_cfg").getAsString(conn);
                int i1 = dbUrl.indexOf("//");
                int i2 = dbUrl.indexOf(":", i1);
                int i3 = dbUrl.indexOf("/", i2);
                int i4 = dbUrl.indexOf("?", i3);
                host = dbUrl.substring(i1 + 2, i2);
                port = dbUrl.substring(i2 + 1, i3);
                String dbName = dbUrl.substring(i3 + 1, i4);
                String params = dbUrl.substring(i4 + 1);
                String[] pars = params.split("&");
                user = pars[0].split("=")[1];
                pass = pars[1].split("=")[1];

                dbs += dbName + " ";
                dbsData = new MySQLQuery("SELECT db FROM bill_instance ORDER by name ASC").getRecords(conn);

                for (Object[] dbsRow : dbsData) {
                    dbs += dbsRow[0].toString() + " ";
                }

                ProcessBuilder builder = new ProcessBuilder("bash", "-c", "mysqldump --single-transaction --skip-lock-tables --host=" + host + " --port=" + port + " --user=" + user + " --password=" + pass + " --skip-comments --default-character-set=latin1 --hex-blob " + dbs + " | gzip -1 > " + backupPath + df.format(new Date()) + ".sql.gz");
                Process p = builder.start(); // may throw IOException
                p.waitFor();

                out.write("<h2>Conexión con el servidor establecida.<h2><br>");
                out.write("<br>");
                out.write("<h4>Resultado de la tarea<h4><br>");
                String result = getAsString(p.getInputStream());
                if (result != null && !result.isEmpty()) {
                    out.write("Resultado: " + result + "<br>");
                }
                String errMsg = getAsString(p.getErrorStream());
                if (errMsg != null && !errMsg.isEmpty()) {
                    out.write("Error: " + errMsg);
                } else {
                    out.write("Backup generado con éxito");
                }
                task.success(conn);
            } catch (Exception ex) {
                if (task != null) {
                    task.error(ex, conn);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(CreateBackup.class.getName()).log(Level.SEVERE, null, ex);
            response.reset();
            response.sendError(500, ex.getMessage());
        } finally {
            System.out.println("Backup Took: " + (System.currentTimeMillis() - l) + "ms");
        }
    }

    public String getAsString(InputStream is) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(is, baos);
        is.close();
        baos.close();
        return new String(baos.toByteArray());
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
        return "Short description";
    }
}
