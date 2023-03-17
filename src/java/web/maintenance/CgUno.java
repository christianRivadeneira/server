package web.maintenance;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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
@WebServlet(name = "CgUno", urlPatterns = {"/CgUno"})
public class CgUno extends HttpServlet {
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
        String shellDir = "/u/uno85c/montagas/";
        String plainDir = "/u/uno85c/montagas/plano/";
        int lastDigit = (Integer.parseInt(new SimpleDateFormat("yy").format(new Date()))) % 10;
        String fileName = lastDigit + new SimpleDateFormat("MMddHHmm").format(new Date()) + ".TXT";
        String fileNamePure = fileName.substring(0, fileName.length() - 4);

        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            JsonObjectBuilder ob = Json.createObjectBuilder();
            InputStream data = request.getPart("data").getInputStream();
            SessionLogin.validate(MySQLQuery.scape(request.getParameter("sessionId")));
            try {
                File file = new File(plainDir + fileName);
                try (FileOutputStream out = new FileOutputStream(file)) {
                    IOUtils.copy(data, out);
                }

                executeFBMMTO(fileNamePure, shellDir);

                File f = new File(shellDir + "prt/" + fileNamePure + ".120");
                if (f.exists()) {
                    byte[] b = Files.readAllBytes(f.toPath());
                    String dataFile = convertStreamToString(new ByteArrayInputStream(b));

                    ob.add("status", "OK");
                    ob.add("file", dataFile);
                } else {
                    ob.add("status", "ERROR");
                }

            } catch (IOException ex) {
                System.out.println("tag_fbatch_error1 " + ex);
                Logger.getLogger(CgUno.class.getName()).log(Level.SEVERE, null, ex);
                sendError(response, ex);
            } catch (Exception ex) {
                System.out.println("tag_fbatch_error2 " + ex);
                Logger.getLogger(CgUno.class.getName()).log(Level.SEVERE, null, ex);
                sendError(response, ex);
            } finally {
                w.writeObject(ob.build());
            }
        } catch (IOException | ServletException ex) {
            System.out.println("tag_fbatch_error3 " + ex);
            Logger.getLogger(CgUno.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500, ex.getMessage());
        }

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(CgUno.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(CgUno.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "CgUno servelet";
    }

    private void sendError(HttpServletResponse resp, Exception ex) throws IOException {
        resp.setStatus(500);
        if (ex.getMessage() != null) {
            resp.getOutputStream().write(ex.getMessage().getBytes("UTF8"));
        } else {
            resp.getOutputStream().write(ex.getClass().getName().getBytes("UTF8"));
        }
    }

    private void readFile(File file) throws FileNotFoundException, IOException {
        String cadena;
        FileReader f = new FileReader(file);
        BufferedReader b = new BufferedReader(f);
        while ((cadena = b.readLine()) != null) {
            System.out.println(cadena);
        }
        b.close();
    }

    private void executeFBMMTO(String fileName, String shellDir) throws FileNotFoundException, IOException {
        try {
            List<String> coms = new ArrayList<>();
            coms.add("/bin/sh");
            coms.add("-c");
            coms.add(shellDir + "FBMMTO-SSH '" + fileName + "'");
            ProcessBuilder pb = new ProcessBuilder(coms);
            pb.directory(new File(shellDir));
            Map<String, String> env = pb.environment();
            env.put("LD_LIBRARY_PATH", "/usr/local/openlink/lib:/u/oracle/11.2//lib:");
            Process proc = pb.start();
            proc.waitFor(20, TimeUnit.SECONDS);

            System.out.println("-------FIBATCH_TAG" + "\n"
                    + "Error[ " + convertStreamToString(proc.getErrorStream()) + " ] " + "\n"
                    + "Proceso[ " + convertStreamToString(proc.getInputStream()) + " ] " + "\n"
                    + "-------------------FIN FIBATCH_TAG EXIT VALUE " + proc.exitValue()
            );

        } catch (InterruptedException ex) {
            System.out.println("-------FIBATCH_TAG EXCEPTION");
            Logger.getLogger(CgUno.class.getName()).log(Level.SEVERE, null, ex);
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
}
