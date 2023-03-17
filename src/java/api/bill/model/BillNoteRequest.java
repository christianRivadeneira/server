package api.bill.model;

import api.BaseAPI;
import api.sys.model.SysCrudLog;
import controller.billing.BillTransactionController;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import model.billing.constants.Accounts;
import model.billing.constants.Transactions;
import model.system.SessionLogin;
import utilities.MySQLPreparedQuery;
import utilities.MySQLQuery;

public class BillNoteRequest {

    public BillNote note;
    public List<BillTransaction> trans = new ArrayList<>();

    public static BillNote createNote(BillNoteRequest req, SessionLogin sess, BaseAPI caller, Connection conn) throws Exception {
        return createNote(req, "reca", sess, caller, conn);
    }
    
    public static BillNote createNote(BillNoteRequest req, String spanStatus, SessionLogin sess, BaseAPI caller, Connection conn) throws Exception {
        caller.useBillInstance(conn);
        BillNote obj = req.note;
        Integer serial = new MySQLQuery("SELECT MAX(serial) FROM bill_note WHERE type_notes = '" + obj.typeNotes + "'").getAsInteger(conn);
        if (serial == null) {
            serial = 0;
        }

        if (req.trans.isEmpty()) {
            throw new Exception("Ingrese algún valor.");
        }

        int recaId = BillSpan.getByClient(spanStatus, obj.clientTankId, caller.getBillInstance(), conn).id;
        if (obj.billSpanId != recaId) {
            throw new RuntimeException("No se puede crear en el periodo indicado.");
        }

        obj.whenNotes = new Date();
        obj.serial = serial + 1;
        obj.createdForSpan = spanStatus;
        obj.active = true;
        obj.insert(conn);

        MySQLPreparedQuery credQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_cred_id = ?1 AND t.cli_tank_id = ?2", conn);
        MySQLPreparedQuery debQ = new MySQLPreparedQuery(" SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_deb_id  = ?1 AND t.cli_tank_id = ?2", conn);

        credQ.setParameter(2, obj.clientTankId);
        debQ.setParameter(2, obj.clientTankId);

        for (int i = 0; i < req.trans.size(); i++) {
            BillTransaction t = req.trans.get(i);
            t.billSpanId = obj.billSpanId;
            t.cliTankId = obj.clientTankId;
            t.creUsuId = sess.employeeId;
            t.modUsuId = sess.employeeId;
            t.created = new Date();

            String accName;
            switch (obj.typeNotes) {
                case "aj_cred":
                case "n_cred":
                    credQ.setParameter(1, t.accountCredId);
                    debQ.setParameter(1, t.accountCredId);
                    accName = Accounts.accNames.get(t.accountCredId);
                    break;
                case "aj_deb":
                case "n_deb":
                    credQ.setParameter(1, t.accountDebId);
                    debQ.setParameter(1, t.accountDebId);
                    accName = Accounts.accNames.get(t.accountDebId);
                    break;
                default:
                    throw new RuntimeException();
            }

            BigDecimal balance = debQ.getAsBigDecimal(true).subtract(credQ.getAsBigDecimal(true));

            switch (obj.typeNotes) {
                case "aj_cred":
                    t.transTypeId = Transactions.N_AJ_CREDIT;
                    if (balance.subtract(t.value).compareTo(BigDecimal.ZERO) < 0 && t.accountCredId != Accounts.E_AJUST) {
                        throw new Exception("El valor excede el saldo en \"" + accName + "\".");
                    }
                    break;
                case "n_cred":
                    t.transTypeId = Transactions.N_CREDIT;
                    if (balance.subtract(t.value).compareTo(BigDecimal.ZERO) < 0) { //suma a la deuda
                        throw new Exception("El valor excede el saldo en \"" + accName + "\".");
                    }
                    break;
                case "aj_deb":
                    t.transTypeId = Transactions.N_AJ_DEBIT;
                    break;
                case "n_deb":
                    t.transTypeId = Transactions.N_DEBIT;
                    if (t.accountDebId == Accounts.C_ANTICIP) {
                        if (balance.add(t.value).compareTo(BigDecimal.ZERO) > 0) {
                            throw new Exception("El valor excede el saldo en \"" + accName + "\".");
                        }
                    }
                    break;
                default:
                    throw new RuntimeException();
            }
            t.docType = "not";
            t.docId = obj.id;
            t.insert(conn);
        }
        obj.lastTransId = BillTransactionController.getLastTrasactionIdByClient(obj.clientTankId, conn);
        obj.update(conn);
        BillBill.anullActiveBills(obj.clientTankId, recaId, conn);
        caller.useDefault(conn);
        SysCrudLog.created(caller, obj, conn);
        conn.commit();
        return obj;
    }
}
