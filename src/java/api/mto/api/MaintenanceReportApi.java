package api.mto.api;

import api.BaseAPI;
import api.GridResult;
import api.mto.dto.MaintenanceRequest;
import api.mto.rpt.MaintenanceReport;
import java.sql.Connection;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.maintenance.list.CostReport;
import utilities.mysqlReport.MySQLReport;

@Path("/maintenanceReport")
public class MaintenanceReportApi extends BaseAPI {

    @POST
    @Path("/getAnnualCostReport")
    public Response getAnnualCostReport(MaintenanceRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = MaintenanceReport.getAnnualCostReport(req.cityId, req.enterId, req.year, req.contract, req.contractor, req.storeMovs, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/getCostReportByDates")
    public Response getCostReportByDates(MaintenanceRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = MaintenanceReport.getCostReportByDates(req.cityId, req.enterId, req.idVeh, req.fBegin, req.fEnd, req.contract, req.contractor, req.storeMovs, req.percCosts, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/findCostReportVeh")
    public Response findCostReportVeh(MaintenanceRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            CostReport response = MaintenanceReport.findCostReportVeh(req.type, req.cityId, req.enterId, req.idVeh, req.year, req.contract, req.contractor, req.storeMovs, conn);
            return createResponse(response);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/findForecastReport")
    public Response findForecastReport(MaintenanceRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            CostReport response = MaintenanceReport.findForecastReport(req.type, req.cityId, req.enterId, req.idVeh, req.year, req.contract, req.contractor, conn);
            return createResponse(response);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/getAnnualCostReportVeh")
    public Response getAnnualCostReportVeh(MaintenanceRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = MaintenanceReport.getAnnualCostReportVeh(req.idVeh, req.year, req.contractor, req.storeMovs, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/findForecast")
    public Response findForecast(MaintenanceRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MaintenanceReport.findForecast(req.year, req.porc, req.contractor, conn);
            return Response.ok().build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/getElementsTotal")
    public Response getElementsTotal(MaintenanceRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = MaintenanceReport.getElementsTotal(req.cityId, req.enterId, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/getElementsAgency")
    public Response getElementsAgency(MaintenanceRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = MaintenanceReport.getElementsAgency(req.cityId, req.enterId, req.elementId, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/getElementsVeh")
    public Response getElementsVeh(MaintenanceRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = MaintenanceReport.getElementsVeh(req.idVeh, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/getDriversVeh")
    public Response getDriversVeh(MaintenanceRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = MaintenanceReport.getDriversVeh(req.idVeh, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/getVehsDriver")
    public Response getVehsDriver(MaintenanceRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = MaintenanceReport.getVehsDriver(req.driverId, req.active, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/getAreasValues")
    public Response getAreasValues(MaintenanceRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult grid = MaintenanceReport.getAreasValues(req.type, req.idVeh, req.year, conn);
            return createResponse(grid);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/getFuelsValues")
    public Response getFuelsValues(MaintenanceRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult grid = MaintenanceReport.getFuelsValues(req.idVeh, req.year, conn);
            return createResponse(grid);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/getCostMonth")
    public Response getCostMonth(MaintenanceRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            CostReport response = MaintenanceReport.findAnnualCostReport(req.type, req.cityId, req.enterId, req.year, req.contract, req.contractor, req.storeMovs, conn);
            return createResponse(response);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/findForecastBudgetReport")
    public Response findForecastBudgetReport(MaintenanceRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            CostReport response = MaintenanceReport.findForecastBudgetReport(req.type, req.cityId, req.enterId, req.year, conn);
            return createResponse(response);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/getCostOneMonth")
    public Response getCostOneMonth(MaintenanceRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            CostReport response = MaintenanceReport.findMonthCostReport(req.type, req.cityId, req.enterId, req.year, req.month, req.contract, req.contractor, req.storeMovs, conn);
            return createResponse(response);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/copyForecast")
    public Response copyForecast(MaintenanceRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MaintenanceReport.copyForecast(req.vehSrc, req.vehDes, req.year, conn);
            return Response.ok().build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/comparativeCosts")
    public Response getComparativeCosts(MaintenanceRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = MaintenanceReport.getComparativeCosts(req.year, req.vehId, req.vhPlate, req.cityId, req.entId, req.cName, req.eName, req.typeB, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/comparativeAreRubr")
    public Response getComparativeAreRubr(MaintenanceRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = MaintenanceReport.getComparativeAreRubr(req.areaId, req.nameArea, req.year, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/rptLubrication")
    public Response getRptLubrication(@QueryParam("cityId") Integer cityId, @QueryParam("sbAreaId") Integer sbAreaId, @QueryParam("month") Integer month, @QueryParam("year") Integer year,
            @QueryParam("type") String type) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = MaintenanceReport.getRptLubrication(cityId, sbAreaId, month, year, type, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/rptTaskProg")
    public Response getRptTaskProg(MaintenanceRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = MaintenanceReport.getRptTaskProg(req.fBegin, req.fEnd, conn);
            return createResponse(rep.write(conn), "exp_cts_como.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
