package api.trk.api;

import api.BaseAPI;
import api.trk.model.TrkCheck;
import api.trk.model.TrkNegAns;
import java.sql.Connection;
import java.util.List;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@Path("/trkNegAns")
public class TrkNegAnsApi extends BaseAPI {

    @POST
    @Path("/batch/{chkId}")
    public Response insert(@PathParam("chkId") int chkId, List<Integer> questIds, @Context HttpHeaders headers) {
        try (Connection conn = MySQLCommon.getDefaultConnection()) {
            SessionLogin.validate(headers.getHeaderString("authorization"), conn);
            new MySQLQuery("DELETE FROM trk_neg_ans WHERE check_id = ?1").setParam(1, chkId).executeUpdate(conn);
            for (int i = 0; i < questIds.size(); i++) {
                TrkNegAns ans = new TrkNegAns();
                ans.checkId = chkId;
                ans.questionId = questIds.get(i);
                ans.insert(conn);
            }            
            int cylId = new TrkCheck().select(chkId, conn).trkCylId;
            new MySQLQuery("UPDATE trk_cyl SET ok = " + questIds.isEmpty() + " WHERE id = "+cylId).executeUpdate(conn);
            new MySQLQuery("UPDATE trk_check SET ok = " + questIds.isEmpty() + " WHERE id = " + chkId).executeUpdate(conn);
            return Response.ok(questIds.size()).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
