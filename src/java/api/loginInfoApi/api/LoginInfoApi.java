package api.loginInfoApi.api;

import api.BaseAPI;
import api.com.model.ComAppQuestion;
import api.com.model.ComSalesAppProfCfg;
import api.loginInfoApi.model.tracking.CenterPermission;
import api.loginInfoApi.model.tracking.Cfg;
import api.loginInfoApi.model.tracking.ChkQuestion;
import api.loginInfoApi.model.tracking.TrackingLoginInfo;
import api.loginInfoApi.model.tracking.TreatmentItem;
import api.inv.model.InvFacCode;
import api.loginInfoApi.model.sales.GpsConfig;
import api.loginInfoApi.model.sales.SalesLoginInfo;
import api.mss.app.MssAppProfCfg;
import api.mss.app.MssLoginInfo;
import api.trk.model.CylinderType;
import java.sql.Connection;
import java.util.ArrayList;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;

@Path("/loginInfoApi")
public class LoginInfoApi extends BaseAPI {

    @GET
    @Path("/trackingLoginInfo")
    public Response getTrackingLoginInfo(@QueryParam("empId") int empId) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            TrackingLoginInfo loginInfo = new TrackingLoginInfo();
            Object[] dataCfg = new MySQLQuery("SELECT c.show_label_lbs, c.insert_nif_app, c.kte_quality_test, chk_prefill FROM inv_cfg c").getRecord(conn);
            loginInfo.cfg = new Cfg();

            loginInfo.cfg.showLabelLb = MySQLQuery.getAsBoolean(dataCfg[0]);
            loginInfo.cfg.insertNif = MySQLQuery.getAsBoolean(dataCfg[1]);
            loginInfo.cfg.kte = MySQLQuery.getAsBigDecimal(dataCfg[2], false);
            loginInfo.cfg.chkPrefill = MySQLQuery.getAsBoolean(dataCfg[3]);

            loginInfo.centerPerms = new ArrayList<>();
            CenterPermission[] cp = CenterPermission.getByEmployeeId(empId, conn);
            for (int i = 0; i < cp.length; i++) {
                loginInfo.centerPerms.add(cp[i]);
            }

            loginInfo.cfg.facCodes = InvFacCode.getAll(conn);
            loginInfo.cfg.cylTypes = CylinderType.getAll(conn);
            loginInfo.nameEnterprise = new MySQLQuery("SELECT `name` FROM enterprise WHERE !alternative ").getAsString(conn);
            if (loginInfo.nameEnterprise == null) {
                loginInfo.nameEnterprise = "SIGMA";
            }
            if (loginInfo.cfg.chkPrefill) {
                Object[][] questData = new MySQLQuery("SELECT q.id, q.name, q.initial_clasify, q.on_process, q.final_product "
                        + "FROM trk_question q "
                        + "INNER JOIN trk_check_version v ON q.check_version_id = v.id "
                        + "WHERE v.active "
                        + "ORDER BY q.place ASC").getRecords(conn);
                loginInfo.cfg.lstQuest = new ArrayList<>();
                for (int i = 0; i < questData.length; i++) {
                    loginInfo.cfg.lstQuest.add(new ChkQuestion(questData[i]));
                }

                Object[][] itemData = new MySQLQuery("SELECT id, name, initial_clasify, on_process, final_product FROM trk_treat_item WHERE active ORDER BY name ASC").getRecords(conn);
                loginInfo.cfg.lstTreatItems = new ArrayList<>();
                for (int i = 0; i < itemData.length; i++) {
                    loginInfo.cfg.lstTreatItems.add(new TreatmentItem(itemData[i]));
                }
            }

