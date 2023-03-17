package web.tanks;

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
import utilities.MySQLQuery;
import web.enterpriseLogo;
import web.fileManager.PathInfo;

@WebServlet(name = "RequestPhoto", urlPatterns = {"/RequestPhoto"})

public class RequestPhoto extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        String poolName = pars.get("poolName");
        String tz = pars.get("tz");
        poolName = poolName != null ? poolName : "sigmads";
        try (Connection con = MySQLCommon.getConnection(poolName, tz);) {
            PathInfo pi = new PathInfo(con);
            Integer fileId;
            if (pars.get("id") != null) {
                fileId = Integer.valueOf(pars.get("id"));
            } else {
                fileId = new MySQLQuery("SELECT "
                        + "id "
                        + "FROM bfile "
                        + "WHERE owner_id = " + pars.get("ownerId") + " "
                        + "AND owner_type = " + pars.get("ownerType") + " LIMIT 1").getAsInteger(con);
            }
            if (fileId != null) {
                File f = pi.getExistingFile(fileId);
                if (f.exists()) {
                    processPhoto(220, 220, f, response.getOutputStream());//220
                } else {
                    //png transparente de 1x1, para las pruebas en local
                    enterpriseLogo.writeEmptyImage(response.getOutputStream());
                }
            }
        } catch (Exception e) {
            Logger.getLogger(RequestPhoto.class.getName()).log(Level.SEVERE, null, e);
            response.sendError(500, e.getMessage());
        }
    }

    private void processPhoto(int width, int height, File f, OutputStream oos) throws Exception {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        BufferedImage imgOrig = ImageIO.read(f);
        int newWidth;
        int newHeight;
        if (imgOrig.getWidth() > imgOrig.getHeight()) {
            newHeight = height;
            newWidth = (int) (((double) height / imgOrig.getHeight()) * imgOrig.getWidth());

        } else {

            newWidth = width;
            newHeight = (int) (((double) width / imgOrig.getWidth()) * imgOrig.getHeight());

        }
        g.drawImage(imgOrig.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), (int) ((width - newWidth) / 2d), (int) ((height - newHeight) / 2d), null);
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
        return "Short description";
    }
}
