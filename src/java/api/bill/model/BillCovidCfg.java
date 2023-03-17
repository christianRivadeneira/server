package api.bill.model;

import api.BaseModel;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class BillCovidCfg extends BaseModel<BillCovidCfg> {
//inicio zona de reemplazo

    public boolean enabled;
    public boolean autoFinance;
    public boolean allowDebt;
    public BigDecimal finanRate;
    public BigDecimal timelyPaymentBonusRate;
    public int timelyPaymentMaxDay;
    public Date paymentsStart;
    public Date firstBill;
    public Date lastBill;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "enabled",
            "auto_finance",
            "allow_debt",
            "finan_rate",
            "timely_payment_bonus_rate",
            "timely_payment_max_day",
            "payments_start",
            "first_bill",
            "last_bill"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, enabled);
        q.setParam(2, autoFinance);
        q.setParam(3, allowDebt);
        q.setParam(4, finanRate);
        q.setParam(5, timelyPaymentBonusRate);
        q.setParam(6, timelyPaymentMaxDay);
        q.setParam(7, paymentsStart);
        q.setParam(8, firstBill);
        q.setParam(9, lastBill);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        enabled = MySQLQuery.getAsBoolean(row[0]);
        autoFinance = MySQLQuery.getAsBoolean(row[1]);
        allowDebt = MySQLQuery.getAsBoolean(row[2]);
        finanRate = MySQLQuery.getAsBigDecimal(row[3], false);
        timelyPaymentBonusRate = MySQLQuery.getAsBigDecimal(row[4], false);
        timelyPaymentMaxDay = MySQLQuery.getAsInteger(row[5]);
        paymentsStart = MySQLQuery.getAsDate(row[6]);
        firstBill = MySQLQuery.getAsDate(row[7]);
        lastBill = MySQLQuery.getAsDate(row[8]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_covid_cfg";
    }

    public static String getSelFlds(String alias) {
        return new BillCovidCfg().getSelFldsForAlias(alias);
    }

    public static List<BillCovidCfg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillCovidCfg().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillCovidCfg().deleteById(id, conn);
    }

    public static List<BillCovidCfg> getAll(Connection conn) throws Exception {
        return new BillCovidCfg().getAllList(conn);
    }

//fin zona de reemplazo
}
