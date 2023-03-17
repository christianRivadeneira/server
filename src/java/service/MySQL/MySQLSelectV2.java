package service.MySQL;

import controller.RuntimeVersion;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import metadata.model.Table;
import model.system.ClosedSessionException;
import model.system.SessionLogin;
import timezone.SigmaDateTime;
import utilities.MySQLQuery;
import web.ShortException;

@MultipartConfig
@WebServlet(name = "MySQLSelectV2", urlPatterns = {"/ds/MySQLSelectV2"})
public class MySQLSelectV2 extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        InputStream is;
        GZIPInputStream zis;
        ObjectInputStream ois;

        OutputStream os = null;
        GZIPOutputStream zos = null;
        ObjectOutputStream oos = null;

        try {
            is = request.getPart("data").getInputStream();
            zis = new GZIPInputStream(is);
            ois = new ObjectInputStream(zis);

            String sessionId = MySQLQuery.scape(ois.readUTF());
            String poolName = MySQLQuery.scape(ois.readUTF());
            String tz = MySQLQuery.scape(ois.readUTF());
            int qNum = ois.readInt();

            String[] queries = new String[qNum];
            for (int i = 0; i < qNum; i++) {
                //no se escapan porque son directamente queries
                queries[i] = ois.readUTF();
            }

            tryClose(ois);
            tryClose(zis);
            tryClose(is);

            os = response.getOutputStream();
            zos = new GZIPOutputStream(os);
            oos = new ObjectOutputStream(zos);
            multiSelectReq(queries, sessionId, poolName, tz, oos);
        } catch (Exception ex) {
            if (oos != null) {
                oos.writeUTF("error");
            }
        } finally {
            tryClose(oos);
            tryClose(zos);
            tryClose(os);
        }
    }

    public static void multiSelectReq(String[] queries, String session_id, String poolName, String tz, ObjectOutputStream oos) throws Exception {
        Connection con = null;
        Statement st = null;
        String failedQuery = null;
        int curQuery = 0;
        try {
            con = MySQLCommon.getConnection(poolName, tz);
            st = con.createStatement();
            if (poolName.startsWith("billing_")) {
                SessionLogin.validate(session_id, con, "sigma");
            } else {
                SessionLogin.validate(session_id, con, null);
            }

            for (int j = 0; j < queries.length; j++, curQuery++) {
                String query = queries[j];
                failedQuery = query;
                if (query.startsWith("DROP TEMPORARY TABLE") || query.startsWith("CREATE TEMPORARY TABLE")) {
                    st.executeUpdate(query);
                    oos.writeUTF("row");
                    oos.writeInt(1);
                    oos.writeObject(0);
                } else {
                    if (Table.DEVEL_MODE || MySQLQuery.PRINT_QUERIES) {
                        Logger.getLogger(MySQLSelectV2.class.getName()).log(Level.INFO, query);
                    }
                    ResultSet rs = st.executeQuery(query);
                    int cols = rs.getMetaData().getColumnCount();
                    while (rs.next()) {
                        oos.writeUTF("row");
                        oos.writeInt(cols);
                        for (int i = 0; i < cols; i++) {
                            Object obj = rs.getObject(i + 1);
                            if (obj instanceof Date) {
                                oos.writeObject(new SigmaDateTime((Date) obj));
                            } else {
                                oos.writeObject(obj);
                            }
                        }
                    }
                }
                oos.writeUTF("end");
            }
            oos.writeUTF("endAll");
        } catch (ClosedSessionException ex) {
            //Logger.getLogger(MySQLSelect.class.getName()).log(Level.SEVERE, null, ex);
            oos.writeUTF("noSession");
            //oos.writeUTF("error");
            String msg = ex.getMessage();
            System.out.println(msg);
            MySQLQuery.insertFailedQuery(failedQuery, ex, con);
            oos.writeUTF("Query " + curQuery + ": " + msg != null && !msg.isEmpty() ? msg : ex.getClass().getName());
        } catch (Exception ex) {
            Logger.getLogger(MySQLSelectV2.class.getName()).log(Level.SEVERE, null, ex);
            oos.writeUTF("error");
            String msg = ex.getMessage();
            MySQLQuery.insertFailedQuery(failedQuery, ex, con);
            oos.writeUTF(msg != null && !msg.isEmpty() ? "Query " + curQuery + ": " + msg : "Query " + curQuery + ": " + ex.getClass().getName());
        } finally {
            MySQLCommon.closeConnection(con, st);
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

    @Override
    public String getServletInfo() {
        return "";
    }

    public static void tryToSendError(ObjectOutputStream oos, Exception ex) {
        if (ex instanceof ShortException) {
            ((ShortException) ex).simplePrint();
        } else {
            Logger.getLogger(MySQLSelectV2.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (oos != null) {
            try {
                oos.writeUTF("error");
                String msg = ex.getMessage();
                oos.writeUTF(msg != null && !msg.isEmpty() ? msg : ex.getClass().getName());
            } catch (IOException exi) {
            }
        }
    }

    public static void tryToSendError(ObjectOutputStream oos, RuntimeVersion ex) {
        Logger.getLogger(MySQLSelectV2.class.getName()).log(Level.SEVERE, null, ex);
        if (oos != null) {
            try {
                oos.writeUTF("runtime_version");
                String msg = ex.getMessage();
                oos.writeUTF(msg != null && !msg.isEmpty() ? msg : ex.getClass().getName());
            } catch (IOException exi) {
            }
        }
    }

    public static void tryClose(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception ex) {
            }
        }
    }
}
