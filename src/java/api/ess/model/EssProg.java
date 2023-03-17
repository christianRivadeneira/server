package api.ess.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class EssProg extends BaseModel<EssProg> {
//inicio zona de reemplazo

    public Date regDt;
    public int empId;
    public Integer personId;
    public Integer unitId;
    public Integer buildId;
    public Date progDt;
    public Date begTime;
    public Date endTime;
    public String notes;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "reg_dt",
            "emp_id",
            "person_id",
            "unit_id",
            "build_id",
            "prog_dt",
            "beg_time",
            "end_time",
            "notes",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, regDt);
        q.setParam(2, empId);
        q.setParam(3, personId);
        q.setParam(4, unitId);
        q.setParam(5, buildId);
        q.setParam(6, progDt);
        q.setParam(7, begTime);
        q.setParam(8, endTime);
        q.setParam(9, notes);
        q.setParam(10, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        regDt = MySQLQuery.getAsDate(row[0]);
        empId = MySQLQuery.getAsInteger(row[1]);
        personId = MySQLQuery.getAsInteger(row[2]);
        unitId = MySQLQuery.getAsInteger(row[3]);
        buildId = MySQLQuery.getAsInteger(row[4]);
        progDt = MySQLQuery.getAsDate(row[5]);
        begTime = MySQLQuery.getAsDate(row[6]);
        endTime = MySQLQuery.getAsDate(row[7]);
        notes = MySQLQuery.getAsString(row[8]);
        active = MySQLQuery.getAsBoolean(row[9]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ess_prog";
    }

    public static String getSelFlds(String alias) {
        return new EssProg().getSelFldsForAlias(alias);
    }

    public static List<EssProg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EssProg().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EssProg().deleteById(id, conn);
    }

    public static List<EssProg> getAll(Connection conn) throws Exception {
        return new EssProg().getAllList(conn);
    }

//fin zona de reemplazo
}
