package api.mss.api;

import api.BaseAPI;
import api.mss.dto.DetailMinuteDto;
import api.mss.rpt.MinutesReport;
import java.sql.Connection;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import utilities.Dates;
import utilities.mysqlReport.MySQLReport;

@Path("/minutesReport")
public class MinutesReportApi extends BaseAPI {

    @POST
    @Path("/detailArrivals")
    public Response getDetailArrivals(DetailMinuteDto req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Dates.validateOrder(req.begDt, req.endDt);
            MySQLReport rep = MinutesReport.getDetailArrivals(req.begDt, req.endDt, req.clientId, req.postId, req.guardId, req.type, conn);
            return createResponse(rep.write(conn), "rpt_detallado_marcacion.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/shiftNolvelty")
    public Response getShiftNolvelty(DetailMinuteDto req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Dates.validateOrder(req.begDt, req.endDt);
            MySQLReport rep = MinutesReport.getShiftNolvelty(req.begDt, req.endDt, req.clientId, req.postId, req.state, conn);
            return createResponse(rep.write(conn), "rpt_novs_turnos.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/superReviewFinding")
    public Response getSuperReviewFinding(DetailMinuteDto req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Dates.validateOrder(req.begDt, req.endDt);
            MySQLReport rep = MinutesReport.getSuperReviewFinding(req.begDt, req.endDt, req.guardId, req.findingId, req.clientId, req.postId, conn);
            return createResponse(rep.write(conn), "rpt_supervision.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/superProgCompliance")
    public Response getSuperProgCompliance(DetailMinuteDto req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Dates.validateOrder(req.begDt, req.endDt);
            MySQLReport rep = MinutesReport.getSuperProgCompliance(req.begDt, req.endDt, req.superId, req.state, req.clientId, req.postId, conn);
            return createResponse(rep.write(conn), "rpt_cumplimiento_programacion.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/failRounds")
    public Response getFailRounds(DetailMinuteDto req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Dates.validateOrder(req.begDt, req.endDt);
            MySQLReport rep = MinutesReport.getFailRounds(req.begDt, req.endDt, req.clientId, req.postId, req.guardId, req.state, conn);
            return createResponse(rep.write(conn), "rpt_rondas.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
