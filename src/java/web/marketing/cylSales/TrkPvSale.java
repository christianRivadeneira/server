package web.marketing.cylSales;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class TrkPvSale implements WarningList{
//inicio zona de reemplazo

    public int id;
    public int storeId;
    public Date dt;
    public int bill;
    public BigDecimal lat;
    public BigDecimal lon;
    public int empId;
    public List<String> lstWarns;
    public boolean credit;
    public String sucursal;
    public Integer stratum;
    public String zone;
    public Integer danePobId;

    private static final String SEL_FLDS = "`store_id`, "
            + "`dt`, "
            + "`bill`, "
            + "`lat`, "
            + "`lon`, "
            + "`emp_id`, "
            + "`credit`, "
            + "`sucursal`, "
            + "`stratum`, "
            + "`zone`, "
            + "`dane_pob_id`";

    private static final String SET_FLDS = "trk_pv_sale SET "
            + "`store_id` = ?1, "
            + "`dt` = ?2, "
            + "`bill` = ?3, "
            + "`lat` = ?4, "
            + "`lon` = ?5, "
            + "`emp_id` = ?6, "
            + "`credit` = ?7, "
            + "`sucursal` = ?8, "
            + "`stratum` = ?9, "
            + "`zone` = ?10, "
            + "`dane_pob_id` = ?11";

    private static void setFields(TrkPvSale obj, MySQLQuery q) {
        q.setParam(1, obj.storeId);
        q.setParam(2, obj.dt);
        q.setParam(3, obj.bill);
        q.setParam(4, obj.lat);
        q.setParam(5, obj.lon);
        q.setParam(6, obj.empId);
        q.setParam(7, obj.credit);
        q.setParam(8, obj.sucursal);
        q.setParam(9, obj.stratum);
        q.setParam(10, obj.zone);
        q.setParam(11, obj.danePobId);
    }

    public static TrkPvSale getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        TrkPvSale obj = new TrkPvSale();
        obj.storeId = MySQLQuery.getAsInteger(row[0]);
        obj.dt = MySQLQuery.getAsDate(row[1]);
        obj.bill = MySQLQuery.getAsInteger(row[2]);
        obj.lat = MySQLQuery.getAsBigDecimal(row[3], false);
        obj.lon = MySQLQuery.getAsBigDecimal(row[4], false);
        obj.empId = MySQLQuery.getAsInteger(row[5]);
        obj.credit = MySQLQuery.getAsBoolean(row[6]);
        obj.sucursal = MySQLQuery.getAsString(row[7]);
        obj.stratum = MySQLQuery.getAsInteger(row[8]);
        obj.zone = MySQLQuery.getAsString(row[9]);
        obj.danePobId = MySQLQuery.getAsInteger(row[10]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo

    public TrkPvSale select(int id, Connection ep) throws Exception {
        return TrkPvSale.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(ep));
    }

    public int insert(TrkPvSale pobj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(pobj, q);
        return q.executeInsert(ep);
    }

    public void update(TrkPvSale pobj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + pobj.id);
        setFields(pobj, q);
        q.executeUpdate(ep);
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM trk_pv_sale WHERE id = " + id;
    }

    public void delete(int id, Connection ep) throws Exception {
        new MySQLQuery("DELETE FROM trk_pv_sale WHERE id = " + id).executeDelete(ep);
    }

    @Override
    public void addWarn(String warn) {
        this.lstWarns.add(warn);
    }
}
