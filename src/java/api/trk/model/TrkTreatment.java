package api.trk.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class TrkTreatment extends BaseModel<TrkTreatment> {
//inicio zona de reemplazo

    public int trkChkId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "trk_chk_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, trkChkId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        trkChkId = MySQLQuery.getAsInteger(row[0]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "trk_treatment";
    }

    public static String getSelFlds(String alias) {
        return new TrkTreatment().getSelFldsForAlias(alias);
    }

    public static List<TrkTreatment> getList(MySQLQuery q, Connection conn) throws Exception {
        return new TrkTreatment().getListFromQuery(q, conn);
    }

//fin zona de reemplazo
}
