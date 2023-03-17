package api.mss.api;

import api.BaseAPI;
import api.mss.dto.PostApp;
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
import api.mss.model.MssPost;
import java.util.ArrayList;
import java.util.List;
import utilities.MySQLQuery;
import utilities.apiClient.StringResponse;

@Path("/mssPost")
public class MssPostApi extends BaseAPI {

    @POST
    public Response insert(MssPost obj) {
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
    public Response update(MssPost obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssPost old = new MssPost().select(obj.id, conn);
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
            MssPost obj = new MssPost().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssPost.delete(id, conn);
            SysCrudLog.deleted(this, MssPost.class, id, conn);
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
            return createResponse(MssPost.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/getPost")
    public Response getPost(@QueryParam("clientId") int clientId) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            Object[][] record = new MySQLQuery("SELECT p.id, p.name "
                    + "FROM mss_post p "
                    + "WHERE p.client_id = ?1").setParam(1, clientId).getRecords(conn);

            List<MssPost> listInfo = new ArrayList<>();
            for (Object[] item : record) {

                MssPost mssValue = new MssPost();
                mssValue.id = MySQLQuery.getAsInteger(item[0]);
                mssValue.name = MySQLQuery.getAsString(item[1]);
                listInfo.add(mssValue);

            }
            return createResponse(listInfo);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/postApp")
    public Response getPostApp() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            Integer superId = MssGuard.getSuperIdFromEmployee(sl.employeeId, conn);
            if (superId == null) {
                throw new Exception("No esta registrado como supervisor.\nComuniquese con el administrador");
            }

            Object[][] record = new MySQLQuery("SELECT "
                    + "p.id, p.name, c.id, c.name, COUNT(po.id) , "
                    + "(SELECT COUNT(*)>0 FROM mss_point pp WHERE pp.post_id = p.id AND pp.is_check ), "
                    + "IF(sp.id IS NOT NULL,1,0) "
                    + "FROM mss_post p "
                    + "INNER JOIN mss_client c ON c.id  = p.client_id "
                    + "LEFT JOIN mss_point po ON po.post_id = p.id "
                    + "LEFT JOIN mss_super_prog sp ON sp.post_id = p.id AND CURDATE() BETWEEN sp.beg_dt AND sp.end_dt AND sp.super_id = ?1 "
                    + "WHERE c.active AND p.active "
                    + "GROUP BY p.id "
                    + "ORDER BY c.name, p.name ").setParam(1, superId).getRecords(conn);

            List<PostApp> listInfo = new ArrayList<>();
            for (Object[] item : record) {

                PostApp clientPost = new PostApp();
                clientPost.postId = MySQLQuery.getAsInteger(item[0]);
                clientPost.postName = MySQLQuery.getAsString(item[1]);
                clientPost.clientId = MySQLQuery.getAsInteger(item[2]);
                clientPost.clientName = MySQLQuery.getAsString(item[3]);
                clientPost.puntos = MySQLQuery.getAsInteger(item[4]);
                clientPost.hasCheck = MySQLQuery.getAsBoolean(item[5]);
                clientPost.isScheduled = MySQLQuery.getAsBoolean(item[6]);
                listInfo.add(clientPost);

            }
            return createResponse(listInfo);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/motto")
    public Response getPostApp(@QueryParam("postId") int postId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            String motto = new MySQLQuery("SELECT p.motto FROM mss_post p "
                    + "WHERE p.id = ?1").setParam(1, postId).getAsString(conn);

            StringResponse res = new StringResponse();
            res.msg = motto;
            return createResponse(res);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/postAppByPath")
    public Response getPostAppByPath(@QueryParam("pathId") int pathId) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            Integer superId = MssGuard.getSuperIdFromEmployee(sl.employeeId, conn);
            if (superId == null) {
                throw new Exception("No esta registrado como supervisor.\nComuniquese con el administrador");
            }

            Object[][] record = new MySQLQuery(" SELECT "
                    + " p.id, p.name, c.id, c.name "
                    + " FROM mss_super_post_path t "
                    + " INNER JOIN mss_post p ON p.id = t.post_id AND p.active "
                    + " INNER JOIN mss_client c ON c.id = p.client_id AND c.active "
                    + " WHERE t.path_id = ?1 "
            ).setParam(1, pathId).getRecords(conn);

            List<PostApp> listInfo = new ArrayList<>();
            for (Object[] item : record) {

                PostApp clientPost = new PostApp();
                clientPost.postId = MySQLQuery.getAsInteger(item[0]);
                clientPost.postName = MySQLQuery.getAsString(item[1]);
                clientPost.clientId = MySQLQuery.getAsInteger(item[2]);
                clientPost.clientName = MySQLQuery.getAsString(item[3]);
                listInfo.add(clientPost);

            }
            return createResponse(listInfo);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
