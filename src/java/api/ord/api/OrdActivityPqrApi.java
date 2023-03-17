package api.ord.api;

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
import api.ord.model.OrdActivityPqr;
import api.sys.model.Employee;
import api.sys.model.SysCrudLog;
import java.util.Date;
import utilities.MySQLQuery;

@Path("/ordActivityPqr")
public class OrdActivityPqrApi extends BaseAPI {

    @POST
    public Response insert(OrdActivityPqr obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.creationDate = new Date();
            obj.modDate = new Date();
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(OrdActivityPqr obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            OrdActivityPqr old = new OrdActivityPqr().select(obj.id, conn);
            obj.modDate = new Date();
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
            OrdActivityPqr obj = new OrdActivityPqr().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/getEvidenceActivity")
    public Response get(@QueryParam("pqrId") int pqrId, @QueryParam("pqrType") int pqrType) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            String condition = null;
            switch (pqrType) {
                case 1:
                    condition = " AND pqr_cyl_id = " + pqrId;
                    break;
                case 2:
                    condition = " AND pqr_tank_id = " + pqrId;
                    break;
                case 3:
                    condition = " AND repair_id = " + pqrId;
                    break;
            }

            MySQLQuery mq = new MySQLQuery("SELECT " + OrdActivityPqr.getSelFlds("") + " FROM ord_activity_pqr "
                    + "WHERE create_id = ?1 AND evidence " + condition).setParam(1, sl.employeeId);

            OrdActivityPqr obj = new OrdActivityPqr().select(mq, conn);

            if (obj == null) {
                Employee employee = new Employee().select(sl.employeeId, conn);
                obj = new OrdActivityPqr();
                obj.createId = sl.employeeId;
                obj.modId = obj.createId;
                obj.actDeveloper = employee.firstName + " " + employee.lastName;
                obj.activity = "Evidencia Encuesta";
                obj.observation = "Evidencia fotografica desde la aplicación";
                obj.creationDate = new Date();
                obj.modDate = new Date();
                obj.actDate = new Date();
                switch (pqrType) {
                    case 1:
                        obj.pqrCylId = pqrId;
                        break;
                    case 2:
                        obj.pqrTankId = pqrId;
                        break;
                    case 3:
                        obj.repairId = pqrId;
                        break;
                }
                obj.evidence = true;
                obj.insert(conn);
            }

            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            if (new MySQLQuery("SELECT COUNT(*) > 0 FROM bfile WHERE owner_type = 20 AND owner_id = " + id).getAsBoolean(conn)) {
                throw new Exception("La actividad tiene adjuntos. No se puede eliminar");
            }

            OrdActivityPqr act = new OrdActivityPqr().select(id, conn);

            new MySQLQuery("INSERT INTO ord_log SET "
                    + "employee_id = " + sl.employeeId + ", "
                    + "log_date = NOW(), "
                    + "notes = 'Se eliminó actividad: " + act.activity + "', "
                    + "owner_id = " + getOwnerId(act) + ", "
                    + "owner_type = " + getOwnerType(act)).executeInsert(conn);
            OrdActivityPqr.delete(id, conn);
            SysCrudLog.deleted(this, OrdActivityPqr.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public int getOwnerId(OrdActivityPqr act) {
        if (act.pqrCylId != null) {
            return act.pqrCylId;
        } else if (act.pqrTankId != null) {
            return act.pqrTankId;
        } else if (act.pqrOtherId != null) {
            return act.pqrOtherId;
        } else if (act.pqrAfilId != null) {
            return act.pqrAfilId;
        } else if (act.comId != null) {
            return act.comId;
        } else if (act.repairId != null) {
            return act.repairId;
        } else {
            return 0;
        }
    }

    public int getOwnerType(OrdActivityPqr act) {
        /**
         * Estos ids vienen de la OrdLog de front
         */
        if (act.pqrCylId != null) {
            return 6;
        } else if (act.pqrTankId != null) {
            return 5;
        } else if (act.pqrOtherId != null) {
            return 3;
        } else if (act.pqrAfilId != null) {
            return 16;
        } else if (act.comId != null) {
            return 13;
        } else if (act.repairId != null) {
            return 15;
        } else {
            return 0;
        }
    }

    @GET
    @Path("/getAll")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(OrdActivityPqr.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getAllByCreateId")
    public Response getAllByCreateId(@QueryParam("pqrType") int type, @QueryParam("pqrId") int pqrId) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(OrdActivityPqr.getAllByCreateId(conn, pqrId, type, sl.employeeId));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
