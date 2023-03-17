package api.per.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class PerCfg extends BaseModel<PerCfg> {
//inicio zona de reemplazo

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
    public int saraRoundExtrasMinutes;
    public int surnocsem;
    public int surdiudom;
    public int surnocdom;
    public boolean advancedEndowment;
    public boolean salesman;
    public boolean mandatoryPay;
    public boolean ctrNov;
    public String certificateClass;
    public Boolean workday;
    public boolean hasCandidate;
    public boolean hasTemplates;
    public boolean hasDocumentsAlerts;
    public String areaName;
    public String subareaName;
    public boolean hasTrialPeriods;
    public boolean hasMtoContractor;
    public boolean clinicHistories;
    public boolean hasReplace;
    public boolean mandatoryChiefPos;
    public boolean mandatoryFieldsCandidate;
    public boolean hasCandidateStates;
    public boolean hasCivilState;
    public boolean hasInsClaim;
    public boolean incDiscount;
    public Integer chiefId;
    public boolean createDriver;
    public boolean showPnlColVh;
    public boolean nearestEvent;
    public boolean showNumHist;
    public boolean showFldsRteWarn;
    public boolean posVeh;
    public boolean trainScore;
    public int prevTimeExtra;
    public int prevTimeLicense;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "exdiusem",
            "exnocsem",
            "exdiudom",
            "exnocdom",
            "diu_begin",
            "diu_end",
            "alr_trial_end",
            "alr_trial_eval",
            "alr_comp_eval",
            "alr_cont_end",
            "alr_induc",
            "alr_agre",
            "goal_efi",
            "goal_rot",
            "goal_desem",
            "goal_comp",
            "goal_acc",
            "chief_name",
            "chief_doc",
            "extra_lim",
            "day_end",
            "showTabGate",
            "enterprise",
            "lock",
            "chief_area",
            "chief_cargo",
            "has_poll",
            "show_alerts",
            "dp_by_office",
            "veh_in_rep",
            "invert_names",
            "has_surcharges",
            "sara_round_extras_minutes",
            "surnocsem",
            "surdiudom",
            "surnocdom",
            "advanced_endowment",
            "salesman",
            "mandatory_pay",
            "ctr_nov",
            "certificate_class",
            "workday",
            "has_candidate",
            "has_templates",
            "has_documents_alerts",
            "area_name",
            "subarea_name",
            "has_trial_periods",
            "has_mto_contractor",
            "clinic_histories",
            "has_replace",
            "mandatory_chief_pos",
            "mandatory_fields_candidate",
            "has_candidate_states",
            "has_civil_state",
            "has_ins_claim",
            "inc_discount",
            "chief_id",
            "create_driver",
            "show_pnl_col_vh",
            "nearest_event",
            "show_num_hist",
            "show_flds_rte_warn",
            "pos_veh",
            "train_score",
            "prev_time_extra",
            "prev_time_license"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, exdiusem);
        q.setParam(2, exnocsem);
        q.setParam(3, exdiudom);
        q.setParam(4, exnocdom);
        q.setParam(5, diuBegin);
        q.setParam(6, diuEnd);
        q.setParam(7, alrTrialEnd);
        q.setParam(8, alrTrialEval);
        q.setParam(9, alrCompEval);
        q.setParam(10, alrContEnd);
        q.setParam(11, alrInduc);
        q.setParam(12, alrAgre);
        q.setParam(13, goalEfi);
        q.setParam(14, goalRot);
        q.setParam(15, goalDesem);
        q.setParam(16, goalComp);
        q.setParam(17, goalAcc);
        q.setParam(18, chiefName);
        q.setParam(19, chiefDoc);
        q.setParam(20, extraLim);
        q.setParam(21, dayEnd);
        q.setParam(22, showTabGate);
        q.setParam(23, enterprise);
        q.setParam(24, lock);
        q.setParam(25, chiefArea);
        q.setParam(26, chiefCargo);
        q.setParam(27, hasPoll);
        q.setParam(28, showAlerts);
        q.setParam(29, dpByOffice);
        q.setParam(30, vehInRep);
        q.setParam(31, invertNames);
        q.setParam(32, hasSurcharges);
        q.setParam(33, saraRoundExtrasMinutes);
        q.setParam(34, surnocsem);
        q.setParam(35, surdiudom);
        q.setParam(36, surnocdom);
        q.setParam(37, advancedEndowment);
        q.setParam(38, salesman);
        q.setParam(39, mandatoryPay);
        q.setParam(40, ctrNov);
        q.setParam(41, certificateClass);
        q.setParam(42, workday);
        q.setParam(43, hasCandidate);
        q.setParam(44, hasTemplates);
        q.setParam(45, hasDocumentsAlerts);
        q.setParam(46, areaName);
        q.setParam(47, subareaName);
        q.setParam(48, hasTrialPeriods);
        q.setParam(49, hasMtoContractor);
        q.setParam(50, clinicHistories);
        q.setParam(51, hasReplace);
        q.setParam(52, mandatoryChiefPos);
        q.setParam(53, mandatoryFieldsCandidate);
        q.setParam(54, hasCandidateStates);
        q.setParam(55, hasCivilState);
        q.setParam(56, hasInsClaim);
        q.setParam(57, incDiscount);
        q.setParam(58, chiefId);
        q.setParam(59, createDriver);
        q.setParam(60, showPnlColVh);
        q.setParam(61, nearestEvent);
        q.setParam(62, showNumHist);
        q.setParam(63, showFldsRteWarn);
        q.setParam(64, posVeh);
        q.setParam(65, trainScore);
        q.setParam(66, prevTimeExtra);
        q.setParam(67, prevTimeLicense);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        exdiusem = MySQLQuery.getAsInteger(row[0]);
        exnocsem = MySQLQuery.getAsInteger(row[1]);
        exdiudom = MySQLQuery.getAsInteger(row[2]);
        exnocdom = MySQLQuery.getAsInteger(row[3]);
        diuBegin = MySQLQuery.getAsDate(row[4]);
        diuEnd = MySQLQuery.getAsDate(row[5]);
        alrTrialEnd = MySQLQuery.getAsInteger(row[6]);
        alrTrialEval = MySQLQuery.getAsInteger(row[7]);
        alrCompEval = MySQLQuery.getAsInteger(row[8]);
        alrContEnd = MySQLQuery.getAsInteger(row[9]);
        alrInduc = MySQLQuery.getAsInteger(row[10]);
        alrAgre = MySQLQuery.getAsInteger(row[11]);
        goalEfi = MySQLQuery.getAsInteger(row[12]);
        goalRot = MySQLQuery.getAsInteger(row[13]);
        goalDesem = MySQLQuery.getAsInteger(row[14]);
        goalComp = MySQLQuery.getAsInteger(row[15]);
        goalAcc = MySQLQuery.getAsInteger(row[16]);
        chiefName = MySQLQuery.getAsString(row[17]);
        chiefDoc = MySQLQuery.getAsString(row[18]);
        extraLim = MySQLQuery.getAsInteger(row[19]);
        dayEnd = MySQLQuery.getAsDate(row[20]);
        showTabGate = MySQLQuery.getAsBoolean(row[21]);
        enterprise = MySQLQuery.getAsString(row[22]);
        lock = MySQLQuery.getAsBoolean(row[23]);
        chiefArea = MySQLQuery.getAsString(row[24]);
        chiefCargo = MySQLQuery.getAsString(row[25]);
        hasPoll = MySQLQuery.getAsBoolean(row[26]);
        showAlerts = MySQLQuery.getAsBoolean(row[27]);
        dpByOffice = MySQLQuery.getAsBoolean(row[28]);
        vehInRep = MySQLQuery.getAsBoolean(row[29]);
        invertNames = MySQLQuery.getAsBoolean(row[30]);
        hasSurcharges = MySQLQuery.getAsBoolean(row[31]);
        saraRoundExtrasMinutes = MySQLQuery.getAsInteger(row[32]);
        surnocsem = MySQLQuery.getAsInteger(row[33]);
        surdiudom = MySQLQuery.getAsInteger(row[34]);
        surnocdom = MySQLQuery.getAsInteger(row[35]);
        advancedEndowment = MySQLQuery.getAsBoolean(row[36]);
        salesman = MySQLQuery.getAsBoolean(row[37]);
        mandatoryPay = MySQLQuery.getAsBoolean(row[38]);
        ctrNov = MySQLQuery.getAsBoolean(row[39]);
        certificateClass = MySQLQuery.getAsString(row[40]);
        workday = MySQLQuery.getAsBoolean(row[41]);
        hasCandidate = MySQLQuery.getAsBoolean(row[42]);
        hasTemplates = MySQLQuery.getAsBoolean(row[43]);
        hasDocumentsAlerts = MySQLQuery.getAsBoolean(row[44]);
        areaName = MySQLQuery.getAsString(row[45]);
        subareaName = MySQLQuery.getAsString(row[46]);
        hasTrialPeriods = MySQLQuery.getAsBoolean(row[47]);
        hasMtoContractor = MySQLQuery.getAsBoolean(row[48]);
        clinicHistories = MySQLQuery.getAsBoolean(row[49]);
        hasReplace = MySQLQuery.getAsBoolean(row[50]);
        mandatoryChiefPos = MySQLQuery.getAsBoolean(row[51]);
        mandatoryFieldsCandidate = MySQLQuery.getAsBoolean(row[52]);
        hasCandidateStates = MySQLQuery.getAsBoolean(row[53]);
        hasCivilState = MySQLQuery.getAsBoolean(row[54]);
        hasInsClaim = MySQLQuery.getAsBoolean(row[55]);
        incDiscount = MySQLQuery.getAsBoolean(row[56]);
        chiefId = MySQLQuery.getAsInteger(row[57]);
        createDriver = MySQLQuery.getAsBoolean(row[58]);
        showPnlColVh = MySQLQuery.getAsBoolean(row[59]);
        nearestEvent = MySQLQuery.getAsBoolean(row[60]);
        showNumHist = MySQLQuery.getAsBoolean(row[61]);
        showFldsRteWarn = MySQLQuery.getAsBoolean(row[62]);
        posVeh = MySQLQuery.getAsBoolean(row[63]);
        trainScore = MySQLQuery.getAsBoolean(row[64]);
        prevTimeExtra = MySQLQuery.getAsInteger(row[65]);
        prevTimeLicense = MySQLQuery.getAsInteger(row[66]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_cfg";
    }

    public static String getSelFlds(String alias) {
        return new PerCfg().getSelFldsForAlias(alias);
    }

    public static List<PerCfg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerCfg().getListFromQuery(q, conn);
    }

//fin zona de reemplazo

}
