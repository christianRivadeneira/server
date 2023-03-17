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
@WebServlet(name = "getE1ReportCsv", urlPatterns = {"/getE1ReportCsv"})
public class GetE1Report extends HttpServlet {

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
        Statement st;
        try (Connection con = MySQLCommon.getConnection("sigmads", null)) {
            JsonObject req = MySQLQuery.scapeJsonObj(request);
            String begin = req.getString("begin");
            String end = req.getString("end");
            String sessionId = req.getString("sessionId");
            SessionLogin.validate(sessionId, con);

            response.setHeader("Content-Disposition", "attachment;filename=E1.csv");
            response.setContentType("text/plain;charset=ISO-8859-1");
            out = response.getWriter();
            out.write("Ventas " + new MySQLQuery("SELECT `name` FROM enterprise WHERE !alternative").getAsString(con) + " Desde: " + begin + " a " + end + System.lineSeparator());
            out.write("" + System.lineSeparator());
            out.write("NIE,NIT,DISTRIBUIDOR,FECHA DE COMPRA,NÚMERO DE FACTURA,CODIGO DE PRESENTACIÓN,CANTIDAD DE CILINDROS,VALOR ($)" + System.lineSeparator());
            /**
             * Se hace por JDBC por optimización, no usar MySQLQuery, en este
             * caso gasta mucha memoria
             */
            
            String str = "SELECT c.nie, "
                    + "'9001758302', "
                    + "DATE_FORMAT(date(ps.dt),\"%d/%m/%Y\") as dt , "
                    + "'TK', "
                    + "CONCAT('CIL' , CAST(t.lb AS CHAR)) , "
                    + "COUNT(pp.id) , "
                    + "COUNT(pp.id) * pp.cyl_type_price "
                    + "FROM trk_pv_sale ps "
                    + "INNER JOIN trk_pv_cyls pc ON pc.pv_sale_id = ps.id AND pc.`type` = 'del' "
                    + "INNER JOIN trk_cyl tc ON tc.id = pc.cyl_id "
                    + "INNER JOIN cylinder_type t ON t.id = tc.cyl_type_id "
                    + "INNER JOIN inv_store st ON st.id = ps.store_id "
                    + "INNER JOIN inv_center c ON c.id = st.center_id "
                    + "INNER JOIN trk_pv_prices pp ON pp.pv_sale_id = ps.id AND pp.cyl_type_id = t.id "
                    + "WHERE dt BETWEEN '" + begin + "' AND '" + end + "' "
                    + "GROUP BY c.id,t.id, pp.cyl_type_price "
                    + "ORDER BY c.id,dt ";

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

        } catch (Exception ex) {
            Logger.getLogger(ExportNif.class.getName()).log(Level.SEVERE, null, ex);
            try {
                response.setStatus(500);
                response.getWriter().write(ex.getMessage());
            } catch (IOException ex1) {
                response.setStatus(500);
                Logger.getLogger(GetE1Report.class.getName()).log(Level.SEVERE, null, ex1);
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
        return "Reporte de Compras E1";
    }
}
