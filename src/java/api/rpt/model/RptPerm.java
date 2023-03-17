package api.rpt.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class RptPerm extends BaseModel<RptPerm> {
//inicio zona de reemplazo

    public Integer rptId;
    public Integer dashId;
    public int empId;
    public String type;
    public boolean canShare;
    public Integer views;
    public Date lastView;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "rpt_id",
            "dash_id",
            "emp_id",
            "type",
            "can_share",
            "views",
            "last_view",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, rptId);
        q.setParam(2, dashId);
        q.setParam(3, empId);
        q.setParam(4, type);
        q.setParam(5, canShare);
        q.setParam(6, views);
        q.setParam(7, lastView);
        q.setParam(8, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        rptId = MySQLQuery.getAsInteger(row[0]);
        dashId = MySQLQuery.getAsInteger(row[1]);
        empId = MySQLQuery.getAsInteger(row[2]);
        type = MySQLQuery.getAsString(row[3]);
        canShare = MySQLQuery.getAsBoolean(row[4]);
        views = MySQLQuery.getAsInteger(row[5]);
        lastView = MySQLQuery.getAsDate(row[6]);
        active = MySQLQuery.getAsBoolean(row[7]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "rpt_perm";
    }

    public static String getSelFlds(String alias) {
        return new RptPerm().getSelFldsForAlias(alias);
    }

    public static List<RptPerm> getList(MySQLQuery q, Connection conn) throws Exception {
        return new RptPerm().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new RptPerm().deleteById(id, conn);
    }

    public static List<RptPerm> getAll(Connection conn) throws Exception {
        return new RptPerm().getAllList(conn);
    }

//fin zona de reemplazo
    
    public static RptPerm getByRpt(int rptId, int empId, Connection conn) throws Exception {
        return new RptPerm().select(new MySQLQuery("SELECT " + getSelFlds("") + " FROM rpt_perm WHERE rpt_id = ?1 AND emp_id = ?2").setParam(1, rptId).setParam(2, empId), conn);
    }

    public static RptPerm getByDash(int dashId, int empId, Connection conn) throws Exception {
        return new RptPerm().select(new MySQLQuery("SELECT " + getSelFlds("") + " FROM rpt_perm WHERE rpt_id = ?1 AND emp_id = ?2").setParam(1, dashId).setParam(2, empId), conn);
    }
}
