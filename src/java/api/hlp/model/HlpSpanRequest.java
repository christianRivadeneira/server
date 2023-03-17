package api.hlp.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class HlpSpanRequest extends BaseModel<HlpSpanRequest> {
//inicio zona de reemplazo

    public int caseId;
    public Integer reviewStateId;
    public Integer inchargeId;
    public Date regDate;
    public Date endDate;
    public boolean last;
    public Integer empInchargeId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "case_id",
            "review_state_id",
            "incharge_id",
            "reg_date",
            "end_date",
            "last",
            "emp_incharge_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, caseId);
        q.setParam(2, reviewStateId);
        q.setParam(3, inchargeId);
        q.setParam(4, regDate);
        q.setParam(5, endDate);
        q.setParam(6, last);
        q.setParam(7, empInchargeId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        caseId = MySQLQuery.getAsInteger(row[0]);
        reviewStateId = MySQLQuery.getAsInteger(row[1]);
        inchargeId = MySQLQuery.getAsInteger(row[2]);
        regDate = MySQLQuery.getAsDate(row[3]);
        endDate = MySQLQuery.getAsDate(row[4]);
        last = MySQLQuery.getAsBoolean(row[5]);
        empInchargeId = MySQLQuery.getAsInteger(row[6]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "hlp_span_request";
    }

    public static String getSelFlds(String alias) {
        return new HlpSpanRequest().getSelFldsForAlias(alias);
    }

    public static List<HlpSpanRequest> getList(MySQLQuery q, Connection conn) throws Exception {
        return new HlpSpanRequest().getListFromQuery(q, conn);
    }

    public static List<HlpSpanRequest> getList(Params p, Connection conn) throws Exception {
        return new HlpSpanRequest().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new HlpSpanRequest().deleteById(id, conn);
    }

    public static List<HlpSpanRequest> getAll(Connection conn) throws Exception {
        return new HlpSpanRequest().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<HlpSpanRequest> getOpenByBacklog(int backlogId, int empId, Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " + getSelFlds("s") + " FROM "
                + "hlp_span_request s "
                + "inner join hlp_request r ON s.case_id = r.id "
                + "WHERE r.prj_backlog_id = ?1 AND s.emp_incharge_id = ?2 AND s.end_date IS NULL;");
        q.setParam(1, backlogId);
        q.setParam(2, empId);
        return getList(q, conn);
    }
}
