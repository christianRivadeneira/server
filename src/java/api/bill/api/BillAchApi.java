package api.bill.api;

import api.BaseAPI;
import api.bill.dto.BillAchDto;
import api.bill.model.BillBill;
import api.bill.model.BillCfg;
import api.bill.model.BillClientTank;
import api.bill.model.BillInstance;
import api.bill.model.BillSpan;
import api.bill.writers.bill.BillForPrint;
import api.bill.writers.bill.BillWriter;
import api.bill.writers.bill.GetBills;
import api.sys.model.SysCfg;
import controller.billing.BillImportAsoc2001;
import controller.billing.BillImportAsoc2001.RefInfo;
import java.io.File;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;
import utilities.SysTask;
import web.billing.BillingServlet;

@Path("/billAch")
public class BillAchApi extends BaseAPI {

    //private static String ACH_MG = "ach.montagas.com.co";
    private static String ACH_MG = "172.16.1.29";

    @GET
    @Path("/getBillInfo")
    public Response getBillInfo(@QueryParam("ref") String ref, @QueryParam("fac") String fac, @Context HttpServletRequest request) {
        try {
            verifyAccess(request);
        } catch (Exception e) {
            return createResponse(e);
        }
        try (Connection conn = getConnection()) {
            RefInfo info = RefInfo.getInfo(ref != null && !ref.isEmpty() ? ref : fac);
            int billId = new BillImportAsoc2001.BillInfoFinder(conn).getBillId(ref != null && !ref.isEmpty() ? ref : fac, null);
            useBillInstance(info.instId, conn);
            BillBill bill = BillBill.getById(billId, conn);

            if (bill == null) {
                throw new Exception("Factura no encontrada. Verifique los datos e intente nuevamente.");
            }

            BillInstance inst = BillingServlet.getInst(info.instId);
            BillForPrint bfp = GetBills.getById(billId, inst, conn);
            BillSpan span = new BillSpan().select(bill.billSpanId, conn);

            BillClientTank client = new BillClientTank().select(bill.clientTankId, conn);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            BillAchDto res = new BillAchDto();
            res.billId = bill.id;
            res.instId = info.instId;
            res.billNum = bill.billNum;
            res.createDate = sdf.format(bill.creationDate);
            res.payDate = new SimpleDateFormat("dd/MM/yyyy").format(span.limitDate);
            res.clientFName = client.firstName;
            res.clientLName = client.lastName;
            res.clieCode = bfp.clieCode;
            res.clientAddress = bfp.address;
            res.clieDoc = bfp.clieDoc;
            res.cliePhone = client.phones;
            res.valToPay = MySQLQuery.getAsInteger(bfp.total);
            res.paymentDate = bill.paymentDate;

            return createResponse(res);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/rePrintPdf")
    public Response rePrintPdf(@QueryParam("billId") int billId, @QueryParam("instId") int instId, @Context HttpServletRequest request) {
        try {
            verifyAccess(request);
        } catch (Exception e) {
            return createResponse(e);
        }
        try (Connection conn = getConnection()) {
            SysCfg sysCfg = SysCfg.select(conn);
            useBillInstance(instId, conn);
            BillCfg cfg = new BillCfg().select(1, conn);
            File f = File.createTempFile("bill", ".pdf");
            BillInstance inst = BillingServlet.getInst(instId);
            BillForPrint bill = GetBills.getById(billId, inst, conn);
            BillWriter writer = BillWriter.getCurrentPdfWriter(inst, sysCfg, cfg, conn, f, true);
            writer.addBill(bill);
            writer.endDocument();
            return createResponse(f, "bill.pdf");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private void verifyAccess(@Context HttpServletRequest request) throws Exception {
        String address = request.getRemoteAddr();
        if (!address.equals("localhost") && !address.equals("127.0.0.1") && !address.equals(ACH_MG)) {
            throw new Exception("Ud no está autorizado a usar este servicio.");
        }
    }

    @GET
    @Path("/achPing")
    public Response achPing() {
        try {
            try (Connection conn = getConnection()) {
                SysTask t = new SysTask(BillAchApi.class, "Ping Ach", 1, conn);
                t.success(conn);
                return createResponse();
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/payBill")
    public Response payBill(@QueryParam("ref") String ref, @Context HttpServletRequest request) {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                verifyAccess(request);

                RefInfo info = RefInfo.getInfo(ref);
                int billId = new BillImportAsoc2001.BillInfoFinder(conn).getBillId(ref, null);
                useBillInstance(info.instId, conn);

                Integer bankId = new MySQLQuery("SELECT id FROM bill_bank WHERE asoc_code = 20").getAsInteger(conn);
                if (bankId == null) {
                    throw new Exception("No se encontró banco para ésta transacción.");
                }

                BillBill bill = BillBill.getById(billId, conn);

                if (bill.paymentDate != null) {
                    throw new Exception("La factura ya fue pagada");
                }
                BillInstance inst = BillingServlet.getInst(info.instId);
                BillBill.payBill(bill.id, new Date(), bankId, 1, inst, conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                return createResponse(e);
            }

            return createResponse();
        } catch (Exception e) {
            return createResponse(e);
        }
    }

}
