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
@WebServlet(name = "getGRTT2", urlPatterns = {"/getGRTT2"})
public class GetGRTT2 extends HttpServlet {

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
            int pobId = req.getInt("pobId");
            String consMonth = req.getString("consMonth");
            int instId = req.getInt("instId");
            SessionLogin.validate(sessionId, con);
            response.setHeader("Content-Disposition", "attachment;filename=clientsCyls.csv");
            response.setContentType("text/plain;charset=ISO-8859-1");
            out = response.getWriter();
            out.write("NIU,"//0
                    + "Tipo de Usuario,"//1
                    + "ID Comercializador,"//2
                    + "ID Mercado,"//3
                    + "Código DANE,"//4
                    + "Ubicación,"//5
                    + "Dirección,"//6
                    + "Información Predial Utilizada," //7
                    + "Cédula Catastral," //8
                    + "Estrato/Sector," //9
                    + "Altitud (usuario)," //10
                    + "Longitud (usuario), " //11
                    + "Latitud (usuario)," //12
                    + "Estado," //13
                    + "Fecha ajuste" //14
                    + System.lineSeparator());

            String str = "SELECT c.code AS 'NIU', " 
                + "2 AS 'Tipo de Usuario', " 
                + "6026 AS 'ID Comercializador', " 
                + "(SELECT bm.id_market FROM sigma.bill_instance AS bi INNER JOIN sigma.bill_market AS bm ON bm.id = bi.market_id WHERE bi.id = " + instId + ") AS 'ID Mercado', " 
                + "(SELECT CAST(code AS UNSIGNED) FROM sigma.dane_poblado WHERE code = " + pobId + ") AS 'Código DANE', " 
                + "IF(c.location IS NOT NULL, CAST(c.location AS UNSIGNED), CAST(2 AS UNSIGNED)) AS 'Ubicación', " 
                + "c.address AS 'Dirección', " 
                + "CAST(c.cad_info AS UNSIGNED) AS 'Información Predial Utilizada', " 
                + "IF(c.cadastral_code IS NULL OR c.cadastral_code = 'No Registra' OR c.cadastral_code = 'Sin Registrar' OR c.cadastral_code = 'No Registrado' OR c.cadastral_code = 'Cod: Sin Registrar' OR c.cadastral_code = 'Cod: No Registra', 0, c.cadastral_code) AS 'Cédula Catastral', " 
                + "(CASE WHEN c.sector_type = 'r' AND c.stratum = 1 THEN 1 WHEN c.sector_type = 'r' AND c.stratum = 2 THEN 2 WHEN c.sector_type = 'r' AND c.stratum = 3 THEN 3 WHEN c.sector_type = 'r' AND c.stratum = 4 THEN 4 WHEN c.sector_type = 'r' AND c.stratum = 5 THEN 5 WHEN c.sector_type = 'r' AND c.stratum = 6 THEN 6 WHEN c.sector_type = 'c' THEN 7 WHEN c.sector_type = 'i' THEN 8 WHEN c.sector_type = 'o' THEN 9 WHEN c.sector_type = 'ea' THEN 10 WHEN c.sector_type = 'ed' THEN 11 END) AS 'Estrato / Sector', " 
                + "(SELECT (CASE WHEN bi.id = 205 THEN 3013 WHEN bi.id = 207 THEN 2985 WHEN bi.id = 208 THEN 2566 WHEN bi.id = 211 THEN 1912 WHEN bi.id = 212 THEN 2103 WHEN bi.id = 213 THEN 1515 WHEN bi.id = 214 THEN 2970 WHEN bi.id = 215 THEN 2881 END) FROM sigma.bill_instance AS bi WHERE bi.id = 207 AND bi.`type`= 'net') AS 'Altitud', " 
                + "(SELECT br.lon FROM " + instDb + ".bill_reading AS br WHERE br.client_tank_id = c.id ORDER BY br.id DESC LIMIT 1) AS 'Longitud', " 
                + "(SELECT br.lat FROM " + instDb + ".bill_reading AS br WHERE br.client_tank_id = c.id ORDER BY br.id DESC LIMIT 1) AS 'Latitud', " 
                + "IF(c.active = 1, 1, 2) AS 'Estado', " 
                + "(CASE WHEN sys.`type` AND sys.dt > '" + consMonth + "' AND sys.dt IS NOT NULL THEN DATE_FORMAT(sys.dt, '%d-%m-%Y') WHEN c.creation_date > '" + consMonth + "' THEN DATE_FORMAT(c.creation_date, '%d-%m-%Y') ELSE DATE_FORMAT(c.creation_date, '%d-%m-%Y') END) AS 'Fecha ajuste' " 
                + "FROM " + instDb + ".bill_client_tank AS c " 
                + "LEFT JOIN sigma.sys_crud_log AS sys ON c.id = sys.owner_serial AND sys.bill_inst_id = " + instId + " GROUP BY c.id";
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
                Logger.getLogger(GetGRTT2.class.getName()).log(Level.SEVERE, null, ex1);
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
