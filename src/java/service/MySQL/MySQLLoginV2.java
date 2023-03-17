package service.MySQL;

import controller.RuntimeVersion;
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
import model.menu.Credential;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "MySQLLoginV2", urlPatterns = {"/ds/MySQLLoginV2"})
public class MySQLLoginV2 extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        try {
            ois = new ObjectInputStream(new GZIPInputStream(request.getPart("data").getInputStream()));
            String poolName = MySQLQuery.scape(ois.readUTF());
            String tz = MySQLQuery.scape(ois.readUTF());
            String user = MySQLQuery.scape(ois.readUTF());
            String pass = MySQLQuery.scape(ois.readUTF());
            boolean sign = true;
            String type = "web";
            String extras = "";
            String phone = "";
            String pack = "";
            boolean returnToken = false;
            try {
                type = MySQLQuery.scape(ois.readUTF());
            } catch (Exception ex) {
            }

            try {
                extras = MySQLQuery.scape(ois.readUTF());
            } catch (Exception ex) {
            }

            try {
                phone = MySQLQuery.scape(ois.readUTF());
            } catch (Exception ex) {
            }

            try {
                pack = MySQLQuery.scape(ois.readUTF());
            } catch (Exception ex) {
            }

            try {
                returnToken = ois.readBoolean();
            } catch (Exception ex) {
            }

            try {
                ois.close();
            } catch (Exception ex) {
            }

            oos = new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));
            if (type.equals("pc")) {
                throw new RuntimeVersion("Usa una versión anterior, reabra el software para actualizar.\nSi el problema continua contacte a soporte.");
            }
            Credential cred = LoginController.getByCredentials(getServletContext(), user, pass, type, extras, phone, pack, request, returnToken, null, poolName, tz, sign);
            oos.writeUTF("ok");
            oos.writeInt(0);
            oos.writeObject(cred.getDaysLeftPasswordExpiration());
            oos.writeInt(cred.getEmployeeId());
            oos.writeUTF(cred.getSessionId());
            if (returnToken) {
                oos.writeUTF(cred.getToken() != null ? cred.getToken() : "");
                oos.writeUTF(cred.getProjectNum());
            }
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
