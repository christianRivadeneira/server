package web.marketing.cylSales;

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
import org.apache.commons.io.FileUtils;
import service.MySQL.MySQLCommon;
import service.MySQL.MySQLSelect;
import utilities.MySQLQuery;
import web.fileManager;
import static web.fileManager.copy;

@MultipartConfig
@WebServlet(name = "UploadSalePhoto", urlPatterns = {"/UploadSalePhoto"})
public class UploadSalePhoto extends HttpServlet {

    private static final String STATUS_ERROR = "ERROR";
    private static final int TRK_NIF_ILLEGIBLE = 128;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;

        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            conn = MySQLCommon.getConnection("sigmads", null);
            JsonObjectBuilder ob = Json.createObjectBuilder();
            String fileName = request.getParameter("fileName");
            InputStream fileStream = request.getPart("file").getInputStream();
            String name = fileName;
            Boolean photoNifRec = new MySQLQuery("SELECT photo_nif_rec from com_cfg").getAsBoolean(conn);

            try {
                if (photoNifRec) {
                    fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                    String fileparts[] = fileName.split("_");
                    String nif = fileparts[1];
                    Integer empId = Integer.valueOf(fileparts[2]);
                    String billNum = fileparts[3];

                    String fs = nif.substring(2, nif.length() - 6);
                    int f = Integer.valueOf(fs);
                    int y = Integer.valueOf(nif.substring(0, 2));
                    int s = Integer.valueOf(nif.substring(nif.length() - 6));

                    Integer ownerId = new MySQLQuery("SELECT id "
                            + " FROM trk_sale "
                            + " WHERE emp_id = " + empId
                            + " AND bill = '" + billNum + "' "
                            + " AND cube_nif_y = " + y
                            + " AND cube_nif_f = " + f
                            + " AND cube_nif_s = " + s
                            + "").print().getAsInteger(conn);

                    Boolean existFile = new MySQLQuery("SELECT COUNT(*)>0 FROM bfile WHERE "
                            + " owner_id = " + ownerId
                            + " AND owner_type = " + TRK_NIF_ILLEGIBLE).getAsBoolean(conn);

                    if (ownerId == null || existFile) {
                        ob.add("status", "ok");
                    } else if (!existFile) {
                        File tmp = File.createTempFile("uploaded", ".bin");
                        copy(fileStream, new BufferedOutputStream(new FileOutputStream(tmp)));
                        saveFile(conn, tmp, empId, ownerId, TRK_NIF_ILLEGIBLE, name);
                        ob.add("status", "ok");
                    }
                } else {
                    //borrar todas las fotos de los celulares
                    ob.add("status", "ok");
                }
            } catch (Exception ex) {
                Logger.getLogger(UploadSalePhoto.class.getName()).log(Level.SEVERE, null, ex);
                response.sendError(500);
                ob.add("status", STATUS_ERROR);
                ob.add("msg", ex.getMessage());
            } finally {
                w.writeObject(ob.build());
            }

        } catch (Exception ex) {
            Logger.getLogger(UploadSalePhoto.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500);
        } finally {
            MySQLSelect.tryClose(conn);
        }

    }

    private void saveFile(Connection con, File tmp, int empId, int ownerId, int ownerType, String fileName) throws Exception {
        fileManager.PathInfo pInfo = new fileManager.PathInfo(con);
        fileManager.upload(empId, ownerId, ownerType, fileName, "Cilindro Ilegible", null, null, pInfo, tmp, con);
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
        return "Cyl Info";

    }
}
