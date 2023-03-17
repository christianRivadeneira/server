package api.ord.api;

import api.BaseAPI;
import api.GridResult;
import api.ord.dto.OrderingRequest;
import api.ord.rpt.OrderingReport;
import java.sql.Connection;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import utilities.mysqlReport.MySQLReport;

@Path("/orderingReport")
public class OrderingReportApi extends BaseAPI {
    
    @POST
    @Path("/winner")
    public Response getWinner(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            int res = OrderingReport.getWinner(req.officeId, req.drawDate, req.begin, req.end, req.type, conn);
            return createResponse(res);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/univIndexDocumentLookup")
    public Response univIndexDocumentLookup(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult grid = OrderingReport.univIndexDocumentLookup(req.cityId, req.document, conn);
            return createResponse(grid);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/pqrsCylReport")
    public Response getPqrsCylReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getPqrsCylReport(req.punctual, req.officeId, req.date, req.entId, req.typeCli, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/cylPQRPollingReport")
    public Response getCylPQRPollingReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getCylPQRPollingReport(req.pollVersionId, req.punctual, req.officeId, req.date, req.typeCli, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/cylPQRPollingMonthlyReport")
    public Response getCylPQRPollingMonthlyReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getCylPQRPollingMonthlyReport(req.pollVersionId, req.officeId, req.begYear, req.endYear, req.begMonth, req.endMonth, req.typeCli, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/cylPQRSatisPollingReport")
    public Response getCylPQRSatisPollingReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getCylPQRSatisPollingReport(req.pollVersionId, req.punctual, req.officeId, req.date, req.typeCli, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/cylPQRSatisOtherReport")
    public Response getCylPQRSatisOtherReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getCylPQRSatisOtherReport(req.pollVersionId, req.punctual, req.officeId, req.date, req.typeCli, req.cyls, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/PQRComSatisReport")
    public Response getPQRComSatisReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getPQRComSatisReport(req.pollVersionId, req.punctual, req.officeId, req.date, req.typeCli, req.cyls, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/cylPQRCancelReport")
    public Response getCylPQRCancelReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getCylPQRCancelReport(req.punctual, req.officeId, req.date, req.typeCli, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/tankPqrReport")
    public Response getTankPqrReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getTankPqrReport(req.punctual, req.officeId, req.date, req.entId, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/tankPQRPollingReport")
    public Response getTankPQRPollingReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getTankPQRPollingReport(req.pollVersionId, req.punctual, req.officeId, req.date, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/tankPQRPollingMonthlyReport")
    public Response getTankPQRPollingMonthlyReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getTankPQRPollingMonthlyReport(req.pollVersionId, req.officeId, req.begYear, req.endYear, req.begMonth, req.endMonth, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/tankPQRSatisPollingReport")
    public Response getTankPQRSatisPollingReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getTankPQRSatisPollingReport(req.pollVersionId, req.punctual, req.officeId, req.date, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/tankPQRCancelReport")
    public Response getTankPQRCancelReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getTankPQRCancelReport(req.punctual, req.officeId, req.date, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/othersPQRCancelReport")
    public Response getOthersPQRCancelReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getOthersPQRCancelReport(req.punctual, req.officeId, req.date, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/PQRComCancelReport")
    public Response getPQRComCancelReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getPQRComCancelReport(req.punctual, req.officeId, req.date, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/othersPQRPollingReport")
    public Response getOthersPQRPollingReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getOthersPQRPollingReport(req.pollVersionId, req.punctual, req.officeId, req.date, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/othersPQRPollingMonthlyReport")
    public Response getOthersPQRPollingMonthlyReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getOthersPQRPollingMonthlyReport(req.pollVersionId, req.officeId, req.begYear, req.endYear, req.begMonth, req.endMonth, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }    
    
    @POST
    @Path("/detailedOtherPqrReport")
    public Response getDetailedOtherPqrReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getDetailedOtherPqrReport(req.entId, req.begin, req.end, req.oprId, req.state, req.officeId, req.channelId, req.supreasonId, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/detailedPqrComReport")
    public Response getDetailedPqrComReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getDetailedPqrComReport(req.entId, req.begin, req.end, req.oprId, req.state, req.officeId, req.channelId, req.supreasonId, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/cylOrdersReport")
    public Response getCylOrdersReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getCylOrdersReport(req.repType, req.officeId, req.begin, req.end, req.entId, req.typeCli, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/tankOrdersReport")
    public Response getTankOrdersReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getTankOrdersReport(req.repType, req.officeId, req.begin, req.end, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/cylVehicleReport")
    public Response getCylVehicleReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getCylVehicleReport(req.repType, req.officeId, req.begin, req.end, req.entId, req.typeCli, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/ordVehicleReport")
    public Response getOrdVehicleReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getOrdVehicleReport(req.repType, req.officeId, req.begin, req.end, req.entId, req.typeCli, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }    
    
    @POST
    @Path("/detailsCylinderReport")
    public Response getDetailsCylinderReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getDetailsCylinderReport(req.entId, req.begin, req.end, req.typeCli, req.oprId, req.vehId, req.driverId, req.justif, req.state, req.officeId, req.descriptions, req.channelId, req.logs, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }  
    
    @POST
    @Path("/detailsTanksReport")
    public Response getDetailsTanksReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getDetailsTanksReport(req.begin, req.end, req.oprId, req.vehId, req.driverId, req.justif, req.state, req.officeId, req.descriptions, req.channelId, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    } 
    
    @POST
    @Path("/changedAddresses")
    public Response getChangedAddresses() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getChangedAddresses(conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    } 
    
    @POST
    @Path("/changedPhones")
    public Response getChangedPhones() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getChangedPhones(conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    } 
    
    @POST
    @Path("/cylOrdersMonthlyReport")
    public Response getCylOrdersMonthlyReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getCylOrdersMonthlyReport(req.repType, req.officeId, req.begYear, req.endYear, req.begMonth, req.endMonth, req.entId, req.type, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/tankOrdersMonthlyReport")
    public Response getTankOrdersMonthlyReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getTankOrdersMonthlyReport(req.repType, req.officeId, req.begYear, req.endYear, req.begMonth, req.endMonth, req.entId, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/cylPollingReport")
    public Response getCylPollingReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getCylPollingReport(req.pollVersionId, req.punctual, req.officeId, req.date, req.typeCli, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/tankVisitPollReport")
    public Response getTankVisitPollReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getTankVisitPollReport(req.pollVersionId, req.punctual, req.date, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/tankPollingReport")
    public Response getTankPollingReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getTankPollingReport(req.pollVersionId, req.punctual, req.officeId, req.date, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/cylOrderCancelReport")
    public Response getCylOrderCancelReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getCylOrderCancelReport(req.punctual, req.officeId, req.date, req.typeCli, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/cylOrderCancelAppReport")
    public Response getCylOrderCancelAppReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getCylOrderCancelAppReport(req.begin, req.end, req.officeId, req.typeCli, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/tankOrderCancelReport")
    public Response getTankOrderCancelReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getTankOrderCancelReport(req.punctual, req.officeId, req.date, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/repurchaseReport")
    public Response getRepurchaseReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getRepurchaseReport(req.officeId, req.maxDesv, req.minDate, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/clientsUnivBrand")
    public Response getClientsUnivBrand() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getClientsUnivBrand(conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/detailedRepairs")
    public Response getDetailedRepairs(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getDetailedRepairs(req.entId, req.begin, req.end, req.oprId, req.state, req.officeId, req.tecId, req.channelId, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/repairsCancel")
    public Response getRepairsCancel(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getRepairsCancel(req.punctual, req.officeId, req.date, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/repairsPollingReport")
    public Response getRepairsPollingReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getRepairsPollingReport(req.pollVersionId, req.punctual, req.officeId, req.date, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/repairsPollingMonthlyReport")
    public Response getRepairsPollingMonthlyReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getRepairsPollingMonthlyReport(req.pollVersionId, req.officeId, req.begYear, req.endYear, req.begMonth, req.endMonth, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/repairsByTech")
    public Response getRepairsByTech(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getRepairsByTech(req.punctual, req.officeId, req.date, req.entId, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/repairsSatisPollingReport")
    public Response getRepairsSatisPollingReport(OrderingRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = OrderingReport.getRepairsSatisPollingReport(req.pollVersionId, req.punctual, req.officeId, req.date, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
