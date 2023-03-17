package api.bill.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.bill.model.BillAccBalance;
import api.bill.model.BillBill;
import api.bill.model.BillBuilding;
import api.bill.model.BillClientTank;
import api.bill.model.BillFinanceNote;
import api.bill.model.BillFinanceNoteFee;
import api.bill.model.BillInstance;
import api.bill.model.BillNote;
import api.bill.model.BillSpan;
import api.bill.model.BillTransaction;
import api.bill.model.EqualPayment;
import api.bill.model.dto.BillFinanInsertRequest;
import api.bill.writers.note.FinanNoteWriter;
import api.bill.writers.note.NoteWriter;
import api.sys.model.City;
import api.sys.model.SysCrudLog;
import controller.billing.BillSpanController;
import controller.billing.BillTransactionController;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import metadata.log.Diff;
import model.billing.BillBank;
import model.billing.constants.Accounts;
import model.billing.constants.Transactions;
import model.system.SessionLogin;
import utilities.MySQLPreparedInsert;
import utilities.MySQLPreparedQuery;
import utilities.MySQLQuery;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.HeaderColumn;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.SummaryRow;
import utilities.mysqlReport.Table;
import utilities.mysqlReport.TableHeader;

@Path("/billFinanceNote")
public class BillFinanceNoteApi extends BaseAPI {

    public static BillFinanceNote createFinanNote(BillFinanInsertRequest req, BillInstance inst, int consId, SessionLogin session, Connection billConn, Connection sigmaConn) throws Exception {
        BillFinanceNote obj = req.note;
        obj.insert(billConn);
        if (obj.consSpanId != consId) {
            throw new Exception("No se puede crear para el periodo seleccionado");
        }

        List<BillAccBalance> accs = req.accs;
        BigDecimal totalCap = BigDecimal.ZERO;
        MySQLPreparedInsert insertQuery = BillTransaction.getInsertQuery(false, billConn);

        MySQLPreparedQuery debQ = BillSpanController.getdebTotalQuery(billConn);
        MySQLPreparedQuery credQ = BillSpanController.getcredTotalQuery(billConn);
        debQ.setParameter(2, obj.clientId);
        credQ.setParameter(2, obj.clientId);

        for (int i = 0; i < accs.size(); i++) {
            BillAccBalance acc = accs.get(i);
            if (acc.value.compareTo(BigDecimal.ZERO) > 0) {
                debQ.setParameter(1, acc.accId);
                credQ.setParameter(1, acc.accId);
                BigDecimal newBal = debQ.getAsBigDecimal(true).subtract(credQ.getAsBigDecimal(true)).subtract(acc.value);
                if (newBal.compareTo(BigDecimal.ZERO) < 0) {
                    throw new Exception("El valor no puede exceder la deuda en " + Accounts.accNames.get(acc.accId));
                }

                if (Accounts.C_FINAN_DEU_POR_COBRAR == acc.accId) {
                    throw new Exception("No se puede incluir la cuenta de " + Accounts.accNames.get(Accounts.C_FINAN_DEU_POR_COBRAR));
                }
                totalCap = totalCap.add(acc.value);
                BillTransaction t = new BillTransaction();
                t.accountDebId = Accounts.C_FINAN_DEU_POR_COBRAR;
                t.accountCredId = acc.accId;
                t.billSpanId = consId - 1;
                t.cliTankId = obj.clientId;
                t.creUsuId = 1;
                t.created = new Date();
                t.docId = obj.id;
                t.docType = "finan";
                t.transTypeId = Transactions.N_FINAN;
                t.value = acc.value;
                BillTransaction.insert(t, insertQuery);
            }
        }
        insertQuery.executeBatch();

        obj.lastTransId = BillTransactionController.getLastTrasactionIdByClient(obj.clientId, billConn);
        obj.update(billConn);

        EqualPayment[] values = EqualPayment.getValues(totalCap, null, obj.interestRate, null, obj.payments);
        for (int i = 0; i < values.length; i++) {
            EqualPayment value = values[i];
            BillFinanceNoteFee fee = new BillFinanceNoteFee();
            fee.place = i;
            fee.capital = value.capital;
            fee.interest = value.interest;
            fee.noteId = obj.id;
            fee.insert(billConn);
        }

        BillBill.anullActiveBills(obj.clientId, consId - 1, billConn);

        SysCrudLog l = new SysCrudLog();
        l.billInstId = inst.id;
        l.dt = new Date();
        l.employeeId = session.employeeId;
        l.sessionId = session.id;
        l.ownerSerial = obj.id;
        l.table = Diff.getTableName(obj);
        l.type = "crea";
        l.insert(sigmaConn);
        return obj;
    }

