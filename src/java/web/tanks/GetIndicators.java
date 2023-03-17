package web.tanks;

import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import service.MySQL.MySQLSelect;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "GetIndicators", urlPatterns = {"/GetIndicators"})
public class GetIndicators extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
        JsonObjectBuilder ob = Json.createObjectBuilder();
        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            try {
                conn = MySQLCommon.getConnection("sigmads", null);
                JsonObject req = MySQLQuery.scapeJsonObj(request);
                String maxDate = req.getString("maxDate");
                String minDate = req.getString("minDate");
                Integer execId = req.getInt("execId");

                Object[] inFactSaleData = new MySQLQuery("SELECT "
                        + "COUNT(*), "//0
                        + "IFNULL((SELECT COUNT(DISTINCT cl.id) "
                        + "FROM ord_tank_client cl "
                        + "LEFT JOIN est_sale s ON s.client_id = cl.id AND s.sale_date BETWEEN '" + minDate + " 00:00:00' AND '" + maxDate + " 23:59:59' "
                        + "WHERE s.id IS NULL "
                        + "AND cl.exec_reg_id = " + execId + " "
                        + "AND cl.`type` <> 'build' "
                        + "AND (cl.created_date < NOW() OR cl.created_date IS NULL) "
                        + "AND cl.active), 0) "//1
                        + "FROM ord_tank_client cl "
                        + "WHERE cl.active "
                        + "AND cl.exec_reg_id = " + execId + " "
                        + "AND cl.type <> 'build' "
                        + "AND (cl.created_date IS NULL OR cl.created_date < '" + maxDate + " 23:59:59')").getRecord(conn);
                int monthGoal = new MySQLQuery("SELECT "
                        + "IFNULL(SUM(e.month_goal), 0) "
                        + "FROM est_exec_reg e "
                        + "WHERE e.per_emp_id = " + execId).getAsInteger(conn);

                int kgSale = new MySQLQuery("SELECT "
                        + "IFNULL(SUM(s.kgs), 0) "
                        + "FROM est_sale s "
                        + "WHERE "
                        + "s.cancel = 0 AND s.sale_date BETWEEN '" + minDate + " 00:00:00' AND '" + maxDate + " 23:59:59' "
                        + "AND s.bill_type <> 'rem' "
                        + "AND s.exec_id =" + execId).getAsInteger(conn);

                int totalSales = 0;
                int zeroSales = 0;
                if (inFactSaleData != null && inFactSaleData.length > 0) {
                    totalSales = MySQLQuery.getAsInteger(inFactSaleData[0]);
                    zeroSales = MySQLQuery.getAsInteger(inFactSaleData[1]);
                }

                int prosGoal = new MySQLQuery("SELECT "
                        + "IFNULL(SUM(e.afil_goal), 0) "
                        + "FROM est_exec_reg e "
                        + "WHERE e.per_emp_id = " + execId).getAsInteger(conn);

                int prosEfec = new MySQLQuery("SELECT COUNT(*) FROM est_prospect WHERE reg_dt BETWEEN '" + minDate + " 00:00:00' AND '" + maxDate + " 23:59:59' AND reg_exec_id = " + execId + " AND client_id IS NOT NULL").getAsInteger(conn);

                Object[] inFactRemData = new MySQLQuery("SELECT "
                        + "COUNT(*), "
                        + "IFNULL((SELECT COUNT(DISTINCT cl.id) "
                        + "FROM ord_tank_client cl "
                        + "LEFT JOIN est_sale s ON s.client_id = cl.id AND s.sale_date BETWEEN '" + minDate + " 00:00:00' AND '" + maxDate + " 23:59:59' "
                        + "WHERE s.id IS NULL "
                        + "AND cl.exec_reg_id = " + execId + " "
                        + "AND cl.`type` = 'build' "
                        + "AND (cl.created_date < NOW() OR cl.created_date IS NULL) "
                        + "AND cl.active), 0) "
                        + "FROM ord_tank_client cl "
                        + "WHERE cl.active "
                        + "AND cl.exec_reg_id = " + execId + " "
                        + "AND cl.type = 'build' "
                        + "AND (cl.created_date IS NULL OR cl.created_date < '" + maxDate + " 23:59:59')").getRecord(conn);
                
                 int kgRemSale = new MySQLQuery("SELECT "
                        + "IFNULL(SUM(s.kgs), 0) "
                        + "FROM est_sale s "
                        + "WHERE "
                        + "s.cancel = 0 AND s.sale_date BETWEEN '" + minDate + " 00:00:00' AND '" + maxDate + " 23:59:59' "
                        + "AND s.bill_type = 'rem' "
                        + "AND s.exec_id =" + execId).getAsInteger(conn);

                int totalRems = 0;
                int zeroRems = 0;
                if (inFactSaleData != null && inFactSaleData.length > 0) {
                    totalRems = MySQLQuery.getAsInteger(inFactRemData[0]);
                    zeroRems = MySQLQuery.getAsInteger(inFactRemData[1]);
                }

                ob.add("totalSales", totalSales);
                ob.add("zeroSales", zeroSales);
                ob.add("monthGoal", monthGoal);
                ob.add("kgSale", kgSale);
                ob.add("prosGoal", prosGoal);
                ob.add("prosEfec", prosEfec);
                ob.add("totalRems", totalRems);
                ob.add("zeroRems", zeroRems);
                ob.add("kgRemSale", kgRemSale);
                ob.add("result", "OK");
            } catch (Exception ex) {
                Logger.getLogger(GetIndicators.class.getName()).log(Level.SEVERE, null, ex);
                ob.add("result", "Error");
                ob.add("msg", ex.getMessage());
            } finally {
                w.write(ob.build());
                MySQLSelect.tryClose(conn);
            }
        } catch (Exception e) {
            Logger.getLogger(GetIndicators.class.getName()).log(Level.SEVERE, null, e);
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
        return "Indicadores";
    }

}
