package service.MySQL;

import controller.RuntimeVersion;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utilities.MySQLQuery;
import web.ShortException;

@MultipartConfig
@WebServlet(name = "MySQLReopenSession", urlPatterns = {"/ds/MySQLReopenSession"})
public class MySQLReopenSession extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        try {
            ois = new ObjectInputStream(new GZIPInputStream(request.getPart("data").getInputStream()));
            String poolName = MySQLQuery.scape(ois.readUTF());
            String tz = MySQLQuery.scape(ois.readUTF());
            String user = MySQLQuery.scape(ois.readUTF());
            String pass = MySQLQuery.scape(ois.readUTF());
            String sessionId = MySQLQuery.scape(ois.readUTF());
            oos = new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));

            try (Connection con = MySQLCommon.getConnection(poolName, tz)) {
                Boolean hasUser = new MySQLQuery("SELECT COUNT(*) > 0 FROM "
                        + "employee as e "
                        + "WHERE e.login = ?1 AND e.password = ?2 AND e.active").setParam(1, user).setParam(2, pass).getAsBoolean(con);
                if (hasUser == null || !hasUser) {
                    Thread.sleep(3000);
                    throw new ShortException("El nombre de usuario y contraseña no son válidos.");
                }
                if (new MySQLQuery("SELECT count(*) = 0 FROM session_login WHERE session_id = ?1 AND end_time IS NULL").setParam(1, sessionId).getAsBoolean(con)) {
                    throw new ShortException("No se encontró la sesión o fue cerrada");
                }
            }
            oos.writeUTF("ok");
        } catch (Exception ex) {
            if (ex instanceof RuntimeVersion) {
                MySQLSelect.tryToSendError(oos, (RuntimeVersion) ex);
            } else {
                MySQLSelect.tryToSendError(oos, ex);
            }
        } finally {
            MySQLSelect.tryClose(ois);
            MySQLSelect.tryClose(oos);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "";
    }
}
