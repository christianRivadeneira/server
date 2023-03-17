package api.bill.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import metadata.model.Table;
import utilities.MySQLQuery;
import web.billing.BillingServlet;

public class BillInstance extends BaseModel<BillInstance> {
//inicio zona de reemplazo

    public String name;
    public String cycle;
    public String cgCoCode;
    public String cgZoneCode;
    public String db;
    public String type;
    public boolean siteBilling;
    public boolean sendMail;
    public boolean sendSms;
    public Integer pobId;
    public Integer marketId;
    public Integer cityId;
    public Integer odorantId;
    public Integer upperCriticalReadRateR;
    public Integer lowerCriticalReadRateR;
    public Integer upperCriticalReadRateNr;
    public Integer lowerCriticalReadRateNr;
    public Integer ipliIoRate;
    public Integer instCheckMonths;
    public Integer meterCheckMonths;
    public Integer suspDebtMonths;
    public Date minPaymentDate;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "cycle",
            "cg_co_code",
            "cg_zone_code",
            "db",
            "type",
            "site_billing",
            "send_mail",
            "send_sms",
            "pob_id",
            "market_id",
            "city_id",
            "odorant_id",
            "upper_critical_read_rate_r",
            "lower_critical_read_rate_r",
            "upper_critical_read_rate_nr",
            "lower_critical_read_rate_nr",
            "ipli_io_rate",
            "inst_check_months",
            "meter_check_months",
            "susp_debt_months",
            "min_payment_date"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, cycle);
        q.setParam(3, cgCoCode);
        q.setParam(4, cgZoneCode);
        q.setParam(5, db);
        q.setParam(6, type);
        q.setParam(7, siteBilling);
        q.setParam(8, sendMail);
        q.setParam(9, sendSms);
        q.setParam(10, pobId);
        q.setParam(11, marketId);
        q.setParam(12, cityId);
        q.setParam(13, odorantId);
        q.setParam(14, upperCriticalReadRateR);
        q.setParam(15, lowerCriticalReadRateR);
        q.setParam(16, upperCriticalReadRateNr);
        q.setParam(17, lowerCriticalReadRateNr);
        q.setParam(18, ipliIoRate);
        q.setParam(19, instCheckMonths);
        q.setParam(20, meterCheckMonths);
        q.setParam(21, suspDebtMonths);
        q.setParam(22, minPaymentDate);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        cycle = MySQLQuery.getAsString(row[1]);
        cgCoCode = MySQLQuery.getAsString(row[2]);
        cgZoneCode = MySQLQuery.getAsString(row[3]);
        db = MySQLQuery.getAsString(row[4]);
        type = MySQLQuery.getAsString(row[5]);
        siteBilling = MySQLQuery.getAsBoolean(row[6]);
        sendMail = MySQLQuery.getAsBoolean(row[7]);
        sendSms = MySQLQuery.getAsBoolean(row[8]);
        pobId = MySQLQuery.getAsInteger(row[9]);
        marketId = MySQLQuery.getAsInteger(row[10]);
        cityId = MySQLQuery.getAsInteger(row[11]);
        odorantId = MySQLQuery.getAsInteger(row[12]);
        upperCriticalReadRateR = MySQLQuery.getAsInteger(row[13]);
        lowerCriticalReadRateR = MySQLQuery.getAsInteger(row[14]);
        upperCriticalReadRateNr = MySQLQuery.getAsInteger(row[15]);
        lowerCriticalReadRateNr = MySQLQuery.getAsInteger(row[16]);
        ipliIoRate = MySQLQuery.getAsInteger(row[17]);
        instCheckMonths = MySQLQuery.getAsInteger(row[18]);
        meterCheckMonths = MySQLQuery.getAsInteger(row[19]);
        suspDebtMonths = MySQLQuery.getAsInteger(row[20]);
        minPaymentDate = MySQLQuery.getAsDate(row[21]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_instance";
    }

    public static String getSelFlds(String alias) {
        return new BillInstance().getSelFldsForAlias(alias);
    }

    public static List<BillInstance> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillInstance().getListFromQuery(q, conn);
    }

    public static List<BillInstance> getList(Params p, Connection conn) throws Exception {
        return new BillInstance().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillInstance().deleteById(id, conn);
    }

    public static List<BillInstance> getAll(Connection conn) throws Exception {
        List<BillInstance> insts = new BillInstance().getListFromQuery(new MySQLQuery("SELECT " + getSelFlds("")
                + " FROM sigma.bill_instance"), conn);
        Iterator<BillInstance> it = insts.iterator();
        while (it.hasNext()) {
            BillInstance inst = it.next();
            if (Table.DEVEL_MODE && new MySQLQuery("SHOW DATABASES LIKE '" + inst.db + "';").getRecords(conn).length == 0) {
                it.remove();
            }
        }
        return insts;
    }

//fin zona de reemplazo
    public static List<BillInstance> getAllNet(Connection conn) throws Exception {
        List<BillInstance> insts = new BillInstance().getListFromQuery(new MySQLQuery("SELECT " + getSelFlds("")
                + " FROM sigma.bill_instance WHERE type = 'net' "), conn);
        Iterator<BillInstance> it = insts.iterator();
        while (it.hasNext()) {
            BillInstance inst = it.next();
            if (Table.DEVEL_MODE && new MySQLQuery("SHOW DATABASES LIKE '" + inst.db + "';").getRecords(conn).length == 0) {
                it.remove();
            }
        }
        return insts;
    }

    public static List<BillInstance> getByMarket(int marketId, Connection conn) throws Exception {
        List<BillInstance> insts = getList(new Params("marketId", marketId).orderBy("name"), conn);
        Iterator<BillInstance> it = insts.iterator();
        while (it.hasNext()) {
            BillInstance inst = it.next();
            if (Table.DEVEL_MODE && new MySQLQuery("SHOW DATABASES LIKE '" + inst.db + "';").getRecords(conn).length == 0) {
                it.remove();
            }
        }
        return insts;
    }

    /**
     * Instancias que ya estén facturando
     *
     * @param marketId
     * @param conn
     * @return
     * @throws Exception
     */
    public static List<BillInstance> getBillingByMarket(int marketId, Connection conn) throws Exception {
        List<BillInstance> lst = getByMarket(marketId, conn);
        Iterator<BillInstance> it = lst.iterator();
        while (it.hasNext()) {
            BillInstance inst = it.next();
            if (new MySQLQuery("SELECT COUNT(*) = 0 FROM " + inst.db + ".bill_span;").getAsBoolean(conn)) {
                it.remove();
            }
        }
        return lst;
    }

    public boolean isTankInstance() {
        return type.equals("tank");
    }

    public boolean isNetInstance() {
        return type.equals("net");
    }

    public Connection useInstance(Connection conn) throws Exception {
        new MySQLQuery("USE " + BillingServlet.getDbName(id)).executeUpdate(conn);
        return conn;
    }

    public Connection useDefault(Connection conn) throws Exception {
        new MySQLQuery("USE sigma").executeUpdate(conn);
        return conn;
    }

}
