package api.per.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class PerSurcharge extends BaseModel<PerSurcharge> {
//inicio zona de reemplazo

    public int empId;
    public int posId;
    public int employeerId;
    public Date payMonth;
    public String evType;
    public int regById;
    public Date regDate;
    public String notes;
    public int approvedTime;
    public boolean active;
    public Date begTime;
    public Date endTime;
    public String regType;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "emp_id",
            "pos_id",
            "employeer_id",
            "pay_month",
            "ev_type",
            "reg_by_id",
            "reg_date",
            "notes",
            "approved_time",
            "active",
            "beg_time",
            "end_time",
            "reg_type"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, empId);
        q.setParam(2, posId);
        q.setParam(3, employeerId);
        q.setParam(4, payMonth);
        q.setParam(5, evType);
        q.setParam(6, regById);
        q.setParam(7, regDate);
        q.setParam(8, notes);
        q.setParam(9, approvedTime);
        q.setParam(10, active);
        q.setParam(11, begTime);
        q.setParam(12, endTime);
        q.setParam(13, regType);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        empId = MySQLQuery.getAsInteger(row[0]);
        posId = MySQLQuery.getAsInteger(row[1]);
        employeerId = MySQLQuery.getAsInteger(row[2]);
        payMonth = MySQLQuery.getAsDate(row[3]);
        evType = MySQLQuery.getAsString(row[4]);
        regById = MySQLQuery.getAsInteger(row[5]);
        regDate = MySQLQuery.getAsDate(row[6]);
        notes = MySQLQuery.getAsString(row[7]);
        approvedTime = MySQLQuery.getAsInteger(row[8]);
        active = MySQLQuery.getAsBoolean(row[9]);
        begTime = MySQLQuery.getAsDate(row[10]);
        endTime = MySQLQuery.getAsDate(row[11]);
        regType = MySQLQuery.getAsString(row[12]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_surcharge";
    }

    public static String getSelFlds(String alias) {
        return new PerSurcharge().getSelFldsForAlias(alias);
    }

    public static List<PerSurcharge> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerSurcharge().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new PerSurcharge().deleteById(id, conn);
    }

    public static List<PerSurcharge> getAll(Connection conn) throws Exception {
        return new PerSurcharge().getAllList(conn);
    }

//fin zona de reemplazo
    
    public String getEnumOptions(String fieldName) {
        if (fieldName.equals("ev_type")) {
            return "NocOrd=Nocturna Ordinaria (RNOC)&DiuDom=Diurna Dominical (HDF)&NocDom=Nocturna Dominical (RNFES)";
        }
        return null;
    }
}