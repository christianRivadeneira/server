package api.bill.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class BillUserServiceFee extends BaseModel<BillUserServiceFee> {
    
    public Boolean caused;
    
//inicio zona de reemplazo

    public Integer place;
    public int serviceId;
    public BigDecimal value;
    public BigDecimal extPay;
    public BigDecimal inter;
    public BigDecimal extInter;
    public BigDecimal interTax;
    public BigDecimal extInterTax;
    public String notes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "place",
            "service_id",
            "value",
            "ext_pay",
            "inter",
            "ext_inter",
            "inter_tax",
            "ext_inter_tax",
            "notes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, place);
        q.setParam(2, serviceId);
        q.setParam(3, value);
        q.setParam(4, extPay);
        q.setParam(5, inter);
        q.setParam(6, extInter);
        q.setParam(7, interTax);
        q.setParam(8, extInterTax);
        q.setParam(9, notes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        place = MySQLQuery.getAsInteger(row[0]);
        serviceId = MySQLQuery.getAsInteger(row[1]);
        value = MySQLQuery.getAsBigDecimal(row[2], false);
        extPay = MySQLQuery.getAsBigDecimal(row[3], false);
        inter = MySQLQuery.getAsBigDecimal(row[4], false);
        extInter = MySQLQuery.getAsBigDecimal(row[5], false);
        interTax = MySQLQuery.getAsBigDecimal(row[6], false);
        extInterTax = MySQLQuery.getAsBigDecimal(row[7], false);
        notes = MySQLQuery.getAsString(row[8]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_user_service_fee";
    }

    public static String getSelFlds(String alias) {
        return new BillUserServiceFee().getSelFldsForAlias(alias);
    }

    public static List<BillUserServiceFee> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillUserServiceFee().getListFromQuery(q, conn);
    }

    public static List<BillUserServiceFee> getList(Params p, Connection conn) throws Exception {
        return new BillUserServiceFee().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillUserServiceFee().deleteById(id, conn);
    }

    public static List<BillUserServiceFee> getAll(Connection conn) throws Exception {
        return new BillUserServiceFee().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<BillUserServiceFee> getByService(int serviceId, Connection conn) throws Exception {
        return getList(new MySQLQuery("SELECT " + getSelFlds("") + " FROM bill_user_service_fee WHERE service_id = ?1 ORDER BY place").setParam(1, serviceId), conn);
    }
    
    
}
