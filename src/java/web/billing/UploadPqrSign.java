package web.billing;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import utilities.MySQLQuery;
import web.fileManager;
import static web.fileManager.copy;

@MultipartConfig
@WebServlet(name = "UploadPqrSign", urlPatterns = {"/UploadPqrSign"})
public class UploadPqrSign extends HttpServlet {

    private static final String STATUS_ERROR = "ERROR";
    private static final int PQR_POLL_SIGN = 112;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;

        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            conn = MySQLCommon.getConnection("sigmads", null);
            JsonObjectBuilder ob = Json.createObjectBuilder();
            String fileName = request.getParameter("fileName");
            InputStream fileStream = request.getPart("file").getInputStream();
            String name = fileName;

            try {
                fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                String fileparts[] = fileName.split("_");
                Integer pqrId = Integer.valueOf(fileparts[0]);
                Integer pqrType = Integer.valueOf(fileparts[1]);
                Integer empId = Integer.valueOf(fileparts[2]);

                Integer regPollId = null;
                Integer bfileId = null;
                Object[] data;

                switch (pqrType) {
                    case 1:
                        data = new MySQLQuery("SELECT e.id ,b.id "
                                + "FROM ord_pqr_cyl p "
                                + "INNER JOIN ord_poll e ON e.id = p.pqr_poll_id "
                                + "INNER JOIN bfile b ON b.owner_id = e.id "
                                + "WHERE "
                                + "p.id = " + pqrId).getRecord(conn);
                        regPollId = MySQLQuery.getAsInteger(data[0]);
                        bfileId = MySQLQuery.getAsInteger(data[1]);
                        break;
                    case 2:
                        data = new MySQLQuery("SELECT e.id ,b.id "
                                + "FROM ord_pqr_tank p "
                                + "INNER JOIN ord_poll e ON e.id = p.pqr_poll_id "
                                + "INNER JOIN bfile b ON b.owner_id = e.id "
                                + "WHERE "
                                + "p.id = " + pqrId).getRecord(conn);
                        regPollId = MySQLQuery.getAsInteger(data[0]);
                        bfileId = MySQLQuery.getAsInteger(data[1]);
                        break;
                    case 3:
                        data = new MySQLQuery("SELECT e.id ,b.id "
                                + "FROM ord_repairs p "
                                + "INNER JOIN ord_poll e ON e.id = p.pqr_poll_id "
                                + "INNER JOIN bfile b ON b.owner_id = e.id "
                                + "WHERE "
                                + "p.id = " + pqrId).getRecord(conn);
                        regPollId = MySQLQuery.getAsInteger(data[0]);
                        bfileId = MySQLQuery.getAsInteger(data[1]);
                        break;
                }

                if (bfileId != null) {
                    ob.add("status", "ok");
                    ob.add("file", pqrId + "_" + pqrType + "_" + empId);
                } else {
                    if (regPollId != null) {
                        File tmp = File.createTempFile("uploaded", ".bin");
                        copy(fileStream, new BufferedOutputStream(new FileOutputStream(tmp)));
                        saveFile(conn, tmp, empId, regPollId, PQR_POLL_SIGN, name);
                        ob.add("status", "ok");
                        ob.add("file", pqrId + "_" + pqrType + "_" + empId);
                    } else {
                        ob.add("status", "ok");
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(UploadPqrSign.class.getName()).log(Level.SEVERE, null, ex);
                response.sendError(500);
                ob.add("status", STATUS_ERROR);
                ob.add("msg", ex.getMessage());
            } finally {
                w.writeObject(ob.build());
            }

        } catch (Exception ex) {
            Logger.getLogger(UploadPqrSign.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500);
        } finally {
            MySQLSelect.tryClose(conn);
        }

    }

    private void saveFile(Connection con, File tmp, int empId, int ownerId, int ownerType, String fileName) throws Exception {
        fileManager.PathInfo pInfo = new fileManager.PathInfo(con);
        fileManager.upload(empId, ownerId, ownerType, fileName, "Firma Encuesta PQR", null, null, pInfo, tmp, con);
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
        return "Pqr Sign";

    }
}
