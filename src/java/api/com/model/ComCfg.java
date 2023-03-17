package api.com.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class ComCfg extends BaseModel<ComCfg> {
//inicio zona de reemplazo

    public Float minShortD;
    public Float minMedD;
    public Float minLongD;
    public Integer minShortT;
    public Integer minMedT;
    public Integer minLongT;
    public BigDecimal movLimitKmh;
    public String geocoderKey;
    public boolean hasPnlCalib;
    public boolean rotationInApp;
    public boolean caseHelpDesk;
    public BigDecimal kteLatLon;
    public boolean workSubsidies;
    public boolean chkSub;
    public boolean chkFull;
    public boolean chkMul;
    public boolean chkNorm;
    public boolean chkStore;
    public boolean ctrlGeofence;
    public boolean ctrlMapLatLon;
    public boolean subsidy;
    public boolean full;
    public boolean multi;
    public boolean pv;
    public boolean summary;
    public boolean scanLoad;
    public boolean ordering;
    public int coordsInOrder;
    public int coordsNorm;
    public boolean priceFromBbl;
    public boolean summDayly;
    public boolean getPhone;
    public boolean pqrCylRelation;
    public boolean showQuestApp;
    public boolean nameDocSearch;
    public boolean lockCylSale;
    public boolean scanNifRec;
    public Integer offlineTime;
    public Integer docPicRate;
    public boolean bonusField;
    public boolean photoNifRec;
    public boolean chkDelCylLoad;
    public Integer delCylLoadLimitMins;
    public boolean phantomNif;
    public boolean chkPlatfBeforeSale;
    public boolean sigmaValidations;
    public int multiPromoValidity;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "min_short_d",
            "min_med_d",
            "min_long_d",
            "min_short_t",
            "min_med_t",
            "min_long_t",
            "mov_limit_kmh",
            "geocoder_key",
            "has_pnl_calib",
            "rotation_in_app",
            "case_help_desk",
            "kte_lat_lon",
            "work_subsidies",
            "chk_sub",
            "chk_full",
            "chk_mul",
            "chk_norm",
            "chk_store",
            "ctrl_geofence",
            "ctrl_map_lat_lon",
            "subsidy",
            "full",
            "multi",
            "pv",
            "summary",
            "scan_load",
            "ordering",
            "coords_in_order",
            "coords_norm",
            "price_from_bbl",
            "summ_dayly",
            "get_phone",
            "pqr_cyl_relation",
            "show_quest_app",
            "name_doc_search",
            "lock_cyl_sale",
            "scan_nif_rec",
            "offline_time",
            "doc_pic_rate",
            "bonus_field",
            "photo_nif_rec",
            "chk_del_cyl_load",
            "del_cyl_load_limit_mins",
            "phantom_nif",
            "chk_platf_before_sale",
            "sigma_validations",
            "multi_promo_validity"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, minShortD);
        q.setParam(2, minMedD);
        q.setParam(3, minLongD);
        q.setParam(4, minShortT);
        q.setParam(5, minMedT);
        q.setParam(6, minLongT);
        q.setParam(7, movLimitKmh);
        q.setParam(8, geocoderKey);
        q.setParam(9, hasPnlCalib);
        q.setParam(10, rotationInApp);
        q.setParam(11, caseHelpDesk);
        q.setParam(12, kteLatLon);
        q.setParam(13, workSubsidies);
        q.setParam(14, chkSub);
        q.setParam(15, chkFull);
        q.setParam(16, chkMul);
        q.setParam(17, chkNorm);
        q.setParam(18, chkStore);
        q.setParam(19, ctrlGeofence);
        q.setParam(20, ctrlMapLatLon);
        q.setParam(21, subsidy);
        q.setParam(22, full);
        q.setParam(23, multi);
        q.setParam(24, pv);
        q.setParam(25, summary);
        q.setParam(26, scanLoad);
        q.setParam(27, ordering);
        q.setParam(28, coordsInOrder);
        q.setParam(29, coordsNorm);
        q.setParam(30, priceFromBbl);
        q.setParam(31, summDayly);
        q.setParam(32, getPhone);
        q.setParam(33, pqrCylRelation);
        q.setParam(34, showQuestApp);
        q.setParam(35, nameDocSearch);
        q.setParam(36, lockCylSale);
        q.setParam(37, scanNifRec);
        q.setParam(38, offlineTime);
        q.setParam(39, docPicRate);
        q.setParam(40, bonusField);
        q.setParam(41, photoNifRec);
        q.setParam(42, chkDelCylLoad);
        q.setParam(43, delCylLoadLimitMins);
        q.setParam(44, phantomNif);
        q.setParam(45, chkPlatfBeforeSale);
        q.setParam(46, sigmaValidations);
        q.setParam(47, multiPromoValidity);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        minShortD = MySQLQuery.getAsFloat(row[0]);
        minMedD = MySQLQuery.getAsFloat(row[1]);
        minLongD = MySQLQuery.getAsFloat(row[2]);
        minShortT = MySQLQuery.getAsInteger(row[3]);
        minMedT = MySQLQuery.getAsInteger(row[4]);
        minLongT = MySQLQuery.getAsInteger(row[5]);
        movLimitKmh = MySQLQuery.getAsBigDecimal(row[6], false);
        geocoderKey = MySQLQuery.getAsString(row[7]);
        hasPnlCalib = MySQLQuery.getAsBoolean(row[8]);
        rotationInApp = MySQLQuery.getAsBoolean(row[9]);
        caseHelpDesk = MySQLQuery.getAsBoolean(row[10]);
        kteLatLon = MySQLQuery.getAsBigDecimal(row[11], false);
        workSubsidies = MySQLQuery.getAsBoolean(row[12]);
        chkSub = MySQLQuery.getAsBoolean(row[13]);
        chkFull = MySQLQuery.getAsBoolean(row[14]);
        chkMul = MySQLQuery.getAsBoolean(row[15]);
        chkNorm = MySQLQuery.getAsBoolean(row[16]);
        chkStore = MySQLQuery.getAsBoolean(row[17]);
        ctrlGeofence = MySQLQuery.getAsBoolean(row[18]);
        ctrlMapLatLon = MySQLQuery.getAsBoolean(row[19]);
        subsidy = MySQLQuery.getAsBoolean(row[20]);
        full = MySQLQuery.getAsBoolean(row[21]);
        multi = MySQLQuery.getAsBoolean(row[22]);
        pv = MySQLQuery.getAsBoolean(row[23]);
        summary = MySQLQuery.getAsBoolean(row[24]);
        scanLoad = MySQLQuery.getAsBoolean(row[25]);
        ordering = MySQLQuery.getAsBoolean(row[26]);
        coordsInOrder = MySQLQuery.getAsInteger(row[27]);
        coordsNorm = MySQLQuery.getAsInteger(row[28]);
        priceFromBbl = MySQLQuery.getAsBoolean(row[29]);
        summDayly = MySQLQuery.getAsBoolean(row[30]);
        getPhone = MySQLQuery.getAsBoolean(row[31]);
        pqrCylRelation = MySQLQuery.getAsBoolean(row[32]);
        showQuestApp = MySQLQuery.getAsBoolean(row[33]);
        nameDocSearch = MySQLQuery.getAsBoolean(row[34]);
        lockCylSale = MySQLQuery.getAsBoolean(row[35]);
        scanNifRec = MySQLQuery.getAsBoolean(row[36]);
        offlineTime = MySQLQuery.getAsInteger(row[37]);
        docPicRate = MySQLQuery.getAsInteger(row[38]);
        bonusField = MySQLQuery.getAsBoolean(row[39]);
        photoNifRec = MySQLQuery.getAsBoolean(row[40]);
        chkDelCylLoad = MySQLQuery.getAsBoolean(row[41]);
        delCylLoadLimitMins = MySQLQuery.getAsInteger(row[42]);
        phantomNif = MySQLQuery.getAsBoolean(row[43]);
        chkPlatfBeforeSale = MySQLQuery.getAsBoolean(row[44]);
        sigmaValidations = MySQLQuery.getAsBoolean(row[45]);
        multiPromoValidity = MySQLQuery.getAsInteger(row[46]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "com_cfg";
    }

    public static String getSelFlds(String alias) {
        return new ComCfg().getSelFldsForAlias(alias);
    }

    public static List<ComCfg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new ComCfg().getListFromQuery(q, conn);
    }

    public static List<ComCfg> getList(Params p, Connection conn) throws Exception {
        return new ComCfg().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new ComCfg().deleteById(id, conn);
    }

    public static List<ComCfg> getAll(Connection conn) throws Exception {
        return new ComCfg().getAllList(conn);
    }

//fin zona de reemplazo

}
