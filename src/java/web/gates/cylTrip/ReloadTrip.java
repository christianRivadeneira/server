package web.gates.cylTrip;

import web.ShortException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
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
@WebServlet(name = "ReloadTrip", urlPatterns = {"/ReloadTrip"})
public class ReloadTrip extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;

        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            JsonObjectBuilder ob = Json.createObjectBuilder();
            conn = MySQLCommon.getConnection("sigmads", null);
            conn.setAutoCommit(false);
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");
            JsonObject req = MySQLQuery.scapeJsonObj(request);

            String header = req.getString("header");
            try {
                switch (header) {
                    case "regIn": {
                        Date invDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(req.getString("evDate"));

                        GregorianCalendar gc = new GregorianCalendar();
                        gc.setTime(invDate);
                        gc.set(GregorianCalendar.HOUR_OF_DAY, 0);
                        gc.set(GregorianCalendar.MINUTE, 0);
                        gc.set(GregorianCalendar.SECOND, 0);
                        Date d1 = gc.getTime();

                        gc.setTime(new Date());
                        gc.set(GregorianCalendar.HOUR_OF_DAY, 0);
                        gc.set(GregorianCalendar.MINUTE, 0);
                        gc.set(GregorianCalendar.SECOND, 0);
                        Date d2 = gc.getTime();
                        if (d1.compareTo(d2) > 0) {
                            throw new Exception("La fecha no debe estár en el futuro.");
                        }

                        JsonArray jar = req.getJsonArray("arrInv");
                        List<CylsAmount> lstAmount = new ArrayList<>();
                        for (int i = 0; i < jar.size(); i++) {
                            JsonObject job = jar.getJsonObject(i);
                            CylsAmount it = new CylsAmount();
                            it.amount = job.getInt("amount");
                            it.capa = job.getInt("capa");
                            it.typeId = job.getInt("typeId");
                            it.state = job.getString("state");
                            lstAmount.add(it);
                        }
                        int tripId = req.getInt("tripId");

                        Integer reloadId = new MySQLQuery("SELECT id FROM gt_trip_reload WHERE !cancelled AND sdt IS NULL AND trip_id = " + tripId).getAsInteger(conn);
                        if (reloadId == null) {
                            reloadId = new MySQLQuery("INSERT INTO gt_trip_reload SET trip_id = " + tripId + ", edt = NOW()").executeInsert(conn);
                        } else {
                            new MySQLQuery("UPDATE gt_trip_reload SET edt = NOW() WHERE id = " + reloadId).executeUpdate(conn);
                            new MySQLQuery("DELETE FROM gt_trip_reload_inv WHERE type = 'e' AND trip_rel_id = " + reloadId).executeDelete(conn);
                        }

                        for (int i = 0; i < lstAmount.size(); i++) {
                            CylsAmount it = lstAmount.get(i);
                            new MySQLQuery("INSERT INTO gt_trip_reload_inv SET cyl_type_id = " + it.capa + ", amount = " + it.amount + ", state = '" + it.state.replace("r", "") + "', type = 'e', trip_rel_id = " + reloadId).executeInsert(conn);
                        }

                        ob.add("reloadId", reloadId);
                        break;
                    }
                    case "regLoad": {
                        Date invDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(req.getString("evDate"));

                        GregorianCalendar gc = new GregorianCalendar();
                        gc.setTime(invDate);
                        gc.set(GregorianCalendar.HOUR_OF_DAY, 0);
                        gc.set(GregorianCalendar.MINUTE, 0);
                        gc.set(GregorianCalendar.SECOND, 0);
                        Date d1 = gc.getTime();

                        gc.setTime(new Date());
                        gc.set(GregorianCalendar.HOUR_OF_DAY, 0);
                        gc.set(GregorianCalendar.MINUTE, 0);
                        gc.set(GregorianCalendar.SECOND, 0);
                        Date d2 = gc.getTime();
                        if (d1.compareTo(d2) > 0) {
                            throw new Exception("La fecha no debe estár en el futuro.");
                        }

                        JsonArray jar = req.getJsonArray("arrInv");
                        List<CylsAmount> lstAmount = new ArrayList<>();
                        for (int i = 0; i < jar.size(); i++) {
                            JsonObject job = jar.getJsonObject(i);
                            CylsAmount it = new CylsAmount();
                            it.amount = job.getInt("amount");
                            it.capa = job.getInt("capa");
                            it.typeId = job.getInt("typeId");
                            it.state = job.getString("state");
                            lstAmount.add(it);
                        }

                        Integer reloadId = req.getInt("reloadId");
                        Integer tripId = req.getInt("tripId");

                        createReloadInv(reloadId, tripId, lstAmount, conn);

                        break;
                    }
                    case "regOut": {
                        Date invDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(req.getString("evDate"));

                        GregorianCalendar gc = new GregorianCalendar();
                        gc.setTime(invDate);
                        gc.set(GregorianCalendar.HOUR_OF_DAY, 0);
                        gc.set(GregorianCalendar.MINUTE, 0);
                        gc.set(GregorianCalendar.SECOND, 0);
                        Date d1 = gc.getTime();

                        gc.setTime(new Date());
                        gc.set(GregorianCalendar.HOUR_OF_DAY, 0);
                        gc.set(GregorianCalendar.MINUTE, 0);
                        gc.set(GregorianCalendar.SECOND, 0);
                        Date d2 = gc.getTime();
                        if (d1.compareTo(d2) > 0) {
                            throw new Exception("La fecha no debe estár en el futuro.");
                        }

                        JsonArray jar = req.getJsonArray("arrInv");
                        List<CylsAmount> lstAmount = new ArrayList<>();
                        for (int i = 0; i < jar.size(); i++) {
                            JsonObject job = jar.getJsonObject(i);
                            CylsAmount it = new CylsAmount();
                            it.amount = job.getInt("amount");
                            it.capa = job.getInt("capa");
                            it.typeId = job.getInt("typeId");
                            it.state = job.getString("state").replace("s", "");
                            lstAmount.add(it);
                        }
                        Integer reloadId = req.getInt("reloadId");

                        Object[][] invData = new MySQLQuery("SELECT SUM(am), ct, st, tp FROM ("
                                + "SELECT "
                                + "i.amount AS am, "
                                + "i.cyl_type_id AS ct, "
                                + "i.state AS st, "
                                + "2 AS tp "
                                + "FROM gt_trip_reload_inv i "
                                + "INNER JOIN gt_trip_reload r ON i.trip_rel_id = r.id "
                                + "WHERE "
                                + "r.id = " + reloadId + " "
                                + "AND i.`type` = 'e' "
                                + "AND i.state = 'l' "
                                + ""
                                + "UNION ALL "
                                + ""
                                + "SELECT "
                                + "i.amount AS am, "
                                + "i.cyl_type_id AS ct, "
                                + "i.state AS st, "
                                + "2 AS tp "
                                + "FROM gt_trip_reload_inv i "
                                + "INNER JOIN gt_trip_reload r ON i.trip_rel_id = r.id "
                                + "WHERE "
                                + "r.id = " + reloadId + " "
                                + "AND i.`type` = 'c') AS l "
                                + "GROUP BY ct ").getRecords(conn);

                        List<CylCmp> lstCmp = new ArrayList<>();

                        for (int i = 0; i < invData.length; i++) {
                            CylsAmount it = new CylsAmount(invData[i]);
                            boolean exist = false;
                            for (int j = 0; j < lstCmp.size(); j++) {
                                CylsAmount cmp = lstCmp.get(j).it;
                                if (cmp.capa == it.capa && cmp.typeId == it.typeId && cmp.state.equals(it.state)) {
                                    exist = true;
                                    break;
                                }
                            }

                            if (!exist) {
                                CylCmp add = new CylCmp();
                                add.it = it;
                                add.match = false;
                                lstCmp.add(add);
                            }
                        }

                        for (int i = 0; i < lstAmount.size(); i++) {
                            CylsAmount it = lstAmount.get(i);
                            boolean exist = false;
                            for (int j = 0; j < lstCmp.size(); j++) {
                                CylsAmount cmp = lstCmp.get(j).it;
                                if (cmp.capa == it.capa && cmp.typeId == it.typeId && cmp.state.equals(it.state)) {
                                    exist = true;
                                    break;
                                }
                            }

                            if (!exist) {
                                CylCmp add = new CylCmp();
                                add.it = it;
                                add.match = false;
                                lstCmp.add(add);
                            }
                        }

                        for (int i = 0; i < lstCmp.size(); i++) {
                            CylsAmount it = lstCmp.get(i).it;
                            for (int j = 0; j < lstAmount.size(); j++) {
                                CylsAmount cmp = lstAmount.get(j);
                                if (it.capa == cmp.capa && it.typeId == cmp.typeId && it.state.equals(cmp.state)) {
                                    lstCmp.get(i).match = it.amount == cmp.amount;
                                    break;
                                }
                            }
                        }

                        for (int i = 0; i < lstCmp.size(); i++) {
                            if (!lstCmp.get(i).match) {
                                CylsAmount cmpIt = lstCmp.get(i).it;
                                for (int j = 0; j < lstAmount.size(); j++) {
                                    CylsAmount it = lstAmount.get(j);
                                    if (it.state.equals("v")) {
                                        if (it.capa == cmpIt.capa && it.amount == cmpIt.amount && it.state.equals(cmpIt.state)) {
                                            lstCmp.get(i).match = true;
                                        }
                                    }
                                }
                            }
                        }

                        for (int i = 0; i < lstCmp.size(); i++) {
                            if (!lstCmp.get(i).match) {
                                throw new Exception("Hay inconsistencias en el inventario de Salida");
                            }
                        }

                        new MySQLQuery("DELETE FROM gt_trip_reload_inv WHERE type = 's' AND trip_rel_id = " + reloadId).executeDelete(conn);
                        for (int i = 0; i < lstAmount.size(); i++) {
                            CylsAmount it = lstAmount.get(i);
                            new MySQLQuery("INSERT INTO gt_trip_reload_inv SET cyl_type_id = " + it.capa + ", amount = " + it.amount + ", state = '" + it.state.replace("s", "") + "', type = 's', trip_rel_id = " + reloadId).executeInsert(conn);
                        }
                        new MySQLQuery("UPDATE gt_trip_reload SET sdt = NOW() WHERE id = " + reloadId).executeUpdate(conn);

                        break;
                    }
                    default:
                        break;
                }
                ob.add("closeFrm", true);
                ob.add("status", "OK");
            } catch (Exception ex) {
                if (ex instanceof ShortException) {
                    ((ShortException) ex).simplePrint();
                } else {
                    Logger.getLogger(ReloadTrip.class.getName()).log(Level.SEVERE, null, ex);
                }
                ob.add("status", "error");
                String m = ex.getMessage();
                if (m != null && !m.isEmpty()) {
                    ob.add("errorMsg", m);
                } else {
                    ob.add("errorMsg", "Error desconocido.");
                }
            } finally {
                w.writeObject(ob.build());
            }
            conn.commit();
        } catch (Exception ex) {
            Logger.getLogger(ReloadTrip.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex1) {
                    Logger.getLogger(ReloadTrip.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        } finally {
            MySQLSelect.tryClose(conn);
        }
    }

    public static void createReloadInv(int relId, int tripId, List<CylsAmount> prods, Connection con) throws Exception {
        Object[][] invData = new MySQLQuery("SELECT SUM(am), cp, st, tp FROM ("
                + "SELECT "
                + "i.amount AS am, "
                + "i.capa_id AS cp, "
                + "i.state AS st, "
                + "i.type_id AS tp "
                + "FROM gt_cyl_inv i "
                + "WHERE i.trip_id = " + tripId + " "
                + "AND i.`type` = 's'"
                + ""
                + "UNION ALL "
                + ""
                + "SELECT "
                + "i.amount AS am, "
                + "i.cyl_type_id AS cp, "
                + "i.state AS st, "
                + "2 AS tp "
                + "FROM gt_trip_reload_inv i "
                + "INNER JOIN gt_trip_reload r ON i.trip_rel_id = r.id "
                + "WHERE "
                + "r.sdt IS NOT NULL "
                + "AND !r.cancelled "
                + "AND i.`type` = 'c' "
                + "AND i.state = 'l' "
                + "AND r.trip_id = " + tripId + ") as l "
                + "GROUP BY cp").getRecords(con);

        List<CylsAmount> lstReload = new ArrayList<>();
        for (int i = 0; i < prods.size(); i++) {
            CylsAmount prod = prods.get(i);
            for (int j = 0; j < invData.length; j++) {
                CylsAmount inv = new CylsAmount(invData[j]);
                if (prod.capa == inv.capa && prod.typeId == inv.typeId) {
                    if (prod.amount < inv.amount) {
                        throw new Exception("Inconsistencia en las cantidades de la planilla de cargue");
                    } else {
                        CylsAmount result = new CylsAmount();
                        result.capa = prod.capa;
                        result.typeId = prod.typeId;
                        result.amount = (prod.amount - inv.amount);
                        if (result.amount > 0) {
                            lstReload.add(result);
                        }
                    }
                }
            }
        }

        Object[][] invInRel = new MySQLQuery("SELECT "
                + "i.amount AS am, "
                + "i.cyl_type_id AS cp, "
                + "i.state AS st, "
                + "2 AS tp "
                + "FROM gt_trip_reload_inv i "
                + "INNER JOIN gt_trip_reload r ON i.trip_rel_id = r.id "
                + "WHERE "
                + "r.sdt IS NULL "
                + "AND !r.cancelled "
                + "AND i.`type` = 'e' "
                + "AND i.state = 'v' "
                + "AND r.trip_id = " + tripId).getRecords(con);

        for (int i = 0; i < lstReload.size(); i++) {
            CylsAmount prod = lstReload.get(i);
            for (int j = 0; j < invInRel.length; j++) {
                CylsAmount inv = new CylsAmount(invInRel[j]);
                if (prod.capa == inv.capa && prod.typeId == inv.typeId) {
                    if (prod.amount > inv.amount) {
                        throw new Exception("Hay inconsistencias en el inventario de Recargue");
                    }
                }
            }
        }

        new MySQLQuery("UPDATE gt_trip_reload SET cdt = NOW() WHERE id = " + relId).executeUpdate(con);
        for (int i = 0; i < lstReload.size(); i++) {
            CylsAmount rel = lstReload.get(i);
            new MySQLQuery("INSERT INTO gt_trip_reload_inv SET trip_rel_id = " + relId + ", cyl_type_id = " + rel.capa + ", amount = " + rel.amount + ", state = 'l', type = 'c'").executeInsert(con);
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
        return "Create Trips";
    }

    private class CylCmp {

        public CylsAmount it;
        boolean match;

    }
}
