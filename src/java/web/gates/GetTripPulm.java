package web.gates;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
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

@WebServlet(name = "GetTripPulm", urlPatterns = {"/GetTripPulm"})
public class GetTripPulm extends HttpServlet {

    private static final int COLUMNS = 2;//NUMERO DE COLUMNAS 
    private static final int CELL_LEN = 14;//NUMERO DE CARACTERES EN CADA CELDA
    private static final int CELL_LENG_RELOAD = 21;//NUMERO DE CARACTERES EN CADA CELDA

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
        response.setContentType("text/plain; charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Connection conn = null;
        Statement st = null;
        try {
            conn = MySQLCommon.getConnection("sigmads", null);
            st = conn.createStatement();
            Map<String, String> par = MySQLQuery.scapedParams(request);
            int tripId = Integer.parseInt(par.get("tripId"));

            List<Model> load = getInventoryData(st, tripId, "s");
            List<Model> unload = getInventoryData(st, tripId, "e");
            List<Model> reloads = getReloadsData(st, tripId);
            List<Model> totalReloads = getTotalReloads(st, tripId);
            List<Model> novs = getNovsData(st, tripId);

            printHeader(st, tripId, out);
            printModels(load, "Cargue", out);
            printReloads(reloads, totalReloads, out);
            printModels(unload, "Descargue", out);
            printNovs(novs, out);
            st.close();
            conn.close();
        } catch (Exception ex) {
            Logger.getLogger(GetTripPulm.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySQLSelect.tryClose(st);
            MySQLSelect.tryClose(conn);
        }
    }

    private void printHeader(Statement st, int tripId, PrintWriter out) throws Exception {
        String str = "SELECT "
                + "t.auth_doc, "
                + "DATE_FORMAT(NOW(),'%d/%m/%Y %h:%i %p'), "
                + "IF(t.driver_id IS NOT NULL ,(SELECT CONCAT(e.last_name,' ',e.first_name) FROM employee AS e WHERE e.id = t.driver_id), t.driver), "
                + "IF(t.vh_id IS NOT NULL, "
                + "(SELECT CONCAT(COALESCE(v.internal,''),' ',e.short_name,' - ', v.plate)FROM vehicle AS v INNER JOIN agency AS a ON v.agency_id = a.id INNER JOIN enterprise AS e ON a.enterprise_id = e.id WHERE v.id = t.vh_id) "
                + ",t.plate) "
                + "FROM gt_cyl_trip AS t "
                + "INNER JOIN gt_trip_type AS tt ON t.type_id = tt.id "
                + "WHERE tt.type = 'cyls' AND t.id = " + tripId;
        try (ResultSet rs = st.executeQuery(str)) {
            if (rs.next()) {
                out.write("VIAJE " + rs.getString(1).toUpperCase());
                out.write("\r\nImpreso: " + rs.getString(2));
                out.write("\r\n\r\nConductor y Vehículo:");
                out.write("\r\n" + rs.getString(3));
                out.write("\r\n" + rs.getString(4));
            }
        }
    }

    private void printNovs(List<Model> ls, PrintWriter out) {
        if (ls.isEmpty()) {
            return;
        }
        out.write("\r\n\r\nNOVEDADES");
        for (Model n : ls) {
            out.write("\r\n" + pad((n.cylsType + " " + n.state.toUpperCase() + " x " + n.amount), CELL_LEN) + n.novName);
        }
    }

    private void printReloads(List<Model> lsReloads, List<Model> lsTotals, PrintWriter out) {
        if (lsReloads.isEmpty()) {
            return;
        }
        out.write("\r\n\r\n" + "RECARGAS" + "\r\n");
        for (int i = 0; i < lsReloads.size(); i++) {
            Model m = lsReloads.get(i);
            String row = m.vehicleR + ":" + m.cylsType + " " + m.state.toUpperCase() + " x " + m.amount;
            out.write(pad(row, CELL_LENG_RELOAD) + m.date);
            if (i < lsReloads.size() - 1) {
                out.write("\r\n");
            }
        }
        /////////////////totales//////////////////////
        printModels(lsTotals, "TOTAL RECARGAS POR TIPO", out);
    }

    private void printModels(List<Model> ls, String title, PrintWriter out) {
        if (ls.isEmpty()) {
            return;
        }
        int total = 0;
        int colCount = 0;
        out.write("\r\n\r\n" + title.toUpperCase() + "\r\n");
        for (Model m : ls) {
            String row = m.cylsType + " " + m.state.toUpperCase() + " x " + m.amount;
            out.write(pad(row, CELL_LEN));
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
        out.write((!title.contains("TOTAL") ? "Total " + title + ": " : "Total Recargas: ") + total);
    }

    public static String pad(String str, int celLength) {
        if (str == null) {
            return null;
        }
        final char[] buf = new char[celLength - str.length()];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = ' ';
        }
        return str.concat(new String(buf));
    }

    private List<Model> getInventoryData(Statement st, int tripId, String type) throws Exception {
        List<Model> load = new ArrayList<>();
        String loadQ = "SELECT CONCAT(capa.`name`, type.short_name), SUM(i.amount), i.state "
                + "FROM gt_cyl_trip AS t "
                + "INNER JOIN gt_cyl_inv AS i ON t.id = i.trip_id "
                + "INNER JOIN inv_cyl_type AS type ON i.type_id = type.id "
                + "INNER JOIN cylinder_type AS capa ON i.capa_id = capa.id "
                + "WHERE i.type = '" + type + "' AND t.id = " + tripId + " "
                + "AND t.sdt IS NOT NULL "
                + "GROUP BY i.capa_id,i.type_id,i.state "
                + "ORDER BY CAST(capa.`name` AS SIGNED) ASC, type.short_name ASC, IF(i.state = 'l', 1, IF(i.state = 'v', 2, 3)) ";
        try (ResultSet rs = st.executeQuery(loadQ)) {
            while (rs.next()) {
                Model m = new Model();
                m.cylsType = rs.getString(1);
                m.amount = rs.getInt(2);
                m.state = rs.getString(3);
                load.add(m);
            }
        }
        return load;
    }

    private List<Model> getNovsData(Statement st, int tripId) throws Exception {
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
                + "WHERE n.trip_id = " + tripId + " "
                + "ORDER BY t.name";
        try (ResultSet rs = st.executeQuery(novsQ)) {
            while (rs.next()) {
                Model n = new Model();
                n.novName = rs.getString(1);
                n.state = rs.getString(2);
                n.amount = rs.getInt(3);
                n.cylsType = rs.getString(4);
                novs.add(n);
            }
        }
        return novs;
    }

    private List<Model> getTotalReloads(Statement st, int tripId) throws Exception {
        List<Model> reloads = new ArrayList<>();
        String reloadQ = "SELECT "
                + "CONCAT(capa.`name`, type.short_name), "
                + "SUM(rc.amount) "
                + "FROM gt_reload AS r "
                + "INNER JOIN gt_reload_cyls AS rc ON rc.reload_id = r.id "
                + "INNER JOIN inv_cyl_type AS type ON rc.type_id = type.id "
                + "INNER JOIN cylinder_type AS capa ON rc.capa_id = capa.id "
                + "WHERE r.pulm_trip_id = " + tripId + " "
                + "AND r.cancel = 0 "
                + "GROUP BY rc.capa_id, rc.type_id "
                + "ORDER BY r.reload_date ASC";
        try (ResultSet rs = st.executeQuery(reloadQ)) {
            while (rs.next()) {
                Model m = new Model();
                m.cylsType = rs.getString(1);
                m.amount = rs.getInt(2);
                m.state = "L";
                reloads.add(m);
            }
        }
        return reloads;
    }

    private List<Model> getReloadsData(Statement st, int tripId) throws Exception {
        List<Model> reloads = new ArrayList<>();
        String reloadQ = "SELECT "
                + "DATE_FORMAT(r.reload_date,'%d/%m %h:%i %p'), "
                + "(SELECT IF(tr.vh_id IS NULL,tr.plate,(SELECT v.plate FROM vehicle AS v WHERE v.id = tr.vh_id)) FROM gt_cyl_trip AS tr WHERE tr.id = r.vh_trip_id ), "
                + "CONCAT(capa.`name`, type.short_name), "
                + "SUM(rc.amount) "
                + "FROM gt_reload AS r "
                + "INNER JOIN gt_reload_cyls AS rc ON rc.reload_id = r.id "
                + "INNER JOIN inv_cyl_type AS type ON rc.type_id = type.id "
                + "INNER JOIN cylinder_type AS capa ON rc.capa_id = capa.id "
                + "WHERE r.pulm_trip_id = " + tripId + " "
                + "AND r.cancel = 0 "
                + "GROUP BY rc.capa_id, rc.type_id,r.id "
                + "ORDER BY r.reload_date ASC ";
        try (ResultSet rs = st.executeQuery(reloadQ)) {
            while (rs.next()) {
                Model m = new Model();
                m.date = rs.getString(1);
                m.vehicleR = rs.getString(2);
                m.cylsType = rs.getString(3);
                m.amount = rs.getInt(4);
                m.state = "L";
                reloads.add(m);
            }
        }
        return reloads;
    }

    class Sales {

        String name;
        int value;
    }

    class Model {

        String date;
        String vehicleR;
        String cylsType;
        int amount;
        String novName;
        String state;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(GetTripPulm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(GetTripPulm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "Info Trip Pulm";
    }

}
