package api.mss.api;

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
import model.system.SessionLogin;
import api.mss.model.MssPointProg;
import utilities.MySQLQuery;
import utilities.cast;

@Path("/mssPointProg")
public class MssPointProgApi extends BaseAPI {

    @POST
    public Response insert(MssPointProg obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Integer place = new MySQLQuery("SELECT MAX(r.place) FROM mss_point_prog r WHERE r.prog_id = ?1 ")
                    .setParam(1, obj.progId).getAsInteger(conn);
            place = (place == null ? 1 : place + 1);
            obj.place = place;
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(MssPointProg obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MssPointProg old = new MssPointProg().select(obj.id, conn);
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
            MssPointProg obj = new MssPointProg().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssPointProg.delete(id, conn);
            SysCrudLog.deleted(this, MssPointProg.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/all")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(MssPointProg.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/up")
    public Response up(@QueryParam("id") int pointProgId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            flip(pointProgId, "up", conn);
            return createResponse(MssPointProg.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/down")
    public Response down(@QueryParam("id") int pointProgId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            flip(pointProgId, "down", conn);
            return createResponse(MssPointProg.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private static void flip(int pointProgId, String where, Connection conn) throws Exception {
        MssPointProg fld = new MssPointProg().select(pointProgId, conn);
        MySQLQuery q;
        if (where.equals("up")) {
            q = new MySQLQuery("SELECT id, place FROM mss_point_prog WHERE prog_id = ?1 AND place < ?2 ORDER BY place DESC LIMIT 1");
        } else {
            q = new MySQLQuery("SELECT id, place FROM mss_point_prog WHERE prog_id = ?1 AND place > ?2 ORDER BY place ASC LIMIT 1");
        }
        q.setParam(1, fld.progId);
        q.setParam(2, fld.place);
        Object[] row = q.getRecord(conn);
        if (row != null) {
            q = new MySQLQuery("UPDATE mss_point_prog SET place = ?1 WHERE id = ?2");
            q.setParam(1, cast.asInt(row, 1));
            q.setParam(2, fld.id);
            q.executeUpdate(conn);

            q = new MySQLQuery("UPDATE mss_point_prog SET place = ?1 WHERE id = ?2");
            q.setParam(1, fld.place);
            q.setParam(2, cast.asInt(row, 0));
            q.executeUpdate(conn);
        }
    }
}
