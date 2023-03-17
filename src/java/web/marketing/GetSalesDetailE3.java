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
@WebServlet(name = "getSalesDetailE3", urlPatterns = {"/getSalesDetailE3"})
public class GetSalesDetailE3 extends HttpServlet {

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

            response.setHeader("Content-Disposition", "attachment;filename=sales.csv");
            response.setContentType("text/plain;charset=ISO-8859-1");
            out = response.getWriter();
            out.write("Ventas " + new MySQLQuery("SELECT `name` FROM enterprise WHERE !alternative").getAsString(con) + " Desde: " + begin + "  a " + end + System.lineSeparator());
            out.write("" + System.lineSeparator());
            out.write("NIE%"//0
                    + "NIF%"//1
                    + "Cliente%"//2
                    + "Código Poblado%"//3
                    + "Centro Poblado%"//4
                    + "Departamento%"//5
                    + "Sector%"//6
                    + "Zona%"//7
                    + "Medio%"//8
                    + "Código de Presentación%"//9
                    + "Número de Factura%"//10
                    + "Valor%"//11
                    + "Cantidad(Kg)%"//12
                    + "Aprobación%"//13
                    + "Subsidio%"//14
                    + "Fecha de Venta%"//15
                    + "No. Liquidación%"//16
                    + "Tipo%"//17
                    + "Fecha de liquidación%"//18
                    + "Centro Operativo%"//19
                    + "Empresa%" //20
                    + "Vendedor%"//21
                    + "Nombre%"//22 
                    + "Dirección%"//23
                    + "Teléfono " //24
                    + System.lineSeparator());

