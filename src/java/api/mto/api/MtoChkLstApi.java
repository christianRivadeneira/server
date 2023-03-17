package api.mto.api;

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
import api.mto.model.MtoChkLst;

@Path("/mtoChkLst")
public class MtoChkLstApi extends BaseAPI {

    @POST
    public Response insert(MtoChkLst obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(MtoChkLst obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MtoChkLst old = new MtoChkLst().select(obj.id, conn);
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
            SessionLogin sl = getSession(conn);
            MtoChkLst obj = new MtoChkLst().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MtoChkLst.delete(id, conn);
            SysCrudLog.deleted(this, MtoChkLst.class, id, conn);
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
            return createResponse(MtoChkLst.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }


    @PUT
    @Path("/updateWarning")
    public Response updateNotes(@QueryParam("warning") String warning, @QueryParam("id") Integer id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            
            MtoChkLst obj = new MtoChkLst().select(id, conn);
            obj.notesWarning = warning;
            
            MtoChkLst old = new MtoChkLst().select(obj.id, conn);
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
