package api.mto.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class MtoCfg extends BaseModel<MtoCfg> {
//inicio zona de reemplazo

    public Integer permDays;
    public int alertDocs;
    public String mtoVehStr1;
    public String mtoVehStr2;
    public String mtoVehStr3;
    public boolean mtoMinorBox;
    public boolean trips;
    public boolean income;
    public boolean daily;
    public boolean pnlsearch;
    public boolean docsdrivers;
    public Integer mileage;
    public Integer weeks;
    public Integer hours;
    public boolean drivers;
    public int elementAlertDays;
    public Integer chkPeriodDays;
    public Integer chkAlertDays;
    public boolean pnlFormats;
    public boolean fastProvider;
    public String bossLstName;
    public String bossLstWork;
    public String bossLstNumberTp;
    public boolean exportAcc;
    public boolean activeSerial;
    public String accWoType;
    public String accFuelType;
    public String accWoCruceType;
    public String accFuelCruceType;
    public String accReteIvaText;
    public String accReteIvaAcc;
    public String accReteIcaText;
    public String accReteIcaAcc;
    public String accReteFueText;
    public String accReteFueAcc;
    public String accReteCxpText;
    public String accReteCxpAcc;
    public boolean gps;
    public Boolean restrictStations;
    public Boolean store;
    public String refPrice;
    public boolean mtoSupplies;
    public boolean mtoPurche;
    public boolean showCreditCheck;
    public boolean workOrderFlow;
    public String workOrderPdfClass;
    public boolean workOrderItemDetail;
    public boolean allProviders;
    public Integer respon;
    public boolean numDocScguno;
    public boolean filterOrderApprov;
    public boolean showForecast;
    public boolean salemanGoal;
    public boolean showPercCosts;
    public boolean mtoGps;
    public boolean mtoHrs;
    public boolean agencyHistory;
    public boolean mtoChkOrder;
    public boolean plateSpace;
    public boolean mandSignDriv;
    public boolean printAll;
    public String fuelImportPath;
    public Integer mtoManager;
    public boolean showSignMan;
    public boolean showMailFlows;
    public boolean printFromMobile;
    public boolean showVhTread;
    public boolean moduleIntegration;
    public boolean showIsTank;
    public Date begChkWorkAlert;
    public boolean showDocDriverAg;
    public boolean showBtnTaskPrev;
    public boolean showTypeItem;
    public boolean unifiedCenter;
    public boolean chkPeriodMonth;
    public boolean insertVehicle;
    public boolean signByAgencyEmp;
    public Integer routeChkTolerance;
    public Integer routeChkRadius;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "perm_days",
            "alert_docs",
            "mto_veh_str1",
            "mto_veh_str2",
            "mto_veh_str3",
            "mto_minor_box",
            "trips",
            "income",
            "daily",
            "pnlsearch",
            "docsdrivers",
            "mileage",
            "weeks",
            "hours",
            "drivers",
            "element_alert_days",
            "chk_period_days",
            "chk_alert_days",
            "pnl_formats",
            "fast_provider",
            "boss_lst_name",
            "boss_lst_work",
            "boss_lst_number_tp",
            "export_acc",
            "active_serial",
            "acc_wo_type",
            "acc_fuel_type",
            "acc_wo_cruce_type",
            "acc_fuel_cruce_type",
            "acc_rete_iva_text",
            "acc_rete_iva_acc",
            "acc_rete_ica_text",
            "acc_rete_ica_acc",
            "acc_rete_fue_text",
            "acc_rete_fue_acc",
            "acc_rete_cxp_text",
            "acc_rete_cxp_acc",
            "gps",
            "restrict_stations",
            "store",
            "ref_price",
            "mto_supplies",
            "mto_purche",
            "show_credit_check",
            "work_order_flow",
            "work_order_pdf_class",
            "work_order_item_detail",
            "all_providers",
            "respon",
            "num_doc_scguno",
            "filter_order_approv",
            "show_forecast",
            "saleman_goal",
            "show_perc_costs",
            "mto_gps",
            "mto_hrs",
            "agency_history",
            "mto_chk_order",
            "plate_space",
            "mand_sign_driv",
            "print_all",
            "fuel_import_path",
            "mto_manager",
            "show_sign_man",
            "show_mail_flows",
            "print_from_mobile",
            "show_vh_tread",
            "module_integration",
            "show_is_tank",
            "beg_chk_work_alert",
            "show_doc_driver_ag",
            "show_btn_task_prev",
            "show_type_item",
            "unified_center",
            "chk_period_month",
            "insert_vehicle",
            "sign_by_agency_emp",
            "route_chk_tolerance",
            "route_chk_radius"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, permDays);
        q.setParam(2, alertDocs);
        q.setParam(3, mtoVehStr1);
        q.setParam(4, mtoVehStr2);
        q.setParam(5, mtoVehStr3);
        q.setParam(6, mtoMinorBox);
        q.setParam(7, trips);
        q.setParam(8, income);
        q.setParam(9, daily);
        q.setParam(10, pnlsearch);
        q.setParam(11, docsdrivers);
        q.setParam(12, mileage);
        q.setParam(13, weeks);
        q.setParam(14, hours);
        q.setParam(15, drivers);
        q.setParam(16, elementAlertDays);
        q.setParam(17, chkPeriodDays);
        q.setParam(18, chkAlertDays);
        q.setParam(19, pnlFormats);
        q.setParam(20, fastProvider);
        q.setParam(21, bossLstName);
        q.setParam(22, bossLstWork);
        q.setParam(23, bossLstNumberTp);
        q.setParam(24, exportAcc);
        q.setParam(25, activeSerial);
        q.setParam(26, accWoType);
        q.setParam(27, accFuelType);
        q.setParam(28, accWoCruceType);
        q.setParam(29, accFuelCruceType);
        q.setParam(30, accReteIvaText);
        q.setParam(31, accReteIvaAcc);
        q.setParam(32, accReteIcaText);
        q.setParam(33, accReteIcaAcc);
        q.setParam(34, accReteFueText);
        q.setParam(35, accReteFueAcc);
        q.setParam(36, accReteCxpText);
        q.setParam(37, accReteCxpAcc);
        q.setParam(38, gps);
        q.setParam(39, restrictStations);
        q.setParam(40, store);
        q.setParam(41, refPrice);
        q.setParam(42, mtoSupplies);
        q.setParam(43, mtoPurche);
        q.setParam(44, showCreditCheck);
        q.setParam(45, workOrderFlow);
        q.setParam(46, workOrderPdfClass);
        q.setParam(47, workOrderItemDetail);
        q.setParam(48, allProviders);
        q.setParam(49, respon);
        q.setParam(50, numDocScguno);
        q.setParam(51, filterOrderApprov);
        q.setParam(52, showForecast);
        q.setParam(53, salemanGoal);
        q.setParam(54, showPercCosts);
        q.setParam(55, mtoGps);
        q.setParam(56, mtoHrs);
        q.setParam(57, agencyHistory);
        q.setParam(58, mtoChkOrder);
        q.setParam(59, plateSpace);
        q.setParam(60, mandSignDriv);
        q.setParam(61, printAll);
        q.setParam(62, fuelImportPath);
        q.setParam(63, mtoManager);
        q.setParam(64, showSignMan);
        q.setParam(65, showMailFlows);
        q.setParam(66, printFromMobile);
        q.setParam(67, showVhTread);
        q.setParam(68, moduleIntegration);
        q.setParam(69, showIsTank);
        q.setParam(70, begChkWorkAlert);
        q.setParam(71, showDocDriverAg);
        q.setParam(72, showBtnTaskPrev);
        q.setParam(73, showTypeItem);
        q.setParam(74, unifiedCenter);
        q.setParam(75, chkPeriodMonth);
        q.setParam(76, insertVehicle);
        q.setParam(77, signByAgencyEmp);
        q.setParam(78, routeChkTolerance);
        q.setParam(79, routeChkRadius);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        permDays = MySQLQuery.getAsInteger(row[0]);
        alertDocs = MySQLQuery.getAsInteger(row[1]);
        mtoVehStr1 = MySQLQuery.getAsString(row[2]);
        mtoVehStr2 = MySQLQuery.getAsString(row[3]);
        mtoVehStr3 = MySQLQuery.getAsString(row[4]);
        mtoMinorBox = MySQLQuery.getAsBoolean(row[5]);
        trips = MySQLQuery.getAsBoolean(row[6]);
        income = MySQLQuery.getAsBoolean(row[7]);
        daily = MySQLQuery.getAsBoolean(row[8]);
        pnlsearch = MySQLQuery.getAsBoolean(row[9]);
        docsdrivers = MySQLQuery.getAsBoolean(row[10]);
        mileage = MySQLQuery.getAsInteger(row[11]);
        weeks = MySQLQuery.getAsInteger(row[12]);
        hours = MySQLQuery.getAsInteger(row[13]);
        drivers = MySQLQuery.getAsBoolean(row[14]);
        elementAlertDays = MySQLQuery.getAsInteger(row[15]);
        chkPeriodDays = MySQLQuery.getAsInteger(row[16]);
        chkAlertDays = MySQLQuery.getAsInteger(row[17]);
        pnlFormats = MySQLQuery.getAsBoolean(row[18]);
        fastProvider = MySQLQuery.getAsBoolean(row[19]);
        bossLstName = MySQLQuery.getAsString(row[20]);
        bossLstWork = MySQLQuery.getAsString(row[21]);
        bossLstNumberTp = MySQLQuery.getAsString(row[22]);
        exportAcc = MySQLQuery.getAsBoolean(row[23]);
        activeSerial = MySQLQuery.getAsBoolean(row[24]);
        accWoType = MySQLQuery.getAsString(row[25]);
        accFuelType = MySQLQuery.getAsString(row[26]);
        accWoCruceType = MySQLQuery.getAsString(row[27]);
        accFuelCruceType = MySQLQuery.getAsString(row[28]);
        accReteIvaText = MySQLQuery.getAsString(row[29]);
        accReteIvaAcc = MySQLQuery.getAsString(row[30]);
        accReteIcaText = MySQLQuery.getAsString(row[31]);
        accReteIcaAcc = MySQLQuery.getAsString(row[32]);
        accReteFueText = MySQLQuery.getAsString(row[33]);
        accReteFueAcc = MySQLQuery.getAsString(row[34]);
        accReteCxpText = MySQLQuery.getAsString(row[35]);
        accReteCxpAcc = MySQLQuery.getAsString(row[36]);
        gps = MySQLQuery.getAsBoolean(row[37]);
        restrictStations = MySQLQuery.getAsBoolean(row[38]);
        store = MySQLQuery.getAsBoolean(row[39]);
        refPrice = MySQLQuery.getAsString(row[40]);
        mtoSupplies = MySQLQuery.getAsBoolean(row[41]);
        mtoPurche = MySQLQuery.getAsBoolean(row[42]);
        showCreditCheck = MySQLQuery.getAsBoolean(row[43]);
        workOrderFlow = MySQLQuery.getAsBoolean(row[44]);
        workOrderPdfClass = MySQLQuery.getAsString(row[45]);
        workOrderItemDetail = MySQLQuery.getAsBoolean(row[46]);
        allProviders = MySQLQuery.getAsBoolean(row[47]);
        respon = MySQLQuery.getAsInteger(row[48]);
        numDocScguno = MySQLQuery.getAsBoolean(row[49]);
        filterOrderApprov = MySQLQuery.getAsBoolean(row[50]);
        showForecast = MySQLQuery.getAsBoolean(row[51]);
        salemanGoal = MySQLQuery.getAsBoolean(row[52]);
        showPercCosts = MySQLQuery.getAsBoolean(row[53]);
        mtoGps = MySQLQuery.getAsBoolean(row[54]);
        mtoHrs = MySQLQuery.getAsBoolean(row[55]);
        agencyHistory = MySQLQuery.getAsBoolean(row[56]);
        mtoChkOrder = MySQLQuery.getAsBoolean(row[57]);
        plateSpace = MySQLQuery.getAsBoolean(row[58]);
        mandSignDriv = MySQLQuery.getAsBoolean(row[59]);
        printAll = MySQLQuery.getAsBoolean(row[60]);
        fuelImportPath = MySQLQuery.getAsString(row[61]);
        mtoManager = MySQLQuery.getAsInteger(row[62]);
        showSignMan = MySQLQuery.getAsBoolean(row[63]);
        showMailFlows = MySQLQuery.getAsBoolean(row[64]);
        printFromMobile = MySQLQuery.getAsBoolean(row[65]);
        showVhTread = MySQLQuery.getAsBoolean(row[66]);
        moduleIntegration = MySQLQuery.getAsBoolean(row[67]);
        showIsTank = MySQLQuery.getAsBoolean(row[68]);
        begChkWorkAlert = MySQLQuery.getAsDate(row[69]);
        showDocDriverAg = MySQLQuery.getAsBoolean(row[70]);
        showBtnTaskPrev = MySQLQuery.getAsBoolean(row[71]);
        showTypeItem = MySQLQuery.getAsBoolean(row[72]);
        unifiedCenter = MySQLQuery.getAsBoolean(row[73]);
        chkPeriodMonth = MySQLQuery.getAsBoolean(row[74]);
        insertVehicle = MySQLQuery.getAsBoolean(row[75]);
        signByAgencyEmp = MySQLQuery.getAsBoolean(row[76]);
        routeChkTolerance = MySQLQuery.getAsInteger(row[77]);
        routeChkRadius = MySQLQuery.getAsInteger(row[78]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mto_cfg";
    }

    public static String getSelFlds(String alias) {
        return new MtoCfg().getSelFldsForAlias(alias);
    }

    public static List<MtoCfg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MtoCfg().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MtoCfg().deleteById(id, conn);
    }

    public static List<MtoCfg> getAll(Connection conn) throws Exception {
        return new MtoCfg().getAllList(conn);
    }

//fin zona de reemplazo
}