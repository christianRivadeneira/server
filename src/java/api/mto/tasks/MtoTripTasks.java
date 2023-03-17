package api.mto.tasks;

import api.mto.api.MtoTripApi;
import api.mto.model.MtoCfg;
import api.mto.model.MtoNoveltyType;
import api.mto.model.MtoProfCfg;
import api.mto.model.MtoRoutePoint;
import api.mto.model.MtoTrip;
import api.mto.model.MtoTripCheck;
import api.mto.model.MtoTripDelayedMsg;
import api.mto.model.MtoTripNovelty;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import service.MySQL.MySQLCommon;
import utilities.Dates;
import utilities.MySQLQuery;
import utilities.ServerNow;
import web.ShortException;
import web.push.GCMUtils;

@Singleton
@Startup
public class MtoTripTasks {

    //IMPORTANTE, cambiar aqui cuando se instale en INVERSIONES PASTO *************
    public static final String POOL_NAME = "invpastods";
    public static final String TZ = "GMT-05:00";

    @Schedule(minute = "*/2", hour = "*")
    protected void checkCoordinates() {
        try (Connection conn = MySQLCommon.getConnection(POOL_NAME, TZ)) {
            MtoCfg cfg = new MtoCfg().select(1, conn);
            Date now = MySQLQuery.now(conn);

            if (cfg.trips) {
                Object[][] tripsData = new MySQLQuery("SELECT c.trip_id, MIN(c.place) FROM "
                        + "mto_trip t "
                        + "INNER JOIN mto_trip_check c ON t.id = c.trip_id "
                        + "WHERE t.canceled = 0 AND c.reg IS NULL AND c.`type` <> 'going_start' AND c.exp_full IS NOT NULL "
                        + "GROUP BY c.trip_id").getRecords(conn);

                for (Object[] tripRow : tripsData) {
                    int tripId = MySQLQuery.getAsInteger(tripRow[0]);
                    int place = MySQLQuery.getAsInteger(tripRow[1]);

                    Object[] checkRow = new MySQLQuery("SELECT c.id, p.lat, p.lon, t.driver_id, exp_full, exp_part, last_notif FROM "
                            + "mto_trip t "
                            + "INNER JOIN mto_trip_check c ON c.trip_id = t.id "
                            + "INNER JOIN mto_route_point p ON p.id = c.point_id "
                            + "WHERE c.trip_id = ?1 AND c.place = ?2").setParam(1, tripId).setParam(2, place).getRecord(conn);

                    int checkId = MySQLQuery.getAsInteger(checkRow[0]);
                    BigDecimal chkLat = MySQLQuery.getAsBigDecimal(checkRow[1], true);
                    BigDecimal chkLon = MySQLQuery.getAsBigDecimal(checkRow[2], true);
                    int driverId = MySQLQuery.getAsInteger(checkRow[3]);
                    Date expFull = MySQLQuery.getAsDate(checkRow[4]);
                    Date expPart = MySQLQuery.getAsDate(checkRow[5]);
                    Date lastNotif = MySQLQuery.getAsDate(checkRow[6]);

                    Date exp = MtoTripApi.getExpected(expPart, expFull);

                    Date begDate = new MySQLQuery("SELECT c.reg FROM "
                            + "mto_trip_check c "
                            + "WHERE c.trip_id = ?1 AND c.`type` = 'going_start';").setParam(1, tripId).getAsDate(conn);

                    if (begDate != null) {
                        Object[][] gpsData = new MySQLQuery("SELECT c.latitude, c.longitude, c.date FROM "
                                + "gps_coordinate c "
                                + "WHERE c.employee_id = ?1 AND c.date > DATE_SUB(NOW(),INTERVAL 6 HOUR) AND c.date > ?2 "
                                + "AND c.`type` = 'norm'").setParam(1, driverId).setParam(2, begDate).getRecords(conn);

                        boolean found = false;
                        for (Object[] gpsRow : gpsData) {
                            BigDecimal gpsLat = MySQLQuery.getAsBigDecimal(gpsRow[0], true);
                            BigDecimal gpsLon = MySQLQuery.getAsBigDecimal(gpsRow[1], true);
                            Date gpsDate = MySQLQuery.getAsDate(gpsRow[2]);

                            double distance = distance(chkLat.doubleValue(), chkLon.doubleValue(), gpsLat.doubleValue(), gpsLon.doubleValue(), "K") * 1000;

                            if (cfg.routeChkRadius >= distance) {
                                MtoTripCheck chk = new MtoTripCheck().select(checkId, conn);
                                chk.reg = gpsDate;
                                chk.update(conn);
                                MtoRoutePoint pt = new MtoRoutePoint().select(chk.pointId, conn);
                                //es el inicio del cumplido
                                if ((pt.mPart != null && pt.mPart == 0) && (pt.hPart != null && pt.hPart == 0)) {
                                    new MySQLQuery("UPDATE mto_trip_check c "
                                            + " INNER JOIN mto_route_point p ON c.point_id = p.id "
                                            + " SET c.exp_part = DATE_ADD(?2,INTERVAL (p.h_part*60) + p.m_part MINUTE) "
                                            + " WHERE c.trip_id = ?1").setParam(1, tripId).setParam(2, chk.reg).executeUpdate(conn);
                                }
                                notifyCheckArrival(checkId, tripId, conn, exp);
                                found = true;
                                break;
                            }
                        }

                        if (!found) {
                            int minuteDiff = (int) ((exp.getTime() - now.getTime()) / 1000 / 60);
                            if (minuteDiff < 0 && Math.abs(minuteDiff) > cfg.routeChkTolerance) {
                                boolean send;
                                if (lastNotif == null) {
                                    send = true;
                                } else {
                                    send = ((now.getTime() - lastNotif.getTime()) / 1000 / 60) > 45;
                                }
                                if (send) {
                                    notifyMissingArrival(checkId, tripId, conn, exp, Math.abs(minuteDiff));
                                }
                            }
                        }
                    }
                }
            }
        } catch (ShortException ex) {
            ex.simplePrint();
        } catch (Exception e) {
            Logger.getLogger(MtoTripTasks.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @Schedule(minute = "*/5", hour = "*")
    protected void checkNonStartedTrips() {
        try (Connection conn = MySQLCommon.getConnection(POOL_NAME, TZ)) {
            if (new MySQLQuery("SELECT trips FROM mto_cfg").getAsBoolean(conn)) {
                //buscar viajes pendientes por iniciar
                MySQLQuery mq = new MySQLQuery("SELECT "
                        + " t.id, "//0
                        + " e.id, "//1
                        + " r.name, "//2
                        + " v.plate, "//3
                        + " CONCAT(e.first_name, ' ', e.last_name), "//4
                        + " tc.exp_full "//5
                        + " FROM mto_trip t "
                        + " INNER JOIN mto_trip_check tc ON t.id = tc.trip_id "
                        + " INNER JOIN mto_route r ON r.id = t.route_id "
                        + " INNER JOIN employee e ON e.id = t.driver_id "
                        + " INNER JOIN vehicle v ON v.id = t.veh_id "
                        + " WHERE t.notified = 0 AND tc.`type` = 'going_start' AND tc.exp_full < NOW() AND tc.reg IS NULL");

                Object[][] pendingTrips = mq.getRecords(conn);

                if (pendingTrips != null && pendingTrips.length > 0) {

                    for (Object[] tripRow : pendingTrips) {
                        int tripId = MySQLQuery.getAsInteger(tripRow[0]);
                        int driverId = MySQLQuery.getAsInteger(tripRow[1]);
                        String routeName = MySQLQuery.getAsString(tripRow[2]);
                        String plate = MySQLQuery.getAsString(tripRow[3]);
                        String driverName = MySQLQuery.getAsString(tripRow[4]);

                        Date expDep = MySQLQuery.getAsDate(tripRow[5]);
                        SimpleDateFormat hf = new SimpleDateFormat("hh:mm a");

                        sendMessage(driverId, "Viaje sin Iniciar", "El viaje '" + routeName + "' debía iniciar a las " + hf.format(expDep), "El viaje '" + routeName + "' debía iniciar a las " + hf.format(expDep) + ". Debe registrar el inicio abriendo la App e ingresando en la sección 'Viajes' del menú principal.", conn);

                        List<Integer> admins = getAdminList(conn);
                        for (int i = 0; i < admins.size(); i++) {
                            Integer id = admins.get(i);
                            String msg = "El viaje '" + routeName + "' de " + driverName + " en el vehículo " + plate + " debía iniciar a las " + hf.format(expDep);
                            sendMessage(id, "Viaje sin Iniciar", msg, msg, conn);
                        }
                        new MySQLQuery("UPDATE mto_trip SET notified = 1 WHERE id = ?1").setParam(1, tripId).executeUpdate(conn);
                    }
                }
            }
        } catch (ShortException ex) {
            ex.simplePrint();
        } catch (Exception e) {
            Logger.getLogger(MtoTripTasks.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @Schedule(minute = "*/5", hour = "*")
    protected void checkDelayedMessages() {
        try (Connection conn = MySQLCommon.getConnection(POOL_NAME, TZ)) {
            Integer appId = new MySQLQuery("SELECT id FROM system_app WHERE package_name = 'qualisys.chklist'").getAsInteger(conn);
            List<MtoTripDelayedMsg> msgs = MtoTripDelayedMsg.getList(new MySQLQuery("SELECT " + MtoTripDelayedMsg.getSelFlds("") + " FROM mto_trip_delayed_msg WHERE sched < NOW()"), conn);
            for (int i = 0; i < msgs.size(); i++) {
                MtoTripDelayedMsg m = msgs.get(i);
                JsonObject json = getJsonNotification(m.subject, m.brief, m.msg, m.event);
                GCMUtils.sendToApp(appId, json, m.empId + "", conn);
                MtoTripDelayedMsg.delete(m.id, conn);
            }
        } catch (ShortException ex) {
            ex.simplePrint();
        } catch (Exception ex) {
            Logger.getLogger(MtoTripTasks.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void notifyCheckArrival(int checkId, int tripId, Connection conn, Date exp) throws Exception {

        Object[] tripRow = new MySQLQuery("SELECT "
                + "e.id, "//1
                + "r.name, "//2
                + "v.plate, "//3
                + "CONCAT(e.first_name, ' ', e.last_name) "//4
                + "FROM mto_trip t "
                + "INNER JOIN mto_route r ON r.id = t.route_id "
                + "INNER JOIN employee e ON e.id = t.driver_id "
                + "INNER JOIN vehicle v ON v.id = t.veh_id "
                + "WHERE t.id = ?1").setParam(1, tripId).getRecord(conn);

        int driverId = MySQLQuery.getAsInteger(tripRow[0]);
        String routeName = MySQLQuery.getAsString(tripRow[1]);
        String plate = MySQLQuery.getAsString(tripRow[2]);
        String driverName = MySQLQuery.getAsString(tripRow[3]);

        Object[] checkRow = new MySQLQuery("SELECT c.reg, p.name FROM "
                + "mto_trip_check c "
                + "INNER JOIN mto_route_point p ON p.id = c.point_id "
                + "WHERE c.id = ?1;").setParam(1, checkId).getRecord(conn);

        Date reg = MySQLQuery.getAsDate(checkRow[0]);
        String ptName = MySQLQuery.getAsString(checkRow[1]);

        SimpleDateFormat hf = new SimpleDateFormat("hh:mm a");

        String driverMsg = "Pasó por el punto de control '" + ptName + "' a las " + hf.format(reg) + ", llegó ";
        int minuteDif = (int) ((exp.getTime() - reg.getTime()) / 1000 / 60);
        if (minuteDif < 0) {
            //tarde
            driverMsg += Math.abs(minuteDif) + " minutos tarde";
        } else {
            //temprano
            driverMsg += minuteDif + " minutos temprano";
        }

        sendMessage(driverId, "Punto de Control", driverMsg, driverMsg, conn);

        List<Integer> admins = getAdminList(conn);
        for (int i = 0; i < admins.size(); i++) {
            Integer id = admins.get(i);
            String msg = "El viaje '" + routeName + "' de " + driverName + " en el vehículo " + plate + " " + driverMsg;
            sendMessage(id, "Punto de Control", msg, msg, conn);
        }
    }

    private static void notifyMissingArrival(int checkId, int tripId, Connection conn, Date exp, int dif) throws Exception {

        if (new MySQLQuery("SELECT COUNT(*)>0 FROM mto_trip_novelty WHERE trip_id = ?1").setParam(1, tripId).getAsBoolean(conn)) {
            return;
        }

        Object[] tripRow = new MySQLQuery("SELECT "
                + "e.id, "//1
                + "r.name, "//2
                + "v.plate, "//3
                + "CONCAT(e.first_name, ' ', e.last_name) "//4
                + "FROM mto_trip t "
                + "INNER JOIN mto_route r ON r.id = t.route_id "
                + "INNER JOIN employee e ON e.id = t.driver_id "
                + "INNER JOIN vehicle v ON v.id = t.veh_id "
                + "WHERE t.id = ?1").setParam(1, tripId).getRecord(conn);

        int driverId = MySQLQuery.getAsInteger(tripRow[0]);
        String routeName = MySQLQuery.getAsString(tripRow[1]);
        String plate = MySQLQuery.getAsString(tripRow[2]);
        String driverName = MySQLQuery.getAsString(tripRow[3]);

        String ptName = new MySQLQuery("SELECT p.name FROM "
                + "mto_trip_check c "
                + "INNER JOIN mto_route_point p ON p.id = c.point_id "
                + "WHERE c.id = ?1;").setParam(1, checkId).getAsString(conn);

        SimpleDateFormat hf = new SimpleDateFormat("hh:mm a");
        String driverMsg = "Debía pasar por el punto de control '" + ptName + "' a las " + hf.format(exp) + ", lleva " + dif + " minutos de retraso";
        sendMessage(driverId, "Punto de Control", driverMsg, driverMsg, conn);

        List<Integer> admins = getAdminList(conn);
        for (int i = 0; i < admins.size(); i++) {
            Integer id = admins.get(i);
            String msg = "El viaje '" + routeName + "' de " + driverName + " en el vehículo " + plate + " " + driverMsg;
            sendMessage(id, "Atraso en Punto de Control", msg, msg, conn);
        }
        new MySQLQuery("UPDATE mto_trip_check SET last_notif = NOW() WHERE id = ?1").setParam(1, checkId).executeUpdate(conn);
    }

    public static void notifyNovelty(MtoTripNovelty n, Connection conn) throws Exception {
        Date now = MySQLQuery.now(conn);
        Object[] tripRow = new MySQLQuery("SELECT "
                + "r.name, "
                + "v.plate, "
                + "CONCAT(e.first_name, ' ', e.last_name) "
                + "FROM mto_trip t "
                + "INNER JOIN mto_route r ON r.id = t.route_id "
                + "INNER JOIN vehicle v ON v.id = t.veh_id "
                + "INNER JOIN employee e ON e.id = t.driver_id "
                + "WHERE t.id = ?1").setParam(1, n.tripId).getRecord(conn);

        String routeName = MySQLQuery.getAsString(tripRow[0]);
        String plate = MySQLQuery.getAsString(tripRow[1]);
        String driverName = MySQLQuery.getAsString(tripRow[2]);

        SimpleDateFormat hf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");

        MtoNoveltyType t = new MtoNoveltyType().select(n.typeId, conn);

        String msg = "El " + hf.format(now) + " el viaje '" + routeName + "' de " + driverName + " en el vehículo " + plate + " reportó una novedad de tipo '" + t.name + "' con las siguientes observaciones: " + n.notes;

        List<Integer> admins = getAdminList(conn);
        for (int i = 0; i < admins.size(); i++) {
            Integer id = admins.get(i);
            sendMessage(id, "Novedad", msg, msg, conn);
        }
    }

    public static void sendMessage(int empId, String subject, String brief, String msg, Connection conn) throws Exception {
        Integer appId = new MySQLQuery("SELECT id FROM system_app WHERE package_name = 'qualisys.chklist'").getAsInteger(conn);
        boolean sendNow;
        double hour = new MySQLQuery("SELECT (hour(NOW())*60+MINUTE(NOW()))/60").getAsDouble(conn);
        if (MtoTrip.isTripAdmin(empId, conn)) {
            sendNow = hour >= 7 && hour <= 19;
        } else {
            sendNow = true;
        }

        if (sendNow) {
            JsonObject json = getJsonNotification(subject, brief, msg, new MySQLQuery("SELECT NOW()").getAsDate(conn));
            GCMUtils.sendToApp(appId, json, empId + "", conn);
        } else {
            MtoTripDelayedMsg d = new MtoTripDelayedMsg();
            d.brief = brief;
            d.empId = empId;
            d.event = new ServerNow();
            d.msg = msg;
            d.subject = subject;

            if (hour > 19) {
                //dia siguiente
                d.sched = new MySQLQuery("SELECT DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 1 DAY), INTERVAL 7 HOUR)").getAsDate(conn);
            } else {
                //mismo día
                d.sched = new MySQLQuery("SELECT DATE_ADD(CURDATE(), INTERVAL 7 HOUR)").getAsDate(conn);
            }
            d.insert(conn);
        }
    }

    public static List<Integer> getAdminList(Connection conn) throws Exception {
        List<Integer> rta = new ArrayList<>();
        Object[][] dataAdminIds = new MySQLQuery("SELECT l.employee_id "
                + "FROM profile p "
                + "INNER JOIN login l ON l.profile_id = p.id "
                + "INNER JOIN mto_prof_cfg cfg ON cfg.prof_id = p.id AND cfg.app_view_all_trips "
                + "WHERE p.active AND p.is_mobile = TRUE AND p.menu_id = " + MtoProfCfg.MODULE_ID + "  ").getRecords(conn);

        if (dataAdminIds != null) {
            for (Object[] dataAdminId : dataAdminIds) {
                rta.add(MySQLQuery.getAsInteger(dataAdminId[0]));
            }
        }
        return rta;
    }

    private static JsonObject getJsonNotification(String subject, String brief, String message, Date evDate) {
        JsonObjectBuilder ob = Json.createObjectBuilder();
        SimpleDateFormat df = Dates.getDateTimeFormat();
        ob.add("dt", df.format(evDate));
        ob.add("subject", subject);
        ob.add("brief", brief);
        ob.add("message", message);
        ob.add("user", "Sistema");
        return ob.build();
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }
            return (dist);
        }
    }

}
