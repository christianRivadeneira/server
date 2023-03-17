package api.mto.api;

import api.BaseAPI;
import api.mto.dto.PointItem;
import api.mto.dto.RouteChangeRequestDto;
import api.mto.dto.TripInfo;
import api.mto.dto.TripItem;
import api.mto.dto.TripPoints;
import api.mto.model.EmployeeTripsInfoDto;
import api.mto.model.MtoCfg;
import api.mto.model.MtoRoute;
import api.mto.model.MtoRoutePoint;
import api.mto.model.MtoTrip;
import api.mto.model.MtoTripCheck;
import api.mto.tasks.MtoTripTasks;
import api.sys.model.Employee;
import api.sys.model.SysCrudLog;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.Dates;
import utilities.MySQLQuery;
import utilities.ServerNow;
import utilities.apiClient.DateWrapper;
import web.maintenance.MaintenanceTask;

@Path("/mtoTrip")
public class MtoTripApi extends BaseAPI {

    public static final String GOING_START = "going_start";
    public static final String GOING = "going";
    public static final String COMMING_START = "comming_start";
    public static final String COMMING = "comming";
    public static final String END = "end";

    @POST
    public Response insert(MtoTrip obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.employeeId = sl.employeeId;
            obj.expGals = new MySQLQuery("SELECT gals FROM mto_exp_fuel WHERE vh_id = " + obj.vehId + " AND route_id = " + obj.routeId).getAsBigDecimal(conn, true);
            obj.price = MtoTrip.getPrice(obj.routeId, obj.gals, obj.tripDate, conn);

            if (obj.price == null && (obj.gals != null && obj.gals.compareTo(BigDecimal.ZERO) != 0)) {
                throw new Exception("Debe definir el precio para la ruta");
            }

            if (MtoRoute.hasPoints(obj.routeId, conn)) {
                obj.tripDate = Dates.trimDate(obj.expDeparture);
                MtoRoute.validateRoutePoints(obj.routeId, conn);

                if (new MySQLQuery("select TIMESTAMPDIFF(MINUTE, NOW(), ?1) >= 10").setParam(1, obj.expDeparture).getAsBoolean(conn)) {
                    int id = obj.insert(conn);
                    List<MtoRoutePoint> pts = MtoRoutePoint.getByRoute(obj.routeId, conn);
                    for (int i = 0; i < pts.size(); i++) {
                        MtoTripCheck check = new MtoTripCheck();
                        check.tripId = id;
                        check.type = pts.get(i).type;
                        check.pointId = pts.get(i).id;
                        check.place = pts.get(i).place;
                        if (pts.get(i).type.equals("going_start")) {
                            check.expFull = obj.expDeparture;
                        }
                        check.insert(conn);
                    }
                } else {
                    throw new Exception("La fecha de salida no puede ser la fecha actual");
                }
            } else {
                obj.insert(conn);
            }

            MaintenanceTask.generatePrevPlan(obj.vehId, conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(MtoTrip obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MtoTrip old = new MtoTrip().select(obj.id, conn);
            if (!Objects.equals(obj.routeId, old.routeId)) {
                boolean ptsOld = MtoRoute.hasPoints(old.routeId, conn);
                boolean ptsNew = MtoRoute.hasPoints(obj.routeId, conn);
                if (ptsNew) {
                    throw new Exception("La nueva ruta tiene puntos.\nDebe usar la herramienta de cambio de ruta.");
                } else if (ptsOld && !ptsNew) {
                    new MySQLQuery("DELETE FROM mto_trip_check  WHERE trip_id = ?1").setParam(1, old.id).executeDelete(conn);
                }
            }

            if (MtoRoute.hasPoints(obj.routeId, conn)) {
                if (!Objects.equals(old.expDeparture, obj.expDeparture)) {
                    Boolean started = new MySQLQuery("SELECT reg IS NOT NULL FROM mto_trip_check c WHERE c.`type` = ?2 AND trip_id = ?1").setParam(1, obj.id).setParam(2, GOING_START).getAsBoolean(conn);
                    if (started != null && started) {
                        throw new Exception("No se puede cambiar la fecha de un viaje que ya ha iniciado.");
                    } else {
                        new MySQLQuery("UPDATE mto_trip_check c SET c.exp_full = ?1 WHERE trip_id = ?2 AND c.`type` = ?3").setParam(1, obj.expDeparture).setParam(2, obj.id).setParam(3, GOING_START).executeUpdate(conn);
                    }
                }
            }

            obj.expGals = new MySQLQuery("SELECT gals FROM mto_exp_fuel WHERE vh_id = " + obj.vehId + " AND route_id = " + obj.routeId).getAsBigDecimal(conn, true);
            obj.price = MtoTrip.getPrice(obj.routeId, obj.gals, obj.tripDate, conn);
            obj.update(conn);
            MaintenanceTask.generatePrevPlan(obj.vehId, conn);
            SysCrudLog.updated(this, obj, old, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MtoTrip obj = new MtoTrip().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/routeChanger")
    public Response routeChanger(RouteChangeRequestDto req) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            MtoTrip t = new MtoTrip().select(req.tripId, conn);
            t.routeId = req.routeId;
            MtoRoute route = new MtoRoute().select(req.routeId, conn);
            t.len = route.len;
            t.expGals = new MySQLQuery("SELECT gals FROM mto_exp_fuel WHERE vh_id = " + t.vehId + " AND route_id = " + req.routeId).getAsBigDecimal(conn, true);
            t.price = MtoTrip.getPrice(req.routeId, t.gals, t.tripDate, conn);

            if (t.price == null && (t.gals != null && t.gals.compareTo(BigDecimal.ZERO) != 0)) {
                throw new Exception("Debe definir el precio para la ruta");
            }
            t.update(conn);

            new MySQLQuery("DELETE FROM mto_trip_check WHERE trip_id = ?1").setParam(1, req.tripId).executeDelete(conn);

            MtoRoutePoint chk = new MtoRoutePoint().select(req.pointId, conn);
            List<MtoRoutePoint> pts = MtoRoutePoint.getByRoute(req.routeId, conn);
            MtoRoutePoint last = pts.get(pts.size() - 1);
            if (last.id != req.pointId) {
                for (int i = 0; i < pts.size(); i++) {
                    MtoRoutePoint p = pts.get(i);
                    if (p.place >= chk.place) {
                        MtoTripCheck c = new MtoTripCheck();
                        if (p.place == chk.place) {
                            c.expFull = req.expDate;
                        } else {
                            c.expFull = null;
                        }
                        c.expPart = null;
                        c.place = p.place;
                        c.pointId = p.id;
                        c.reg = null;
                        c.tripId = req.tripId;
                        c.type = p.type;
                        c.insert(conn);
                    }
                }
            }
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MtoTrip obj = new MtoTrip().select(id, conn);
            MtoTrip.delete(id, conn);
            MaintenanceTask.generatePrevPlan(obj.vehId, conn);
            SysCrudLog.deleted(this, MtoTrip.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/open")
    public Response getOpen() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MtoCfg cfg = new MtoCfg().select(1, conn);
            Date now = new MySQLQuery("SELECT NOW()").getAsDate(conn);
            MySQLQuery mysqlQuery = new MySQLQuery("SELECT "
                    + "t.id, "
                    + "r.name, "
                    + "CONCAT(d.first_name, ' ', d.last_name), "
                    + "v.plate, "
                    + "s.reg, "
                    + "e.reg, "
                    + "(SELECT MAX(place) FROM mto_trip_check WHERE trip_id = t.id AND reg IS NOT NULL) as place "
                    + "FROM "
                    + "mto_trip t "
                    + "INNER JOIN employee d ON d.id = t.driver_id "
                    + "INNER JOIN vehicle v ON v.id = t.veh_id "
                    + "INNER JOIN mto_route r ON r.id = t.route_id "
                    + "INNER JOIN mto_trip_check s ON t.id = s.trip_id AND s.`type` = 'going_start' "
                    + "INNER JOIN mto_trip_check e ON t.id = e.trip_id AND e.`type` = 'end' AND e.reg IS NULL "
                    + "WHERE t.canceled = 0 GROUP BY t.id");

            Object tripsData[][] = mysqlQuery.getRecords(conn);

            List<TripItem> trips = new ArrayList<>();
            for (Object tripRow[] : tripsData) {
                TripItem info = new TripItem();
                info.id = MySQLQuery.getAsInteger(tripRow[0]);
                info.route = MySQLQuery.getAsString(tripRow[1]);
                info.driver = MySQLQuery.getAsString(tripRow[2]);
                info.plate = MySQLQuery.getAsString(tripRow[3]);
                info.start = MySQLQuery.getAsDate(tripRow[4]);
                info.end = MySQLQuery.getAsDate(tripRow[5]);
                Integer place = MySQLQuery.getAsInteger(tripRow[6]);

                if (place != null) {
                    Object chkRow[] = new MySQLQuery("SELECT "
                            + " r.name, "//0
                            + " c.`type`,"//1
                            + " c.exp_full, "//2
                            + " c.exp_part, "//3
                            + " c.reg "//4
                            + " FROM mto_trip t "
                            + " INNER JOIN mto_trip_check c ON c.trip_id = t.id "
                            + " INNER JOIN mto_route_point r ON r.id = c.point_id "
                            + " WHERE t.id = ?1 AND c.place = ?2"
                            + " ORDER BY r.place ").setParam(1, info.id).setParam(2, place).getRecord(conn);

                    String name = MySQLQuery.getAsString(chkRow[0]);
                    String type = MySQLQuery.getAsString(chkRow[1]);
                    Date expFull = MySQLQuery.getAsDate(chkRow[2]);
                    Date expPart = MySQLQuery.getAsDate(chkRow[3]);
                    Date reg = MySQLQuery.getAsDate(chkRow[4]);
                    info.lastPoint = new PointItem(name, type, expFull, expPart, reg, now, cfg.routeChkTolerance);
                } else {
                    info.lastPoint = null;
                }

                trips.add(info);
            }
            return createResponse(trips);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/closed")
    public Response getClosed(DateWrapper date) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Date now = new MySQLQuery("SELECT NOW()").getAsDate(conn);
            MtoCfg cfg = new MtoCfg().select(1, conn);
            MySQLQuery mysqlQuery = new MySQLQuery("SELECT "
                    + "t.id, "
                    + "r.name, "
                    + "CONCAT(d.first_name, ' ', d.last_name), "
                    + "v.plate, "
                    + "s.reg, "
                    + "e.reg, "
                    + "(SELECT MAX(place) FROM mto_trip_check WHERE trip_id = t.id AND reg IS NOT NULL) "
                    + "FROM "
                    + "mto_trip t "
                    + "INNER JOIN employee d ON d.id = t.driver_id "
                    + "INNER JOIN vehicle v ON v.id = t.veh_id "
                    + "INNER JOIN mto_route r ON r.id = t.route_id "
                    + "INNER JOIN mto_trip_check s ON t.id = s.trip_id AND s.`type` = 'going_start' "
                    + "INNER JOIN mto_trip_check e ON t.id = e.trip_id AND e.`type` = 'end' AND e.reg IS NOT NULL "
                    + "WHERE DATE(e.reg) = DATE(?1) AND t.canceled = 0 GROUP BY t.id ").setParam(1, date.date);

            Object tripsData[][] = mysqlQuery.getRecords(conn);

            List<TripItem> trips = new ArrayList<>();
            for (Object tripRow[] : tripsData) {
                TripItem info = new TripItem();
                info.id = MySQLQuery.getAsInteger(tripRow[0]);
                info.route = MySQLQuery.getAsString(tripRow[1]);
                info.driver = MySQLQuery.getAsString(tripRow[2]);
                info.plate = MySQLQuery.getAsString(tripRow[3]);
                info.start = MySQLQuery.getAsDate(tripRow[4]);
                info.end = MySQLQuery.getAsDate(tripRow[5]);
                Integer place = MySQLQuery.getAsInteger(tripRow[6]);

                if (place != null) {
                    Object chkRow[] = new MySQLQuery("SELECT "
                            + " r.name, "//0
                            + " c.`type`,"//1
                            + " c.exp_full, "//2
                            + " c.exp_part, "//3
                            + " c.reg "//4
                            + " FROM mto_trip t "
                            + " INNER JOIN mto_trip_check c ON c.trip_id = t.id "
                            + " INNER JOIN mto_route_point r ON r.id = c.point_id "
                            + " WHERE t.id = ?1 AND c.place = ?2"
                            + " ORDER BY r.place ").setParam(1, info.id).setParam(2, place).getRecord(conn);

                    String name = MySQLQuery.getAsString(chkRow[0]);
                    String type = MySQLQuery.getAsString(chkRow[1]);
                    Date expFull = MySQLQuery.getAsDate(chkRow[2]);
                    Date expPart = MySQLQuery.getAsDate(chkRow[3]);
                    Date reg = MySQLQuery.getAsDate(chkRow[4]);
                    info.lastPoint = new PointItem(name, type, expFull, expPart, reg, now, cfg.routeChkTolerance);
                } else {
                    info.lastPoint = null;
                }

                trips.add(info);
            }
            return createResponse(trips);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/tripPointsInfo")
    public Response getTripPointsInfo(@QueryParam("tripId") int tripId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(getTripPoints(tripId, conn, getTimeZone()));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public static Date getExpected(Date expPart, Date expFull) {
        if (expPart != null) {
            return expFull.compareTo(expPart) > 0 ? expPart : expFull;
        } else {
            return expFull;
        }
    }

    private static TripPoints getTripPoints(int tripId, Connection conn, TimeZone timeZone) throws Exception {
        TripPoints tripPoints = new TripPoints();
        tripPoints.tripLabel = new MySQLQuery("SELECT r.name "
                + " FROM mto_trip t "
                + " INNER JOIN mto_route r ON r.id = t.route_id "
                + " WHERE t.id = ?1 ").setParam(1, tripId).getAsString(conn);

        tripPoints.started = new MySQLQuery("SELECT c.reg IS NOT NULL "
                + "FROM mto_trip t "
                + "INNER JOIN mto_trip_check c ON c.trip_id = t.id AND c.`type` = 'going_start' "
                + "WHERE t.id = ?1 ").setParam(1, tripId).getAsBoolean(conn);

        Object dataPoints[][] = new MySQLQuery("SELECT "
                + " r.name, "//0
                + " c.`type`,"//1
                + " c.exp_full, "//2
                + " c.exp_part, "//3
                + " c.reg "//4
                + " FROM mto_trip t "
                + " INNER JOIN mto_trip_check c ON c.trip_id = t.id "
                + " INNER JOIN mto_route_point r ON r.id = c.point_id "
                + " WHERE t.id = ?1 "
                + " ORDER BY r.place ").setParam(1, tripId).getRecords(conn);

        tripPoints.points = new ArrayList<>();

        Integer tolerance = new MySQLQuery("SELECT route_chk_tolerance FROM mto_cfg").getAsInteger(conn);
        if (tolerance == null) {
            throw new Exception("No se ha configurado la tolerancia");
        }

        Date now = new MySQLQuery("SELECT NOW()").getAsDate(conn);

        for (Object[] row : dataPoints) {
            String name = MySQLQuery.getAsString(row[0]);
            String type = MySQLQuery.getAsString(row[1]);
            Date expFull = MySQLQuery.getAsDate(row[2]);
            Date expPart = MySQLQuery.getAsDate(row[3]);
            Date reg = MySQLQuery.getAsDate(row[4]);
            tripPoints.points.add(new PointItem(name, type, expFull, expPart, reg, now, tolerance));
        }
        return tripPoints;
    }

    @GET
    @Path("/tripInfo")
    public Response getTripInfo(@QueryParam("empId") int empId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            boolean isAdmin = MtoTrip.isTripAdmin(empId, conn);
            TripInfo obj = new TripInfo();
            if (!isAdmin) {
                //El conductor puede tener más de un viaje asignado, pero solo ver uno a la vez
                Object[] record = new MySQLQuery("SELECT t.id, r.name "
                        + "FROM mto_trip t "
                        + "INNER JOIN mto_trip_check c ON t.id = c.trip_id AND c.reg IS NULL AND c.type = 'end'  "
                        + "INNER JOIN mto_route r ON r.id = t.route_id "
                        + "WHERE t.driver_id = ?1 AND t.canceled = 0 "
                        + "ORDER BY t.exp_departure ASC LIMIT 1").setParam(1, empId).getRecord(conn);
                if (record != null) {
                    obj.id = MySQLQuery.getAsInteger(record[0]);
                    obj.routeName = MySQLQuery.getAsString(record[1]);
                    if (obj.id != null) {
                        obj.started = new MySQLQuery("SELECT c.reg IS NOT NULL "
                                + "FROM mto_trip t "
                                + "INNER JOIN mto_trip_check c ON c.trip_id = t.id AND c.`type` = 'going_start' "
                                + "WHERE t.id = ?1 ").setParam(1, obj.id).getAsBoolean(conn);
                    }
                }
            } else {
                Integer countTrips = new MySQLQuery("SELECT COUNT(*) "
                        + "FROM mto_trip_check c "
                        + "INNER JOIN mto_trip t ON t.id = c.trip_id AND c.reg IS NULL AND c.type = 'end' "
                        + "INNER JOIN mto_route r ON r.id = t.route_id "
                        + "WHERE "
                        + "t.canceled = 0").setParam(1, empId).getAsInteger(conn);
                obj.countTrips = countTrips == null ? 0 : countTrips;
            }

            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private void startTrip(int tripId, boolean firstStart, Connection conn) throws Exception {
        if (firstStart) {
            if (new MySQLQuery("SELECT c.reg IS NOT NULL "
                    + " FROM mto_trip_check c "
                    + " WHERE c.`type` = 'going_start' AND c.trip_id = ?1;"
            ).setParam(1, tripId).getAsBoolean(conn)) {
                throw new Exception("El viaje ya fue iniciado");
            }
        } else {
            Integer commingPlace = new MySQLQuery("SELECT c.place FROM "
                    + "mto_trip_check c "
                    + "WHERE c.`type` = 'comming_start' AND c.trip_id = ?1;").setParam(1, tripId).getAsInteger(conn);

            if (new MySQLQuery("SELECT c.reg IS NULL FROM "
                    + "mto_trip_check c "
                    + "WHERE c.place = ?2 AND c.trip_id = ?1;").setParam(1, tripId).setParam(2, commingPlace - 1).getAsBoolean(conn)) {
                throw new Exception("Debe pasar por los puntos de control anteriores");
            }

            if (new MySQLQuery("SELECT c.exp_full IS NOT NULL FROM "
                    + "mto_trip_check c "
                    + "WHERE c.place = ?2 AND c.trip_id = ?1;").setParam(1, tripId).setParam(2, commingPlace + 1).getAsBoolean(conn)) {
                throw new Exception("Ya se inició el viaje de regreso");
            }
        }

        int checkPointId = new MySQLQuery("SELECT c.id "
                + " FROM mto_trip t  "
                + " INNER JOIN mto_trip_check c ON c.trip_id = t.id "
                + " INNER JOIN mto_route_point p ON p.id = c.point_id "
                + " WHERE p.`type` = ?2 AND t.id = ?1 ").setParam(1, tripId).setParam(2, firstStart ? "going_start" : "comming_start").getAsInteger(conn);

        MtoTripCheck chkStart = new MtoTripCheck().select(checkPointId, conn);
        chkStart.reg = new ServerNow();
        chkStart.update(conn);

        String types = firstStart ? "'going','comming_start'" : "'comming','end'";

        new MySQLQuery("UPDATE mto_trip_check c "
                + " INNER JOIN mto_route_point p ON c.point_id = p.id "
                + " SET c.exp_full = DATE_ADD(NOW(), INTERVAL (p.h_full*60) + p.m_full MINUTE) "
                + " WHERE c.trip_id = ?1 AND p.type IN (" + types + ") ").setParam(1, tripId).executeUpdate(conn);

        sendStartNotif(tripId, firstStart, conn);
    }

    @PUT
    @Path("/startTrip")
    public Response startTrip(@QueryParam("tripId") int tripId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            startTrip(tripId, true, conn);
            return Response.ok(getTripPoints(tripId, conn, getTimeZone())).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/saveCompliment")
    public Response saveCompliment(@QueryParam("tripId") int tripId, @QueryParam("compliment") String compliment, @QueryParam("guide") String guide) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            List<MtoTripCheck> checks = MtoTripCheck.getByTrip(tripId, conn);
            for (int i = 0; i < checks.size(); i++) {
                MtoTripCheck c = checks.get(i);
                if (c.type.equals(COMMING_START)) {
                    if (c.reg == null) {
                        throw new Exception("Aun no ha llegado a la almacenadora");
                    }
                    if (checks.get(i + 1).expFull != null) {
                        throw new Exception("El viaje ya está iniciado");
                    }
                }
            }

            MtoTrip trip = new MtoTrip().select(tripId, conn);
            trip.compliment = compliment;
            trip.guide = guide;
            trip.update(conn);
            startTrip(tripId, false, conn);
            return Response.ok().build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/employeeTripsInfo")
    public Response getUserType(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Employee obj = new Employee().select(id, conn);
            EmployeeTripsInfoDto info = new EmployeeTripsInfoDto();
            Boolean tripManager = new MySQLQuery("SELECT trips_manager FROM employee WHERE employee.id = " + id).getAsBoolean(conn);
            if (tripManager == null) {
                throw new Exception("El empleado no existe");
            }

            if (obj.driver && tripManager) {
                throw new Exception("No puede ser conductor y administrador");
            }
            if (obj.driver) {
                info.label = new MySQLQuery(""
                        + "SELECT r.name FROM "
                        + "mto_trip t "
                        + "INNER JOIN employee e ON e.id = t.driver_id "
                        + "INNER JOIN mto_route r ON r.id = r.route_id "
                        + "WHERE t.canceled = 0 AND e.id = " + id).getAsString(conn);

                info.label = info.label == null ? "Sin Viaje Programado" : info.label;
                info.type = "driver";
            } else if (tripManager) {
                int count = (new MySQLQuery("SELECT COUNT(*) "
                        + "FROM mto_trip t "
                        + "JOIN mto_route r ON t.route_id = r.id "
                        + "JOIN mto_route_point p ON r.id = p.route_id "
                        + "WHERE "
                        + "t.canceled = 0")).getAsInteger(conn);
                info.type = "admin";
                info.label = String.valueOf(count);
            } else {
                info.type = "none";
                info.label = "none";
            }
            return Response.ok(info).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private void sendStartNotif(int tripId, boolean going, Connection conn) throws Exception {

        List<Integer> admins = MtoTripTasks.getAdminList(conn);

        if (!admins.isEmpty()) {
            Object[] tripRow = new MySQLQuery("SELECT "
                    + "r.name, "
                    + "v.plate, "
                    + "CONCAT(e.first_name, ' ', e.last_name), "
                    + "cs.exp_full, "
                    + "p.name "
                    + "FROM mto_trip t "
                    + "INNER JOIN mto_route r ON r.id = t.route_id "
                    + "INNER JOIN employee e ON e.id = t.driver_id "
                    + "INNER JOIN vehicle v ON v.id = t.veh_id "
                    + "INNER JOIN mto_trip_check cs ON cs.trip_id = t.id AND cs.type = ?2 "
                    + "INNER JOIN mto_route_point p ON p.id = cs.point_id "
                    + "WHERE t.id = ?1").setParam(1, tripId).setParam(2, going ? GOING_START : COMMING_START).getRecord(conn);

            String route = MySQLQuery.getAsString(tripRow[0]);
            String plate = MySQLQuery.getAsString(tripRow[1]);
            String driver = MySQLQuery.getAsString(tripRow[2]);
            String ptName = MySQLQuery.getAsString(tripRow[4]);

            Date exp = MySQLQuery.getAsDate(tripRow[3]);
            SimpleDateFormat hf = new SimpleDateFormat("hh:mm a");

            StringBuilder sb = new StringBuilder();
            sb.append("El viaje '").append(route).append("' de ").append(driver).append(" en el vehículo ").append(plate).append(" programado para las ").append(hf.format(exp)).append(", acaba de salir de '").append(ptName).append("'.");
            String msg = sb.toString();

            for (Integer adminId : admins) {
                MtoTripTasks.sendMessage(adminId, "Inicio de Viaje", msg, msg, conn);
            }
        }
    }
}
