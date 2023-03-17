package web.launcher;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utilities.MySQLQuery;

@WebServlet(urlPatterns = {"/launcher/generate"})
public class generate extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Content-Description", "File Transfer");
        response.setHeader("Content-Type", "application/x-jar");
        response.setHeader("Content-Disposition", "attachment; filename=Qualisys.jar");
        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            try (ZipInputStream zis = new ZipInputStream(generate.class.getResourceAsStream("/web/launcher/QualisysLauncher.jar"))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    zos.putNextEntry(entry);
                    int len;
                    byte[] buffer = new byte[2048];
                    while ((len = zis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                }
            }

            Properties properties = new Properties();
            Map<String, String> pars = MySQLQuery.scapedParams(request);

            String name = pars.get("name");
            String poolName = pars.get("poolName");
            String smode = pars.get("smode");
            String tz = pars.get("tz");
            String repoPath = pars.get("repoPath");
            String serverAddr = "http://" + request.getServerName() + ":" + request.getServerPort();
            String sign = pars.get("sign");

            name = name == null ? "sigma" : name;
            poolName = poolName == null ? "sigmads" : poolName;
            smode = smode == null ? "GF" : smode;
            tz = tz == null ? "GMT-5:00" : tz;
            sign = sign == null ? "0" : sign;

            properties.setProperty("address", serverAddr);
            properties.setProperty("name", name);
            properties.setProperty("poolName", poolName);
            properties.setProperty("smode", smode);
            properties.setProperty("tz", tz);
            properties.setProperty("repoPath", repoPath);
            properties.setProperty("sign", sign);

            File file = new File("parameters");
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                properties.store(fileOut, "qualisys");
            } catch (IOException ex) {
                throw new IOException(ex);
            }

            byte[] b = Files.readAllBytes(file.toPath());
            String dataFile = convertStreamToString(new ByteArrayInputStream(b));

            zos.putNextEntry(new ZipEntry("parameters"));
            zos.write(dataFile.getBytes());
        }
    }

    public String convertStreamToString(InputStream is) throws IOException {
        if (is != null) {
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
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
