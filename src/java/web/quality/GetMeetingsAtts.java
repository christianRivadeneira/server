package web.quality;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import web.fileManager;

@WebServlet(name = "getMeetingsAtts", urlPatterns = {"/getMeetingsAtts"})
public class GetMeetingsAtts extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        try (Connection con = MySQLCommon.getConnection(pars.get("poolName"), pars.get("tz")); ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            SessionLogin.validate(pars.get("sessionId"), con);
            fileManager.PathInfo pi = new fileManager.PathInfo(con);
            String ownerType = pars.get("ownerType");

            List<String> entries = new ArrayList<>();

            Object[][] yearData = new MySQLQuery("SELECT DISTINCT YEAR(cm.beg_date) FROM cal_meet cm ORDER BY cm.beg_date ASC").getRecords(con);
            for (Object[] yearRow : yearData) {
                int year = MySQLQuery.getAsInteger(yearRow[0]);

                Object[][] meetData = new MySQLQuery("SELECT cm.id, CONCAT(cm.beg_date, COALESCE(CONCAT('_',cm.place),'')) FROM cal_meet cm  WHERE YEAR(cm.beg_date) = " + year + " ORDER BY cm.beg_date ASC").getRecords(con);

                for (Object[] meetRow : meetData) {
                    int ownerId = MySQLQuery.getAsInteger(meetRow[0]);
                    String location = MySQLQuery.getAsString(meetRow[1]);
                    Object[][] fileData = new MySQLQuery("SELECT b.id, b.file_name FROM bfile b WHERE b.owner_id =  " + ownerId + " AND b.owner_type = " + ownerType).getRecords(con);
                    for (Object[] fileRow : fileData) {
                        int bfileId = MySQLQuery.getAsInteger(fileRow[0]);
                        String bfileName = MySQLQuery.getAsString(fileRow[1]);
                        File f = pi.getExistingFile(bfileId);
                        if (f!= null && f.exists()) {
                            ZipEntry ze;
                            String pathZip = year + "/" + location + "/" + bfileName;
                            boolean exist = entries.contains(pathZip);
                            ze = (exist ? new ZipEntry(getConsecutiveName(pathZip, entries)) : new ZipEntry(pathZip));

                            entries.add(ze.getName());
                            zos.putNextEntry(ze);
                            try (FileInputStream fin = new FileInputStream(f)) {
                                fileManager.copy(fin, zos, true, false);
                            }
                            zos.closeEntry();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(GetMeetingsAtts.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500, ex.getMessage());
        }
    }

    public String getConsecutiveName(String pName, List<String> entries) throws IOException {

        String fname = pName;
        int dot = fname.lastIndexOf(".");
        String name;
        String type;
        if (dot >= 0) {
            name = fname.substring(0, dot);
            type = fname.substring(dot, fname.length());
        } else {
            name = fname;
            type = "";
        }

        boolean exists = true;
        String curName = "";
        for (int i = 0; exists; i++) {
            curName = (i == 0 ? fname : name + "(" + i + ")" + type);
            exists = entries.contains(curName);
        }
        return curName;
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
