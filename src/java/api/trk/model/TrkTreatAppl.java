package api.trk.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class TrkTreatAppl extends BaseModel<TrkTreatAppl> {
//inicio zona de reemplazo

    public int treatId;
    public int itemId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "treat_id",
            "item_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, treatId);
        q.setParam(2, itemId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        treatId = MySQLQuery.getAsInteger(row[0]);
        itemId = MySQLQuery.getAsInteger(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "trk_treat_appl";
    }

    public static String getSelFlds(String alias) {
        return new TrkTreatAppl().getSelFldsForAlias(alias);
    }

    public static List<TrkTreatAppl> getList(MySQLQuery q, Connection conn) throws Exception {
        return new TrkTreatAppl().getListFromQuery(q, conn);
    }

//fin zona de reemplazo
    
}