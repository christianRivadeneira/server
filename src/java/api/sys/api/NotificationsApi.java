package api.sys.api;

import api.BaseAPI;
import api.sys.dto.Notify;
import java.sql.Connection;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/notifier")
public class NotificationsApi extends BaseAPI {

    @POST
    @Path("/pushEss")
    public Response pushEss(Notify obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Notify.sendNotification(conn, obj);
            return createResponse();
        } catch (Exception e) {
            return createResponse(e);
        }
    }

}
