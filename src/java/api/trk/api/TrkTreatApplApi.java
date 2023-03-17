package api.trk.api;

import api.BaseAPI;
import api.trk.model.TrkTreatAppl;
import api.trk.model.TrkTreatment;
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

@Path("/trkTreatAppl")
public class TrkTreatApplApi extends BaseAPI {

    @POST
    @Path("/batch/{chkId}")
    public Response insert(@PathParam("chkId") int chkId, List<Integer> itemIds, @Context HttpHeaders headers) {
        try (Connection conn = MySQLCommon.getDefaultConnection()) {
            conn.setAutoCommit(false);
            try {
                SessionLogin.validate(headers.getHeaderString("authorization"), conn);
                removeTreatment(chkId, conn);
                if (!itemIds.isEmpty()) {
                    TrkTreatment t = new TrkTreatment();
                    t.trkChkId = chkId;
                    t.insert(conn);
                    for (int i = 0; i < itemIds.size(); i++) {
                        TrkTreatAppl ti = new TrkTreatAppl();
                        ti.treatId = t.id;
                        ti.itemId = itemIds.get(i);
                        ti.insert(conn);
                    }
                }
                conn.commit();
                return Response.ok(itemIds.size()).build();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public static void removeTreatment(int chkId, Connection conn) throws Exception {
        Integer treatId = new MySQLQuery("SELECT id FROM trk_treatment WHERE trk_chk_id = ?1").setParam(1, chkId).getAsInteger(conn);
        if (treatId != null) {
            new MySQLQuery("DELETE FROM trk_treat_appl WHERE treat_id = ?1").setParam(1, treatId).executeUpdate(conn);
            new MySQLQuery("DELETE FROM trk_treatment WHERE id = ?1").setParam(1, treatId).executeUpdate(conn);
        }
    }
}
