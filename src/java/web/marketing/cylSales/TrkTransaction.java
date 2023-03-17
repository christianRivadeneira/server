package web.marketing.cylSales;

import java.sql.Connection;
import java.util.Date;
import utilities.MySQLQuery;

public class TrkTransaction {
//inicio zona de reemplazo

    public int id;
    public Date dt;
    public String document;
    public Integer price;
    public Integer valSub;
    public Integer cylId;
    public Integer indexId;
    public String lat;
    public String lon;
    public String error1;
    public String error2;
    public Integer empId;
    public String auth;
    public String bill;
    public Integer cylReceivedId;
    public Integer discount;
    public String bonusCode;

    private static final String selFlds = "`dt`, "
            + "`document`, "
            + "`price`, "
            + "`val_sub`, "
            + "`cyl_id`, "
            + "`index_id`, "
            + "`lat`, "
            + "`lon`, "
            + "`error1`, "
            + "`error2`, "
            + "`emp_id`, "
            + "`auth`, "
            + "`bill`, "
            + "`cyl_received_id`, "
            + "`discount`, "
            + "`bonus_code`";

    private static final String setFlds = "trk_transaction SET "
            + "`dt` = ?1, "
            + "`document` = ?2, "
            + "`price` = ?3, "
            + "`val_sub` = ?4, "
            + "`cyl_id` = ?5, "
            + "`index_id` = ?6, "
            + "`lat` = ?7, "
            + "`lon` = ?8, "
            + "`error1` = ?9, "
            + "`error2` = ?10, "
            + "`emp_id` = ?11, "
            + "`auth` = ?12, "
            + "`bill` = ?13, "
            + "`cyl_received_id` = ?14, "
            + "`discount` = ?15, "
            + "`bonus_code` = ?16";

    private void setFields(TrkTransaction obj, MySQLQuery q) {
        q.setParam(1, obj.dt);
        q.setParam(2, obj.document);
        q.setParam(3, obj.price);
        q.setParam(4, obj.valSub);
        q.setParam(5, obj.cylId);
        q.setParam(6, obj.indexId);
        q.setParam(7, obj.lat);
        q.setParam(8, obj.lon);
        q.setParam(9, obj.error1);
        q.setParam(10, obj.error2);
        q.setParam(11, obj.empId);
        q.setParam(12, obj.auth);
        q.setParam(13, obj.bill);
        q.setParam(14, obj.cylReceivedId);
        q.setParam(15, obj.discount);
        q.setParam(16, obj.bonusCode);
    }

    public TrkTransaction select(int id, Connection ep) throws Exception {
        TrkTransaction obj = new TrkTransaction();
        MySQLQuery q = new MySQLQuery("SELECT " + selFlds + " FROM trk_transaction WHERE id = " + id + " FOR UPDATE");
        Object[] row = q.getRecord(ep);
        obj.dt = MySQLQuery.getAsDate(row[0]);
        obj.document = MySQLQuery.getAsString(row[1]);
        obj.price = MySQLQuery.getAsInteger(row[2]);
        obj.valSub = MySQLQuery.getAsInteger(row[3]);
        obj.cylId = MySQLQuery.getAsInteger(row[4]);
        obj.indexId = MySQLQuery.getAsInteger(row[5]);
        obj.lat = MySQLQuery.getAsString(row[6]);
        obj.lon = MySQLQuery.getAsString(row[7]);
        obj.error1 = MySQLQuery.getAsString(row[8]);
        obj.error2 = MySQLQuery.getAsString(row[9]);
        obj.empId = MySQLQuery.getAsInteger(row[10]);
        obj.auth = MySQLQuery.getAsString(row[11]);
        obj.bill = MySQLQuery.getAsString(row[12]);
        obj.cylReceivedId = MySQLQuery.getAsInteger(row[13]);
        obj.discount = MySQLQuery.getAsInteger(row[14]);
        obj.bonusCode = MySQLQuery.getAsString(row[15]);

        obj.id = id;
        return obj;
    }

//fin zona de reemplazo
    public int insert(TrkTransaction pobj, Connection ep) throws Exception {
        TrkTransaction obj = (TrkTransaction) pobj;
        MySQLQuery q = new MySQLQuery("INSERT INTO " + setFlds);
        setFields(obj, q);
        return q.executeInsert(ep);
    }

    public void update(TrkTransaction pobj, Connection ep) throws Exception {
        TrkTransaction obj = (TrkTransaction) pobj;
        MySQLQuery q = new MySQLQuery("UPDATE " + setFlds + " WHERE id = " + obj.id);
        setFields(obj, q);
        q.executeUpdate(ep);
    }

    public void delete(int id, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("DELETE FROM trk_transaction WHERE id = " + id);
        q.executeDelete(ep);
    }

}
