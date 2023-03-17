package web.emas;

import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "EmasConsManHist", urlPatterns = {"/EmasConsManHist"})
public class EmasConsManHist extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try (Connection conn = MySQLCommon.getConnection(request.getParameter("poolName"), null); JsonWriter w = Json.createWriter(response.getOutputStream())) {
            JsonArrayBuilder jar = Json.createArrayBuilder();
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");
            JsonObject req = Json.createReader(request.getPart("data").getInputStream()).readObject();

            try {
                int driverId = req.getInt("driverId");

                int schedSedeCount = new MySQLQuery("SELECT COUNT(*) FROM emas_schedule s INNER JOIN emas_vehicle v ON v.id = s.vh_id INNER JOIN emas_prog p ON p.vh_id = v.id AND CURDATE() BETWEEN p.prog_date AND p.end_date WHERE s.visit_date BETWEEN p.prog_date AND p.end_date AND v.driver_id = " + driverId).getAsInteger(conn);
                int vhFreeMan = new MySQLQuery("SELECT COUNT(*) FROM emas_cons_man_hist WHERE driver_id = " + driverId + " AND used = 0").getAsInteger(conn);

                int manifestsToInsert = (schedSedeCount - vhFreeMan) * 2;

                if (manifestsToInsert > 0) {
                    Integer maxNum = new MySQLQuery("SELECT MAX(man_num) FROM emas_cons_man_hist").getAsInteger(conn);

                    for (int i = 1; i <= manifestsToInsert; i++) {
                        int manNum = (maxNum != null ? maxNum : 0) + i;
                        MySQLQuery mq = new MySQLQuery("INSERT INTO emas_cons_man_hist SET man_num = " + manNum + ", driver_id = " + driverId);
                        mq.executeInsert(conn);
                    }
                }

                Object[][] manNums = new MySQLQuery("SELECT man_num FROM emas_cons_man_hist WHERE driver_id = " + driverId + " AND used = 0").getRecords(conn);

                for (int i = 0; i < manNums.length; i++) {
                    JsonObjectBuilder mob = Json.createObjectBuilder();
                    mob.add("manNum", MySQLQuery.getAsInteger(manNums[i][0]));
                    jar.add(mob);
                }

            } catch (Exception ex) {
                Logger.getLogger(EmasConsManHist.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                w.writeArray(jar.build());
            }

        } catch (Exception ex) {
            Logger.getLogger(EmasConsManHist.class.getName()).log(Level.SEVERE, null, ex);
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
        return "Short description";
    }

}
