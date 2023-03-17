package api.bill.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class BillMeasure extends BaseModel<BillMeasure> {
//inicio zona de reemplazo

    public int clientId;
    public int spanId;
    public Date takenDt;
    public BigDecimal pressure;
    public Boolean pressureOk;
    public Integer odorantId;
    public BigDecimal odorantAmount;
    public Boolean odorantOk;
    public String notes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "client_id",
            "span_id",
            "taken_dt",
            "pressure",
            "pressure_ok",
            "odorant_id",
            "odorant_amount",
            "odorant_ok",
            "notes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, clientId);
        q.setParam(2, spanId);
        q.setParam(3, takenDt);
        q.setParam(4, pressure);
        q.setParam(5, pressureOk);
        q.setParam(6, odorantId);
        q.setParam(7, odorantAmount);
        q.setParam(8, odorantOk);
        q.setParam(9, notes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        clientId = MySQLQuery.getAsInteger(row[0]);
        spanId = MySQLQuery.getAsInteger(row[1]);
        takenDt = MySQLQuery.getAsDate(row[2]);
        pressure = MySQLQuery.getAsBigDecimal(row[3], false);
        pressureOk = MySQLQuery.getAsBoolean(row[4]);
        odorantId = MySQLQuery.getAsInteger(row[5]);
        odorantAmount = MySQLQuery.getAsBigDecimal(row[6], false);
        odorantOk = MySQLQuery.getAsBoolean(row[7]);
        notes = MySQLQuery.getAsString(row[8]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_measure";
    }

    public static String getSelFlds(String alias) {
        return new BillMeasure().getSelFldsForAlias(alias);
    }

    public static List<BillMeasure> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillMeasure().getListFromQuery(q, conn);
    }

    public static List<BillMeasure> getList(Params p, Connection conn) throws Exception {
        return new BillMeasure().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillMeasure().deleteById(id, conn);
    }

    public static List<BillMeasure> getAll(Connection conn) throws Exception {
        return new BillMeasure().getAllList(conn);
    }

//fin zona de reemplazo
    
    public static List<BillMeasure> getBySpan(int spanId, Connection conn) throws Exception {
        Params p = new Params("spanId", spanId);
        return BillMeasure.getList(p, conn);
    }
}
