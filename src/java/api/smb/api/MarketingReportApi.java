/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.smb.api;

import api.BaseAPI;
import api.smb.dto.MarketingReportRequest;
import api.smb.rpt.MarketingReport;
import java.sql.Connection;
import java.util.List;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import model.marketing.lists.CylinderAreaCount;
import model.marketing.lists.MonthlySeedingListItem1;
import utilities.mysqlReport.MySQLReport;

/**
 *
 * @author andre
 */
@Path("/marketingReport")
public class MarketingReportApi extends BaseAPI {
    
    @POST
    @Path("/getCtrComoReport")
    public Response getCtrComoReport(MarketingReportRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = MarketingReport.getCtrComoReport(req.zone, req.city, req.sector, req.neigh, req.begin, req.end, req.tdate, req.sower, req.writer, req.veh, req.checked, req.anull, req.cyls, req.moreThan, req.tneigh, req.program, req.data, conn);
            useDefault(conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/getCorrectsContracts")
    public Response getCorrectsContracts(MarketingReportRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = MarketingReport.getCorrectsContracts(req.zone, req.city, req.sector, req.neigh, req.state, req.begin, req.end, conn);
            useDefault(conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/getDailyReportDiff")
    public Response getDailyReportDiff(MarketingReportRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = MarketingReport.getDailyReportDiff(req.begin, req.end, conn);
            useDefault(conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/getCylinderCountList")
    public Response getCylinderCountList(MarketingReportRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            List<CylinderAreaCount> response = MarketingReport.getCylinderCountList(req.zone, req.city, req.sector, req.neigh, req.year, req.cancel, req.checked, req.begin, req.end, req.typeNeigh, req.afil, conn);
            return createResponse(response);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/getDailyReportByMonths")
    public Response getDailyReportByMonths(MarketingReportRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = MarketingReport.getDailyReportByMonths(req.begin, req.end, conn);
            useDefault(conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/getDailyReport")
    public Response getDailyReport(MarketingReportRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = MarketingReport.getDailyReport(req.date, conn);
            useDefault(conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/getMissingContract")
    public Response getMissingContract(MarketingReportRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = MarketingReport.getMissingContract(req.from_serie, req.to_serie, req.afil, conn);
            useDefault(conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/getMonthlySeeding")
    public Response getMonthlySeeding(MarketingReportRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            List<MonthlySeedingListItem1> response = MarketingReport.getMonthlySeeding(req.year, req.month, req.zone, req.cylTypeId, req.sowerId, req.anul, req.canc, req.afil, conn);
            return createResponse(response);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/getClientsManyContracts")
    public Response getClientsManyContracts(MarketingReportRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = MarketingReport.getClientsManyContracts(req.zone, req.city, req.sector, req.neigh, req.type, req.afil, conn);
            useDefault(conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/getCylinderContractByEstablish")
    public Response getCylinderContractByEstablish(MarketingReportRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = MarketingReport.getCylinderContractByEstablish(req.zone, req.city, req.sector, req.begin, req.end, req.afil, conn);
            useDefault(conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/getContractReport")
    public Response getContractReport(MarketingReportRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = MarketingReport.getContractReport(req.zone, req.city, req.sector, req.neigh, req.begin, req.end, req.tdate, req.sower, req.writer, req.veh, req.checked, req.anull, req.cyls, req.moreThan, req.typeNeigh, req.program, req.data, req.deposit, req.showAddFields, conn);
            useDefault(conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
}
