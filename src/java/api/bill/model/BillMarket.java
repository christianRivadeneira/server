package api.bill.model;

import api.BaseModel;
import api.MySQLCol;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;
import utilities.logs.LogUtils;

public class BillMarket extends BaseModel<BillMarket> {
//inicio zona de reemplazo

    public String code;
    public Integer fssriCode;
    public String name;
    public String method;
    public Date baseMonth;
    public BigDecimal dInvBaseR;
    public BigDecimal dInvBaseNr;
    public BigDecimal dAomBaseR;
    public BigDecimal dAomBaseNr;
    public BigDecimal cfBase;
    public Date cfProdBaseMonth;
    public BigDecimal failCost;
    public Date failCostBaseMonth;
    public BigDecimal sumGobAomR;
    public BigDecimal sumGobAomNR;
    public boolean tarifaPlena;
    public Integer idMarket;
    public String resolution;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "code",
            "fssri_code",
            "name",
            "method",
            "base_month",
            "d_inv_base_r",
            "d_inv_base_nr",
            "d_aom_base_r",
            "d_aom_base_nr",
            "cf_base",
            "cf_prod_base_month",
            "fail_cost",
            "fail_cost_base_month",
            "sum_gob_aomR",
            "sum_gob_aomNR",
            "tarifa_plena",
            "id_market",
            "resolution"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, code);
        q.setParam(2, fssriCode);
        q.setParam(3, name);
        q.setParam(4, method);
        q.setParam(5, baseMonth);
        q.setParam(6, dInvBaseR);
        q.setParam(7, dInvBaseNr);
        q.setParam(8, dAomBaseR);
        q.setParam(9, dAomBaseNr);
        q.setParam(10, cfBase);
        q.setParam(11, cfProdBaseMonth);
        q.setParam(12, failCost);
        q.setParam(13, failCostBaseMonth);
        q.setParam(14, sumGobAomR);
        q.setParam(15, sumGobAomNR);
        q.setParam(16, tarifaPlena);
        q.setParam(17, idMarket);
        q.setParam(18, resolution);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        code = MySQLQuery.getAsString(row[0]);
        fssriCode = MySQLQuery.getAsInteger(row[1]);
        name = MySQLQuery.getAsString(row[2]);
        method = MySQLQuery.getAsString(row[3]);
        baseMonth = MySQLQuery.getAsDate(row[4]);
        dInvBaseR = MySQLQuery.getAsBigDecimal(row[5], false);
        dInvBaseNr = MySQLQuery.getAsBigDecimal(row[6], false);
        dAomBaseR = MySQLQuery.getAsBigDecimal(row[7], false);
        dAomBaseNr = MySQLQuery.getAsBigDecimal(row[8], false);
        cfBase = MySQLQuery.getAsBigDecimal(row[9], false);
        cfProdBaseMonth = MySQLQuery.getAsDate(row[10]);
        failCost = MySQLQuery.getAsBigDecimal(row[11], false);
        failCostBaseMonth = MySQLQuery.getAsDate(row[12]);
        sumGobAomR=MySQLQuery.getAsBigDecimal(row[13], false);
        sumGobAomNR=MySQLQuery.getAsBigDecimal(row[14], false);
        tarifaPlena=MySQLQuery.getAsBoolean(row[15]);
        idMarket = MySQLQuery.getAsInteger(row[16]);
        resolution = MySQLQuery.getAsString(row[17]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_market";
    }

    public static String getSelFlds(String alias) {
        return new BillMarket().getSelFldsForAlias(alias);
    }

    public static List<BillMarket> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillMarket().getListFromQuery(q, conn);
    }

    public static List<BillMarket> getList(Params p, Connection conn) throws Exception {
        return new BillMarket().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillMarket().deleteById(id, conn);
    }

    public static List<BillMarket> getAll(Connection conn) throws Exception {
        return new BillMarket().getAllList(conn);
    }

//fin zona de reemplazo
    
    public static String getLogs(BillMarket orig, BillMarket obj) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("Se editó el registro: ");
        int nov = 0;
        nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_TEXT, "Código", orig.code, obj.code);
        nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_TEXT, "Nombre", orig.name, obj.name);
        nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_TEXT, "Método", orig.method, obj.method);
        nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_DD_MM_YYYY, "Mes Base", orig.baseMonth, obj.baseMonth);
        nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_DECIMAL_2, "D Inv. Res.", orig.dInvBaseR, obj.dInvBaseR);
        nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_DECIMAL_2, "D Inv. No Res.", orig.dInvBaseNr, obj.dInvBaseNr);
        nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_DECIMAL_2, "D AOM Res.", orig.dAomBaseR, obj.dAomBaseR);
        nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_DECIMAL_2, "D AOM No Res.", orig.dAomBaseNr, obj.dAomBaseNr);
        nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_DECIMAL_2, "Cargo Fijo", orig.cfBase, obj.cfBase);
        if (nov > 0) {
            return sb.toString();
        } else {
            return null;
        }
    }
}
