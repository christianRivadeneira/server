package web.billing.readings;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utilities.MySQLQuery;
import web.billing.BillingServlet;

@WebServlet(name = "/readings/span", urlPatterns = {"/readings/span"})
public class Span extends BillingServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (PrintWriter w = response.getWriter(); Connection conn = getConnection(Integer.valueOf(request.getParameter("cityId")))) {
            int spanId = new MySQLQuery("SELECT id FROM bill_span WHERE state = 'cons'").getAsInteger(conn);
            w.write(String.valueOf(spanId));
        } catch (Exception ex) {
            Logger.getLogger(Span.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500, ex.getMessage());
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
