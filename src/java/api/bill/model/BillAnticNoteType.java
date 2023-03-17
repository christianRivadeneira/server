package api.bill.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class BillAnticNoteType extends BaseModel<BillAnticNoteType> {
//inicio zona de reemplazo

    public String name;
    public String type;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "type",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, type);
        q.setParam(3, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        type = MySQLQuery.getAsString(row[1]);
        active = MySQLQuery.getAsBoolean(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_antic_note_type";
    }

    public static String getSelFlds(String alias) {
        return new BillAnticNoteType().getSelFldsForAlias(alias);
    }

    public static List<BillAnticNoteType> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillAnticNoteType().getListFromQuery(q, conn);
    }

    public static List<BillAnticNoteType> getList(Params p, Connection conn) throws Exception {
        return new BillAnticNoteType().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillAnticNoteType().deleteById(id, conn);
    }

    public static List<BillAnticNoteType> getAll(Connection conn) throws Exception {
        return new BillAnticNoteType().getAllList(conn);
    }

//fin zona de reemplazo
    public static BillAnticNoteType getCovidType(Connection conn) throws Exception {
        return new BillAnticNoteType().select(new MySQLQuery("SELECT " + getSelFlds("") + " FROM bill_antic_note_type WHERE type = 'covid' LIMIT 1"), conn);
    }
    
    public static BillAnticNoteType getSrvFailype(Connection conn) throws Exception {
        return new BillAnticNoteType().select(new MySQLQuery("SELECT " + getSelFlds("") + " FROM bill_antic_note_type WHERE type = 'srv_fail' LIMIT 1"), conn);
    }

}
