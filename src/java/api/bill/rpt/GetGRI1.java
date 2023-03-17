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
@WebServlet(name = "getGRI1", urlPatterns = {"/getGRI1"})
public class GetGRI1 extends HttpServlet {

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
            SessionLogin.validate(sessionId, con);

            response.setHeader("Content-Disposition", "attachment;filename=GRI1.csv");
            response.setContentType("text/plain;charset=ISO-8859-1");
            out = response.getWriter();
            out.write("Código DANE,"//0
                    + "Longitud,"//1
                    + "Latitud,"//2
                    + "Altitud,"//3
                    + "Tipo de Estación,"//4
                    + "Código Estación,"//5
                    + "Fecha Entrada en Operación,"//6
                    + "Fecha Cumplimiento VUN," //7
                    + "Resolución," //8
                    + "Capacidad regulación/almacenamiento," //9
                    + "Código del Certificado de conformidad del almacenamiento de GLP," //10
                    + "Nombre del organismo de inspección que certificó el almacenamiento de GLP" //11
                    + System.lineSeparator());

            String str = "SELECT " 
                    + "p.code AS 'Código DANE', " 
                    + "TRUNCATE(bs.lon, 6) AS 'Longitud', " 
                    + "TRUNCATE(bs.lat, 6) AS 'Latitud', " 
                    + "TRUNCATE(bs.alt, 0) AS 'Altitud', " 
                    + "3 AS 'Tipo de Estación', " 
                    + "bs.code AS 'Código Estación', " 
                    + "CAST(DATE_FORMAT(bs.beg_date,'%d-%m-%Y') AS CHAR) AS 'Fecha Entrada en Operación', " 
                    + "NULL AS 'Fecha Cumplimiento VUN', " 
                    + "bm.resolution AS 'Resolución', " 
                    + "bs.capacity AS 'Capacidad regulación/almacenamiento', " 
                    + "bs.cod_cert AS 'Código del Certificado de conformidad del almacenamiento de GLP', " 
                    + "bii.name AS 'Nombre del organismo de inspección que certificó el almacenamiento de GLP' " 
                    + "FROM sigma.bill_station AS bs " 
                    + "INNER JOIN sigma.bill_instance AS i ON bs.inst_id = i.id " 
                    + "INNER JOIN sigma.dane_poblado AS p ON p.id = i.pob_id " 
                    + "INNER JOIN sigma.bill_market AS bm ON i.market_id = bm.id " 
                    + "LEFT JOIN sigma.bill_inst_inspector AS bii ON bs.inspector_id = bii.id";
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
                Logger.getLogger(GetGRI1.class.getName()).log(Level.SEVERE, null, ex1);
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
        return "Informe SUI GRI1";
    }
}