            String str = "SELECT "
                    + "(SELECT nie FROM inv_center c WHERE lat IS NOT NULL ORDER BY (POW(c.lat - (ts.lat),2) + POW(c.lon - (ts.lon ),2)) ASC LIMIT 1), " //0
                    + "CONCAT(CAST(LPAD(ts.cube_nif_y, 2, 0) AS CHAR), CAST(LPAD(ts.cube_nif_f, 4, 0) AS CHAR), CAST(LPAD(ts.cube_nif_s, 6, 0) AS CHAR)), " //1
                    + "(SELECT document FROM ord_contract_index WHERE id = ts.index_id), "//2
                    + "CAST(dp.code AS CHAR), "//3
                    + "CONCAT(dp.name, ' - ', dm.name), "//4
                    + "dd.name, "//5
                    + "CONCAT('E', CAST(ts.stratum AS CHAR)), "//6
                    + "ts.`zone`, "//7
                    + "IF(e.store_id IS NOT NULL, 'E', 'VRC'), "//8
                    + "CONCAT('CIL', ct.name), "//9
                    + "ts.bill, "//10
                    + "CAST(ts.price AS CHAR), "//11
                    + "ct.kg, "//12
                    + "CAST(IFNULL(ts.auth, '') as char), "//13
                    + "CAST(IFNULL(ts.subsidy, 0) as char), "//14
                    + "DATE_FORMAT(ts.date, '%d/%m/%Y %H:%i'), "//15
                    + "dl.id, "//16
                    + "dte.cguno_type, "//17
                    + "DATE_FORMAT(dl.dt, '%d/%m/%Y'), "//18
                    + "(SELECT dc.name FROM dto_center dc INNER JOIN dto_salesman myds ON dc.id = myds.center_id WHERE myds.id = ts.sman_id), "//19
                    + "(SELECT ent.name FROM dto_center dc INNER JOIN sys_center sy ON dc.sys_center_id = sy.id INNER JOIN enterprise ent ON sy.enterprise_id = ent.id INNER JOIN dto_salesman myds ON dc.id = myds.center_id WHERE myds.id = ts.sman_id), "// 20
                    + "(SELECT myds.document FROM dto_salesman AS myds WHERE myds.id = ts.sman_id), "//21
                    + "(SELECT CONCAT(first_name,' ',last_name) FROM ord_contract_index WHERE id = ts.index_id), "//22
                    + "(SELECT address FROM ord_contract_index WHERE id = ts.index_id), "//23
                    + "(SELECT phones FROM ord_contract_index WHERE id = ts.index_id)  "//24
                    + "FROM trk_sale ts "
                    + "INNER JOIN dane_poblado dp ON ts.dane_pob_id = dp.id "
                    + "INNER JOIN dane_municipality dm ON dp.mun_id = dm.id "
                    + "INNER JOIN dane_department dd ON dm.dep_id = dd.id "
                    + "INNER JOIN employee e ON ts.emp_id = e.id "
                    + "INNER JOIN cylinder_type ct ON ts.cube_cyl_type_id = ct.id "
                    + "LEFT JOIN dto_liquidation dl ON dl.id = ts.liq_id "
                    + "LEFT JOIN dto_types_equiv dte ON dte.sigma_type = ts.sale_type "
                    + "LEFT JOIN dto_salesman drv ON drv.driver_id = e.id AND drv.active AND e.driver "
                    + "LEFT JOIN dto_salesman dis ON dis.distributor_id = e.id AND dis.active AND e.distributor "
                    + "LEFT JOIN dto_salesman sto ON sto.store_id = e.store_id AND sto.active "
                    + "LEFT JOIN dto_salesman ctr ON ctr.contractor_id = e.contractor_id AND ctr.active "
                    + "LEFT JOIN trk_anul_sale anu ON anu.trk_sale_id = ts.id "
                    + "LEFT JOIN trk_cyl rc ON ts.cyl_received_id = rc.id "
                    + "WHERE ts.sale_type <> 'mul' "
                    + "AND ts.`date` BETWEEN '" + begin + "' AND '" + end + "' "
                    + "AND anu.id IS NULL "
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
                        strRow += "%";
                    }
                }
                strRow += (System.lineSeparator());
                out.write(strRow);
            }

            str = "SELECT "
                    + "(SELECT nie FROM inv_center c WHERE lat IS NOT NULL ORDER BY (POW(c.lat - (ts.lat),2) + POW(c.lon - (ts.lon ),2)) ASC LIMIT 1), "//0
                    + "CONCAT(CAST(LPAD(tmc.cube_nif_y, 2, 0) AS CHAR), CAST(LPAD(tmc.cube_nif_f, 4, 0) AS CHAR), CAST(LPAD(tmc.cube_nif_s, 6, 0) AS CHAR)), "//1
                    + "(SELECT document FROM ord_contract_index WHERE id = ts.index_id), "//2
                    + "CAST(dp.code AS CHAR), "//3
                    + "CONCAT(dp.name, ' - ', dm.name), "//4
                    + "dd.name, "//5
                    + "CONCAT('E', CAST(ts.stratum AS CHAR)), "//6
                    + "ts.`zone`, "//7
                    + "IF(e.store_id IS NOT NULL, 'E', 'VRC'), "//8
                    + "CONCAT('CIL', ct.name), "//9
                    + "ts.bill, "//10
                    + "CAST((SELECT p.cyl_type_price FROM trk_multi_prices p WHERE p.cyl_type_id = ct.id AND p.sale_id = ts.id) AS CHAR), "//11
                    + "ct.kg, "//12
                    + "CAST('' as char), "//13
                    + "CAST(0 as char), "//14
                    + "DATE_FORMAT(ts.date, '%d/%m/%Y %H:%i'), "//15
                    + "dl.id, "//16
                    + "dte.cguno_type, "//17
                    + "DATE_FORMAT(dl.dt, '%d/%m/%Y'), "//18
                    + "(SELECT dc.name FROM dto_center dc INNER JOIN dto_salesman myds ON dc.id = myds.center_id WHERE myds.id = ts.sman_id), "//19
                    + "(SELECT ent.name FROM dto_center dc INNER JOIN sys_center sy ON dc.sys_center_id = sy.id INNER JOIN enterprise ent ON sy.enterprise_id = ent.id INNER JOIN dto_salesman myds ON dc.id = myds.center_id WHERE myds.id = ts.sman_id), "// 20
                    + "(SELECT myds.document FROM dto_salesman AS myds WHERE myds.id = ts.sman_id), "//21
                    + "(SELECT CONCAT(first_name,' ',last_name) FROM ord_contract_index WHERE id = ts.index_id), "//22
                    + "(SELECT address FROM ord_contract_index WHERE id = ts.index_id), "//23
                    + "(SELECT phones FROM ord_contract_index WHERE id = ts.index_id)  "//24                    
                    + "FROM trk_sale ts "
                    + "INNER JOIN trk_multi_cyls tmc ON tmc.sale_id = ts.id AND tmc.`type` = 'del' "
                    + "INNER JOIN dane_poblado dp ON ts.dane_pob_id = dp.id "
                    + "INNER JOIN dane_municipality dm ON dp.mun_id = dm.id "
                    + "INNER JOIN dane_department dd ON dm.dep_id = dd.id "
                    + "INNER JOIN employee e ON ts.emp_id = e.id "
                    + "INNER JOIN cylinder_type ct ON tmc.cube_cyl_type_id = ct.id "
                    + "LEFT JOIN dto_liquidation dl ON dl.id = ts.liq_id "
                    + "LEFT JOIN dto_types_equiv dte ON dte.sigma_type = ts.sale_type "
                    + "LEFT JOIN dto_salesman drv ON drv.driver_id = e.id AND drv.active "
                    + "LEFT JOIN dto_salesman dis ON dis.distributor_id = e.id AND dis.active AND e.distributor "
                    + "LEFT JOIN dto_salesman sto ON sto.store_id = e.store_id AND sto.active "
                    + "LEFT JOIN dto_salesman ctr ON ctr.contractor_id = e.contractor_id AND ctr.active "
                    + "LEFT JOIN trk_anul_sale anu ON anu.trk_sale_id = ts.id "
                    + "LEFT JOIN trk_cyl rc ON ts.cyl_received_id = rc.id "
                    + "WHERE ts.sale_type = 'mul' "
                    + "AND ts.`date` BETWEEN '" + begin + "' AND '" + end + "' "
                    + "AND anu.id IS NULL "
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
                        strRow += "%";
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
                Logger.getLogger(GetSalesDetailE3.class.getName()).log(Level.SEVERE, null, ex1);
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
        return "Ventas Montagas";
    }
}
