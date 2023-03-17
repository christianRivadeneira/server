package api.bill.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class BillClientList extends BaseModel<BillClientList> {
//inicio zona de reemplazo

    public int clientId;
    public int spanId;
    public int listId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "client_id",
            "span_id",
            "list_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, clientId);
        q.setParam(2, spanId);
        q.setParam(3, listId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        clientId = MySQLQuery.getAsInteger(row[0]);
        spanId = MySQLQuery.getAsInteger(row[1]);
        listId = MySQLQuery.getAsInteger(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_client_list";
    }

    public static String getSelFlds(String alias) {
        return new BillClientList().getSelFldsForAlias(alias);
    }

    public static List<BillClientList> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillClientList().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillClientList().deleteById(id, conn);
    }

    public static List<BillClientList> getAll(Connection conn) throws Exception {
        return new BillClientList().getAllList(conn);
    }

//fin zona de reemplazo
}