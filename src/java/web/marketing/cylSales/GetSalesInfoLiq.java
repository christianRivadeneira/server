package web.marketing.cylSales;

import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
@WebServlet(name = "GetSalesInfoLiq", urlPatterns = {"/GetSalesInfoLiq"})
public class GetSalesInfoLiq extends HttpServlet {

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
                    dt = new MySQLQuery("SELECT l.dt FROM dto_liquidation l WHERE l.salesman_id = (SELECT IFNULL(drv.id, IFNULL(dis.id ,IFNULL(str.id, ctr.id)) ) "
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

                MySQLQuery q = new MySQLQuery("SELECT "
                        + "t.id, " //0
                        + "t.`name`, " //1
                        + "COUNT(*), " //2
                        + "s.price, " //3
                        + "SUM(s.price), " //4
                        + "SUM(s.subsidy) " //5
                        + "FROM trk_sale AS s "
                        + "INNER JOIN trk_cyl AS tc ON tc.id = s.cylinder_id "
                        + "INNER JOIN cylinder_type AS t ON tc.cyl_type_id = t.id "
                        + "LEFT JOIN trk_anul_sale an ON an.trk_sale_id = s.id "
                        + "WHERE "
                        + (summDayly ? "s.date > CURDATE() " : "s.date > ?1 AND s.liq_id IS NULL ")
                        + "AND an.id IS NULL "
                        + "AND s.hide_dt IS NULL "
                        + "AND s.emp_id = " + empId + " "
                        + "AND s.sale_type <> 'mul' "
                        + "GROUP BY t.id");
                if (!summDayly) {
                    q.setParam(1, dt);
                }
                Object[][] unitData = q.getRecords(conn);

                q = new MySQLQuery(" SELECT "
                        + "t.id, " //0
                        + "t.name, " //1
                        + "COUNT(tc.id), " //2
                        + "mp.cyl_type_price "
                        + "FROM trk_sale AS s "
                        + "INNER JOIN trk_multi_prices AS mp ON mp.sale_id = s.id "
                        + "INNER JOIN cylinder_type AS t ON mp.cyl_type_id = t.id "
                        + "INNER JOIN trk_multi_cyls AS mc ON mc.sale_id = s.id AND mc.`type`= 'del' "
                        + "INNER JOIN trk_cyl AS tc ON tc.id = mc.cyl_id AND tc.cyl_type_id = t.id "
                        + "LEFT JOIN trk_anul_sale an ON an.trk_sale_id = s.id "
                        + "WHERE "
                        + (summDayly ? "s.date > CURDATE() " : "s.date > ?1 AND s.liq_id IS NULL ")
                        + "AND an.id IS NULL "
                        + "AND s.hide_dt IS NULL "
                        + "AND s.emp_id = " + empId + " "
                        + "AND s.sale_type = 'mul' "
                        + "GROUP BY mp.cyl_type_id,mp.cyl_type_price; ");
                if (!summDayly) {
                    q.setParam(1, dt);
                }
                Object[][] multiData = q.getRecords(conn);

                q = new MySQLQuery("SELECT "
                        + "t.id, " //0
                        + "t.name, " //1
                        + "COUNT(tc.id), "
                        + "mp.cyl_type_price "
                        + "FROM trk_pv_sale AS s "
                        + "INNER JOIN trk_pv_prices AS mp ON mp.pv_sale_id = s.id "
                        + "INNER JOIN cylinder_type AS t ON mp.cyl_type_id = t.id "
                        + "INNER JOIN trk_pv_cyls as pc ON pc.pv_sale_id = s.id AND pc.`type`= 'del' "
                        + "INNER JOIN trk_cyl AS tc ON tc.id = pc.cyl_id AND tc.cyl_type_id = t.id "
                        + "WHERE "
                        + (summDayly ? "s.dt > CURDATE() " : "s.dt > ?1 ")
                        + "AND s.emp_id = " + empId + " "
                        + "GROUP BY mp.cyl_type_id,mp.cyl_type_price; ");
                if (!summDayly) {
                    q.setParam(1, dt);
                }
                Object[][] pvData = q.getRecords(conn);

                List<Info> items = new ArrayList<>();

                for (Object[] unitData1 : unitData) {
                    for (Object[] multiData1 : multiData) {
                        if (unitData1[0].equals(multiData1[0])) {
                            unitData1[2] = MySQLQuery.getAsInteger(unitData1[2]) + MySQLQuery.getAsInteger(multiData1[2]);
                            unitData1[4] = MySQLQuery.getAsInteger(unitData1[4]) + (MySQLQuery.getAsInteger(multiData1[2]) * MySQLQuery.getAsInteger(multiData1[3]));
                            break;
                        }
                    }
                    Info inf = new Info();
                    inf.typeId = MySQLQuery.getAsInteger(unitData1[0]);
                    inf.cap = MySQLQuery.getAsString(unitData1[1]);
                    inf.cant = MySQLQuery.getAsString(unitData1[2]);
                    inf.vlrUnit = MySQLQuery.getAsString(unitData1[3]);
                    inf.vlrTotal = MySQLQuery.getAsString(unitData1[4]);
                    inf.vlrSub = unitData1[5] != null ? MySQLQuery.getAsString(unitData1[5]) : "0";
                    items.add(inf);
                }

                for (Object[] multiData1 : multiData) {
                    boolean exist = false;
                    for (Object[] unitData1 : unitData) {
                        if (unitData1[0].equals(multiData1[0])) {
                            exist = true;
                            break;
                        }
                    }
                    if (!exist) {
                        Info inf = new Info();
                        inf.typeId = MySQLQuery.getAsInteger(multiData1[0]);
                        inf.cap = MySQLQuery.getAsString(multiData1[1]);
                        inf.cant = MySQLQuery.getAsString(multiData1[2]);
                        inf.vlrUnit = MySQLQuery.getAsString(multiData1[3]);
                        inf.vlrTotal = String.valueOf(Integer.valueOf(inf.cant) * Integer.valueOf(inf.vlrUnit));
                        inf.vlrSub = "0";
                        items.add(inf);
                    }
                }

                for (Object[] pvData1 : pvData) {
                    boolean exist = false;
                    for (Object[] unitData1 : unitData) {
                        if (unitData1[0].equals(pvData1[0])) {
                            exist = true;
                            break;
                        }
                    }
                    if (!exist) {
                        Info inf = new Info();
                        inf.typeId = MySQLQuery.getAsInteger(pvData1[0]);
                        inf.cap = MySQLQuery.getAsString(pvData1[1]);
                        inf.cant = MySQLQuery.getAsString(pvData1[2]);
                        inf.vlrUnit = MySQLQuery.getAsString(pvData1[3]);
                        inf.vlrTotal = String.valueOf(Integer.valueOf(inf.cant) * Integer.valueOf(inf.vlrUnit));
                        inf.vlrSub = "0";
                        items.add(inf);
                    }
                }

                JsonArrayBuilder ab = Json.createArrayBuilder();
                for (Info item : items) {
                    JsonObjectBuilder ob = Json.createObjectBuilder();

                    ob.add("typeId", item.typeId);
                    ob.add("cap", item.cap);
                    ob.add("cant", item.cant);
                    ob.add("vlrUnit", item.vlrUnit);
                    ob.add("vlrSub", item.vlrSub);
                    ob.add("vlrTotal", item.vlrTotal);
                    ab.add(ob.build());
                }
                w.writeObject(Json.createObjectBuilder().add("data", ab.build()).build());
            } catch (Exception ex) {
                Logger.getLogger(GetSalesInfoLiq.class.getName()).log(Level.SEVERE, null, ex);
                w.writeObject(Json.createObjectBuilder().add("error", ex.getMessage()).build());
            }
        } catch (Exception ex) {
            Logger.getLogger(GetSalesInfoLiq.class.getName()).log(Level.SEVERE, null, ex);
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

    private class Info {

        public Integer typeId;
        public String cap;
        public String cant;
        public String vlrUnit;
        public String vlrSub;
        public String vlrTotal;

        public Info() {
        }
    }
}
