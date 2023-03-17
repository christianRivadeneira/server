package web.tanks;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@WebServlet(name = "RequestBills", urlPatterns = {"/RequestBills"})
public class RequestBills extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        try (Connection con = MySQLCommon.getConnection("sigmads", null);) {

            response.setContentType("text/plain; charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();

            String numberBills = pars.get("numberBills");
            String employeeId = pars.get("employeeId");

            List<Integer> bills = new ArrayList<>();
            new MySQLQuery("UPDATE est_res_bill_hist SET employee_id = NULL WHERE employee_id = " + employeeId + " AND used = 0").executeUpdate(con);
            Object[][] data = new MySQLQuery("SELECT bill_num FROM est_res_bill_hist WHERE employee_id IS NULL AND used=0 LIMIT " + Integer.valueOf(numberBills) + " FOR UPDATE").getRecords(con);
            for (Object[] row : data) {
                bills.add(MySQLQuery.getAsInteger(row[0]));
            }

            if (!bills.isEmpty()) {
                StringBuilder cadBills = new StringBuilder();
                for (Integer numBill : bills) {
                    cadBills.append(numBill).append(",");
                }
                cadBills.deleteCharAt(cadBills.length() - 1);
                new MySQLQuery("UPDATE est_res_bill_hist SET employee_id=" + employeeId + " WHERE bill_num IN (" + cadBills + ")").executeUpdate(con);
                out.write(cadBills.toString());
            }
        } catch (Exception e) {
            Logger.getLogger(RequestBills.class.getName()).log(Level.SEVERE, null, e);
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
        return "Facturas";
    }
}
