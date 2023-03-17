package web.gates;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import service.MySQL.MySQLSelect;
import utilities.MySQLQuery;

@WebServlet(name = "GetTrip", urlPatterns = {"/GetTrip"})
public class GetTrip extends HttpServlet {

    private static final int COLUMNS = 2;//NUMERO DE COLUMNAS
    private static final int CELL_LEN = 14;//NUMERO DE CARACTERES EN CADA CELDA

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
        response.addHeader("Access-Control-Allow-Credentials", "true");
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");
        PrintWriter out = response.getWriter();
        Connection conn = null;
        try {
            conn = MySQLCommon.getConnection("sigmads", null);
            Map<String, String> req = MySQLQuery.scapedParams(request);
            int tripId = MySQLQuery.getAsInteger(req.get("tripId"));
            List<Model> load = getInventoryData(conn, tripId, "s");
            List<Model> unload = getInventoryData(conn, tripId, "e");
            List<Pulm> reloads = getReloadsData(conn, tripId);
            List<Model> reloadsPl = getReloadsPlantData(conn, tripId);
            List<Sales> sales = getSalesData(conn, tripId);
            List<Model> novs = getNovsData(conn, tripId);

            printHeader(conn, tripId, out);
            printModels(load, "Cargue", out);

            printReloads(reloads, "Recargas Pulmón", out);
            printModels(reloadsPl, "Recargas Planta", out);

            printModels(unload, "Descargue", out);
            printNovs(novs, out);
            printSales(sales, out);

            conn.close();
        } catch (Exception ex) {
            Logger.getLogger(GetTrip.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySQLSelect.tryClose(conn);
        }
    }

