package web.personal;

import java.sql.Connection;
import java.util.Date;
import utilities.MySQLQuery;

public class PerCfg {
//inicio zona de reemplazo

    public int id;
    public int exdiusem;
    public int exnocsem;
    public int exdiudom;
    public int exnocdom;
    public Date diuBegin;
    public Date diuEnd;
    public int alrTrialEnd;
    public int alrTrialEval;
    public int alrCompEval;
    public int alrContEnd;
    public int alrInduc;
    public int alrAgre;
    public int goalEfi;
    public int goalRot;
    public int goalDesem;
    public int goalComp;
    public int goalAcc;
    public String chiefName;
    public String chiefDoc;
    public int extraLim;
    public Date dayEnd;
    public boolean showTabGate;
    public String enterprise;
    public boolean lock;
    public String chiefArea;
    public String chiefCargo;
    public boolean hasPoll;
    public boolean showAlerts;
    public boolean dpByOffice;
    public boolean vehInRep;
    public boolean invertNames;
    public boolean hasSurcharges;
    public int surnocsem;
    public int surdiudom;
    public int surnocdom;
    public boolean advancedEndowment;
    public boolean salesman;
    public Boolean mandatoryPay;
    public Boolean ctrNov;
    public String certificateClass;
    public Boolean workday;
    public Boolean hasCandidate;
    public Boolean hasTemplates;
    public Boolean hasDocumentsAlerts;
    public String areaName;
    public String subareaName;
    public Boolean hasTrialPeriods;
    public boolean hasMtoContractor;
    public boolean clinicHistories;

    private static final String SEL_FLDS = "`exdiusem`, "
            + "`exnocsem`, "
            + "`exdiudom`, "
            + "`exnocdom`, "
            + "`diu_begin`, "
            + "`diu_end`, "
            + "`alr_trial_end`, "
            + "`alr_trial_eval`, "
            + "`alr_comp_eval`, "
            + "`alr_cont_end`, "
            + "`alr_induc`, "
            + "`alr_agre`, "
            + "`goal_efi`, "
            + "`goal_rot`, "
            + "`goal_desem`, "
            + "`goal_comp`, "
            + "`goal_acc`, "
            + "`chief_name`, "
            + "`chief_doc`, "
            + "`extra_lim`, "
            + "`day_end`, "
            + "`showTabGate`, "
            + "`enterprise`, "
            + "`lock`, "
            + "`chief_area`, "
            + "`chief_cargo`, "
            + "`has_poll`, "
            + "`show_alerts`, "
            + "`dp_by_office`, "
            + "`veh_in_rep`, "
            + "`invert_names`, "
            + "`has_surcharges`, "
            + "`surnocsem`, "
            + "`surdiudom`, "
            + "`surnocdom`, "
            + "`advanced_endowment`, "
            + "`salesman`, "
            + "`mandatory_pay`, "
            + "`ctr_nov`, "
            + "`certificate_class`, "
            + "`workday`, "
            + "`has_candidate`, "
            + "`has_templates`, "
            + "`has_documents_alerts`, "
            + "`area_name`, "
            + "`subarea_name`, "
            + "`has_trial_periods`, "
            + "`has_mto_contractor`, "
            + "`clinic_histories`";

