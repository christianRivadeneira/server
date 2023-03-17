package api.bill.model;

import api.BaseAPI;
import api.BaseModel;
import api.Params;
import api.sys.model.SysCrudLog;
import controller.billing.BillTransactionController;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import model.billing.constants.Accounts;
import utilities.MySQLQuery;

public class BillNote extends BaseModel<BillNote> {
//inicio zona de reemplazo

    public String descNotes;
    public String typeNotes;
    public Date whenNotes;
    public int clientTankId;
    public int billSpanId;
    public int serial;
    public int cauNoteId;
    public Integer rebillId;
    public Integer bank;
    public int lastTransId;
    public boolean active;
    public String createdForSpan;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "desc_notes",
            "type_notes",
            "when_notes",
            "client_tank_id",
            "bill_span_id",
            "serial",
            "cau_note_id",
            "rebill_id",
            "bank",
            "last_trans_id",
            "active",
            "created_for_span"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, descNotes);
        q.setParam(2, typeNotes);
        q.setParam(3, whenNotes);
        q.setParam(4, clientTankId);
        q.setParam(5, billSpanId);
        q.setParam(6, serial);
        q.setParam(7, cauNoteId);
        q.setParam(8, rebillId);
        q.setParam(9, bank);
        q.setParam(10, lastTransId);
        q.setParam(11, active);
        q.setParam(12, createdForSpan);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        descNotes = MySQLQuery.getAsString(row[0]);
        typeNotes = MySQLQuery.getAsString(row[1]);
        whenNotes = MySQLQuery.getAsDate(row[2]);
        clientTankId = MySQLQuery.getAsInteger(row[3]);
        billSpanId = MySQLQuery.getAsInteger(row[4]);
        serial = MySQLQuery.getAsInteger(row[5]);
        cauNoteId = MySQLQuery.getAsInteger(row[6]);
        rebillId = MySQLQuery.getAsInteger(row[7]);
        bank = MySQLQuery.getAsInteger(row[8]);
        lastTransId = MySQLQuery.getAsInteger(row[9]);
        active = MySQLQuery.getAsBoolean(row[10]);
        createdForSpan = MySQLQuery.getAsString(row[11]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_note";
    }

    public static String getSelFlds(String alias) {
        return new BillNote().getSelFldsForAlias(alias);
    }

    public static List<BillNote> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillNote().getListFromQuery(q, conn);
    }

    public static List<BillNote> getList(Params p, Connection conn) throws Exception {
        return new BillNote().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillNote().deleteById(id, conn);
    }

    public static List<BillNote> getAll(Connection conn) throws Exception {
        return new BillNote().getAllList(conn);
    }

//fin zona de reemplazo
    public static BillNote getByRebill(int rebillId, Connection conn) throws Exception {
        Params p = new Params("rebillId", rebillId);
        return new BillNote().select(p, conn);
    }

    public static void cancel(BaseAPI caller, BillNote note, boolean fromRebill, Connection conn) throws Exception {
        BillInstance inst = caller.getBillInstance();
        if (note.rebillId != null && !fromRebill) {
            throw new Exception("La nota está vinculada a una refacturación.\nDebe anular la refacturación");
        }

        if (!note.active) {
            throw new Exception("La nota ya ha sido anulada.");
        }

        BillSpan span = BillSpan.getByClient(note.createdForSpan, note.clientTankId, inst, conn);

        if (span.id != note.billSpanId) {
            throw new Exception("La nota pertenece a otro periodo.");
        }
        int lastTransId = BillTransactionController.getLastTrasactionIdByClient(note.clientTankId, conn);
        if (lastTransId != note.lastTransId) {
            throw new Exception("La cuenta del cliente ha sido afectada por otros documentos.");
        }

        Map<Integer, String> cts = Accounts.accNames;
        DecimalFormat df = new DecimalFormat("#,##0.00");
        StringBuilder sb = new StringBuilder();
        sb.append("Contenido de la nota anulada: ");
        List<BillTransaction> trans = BillTransaction.getByDoc(note.id, "not", conn);
        for (BillTransaction tran : trans) {
            switch (note.typeNotes) {
                case "n_cred":
                case "aj_cred":
                    sb.append(cts.get(tran.accountCredId)).append(" ").append(df.format(tran.value)).append(" ");
                    break;
                case "n_deb":
                    sb.append(cts.get(tran.accountDebId)).append(" ").append(df.format(tran.value)).append(" ");
                    break;
                default:
                    throw new Exception("Tipo de nota no soportado." + note.typeNotes);
            }
        }
        note.descNotes = note.descNotes + "\n" + sb.toString();
        note.active = false;
        note.update(conn);
        new MySQLQuery("DELETE FROM bill_transaction WHERE doc_id = " + note.id + " AND doc_type = 'not'").executeUpdate(conn);
        BillBill.anullActiveBills(note.clientTankId, span.id, conn);
        caller.useDefault(conn);
        SysCrudLog.updated(caller, note, "Se anuló la nota", conn);
    }

}
