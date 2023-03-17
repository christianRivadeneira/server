package web.billing;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import model.system.SessionLogin;
import utilities.IO;
import utilities.MySQLQuery;

@WebServlet(name = "createBillingCity", urlPatterns = {"/createBillingCity"})
public class CreateBillingCity extends HttpServlet {

    /*
    * Líneas comentadas son para uso de pruebas en local;
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            
            response.setStatus(200);
            response.getWriter().write("Exito!!!");
        } catch (Exception ex) {
            sendError(response, ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

    public static String convertStreamToString(Part is) throws IOException {
        if (is == null) {
            return null;
        }
        return IO.convertStreamToString(is.getInputStream());
    }

    private void sendError(HttpServletResponse resp, Exception ex) throws IOException {
        Logger.getLogger(CreateBillingCity.class.getName()).log(Level.SEVERE, null, ex);
        resp.setStatus(500);
        if (ex.getMessage() != null) {
            resp.getOutputStream().write(ex.getMessage().getBytes("UTF8"));
        } else {
            resp.getOutputStream().write(ex.getClass().getName().getBytes("UTF8"));
        }
    }

    
}
