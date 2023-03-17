package web.marketing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import model.system.SessionLogin;
import org.apache.commons.io.IOUtils;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "CgUnoSowClc", urlPatterns = {"/CgUnoSowClc"})
public class CgUnoSowClc extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {

        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            JsonObjectBuilder ob = Json.createObjectBuilder();

            InputStream data = request.getPart("data").getInputStream();
            String fileName = "SI" + new SimpleDateFormat("dd").format(new Date()) + ".TXT";

            String plainDir = geFilesSow();

            SessionLogin.validate(MySQLQuery.scape(request.getParameter("sessionId")));
            try {
                File file = new File(plainDir + fileName);
                try (FileOutputStream out = new FileOutputStream(file)) {
                    IOUtils.copy(data, out);
                }

                File f = new File(plainDir + fileName);
                if (f.exists()) {
                    ob.add("status", "OK");
                } else {
                    ob.add("status", "ERROR");
                }

            } catch (IOException ex) {
                System.out.println("cguno tag_read_file");
                Logger.getLogger(CgUnoSowClc.class.getName()).log(Level.SEVERE, null, ex);
                sendError(response, ex);
            } finally {
                w.writeObject(ob.build());
            }
        } catch (IOException | ServletException ex) {
            Logger.getLogger(CgUnoSowClc.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500, ex.getMessage());
        }

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(CgUnoSowClc.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(CgUnoSowClc.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "CgUnoSowClc servelet";
    }

    private void sendError(HttpServletResponse resp, Exception ex) throws IOException {
        resp.setStatus(500);
        if (ex.getMessage() != null) {
            resp.getOutputStream().write(ex.getMessage().getBytes("UTF8"));
        } else {
            resp.getOutputStream().write(ex.getClass().getName().getBytes("UTF8"));
        }
    }

    public static String geFilesSow() throws Exception {
        String home = File.separator + "home" + File.separator + "cguno" + File.separator + "siembra" + File.separator;
        File f = new File(home);
        if (!f.exists() && !f.mkdirs()) {
            throw new Exception("No se pudo crear la carpeta de logs.\n" + home);
        }
        return home;
    }

}
