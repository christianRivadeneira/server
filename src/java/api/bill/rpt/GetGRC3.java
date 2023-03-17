package api.bill.rpt;

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
@WebServlet(name = "getGRC3", urlPatterns = {"/getGRC3"})
public class GetGRC3 extends HttpServlet {

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
            String sessionId = req.getString("sessionId");
            int spanId = req.getInt("spanId");
            String instId = req.getString("instId");
            SessionLogin.validate(sessionId, con);

            response.setHeader("Content-Disposition", "attachment;filename=clientsCyls.csv");
            response.setContentType("text/plain;charset=ISO-8859-1");
            out = response.getWriter();
            out.write("NIU,"//0
                    + "Tipo de gas,"//1
                    + "Tipo sector de consumo,"//2
                    + "ID Factura,"//3
                    + "Periodo Compensado,"//4
                    + "Año,"//5
                    + "DES,"//6
                    + "CI," //7
                    + "Demanda Promedio," //8
                    + "Valor Compensado," //9
                    + "Código Causal" //10
                    + System.lineSeparator());

            String str = "SELECT c.code AS 'NIU', " 
                + "3 AS 'Tipo de gas', " 
                + "IF(c.sector_type = 'r', 1, 2) AS 'Tipo sector de consumo', " 
                + "(SELECT b.bill_num FROM " 
                + "" + instId + ".bill_bill b " 
                + "INNER JOIN " + instId + ".bill_antic_note n ON b.bill_span_id = n.bill_span_id AND b.client_tank_id = n.client_tank_id " 
                + "WHERE n.srv_fail_id = s.id " 
                + "ORDER BY b.creation_date ASC LIMIT 1) AS 'ID Factura', " 
                + "MONTH (p.cons_month) AS 'Periodo Compensado', " 
                + "YEAR (p.cons_month) AS 'Año', " 
                + "TIME_FORMAT(TIMEDIFF(s.end_dt, s.beg_dt), '%H:%i') AS 'DES', " 
                + "TRUNCATE(s.creg_cost, 0) AS 'CI', " 
                + "TRUNCATE((s.creg_cost * s.avg_cons), 2) AS 'Demanda Promedio', " 
                + "TRUNCATE(s.cost, 0) AS 'Valor Compensado', " 
                + "CAST(s.causal_type AS UNSIGNED) AS 'Código Causal' " 
                + "FROM " + instId + ".bill_service_fail s " 
                + "INNER JOIN " + instId + ".bill_client_tank c ON c.id = s.client_id " 
                + "INNER JOIN " + instId + ".bill_span p ON s.span_id = p.id WHERE s.span_id = "+spanId;
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
                Logger.getLogger(GetGRC3.class.getName()).log(Level.SEVERE, null, ex1);
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
        return "Clientes Cilindros Montagas";
    }
}
