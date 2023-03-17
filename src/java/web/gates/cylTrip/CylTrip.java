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
import utilities.JsonUtils;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "CylTrip", urlPatterns = {"/CylTrip"})
public class CylTrip extends HttpServlet {

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
                    case "create": {
                        GtCylTrip trip = new GtCylTrip();
                        trip = getTrip(req, trip);
                        int tripId = GtTripManager.createCylTrip(trip, req.getString("notes"), req.containsKey("fromMobile"), conn);
                        ob.add("tripId", tripId);
                        break;
                    }
                    case "edit": {
                        GtCylTrip trip = new GtCylTrip();
                        trip = getTrip(req, trip);
                        GtTripManager.editCylTrip(trip, req.getString("notes"), req.containsKey("fromMobile"), conn);
                        break;
                    }
                    case "fillInv": {
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
                        boolean fromMobile = req.containsKey("fromMobile");
                        int tripId = req.getInt("tripId");
                        int empId = req.getInt("empId");
                        String evType = req.getString("evType");
                        if (evType.toLowerCase().equals("e")) {
                            if (new MySQLQuery("SELECT COUNT(*) > 0 FROM gt_trip_reload r WHERE r.trip_id = " + tripId + " AND !r.cancelled AND r.sdt IS NULL").getAsBoolean(conn)) {
                                throw new Exception("Existe un recargue en planta pendiente. Continúe con el recargue o cancélelo.");
                            }
                        }
                        boolean closeFrm = GtTripManager.fillTripInventory(tripId, empId, evType, invDate, lstAmount, req.getString("notes"), fromMobile, conn);
                        ob.add("closeFrm", closeFrm);
                        break;
                    }
                    case "cancel": {
                        int tripId = req.getInt("tripId");
                        boolean cancel = req.getBoolean("cancel");
                        String cancelNotes = req.getString("cancelNotes");
                        String notes = req.getString("notes");
                        int empId = req.getInt("empId");
                        new MySQLQuery("UPDATE gt_cyl_trip SET cancel = ?1, cancel_notes = ?2 WHERE id = " + tripId).setParam(1, cancel).setParam(2, cancelNotes).executeUpdate(conn);
                        GtTripManager.createTripLog(false, tripId, empId, notes, req.containsKey("fromMobile"), null, conn);

                        Object[][] movData = new MySQLQuery("SELECT id FROM inv_movement WHERE gt_cyl_trip_id = " + tripId).getRecords(conn);
                        if (movData != null && movData.length > 0) {
                            for (int i = 0; i < movData.length; i++) {
                                Integer invMovId = MySQLQuery.getAsInteger(movData[i][0]);
                                new MySQLQuery("UPDATE inv_movement SET cancel = " + (cancel ? "1" : "0") + ", cancel_notes = '" + ("Modificado desde Porterías. " + cancelNotes + notes) + "' WHERE id = " + invMovId).executeUpdate(conn);

                                new MySQLQuery(InvLog.getLogQuery(invMovId, InvLog.INV_MOV, "Tipo: Cancelación de Movimiento"
                                        + (cancel ? "\nEstado: Cancelada" : "\nEstado: Activo") + (" Modificado desde Porterías. " + cancelNotes + notes), empId, conn)).executeInsert(conn);
                            }
                        }

                        new MySQLQuery("UPDATE trk_cyl_load l INNER JOIN trk_cyl c ON l.cyl_id = c.id SET c.salable = 1, c.resp_id = NULL WHERE l.cyl_trip_id = " + tripId).executeUpdate(conn);
                        break;
                    }
                    default:
                        break;
                }
                ob.add("status", "OK");
            } catch (Exception ex) {
                if (ex instanceof ShortException) {
                    ((ShortException) ex).simplePrint();
                } else {
                    Logger.getLogger(CylTrip.class.getName()).log(Level.SEVERE, null, ex);
                }
                ob.add("status", "ERROR");
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
            Logger.getLogger(CylTrip.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex1) {
                    Logger.getLogger(CylTrip.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        } finally {
            MySQLSelect.tryClose(conn);
        }
    }

    private GtCylTrip getTrip(JsonObject r, GtCylTrip trip) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        trip.id = r.containsKey("id") ? JsonUtils.getInt(r, "id") : null;
        trip.tripDate = r.containsKey("tripDate") ? sdf.parse(JsonUtils.getString(r, "tripDate")) : null;
        trip.typeId = r.containsKey("typeId") ? JsonUtils.getInt(r, "typeId") : null;
        trip.vhId = r.containsKey("vhId") ? JsonUtils.getInt(r, "vhId") : null;
        trip.driverId = r.containsKey("driverId") ? JsonUtils.getInt(r, "driverId") : null;
        trip.nextTripId = r.containsKey("nextTripId") ? JsonUtils.getInt(r, "nextTripId") : null;
        trip.plate = r.containsKey("plate") ? JsonUtils.getString(r, "plate") : null;
        trip.driver = r.containsKey("driver") ? JsonUtils.getString(r, "driver") : null;
        trip.authDoc = r.containsKey("authDoc") ? JsonUtils.getString(r, "authDoc") : null;
        trip.tryNum = r.containsKey("tryNum") ? JsonUtils.getInt(r, "tryNum") : null;
        trip.blocked = r.containsKey("blocked") ? JsonUtils.getBoolean(r, "blocked", false) : null;
        trip.centerOrigId = r.containsKey("centerOrigId") ? JsonUtils.getInt(r, "centerOrigId") : null;
        trip.centerDestId = r.containsKey("centerDestId") ? JsonUtils.getInt(r, "centerDestId") : null;
        trip.enterpriseId = r.containsKey("enterpriseId") ? JsonUtils.getInt(r, "enterpriseId") : null;
        trip.employeeId = r.containsKey("employeeId") ? JsonUtils.getInt(r, "employeeId") : null;
        trip.cancel = r.containsKey("cancel") ? JsonUtils.getBoolean(r, "cancel", false) : null;
        trip.cancelNotes = r.containsKey("cancelNotes") ? JsonUtils.getString(r, "cancelNotes") : null;
        trip.steps = r.containsKey("steps") ? JsonUtils.getInt(r, "steps") : null;
        trip.reqSteps = r.containsKey("reqSteps") ? JsonUtils.getInt(r, "reqSteps") : null;
        trip.orgTripId = r.containsKey("orgTripId") ? JsonUtils.getInt(r, "orgTripId") : null;
        trip.liqId = r.containsKey("liqId") ? JsonUtils.getInt(r, "liqId") : null;
        trip.showInLiq = r.containsKey("showInLiq") ? JsonUtils.getBoolean(r, "showInLiq", false) : null;
        trip.factoryId = r.containsKey("factoryId") ? JsonUtils.getInt(r, "factoryId") : null;

        if (r.containsKey("ssign")) {
            String ssign = JsonUtils.getString(r, "ssign");
            trip.ssign = ssign != null ? ssign.getBytes() : null;
        }
        if (r.containsKey("esign")) {
            String esign = JsonUtils.getString(r, "esign");
            trip.esign = esign != null ? esign.getBytes() : null;
        }
        if (r.containsKey("textData")) {
            String textData = JsonUtils.getString(r, "textData");
            trip.textData = textData != null ? textData.getBytes() : null;
        }
        if (r.containsKey("cdt")) {
            String cdt = JsonUtils.getString(r, "cdt");
            trip.cdt = cdt != null ? sdf.parse(cdt) : null;
        }
        if (r.containsKey("sdt")) {
            String sdt = JsonUtils.getString(r, "sdt");
            trip.sdt = sdt != null ? sdf.parse(sdt) : null;
        }
        if (r.containsKey("edt")) {
            String edt = JsonUtils.getString(r, "edt");
            trip.edt = edt != null ? sdf.parse(edt) : null;
        }
        if (r.containsKey("ddt")) {
            String ddt = JsonUtils.getString(r, "ddt");
            trip.ddt = ddt != null ? sdf.parse(ddt) : null;
        }
        return trip;
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
}
