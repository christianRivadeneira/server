package api.trk.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class TrkCylNov extends BaseModel<TrkCylNov> {
//inicio zona de reemplazo

    public int cylId;
    public Date dt;
    public int empId;
    public String notes;
    public String type;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "cyl_id",
            "dt",
            "emp_id",
            "notes",
            "type"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, cylId);
        q.setParam(2, dt);
        q.setParam(3, empId);
        q.setParam(4, notes);
        q.setParam(5, type);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        cylId = MySQLQuery.getAsInteger(row[0]);
        dt = MySQLQuery.getAsDate(row[1]);
        empId = MySQLQuery.getAsInteger(row[2]);
        notes = MySQLQuery.getAsString(row[3]);
        type = MySQLQuery.getAsString(row[4]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "trk_cyl_nov";
    }

    public static String getSelFlds(String alias) {
        return new TrkCylNov().getSelFldsForAlias(alias);
    }

    public static List<TrkCylNov> getList(MySQLQuery q, Connection conn) throws Exception {
        return new TrkCylNov().getListFromQuery(q, conn);
    }

//fin zona de reemplazo
}