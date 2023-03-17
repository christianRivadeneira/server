package web.billing;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.DBSettings;
import utilities.DesEncrypter;
import utilities.MySQLQuery;
import web.fileManager;

@MultipartConfig
@WebServlet(name = "restoreBillingBackup", urlPatterns = {"/restoreBillingBackup"})
public class restoreBillingBackup extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        Map<String, String> pars = MySQLQuery.scapedParams(request);

        try (PrintWriter out = response.getWriter(); Connection conn = MySQLCommon.getConnection("sigmads", null)) {
            SessionLogin.validate(pars.get("sessionId"), conn);
            try {
                FileInputStream fis;
                FileOutputStream fos;

                //copiando el archivo adjunto a disco duro. 
                //Entra el stream de la solicitud, sale el archivo tmp
                File encFile = File.createTempFile("billingBk", ".tmp");
                if (encFile.exists()) {
                    if (!encFile.delete()) {
                        throw new Exception("No se puede crear el archivo.");
                    }
                }

                fos = new FileOutputStream(encFile);
                fileManager.copy(request.getPart("file").getInputStream(), new BufferedOutputStream(fos));
                fos.close();

                //desencriptando el archivo
                //entra el .tmp y sale en .zip, se borra el .tmp
                DesEncrypter des = new DesEncrypter("YVD2gYRWXMJZ");
                File zFile = File.createTempFile("billingZip", ".zip");
                fos = new FileOutputStream(zFile);
                fis = new FileInputStream(encFile);
                des.decrypt(fis, fos);
                fis.close();
                fos.close();
                encFile.delete();

                //extrayendo el contenido del .zip
                File sqlFile = null;
                String dbName = null;
                fis = new FileInputStream(zFile);
                try (ZipInputStream zis = new ZipInputStream(fis)) {
                    ZipEntry ze = zis.getNextEntry();
                    if (ze != null) {
                        dbName = ze.getName();
                        sqlFile = File.createTempFile(ze.getName(), ".sql");
                        fos = new FileOutputStream(sqlFile);
                        fileManager.copy(zis, fos);
                        fos.close();
                    }
                }
                zFile.delete();

                //ejecutando el sql   
                if (sqlFile != null) {
                    try {
                        out.write("TRATANDO DE EJECUTAR\n");
                        executeSQLFile(sqlFile, dbName);
                        out.write("FIN EJECUTAR\n");
                    } finally {
                        sqlFile.delete();
                    }
                } else {
                    throw new Exception("El backup no tenía archivos.");
                }
                out.write("\nOK");
            } catch (Exception ex) {
                Logger.getLogger(restoreBillingBackup.class.getName()).log(Level.SEVERE, null, ex);
                response.reset();
                response.sendError(500, ex.getMessage());
            }
        } catch (Exception ex) {
            Logger.getLogger(restoreBillingBackup.class.getName()).log(Level.SEVERE, null, ex);
            response.getOutputStream().write(ex.getMessage().getBytes());
            //response.sendError(500, ex.getMessage());
        }
    }

    public static int executeSQLFile(File file, String dbPar) throws Exception {
        Connection con = null;
        try {
            con = MySQLCommon.getConnection("sigmads", null);
            DBSettings db = new DBSettings(con);
            Process run = Runtime.getRuntime().exec(String.format("mysql -u%s -p%s -h%s -P%s %s", db.user, db.pass, db.host, db.port, dbPar));
            try (FileInputStream fis = new FileInputStream(file); OutputStream oos = run.getOutputStream()) {
                fileManager.copy(fis, oos);
            }
            run.waitFor();
            file.delete();
            return run.exitValue();
        } finally {
            MySQLCommon.closeConnection(con);
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
