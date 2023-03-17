package api.bill.api;

import api.BaseAPI;
import api.bill.model.BillFinanceNote;
import api.bill.model.BillFinanceNoteFee;
import api.bill.model.BillFinanceNoteFeeReplaceRequest;
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

@Path("/billFinanceNoteFee")
public class BillFinanceNoteFeeApi extends BaseAPI {

    @GET
    @Path("/byNote")
    public Response getByNote(@QueryParam("noteId") int noteId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            List<BillFinanceNoteFee> fees = BillFinanceNoteFee.getByNote(noteId, conn);
            return createResponse(fees);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/replace")
    public Response replace(BillFinanceNoteFeeReplaceRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            DecimalFormat df = new DecimalFormat("#,###.0000");
            BillFinanceNote serv = new BillFinanceNote().select(req.noteId, conn);
            List<BillFinanceNoteFee> origFees = BillFinanceNoteFee.getByNote(serv.id, conn);

            BigDecimal totalBill = BigDecimal.ZERO;

            for (BillFinanceNoteFee fee : origFees) {
                totalBill = totalBill.add(fee.capital.add(fee.interest));
            }

            String log = "Se editaron las cuotas del servicio, eran " + origFees.size() + " cuotas por " + df.format(totalBill);
            new MySQLQuery("DELETE FROM bill_finance_note_fee WHERE `note_id` = " + req.noteId).executeUpdate(conn);

            for (int i = 0; i < req.fees.size(); i++) {
                BillFinanceNoteFee fee = req.fees.get(i);
                fee.noteId = req.noteId;
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
    public Response getSimulation(@QueryParam("total") BigDecimal total, @QueryParam("creditInter") BigDecimal creditInter, @QueryParam("payments") int payments) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            EqualPayment[] values = EqualPayment.getValues(total, null, creditInter, null, payments);
            List<BillFinanceNoteFee> rta = new ArrayList<>();
            for (EqualPayment value : values) {
                BillFinanceNoteFee fee = new BillFinanceNoteFee();
                fee.capital = value.capital;
                fee.interest = value.interest;
                rta.add(fee);
            }
            useDefault(conn);
            return Response.ok(rta).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
