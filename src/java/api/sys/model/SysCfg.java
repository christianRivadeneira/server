package api.sys.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class SysCfg extends BaseModel<SysCfg> {

//inicio zona de reemplazo

    public String appName;
    public String glassPath;
    public String dbUrl;
    public Integer passwordExpiresDays;
    public int orderingPanelsRefreshTime;
    public String filesPath;
    public String extractCmd;
    public Integer sign;
    public int pingInterval;
    public int pingLimit;
    public BigDecimal pingProb;
    public BigDecimal galToKgKte;
    public String allowedFiles;
    public int maxFileSizeKb;
    public boolean askdane;
    public Boolean passMayus;
    public Boolean passMinus;
    public Boolean passNum;
    public Boolean passEsp;
    public Integer maxUsrs;
    public String mobileWebPage;
    public Boolean mailActive;
    public String mailFrom;
    public Boolean mailAuth;
    public String mailUser;
    public String mailPasswd;
    public Boolean mailTls;
    public String smtpHost;
    public String smtpPort;
    public String mailSignatureUrl;
    public String mailAlertLogoUrl;
    public Boolean ssl;
    public Boolean chatActive;
    public Boolean trackReportsTimes;
    public Integer monthCoords;
    public Boolean helpDeskActive;
    public Boolean codeDaneDupl;
    public String customBillTemplate;
    public String customBillTemplateNoLogos;
    public BigDecimal buildingRadius;
    public boolean instanceActive;
    public int inspectionInterval;
    public int inspectionAlert;
    public boolean tracking;
    public boolean suspRecon;
    public BigDecimal suspValue;
    public boolean eqsToFleet;
    public boolean sendMtoMail;
    public boolean sendThMail;
    public boolean showIp;
    public String webPassword;
    public Integer idleMinutes;
    public boolean billAnticType;
    public String slowQueryLogPath;
    public boolean showApartment;
    public boolean skipZeros;
    public boolean skipMinCons;
    public boolean skipZeroBills;
    public boolean billGpsValidate;
    public boolean billUseCode;
    public Integer billMinRise;
    public boolean uniqueSession;
    public boolean restore;
    public boolean createExternalEmps;
    public String urlNewLauncher;
    public boolean showDistributor;
    public boolean showBi;
    public String classPdfHeader;
    public String classPdfHeaderBack;
    public String billPseLabel;
    public String tutosBaseDir;
    public String tutosExtVideoUrl;
    public String tutosExtDocUrl;
    public String jvmPath;
    public String sigmaPath;
    public String urlExeLauncher;
    public String timeZone;
    public boolean genEstBi;
    public boolean mailSaleTk;
    public boolean sysMinasTest;
    public int limDaysOptApp;
    public boolean flowExtrasMenu;
    public Boolean billMailActive;
    public String billMailFrom;
    public Boolean billMailAuth;
    public String billMailUser;
    public String billMailPasswd;
    public Boolean billMailTls;
    public Boolean billMailSsl;
    public String billSmtpHost;
    public String billSmtpPort;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "app_name",
            "glass_path",
            "db_url",
            "password_expires_days",
            "ordering_panels_refresh_time",
            "files_path",
            "extract_cmd",
            "sign",
            "ping_interval",
            "ping_limit",
            "ping_prob",
            "gal_to_kg_kte",
            "allowed_files",
            "max_file_size_kb",
            "askdane",
            "pass_mayus",
            "pass_minus",
            "pass_num",
            "pass_esp",
            "max_usrs",
            "mobile_web_page",
            "mail_active",
            "mail_from",
            "mail_auth",
            "mail_user",
            "mail_passwd",
            "mail_tls",
            "smtp_host",
            "smtp_port",
            "mail_signature_url",
            "mail_alert_logo_url",
            "ssl",
            "chat_active",
            "track_reports_times",
            "month_coords",
            "help_desk_active",
            "code_dane_dupl",
            "custom_bill_template",
            "custom_bill_template_no_logos",
            "building_radius",
            "instance_active",
            "inspection_interval",
            "inspection_alert",
            "tracking",
            "susp_recon",
            "susp_value",
            "eqs_to_fleet",
            "send_mto_mail",
            "send_th_mail",
            "show_ip",
            "web_password",
            "idle_minutes",
            "bill_antic_type",
            "slow_query_log_path",
            "show_apartment",
            "skip_zeros",
            "skip_min_cons",
            "skip_zero_bills",
            "bill_gps_validate",
            "bill_use_code",
            "bill_min_rise",
            "unique_session",
            "restore",
            "create_external_emps",
            "url_new_launcher",
            "show_distributor",
            "show_bi",
            "class_pdf_header",
            "class_pdf_header_back",
            "bill_pse_label",
            "tutos_base_dir",
            "tutos_ext_video_url",
            "tutos_ext_doc_url",
            "jvm_path",
            "sigma_path",
            "url_exe_launcher",
            "time_zone",
            "gen_est_bi",
            "mail_sale_tk",
            "sys_minas_test",
            "lim_days_opt_app",
            "flow_extras_menu",
            "bill_mail_active",
            "bill_mail_from",
            "bill_mail_auth",
            "bill_mail_user",
            "bill_mail_passwd",
            "bill_mail_tls",
            "bill_mail_ssl",
            "bill_smtp_host",
            "bill_smtp_port"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, appName);
        q.setParam(2, glassPath);
        q.setParam(3, dbUrl);
        q.setParam(4, passwordExpiresDays);
        q.setParam(5, orderingPanelsRefreshTime);
        q.setParam(6, filesPath);
        q.setParam(7, extractCmd);
        q.setParam(8, sign);
        q.setParam(9, pingInterval);
        q.setParam(10, pingLimit);
        q.setParam(11, pingProb);
        q.setParam(12, galToKgKte);
        q.setParam(13, allowedFiles);
        q.setParam(14, maxFileSizeKb);
        q.setParam(15, askdane);
        q.setParam(16, passMayus);
        q.setParam(17, passMinus);
        q.setParam(18, passNum);
        q.setParam(19, passEsp);
        q.setParam(20, maxUsrs);
        q.setParam(21, mobileWebPage);
        q.setParam(22, mailActive);
        q.setParam(23, mailFrom);
        q.setParam(24, mailAuth);
        q.setParam(25, mailUser);
        q.setParam(26, mailPasswd);
        q.setParam(27, mailTls);
        q.setParam(28, smtpHost);
        q.setParam(29, smtpPort);
        q.setParam(30, mailSignatureUrl);
        q.setParam(31, mailAlertLogoUrl);
        q.setParam(32, ssl);
        q.setParam(33, chatActive);
        q.setParam(34, trackReportsTimes);
        q.setParam(35, monthCoords);
        q.setParam(36, helpDeskActive);
        q.setParam(37, codeDaneDupl);
        q.setParam(38, customBillTemplate);
        q.setParam(39, customBillTemplateNoLogos);
        q.setParam(40, buildingRadius);
        q.setParam(41, instanceActive);
        q.setParam(42, inspectionInterval);
        q.setParam(43, inspectionAlert);
        q.setParam(44, tracking);
        q.setParam(45, suspRecon);
        q.setParam(46, suspValue);
        q.setParam(47, eqsToFleet);
        q.setParam(48, sendMtoMail);
        q.setParam(49, sendThMail);
        q.setParam(50, showIp);
        q.setParam(51, webPassword);
        q.setParam(52, idleMinutes);
        q.setParam(53, billAnticType);
        q.setParam(54, slowQueryLogPath);
        q.setParam(55, showApartment);
        q.setParam(56, skipZeros);
        q.setParam(57, skipMinCons);
        q.setParam(58, skipZeroBills);
        q.setParam(59, billGpsValidate);
        q.setParam(60, billUseCode);
        q.setParam(61, billMinRise);
        q.setParam(62, uniqueSession);
        q.setParam(63, restore);
        q.setParam(64, createExternalEmps);
        q.setParam(65, urlNewLauncher);
        q.setParam(66, showDistributor);
        q.setParam(67, showBi);
        q.setParam(68, classPdfHeader);
        q.setParam(69, classPdfHeaderBack);
        q.setParam(70, billPseLabel);
        q.setParam(71, tutosBaseDir);
        q.setParam(72, tutosExtVideoUrl);
        q.setParam(73, tutosExtDocUrl);
        q.setParam(74, jvmPath);
        q.setParam(75, sigmaPath);
        q.setParam(76, urlExeLauncher);
        q.setParam(77, timeZone);
        q.setParam(78, genEstBi);
        q.setParam(79, mailSaleTk);
        q.setParam(80, sysMinasTest);
        q.setParam(81, limDaysOptApp);
        q.setParam(82, flowExtrasMenu);
        q.setParam(83, billMailActive);
        q.setParam(84, billMailFrom);
        q.setParam(85, billMailAuth);
        q.setParam(86, billMailUser);
        q.setParam(87, billMailPasswd);
        q.setParam(88, billMailTls);
        q.setParam(89, billMailSsl);
        q.setParam(90, billSmtpHost);
        q.setParam(91, billSmtpPort);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        appName = MySQLQuery.getAsString(row[0]);
        glassPath = MySQLQuery.getAsString(row[1]);
        dbUrl = MySQLQuery.getAsString(row[2]);
        passwordExpiresDays = MySQLQuery.getAsInteger(row[3]);
        orderingPanelsRefreshTime = MySQLQuery.getAsInteger(row[4]);
        filesPath = MySQLQuery.getAsString(row[5]);
        extractCmd = MySQLQuery.getAsString(row[6]);
        sign = MySQLQuery.getAsInteger(row[7]);
        pingInterval = MySQLQuery.getAsInteger(row[8]);
        pingLimit = MySQLQuery.getAsInteger(row[9]);
        pingProb = MySQLQuery.getAsBigDecimal(row[10], false);
        galToKgKte = MySQLQuery.getAsBigDecimal(row[11], false);
        allowedFiles = MySQLQuery.getAsString(row[12]);
        maxFileSizeKb = MySQLQuery.getAsInteger(row[13]);
        askdane = MySQLQuery.getAsBoolean(row[14]);
        passMayus = MySQLQuery.getAsBoolean(row[15]);
        passMinus = MySQLQuery.getAsBoolean(row[16]);
        passNum = MySQLQuery.getAsBoolean(row[17]);
        passEsp = MySQLQuery.getAsBoolean(row[18]);
        maxUsrs = MySQLQuery.getAsInteger(row[19]);
        mobileWebPage = MySQLQuery.getAsString(row[20]);
        mailActive = MySQLQuery.getAsBoolean(row[21]);
        mailFrom = MySQLQuery.getAsString(row[22]);
        mailAuth = MySQLQuery.getAsBoolean(row[23]);
        mailUser = MySQLQuery.getAsString(row[24]);
        mailPasswd = MySQLQuery.getAsString(row[25]);
        mailTls = MySQLQuery.getAsBoolean(row[26]);
        smtpHost = MySQLQuery.getAsString(row[27]);
        smtpPort = MySQLQuery.getAsString(row[28]);
        mailSignatureUrl = MySQLQuery.getAsString(row[29]);
        mailAlertLogoUrl = MySQLQuery.getAsString(row[30]);
        ssl = MySQLQuery.getAsBoolean(row[31]);
        chatActive = MySQLQuery.getAsBoolean(row[32]);
        trackReportsTimes = MySQLQuery.getAsBoolean(row[33]);
        monthCoords = MySQLQuery.getAsInteger(row[34]);
        helpDeskActive = MySQLQuery.getAsBoolean(row[35]);
        codeDaneDupl = MySQLQuery.getAsBoolean(row[36]);
        customBillTemplate = MySQLQuery.getAsString(row[37]);
        customBillTemplateNoLogos = MySQLQuery.getAsString(row[38]);
        buildingRadius = MySQLQuery.getAsBigDecimal(row[39], false);
        instanceActive = MySQLQuery.getAsBoolean(row[40]);
        inspectionInterval = MySQLQuery.getAsInteger(row[41]);
        inspectionAlert = MySQLQuery.getAsInteger(row[42]);
        tracking = MySQLQuery.getAsBoolean(row[43]);
        suspRecon = MySQLQuery.getAsBoolean(row[44]);
        suspValue = MySQLQuery.getAsBigDecimal(row[45], false);
        eqsToFleet = MySQLQuery.getAsBoolean(row[46]);
        sendMtoMail = MySQLQuery.getAsBoolean(row[47]);
        sendThMail = MySQLQuery.getAsBoolean(row[48]);
        showIp = MySQLQuery.getAsBoolean(row[49]);
        webPassword = MySQLQuery.getAsString(row[50]);
        idleMinutes = MySQLQuery.getAsInteger(row[51]);
        billAnticType = MySQLQuery.getAsBoolean(row[52]);
        slowQueryLogPath = MySQLQuery.getAsString(row[53]);
        showApartment = MySQLQuery.getAsBoolean(row[54]);
        skipZeros = MySQLQuery.getAsBoolean(row[55]);
        skipMinCons = MySQLQuery.getAsBoolean(row[56]);
        skipZeroBills = MySQLQuery.getAsBoolean(row[57]);
        billGpsValidate = MySQLQuery.getAsBoolean(row[58]);
        billUseCode = MySQLQuery.getAsBoolean(row[59]);
        billMinRise = MySQLQuery.getAsInteger(row[60]);
        uniqueSession = MySQLQuery.getAsBoolean(row[61]);
        restore = MySQLQuery.getAsBoolean(row[62]);
        createExternalEmps = MySQLQuery.getAsBoolean(row[63]);
        urlNewLauncher = MySQLQuery.getAsString(row[64]);
        showDistributor = MySQLQuery.getAsBoolean(row[65]);
        showBi = MySQLQuery.getAsBoolean(row[66]);
        classPdfHeader = MySQLQuery.getAsString(row[67]);
        classPdfHeaderBack = MySQLQuery.getAsString(row[68]);
        billPseLabel = MySQLQuery.getAsString(row[69]);
        tutosBaseDir = MySQLQuery.getAsString(row[70]);
        tutosExtVideoUrl = MySQLQuery.getAsString(row[71]);
        tutosExtDocUrl = MySQLQuery.getAsString(row[72]);
        jvmPath = MySQLQuery.getAsString(row[73]);
        sigmaPath = MySQLQuery.getAsString(row[74]);
        urlExeLauncher = MySQLQuery.getAsString(row[75]);
        timeZone = MySQLQuery.getAsString(row[76]);
        genEstBi = MySQLQuery.getAsBoolean(row[77]);
        mailSaleTk = MySQLQuery.getAsBoolean(row[78]);
        sysMinasTest = MySQLQuery.getAsBoolean(row[79]);
        limDaysOptApp = MySQLQuery.getAsInteger(row[80]);
        flowExtrasMenu = MySQLQuery.getAsBoolean(row[81]);
        billMailActive = MySQLQuery.getAsBoolean(row[82]);
        billMailFrom = MySQLQuery.getAsString(row[83]);
        billMailAuth = MySQLQuery.getAsBoolean(row[84]);
        billMailUser = MySQLQuery.getAsString(row[85]);
        billMailPasswd = MySQLQuery.getAsString(row[86]);
        billMailTls = MySQLQuery.getAsBoolean(row[87]);
        billMailSsl = MySQLQuery.getAsBoolean(row[88]);
        billSmtpHost = MySQLQuery.getAsString(row[89]);
        billSmtpPort = MySQLQuery.getAsString(row[90]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "sys_cfg";
    }

    public static String getSelFlds(String alias) {
        return new SysCfg().getSelFldsForAlias(alias);
    }

    public static List<SysCfg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new SysCfg().getListFromQuery(q, conn);
    }

    public static List<SysCfg> getList(Params p, Connection conn) throws Exception {
        return new SysCfg().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new SysCfg().deleteById(id, conn);
    }

    public static List<SysCfg> getAll(Connection conn) throws Exception {
        return new SysCfg().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<SysCfg> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/
    public static SysCfg select(Connection conn) throws Exception {
        return new SysCfg().select(1, conn);
    }
}
