package controller.system;

import api.sys.model.SysCfg;
import api.sys.model.Token;
import controller.RuntimeVersion;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import model.menu.Credential;
import service.MySQL.MySQLCommon;
import utilities.AES;
import utilities.MySQLQuery;
import web.ShortException;

public class LoginController {

    private static String getServerSign(ServletContext ctx) throws Exception {
        File clJar = new File(ctx.getRealPath("/") + "client.jar");
        if (clJar.exists()) {
            try (FileInputStream fis = new FileInputStream(clJar); ZipInputStream zis = new ZipInputStream(fis)) {
                ZipEntry origEntry;
                while ((origEntry = zis.getNextEntry()) != null) {
                    if (origEntry.getName().equals("META-INF/MANIFEST.MF")) {
                        try (InputStreamReader is = new InputStreamReader(zis); BufferedReader b = new BufferedReader(is)) {
                            String line;
                            while ((line = b.readLine()) != null) {
                                if (line.contains("Bundle-Date")) {
                                    return line.replaceAll("Bundle-Date[ ]*:", "").trim();
                                }
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }

    public static Credential getByCredentials(ServletContext ctx, String login, String pass, String type, String extras, String phone, String pack, HttpServletRequest req, boolean returnToken, String clieCompilDate, String poolName, String tz, boolean sign) throws Exception, RuntimeVersion {
        Connection con = null;
        Statement st = null;
        try {
            con = MySQLCommon.getConnection(poolName, tz);
            if (type.equals("pc") && (clieCompilDate != null && !clieCompilDate.isEmpty()) && sign) {
                String srvCompilDate = getServerSign(ctx);
                if (srvCompilDate != null && !srvCompilDate.equals(clieCompilDate)) {
                    System.out.println(srvCompilDate);
                    System.out.println(clieCompilDate);
                    throw new RuntimeVersion("Usa una versión anterior, reabra el software para actualizar.\nSi el problema continua contacte a soporte.");
                }
            }

            if (returnToken && ((phone == null || phone.isEmpty()) || (pack == null || pack.isEmpty()))) {
                throw new Exception("Para retornar token debe indicar IMEI y paquete.");
            }

            SysCfg cfg = SysCfg.select(con);

            MySQLQuery q;

            q = new MySQLQuery(""
                    + "SELECT e.id, e.last_password_change, " + (type.equals("pc") ? "e.uni_desktop_session" : (type.equals("android") ? "e.uni_movil_session" : "false")) + ", e.imei_movil_session FROM "
                    + "employee as e "
                    + "WHERE e.login = ?1 AND e.password = ?2 AND e.active");
            q.setParam(1, login);
            q.setParam(2, pass);

            Object[] loginRow = q.getRecord(con);

            if (loginRow != null) {
                int empId = MySQLQuery.getAsInteger(loginRow[0]);
                boolean closeSessions = (cfg.uniqueSession ? MySQLQuery.getAsBoolean(loginRow[2]) : false);
                String imeiSession = (cfg.uniqueSession ? MySQLQuery.getAsString(loginRow[3]) : null);
                String projNum = null;
                String token = null;
                if (returnToken) {
                    Object[] appRecord = new MySQLQuery("SELECT id, google_project_number FROM `system_app` WHERE `package_name` = ?1").setParam(1, pack).getRecord(con);
                    int appId = MySQLQuery.getAsInteger(appRecord[0]);
                    projNum = MySQLQuery.getAsString(appRecord[1]);
                    token = new MySQLQuery("SELECT `token` FROM sys_gcm_token WHERE `app_id` = ?1 AND `imei` = ?2 AND emp_id = ?3").setParam(1, appId).setParam(2, phone).setParam(3, empId).getAsString(con);
                }
                Date lastPassChange = MySQLQuery.getAsDate(loginRow[1]);

                if (!req.getRemoteAddr().equals("127.0.0.1") && empId != 1) {
                    //if (!SystemKeysController.isActive(st)) {
                        // throw new Exception("Esta copia del software no está autorizada.\nSi considera que no debe recibir este mensaje, pongase en contado con el personal de soporte.");
                    //}
                }

                Credential cred = new Credential();
                cred.setEmployeeId(empId);
                cred.setToken(token);
                cred.setProjectNum(projNum);

                if (lastPassChange != null && empId != 1) {
                    int passExpDays = new MySQLQuery("SELECT password_expires_days FROM sys_cfg").getAsInteger(con);
                    if (passExpDays > 0) {
                        long t = new Date().getTime();
                        t -= (t % 86400000);
                        cred.setDaysLeftPasswordExpiration(passExpDays - (int) Math.ceil((t - lastPassChange.getTime()) / 86400000d));
                    }
                }
                cred.setSessionId(openSession(empId, type, closeSessions, imeiSession, extras, phone, pack, con, poolName, tz, req));
                return cred;
            } else {
                Thread.sleep(2000);
                System.out.println(login + "@" + req.getRemoteAddr());
                throw new ShortException("El nombre de usuario y contraseña no son válidos.");
            }
        } finally {
            MySQLCommon.closeConnection(con, st);
        }
    }

    public static String openSession(int empId, String type, boolean closeSessions, String imeiSession, String extras, String phone, String pack, Connection conn, String poolName, String tz, HttpServletRequest req) throws Exception {
        int id;
        if (closeSessions && type.equals("android") && imeiSession != null && !imeiSession.equals(phone)) {
            throw new ShortException("IMEI no autorizado, no puede usar la App en éste dispositivo.");
        }
        try {
            id = new MySQLQuery("INSERT INTO session_login SET extras = '" + extras + "', type = '" + type + "', begin_time = NOW(), employee_id = " + empId + ", session_id = '', server_ip = '" + req.getServerName() + "', user_ip = '" + req.getRemoteAddr() + "', phone = '" + phone + "', app_id = (SELECT id FROM system_app WHERE package_name = '" + pack + "')").executeInsert(conn);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("No se pudo recuperar la llave.");
        }
        //clave de la sessión
        Token t = new Token();
        t.p = poolName;
        t.t = tz;
        t.id = id;
        //ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //JSONEncoder.encode(t, baos);
        //String json = new String(baos.toByteArray());
        String sessId = AES.encrypt(t.serialize(), Token.KEY);
        new MySQLQuery("UPDATE session_login SET session_id = \"" + sessId + "\" WHERE id = " + id).executeUpdate(conn);
        //cerrar cualquier otra sesión que pueda haber quedado abierta.
        if (closeSessions && empId != 1) {
            new MySQLQuery("UPDATE session_login SET end_time = now() WHERE id <> " + id + " AND type = '" + type + "' AND employee_id = " + empId).executeUpdate(conn);
        }

        return sessId;
    }

    public static void closeSession(String sessionId, String poolName, String tz) throws Exception {
        Connection con = null;
        Statement st = null;
        try {
            con = MySQLCommon.getConnection(poolName, tz);
            st = con.createStatement();
            st.executeUpdate("UPDATE session_login SET end_time = now() WHERE session_id = '" + sessionId + "' AND end_time IS NULL");
        } finally {
            MySQLCommon.closeConnection(con, st);
        }
    }
}