    private static final String SET_FLDS = "per_cfg SET "
            + "`exdiusem` = ?1, "
            + "`exnocsem` = ?2, "
            + "`exdiudom` = ?3, "
            + "`exnocdom` = ?4, "
            + "`diu_begin` = ?5, "
            + "`diu_end` = ?6, "
            + "`alr_trial_end` = ?7, "
            + "`alr_trial_eval` = ?8, "
            + "`alr_comp_eval` = ?9, "
            + "`alr_cont_end` = ?10, "
            + "`alr_induc` = ?11, "
            + "`alr_agre` = ?12, "
            + "`goal_efi` = ?13, "
            + "`goal_rot` = ?14, "
            + "`goal_desem` = ?15, "
            + "`goal_comp` = ?16, "
            + "`goal_acc` = ?17, "
            + "`chief_name` = ?18, "
            + "`chief_doc` = ?19, "
            + "`extra_lim` = ?20, "
            + "`day_end` = ?21, "
            + "`showTabGate` = ?22, "
            + "`enterprise` = ?23, "
            + "`lock` = ?24, "
            + "`chief_area` = ?25, "
            + "`chief_cargo` = ?26, "
            + "`has_poll` = ?27, "
            + "`show_alerts` = ?28, "
            + "`dp_by_office` = ?29, "
            + "`veh_in_rep` = ?30, "
            + "`invert_names` = ?31, "
            + "`has_surcharges` = ?32, "
            + "`surnocsem` = ?33, "
            + "`surdiudom` = ?34, "
            + "`surnocdom` = ?35, "
            + "`advanced_endowment` = ?36, "
            + "`salesman` = ?37, "
            + "`mandatory_pay` = ?38, "
            + "`ctr_nov` = ?39, "
            + "`certificate_class` = ?40, "
            + "`workday` = ?41, "
            + "`has_candidate` = ?42, "
            + "`has_templates` = ?43, "
            + "`has_documents_alerts` = ?44, "
            + "`area_name` = ?45, "
            + "`subarea_name` = ?46, "
            + "`has_trial_periods` = ?47, "
            + "`has_mto_contractor` = ?48, "
            + "`clinic_histories` = ?49";

    private static void setFields(PerCfg obj, MySQLQuery q) {
        q.setParam(1, obj.exdiusem);
        q.setParam(2, obj.exnocsem);
        q.setParam(3, obj.exdiudom);
        q.setParam(4, obj.exnocdom);
        q.setParam(5, obj.diuBegin);
        q.setParam(6, obj.diuEnd);
        q.setParam(7, obj.alrTrialEnd);
        q.setParam(8, obj.alrTrialEval);
        q.setParam(9, obj.alrCompEval);
        q.setParam(10, obj.alrContEnd);
        q.setParam(11, obj.alrInduc);
        q.setParam(12, obj.alrAgre);
        q.setParam(13, obj.goalEfi);
        q.setParam(14, obj.goalRot);
        q.setParam(15, obj.goalDesem);
        q.setParam(16, obj.goalComp);
        q.setParam(17, obj.goalAcc);
        q.setParam(18, obj.chiefName);
        q.setParam(19, obj.chiefDoc);
        q.setParam(20, obj.extraLim);
        q.setParam(21, obj.dayEnd);
        q.setParam(22, obj.showTabGate);
        q.setParam(23, obj.enterprise);
        q.setParam(24, obj.lock);
        q.setParam(25, obj.chiefArea);
        q.setParam(26, obj.chiefCargo);
        q.setParam(27, obj.hasPoll);
        q.setParam(28, obj.showAlerts);
        q.setParam(29, obj.dpByOffice);
        q.setParam(30, obj.vehInRep);
        q.setParam(31, obj.invertNames);
        q.setParam(32, obj.hasSurcharges);
        q.setParam(33, obj.surnocsem);
        q.setParam(34, obj.surdiudom);
        q.setParam(35, obj.surnocdom);
        q.setParam(36, obj.advancedEndowment);
        q.setParam(37, obj.salesman);
        q.setParam(38, obj.mandatoryPay);
        q.setParam(39, obj.ctrNov);
        q.setParam(40, obj.certificateClass);
        q.setParam(41, obj.workday);
        q.setParam(42, obj.hasCandidate);
        q.setParam(43, obj.hasTemplates);
        q.setParam(44, obj.hasDocumentsAlerts);
        q.setParam(45, obj.areaName);
        q.setParam(46, obj.subareaName);
        q.setParam(47, obj.hasTrialPeriods);
        q.setParam(48, obj.hasMtoContractor);
        q.setParam(49, obj.clinicHistories);

    }

