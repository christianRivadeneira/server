package api.trk.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class TrkCylNovelty extends BaseModel<TrkCylNovelty> {

    //Fuera de la zona de reemplazo-----
    public List<TrkCylNoveltyItem> items;
    public String pqrNum;
    //---------------------------------------------------------

    //inicio zona de reemplazo
    public Date dt;
    public int cylId;
    public Integer pqrId;
    public String notes;
    public Integer tripId;
    public int empId;
    public String revNotes;
    public Date revDt;
    public Integer revEmpId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "dt",
            "cyl_id",
            "pqr_id",
            "notes",
            "trip_id",
            "emp_id", 
            "rev_notes", 
            "rev_dt", 
            "rev_emp_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, dt);
        q.setParam(2, cylId);
        q.setParam(3, pqrId);
        q.setParam(4, notes);
        q.setParam(5, tripId);
        q.setParam(6, empId);
        q.setParam(7, revNotes);
        q.setParam(8, revDt);
        q.setParam(9, revEmpId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        dt = MySQLQuery.getAsDate(row[0]);
        cylId = MySQLQuery.getAsInteger(row[1]);
        pqrId = MySQLQuery.getAsInteger(row[2]);
        notes = MySQLQuery.getAsString(row[3]);
        tripId = MySQLQuery.getAsInteger(row[4]);
        empId = MySQLQuery.getAsInteger(row[5]);
        revNotes =MySQLQuery.getAsString(row[6]);
        revDt = MySQLQuery.getAsDate(row[7]);
        revEmpId = MySQLQuery.getAsInteger(row[8]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "trk_cyl_novelty";
    }

    public static String getSelFlds(String alias) {
        return new TrkCylNovelty().getSelFldsForAlias(alias);
    }

    public static List<TrkCylNovelty> getList(MySQLQuery q, Connection conn) throws Exception {
        return new TrkCylNovelty().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new TrkCylNovelty().deleteById(id, conn);
    }

    public static List<TrkCylNovelty> getAll(Connection conn) throws Exception {
        return new TrkCylNovelty().getAllList(conn);
    }

//fin zona de reemplazo
}
