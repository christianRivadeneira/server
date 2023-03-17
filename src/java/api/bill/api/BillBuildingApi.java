package api.bill.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.bill.model.BillBuilding;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;

@Path("/billBuilding")
public class BillBuildingApi extends BaseAPI {

    @POST
    public Response insert(BillBuilding obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @PUT
    public Response update(BillBuilding obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
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
            useBillInstance(conn);
            BillBuilding obj = new BillBuilding().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillBuilding.delete(id, conn);
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
            return createResponse(BillBuilding.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getValidBuildings")
    public Response getValidBuildings(@QueryParam("spanId") int spanId, @QueryParam("instanceId") Integer instanceId) {
        //edificios con cliente y tanques
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(instanceId, conn);
            Object[][] res = new MySQLQuery("SELECT b.id "
                    + "FROM bill_building AS b "
                    + "INNER JOIN bill_client_tank AS c ON c.building_id = b.id AND c.active = 1 "//0
                    + "GROUP BY b.id ORDER BY b.old_id ASC").getRecords(conn);

            MySQLQuery tanksQ = new MySQLQuery("SELECT COUNT(*) FROM ord_tank_client AS c INNER JOIN est_tank AS t ON t.client_id = c.id "
                    + "WHERE c.mirror_id = ?1 AND c.bill_instance_id = " + instanceId + " ");

            useDefault(conn);
            List<Integer> rta = new ArrayList<>();
            for (Object[] re : res) {
                int id = MySQLQuery.getAsInteger(re[0]);
                tanksQ.setParam(1, id);
                Integer tanks = tanksQ.getAsInteger(conn);
                if (tanks != null && tanks > 0) {
                    rta.add(id);
                }
            }
            Integer[] buildings = rta.toArray(new Integer[0]);

            return createResponse(buildings);
        } catch (Exception ex) {
            return createResponse(ex);
        }

    }

    @GET
    @Path("/grid")
    public Response getGrid(@QueryParam("active") boolean active) throws Exception {
        try (Connection conn = getConnection()) {
            GridResult gr = new GridResult();
            getSession(conn);
            useBillInstance(conn);

            gr.data = new MySQLQuery("SELECT "
                    + "id, "
                    + "old_id, "
                    + "name, "
                    + "address, "
                    + "(SELECT COUNT(*) FROM bill_client_tank WHERE `building_id` = b.id AND `active` = 1) "
                    + "FROM bill_building as b "
                    + "WHERE b.active = " + active + " "
                    + "ORDER BY b.old_id ASC ").getRecords(conn);
            gr.sortColIndex = 1;
            gr.sortType = GridResult.SORT_ASC;
            gr.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_INTEGER, 40, "Código"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 160, "Nombre"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 160, "Dirección"),
                new MySQLCol(MySQLCol.TYPE_INTEGER, 40, "Clientes")
            };
            return createResponse(gr);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
