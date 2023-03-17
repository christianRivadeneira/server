package service.MySQL;

import controller.system.LoginController;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "MySQLLogout", urlPatterns = {"/ds/MySQLLogout"})
public class MySQLLogout extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        try {
            ois = new ObjectInputStream(new GZIPInputStream(request.getPart("data").getInputStream()));
            String poolName = MySQLQuery.scape(ois.readUTF());
            String tz = MySQLQuery.scape(ois.readUTF());
            String sessionId = MySQLQuery.scape(ois.readUTF());
            ois.close();
            oos = new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));
            LoginController.closeSession(sessionId, poolName, tz);
            oos.writeUTF("ok");
            oos.close();
        } catch (Exception ex) {
            MySQLSelect.tryToSendError(oos, ex);
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
