package api.bill.api;

import api.BaseAPI;
import api.GridResult;
import api.MultiPartRequest;
import api.MySQLCol;
import api.bill.model.BillBill;
import api.bill.model.BillBillExporter;
import api.bill.model.BillCfg;
import api.bill.model.BillClientTank;
import api.bill.model.BillInstance;
import api.bill.model.BillPartialPayRequest;
import api.bill.model.BillPrintRequest;
import api.bill.model.BillSpan;
import api.bill.writers.bill.BillForPrint;
import api.bill.writers.bill.BillWriter;
import api.bill.writers.bill.GetBills;
import api.sys.model.SysCfg;
import api.sys.model.SysCrudLog;
import controller.billing.BillChangeBankAsob2001;
import controller.billing.BillImportAsoc2001;
import controller.billing.BillTransactionController;
import controller.billing.EmployeeController;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import model.billing.constants.Accounts;
import model.system.SessionLogin;
import service.MySQL.MySQLSelect;
import utilities.Dates;
import utilities.MySQLQuery;
import utilities.apiClient.BooleanResponse;
import utilities.apiClient.StringResponse;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.Table;
import utilities.shrinkfiles.FileShrinker;
import web.fileManager;

@Path("/billBill")
public class BillBillApi extends BaseAPI {

    @GET
    @Path("/gridByClient")
    public Response getGridByClient(@QueryParam("clientId") int clientId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            GridResult tbl = new GridResult();
            Object[][] dataBills = new MySQLQuery("SELECT "
                    + "b.bill_num, "//0
                    + "b.creator_id, "//1
                    + "b.registrar_id, "//2
                    + "k.name, "//3
                    + "b.creation_date, "//4
                    + "b.payment_date, "//5
                    + "b.regist_date, "//6
                    + "NOT(b.active), "//7
                    + "NOT(b.total), "//8
                    + "b.id, "//9
                    + "k.name, "//10
                    + "(SELECT SUM(p.value) FROM bill_plan p WHERE p.account_deb_id = " + Accounts.BANCOS + " "
                    + "AND p.cli_tank_id = " + clientId + " AND p.doc_id = b.id AND p.doc_type = 'fac'), "//11
                    + "IF(b.regist_date IS NOT NULL, 1, 0) "//12
                    + "FROM "
                    + "bill_bill AS b "
                    + "LEFT JOIN bill_bank k ON k.id = b.bank_id "
                    + "WHERE "
                    + "b.client_tank_id = " + clientId).getRecords(conn);

            useDefault(conn);
            for (Object[] billRow : dataBills) {
                billRow[1] = EmployeeController.getEmployeeShortName(MySQLQuery.getAsInteger(billRow[1]), conn);
                billRow[2] = EmployeeController.getEmployeeShortName(MySQLQuery.getAsInteger(billRow[2]), conn);
            }

            tbl.data = dataBills;
            tbl.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_TEXT, 180, "Cupón"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 180, "Creó"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 180, "Registró"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 220, "Banco"),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 160, "Creación"),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 160, "Pago"),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 160, "Registro"),
                new MySQLCol(MySQLCol.TYPE_BOOLEAN, 75, "Anulada"),
                new MySQLCol(MySQLCol.TYPE_BOOLEAN, 75, "Parcial"),
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_KEY),};

            tbl.sortColIndex = 4;
            tbl.sortType = GridResult.SORT_DESC;
            return createResponse(tbl);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/isValidCity")
    public Response isValidCity(@QueryParam("instanceId") int instanceId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance bi = new BillInstance().select(instanceId, conn);
            if (bi != null) {
                return Response.ok(new BooleanResponse(true)).build();
            } else {
                return Response.ok(new BooleanResponse(false)).build();
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/reverseBill")
    public Response reverseBill(@QueryParam("billId") int billId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillBill bill = new BillBill().select(billId, conn);
            if (bill.bankId == null) {
                throw new Exception("No se puede anular el pago de cupón, no ha sido pagado.");
            }

            BillSpan reca = BillSpan.getByClient("reca", bill.clientTankId, getBillInstance(), conn);
            if (reca.id != bill.billSpanId) {
                throw new Exception("El pago del cupón no se puede anular porque pertenece a otro periodo.");
            } else {
                int lId = BillTransactionController.getLastTrasactionIdByClient(bill.clientTankId, conn);
                if (bill.lastTransId == null) {
                    throw new Exception("El pago del cupón no se puede anular porque la cuenta del cliente ha sido afectada por otros documentos.");
                }
                if (lId != bill.lastTransId) {
                    throw new Exception("El pago del cupón no se puede anular porque la cuenta del cliente ha sido afectada por otros documentos.");
                }
            }

            new MySQLQuery("DELETE FROM bill_transaction WHERE doc_id = " + billId + " AND doc_type = 'fac'").executeUpdate(conn);
            new MySQLQuery("UPDATE bill_bill SET payment_date = NULL, regist_date = NULL, registrar_id = NULL, bank_id = NULL, active = 0 WHERE id = " + billId + " ").executeUpdate(conn);
            new MySQLQuery("UPDATE bill_bill SET active = 0 WHERE payment_date IS NULL AND client_tank_id = " + bill.clientTankId).executeUpdate(conn);

            useDefault(conn);
            SysCrudLog.updated(this, bill, "Se reversó el pago", conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/createForAll")
    public Response createForAll(final @QueryParam("printLogos") boolean printLogos) {
        try {
            final Connection sigmaConn = getConnection();
            final Connection billConn = getConnection();
            final Connection progConn = getConnection();

            useBillInstance(billConn);

            final SessionLogin sl = getSession(sigmaConn);
            if (getBillInstance().siteBilling) {
                throw new Exception("Opción no disponible para facturación en sitio");
            }
            final BillPrintRequest req = new BillPrintRequest();
            req.begDt = new Date();
            req.instId = getBillInstId();
            req.empId = sl.employeeId;
            req.status = "running";
            req.insert(progConn);

            sigmaConn.setAutoCommit(false);
            billConn.setAutoCommit(false);

            final BillInstance inst = getBillInstance();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final List<BillForPrint> bills = new ArrayList<>();
                        BillCfg cfg = new BillCfg().select(1, billConn);
                        SysCfg sysCfg = SysCfg.select(sigmaConn);

                        BillWriter memoryWriter = new BillWriter() {

                            @Override
                            public void addBill(BillForPrint bill) throws Exception {
                                bills.add(bill);
                            }

                            @Override
                            public void prepare(Connection ep) throws Exception {

                            }

                            @Override
                            public File endDocument() throws Exception {
                                return null;
                            }
                        };

                        GetBills.create(null, inst, sysCfg, req, sigmaConn, billConn, sl, memoryWriter);

                        //Itext a veces genera null pointer exception en writer.endDocument() en linux, por eso se coloca el reintento.
                        int attempts = 0;
                        while (true) {
                            attempts++;
                            try {
                                File f = File.createTempFile("bill", ".pdf");
                                BillWriter writer = BillWriter.getCurrentPdfWriter(inst, sysCfg, cfg, billConn, f, printLogos);
                                for (int i = 0; i < bills.size(); i++) {
                                    req.tick();
                                    writer.addBill(bills.get(i));
                                }
                                writer.endDocument();
                                fileManager.PathInfo pi = new fileManager.PathInfo(sigmaConn);
                                req.bfileId = fileManager.upload(sl.employeeId, req.id, 137, "bills.pdf", "bills", true, FileShrinker.TYPE_NONE, pi, f, sigmaConn).id;
                                req.fileLenKb = (int) (pi.getExistingFile(req.bfileId).length() / 1024);
                                req.status = "ready";
                                req.endDt = new Date();
                                req.update();
                                break;
                            } catch (NullPointerException ex) {
                                if (attempts == 3) {
                                    throw ex;
                                }
                            }
                        }
                        billConn.commit();
                        sigmaConn.commit();
                    } catch (Exception ex) {
                        MySQLSelect.tryToRollback(billConn);
                        MySQLSelect.tryToRollback(sigmaConn);
                        try {
                            req.setException(ex);
                        } catch (Exception ex1) {
                            Logger.getLogger(BillBillApi.class.getName()).log(Level.SEVERE, null, ex1);
                        }
                    } finally {
                        MySQLSelect.tryClose(sigmaConn);
                        MySQLSelect.tryClose(billConn);
                        MySQLSelect.tryClose(progConn);
                    }
                }
            }).start();
            return createResponse(req);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/createForClient")
    public Response createForClient(@QueryParam("clientId") int clientId, @QueryParam("pdf") boolean pdf) {
        try (Connection conn = getConnection()) {
            SysCfg sysCfg = SysCfg.select(conn);
            SessionLogin sess = getSession(conn);
            useBillInstance(conn);
            BillCfg cfg = new BillCfg().select(1, conn);
            if (pdf) {
                File f = File.createTempFile("bill", ".pdf");
                BillWriter writer = BillWriter.getCurrentPdfWriter(getBillInstance(), sysCfg, cfg, conn, f, true);
                GetBills.create(clientId, getBillInstance(), sysCfg, null, null, conn, sess, writer);
                writer.endDocument();
                return createResponse(f, "bill.pdf");
            } else {
                File f = File.createTempFile("bill", ".zpl");
                BillWriter writer = BillWriter.getCurrentZplWriter(getBillInstance(), sysCfg, cfg, conn, f, true);
                GetBills.create(clientId, getBillInstance(), sysCfg, null, null, conn, sess, writer);
                writer.endDocument();
                return createResponse(f, "bill.zpl");
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/reprintZplBill")
    public Response reprintZplBill(@QueryParam("clientId") int clientId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            SysCfg sysCfg = SysCfg.select(conn);
            useBillInstance(conn);
            BillCfg billCfg = new BillCfg().select(1, conn);
            BillInstance bi = getBillInstance();

            BillClientTank client = new BillClientTank().select(clientId, conn);
            if (!client.spanClosed) {
                throw new Exception("El cliente aun no tiene lectura para este periodo");
            }

            BillSpan reca = BillSpan.getByClient("reca", clientId, bi, conn);
            Integer billId = new MySQLQuery("SELECT id FROM bill_bill b WHERE b.client_tank_id = ?1 "
                    + "AND b.active AND b.total AND b.bill_span_id = ?2 ORDER BY b.id DESC LIMIT 1").setParam(1, clientId).setParam(2, reca.id).getAsInteger(conn);
            BillForPrint bill = GetBills.getById(billId, getBillInstance(), conn);
            //temporal
            File tmp = File.createTempFile("bill", ".zpl");
            BillWriter writer = BillWriter.getCurrentZplWriter(bi, sysCfg, billCfg, conn, tmp, true);
            writer.addBill(bill);
            writer.endDocument();
            return createResponse(tmp, "bill.zpl");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/rePrintPdf")
    public Response rePrint(@QueryParam("billId") int billId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            File f = BillBill.reprint(billId, this.getBillInstance(), conn);
            return createResponse(f, "bill.pdf");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/billInfo")
    public Response billInfo(@QueryParam("billNum") String billNum, @QueryParam("clieRef") String clieRef) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance inst = getBillInstance();
            useBillInstance(conn);
            BillForPrint billInfo = null;
            if (billNum != null) {
                BillBill bill = BillBill.getByBillNum(billNum, conn);
                if (bill == null) {
                    throw new Exception("No se encontró el cupón " + billNum);
                }
                billInfo = GetBills.getById(bill.id, inst, conn);
            } else if (clieRef != null) {
                billInfo = GetBills.getById(new BillImportAsoc2001.BillInfoFinder(conn).getBillId(clieRef, null), inst, conn);
                if (billInfo == null) {
                    throw new Exception("No se encontró el cupón " + billNum);
                }
            }
            return Response.ok(billInfo).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/changeInfo")
    public Response changeInfo(BillBill bill) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            BillInstance inst = getBillInstance();
            useBillInstance(conn);
            BillBill obj = new BillBill().select(bill.id, conn);
            BillSpan reca = BillSpan.getByClient("reca", obj.clientTankId, inst, conn);

            if (obj.billSpanId != reca.id) {
                throw new Exception("El cupón pertenece a otro periodo de recaudo, no se puede modificar.");
            }
            if (obj.paymentDate == null) {
                throw new Exception("El cupón no ha sido pagado.");
            }
            obj.bankId = bill.bankId;
            obj.paymentDate = bill.paymentDate;
            obj.registDate = new Date();
            obj.paymentDate = bill.paymentDate;
            obj.registrarId = sl.employeeId;
            obj.update(conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/payBill")
    public Response payBill(BillBill bill) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            try {
                conn.setAutoCommit(false);
                useBillInstance(conn);

                Date minDate = new MySQLQuery("SELECT min_payment_date FROM sigma.bill_instance WHERE id = ?1").setParam(1, getBillInstId()).getAsDate(conn);
                if (Dates.trimDate(bill.paymentDate).compareTo(Dates.trimDate(minDate)) < 0) {
                    throw new Exception("La fecha de pago no puede ser anterior a " + Dates.getDefaultFormat().format(minDate));
                }

                GregorianCalendar gc = new GregorianCalendar();
                gc.add(GregorianCalendar.DAY_OF_MONTH, 5);

                if (Dates.trimDate(bill.paymentDate).compareTo(Dates.trimDate(gc.getTime())) > 0) {
                    throw new Exception("La fecha de pago no puede ser posterior a " + Dates.getDefaultFormat().format(gc.getTime()));
                }

                BillBill.payBill(bill.id, bill.paymentDate, bill.bankId, sl.employeeId, getBillInstance(), conn);
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/partialPay")
    public Response partialPay(BillPartialPayRequest req) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            BillBill bill = BillPartialPayRequest.processRequest(req, this.getBillInstance(), sl, conn);
            return createResponse(bill);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/analyseAsob2001")
    public Response analyseAsob2001(@Context HttpServletRequest request) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);
            return createResponse(new StringResponse(null, BillImportAsoc2001.analyseAsob2001(mr.getFile().file)));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/importAsob2001")
    public Response importAsob2001(@Context HttpServletRequest request) throws Exception {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);
            String asocData = mr.params.get("asocData");
            return createResponse(new StringResponse(null, BillImportAsoc2001.importAsob2001(sl.employeeId, mr.getFile().file, asocData)));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/changeBankAsob2001")
    public Response changeBankAsob2001(@Context HttpServletRequest request) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);
            String asocData = mr.params.get("asocData");
            return createResponse(new StringResponse(null, BillChangeBankAsob2001.changeBank2001(mr.getFile().file, asocData)));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/export")
    public Response export(@QueryParam("spanId") int spanId, @QueryParam("format") int format) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            SysCfg sysCfg = SysCfg.select(conn);
            useBillInstance(conn);
            BillCfg billCfg = new BillCfg().select(1, conn);
            BillInstance inst = getBillInstance();
            File txt = BillBillExporter.exportBills(spanId, sysCfg, billCfg, inst, format, conn);
            return createResponse(txt, "bills.txt");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/rptGeneratedBills")
    public Response getRptGeneratedBills(@QueryParam("spanId") Integer spanId, @QueryParam("showZero") boolean showZero, @QueryParam("allInstances") boolean allInstances) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillSpan baseSpan = new BillSpan().select(spanId, conn);

            List<BillInstance> instances;

            if (allInstances) {
                instances = BillInstance.getAll(conn);
            } else {
                instances = new ArrayList<>();
                instances.add(getBillInstance());
            }

            MySQLReport rep = new MySQLReport("Reporte de Cupones Generados", "", "", MySQLQuery.now(conn));
            rep.getSubTitles().add("Periodo: " + baseSpan.getConsLabel());
            rep.setShowNumbers(true);

            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
            rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.00"));//1
            rep.setZoomFactor(80);

            for (int i = 0; i < instances.size(); i++) {
                BillInstance inst = instances.get(i);
                inst.useInstance(conn);
                BillSpan span = BillSpan.getByMonth(inst.db, baseSpan.consMonth, conn);
                if (span != null) {
                    Date printDate = new MySQLQuery("SELECT dt "
                            + "FROM (SELECT DATE(b.creation_date) as dt, COUNT(*) reg "
                            + "FROM bill_bill b WHERE b.bill_span_id = " + span.id + " "
                            + "GROUP BY DATE(b.creation_date) "
                            + "ORDER BY reg DESC LIMIT 1) AS tbl").getAsDate(conn);

                    if (printDate != null) {
                        Table tb = new Table(inst.name);
                        MySQLQuery bills;
                        if (inst.isTankInstance()) {
                            tb.getColumns().add(new Column("Edificio", 40, 0));//0
                            tb.getColumns().add(new Column("Dirección", 30, 0));//1
                            tb.getColumns().add(new Column("No. Fac", 15, 0));//2
                            tb.getColumns().add(new Column("Referencia", 15, 0));//3
                            tb.getColumns().add(new Column("Cliente", 45, 0));//4         
                            tb.getColumns().add(new Column("Documento", 15, 0));//5
                            tb.getColumns().add(new Column("Apto", 12, 0));//6
                            tb.getColumns().add(new Column("No. Instalación", 15, 0));//7
                            tb.getColumns().add(new Column("Total", 18, 1));//8

                            bills = new MySQLQuery(
                                    " SELECT "
                                    + " bb.name, "
                                    + " bb.address, "
                                    + " b.bill_num, "
                                    + " c.code, "
                                    + " CONCAT(c.first_name, ' ' ,IFNULL(c.last_name,'')), "
                                    + " c.doc, "
                                    + " c.apartment, "
                                    + " c.num_install, "
                                    + " IFNULL(SUM(DISTINCT pl.value), 0) total "
                                    + " FROM bill_bill b "
                                    //  + " INNER JOIN bill_bill_pres p ON p.bill_id = b.id "
                                    + " INNER JOIN bill_client_tank c ON c.id = b.client_tank_id "
                                    + " INNER JOIN bill_building bb ON bb.id = c.building_id "
                                    + " LEFT JOIN bill_plan pl ON pl.account_deb_id = 15 AND pl.cli_tank_id = b.client_tank_id AND pl.doc_id = b.id AND pl.doc_type = 'fac' "
                                    + " WHERE  "
                                    + " b.bill_span_id = " + span.id + " "
                                    + " AND DATE(b.creation_date) = DATE( ?1 ) "
                                    + " AND b.total = 1 "
                                    + " GROUP BY b.id "
                                    + (showZero ? " " : " HAVING  total > 0 ")
                                    + " ORDER BY bb.name; ").setParam(1, printDate);

                        } else {
                            tb.getColumns().add(new Column("Barrio", 40, 0));//0
                            tb.getColumns().add(new Column("Dirección", 30, 0));//1
                            tb.getColumns().add(new Column("No. Fac", 15, 0));//2
                            tb.getColumns().add(new Column("Referencia", 15, 0));//3
                            tb.getColumns().add(new Column("Cliente", 45, 0));//4         
                            tb.getColumns().add(new Column("Documento", 15, 0));//5
                            tb.getColumns().add(new Column("No. Instalación", 15, 0));//6
                            tb.getColumns().add(new Column("Contrato", 12, 0));//6
                            tb.getColumns().add(new Column("Total", 18, 1));//7

                            bills = new MySQLQuery(
                                    " SELECT "
                                    + " bb.name, "
                                    + " c.address, "
                                    + " b.bill_num, "
                                    + " c.code, "
                                    + " CONCAT(c.first_name, ' ' ,IFNULL(c.last_name,'')), "
                                    + " c.doc, "
                                    + " c.num_install, "
                                    + " c.contract_num, "
                                    + " IFNULL(SUM(DISTINCT pl.value), 0) total "
                                    + " FROM bill_bill b "
                                    //  + " INNER JOIN bill_bill_pres p ON p.bill_id = b.id "
                                    + " INNER JOIN bill_client_tank c ON c.id = b.client_tank_id "
                                    + " LEFT JOIN sigma.neigh bb ON bb.id = c.neigh_id "
                                    + " LEFT JOIN bill_plan pl ON pl.account_deb_id = 15 AND pl.cli_tank_id = b.client_tank_id AND pl.doc_id = b.id AND pl.doc_type = 'fac' "
                                    + " WHERE  "
                                    + " b.bill_span_id = " + span.id + " "
                                    + " AND DATE(b.creation_date) = DATE( ?1 ) "
                                    + " AND b.total = 1 "
                                    + " GROUP BY b.id "
                                    + (showZero ? " " : " HAVING  total > 0 ")
                                    + " ORDER BY bb.name; ").setParam(1, printDate);
                        }
                        tb.setData(bills.getRecords(conn));
                        if (tb.getData().length > 0) {
                            rep.getTables().add(tb);
                        }
                    }
                }
            }

            useDefault(conn);
            return createResponse(rep.write(conn), "listado_fras.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
