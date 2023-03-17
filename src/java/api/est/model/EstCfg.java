package api.est.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.math.BigDecimal;

public class EstCfg extends BaseModel<EstCfg> {
//inicio zona de reemplazo

    public int totalInterval;
    public int partialInterval;
    public int totalAlert;
    public int partialAlert;
    public boolean schedTask;
    public int execAlert;
    public int alertLevel;
    public BigDecimal kte;
    public int alertConsum;
    public BigDecimal minGlVal;
    public BigDecimal maxGlVal;
    public BigDecimal minTotal;
    public BigDecimal maxTotal;
    public int extAlertDays;
    public int maxNumBills;
    public BigDecimal minNetVal;
    public BigDecimal maxNetVal;
    public boolean accCityMandatory;
    public boolean onlyKgs;
    public boolean vhMandatory;
    public boolean listPriceMandatory;
    public boolean serialMandatory;
    public boolean internalMandatory;
    public boolean editSaleMobile;
    public boolean ideTankMandatory;
    public boolean multPrintsMob;
    public boolean tankMandatory;
    public boolean billPend;
    public String billPrefix;
    public boolean showPath;
    public boolean showPathRoute;
    public boolean showTransfer;
    public boolean showCertificate;
    public BigDecimal lowerGlTank;
    public int lowerNroExt;
    public int lowerLbsExt;
    public BigDecimal upperGlTank;
    public int upperNroExt;
    public int upperLbsExt;
    public BigDecimal fireSystemGl;
    public boolean showExts;
    public boolean autoUsersTank;
    public boolean internalSingleClie;
    public boolean hasContractor;
    public boolean validateAddress;
    public boolean serialSingleTank;
    public boolean editDtCreate;
    public boolean showSupervisor;
    public boolean editPhoneApp;
    public boolean showStratumNeigh;
    public boolean unifiedCenter;
    public boolean showUsersNumber;
    public boolean showQuestApp;
    public boolean contactFldsApp;
    public String resolution;
    public Boolean autoSort;
    public String mailFormatNotify;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "total_interval",
            "partial_interval",
            "total_alert",
            "partial_alert",
            "sched_task",
            "exec_alert",
            "alert_level",
            "kte",
            "alert_consum",
            "min_gl_val",
            "max_gl_val",
            "min_total",
            "max_total",
            "ext_alert_days",
            "max_num_bills",
            "min_net_val",
            "max_net_val",
            "acc_city_mandatory",
            "only_kgs",
            "vh_mandatory",
            "list_price_mandatory",
            "serial_mandatory",
            "internal_mandatory",
            "edit_sale_mobile",
            "ide_tank_mandatory",
            "mult_prints_mob",
            "tank_mandatory",
            "bill_pend",
            "bill_prefix",
            "show_path",
            "show_path_route",
            "show_transfer",
            "show_certificate",
            "lower_gl_tank",
            "lower_nro_ext",
            "lower_lbs_ext",
            "upper_gl_tank",
            "upper_nro_ext",
            "upper_lbs_ext",
            "fire_system_gl",
            "show_exts",
            "auto_users_tank",
            "internal_single_clie",
            "has_contractor",
            "validate_address",
            "serial_single_tank",
            "edit_dt_create",
            "show_supervisor",
            "edit_phone_app",
            "show_stratum_neigh",
            "unified_center",
            "show_users_number",
            "show_quest_app",
            "contact_flds_app",
            "resolution",
            "auto_sort",
            "mail_format_notify"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, totalInterval);
        q.setParam(2, partialInterval);
        q.setParam(3, totalAlert);
        q.setParam(4, partialAlert);
        q.setParam(5, schedTask);
        q.setParam(6, execAlert);
        q.setParam(7, alertLevel);
        q.setParam(8, kte);
        q.setParam(9, alertConsum);
        q.setParam(10, minGlVal);
        q.setParam(11, maxGlVal);
        q.setParam(12, minTotal);
        q.setParam(13, maxTotal);
        q.setParam(14, extAlertDays);
        q.setParam(15, maxNumBills);
        q.setParam(16, minNetVal);
        q.setParam(17, maxNetVal);
        q.setParam(18, accCityMandatory);
        q.setParam(19, onlyKgs);
        q.setParam(20, vhMandatory);
        q.setParam(21, listPriceMandatory);
        q.setParam(22, serialMandatory);
        q.setParam(23, internalMandatory);
        q.setParam(24, editSaleMobile);
        q.setParam(25, ideTankMandatory);
        q.setParam(26, multPrintsMob);
        q.setParam(27, tankMandatory);
        q.setParam(28, billPend);
        q.setParam(29, billPrefix);
        q.setParam(30, showPath);
        q.setParam(31, showPathRoute);
        q.setParam(32, showTransfer);
        q.setParam(33, showCertificate);
        q.setParam(34, lowerGlTank);
        q.setParam(35, lowerNroExt);
        q.setParam(36, lowerLbsExt);
        q.setParam(37, upperGlTank);
        q.setParam(38, upperNroExt);
        q.setParam(39, upperLbsExt);
        q.setParam(40, fireSystemGl);
        q.setParam(41, showExts);
        q.setParam(42, autoUsersTank);
        q.setParam(43, internalSingleClie);
        q.setParam(44, hasContractor);
        q.setParam(45, validateAddress);
        q.setParam(46, serialSingleTank);
        q.setParam(47, editDtCreate);
        q.setParam(48, showSupervisor);
        q.setParam(49, editPhoneApp);
        q.setParam(50, showStratumNeigh);
        q.setParam(51, unifiedCenter);
        q.setParam(52, showUsersNumber);
        q.setParam(53, showQuestApp);
        q.setParam(54, contactFldsApp);
        q.setParam(55, resolution);
        q.setParam(56, autoSort);
        q.setParam(57, mailFormatNotify);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        totalInterval = MySQLQuery.getAsInteger(row[0]);
        partialInterval = MySQLQuery.getAsInteger(row[1]);
        totalAlert = MySQLQuery.getAsInteger(row[2]);
        partialAlert = MySQLQuery.getAsInteger(row[3]);
        schedTask = MySQLQuery.getAsBoolean(row[4]);
        execAlert = MySQLQuery.getAsInteger(row[5]);
        alertLevel = MySQLQuery.getAsInteger(row[6]);
        kte = MySQLQuery.getAsBigDecimal(row[7], false);
        alertConsum = MySQLQuery.getAsInteger(row[8]);
        minGlVal = MySQLQuery.getAsBigDecimal(row[9], false);
        maxGlVal = MySQLQuery.getAsBigDecimal(row[10], false);
        minTotal = MySQLQuery.getAsBigDecimal(row[11], false);
        maxTotal = MySQLQuery.getAsBigDecimal(row[12], false);
        extAlertDays = MySQLQuery.getAsInteger(row[13]);
        maxNumBills = MySQLQuery.getAsInteger(row[14]);
        minNetVal = MySQLQuery.getAsBigDecimal(row[15], false);
        maxNetVal = MySQLQuery.getAsBigDecimal(row[16], false);
        accCityMandatory = MySQLQuery.getAsBoolean(row[17]);
        onlyKgs = MySQLQuery.getAsBoolean(row[18]);
        vhMandatory = MySQLQuery.getAsBoolean(row[19]);
        listPriceMandatory = MySQLQuery.getAsBoolean(row[20]);
        serialMandatory = MySQLQuery.getAsBoolean(row[21]);
        internalMandatory = MySQLQuery.getAsBoolean(row[22]);
        editSaleMobile = MySQLQuery.getAsBoolean(row[23]);
        ideTankMandatory = MySQLQuery.getAsBoolean(row[24]);
        multPrintsMob = MySQLQuery.getAsBoolean(row[25]);
        tankMandatory = MySQLQuery.getAsBoolean(row[26]);
        billPend = MySQLQuery.getAsBoolean(row[27]);
        billPrefix = MySQLQuery.getAsString(row[28]);
        showPath = MySQLQuery.getAsBoolean(row[29]);
        showPathRoute = MySQLQuery.getAsBoolean(row[30]);
        showTransfer = MySQLQuery.getAsBoolean(row[31]);
        showCertificate = MySQLQuery.getAsBoolean(row[32]);
        lowerGlTank = MySQLQuery.getAsBigDecimal(row[33], false);
        lowerNroExt = MySQLQuery.getAsInteger(row[34]);
        lowerLbsExt = MySQLQuery.getAsInteger(row[35]);
        upperGlTank = MySQLQuery.getAsBigDecimal(row[36], false);
        upperNroExt = MySQLQuery.getAsInteger(row[37]);
        upperLbsExt = MySQLQuery.getAsInteger(row[38]);
        fireSystemGl = MySQLQuery.getAsBigDecimal(row[39], false);
        showExts = MySQLQuery.getAsBoolean(row[40]);
        autoUsersTank = MySQLQuery.getAsBoolean(row[41]);
        internalSingleClie = MySQLQuery.getAsBoolean(row[42]);
        hasContractor = MySQLQuery.getAsBoolean(row[43]);
        validateAddress = MySQLQuery.getAsBoolean(row[44]);
        serialSingleTank = MySQLQuery.getAsBoolean(row[45]);
        editDtCreate = MySQLQuery.getAsBoolean(row[46]);
        showSupervisor = MySQLQuery.getAsBoolean(row[47]);
        editPhoneApp = MySQLQuery.getAsBoolean(row[48]);
        showStratumNeigh = MySQLQuery.getAsBoolean(row[49]);
        unifiedCenter = MySQLQuery.getAsBoolean(row[50]);
        showUsersNumber = MySQLQuery.getAsBoolean(row[51]);
        showQuestApp = MySQLQuery.getAsBoolean(row[52]);
        contactFldsApp = MySQLQuery.getAsBoolean(row[53]);
        resolution = MySQLQuery.getAsString(row[54]);
        autoSort = MySQLQuery.getAsBoolean(row[55]);
        mailFormatNotify = MySQLQuery.getAsString(row[56]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "est_cfg";
    }

    public static String getSelFlds(String alias) {
        return new EstCfg().getSelFldsForAlias(alias);
    }

    public static List<EstCfg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EstCfg().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EstCfg().deleteById(id, conn);
    }

    public static List<EstCfg> getAll(Connection conn) throws Exception {
        return new EstCfg().getAllList(conn);
    }

//fin zona de reemplazo
}