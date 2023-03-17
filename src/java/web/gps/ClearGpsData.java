package web.gps;

import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.MySQLPreparedUpdate;
import utilities.MySQLQuery;

@WebServlet(name = "ClearGpsData", urlPatterns = {"/ClearGpsData"})
public class ClearGpsData extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Map<String, String> req = MySQLQuery.scapedParams(request);
            String sDate = MySQLQuery.getAsString(req.get("date"));
            int dist = MySQLQuery.getAsInteger(req.get("d"));
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(sDate);
            optimize(date, dist);
        } catch (Exception ex) {
            Logger.getLogger(ClearGpsData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
    Se llama desde otros algoritmos
    */
    
    public static final void optimize(Date date, int minDist) throws Exception {
        try (Connection con = MySQLCommon.getConnection("sigmads", null)) {
            con.setAutoCommit(false);
            String sDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
            new MySQLQuery("DELETE FROM gps_coordinate WHERE date between '" + sDate + " 00:00:00' and '" + sDate + " 23:59:59' and latitude = 0;").executeDelete(con);
            con.commit();

            new MySQLQuery("CREATE TEMPORARY TABLE IF NOT EXISTS tmp \n"
                    + "(INDEX sess (session_id))\n"
                    + "SELECT id, latitude, longitude, session_id, date FROM gps_coordinate WHERE date between '" + sDate + " 00:00:00' and '" + sDate + " 23:59:59';").executeUpdate(con);
            con.commit();

            Object[][] empsData = new MySQLQuery("SELECT distinct session_id FROM tmp;").getRecords(con);
            int batch = 0;

            MySQLPreparedUpdate pu = new MySQLPreparedUpdate("DELETE FROM gps_coordinate WHERE id = ?1", con);
            for (Object[] empRow : empsData) {
                int sessId = MySQLQuery.getAsInteger(empRow[0]);
                Object[][] coordsData = new MySQLQuery("select id, latitude, longitude from tmp where session_id = " + sessId + " order by date;").getRecords(con);
                if (coordsData != null && coordsData.length > 0) {

                    Pt lp = new Pt(coordsData[0]);

                    for (int i = 1; i < coordsData.length; i++) {
                        Pt p = new Pt(coordsData[i]);
                        if (dist(p, lp) >= minDist) {
                            lp = p;
                        } else {
                            batch++;
                            pu.setParameter(1, p.id);
                            pu.addBatch();
                            if (batch == 50000) {
                                pu.executeBatch();
                                con.commit();
                                pu.printStats("");
                                pu = new MySQLPreparedUpdate("DELETE FROM gps_coordinate WHERE id = ?1", con);
                                batch = 0;
                            }
                        }
                    }
                }
            }
            if (batch > 0) {
                pu.executeBatch();
                con.commit();
                pu.printStats("");
            }
        }
    }

    private static double dist(Pt p1, Pt p2) {
        return dist(p1.lat, p1.lon, p2.lat, p2.lon);
    }

    private static double dist(double lat1, double lon1, double lat2, double lon2) {
        double R = 6378.137; // Radius of earth in KM
        double dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
        double dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;
        return d * 1000; // meters
    }

    static class Pt {

        int id;
        double lat;
        double lon;

        public Pt(Object[] coordRow) {
            id = MySQLQuery.getAsInteger(coordRow[0]);
            lat = MySQLQuery.getAsDouble(coordRow[1]);
            lon = MySQLQuery.getAsDouble(coordRow[2]);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
