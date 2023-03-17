package api.sys.api;

import api.BaseAPI;
import api.sys.dto.SmsDto;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import web.marketing.smsClaro.ClaroSmsSender;

@Path("/sms")
public class SmsApi extends BaseAPI {

    @POST
    @Path("/send")
    public Response isShowingQueries(SmsDto obj) {
        try {
            if (obj.pass.equals("MnNkAnXggbf3Mc7q")) {
                String code = new ClaroSmsSender().sendMsg(obj.message, "1", obj.phone);
                return createResponse(code);
            } else {
                throw new Exception("No está autorizado su acceso a este servicio.");
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
