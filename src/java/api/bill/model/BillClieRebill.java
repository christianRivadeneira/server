package api.bill.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class BillClieRebill extends BaseModel<BillClieRebill> {
//inicio zona de reemplazo

    public int clientId;
    public int errorSpanId;
    public int rebillSpanId;
    public int creatorId;
    public Date created;
    public int newStratum;
    public String newSector;
    public BigDecimal newMeterFactor;
    public BigDecimal diffM3Subs;
    public BigDecimal diffValConsSubs;
    public BigDecimal diffM3NoSubs;
    public BigDecimal diffValConsNoSubs;
    public BigDecimal diffValSubs;
    public BigDecimal diffValContrib;
    public BigDecimal diffValExcContrib;
    public BigDecimal diffFixedCharge;
    public String jsonText;
    public String reason;
    public Integer lastTransId;
    public BigDecimal origBegRead;
    public BigDecimal origEndRead;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "client_id",
            "error_span_id",
            "rebill_span_id",
            "creator_id",
            "created",
            "new_stratum",
            "new_sector",
            "new_meter_factor",
            "diff_m3_subs",
            "diff_val_cons_subs",
            "diff_m3_no_subs",
            "diff_val_cons_no_subs",
            "diff_val_subs",
            "diff_val_contrib",
            "diff_val_exc_contrib",
            "diff_fixed_charge",
            "json_text",
            "reason",
            "last_trans_id",
            "orig_beg_read",
            "orig_end_read",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, clientId);
        q.setParam(2, errorSpanId);
        q.setParam(3, rebillSpanId);
        q.setParam(4, creatorId);
        q.setParam(5, created);
        q.setParam(6, newStratum);
        q.setParam(7, newSector);
        q.setParam(8, newMeterFactor);
        q.setParam(9, diffM3Subs);
        q.setParam(10, diffValConsSubs);
        q.setParam(11, diffM3NoSubs);
        q.setParam(12, diffValConsNoSubs);
        q.setParam(13, diffValSubs);
        q.setParam(14, diffValContrib);
        q.setParam(15, diffValExcContrib);
        q.setParam(16, diffFixedCharge);
        q.setParam(17, jsonText);
        q.setParam(18, reason);
        q.setParam(19, lastTransId);
        q.setParam(20, origBegRead);
        q.setParam(21, origEndRead);
        q.setParam(22, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        clientId = MySQLQuery.getAsInteger(row[0]);
        errorSpanId = MySQLQuery.getAsInteger(row[1]);
        rebillSpanId = MySQLQuery.getAsInteger(row[2]);
        creatorId = MySQLQuery.getAsInteger(row[3]);
        created = MySQLQuery.getAsDate(row[4]);
        newStratum = MySQLQuery.getAsInteger(row[5]);
        newSector = MySQLQuery.getAsString(row[6]);
        newMeterFactor = MySQLQuery.getAsBigDecimal(row[7], false);
        diffM3Subs = MySQLQuery.getAsBigDecimal(row[8], false);
        diffValConsSubs = MySQLQuery.getAsBigDecimal(row[9], false);
        diffM3NoSubs = MySQLQuery.getAsBigDecimal(row[10], false);
        diffValConsNoSubs = MySQLQuery.getAsBigDecimal(row[11], false);
        diffValSubs = MySQLQuery.getAsBigDecimal(row[12], false);
        diffValContrib = MySQLQuery.getAsBigDecimal(row[13], false);
        diffValExcContrib = MySQLQuery.getAsBigDecimal(row[14], false);
        diffFixedCharge = MySQLQuery.getAsBigDecimal(row[15], false);
        jsonText = MySQLQuery.getAsString(row[16]);
        reason = MySQLQuery.getAsString(row[17]);
        lastTransId = MySQLQuery.getAsInteger(row[18]);
        origBegRead = MySQLQuery.getAsBigDecimal(row[19], false);
        origEndRead = MySQLQuery.getAsBigDecimal(row[20], false);
        active = MySQLQuery.getAsBoolean(row[21]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_clie_rebill";
    }

    public static String getSelFlds(String alias) {
        return new BillClieRebill().getSelFldsForAlias(alias);
    }

    public static List<BillClieRebill> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillClieRebill().getListFromQuery(q, conn);
    }

    public static List<BillClieRebill> getList(Params p, Connection conn) throws Exception {
        return new BillClieRebill().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillClieRebill().deleteById(id, conn);
    }

    public static List<BillClieRebill> getAll(Connection conn) throws Exception {
        return new BillClieRebill().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<BillClieRebill> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/
    public static BillClieRebill getByClientErrorSpan(int clientId, int errorSpanId, Connection conn) throws Exception {
        return new BillClieRebill().select(new Params("clientId", clientId).param("error_span_id", errorSpanId).param("active", true), conn);
    }

    public static List<BillClieRebill> getByClientRebillSpan(int clientId, int rebillSpanId, Connection conn) throws Exception {
        return new BillClieRebill().getListFromParams(new Params("clientId", clientId).param("rebill_span_id", rebillSpanId).param("active", true), conn);
    }
}
