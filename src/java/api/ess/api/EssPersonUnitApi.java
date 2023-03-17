package api.ess.api;

import api.BaseAPI;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import api.ess.model.EssPersonUnit;
import api.ess.model.EssUnit;

@Path("/essPersonUnit")
public class EssPersonUnitApi extends BaseAPI {

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            EssPersonUnit u = new EssPersonUnit().select(id, conn);
            EssPersonUnit.delete(id, conn);
            SysCrudLog.deleted(this, EssPersonUnit.class, id, conn);
            EssUnit.updateCache(u.unitId, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
