package api.sys.api;

import api.BaseAPI;
import api.sys.model.Neigh;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
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

@Path("/neigh")
public class NeighApi extends BaseAPI {

    @POST
    public Response insert(Neigh obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(Neigh obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            Neigh old = new Neigh().select(obj.id, conn);
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
            Neigh obj = new Neigh().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            Neigh.delete(id, conn);
            SysCrudLog.deleted(this, Neigh.class, id, conn);
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
            return createResponse(Neigh.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/byBillCity")
    public Response instances() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            MySQLQuery mq = new MySQLQuery("SELECT " + Neigh.getSelFlds("n") + " "
                    + "FROM bill_client_tank tc "
                    + "INNER JOIN sigma.neigh n ON tc.neigh_id = n.id "
                    + "GROUP BY n.id "
                    + "ORDER by n.`name` ASC");
            List<Neigh> list = Neigh.getList(mq, conn);
            return createResponse(list);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