            return createResponse(loginInfo);
        } catch (Exception e) {
            return createResponse(e);
        }
    }

    @GET
    @Path("/salesLogininfo")
    public Response getSalesLoginInfo(@QueryParam("pkgName") String pkgName, @QueryParam("empId") int empId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            SalesLoginInfo li = new SalesLoginInfo();

            Object[] appCfgData = new MySQLQuery("SELECT "
                    + "doc_pic_rate, "//0
                    + "phantom_nif, "//1
                    + "offline_time, "//2
                    + "lock_cyl_sale, "//3
                    + "price_from_bbl, "//4
                    + "sigma_validations "//5
                    + "FROM com_cfg").getRecord(conn);

            li.docPicRate = appCfgData[0] != null ? MySQLQuery.getAsDouble(MySQLQuery.getAsInteger(appCfgData[0]) / 100d) : 0d;
            li.phantomNif = MySQLQuery.getAsBoolean(appCfgData[1]);
            li.offlineTime = MySQLQuery.getAsInteger(appCfgData[2]);
            li.lockCylSale = MySQLQuery.getAsBoolean(appCfgData[3]);
            li.getFromBiable = MySQLQuery.getAsBoolean(appCfgData[4]);
            li.sigmaValidations = MySQLQuery.getAsBoolean(appCfgData[5]);

            Object[][] cfgData = new MySQLQuery("SELECT " + ComSalesAppProfCfg.getSelFlds("c") + ", pr.name "
                    + "FROM com_sales_app_prof_cfg c "
                    + "INNER JOIN sys_app_profile_emp p ON p.app_profile_id = c.prof_id "
                    + "INNER JOIN sys_app_profile pr ON p.app_profile_id = pr.id "
                    + "WHERE p.emp_id = " + empId).getRecords(conn);

            li.profiles = new ArrayList<>();
            for (int i = 0; i < cfgData.length; i++) {
                li.profiles.add(new ComSalesAppProfCfg().getFromRow(cfgData[i]));
            }

            if (li.profiles.isEmpty()) {
                throw new Exception("Usted no tiene perfil asignado. Comuníquese con Sistemas");
            }

            for (int i = 0; i < li.profiles.size(); i++) {
                ComSalesAppProfCfg item = li.profiles.get(i);
                item.subsidy = item.subsidy && pkgName.equals("com.glp.subsidiosonline");

                if (item.ordering) {
                    li.plate = new MySQLQuery("SELECT plate FROM driver_vehicle dv INNER JOIN vehicle v ON v.id = dv.vehicle_id WHERE dv.`end` IS NULL AND dv.driver_id = " + empId).getAsString(conn);//7
                }

                if (item.showQuestion) {
                    li.lstQuestion = ComAppQuestion.getList(new MySQLQuery("SELECT " + ComAppQuestion.getSelFlds("q") + " FROM com_app_question q WHERE q.`type` = 'cyl' AND active = 1 ORDER BY q.place"), conn);//9
                }
            }

            li.entMinasId = new MySQLQuery("SELECT enterprise_id FROM dto_cfg").getAsString(conn);//1
            Object[] salesman = new MySQLQuery("SELECT COUNT(*) > 0 FROM dto_salesman WHERE document = (SELECT document FROM employee WHERE id = " + empId + ") AND active").getRecord(conn);
            if (salesman == null || (salesman.length == 0 || !MySQLQuery.getAsBoolean(salesman[0]))) {
                throw new Exception("No es un vendedor registrado.");
            }
            li.giveSubs = MySQLQuery.getAsBoolean(salesman[0]);//2

            Object[][] smanData = new MySQLQuery("SELECT training, minas_pass, as_exp, scan_load, exp_only_own_cyls, id FROM dto_salesman WHERE active = 1 AND document = (SELECT document FROM employee WHERE id = " + empId + ")").getRecords(conn);//3
            li.inTraining = MySQLQuery.getAsBoolean(smanData[0][0]);
            li.minasPass = MySQLQuery.getAsString(smanData[0][1]);
            li.isExp = MySQLQuery.getAsBoolean(smanData[0][2]);
            li.scanLoad = MySQLQuery.getAsBoolean(smanData[0][3]);
            li.expOnlyOwnCyls = MySQLQuery.getAsBoolean(smanData[0][4]);
            li.smanId = MySQLQuery.getAsInteger(smanData[0][5]);

            Object[] gpsCfg = new MySQLQuery("SELECT `min_short_d`,`min_med_d`,`min_long_d`,`min_short_t`,`min_med_t`,`min_long_t`,`mov_limit_kmh` FROM com_cfg WHERE id = 1").getRecord(conn);//4
            if (gpsCfg == null || gpsCfg.length == 0) {
                throw new Exception("No está definida la configuración del gps.");
            }
            li.gpsConfig = new GpsConfig();
            li.gpsConfig.shortDist = MySQLQuery.getAsDouble(gpsCfg[0]);
            li.gpsConfig.medDist = MySQLQuery.getAsDouble(gpsCfg[1]);
            li.gpsConfig.longDist = MySQLQuery.getAsDouble(gpsCfg[2]);
            li.gpsConfig.shortTime = MySQLQuery.getAsLong(gpsCfg[3]);
            li.gpsConfig.medTime = MySQLQuery.getAsLong(gpsCfg[4]);
            li.gpsConfig.longTime = MySQLQuery.getAsLong(gpsCfg[5]);
            li.gpsConfig.movLimitKMH = MySQLQuery.getAsDouble(gpsCfg[6]);

            li.salesApp = new MySQLQuery("SELECT sales_app FROM ord_cfg WHERE id = 1").getAsBoolean(conn);//5
            li.cylTypes = CylinderType.getList(new MySQLQuery("SELECT " + CylinderType.getSelFlds("capa") + " FROM inv_type_capa AS mm "
                    + "INNER JOIN inv_cyl_type AS type ON mm.type_id = type.id "
                    + "INNER JOIN cylinder_type AS capa ON mm.capa_id = capa.id "
                    + "WHERE brand "
                    + "ORDER BY capa.place ASC"), conn); //6

            li.lastLogin = new MySQLQuery("SELECT s.begin_time  FROM session_login s WHERE s.employee_id = " + empId + " AND s.`type`= 'android' AND s.app_id = (SELECT id FROM system_app WHERE package_name = 'com.glp.subsidiosonline') ORDER BY  s.id DESC limit 1").getAsString(conn);//10
            li.serverTimeZone = new MySQLQuery("SELECT EXTRACT(HOUR FROM (TIMEDIFF(NOW(), UTC_TIMESTAMP)))").getAsInteger(conn);//11

            return createResponse(li);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/minutaLoginInfo")
    public Response getMinutaLoginInfo(@QueryParam("empId") int empId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MssLoginInfo li = new MssLoginInfo();

            MySQLQuery mq = new MySQLQuery("SELECT " + MssAppProfCfg.getSelFlds("c") + ", pr.name "
                    + "FROM mss_app_prof_cfg c "
                    + "INNER JOIN sys_app_profile_emp p ON p.app_profile_id = c.prof_id "
                    + "INNER JOIN sys_app_profile pr ON p.app_profile_id = pr.id "
                    + "WHERE p.emp_id = ?1").setParam(1, empId);

            li.profiles = new ArrayList<>();
            li.profiles = MssAppProfCfg.getList(mq, conn);

            if (li.profiles.isEmpty()) {
                throw new Exception("Usted no tiene perfil asignado. Comuníquese con Sistemas");
            }

            Object[] gpsCfg = new MySQLQuery("SELECT `min_short_d`,`min_med_d`,`min_long_d`,`min_short_t`,`min_med_t`,`min_long_t`,`mov_limit_kmh` FROM com_cfg WHERE id = 1").getRecord(conn);//4
            if (gpsCfg == null || gpsCfg.length == 0) {
                throw new Exception("No está definida la configuración del gps.");
            }
            li.gpsConfig = new GpsConfig();
            li.gpsConfig.shortDist = MySQLQuery.getAsDouble(gpsCfg[0]);
            li.gpsConfig.medDist = MySQLQuery.getAsDouble(gpsCfg[1]);
            li.gpsConfig.longDist = MySQLQuery.getAsDouble(gpsCfg[2]);
            li.gpsConfig.shortTime = MySQLQuery.getAsLong(gpsCfg[3]);
            li.gpsConfig.medTime = MySQLQuery.getAsLong(gpsCfg[4]);
            li.gpsConfig.longTime = MySQLQuery.getAsLong(gpsCfg[5]);
            li.gpsConfig.movLimitKMH = MySQLQuery.getAsDouble(gpsCfg[6]);

            return createResponse(li);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