    private void printHeader(Connection con, int tripId, PrintWriter out) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT "
                + "t.auth_doc, "
                + "DATE_FORMAT(NOW(),'%d/%m/%Y %h:%i %p'), "
                + "IF(t.driver_id IS NOT NULL ,(SELECT CONCAT(e.last_name,' ',e.first_name) FROM employee AS e WHERE e.id = t.driver_id), t.driver), "//1
                + "IF(t.vh_id IS NOT NULL , (SELECT CONCAT(COALESCE(v.internal,''),' ',e.short_name,' - ', v.plate)FROM vehicle AS v INNER JOIN agency AS a ON v.agency_id = a.id INNER JOIN enterprise AS e ON a.enterprise_id = e.id WHERE v.id = t.vh_id), t.plate) "//2
                + "FROM gt_cyl_trip AS t "
                + "INNER JOIN gt_trip_type AS tt ON t.type_id = tt.id "
                + "WHERE tt.type = 'cyls' "
                + "AND t.id = " + tripId);
        Object[][] data = q.getRecords(con);
        for (int i = 0; i < data.length; i++) {
            out.write("VIAJE " + MySQLQuery.getAsString(data[i][0]).toUpperCase());
            out.write("\r\nImpreso: " + MySQLQuery.getAsString(data[i][1]));
            out.write("\r\n\r\nConductor y Vehículo:");
            out.write("\r\n" + data[i][2]);
            out.write("\r\n" + MySQLQuery.getAsString(data[i][3]));
        }
    }

    private void printNovs(List<Model> ls, PrintWriter out) {
        if (ls.isEmpty()) {
            return;
        }
        out.write("\r\n\r\nNOVEDADES");
        for (int i = 0; i < ls.size(); i++) {
            Model n = ls.get(i);
            out.write("\r\n" + n.novName);
            out.write("\r\n" + pad(n.cylsType + " " + n.state.toUpperCase() + " x " + n.amount));
            if (i < ls.size() - 1) {
                out.write("\r\n");
            }
        }
    }

    private void printReloads(List<Pulm> pulms, String title, PrintWriter out) {
        if (pulms.isEmpty()) {
            return;
        }
        out.write("\r\n\r\n" + title.toUpperCase() + "");
        int total = 0;
        for (Pulm p : pulms) {
            total += printModels(p.cyls, p.platePulm, out);
        }
        out.write("\r\nTotal " + title + ": " + total);
    }

    private int printModels(List<Model> ls, String title, PrintWriter out) {
        if (ls.isEmpty()) {
            return 0;
        }
        int total = 0;
        int colCount = 0;
        out.write("\r\n\r\n" + title.toUpperCase() + "\r\n");
        for (Model m : ls) {
            String row = m.cylsType + " " + m.state.toUpperCase() + " x " + m.amount;
            out.write(pad(row));
            colCount++;
            total += m.amount;
            if (colCount == COLUMNS) {
                out.write("\r\n");
                colCount = 0;
            }
        }
        if (ls.size() % COLUMNS != 0) {
            out.write("\r\n");
        }
        out.write("Total " + title + ": " + total);
        return total;
    }

    private void printSales(List<Sales> ls, PrintWriter out) {
        if (ls.isEmpty()) {
            return;
        }
        int total = 0;
        int colCount = 0;
        out.write("\r\n\r\nVENTAS\r\n");
        for (Sales s : ls) {
            String row = pad(s.name + " x " + s.value + " ");
            out.write(pad(row));
            colCount++;
            total += s.value;
            if (colCount == COLUMNS) {
                out.write("\r\n");
                colCount = 0;
            }
        }
        if (ls.size() % COLUMNS != 0) {
            out.write("\r\n");
        }
        out.write("Total Ventas: " + total);
    }

    public static String pad(String str) {
        if (str == null) {
            return null;
        }
        final char[] buf = new char[CELL_LEN - str.length()];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = ' ';
        }
        return str.concat(new String(buf));
    }

    private List<Model> getInventoryData(Connection con, int tripId, String type) throws Exception {
        List<Model> load = new ArrayList<>();
        String loadQ = "SELECT CONCAT(capa.`name`, type.short_name), SUM(i.amount), i.state "
                + "FROM gt_cyl_trip AS t "
                + "INNER JOIN gt_cyl_inv AS i ON t.id = i.trip_id "
                + "INNER JOIN inv_cyl_type AS type ON i.type_id = type.id "
                + "INNER JOIN cylinder_type AS capa ON i.capa_id = capa.id "
                + "WHERE "
                + "i.type = '" + type + "' AND t.id = " + tripId + " "
                + "AND t.sdt IS NOT NULL "
                + "GROUP BY i.capa_id,i.type_id,i.state "
                + "ORDER BY CAST(capa.`name` AS SIGNED) ASC, type.short_name ASC, IF(i.state = 'l', 1, IF(i.state = 'v', 2, 3)) ";
        MySQLQuery q = new MySQLQuery(loadQ);
        Object[][] data = q.getRecords(con);
        for (Object[] row : data) {
            Model m = new Model();
            m.cylsType = MySQLQuery.getAsString(row[0]);
            m.amount = MySQLQuery.getAsInteger(row[1]);
            m.state = MySQLQuery.getAsString(row[2]);
            load.add(m);
        }
        return load;
    }

    private List<Model> getNovsData(Connection con, int tripId) throws Exception {
        List<Model> novs = new ArrayList<>();
        String novsQ = "SELECT "
                + "t.`name`,"
                + "n.state,"
                + "amount,"
                + "CONCAT(capa.`name`, type.short_name) "
                + "FROM gt_cyl_nov AS n "
                + "INNER JOIN gt_nov_type AS t ON t.id = n.nov_type_id "
                + "INNER JOIN gt_cyl_trip AS tr ON tr.id = n.trip_id "
                + "INNER JOIN inv_cyl_type AS type ON n.type_id = type.id "
                + "INNER JOIN cylinder_type AS capa ON n.capa_id = capa.id "
                + "WHERE "
                + "n.trip_id = " + tripId + " "
                + "ORDER BY t.name";
        MySQLQuery q = new MySQLQuery(novsQ);
        Object[][] data = q.getRecords(con);
        for (Object[] row : data) {
            Model n = new Model();
            n.novName = MySQLQuery.getAsString(row[0]);
            n.state = MySQLQuery.getAsString(row[1]);
            n.amount = MySQLQuery.getAsInteger(row[2]);
            n.cylsType = MySQLQuery.getAsString(row[3]);
            novs.add(n);
        }
        return novs;
    }

    private List<Pulm> getReloadsData(Connection con, int tripId) throws Exception {
        List<Pulm> pulms = new ArrayList<>();
        String dataReload = "SELECT r.pulm_trip_id "
                + "FROM gt_reload AS r "
                + "INNER JOIN gt_cyl_trip AS ct ON ct.id = r.vh_trip_id "
                + "WHERE r.vh_trip_id = " + tripId + " "
                + "AND r.cancel = 0 "
                + "GROUP BY r.pulm_trip_id ";
        Object[][] data = new MySQLQuery(dataReload).getRecords(con);

        for (Object[] row : data) {
            Pulm p = new Pulm();
            Integer pulmTripid = MySQLQuery.getAsInteger(row[0]);
            p.platePulm = new MySQLQuery("SELECT "
                    + "IF(ct.vh_id IS NOT NULL,"
                    + "(SELECT CONCAT(COALESCE(v.internal,''),' ',e.short_name,' - ', v.plate) FROM vehicle AS v INNER JOIN agency AS a ON v.agency_id = a.id INNER JOIN enterprise AS e ON a.enterprise_id = e.id WHERE v.id = ct.vh_id),"
                    + "ct.plate) "
                    + "FROM gt_cyl_trip AS ct "
                    + "WHERE ct.id = " + pulmTripid).getAsString(con);

            Object[][] dataCylsReloads = new MySQLQuery("SELECT CONCAT(capa.`name`, type.short_name), SUM(rc.amount) "
                    + "FROM gt_reload AS r "
                    + "INNER JOIN gt_reload_cyls AS rc ON rc.reload_id = r.id "
                    + "INNER JOIN inv_cyl_type AS type ON rc.type_id = type.id "
                    + "INNER JOIN cylinder_type AS capa ON rc.capa_id = capa.id "
                    + "WHERE r.pulm_trip_id = " + pulmTripid + " AND r.vh_trip_id = " + tripId + " "
                    + "AND r.cancel = 0 "
                    + "GROUP BY rc.capa_id, rc.type_id "
                    + "ORDER BY CAST(capa.`name` AS SIGNED) ASC ").getRecords(con);
            p.cyls = new ArrayList<>();
            for (Object[] rowCyls : dataCylsReloads) {
                Model m = new Model();
                m.cylsType = MySQLQuery.getAsString(rowCyls[0]);
                m.amount = MySQLQuery.getAsInteger(rowCyls[1]);
                m.state = "L";
                p.cyls.add(m);
            }
            pulms.add(p);
        }
        return pulms;
    }

    private List<Model> getReloadsPlantData(Connection con, int tripId) throws Exception {
        List<Model> load = new ArrayList<>();
        String loadQ = "SELECT CONCAT(capa.`name`, 'M'), SUM(i.amount) AS cnt, i.state "
                + "FROM gt_trip_reload AS t "
                + "INNER JOIN gt_trip_reload_inv AS i ON t.id = i.trip_rel_id "
                + "INNER JOIN cylinder_type AS capa ON i.cyl_type_id = capa.id "
                + "WHERE "
                + "i.`type` = 'c' AND t.trip_id = " + tripId + " "
                + "AND t.sdt IS NOT NULL "
                + "AND !t.cancelled "
                + "GROUP BY i.cyl_type_id, i.state "
                + "HAVING cnt > 0 "
                + "ORDER BY CAST(capa.`name` AS SIGNED) ASC ";
        MySQLQuery q = new MySQLQuery(loadQ);
        Object[][] data = q.getRecords(con);
        for (Object[] row : data) {
            Model m = new Model();
            m.cylsType = MySQLQuery.getAsString(row[0]);
            m.amount = MySQLQuery.getAsInteger(row[1]);
            m.state = MySQLQuery.getAsString(row[2]);
            load.add(m);
        }
        return load;
    }

    private List<Sales> getSalesData(Connection conn, int tripId) throws Exception {
        List<Sales> sales = new ArrayList<>();
        String salesQ = "SELECT CONCAT(capa.`name`, type.short_name), SUM(c) "
                + "FROM "
                + "(SELECT i.capa_id, i.type_id, SUM(i.amount) AS c "
                + "FROM gt_cyl_trip AS t "
                + "INNER JOIN gt_cyl_inv AS i ON t.id = i.trip_id "
                + "WHERE i.state = 'l' "
                + "AND i.type = 's' "
                + "AND t.id = " + tripId + " "
                + "AND t.sdt IS NOT NULL "
                + "GROUP BY i.capa_id,i.trip_id "
                + "UNION ALL "
                + "SELECT i.capa_id, i.type_id, SUM(i.amount)*-1 AS c "
                + "FROM gt_cyl_trip AS t "
                + "INNER JOIN gt_cyl_inv AS i ON t.id = i.trip_id "
                + "WHERE (i.state = 'l' OR i.state = 'f') "
                + "AND i.type = 'e' "
                + "AND t.id = " + tripId + " "
                + "AND t.sdt IS NOT NULL "
                + "GROUP BY i.capa_id,i.trip_id "
                + "UNION ALL "
                + "SELECT rc.capa_id, rc.type_id, SUM(rc.amount) AS c "
                + "FROM gt_reload AS r "
                + "INNER JOIN gt_reload_cyls AS rc ON rc.reload_id = r.id "
                + "WHERE "
                + "r.vh_trip_id = " + tripId + " "
                + "AND r.cancel = 0 "
                + "GROUP BY rc.capa_id, rc.type_id, r.id "
                + "UNION ALL "
                + "SELECT gtri.cyl_type_id, 2, gtri.amount AS c "
                + "FROM gt_trip_reload AS gtr "
                + "INNER JOIN gt_trip_reload_inv AS gtri ON gtri.trip_rel_id = gtr.id "
                + "WHERE "
                + "!gtr.cancelled "
                + "AND gtr.trip_id = " + tripId + " "
                + "AND gtri.`type` = 'c' "
                + "AND gtri.amount > 0 "
                + "GROUP BY gtri.cyl_type_id, gtr.id "
                + "UNION ALL "
                + "SELECT n.capa_id, n.type_id, SUM(n.amount) AS c "
                + "FROM gt_cyl_nov AS n "
                + "INNER JOIN gt_nov_type AS nt ON nt.id = n.nov_type_id "
                + "WHERE n.trip_id = " + tripId + " "
                + "AND (n.state = 'l' OR n.state = 'f') "
                + "AND nt.affects_glp = 1 "
                + "GROUP BY n.capa_id, n.type_id "
                + ")AS l "
                + "INNER JOIN inv_cyl_type AS type ON l.type_id = type.id "
                + "INNER JOIN cylinder_type AS capa ON l.capa_id = capa.id "
                + "GROUP BY l.capa_id, l.type_id "
                + "ORDER BY CAST(capa.`name` AS SIGNED) ASC ";
        MySQLQuery q = new MySQLQuery(salesQ);
        Object[][] data = q.getRecords(conn);
        for (Object[] row : data) {
            Sales s = new Sales();
            s.name = MySQLQuery.getAsString(row[0]);
            s.value = MySQLQuery.getAsInteger(row[1]);
            if (s.value != 0) {
                sales.add(s);
            }
        }
        return sales;
    }

    class Sales {

        String name;
        int value;
    }

    class Model {

        String cylsType;
        int amount;
        String novName;
        String state;
    }

    class Pulm {

        String platePulm;
        List<Model> cyls = new ArrayList<>();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(GetTrip.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(GetTrip.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "Info Trip";
    }

}
