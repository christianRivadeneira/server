package api.ess.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.ess.model.EssBuildAdmin;
import api.ess.model.EssBuilding;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import api.ess.model.EssPerson;
import api.ess.model.EssPersonUnit;
import api.ess.model.EssUnit;
import api.ess.model.dto.EssLoginInfo;
import api.ess.model.dto.PasswordRequest;
import api.ess.utils.SendRecoveryMail;
import api.sys.model.Employee;
import api.sys.model.LoginRequest;
import api.sys.model.LoginResponse;
import api.sys.model.SysCrudLog;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import metadata.model.GridRequest;
import model.menu.Credential;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import utilities.apiClient.IntegerResponse;

@Path("/essPerson")
public class EssPersonApi extends BaseAPI {

    @POST
    public Response insert(EssPerson obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            if (!MySQLQuery.isEmpty(obj.mail)) {
                Boolean existMail = new MySQLQuery("SELECT COUNT(*)>0 "
                        + "FROM ess_person p WHERE UPPER(p.mail) = ?1 ")
                        .setParam(1, obj.mail).getAsBoolean(conn);
                if (existMail) {
                    throw new Exception("El correo ya se encuentra en uso por otro habitante");
                }
            }

            Integer buildId = obj.buildId;
            obj.buildId = null;

            Integer unitId = obj.unitId;
            String callPriotity = obj.callPriotity;
            Boolean notify = obj.notify;

            obj.unitId = null;
            obj.callPriotity = null;
            obj.notify = null;

            EssPerson old = EssPerson.getByDoc(obj.document, conn);
            try {
                conn.setAutoCommit(false);
                if (old == null) {
                    Employee e = new Employee();
                    e.document = obj.document;
                    e.firstName = obj.firstName;
                    e.lastName = obj.lastName;
                    if (!MySQLQuery.isEmpty(obj.mail)) {
                        e.login = obj.mail;
                        e.mail = obj.mail;
                    }
                    e.active = true;
                    e.insert(conn);
                    obj.empId = e.id;
                    obj.insert(conn);
                    SysCrudLog.created(this, obj, conn);
                    conn.commit();
                } else {
                    old.document = obj.document;
                    old.firstName = obj.firstName;
                    old.lastName = obj.lastName;
                    old.phone = obj.phone;
                    old.mail = obj.mail;
                    old.fPrintCode = obj.fPrintCode;
                    old.update(conn);

                    Employee e = new Employee().select(old.empId, conn);
                    e.document = obj.document;
                    e.firstName = obj.firstName;
                    e.lastName = obj.lastName;
                    if (!MySQLQuery.isEmpty(obj.mail)) {
                        e.login = obj.mail;
                    }
                    e.active = true;
                    e.update(conn);
                    obj.id = old.id;
                }

                if (buildId != null) {
                    EssBuildAdmin a = new EssBuildAdmin();
                    a.buildId = buildId;
                    a.personId = obj.id;
                    a.insert(conn);
                }

                if (unitId != null) {
                    EssPersonUnit pu = new EssPersonUnit();
                    pu.unitId = unitId;
                    pu.personId = obj.id;
                    pu.callPriority = callPriotity;
                    pu.notify = notify;
                    pu.insert(conn);
                    EssUnit.updateCache(unitId, conn);
                }
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(EssPerson obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            if (!MySQLQuery.isEmpty(obj.mail)) {
                Boolean existMail = new MySQLQuery("SELECT COUNT(*)>0 "
                        + "FROM ess_person p WHERE UPPER(p.mail) = ?1 AND "
                        + "p.id <> ?2")
                        .setParam(1, obj.mail).setParam(2, obj.id).getAsBoolean(conn);
                if (existMail) {
                    throw new Exception("El correo ya se encuentra en uso por otro habitante");
                }
            }

            EssPerson old = new EssPerson().select(obj.id, conn);
            Employee oldEmp = null;
            if (old != null && obj.empId != null) {
                oldEmp = new Employee().select(obj.empId, conn);
            }

            obj.buildId = null;

            Integer unitId = obj.unitId;
            String callPriotity = obj.callPriotity;
            Boolean notify = obj.notify;
            //estos campos se van a manejar desde otra tabla, por eso no tiene logs
            obj.unitId = null;
            obj.callPriotity = null;
            obj.notify = null;
            obj.update(conn);

            if (unitId != null) {
                EssPersonUnit pu = EssPersonUnit.getByUnit(unitId, old.id, conn);
                pu.callPriority = callPriotity;
                pu.notify = notify;
                pu.update(conn);
                EssUnit.updateCache(unitId, conn);
            }

            if (old != null) {
                SysCrudLog.updated(this, obj, old, conn);

                old.document = obj.document;
                old.firstName = obj.firstName;
                old.lastName = obj.lastName;
                old.phone = obj.phone;
                old.mail = obj.mail;
                old.update(conn);

                if (oldEmp != null) {
                    Employee newEmp = new Employee().select(old.empId, conn);
                    newEmp.document = obj.document;
                    newEmp.firstName = obj.firstName;
                    newEmp.lastName = obj.lastName;
                    if (!MySQLQuery.isEmpty(obj.mail)) {
                        newEmp.login = obj.mail;
                        newEmp.mail = obj.mail;
                    }
                    newEmp.active = true;
                    newEmp.update(conn);
                    SysCrudLog.updated(this, newEmp, oldEmp, conn);
                }
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
            EssPerson obj = new EssPerson().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            EssPerson.delete(id, conn);
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
            return createResponse(EssPerson.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest req, @Context HttpServletRequest sr, @Context ServletContext c) {
        try {
            LoginResponse rta = new LoginResponse();
            Credential cr;
            try (Connection con = MySQLCommon.getConnection(req.poolName, req.tz)) {
                cr = req.toCredential(c, sr);
                rta.employee = new Employee().select(cr.getEmployeeId(), con);
                //verificar si tiene acceso al app
                Boolean hasUnit = new MySQLQuery("SELECT COUNT(*) > 0 "
                        + "FROM ess_person p "
                        + "INNER JOIN ess_person_unit pu ON pu.person_id = p.id "
                        + "WHERE p.emp_id = " + rta.employee.id).getAsBoolean(con);

                Boolean hasBuild = new MySQLQuery("SELECT COUNT(*) > 0 "
                        + "FROM ess_person p "
                        + "INNER JOIN ess_build_admin ba ON ba.person_id = p.id "
                        + "WHERE p.emp_id = " + rta.employee.id).getAsBoolean(con);

                if (!hasBuild && !hasUnit) {
                    throw new Exception("No tiene permisos para acceder a la aplicación, Comuniquese con Seguridad del Sur.");
                }
            }
            rta.sessionId = cr.getSessionId();

            return Response.ok(rta).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/logout")
    public Response logout(@QueryParam("empId") Integer empId) {
        try (Connection conn = getConnection()) {
            SessionLogin sess = getSession(conn);

            if (empId == null) {
                empId = sess.employeeId;
            }
            Integer sessions = Employee.closeSessions(empId, conn);
            new MySQLQuery("DELETE FROM ess_fcm_token s WHERE usr_id = " + empId).executeDelete(conn);

            IntegerResponse rta = new IntegerResponse(sessions);
            return Response.ok(rta).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/generatePIN")
    public Response generatePIN(PasswordRequest obj) {
        try (Connection conn = MySQLCommon.getConnection(obj.poolName, obj.tz)) {
            if (obj.mail == null || obj.mail.length() == 0) {
                throw new Exception("No se encontro un email valido");
            }

            EssPerson usr = EssPerson.getFromMail(obj.mail, conn);
            String randomKey = "";
            while (randomKey.length() < 4) {
                int c = (int) Math.floor(Math.random() * 11);
                randomKey += c + "";
            }
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(new Date());
            gc.add(GregorianCalendar.HOUR_OF_DAY, 1);
            usr.recoveryPin = randomKey;
            usr.recoveryExp = gc.getTime();
            usr.recoveryAccepted = false;

            usr.update(conn);
            SendRecoveryMail.sendMail(usr, conn);

            return Response.ok().build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/confirmPIN")
    public Response confirmPIN(PasswordRequest obj) {
        try (Connection conn = MySQLCommon.getConnection(obj.poolName, obj.tz)) {
            if (obj.mail == null || obj.mail.length() == 0) {
                throw new Exception("No se encontro un email valido");
            }

            String recPIN = obj.pin.toUpperCase();
            EssPerson usr = EssPerson.getFromMail(obj.mail, conn);
            if (usr.recoveryPin == null) {
                throw new Exception("No se ha iniciado el proceso de recuperación");
            } else if (usr.recoveryExp.compareTo(new Date()) < 0) {
                throw new Exception("El PIN ha expirado.");
            } else if (!usr.recoveryPin.equals(recPIN)) {
                Thread.sleep(3000);
                throw new Exception("El PIN no coincide.");
            } else {
                usr.recoveryAccepted = true;
                usr.update(conn);
                return Response.ok().build();
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/setPassword")
    public Response setPassword(PasswordRequest obj) {
        try (Connection conn = MySQLCommon.getConnection(obj.poolName, obj.tz)) {
            EssPerson usr = EssPerson.getFromMail(obj.mail, conn);
            if (usr.recoveryPin == null) {
                throw new Exception("No se ha iniciado el proceso de recuperación");
            } else if (!usr.recoveryAccepted) {
                throw new Exception("El PIN no ha sido confirmado.");
            } else if (usr.recoveryExp.compareTo(new Date()) < 0) {
                throw new Exception("El PIN no ha expirado.");
            }

            Employee emp = new Employee().select(usr.empId, conn);
            emp.password = obj.password;
            emp.update(conn);

            return Response.ok().build();

        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/essLoginInfo")
    public Response essLoginInfo() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            EssLoginInfo rta = new EssLoginInfo();

            MySQLQuery mq = new MySQLQuery("SELECT " + EssBuilding.getSelFlds("b")
                    + " FROM ess_building b "
                    + " INNER JOIN ess_build_admin ba ON ba.build_id = b.id "
                    + " INNER JOIN ess_person p ON p.id = ba.person_id "
                    + " WHERE p.emp_id = " + sl.employeeId);

            rta.buildings = EssBuilding.getList(mq, conn);
            rta.isAdmin = rta.buildings != null && !rta.buildings.isEmpty();

            mq = new MySQLQuery("SELECT " + EssUnit.getSelFlds("u")
                    + " FROM ess_unit u "
                    + " INNER JOIN ess_person_unit pu ON pu.unit_id = u.id "
                    + " INNER JOIN ess_person p ON p.id = pu.person_id "
                    + " WHERE p.emp_id = " + sl.employeeId);

            rta.units = EssUnit.getList(mq, conn);
            rta.isResident = rta.units != null && !rta.units.isEmpty();

            return createResponse(rta);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/build")
    public Response getByBuild(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            EssBuildAdmin ba = new EssBuildAdmin().select(id, conn);
            EssPerson p = new EssPerson().select(ba.personId, conn);
            return createResponse(p);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/unit")
    public Response getByUnit(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            EssPersonUnit ba = new EssPersonUnit().select(id, conn);
            EssPerson p = new EssPerson().select(ba.personId, conn);
            p.callPriotity = ba.callPriority;
            p.notify = ba.notify;
            return createResponse(p);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/profilesEss")
    public Response profilesEss(GridRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult r = new GridResult();
            r.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 75, "Nombre")};

            r.data = new MySQLQuery("SELECT id, name "
                    + "FROM profile "
                    + "WHERE id <> 1 AND "
                    + "menu_id= 2254 AND active = 1 "
                    + "AND is_mobile = false ORDER BY name ASC "
            ).getRecords(conn);

            r.sortType = GridResult.SORT_ASC;
            r.sortColIndex = 1;

            return createResponse(r);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
