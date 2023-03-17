package api.trk.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class TrkIllegibleTrip extends BaseModel<TrkIllegibleTrip> {
  
//inicio zona de reemplazo

    public int gtTripId;
    public Date reportDt;
    public int platEmpId;
    public int amSmanRep;
    public int amPlatfRep;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "gt_trip_id",
            "report_dt",
            "plat_emp_id",
            "am_sman_rep",
            "am_platf_rep"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, gtTripId);
        q.setParam(2, reportDt);
        q.setParam(3, platEmpId);
        q.setParam(4, amSmanRep);
        q.setParam(5, amPlatfRep);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        gtTripId = MySQLQuery.getAsInteger(row[0]);
        reportDt = MySQLQuery.getAsDate(row[1]);
        platEmpId = MySQLQuery.getAsInteger(row[2]);
        amSmanRep = MySQLQuery.getAsInteger(row[3]);
        amPlatfRep = MySQLQuery.getAsInteger(row[4]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "trk_illegible_trip";
    }

    public static String getSelFlds(String alias) {
        return new TrkIllegibleTrip().getSelFldsForAlias(alias);
    }

    public static List<TrkIllegibleTrip> getList(MySQLQuery q, Connection conn) throws Exception {
        return new TrkIllegibleTrip().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new TrkIllegibleTrip().deleteById(id, conn);
    }

    public static List<TrkIllegibleTrip> getAll(Connection conn) throws Exception {
        return new TrkIllegibleTrip().getAllList(conn);
    }

//fin zona de reemplazo
}