    @POST
    @Path("insert")
    public Response insert(BillFinanInsertRequest req) {
        try (Connection billConn = getConnection(); Connection sigmaConn = getConnection()) {
            try {
                useBillInstance(billConn);
                billConn.setAutoCommit(false);
                sigmaConn.setAutoCommit(false);
                BillInstance inst = getBillInstance();
                SessionLogin session = getSession(sigmaConn);
                int consId = BillSpan.getByClient("cons", req.note.clientId, inst, billConn).id;
                BillFinanceNote obj = createFinanNote(req, inst, consId, session, billConn, sigmaConn);
                billConn.commit();
                sigmaConn.commit();
                return Response.ok(obj).build();
            } catch (Exception ex) {
                billConn.rollback();
                sigmaConn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(BillFinanceNote obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillFinanceNote old = new BillFinanceNote().select(obj.id, conn);
            old.caused = false;
            obj.update(conn);
            useDefault(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillFinanceNote obj = new BillFinanceNote().select(id, conn);
            BillSpan reca = BillSpan.getByClient("reca", obj.clientId, getBillInstance(), conn);
            Integer bills = new MySQLQuery("SELECT COUNT(*) FROM bill_finance_note_fee WHERE note_id = ?1").setParam(1, obj.id).getAsInteger(conn);
            //reca + 1 es el periodo en consumo
            //startSpanId + bills.size() -1 + 1 es el ID del periodo que le correspondería a la nueva cuota
            //     obj.caused = (reca.id + 1 > obj.consSpanId + bills);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection billConn = getConnection(); Connection gralConn = getConnection()) {
            getSession(billConn);
            try {
                billConn.setAutoCommit(false);
                useBillInstance(billConn);
                BillFinanceNote obj = new BillFinanceNote().select(id, billConn);
                Integer lastId = BillTransactionController.getLastTrasactionIdByClient(obj.clientId, billConn);
                if (obj.lastTransId != lastId) {
                    throw new Exception("Han habido cambios en la cuenta.");
                }

                DecimalFormat df = new DecimalFormat("#,##0.00");
                List<BillTransaction> ts = BillTransaction.getByDoc(obj.id, "finan", billConn);
                StringBuilder sb = new StringBuilder();
                sb.append("Valores\n");
                for (int i = 0; i < ts.size(); i++) {
                    BillTransaction t = ts.get(i);
                    String accName = Accounts.accNames.get(t.accountCredId);
                    sb.append(accName).append(": ").append(df.format(t.value)).append("\n");
                    BillTransaction.delete(t.id, billConn);
                }
                sb.append("Cuotas\n");
                List<BillFinanceNoteFee> fees = BillFinanceNoteFee.getByNote(obj.id, billConn);
                for (int i = 0; i < fees.size(); i++) {
                    BillFinanceNoteFee f = fees.get(i);
                    sb.append((i + 1)).append(" ").append("Capital: ").append(df.format(f.capital)).append(" Interés: ").append(df.format(f.interest)).append("\n");
                    BillFinanceNoteFee.delete(f.id, billConn);
                }
                obj.canceled = true;
                obj.update(billConn);
                billConn.commit();
                SysCrudLog.updated(this, obj, "Se Canceló:\n" + sb.toString(), gralConn);
                return createResponse();
            } catch (Exception ex) {
                billConn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/grid")
    public Response getGrid(@QueryParam("clientId") int clientId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillSpan reca = BillSpan.getByClient("reca", clientId, getBillInstance(), conn);
            GridResult gr = new GridResult();

            String q = ""
                    + "SELECT "
                    + "s.id, "
                    + "t.name, "
                    + "CONCAT('Consumos de ', DATE_FORMAT(p.cons_month,'%M de %Y')), "
                    + "(SELECT SUM(f.capital + COALESCE(f.interest, 0)) FROM bill_finance_note_fee f WHERE f.note_id = s.id), "
                    + "(SELECT SUM(f.capital + COALESCE(f.interest, 0)) FROM bill_finance_note_fee f WHERE f.note_id = s.id AND f.caused), "
                    + "(SELECT SUM(f.capital + COALESCE(f.interest, 0)) FROM bill_finance_note_fee f WHERE f.note_id = s.id AND !f.caused) "
                    + "FROM "
                    + "bill_finance_note as s "
                    + "INNER JOIN sigma.bill_finance_note_type as t ON s.type_id = t.id "
                    + "INNER JOIN bill_span as p ON s.cons_span_id = p.id "
                    + "WHERE s.client_id = " + clientId;
            gr.data = new MySQLQuery(q).setParam(1, reca.id).getRecords(conn);
            gr.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 350, "Tipo"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 350, "Desde"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 200, "Valor"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 200, "Causado"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 200, "Por Causar")};
            return createResponse(gr);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/report")
    public Response getReport(@QueryParam("justPending") boolean justPending) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance inst = getBillInstance();
            useBillInstance(conn);

            if (new MySQLQuery("SELECT COUNT(*) > 0 FROM bill_client_tank t WHERE t.span_closed and t.active").getAsBoolean(conn)) {
                throw new Exception("El cierre del periodo está en progreso");
            }
            MySQLReport rep = new MySQLReport("Financiaciones - " + inst.name, "", "Hoja 1", now(conn));

            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
            rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.00"));//1
            rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//2
            rep.setZoomFactor(85);
            rep.setVerticalFreeze(6);

            List<BillFinanceNote> srvs;
            if (justPending) {
                srvs = BillFinanceNote.getList(new MySQLQuery("SELECT " + BillFinanceNote.getSelFlds("s") + " FROM bill_finance_note s WHERE s.caused = 0 ORDER BY s.id DESC"), conn);
            } else {
                srvs = BillFinanceNote.getList(new MySQLQuery("SELECT " + BillFinanceNote.getSelFlds("s") + " FROM bill_finance_note s ORDER BY s.id DESC"), conn);
            }

            List<Object[]> data = new ArrayList<>();

            for (int i = 0; i < srvs.size(); i++) {
                BillFinanceNote note = srvs.get(i);
                Object[] extraRow = new MySQLQuery("SELECT "
                        + "(SELECT SUM(f.capital) FROM bill_finance_note_fee f WHERE f.note_id = s.id), "
                        + "(SELECT SUM(f.capital) FROM bill_finance_note_fee f WHERE f.note_id = s.id AND f.caused = 1), "
                        + "(SELECT SUM(f.capital) FROM bill_finance_note_fee f WHERE f.note_id = s.id AND f.caused = 0), "
                        + "(SELECT SUM(f.interest) FROM bill_finance_note_fee f WHERE f.note_id = s.id), "
                        + "(SELECT SUM(f.interest) FROM bill_finance_note_fee f WHERE f.note_id = s.id AND f.caused = 1), "
                        + "(SELECT SUM(f.interest) FROM bill_finance_note_fee f WHERE f.note_id = s.id AND f.caused = 0), "
                        + "(SELECT count(*) FROM bill_finance_note_fee f WHERE f.note_id = s.id), "
                        + "(SELECT count(*) FROM bill_finance_note_fee f WHERE f.note_id = s.id AND f.caused = 1), "
                        + "(SELECT count(*) FROM bill_finance_note_fee f WHERE f.note_id = s.id AND f.caused = 0) "
                        + "FROM "
                        + "bill_finance_note as s "
                        + "WHERE s.id = ?1").setParam(1, note.id).getRecord(conn);

                BillClientTank c = new BillClientTank().select(note.clientId, conn);
                
                
                
                //BillServiceType t = new BillServiceType().select(srv.typeId, conn);
                BillSpan sp = new BillSpan().select(note.consSpanId, conn);
                Object[] row = new Object[13];
                if (inst.isTankInstance()) {
                    row[0] = c.numInstall;
                } else {
                    row[0] = c.code;
                }
                row[1] = c.firstName + (c.lastName != null ? " " + c.lastName : "");
                row[2] = new MySQLQuery("SELECT name FROM sigma.bill_finance_note_type t WHERE t.id = ?1").setParam(1, note.typeId).getAsString(conn);
                row[3] = sp.getConsLabel();
                System.arraycopy(extraRow, 0, row, 4, extraRow.length);
                data.add(row);

            }

            if (!data.isEmpty()) {

                Table tbl = new Table("Financiaciones");
                TableHeader th = new TableHeader();
                tbl.getHeaders().add(th);
                th.getColums().add(new HeaderColumn("Num. Inst", 1, 2));//0
                th.getColums().add(new HeaderColumn("Nombre", 1, 2));
                th.getColums().add(new HeaderColumn("Tipo", 1, 2));
                th.getColums().add(new HeaderColumn("Desde", 1, 2));
                th.getColums().add(new HeaderColumn("Capital", 3, 1));
                th.getColums().add(new HeaderColumn("Interes", 3, 1));
                th.getColums().add(new HeaderColumn("Cuotas", 3, 1));

                if (inst.isTankInstance()) {
                    tbl.getColumns().add(new Column("Num. Inst", 12, 0));//0
                } else {
                    tbl.getColumns().add(new Column("NIU", 12, 0));//0
                }
                tbl.getColumns().add(new Column("Nombre", 40, 0));//0
                tbl.getColumns().add(new Column("Tipo", 40, 0));//0
                tbl.getColumns().add(new Column("Desde", 28, 0));//2
                tbl.getColumns().add(new Column("Total", 20, 1));//1
                tbl.getColumns().add(new Column("Facturado", 20, 1));//1
                tbl.getColumns().add(new Column("Por Facturar", 20, 1));//1
                tbl.getColumns().add(new Column("Total", 20, 1));//1                
                tbl.getColumns().add(new Column("Facturado", 20, 1));//1                
                tbl.getColumns().add(new Column("Por Facturar", 20, 1));//1
                tbl.getColumns().add(new Column("Total", 15, 2));//1
                tbl.getColumns().add(new Column("Facturadas", 15, 2));//1
                tbl.getColumns().add(new Column("Por Facturar", 15, 2));//1

                tbl.setData(data);
                tbl.setSummaryRow(new SummaryRow("Totales", 4));
                rep.getTables().add(tbl);
                useDefault(conn);
                return createResponse(rep.write(conn), "servicios_usuario.xls");
            } else {
                throw new Exception("No se hallaron datos");
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/print")
    public Response print(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance inst = getBillInstance();
            FinanNoteWriter w = new FinanNoteWriter(inst, conn);
            City city = new City().select(inst.cityId, conn);

            useBillInstance(conn);
            BillFinanceNote note = new BillFinanceNote().select(id, conn);
            BillClientTank client = new BillClientTank().select(note.clientId, conn);

            BillBuilding build = null;
            if (inst.isTankInstance()) {
                build = new BillBuilding().select(client.buildingId, conn);
            }
            BillSpan span = new BillSpan().select(note.consSpanId, conn);
            BillBank bank = null;
            List<BillFinanceNoteFee> lst = BillFinanceNoteFee.getByNote(note.id, conn);

            w.beginDocument();
            w.addNote(note, client, build, span, bank, city, lst);
            return createResponse(w.endDocument(), "nota.pdf");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/cancel")
    public Response cancel(@QueryParam("id") int id) {
        try (Connection billConn = getConnection(); Connection gralConn = getConnection()) {
            SessionLogin sess = getSession(billConn);
            try {
                billConn.setAutoCommit(false);
                useBillInstance(billConn);
                BillFinanceNote obj = new BillFinanceNote().select(id, billConn);
                BigDecimal pendCap = new MySQLQuery("SELECT sum(capital) FROM bill_finance_note_fee f WHERE f.note_id = ?1 AND f.caused = 0").setParam(1, obj.id).getAsBigDecimal(billConn, true);

                MySQLPreparedQuery debQ = BillSpanController.getdebTotalQuery(billConn);
                MySQLPreparedQuery credQ = BillSpanController.getcredTotalQuery(billConn);
                debQ.setParameter(1, Accounts.C_FINAN_DEU_POR_COBRAR);
                credQ.setParameter(1, Accounts.C_FINAN_DEU_POR_COBRAR);
                debQ.setParameter(2, obj.clientId);
                credQ.setParameter(2, obj.clientId);

                BigDecimal totalDeu = debQ.getAsBigDecimal(true).subtract(credQ.getAsBigDecimal(true));
                BillSpan reca = BillSpan.getByClient("reca", obj.clientId, getBillInstance(), billConn);

                BillTransaction t = new BillTransaction();
                t.accountDebId = Accounts.C_CUOTA_FINAN_DEU;
                t.accountCredId = Accounts.C_FINAN_DEU_POR_COBRAR;
                t.billSpanId = reca.id;
                t.cliTankId = obj.clientId;
                t.creUsuId = sess.employeeId;
                t.created = new Date();
                //t.docId = obj.id;
                //t.docType = "finan";
                //t.transTypeId = Transactions.N_FINAN;
                t.extra = "Cuotas Restantes Financiación";
                t.value = pendCap;
                MySQLPreparedInsert insertQuery = BillTransaction.getInsertQuery(false, billConn);
                BillTransaction.insert(t, insertQuery);
                insertQuery.executeBatch();
                new MySQLQuery("DELETE FROM bill_finance_note_fee WHERE note_id = ?1 AND caused = 0").setParam(1, obj.id).executeDelete(billConn);

                /*Integer lastId = BillTransactionController.getLastTrasactionIdByClient(obj.clientId, billConn);
                if (obj.lastTransId != lastId) {
                    throw new Exception("Han habido cambios en la cuenta.");
                }

                DecimalFormat df = new DecimalFormat("#,##0.00");
                List<BillTransaction> ts = BillTransaction.getByDoc(obj.id, "finan", billConn);
                StringBuilder sb = new StringBuilder();
                sb.append("Valores\n");
                for (int i = 0; i < ts.size(); i++) {
                    BillTransaction t = ts.get(i);
                    String accName = Accounts.accNames.get(t.accountCredId);
                    sb.append(accName).append(": ").append(df.format(t.value)).append("\n");
                    BillTransaction.delete(t.id, billConn);
                }
                sb.append("Cuotas\n");
                List<BillFinanceNoteFee> fees = BillFinanceNoteFee.getByNote(obj.id, billConn);
                for (int i = 0; i < fees.size(); i++) {
                    BillFinanceNoteFee f = fees.get(i);
                    sb.append((i + 1)).append(" ").append("Capital: ").append(df.format(f.capital)).append(" Interés: ").append(df.format(f.interest)).append("\n");
                    BillFinanceNoteFee.delete(f.id, billConn);
                }
                obj.canceled = true;
                obj.update(billConn);*/
                BillBill.anullActiveBills(obj.clientId, reca.id, billConn);
                obj.caused = true;
                obj.update(billConn);
                billConn.commit();
                SysCrudLog.updated(this, obj, "Cobro total en el periodo actual", gralConn);
                return createResponse();
            } catch (Exception ex) {
                billConn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
