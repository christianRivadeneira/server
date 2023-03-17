package web;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.IO;
import utilities.MySQLQuery;

@WebServlet(name = "ApkToOracle", urlPatterns = {"/ApkToOracle"})
public class ApkToOracle extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (Connection conn = MySQLCommon.getDefaultConnection()) {
            Map<String, String> pars = MySQLQuery.scapedParams(request);
            fileManager.PathInfo pi = new fileManager.PathInfo(conn);

            String sessionId = pars.get("sessionId");
            SessionLogin.validate(sessionId);

            Integer fileId = Integer.valueOf(pars.get("fileId"));

            if (fileId == null) {
                throw new Exception("Falta el nombre del archivo");
            }
            File f = pi.getExistingFile(fileId);
            exec("/usr/bin/mlcpapk.sh " + f.getAbsolutePath());
            response.setStatus(200);
            response.getWriter().write("Apk Subido con éxito");

        } catch (Exception ex) {
            sendError(response, ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

    public static String convertStreamToString(Part is) throws IOException {
        if (is == null) {
            return null;
        }
        return IO.convertStreamToString(is.getInputStream());
    }

    private void sendError(HttpServletResponse resp, Exception ex) throws IOException {
        Logger.getLogger(ApkToOracle.class.getName()).log(Level.SEVERE, null, ex);
        resp.setStatus(500);
        if (ex.getMessage() != null) {
            resp.getOutputStream().write(ex.getMessage().getBytes("UTF8"));
        } else {
            resp.getOutputStream().write(ex.getClass().getName().getBytes("UTF8"));
        }
    }

    private static void exec(String command) throws Exception {
        Process p = Runtime.getRuntime().exec(command);
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }
    }
}
