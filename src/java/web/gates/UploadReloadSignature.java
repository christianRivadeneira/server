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
@WebServlet(name = "uploadReloadSignature", urlPatterns = {"/uploadReloadSignature"})
public class UploadReloadSignature extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        String poolName = pars.get("poolName").toLowerCase();
        String tz = pars.get("tz");
        try (Connection con = MySQLCommon.getConnection(poolName, tz)) {

            PathInfo pInfo = new PathInfo(con);

            int employeeId = Integer.parseInt(pars.get("employee"));
            String fileName = pars.get("fileName").toLowerCase();
            String ownerId = pars.get("reloadId");
            String ownerType = pars.get("ownerType");
            String desc = "Firma plataformas y porteria Recargue";

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

            String[] id = ownerId.split(",");
            for (int i = 0; i < id.length; i++) {
                String queryBfile = "INSERT INTO bfile SET "
                        + "file_name = '" + fileName + "' "
                        + ",description = '" + desc + "' "
                        + ",owner_id = " + id[i] + " "
                        + ",owner_type = " + ownerType + " "
                        + ",created_by = " + employeeId + " "
                        + ",updated_by = " + employeeId + " "
                        + ",size = " + tmp.length() + " "
                        + ",created = NOW() "
                        + ",updated = NOW() "
                        + ",keywords = ''"
                        + ",shrunken = " + (shrunken ? "1" : "0");

                int idFile = new MySQLQuery(queryBfile).executeInsert(con);
                File nFile = pInfo.getNewFile(idFile);
                FileUtils.copyFile(tmp, nFile);
            }
            tmp.delete();
            response.setStatus(200);
        } catch (Exception ex) {
            Logger.getLogger(UploadReloadSignature.class.getName()).log(Level.SEVERE, null, ex);
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
