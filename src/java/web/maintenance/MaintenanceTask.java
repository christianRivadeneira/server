package web.maintenance;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@WebServlet(name = "MaintTask", urlPatterns = {"/MaintTask"})
public class MaintenanceTask extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.addHeader("Access-Control-Allow-Credentials", "true");
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");
        PrintWriter out = response.getWriter();
        boolean hasPoll = false; //por retrocompatibilidad de la app
        Map<String, String> req = MySQLQuery.scapedParams(request);
        String poolName = "sigmads";
        Integer vehId = null;
        if (req.containsKey("poolName")) {
            hasPoll = true;
            poolName = MySQLQuery.getAsString(req.get("poolName"));
        }
        if (req.containsKey("vehId")) {
            vehId = MySQLQuery.getAsInteger(req.get("vehId"));
        }
        try (Connection conn = MySQLCommon.getConnection(poolName, null)) {
            generatePrevPlan(vehId, conn);
            out.write("Tarea Preventiva OK");
        } catch (Exception ex) {
            Logger.getLogger(MaintenanceTask.class.getName()).log(Level.SEVERE, null, ex);
            try {
                if (hasPoll) {
                    response.setStatus(500);
                    response.getWriter().write(ex.getMessage());
                }
            } catch (IOException ex1) {
                response.setStatus(500);
                Logger.getLogger(MaintenanceTask.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    public static void generatePrevPlan(Integer vehId, Connection conn) throws Exception {
        String sdel;
        if (vehId == null) {
            sdel = "DELETE FROM mto_pend_task WHERE task_prog_id IS NULL;";
        } else {
            sdel = "DELETE FROM mto_pend_task WHERE vehicle_id =" + vehId + " AND task_prog_id IS NULL;";
        }

        String str = "INSERT INTO mto_pend_task "
                + "(`id`, "
                + "`maint_task_id`, "
                + "`vehicle_id`, "
                + "`km_left`, "
                + "`hrs_left`, "
                + "`days_limit` "
                + ") "
                + "SELECT "
                + "null, "
                + "mt.id, "
                + "v.id, "
                + "CASE km_src "
                + "WHEN 'fueload' THEN COALESCE(vmt.mileage, mt.mileage) - COALESCE((SELECT SUM(l.mileage_cur - COALESCE(l.mileage_last, l.mileage_cur)) FROM fuel_load l  WHERE l.vehicle_id = v.id AND IF(MAX(w.`begin`) IS NULL, TRUE, l.days     >= MAX(w.`begin`))),0) "
                + "WHEN 'chk'     THEN COALESCE(vmt.mileage, mt.mileage) - COALESCE((SELECT SUM(l.mileage     - COALESCE(l.last_mileage, l.mileage)) FROM mto_chk_lst l WHERE l.vh_id         = v.id AND IF(MAX(w.`begin`) IS NULL, TRUE, DATE(l.dt) >= MAX(w.`begin`))),0) "
                + "WHEN 'route'   THEN COALESCE(vmt.mileage, mt.mileage) - COALESCE((SELECT SUM(len) FROM mto_trip t WHERE t.veh_id = v.id AND IF(MAX(w.`begin`) IS NULL, TRUE, t.trip_date >= MAX(w.`begin`))),0) "
                + "WHEN 'gps'     THEN COALESCE(vmt.mileage, mt.mileage) - COALESCE((SELECT SUM(km) FROM mto_gps_km r WHERE r.vh_id = v.id AND r.dt >= MAX(w.`begin`)),0) "
                + "WHEN 'manual'  THEN COALESCE(vmt.mileage, mt.mileage) - "
                + "                         (SELECT MAX(l.mileage_cur) - "
                + "                             COALESCE((SELECT "
                + "                                  MAX(t.kms) "
                + "                                  FROM "
                + "                                  mto_task_prog t "
                + "                                  INNER JOIN mto_pend_task p ON p.task_prog_id = t.id   "
                + "                                  WHERE "
                + "                                  p.vehicle_id = v.id AND   "
                + "                                  t.maint_task_id = mt.id  ), "
                + "                             MAX(l.mileage_last)) "
                + "                             FROM mto_kms_manual l   "
                + "                             WHERE "
                + "                             l.vh_id = v.id ) "
                + "WHEN 'none'    THEN NULL "
                + "END, "
                + "IF(hr_src, COALESCE(vmt.hours, mt.hours) - (SELECT SUM(IFNULL(l.hr, 0) - COALESCE(l.last_hr, IFNULL(l.hr, 0))) FROM mto_chk_lst l WHERE l.vh_id = v.id AND IF(MAX(w.`begin`) IS NULL, TRUE, DATE(l.dt) >= MAX(w.`begin`))), NULL), "
                + "DATE_ADD(COALESCE(MAX(w.`begin`), (SELECT MAX(t.dt) FROM mto_pend_task p INNER JOIN mto_task_prog t ON t.id = p.task_prog_id WHERE p.vehicle_id = v.id), v.register_date),INTERVAL COALESCE(vmt.weeks, mt.`week`) WEEK) "//9
                + "FROM "
                + "vehicle AS v "
                + "INNER JOIN vehicle_type AS vt ON v.vehicle_type_id = vt.id "
                + "INNER JOIN maint_task AS mt ON vt.id = mt.type_id "
                + "LEFT JOIN vehicle_maint_task AS vmt ON vmt.vehicle_id = v.id AND vmt.maint_task_id = mt.id "
                + "LEFT JOIN item AS i ON i.maint_task_id = mt.id "
                + "LEFT JOIN work_order AS w ON w.id = i.work_id AND w.vehicle_id = v.id "
                + "WHERE "
                + "v.prev_mto = 1 AND "
                + "v.active = 1 AND "
                + "v.visible = 1 ";
        if (vehId != null) {
            str += " AND v.id =" + vehId + " ";
        }
        str += "GROUP BY v.id, mt.id ;";
        new MySQLQuery(sdel).executeDelete(conn);
        new MySQLQuery(str).executeUpdate(conn);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (IOException | ServletException ex) {
            Logger.getLogger(MaintenanceTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (IOException | ServletException ex) {
            Logger.getLogger(MaintenanceTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
