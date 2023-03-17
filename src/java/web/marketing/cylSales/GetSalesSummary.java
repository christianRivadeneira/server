package web.marketing.cylSales;

import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
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
import service.MySQL.MySQLSelect;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "GetSalesSummary", urlPatterns = {"/GetSalesSummary"})
public class GetSalesSummary extends HttpServlet {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;

        response.addHeader("Access-Control-Allow-Credentials", "true");
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");

        Map<String, String> req = MySQLQuery.scapedParams(request);
        Integer empId = Integer.valueOf(req.get("emp_id"));
        String sessionId = req.get("session_id");

        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            SessionLogin.validate(sessionId);
            conn = MySQLCommon.getConnection("sigmads", null);

            try {
                Date dt = null;
                Boolean summDayly = new MySQLQuery("SELECT summ_dayly FROM com_cfg").getAsBoolean(conn);

                if (!summDayly) {
                    dt = new MySQLQuery("SELECT l.dt FROM dto_liquidation l WHERE l.salesman_id = (SELECT IFNULL(drv.id, IFNULL(dis.id, IFNULL(str.id, ctr.id))) "
                            + "FROM employee e "
                            + "LEFT JOIN dto_salesman drv ON e.id = drv.driver_id AND drv.active "
                            + "LEFT JOIN dto_salesman dis ON e.id = dis.distributor_id AND dis.active "
                            + "LEFT JOIN dto_salesman str ON e.store_id = str.store_id AND str.active "
                            + "LEFT JOIN dto_salesman ctr ON e.contractor_id = ctr.contractor_id AND ctr.active "
                            + "WHERE e.id = " + empId + " AND e.active) ORDER BY id DESC LIMIT 1").getAsDate(conn);

                    if (dt == null) {
                        dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2016-01-01 00:00:00");
                    }
                }

                MySQLQuery q = new MySQLQuery("SELECT * FROM "
                        + "(SELECT "
                        + "s.date as dt, "
                        + "c.document, "
                        + "s.auth, "
                        + "s.subsidy, "
                        + "s.bill, "
                        + "CONCAT(LPAD(s.cube_nif_y, 2, 0), '-', LPAD(s.cube_nif_f, 4, 0), '-' , LPAD(s.cube_nif_s, 6, 0)), "
                        + "s.sale_type, "
                        + "s.is_sowing, "
                        + "s.credit, "
                        + "s.courtesy "
                        + "FROM trk_sale AS s "
                        + "INNER JOIN ord_contract_index AS c ON s.index_id = c.id "
                        + "LEFT JOIN trk_anul_sale an ON an.trk_sale_id = s.id "
                        + "WHERE s.emp_id = " + empId + " "
                        + "AND s.hide_dt IS NULL "
                        + "AND an.id IS NULL "
                        + (summDayly ? "AND s.`date` > CURDATE() " : "AND s.`date` > ?1 AND s.liq_id IS NULL ")
                        + ""
                        + (summDayly ? "UNION ALL "
                                + ""
                                + "SELECT "
                                + "s.dt as dt, "
                                + "st.document, "
                                + "NULL, "
                                + "NULL, "
                                + "s.bill, "
                                + "NULL, "
                                + "'pv', "
                                + "0, "
                                + "s.credit, "
                                + "0 "
                                + "FROM trk_pv_sale AS s "
                                + "INNER JOIN inv_store AS st ON st.id = s.store_id "
                                + "WHERE emp_id = " + empId + " "
                                + "AND s.dt > CURDATE() " : "")
                        + ") AS l ORDER BY l.dt DESC");

                if (!summDayly) {
                    q.setParam(1, dt);
                }

                Object[][] data = q.getRecords(conn);
                JsonArrayBuilder ab = Json.createArrayBuilder();
                for (Object[] row : data) {
                    String auth = MySQLQuery.getAsString(row[2]);
                    Integer subsidy = MySQLQuery.getAsInteger(row[3]);

                    JsonObjectBuilder ob = Json.createObjectBuilder();
                    ob.add("sale_date", sdf.format(MySQLQuery.getAsDate(row[0])));
                    ob.add("doc", MySQLQuery.getAsString(row[1]));
                    ob.add("aut", (auth != null ? auth : "No Aplica"));
                    ob.add("valSub", (subsidy != null ? subsidy : 0));
                    ob.add("bill", MySQLQuery.getAsString(row[4]));
                    ob.add("nif", (row[5] != null ? MySQLQuery.getAsString(row[5]) : "No Aplica"));
                    ob.add("sale_type", MySQLQuery.getAsString(row[6]));
                    ob.add("isSowing", MySQLQuery.getAsBoolean(row[7]));
                    ob.add("credit", MySQLQuery.getAsBoolean(row[8]));
                    ob.add("courtesy", MySQLQuery.getAsBoolean(row[9]));
                    ab.add(ob.build());
                }
                w.writeObject(Json.createObjectBuilder().add("data", ab.build()).build());
            } catch (Exception ex) {
                Logger.getLogger(GetSalesSummary.class.getName()).log(Level.SEVERE, null, ex);
                w.writeObject(Json.createObjectBuilder().add("error", ex.getMessage()).build());
            }
        } catch (Exception ex) {
            Logger.getLogger(GetSalesSummary.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySQLSelect.tryClose(conn);
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
        return "Short description";
    }
}
