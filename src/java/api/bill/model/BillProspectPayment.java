package api.bill.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class BillProspectPayment extends BaseModel<BillProspectPayment> {
//inicio zona de reemplazo

    public int serviceId;
    public BigDecimal total;
    public Date date;
    public String notes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "service_id",
            "total",
            "date",
            "notes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, serviceId);
        q.setParam(2, total);
        q.setParam(3, date);
        q.setParam(4, notes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        serviceId = MySQLQuery.getAsInteger(row[0]);
        total = MySQLQuery.getAsBigDecimal(row[1], false);
        date = MySQLQuery.getAsDate(row[2]);
        notes = MySQLQuery.getAsString(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_prospect_payment";
    }

    public static String getSelFlds(String alias) {
        return new BillProspectPayment().getSelFldsForAlias(alias);
    }

    public static List<BillProspectPayment> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillProspectPayment().getListFromQuery(q, conn);
    }

    public static List<BillProspectPayment> getList(Params p, Connection conn) throws Exception {
        return new BillProspectPayment().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillProspectPayment().deleteById(id, conn);
    }

    public static List<BillProspectPayment> getAll(Connection conn) throws Exception {
        return new BillProspectPayment().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<BillProspectPayment> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}
