package api.mss.api;

import api.BaseAPI;
import api.MultiPartRequest;
import api.Params;
import api.mss.model.MssPoint;
import api.sys.model.SysCrudLog;
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
import web.fileManager;

@Path("/mssPoint")
public class MssPointApi extends BaseAPI {

    @POST
    public Response insert(MssPoint obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            if (MssPoint.existPointByCodeAndPost(obj.code, null, conn ,obj.postId)) {
                throw new Exception("Ya existe un punto con este QR, intente con otro");
            }

            if (obj.isCheck) {
                new MySQLQuery("update mss_point set is_check = 0 where post_id = ?1").setParam(1, obj.postId).executeUpdate(conn);
            }

            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/appPoint")
    public Response insertFromApp(@Context HttpServletRequest request) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);
            try {
                boolean isNew = !mr.params.containsKey("id");

                MssPoint obj = new MssPoint();
                obj.postId = MySQLQuery.getAsInteger(mr.params.get("postId"));
                obj.name = MySQLQuery.getAsString(mr.params.get("name"));
                obj.lat = MySQLQuery.getAsBigDecimal(mr.params.get("lat"), true);
                obj.lon = MySQLQuery.getAsBigDecimal(mr.params.get("lon"), true);
                obj.code = MySQLQuery.getAsString(mr.params.get("code"));
                obj.isCheck = MySQLQuery.getAsBoolean(mr.params.get("isCheck"));
                if (!isNew) {
                    obj.id = MySQLQuery.getAsInteger(mr.params.get("id"));
                }

                boolean existPoint = MssPoint.existPointByCodeAndPost(obj.code, (isNew ? null : obj.id), conn, obj.postId);
                if (existPoint) {
                    throw new Exception("Ya existe un punto con este QR, intente con otro");
                }

                if (obj.isCheck) {
                    new MySQLQuery("UPDATE mss_point SET is_check = 0 WHERE post_id = ?1").setParam(1, obj.postId).executeUpdate(conn);
                }

                if (isNew) {
                    obj.insert(conn);
                    SysCrudLog.created(this, obj, conn);
                } else {
                    MssPoint old = new MssPoint().select(obj.id, conn);
                    obj.update(conn);
                    SysCrudLog.updated(this, obj, old, conn);
                }

                if (mr.getFile() != null && mr.getFile().file != null) {
                    fileManager.upload(
                            sl.employeeId,
                            obj.id, //ownerId
                            null,//ownerType, 
                            "mss_point", //tableName
                            "punto.jpg", //fileName, 
                            "Foto de punto", //desc, 
                            true, //unique
                            true,//override
                            null,//shrinkType
                            pi, mr.getFile().file, conn
                    );
                }
                return Response.ok(obj).build();
            } finally {
                mr.deleteFiles();
            }            
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(MssPoint obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            if (MssPoint.existPointByCodeAndPost(obj.code, obj.id, conn, obj.postId)) {
                throw new Exception("Ya existe un punto con este QR, intente con otro");
            }
            if (obj.isCheck) {
                new MySQLQuery("update mss_point set is_check = 0 where post_id = ?1").setParam(1, obj.postId).executeUpdate(conn);
            }
            MssPoint old = new MssPoint().select(obj.id, conn);
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
            MssPoint obj = new MssPoint().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssPoint.delete(id, conn);
            SysCrudLog.deleted(this, MssPoint.class, id, conn);
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
            return createResponse(MssPoint.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getPoint")
    public Response getPoint(@QueryParam("postId") int postId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            List<MssPoint> listInfo = MssPoint.getList(new Params("post_id", postId), conn);
            return createResponse(listInfo);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/byRound")
    public Response getRound(@QueryParam("roundId") int roundId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            List<MssPoint> listInfo = MssPoint.getList(new MySQLQuery("SELECT " + MssPoint.getSelFlds("p") + " FROM "
                    + "mss_point p "
                    + "INNER JOIN mss_round_point rp ON rp.point_id = p.id "
                    + "WHERE rp.round_id = ?1 "
                    + "ORDER BY rp.place").setParam(1, roundId), conn);
            return createResponse(listInfo);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
