package web.tanks;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Timeout;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@WebServlet(name = "RequestRems", urlPatterns = {"/RequestRems"})
public class RequestRems extends HttpServlet {

    @SuppressWarnings("null")
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        updateResRem(request, response);
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
        return "Remisiones";
    }

    @Timeout
    synchronized public void updateResRem(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<Integer> rems = new ArrayList<>();
        try (Connection con = MySQLCommon.getConnection("sigmads", null);) {

            response.setContentType("text/plain; charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();

            Map<String, String> req = MySQLQuery.scapedParams(request);
            String numberBills = req.get("numberRems");
            String employeeId = req.get("employeeId");

            new MySQLQuery("UPDATE est_res_rem_hist SET emp_id = NULL WHERE emp_id = " + employeeId + " AND used = 0").executeUpdate(con);
            Object[][] records = new MySQLQuery("SELECT rem_num FROM est_res_rem_hist WHERE emp_id IS NULL AND used=0 LIMIT " + Integer.valueOf(numberBills) + " FOR UPDATE").getRecords(con);
            for (Object[] row : records) {
                rems.add(MySQLQuery.getAsInteger(row[0]));
            }
            if (!rems.isEmpty()) {
                StringBuilder cadRems = new StringBuilder();
                for (Integer numRem : rems) {
                    cadRems.append(numRem).append(",");
                }
                cadRems.deleteCharAt(cadRems.length() - 1);
                new MySQLQuery("UPDATE est_res_rem_hist SET emp_id=" + employeeId + " WHERE rem_num IN (" + cadRems + ")").executeUpdate(con);
                out.write(cadRems.toString());
            }
        } catch (Exception e) {
            Logger.getLogger(RequestRems.class.getName()).log(Level.SEVERE, null, e);
            response.sendError(500, e.getMessage());
        }
    }
}
