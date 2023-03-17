package model.quality;

import utilities.MySQLQuery;
import java.sql.Connection;

public class CalCfg {
//inicio zona de reemplazo

    public int id;
    public Integer chiefId;
    public String managerId;
    public Integer remind1;
    public Integer remind2;
    public Integer remind3;
    public Integer remind4;
    public int daysBeforeMeet;
    public int daysBeforeAgreement;
    public int daysBeforeActivity;
    public int daysBeforeAudit;
    public int indDayLimit;
    public int auditDay;
    public boolean showImageMap;
    public boolean showNews;
    public boolean showBsc;
    public boolean showFollow;
    public boolean showRisks;
    public String docRoot;
    public String docRootPrefix;
    public String docMacroPrefix;
    public String docProcPrefix;
    public boolean docAllowSuperEdit;
    public boolean docAllowLeadAsSuper;
    public boolean docAskForPdf;
    public boolean generateCodes;
    public boolean showStandard;
    public boolean respons;
    public boolean showPnlMails;
    public boolean changeTypeDoc;
    public int indCutMonth;
    public boolean showEntName;
    public String leftLblMap;
    public String rightLblMap;
    public boolean shAproBy;
    public String upLblMap;
    public boolean showLblMap;
    public boolean sendMailManager;
    public boolean sendMailProcemp;
    public boolean showAccess;
    public Integer approvId;
    public boolean showTypeSource;
    public boolean selSourceAction;
    public boolean showActDesc;
    public boolean allowsCosts;
    public boolean showFooter;
    public boolean revEditDocs;
    public boolean aproEditDocs;
    public boolean checkNumDocs;
    public boolean validateIndInputDate;
    public boolean showCodsExcel;
    public boolean extraFieldsExtDoc;
    public boolean showTempMod;

    private static final String SEL_FLDS = "`chief_id`, "
            + "`manager_id`, "
            + "`remind_1`, "
            + "`remind_2`, "
            + "`remind_3`, "
            + "`remind_4`, "
            + "`days_before_meet`, "
            + "`days_before_agreement`, "
            + "`days_before_activity`, "
            + "`days_before_audit`, "
            + "`ind_day_limit`, "
            + "`audit_day`, "
            + "`show_image_map`, "
            + "`show_news`, "
            + "`show_bsc`, "
            + "`show_follow`, "
            + "`show_risks`, "
            + "`doc_root`, "
            + "`doc_root_prefix`, "
            + "`doc_macro_prefix`, "
            + "`doc_proc_prefix`, "
            + "`doc_allow_super_edit`, "
            + "`doc_allow_lead_as_super`, "
            + "`doc_ask_for_pdf`, "
            + "`generate_codes`, "
            + "`show_standard`, "
            + "`respons`, "
            + "`show_pnl_mails`, "
            + "`change_type_doc`, "
            + "`ind_cut_month`, "
            + "`show_ent_name`, "
            + "`left_lbl_map`, "
            + "`right_lbl_map`, "
            + "`sh_apro_by`, "
            + "`up_lbl_map`, "
            + "`show_lbl_map`, "
            + "`send_mail_manager`, "
            + "`send_mail_procemp`, "
            + "`show_access`, "
            + "`approv_id`, "
            + "`show_type_source`, "
            + "`sel_source_action`, "
            + "`show_act_desc`, "
            + "`allows_costs`, "
            + "`show_footer`, "
            + "`rev_edit_docs`, "
            + "`apro_edit_docs`, "
            + "`check_num_docs`, "
            + "`validate_ind_input_date`, "
            + "`show_cods_excel`,"
            + "`extra_fields_ext_doc`, "
            + "`show_temp_mod` ";

