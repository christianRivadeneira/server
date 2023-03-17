package api.gt.model;

import api.BaseModel;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class GtGlpInv extends BaseModel<GtGlpInv> {
//inicio zona de reemplazo

    public int tripId;
    public Date invDate;
    public String type;
    public BigDecimal tmpB;
    public BigDecimal tmpE;
    public BigDecimal rgB;
    public BigDecimal rgE;
    public BigDecimal psiB;
    public BigDecimal psiE;
    public BigDecimal d1B;
    public BigDecimal d1E;
    public BigDecimal d2B;
    public BigDecimal d2E;
    public String notes;
    public Integer minutesBegin;
    public Integer minutesEnd;
    public Boolean single;
    public boolean done;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "trip_id",
            "inv_date",
            "type",
            "tmp_b",
            "tmp_e",
            "rg_b",
            "rg_e",
            "psi_b",
            "psi_e",
            "d1_b",
            "d1_e",
            "d2_b",
            "d2_e",
            "notes",
            "minutes_begin",
            "minutes_end",
            "single",
            "done"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, tripId);
        q.setParam(2, invDate);
        q.setParam(3, type);
        q.setParam(4, tmpB);
        q.setParam(5, tmpE);
        q.setParam(6, rgB);
        q.setParam(7, rgE);
        q.setParam(8, psiB);
        q.setParam(9, psiE);
        q.setParam(10, d1B);
        q.setParam(11, d1E);
        q.setParam(12, d2B);
        q.setParam(13, d2E);
        q.setParam(14, notes);
        q.setParam(15, minutesBegin);
        q.setParam(16, minutesEnd);
        q.setParam(17, single);
        q.setParam(18, done);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        tripId = MySQLQuery.getAsInteger(row[0]);
        invDate = MySQLQuery.getAsDate(row[1]);
        type = MySQLQuery.getAsString(row[2]);
        tmpB = MySQLQuery.getAsBigDecimal(row[3], false);
        tmpE = MySQLQuery.getAsBigDecimal(row[4], false);
        rgB = MySQLQuery.getAsBigDecimal(row[5], false);
        rgE = MySQLQuery.getAsBigDecimal(row[6], false);
        psiB = MySQLQuery.getAsBigDecimal(row[7], false);
        psiE = MySQLQuery.getAsBigDecimal(row[8], false);
        d1B = MySQLQuery.getAsBigDecimal(row[9], false);
        d1E = MySQLQuery.getAsBigDecimal(row[10], false);
        d2B = MySQLQuery.getAsBigDecimal(row[11], false);
        d2E = MySQLQuery.getAsBigDecimal(row[12], false);
        notes = MySQLQuery.getAsString(row[13]);
        minutesBegin = MySQLQuery.getAsInteger(row[14]);
        minutesEnd = MySQLQuery.getAsInteger(row[15]);
        single = MySQLQuery.getAsBoolean(row[16]);
        done = MySQLQuery.getAsBoolean(row[17]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "gt_glp_inv";
    }

    public static String getSelFlds(String alias) {
        return new GtGlpInv().getSelFldsForAlias(alias);
    }

    public static List<GtGlpInv> getList(MySQLQuery q, Connection conn) throws Exception {
        return new GtGlpInv().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new GtGlpInv().deleteById(id, conn);
    }

    public static List<GtGlpInv> getAll(Connection conn) throws Exception {
        return new GtGlpInv().getAllList(conn);
    }

//fin zona de reemplazo
}
