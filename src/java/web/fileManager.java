package web;

import api.Params;
import api.sys.model.Bfile;
import java.io.*;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
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
import utilities.IO;
import utilities.MySQLQuery;
import utilities.shrinkfiles.FileShrinker;
import utilities.shrinkfiles.ShrunkenFile;

@MultipartConfig
@WebServlet(name = "fileManager", urlPatterns = {"/fileManager"})
public class fileManager extends HttpServlet {

    public static class PathInfo {

        public String path;
        public String extractCmd;
        private final String[] allowedTypes;
        public int maxFileSizeKb;

        public PathInfo(Connection con) throws Exception {
            Object[] cfgRow = new MySQLQuery("SELECT files_path, allowed_files, extract_cmd, max_file_size_kb FROM sys_cfg").getRecord(con);
            path = MySQLQuery.getAsString(cfgRow[0]);
            //path = "C:\\bdata";
            //System.err.println("********************" + path);

            if (!path.endsWith(File.separator)) {
                path = path + File.separator;
            }
            allowedTypes = MySQLQuery.getAsString(cfgRow[1]).split(",");
            extractCmd = MySQLQuery.getAsString(cfgRow[2]);
            maxFileSizeKb = MySQLQuery.getAsInteger(cfgRow[3]);
            checkDataDir(path);
        }

        public boolean isAllowed(String fileName) {
            for (String type : allowedTypes) {
                if (fileName.toLowerCase().endsWith("." + type.trim())) {
                    return true;
                }
            }
            return false;
        }

        public static void checkDataDir(String path) throws Exception {
            File dir = new File(path);
            if (!dir.exists()) {
                
                if (!dir.mkdir()) {
                    throw new IOException("Unable to locate data dir");
                }
            }
        }

        private File getFile(int id, boolean newFormat) throws Exception {
            if (newFormat) {
                int fname = (int) Math.floor(id / 10000d);
                File folder = new File(path + fname + File.separator);
                if (!folder.exists()) {
                    if (!folder.mkdirs()) {
                        throw new Exception("No se pudo crear el directorio: " + folder.getAbsolutePath());
                    }
                }
                return new File(path + fname + File.separator + id + ".bin");
            } else {
                return new File(path + id + ".bin");
            }
        }

        public File getExistingFile(int id) throws Exception {
            File nf = getFile(id, true);
            if (nf.exists()) {
                return nf;
            } else {
                File of = getFile(id, false);
                if (of.exists()) {
                    return of;
                }
            }
            return null;
        }

        public File getNewFile(int id) throws Exception {
            return getFile(id, true);
        }

    }

    public static Bfile upload(int employeeId, int ownerId, Integer ownerType, String fileName, String desc, Boolean unique, Integer shrinkType, PathInfo pInfo, File tmp, Connection conn) throws Exception {
        return upload(employeeId, ownerId, ownerType, null, fileName, desc, unique, null, shrinkType, pInfo, tmp, conn);
    }

    public static Bfile upload(int employeeId, int ownerId, Integer ownerType, String tableName, String fileName, String desc, Boolean unique, Integer shrinkType, PathInfo pInfo, File tmp, Connection conn) throws Exception {
        return upload(employeeId, ownerId, ownerType, tableName, fileName, desc, unique, null, shrinkType, pInfo, tmp, conn);
    }

