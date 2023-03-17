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
import api.mss.model.MssRoundProgTime;
import java.util.GregorianCalendar;

@Path("/mssRoundProgTime")
public class MssRoundProgTimeApi extends BaseAPI {

    @POST
    public Response insert(MssRoundProgTime obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            validateRound(obj);
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(MssRoundProgTime obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            validateRound(obj);
            MssRoundProgTime old = new MssRoundProgTime().select(obj.id, conn);
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
            MssRoundProgTime obj = new MssRoundProgTime().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssRoundProgTime.delete(id, conn);
            SysCrudLog.deleted(this, MssRoundProgTime.class, id, conn);
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
            return createResponse(MssRoundProgTime.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private void validateRound(MssRoundProgTime obj) throws Exception {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(obj.begin);
        int begMin = gc.get(GregorianCalendar.MINUTE);
        if (begMin % 5 != 0) {
            throw new Exception("Los minutos de la hora de ronda deben ser multiplo de 5");
        }
    }

}