    private static final String SET_FLDS = "cal_cfg SET "
            + "`chief_id` = ?1, "
            + "`manager_id` = ?2, "
            + "`remind_1` = ?3, "
            + "`remind_2` = ?4, "
            + "`remind_3` = ?5, "
            + "`remind_4` = ?6, "
            + "`days_before_meet` = ?7, "
            + "`days_before_agreement` = ?8, "
            + "`days_before_activity` = ?9, "
            + "`days_before_audit` = ?10, "
            + "`ind_day_limit` = ?11, "
            + "`audit_day` = ?12, "
            + "`show_image_map` = ?13, "
            + "`show_news` = ?14, "
            + "`show_bsc` = ?15, "
            + "`show_follow` = ?16, "
            + "`show_risks` = ?17, "
            + "`doc_root` = ?18, "
            + "`doc_root_prefix` = ?19, "
            + "`doc_macro_prefix` = ?20, "
            + "`doc_proc_prefix` = ?21, "
            + "`doc_allow_super_edit` = ?22, "
            + "`doc_allow_lead_as_super` = ?23, "
            + "`doc_ask_for_pdf` = ?24, "
            + "`generate_codes` = ?25, "
            + "`show_standard` = ?26, "
            + "`respons` = ?27, "
            + "`show_pnl_mails` = ?28, "
            + "`change_type_doc` = ?29, "
            + "`ind_cut_month` = ?30, "
            + "`show_ent_name` = ?31, "
            + "`left_lbl_map` = ?32, "
            + "`right_lbl_map` = ?33, "
            + "`sh_apro_by` = ?34, "
            + "`up_lbl_map` = ?35, "
            + "`show_lbl_map` = ?36, "
            + "`send_mail_manager` = ?37, "
            + "`send_mail_procemp` = ?38, "
            + "`show_access` = ?39, "
            + "`approv_id` = ?40, "
            + "`show_type_source` = ?41, "
            + "`sel_source_action` = ?42, "
            + "`show_act_desc` = ?43, "
            + "`allows_costs` = ?44, "
            + "`show_footer` = ?45, "
            + "`rev_edit_docs` = ?46, "
            + "`apro_edit_docs` = ?47, "
            + "`check_num_docs` = ?48, "
            + "`validate_ind_input_date` = ?49, "
            + "`show_cods_excel` = ?50, "
            + "`extra_fields_ext_doc` = ?51, "
            + "`show_temp_mod` = ?52 ";

    private static void setFields(CalCfg obj, MySQLQuery q) {
        q.setParam(1, obj.chiefId);
        q.setParam(2, obj.managerId);
        q.setParam(3, obj.remind1);
        q.setParam(4, obj.remind2);
        q.setParam(5, obj.remind3);
        q.setParam(6, obj.remind4);
        q.setParam(7, obj.daysBeforeMeet);
        q.setParam(8, obj.daysBeforeAgreement);
        q.setParam(9, obj.daysBeforeActivity);
        q.setParam(10, obj.daysBeforeAudit);
        q.setParam(11, obj.indDayLimit);
        q.setParam(12, obj.auditDay);
        q.setParam(13, obj.showImageMap);
        q.setParam(14, obj.showNews);
        q.setParam(15, obj.showBsc);
        q.setParam(16, obj.showFollow);
        q.setParam(17, obj.showRisks);
        q.setParam(18, obj.docRoot);
        q.setParam(19, obj.docRootPrefix);
        q.setParam(20, obj.docMacroPrefix);
        q.setParam(21, obj.docProcPrefix);
        q.setParam(22, obj.docAllowSuperEdit);
        q.setParam(23, obj.docAllowLeadAsSuper);
        q.setParam(24, obj.docAskForPdf);
        q.setParam(25, obj.generateCodes);
        q.setParam(26, obj.showStandard);
        q.setParam(27, obj.respons);
        q.setParam(28, obj.showPnlMails);
        q.setParam(29, obj.changeTypeDoc);
        q.setParam(30, obj.indCutMonth);
        q.setParam(31, obj.showEntName);
        q.setParam(32, obj.leftLblMap);
        q.setParam(33, obj.rightLblMap);
        q.setParam(34, obj.shAproBy);
        q.setParam(35, obj.upLblMap);
        q.setParam(36, obj.showLblMap);
        q.setParam(37, obj.sendMailManager);
        q.setParam(38, obj.sendMailProcemp);
        q.setParam(39, obj.showAccess);
        q.setParam(40, obj.approvId);
        q.setParam(41, obj.showTypeSource);
        q.setParam(42, obj.selSourceAction);
        q.setParam(43, obj.showActDesc);
        q.setParam(44, obj.allowsCosts);
        q.setParam(45, obj.showFooter);
        q.setParam(46, obj.revEditDocs);
        q.setParam(47, obj.aproEditDocs);
        q.setParam(48, obj.checkNumDocs);
        q.setParam(49, obj.validateIndInputDate);
        q.setParam(50, obj.showCodsExcel);
        q.setParam(51, obj.extraFieldsExtDoc);
        q.setParam(52, obj.showTempMod);

    }

