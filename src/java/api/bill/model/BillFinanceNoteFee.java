package api.bill.model;

import api.BaseModel;
import static api.bill.model.BillUserServiceFee.getList;
import static api.bill.model.BillUserServiceFee.getSelFlds;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class BillFinanceNoteFee extends BaseModel<BillFinanceNoteFee> {
//inicio zona de reemplazo

    public int noteId;
    public BigDecimal capital;
    public BigDecimal interest;
    public Integer place;
    public boolean caused;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "note_id",
            "capital",
            "interest",
            "place",
            "caused"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, noteId);
        q.setParam(2, capital);
        q.setParam(3, interest);
        q.setParam(4, place);
        q.setParam(5, caused);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        noteId = MySQLQuery.getAsInteger(row[0]);
        capital = MySQLQuery.getAsBigDecimal(row[1], false);
        interest = MySQLQuery.getAsBigDecimal(row[2], false);
        place = MySQLQuery.getAsInteger(row[3]);
        caused = MySQLQuery.getAsBoolean(row[4]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_finance_note_fee";
    }

    public static String getSelFlds(String alias) {
        return new BillFinanceNoteFee().getSelFldsForAlias(alias);
    }

    public static List<BillFinanceNoteFee> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillFinanceNoteFee().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillFinanceNoteFee().deleteById(id, conn);
    }

    public static List<BillFinanceNoteFee> getAll(Connection conn) throws Exception {
        return new BillFinanceNoteFee().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<BillFinanceNoteFee> getByNote(int noteId, Connection conn) throws Exception {
        return getList(new MySQLQuery("SELECT " + getSelFlds("") + " FROM bill_finance_note_fee WHERE note_id = ?1 ORDER BY place").setParam(1, noteId), conn);
    }

}
