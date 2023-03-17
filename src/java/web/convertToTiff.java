package web;

import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Connection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import service.MySQL.MySQLCommon;
import utilities.shrinkfiles.FileShrinker;
import utilities.IO;
import utilities.MySQLQuery;
import static utilities.shrinkfiles.FileShrinker.saveAsTIF;
import utilities.shrinkfiles.FileTypes;
import web.fileManager.PathInfo;

@MultipartConfig
@WebServlet(name = "convertToTiff", urlPatterns = {"/convertToTiff"})
public class convertToTiff extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        Connection con = null;
        try {
            con = MySQLCommon.getConnection(pars.get("poolName"), pars.get("tz"));
            String ownerType = pars.get("ownerType");
            String begDate = pars.get("begDate");
            String endDate = pars.get("endDate");
            PathInfo pInfo = new PathInfo(con);

            Object[][] objs = new MySQLQuery("SELECT b.id, "
                    + "b.file_name "
                    + "FROM bfile b "
                    + "WHERE b.owner_type = " + ownerType + " "
                    + "AND b.file_name LIKE '%pdf%' "
                    + "AND DATE(b.created) BETWEEN '" + begDate + "' AND '" + endDate + "'").getRecords(con);

            for (Object[] row : objs) {
                String fileName = MySQLQuery.getAsString(row[1]);

                File oldFile = new File(pInfo.path + row[0] + ".bin");
                File newFile = new File(pInfo.path + row[0] + "_pdf.bin");
                oldFile.renameTo(newFile);

                File tmp = File.createTempFile("uploaded", ".bin");

                BufferedImage img = FileShrinker.proccessImage(testPDFBoxExtractImages(pInfo.path + row[0] + "_pdf.bin"), FileShrinker.TYPE_TIFF);
                saveAsTIF(img, tmp);
                fileName = FileShrinker.changeFileType(fileName, ".tiff");

                File nFile = new File(pInfo.path + row[0] + ".bin");
                if (nFile.exists()) {
                    nFile.delete();
                }
                FileUtils.moveFile(tmp, nFile);

                new MySQLQuery("UPDATE bfile SET file_name = '" + fileName + "', size = " + tmp.length() + " WHERE id = " + row[0] + ";").executeUpdate(con);
                response.getOutputStream().write(String.valueOf("ok").getBytes());
            }

        } catch (Exception ex) {
            Logger.getLogger(convertToTiff.class.getName()).log(Level.SEVERE, null, ex);
            response.reset();
            if (ex.getMessage() != null) {
                response.sendError(500, ex.getMessage());
            } else {
                response.sendError(500);
            }
        } finally {
            MySQLCommon.closeConnection(con);
        }
    }

    public void FileCopy(String sourceFile, String destinationFile) throws Exception {
        File inFile = new File(sourceFile);
        File outFile = new File(destinationFile);

        FileInputStream in = new FileInputStream(inFile);
        FileOutputStream out = new FileOutputStream(outFile);

        int c;
        while ((c = in.read()) != -1) {
            out.write(c);
        }
        in.close();
        out.close();
    }

    public static BufferedImage testPDFBoxExtractImages(String path) throws Exception {
        File temp = new File(path);
        int type = FileTypes.getType(temp);
        switch (type) {
            case FileTypes.PDF: {
                PDDocument document = PDDocument.load(temp);
                PDPageTree list = document.getPages();
                for (PDPage page : list) {
                    PDResources pdResources = page.getResources();
                    for (COSName c : pdResources.getXObjectNames()) {
                        PDXObject o = pdResources.getXObject(c);
                        if (o instanceof org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject) {
                            return ((org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject) o).getImage();
                        }
                    }
                }
            }
            default: {
                return FileShrinker.proccessImage(ImageIO.read(temp), FileShrinker.TYPE_TIFF);
            }
        }
    }

    public static String convertStreamToString(InputStream is) throws IOException {
        return IO.convertStreamToString(is);
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
