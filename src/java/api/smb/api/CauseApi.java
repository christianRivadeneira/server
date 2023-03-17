package api.smb.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.smb.model.Cause;
import api.sys.model.SysCrudLog;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;

@Path("/cause")
public class CauseApi extends BaseAPI {

    @POST
    public Response insert(Cause obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            if (obj.kind.equals("cancel") && obj.defaultOp) {
                new MySQLQuery("UPDATE cause SET default_op = 0 WHERE kind = 'cancel' ").executeUpdate(conn);
            }
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(Cause obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Cause old = new Cause().select(obj.id, conn);
            if (obj.kind.equals("cancel") && obj.defaultOp) {
                new MySQLQuery("UPDATE cause SET default_op = 0 WHERE kind = 'cancel' ").executeUpdate(conn);
            }
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
            Cause obj = new Cause().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BigDecimal num = new MySQLQuery("SELECT count(*) FROM contract AS o WHERE o.cancel_cause_id = ?1").setParam(1, id).getAsBigDecimal(conn, true);
            if (num.intValue() > 0) {
                throw new Exception("Existen contratos registrados con esta causal.");
            }
            Cause.delete(id, conn);
            SysCrudLog.deleted(this, Cause.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/all")
    public Response getAll(@QueryParam("type") String type) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(Cause.getByType(conn, type));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/grid")
    public Response getGrid(@QueryParam("type") String type) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult tbl = new GridResult();
            List<Cause> lst = Cause.getByType(conn, type);
            if (type.equals("anull")) {
                tbl.data = new Object[lst.size()][2];
                for (int i = 0; i < lst.size(); i++) {
                    Cause c = lst.get(i);
                    tbl.data[i][0] = c.id;
                    tbl.data[i][1] = c.description;
                }
                tbl.cols = new MySQLCol[]{
                    new MySQLCol(MySQLCol.TYPE_KEY),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 180, "Descripción")
                };
            } else if (type.equals("cancel")) {
                tbl.data = new Object[lst.size()][3];
                for (int i = 0; i < lst.size(); i++) {
                    Cause c = lst.get(i);
                    tbl.data[i][0] = c.id;
                    tbl.data[i][1] = c.description;
                    tbl.data[i][2] = c.defaultOp;
                }
                tbl.cols = new MySQLCol[]{
                    new MySQLCol(MySQLCol.TYPE_KEY),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 90, "Descripción"),
                    new MySQLCol(MySQLCol.TYPE_BOOLEAN, 20, "Defecto")
                };
            }
            return createResponse(tbl);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
