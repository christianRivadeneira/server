package web.system;

import web.marketing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import utilities.ServerNow;
import web.discount.ExportNif;

@MultipartConfig
@WebServlet(name = "getClientsCyls", urlPatterns = {"/getClientsCyls"})
public class GetClientsCyls extends HttpServlet {

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

            response.setHeader("Content-Disposition", "attachment;filename=clientsCyls.csv");
            response.setContentType("text/plain;charset=ISO-8859-1");
            out = response.getWriter();
            out.write("Listado de Clientes de Cilindros - Generado el " + new SimpleDateFormat("yyyy-MM-dd_HH-mm").format(new ServerNow()) + System.lineSeparator());
            out.write("" + System.lineSeparator());
            out.write("Documento,"//0
                    + "Nombre,"//1
                    + "Dirección,"//2
                    + "Teléfono,"//3
                    + "Barrio,"//4
                    + "Ciudad "//5
                    + System.lineSeparator());

            String str = "SELECT o.document AS Documento, " //0
                    + "CONCAT(o.first_name ,' ',o.last_name) AS Nombre, " //1
                    + "o.address AS Dirección, " //2
                    + "o.phones AS Teléfono, " //3
                    + "n.name AS Barrio, " //4
                    + "c.name AS Ciudad " //5
                    + "FROM trk_sale s " 
                    + "INNER JOIN ord_contract_index o ON o.id = s.index_id " 
                    + "LEFT JOIN neigh n ON n.id = o.neigh_id " 
                    + "LEFT JOIN city c ON c.id = o.city_id " 
                    + "GROUP BY s.index_id " 
                    + "HAVING COUNT(*) > 1 " 
                    + "ORDER BY c.name";
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
                Logger.getLogger(GetClientsCyls.class.getName()).log(Level.SEVERE, null, ex1);
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
