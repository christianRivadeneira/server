package api.ess.api;

import api.BaseAPI;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import api.ess.model.EssFcmToken;
import utilities.MySQLQuery;

@Path("/essFcmToken")
public class EssFcmTokenApi extends BaseAPI {

    @POST
    public Response insert(EssFcmToken obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            MySQLQuery mq = new MySQLQuery("SELECT id "
                    + "FROM ess_fcm_token "
                    + "WHERE device = ?1 AND usr_id = ?2 ");
            mq.setParam(1, obj.device);
            mq.setParam(2, obj.usrId);
            Integer tokenId = mq.getAsInteger(conn);

            if (tokenId != null) {
                EssFcmToken old = new EssFcmToken().select(tokenId, conn);
                old.token = obj.token;
                old.update(conn);
            } else {
                obj.insert(conn);
                SysCrudLog.created(this, obj, conn);
            }
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(EssFcmToken obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            EssFcmToken old = new EssFcmToken().select(obj.id, conn);
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
            getSession(conn);
            EssFcmToken obj = new EssFcmToken().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            EssFcmToken.delete(id, conn);
            SysCrudLog.deleted(this, EssFcmToken.class, id, conn);
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
            return createResponse(EssFcmToken.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
