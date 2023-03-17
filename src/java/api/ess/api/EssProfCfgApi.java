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
import model.system.SessionLogin;
import api.ess.model.EssProfCfg;
import utilities.MySQLQuery;

@Path("/essProfCfg")
public class EssProfCfgApi extends BaseAPI {

    @POST
    public Response insert(EssProfCfg obj) {
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
    public Response update(EssProfCfg obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            EssProfCfg old = new EssProfCfg().select(obj.id, conn);
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
            EssProfCfg obj = new EssProfCfg().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
     @GET
     @Path("/byProfile")
    public Response byProfile(@QueryParam("profId") int profId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLQuery mq = new MySQLQuery("SELECT " + EssProfCfg.getSelFlds("") + " FROM ess_prof_cfg WHERE prof_id = " + profId);
            EssProfCfg obj = new EssProfCfg().select(mq, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            EssProfCfg.delete(id, conn);
            SysCrudLog.deleted(this, EssProfCfg.class, id, conn);
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
            return createResponse(EssProfCfg.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
