package api.mss.api;

import api.BaseAPI;
import api.mss.model.MssMinuteField;
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
import utilities.cast;

@Path("/mssMinuteField")
public class MssMinuteFieldApi extends BaseAPI {

    @POST
    public Response insert(MssMinuteField obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Integer place = new MySQLQuery("SELECT MAX(place) FROM mss_minute_field WHERE type_id = ?1").setParam(1, obj.typeId).getAsInteger(conn);
            if (place == null) {
                place = 0;
            } else {
                place++;
            }
            obj.place = place;
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(MssMinuteField obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssMinuteField old = new MssMinuteField().select(obj.id, conn);
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
            MssMinuteField obj = new MssMinuteField().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssMinuteField.delete(id, conn);
            SysCrudLog.deleted(this, MssMinuteField.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/up")
    public Response up(@QueryParam("id") int fieldId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            flip(fieldId, "up", conn);
            return createResponse(MssMinuteField.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/down")
    public Response down(@QueryParam("id") int fieldId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            flip(fieldId, "down", conn);
            return createResponse(MssMinuteField.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private static void flip(int fieldId, String where, Connection conn) throws Exception {
        MssMinuteField fld = new MssMinuteField().select(fieldId, conn);
        MySQLQuery q;
        if (where.equals("up")) {
            q = new MySQLQuery("SELECT id, place FROM mss_minute_field WHERE type_id = ?1 AND place < ?2 ORDER BY place DESC LIMIT 1");
        } else {
            q = new MySQLQuery("SELECT id, place FROM mss_minute_field WHERE type_id = ?1 AND place > ?2 ORDER BY place ASC LIMIT 1");
        }
        q.setParam(1, fld.typeId);
        q.setParam(2, fld.place);
        Object[] row = q.getRecord(conn);
        if (row != null) {
            q = new MySQLQuery("UPDATE mss_minute_field SET place = ?1 WHERE id = ?2");
            q.setParam(1, cast.asInt(row, 1));
            q.setParam(2, fld.id);
            q.executeUpdate(conn);

            q = new MySQLQuery("UPDATE mss_minute_field SET place = ?1 WHERE id = ?2");
            q.setParam(1, fld.place);
            q.setParam(2, cast.asInt(row, 0));
            q.executeUpdate(conn);
        }
    }

    @GET
    @Path("/allField")
    public Response getAllField(@QueryParam("typeId") int typeId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            List<MssMinuteField> fields = MssMinuteField.getAll(typeId, conn);
            if (fields.isEmpty()) {
                throw new Exception("El tipo de minuta no tiene campos. Contacte al administrador.");
            }
            return createResponse(fields);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
