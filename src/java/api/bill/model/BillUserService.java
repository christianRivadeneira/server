package api.bill.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class BillUserService extends BaseModel<BillUserService> {

    public Boolean fullyCaused;

//inicio zona de reemplazo

    public Integer typeId;
    public int billClientTankId;
    public int billSpanId;
    public Integer prospectServiceId;
    public Integer payments;
    public BigDecimal total;
    public BigDecimal ivaRate;
    public BigDecimal creditInter;
    public String description;
    public BigDecimal inteIvaRate;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "type_id",
            "bill_client_tank_id",
            "bill_span_id",
            "prospect_service_id",
            "payments",
            "total",
            "iva_rate",
            "credit_inter",
            "description",
            "inte_iva_rate"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, typeId);
        q.setParam(2, billClientTankId);
        q.setParam(3, billSpanId);
        q.setParam(4, prospectServiceId);
        q.setParam(5, payments);
        q.setParam(6, total);
        q.setParam(7, ivaRate);
        q.setParam(8, creditInter);
        q.setParam(9, description);
        q.setParam(10, inteIvaRate);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        typeId = MySQLQuery.getAsInteger(row[0]);
        billClientTankId = MySQLQuery.getAsInteger(row[1]);
        billSpanId = MySQLQuery.getAsInteger(row[2]);
        prospectServiceId = MySQLQuery.getAsInteger(row[3]);
        payments = MySQLQuery.getAsInteger(row[4]);
        total = MySQLQuery.getAsBigDecimal(row[5], false);
        ivaRate = MySQLQuery.getAsBigDecimal(row[6], false);
        creditInter = MySQLQuery.getAsBigDecimal(row[7], false);
        description = MySQLQuery.getAsString(row[8]);
        inteIvaRate = MySQLQuery.getAsBigDecimal(row[9], false);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_user_service";
    }

    public static String getSelFlds(String alias) {
        return new BillUserService().getSelFldsForAlias(alias);
    }

    public static List<BillUserService> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillUserService().getListFromQuery(q, conn);
    }

    public static List<BillUserService> getList(Params p, Connection conn) throws Exception {
        return new BillUserService().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillUserService().deleteById(id, conn);
    }

    public static List<BillUserService> getAll(Connection conn) throws Exception {
        return new BillUserService().getAllList(conn);
    }

//fin zona de reemplazo
}
