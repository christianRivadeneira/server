/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web.gps;

import api.BaseAPI;
import api.sys.model.Token;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.sql.Connection;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import web.fileManager;

@WebServlet(name = "getTile", urlPatterns = {"/getTile"})
public class getTile extends HttpServlet {

    private static final int SYS_GPS_TILE = 82;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("image/png");
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        String x = pars.get("x");
        String y = pars.get("y");
        String z = pars.get("z");
        String poolName = pars.get("poolName");
        String tz = null;
        String sessionId = pars.get("sessionId");
                
        try {
            Token t = BaseAPI.getToken(sessionId);
            poolName = t.p;
            tz = t.t;
        } catch (Exception ex) {
            Logger.getLogger(getTile.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try (Connection con = MySQLCommon.getConnection(poolName, tz)) {
            SessionLogin.validate(sessionId, con);
            fileManager.PathInfo pInfo = new fileManager.PathInfo(con);

            Object[] tileRow = new MySQLQuery(""
                    + "SELECT t.id, f.id, t.dt FROM "
                    + "gps_tile t "
                    + "INNER JOIN bfile f ON f.owner_id = t.id AND f.owner_type = " + SYS_GPS_TILE + " "
                    + "WHERE t.x = " + x + " AND t.y = " + y + " AND t.z = " + z
            ).getRecord(con);
            File f;
            if (tileRow != null && tileRow.length > 0) {
                int tileId = MySQLQuery.getAsInteger(tileRow[0]);
                int fileId = MySQLQuery.getAsInteger(tileRow[1]);
                Date tileDate = MySQLQuery.getAsDate(tileRow[2]);
                if (new Date().getTime() - tileDate.getTime() > 604800000) {
                    if (Math.random() < 0.5) {
                        f = update(tileId, fileId, x, y, z, pInfo, con);
                    } else {
                        f = retreive(tileId, fileId, x, y, z, pInfo, con);
                    }
                } else {
                    f = retreive(tileId, fileId, x, y, z, pInfo, con);
                }
            } else {
                f = downloadNew(x, y, z, pInfo, con);
            }
            FileInputStream fis = new FileInputStream(f);
            fileManager.copy(fis, response.getOutputStream(), true, true);
        } catch (Exception e) {
            Logger.getLogger(getTile.class.getName()).log(Level.SEVERE, null, e);
            response.sendError(500);
        }
    }

    private File retreive(int tileId, int fileId, String x, String y, String z, fileManager.PathInfo pInfo, Connection con) throws Exception {
        File file = pInfo.getExistingFile(fileId);
        if (file != null && file.exists()) {
            return file;
        } else {
            return update(tileId, fileId, x, y, z, pInfo, con);
        }
    }

    private File update(int tileId, int fileId, String x, String y, String z, fileManager.PathInfo pInfo, Connection con) throws Exception {
        File tmp = File.createTempFile("tile", ".png");
        HttpURLConnection conn = (HttpURLConnection) new URL("http://tile.openstreetmap.org/" + z + "/" + x + "/" + y + ".png").openConnection();
        conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
        File file = pInfo.getNewFile(fileId);
        try (FileOutputStream fos = new FileOutputStream(tmp)) {
            fileManager.copy(conn.getInputStream(), fos, true, true);
            Files.move(tmp.toPath(), file.toPath(), REPLACE_EXISTING);
            new MySQLQuery("UPDATE gps_tile SET dt = NOW() WHERE id = " + tileId).executeUpdate(con);
            return file;
        } catch (Exception ex) {
            if (file.exists()) {
                return file;
            } else {
                new MySQLQuery("DELETE FROM bfile WHERE id = " + fileId).executeDelete(con);
                new MySQLQuery("DELETE FROM gps_tile WHERE id = " + tileId).executeDelete(con);
                throw new Exception("No se pudo descargar el tile.", ex);
            }
        } finally {
            tmp.delete();
        }
    }

    private File downloadNew(String x, String y, String z, fileManager.PathInfo pInfo, Connection con) throws Exception {
        File tmp = File.createTempFile("tile", ".png");
        HttpURLConnection conn = (HttpURLConnection) new URL("http://tile.openstreetmap.org/" + z + "/" + x + "/" + y + ".png").openConnection();
        conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
        try (FileOutputStream fos = new FileOutputStream(tmp)) {
            fileManager.copy(conn.getInputStream(), fos, true, true);
            int tileId = new MySQLQuery("INSERT INTO gps_tile SET x = " + x + ", y = " + y + ", z = " + z + " , dt = NOW();").executeInsert(con);
            new MySQLQuery("DELETE FROM bfile WHERE owner_id = " + tileId + " AND owner_type = " + SYS_GPS_TILE).executeDelete(con);
            int fileId = new MySQLQuery("INSERT INTO bfile SET "
                    + "file_name = '', description = '' "
                    + ",owner_id = " + tileId + ", owner_type = " + SYS_GPS_TILE + " "
                    + ",created_by = 1, updated_by = 1 "
                    + ",created = NOW(),updated = NOW() "
                    + ",keywords = ''").executeInsert(con);
            File file = pInfo.getNewFile(fileId);
            Files.move(tmp.toPath(), file.toPath(), REPLACE_EXISTING);
            return file;
        } catch (Exception ex) {
            throw new Exception("No se pudo descargar el tile.", ex);
        } finally {
            tmp.delete();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
