package api.per.model;

import api.BaseModel;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class PerSickLeave extends BaseModel<PerSickLeave> {
//inicio zona de reemplazo

    public int empId;
    public Date regDate;
    public Date endDate;
    public int causeId;
    public BigDecimal days;
    public BigDecimal extDays;
    public String notes;
    public boolean active;
    public Integer perCie10Id;
    public Integer entityId;
    public String cie10Description;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "emp_id",
            "reg_date",
            "end_date",
            "cause_id",
            "days",
            "ext_days",
            "notes",
            "active",
            "per_cie10_id",
            "entity_id",
            "cie10_description"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, empId);
        q.setParam(2, regDate);
        q.setParam(3, endDate);
        q.setParam(4, causeId);
        q.setParam(5, days);
        q.setParam(6, extDays);
        q.setParam(7, notes);
        q.setParam(8, active);
        q.setParam(9, perCie10Id);
        q.setParam(10, entityId);
        q.setParam(11, cie10Description);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        empId = MySQLQuery.getAsInteger(row[0]);
        regDate = MySQLQuery.getAsDate(row[1]);
        endDate = MySQLQuery.getAsDate(row[2]);
        causeId = MySQLQuery.getAsInteger(row[3]);
        days = MySQLQuery.getAsBigDecimal(row[4], false);
        extDays = MySQLQuery.getAsBigDecimal(row[5], false);
        notes = MySQLQuery.getAsString(row[6]);
        active = MySQLQuery.getAsBoolean(row[7]);
        perCie10Id = MySQLQuery.getAsInteger(row[8]);
        entityId = MySQLQuery.getAsInteger(row[9]);
        cie10Description = MySQLQuery.getAsString(row[10]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_sick_leave";
    }

    public static String getSelFlds(String alias) {
        return new PerSickLeave().getSelFldsForAlias(alias);
    }

    public static List<PerSickLeave> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerSickLeave().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new PerSickLeave().deleteById(id, conn);
    }

    public static List<PerSickLeave> getAll(Connection conn) throws Exception {
        return new PerSickLeave().getAllList(conn);
    }

//fin zona de reemplazo
  
}
