package web.gates;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "uploadPrinterPing", urlPatterns = {"/uploadPrinterPing"})
public class uploadPrinterPing extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        OutputStream os = response.getOutputStream();

        try (GZIPOutputStream zos = new GZIPOutputStream(os); ObjectOutputStream oos = new ObjectOutputStream(zos);) {
            try (Connection con = MySQLCommon.getConnection("sigmads", null); Statement st = con.createStatement();) {
                Map<String, String> req = MySQLQuery.scapedParams(request);
                String pingString = MySQLQuery.getAsString(req.get("pingString"));
                st.executeUpdate("DELETE FROM gt_printer_ping WHERE location = '" + pingString + "'");
                st.executeUpdate("INSERT INTO gt_printer_ping SET location = '" + pingString + "', dt = NOW()");
                oos.writeUTF("ok");
            } catch (Exception ex) {
                oos.writeUTF("error");
                Logger.getLogger(uploadPrinterPing.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (Exception ex) {
            Logger.getLogger(uploadPrinterPing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

}
