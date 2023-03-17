package api.bill.api;

import api.BaseAPI;
import api.bill.model.BillBill;
import api.bill.model.BillClientTank;
import api.bill.model.BillCovidCfg;
import api.bill.model.BillCovidPoll;
import api.bill.model.BillFinanceNote;
import api.bill.model.BillFinanceNoteFee;
import api.bill.model.BillPriceSpan;
import api.bill.model.BillSpan;
import api.bill.model.BillTransaction;
import api.bill.model.EqualPayment;
import controller.billing.BillImportAsoc2001.RefInfo;
import controller.billing.BillTransactionController;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.billing.constants.Accounts;
import model.billing.constants.Transactions;
import utilities.Dates;
import static utilities.Dates.getMonths;
import utilities.MySQLPreparedInsert;
import utilities.MySQLQuery;
import utilities.apiClient.IntegerResponse;
import web.marketing.smsClaro.ClaroSmsSender;
import web.quality.MailCfg;
import web.quality.SendMail;

@Path("/billCovidFinance")

public class BillCovidFinanceApi extends BaseAPI {

    private static BillSpan getByMonth(Date month, Connection conn) throws Exception {
        return new BillSpan().select(new MySQLQuery("SELECT " + BillSpan.getSelFlds("") + " FROM bill_span WHERE YEAR(end_date) = YEAR(?1) AND MONTH(end_date) = MONTH(?1)").setParam(1, month), conn);
    }

    public static boolean canBeFinanced(BillCovidCfg cfg, int spanId, Connection conn) throws Exception {
        BillSpan m = getByMonth(cfg.firstBill, conn);
        if (m == null) {
            return false;
        }
        int beg = m.id - 1;
        int end = beg + Dates.getMonths(cfg.firstBill, cfg.lastBill);
        return beg <= spanId && spanId <= end;
    }

