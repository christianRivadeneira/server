package api.trk.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class TrkMto extends BaseModel<TrkMto> {
//inicio zona de reemplazo

    public Date date;
    public Integer trkCylId;
    public Integer mtoType;
    public Integer cylTripId;
    public String mtoCert;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "date",
            "trk_cyl_id",
            "mto_type",
            "cyl_trip_id",
            "mto_cert"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, date);
        q.setParam(2, trkCylId);
        q.setParam(3, mtoType);
        q.setParam(4, cylTripId);
        q.setParam(5, mtoCert);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        date = MySQLQuery.getAsDate(row[0]);
        trkCylId = MySQLQuery.getAsInteger(row[1]);
        mtoType = MySQLQuery.getAsInteger(row[2]);
        cylTripId = MySQLQuery.getAsInteger(row[3]);
        mtoCert = MySQLQuery.getAsString(row[4]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "trk_mto";
    }

    public static String getSelFlds(String alias) {
        return new TrkMto().getSelFldsForAlias(alias);
    }

    public static List<TrkMto> getList(MySQLQuery q, Connection conn) throws Exception {
        return new TrkMto().getListFromQuery(q, conn);
    }

//fin zona de reemplazo
    
}