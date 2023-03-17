package web.emas;

import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "EmasSchedule", urlPatterns = {"/EmasSchedule"})
public class EmasScheduleServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            insertScheduledPath(request.getParameter("poolName"), request.getParameter("date"), Integer.parseInt(request.getParameter("vhId")));
        } catch (Exception ex) {
            Logger.getLogger(EmasScheduleServlet.class.getName()).log(Level.SEVERE, null, ex);
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
        return "Short description";
    }

    private static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371.0; // miles 3958.75 (or 6371.0 kilometers)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;
        return dist;
    }

    public synchronized static void insertScheduledPath(String poolName, String date, Integer vhId) throws Exception {
        try (Connection conn = MySQLCommon.getConnection(poolName, null)) {
            boolean autoSort = new MySQLQuery("SELECT c.auto_sort FROM emas_cfg c LIMIT 1").getAsBoolean(conn);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date progDate = formatter.parse(date);

            Calendar calendar = Calendar.getInstance();
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            calendar.setMinimalDaysInFirstWeek(2);
            calendar.setTime(progDate);
            int curWeek = calendar.get(Calendar.WEEK_OF_YEAR);

            int week = findWeek(curWeek);
            Object[][] pathData = new MySQLQuery("SELECT "
                    + "sede.id, "
                    + "sede.lat, "
                    + "sede.lon, "
                    + "sede.frec_type "
                    + "FROM emas_prog prog "
                    + "INNER JOIN emas_path path ON prog.path_id=path.id "
                    + "INNER JOIN emas_sede_frec_path spath ON path.id=spath.path_id "
                    + "INNER JOIN emas_clie_sede sede ON spath.sede_id = sede.id "
                    + "INNER JOIN emas_client clie ON clie.id=sede.client_id AND clie.active "
                    + "WHERE prog.vh_id = " + vhId + " "
                    + "AND prog.prog_date = ?1 "
                    + "AND spath.week = " + week + " "
                    + "AND !clie.suspended "
                    + "AND sede.active "
                    + "ORDER BY spath.place;").setParam(1, progDate).getRecords(conn);

            Object[][] ocasData = new MySQLQuery("SELECT sede.id, sede.lat, sede.lon "
                    + "FROM emas_prog prog  "
                    + "INNER JOIN emas_prog_sede psede ON psede.prog_id = prog.id "
                    + "INNER JOIN emas_clie_sede sede ON psede.clie_sede_id = sede.id "
                    + "WHERE vh_id = " + vhId + " AND prog.prog_date = ?1 ;").setParam(1, progDate).getRecords(conn);

            List<EmasClientServices> sorted = new ArrayList<>();
            List<EmasClientServices> unsorted = new ArrayList<>();

            for (Object[] pathRow : pathData) {
                sorted.add(EmasClientServices.getFromRow(pathRow, "path"));
            }

            for (Object[] ocasRow : ocasData) {
                if (autoSort) {
                    unsorted.add(EmasClientServices.getFromRow(ocasRow, "ocas"));
                } else {
                    sorted.add(EmasClientServices.getFromRow(ocasRow, "ocas"));
                }
            }

            if (autoSort) {
                for (int j = 0; j < unsorted.size(); j++) {
                    double minD = Double.MAX_VALUE;
                    int minIndex = 0;
                    EmasClientServices cur = unsorted.get(j);

                    if (cur.lat != null && cur.lon != null) {
                        for (int k = 0; k < sorted.size(); k++) {
                            if (sorted.get(k).lat != null && sorted.get(k).lon != null) {
                                double dis = distFrom(cur.lat, cur.lon, sorted.get(k).lat, sorted.get(k).lon);
                                if (dis < minD) {
                                    minD = dis;
                                    minIndex = k;
                                }
                            }
                        }

                        if (minIndex < sorted.size() - 1) {
                            sorted.add(minIndex + 1, cur);
                        } else {
                            sorted.add(cur);
                        }

                    } else {
                        sorted.add(unsorted.get(j));
                    }

                }
            }
            new MySQLQuery("DELETE FROM emas_schedule WHERE visit_date = ?1 AND vh_id = " + vhId + " AND visit_id IS NULL;").setParam(1, date).executeDelete(conn);
            for (int j = 0; j < sorted.size(); j++) {
                Integer driverId = new MySQLQuery("SELECT driver_id FROM emas_vehicle WHERE id = " + vhId).getAsInteger(conn);
                if (driverId != null) {
                    new MySQLQuery("INSERT INTO emas_schedule SET emp_id= " + driverId + ", visit_date = ?1, place = " + j + ", clie_sede_id = " + sorted.get(j).sedeId + ",type = '" + sorted.get(j).type + "', vh_id = " + vhId + ";").setParam(1, date).executeInsert(conn);
                } else {
                    new MySQLQuery("INSERT INTO emas_schedule SET  visit_date = ?1, place = " + j + ", clie_sede_id = " + sorted.get(j).sedeId + ",type = '" + sorted.get(j).type + "', vh_id = " + vhId + ";").setParam(1, date).executeInsert(conn);
                }
            }
        } catch (Exception e) {
            Logger.getLogger(EmasScheduleServlet.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static int findWeek(int curWeek) {
        int month = 0;
        for (int i = 0; i < 13; i++) {
            for (int y = 1; y < 5; y++) {
                month = month + 1;
                if (month == curWeek) {
                    return y;
                }
            }
        }
        return 0;
    }

    static class EmasClientServices {

        public int sedeId;
        public int vehId;
        public String type;
        public Double lat;
        public Double lon;

        public static EmasClientServices getFromRow(Object[] row, String type) {
            EmasClientServices item = new EmasClientServices();
            item.sedeId = MySQLQuery.getAsInteger(row[0]);
            item.type = type;
            item.lat = MySQLQuery.getAsDouble(row[1]);
            item.lon = MySQLQuery.getAsDouble(row[2]);
            return item;
        }
    }
}
