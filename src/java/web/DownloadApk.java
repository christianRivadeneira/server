package web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import web.fileManager.PathInfo;

@WebServlet(name = "DownloadApk", urlPatterns = {"/DownloadApk"})
public class DownloadApk extends HttpServlet {

    private static final int DEFAULT_BUFFER_SIZE = 4096; // 4KB.
    public static final int DEPLOY_APP = 78; //*******************owner Type

    //Al descargar desde navegador se hacen 2 peticiones, la primera la hace el navegador para conoconer los headers del archivo,
    //y luego la cierra para pasarle la descarga al android download manager, quien hace la segunda petición.
    //en la segunda petición se reenvia cualquier cookie que se establezca en la primera petición, si no se envía este cookie, enviía
    //un header cookie en blanco que hace fallar a glassfish, por eso se agrega el envío de un cookie dummy.
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            Map<String, String> pars = MySQLQuery.scapedParams(request);
            String appName = pars.get("appName");
            String version = pars.get("version");
            String poolName = pars.get("poolName");
            String tz = pars.get("tz");
            poolName = poolName != null ? poolName : "sigmads";

            Object[][] appInfo;
            Object[] bFile;
            PathInfo path;
            try (Connection con = MySQLCommon.getConnection(poolName, tz)) {
                path = new PathInfo(con);
                appInfo = new MySQLQuery("SELECT id, version FROM system_app WHERE package_name = '" + appName + "'").getRecords(con);
                Integer appId = MySQLQuery.getAsInteger(appInfo[0][0]);
                bFile = new MySQLQuery("SELECT id, file_name FROM bfile WHERE owner_id = " + appId + " AND owner_type = " + DEPLOY_APP).getRecord(con);
            }

            String serverVersion = MySQLQuery.getAsString(appInfo[0][1]);
            if (version != null && serverVersion.equals(version)) {
                response.setContentType("text/plain; charset=utf-8");
                response.setCharacterEncoding("UTF-8");
                try (PrintWriter out = response.getWriter()) {
                    out.write("ok");
                }
            } else {
                Integer bfileId = MySQLQuery.getAsInteger(bFile[0]);
                String fileName = MySQLQuery.getAsString(bFile[1]);
                String appN = fileName.substring(0, fileName.indexOf("."));
                File f = path.getExistingFile(bfileId);
                response.setDateHeader("Last-Modified", new Date().getTime());
                response.setHeader("ETag", new Date().getTime() + "");
                response.setContentLength((int) f.length());
                //response.setHeader("Connection", "Keep-Alive");
                response.setContentType("application/vnd.android.package-archive");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + appN + ".apk\"");
                response.addCookie(new Cookie("dummy", "some_random_content"));

                try (ServletOutputStream os = response.getOutputStream(); FileInputStream fis = new FileInputStream(f); BufferedInputStream input = new BufferedInputStream(fis, DEFAULT_BUFFER_SIZE); BufferedOutputStream output = new BufferedOutputStream(os, DEFAULT_BUFFER_SIZE);) {
                    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                    int length;
                    try {
                        while ((length = input.read(buffer)) > 0) {
                            output.write(buffer, 0, length);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(DownloadApk.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(DownloadApk.class.getName()).log(Level.SEVERE, null, e);
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
        return "Download Apk";
    }
}
