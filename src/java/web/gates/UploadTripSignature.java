package web.gates;

import java.io.*;
import java.sql.Connection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import utilities.shrinkfiles.FileShrinker;
import utilities.shrinkfiles.ShrunkenFile;
import web.fileManager;
import web.fileManager.PathInfo;

@MultipartConfig
@WebServlet(name = "uploadTripSignature", urlPatterns = {"/uploadTripSignature"})
public class UploadTripSignature extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        String poolName = pars.get("poolName").toLowerCase();
        String tz = pars.get("tz");
        try (Connection con = MySQLCommon.getConnection(poolName, tz)) {

            PathInfo pInfo = new PathInfo(con);

            int employeeId = Integer.parseInt(pars.get("employee"));
            String fileName = pars.get("fileName").toLowerCase();
            String idsString = pars.get("ids");
            String caracter = pars.get("caracter");
            int ownerType = Integer.parseInt(pars.get("ownerType"));
            int operation = Integer.parseInt(pars.get("operation"));
            String desc = "Firma plataformas y porteria " + (ownerType == 120 ? "Salida" : "Entrada");

            File tmp = File.createTempFile("uploaded", ".bin");
            fileManager.copy(request.getPart("file").getInputStream(), new BufferedOutputStream(new FileOutputStream(tmp)));
            boolean shrunken = false;
            ShrunkenFile sf = FileShrinker.shrinkFile(tmp, fileName, FileShrinker.TYPE_TIFF_PDF);
            if (sf.shrunken && (sf.f != null && (sf.f.length() > 0))) {
                FileUtils.forceDelete(tmp);
                tmp = sf.f;
                if (desc.equals(fileName)) {
                    desc = sf.fileName;
                }
                fileName = sf.fileName;
                shrunken = true;
            }
            String[] ids = idsString.split(",");
            for (int i = 0; i < ids.length; i++) {
                String values = ids[i];
                if (values.length() > 0) {
                    String[] valuesSplit = values.split("%");
                    int ownerId = Integer.parseInt(valuesSplit[0]);
                    String queryBfile = "INSERT INTO bfile SET "
                            + "file_name = '" + fileName + "' "
                            + ",description = '" + desc + "' "
                            + ",owner_id = " + ownerId + " "
                            + ",owner_type = " + ownerType + " "
                            + ",created_by = " + employeeId + " "
                            + ",updated_by = " + employeeId + " "
                            + ",size = " + tmp.length() + " "
                            + ",created = NOW() "
                            + ",updated = NOW() "
                            + ",keywords = ''"
                            + ",shrunken = " + (shrunken ? "1" : "0");
                    String query, query2, query3 = queryBfile;

                    if (operation == 1) {
                        //query = "UPDATE gt_cyl_trip SET try_num = 0," + caracter + "dt = NOW() WHERE id = " + ownerId;
                        query2 = "UPDATE gt_cyl_trip SET steps = IF(cdt IS NOT NULL,1,0)+IF(sdt IS NOT NULL,1,0)+IF(edt IS NOT NULL,1,0)+IF(ddt IS NOT NULL,1,0) WHERE id = " + ownerId;
                    } else {

                        String subquery = "";
                        if (pars.get("type") != null) {
                            subquery = " AND type ='" + pars.get("type") + "'";
                        }
                        String notes = pars.get("notes");
                        String typeLog = pars.get("typeLog");

                        query = "UPDATE gt_glp_inv SET inv_date = NOW(), single = " + valuesSplit[i] + " WHERE id = " + ownerId + subquery;
                        query2 = "UPDATE gt_glp_trip SET steps = (SELECT COUNT(*) FROM gt_glp_inv WHERE trip_id = " + ownerId + " AND IF(single, minutes_begin IS NOT NULL, (minutes_begin IS NOT NULL AND minutes_end IS NOT NULL))) WHERE id = " + ownerId;
                        query3 = "INSERT INTO gt_glp_trip_log SET trip_id = " + ownerId + ", employee_id = " + employeeId + ", log_date = NOW(), type = '" + typeLog + "', notes = 'Dispositivo: Movil \\n" + notes + "', glp_inv_id = " + null;
                    }

                    //new MySQLQuery(query).executeUpdate(con); //comentado porque desde la app ya hace éste mismo query
                    new MySQLQuery(query2).executeUpdate(con);
                    int fileId = new MySQLQuery(query3).executeInsert(con);
                    if (operation == 2) {
                        fileId = new MySQLQuery(queryBfile).executeInsert(con);
                    }
                    File nFile = pInfo.getNewFile(fileId);
                    FileUtils.copyFile(tmp, nFile);
                }
            }
            tmp.delete();
            response.setStatus(200);

        } catch (Exception ex) {
            Logger.getLogger(UploadTripSignature.class.getName()).log(Level.SEVERE, null, ex);
            response.reset();
            if (ex.getMessage() != null) {
                response.sendError(500, ex.getMessage());
            } else {
                response.sendError(500);
            }
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
