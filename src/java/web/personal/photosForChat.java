package web.personal;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import service.MySQL.MySQLSelect;
import utilities.MySQLQuery;
import web.fileManager;
import web.fileManager.PathInfo;

@WebServlet(name = "PhotosForChat", urlPatterns = {"/PhotosForChat"})
public class photosForChat extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Connection conn = null;
        try {
            Map<String, String> req = MySQLQuery.scapedParams(request);
            String poolName = req.get("poolName");
            String tz = req.get("tz");
            poolName = (poolName != null ? poolName : "sigmads");
            response.setContentType("application/gzip");
            conn = MySQLCommon.getConnection(poolName, tz);

            //EMPLOYEE_PHOTO = 10
            Object[][] users = new MySQLQuery("SELECT e.id, bfile.id "
                    + "FROM "
                    + "employee AS e "
                    + "INNER JOIN bfile ON e.per_employee_id = bfile.owner_id "
                    + "WHERE e.login IS NOT NULL AND e.active = 1 AND bfile.owner_type = 10 group by e.id").getRecords(conn);

            PathInfo pathInfo = new fileManager.PathInfo(conn);

            try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
                for (Object[] userRow : users) {
                    File file = pathInfo.getExistingFile(MySQLQuery.getAsInteger(userRow[1]));
                    if (file != null && file.exists()) {
                        zos.putNextEntry(new ZipEntry(userRow[0].toString()));
                        processPhoto(file, zos);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(photosForChat.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500, ex.getMessage());
        } finally {
            MySQLSelect.tryClose(conn);
        }
    }

    private void processPhoto(File f, OutputStream oos) throws Exception {
        BufferedImage resizedImage = new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setColor(Color.white);
        g.fillRect(0, 0, 40, 40);
        BufferedImage imgOrig = ImageIO.read(f);
        int newWidth;
        int newHeight;
        if (imgOrig.getWidth() > imgOrig.getHeight()) {
            newHeight = 40;
            newWidth = (int) ((40d / imgOrig.getHeight()) * imgOrig.getWidth());
        } else {
            newWidth = 40;
            newHeight = (int) ((40d / imgOrig.getWidth()) * imgOrig.getHeight());
        }
        g.drawImage(imgOrig.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), (int) ((40d - newWidth) / 2d), (int) ((40d - newHeight) / 2d), null);
        g.dispose();

        ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
        jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpgWriteParam.setCompressionQuality(0.9f);

        MemoryCacheImageOutputStream baos = new MemoryCacheImageOutputStream(oos);
        jpgWriter.setOutput(baos);
        IIOImage outputImage = new IIOImage(resizedImage, null, null);
        jpgWriter.write(null, outputImage, jpgWriteParam);
        jpgWriter.dispose();
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
