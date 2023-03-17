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
import api.trk.model.TrkIllegibleTrip;
import java.util.Date;
import model.system.SessionLogin;
import utilities.MySQLQuery;

@Path("/trkIllegibleTrip")
public class TrkIllegibleTripApi extends BaseAPI {

    @POST
    public Response insert(TrkIllegibleTrip obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(TrkIllegibleTrip obj) {
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
            TrkIllegibleTrip obj = new TrkIllegibleTrip().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            TrkIllegibleTrip.delete(id, conn);
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
            return createResponse(TrkIllegibleTrip.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/registerIllegible")
    public Response registerIllegible(@QueryParam("tripId") int tripId, @QueryParam("amSman") int illegibleSman, @QueryParam("amPlatf") int illegiblePlatf) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            TrkIllegibleTrip item = new TrkIllegibleTrip().select(new MySQLQuery("SELECT " + TrkIllegibleTrip.getSelFlds("t") + " FROM trk_illegible_trip t WHERE t.gt_trip_id = " + tripId), conn);
            if (item != null) {
                if (illegiblePlatf == illegibleSman) {
                    new MySQLQuery("DELETE FROM trk_illegible_trip WHERE id = " + item.id).executeDelete(conn);
                } else if (item.amPlatfRep != illegiblePlatf || item.amSmanRep != illegibleSman) {
                    new MySQLQuery("UPDATE trk_illegible_trip t SET am_platf_rep = " + illegiblePlatf + ", am_sman_rep = " + illegibleSman + ", report_dt = NOW(), plat_emp_id = " + sl.employeeId + " WHERE id = " + item.id).executeUpdate(conn);
                }
            } else if (item == null && illegiblePlatf != illegibleSman) {
                item = new TrkIllegibleTrip();
                item.gtTripId = tripId;
                item.platEmpId = sl.employeeId;
                item.amPlatfRep = illegiblePlatf;
                item.amSmanRep = illegibleSman;
                item.reportDt = new Date();
                item.insert(conn);
            }

            return Response.ok().build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
