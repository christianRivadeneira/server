package web.sessionReport;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
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
@WebServlet(name = "SessionReport", urlPatterns = {"/SessionReport"})
public class SessionReport extends HttpServlet {

    private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        
        response.setContentType("text/html;charset=UTF-8");

        try (Connection con = MySQLCommon.getConnection("sigmads", null)) {
            String begDate = pars.get("begDate");
            String endDate = pars.get("endDate");
            String pass = pars.get("pass");
            if (begDate == null || endDate == null || pass == null) {
                response.sendError(400, "?begDate=yyyy-MM-dd&endDate=yyyy-MM-dd&pass=secret");
            } else if (!pass.equals("Zj6MaFGg")) {
                response.sendError(401, "contraseña incorrecta");
            } else {
                Date beg;
                Date end;
                try {
                    beg = SDF.parse(begDate);
                    end = SDF.parse(endDate);
                    MySQLQuery q = new MySQLQuery("SELECT "
                            + "CONCAT(e.first_name, ' ', e.last_name),"
                            + "user_ip, "
                            + "s.type, "
                            + "s.begin_time, "
                            + "s.end_time "
                            + "FROM session_login s "
                            + "INNER JOIN employee e ON e.id = s.employee_id "
                            + "WHERE "
                            + "DATE(s.begin_time) BETWEEN ?1 AND ?2 "
                            + "ORDER BY s.begin_time DESC").setParam(1, beg).setParam(2, end);

                    Object[][] data = q.getRecords(con);

                    response.setHeader("Content-Disposition", "attachment;filename=SessionReport.csv");
                    response.setContentType("text/csv;charset=ISO-8859-1");
                    try (PrintWriter out = response.getWriter()) {

                        out.write("Usuario, IP, tipo, inicio, fin");
                        out.write(System.lineSeparator());
                        for (Object[] row : data) {
                            out.write(row[0] + ","
                                    + row[1] + ","
                                    + row[2] + ","
                                    + row[3] + ","
                                    + (row[4] != null ? row[4] : ""));
                            out.write(System.lineSeparator());
                        }
                    }
                } catch (ParseException ex) {
                    response.sendError(400, "Las fechas deben estar en formato: yyyy-MM-dd");
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(SessionReport.class.getName()).log(Level.SEVERE, null, ex);
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
        return "Reporte CSV de sesiones";
    }
}
