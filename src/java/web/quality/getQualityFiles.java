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

@WebServlet(name = "getQualityFiles", urlPatterns = {"/getQualityFiles"})
public class getQualityFiles extends HttpServlet {

    public static final int QUAL_RW = 12;
    public static final int QUAL_RO = 13;
    public static final int CAL_RECORDS = 42;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        try (Connection con = MySQLCommon.getConnection(pars.get("poolName"), pars.get("tz")); ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            SessionLogin.validate(pars.get("sessionId"), con);

            fileManager.PathInfo pi = new fileManager.PathInfo(con);
            List<String> entries = new ArrayList<>();

            Object[][] procData = new MySQLQuery("SELECT id, name FROM cal_proc WHERE active = 1 ORDER BY name").getRecords(con);

            for (Object[] procRow : procData) {
                int prodId = MySQLQuery.getAsInteger(procRow[0]);
                String procName = MySQLQuery.getAsString(procRow[1]);

                Object[][] docTypeData = new MySQLQuery("SELECT id, plural FROM cal_doc_type WHERE proc_id IS NULL OR proc_id = " + prodId + " ORDER BY plural").getRecords(con);

                for (Object[] docTypeRow : docTypeData) {
                    int docTypeId = MySQLQuery.getAsInteger(docTypeRow[0]);
                    String docTypeName = MySQLQuery.getAsString(docTypeRow[1]);

                    Object[][] docsData = new MySQLQuery("SELECT id, name FROM cal_node WHERE proc_id = " + prodId + " AND doc_id = " + docTypeId).getRecords(con);
                    for (Object[] docsRow : docsData) {
                        int docId = MySQLQuery.getAsInteger(docsRow[0]);
                        String docName = MySQLQuery.getAsString(docsRow[1]);

                        Object[][] versData = new MySQLQuery("SELECT id, num, obsolete FROM cal_version WHERE node_id = " + docId).getRecords(con);
                        boolean versVig = false;
                        for (Object[] row : versData) {
                            boolean obsolete = MySQLQuery.getAsBoolean(row[2]);
                            if (!obsolete) {
                                versVig = true;
                            }
                        }

                        for (Object[] versRow : versData) {
                            int verId = MySQLQuery.getAsInteger(versRow[0]);
                            int verNum = MySQLQuery.getAsInteger(versRow[1]);

                            Object[][] versBFileData = new MySQLQuery("SELECT id, file_name FROM bfile WHERE owner_id = " + verId + " AND owner_type IN (" + QUAL_RO + ", " + QUAL_RW + ")").getRecords(con);

                            for (Object[] versBFileRow : versBFileData) {
                                int verBFileId = MySQLQuery.getAsInteger(versBFileRow[0]);
                                String verBFileName = MySQLQuery.getAsString(versBFileRow[1]);
                                File f = pi.getExistingFile(verBFileId);
                                if (f != null && f.exists()) {
                                    String path;

                                    if (versVig) {
                                        path = procName + "/" + docTypeName + "/" + docName + "/Version " + verNum + "" + getFileExt(verBFileName);
                                    } else {
                                        path = procName + "/" + docTypeName + "/Obsoleta/" + docName + "/Version " + verNum + "" + getFileExt(verBFileName);
                                    }

                                    ZipEntry ze = new ZipEntry(path);
                                    if (!entries.contains(ze.getName())) {
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
                        //buscar registros
                        Object[][] regsData = new MySQLQuery("SELECT b.id, b.file_name FROM cal_record r INNER JOIN bfile b ON r.id = b.owner_id AND b.owner_type = " + CAL_RECORDS + " WHERE r.node_id = " + docId + " ORDER BY rec_date").getRecords(con);

                        for (int i = 0; i < regsData.length; i++) {
                            int regBFileId = MySQLQuery.getAsInteger(regsData[i][0]);
                            String regBFileName = MySQLQuery.getAsString(regsData[i][1]);
                            File f = pi.getExistingFile(regBFileId);
                            if (f != null && f.exists()) {
                                ZipEntry ze = new ZipEntry(procName + "/" + docTypeName + "/" + docName + "/Registros/" + (i + 1) + getFileExt(regBFileName));
                                if (!entries.contains(ze.getName())) {
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
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(getQualityFiles.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500, ex.getMessage());
        }
    }

    public static String getFileExt(String fname) {
        int dot = fname.lastIndexOf(".");
        return (dot >= 0 ? fname.substring(dot, fname.length()) : "");
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
