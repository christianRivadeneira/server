package api.trk.api;

import api.BaseAPI;
import api.trk.model.TrkSale;
import java.sql.Connection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;

@Path("/trkSale")
public class TrkSaleApi extends BaseAPI {

    /* @POST
    public Response insert(TrkSale obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(TrkSale obj) {
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
            TrkSale obj = new TrkSale().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            TrkSale.delete(id, conn);
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
            return createResponse(TrkSale.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }*/
    @GET
    @Path("/createFromDto")
    public Response createFromDto(@QueryParam("year") int year, @QueryParam("month") int month) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            TrkSale.createFromDtoSale(conn, year, month);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/test")
    public Response test() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Object[][] empData = new MySQLQuery("SELECT DISTINCT s.emp_id FROM \n"
                    + "trk_sale s").getRecords(conn);

            for (Object[] empRow : empData) {
                Integer empId = MySQLQuery.getAsInteger(empRow[0]);

                Integer vehicleId = new MySQLQuery("SELECT dv.vehicle_id FROM driver_vehicle dv WHERE dv.driver_id = " + empId + " AND dv.`end` IS NULL").getAsInteger(conn);
                Integer manId = null;
                if (vehicleId != null) {
                    manId = new MySQLQuery("SELECT man_id FROM com_man_veh WHERE veh_id = " + vehicleId).getAsInteger(conn);
                }
                if (manId == null) {
                    manId = new MySQLQuery("SELECT s.man_id FROM "
                            + "employee e "
                            + "INNER JOIN com_man_store s ON s.store_id = e.store_id "
                            + "WHERE e.id = " + empId).getAsInteger(conn);
                }
                if (manId != null) {
                    new MySQLQuery("UPDATE trk_sale SET man_id = " + manId + " WHERE emp_id = " + empId).executeUpdate(conn);
                }
            }

            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
