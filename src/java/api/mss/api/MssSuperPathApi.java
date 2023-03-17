package api.mss.api;

import api.BaseAPI;
import api.mss.dto.PathApp;
import api.mss.model.MssGuard;
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
import api.mss.model.MssSuperPath;
import java.util.ArrayList;
import java.util.List;
import utilities.MySQLQuery;
import utilities.cast;

@Path("/mssSuperPath")
public class MssSuperPathApi extends BaseAPI {

    @POST
    public Response insert(MssSuperPath obj) {
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
    public Response update(MssSuperPath obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssSuperPath old = new MssSuperPath().select(obj.id, conn);
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
            MssSuperPath obj = new MssSuperPath().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssSuperPath.delete(id, conn);
            SysCrudLog.deleted(this, MssSuperPath.class, id, conn);
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
            return createResponse(MssSuperPath.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/path-app")
    public Response getPathApp() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            Integer superId = MssGuard.getSuperIdFromEmployee(sl.employeeId, conn);
            if (superId == null) {
                throw new Exception("No esta registrado como supervisor.\nComuniquese con el administrador");
            }

            MySQLQuery qPath = new MySQLQuery("SELECT r.id, r.name, r.code, COUNT(distinct pst.id) , if(prog.id IS NOT NULL, 1, 0) "
                    + "FROM mss_super_path r "
                    + "INNER JOIN mss_super_post_path p ON p.path_id = r.id "
                    + "INNER JOIN mss_post pst ON pst.id = p.post_id "
                    + "LEFT JOIN mss_super_prog prog ON prog.path_id = r.id AND CURDATE() BETWEEN prog.beg_dt AND prog.end_dt AND  prog.super_id = ?1 "
                    + "GROUP BY r.id ").setParam(1, superId);

            Object[][] data = qPath.getRecords(conn);
            List<PathApp> list = new ArrayList<>();
            for (Object[] row : data) {
                PathApp obj = new PathApp();
                obj.pathId = cast.asInt(row, 0);
                obj.name = cast.asString(row, 1);
                obj.code = cast.asString(row, 2);
                obj.postAmount = cast.asInt(row, 3);
                obj.isProgammed = cast.asBoolean(row, 4);
                list.add(obj);
            }

            return createResponse(list);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
