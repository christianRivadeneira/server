package web.marketing.cylSales.promo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import utilities.MySQLQuery;

public class ComPromoTarget {

    public int id;
    public Integer zoneId;
    public Integer cityId;
    public Integer sectorId;
    public Integer cylTypeId;
    public String clieDoc;
    public Integer prefIndexId;
    public Integer establishId;
    public Integer promoId;
    public Integer tkClientId;
    public Integer tkCatType;
    public Integer tkCategory;
    public boolean individualCount;

    private static final String SEL_FLDS = "`id`, "
            + "`zone_id`, "
            + "`city_id`, "
            + "`sector_id`, "
            + "`cyl_type_id`, "
            + "`clie_doc`, "
            + "`pref_index_id`, "
            + "`establish_id`, "
            + "`promo_id`, "
            + "`tk_client_id`, "
            + "`tk_cat_type`, "
            + "`tk_category`, "
            + "`individual_count`";

    public static List<ComPromoTarget> getFromPromo(int promoId, Connection conn) throws Exception {
        Object[][] targets = new MySQLQuery("SELECT " + SEL_FLDS + " FROM com_promo_target WHERE promo_id = " + promoId + " ORDER BY zone_id DESC, cyl_type_id DESC, establish_id DESC, pref_index_id DESC, index_id DESC").getRecords(conn);
        List<ComPromoTarget> lstTarget = new ArrayList<>();
        for (Object[] target : targets) {
            lstTarget.add(getItem(target));
        }
        return lstTarget;
    }

    public static List<ComPromoTargetGrouped> getFromPromoGroupedCyls(int promoId, Connection conn) throws Exception {
        List<ComPromoTargetGrouped> lst = new ArrayList<>();

        Object[][] geog = new MySQLQuery("SELECT " + SEL_FLDS + " FROM com_promo_target WHERE zone_id IS NOT NULL AND promo_id = " + promoId).getRecords(conn);
        if (geog.length > 0) {
            lst.add(getItemGroup(geog, ComPromoTargetGrouped.TYPE_ZONE));
        }

        Object[][] city = new MySQLQuery("SELECT " + SEL_FLDS + " FROM com_promo_target WHERE city_id IS NOT NULL AND promo_id = " + promoId).getRecords(conn);
        if (city.length > 0) {
            lst.add(getItemGroup(city, ComPromoTargetGrouped.TYPE_CITY));
        }

        Object[][] sector = new MySQLQuery("SELECT " + SEL_FLDS + " FROM com_promo_target WHERE sector_id IS NOT NULL AND promo_id = " + promoId).getRecords(conn);
        if (sector.length > 0) {
            lst.add(getItemGroup(sector, ComPromoTargetGrouped.TYPE_SECTOR));
        }

        Object[][] cylType = new MySQLQuery("SELECT " + SEL_FLDS + " FROM com_promo_target WHERE cyl_type_id IS NOT NULL AND promo_id = " + promoId).getRecords(conn);
        if (cylType.length > 0) {
            lst.add(getItemGroup(cylType, ComPromoTargetGrouped.TYPE_CYL_TYPE));
        }

        Object[][] estType = new MySQLQuery("SELECT " + SEL_FLDS + " FROM com_promo_target WHERE establish_id IS NOT NULL AND promo_id = " + promoId).getRecords(conn);
        if (estType.length > 0) {
            lst.add(getItemGroup(estType, ComPromoTargetGrouped.TYPE_EST_TYPE));
        }

        Object[][] clieType = new MySQLQuery("SELECT " + SEL_FLDS + " FROM com_promo_target WHERE clie_doc IS NOT NULL AND promo_id = " + promoId).getRecords(conn);
        if (clieType.length > 0) {
            lst.add(getItemGroup(clieType, ComPromoTargetGrouped.TYPE_CLIE));
        }

        Object[][] prefType = new MySQLQuery("SELECT " + SEL_FLDS + " FROM com_promo_target WHERE pref_index_id IS NOT NULL AND promo_id = " + promoId).getRecords(conn);
        if (prefType.length > 0) {
            lst.add(getItemGroup(prefType, ComPromoTargetGrouped.TYPE_PREF));
        }

        return lst;
    }

