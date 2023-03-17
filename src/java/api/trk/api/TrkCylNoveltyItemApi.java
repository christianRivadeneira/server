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
import api.trk.model.TrkCylNoveltyItem;
import utilities.MySQLQuery;

@Path("/trkCylNoveltyItem")
public class TrkCylNoveltyItemApi extends BaseAPI {

    @POST
    public Response insert(TrkCylNoveltyItem obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(TrkCylNoveltyItem obj) {
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
            TrkCylNoveltyItem obj = new TrkCylNoveltyItem().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            TrkCylNoveltyItem.delete(id, conn);
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
            return createResponse(TrkCylNoveltyItem.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @GET
    @Path("/getActive")
    public Response getActive() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(TrkCylNoveltyItem.getList(new MySQLQuery("SELECT " + TrkCylNoveltyItem.getSelFlds("") + " FROM trk_cyl_novelty_item WHERE active"), conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
}
