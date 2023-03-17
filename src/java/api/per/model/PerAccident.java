package api.per.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class PerAccident extends BaseModel<PerAccident> {
//inicio zona de reemplazo

    public int empId;
    public Date regDate;
    public int causeId;
    public int days;
    public Integer extDays;
    public Integer loadedDays;
    public int entityId;
    public String notes;
    public Boolean investigate;
    public Boolean active;
    public int employeerId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "emp_id",
            "reg_date",
            "cause_id",
            "days",
            "ext_days",
            "loaded_days",
            "entity_id",
            "notes",
            "investigate",
            "active",
            "employeer_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, empId);
        q.setParam(2, regDate);
        q.setParam(3, causeId);
        q.setParam(4, days);
        q.setParam(5, extDays);
        q.setParam(6, loadedDays);
        q.setParam(7, entityId);
        q.setParam(8, notes);
        q.setParam(9, investigate);
        q.setParam(10, active);
        q.setParam(11, employeerId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        empId = MySQLQuery.getAsInteger(row[0]);
        regDate = MySQLQuery.getAsDate(row[1]);
        causeId = MySQLQuery.getAsInteger(row[2]);
        days = MySQLQuery.getAsInteger(row[3]);
        extDays = MySQLQuery.getAsInteger(row[4]);
        loadedDays = MySQLQuery.getAsInteger(row[5]);
        entityId = MySQLQuery.getAsInteger(row[6]);
        notes = MySQLQuery.getAsString(row[7]);
        investigate = MySQLQuery.getAsBoolean(row[8]);
        active = MySQLQuery.getAsBoolean(row[9]);
        employeerId = MySQLQuery.getAsInteger(row[10]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_accident";
    }

    public static String getSelFlds(String alias) {
        return new PerAccident().getSelFldsForAlias(alias);
    }

    public static List<PerAccident> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerAccident().getListFromQuery(q, conn);
    }

//fin zona de reemplazo

}