package api.trk.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class TrkCheck extends BaseModel<TrkCheck> {
//inicio zona de reemplazo

    public String notes;
    public Date dt;
    public boolean ok;
    public Integer trkCylId;
    public Integer invCenterId;
    public Integer empId;
    public Integer checkVersionId;
    public String type;
    public Integer procAreaId;
    public Integer tankId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "notes",
            "dt",
            "ok",
            "trk_cyl_id",
            "inv_center_id",
            "emp_id",
            "check_version_id",
            "type", 
            "proc_area_id",
            "tank_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, notes);
        q.setParam(2, dt);
        q.setParam(3, ok);
        q.setParam(4, trkCylId);
        q.setParam(5, invCenterId);
        q.setParam(6, empId);
        q.setParam(7, checkVersionId);
        q.setParam(8, type);
        q.setParam(9, procAreaId);
        q.setParam(10, tankId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        notes = MySQLQuery.getAsString(row[0]);
        dt = MySQLQuery.getAsDate(row[1]);
        ok = MySQLQuery.getAsBoolean(row[2]);
        trkCylId = MySQLQuery.getAsInteger(row[3]);
        invCenterId = MySQLQuery.getAsInteger(row[4]);
        empId = MySQLQuery.getAsInteger(row[5]);
        checkVersionId = MySQLQuery.getAsInteger(row[6]);
        type = MySQLQuery.getAsString(row[7]);
        procAreaId = MySQLQuery.getAsInteger(row[8]);
        tankId = MySQLQuery.getAsInteger(row[9]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "trk_check";
    }

    public static String getSelFlds(String alias) {
        return new TrkCheck().getSelFldsForAlias(alias);
    }

    public static List<TrkCheck> getList(MySQLQuery q, Connection conn) throws Exception {
        return new TrkCheck().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new TrkCheck().deleteById(id, conn);
    }

    public static List<TrkCheck> getAll(Connection conn) throws Exception {
        return new TrkCheck().getAllList(conn);
    }

//fin zona de reemplazo
  }
