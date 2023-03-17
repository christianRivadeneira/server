package api.mto.api;

import api.BaseAPI;
import api.mto.dto.VehicleList;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import api.mto.model.Vehicle;
import java.util.ArrayList;
import java.util.List;
import utilities.MySQLQuery;

@Path("/vehicle")
public class VehicleApi extends BaseAPI {

    @POST
    public Response insert(Vehicle obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(Vehicle obj) {
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
            Vehicle obj = new Vehicle().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Vehicle.delete(id, conn);
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
            return createResponse(Vehicle.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getActiveVh")
    public Response getActiveVh(@QueryParam("type") String type) {
        try (Connection conn = getConnection()) {
            List<Vehicle> list;

            if (type == null) {
                list = Vehicle.getList(new MySQLQuery("SELECT " + Vehicle.getSelFlds("v") + " FROM vehicle v WHERE v.active"), conn);
            } else {
                list = new ArrayList<>();

                Object[][] data = new MySQLQuery("SELECT DISTINCT v.id, v.plate, TRIM(CONCAT(COALESCE(v.internal,''), ' ', e.short_name)) AS internal "
                        + "FROM gt_cyl_trip AS ct "
                        + "INNER JOIN gt_trip_type AS tt ON ct.type_id = tt.id "
                        + "INNER JOIN vehicle AS v ON v.id = ct.vh_id "
                        + "INNER JOIN agency AS a ON a.id = v.agency_id "
                        + "INNER JOIN enterprise AS e ON e.id = a.enterprise_id WHERE tt.pul = 1 AND ct.sdt IS NOT NULL AND ct.ddt IS NULL AND ct.cancel = 0 AND v.active = 1").getRecords(conn);
                
                if(data != null && data.length > 0) {
                    for(Object[] row : data) {
                        Vehicle veh = new Vehicle();
                        veh.id = MySQLQuery.getAsInteger(row[0]);
                        veh.plate = MySQLQuery.getAsString(row[1]);
                        veh.internal = MySQLQuery.getAsString(row[2]);
                        list.add(veh);
                    }
                }
            }

            return createResponse(list);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @GET
    @Path("/vehicleListByPlate")
    public Response getVehicleListByPlate(@QueryParam("plate") String plate) {
        try (Connection conn = getConnection()) {
            return createResponse(VehicleList.getVehicleListByPlate(plate, conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @GET
    @Path("/vehicleList")
    public Response findVehicleList() {
        try (Connection conn = getConnection()) {
            return createResponse(VehicleList.findVehicleList2(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @GET
    @Path("/vehicleListById")
    public Response getVehicleListById(@QueryParam("vehicleId") Integer vehicleId) {
        try (Connection conn = getConnection()) {
            return createResponse(VehicleList.getVehicleListById(vehicleId, conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
   
}