    public static CalCfg getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        CalCfg obj = new CalCfg();
        obj.chiefId = MySQLQuery.getAsInteger(row[0]);
        obj.managerId = MySQLQuery.getAsString(row[1]);
        obj.remind1 = MySQLQuery.getAsInteger(row[2]);
        obj.remind2 = MySQLQuery.getAsInteger(row[3]);
        obj.remind3 = MySQLQuery.getAsInteger(row[4]);
        obj.remind4 = MySQLQuery.getAsInteger(row[5]);
        obj.daysBeforeMeet = MySQLQuery.getAsInteger(row[6]);
        obj.daysBeforeAgreement = MySQLQuery.getAsInteger(row[7]);
        obj.daysBeforeActivity = MySQLQuery.getAsInteger(row[8]);
        obj.daysBeforeAudit = MySQLQuery.getAsInteger(row[9]);
        obj.indDayLimit = MySQLQuery.getAsInteger(row[10]);
        obj.auditDay = MySQLQuery.getAsInteger(row[11]);
        obj.showImageMap = MySQLQuery.getAsBoolean(row[12]);
        obj.showNews = MySQLQuery.getAsBoolean(row[13]);
        obj.showBsc = MySQLQuery.getAsBoolean(row[14]);
        obj.showFollow = MySQLQuery.getAsBoolean(row[15]);
        obj.showRisks = MySQLQuery.getAsBoolean(row[16]);
        obj.docRoot = MySQLQuery.getAsString(row[17]);
        obj.docRootPrefix = MySQLQuery.getAsString(row[18]);
        obj.docMacroPrefix = MySQLQuery.getAsString(row[19]);
        obj.docProcPrefix = MySQLQuery.getAsString(row[20]);
        obj.docAllowSuperEdit = MySQLQuery.getAsBoolean(row[21]);
        obj.docAllowLeadAsSuper = MySQLQuery.getAsBoolean(row[22]);
        obj.docAskForPdf = MySQLQuery.getAsBoolean(row[23]);
        obj.generateCodes = MySQLQuery.getAsBoolean(row[24]);
        obj.showStandard = MySQLQuery.getAsBoolean(row[25]);
        obj.respons = MySQLQuery.getAsBoolean(row[26]);
        obj.showPnlMails = MySQLQuery.getAsBoolean(row[27]);
        obj.changeTypeDoc = MySQLQuery.getAsBoolean(row[28]);
        obj.indCutMonth = MySQLQuery.getAsInteger(row[29]);
        obj.showEntName = MySQLQuery.getAsBoolean(row[30]);
        obj.leftLblMap = MySQLQuery.getAsString(row[31]);
        obj.rightLblMap = MySQLQuery.getAsString(row[32]);
        obj.shAproBy = MySQLQuery.getAsBoolean(row[33]);
        obj.upLblMap = MySQLQuery.getAsString(row[34]);
        obj.showLblMap = MySQLQuery.getAsBoolean(row[35]);
        obj.sendMailManager = MySQLQuery.getAsBoolean(row[36]);
        obj.sendMailProcemp = MySQLQuery.getAsBoolean(row[37]);
        obj.showAccess = MySQLQuery.getAsBoolean(row[38]);
        obj.approvId = MySQLQuery.getAsInteger(row[39]);
        obj.showTypeSource = MySQLQuery.getAsBoolean(row[40]);
        obj.selSourceAction = MySQLQuery.getAsBoolean(row[41]);
        obj.showActDesc = MySQLQuery.getAsBoolean(row[42]);
        obj.allowsCosts = MySQLQuery.getAsBoolean(row[43]);
        obj.showFooter = MySQLQuery.getAsBoolean(row[44]);
        obj.revEditDocs = MySQLQuery.getAsBoolean(row[45]);
        obj.aproEditDocs = MySQLQuery.getAsBoolean(row[46]);
        obj.checkNumDocs = MySQLQuery.getAsBoolean(row[47]);
        obj.validateIndInputDate = MySQLQuery.getAsBoolean(row[48]);
        obj.showCodsExcel = MySQLQuery.getAsBoolean(row[49]);
        obj.extraFieldsExtDoc = MySQLQuery.getAsBoolean(row[50]);
        obj.showTempMod = MySQLQuery.getAsBoolean(row[51]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public static CalCfg getCfg(Connection ep) throws Exception {
        return getFromRow(new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM cal_cfg WHERE id = 1").getRecord(ep));
    }
}
