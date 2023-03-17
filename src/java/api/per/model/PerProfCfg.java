package api.per.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class PerProfCfg extends BaseModel<PerProfCfg> {
//inicio zona de reemplazo

    public int profId;
    public boolean pnlSearch;
    public boolean pnlEnterprise;
    public boolean pnlAlerts;
    public boolean pnlAttend;
    public boolean pnlCandidate;
    public boolean pnlClinic;
    public boolean pnlInsClaim;
    public boolean pnlExtraFlow;
    public boolean alertClinic;
    public boolean hasEvaluations;
    public boolean hasExtraTime;
    public boolean isReadOnly;
    public boolean isAgencyCheck;
    public boolean defaultAlerts;
    public boolean showToEmp;
    public boolean canAssignEqs;
    public boolean regAnyAttenNov;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "prof_id",
            "pnl_search",
            "pnl_enterprise",
            "pnl_alerts",
            "pnl_attend",
            "pnl_candidate",
            "pnl_clinic",
            "pnl_ins_claim",
            "pnl_extra_flow",
            "alert_clinic",
            "has_evaluations",
            "has_extra_time",
            "is_read_only",
            "is_agency_check",
            "default_alerts",
            "show_to_emp",
            "can_assign_eqs",
            "reg_any_atten_nov"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, profId);
        q.setParam(2, pnlSearch);
        q.setParam(3, pnlEnterprise);
        q.setParam(4, pnlAlerts);
        q.setParam(5, pnlAttend);
        q.setParam(6, pnlCandidate);
        q.setParam(7, pnlClinic);
        q.setParam(8, pnlInsClaim);
        q.setParam(9, pnlExtraFlow);
        q.setParam(10, alertClinic);
        q.setParam(11, hasEvaluations);
        q.setParam(12, hasExtraTime);
        q.setParam(13, isReadOnly);
        q.setParam(14, isAgencyCheck);
        q.setParam(15, defaultAlerts);
        q.setParam(16, showToEmp);
        q.setParam(17, canAssignEqs);
        q.setParam(18, regAnyAttenNov);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        profId = MySQLQuery.getAsInteger(row[0]);
        pnlSearch = MySQLQuery.getAsBoolean(row[1]);
        pnlEnterprise = MySQLQuery.getAsBoolean(row[2]);
        pnlAlerts = MySQLQuery.getAsBoolean(row[3]);
        pnlAttend = MySQLQuery.getAsBoolean(row[4]);
        pnlCandidate = MySQLQuery.getAsBoolean(row[5]);
        pnlClinic = MySQLQuery.getAsBoolean(row[6]);
        pnlInsClaim = MySQLQuery.getAsBoolean(row[7]);
        pnlExtraFlow = MySQLQuery.getAsBoolean(row[8]);
        alertClinic = MySQLQuery.getAsBoolean(row[9]);
        hasEvaluations = MySQLQuery.getAsBoolean(row[10]);
        hasExtraTime = MySQLQuery.getAsBoolean(row[11]);
        isReadOnly = MySQLQuery.getAsBoolean(row[12]);
        isAgencyCheck = MySQLQuery.getAsBoolean(row[13]);
        defaultAlerts = MySQLQuery.getAsBoolean(row[14]);
        showToEmp = MySQLQuery.getAsBoolean(row[15]);
        canAssignEqs = MySQLQuery.getAsBoolean(row[16]);
        regAnyAttenNov = MySQLQuery.getAsBoolean(row[17]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_prof_cfg";
    }

    public static String getSelFlds(String alias) {
        return new PerProfCfg().getSelFldsForAlias(alias);
    }

    public static List<PerProfCfg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerProfCfg().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new PerProfCfg().deleteById(id, conn);
    }

    public static List<PerProfCfg> getAll(Connection conn) throws Exception {
        return new PerProfCfg().getAllList(conn);
    }

//fin zona de reemplazo
}
