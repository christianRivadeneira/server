package web.maintenance;

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
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import model.system.SessionLogin;
import printout.PrintFormats;

@WebServlet(name = "PrintChkFormat", urlPatterns = {"/PrintChkFormat"})
public class PrintChkFormat extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            response.setContentType("text/html;charset=UTF-8");
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");

            Map<String, String> pars = MySQLQuery.scapedParams(request);

            String sessionId = pars.get("sessionId");
            String poolName = pars.get("poolName");
            String tz = pars.get("tz");
            String registId = pars.get("registId");
            String chkTypeClass = (request.getParameter("nameClass").equals("null") ? null : request.getParameter("nameClass"));
            
            try (Connection con = MySQLCommon.getConnection(poolName, tz);) {
                SessionLogin.validate(sessionId, con);

                FileInputStream fis = new FileInputStream(PrintFormats.Print(con, MySQLQuery.getAsInteger(registId), chkTypeClass));
                copy(fis, response.getOutputStream());
            } catch (Exception e) {
                Logger.getLogger(PrintChkFormat.class.getName()).log(Level.SEVERE, null, e);
            }
        } catch (Exception e) {
            Logger.getLogger(PrintChkFormat.class.getName()).log(Level.SEVERE, null, e);
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
        return "Subsidiios";
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        input.close();
        output.close();
        return count;
    }
}
