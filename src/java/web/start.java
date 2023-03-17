package web;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import api.sys.model.SysCfg;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@WebServlet(name = "start", urlPatterns = {"/start"})
public class start extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        try (Connection con = MySQLCommon.getConnection("sigmads", null)) {
            boolean zebra = pars.get("zebra") != null;
            boolean dp = pars.get("dp") != null;
            response.setContentType("text/html;charset=UTF-8");
            String appName = SysCfg.select(con).appName;
            try (PrintWriter out = response.getWriter()) {
                StringBuilder sb = new StringBuilder("<jnlp codebase=\"http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/\" spec=\"1.0+\">");
                sb.append("\n    <information>");
                sb.append("\n        <title>").append(appName).append("</title>");
                sb.append("\n        <vendor>Qualisys</vendor>");
                sb.append("\n        <homepage href=\"\"/>");
                sb.append("\n       <icon href=\"splash.png\" kind=\"splash\"/>");
                sb.append("\n        <description>").append(appName).append("</description>");
                sb.append("\n        <description kind=\"short\">").append(appName).append("</description>");
                sb.append("\n    </information>");
                sb.append("\n    <security>");
                sb.append("\n        <all-permissions/>");
                sb.append("\n    </security>");
                sb.append("\n    <resources>");
                sb.append("\n        <property name=\"jnlp.packEnabled\" value=\"true\"/>");
                sb.append("\n        <jar href=\"client.jar\" main=\"true\"/>");
                sb.append("\n        <j2se java-vm-args=\"-Xms128m -Xmx512m\" version=\"1.7+\"/>");
                File[] files = new File(getServletContext().getRealPath("/lib")).listFiles();
                for (File file : files) {
                    boolean add = true;
                    String fName = file.getName();
                    if (!fName.endsWith(".jar")) {
                        add = false;
                    } else if (fName.equals("mysql-connector-java-5.1.27-bin.jar") || fName.equals("mail.jar")) {
                        add = false;
                    } else if (!zebra && (fName.equals("ZSDK_API.jar") || fName.equals("jackson-core-2.2.3.jar") || fName.equals("jackson-databind-2.2.3.jar"))) {
                        add = false;
                    } else if (!dp && (fName.equals("dpfpenrollment.jar") || fName.equals("dpotapi.jar") || fName.equals("dpfpverification.jar") || fName.equals("dpotjni.jar"))) {
                        add = false;
                    }
                    if (add) {
                        sb.append("\n        <jar href=\"lib/").append(fName).append("\"/>");
                    }
                }
                sb.append("\n    </resources>");
                sb.append("\n    <application-desc main-class=\"forms.Main\">");
                sb.append("\n    <argument>http://").append(request.getServerName()).append(":").append(request.getServerPort()).append(request.getContextPath()).append("/</argument>");
                sb.append("\n    </application-desc>");
                sb.append("\n</jnlp>");
                String attachment = "inline; filename=\"" + appName.toLowerCase().replaceAll("[^a-z]", "") + ".jnlp\"";
                response.setContentType("application/x-java-jnlp-file");
                response.setHeader("Cache-Control", "max-age=30");
                response.setHeader("Content-disposition", attachment);
                out.write(sb.toString());
                out.flush();
            }
        } catch (Exception ex) {
            Logger.getLogger(start.class.getName()).log(Level.SEVERE, null, ex);
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
