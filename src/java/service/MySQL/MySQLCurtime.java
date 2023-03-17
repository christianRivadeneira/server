package service.MySQL;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@MultipartConfig
@WebServlet(name = "MySQLCurtime", urlPatterns = {"/ds/MySQLCurtime"})
public class MySQLCurtime extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        Connection con = null;
        Statement st = null;
        try {
            ois = new ObjectInputStream(new GZIPInputStream(request.getPart("data").getInputStream()));
            String poolName = ois.readUTF();
            String tz = ois.readUTF();
            ois.close();

            con = MySQLCommon.getConnection(poolName, tz);
            st = con.createStatement();

            try (ResultSet rs = st.executeQuery("SELECT unix_timestamp(now())")) {
                rs.next();
                oos = new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));
                oos.writeUTF("ok");
                oos.writeLong(rs.getLong(1));
                oos.close();
            }
        } catch (Exception ex) {
            MySQLSelect.tryToSendError(oos, ex);
        } finally {
            MySQLSelect.tryClose(ois);
            MySQLSelect.tryClose(oos);
            MySQLCommon.closeConnection(con, st);
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
