package web;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
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
import static service.MySQL.MySQLCommon.getConnection;
import utilities.Dates;
import utilities.MySQLQuery;

@WebServlet(name = "CreateBackupZip", urlPatterns = {"/CreateBackupZip"})
public class CreateBackupZip extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> args = MySQLQuery.scapedParams(request);
        long l = System.currentTimeMillis();
        response.setContentType("text/html;charset=UTF-8");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

        String backupPath = args.get("path");
        String poolName = args.get("poolName");
        String tz = args.get("tz");
        String attach = args.get("attach");
        backupPath = backupPath + (backupPath.endsWith(File.separator) ? "" : File.separator);
        if (poolName == null) {
            poolName = "sigmads";
            tz = "";
        }
        if (attach == null) {
            attach = "all";
        }
        attach = attach.trim().toLowerCase();
        try {
            PrintWriter out = response.getWriter();
            String filesPath;
            String host;
            String port;
            String user;
            String pass;
            List<String> dbs;
            Object[][] dbsData;
            try (Connection conn = getConnection(poolName, tz)) {
                String dbUrl = new MySQLQuery("SELECT db_url FROM sys_cfg").getAsString(conn);
                filesPath = new MySQLQuery("SELECT files_path FROM sys_cfg").getAsString(conn);
                filesPath = filesPath + (filesPath.endsWith(File.separator) ? "" : File.separator);
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
                dbs = new ArrayList();
                dbs.add(dbName);
                dbsData = new MySQLQuery("SELECT db FROM bill_instance ORDER by name ASC").getRecords(conn);

                for (Object[] dbsRow : dbsData) {
                    dbs.add(dbsRow[0].toString());
                }

                File tmpZip = new File(backupPath + df.format(new Date()) + "_" + getTypeDesc(attach) + ".zip");
                try (FileOutputStream tmpZipFos = new FileOutputStream(tmpZip); ZipOutputStream zos = new ZipOutputStream(tmpZipFos)) {
                    zos.setLevel(ZipOutputStream.STORED);
                    for (String curDbName : dbs) {
                        Process exec = Runtime.getRuntime().exec("mysqldump --host=" + host + " --port=" + port + " " + " --user=" + user + " --password=" + pass + " " + " --skip-comments --default-character-set=latin1 --hex-blob " + curDbName);
                        ZipEntry ze = new ZipEntry(curDbName + ".sql");
                        zos.putNextEntry(ze);
                        fileManager.copy(exec.getInputStream(), zos, true, false);
                        zos.closeEntry();
                    }

                    if (!attach.equals("none")) {
                        File folder = new File(filesPath);
                        final String att = attach;
                        final Date today = Dates.trimDate(new Date());
                        File[] files = folder.listFiles(new FileFilter() {
                            @Override
                            public boolean accept(File f) {
                                boolean b = f.canRead() && f.exists() && f.isFile() && !f.isHidden();
                                return b && (att.equals("all") || (att.equals("day") && Dates.trimDate(new Date(f.lastModified())).compareTo(today) == 0));
                            }
                        });
                        for (File file : files) {
                            zos.putNextEntry(new ZipEntry("bdata/" + file.getName()));
                            FileInputStream fis = new FileInputStream(file);
                            fileManager.copy(fis, zos, true, false);
                            zos.closeEntry();
                        }
                    }
                }
                out.write("OK");
            }
        } catch (Exception ex) {
            Logger.getLogger(CreateBackupZip.class.getName()).log(Level.SEVERE, null, ex);
            response.reset();
            response.sendError(500, ex.getMessage());
        } finally {
            System.out.println("Backup Took: " + (System.currentTimeMillis() - l) + "ms");
        }
    }

    private String getTypeDesc(String type) {
        switch (type) {
            case "all":
                return "DATA_AND_FILES";
            case "day":
                return "JUST_MOD_FILES";
            case "none":
                return "JUST_DATA";
        }
        throw new RuntimeException("Unkwnown Type: " + type);
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
