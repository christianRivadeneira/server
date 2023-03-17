package web.billing;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import model.system.SessionLogin;
import utilities.IO;
import utilities.MySQLQuery;

@WebServlet(name = "GetSerialTicket", urlPatterns = {"/GetSerialTicket"})
public class GetSerialTicket extends BillingServlet {

    protected synchronized void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> params = MySQLQuery.scapedParams(request);
        String oper = params.get("oper");
        String serial = params.get("serial");
        String sessionId = params.get("sessionId");
        int cityId = Integer.valueOf(params.get("cityId"));
        oper = (oper != null ? oper : "get");
        try (PrintWriter pw = new PrintWriter(response.getOutputStream())) {
            try (Connection conn = getConnection(cityId)) {
                SessionLogin.validate(sessionId, conn, "sigma");

                if (!oper.equals("get") && !oper.equals("lock") && !oper.equals("unlock")) {
                    throw new Exception("\"oper\" debe ser lock, get o unlock.");
                }

                Boolean locked = new MySQLQuery("SELECT locked FROM bill_serial_ticket;").getAsBoolean(conn);
                if (locked == null) {
                    throw new Exception("Debe inicializar el serial primero.");
                }

                switch (oper) {
                    case "get":
                        if (locked) {
                            throw new Exception("Se está configurando en este momento.");
                        }
                        new MySQLQuery("UPDATE bill_serial_ticket SET last = COALESCE(last, 0) + 1;").executeUpdate(conn);
                        break;
                    case "lock":
                        if (locked) {
                            throw new Exception("Se está configurando en este momento.");
                        }
                        new MySQLQuery("UPDATE bill_serial_ticket SET locked = 1;").executeUpdate(conn);
                        break;
                    case "unlock":
                        if (serial != null) {
                            new MySQLQuery("UPDATE bill_serial_ticket SET last = " + serial + ";").executeUpdate(conn);
                        }
                        new MySQLQuery("UPDATE bill_serial_ticket SET locked = 0;").executeUpdate(conn);
                        break;
                    default:
                        break;
                }
                Integer cur = new MySQLQuery("SELECT last FROM bill_serial_ticket;").getAsInteger(conn);
                pw.write("ok," + cur);
            } catch (Exception ex) {
                pw.write("error," + (ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName()));
            }
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

    public static String convertStreamToString(Part is) throws IOException {
        if (is == null) {
            return null;
        }
        return IO.convertStreamToString(is.getInputStream());
    }
}
