package api.trk.api;

import api.BaseAPI;
import api.trk.dto.TrkCylReport;
import api.trk.rpt.TrkCylReports;
import java.io.File;
import java.sql.Connection;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/trkRpte")
public class RptApi  extends BaseAPI {
    
    @POST
    @Path("/getNoSaleCyls")
    public Response getNoSaleCyls(TrkCylReport obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            File file = TrkCylReports.getNoSaleCyls(obj.beginDate, conn);
            return createResponse(file, "cilindros.csv");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
}
