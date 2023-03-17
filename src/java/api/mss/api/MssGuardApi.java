package api.mss.api;

import api.BaseAPI;
import api.mss.dto.GuardInfo;
import api.mss.model.MssGuard;
import api.sys.model.Employee;
import api.sys.model.SysAppProfile;
import api.sys.model.SysAppProfileEmp;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.Dates;
import utilities.MySQLQuery;
import utilities.cast;
import web.MD5;

@Path("/mssGuard")
public class MssGuardApi extends BaseAPI {

    @POST
    public Response insert(MssGuard obj) {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                getSession(conn);

                MD5 md = MD5.getInstance();
                String name = "";
                if (obj.type != null && obj.type.equals("agent")) {
                    name = "Agentes";

                    MssGuard agent = MssGuard.getByDoc(obj.document, "agent", conn);

                    if (agent != null) {
                        throw new Exception("Ya existe un agente con este número de documento");
                    }
                } else if (obj.supervisor) {
                    name = "Supervisores";
                } else {
                    name = "Guardas";
                }
                SysAppProfile appProfile = SysAppProfile.getProfileByName("com.qualisys.minutas", name, conn);
                if (appProfile == null) {
                    throw new Exception("No se ha creado el perfil para "
                            + (obj.supervisor ? "Supervisores" : "Guardas")
                            + ".\nComuniquese con sistemas.");
                }

                boolean newEmp = false;
                Employee emp = Employee.getByDoc(obj.document, conn);
                if (emp == null) {
                    newEmp = true;
                    emp = new Employee();
                    emp.document = obj.document;
                    emp.login = obj.document;
                    emp.password = md.hashData(emp.document.getBytes());
                }
                emp.firstName = obj.firstName;
                emp.lastName = obj.lastName;
                emp.phone = obj.phone;
                emp.mail = obj.email;
                emp.active = true;
                if (newEmp) {
                    emp.insert(conn);
                } else {
                    emp.update(conn);
                }

                if (!SysAppProfileEmp.hasProfileByEmp(emp.id, appProfile.id, conn)) {
                    SysAppProfileEmp pemp = new SysAppProfileEmp();
                    pemp.empId = emp.id;
                    pemp.appProfileId = appProfile.id;
                    pemp.insert(conn);
                }

                obj.empId = emp.id;
                obj.insert(conn);
                SysCrudLog.created(this, obj, conn);
                conn.commit();
                return Response.ok(obj).build();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(MssGuard obj
    ) {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                getSession(conn);
                MssGuard old = new MssGuard().select(obj.id, conn);
                obj.update(conn);
                Employee e = new Employee().select(obj.empId, conn);
                e.document = obj.document;
                e.firstName = obj.firstName;
                e.lastName = obj.lastName;
                e.login = obj.document;
                e.mail = obj.email;
                SysCrudLog.updated(this, obj, old, conn);
                conn.commit();
                return Response.ok(obj).build();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id
    ) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssGuard obj = new MssGuard().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id
    ) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssGuard.delete(id, conn);
            SysCrudLog.deleted(this, MssGuard.class, id, conn);
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
            return createResponse(MssGuard.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/guardsToReview")
    public Response getPostInfo(@QueryParam("postId") int postId, @QueryParam("progId") int progId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLQuery mq = new MySQLQuery(
                    "SELECT s.id, g.id, g.document, CONCAT(g.first_name, ' ', g.last_name), "
                    + "s.exp_beg, s.exp_end, r.id "
                    + "FROM mss_shift s "
                    + "INNER JOIN mss_guard g ON g.id = s.guard_id AND g.supervisor = 0 "
                    + "LEFT JOIN mss_super_review r ON r.guard_id = s.guard_id AND r.post_id = s.post_id AND r.prog_id = ?2 "
                    + "WHERE NOW() > s.exp_beg AND NOW() < s.exp_end AND s.post_id = ?1 AND s.active "
                    + "AND s.reg_beg IS NOT NULL AND s.reg_end IS NULL "
                    + "ORDER BY g.first_name; "
            ).setParam(1, postId).setParam(2, progId);

            Object[][] dataGuards = mq.getRecords(conn);
            List<GuardInfo> guards = new ArrayList<>();

            for (Object[] dataGuard : dataGuards) {
                GuardInfo guard = new GuardInfo();
                guard.shiftId = cast.asInt(dataGuard[0]);
                guard.guardId = cast.asInt(dataGuard[1]);
                guard.document = cast.asString(dataGuard[2]);
                guard.guardName = cast.asString(dataGuard[3]);
                Date expBeg = cast.asDate(dataGuard[4]);
                Date expEnd = cast.asDate(dataGuard[5]);
                guard.reviewId = cast.asInt(dataGuard[6]);

                SimpleDateFormat sdf = Dates.getHourFormat();
                guard.status = "Turno de: " + sdf.format(expBeg) + " - " + sdf.format(expEnd);

                guards.add(guard);
            }
            return Response.ok(guards).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getAgent")
    public Response getAgent(@QueryParam("doc") String doc) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssGuard agent = MssGuard.getByDoc(doc, "agent", conn);

            if (agent != null) {
                throw new Exception("Ya existe un agente con este número de documento");
            }
            Employee obj = Employee.getByDoc(doc, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
