package api.gt.api;

import api.BaseAPI;
import api.gt.dto.GtTripDataTank;
import api.gt.model.GtCenter;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import api.gt.model.GtGlpTrip;
import api.sys.model.Employee;
import api.sys.model.Enterprise;
import java.util.Date;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import web.gates.GtTripType;

@Path("/gtGlpTrip")
public class GtGlpTripApi extends BaseAPI {

    @POST
    public Response insert(GtGlpTrip obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            if (obj.vhId != null) {//Empresa
                String str = "SELECT COUNT(*)>0 "
                        + "FROM gt_glp_trip AS t "//0
                        + "WHERE t.vh_id = " + obj.vhId + " AND t.type_id = " + obj.typeId + " "
                        + "AND cancel = FALSE "//2
                        + "AND t.steps <> t.req_steps";

                if (new MySQLQuery(str).getAsBoolean(conn)) {
                    String plate = new MySQLQuery("SELECT plate FROM vehicle WHERE id = " + obj.vhId).getAsString(conn);
                    throw new Exception("Existen viajes de " + plate + " sin terminar");
                }
            }

            obj.tripDate = new Date();
            obj.id = obj.insert(conn);

            new MySQLQuery("INSERT INTO gt_glp_trip_log SET "
                    + "`trip_id` = " + obj.id + ", "
                    + "`employee_id` = " + sl.employeeId + ", "
                    + "`log_date` = NOW(), "
                    + "`type` = 'new', "
                    + "`notes` = '" + obj.logNotes + "', "
                    + "`glp_inv_id` = NULL").executeInsert(conn);

            //SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(GtGlpTrip obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            //GtGlpTrip old = new GtGlpTrip().select(obj.id, conn);
            obj.update(conn);

            new MySQLQuery("INSERT INTO gt_glp_trip_log SET "
                    + "`trip_id` = " + obj.id + ", "
                    + "`employee_id` = " + sl.employeeId + ", "
                    + "`log_date` = NOW(), "
                    + "`type` = 'edit', "
                    + "`notes` = '" + obj.logNotes + "', "
                    + "`glp_inv_id` = NULL").executeInsert(conn);

            //SysCrudLog.updated(this, obj, old, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GtGlpTrip obj = new GtGlpTrip().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GtGlpTrip.delete(id, conn);
            SysCrudLog.deleted(this, GtGlpTrip.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getAll")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(GtGlpTrip.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/readTripTank")
    public Response readTripTank(@QueryParam("tripId") Integer tripId, @QueryParam("typeTanker") boolean typeTanker, @QueryParam("vhId") Integer vhId, @QueryParam("isLoad") boolean isLoad, @QueryParam("isPlatf") boolean isPlatf) {
        GtTripDataTank obj = new GtTripDataTank();
        try (Connection conn = getConnection()) {
            getSession(conn);
            if (tripId != null) {

                obj.trip = new GtGlpTrip().select(tripId, conn);

                if (obj.trip.driverId != null) {
                    obj.drv = new Employee().select(obj.trip.driverId, conn);
                }

                obj.ent = new Enterprise().select(obj.trip.enterpriseId, conn);
                obj.type = new GtTripType().select(obj.trip.typeId, conn);
                if (!obj.type.sameCenter) {
                    obj.cent = new GtCenter().select(obj.trip.centerOrigId, conn);
                }
            } else {
                if (typeTanker) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("SELECT t.type_id ")
                            .append("FROM gt_glp_trip AS t ")
                            .append("INNER JOIN gt_trip_type AS tt ON tt.id = t.type_id ")
                            .append("WHERE t.vh_id = ")
                            .append(vhId)
                            .append(" AND tt.type = 't' ");
                    Integer defaultType = null;
                    if (isLoad) {
                        if (isPlatf) {
                            sb.append("AND active = 1 AND c = 1  ");
                            defaultType = 148;// traslado t
                        } else {
                            sb.append("AND active = 1 AND s = 1 AND c = 0 ");
                            // defalutType = pendiente...
                        }
                    } else if (!isLoad) {
                        if (!isPlatf) {
                            sb.append("AND active = 1 AND e = 1 AND c = 0  ");
                            defaultType = 149;// compra t
                        }
                    }
                    sb.append("AND t.trip_date >= DATE_SUB(NOW(),INTERVAL 15 DAY) AND t.cancel = 0 ");
                    sb.append("GROUP BY t.type_id ORDER BY COUNT(*) DESC LIMIT 0,1");
                    Integer typeId = new MySQLQuery(sb.toString()).getAsInteger(conn);// type
                    if (typeId == null) {
                        typeId = defaultType;
                    }

                    Integer entId = new MySQLQuery("SELECT enterprise_id FROM gt_glp_trip WHERE vh_id = " + vhId + " AND trip_date >= DATE_SUB(NOW(),INTERVAL 15 DAY) AND cancel = 0 GROUP BY enterprise_id ORDER BY count(*) desc limit 0,1").getAsInteger(conn);// enterprise
                    if (entId == null) {
                        entId = 3;// montagas
                    }

                    Integer centerDestId = new MySQLQuery("SELECT center_dest_id FROM gt_glp_trip WHERE vh_id = " + vhId + " AND trip_date >= DATE_SUB(NOW(),INTERVAL 15 DAY) AND cancel = 0 GROUP BY center_dest_id ORDER BY count(*) desc limit 0,1").getAsInteger(conn);// center_dest
                    if (centerDestId == null) {
                        centerDestId = 7;//pasto
                    }

                    Integer lastDrvId = new MySQLQuery("SELECT driver_id FROM gt_glp_trip WHERE vh_id = " + vhId + " AND trip_date >= DATE_SUB(NOW(),INTERVAL 15 DAY) AND cancel = 0 GROUP BY driver_id ORDER BY count(*) desc limit 0,1").getAsInteger(conn);// driver_id
                    Integer mtoDrvId = new MySQLQuery("SELECT driver_id FROM driver_vehicle AS dv WHERE dv.vehicle_id = " + vhId + " AND dv.`end` IS NULL").getAsInteger(conn);// driver_id
                    Integer drvId = (lastDrvId != null ? lastDrvId : mtoDrvId);

                    Double lastCapaFull = new MySQLQuery("SELECT IFNULL(v.gal_cap, t.capa_full) "
                            + "FROM gt_glp_trip t "
                            + "INNER JOIN vehicle v ON t.vh_id = v.id "
                            + "WHERE t.vh_id = " + vhId + " "
                            + "AND t.trip_date >= DATE_SUB(NOW(),INTERVAL 15 DAY) AND t.cancel = 0 "
                            + "GROUP BY t.capa_full HAVING t.capa_full > 0 ORDER BY count(*) ASC limit 0,1").getAsDouble(conn);// capafull
                    String lastRemol = new MySQLQuery("SELECT rem_plate FROM gt_glp_trip WHERE vh_id = " + vhId + " AND trip_date >= DATE_SUB(NOW(),INTERVAL 15 DAY) AND cancel = 0 GROUP BY rem_plate ORDER BY count(*) ASC limit 0,1").getAsString(conn);// remolq

                    obj.ent = new Enterprise().select(entId, conn);
                    obj.type = typeId != null ? new GtTripType().select(typeId, conn) : null;
                    if (obj.type != null) {
                        if (!obj.type.sameCenter) {
                            obj.cent = new GtCenter().select(centerDestId, conn);
                        }
                    }
                    if (drvId != null) {
                        obj.drv = new Employee().select(drvId, conn);
                    }

                    obj.trip = new GtGlpTrip();
                    if (lastCapaFull != null) {
                        obj.trip.capaFull = MySQLQuery.getAsBigDecimal(lastCapaFull, false);
                    }
                    if (lastRemol != null) {
                        obj.trip.remPlate = lastRemol;
                    }
                }
            }

            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
