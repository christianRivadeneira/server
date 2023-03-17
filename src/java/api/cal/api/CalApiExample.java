package api.cal.api;

import api.BaseAPI;
import api.cal.model.CalIndicatorRequest;
import java.math.BigDecimal;
import java.sql.Connection;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/calApiExample")
public class CalApiExample extends BaseAPI {

    //*******************
    //** ejemplo dummy para crear un api de indicadores
    //*******************
    @POST
    @Path("/getSpace")
    public Response get(CalIndicatorRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BigDecimal bigDecimal = new BigDecimal(45800.01);
            return Response.ok(bigDecimal).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/getTime")
    public Response getTime(CalIndicatorRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BigDecimal bigDecimal = new BigDecimal(10.00);
            return Response.ok(bigDecimal).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
