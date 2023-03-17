package api.trk.api;

import api.BaseAPI;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import api.trk.model.TrkCylNovelty;
import java.util.Date;
import utilities.Dates;
import utilities.MySQLQuery;

@Path("/trkCylNovelty")
public class TrkCylNoveltyApi extends BaseAPI {

    @POST
    public Response insert(TrkCylNovelty obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            obj.dt = new Date();
            obj.empId = sl.employeeId;
            if (obj.pqrNum != null) {
                obj.pqrId = new MySQLQuery("SELECT id FROM ord_pqr_cyl WHERE bill_num = '" + obj.pqrNum + "'").getAsInteger(conn);
                if (obj.pqrId == null) {
                    throw new Exception("No se pudo encontrar una pqr para la boleta " + obj.pqrNum);
                }
            }

            obj.tripId = new MySQLQuery("SELECT t.id FROM gt_cyl_trip t "
                    + "WHERE "
                    + " t.driver_id = " + sl.employeeId + " "
                    + " AND !t.cancel "
                    + "AND t.steps < t.req_steps "
                    + " AND '" + Dates.getSQLDateTimeFormat().format(new Date()) + "' > t.edt").getAsInteger(conn);
            obj.id = obj.insert(conn);

            if (obj.items != null && !obj.items.isEmpty()) {
                for (int i = 0; i < obj.items.size(); i++) {
                    new MySQLQuery("INSERT INTO trk_nov_item_appl SET nov_id = " + obj.id + ", item_id = " + obj.items.get(i).id).executeInsert(conn);
                }
            }
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(TrkCylNovelty obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            obj.update(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            TrkCylNovelty obj = new TrkCylNovelty().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            TrkCylNovelty.delete(id, conn);
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
            return createResponse(TrkCylNovelty.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
