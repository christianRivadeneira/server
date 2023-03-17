package service.MySQL;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import metadata.model.Table;
import model.system.SessionLogin;
import org.apache.commons.io.IOUtils;
import utilities.ClientHttpRequest;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "MySQLUpdate", urlPatterns = {"/ds/MySQLUpdate"})
public class MySQLUpdate extends HttpServlet {

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
            ois.readInt();//no se puede borrar, se mantiene por retro compatibilidad
            int qNum = ois.readInt();

            String[] queries = new String[qNum];
            for (int i = 0; i < qNum; i++) {
                //no se escapa porque es parámetro para un query, sino el query completo
                queries[i] = ois.readUTF();
            }

            String[] servlets;
            if (ois.available() > 0) {
                int sNum = ois.readInt();
                servlets = new String[sNum];
                for (int i = 0; i < sNum; i++) {
                    //no se escapa porque es parámetro para un query, sino el query completo
                    servlets[i] = ois.readUTF();
                }
            } else {
                servlets = new String[0];
            }

            MySQLSelect.tryClose(ois);
            MySQLSelect.tryClose(zis);
            MySQLSelect.tryClose(is);

            os = response.getOutputStream();
            zos = new GZIPOutputStream(os);
            oos = new ObjectOutputStream(zos);
            multiUpdate(queries, servlets, qNum, sessionId, poolName, tz, request, oos);
        } catch (Exception ex) {
            Logger.getLogger(MySQLUpdate.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySQLSelect.tryClose(oos);
            MySQLSelect.tryClose(zos);
            MySQLSelect.tryClose(os);
        }
    }

    public static Exception maskSQLException(String q, int i, Exception ex) {
        String head = q.substring(0, Math.min(q.length(), 10)).trim().toLowerCase();
        if (ex instanceof com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException) {
            String msg;
            if (!Table.DEVEL_MODE) {
                if (head.startsWith("insert ")) {
                    msg = "Ya existe un registro con estos datos, no se puede guardar.\nVerifique en inactivos.";
                } else if (head.startsWith("update ")) {
                    msg = "Ya existe un registro con estos datos, no se puede actualizar.\nVerifique en inactivos.";
                } else if (head.startsWith("delete ")) {
                    msg = "El elemento tiene otros registros asociados\nNo se puede eliminar.";
                } else {
                    msg = "Error en llaves.";
                }
            } else {
                msg = "Error de llaves en el Query: " + i + ": " + ex.getMessage();
            }
            return new Exception(msg);
        } else {
            String msg;
            if (!Table.DEVEL_MODE) {
                msg = "Error en consulta a la base de datos\nContacte al administrador";
            } else {
                msg = ex.getMessage();
                msg = msg != null && !msg.isEmpty() ? msg : ex.getClass().getName();
                msg = "Error en query: " + i + ": " + msg;
            }
            return new Exception(msg);
        }
    }

    private static void multiUpdate(String[] queries, String[] servlets, int sign, String session_id, String poolName, String tz, HttpServletRequest req, ObjectOutputStream oos) throws Exception {
        Connection con = null;
        Statement st = null;
        String failedQuery = null;
        int empId;
        try {
            con = MySQLCommon.getConnection(poolName, tz);
            con.setAutoCommit(false);
            st = con.createStatement();
            if (poolName.startsWith("billing_")) {
                empId = SessionLogin.validate(session_id, con, "sigma").employeeId;
            } else {
                empId = SessionLogin.validate(session_id, con, null).employeeId;
            }

            int[] res = new int[queries.length];

            for (int i = 0; i < queries.length; i++) {
                String q = queries[i];
                String head = q.substring(0, Math.min(q.length(), 10)).trim().toLowerCase();
                try {
                    q = replaceLIds(i, res, q);
                    if (Table.DEVEL_MODE || MySQLQuery.PRINT_QUERIES) {
                        Logger.getLogger(MySQLQuery.class.getName()).log(Level.INFO, q);
                    }
                    if (head.startsWith("insert ")) {
                        int affectedRows = st.executeUpdate(q, Statement.RETURN_GENERATED_KEYS);
                        if (affectedRows == 1) {
                            try (ResultSet rs1 = st.getGeneratedKeys()) {
                                if (rs1.next()) {
                                    res[i] = rs1.getInt(1);
                                } else {
                                    //se comenta por la tabla que no tiene auto_increment
                                    //throw new Exception("No se pudo recuperar la llave.");
                                }
                            }
                        } else {
                            res[i] = affectedRows;
                        }
                    } else {
                        res[i] = st.executeUpdate(q);
                    }
                } catch (Exception ex) {
                    failedQuery = q;
                    Logger.getLogger(MySQLUpdate.class.getName()).log(Level.SEVERE, null, ex);
                    throw maskSQLException(q, i, ex);
                }
            }

            con.commit();

            String[] servletResps = new String[servlets.length];
            String baseUrl = "http://" + req.getLocalAddr() + ":" + req.getLocalPort() + req.getContextPath();

            for (int i = 0; i < servlets.length; i++) {
                String url = baseUrl + replaceLIds(queries.length, res, servlets[i]);
                try {
                    servletResps[i] = IOUtils.toString(new ClientHttpRequest(url).post(), Charset.defaultCharset());
                } catch (IOException ex) {
                    String msg;
                    if (empId != 1) {
                        msg = "Error en consulta a recurso HTTP local\nContacte al administrador";
                    } else {
                        msg = "Error en servlet: " + i + ": " + url + ": " + ex.getMessage();
                    }
                    Logger.getLogger(MySQLUpdate.class.getName()).log(Level.SEVERE, null, ex);
                    throw new Exception(msg);
                }
            }

            oos.writeUTF("ok");
            oos.writeInt(res.length);
            for (int i = 0; i < res.length; i++) {
                oos.writeInt(res[i]);
            }
            if (servletResps.length > 0) {
                oos.writeInt(servletResps.length);
                for (String servletResp : servletResps) {
                    oos.writeUTF(servletResp);
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(MySQLUpdate.class.getName()).log(Level.SEVERE, null, ex);
            oos.writeUTF("error");
            oos.writeUTF(ex.getMessage() != null ? ex.getMessage() : ex.getClass().toString());
            if (con != null) {
                try {
                    con.rollback();
                    if (failedQuery != null && !failedQuery.isEmpty()) {
                        MySQLQuery.insertFailedQuery(failedQuery, ex, con);
                    }
                } catch (SQLException exi) {
                    Logger.getLogger(MySQLUpdate.class.getName()).log(Level.WARNING, null, exi);
                }
            }
        } finally {
            MySQLCommon.closeConnection(con, st);
        }
    }

    private static String replaceLIds(int queryIndex, int[] lids, String query) {
        for (int i = 0; i < queryIndex; i++) {
            query = Pattern.compile("\\?[lL][iI][dD]" + i + "(?<end>[^0-9]|$)", Pattern.MULTILINE).matcher(query).replaceAll(lids[i] + "${end}");
        }
        return query;
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
}
