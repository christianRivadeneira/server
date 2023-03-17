package web;

import java.io.*;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import utilities.Dates;
import utilities.DesEncrypter;
import utilities.MySQLQuery;

@WebServlet(name = "completeBackup", urlPatterns = {"/completeBackup"})
public class completeBackup extends HttpServlet {

    private static final int DATA_AND_FILES = 1;
    private static final int JUST_MOD_FILES = 2;
    private static final int JUST_DATA = 3;

    private String getTypeDesc(int type) {
        switch (type) {
            case DATA_AND_FILES:
                return "DATA_AND_FILES";
            case JUST_MOD_FILES:
                return "JUST_MOD_FILES";
            case JUST_DATA:
                return "JUST_DATA";
            default:
                break;
        }
        throw new RuntimeException("Unkwnown Type: " + type);
    }

    protected void processRequest(final HttpServletRequest request, HttpServletResponse response) throws Exception {

        Map<String, String> args = MySQLQuery.scapedParams(request);
        String poolName = args.get("poolName");
        String sessionId = args.get("sessionId");
        String tz = args.get("tz");
        if (poolName == null) {
            poolName = "sigmads";
            tz = "";
        }

        try (Connection conn = MySQLCommon.getConnection(poolName, tz)) {
            SessionLogin.validate(sessionId, conn, null);
            final int attachments;
            if (args.get("attachments") != null && !args.get("attachments").isEmpty()) {
                attachments = Integer.valueOf(args.get("attachments"));
            } else {
                attachments = JUST_DATA;
            }

            DesEncrypter cr = new DesEncrypter(args.get("pass"));
            //jdbc:mysql//192.168.1.2:3306/sigma?user=root&pass=root
            //final SysCfg cfg = SysCfgController.getSysCfg(em);
            Object[] rs = new MySQLQuery("SELECT db_url, files_path FROM sys_cfg").getRecord(conn);

            String u = MySQLQuery.getAsString(rs[0]);
            final String filesPath = MySQLQuery.getAsString(rs[1]);

            int i1 = u.indexOf("//");
            int i2 = u.indexOf(":", i1);
            int i3 = u.indexOf("/", i2);
            int i4 = u.indexOf("?", i3);
            final String host = u.substring(i1 + 2, i2);
            final String port = u.substring(i2 + 1, i3);
            final String dbName = u.substring(i3 + 1, i4);
            String params = u.substring(i4 + 1);

            String[] pars = params.split("&");
            final String user = pars[0].split("=")[1];
            final String pass = pars[1].split("=")[1];
            //backup general
            final List<String> dbs = new ArrayList<>();
            dbs.add(dbName);
            Object[][] dbsData = new MySQLQuery("SELECT db FROM bill_instance ORDER by name ASC").getRecords(conn);
            for (Object[] dbRow : dbsData) {
                dbs.add(dbRow[0].toString());
            }

            //truco flujos
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_H-m-s");
            response.setHeader("Content-Disposition", "attachment; filename=" + getTypeDesc(attachments) + "@" + df.format(new Date()) + ".esb");

            File tmpZip = File.createTempFile("bkpsigma", "tmpsigma");
            FileOutputStream tmpZipFos = new FileOutputStream(tmpZip);
            ZipOutputStream zos = new ZipOutputStream(tmpZipFos);
            zos.setLevel(0);
            for (int i = 0; i < dbs.size(); i++) {
                String curDbName = dbs.get(i);
                Process exec = Runtime.getRuntime().exec(
                        "mysqldump --host=" + host + " --port=" + port + " "
                        + " --user=" + user + " --password=" + pass + " "
                        + " --skip-comments --default-character-set=latin1 --hex-blob " + curDbName);

                ZipEntry ze = new ZipEntry(curDbName + ".sql");
                zos.putNextEntry(ze);
                copy(exec.getInputStream(), zos);
                zos.closeEntry();
            }

            if (attachments != JUST_DATA) {
                Date today = Dates.trimDate(new Date());
                File folder = new File(filesPath);
                File[] files = folder.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.canRead() && f.exists() && f.isFile() && !f.isHidden() && f.getAbsolutePath().toLowerCase().endsWith(".bin");
                    }
                });
                for (File file : files) {
                    boolean backupFile = false;
                    if (attachments == DATA_AND_FILES) {
                        backupFile = true;
                    } else if (attachments == JUST_MOD_FILES) {
                        backupFile = Dates.trimDate(new Date(file.lastModified())).equals(today);
                    }
                    if (backupFile) {
                        ZipEntry ze = new ZipEntry("bdata/" + file.getName());
                        zos.putNextEntry(ze);
                        FileInputStream fis = new FileInputStream(file);
                        copy(fis, zos);
                        fis.close();
                        zos.closeEntry();

                    }
                }
            }
            zos.close();
            tmpZipFos.close();

            FileInputStream tmpZipFin = new FileInputStream(tmpZip);
            cr.encrypt(tmpZipFin, response.getOutputStream());
            tmpZipFin.close();
            tmpZip.delete();
        } catch (Exception ex) {
            response.reset();
            response.sendError(500, ex.getMessage());
            Logger.getLogger(completeBackup.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
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
            Logger.getLogger(completeBackup.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(completeBackup.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex.getMessage());
        }
    }

    @Override
    public String getServletInfo() {
        return "completeBackup";
    }
}