    @GET
    @Path("query")
    public Response query(@QueryParam("ref") String ref, @QueryParam("fac") String fac, @QueryParam("total") int total, @QueryParam("payments") int payments) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        try (Connection conn = getConnection()) {
            BillCovidCfg cfg = new BillCovidCfg().select(1, conn);
            QueryResponse rta = new QueryResponse();
            try {
                if (cfg.enabled) {
                    RefInfo info = RefInfo.getInfo(fac);
                    useBillInstance(info.instId, conn);
                    BillBill bill = BillBill.getById(info.id, conn);
                    BillClientTank client = new BillClientTank().select(bill.clientTankId, conn);
                    if (client.code.equals(ref)) {
                        BigDecimal bTotal = new MySQLQuery("SELECT SUM(value) FROM bill_plan WHERE doc_id = ?1").setParam(1, bill.id).getAsBigDecimal(conn, true);
                        if (bTotal.compareTo(new BigDecimal(total)) == 0) {
                            BillSpan reca = BillSpan.getByState("reca", conn);
                            if (canBeFinanced(cfg, bill.billSpanId, conn)) {
                                if (bill.billSpanId == reca.id) {
                                    BigDecimal totalCart = getTotalCart(client.id, reca, conn);
                                    if (cfg.allowDebt || totalCart.compareTo(BigDecimal.ZERO) == 0) {
                                        BigDecimal finanConsDebt = new MySQLQuery("SELECT SUM(IF(t.account_deb_id = ?2, t.value, t.value*-1)) FROM bill_transaction t "
                                                + "WHERE "
                                                + "t.cli_tank_id = ?1 "
                                                + "AND t.bill_span_id = " + reca.id + " "
                                                + "AND (t.account_cred_id = ?2 OR t.account_deb_id = ?2)").setParam(1, client.id).setParam(2, Accounts.C_CONS).getAsBigDecimal(conn, true);

                                        if (finanConsDebt.compareTo(BigDecimal.ZERO) > 0) {
                                            int listId = BillPriceSpan.getListId(conn, reca.id, client.id);
                                            Integer maxPayments = new MySQLQuery("SELECT max_finan_payments FROM bill_price_list WHERE id = ?1").setParam(1, listId).getAsInteger(conn);
                                            if (maxPayments > 0) {
                                                rta.payments = Integer.min(maxPayments, payments);
                                                rta.billId = bill.id;
                                                rta.instId = info.instId;
                                                rta.capital = df.format(finanConsDebt);
                                                EqualPayment[] values = EqualPayment.getValues(finanConsDebt, null, cfg.finanRate, null, rta.payments);
                                                BigDecimal totalInter = BigDecimal.ZERO;
                                                for (EqualPayment value : values) {
                                                    totalInter = totalInter.add(value.interest);
                                                }
                                                rta.interest = df.format(totalInter);
                                                rta.payment = df.format(values[0].capital.add(values[0].interest));
                                            } else {
                                                rta.error = "Su categoría no tiene este beneficio, para otras opciones de financiación comuníquese sin costo al 018000914080 o al #876";
                                            }
                                        } else {
                                            rta.error = "La cuenta no tiene deudas por consumo, para otras opciones de financiación comuníquese sin costo al 018000914080 o al #876";
                                        }
                                    } else {
                                        rta.error = "Para acceder a este beneficio debe estár al día, en este momento la cuenta presenta una deuda por " + df.format(totalCart) + ". Para otras opciones de financiación comuníquese sin costo al 018000914080 o al #876.";
                                        rta.hasDebt = true;
                                    }
                                } else {
                                    rta.error = "La factura " + fac + " está vencida, para otras opciones de financiación comuníquese sin costo al 018000914080 o al #876";
                                }
                            } else {
                                rta.error = "La factura " + fac + " no pertenece a un periodo que goce de este beneficio, para otras opciones de financiación comuníquese sin costo al 018000914080 o al #876";
                            }
                        } else {
                            rta.error = "El total " + total + " no corresponde con la factura, por favor verifique los datos e intentelo nuevamente.";
                        }
                    } else {
                        rta.error = "La referencia " + ref + " no corresponde con la factura, por favor verifique los datos e intentelo nuevamente.";
                    }
                    rta.instNum = client.numInstall;
                } else {
                    rta.error = "La opción no está activa aún.";
                }
            } catch (Exception ex) {
                Logger.getLogger(BillCovidFinanceApi.class.getName()).log(Level.SEVERE, "message", ex);
                rta.error = "No se encontró la factura " + fac + ", por favor verifique los datos e intentelo nuevamente.";
            }
            return createResponse(rta);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("sendCode")
    public Response sendCode(
            @QueryParam("instId") int instId,
            @QueryParam("billId") int billId,
            @QueryParam("names") String names,
            @QueryParam("phone") String phone,
            @QueryParam("mail") String mail,
            @QueryParam("promo") String promo,
            @QueryParam("sendTo") String sendTo
    ) {
        //I, l, 1, 0, O, 5, S se excluyen porque se prestan a confusión
        char[] l = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q', 'R', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '2', '3', '4', '6', '7', '8', '9'};
        phone = phone.replaceAll("-", "");
        try (Connection conn = getConnection()) {
            BillCovidPoll p = new BillCovidPoll();
            p.billId = billId;
            p.instId = instId;
            useBillInstance(instId, conn);
            BillBill bill = BillBill.getById(billId, conn);
            p.clientId = bill.clientTankId;
            p.code = new String(new char[]{l[(int) (Math.random() * l.length)], l[(int) (Math.random() * l.length)], l[(int) (Math.random() * l.length)], l[(int) (Math.random() * l.length)]});
            p.names = names;
            p.phone = phone;
            p.mail = mail;
            p.dt = new Date();
            p.sendPromo = getAsBoolean(promo);
            useDefault(conn);
            switch (sendTo) {
                case "mail":
                    MailCfg cfg = MailCfg.select(conn);
                    String html = SendMail.getHtmlMsg(conn, "Código Secreto", "Por favor, ingrese el siguiente código cuando le sea solicitado en su proceso de solicitud de financiación.<br/><h3>" + p.code + "</h3>");
                    SendMail.sendMail(cfg, mail, "Código Secreto", html, p.code, null, null, null, null);
                    break;
                case "sms":
                    String code = new ClaroSmsSender().sendMsg("Este es su codigo para continuar con el proceso de financiacion Montagas " + p.code, "1", phone);
                    System.out.println(code);
                    break;
                default:
                    throw new RuntimeException("unknown: " + sendTo);
            }
            return createResponse(new IntegerResponse(p.insert(conn)));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private boolean getAsBoolean(String s) throws Exception {
        if (s == null || s.isEmpty()) {
            return false;
        }
        switch (s.toLowerCase()) {
            case "true":
                return true;
            case "false":
                return false;
            default:
                throw new Exception("Unexpected: " + s);
        }
    }

    @GET
    @Path("create")
    public Response create(
            @QueryParam("pollId") int pollId,
            @QueryParam("payments") int payments,
            @QueryParam("stove") String stove,
            @QueryParam("heater") String heater,
            @QueryParam("chimney") String chimney,
            @QueryParam("washer") String washer,
            @QueryParam("other") String other,
            @QueryParam("code") String code) {

        DecimalFormat df = new DecimalFormat("#,##0.00");
        CreateResponse rta = new CreateResponse();
        try (Connection billConn = getConnection(); Connection sigmaConn = getConnection()) {
            BillCovidCfg cfg = new BillCovidCfg().select(1, sigmaConn);
            billConn.setAutoCommit(false);
            try {
                if (cfg.enabled) {
                    BillCovidPoll poll = new BillCovidPoll().select(pollId, sigmaConn);
                    useBillInstance(poll.instId, billConn);
                    poll.stove = getAsBoolean(stove);
                    poll.heater = getAsBoolean(heater);
                    poll.chimney = getAsBoolean(chimney);
                    poll.washer = getAsBoolean(washer);
                    poll.other = other;
                    poll.update(sigmaConn);
                    if (poll.code.equals(code.toUpperCase())) {

                        BillSpan reca = BillSpan.getByState("reca", billConn);
                        BillBill bill = BillBill.getById(poll.billId, billConn);
                        BillClientTank client = new BillClientTank().select(bill.clientTankId, billConn);
                        if (bill.billSpanId == reca.id) {
                            if (canBeFinanced(cfg, bill.billSpanId, billConn)) {
                                BigDecimal totalCart = getTotalCart(client.id, reca, billConn);
                                if (cfg.allowDebt || totalCart.compareTo(BigDecimal.ZERO) == 0) {
                                    BigDecimal finanConsDebt = new MySQLQuery("SELECT SUM(IF(t.account_deb_id = ?2, t.value, t.value*-1)) FROM bill_transaction t "
                                            + "WHERE "
                                            + "t.cli_tank_id = ?1 "
                                            + "AND t.bill_span_id = " + reca.id + " "
                                            + "AND (t.account_cred_id = ?2 OR t.account_deb_id = ?2)").setParam(1, client.id).setParam(2, Accounts.C_CONS).getAsBigDecimal(billConn, true);

                                    if (finanConsDebt.compareTo(BigDecimal.ZERO) > 0) {
                                        int listId = BillPriceSpan.getListId(billConn, reca.id, client.id);
                                        Integer maxPayments = new MySQLQuery("SELECT max_finan_payments FROM bill_price_list WHERE id = ?1").setParam(1, listId).getAsInteger(billConn);
                                        if (maxPayments > 0) {
                                            BillFinanceNote n = new BillFinanceNote();
                                            n.clientId = client.id;
                                            n.consSpanId = reca.id + 1;
                                            n.typeId = 1;
                                            SimpleDateFormat sdf = new SimpleDateFormat("MMMM/yyyy");
                                            rta.firstPayment = sdf.format(cfg.paymentsStart);
                                            //se resta uno porque cuenta desde el reca pero el cobro inicia en cons y otro porque el cobro inicia en el mes final y por eso no se cuenta
                                            n.delayPaymentSpans = getMonths(reca.consMonth, cfg.paymentsStart) - 2;
                                            n.description = "Financiación de la factura " + bill.billNum + " por emergencia covid-19.";
                                            n.insert(billConn);

                                            //faltan logs
                                            BillTransaction t = new BillTransaction();
                                            t.accountDebId = Accounts.C_FINAN_DEU_POR_COBRAR;
                                            t.accountCredId = Accounts.C_CONS;
                                            t.billSpanId = reca.id;
                                            t.cliTankId = client.id;
                                            t.creUsuId = 1;
                                            t.created = new Date();
                                            t.docId = n.id;
                                            t.docType = "finan";
                                            t.transTypeId = Transactions.N_FINAN;
                                            t.value = finanConsDebt;

                                            MySQLPreparedInsert insertQuery = BillTransaction.getInsertQuery(false, billConn);
                                            BillTransaction.insert(t, insertQuery);
                                            insertQuery.executeBatch();

                                            n.lastTransId = BillTransactionController.getLastTrasactionIdByClient(n.clientId, billConn);
                                            n.update(billConn);

                                            payments = Integer.min(maxPayments, payments);
                                            EqualPayment[] values = EqualPayment.getValues(finanConsDebt, null, cfg.finanRate, null, payments);
                                            BigDecimal totalInter = BigDecimal.ZERO;
                                            for (int i = 0; i < values.length; i++) {
                                                EqualPayment p = values[i];
                                                BillFinanceNoteFee fee = new BillFinanceNoteFee();
                                                fee.capital = p.capital;
                                                fee.interest = p.interest;
                                                fee.noteId = n.id;
                                                fee.place = i;
                                                fee.insert(billConn);
                                                totalInter = totalInter.add(p.interest);
                                            }

                                            String capital = df.format(finanConsDebt);
                                            String interest = df.format(totalInter);
                                            String sPayments = Integer.min(maxPayments, payments) + "";
                                            String payment = df.format(values[0].capital.add(values[0].interest));

                                            String htmlbody = "La financiación de la factura " + bill.billNum + " fue exitosa, aplican las siguientes condiciones: <br>"
                                                    + "<b>Capital a Financiar: </b>" + capital + "<br/>"
                                                    + "<b>Total Intereses: </b>" + interest + "<br/>"
                                                    + "<b>Número de Cuotas: </b>" + sPayments + "<br/>"
                                                    + "<b>Valor de la Cuota: </b>" + payment + "<br/>"
                                                    + "<b>Inicio de las Cuotas: </b>" + rta.firstPayment + "<br/>";

                                            MailCfg mailCfg = MailCfg.select(sigmaConn);
                                            String html = SendMail.getHtmlMsg(sigmaConn, "Financiación Existosa", htmlbody);
                                            SendMail.sendMail(mailCfg, poll.mail, "Financiación Existosa", html, html, null, null, null, null);
                                            new ClaroSmsSender().sendMsg("Su financiacion Montagas fue registrada con exito, encontrara los detalles en su correo", "1", poll.phone);
                                        } else {
                                            rta.error = "Su categoría no tiene este beneficio, para otras opciones de financiación comuníquese sin costo al 018000914080 o al #876";
                                        }
                                    } else {
                                        rta.error = "La cuenta no tiene deudas por consumo, para otras opciones de financiación comuníquese sin costo al 018000914080 o al #876";
                                    }
                                } else {
                                    rta.error = "La cuenta tiene una deuda por " + df.format(totalCart) + ", para otras opciones de financiación comuníquese sin costo al 018000914080 o al #876";
                                }
                            } else {
                                rta.error = "La factura " + bill.billNum + " no pertenece a un periodo que goce de este beneficio, para otras opciones de financiación comuníquese sin costo al 018000914080 o al #876";
                            }

                        } else {
                            rta.error = "La factura " + bill.billNum + " está vencida, para otras opciones de financiación comuníquese sin costo al 018000914080 o al #876";
                        }
                    } else {
                        rta.error = "El código de seguridad no coincide";
                    }
                } else {
                    rta.error = "La opción no está activa aún.";
                }
                billConn.commit();
                return createResponse(rta);
            } catch (Exception ex) {
                billConn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private static BigDecimal getTotalCart(int clientId, BillSpan reca, Connection conn) throws Exception {
        int[] accs = new int[]{
            Accounts.C_CAR_GLP,
            Accounts.C_CAR_SRV,
            Accounts.C_CAR_FINAN_DEU,
            Accounts.C_CAR_CONTRIB,
            Accounts.C_CAR_INTE_CRE,
            Accounts.C_CAR_OLD,
            Accounts.C_INT_GLP,
            Accounts.C_INT_SRV,
            Accounts.C_INT_FINAN_DEU,
            Accounts.C_INT_CONTRIB,
            Accounts.C_INT_OLD
        };

        MySQLQuery inteCauGlpQ = new MySQLQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.cli_tank_id = ?1 AND t.trans_type_id = " + Transactions.CAUSA_INTE_GLP + " AND t.bill_span_id = " + (reca.id - 1));
        MySQLQuery inteCauSrvQ = new MySQLQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.cli_tank_id = ?1 AND t.trans_type_id = " + Transactions.CAUSA_INTE_SRV + " AND t.bill_span_id = " + (reca.id - 1));

        BigDecimal cauInteGlp = inteCauGlpQ.setParam(1, clientId).getAsBigDecimal(conn, true);
        BigDecimal cauInteSrv = inteCauSrvQ.setParam(1, clientId).getAsBigDecimal(conn, true);

        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < accs.length; i++) {
            total = total.add(getBalance(accs[i], clientId, conn));
        }
        return total.subtract(cauInteGlp).subtract(cauInteSrv);
    }

    private static BigDecimal getBalance(int account, int clientId, Connection conn) throws Exception {
        return new MySQLQuery("SELECT SUM(IF(t.account_deb_id = " + account + ", t.value, t.value * -1)) FROM bill_transaction t "
                + "WHERE "
                + "t.cli_tank_id = ?1 "
                + "AND (t.account_cred_id = " + account + " OR t.account_deb_id = " + account + ")").setParam(1, clientId).getAsBigDecimal(conn, true);
    }

    public class QueryResponse {

        public String instNum;
        public String error;
        public boolean hasDebt = false;
        public Integer billId;
        public Integer instId;
        public String capital;
        public String interest;
        public String payment;
        public Integer payments;
    }

    public class CreateResponse {

        public String error;
        public String firstPayment;
    }

}
