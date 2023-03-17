package web.system;

import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import service.MySQL.MySQLSelect;

@MultipartConfig
@WebServlet(name = "Test", urlPatterns = {"/Test"})
public class TestSpeedWeb extends HttpServlet {

    private static final String STATUS_OK = "OK";
    private static final String STATUS_ERROR = "ERROR";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            JsonObjectBuilder ob = Json.createObjectBuilder();

            conn = MySQLCommon.getConnection("sigmads", null);
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");
            try {
                StringBuilder test = new StringBuilder("");
                for (int i = 0; i <= 1052000; i++) {
                    test.append("1");
                }
                ob.add("test", test.toString());
                ob.add("status", STATUS_OK);
                //System.out.println(test);

            } catch (Exception ex) {
                Logger.getLogger(TestSpeedWeb.class.getName()).log(Level.SEVERE, null, ex);
                ob.add("status", STATUS_ERROR);
                ob.add("msg", ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
            } finally {
                w.writeObject(ob.build());
            }
        } catch (Exception ex) {
            Logger.getLogger(TestSpeedWeb.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500, ex.getMessage());
        } finally {
            MySQLSelect.tryClose(conn);
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
}
