package api.jss.api;

import api.BaseAPI;
import api.jss.dto.DetailAgentDto;
import api.jss.rpt.AgentReport;
import java.sql.Connection;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import utilities.Dates;
import utilities.mysqlReport.MySQLReport;

@Path("/agentReport")
public class AgentReportApi extends BaseAPI {

    @POST
    @Path("/rptAverageTime")
    public Response getRptAverageTime(DetailAgentDto req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Dates.validateOrder(req.begDt, req.endDt);
            MySQLReport rep = AgentReport.getRptAverageTime(req.begDt, req.endDt, req.clientId, req.agentId, req.greater, req.smaller, conn);
            return createResponse(rep.write(conn), "rpt_tiempo_prom_agentes.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/rptVisitsZones")
    public Response getRptVisitsZones(DetailAgentDto req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Dates.validateOrder(req.begDt, req.endDt);
            MySQLReport rep = AgentReport.getRptVisitsZones(req.begDt, req.endDt, req.zoneId, req.agentId, conn);
            return createResponse(rep.write(conn), "rpt_visitas_zonas.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/rptCompVisitsZones")
    public Response getRptCompVisitsZones(DetailAgentDto req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Dates.validateOrder(req.begDt, req.endDt);
            MySQLReport rep = AgentReport.getRptCompVisitsZones(req.begDt, req.endDt, req.zoneId, req.agentId, conn);
            return createResponse(rep.write(conn), "rpt_visitas_zonas.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/rptOneMinute")
    public Response getRptOneMinute(DetailAgentDto req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Dates.validateOrder(req.begDt, req.endDt);
            MySQLReport rep = AgentReport.getRptOneMinute(req.begDt, req.endDt, req.clientId, req.agentId, req.greater, req.smaller, conn);
            return createResponse(rep.write(conn), "rpt_tiempo_agentes.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/rptVisits")
    public Response getRptVisits(DetailAgentDto req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Dates.validateOrder(req.begDt, req.endDt);
            MySQLReport rep = AgentReport.getRptVisits(req.begDt, req.endDt, req.zoneId, conn);
            return createResponse(rep.write(conn), "rpt_visitas.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
