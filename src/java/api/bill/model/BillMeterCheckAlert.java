package api.bill.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class BillMeterCheckAlert extends BaseModel<BillMeterCheckAlert> {
//inicio zona de reemplazo

    public int meterId;
    public Date limitDate;
    public boolean done;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "meter_id",
            "limit_date",
            "done"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, meterId);
        q.setParam(2, limitDate);
        q.setParam(3, done);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        meterId = MySQLQuery.getAsInteger(row[0]);
        limitDate = MySQLQuery.getAsDate(row[1]);
        done = MySQLQuery.getAsBoolean(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_meter_check_alert";
    }

    public static String getSelFlds(String alias) {
        return new BillMeterCheckAlert().getSelFldsForAlias(alias);
    }

    public static List<BillMeterCheckAlert> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillMeterCheckAlert().getListFromQuery(q, conn);
    }

    public static List<BillMeterCheckAlert> getList(Params p, Connection conn) throws Exception {
        return new BillMeterCheckAlert().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillMeterCheckAlert().deleteById(id, conn);
    }

    public static List<BillMeterCheckAlert> getAll(Connection conn) throws Exception {
        return new BillMeterCheckAlert().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<BillMeterCheckAlert> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}