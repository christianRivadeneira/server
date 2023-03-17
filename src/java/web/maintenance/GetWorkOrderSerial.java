package web.maintenance;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import model.system.SessionLogin;
import static service.MySQL.MySQLCommon.getConnection;
import utilities.IO;
import utilities.MySQLQuery;

@WebServlet(name = "GetWorkOrderSerial", urlPatterns = {"/GetWorkOrderSerial"})
public class GetWorkOrderSerial extends HttpServlet {

    protected static void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> params = MySQLQuery.scapedParams(request);
        String sessionId = params.get("sessionId");
        String poolName = params.get("poolName");
        String tz = params.get("tz");
        String oper = params.get("oper");
        String serial = params.get("serial");
        oper = (oper != null ? oper : "get");
        if (poolName == null) {
            poolName = "sigmads";
            tz = null;
        }

        try (PrintWriter pw = new PrintWriter(response.getOutputStream())) {
            try (Connection conn = getConnection(poolName, tz)) {
                SessionLogin.validate(sessionId, conn);
                if (!oper.equals("get") && !oper.equals("lock") && !oper.equals("unlock")) {
                    throw new Exception("\"oper\" debe ser lock, get o unlock.");
                }

                Boolean locked = new MySQLQuery("SELECT locked FROM mto_order_serial;").getAsBoolean(conn);
                if (locked == null) {
                    throw new Exception("Debe inicializar el serial primero.");
                }

                switch (oper) {
                    case "get":
                        if (locked) {
                            throw new Exception("Se está configurando en este momento.");
                        }
                        new MySQLQuery("UPDATE mto_order_serial SET last = COALESCE(last, 0) + 1;").executeUpdate(conn);
                        break;
                    case "lock":
                        if (locked) {
                            throw new Exception("Se está configurando en este momento.");
                        }
                        new MySQLQuery("UPDATE mto_order_serial SET locked = 1;").executeUpdate(conn);
                        break;
                    case "unlock":
                        if (serial != null) {
                            new MySQLQuery("UPDATE mto_order_serial SET last = " + serial + ";").executeUpdate(conn);
                        }
                        new MySQLQuery("UPDATE mto_order_serial SET locked = 0;").executeUpdate(conn);
                        break;
                    default:
                        break;
                }
                Integer cur = new MySQLQuery("SELECT last FROM mto_order_serial;").getAsInteger(conn);
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
