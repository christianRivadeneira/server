package api.ord.api;

import api.BaseAPI;
import api.ord.model.OrdChannel;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;

@Path("/ordChannel")
public class OrdChannelApi extends BaseAPI {

    @POST
    public Response insert(OrdChannel obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            verifyDefault(obj, conn);
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(OrdChannel obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            verifyDefault(obj, conn);
            OrdChannel old = new OrdChannel().select(obj.id, conn);
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            OrdChannel obj = new OrdChannel().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            OrdChannel.delete(id, conn);
            SysCrudLog.deleted(this, OrdChannel.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/all")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(OrdChannel.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private void verifyDefault(OrdChannel obj, Connection ep) throws Exception {
        Integer defId = new MySQLQuery("SELECT id FROM ord_channel WHERE def_op").getAsInteger(ep);
        if (obj.defOp && defId != null) {
            new MySQLQuery("UPDATE ord_channel SET def_op = 0 WHERE id = " + defId).executeUpdate(ep);
        }
    }

    /*@GET
    @Path("/grid")
    public Response getGrid() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult tbl = new GridResult();
            tbl.data = new MySQLQuery("").getRecords(conn);
            tbl.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 180, "Cupón"),
            };
            tbl.sortColIndex = 4;
            tbl.sortType = GridResult.SORT_ASC;
            return createResponse(tbl);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }*/

}
