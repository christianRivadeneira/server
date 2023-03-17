package api.bill.model;

import api.bill.writers.bill.GetBills;
import controller.billing.BillClientTankController;
import controller.billing.BillSpanController;
import static controller.billing.BillSpanController.getAdjusment;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import model.billing.BillBillPres;
import model.billing.constants.Accounts;
import model.billing.constants.Transactions;
import model.system.SessionLogin;
import utilities.MySQLPreparedInsert;
import utilities.MySQLPreparedQuery;
import utilities.MySQLQuery;

public class BillPartialPayRequest {

    public int clientId;
    public List<BillPlan> plans = new ArrayList<>();

    public static BillBill processRequest(BillPartialPayRequest req, BillInstance inst, SessionLogin sl, Connection conn) throws Exception {
        inst.useInstance(conn);

        if (req.plans.isEmpty()) {
            throw new Exception("No se puede crear el cupón ya que no contiene ninguna transacción.");
        }

        BillSpan reca = BillSpan.getByClient("reca", req.clientId, inst, conn);

        //cuenta causaciones en el periodo
        Long contCau = new MySQLPreparedQuery("SELECT COUNT(t.id) FROM bill_transaction t WHERE t.trans_type_id = " + Transactions.CAUSA + " AND t.bill_span_id = " + reca.id, conn).getAsLong();
        if (contCau == null || contCau == 0) {
            throw new Exception("Aun no ha causado los servicios para este periodo.\nImposible continuar.");
        }

        //BillClientTank client = new BillClientTank().select(req.clientId, conn);
        new MySQLQuery("UPDATE bill_bill SET active = 0 WHERE payment_date IS NULL AND client_tank_id = " + req.clientId).executeUpdate(conn);

        BillBill bill = new BillBill();
        bill.active = true;
        bill.billSpanId = reca.id;
        bill.creationDate = new Date();
        bill.creatorId = sl.employeeId;
        bill.clientTankId = req.clientId;
        bill.insert(conn);

        int recaId = BillSpan.getByClient("reca", bill.clientTankId, inst, conn).id;
        if (bill.billSpanId != recaId) {
            throw new RuntimeException("No se puede crear en el periodo indicado.");
        }

        MySQLPreparedQuery debTotalQ = BillSpanController.getdebTotalQuery(conn);
        MySQLPreparedQuery credTotalQ = BillSpanController.getcredTotalQuery(conn);
        MySQLPreparedQuery monthsQuery = BillClientTankController.getCartBySpanQuery(Accounts.C_CAR_GLP, conn);

        //colocando el número de factura
        bill.billNum = (BillBill.getPaymentReference(inst.id, bill));
        //hallando las deudas por rubro.
        BigDecimal carteraOld = BillSpanController.getTotalBalance(debTotalQ, credTotalQ, Accounts.C_CAR_OLD, req.clientId);
        BigDecimal interesesOld = BillSpanController.getTotalBalance(debTotalQ, credTotalQ, Accounts.C_INT_OLD, req.clientId);

        //hallando el crédito disponible
        //contar los meses en deuda
        BigDecimal cartera = BillSpanController.getTotalBalance(debTotalQ, credTotalQ, Accounts.C_CAR_GLP, req.clientId);
        int debMonts = BillClientTankController.getDebtMonths(req.clientId, monthsQuery, conn, cartera);
        if (carteraOld.compareTo(BigDecimal.ZERO) != 0 || interesesOld.compareTo(BigDecimal.ZERO) != 0) {
            debMonts++;
        }
        bill.months = debMonts;
        bill.update(conn);
        ///bancos

        BigDecimal fromBank = BigDecimal.ZERO;
        Date today = new Date();
        MySQLPreparedInsert planInsertQ = BillPlan.getInsertQuery(conn);

        MySQLPreparedQuery credQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_cred_id = ?1 AND t.cli_tank_id = ?2", conn);
        MySQLPreparedQuery debQ = new MySQLPreparedQuery(" SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_deb_id  = ?1 AND t.cli_tank_id = ?2", conn);

        credQ.setParameter(2, bill.clientTankId);
        debQ.setParameter(2, bill.clientTankId);

        MySQLPreparedInsert presQ = BillBillPres.getInsertQuery(conn);

        for (int i = 0; i < req.plans.size(); i++) {
            BillPlan plan = req.plans.get(i);
            BillBillPres pres = new BillBillPres(bill.id, Accounts.accNames.get(plan.accountCredId), plan.value, i, false);
            BillBillPres.insert(pres, presQ);

            plan.created = today;
            plan.docId = bill.id;
            plan.transTypeId = Transactions.PAGO_BANCO;
            plan.docType = "fac";
            plan.billSpanId = reca.id;
            plan.cliTankId = req.clientId;
            plan.creUsuId = sl.employeeId;
            plan.modUsuId = sl.employeeId;
            plan.credit = BigDecimal.ZERO;
            plan.prev = false;
            BillPlan.insert(plan, planInsertQ);
            fromBank = fromBank.add(plan.value);

            credQ.setParameter(1, plan.accountCredId);
            debQ.setParameter(1, plan.accountCredId);
            BigDecimal balance = debQ.getAsBigDecimal(true).subtract(credQ.getAsBigDecimal(true));

            BigDecimal res = balance.subtract(plan.value);
            if (res.compareTo(BigDecimal.ZERO) < 0 && plan.accountCredId != Accounts.E_AJUST) {
                String accName = Accounts.accNames.get(plan.accountCredId);
                throw new Exception("El valor excede el saldo en \"" + accName + "\".");
            }
        }

        //ajuste a la unidad
        BigDecimal spanAdjust = new BigDecimal(reca.adjust);

        BigDecimal adjust = getAdjusment(fromBank, spanAdjust);
        if (adjust.compareTo(BigDecimal.ZERO) != 0) {
            BillBillPres pres = new BillBillPres(bill.id, "Ajuste a la Decena", adjust, req.plans.size(), false);
            BillBillPres.insert(pres, presQ);
            fromBank = GetBills.payDebtFromBank(fromBank, bill.id, planInsertQ, adjust, Accounts.E_AJUST, Transactions.PAGO_BANCO, reca.id, req.clientId, sl.employeeId);
        }
        BillBillPres pres = new BillBillPres(bill.id, "Total Abono", fromBank, req.plans.size() + 1, true);
        BillBillPres.insert(pres, presQ);
        presQ.executeBatch();
        planInsertQ.executeBatch();
        return bill;
    }
}
