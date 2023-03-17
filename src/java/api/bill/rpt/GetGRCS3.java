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
@WebServlet(name = "getGRCS3", urlPatterns = {"/getGRCS3"})
public class GetGRCS3 extends HttpServlet {

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
            String instDb = req.getString("instDb");
            SessionLogin.validate(sessionId, con);

            response.setHeader("Content-Disposition", "attachment;filename=clientsCyls.csv");
            response.setContentType("text/plain;charset=ISO-8859-1");
            out = response.getWriter();
            out.write("NIU,"//0
                    + "Fecha de medición,"//1
                    + "Hora de medición,"//2
                    + "Tipo de Gas,"//3
                    + "Presión Medida,"//4
                    + "Método,"//5
                    + "Sustancia Odorante,"//6
                    + "Nivel de Concentración Mínimo," //7
                    + "Nivel de Concentración Medido," //8
                    + "Observaciones" //9
                    + System.lineSeparator());

            String str = "SELECT c.code AS 'NIU', " 
                + "DATE_FORMAT(m.taken_dt, '%d-%m-%Y') AS 'Fecha de medición', " 
                + "DATE_FORMAT(m.taken_dt, '%H:%i') AS 'Hora de medición', " 
                + "(SELECT id FROM sigma.gt_type_gas WHERE active = 1) AS 'Tipo de Gas', " 
                + "TRUNCATE(m.pressure, 2) AS 'Presión Medida', " 
                + "1 AS 'Método', " 
                + "o.id AS 'Sustancia Odorante', " 
                + "TRUNCATE(o.min, 2) AS 'Nivel de concentración mínimo', " 
                + "TRUNCATE(m.odorant_amount, 2) AS 'Nivel de Concentración Medido', " 
                + "'No Aplica' " 
                + "FROM " + instDb + ".bill_measure m " 
                + "INNER JOIN " + instDb + ".bill_client_tank c ON c.id = m.client_id " 
                + "INNER JOIN sigma.bill_odorant o ON o.id = m.odorant_id " 
                + "WHERE m.span_id = " + spanId + " AND m.taken_dt IS NOT NULL";
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
                Logger.getLogger(GetGRCS3.class.getName()).log(Level.SEVERE, null, ex1);
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
        return "Informe SUI GRCS3";
    }
}
