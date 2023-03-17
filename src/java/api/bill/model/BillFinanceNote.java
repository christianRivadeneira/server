package api.bill.model;

import api.BaseModel;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class BillFinanceNote extends BaseModel<BillFinanceNote> {
//inicio zona de reemplazo

    public int clientId;
    public int consSpanId;
    public int delayPaymentSpans;
    public int payments;
    public int typeId;
    public BigDecimal interestRate;
    public String description;
    public boolean caused;
    public boolean canceled;
    public int lastTransId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "client_id",
            "cons_span_id",
            "delay_payment_spans",
            "payments",
            "type_id",
            "interest_rate",
            "description",
            "caused",
            "canceled",
            "last_trans_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, clientId);
        q.setParam(2, consSpanId);
        q.setParam(3, delayPaymentSpans);
        q.setParam(4, payments);
        q.setParam(5, typeId);
        q.setParam(6, interestRate);
        q.setParam(7, description);
        q.setParam(8, caused);
        q.setParam(9, canceled);
        q.setParam(10, lastTransId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        clientId = MySQLQuery.getAsInteger(row[0]);
        consSpanId = MySQLQuery.getAsInteger(row[1]);
        delayPaymentSpans = MySQLQuery.getAsInteger(row[2]);
        payments = MySQLQuery.getAsInteger(row[3]);
        typeId = MySQLQuery.getAsInteger(row[4]);
        interestRate = MySQLQuery.getAsBigDecimal(row[5], false);
        description = MySQLQuery.getAsString(row[6]);
        caused = MySQLQuery.getAsBoolean(row[7]);
        canceled = MySQLQuery.getAsBoolean(row[8]);
        lastTransId = MySQLQuery.getAsInteger(row[9]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_finance_note";
    }

    public static String getSelFlds(String alias) {
        return new BillFinanceNote().getSelFldsForAlias(alias);
    }

    public static List<BillFinanceNote> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillFinanceNote().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillFinanceNote().deleteById(id, conn);
    }

    public static List<BillFinanceNote> getAll(Connection conn) throws Exception {
        return new BillFinanceNote().getAllList(conn);
    }

//fin zona de reemplazo
}
