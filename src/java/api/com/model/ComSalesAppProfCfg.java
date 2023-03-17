package api.com.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class ComSalesAppProfCfg extends BaseModel<ComSalesAppProfCfg> {

    public String profileName;

//inicio zona de reemplazo

    public int profId;
    public boolean subsidy;
    public boolean full;
    public boolean multi;
    public boolean pv;
    public boolean summary;
    public boolean scanLoad;
    public boolean ordering;
    public int coordsInOrder;
    public int coordsNorm;
    public boolean getPhone;
    public boolean pqrCylRelation;
    public boolean nameDocSearch;
    public boolean scanNifRec;
    public boolean bonusField;
    public boolean photoNifRec;
    public boolean createOrder;
    public boolean listOrders;
    public boolean showQuestion;
    public boolean pvSaleSummary;
    public boolean pvOrderHist;
    public boolean institutional;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "prof_id",
            "subsidy",
            "full",
            "multi",
            "pv",
            "summary",
            "scan_load",
            "ordering",
            "coords_in_order",
            "coords_norm",
            "get_phone",
            "pqr_cyl_relation",
            "name_doc_search",
            "scan_nif_rec",
            "bonus_field",
            "photo_nif_rec",
            "create_order",
            "list_orders",
            "show_question",
            "pv_sale_summary",
            "pv_order_hist",
            "institutional"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, profId);
        q.setParam(2, subsidy);
        q.setParam(3, full);
        q.setParam(4, multi);
        q.setParam(5, pv);
        q.setParam(6, summary);
        q.setParam(7, scanLoad);
        q.setParam(8, ordering);
        q.setParam(9, coordsInOrder);
        q.setParam(10, coordsNorm);
        q.setParam(11, getPhone);
        q.setParam(12, pqrCylRelation);
        q.setParam(13, nameDocSearch);
        q.setParam(14, scanNifRec);
        q.setParam(15, bonusField);
        q.setParam(16, photoNifRec);
        q.setParam(17, createOrder);
        q.setParam(18, listOrders);
        q.setParam(19, showQuestion);
        q.setParam(20, pvSaleSummary);
        q.setParam(21, pvOrderHist);
        q.setParam(22, institutional);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        profId = MySQLQuery.getAsInteger(row[0]);
        subsidy = MySQLQuery.getAsBoolean(row[1]);
        full = MySQLQuery.getAsBoolean(row[2]);
        multi = MySQLQuery.getAsBoolean(row[3]);
        pv = MySQLQuery.getAsBoolean(row[4]);
        summary = MySQLQuery.getAsBoolean(row[5]);
        scanLoad = MySQLQuery.getAsBoolean(row[6]);
        ordering = MySQLQuery.getAsBoolean(row[7]);
        coordsInOrder = MySQLQuery.getAsInteger(row[8]);
        coordsNorm = MySQLQuery.getAsInteger(row[9]);
        getPhone = MySQLQuery.getAsBoolean(row[10]);
        pqrCylRelation = MySQLQuery.getAsBoolean(row[11]);
        nameDocSearch = MySQLQuery.getAsBoolean(row[12]);
        scanNifRec = MySQLQuery.getAsBoolean(row[13]);
        bonusField = MySQLQuery.getAsBoolean(row[14]);
        photoNifRec = MySQLQuery.getAsBoolean(row[15]);
        createOrder = MySQLQuery.getAsBoolean(row[16]);
        listOrders = MySQLQuery.getAsBoolean(row[17]);
        showQuestion = MySQLQuery.getAsBoolean(row[18]);
        pvSaleSummary = MySQLQuery.getAsBoolean(row[19]);
        pvOrderHist = MySQLQuery.getAsBoolean(row[20]);
        institutional = MySQLQuery.getAsBoolean(row[21]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "com_sales_app_prof_cfg";
    }

    public static String getSelFlds(String alias) {
        return new ComSalesAppProfCfg().getSelFldsForAlias(alias);
    }

    public static List<ComSalesAppProfCfg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new ComSalesAppProfCfg().getListFromQuery(q, conn);
    }

    public static List<ComSalesAppProfCfg> getList(Params p, Connection conn) throws Exception {
        return new ComSalesAppProfCfg().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new ComSalesAppProfCfg().deleteById(id, conn);
    }

    public static List<ComSalesAppProfCfg> getAll(Connection conn) throws Exception {
        return new ComSalesAppProfCfg().getAllList(conn);
    }

//fin zona de reemplazo
    public ComSalesAppProfCfg getFromRow(Object[] row) throws Exception {
        ComSalesAppProfCfg obj = new ComSalesAppProfCfg();
        obj.profId = MySQLQuery.getAsInteger(row[0]);
        obj.subsidy = MySQLQuery.getAsBoolean(row[1]);
        obj.full = MySQLQuery.getAsBoolean(row[2]);
        obj.multi = MySQLQuery.getAsBoolean(row[3]);
        obj.pv = MySQLQuery.getAsBoolean(row[4]);
        obj.summary = MySQLQuery.getAsBoolean(row[5]);
        obj.scanLoad = MySQLQuery.getAsBoolean(row[6]);
        obj.ordering = MySQLQuery.getAsBoolean(row[7]);
        obj.coordsInOrder = MySQLQuery.getAsInteger(row[8]);
        obj.coordsNorm = MySQLQuery.getAsInteger(row[9]);
        obj.getPhone = MySQLQuery.getAsBoolean(row[10]);
        obj.pqrCylRelation = MySQLQuery.getAsBoolean(row[11]);
        obj.nameDocSearch = MySQLQuery.getAsBoolean(row[12]);
        obj.scanNifRec = MySQLQuery.getAsBoolean(row[13]);
        obj.bonusField = MySQLQuery.getAsBoolean(row[14]);
        obj.photoNifRec = MySQLQuery.getAsBoolean(row[15]);
        obj.createOrder = MySQLQuery.getAsBoolean(row[16]);
        obj.listOrders = MySQLQuery.getAsBoolean(row[17]);
        obj.showQuestion = MySQLQuery.getAsBoolean(row[18]);
        obj.pvSaleSummary = MySQLQuery.getAsBoolean(row[19]);
        obj.pvOrderHist = MySQLQuery.getAsBoolean(row[20]);
        obj.institutional = MySQLQuery.getAsBoolean(row[21]);
        obj.id = MySQLQuery.getAsInteger(row[22]);
        obj.profileName = MySQLQuery.getAsString(row[23]);
        
        return obj;
    }
}
