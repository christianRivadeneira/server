package api.mto.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class MtoTaskProg extends BaseModel<MtoTaskProg> {
//inicio zona de reemplazo

    public Integer maintTaskId;
    public Date dt;
    public String notes;
    public int empId;
    public BigDecimal kms;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "maint_task_id",
            "dt",
            "notes",
            "emp_id",
            "kms"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, maintTaskId);
        q.setParam(2, dt);
        q.setParam(3, notes);
        q.setParam(4, empId);
        q.setParam(5, kms);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        maintTaskId = MySQLQuery.getAsInteger(row[0]);
        dt = MySQLQuery.getAsDate(row[1]);
        notes = MySQLQuery.getAsString(row[2]);
        empId = MySQLQuery.getAsInteger(row[3]);
        kms = MySQLQuery.getAsBigDecimal(row[4], false);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mto_task_prog";
    }

    public static String getSelFlds(String alias) {
        return new MtoTaskProg().getSelFldsForAlias(alias);
    }

    public static List<MtoTaskProg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MtoTaskProg().getListFromQuery(q, conn);
    }

    public static List<MtoTaskProg> getList(Params p, Connection conn) throws Exception {
        return new MtoTaskProg().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MtoTaskProg().deleteById(id, conn);
    }

    public static List<MtoTaskProg> getAll(Connection conn) throws Exception {
        return new MtoTaskProg().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<MtoTaskProg> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}
