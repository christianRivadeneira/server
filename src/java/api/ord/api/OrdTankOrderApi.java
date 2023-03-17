package api.ord.api;

import api.BaseAPI;
import api.est.model.EstLog;
import api.est.model.EstProg;
import api.est.model.EstProgSede;
import api.ord.model.OrdTankClient;
import api.ord.model.OrdTankOrder;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import web.tanks.EstScheduleServlet;

@Path("/ordTankOrder")
public class OrdTankOrderApi extends BaseAPI {

    @POST
    public Response insert(OrdTankOrder obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            Response response = newOrder(obj, conn);
            return response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("insertOrder")
    public Response insertOrder(OrdTankOrder obj) {
        try (Connection conn = getConnection()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (obj.vehicleId == null) {
                throw new Exception("Seleccione un vehículo");
            }

            if (obj.driverId == null) {
                throw new Exception("El vehículo seleccionado no tiene asignado un conductor");
            }

            if (obj.tankClientId == null) {
                throw new Exception("El cliente seleccionado no esta asociado a un estacionario");
            }

            if (obj.officeId == null) {
                throw new Exception("La ciudad del cliente no esta asociada a una oficina");
            }
            
            Date date = obj.day;
            int curNumberWeek = Dates.curWeekOfYear();
            int week = Dates.weekMonth(curNumberWeek);
            EstProg.validateSchedule(obj.tankClientId, week, EstProg.getWeekday(date), new SimpleDateFormat("yyyy-MM-dd").format(date), conn);

            obj.enterpriseId = 3; // lo coloque fijo para no fectar la interfaz de la app la mayoria sino todos los pedidos son de montagas:: Danny 
            obj.insert(conn);
            OrdTankClient client = new OrdTankClient().select(obj.tankClientId, conn);

            programEst(obj, client, obj.vehicleId, obj.driverId, obj.assigById, conn);

            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(OrdTankOrder obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.update(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            OrdTankOrder obj = new OrdTankOrder().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            OrdTankOrder.delete(id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getAll")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(OrdTankOrder.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public synchronized Response newOrder(OrdTankOrder obj, Connection con) throws Exception {
        boolean existOrder = new MySQLQuery("SELECT "
                + "(SELECT COUNT(*) "
                + "FROM est_schedule s "
                + "WHERE s.visit_date = CURDATE() "
                + "AND s.clie_tank_id = " + obj.tankClientId + ")"
                + "+ "
                + "(SELECT COUNT(*) "
                + "FROM est_prog_sede ps "
                + "INNER JOIN est_prog p ON ps.prog_id = p.id "
                + "WHERE ps.tank_client_id = " + obj.tankClientId + " "
                + "AND p.prog_date = CURDATE()) > 0").getAsBoolean(con);
        if (existOrder) {
            String sman = new MySQLQuery("SELECT "
                    + "CONCAT(e.first_name, ' ', e.last_name, ': ', v.plate, ' - ', v.internal) "
                    + "FROM vehicle v "
                    + "INNER JOIN driver_vehicle dv ON dv.vehicle_id = v.id "
                    + "INNER JOIN employee e ON dv.driver_id = e.id "
                    + "WHERE dv.`end` IS NULL "
                    + "AND v.id = COALESCE((SELECT s.vh_id "
                    + "FROM est_schedule s "
                    + "WHERE s.visit_date = CURDATE() "
                    + "AND s.clie_tank_id = " + obj.tankClientId + "), "
                    + "(SELECT p.vh_id "
                    + "FROM est_prog_sede ps "
                    + "INNER JOIN est_prog p ON ps.prog_id = p.id "
                    + "WHERE ps.tank_client_id = " + obj.tankClientId + " "
                    + "AND p.prog_date = CURDATE()))").getAsString(con);
            throw new Exception("El cliente ya tiene una visita programada para hoy." + (sman != null ? "\nVendedor: " + sman : ""));
        }

        obj.insert(con);
        return Response.ok(obj).build();

    }

    public static void programEst(OrdTankOrder order, OrdTankClient client, int vehicleId, int driverId, int empId, Connection ep) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String format = formatter.format(order.day);

        EstProgSede progSede = new EstProgSede();
        Integer progOcas = new MySQLQuery("SELECT id FROM est_prog WHERE "
                + "prog_date = ?1 "
                + "AND end_date = ?2 "
                + "AND vh_id = ?3 AND path_id IS NULL")
                .setParam(1, format)
                .setParam(2, format)
                .setParam(3, vehicleId).getAsInteger(ep);
        if (progOcas == null) {
            EstProg prog = new EstProg();
            prog.endDate = order.day;
            prog.progDate = order.day;
            prog.vhId = vehicleId;
            prog.emploId = empId;
            progOcas = prog.insert(ep);
        }
        progSede.progId = progOcas;
        progSede.tankClientId = client.id;
        progSede.orderTankId = order.id;
        EstLog.createLog(progSede.progId, EstLog.EST_PROG, "Se Adiciono:\\n" + client.name, empId, ep);
        progSede.insert(ep);

        EstScheduleServlet.insertScheduledPath(ep, format, vehicleId);

    }
}
