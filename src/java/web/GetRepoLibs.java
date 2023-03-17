package web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utilities.MySQLQuery;

@WebServlet(urlPatterns = {"/GetRepoLibs"})
public class GetRepoLibs extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            String path = pars.get("repoPath");
            if (path == null || path.isEmpty()) {
                path = getServletContext().getRealPath("/");
            }
            String libPath = path + "lib";

            FilenameFilter filter = new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jar") || name.endsWith(".jar.pack.gz");
                }
            };

            Comparator comp = new Comparator<File>() {

                @Override
                public int compare(File o1, File o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            };

            List<File> execs = Arrays.asList(new File(path).listFiles(filter));
            Collections.sort(execs, comp);
            List<File> libs = Arrays.asList(new File(libPath).listFiles(filter));
            Collections.sort(libs, comp);

            for (File file : execs) {
                writeFile(out, file);
            }
            out.write("libs\n");
            for (File file : libs) {
                writeFile(out, file);
            }

        } catch (Exception ex) {
            Logger.getLogger(GetRepoLibs.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void writeFile(PrintWriter out, File file) throws Exception {
        out.write(file.getName());
        out.write("\t");
        out.write(file.length() + "");
        out.write("\t");
        out.write(getMD5Checksum(file));
        out.write("\n");
    }

    private static byte[] createChecksum(File f) throws Exception {
        MessageDigest complete;
        try (InputStream fis = new FileInputStream(f)) {
            byte[] buffer = new byte[1024];
            complete = MessageDigest.getInstance("MD5");
            int numRead;
            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);
        }
        return complete.digest();
    }

    public static String getMD5Checksum(File f) throws Exception {
        byte[] b = createChecksum(f);
        String result = "";

        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
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
