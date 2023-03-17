package web.push;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
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

@WebServlet(name = "updateGcmToken", urlPatterns = {"/updateGcmToken"})
public class updateGcmToken extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Map<String, String> pars = MySQLQuery.scapedParams(request);
        int empId = Integer.valueOf(pars.get("empId"));
        String appPackage = pars.get("appPackage");
        String imei = pars.get("imei");
        String token = pars.get("token");
        String poolName = pars.get("poolName");
        String tz = pars.get("tz");

        try (Connection con = MySQLCommon.getConnection(poolName, tz); PrintWriter out = response.getWriter()) {
            try {
                con.setAutoCommit(false);
                Integer appId = new MySQLQuery("SELECT id FROM system_app WHERE package_name = '" + appPackage + "'").getAsInteger(con);
                SysGcmToken t = SysGcmToken.getByImei(appId, imei, con);
                if (t != null) {
                    t.empId = empId;
                    if (token != null) {
                        t.token = token;
                    }
                    t.update(t, con);
                } else {
                    if (token == null) {
                        throw new Exception("El token no puede ser nulo para la creación.");
                    }
                    t = new SysGcmToken();
                    t.appId = appId;
                    t.empId = empId;
                    t.imei = imei;
                    t.token = token;
                    t.insert(t, con);
                }
                con.commit();
                out.write("ok");
            } catch (Exception ex) {
                Logger.getLogger(updateGcmToken.class.getName()).log(Level.SEVERE, null, ex);
                con.rollback();
                out.write("error");
            }
        } catch (Exception ex) {
            Logger.getLogger(updateGcmToken.class.getName()).log(Level.SEVERE, null, ex);
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
