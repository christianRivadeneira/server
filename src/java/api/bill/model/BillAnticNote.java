package api.bill.model;

import api.BaseAPI;
import api.BaseModel;
import api.Params;
import api.sys.model.SysCrudLog;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import metadata.log.Diff;
import model.billing.constants.Accounts;
import model.billing.constants.Transactions;
import model.system.SessionLogin;
import utilities.MySQLQuery;

public class BillAnticNote extends BaseModel<BillAnticNote> {
//inicio zona de reemplazo

    public String descNotes;
    public Date whenNotes;
    public int clientTankId;
    public int billSpanId;
    public int typeId;
    public int serial;
    public boolean active;
    public String label;
    public int creUsuId;
    public Integer bankId;
    public Integer srvFailId;
    public Integer rebillId;
    public Date bankDate;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "desc_notes",
            "when_notes",
            "client_tank_id",
            "bill_span_id",
            "type_id",
            "serial",
            "active",
            "label",
            "cre_usu_id",
            "bank_id",
            "srv_fail_id",
            "rebill_id",
            "bank_date"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, descNotes);
        q.setParam(2, whenNotes);
        q.setParam(3, clientTankId);
        q.setParam(4, billSpanId);
        q.setParam(5, typeId);
        q.setParam(6, serial);
        q.setParam(7, active);
        q.setParam(8, label);
        q.setParam(9, creUsuId);
        q.setParam(10, bankId);
        q.setParam(11, srvFailId);
        q.setParam(12, rebillId);
        q.setParam(13, bankDate);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        descNotes = MySQLQuery.getAsString(row[0]);
        whenNotes = MySQLQuery.getAsDate(row[1]);
        clientTankId = MySQLQuery.getAsInteger(row[2]);
        billSpanId = MySQLQuery.getAsInteger(row[3]);
        typeId = MySQLQuery.getAsInteger(row[4]);
        serial = MySQLQuery.getAsInteger(row[5]);
        active = MySQLQuery.getAsBoolean(row[6]);
        label = MySQLQuery.getAsString(row[7]);
        creUsuId = MySQLQuery.getAsInteger(row[8]);
        bankId = MySQLQuery.getAsInteger(row[9]);
        srvFailId = MySQLQuery.getAsInteger(row[10]);
        rebillId = MySQLQuery.getAsInteger(row[11]);
        bankDate = MySQLQuery.getAsDate(row[12]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_antic_note";
    }

    public static String getSelFlds(String alias) {
        return new BillAnticNote().getSelFldsForAlias(alias);
    }

    public static List<BillAnticNote> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillAnticNote().getListFromQuery(q, conn);
    }

    public static List<BillAnticNote> getList(Params p, Connection conn) throws Exception {
        return new BillAnticNote().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillAnticNote().deleteById(id, conn);
    }

    public static List<BillAnticNote> getAll(Connection conn) throws Exception {
        return new BillAnticNote().getAllList(conn);
    }

//fin zona de reemplazo
    public static BillAnticNote getBySrvFail(int failId, Connection conn) throws Exception {
        Params p = new Params("srv_fail_id", failId);
        return new BillAnticNote().select(p, conn);
    }

    public static BillAnticNote createNote(BillAnticNoteRequest req, SessionLogin sl, BillInstance inst, Connection conn) throws SQLException, Exception {

        BillAnticNote n = req.note;
        Integer serial = new MySQLQuery("SELECT MAX(serial) FROM bill_antic_note").getAsInteger(conn);
        if (serial == null) {
            serial = 0;
        }

        BillSpan cons = BillSpan.getByClient("cons", n.clientTankId, inst, conn);
        if (cons.id != n.billSpanId) {
            throw new Exception("No se puede crear en este periodo");
        }

        n.billSpanId = cons.id;
        n.creUsuId = sl.employeeId;
        n.whenNotes = new Date();
        n.active = true;
        n.serial = serial + 1;
        n.insert(conn);

        BillTransaction t = new BillTransaction();

        t.accountCredId = (n.bankId != null ? Accounts.BANCOS : Accounts.C_ANTICIP);//bancos o pago anticipado 
        t.billBankId = n.bankId;
        t.created = n.whenNotes;
        t.accountDebId = Accounts.E_ING_OP;
        t.transTypeId = Transactions.N_ANTICIP;
        t.docType = "pag_antic";
        t.billSpanId = cons.id;
        t.cliTankId = n.clientTankId;
        t.creUsuId = sl.employeeId;
        t.modUsuId = sl.employeeId;
        t.docId = n.id;
        t.value = req.value;
        t.insert(conn);

        SysCrudLog l = new SysCrudLog();
        l.billInstId = inst.id;
        l.dt = new Date();
        l.employeeId = sl.employeeId;
        l.sessionId = sl.id;
        l.ownerSerial = n.id;
        l.table = Diff.getTableName(n);
        l.type = "crea";
        new MySQLQuery("USE sigma").executeUpdate(conn);
        l.insert(conn);
        return n;
    }

    public static BillAnticNote getByRebill(int rebillId, Connection conn) throws Exception {
        Params p = new Params("rebillId", rebillId);
        return new BillAnticNote().select(p, conn);
    }

    public static void cancel(BaseAPI caller, BillAnticNote n, boolean fromRebill, Connection conn) throws Exception {
        BillInstance inst = caller.getBillInstance();
        if (n.rebillId != null && !fromRebill) {
            throw new Exception("La nota está vinculada a una refacturación.\nDebe anular la refacturación");
        }

        if (!n.active) {
            throw new Exception("La nota ya fue anulada.");
        }

        BillSpan reca = BillSpan.getByClient("reca", n.clientTankId, inst, conn);
        if (n.billSpanId != reca.id + 1) {
            throw new Exception("La nota no se puede anular porque pertenece a otro periodo.");
        }

        BigDecimal origValue = new MySQLQuery("SELECT value FROM bill_transaction t WHERE t.doc_id = " + n.id + " AND t.doc_type = 'pag_antic'").getAsBigDecimal(conn, true);
        n.descNotes = n.descNotes + "\nValor Original de la Nota Anulada: " + new DecimalFormat("#,##0.00").format(origValue);
        n.active = false;
        n.update(conn);
        new MySQLQuery("DELETE FROM bill_transaction WHERE doc_id = " + n.id + " AND doc_type = 'pag_antic'").executeUpdate(conn);
        inst.useDefault(conn);
        SysCrudLog.updated(caller, n, "Se anuló la nota", conn);
    }
}
