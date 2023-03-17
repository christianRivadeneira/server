package web.maps;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.apache.jasper.xmlparser.UCSReader.DEFAULT_BUFFER_SIZE;
import utilities.MySQLQuery;

@WebServlet(name = "GetRouteKml", urlPatterns = {"/GetRouteKml"})
public class GetRouteKml extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        File f = null;
        try {
            String fileName = pars.get("fileName");
            f = new File("d:\\" + fileName);
            if (f.exists()) {
                response.setDateHeader("Last-Modified", new Date().getTime());
                response.setHeader("ETag", new Date().getTime() + "");
                response.setContentLength((int) f.length());
                response.setContentType("application/vnd.android.package-archive");
                response.setHeader("Content-Disposition", "attachment; filename=d:\\" + fileName);
                response.addCookie(new Cookie("dummy", "some_random_content"));

                try (ServletOutputStream os = response.getOutputStream(); FileInputStream fis = new FileInputStream(f); BufferedInputStream input = new BufferedInputStream(fis, DEFAULT_BUFFER_SIZE); BufferedOutputStream output = new BufferedOutputStream(os, DEFAULT_BUFFER_SIZE);) {
                    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                    int length;
                    try {
                        while ((length = input.read(buffer)) > 0) {
                            output.write(buffer, 0, length);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(GetRouteKml.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                throw new Exception("Archivo no encontrado " + f.getPath());
            }
        } catch (Exception ex) {
            Logger.getLogger(GetRouteKml.class.getName()).log(Level.SEVERE, null, ex);
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
