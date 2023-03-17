package api.trk.api;

import api.BaseAPI;
import api.trk.model.TrkMto;
import java.sql.Connection;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/trkMto")
public class TrkMtoApi extends BaseAPI {

    @POST
    public Response insert(TrkMto mto) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            mto.mtoType = 1;
            mto.insert(conn);
            return Response.ok(mto).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
