package api.per.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class PerExtraRequest extends BaseModel<PerExtraRequest> {
//inicio zona de reemplazo

    public int sysReqId;
    public int perEmpId;
    public Integer approvById;
    public Integer reviewById;
    public Integer causeId;
    public Date begDate;
    public Date endDate;
    public String motive;
    public String detail;
    public String type;
    public String paymentType;
    public String evType;
    public Integer approvedTime;
    public boolean checked;
    public Integer perLicenseId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "sys_req_id",
            "per_emp_id",
            "approv_by_id",
            "review_by_id",
            "cause_id",
            "beg_date",
            "end_date",
            "motive",
            "detail",
            "type",
            "payment_type",
            "ev_type",
            "approved_time",
            "checked",
            "per_license_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, sysReqId);
        q.setParam(2, perEmpId);
        q.setParam(3, approvById);
        q.setParam(4, reviewById);
        q.setParam(5, causeId);
        q.setParam(6, begDate);
        q.setParam(7, endDate);
        q.setParam(8, motive);
        q.setParam(9, detail);
        q.setParam(10, type);
        q.setParam(11, paymentType);
        q.setParam(12, evType);
        q.setParam(13, approvedTime);
        q.setParam(14, checked);
        q.setParam(15, perLicenseId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        sysReqId = MySQLQuery.getAsInteger(row[0]);
        perEmpId = MySQLQuery.getAsInteger(row[1]);
        approvById = MySQLQuery.getAsInteger(row[2]);
        reviewById = MySQLQuery.getAsInteger(row[3]);
        causeId = MySQLQuery.getAsInteger(row[4]);
        begDate = MySQLQuery.getAsDate(row[5]);
        endDate = MySQLQuery.getAsDate(row[6]);
        motive = MySQLQuery.getAsString(row[7]);
        detail = MySQLQuery.getAsString(row[8]);
        type = MySQLQuery.getAsString(row[9]);
        paymentType = MySQLQuery.getAsString(row[10]);
        evType = MySQLQuery.getAsString(row[11]);
        approvedTime = MySQLQuery.getAsInteger(row[12]);
        checked = MySQLQuery.getAsBoolean(row[13]);
        perLicenseId = MySQLQuery.getAsInteger(row[14]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_extra_request";
    }

    public static String getSelFlds(String alias) {
        return new PerExtraRequest().getSelFldsForAlias(alias);
    }

    public static List<PerExtraRequest> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerExtraRequest().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new PerExtraRequest().deleteById(id, conn);
    }

    public static List<PerExtraRequest> getAll(Connection conn) throws Exception {
        return new PerExtraRequest().getAllList(conn);
    }

//fin zona de reemplazo
    
}
