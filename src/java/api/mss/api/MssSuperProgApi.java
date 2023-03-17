package api.mss.api;

import api.BaseAPI;
import api.Params;
import api.mss.app.GetPostList;
import api.mss.dto.SuperProgInfo;
import api.mss.model.MssGuard;
import api.mss.model.MssSuperPostPath;
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
import api.mss.model.MssSuperProg;
import api.mss.dto.MssPostApp;
import api.mss.dto.SuperProgEvent;
import api.mss.model.MssCfg;
import api.mss.model.MssPoint;
import static api.mto.tasks.MtoTripTasks.distance;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import utilities.Dates;
import utilities.MySQLQuery;

@Path("/mssSuperProg")
public class MssSuperProgApi extends BaseAPI {

    @POST
    public Response insert(MssSuperProg obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            Dates.validateOrder(obj.begDt, obj.endDt);

            if (obj.superId == 0) {//app
                Integer superId = MssGuard.getSuperIdFromEmployee(sl.employeeId, conn);
                if (superId == null) {
                    throw new Exception("No esta registrado como supervisor.\nComuniquese con el administrador");
                }
                obj.superId = superId;
            }

            boolean alreadyExist = new MySQLQuery("SELECT COUNT(*)>0 FROM mss_super_prog WHERE ((beg_dt BETWEEN DATE(?1) AND DATE(?2)) OR (end_dt BETWEEN DATE(?1) AND DATE(?2))) AND path_id = ?3 AND super_id = ?4 ")
                    .setParam(1, obj.begDt)
                    .setParam(2, obj.endDt)
                    .setParam(3, obj.pathId)
                    .setParam(4, obj.superId)
                    .getAsBoolean(conn);

            if (alreadyExist) {
                throw new Exception("La ruta selecionada ya se encuentra establecida para este fecha");
            }

            if (obj.pathId != null) {
                List<MssSuperPostPath> list = MssSuperPostPath.getList(new Params("path_id", obj.pathId), conn);
                for (MssSuperPostPath postPath : list) {
                    MssSuperProg newObj = new MssSuperProg();
                    newObj.begDt = obj.begDt;
                    newObj.endDt = obj.endDt;
                    newObj.postId = postPath.postId;
                    newObj.pathId = postPath.pathId;
                    newObj.superId = obj.superId;
                    newObj.insert(conn);
                }
            } else {
                obj.insert(conn);
                SysCrudLog.created(this, obj, conn);
            }
            return Response.ok().build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/eventual")
    public Response insertEventual(@QueryParam("postId") int postId) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            Integer superId = MssGuard.getSuperIdFromEmployee(sl.employeeId, conn);
            if (superId == null) {
                throw new Exception("No esta registrado como supervisor.\nComuniquese con el administrador");
            }

            Boolean alreadyExist = new MySQLQuery("SELECT COUNT(*) > 0 "
                    + "FROM mss_super_prog p "
                    + "WHERE p.post_id = ?1 "
                    + "AND (p.beg_dt = CURDATE() OR p.end_dt = CURDATE()) "
                    + "AND p.arrival_dt IS NULL ").setParam(1, postId).getAsBoolean(conn);

            if (alreadyExist) {
                throw new Exception("El puesto ya tiene una programación sin completar");
            }

            Date curDate = now(conn);
            MssSuperProg superProg = new MssSuperProg();
            superProg.begDt = curDate;
            superProg.endDt = curDate;
            superProg.postId = postId;
            superProg.pathId = null;
            superProg.superId = superId;
            superProg.insert(conn);

            SysCrudLog.created(this, superProg, conn);

            return Response.ok().build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(MssSuperProg obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            Dates.validateOrder(obj.begDt, obj.endDt);
            MssSuperProg old = new MssSuperProg().select(obj.id, conn);
            if (obj.pathId != null) {
                new MySQLQuery("UPDATE mss_super_prog "
                        + "SET "
                        + "path_id = ?1, beg_dt = ?2, end_dt = ?3, super_id = ?4 "
                        + "WHERE "
                        + "path_id = ?5 AND beg_dt = ?6 AND end_dt = ?7 AND super_id = ?8 ")
                        .setParam(1, obj.pathId).setParam(2, obj.begDt).setParam(3, obj.endDt).setParam(4, obj.superId)
                        .setParam(5, old.pathId).setParam(6, old.begDt).setParam(7, old.endDt).setParam(8, old.superId)
                        .executeUpdate(conn);
            } else {
                obj.update(conn);
                SysCrudLog.updated(this, obj, old, conn);
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
            MssSuperProg obj = new MssSuperProg().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssSuperProg obj = new MssSuperProg().select(id, conn);
            if (obj.pathId != null) {
                new MySQLQuery("DELETE FROM mss_super_prog WHERE path_id = ?1 AND "
                        + "beg_dt = ?2 AND end_dt = ?3 AND super_id ?4 ").setParam(1, obj.pathId)
                        .setParam(2, obj.begDt).setParam(3, obj.endDt).setParam(4, obj.superId).executeUpdate(conn);
            } else {
                MssSuperProg.delete(id, conn);
                SysCrudLog.deleted(this, MssSuperProg.class, id, conn);
            }
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    @Path("/progPost")
    public Response deleteProgPost(@QueryParam("progId") int progId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MssSuperProg obj = new MssSuperProg().select(progId, conn);
            if (obj != null) {
                MssSuperProg.delete(obj.id, conn);
                SysCrudLog.deleted(this, MssSuperProg.class, obj.id, conn);
            }
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
            return createResponse(MssSuperProg.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/bySupervisor")//programacion app
    public synchronized Response getPostsApp(@QueryParam("version") int version) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            List<MssPostApp> postClients = new ArrayList<>();
            if (version == 1) {
                postClients = GetPostList.GetListPostV1(sl.employeeId, conn);
            } else {
                throw new Exception("No implementado");
            }
            return createResponse(postClients, "suspDone");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/superProgInfo")
    public Response getsuperProgInfo() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            SuperProgInfo info = new SuperProgInfo();
            Integer superId = MssGuard.getSuperIdFromEmployee(sl.employeeId, conn);
            if (superId == null) {
                throw new Exception("No esta registrado como supervisor.\nComuniquese con el administrador");
            }
            info.hasProg = new MySQLQuery("SELECT COUNT(*) > 0 "
                    + "FROM mss_super_prog p "
                    + "WHERE CURDATE() BETWEEN p.beg_dt AND p.end_dt AND p.super_id = ?1 ")
                    .setParam(1, superId).getAsBoolean(conn);
            if (info.hasProg) {
                info.done = new MySQLQuery("SELECT COUNT(p.id) "
                        + "FROM mss_super_prog p "
                        + "WHERE CURDATE() BETWEEN p.beg_dt AND p.end_dt AND p.super_id = ?1 AND "
                        + "p.arrival_dt IS NOT NULL;")
                        .setParam(1, superId).getAsInteger(conn);

                info.total = new MySQLQuery("SELECT COUNT(*) "
                        + "FROM mss_super_prog p "
                        + "WHERE CURDATE() BETWEEN p.beg_dt AND p.end_dt AND p.super_id = ?1 ")
                        .setParam(1, superId).getAsInteger(conn);
            }

            return Response.ok(info).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/regSuperArrival")
    public Response regEvent(SuperProgEvent obj) {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                SessionLogin sl = getSession(conn);
                MssCfg cfg = new MssCfg().select(1, conn);

                Integer superId = MssGuard.getSuperIdFromEmployee(sl.employeeId, conn);
                if (superId == null) {
                    throw new Exception("No se encuentra registrado como supervisor");
                }

                MssPoint point = MssPoint.getPointByQrCodeAndPost(obj.code, obj.postId, conn);
                if (point == null) {
                    throw new Exception("No se encontro un punto con ese código QR");
                }

                if (!point.isCheck) {
                    throw new Exception("Esta escaneando un código que no es de check-in");
                }

                System.out.println("point location: [ " + point.lat.doubleValue() + "," + point.lon.doubleValue() + " ]");
                System.out.println("curt location:  [" + obj.lat.doubleValue() + "," + obj.lon.doubleValue() + " ]");

                double distance = distance(point.lat.doubleValue(), point.lon.doubleValue(), obj.lat.doubleValue(), obj.lon.doubleValue(), "K") * 1000;
                if (distance > cfg.pointRadiusTolerance) {
                    throw new Exception("No se encuentra ubicado en el punto de check in/out del puesto");
                }

                new MySQLQuery(" UPDATE mss_super_prog msp "
                        + " INNER JOIN( "
                        + " SELECT pr.id AS id "
                        + " FROM mss_point p1 "
                        + " LEFT JOIN mss_point p2 ON p2.code = p1.code AND p1.is_check AND p2.is_check "
                        + " INNER JOIN mss_post post1 ON post1.id = p1.post_id AND post1.id = ?1 "
                        + " LEFT JOIN mss_post post2 ON post2.id = p2.post_id AND post1.client_id = post2.client_id AND post1.id <> post2.id "
                        + " INNER JOIN mss_super_prog pr ON (pr.post_id = post1.id OR pr.post_id = post2.id) "
                        + " WHERE CURDATE() BETWEEN pr.beg_dt AND pr.end_dt AND pr.arrival_dt IS NULL AND pr.super_id = ?2 "
                        + " GROUP BY pr.id)aux SET msp.arrival_dt = NOW() "
                        + " WHERE msp.id = aux.id ").setParam(1, point.postId).setParam(2, superId).executeUpdate(conn);

                conn.commit();
                return createResponse();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
