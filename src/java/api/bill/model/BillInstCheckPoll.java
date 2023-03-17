package api.bill.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class BillInstCheckPoll extends BaseModel<BillInstCheckPoll> {
//inicio zona de reemplazo

    public int clientId;
    public Date limitDate;
    public String reason;
    public String notes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "client_id",
            "limit_date",
            "reason",
            "notes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, clientId);
        q.setParam(2, limitDate);
        q.setParam(3, reason);
        q.setParam(4, notes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        clientId = MySQLQuery.getAsInteger(row[0]);
        limitDate = MySQLQuery.getAsDate(row[1]);
        reason = MySQLQuery.getAsString(row[2]);
        notes = MySQLQuery.getAsString(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_inst_check_poll";
    }

    public static String getSelFlds(String alias) {
        return new BillInstCheckPoll().getSelFldsForAlias(alias);
    }

    public static List<BillInstCheckPoll> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillInstCheckPoll().getListFromQuery(q, conn);
    }

    public static List<BillInstCheckPoll> getList(Params p, Connection conn) throws Exception {
        return new BillInstCheckPoll().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillInstCheckPoll().deleteById(id, conn);
    }

    public static List<BillInstCheckPoll> getAll(Connection conn) throws Exception {
        return new BillInstCheckPoll().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<BillInstCheckPoll> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}
