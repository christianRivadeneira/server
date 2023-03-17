package api.mss.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.mss.model.MssShift;
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
import api.mss.model.MssShiftChange;
import metadata.model.GridRequest;
import utilities.MySQLQuery;

@Path("/mssShiftChange")
public class MssShiftChangeApi extends BaseAPI {

    @POST
    public Response insert(MssShiftChange obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            obj.regDt = MySQLQuery.now(conn);
            
            MssShift shift = new MssShift().select(obj.shiftId, conn);            
            if(obj.newGuardId == shift.guardId){
                throw new Exception("El nuevo guarda debe ser diferente al anterior");
            }
            boolean hasPreviousShift = MssShift.hasShiftByDates(obj.newGuardId, shift.expBeg, shift.expEnd, conn);
            if(hasPreviousShift){
                throw new Exception("El nuevo guarda ya tiene un turno para este rango de día");
            }
            
            obj.oldGuardId = shift.guardId;                        
            obj.insert(conn);
            shift.guardId = obj.newGuardId;
            shift.update(conn);
            
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(MssShiftChange obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssShiftChange old = new MssShiftChange().select(obj.id, conn);                                    
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
            MssShiftChange obj = new MssShiftChange().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssShiftChange.delete(id, conn);
            SysCrudLog.deleted(this, MssShiftChange.class, id, conn);
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
            return createResponse(MssShiftChange.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }  

    @POST
    @Path("/changesByShift")
    public Response getGrid(GridRequest obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult tbl = new GridResult();
            tbl.data = new MySQLQuery("SELECT sh.id, sh.reg_dt, "
                    + "go.document, "
                    + "gn.document, "
                    + "sh.notes "
                    + "FROM mss_shift_change sh "
                    + "INNER JOIN mss_shift s ON s.id = sh.shift_id "
                    + "INNER JOIN mss_guard go ON go.id = sh.old_guard_id "
                    + "INNER JOIN mss_guard gn ON gn.id = sh.new_guard_id "
                    + "WHERE s.id = ?1 ").setParam(1, obj.ints.get(0)).getRecords(conn);
            tbl.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),                
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_A, 40, "Registro"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 40, "Guarda Anterior"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 40, "Guarda Nuevo"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 60, "Notas"),
            };
            tbl.sortColIndex = 1;
            tbl.sortType = GridResult.SORT_DESC;            
            
            return createResponse(tbl);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
