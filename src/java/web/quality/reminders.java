package web.quality;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
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
import utilities.MySQLQuery;

@WebServlet(name = "reminders", urlPatterns = {"/quality/reminders"})
public class reminders extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> req = MySQLQuery.scapedParams(request);
        String poolName = req.get("poolName");
        String tz = req.get("tz");

        Connection con = null;
        Statement st = null;

        try {
            con = MySQLCommon.getConnection(poolName, tz);
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT "
                    + "GROUP_CONCAT(em.per_employee_id), "//1
                    + "mt.`name`, "//2
                    + "m.beg_date, "//3
                    + "o.`name`, "//4
                    + "m.place, "//5
                    + "m.sched_dates, "//6
                    + "m.execution_dates, "//7
                    + "m.reason, "//8
                    + "m.notes "//9
                    + "FROM "
                    + "cal_meet AS m "
                    + "INNER JOIN cal_meet_type AS mt ON m.type_id = mt.id "
                    + "INNER JOIN cal_emp_meeting AS em ON m.id = em.meet_id "
                    + "LEFT JOIN cal_office AS o ON m.office_id = o.id , "
                    + "cal_cfg AS cf "
                    + "WHERE "
                    + "DATEDIFF(m.beg_date,CURDATE()) = cf.remind_1 OR "//0
                    + "DATEDIFF(m.beg_date,CURDATE()) = cf.remind_2 OR "//0
                    + "DATEDIFF(m.beg_date,CURDATE()) = cf.remind_3 "//0
                    + "GROUP BY m.id");

            while (rs.next()) {
                String pers = rs.getString(1);
                String meetingName = rs.getString(2);
                Date beginDate = rs.getDate(3);
                String office = rs.getString(4);
                String place = rs.getString(5);
                String schedDates = rs.getString(6);
                String doneDates = rs.getString(7);
                String reason = rs.getString(8);
                String notes = rs.getString(9);

                StringBuilder dataMsg = new StringBuilder("");
                if (meetingName != null) {
                    dataMsg.append("Reunion: ").append(meetingName).append("<br />");
                }
                if (beginDate != null) {
                    dataMsg.append("Fecha de inicio: ").append(new SimpleDateFormat("dd/MMMM/yyyy").format(beginDate)).append("<br />");
                }
                if (office != null) {
                    dataMsg.append("Oficina: ").append(office).append("<br />");
                }
                if (place != null) {
                    dataMsg.append("Lugar: ").append(place).append("<br />");
                }
                if (schedDates != null) {
                    dataMsg.append("Programación: ").append(schedDates).append("<br />");
                }
                if (doneDates != null) {
                    dataMsg.append("Ejecución: ").append(doneDates).append("<br />");
                }
                if (reason != null) {
                    dataMsg.append("Motivo: ").append(reason).append("<br />");
                }
                if (notes != null) {
                    dataMsg.append("Notas: ").append(notes).append("<br />");
                }
                SendMail.sendMail("employee","Recordatorio de reunión ", dataMsg.toString(), pers, true, null, null, null, poolName, tz, "cal");                
            }

            response.setStatus(200);
            response.getWriter().write("ok");
        } catch (Exception ex) {
            Logger.getLogger(reminders.class.getName()).log(Level.SEVERE, null, ex);
            StackTraceElement[] stacks = ex.getStackTrace();
            StringBuilder sb = new StringBuilder(ex.getClass().toString());
            for (StackTraceElement stack : stacks) {
                sb.append(stack.toString()).append("\r\n");
            }
            response.sendError(500, sb.toString());
        } finally {
            MySQLCommon.closeConnection(con, st);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
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
     * Handles the HTTP
     * <code>POST</code> method.
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