    public static PerCfg getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        PerCfg obj = new PerCfg();
        obj.exdiusem = MySQLQuery.getAsInteger(row[0]);
        obj.exnocsem = MySQLQuery.getAsInteger(row[1]);
        obj.exdiudom = MySQLQuery.getAsInteger(row[2]);
        obj.exnocdom = MySQLQuery.getAsInteger(row[3]);
        obj.diuBegin = MySQLQuery.getAsDate(row[4]);
        obj.diuEnd = MySQLQuery.getAsDate(row[5]);
        obj.alrTrialEnd = MySQLQuery.getAsInteger(row[6]);
        obj.alrTrialEval = MySQLQuery.getAsInteger(row[7]);
        obj.alrCompEval = MySQLQuery.getAsInteger(row[8]);
        obj.alrContEnd = MySQLQuery.getAsInteger(row[9]);
        obj.alrInduc = MySQLQuery.getAsInteger(row[10]);
        obj.alrAgre = MySQLQuery.getAsInteger(row[11]);
        obj.goalEfi = MySQLQuery.getAsInteger(row[12]);
        obj.goalRot = MySQLQuery.getAsInteger(row[13]);
        obj.goalDesem = MySQLQuery.getAsInteger(row[14]);
        obj.goalComp = MySQLQuery.getAsInteger(row[15]);
        obj.goalAcc = MySQLQuery.getAsInteger(row[16]);
        obj.chiefName = MySQLQuery.getAsString(row[17]);
        obj.chiefDoc = MySQLQuery.getAsString(row[18]);
        obj.extraLim = MySQLQuery.getAsInteger(row[19]);
        obj.dayEnd = MySQLQuery.getAsDate(row[20]);
        obj.showTabGate = MySQLQuery.getAsBoolean(row[21]);
        obj.enterprise = MySQLQuery.getAsString(row[22]);
        obj.lock = MySQLQuery.getAsBoolean(row[23]);
        obj.chiefArea = MySQLQuery.getAsString(row[24]);
        obj.chiefCargo = MySQLQuery.getAsString(row[25]);
        obj.hasPoll = MySQLQuery.getAsBoolean(row[26]);
        obj.showAlerts = MySQLQuery.getAsBoolean(row[27]);
        obj.dpByOffice = MySQLQuery.getAsBoolean(row[28]);
        obj.vehInRep = MySQLQuery.getAsBoolean(row[29]);
        obj.invertNames = MySQLQuery.getAsBoolean(row[30]);
        obj.hasSurcharges = MySQLQuery.getAsBoolean(row[31]);
        obj.surnocsem = MySQLQuery.getAsInteger(row[32]);
        obj.surdiudom = MySQLQuery.getAsInteger(row[33]);
        obj.surnocdom = MySQLQuery.getAsInteger(row[34]);
        obj.advancedEndowment = MySQLQuery.getAsBoolean(row[35]);
        obj.salesman = MySQLQuery.getAsBoolean(row[36]);
        obj.mandatoryPay = MySQLQuery.getAsBoolean(row[37]);
        obj.ctrNov = MySQLQuery.getAsBoolean(row[38]);
        obj.certificateClass = MySQLQuery.getAsString(row[39]);
        obj.workday = MySQLQuery.getAsBoolean(row[40]);
        obj.hasCandidate = MySQLQuery.getAsBoolean(row[41]);
        obj.hasTemplates = MySQLQuery.getAsBoolean(row[42]);
        obj.hasDocumentsAlerts = MySQLQuery.getAsBoolean(row[43]);
        obj.areaName = MySQLQuery.getAsString(row[44]);
        obj.subareaName = MySQLQuery.getAsString(row[45]);
        obj.hasTrialPeriods = MySQLQuery.getAsBoolean(row[46]);
        obj.hasMtoContractor = MySQLQuery.getAsBoolean(row[47]);
        obj.clinicHistories = MySQLQuery.getAsBoolean(row[48]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM per_cfg WHERE id = " + id;
    }

    public PerCfg select(int id, Connection ep) throws Exception {
        return PerCfg.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(ep));
    }

    public void update(PerCfg pobj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + pobj.id);
        setFields(pobj, q);
        q.executeUpdate(ep);
    }
}
