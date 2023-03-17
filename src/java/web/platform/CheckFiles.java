package web.platform;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import web.fileManager;
import web.fileManager.PathInfo;

public class CheckFiles extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection con = null;
        try (PrintWriter w = response.getWriter()) {
            Map<String, String> req = MySQLQuery.scapedParams(request);
            String mode = req.get("mode");
            boolean delete = (mode != null && mode.equals("delete"));
            con = MySQLCommon.getConnection(req.get("poolName"), req.get("tz"));

            PathInfo pi = new fileManager.PathInfo(con);
            String path = pi.path;

            w.write("\n.::REGISTROS SIN ARCHIVO::.\n\n");

            Object[][] bfiles = new MySQLQuery("SELECT id FROM bfile;").getRecords(con);
            for (Object[] bfile : bfiles) {
                File f = pi.getExistingFile(MySQLQuery.getAsInteger(bfile[0]));
                if (f != null && !f.exists()) {
                    w.write(f.getAbsolutePath() + "\n");
                    if (delete) {
                        new MySQLQuery("DELETE FROM bfile WHERE id = " + bfile[0]).executeUpdate(con);
                    }
                }
            }

            w.write("\n\n.::ARCHIVOS SIN REGISTRO::.\n\n");
            File[] files = new File(path).listFiles(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isFile() && f.getName().endsWith(".bin");
                }
            });

            if (mode.equals("size")) {
                for (File file : files) {
                    String id = file.getName().substring(0, file.getName().lastIndexOf("."));
                    if (new MySQLQuery("SELECT COUNT(*) = 0 FROM bfile WHERE id = " + id).getAsBoolean(con)) {
                        if (delete) {
                            file.delete();
                        }
                        w.write(file.getAbsolutePath() + "\n");
                    } else {
                        new MySQLQuery("UPDATE bfile SET size = " + file.length() + " WHERE id = " + id).executeUpdate(con);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(CheckFiles.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySQLCommon.closeConnection(con);
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
