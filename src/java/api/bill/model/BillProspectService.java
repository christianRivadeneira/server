package api.bill.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class BillProspectService extends BaseModel<BillProspectService> {
//inicio zona de reemplazo

    public int prospectId;
    public int typeId;
    public BigDecimal comVal;
    public BigDecimal dtoRate;
    public BigDecimal subsRate;
    public BigDecimal taxRate;
    public BigDecimal netVal;
    public BigDecimal tax;
    public int payments;
    public BigDecimal finanRate;
    public BigDecimal inteTax;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "prospect_id",
            "type_id",
            "com_val",
            "dto_rate",
            "subs_rate",
            "tax_rate",
            "net_val",
            "tax",
            "payments",
            "finan_rate",
            "inte_tax"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, prospectId);
        q.setParam(2, typeId);
        q.setParam(3, comVal);
        q.setParam(4, dtoRate);
        q.setParam(5, subsRate);
        q.setParam(6, taxRate);
        q.setParam(7, netVal);
        q.setParam(8, tax);
        q.setParam(9, payments);
        q.setParam(10, finanRate);
        q.setParam(11, inteTax);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        prospectId = MySQLQuery.getAsInteger(row[0]);
        typeId = MySQLQuery.getAsInteger(row[1]);
        comVal = MySQLQuery.getAsBigDecimal(row[2], false);
        dtoRate = MySQLQuery.getAsBigDecimal(row[3], false);
        subsRate = MySQLQuery.getAsBigDecimal(row[4], false);
        taxRate = MySQLQuery.getAsBigDecimal(row[5], false);
        netVal = MySQLQuery.getAsBigDecimal(row[6], false);
        tax = MySQLQuery.getAsBigDecimal(row[7], false);
        payments = MySQLQuery.getAsInteger(row[8]);
        finanRate = MySQLQuery.getAsBigDecimal(row[9], false);
        inteTax = MySQLQuery.getAsBigDecimal(row[10], false);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_prospect_service";
    }

    public static String getSelFlds(String alias) {
        return new BillProspectService().getSelFldsForAlias(alias);
    }

    public static List<BillProspectService> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillProspectService().getListFromQuery(q, conn);
    }

    public static List<BillProspectService> getList(Params p, Connection conn) throws Exception {
        return new BillProspectService().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillProspectService().deleteById(id, conn);
    }

    public static List<BillProspectService> getAll(Connection conn) throws Exception {
        return new BillProspectService().getAllList(conn);
    }

//fin zona de reemplazo
    
    public static List<BillProspectService> getByProspect(int prospectId,  Connection conn) throws Exception {
        Params p = new Params("prospectId", prospectId);
        return BillProspectService.getList(p, conn);
    }

}
