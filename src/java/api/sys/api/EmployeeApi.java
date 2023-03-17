package api.sys.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.sys.model.Employee;
import api.sys.model.LoginRequest;
import api.sys.model.LoginResponse;
import api.sys.model.SysCrudLog;
import java.security.MessageDigest;
import java.sql.Connection;
import java.util.Date;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import metadata.model.GridRequest;
import model.menu.Credential;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import utilities.apiClient.StringResponse;
import utilities.cast;
import web.MD5;

@Path("/employee")
public class EmployeeApi extends BaseAPI {

    @POST
    public Response insert(Employee obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            int empId = obj.insert(conn);
            if (!MySQLQuery.isEmpty(obj.login) && !MySQLQuery.isEmpty(obj.document)) {
                new MySQLQuery("UPDATE employee e SET e.password = md5(" + obj.document + ") WHERE e.id = " + empId).executeUpdate(conn);
            }
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(Employee obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Employee old = new Employee().select(obj.id, conn);
            obj.password = old.password;
            boolean updatePassword = false;
            if (MySQLQuery.isEmpty(old.login) && !MySQLQuery.isEmpty(obj.login)) {
                obj.login = old.login;
                if (MySQLQuery.isEmpty(old.password)) {
                    updatePassword = true;
                }
            }
            obj.update(conn);
            if (updatePassword) {
                new MySQLQuery("UPDATE employee e SET e.password = md5(" + obj.document + ") WHERE e.id = " + obj.id).executeUpdate(conn);
            }

            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            Employee obj = new Employee().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Employee.delete(id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    @Path("/inactive")
    public Response inactive(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Employee obj = new Employee().select(id, conn);
            obj.active = false;
            obj.update(conn);
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
            return createResponse(Employee.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest req, @Context HttpServletRequest sr, @Context ServletContext c) {
        try {
            LoginResponse rta = new LoginResponse();
            Credential cr = req.toCredential(c, sr);
            try (Connection con = MySQLCommon.getConnection(req.poolName, req.tz)) {
                rta.employee = new Employee().select(cr.getEmployeeId(), con);
                MD5 md = MD5.getInstance();
                String hashData = md.hashData(rta.employee.document.getBytes());
                if (req.pass.equals(hashData)) {
                    rta.documentAsPassword = true;
                }

                if (rta.employee.lastProfile == null) {
                    Integer defaultProfileId = new MySQLQuery("SELECT p.id "
                            + "FROM login l "
                            + "INNER JOIN profile p ON p.id = l.profile_id "
                            + "AND p.is_mobile = false AND p.active "
                            + "WHERE l.employee_id = ?1 "
                            + "GROUP BY p.menu_id "
                            + "LIMIT 1")
                            .setParam(1, rta.employee.id)
                            .getAsInteger(con);

                    if (defaultProfileId != null) {
                        new MySQLQuery("UPDATE employee SET last_profile = ?1 WHERE id = " + rta.employee.id)
                                .setParam(1, defaultProfileId)
                                .executeUpdate(con);
                        rta.employee.lastProfile = defaultProfileId;
                    }
                }
            }
            rta.sessionId = cr.getSessionId();

            return Response.ok(rta).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/updatePassword")
    public Response updatePassword(StringResponse stringResponse) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            Employee obj = new Employee().select(sl.employeeId, conn);
            obj.password = stringResponse.msg;
            obj.lastPasswordChange = new Date();
            obj.update(conn);
            return Response.ok().build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getDrivers")
    public Response getDrivers() {
        try (Connection conn = getConnection()) {
            return createResponse(Employee.getList(new MySQLQuery("SELECT " + Employee.getSelFlds("e") + " FROM employee e WHERE driver AND active"), conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/resetPassword")
    public Response resetPassword(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            Employee emp = new Employee().select(id, conn);
            String document = emp.document;

            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(document.getBytes());
            byte[] digest = md.digest();
            String hash = DatatypeConverter.printHexBinary(digest).toUpperCase();

            emp.password = hash;
            emp.update(conn);
            SysCrudLog.updated(this, emp, "Se reestablecio la contraseña", conn);
            StringResponse response = new StringResponse("SUCCESS", "La contraseña ha sido reestablecida satisfactoriamente.");
            return Response.ok(response).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/closeSession")
    public Response closeSessions(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Employee emp = new Employee().select(id, conn);
            Integer sessions = Employee.closeSessions(id, conn);
            SysCrudLog.updated(this, emp, "Se cerró todas las sesiones", conn);
            String label = sessions == 1 ? " Sesion" : " Sesiones";
            StringResponse response = new StringResponse("SUCCESS", "Se cerraron " + (sessions == 0 ? sessions + label : "las sesiones"));
            return Response.ok(response).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/forSystemEmployee")
    public Response getForSystemEmployee(GridRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            GridResult r = new GridResult();
            r.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 50, "Documento"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 50, "Nombres"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 50, "Apellidos"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 50, "Correo"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 50, "Teléfono")};

            r.cols[4].toString = true;

            MySQLQuery q = new MySQLQuery("SELECT "
                    + " employee.id, "
                    + " GROUP_CONCAT(distinct profile.name), "
                    + " GROUP_CONCAT(distinct menu.label), "
                    + " employee.document, employee.first_name, "
                    + " employee.last_name, employee.mail, employee.phone "
                    + " FROM employee "
                    + " LEFT JOIN login ON login.employee_id = employee.id "
                    + " LEFT JOIN profile ON login.profile_id = profile.id "
                    + " LEFT JOIN menu ON profile.menu_id = menu.id "
                    + " WHERE employee.active = ?1 "
                    + " GROUP BY employee.id; ").
                    setParam(1, req.bools.get(0));
            System.out.println("xxxxxxxxxxxxxxxx");
            System.out.println(q.getParametrizedQuery());
            r.data = q.getRecords(conn);

            r.sortType = GridResult.SORT_ASC;
            r.sortColIndex = 3;

            return createResponse(r);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
