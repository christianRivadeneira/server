package api.per.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class PerExtra extends BaseModel<PerExtra> {
//inicio zona de reemplazo

    public int empId;
    public int posId;
    public int employeerId;
    public Date payMonth;
    public Date evDate;
    public Date begTime;
    public Date endTime;
    public String evType;
    public int regById;
    public String regType;
    public Date regDate;
    public boolean checked;
    public Integer checkedById;
    public String notes;
    public String inputType;
    public Integer gateEventId;
    public Integer gateSpanId;
    public int approvedTime;
    public boolean active;
    public Integer extraReqId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "emp_id",
            "pos_id",
            "employeer_id",
            "pay_month",
            "ev_date",
            "beg_time",
            "end_time",
            "ev_type",
            "reg_by_id",
            "reg_type",
            "reg_date",
            "checked",
            "checked_by_id",
            "notes",
            "input_type",
            "gate_event_id",
            "gate_span_id",
            "approved_time",
            "active",
            "extra_req_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, empId);
        q.setParam(2, posId);
        q.setParam(3, employeerId);
        q.setParam(4, payMonth);
        q.setParam(5, evDate);
        q.setParam(6, begTime);
        q.setParam(7, endTime);
        q.setParam(8, evType);
        q.setParam(9, regById);
        q.setParam(10, regType);
        q.setParam(11, regDate);
        q.setParam(12, checked);
        q.setParam(13, checkedById);
        q.setParam(14, notes);
        q.setParam(15, inputType);
        q.setParam(16, gateEventId);
        q.setParam(17, gateSpanId);
        q.setParam(18, approvedTime);
        q.setParam(19, active);
        q.setParam(20, extraReqId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        empId = MySQLQuery.getAsInteger(row[0]);
        posId = MySQLQuery.getAsInteger(row[1]);
        employeerId = MySQLQuery.getAsInteger(row[2]);
        payMonth = MySQLQuery.getAsDate(row[3]);
        evDate = MySQLQuery.getAsDate(row[4]);
        begTime = MySQLQuery.getAsDate(row[5]);
        endTime = MySQLQuery.getAsDate(row[6]);
        evType = MySQLQuery.getAsString(row[7]);
        regById = MySQLQuery.getAsInteger(row[8]);
        regType = MySQLQuery.getAsString(row[9]);
        regDate = MySQLQuery.getAsDate(row[10]);
        checked = MySQLQuery.getAsBoolean(row[11]);
        checkedById = MySQLQuery.getAsInteger(row[12]);
        notes = MySQLQuery.getAsString(row[13]);
        inputType = MySQLQuery.getAsString(row[14]);
        gateEventId = MySQLQuery.getAsInteger(row[15]);
        gateSpanId = MySQLQuery.getAsInteger(row[16]);
        approvedTime = MySQLQuery.getAsInteger(row[17]);
        active = MySQLQuery.getAsBoolean(row[18]);
        extraReqId = MySQLQuery.getAsInteger(row[19]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_extra";
    }

    public static String getSelFlds(String alias) {
        return new PerExtra().getSelFldsForAlias(alias);
    }

    public static List<PerExtra> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerExtra().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new PerExtra().deleteById(id, conn);
    }

    public static List<PerExtra> getAll(Connection conn) throws Exception {
        return new PerExtra().getAllList(conn);
    }

//fin zona de reemplazo
    
    public String getEnumOptions(String fieldName) {
        if (fieldName.equals("ev_type")) {
            return "ExDiuSem=Diu. Ord.&ExNocSem=Noct. Ord.&ExDiuDom=Diu. Dom.&ExNocDom=Noct. Dom.";
        } else if (fieldName.equals("reg_type")) {
            return "prog=prog&bill=bill";
        } else if (fieldName.equals("input_type")) {
            return "man=Manual&gate=Portería";
        }
        return null;
    }
}