    public static List<ComPromoTargetGrouped> getFromPromoGroupedTanks(int promoId, Connection conn) throws Exception {
        List<ComPromoTargetGrouped> lst = new ArrayList<>();

        Object[][] geog = new MySQLQuery("SELECT " + SEL_FLDS + " FROM com_promo_target WHERE zone_id IS NOT NULL AND promo_id = " + promoId).getRecords(conn);
        if (geog.length > 0) {
            lst.add(getItemGroup(geog, ComPromoTargetGrouped.TYPE_ZONE));
        }

        Object[][] city = new MySQLQuery("SELECT " + SEL_FLDS + " FROM com_promo_target WHERE city_id IS NOT NULL AND promo_id = " + promoId).getRecords(conn);
        if (city.length > 0) {
            lst.add(getItemGroup(city, ComPromoTargetGrouped.TYPE_CITY));
        }

        Object[][] sector = new MySQLQuery("SELECT " + SEL_FLDS + " FROM com_promo_target WHERE sector_id IS NOT NULL AND promo_id = " + promoId).getRecords(conn);
        if (sector.length > 0) {
            lst.add(getItemGroup(sector, ComPromoTargetGrouped.TYPE_SECTOR));
        }

        Object[][] tkClient = new MySQLQuery("SELECT " + SEL_FLDS + " FROM com_promo_target WHERE tk_client_id IS NOT NULL AND promo_id = " + promoId).getRecords(conn);
        if (tkClient.length > 0) {
            lst.add(getItemGroup(tkClient, ComPromoTargetGrouped.TYPE_TK_CLIENT));
        }

        Object[][] cat = new MySQLQuery("SELECT " + SEL_FLDS + " FROM com_promo_target WHERE tk_category IS NOT NULL AND promo_id = " + promoId).getRecords(conn);
        if (cat.length > 0) {
            lst.add(getItemGroup(cat, ComPromoTargetGrouped.TYPE_TK_CATEGORY));
        }

        if (cat.length == 0) {
            Object[][] catType = new MySQLQuery("SELECT " + SEL_FLDS + " FROM com_promo_target WHERE tk_cat_type IS NOT NULL AND promo_id = " + promoId).getRecords(conn);
            if (catType.length > 0) {
                lst.add(getItemGroup(catType, ComPromoTargetGrouped.TYPE_TK_CAT_TYPE));
            }
        }

        return lst;
    }

    private static ComPromoTargetGrouped getItemGroup(Object[][] data, int type) throws Exception {
        ComPromoTargetGrouped item;
        List<ComPromoTarget> lstCylType = new ArrayList<>();
        for (Object[] row : data) {
            lstCylType.add(getItem(row));
        }
        item = new ComPromoTargetGrouped();
        item.targType = type;
        item.targLst = lstCylType;
        return item;
    }

    public static ComPromoTarget getItem(Object[] row) throws Exception {
        ComPromoTarget item = new ComPromoTarget();
        item.id = MySQLQuery.getAsInteger(row[0]);
        item.zoneId = MySQLQuery.getAsInteger(row[1]);
        item.cityId = MySQLQuery.getAsInteger(row[2]);
        item.sectorId = MySQLQuery.getAsInteger(row[3]);
        item.cylTypeId = MySQLQuery.getAsInteger(row[4]);
        item.clieDoc = MySQLQuery.getAsString(row[5]);
        item.prefIndexId = MySQLQuery.getAsInteger(row[6]);
        item.establishId = MySQLQuery.getAsInteger(row[7]);
        item.promoId = MySQLQuery.getAsInteger(row[8]);
        item.tkClientId = MySQLQuery.getAsInteger(row[9]);
        item.tkCatType = MySQLQuery.getAsInteger(row[10]);
        item.tkCategory = MySQLQuery.getAsInteger(row[11]);
        item.individualCount = MySQLQuery.getAsBoolean(row[12]);
        return item;
    }
}
