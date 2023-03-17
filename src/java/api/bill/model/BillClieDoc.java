package api.bill.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class BillClieDoc extends BaseModel<BillClieDoc> {
//inicio zona de reemplazo

    public int instId;
    public int clientId;
    public int typeId;
    public String notes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "inst_id",
            "client_id",
            "type_id",
            "notes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, instId);
        q.setParam(2, clientId);
        q.setParam(3, typeId);
        q.setParam(4, notes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        instId = MySQLQuery.getAsInteger(row[0]);
        clientId = MySQLQuery.getAsInteger(row[1]);
        typeId = MySQLQuery.getAsInteger(row[2]);
        notes = MySQLQuery.getAsString(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_clie_doc";
    }

    public static String getSelFlds(String alias) {
        return new BillClieDoc().getSelFldsForAlias(alias);
    }

    public static List<BillClieDoc> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillClieDoc().getListFromQuery(q, conn);
    }

    public static List<BillClieDoc> getList(Params p, Connection conn) throws Exception {
        return new BillClieDoc().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillClieDoc().deleteById(id, conn);
    }

    public static List<BillClieDoc> getAll(Connection conn) throws Exception {
        return new BillClieDoc().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<BillClieDoc> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}
