package api.sys.api;

import api.BaseAPI;
import api.ord.api.OrdTankClientApi;
import java.sql.Connection;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/SysHVApi")
public class SysHVApi extends BaseAPI {

    public static final int ORD_TANK_CLIENT = 22;
    public static final int CRM_CLIENT = 43;

    @POST
    @Path("/HvTank")
    public Response getHv(@QueryParam("idOrdTankClient") Integer clientId,
            @QueryParam("maxReg") boolean maxReg, @QueryParam("visPhotos") boolean visPhotos) {
        try (Connection conn = getConnection()) {
            return createResponse(OrdTankClientApi.getHv(clientId, maxReg, visPhotos, conn), "hv.pdf");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
