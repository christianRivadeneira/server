package service.MySQL;

import controller.RuntimeVersion;
import controller.system.LoginController;
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
import model.menu.Credential;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "MySQLLoginPCV3", urlPatterns = {"/ds/MySQLLoginPCV3"})
public class MySQLLoginPCV3 extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        try {
            ois = new ObjectInputStream(new GZIPInputStream(request.getPart("data").getInputStream()));
            String poolName = MySQLQuery.scape(ois.readUTF());
            String user = MySQLQuery.scape(ois.readUTF());
            String pass = MySQLQuery.scape(ois.readUTF());
            String extras = MySQLQuery.scape(ois.readUTF());
            String signParam = MySQLQuery.scape(ois.readUTF());
            boolean sign = signParam.equals("1");
            String compilDate = "";

            try {
                compilDate = MySQLQuery.scape(ois.readUTF());
            } catch (Exception ex) {
            }

            try {
                ois.close();
            } catch (Exception ex) {
            }
            oos = new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));

            Connection conn = MySQLCommon.getConnection(poolName, null);
            String tz = new MySQLQuery("SELECT time_zone FROM sys_cfg").getAsString(conn);

            Credential cred = LoginController.getByCredentials(getServletContext(), user, pass, "pc", extras, null, null, request, false, compilDate, poolName, tz, sign);
            oos.writeUTF("ok");
            oos.writeInt(0);
            oos.writeObject(cred.getDaysLeftPasswordExpiration());
            oos.writeInt(cred.getEmployeeId());
            oos.writeUTF(cred.getSessionId());
            oos.writeUTF(tz != null ? tz : "");

            oos.close();
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
