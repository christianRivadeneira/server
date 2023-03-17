package web.marketing.cylSales.syncSiesa;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@WebServlet(name = "RequestAltDoc", urlPatterns = {"/RequestAltDoc"})
public class ReqAltDoc extends HttpServlet {

    protected synchronized void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (Connection con = MySQLCommon.getConnection("sigmads", null);) {
            response.setContentType("text/plain; charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();

            int oldSerial = new MySQLQuery("SELECT IFNULL(doc_alt, 0) FROM dto_cfg").getAsInteger(con);
            int newSerial = oldSerial + 1;
            if (MySQLQuery.getAsString(newSerial).length() > 8) {
                newSerial = 0;
            }
            new MySQLQuery("UPDATE dto_cfg SET doc_alt = " + newSerial + " WHERE id = 1").executeUpdate(con);
            out.write(newSerial + "");
        } catch (Exception e) {
            Logger.getLogger(ReqAltDoc.class.getName()).log(Level.SEVERE, null, e);
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
        return "Serial alterno";
    }
}
