package web.marketing;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import web.discount.ExportNif;

@MultipartConfig
@WebServlet(name = "getE3ReportCsv", urlPatterns = {"/getE3ReportCsv"})
public class GetE3Report extends HttpServlet {

    /**
     * Se hace por JDBC por optimización, no usar MySQLQuery, en este caso gasta
     * mucha memoria
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = null;
        Statement st = null;
        try (Connection con = MySQLCommon.getConnection("sigmads", null)) {
            JsonObject req = MySQLQuery.scapeJsonObj(request);
            String begin = req.getString("begin");
            String end = req.getString("end");
            String sessionId = req.getString("sessionId");
            SessionLogin.validate(sessionId, con);

            response.setHeader("Content-Disposition", "attachment;filename=etres.csv");
            response.setContentType("text/plain;charset=ISO-8859-1");
            out = response.getWriter();
            out.write("Ventas " + new MySQLQuery("SELECT `name` FROM enterprise WHERE !alternative").getAsString(con) + " Desde: " + begin + "  a " + end + System.lineSeparator());
            out.write("" + System.lineSeparator());
            out.write("NIE,NIF,Centro Poblado,Sector,Zona,Medio,Códgo de Presentación,Número de Factura,Valor Factura,Cantidad(Kg),Subsidio" + System.lineSeparator());

            /**
             * Se hace por JDBC por optimización, no usar MySQLQuery, en este
             * caso gasta mucha memoria
             */
            String str = "SELECT "
                    + "(SELECT nie FROM inv_center c WHERE lat IS NOT NULL ORDER BY (POW(c.lat - (ts.lat),2) + POW(c.lon - (ts.lon ),2)) ASC LIMIT 1), "
                    + "CONCAT(CAST(LPAD(ts.cube_nif_y, 2, 0) AS CHAR), CAST(LPAD(ts.cube_nif_f, 4, 0) AS CHAR), CAST(LPAD(ts.cube_nif_s, 6, 0) AS CHAR)), "
                    + "CAST(dp.code AS CHAR), "
                    + "CONCAT('E', CAST(ts.stratum AS CHAR)), "
                    + "ts.`zone`, "
                    + "IF(e.store_id IS NOT NULL, 'E', 'VRC'), "
                    + "CONCAT('CIL', ct.name), "
                    + "ts.bill, "
                    + "CAST(ts.price AS CHAR), "
                    + "ct.kg, "
                    + "CAST(IFNULL(ts.subsidy, 0) as char) "
                    + "FROM trk_sale ts "
                    + "INNER JOIN dane_poblado dp ON ts.dane_pob_id = dp.id "
                    + "INNER JOIN employee e ON ts.emp_id = e.id "
                    + "INNER JOIN cylinder_type ct ON ts.cube_cyl_type_id = ct.id "
                    + "LEFT JOIN trk_anul_sale an ON an.trk_sale_id = ts.id "
                    + "WHERE ts.sale_type <> 'mul' "
                    + "AND an.id IS NULL "
                    + "AND ts.`date` BETWEEN '" + begin + "' AND '" + end + "' "
                    + "AND !ts.training";

            st = con.createStatement();
            ResultSet rs = st.executeQuery(str);
            int cols = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                String strRow = "";
                for (int j = 0; j < cols; j++) {
                    Object cell = rs.getObject(j + 1);
                    strRow += (cell != null ? cell.toString() : "");
                    if (j < cols - 1) {
                        strRow += ",";
                    }
                }
                strRow += (System.lineSeparator());
                out.write(strRow);
            }

            str = "SELECT "
                    + "(SELECT nie FROM inv_center c WHERE lat IS NOT NULL ORDER BY (POW(c.lat - (ts.lat),2) + POW(c.lon - (ts.lon ),2)) ASC LIMIT 1), "
                    + "CONCAT(CAST(LPAD(tmc.cube_nif_y, 2, 0) AS CHAR), CAST(LPAD(tmc.cube_nif_f, 4, 0) AS CHAR), CAST(LPAD(tmc.cube_nif_s, 6, 0) AS CHAR)), "
                    + "CAST(dp.code AS CHAR), "
                    + "CONCAT('E', CAST(ts.stratum AS CHAR)), "
                    + "ts.`zone`, "
                    + "IF(e.store_id IS NOT NULL, 'E', 'VRC'), "
                    + "CONCAT('CIL', ct.name), "
                    + "ts.bill, "
                    + "CAST((SELECT cyl_type_price FROM trk_multi_prices WHERE sale_id = ts.id AND cyl_type_id = ct.id) AS CHAR), "
                    + "ct.kg, "
                    + "CAST(0 as char) "
                    + "FROM trk_sale ts "
                    + "INNER JOIN trk_multi_cyls tmc ON tmc.sale_id = ts.id AND tmc.`type` = 'del' "
                    + "INNER JOIN dane_poblado dp ON ts.dane_pob_id = dp.id "
                    + "INNER JOIN employee e ON ts.emp_id = e.id "
                    + "INNER JOIN cylinder_type ct ON tmc.cube_cyl_type_id = ct.id "
                    + "LEFT JOIN trk_anul_sale an ON an.trk_sale_id = ts.id "
                    + "WHERE ts.sale_type = 'mul' "
                    + "AND an.id IS NULL "
                    + "AND ts.`date` BETWEEN '" + begin + "' AND '" + end + "' "
                    + "AND !ts.training";

            st = con.createStatement();
            rs = st.executeQuery(str);
            cols = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                String strRow = "";
                for (int j = 0; j < cols; j++) {
                    Object cell = rs.getObject(j + 1);
                    strRow += (cell != null ? cell.toString() : "");
                    if (j < cols - 1) {
                        strRow += ",";
                    }
                }
                strRow += (System.lineSeparator());
                out.write(strRow);
            }

        } catch (Exception ex) {
            Logger.getLogger(ExportNif.class.getName()).log(Level.SEVERE, null, ex);
            try {
                response.setStatus(500);
                response.getWriter().write(ex.getMessage());
            } catch (IOException ex1) {
                response.setStatus(500);
                Logger.getLogger(GetE3Report.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
            }
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
        return "Reporte de Contratos";
    }
}
