package api.mss.api;

import api.BaseAPI;
import api.GridResult;
import api.MultiPartRequest;
import api.MySQLCol;
import api.mss.model.MssRound;
import api.mss.dto.ReviewInfo;
import api.mss.dto.RoundPointApp;
import api.mss.model.MssRoundProg;
import api.sys.model.SysCrudLog;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import metadata.model.GridRequest;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import utilities.Reports;
import utilities.apiClient.StringResponse;
import utilities.cast;
import utilities.json.JSONDecoder;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.Table;
import web.fileManager;

@Path("/mssRound")
public class MssRoundApi extends BaseAPI {

    @POST
    public Response insert(MssRound obj) {
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
    public Response update(MssRound obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssRound old = new MssRound().select(obj.id, conn);
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
            getSession(conn);
            MssRound obj = new MssRound().select(id, conn);
            MssRoundProg roundProg = new MssRoundProg().select(obj.roundProgId, conn);
            obj.roundName = roundProg.name;
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/openRound")
    public Response getOpenRound(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            MySQLQuery q = new MySQLQuery("SELECT " + MssRound.getSelFlds("") + ",id "
                    + "FROM mss_round WHERE "
                    + "end_dt IS NULL AND id = ?1").setParam(1, id);
            MssRound obj = new MssRound().select(q, conn);
            if (obj != null) {
                MssRoundProg roundProg = new MssRoundProg().select(obj.roundProgId, conn);
                obj.roundName = roundProg.name;
            }
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/cancelRound")
    public Response cancelRound(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MssRound obj = new MssRound().select(id, conn);
            obj.begDt = now(conn);
            obj.endDt = now(conn);
            obj.update(conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/closeRound")
    public Response closeRound(MssRound obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MssRound old = new MssRound().select(obj.id, conn);
            old.roundName = null;            
            obj.closeDt = now(conn);
            obj.roundName = null;
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }     
    
    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssRound.delete(id, conn);
            SysCrudLog.deleted(this, MssRound.class, id, conn);
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
            return createResponse(MssRound.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/pendingRound")
    public Response pendingRound() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            ReviewInfo pendingRound = MssRound.getPendingRound(conn);
            return Response.ok(pendingRound).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/roundStatus")
    public Response roundStatus(@QueryParam("id") int id, @QueryParam("status") String status) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            if (!status.equals("ok") && !status.equals("fail")) {
                throw new Exception("invalid status");
            }
            MssRound obj = new MssRound().select(id, conn);
            obj.chkStatus = (status.equals("ok") ? "ok" : "fail");
            obj.update(conn);

            ReviewInfo pendingRound = MssRound.getPendingRound(conn);
            return Response.ok(pendingRound).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/incompleteRounds")
    public Response getUnchecked(GridRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            GridResult tbl = getIncompleteRoundsGrid(conn);
            return createResponse(tbl);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Produces(value = "application/vnd.ms-excel")
    @Path("/incompleteRounds")
    public Response exportUnchecked(GridRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult r = getIncompleteRoundsGrid(conn);
            MySQLReport rep = new MySQLReport("Rondas incompletas", null, "hoja1", now(conn));
            rep.setVerticalFreeze(5);
            rep.setZoomFactor(80);
            rep.setShowNumbers(true);
            rep.setMultiRowTitles(true);
            rep.getTables().add(new Table("Rondas incompleta"));

            rep = MySQLReport.getReport(rep, r.cols, r.data);
            return createResponse(rep.write(conn), "Rondas incompleta.xls");

        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    private GridResult getIncompleteRoundsGrid(Connection conn) throws Exception {
        GridResult tbl = new GridResult();
            tbl.data = new MySQLQuery("SELECT "
                    + "r.id, "
                    + "c.name, "
                    + "prog.name, "
                    + "r.reg_dt, "
                    + "CONCAT(g.first_name, ' ', g.last_name) "
                    + "FROM mss_round r "
                    + "INNER JOIN mss_round_prog prog ON prog.id = r.round_prog_id "
                    + "INNER JOIN mss_post p ON p.id = prog.post_id "
                    + "INNER JOIN mss_client c ON c.id = p.client_id "
                    + "INNER JOIN mss_guard g ON g.id = r.guard_id "
                    + "LEFT JOIN mss_round_point rp ON rp.round_id = r.id "
                    + "WHERE "
                    + "r.close_dt IS NULL AND (r.end_dt IS NULL "
                    + "OR ((SELECT COUNT(*)>0 FROM mss_round_point rpaux WHERE rpaux.round_id = r.id AND rpaux.dt IS NOT NULL) = 0) "
                    + "AND (TRUE = IF(r.tolerance IS NOT NULL AND r.tolerance > 0 , (SELECT TIMESTAMPDIFF(MINUTE, r.reg_dt,r.end_dt)) > r.tolerance, 0))) "
                    + "GROUP BY r.id "
            ).getRecords(conn);

            tbl.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 40, "Cliente"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 45, "Ronda"),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_A, 25, "Fecha"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 50, "Guarda")};
            return tbl;
    }

    @GET
    @Path("/roundPointsOffline")
    public Response getRoundPointsOffline(@QueryParam("roundId") Integer roundId) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            MssRound round = new MssRound().select(roundId, conn);
            if (round == null) {
                throw new Exception("La ronda no existe");
            }

            roundId = new MySQLQuery("SELECT max(r.id) FROM mss_round r WHERE r.guard_id = ?1 AND round_prog_id = ?2 ")
                    .setParam(1, round.guardId).setParam(2, round.roundProgId).getAsInteger(conn);

            Object[][] data = new MySQLQuery("SELECT p.id, r.id, p.place, prog.post_id, pt.name, pt.code, pt.lat, pt.lon "
                    + "FROM mss_round r "
                    + "INNER JOIN mss_round_point p ON p.round_id = r.id "
                    + "INNER JOIN mss_point pt ON pt.id = p.point_id "
                    + "INNER JOIN mss_round_prog prog ON prog.id = r.round_prog_id "
                    + "WHERE r.id = ?1 "
                    + "ORDER BY p.place ASC ").setParam(1, roundId).getRecords(conn);

            List<RoundPointApp> listPoints = new ArrayList<>();
            if (data != null && data.length > 0) {
                for (Object[] obj : data) {
                    RoundPointApp rp = new RoundPointApp();
                    rp.id = cast.asInt(obj, 0);
                    rp.roundId = cast.asInt(obj, 1);
                    rp.postId = cast.asInt(obj, 2);
                    rp.place = cast.asInt(obj, 3);
                    rp.pointName = cast.asString(obj, 4);
                    rp.code = cast.asString(obj, 5);
                    rp.lat = cast.asBigDecimal(obj, 6);
                    rp.lon = cast.asBigDecimal(obj, 7);
                    listPoints.add(rp);
                }
            }

            return createResponse(listPoints, "listPoints");

        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/syncRoundPointsOffline")
    public Response syncRoundPointsOffline(@Context HttpServletRequest request) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);

            ByteArrayOutputStream baos;
            try (FileInputStream fis = new FileInputStream(mr.getFile().file); GZIPInputStream giz = new GZIPInputStream(fis)) {
                baos = new ByteArrayOutputStream();
                Reports.copy(giz, baos);
                baos.close();
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            List<RoundPointApp> data = new JSONDecoder().getList(bais, RoundPointApp.class);

            StringResponse sr = new StringResponse();

            if (!MySQLQuery.isEmpty(data)) {

                for (RoundPointApp rp : data) {
                    new MySQLQuery("UPDATE mss_round_point SET dt = ?1, lat = ?2, lon = ?3 WHERE id = ?4")
                            .setParam(1, rp.regDate)
                            .setParam(2, rp.curLat)
                            .setParam(3, rp.curLon)
                            .setParam(4, rp.id).executeUpdate(conn);
                }

                new MySQLQuery("UPDATE mss_round r "
                        + "INNER JOIN( "
                        + "SELECT r.id AS round_id, MIN(p1.dt) AS min_dt, MAX(p1.dt) AS max_dt, COUNT(DISTINCT pnull.id)>0 AS is_null "
                        + "FROM mss_round r "
                        + "INNER JOIN mss_round_point p1 ON p1.round_id = r.id "
                        + "LEFT JOIN mss_round_point pnull ON pnull.round_id = r.id AND pnull.dt IS NULL "
                        + "WHERE  "
                        + "r.beg_dt IS NULL AND r.end_dt IS NULL "
                        + "GROUP BY r.id "
                        + ") as aux_point on aux_point.round_id = r.id "
                        + "SET  "
                        + "r.beg_dt = aux_point.min_dt, "
                        + "r.end_dt = IF(aux_point.is_null, NULL, aux_point.max_dt) ").executeUpdate(conn);

                sr.msg = "ok";
            } else {
                sr.msg = "fail";
            }
            mr.deleteFiles();
            return Response.ok(sr).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