    public static Bfile upload(int employeeId, int ownerId, Integer ownerType, String tableName, String fileName, String desc, Boolean unique, Boolean override, Integer shrinkType, PathInfo pInfo, File tmp, Connection conn) throws Exception {
        if (ownerType == null && tableName == null) {
            throw new Exception("debe indicar owner_type o table_name");
        } else if (ownerType != null && tableName != null) {
            if (ownerType == 0) {
                ownerType = null;
            } else {
                throw new Exception("debe unicamente owner_type o table_name no ambos");
            }
        }

        if (shrinkType == null) {
            shrinkType = FileShrinker.TYPE_NONE;
        }

        if (unique == null) {
            unique = false;
        }

        if (override == null) {
            override = false;
        }

        if (desc == null) {
            desc = fileName;
        }

        //validando el tipo de archivo
        if (!pInfo.isAllowed(fileName)) {
            throw new Exception("El tipo de archivo no está permitido.");
        }

        Bfile oldBfile = null;

        //fin validación de tipo de archivo
        if (unique) {// lo uso en donde unicamente se debe insertar un archivo por type ej documentos
            if (MySQLQuery.isEmpty(tableName)) {
                if (new MySQLQuery("SELECT count(*) > 0 FROM bfile WHERE owner_type=" + ownerType + " AND owner_id =" + ownerId + " ").getAsBoolean(conn)) {
                    throw new Exception("Error, intenta subir un archivo que ya existe.");
                }
            } else {
                oldBfile = new Bfile().select(
                        new Params("table", tableName).param("owner_id", ownerId), conn);
                if (!override && oldBfile != null) {
                    throw new Exception("Error, intenta subir un archivo que ya existe.");
                }
            }

        }

        boolean shrunken = false;
        if (shrinkType != FileShrinker.TYPE_NONE) {
            ShrunkenFile sf = FileShrinker.shrinkFile(tmp, fileName, shrinkType);
            //if (sf.shrunken && (sf.f != null && (sf.f.length() > 0 && sf.f.length() < tmp.length()))) {
            if (sf.shrunken && (sf.f != null && (sf.f.length() > 0))) {
                FileUtils.forceDelete(tmp);
                tmp = sf.f;
                if (desc.equals(fileName)) {
                    desc = sf.fileName;
                }
                fileName = sf.fileName;
                shrunken = true;
            }
        }

        if (override && oldBfile != null) {
            Bfile.delete(oldBfile.id, conn);
        }

        Bfile f = new Bfile();
        f.fileName = fileName;
        f.description = (desc == null ? f.fileName : desc);
        f.ownerId = ownerId;
        f.ownerType = ownerType;
        f.createdBy = employeeId;
        f.updatedBy = employeeId;
        f.size = (int) tmp.length();
        f.created = new Date();
        f.updated = new Date();
        f.shrunken = shrunken;
        f.table = tableName;
        f.insert(conn);

        File nFile = pInfo.getNewFile(f.id);
        FileUtils.moveFile(tmp, nFile);
        setWords(nFile, f.id, fileName, conn, pInfo.extractCmd);
        return f;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        Connection con = null;
        try {
            String poolName = pars.get("poolName");
            String tz = pars.get("tz");
            con = MySQLCommon.getConnection(poolName, tz);
            PathInfo pInfo = new PathInfo(con);

            String operation = pars.get("operation");
            if (operation.equals("0")) {
                int employeeId = Integer.parseInt(pars.get("employee"));
                int ownerId = Integer.parseInt(pars.get("ownerId"));
                String table = null;
                Integer ownerType = null;
                if (pars.containsKey("table")) {
                    table = pars.get("table");
                } else if (pars.containsKey("ownerType")) {
                    ownerType = Integer.parseInt(pars.get("ownerType"));;
                } else {
                    throw new Exception("Debe indicar table u ownerType");
                }
                String fileName = pars.get("fileName").trim().toLowerCase();
                String desc = pars.get("desc");
                Boolean unique = null;
                if (pars.containsKey("unique")) {
                    unique = pars.get("unique").equals("1");
                }

                Integer shrinkType = null;
                if (pars.containsKey("shrinkType")) {
                    shrinkType = Integer.valueOf(pars.get("shrinkType"));
                }
                InputStream in = request.getPart("file").getInputStream();
                File tmp = File.createTempFile("uploaded", ".bin");
                copy(in, new BufferedOutputStream(new FileOutputStream(tmp)));
                int id = upload(employeeId, ownerId, ownerType, table, fileName, desc, unique, shrinkType, pInfo, tmp, con).id;
                response.getOutputStream().write(String.valueOf(id).getBytes());
            } else if (operation.equals("1")) {
                //leer                
                int id = Integer.parseInt(pars.get("id"));
                File f = pInfo.getExistingFile(id);
                if (f == null) {
                    throw new Exception("El archivo no exíste " + id);
                }
                response.setContentLength((int) f.length());
                FileInputStream fis = new FileInputStream(f);
                copy(fis, response.getOutputStream());

            } else if (operation.equals("2")) {
                //actualizar
                File tmp = File.createTempFile("uploaded", ".bin");
                copy(request.getPart("file").getInputStream(), new BufferedOutputStream(new FileOutputStream(tmp)));

                int id = Integer.parseInt(pars.get("id"));
                int employeeId = Integer.parseInt(pars.get("employee"));

                String fileName = new MySQLQuery("SELECT file_name FROM bfile WHERE id = " + id).getAsString(con);

                new MySQLQuery("UPDATE bfile SET "
                        + "updated_by = " + employeeId + ", "
                        + "updated = NOW(), "
                        + "size = " + tmp.length() + " "
                        + "WHERE id = " + id).executeUpdate(con);

                File f = pInfo.getExistingFile(id);
                if (f != null) {
                    f.delete();
                }
                File file = pInfo.getFile(id, true);
                FileUtils.moveFile(tmp, file);
                setWords(file, id, fileName, con, pInfo.extractCmd);
                response.getOutputStream().write(String.valueOf(id).getBytes());
            } else if (operation.equals("3")) {
                //eliminar
                int id = Integer.parseInt(pars.get("id"));
                File file = pInfo.getExistingFile(id);
                if (file != null && !file.delete()) {
                    throw new IOException("El archivo no pudo ser eliminado.");
                }
                new MySQLQuery("DELETE FROM bfile WHERE id = " + id).executeUpdate(con);
            } else if (operation.equals("5")) {
                //copia de archivos
                int sourceId = Integer.parseInt(pars.get("sourceId"));
                int destId = Integer.parseInt(pars.get("destId"));
                //nombre del archivo original.
                String origName = new MySQLQuery("SELECT file_name FROM bfile WHERE id = " + sourceId).getAsString(con);
                new MySQLQuery("UPDATE bfile SET file_name = \"" + origName + "\"  WHERE id = " + destId).executeUpdate(con);
                File src = pInfo.getExistingFile(sourceId);
                if (!src.exists()) {
                    throw new Exception("El archivo de origen no exíste");
                }
                File dest = pInfo.getNewFile(destId);
                if (dest.exists() && !dest.delete()) {
                    throw new Exception("No se pudo borrar el archivo de destino.");
                }
                copy(new FileInputStream(src), new FileOutputStream(dest));
                setWords(dest, destId, origName, con, pInfo.extractCmd);
                new MySQLQuery("UPDATE bfile SET size = " + dest.length() + " WHERE id = " + destId).executeUpdate(con);
                response.getOutputStream().write("OK".getBytes());
            } else if (operation.equals("6")) {
                //keywords
                new MySQLQuery("UPDATE bfile SET keywords = ''").executeUpdate(con);
                Object[][] data = new MySQLQuery("SELECT id, file_name FROM bfile").getRecords(con);
                for (Object[] row : data) {
                    int id = MySQLQuery.getAsInteger(row[0]);
                    String fName = MySQLQuery.getAsString(row[1]);
                    File f = pInfo.getExistingFile(id);
                    setWords(f, id, fName, con, pInfo.extractCmd);

                }
            } else if (operation.equals("666")) {
                long l = System.currentTimeMillis();
                File[] files = new File(pInfo.path).listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".bin");
                    }
                });

                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    String sid = file.getName().replaceAll(".bin", "");
                    Integer id = Integer.valueOf(sid);
                    File nf = pInfo.getFile(id, true);
                    file.renameTo(nf);
                }
                System.out.println(System.currentTimeMillis() - l);
            }
        } catch (Exception ex) {
            Logger.getLogger(fileManager.class.getName()).log(Level.SEVERE, null, ex);
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

    static List<String> ban = null;

    public static void setWords(File f, int id, String name, Connection con, String command) throws Exception {

        if ((command != null && !command.trim().isEmpty()) && (f.exists() && f.isFile())) {
            try {
                InputStream inputStream = null;
                ByteArrayOutputStream baos = null;
                try {
                    inputStream = Runtime.getRuntime().exec(command + " " + f.getAbsolutePath() + " " + name.replaceAll(" ", "_")).getInputStream();
                    baos = new ByteArrayOutputStream();
                    copy(inputStream, baos);
                    String words = new String(baos.toByteArray());
                    setKeyWords(words, id, con);
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException ex) {
                        }
                    }
                    if (baos != null) {
                        try {
                            baos.close();
                        } catch (IOException ex) {
                        }
                    }
                }
            } catch (IOException ex) {
                setKeyWords(ex.getClass().toString(), id, con);
                throw ex;
            }
        } else {
            setKeyWords("", id, con);
        }
    }

    private static void setKeyWords(String keyWords, int id, Connection con) throws Exception {
        new MySQLQuery("UPDATE bfile SET keywords = '" + keyWords + "' WHERE id = " + id).executeUpdate(con);
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        return copy(input, output, true, true);
    }

    public static int copy(InputStream input, OutputStream output, boolean closeInput, boolean closeOutput) throws IOException {
        byte[] buffer = new byte[4096];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        if (closeInput) {
            input.close();
        }
        if (closeOutput) {
            output.close();
        }
        return count;
    }

    public static String convertStreamToString(InputStream is) throws IOException {
        return IO.convertStreamToString(is);
    }

    public static void copyFile(PathInfo pInfo, int sourceFileId, int destFileId) throws Exception {
        File src = pInfo.getExistingFile(sourceFileId);
        if (!src.exists()) {
            throw new Exception("El archivo de origen no exíste");
        }
        File dest = pInfo.getNewFile(destFileId);
        if (dest.exists() && !dest.delete()) {
            throw new Exception("No se pudo borrar el archivo de destino.");
        }
        copy(new FileInputStream(src), new FileOutputStream(dest));

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
