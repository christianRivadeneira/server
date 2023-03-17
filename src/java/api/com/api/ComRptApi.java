package api.com.api;

import api.BaseAPI;
import api.com.rpt.ComReports;
import java.sql.Connection;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Date;
import java.util.List;
import javax.ws.rs.POST;
import metadata.model.GridRequest;
import utilities.Reports;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;

@Path("/comRptApi")
public class ComRptApi extends BaseAPI {

    @POST
    @Path("/getPriceLists")
    public Response getPriceList() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            List<MySQLReport> rep = ComReports.getPriceListBiable(conn);
            File file = Reports.createReportFile("Listado de Precios", "xls");
            MySQLReport[] array = rep.toArray(new MySQLReport[0]); // convert list to array 
            MySQLReportWriter.write(array, file, conn);
            return createResponse(file, file.getName());
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/rptCylOrderStoreVsDeliv")
    public Response rptCylOrderStoreVsDeliv(GridRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Date begDt = req.dates.get(0);
            Date endDt = req.dates.get(1);
            Integer storeId = req.ints.get(0);

            MySQLReport rep = ComReports.rptCylOrderStoreVsDeliv(storeId, begDt, endDt, conn);
            File file = Reports.createReportFile("Pedidos de Cilindros vs Entregados", "xls");
            MySQLReportWriter.write(rep, file, conn);
            return createResponse(file, file.getName());
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
