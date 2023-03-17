package api.trk.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class TrkTreatItem extends BaseModel<TrkTreatItem> {
//inicio zona de reemplazo

    public String name;
    public String notes;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "notes",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, notes);
        q.setParam(3, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        notes = MySQLQuery.getAsString(row[1]);
        active = MySQLQuery.getAsBoolean(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "trk_treat_item";
    }

    public static String getSelFlds(String alias) {
        return new TrkTreatItem().getSelFldsForAlias(alias);
    }

    public static List<TrkTreatItem> getList(MySQLQuery q, Connection conn) throws Exception {
        return new TrkTreatItem().getListFromQuery(q, conn);
    }

//fin zona de reemplazo
}
