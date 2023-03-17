package api.trk.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class TrkSaleWarning extends BaseModel<TrkSaleWarning> {

    public TrkSaleWarning() {

    }

    public TrkSaleWarning(int saleId, boolean lock, Exception ex) {
        this(saleId, true, ex.getMessage());
    }

    public TrkSaleWarning(int saleId, boolean lock, String msg) {
        this.saleId = saleId;
        this.warning = msg;
        this.dt = new Date();
        this.lockLiq = lock;
    }

//inicio zona de reemplazo
    public int saleId;
    public String warning;
    public Date dt;
    public Boolean lockLiq;
    public String unlockNotes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "sale_id",
            "warning",
            "dt",
            "lock_liq",
            "unlock_notes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, saleId);
        q.setParam(2, warning);
        q.setParam(3, dt);
        q.setParam(4, lockLiq);
        q.setParam(5, unlockNotes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        saleId = MySQLQuery.getAsInteger(row[0]);
        warning = MySQLQuery.getAsString(row[1]);
        dt = MySQLQuery.getAsDate(row[2]);
        lockLiq = MySQLQuery.getAsBoolean(row[3]);
        unlockNotes = MySQLQuery.getAsString(row[4]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "trk_sale_warning";
    }

    public static String getSelFlds(String alias) {
        return new TrkSaleWarning().getSelFldsForAlias(alias);
    }

    public static List<TrkSaleWarning> getList(MySQLQuery q, Connection conn) throws Exception {
        return new TrkSaleWarning().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new TrkSaleWarning().deleteById(id, conn);
    }

    public static List<TrkSaleWarning> getAll(Connection conn) throws Exception {
        return new TrkSaleWarning().getAllList(conn);
    }

//fin zona de reemplazo
}
