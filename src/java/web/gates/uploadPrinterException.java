package web.gates;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
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

@WebServlet(name = "uploadPrinterException", urlPatterns = {"/uploadPrinterException"})
public class uploadPrinterException extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (Connection con = MySQLCommon.getConnection("sigmads", null); Statement st = con.createStatement();) {
            Map<String, String> req = MySQLQuery.scapedParams(request);
            String pingString = MySQLQuery.getAsString(req.get("exeptionLog"));
            String location = MySQLQuery.getAsString(req.get("location"));
            st.executeUpdate("INSERT INTO gt_printer_exception SET location='" + location + "',exception_log = '" + pingString + "', dt = NOW()");
            response.setStatus(200);
        } catch (Exception ex) {
            Logger.getLogger(uploadPrinterException.class.getName()).log(Level.SEVERE, null, ex);
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

}
