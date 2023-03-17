package web.discount;

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
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@WebServlet(name = "ExportNif", urlPatterns = {"/ExportNif"})
public class ExportNif extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        String begDate = pars.get("begDate");
        String endDate = pars.get("endDate");
        String sessionId = pars.get("sessionId");
        boolean showFact = pars.get("showFact").equals("1");
        boolean showTara = pars.get("showTara").equals("1");
        boolean showFab = pars.get("showFab").equals("1");
        boolean showNotes = pars.get("showNotes").equals("1");
        boolean showOrig = pars.get("showOrig").equals("1");
        boolean showBuy = pars.get("showBuy").equals("1");

        try (Connection con = MySQLCommon.getConnection("sigmads", null)) {
            SessionLogin.validate(sessionId, con);
            Object[][] data = new MySQLQuery("SELECT "
                    + "f.`name`, " //0
                    + "CAST(CONCAT(LPAD(nif_y, 2, '0'), '', LPAD(nif_f, fac_len, '0'), '', LPAD(nif_s, 6, '0')) AS CHAR), " //1
                    + "ct.kg," //2
                    + "IFNULL(tc.tara,0), " //3
                    + "DATE_FORMAT(tc.fab_date, '%d/%m/%Y'), " //4
                    + "IFNULL(tc.notes,'Sin Observ.'), " //5
                    + "IF(tc.imported,'Importado','App'), " //6
                    + "IFNULL(DATE_FORMAT(tc.buy_date, '%d/%m/%Y'), ''), " //7
                    + "IFNULL(FORMAT(tc.buy_price,0), 0) " //8
                    + "FROM trk_cyl tc "
                    + "INNER JOIN cylinder_type ct ON tc.cyl_type_id=ct.id "
                    + "INNER JOIN inv_factory AS f ON f.id = tc.factory_id "
                    + "WHERE "
                    + "tc.fab_date BETWEEN '" + begDate + "' AND '" + endDate + "'"
            ).getRecords(con);

            try (PrintWriter out = response.getWriter();) {
                response.setHeader("Content-Disposition", "attachment;filename=NIFS.csv");
                response.setContentType("text/plain;charset=ISO-8859-1");
                for (Object[] rs : data) {
                    out.write((showFact ? rs[0] + "," : "")
                            + rs[1]
                            + "," + rs[2]
                            + (showTara ? ("," + rs[3]) : "")
                            + (showFab ? ("," + rs[4]) : "")
                            + (showNotes ? ("," + rs[5]) : "")
                            + (showOrig ? ("," + rs[6]) : "")
                            + (showBuy ? ("," + rs[7] + "," + MySQLQuery.getAsString(rs[8]).replaceAll("[^0-9]", "")) : "")
                    );
                    out.println();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ExportNif.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(500);
            response.getWriter().write(ex.getMessage());
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

    @Override
    public String getServletInfo() {
        return "Export Nif";
    }
}
