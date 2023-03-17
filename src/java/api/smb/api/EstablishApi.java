package api.smb.api;

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
import api.smb.model.Establish;
import java.math.BigDecimal;
import utilities.MySQLQuery;

@Path("/establish")
public class EstablishApi extends BaseAPI {

    @POST
    public Response insert(Establish obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            if (obj.establishId == null) { // tipo de establecimiento
                BigDecimal num = new MySQLQuery("SELECT count(*) FROM establish AS o WHERE o.name LIKE ?1 AND o.establish_id IS NULL").setParam(1, "%" + obj.name + "%").getAsBigDecimal(conn, true);

                if (num.intValue() > 0) {
                    throw new Exception("Ya existe un tipo de establecimiento con el mismo nombre");
                }
            } else {// estableciemiento
                BigDecimal num = new MySQLQuery("SELECT count(*) FROM establish AS o WHERE o.name LIKE ?1 AND o.establish_id = ?2").setParam(1, "%" + obj.name + "%").setParam(2, obj.establishId).getAsBigDecimal(conn, true);

                if (num.intValue() > 0) {
                    throw new Exception("Ya existe un establecimiento con el mismo nombre");
                }
            }

            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(Establish obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            Establish old = new Establish().select(obj.id, conn);

            if (old == null) {
                throw new Exception("El registro que intenta modificar ha sido removido por otro usuario");
            }

            if (old.establishId == null) {// tipo de establecimiento
                BigDecimal num = new MySQLQuery("SELECT count(*) FROM establish AS o WHERE o.name LIKE ?1 AND o.establish_id IS NULL AND o.id <> ?2").setParam(1, "%" + obj.name + "%").setParam(2, obj.id).getAsBigDecimal(conn, true);

                if (num.intValue() > 0) {
                    throw new Exception("Ya existe un tipo de establecimiento con el mismo nombre");
                }
            } else {//subEstablish
                BigDecimal num = new MySQLQuery("SELECT count(*) FROM establish AS o WHERE o.name LIKE ?1 AND o.establish_id = ?2 AND o.id <> ?3").setParam(1, "%" + obj.name + "%").setParam(2, obj.establishId).setParam(3, obj.id).getAsBigDecimal(conn, true);

                if (num.intValue() > 0) {
                    throw new Exception("Ya existe un establecimiento con el mismo nombre");
                }
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
            SessionLogin sl = getSession(conn);
            Establish obj = new Establish().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            BigDecimal num = new MySQLQuery("SELECT count(*) FROM establish AS o WHERE o.establish_id = ?1").setParam(1, id).getAsBigDecimal(conn, true);
            if (num.intValue() > 0) {
                throw new Exception("Existen establecimientos asociados a este tipo.");
            }

            num = new MySQLQuery("SELECT count(*) FROM contract AS o WHERE o.establish_id = ?1").setParam(1, id).getAsBigDecimal(conn, true);
            if (num.intValue() > 0) {
                throw new Exception("Existen contratos de marca registrados con este tipo de establecimiento.");
            }

            num = new MySQLQuery("SELECT count(*) FROM ord_contract AS o WHERE o.establish_id = ?1").setParam(1, id).getAsBigDecimal(conn, true);
            if (num.intValue() > 0) {
                throw new Exception("Existen contratos provisionales registrados con este tipo de establecimiento.");
            }

            Establish.delete(id, conn);
            SysCrudLog.deleted(this, Establish.class, id, conn);
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
            return createResponse(Establish.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getAllSuperEstablishs")
    public Response getAllSuperEstablishs() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(Establish.getAllSuperEstablishs(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getAllSubEstablishs")
    public Response getAllSubEstablishs(@QueryParam("establishId") Integer establishId) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(Establish.getAllSubEstablishs(conn, establishId));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @GET
    @Path("/findEstablish")
    public Response findEstablish(@QueryParam("establishId") Integer establishId) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(Establish.findEstablish(conn, establishId));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
