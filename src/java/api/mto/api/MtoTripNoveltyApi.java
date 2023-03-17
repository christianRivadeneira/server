package api.mto.api;

import api.BaseAPI;
import api.mto.dto.MtoTripNoveltyAux;
import api.mto.model.MtoTripNovelty;
import api.mto.tasks.MtoTripTasks;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

@Path("/mtoTripNovelty")
public class MtoTripNoveltyApi extends BaseAPI {

    @POST
    public Response insert(MtoTripNovelty obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.empId = sl.employeeId;
            obj.regDt = new Date();
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            MtoTripTasks.notifyNovelty(obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(MtoTripNovelty obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MtoTripNovelty old = new MtoTripNovelty().select(obj.id, conn);
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MtoTripNovelty obj = new MtoTripNovelty().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MtoTripNovelty.delete(id, conn);
            SysCrudLog.deleted(this, MtoTripNovelty.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/all")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(MtoTripNovelty.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getNovelties")
    public Response getNovelties(@QueryParam("tripId") int tripId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            String query = "SELECT"
                    + " n.notes, "
                    + " t.name, "
                    + " n.reg_dt, "
                    + " CONCAT(e.first_name,' ',e.last_name) "
                    + " FROM mto_trip_novelty n "
                    + " INNER JOIN mto_novelty_type t ON t.id = n.type_id  "
                    + " INNER JOIN employee e ON e.id = n.emp_id "
                    + " WHERE n.trip_id = ?1 "
                    + " ORDER BY n.id asc ";

            Object data[][] = new MySQLQuery(query).setParam(1, tripId).getRecords(conn);
            List<MtoTripNoveltyAux> trips = new ArrayList<>();
            for (Object obj[] : data) {
                MtoTripNoveltyAux info = new MtoTripNoveltyAux();
                info.text = String.valueOf(obj[0]);
                info.type = String.valueOf(obj[1]);                
                String date = Dates.getDateTimeFormat().format(MySQLQuery.getAsDate(obj[2]));                
                info.date = date;
                info.employee = String.valueOf(obj[3]);
                trips.add(info);
            }
            return createResponse(trips);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
