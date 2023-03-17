package web.ordering;

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
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@WebServlet(name = "GetOrderingSerial", urlPatterns = {"/GetOrderingSerial"})
public class GetSerial extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Map<String, String> pars = MySQLQuery.scapedParams(request);
        String type = pars.get("type");
        String officeId = pars.get("officeId");
        String sessionId = pars.get("sessionId");

        try (Connection conn = MySQLCommon.getConnection("sigmads", null); PrintWriter w = new PrintWriter(response.getOutputStream())) {
            SessionLogin.validate(sessionId, conn);
            switch (type) {
                case "pqr": {
                    w.write(String.valueOf(getPqrCylSerial(officeId, conn)));
                    break;
                }
                case "repair": {
                    w.write(String.valueOf(getRepairSerial(officeId, conn)));
                    break;
                }
                case "tank": {
                    w.write(String.valueOf(getPqrTankSerial(officeId, conn)));
                    break;
                }
                case "other": {
                    w.write(String.valueOf(getPqrOtherSerial(officeId, conn)));
                    break;
                }
                default: {
                    response.sendError(500, "Parámetros Incorrectos.");
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(GetSerial.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500, ex.getMessage());
        }
    }

    public synchronized static int getPqrCylSerial(String officeId, Connection conn) throws Exception {
        return GetSerial.getSerial(officeId, "seq_cyl", conn);
    }

    public synchronized static int getRepairSerial(String officeId, Connection conn) throws Exception {
        return GetSerial.getSerial(officeId, "seq_repairs", conn);
    }

    public synchronized static int getPqrTankSerial(String officeId, Connection conn) throws Exception {
        return GetSerial.getSerial(officeId, "seq_tank", conn);
    }

    public synchronized static int getPqrOtherSerial(String officeId, Connection conn) throws Exception {
        return GetSerial.getSerial(officeId, "seq_other", conn);
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
        return "Get Serials";
    }

    private static int getSerial(String officeId, String field, Connection conn) throws Exception {
        if (new MySQLQuery("SELECT global_serials FROM ord_cfg").getAsBoolean(conn)) {
            if (new MySQLQuery("SELECT count(*) = 0 FROM ord_global_serial").getAsBoolean(conn)) {
                new MySQLQuery("INSERT INTO ord_global_serial (id) values (1);").executeInsert(conn);
            }
            int serie = new MySQLQuery("SELECT " + field + " FROM ord_global_serial").getAsInteger(conn);
            serie++;
            new MySQLQuery("UPDATE ord_global_serial SET " + field + " = " + serie).executeUpdate(conn);
            return serie;
        } else {
            int serie = new MySQLQuery("SELECT " + field + " FROM ord_office WHERE id = " + officeId + "").getAsInteger(conn);
            serie++;
            new MySQLQuery("UPDATE ord_office SET " + field + " = " + serie + " WHERE id = " + officeId + "").executeUpdate(conn);
            return serie;
        }
    }

}
