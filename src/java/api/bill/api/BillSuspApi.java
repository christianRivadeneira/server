package api.bill.api;

import api.BaseAPI;
import api.GridResult;
import api.MultiPartRequest;
import api.MySQLCol;
import api.bill.app.SynchronizeDataSusps;
import api.bill.model.BillClientTank;
import api.bill.model.BillInstance;
import api.bill.model.BillSpan;
import api.bill.model.BillSusp;
import api.bill.model.dto.BillSuspRequest;
import api.sys.model.SysCfg;
import api.sys.model.SysCrudLog;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import model.billing.constants.Accounts;
import model.system.SessionLogin;
import utilities.MySQLPreparedQuery;
import utilities.MySQLQuery;
import utilities.Reports;
import utilities.json.JSONDecoder;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.Table;
import web.fileManager;

@Path("/billSusp")
public class BillSuspApi extends BaseAPI {

    @POST
    public Response insert(BillSusp obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(BillSusp obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            BillSusp old = new BillSusp().select(obj.id, conn);
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            BillSusp obj = new BillSusp().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            BillSusp.delete(id, conn);
            SysCrudLog.deleted(this, BillSusp.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @Path("/getAll")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(BillSusp.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/syncSuspRecon")
    public Response syncSuspRecon(@Context HttpServletRequest request) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            SysCfg sysCfg = SysCfg.select(conn);

            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);
            Integer version = Integer.valueOf(mr.params.get("version"));

            useBillInstance(conn);

            ByteArrayOutputStream baos;
            try (FileInputStream fis = new FileInputStream(mr.getFile().file); GZIPInputStream giz = new GZIPInputStream(fis)) {
                baos = new ByteArrayOutputStream();
                Reports.copy(giz, baos);
                baos.close();
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            List<BillSuspRequest> data = new JSONDecoder().getList(bais, BillSuspRequest.class);
            List<BillSuspRequest> syncSuspRecon = new ArrayList<>();
            if (version == 1) {
                syncSuspRecon = SynchronizeDataSusps.syncSuspReconV1(data, getBillInstance(), sysCfg, conn);
            } else {
                throw new Exception("No implementado");
            }
            mr.deleteFiles();
            return createResponse(syncSuspRecon, "suspRecon");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/syncSuspDone")
    public Response syncSuspDone(@Context HttpServletRequest request) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            SysCfg sysCfg = SysCfg.select(conn);

            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);
            Integer version = Integer.valueOf(mr.params.get("version"));

            useBillInstance(conn);

            ByteArrayOutputStream baos;
            try (FileInputStream fis = new FileInputStream(mr.getFile().file); GZIPInputStream giz = new GZIPInputStream(fis)) {
                baos = new ByteArrayOutputStream();
                Reports.copy(giz, baos);
                baos.close();
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            List<BillSuspRequest> data = new JSONDecoder().getList(bais, BillSuspRequest.class);
            List<BillSuspRequest> syncSuspDone = new ArrayList<>();
            if (version == 1) {
                syncSuspDone = SynchronizeDataSusps.syncSuspDoneV1(data, getBillInstance(), sysCfg, conn);
            } else {
                throw new Exception("No implementado");
            }
            mr.deleteFiles();
            return createResponse(syncSuspDone, "suspDone");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/rptSuspApp")
    public Response getRptSuspApp(@QueryParam("clientId") Integer clientId) {
        try (Connection conn = getConnection(); Connection gralConn = getConnection()) {
            getSession(conn);
            BillInstance inst = getBillInstance();
            useBillInstance(conn);
            SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");
            SysCfg sysCfg = SysCfg.select(gralConn);

            Object[][] data = new MySQLQuery("SELECT "
                    + "trim(concat(c.first_name, ' ', ifnull(c.last_name, ''))), "
                    + "concat(" + (sysCfg.showApartment ? "c.apartment" : "c.num_install") + ", IFNULL(CONCAT(' (',(SELECT `number` FROM bill_meter WHERE client_id = c.id ORDER BY start_span_id DESC LIMIT 1),')'), '')), "
                    + "b.name, "
                    + "susp_date, "
                    + "CONCAT(e.first_name, ' ', e.last_name) "
                    + "FROM bill_susp s "
                    + "INNER JOIN bill_client_tank c ON c.id = s.client_id "
                    + "LEFT JOIN bill_building b ON b.id = c.building_id "
                    + "LEFT JOIN sigma.ord_tank_client cc ON cc.mirror_id=b.id AND cc.`type`='build' "
                    + "LEFT JOIN sigma.employee e ON e.id = s.susp_tec_id  "
                    + "WHERE cancelled = 0 AND "
                    + "((susp_order_date IS NOT NULL AND susp_date IS NULL) OR (recon_order_date IS NOT NULL AND recon_date IS NULL)"
                    + " OR (recon_order_date IS NULL AND recon_date IS NULL AND susp_date IS NOT NULL)) "
                    + "AND s.susp_order_date IS NOT NULL AND s.recon_order_date IS NULL "
                    + (clientId != null ? "c.id = " + clientId + " " : "")
                    + "GROUP BY s.id").getRecords(conn);

            MySQLReport rep = new MySQLReport("Reporte Cortes App", "", "Cortes App", MySQLQuery.now(conn));

            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
            rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//1

            rep.setZoomFactor(85);
            rep.setShowNumbers(true);
            rep.getFormats().get(0).setWrap(true);
            rep.setVerticalFreeze(5);
            Table tb = new Table("Cortes");
            tb.getColumns().add(new Column("Cliente", 50, 0));
            tb.getColumns().add(new Column("Num. Instalación", 35, 0));
            tb.getColumns().add(new Column("Edificio", 35, 0));
            tb.getColumns().add(new Column("Fecha Corte", 15, 1));
            tb.getColumns().add(new Column("Encargado Corte", 35, 0));
            tb.setData(data);
            if (tb.getData().length > 0) {
                rep.getTables().add(tb);
            }
            return createResponse(rep.write(gralConn), "susp_app.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private static String join(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]).append(",");
        }
        sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }

    @GET
    @Path("/suspProspects")
    public Response getSuspProspects() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            SysCfg cfg = SysCfg.select(conn);
            BillInstance inst = getBillInstance();

            useBillInstance(conn);
            BillSpan reca = BillSpan.getByState("reca", conn);

            //meses en deuda
            int[] cartAccs = {
                Accounts.C_CAR_GLP,
                Accounts.C_CAR_SRV,
                Accounts.C_CAR_FINAN_DEU,
                Accounts.C_CAR_CONTRIB,
                Accounts.C_CAR_INTE_CRE
            };

            int[] curAccs = {
                Accounts.C_CONS,
                Accounts.C_CONS_SUBS,
                Accounts.C_REBILL,
                Accounts.C_BASI,
                Accounts.C_CONTRIB,
                Accounts.C_RECON,
                Accounts.C_CUOTA_SER_CLI_GLP,
                Accounts.C_CUOTA_SER_CLI_SRV,
                Accounts.C_CUOTA_SER_EDI,
                Accounts.C_CUOTA_FINAN_DEU,
                Accounts.C_CUOTA_INT_CRE
            };

            String cart = join(cartAccs);
            String curr = join(curAccs);

            MySQLPreparedQuery currCredQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_cred_id in (" + curr + ") AND t.cli_tank_id = ?1", conn);
            MySQLPreparedQuery currdebQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_deb_id in (" + curr + ") AND t.cli_tank_id = ?1", conn);

            BillClientTank[] clients = BillClientTank.getAll(true, conn);
            List<Object[]> data = new ArrayList<>();
            for (BillClientTank cl : clients) {
                Integer paidTotalBills = new MySQLQuery("SELECT count(*) "
                        + "FROM bill_bill "
                        + "WHERE bill_span_id = ?1 AND client_tank_id = ?2 AND total AND payment_date IS NOT NULL").setParam(1, cl.id).setParam(2, reca.id).getAsInteger(conn);
                if (paidTotalBills == 0) {
                    Boolean isSusp = new MySQLQuery("SELECT COUNT(*)>0 "
                            + "FROM bill_susp s "
                            + "WHERE s.recon_date IS NULL AND s.cancelled = 0 AND s.client_id = ?1").setParam(1, cl.id).getAsBoolean(conn);
                    if (!isSusp) {
                        List<CartAges> calc = CartAges.calc(cl.id, reca.id, cart, conn);
                        BigDecimal total = BigDecimal.ZERO;
                        int months = 0;
                        for (int i = 0; i < calc.size(); i++) {
                            CartAges c = calc.get(i);
                            total = total.add(c.leftValue);
                            if (c.leftValue.compareTo(BigDecimal.ZERO) > 0) {
                                months++;
                            }
                        }

                        if (new Date().compareTo(reca.limitDate) >= 0) {
                            //para que se tenga en cuenta la deuda del mes actual como cartera luego de la fecha limite de pago                            
                            currdebQ.setParameter(1, cl.id);
                            BigDecimal deb = currdebQ.getAsBigDecimal(true);

                            currCredQ.setParameter(1, cl.id);
                            BigDecimal cred = currCredQ.getAsBigDecimal(true);

                            BigDecimal thisMonth = deb.subtract(cred);
                            if (thisMonth.compareTo(BigDecimal.ZERO) > 0) {
                                total = total.add(thisMonth);
                                months++;
                            }
                        }
                        if (total.compareTo(cfg.suspValue) > 0 && total.compareTo(BigDecimal.ZERO) > 0 && months >= inst.suspDebtMonths) {

                            BigDecimal partialPayments = BigDecimal.ZERO;

                            String docIds = new MySQLQuery("SELECT GROUP_CONCAT(bb.id) "
                                    + "FROM bill_bill bb WHERE "
                                    + "bb.client_tank_id = ?1 AND  bb.active = 1 AND bb.total = 0 AND bb.bill_span_id = " + reca.id + " "
                                    + "AND bb.payment_date IS NOT NULL").setParam(1, cl.id).getAsString(conn);
                            if (docIds != null && !docIds.isEmpty()) {
                                partialPayments = new MySQLQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.bill_span_id = " + reca.id + " AND t.doc_id IN (" + docIds + ")").getAsBigDecimal(conn, true);
                            }

                            data.add(new Object[]{
                                cl.id,
                                BillClientTank.getClientName(cl),
                                cl.code,
                                cl.doc != null ? cl.doc : "",
                                total,
                                months,
                                partialPayments,
                                false
                            });
                        }
                    }
                }
            }

            GridResult gr = new GridResult();
            gr.data = data.toArray(new Object[0][]);
            gr.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 15, "Cliente"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 8, "Código"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 8, "Documento"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 8, "Deuda"),
                new MySQLCol(MySQLCol.TYPE_INTEGER, 8, "Meses"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 8, "Abono"),
                new MySQLCol(MySQLCol.TYPE_BOOLEAN, 6, "Susp.")
            };
            return createResponse(gr);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/testClient")
    public Response getTestClient(@QueryParam("clientId") int clientId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            SysCfg cfg = SysCfg.select(conn);
            BillInstance inst = getBillInstance();

            useBillInstance(conn);
            BillSpan reca = BillSpan.getByState("reca", conn);

            //meses en deuda
            int[] cartAccs = {
                Accounts.C_CAR_GLP,
                Accounts.C_CAR_SRV,
                Accounts.C_CAR_FINAN_DEU,
                Accounts.C_CAR_CONTRIB,
                Accounts.C_CAR_INTE_CRE
            };

            int[] curAccs = {
                Accounts.C_CONS,
                Accounts.C_CONS_SUBS,
                Accounts.C_REBILL,
                Accounts.C_BASI,
                Accounts.C_CONTRIB,
                Accounts.C_RECON,
                Accounts.C_CUOTA_SER_CLI_GLP,
                Accounts.C_CUOTA_SER_CLI_SRV,
                Accounts.C_CUOTA_SER_EDI,
                Accounts.C_CUOTA_FINAN_DEU,
                Accounts.C_CUOTA_INT_CRE
            };

            String cart = join(cartAccs);
            String curr = join(curAccs);

            MySQLPreparedQuery currCredQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_cred_id in (" + curr + ") AND t.cli_tank_id = ?1", conn);
            MySQLPreparedQuery currdebQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_deb_id in (" + curr + ") AND t.cli_tank_id = ?1", conn);

            Integer paidTotalBills = new MySQLQuery("SELECT count(*) "
                    + "FROM bill_bill "
                    + "WHERE bill_span_id = ?1 AND client_tank_id = ?2 AND total AND payment_date IS NOT NULL").setParam(1, clientId).setParam(2, reca.id).getAsInteger(conn);
            if (paidTotalBills == 0) {
                Boolean isSusp = new MySQLQuery("SELECT COUNT(*)>0 "
                        + "FROM bill_susp s "
                        + "WHERE s.recon_date IS NULL AND s.cancelled = 0 AND s.client_id = ?1").setParam(1, clientId).getAsBoolean(conn);
                if (!isSusp) {
                    List<CartAges> calc = CartAges.calc(clientId, reca.id, cart, conn);
                    BigDecimal total = BigDecimal.ZERO;
                    int months = 0;
                    for (int i = 0; i < calc.size(); i++) {
                        CartAges c = calc.get(i);
                        total = total.add(c.leftValue);
                        if (c.leftValue.compareTo(BigDecimal.ZERO) > 0) {
                            months++;
                        }
                    }

                    /*//para que se tenga en cuenta la deuda del mes actual como cartera luego de la fecha limite de pago
                        //faltaría implementar lo de fecha porque ahora lo toma en cuenta todo el tiempo
                        //no se habilita ni se termina de programar porque mg no compró la actualización
                        currdebQ.setParameter(1, cl.id);
                        BigDecimal deb = currdebQ.getAsBigDecimal(true);

                        currCredQ.setParameter(1, cl.id);
                        BigDecimal cred = currCredQ.getAsBigDecimal(true);

                        BigDecimal thisMonth = deb.subtract(cred);
                        if (thisMonth.compareTo(BigDecimal.ZERO) > 0) {
                            total = total.add(thisMonth);
                            months++;
                        }*/
                    DecimalFormat df = new DecimalFormat("#,###.####");

                    if (total.compareTo(cfg.suspValue) <= 0) {
                        throw new Exception("La deuda (" + df.format(total) + ") no supera el mínimo (" + df.format(cfg.suspValue) + ")");
                    }

                    if (total.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new Exception("El cliente está al día");
                    }

                    if (months < inst.suspDebtMonths) {
                        throw new Exception("Los meses en mora (" + months + ") no superan el mínimo (" + inst.suspDebtMonths + ")");
                    }
                    throw new Exception("El cliente es candidato para suspensión");
                } else {
                    throw new Exception("El cliente está suspendido o tiene orden de corte");
                }
            } else {
                throw new Exception("El cliente presenta una factura de pago total en el periodo");
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
