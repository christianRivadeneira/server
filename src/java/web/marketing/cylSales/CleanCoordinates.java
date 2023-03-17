package web.marketing.cylSales;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@WebServlet(name = "CleanCoordinates", urlPatterns = {"/CleanCoordinates"})
public class CleanCoordinates extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        Connection conn = null;
        try {
            conn = MySQLCommon.getConnection("sigmads", null);
            Object[] data = new MySQLQuery("SELECT month_coords, CURDATE() FROM sys_cfg").getRecord(conn);
            if (data != null && data.length > 0 && data[0] != null) {
                Date serverDate = MySQLQuery.getAsDate(data[1]);
                int limitMonths = MySQLQuery.getAsInteger(data[0]);
                GregorianCalendar gc = new GregorianCalendar();
                gc.setTime(serverDate);
                gc.add(Calendar.MONTH, -limitMonths);
                gc.set(Calendar.DAY_OF_MONTH, gc.getActualMaximum(Calendar.DAY_OF_MONTH));
                
                String baseDate = new SimpleDateFormat("yyyy-MM-dd").format(gc.getTime());
                new MySQLQuery("DELETE FROM gps_coordinate WHERE DATE(date) <= '" + baseDate + "'").executeDelete(conn);

                try (PrintWriter out = response.getWriter()) {
                    out.println("Borrado exitoso. Se dejaron datos a partir de " + baseDate);
                }
            } else {
                throw new Exception("No se ha configurado el número de meses para mantener coordenadas");
            }
        } catch (Exception ex) {
            Logger.getLogger(CleanCoordinates.class.getName()).log(Level.SEVERE, null, ex);
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
        return "Debe llamarse cuando se considere necesario.";
    }
}
