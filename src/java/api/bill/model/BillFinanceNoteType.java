package api.bill.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class BillFinanceNoteType extends BaseModel<BillFinanceNoteType> {
//inicio zona de reemplazo

    public String name;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        active = MySQLQuery.getAsBoolean(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_finance_note_type";
    }

    public static String getSelFlds(String alias) {
        return new BillFinanceNoteType().getSelFldsForAlias(alias);
    }

    public static List<BillFinanceNoteType> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillFinanceNoteType().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillFinanceNoteType().deleteById(id, conn);
    }

    public static List<BillFinanceNoteType> getAll(Connection conn) throws Exception {
        return new BillFinanceNoteType().getAllList(conn);
    }

//fin zona de reemplazo
}