package api.bill.api;

import api.BaseAPI;
import api.Params;
import api.bill.model.BillClieCau;
import api.bill.model.BillClientTank;
import api.bill.model.BillMeter;
import api.bill.model.BillSpan;
import java.math.BigDecimal;
import java.sql.Connection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/billClieCau")
public class BillClieCauApi extends BaseAPI {

    @GET
    @Path("byClientSpan")
    public Response getByClientSpan(@QueryParam("clientId") int clientId, @QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillClieCau cc = new BillClieCau().select(new Params("clientId", clientId).param("spanId", spanId), conn);
            return createResponse(cc);
        } catch (Exception e) {
            return createResponse(e);
        }
    }

    @GET
    @Path("simulateBill")
    public Response simulateBill(@QueryParam("stratum") int stratum,
            @QueryParam("sectorType") String sectorType,
            @QueryParam("factor") BigDecimal factor,
            @QueryParam("rawM3") BigDecimal rawM3,
            @QueryParam("skipContrib") boolean skipContrib) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillClientTank client = new BillClientTank();
            client.sectorType = sectorType;
            client.stratum = stratum;
            client.skipContrib = skipContrib;

            BillMeter meter = new BillMeter();
            meter.factor = factor;

            useBillInstance(conn);
            BillSpan cons = BillSpan.getByState("cons", conn);
            return createResponse(BillClieCau.calc(client, cons, meter, rawM3));
        } catch (Exception e) {
            return createResponse(e);
        }
    }
}
