package web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
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
import utilities.DBSettings;
import utilities.DesEncrypter;
import utilities.MySQLQuery;

@WebServlet(name = "cityBackup", urlPatterns = {"/cityBackup"})
public class cityBackup extends HttpServlet {

    protected void processRequest(final HttpServletRequest request, HttpServletResponse response) throws Exception {
        try (Connection con = MySQLCommon.getConnection("sigmads", null);) {

            Map<String, String> pars = MySQLQuery.scapedParams(request);
            Integer cityId = Integer.valueOf(pars.get("cityId"));
            SessionLogin.validate(pars.get("sessionId"), con);

            final DBSettings db = new DBSettings(con);
            final String dbName = new MySQLQuery("SELECT db FROM bill_instance WHERE id = " + cityId).getAsString(con);
            DesEncrypter cr = new DesEncrypter("YVD2gYRWXMJZ");

            //truco flujos
            PipedInputStream in = new PipedInputStream();
            final PipedOutputStream out = new PipedOutputStream(in);

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_H-m-s");
            response.setHeader("Content-Disposition", "attachment; filename=" + df.format(new Date()) + ".esb");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Process exec = Runtime.getRuntime().exec(
                                "mysqldump --host=" + db.host + " --port=" + db.port + " "
                                + " --user=" + db.user + " --password=" + db.pass + " "
                                + " --skip-comments --default-character-set=latin1 --hex-blob " + dbName);
                        try (ZipOutputStream zos = new ZipOutputStream(out)) {
                            ZipEntry ze = new ZipEntry(dbName);
                            zos.putNextEntry(ze);
                            copy(exec.getInputStream(), zos);
                            zos.closeEntry();
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(cityBackup.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }).start();
            cr.encrypt(in, response.getOutputStream());
        }
    }

    private static int copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(cityBackup.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(cityBackup.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex.getMessage());
        }
    }

    @Override
    public String getServletInfo() {
        return "cityBackup";
    }
}
