package api.bill.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class BillServiceFail extends BaseModel<BillServiceFail> {
//inicio zona de reemplazo

    public int clientId;
    public int spanId;
    public Date begDt;
    public Date endDt;
    public String notes;
    public BigDecimal avgCons;
    public BigDecimal cregCost;
    public BigDecimal cost;
    public String causalType;
    public String tipoSusp;
    public String orgSusp;
    public String medio;
    public Date dateMedio;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "client_id",
            "span_id",
            "beg_dt",
            "end_dt",
            "notes",
            "avg_cons",
            "creg_cost",
            "cost",
            "causal_type",
            "tipo_susp",
            "org_susp",
            "medio",
            "date_medio"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, clientId);
        q.setParam(2, spanId);
        q.setParam(3, begDt);
        q.setParam(4, endDt);
        q.setParam(5, notes);
        q.setParam(6, avgCons);
        q.setParam(7, cregCost);
        q.setParam(8, cost);
        q.setParam(9, causalType);
        q.setParam(10, tipoSusp);
        q.setParam(11, orgSusp);
        q.setParam(12, medio);
        q.setParam(13, dateMedio);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        clientId = MySQLQuery.getAsInteger(row[0]);
        spanId = MySQLQuery.getAsInteger(row[1]);
        begDt = MySQLQuery.getAsDate(row[2]);
        endDt = MySQLQuery.getAsDate(row[3]);
        notes = MySQLQuery.getAsString(row[4]);
        avgCons = MySQLQuery.getAsBigDecimal(row[5], false);
        cregCost = MySQLQuery.getAsBigDecimal(row[6], false);
        cost = MySQLQuery.getAsBigDecimal(row[7], false);
        causalType = MySQLQuery.getAsString(row[8]);
        tipoSusp= MySQLQuery.getAsString(row[9]);
        orgSusp= MySQLQuery.getAsString(row[10]);
        medio= MySQLQuery.getAsString(row[11]);
        dateMedio = MySQLQuery.getAsDate(row[12]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_service_fail";
    }

    public static String getSelFlds(String alias) {
        return new BillServiceFail().getSelFldsForAlias(alias);
    }

    public static List<BillServiceFail> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillServiceFail().getListFromQuery(q, conn);
    }

    public static List<BillServiceFail> getList(Params p, Connection conn) throws Exception {
        return new BillServiceFail().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillServiceFail().deleteById(id, conn);
    }

    public static List<BillServiceFail> getAll(Connection conn) throws Exception {
        return new BillServiceFail().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<BillServiceFail> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}
