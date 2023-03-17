package api.bill.api;

import api.BaseAPI;
import api.bill.model.BillSpan;
import api.bill.model.BillUserService;
import api.bill.model.BillUserServiceFee;
import api.bill.model.BillUserServiceFeeReplaceRequest;
import api.bill.model.EqualPayment;
import api.sys.model.SysCrudLog;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;

@Path("/billUserServiceFee") /* Tabla de redes */
public class BillUserServiceFeeApi extends BaseAPI {

    @GET
    @Path("/byService")
    public Response getByService(@QueryParam("serviceId") int serviceId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);

            BillUserService serv = new BillUserService().select(serviceId, conn);
            BillSpan reca = BillSpan.getByClient("reca", serv.billClientTankId, getBillInstance(), conn);

            List<BillUserServiceFee> fees = BillUserServiceFee.getByService(serviceId, conn);
            for (int i = 0; i < fees.size(); i++) {
                BillUserServiceFee fee = fees.get(i);
                fee.caused = serv.billSpanId + i <= reca.id;
            }
            return createResponse(fees);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/replace")
    public Response replaceFees(BillUserServiceFeeReplaceRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            DecimalFormat df = new DecimalFormat("#,###.0000");
            BillUserService serv = new BillUserService().select(req.serviceId, conn);
            List<BillUserServiceFee> origFees = BillUserServiceFee.getByService(serv.id, conn);

            BigDecimal totalBill = BigDecimal.ZERO;
            BigDecimal totalExt = BigDecimal.ZERO;

            for (BillUserServiceFee fee : origFees) {
                totalBill = totalBill.add(fee.value.add(fee.inter));
                totalExt = totalExt.add(fee.extPay.add(fee.extInter));
            }

            String log = "Se editaron las cuotas del servicio, eran " + origFees.size() + " cuotas por " + df.format(totalBill) + " con abonos externos por " + df.format(totalExt);
            new MySQLQuery("DELETE FROM bill_user_service_fee WHERE `service_id` = " + req.serviceId).executeUpdate(conn);

            for (int i = 0; i < req.fees.size(); i++) {
                BillUserServiceFee fee = req.fees.get(i);
                fee.serviceId = req.serviceId;
                fee.place = i;
                fee.insert(conn);
            }
            useDefault(conn);
            SysCrudLog.updated(this, serv, log, conn);
            return createResponse(origFees);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("simulation")
    public Response getSimulation(@QueryParam("total") BigDecimal total, @QueryParam("saleIva") BigDecimal saleVat, @QueryParam("creditInter") BigDecimal creditInter, @QueryParam("interIva") BigDecimal interIva, @QueryParam("payments") int payments) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            EqualPayment[] values = EqualPayment.getValues(total, saleVat, creditInter, interIva, payments);
            List<BillUserServiceFee> rta = new ArrayList<>();
            for (EqualPayment value : values) {
                BillUserServiceFee fee = new BillUserServiceFee();
                fee.value = value.capital;
                fee.inter = value.interest;
                fee.interTax = value.interVat;
                rta.add(fee);
            }
            useDefault(conn);
            return Response.ok(rta).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
