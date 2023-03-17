package api.mss.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.mss.model.MssCfg;
import api.mss.model.MssChallenge;
import api.sys.model.SystemApp;
import java.sql.Connection;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import metadata.model.GridRequest;
import utilities.MySQLQuery;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.Table;
import web.push.GCMUtils;

@Path("/mssChallenge")
public class MssChallengeApi extends BaseAPI {

    @PUT
    public Response update(MssChallenge obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MssChallenge oldObj = new MssChallenge().select(obj.id, conn);
            if (oldObj != null) {
                oldObj.answerDt = now(conn);
                oldObj.regResult = obj.regResult;
                oldObj.update(conn);
            } else {
                throw new Exception("No se encontró el reto");
            }
            return createResponse(oldObj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MssChallenge obj = new MssChallenge().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/sender")
    public Response sender(@QueryParam("empId") int empId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            int appId = SystemApp.getByPkgName("com.qualisys.minutas", conn).id;

            Integer shiftId = new MySQLQuery("SELECT s.id FROM "
                    + "mss_shift s "
                    + "INNER JOIN mss_guard g ON g.id = s.guard_id "
                    + "WHERE "
                    + "NOW() BETWEEN s.exp_beg AND s.exp_end "
                    + "AND s.reg_beg IS NOT NULL AND g.emp_id = ?1").setParam(1, empId).getAsInteger(conn);

            if (shiftId == null) {
                throw new Exception("El guarda no tiene un turno activo");
            }

            MssChallenge.sendChallenge(appId, shiftId, empId, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/test")
    public Response test() {
        try (Connection conn = getConnection()) {

            JsonObjectBuilder ob = Json.createObjectBuilder();
            ob.add("challengeId", 1);
            ob.add("type", "challenge");
            JsonObject json = ob.build();
            GCMUtils.sendToAppAsync(20, json, "4678");
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/unchecked")
    public Response getUnchecked(GridRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult tbl = getIncompleteChallengeGrid(conn);
            return createResponse(tbl);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Produces(value = "application/vnd.ms-excel")
    @Path("/unchecked")
    public Response exportUnchecked(GridRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult r = getIncompleteChallengeGrid(conn);
            MySQLReport rep = new MySQLReport("Retos incompletos", null, "hoja1", now(conn));
            rep.setVerticalFreeze(5);
            rep.setZoomFactor(80);
            rep.setShowNumbers(true);
            rep.setMultiRowTitles(true);
            rep.getTables().add(new Table("Retos incompletos"));

            rep = MySQLReport.getReport(rep, r.cols, r.data);
            return createResponse(rep.write(conn), "Retos incompletos.xls");

        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private GridResult getIncompleteChallengeGrid(Connection conn) throws Exception {
        MssCfg cfg = new MssCfg().select(1, conn);
        GridResult tbl = new GridResult();
        tbl.data = new MySQLQuery("SELECT "
                + "c.id,  "
                + "s.exp_beg, "
                + "g.document, "
                + "CONCAT(g.first_name, ' ' ,g.last_name), "
                + "CONCAT(mss_client.`name`, ' - ', p.`name`), "
                + "c.exp_result, "
                + "c.reg_result, "
                + "ROUND(TIME_TO_SEC(TIMEDIFF(c.answer_dt, c.question_dt)) / 60, 0) "
                + "FROM "
                + "mss_challenge AS c "
                + "INNER JOIN mss_shift AS s ON c.shift_id = s.id "
                + "INNER JOIN mss_guard AS g ON s.guard_id = g.id "
                + "INNER JOIN mss_post AS p ON s.post_id = p.id "
                + "INNER JOIN mss_client ON p.client_id = mss_client.id "
                + "WHERE "
                + "((c.question_dt < NOW() AND c.reg_result IS NULL) OR (c.exp_result <> c.reg_result) "
                + (cfg.challengeTolerance == 0 ? " " : "OR (TIME_TO_SEC(TIMEDIFF(c.answer_dt, c.question_dt)) / 60) > ?1 ")
                + ") AND c.checked = 0; "
        ).setParam(1, cfg.challengeTolerance).getRecords(conn);;
        tbl.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_KEY),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 30, "Fecha"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 38, "Documento"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 70, "Nombres"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 130, "Puesto"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 25, "Resultado\nEsperado"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 25, "Resultado\nRegistrado"),
            new MySQLCol(MySQLCol.TYPE_INTEGER, 25, "Duración\n(Minutos)"),};
        return tbl;
    }
}
