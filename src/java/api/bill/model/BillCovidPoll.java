package api.bill.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class BillCovidPoll extends BaseModel<BillCovidPoll> {
//inicio zona de reemplazo

    public int instId;
    public int clientId;
    public int billId;
    public int payments;
    public Date dt;
    public String names;
    public String phone;
    public String mail;
    public String code;
    public boolean sendPromo;
    public boolean stove;
    public boolean heater;
    public boolean chimney;
    public boolean washer;
    public boolean mailSent;
    public boolean smsSent;
    public boolean confirmedCode;
    public String other;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "inst_id",
            "client_id",
            "bill_id",
            "payments",
            "dt",
            "names",
            "phone",
            "mail",
            "code",
            "send_promo",
            "stove",
            "heater",
            "chimney",
            "washer",
            "mail_sent",
            "sms_sent",
            "confirmed_code",
            "other"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, instId);
        q.setParam(2, clientId);
        q.setParam(3, billId);
        q.setParam(4, payments);
        q.setParam(5, dt);
        q.setParam(6, names);
        q.setParam(7, phone);
        q.setParam(8, mail);
        q.setParam(9, code);
        q.setParam(10, sendPromo);
        q.setParam(11, stove);
        q.setParam(12, heater);
        q.setParam(13, chimney);
        q.setParam(14, washer);
        q.setParam(15, mailSent);
        q.setParam(16, smsSent);
        q.setParam(17, confirmedCode);
        q.setParam(18, other);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        instId = MySQLQuery.getAsInteger(row[0]);
        clientId = MySQLQuery.getAsInteger(row[1]);
        billId = MySQLQuery.getAsInteger(row[2]);
        payments = MySQLQuery.getAsInteger(row[3]);
        dt = MySQLQuery.getAsDate(row[4]);
        names = MySQLQuery.getAsString(row[5]);
        phone = MySQLQuery.getAsString(row[6]);
        mail = MySQLQuery.getAsString(row[7]);
        code = MySQLQuery.getAsString(row[8]);
        sendPromo = MySQLQuery.getAsBoolean(row[9]);
        stove = MySQLQuery.getAsBoolean(row[10]);
        heater = MySQLQuery.getAsBoolean(row[11]);
        chimney = MySQLQuery.getAsBoolean(row[12]);
        washer = MySQLQuery.getAsBoolean(row[13]);
        mailSent = MySQLQuery.getAsBoolean(row[14]);
        smsSent = MySQLQuery.getAsBoolean(row[15]);
        confirmedCode = MySQLQuery.getAsBoolean(row[16]);
        other = MySQLQuery.getAsString(row[17]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_covid_poll";
    }

    public static String getSelFlds(String alias) {
        return new BillCovidPoll().getSelFldsForAlias(alias);
    }

    public static List<BillCovidPoll> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillCovidPoll().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillCovidPoll().deleteById(id, conn);
    }

    public static List<BillCovidPoll> getAll(Connection conn) throws Exception {
        return new BillCovidPoll().getAllList(conn);
    }

//fin zona de reemplazo
}