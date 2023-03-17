package api.mss.api;

import api.BaseAPI;
import api.MultiPartRequest;
import api.mss.model.MssGuard;
import static api.mss.model.MssGuard.getFromEmployee;
import api.mss.model.MssPoint;
import api.mss.model.MssRound;
import api.mss.model.MssRoundPoint;
import static api.mto.tasks.MtoTripTasks.distance;
import api.sys.model.SysCrudLog;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import utilities.ServerNow;
import utilities.cast;
import web.fileManager;

@Path("/mssRoundPoint")
public class MssRoundPointApi extends BaseAPI {

    @POST
    public Response insert(MssRoundPoint obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(MssRoundPoint obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MssRoundPoint old = new MssRoundPoint().select(obj.id, conn);
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
            MssRoundPoint obj = new MssRoundPoint().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MssRoundPoint.delete(id, conn);
            SysCrudLog.deleted(this, MssRoundPoint.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/check")
    public Response check(@Context HttpServletRequest request) {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                SessionLogin sl = getSession(conn);

                Integer guardId = getFromEmployee(sl.employeeId, MssGuard.TYPE_GUARD, conn);
                if (guardId == null) {
                    throw new Exception("No se encuentra registrado como guarda de seguridad");
                }

                fileManager.PathInfo pi = new fileManager.PathInfo(conn);
                MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);
                try {
                    String code = mr.params.get("postCode");
                    int roundId = MySQLQuery.getAsInteger(mr.params.get("roundId"));

                    BigDecimal lat = MySQLQuery.getAsBigDecimal(mr.params.get("lat"), true);
                    BigDecimal lon = MySQLQuery.getAsBigDecimal(mr.params.get("lon"), true);

                    MssRound round = new MssRound().select(roundId, conn);
                    List<MssPoint> points = MssPoint.getPointsByQrCode(code, conn);

                    if (MySQLQuery.isEmpty(points)) {
                        throw new Exception("El código no corresponde a ningun puesto");
                    }
                    MssPoint point = points.get(0);

                    if (!guardId.equals(round.guardId)) {
                        throw new Exception("No le corresponde realizar esta ronda");
                    }

                    Boolean isValidPoint = new MySQLQuery("SELECT COUNT(*) > 0 "
                            + "FROM mss_round r "
                            + "INNER JOIN mss_round_point rp ON rp.round_id = r.id "
                            + "INNER JOIN mss_point p ON p.id = rp.point_id "
                            + "WHERE p.code = ?1 "
                            + "AND r.id = ?2; ").setParam(1, code).setParam(2, roundId).getAsBoolean(conn);

                    if (!isValidPoint) {
                        throw new Exception("El código escaneado no pertenece a la ronda actual");
                    }

                    Object[] curPointRow = new MySQLQuery("SELECT p.code, rp.id, p.name FROM "
                            + "mss_round_point rp "
                            + "INNER JOIN mss_point p ON rp.point_id = p.id "
                            + "WHERE rp.round_id = ?1 AND rp.dt IS NULL "
                            + "ORDER BY rp.place ASC LIMIT 1").setParam(1, roundId).getRecord(conn);

                    if (curPointRow == null) {
                        throw new Exception("La ronda está completa");
                    }

                    String curPointCode = cast.asString(curPointRow, 0);
                    int curRoundPointId = cast.asInt(curPointRow, 1);
                    String curPointName = cast.asString(curPointRow, 2);

                    if (!curPointCode.equals(point.code)) {
                        throw new Exception("El punto que se espera es " + curPointName);
                    }

                    double distance = distance(point.lat.doubleValue(), point.lon.doubleValue(), lat.doubleValue(), lon.doubleValue(), "K") * 1000;
                    if (distance > 20) {
                        throw new Exception("No se encuentra ubicado en el punto " + point.name);
                    }

                    Integer firstRpId = new MySQLQuery("SELECT rp.id FROM mss_round_point rp "
                            + "WHERE rp.place = (SELECT MIN(i.place) from mss_round_point i WHERE i.round_id = ?1) AND rp.round_id = ?1").setParam(1, roundId).getAsInteger(conn);

                    Integer lastRpId = new MySQLQuery("SELECT rp.id FROM mss_round_point rp "
                            + "WHERE rp.place = (SELECT MAX(i.place) from mss_round_point i WHERE i.round_id = ?1) AND rp.round_id = ?1").setParam(1, roundId).getAsInteger(conn);

                    if (firstRpId == curRoundPointId) {
                        MssRound r = new MssRound().select(roundId, conn);
                        r.begDt = new ServerNow();
                        r.update(conn);
                    }

                    if (lastRpId == curRoundPointId) {
                        MssRound r = new MssRound().select(roundId, conn);
                        r.endDt = new ServerNow();
                        r.update(conn);
                    }

                    MssRoundPoint rp = new MssRoundPoint().select(curRoundPointId, conn);
                    rp.lat = lat;
                    rp.lon = lon;
                    rp.dt = new ServerNow();
                    rp.update(conn);

                    fileManager.upload(
                            sl.employeeId,
                            rp.id, //ownerId
                            null,//ownerType, 
                            "mss_round_point", //tableName
                            "codigo.jpg", //fileName, 
                            "codigo.jpg", //desc, 
                            false, //unique
                            null,//shrinkType
                            pi,
                            mr.getFile().file,
                            conn
                    );
                    conn.commit();
                    return createResponse();
                } finally {
                    mr.deleteFiles();
                }
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
