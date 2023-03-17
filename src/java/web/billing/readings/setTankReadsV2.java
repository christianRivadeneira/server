package web.billing.readings;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.system.SessionLogin;
import utilities.Dates;
import utilities.IO;
import utilities.MySQLQuery;
import web.billing.BillingServlet;

@MultipartConfig
@WebServlet(name = "/readings/setTankReadsV2", urlPatterns = {"/readings/setTankReadsV2"})
public class setTankReadsV2 extends BillingServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String sessionId = IO.convertStreamToString(request.getPart("sessionId").getInputStream());
            String data = IO.convertStreamToString(request.getPart("data").getInputStream());
            int cityId = Integer.valueOf(IO.convertStreamToString(request.getPart("cityId").getInputStream()));
            int reqSpanId = Integer.valueOf(IO.convertStreamToString(request.getPart("spanId").getInputStream()));

            String[] lines = data.split("B");
            try (Connection gralConn = getConnection(); Connection cityConn = getConnection(cityId)) {
                SessionLogin.validate(sessionId, gralConn, null);
                int spanId = new MySQLQuery("SELECT id FROM bill_span WHERE state = 'cons'").getAsInteger(cityConn);
                if (spanId != reqSpanId) {
                    throw new Exception("La lectura no corresponde al periodo actual.");
                }
                for (String line : lines) {
                    String[] parts = line.split("A");
                    if (!parts[1].isEmpty()) {
                        int tankId = Integer.valueOf(parts[0]);
                        Date rd = new Date();
                        boolean sales = false;
                        if (parts.length > 6) {
                            String dt = parts[6];
                            int d = Integer.valueOf(dt.substring(0, 2));
                            int m = Integer.valueOf(dt.substring(2, 4));
                            int y = Integer.valueOf("20" + dt.substring(4, 6));
                            rd = Dates.trimDate(new GregorianCalendar(y, m - 1, d).getTime());
                            MySQLQuery salesQ = new MySQLQuery("SELECT COUNT(s.id) > 0 FROM est_sale AS s INNER JOIN est_tank ON est_tank.client_id = s.client_id WHERE s.sale_date >= ?1 AND est_tank.id = " + tankId);
                            salesQ.setParam(1, rd);
                            sales = salesQ.getAsBoolean(gralConn);
                        }

                        BigDecimal lastRead = new MySQLQuery("SELECT percent FROM est_tank_read WHERE tank_id = " + tankId + " AND bill_span_id = " + (spanId - 1)).getAsBigDecimal(gralConn, true);
                        new MySQLQuery("DELETE FROM est_tank_read WHERE tank_id = " + tankId + " AND bill_span_id = " + spanId).executeDelete(gralConn);
                        BigDecimal read = new BigDecimal(parts[1]);
                        Double lat = parts[2].equals("null") ? null : Double.parseDouble(parts[2]);
                        Double lon = parts[3].equals("null") ? null : Double.parseDouble(parts[3]);
                        Boolean inRadius = parts[4].equals("1");
                        boolean fromScan = parts[5].equals("1");

                        lastRead = lastRead.equals(BigDecimal.ZERO) ? null : lastRead;
                        MySQLQuery ps = new MySQLQuery("INSERT INTO est_tank_read SET read_date = ?1, last_percent = ?2, percent = ?3, lat = ?4, lon = ?5, in_radius = ?6, from_scan = ?7, tank_id = " + tankId + ", bill_span_id = " + spanId + ", sales = " + (sales ? "1" : "0") + ", client_id=(SELECT tk.client_id FROM est_tank AS tk WHERE tk.id = " + tankId + ")");
                        ps.setParam(1, rd);
                        ps.setParam(2, lastRead);
                        ps.setParam(3, read);
                        ps.setParam(4, lat);
                        ps.setParam(5, lon);
                        ps.setParam(6, inRadius);
                        ps.setParam(7, fromScan);
                        ps.executeUpdate(gralConn);
                    }
                }
                response.setStatus(200);
                response.getWriter().write("ok");
            } catch (Exception ex) {
                Logger.getLogger(setTankReadsV2.class.getName()).log(Level.SEVERE, null, ex);
                response.sendError(500, ex.getMessage());
            }
        } catch (IOException | NumberFormatException | ServletException ex) {
            Logger.getLogger(setTankReadsV2.class.getName()).log(Level.SEVERE, null, ex);
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
