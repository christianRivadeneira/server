package api.emas.api;

import api.BaseAPI;
import api.emas.model.EmasCom;
import api.emas.model.EmasComClientDto;
import api.emas.model.EmasComSetClientsRequest;
import api.emas.model.EmasLog;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
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
import utilities.MySQLQuery;

@Path("/emasCom")
public class EmasComApi extends BaseAPI {

    @POST
    public Response insert(EmasCom obj) {
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
    public Response update(EmasCom obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            EmasCom old = new EmasCom().select(obj.id, conn);
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
            EmasCom obj = new EmasCom().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            EmasCom.delete(id, conn);
            SysCrudLog.deleted(this, EmasCom.class, id, conn);
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
            return createResponse(EmasCom.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/clients")
    public Response getClients(@QueryParam("comId") int comId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Object[][] data = new MySQLQuery("SELECT id, name FROM emas_client WHERE com_id = ?1 AND active").setParam(1, comId).getRecords(conn);
            List<EmasComClientDto> rta = new ArrayList<>();
            for (Object[] row : data) {
                EmasComClientDto c = new EmasComClientDto();
                c.id = MySQLQuery.getAsInteger(row[0]);
                c.name = MySQLQuery.getAsString(row[1]);
                rta.add(c);
            }
            return createResponse(rta);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/clients")
    public Response setClients(EmasComSetClientsRequest req) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            SessionLogin sl = getSession(conn);
            try {
                for (int i = 0; i < req.clientIds.size(); i++) {
                    int clieId = req.clientIds.get(i);
                    String origCom = new MySQLQuery("SELECT CONCAT('Se cambió el comercial: ',e.first_name, ' ',e.last_name) FROM "
                            + "emas_client cl "
                            + "INNER JOIN emas_com c ON c.id = cl.com_id "
                            + "INNER JOIN employee e ON e.id = c.emp_id "
                            + "WHERE cl.id = ?1;").setParam(1, clieId).getAsString(conn);
                    new MySQLQuery("update emas_client SET com_id = ?1 WHERE id = ?2").setParam(1, req.comId).setParam(2, clieId).executeUpdate(conn);
                    EmasLog log = new EmasLog();
                    log.employeeId = sl.employeeId;
                    log.logDate = new Date();
                    if (origCom != null) {
                        log.notes = "Se cambió al comercial " + origCom;
                    } else {
                        log.notes = "Se agregó comercial";
                    }
                    log.ownerId = clieId;
                    log.ownerType = 1;//log clientes
                    log.insert(conn);
                }
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
