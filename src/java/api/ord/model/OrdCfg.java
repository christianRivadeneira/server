package api.ord.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class OrdCfg extends BaseModel<OrdCfg> {
//inicio zona de reemplazo

    public int cylPollRatio;
    public int tankPollRatio;
    public int limitTime;
    public int criticTime;
    public int alertTime;
    public int cylPqrLimitTime;
    public int cylPqrCriticTime;
    public int cylPqrAlertTime;
    public int tankPqrLimitTime;
    public int tankPqrCriticTime;
    public int tankPqrAlertTime;
    public int otherPqrLimitTime;
    public int otherPqrCriticTime;
    public int otherPqrAlertTime;
    public int pqrComLimitTime;
    public int pqrComCriticTime;
    public int pqrComAlertTime;
    public boolean pnlRequest;
    public int cylOrderLockTime;
    public int tankVisitPollRatio;
    public int storeVisitPollRatio;
    public int tankVisitDays;
    public int storeVisitDays;
    public int repairLimitTime;
    public int repairCriticTime;
    public int repairAlertTime;
    public boolean pollByStadistics;
    public boolean oldOfficeMode;
    public boolean birthday;
    public boolean subreason;
    public boolean bills;
    public boolean store;
    public boolean showAfilPnl;
    public boolean chklist;
    public boolean enterprise;
    public boolean useCmbResp;
    public boolean subAndDesc;
    public boolean showChannel;
    public boolean printPqrs;
    public boolean pqrOtherAutoSerial;
    public boolean uniqueOffVh;
    public boolean resetUnattended;
    public boolean globalSerials;
    public boolean clientsApp;
    public boolean salesApp;
    public boolean virtualApp;
    public boolean orderPrice;
    public boolean descSui;
    public boolean supreason;
    public boolean orderCylFuture;
    public boolean pqrOtherHourConf;
    public int afilPqrLimitTime;
    public int afilPqrCriticTime;
    public int afilPqrAlertTime;
    public boolean showAttach;
    public boolean showColumnSui;
    public boolean requiredEmail;
    public boolean cylCaptVh;
    public boolean advEmailChk;
    public boolean pollResp;
    public boolean crtAllPobs;
    public boolean showDetCauseSui;
    public boolean showPnlWarning;
    public boolean showAssiPoll;
    public int assisPollRatio;
    public boolean showSubject;
    public boolean autoasignTech;
    public boolean inactCtrProv;
    public boolean hasObsvOtherAct;
    public boolean pollOtherOperator;
    public String pqrCylNotes;
    public String pqrTankNotes;
    public String pqrOtherNotes;
    public boolean hasTechApp;
    public boolean showPnlStore;
    public boolean showPqrNotes;
    public boolean workIncentives;
    public boolean showKgsOrder;
    public boolean orderTankFuture;
    public boolean pqrConfFilterDate;
    public boolean editOrderConf;
    public boolean locateOrderTanks;
    public boolean notesToNovs;
    public boolean getCallDate;
    public boolean ordCreateVisit;
    public boolean oldOrdConf;
    public boolean assignGps;
    public boolean listAllTks;
    public boolean pqrAdmissible;
    public boolean orfeo;
    public boolean orfeoMailUsers;
    public boolean pnlCylNovAlert;
    public boolean orderCylAttach;
    public boolean showReferences;
    public boolean showNoPoll;
    public int execVisitDays;
    public int execVisitPollRatio;
    public String orfeoDependencyCode;
    public boolean showSinister;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "cyl_poll_ratio",
            "tank_poll_ratio",
            "limit_time",
            "critic_time",
            "alert_time",
            "cyl_pqr_limit_time",
            "cyl_pqr_critic_time",
            "cyl_pqr_alert_time",
            "tank_pqr_limit_time",
            "tank_pqr_critic_time",
            "tank_pqr_alert_time",
            "other_pqr_limit_time",
            "other_pqr_critic_time",
            "other_pqr_alert_time",
            "pqr_com_limit_time",
            "pqr_com_critic_time",
            "pqr_com_alert_time",
            "pnl_request",
            "cyl_order_lock_time",
            "tank_visit_poll_ratio",
            "store_visit_poll_ratio",
            "tank_visit_days",
            "store_visit_days",
            "repair_limit_time",
            "repair_critic_time",
            "repair_alert_time",
            "poll_by_stadistics",
            "old_office_mode",
            "birthday",
            "subreason",
            "bills",
            "store",
            "show_afil_pnl",
            "chklist",
            "enterprise",
            "use_cmb_resp",
            "sub_and_desc",
            "show_channel",
            "print_pqrs",
            "pqr_other_auto_serial",
            "unique_off_vh",
            "reset_unattended",
            "global_serials",
            "clients_app",
            "sales_app",
            "virtual_app",
            "order_price",
            "desc_sui",
            "supreason",
            "order_cyl_future",
            "pqr_other_hour_conf",
            "afil_pqr_limit_time",
            "afil_pqr_critic_time",
            "afil_pqr_alert_time",
            "show_attach",
            "show_column_sui",
            "required_email",
            "cyl_capt_vh",
            "adv_email_chk",
            "poll_resp",
            "crt_all_pobs",
            "show_det_cause_sui",
            "show_pnl_warning",
            "show_assi_poll",
            "assis_poll_ratio",
            "show_subject",
            "autoasign_tech",
            "inact_ctr_prov",
            "has_obsv_other_act",
            "poll_other_operator",
            "pqr_cyl_notes",
            "pqr_tank_notes",
            "pqr_other_notes",
            "has_tech_app",
            "show_pnl_store",
            "show_pqr_notes",
            "work_incentives",
            "show_kgs_order",
            "order_tank_future",
            "pqr_conf_filter_date",
            "edit_order_conf",
            "locate_order_tanks",
            "notes_to_novs",
            "get_call_date",
            "ord_create_visit",
            "old_ord_conf",
            "assign_gps",
            "list_all_tks",
            "pqr_admissible",
            "orfeo",
            "orfeo_mail_users",
            "pnl_cyl_nov_alert",
            "order_cyl_attach",
            "show_references",
            "show_no_poll",
            "exec_visit_days",
            "exec_visit_poll_ratio",
            "orfeo_dependency_code",
            "show_sinister"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, cylPollRatio);
        q.setParam(2, tankPollRatio);
        q.setParam(3, limitTime);
        q.setParam(4, criticTime);
        q.setParam(5, alertTime);
        q.setParam(6, cylPqrLimitTime);
        q.setParam(7, cylPqrCriticTime);
        q.setParam(8, cylPqrAlertTime);
        q.setParam(9, tankPqrLimitTime);
        q.setParam(10, tankPqrCriticTime);
        q.setParam(11, tankPqrAlertTime);
        q.setParam(12, otherPqrLimitTime);
        q.setParam(13, otherPqrCriticTime);
        q.setParam(14, otherPqrAlertTime);
        q.setParam(15, pqrComLimitTime);
        q.setParam(16, pqrComCriticTime);
        q.setParam(17, pqrComAlertTime);
        q.setParam(18, pnlRequest);
        q.setParam(19, cylOrderLockTime);
        q.setParam(20, tankVisitPollRatio);
        q.setParam(21, storeVisitPollRatio);
        q.setParam(22, tankVisitDays);
        q.setParam(23, storeVisitDays);
        q.setParam(24, repairLimitTime);
        q.setParam(25, repairCriticTime);
        q.setParam(26, repairAlertTime);
        q.setParam(27, pollByStadistics);
        q.setParam(28, oldOfficeMode);
        q.setParam(29, birthday);
        q.setParam(30, subreason);
        q.setParam(31, bills);
        q.setParam(32, store);
        q.setParam(33, showAfilPnl);
        q.setParam(34, chklist);
        q.setParam(35, enterprise);
        q.setParam(36, useCmbResp);
        q.setParam(37, subAndDesc);
        q.setParam(38, showChannel);
        q.setParam(39, printPqrs);
        q.setParam(40, pqrOtherAutoSerial);
        q.setParam(41, uniqueOffVh);
        q.setParam(42, resetUnattended);
        q.setParam(43, globalSerials);
        q.setParam(44, clientsApp);
        q.setParam(45, salesApp);
        q.setParam(46, virtualApp);
        q.setParam(47, orderPrice);
        q.setParam(48, descSui);
        q.setParam(49, supreason);
        q.setParam(50, orderCylFuture);
        q.setParam(51, pqrOtherHourConf);
        q.setParam(52, afilPqrLimitTime);
        q.setParam(53, afilPqrCriticTime);
        q.setParam(54, afilPqrAlertTime);
        q.setParam(55, showAttach);
        q.setParam(56, showColumnSui);
        q.setParam(57, requiredEmail);
        q.setParam(58, cylCaptVh);
        q.setParam(59, advEmailChk);
        q.setParam(60, pollResp);
        q.setParam(61, crtAllPobs);
        q.setParam(62, showDetCauseSui);
        q.setParam(63, showPnlWarning);
        q.setParam(64, showAssiPoll);
        q.setParam(65, assisPollRatio);
        q.setParam(66, showSubject);
        q.setParam(67, autoasignTech);
        q.setParam(68, inactCtrProv);
        q.setParam(69, hasObsvOtherAct);
        q.setParam(70, pollOtherOperator);
        q.setParam(71, pqrCylNotes);
        q.setParam(72, pqrTankNotes);
        q.setParam(73, pqrOtherNotes);
        q.setParam(74, hasTechApp);
        q.setParam(75, showPnlStore);
        q.setParam(76, showPqrNotes);
        q.setParam(77, workIncentives);
        q.setParam(78, showKgsOrder);
        q.setParam(79, orderTankFuture);
        q.setParam(80, pqrConfFilterDate);
        q.setParam(81, editOrderConf);
        q.setParam(82, locateOrderTanks);
        q.setParam(83, notesToNovs);
        q.setParam(84, getCallDate);
        q.setParam(85, ordCreateVisit);
        q.setParam(86, oldOrdConf);
        q.setParam(87, assignGps);
        q.setParam(88, listAllTks);
        q.setParam(89, pqrAdmissible);
        q.setParam(90, orfeo);
        q.setParam(91, orfeoMailUsers);
        q.setParam(92, pnlCylNovAlert);
        q.setParam(93, orderCylAttach);
        q.setParam(94, showReferences);
        q.setParam(95, showNoPoll);
        q.setParam(96, execVisitDays);
        q.setParam(97, execVisitPollRatio);
        q.setParam(98, orfeoDependencyCode);
        q.setParam(99, showSinister);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        cylPollRatio = MySQLQuery.getAsInteger(row[0]);
        tankPollRatio = MySQLQuery.getAsInteger(row[1]);
        limitTime = MySQLQuery.getAsInteger(row[2]);
        criticTime = MySQLQuery.getAsInteger(row[3]);
        alertTime = MySQLQuery.getAsInteger(row[4]);
        cylPqrLimitTime = MySQLQuery.getAsInteger(row[5]);
        cylPqrCriticTime = MySQLQuery.getAsInteger(row[6]);
        cylPqrAlertTime = MySQLQuery.getAsInteger(row[7]);
        tankPqrLimitTime = MySQLQuery.getAsInteger(row[8]);
        tankPqrCriticTime = MySQLQuery.getAsInteger(row[9]);
        tankPqrAlertTime = MySQLQuery.getAsInteger(row[10]);
        otherPqrLimitTime = MySQLQuery.getAsInteger(row[11]);
        otherPqrCriticTime = MySQLQuery.getAsInteger(row[12]);
        otherPqrAlertTime = MySQLQuery.getAsInteger(row[13]);
        pqrComLimitTime = MySQLQuery.getAsInteger(row[14]);
        pqrComCriticTime = MySQLQuery.getAsInteger(row[15]);
        pqrComAlertTime = MySQLQuery.getAsInteger(row[16]);
        pnlRequest = MySQLQuery.getAsBoolean(row[17]);
        cylOrderLockTime = MySQLQuery.getAsInteger(row[18]);
        tankVisitPollRatio = MySQLQuery.getAsInteger(row[19]);
        storeVisitPollRatio = MySQLQuery.getAsInteger(row[20]);
        tankVisitDays = MySQLQuery.getAsInteger(row[21]);
        storeVisitDays = MySQLQuery.getAsInteger(row[22]);
        repairLimitTime = MySQLQuery.getAsInteger(row[23]);
        repairCriticTime = MySQLQuery.getAsInteger(row[24]);
        repairAlertTime = MySQLQuery.getAsInteger(row[25]);
        pollByStadistics = MySQLQuery.getAsBoolean(row[26]);
        oldOfficeMode = MySQLQuery.getAsBoolean(row[27]);
        birthday = MySQLQuery.getAsBoolean(row[28]);
        subreason = MySQLQuery.getAsBoolean(row[29]);
        bills = MySQLQuery.getAsBoolean(row[30]);
        store = MySQLQuery.getAsBoolean(row[31]);
        showAfilPnl = MySQLQuery.getAsBoolean(row[32]);
        chklist = MySQLQuery.getAsBoolean(row[33]);
        enterprise = MySQLQuery.getAsBoolean(row[34]);
        useCmbResp = MySQLQuery.getAsBoolean(row[35]);
        subAndDesc = MySQLQuery.getAsBoolean(row[36]);
        showChannel = MySQLQuery.getAsBoolean(row[37]);
        printPqrs = MySQLQuery.getAsBoolean(row[38]);
        pqrOtherAutoSerial = MySQLQuery.getAsBoolean(row[39]);
        uniqueOffVh = MySQLQuery.getAsBoolean(row[40]);
        resetUnattended = MySQLQuery.getAsBoolean(row[41]);
        globalSerials = MySQLQuery.getAsBoolean(row[42]);
        clientsApp = MySQLQuery.getAsBoolean(row[43]);
        salesApp = MySQLQuery.getAsBoolean(row[44]);
        virtualApp = MySQLQuery.getAsBoolean(row[45]);
        orderPrice = MySQLQuery.getAsBoolean(row[46]);
        descSui = MySQLQuery.getAsBoolean(row[47]);
        supreason = MySQLQuery.getAsBoolean(row[48]);
        orderCylFuture = MySQLQuery.getAsBoolean(row[49]);
        pqrOtherHourConf = MySQLQuery.getAsBoolean(row[50]);
        afilPqrLimitTime = MySQLQuery.getAsInteger(row[51]);
        afilPqrCriticTime = MySQLQuery.getAsInteger(row[52]);
        afilPqrAlertTime = MySQLQuery.getAsInteger(row[53]);
        showAttach = MySQLQuery.getAsBoolean(row[54]);
        showColumnSui = MySQLQuery.getAsBoolean(row[55]);
        requiredEmail = MySQLQuery.getAsBoolean(row[56]);
        cylCaptVh = MySQLQuery.getAsBoolean(row[57]);
        advEmailChk = MySQLQuery.getAsBoolean(row[58]);
        pollResp = MySQLQuery.getAsBoolean(row[59]);
        crtAllPobs = MySQLQuery.getAsBoolean(row[60]);
        showDetCauseSui = MySQLQuery.getAsBoolean(row[61]);
        showPnlWarning = MySQLQuery.getAsBoolean(row[62]);
        showAssiPoll = MySQLQuery.getAsBoolean(row[63]);
        assisPollRatio = MySQLQuery.getAsInteger(row[64]);
        showSubject = MySQLQuery.getAsBoolean(row[65]);
        autoasignTech = MySQLQuery.getAsBoolean(row[66]);
        inactCtrProv = MySQLQuery.getAsBoolean(row[67]);
        hasObsvOtherAct = MySQLQuery.getAsBoolean(row[68]);
        pollOtherOperator = MySQLQuery.getAsBoolean(row[69]);
        pqrCylNotes = MySQLQuery.getAsString(row[70]);
        pqrTankNotes = MySQLQuery.getAsString(row[71]);
        pqrOtherNotes = MySQLQuery.getAsString(row[72]);
        hasTechApp = MySQLQuery.getAsBoolean(row[73]);
        showPnlStore = MySQLQuery.getAsBoolean(row[74]);
        showPqrNotes = MySQLQuery.getAsBoolean(row[75]);
        workIncentives = MySQLQuery.getAsBoolean(row[76]);
        showKgsOrder = MySQLQuery.getAsBoolean(row[77]);
        orderTankFuture = MySQLQuery.getAsBoolean(row[78]);
        pqrConfFilterDate = MySQLQuery.getAsBoolean(row[79]);
        editOrderConf = MySQLQuery.getAsBoolean(row[80]);
        locateOrderTanks = MySQLQuery.getAsBoolean(row[81]);
        notesToNovs = MySQLQuery.getAsBoolean(row[82]);
        getCallDate = MySQLQuery.getAsBoolean(row[83]);
        ordCreateVisit = MySQLQuery.getAsBoolean(row[84]);
        oldOrdConf = MySQLQuery.getAsBoolean(row[85]);
        assignGps = MySQLQuery.getAsBoolean(row[86]);
        listAllTks = MySQLQuery.getAsBoolean(row[87]);
        pqrAdmissible = MySQLQuery.getAsBoolean(row[88]);
        orfeo = MySQLQuery.getAsBoolean(row[89]);
        orfeoMailUsers = MySQLQuery.getAsBoolean(row[90]);
        pnlCylNovAlert = MySQLQuery.getAsBoolean(row[91]);
        orderCylAttach = MySQLQuery.getAsBoolean(row[92]);
        showReferences = MySQLQuery.getAsBoolean(row[93]);
        showNoPoll = MySQLQuery.getAsBoolean(row[94]);
        execVisitDays = MySQLQuery.getAsInteger(row[95]);
        execVisitPollRatio = MySQLQuery.getAsInteger(row[96]);
        orfeoDependencyCode = MySQLQuery.getAsString(row[97]);
        showSinister = MySQLQuery.getAsBoolean(row[98]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_cfg";
    }

    public static String getSelFlds(String alias) {
        return new OrdCfg().getSelFldsForAlias(alias);
    }

    public static List<OrdCfg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdCfg().getListFromQuery(q, conn);
    }

    public static List<OrdCfg> getList(Params p, Connection conn) throws Exception {
        return new OrdCfg().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdCfg().deleteById(id, conn);
    }

    public static List<OrdCfg> getAll(Connection conn) throws Exception {
        return new OrdCfg().getAllList(conn);
    }

//fin zona de reemplazo

}
