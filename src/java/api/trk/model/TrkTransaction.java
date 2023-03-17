package api.trk.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class TrkTransaction extends BaseModel<TrkTransaction> {
//inicio zona de reemplazo

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

    @Override
    protected String[] getFlds() {
        return new String[]{
            "dt",
            "document",
            "price",
            "val_sub",
            "cyl_id",
            "index_id",
            "lat",
            "lon",
            "error1",
            "error2",
            "emp_id",
            "auth",
            "bill",
            "cyl_received_id",
            "discount",
            "bonus_code"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, dt);
        q.setParam(2, document);
        q.setParam(3, price);
        q.setParam(4, valSub);
        q.setParam(5, cylId);
        q.setParam(6, indexId);
        q.setParam(7, lat);
        q.setParam(8, lon);
        q.setParam(9, error1);
        q.setParam(10, error2);
        q.setParam(11, empId);
        q.setParam(12, auth);
        q.setParam(13, bill);
        q.setParam(14, cylReceivedId);
        q.setParam(15, discount);
        q.setParam(16, bonusCode);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        dt = MySQLQuery.getAsDate(row[0]);
        document = MySQLQuery.getAsString(row[1]);
        price = MySQLQuery.getAsInteger(row[2]);
        valSub = MySQLQuery.getAsInteger(row[3]);
        cylId = MySQLQuery.getAsInteger(row[4]);
        indexId = MySQLQuery.getAsInteger(row[5]);
        lat = MySQLQuery.getAsString(row[6]);
        lon = MySQLQuery.getAsString(row[7]);
        error1 = MySQLQuery.getAsString(row[8]);
        error2 = MySQLQuery.getAsString(row[9]);
        empId = MySQLQuery.getAsInteger(row[10]);
        auth = MySQLQuery.getAsString(row[11]);
        bill = MySQLQuery.getAsString(row[12]);
        cylReceivedId = MySQLQuery.getAsInteger(row[13]);
        discount = MySQLQuery.getAsInteger(row[14]);
        bonusCode = MySQLQuery.getAsString(row[15]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "trk_transaction";
    }

    public static String getSelFlds(String alias) {
        return new TrkTransaction().getSelFldsForAlias(alias);
    }

    public static List<TrkTransaction> getList(MySQLQuery q, Connection conn) throws Exception {
        return new TrkTransaction().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new TrkTransaction().deleteById(id, conn);
    }

    public static List<TrkTransaction> getAll(Connection conn) throws Exception {
        return new TrkTransaction().getAllList(conn);
    }

//fin zona de reemplazo
   
}
