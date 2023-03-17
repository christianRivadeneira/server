package web.marketing.cylSales.promo;

import java.util.List;

public class ComPromoTargetGrouped {
    
    public static int TYPE_ZONE = 1;
    public static int TYPE_CITY = 2;
    public static int TYPE_SECTOR = 3;
    public static int TYPE_CYL_TYPE = 4;
    public static int TYPE_EST_TYPE = 5;
    public static int TYPE_CLIE = 6;
    public static int TYPE_PREF = 7;
    public static int TYPE_TK_CLIENT = 8;
    public static int TYPE_TK_CAT_TYPE = 9;
    public static int TYPE_TK_CATEGORY = 10;
    
    
    public int targType;
    public List<ComPromoTarget> targLst;
    
}
