package web.inventory;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.JsonUtils;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "getLiqCsv", urlPatterns = {"/getLiqCsv"})
public class GetLiqCsv extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = null;
        Statement st = null;
        long t = System.currentTimeMillis();

        JsonObjectBuilder ob = Json.createObjectBuilder();
        JsonObject req = MySQLQuery.scapeJsonObj(request);

        String sessionId = req.getString("sessionId");
        String begin = req.getString("begin") + " 00:00:00";
        String end = req.getString("end") + " 23:59:59";
        Integer smanId = req.isNull("smanId") ? null : req.getInt("smanId");
        int centerId = req.getInt("centerId");
        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            try (Connection con = MySQLCommon.getConnection("sigmads", null)) {
                SessionLogin.validate(sessionId, con);
                /*Se hace por JDBC por optimización, no usar MySQLQuery, eneste caso gasta mucha memoria*/

                String strContSales = "SELECT "
                        + "s.dt, "
                        + "CONCAT(COALESCE(e.first_name,''),' ',COALESCE(e.last_name,'')), "
                        + "pv.internal, "
                        + "COALESCE(s.sucursal, ''), "
                        + "pv.document, "
                        + "CONCAT(pv.first_name, ' ', IFNULL(pv.last_name, '')), "
                        + "s.bill, "
                        + "ct.`name`, "
                        + "CONCAT(LPAD(cyl.nif_y, 2, 0), '-', LPAD(cyl.nif_f, cyl.fac_len, 0), '-', LPAD(cyl.nif_s, 6, 0)), "
                        + "(SELECT p.cyl_type_price FROM trk_pv_prices p WHERE p.cyl_type_id = cyl.cyl_type_id AND p.pv_sale_id = s.id), "
                        + "w.warning "
                        + "FROM trk_pv_sale s "
                        + "LEFT JOIN trk_pv_sale_warning w ON w.pv_sale_id = s.id "
                        + "INNER JOIN trk_pv_cyls c ON c.pv_sale_id = s.id "
                        + "INNER JOIN inv_store pv ON s.store_id = pv.id "
                        + "INNER JOIN trk_cyl cyl ON c.cyl_id = cyl.id "
                        + "INNER JOIN cylinder_type ct ON cyl.cyl_type_id = ct.id "
                        + "INNER JOIN employee e ON e.id = s.emp_id AND e.driver = 1 "
                        + "INNER JOIN dto_salesman ds ON ds.driver_id = e.id AND ds.center_id = " + centerId + " "
                        + "WHERE c.`type` = 'del' "
                        + "AND !s.credit AND !s.training "
                        + (smanId != null ? "AND s.emp_id = " + smanId + " " : "")
                        + "AND s.dt BETWEEN '" + begin + "' AND '" + end + "' "
                        + "ORDER BY dt ASC, e.id ASC ";

                Object[][] dataPVCo = getData(st, con, strContSales, out);
                JsonArrayBuilder jsonCase = Json.createArrayBuilder();
                for (Object[] data : dataPVCo) {
                    JsonObjectBuilder row = Json.createObjectBuilder();
                    JsonUtils.addDate(row, "Fecha", data[0]);
                    JsonUtils.addString(row, "Vendedor", data[1]);
                    JsonUtils.addString(row, "Interno", data[2]);
                    JsonUtils.addString(row, "Sucursal", data[3]);
                    JsonUtils.addString(row, "Documento", data[4]);
                    JsonUtils.addString(row, "Cliente", data[5]);
                    JsonUtils.addString(row, "Factura", data[6]);
                    JsonUtils.addString(row, "Tipo", data[7]);
                    JsonUtils.addString(row, "Nif", data[8]);
                    JsonUtils.addBigDecimal(row, "Precio", data[9], true);
                    JsonUtils.addString(row, "Advertencia", data[10]);
                    jsonCase.add(row);
                }
                ob.add("pvCash", jsonCase);

                String strCredSales = "SELECT "
                        + "s.dt, "
                        + "CONCAT(COALESCE(e.first_name,''),' ',COALESCE(e.last_name,'')), "
                        + "pv.internal, "
                        + "COALESCE(s.sucursal, ''), "
                        + "pv.document, "
                        + "CONCAT(pv.first_name, ' ', IFNULL(pv.last_name, '')), "
                        + "s.bill, "
                        + "ct.`name`, "
                        + "CONCAT(LPAD(cyl.nif_y, 2, 0), '-', LPAD(cyl.nif_f, cyl.fac_len, 0), '-', LPAD(cyl.nif_s, 6, 0)), "
                        + "(SELECT p.cyl_type_price FROM trk_pv_prices p WHERE p.cyl_type_id = cyl.cyl_type_id AND p.pv_sale_id = s.id), "
                        + "w.warning "
                        + "FROM trk_pv_sale s "
                        + "LEFT JOIN trk_pv_sale_warning w ON w.pv_sale_id = s.id "
                        + "INNER JOIN trk_pv_cyls c ON c.pv_sale_id = s.id "
                        + "INNER JOIN inv_store pv ON s.store_id = pv.id "
                        + "INNER JOIN trk_cyl cyl ON c.cyl_id = cyl.id "
                        + "INNER JOIN cylinder_type ct ON cyl.cyl_type_id = ct.id "
                        + "INNER JOIN employee e ON e.id = s.emp_id AND e.driver = 1 "
                        + "INNER JOIN dto_salesman ds ON ds.driver_id = e.id AND ds.center_id = " + centerId + " "
                        + "WHERE c.`type` = 'del' "
                        + "AND s.credit AND !s.training "
                        + (smanId != null ? "AND s.emp_id = " + smanId + " " : "")
                        + "AND s.dt BETWEEN '" + begin + "' AND '" + end + "' ";
                Object[][] dataPVCre = getData(st, con, strCredSales, out);
                jsonCase = Json.createArrayBuilder();
                for (Object[] data : dataPVCre) {
                    JsonObjectBuilder row = Json.createObjectBuilder();
                    JsonUtils.addDate(row, "Fecha", data[0]);
                    JsonUtils.addString(row, "Vendedor", data[1]);
                    JsonUtils.addString(row, "Interno", data[2]);
                    JsonUtils.addString(row, "Sucursal", data[3]);
                    JsonUtils.addString(row, "Documento", data[4]);
                    JsonUtils.addString(row, "Cliente", data[5]);
                    JsonUtils.addString(row, "Factura", data[6]);
                    JsonUtils.addString(row, "Tipo", data[7]);
                    JsonUtils.addString(row, "Nif", data[8]);
                    JsonUtils.addBigDecimal(row, "Precio", data[9], true);
                    JsonUtils.addString(row, "Advertencia", data[10]);
                    jsonCase.add(row);
                }
                ob.add("pvCredit", jsonCase);

                String strClieCont = "SELECT "
                        + "s.date, "
                        + "CONCAT(COALESCE(e.first_name,''),' ',COALESCE(e.last_name,'')), "
                        + "'', "
                        + "'', "
                        + "i.document, "
                        + "CONCAT(i.first_name, ' ', IFNULL(i.last_name,'')), "
                        + "s.bill, "
                        + "ct.`name`, "
                        + "CONCAT(LPAD(c.nif_y, 2, 0), '-', LPAD(c.nif_f, c.fac_len, 0), '-', LPAD(c.nif_s, 6, 0)), "
                        + "(SELECT tpp.cyl_type_price FROM trk_multi_prices tpp WHERE tpp.sale_id = s.id AND tpp.cyl_type_id = ct.id), "
                        + "w.warning "
                        + "FROM trk_sale s "
                        + "LEFT JOIN trk_sale_warning w ON w.sale_id = s.id "
                        + "INNER JOIN ord_contract_index i ON s.index_id = i.id "
                        + "INNER JOIN trk_multi_cyls tpc ON tpc.sale_id = s.id AND tpc.`type` = 'del' "
                        + "INNER JOIN trk_cyl c ON tpc.cyl_id = c.id "
                        + "INNER JOIN cylinder_type ct ON c.cyl_type_id = ct.id "
                        + "INNER JOIN employee e ON e.id = s.emp_id AND e.driver = 1 "
                        + "INNER JOIN dto_salesman ds ON ds.driver_id = e.id AND ds.center_id = " + centerId + " "
                        + "WHERE "
                        + "s.date BETWEEN '" + begin + "' AND '" + end + "' AND !s.training "
                        + (smanId != null ? "AND s.emp_id = " + smanId + " " : "")
                        + "AND !s.credit";

                Object[][] cliCash = getData(st, con, strClieCont, out);

                jsonCase = Json.createArrayBuilder();
                for (Object[] data : cliCash) {
                    JsonObjectBuilder row = Json.createObjectBuilder();
                    JsonUtils.addDate(row, "Fecha", data[0]);
                    JsonUtils.addString(row, "Vendedor", data[1]);
                    JsonUtils.addString(row, "Interno", data[2]);
                    JsonUtils.addString(row, "Sucursal", data[3]);
                    JsonUtils.addString(row, "Documento", data[4]);
                    JsonUtils.addString(row, "Cliente", data[5]);
                    JsonUtils.addString(row, "Factura", data[6]);
                    JsonUtils.addString(row, "Tipo", data[7]);
                    JsonUtils.addString(row, "Nif", data[8]);
                    JsonUtils.addBigDecimal(row, "Precio", data[9], true);
                    JsonUtils.addString(row, "Advertencia", data[10]);
                    jsonCase.add(row);
                }
                ob.add("cliCash", jsonCase);

                String strClieCred = "SELECT "
                        + "s.date, "
                        + "CONCAT(COALESCE(e.first_name,''),' ',COALESCE(e.last_name,'')), "
                        + "'', "
                        + "'', "
                        + "i.document, "
                        + "CONCAT(i.first_name, ' ', IFNULL(i.last_name,'')), "
                        + "s.bill, "
                        + "ct.`name`, "
                        + "CONCAT(LPAD(c.nif_y, 2, 0), '-', LPAD(c.nif_f, c.fac_len, 0), '-', LPAD(c.nif_s, 6, 0)), "
                        + "(SELECT tpp.cyl_type_price FROM trk_multi_prices tpp WHERE tpp.sale_id = s.id AND tpp.cyl_type_id = ct.id), "
                        + "w.warning "
                        + "FROM trk_sale s "
                        + "LEFT JOIN trk_sale_warning w ON w.sale_id = s.id "
                        + "INNER JOIN ord_contract_index i ON s.index_id = i.id "
                        + "INNER JOIN trk_multi_cyls tpc ON tpc.sale_id = s.id AND tpc.`type` = 'del' "
                        + "INNER JOIN trk_cyl c ON tpc.cyl_id = c.id "
                        + "INNER JOIN cylinder_type ct ON c.cyl_type_id = ct.id "
                        + "INNER JOIN employee e ON e.id = s.emp_id AND e.driver = 1 "
                        + "INNER JOIN dto_salesman ds ON ds.driver_id = e.id AND ds.center_id = " + centerId + " "
                        + "WHERE "
                        + "s.date BETWEEN '" + begin + "' AND '" + end + "' AND !s.training "
                        + (smanId != null ? "AND s.emp_id = " + smanId + " " : "")
                        + "AND s.credit";

                Object[][] cliCredit = getData(st, con, strClieCred, out);

                jsonCase = Json.createArrayBuilder();
                for (Object[] data : cliCredit) {
                    JsonObjectBuilder row = Json.createObjectBuilder();
                    JsonUtils.addDate(row, "Fecha", data[0]);
                    JsonUtils.addString(row, "Vendedor", data[1]);
                    JsonUtils.addString(row, "Interno", data[2]);
                    JsonUtils.addString(row, "Sucursal", data[3]);
                    JsonUtils.addString(row, "Documento", data[4]);
                    JsonUtils.addString(row, "Cliente", data[5]);
                    JsonUtils.addString(row, "Factura", data[6]);
                    JsonUtils.addString(row, "Tipo", data[7]);
                    JsonUtils.addString(row, "Nif", data[8]);
                    JsonUtils.addBigDecimal(row, "Precio", data[9], true);
                    JsonUtils.addString(row, "Advertencia", data[10]);
                    jsonCase.add(row);
                }
                ob.add("cliCredit", jsonCase);

            } catch (Exception ex) {
                ob.add("status", "error");
                ob.add("msg", ex.getMessage());
                Logger.getLogger(GetLiqCsv.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                w.writeObject(ob.build());
                System.out.println("GetLiqCsv " + (System.currentTimeMillis() - t) + "ms");
            }
        } catch (Exception ex) {
            Logger.getLogger(GetLiqCsv.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Object[][] getData(Statement st, Connection con, String str, PrintWriter out) throws Exception {
        st = con.createStatement();
        ResultSet rs = st.executeQuery(str);
        int cols = rs.getMetaData().getColumnCount();

        List<Object> objs = new ArrayList<>();
        while (rs.next()) {
            Object obj[] = new Object[cols];
            for (int j = 0; j < cols; j++) {
                obj[j] = rs.getObject(j + 1);
            }
            objs.add(obj);
        }
        Object[][] data = objs.toArray(new Object[0][0]);
        return data;
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
        return "Reporte de Liquidación";
    }
}
