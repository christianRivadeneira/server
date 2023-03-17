package api.ess.api;

import api.BaseAPI;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import api.ess.model.EssProg;
import api.sys.model.SysCrudLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

@Path("/essProg")
public class EssProgApi extends BaseAPI {

    @POST
    public Response insert(EssProg obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.regDt = new Date();
            obj.empId = sl.employeeId;
            obj.active = true;
            if (obj.personId == null) {//entra aqui desde el app
                obj.personId = new MySQLQuery("SELECT p.id FROM ess_person p WHERE p.emp_id = ?1").setParam(1, sl.employeeId).getAsInteger(conn);
            }

            obj.unitId = new MySQLQuery("SELECT u.unit_id FROM ess_person_unit u WHERE u.person_id = ?1 LIMIT 1").setParam(1, obj.personId).getAsInteger(conn);
            if (obj.unitId == null) {
                obj.buildId = new MySQLQuery("SELECT a.build_id FROM ess_build_admin a WHERE a.person_id = ?1 LIMIT 1").setParam(1, obj.personId).getAsInteger(conn);
            }

            SysCrudLog.created(this, obj, conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(EssProg obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            EssProg old = new EssProg().select(obj.id, conn);
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
            EssProg obj = new EssProg().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            EssProg.delete(id, conn);
            SysCrudLog.deleted(this, EssProg.class, id, conn);
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
            return createResponse(EssProg.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getByEmp")
    public Response getByEmp(@QueryParam("empId") int empId, @QueryParam("asAdmin") boolean asAdmin) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            List<EssProg> list = new ArrayList<>();
            String buildAdms = null;
            String units = new MySQLQuery(" SELECT GROUP_CONCAT(pu.unit_id) "
                    + " FROM ess_person p "
                    + " INNER JOIN ess_person_unit pu ON pu.person_id = p.id "
                    + " WHERE p.emp_id = ?1").setParam(1, empId).getAsString(conn);
            MySQLQuery mq;

            if (asAdmin) {
                buildAdms = new MySQLQuery("SELECT GROUP_CONCAT(ba.build_id) "
                        + " FROM ess_build_admin ba "
                        + " INNER JOIN ess_person p ON p.id = ba.person_id "
                        + " WHERE p.emp_id = ?1 "
                ).setParam(1, empId).getAsString(conn);
            }

            if (asAdmin) {
                if (buildAdms != null && !buildAdms.isEmpty()) {
                    mq = new MySQLQuery("SELECT " + EssProg.getSelFlds("p") + " "
                            + "FROM ess_prog p "
                            + "WHERE p.build_id IN (" + buildAdms + ") and p.active "
                            + "ORDER BY p.prog_dt DESC");
                    list = EssProg.getList(mq, conn);
                }

            } else {
                mq = new MySQLQuery("SELECT " + EssProg.getSelFlds("p") + " "
                        + "FROM ess_prog p "
                        + "WHERE p.unit_id IN (" + units + ")  and p.active "
                        + "ORDER BY p.prog_dt DESC");
                list = EssProg.getList(mq, conn);
            }

            return createResponse(list);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
