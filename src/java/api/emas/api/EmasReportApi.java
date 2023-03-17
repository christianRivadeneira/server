package api.emas.api;

import api.BaseAPI;
import api.emas.rpt.EmasReport;
import java.sql.Connection;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import utilities.mysqlReport.MySQLReport;

@Path("/emasReport")
public class EmasReportApi extends BaseAPI {
    
    @POST
    @Path("/getExportSedes")
    public Response getExportSedes() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = EmasReport.getExportSedes(conn);
            return createResponse(rep.write(conn), "sedes.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/getExportClients")
    public Response getExportClients() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = EmasReport.getExportClients(conn);
            return createResponse(rep.write(conn), "clientes.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
}
