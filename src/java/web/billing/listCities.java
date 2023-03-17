package web.billing;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@WebServlet(name = "listCities", urlPatterns = {"/readings/listCities"})
public class listCities extends BillingServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;

        response.setContentType("plain/text;charset=UTF-8");
        try (PrintWriter w = response.getWriter()) {
            conn = getConnection();
            Object[][] res = new MySQLQuery("SELECT id, name FROM bill_instance ORDER BY name ASC").getRecords(conn);
            for (int i = 0; i < res.length; i++) {
                Object[] row = res[i];
                w.write(row[0].toString());
                w.write(9);
                w.write(row[1].toString());
                if (i < res.length - 1) {
                    w.write(13);
                    w.write(10);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(listCities.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500, ex.getMessage());
        } finally {
            MySQLCommon.closeConnection(conn);
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
