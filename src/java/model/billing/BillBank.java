package model.billing;

import java.sql.Connection;
import utilities.MySQLQuery;

public class BillBank {
//inicio zona de reemplazo

    public int id;
    public String name;
    public String numAccount;
    public Integer asocCode;
    public Integer asocType;
    public String asocAccount;
    public String billLabel;

    private static final String SEL_FLDS = "`name`, "
            + "`num_account`, "
            + "`asoc_code`, "
            + "`asoc_type`, "
            + "`asoc_account`, "
            + "`bill_label`";

    private static final String SET_FLDS = "bill_bank SET "
            + "`name` = ?1, "
            + "`num_account` = ?2, "
            + "`asoc_code` = ?3, "
            + "`asoc_type` = ?4, "
            + "`asoc_account` = ?5, "
            + "`bill_label` = ?6";

    private static void setFields(BillBank obj, MySQLQuery q) {
        q.setParam(1, obj.name);
        q.setParam(2, obj.numAccount);
        q.setParam(3, obj.asocCode);
        q.setParam(4, obj.asocType);
        q.setParam(5, obj.asocAccount);
        q.setParam(6, obj.billLabel);

    }

    public static BillBank getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        BillBank obj = new BillBank();
        obj.name = MySQLQuery.getAsString(row[0]);
        obj.numAccount = MySQLQuery.getAsString(row[1]);
        obj.asocCode = MySQLQuery.getAsInteger(row[2]);
        obj.asocType = MySQLQuery.getAsInteger(row[3]);
        obj.asocAccount = MySQLQuery.getAsString(row[4]);
        obj.billLabel = MySQLQuery.getAsString(row[5]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public static BillBank select(int id, Connection conn) throws Exception {
        return BillBank.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(conn));
    }

    public static BillBank[] getAll(Connection conn) throws Exception {
        Object[][] data = new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM bill_bank ORDER BY name").getRecords(conn);
        BillBank[] rta = new BillBank[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = getFromRow(data[i]);
        }
        return rta;
    }

    public static BillBank getByAsobData(int code, int type, String account, Connection conn) throws Exception {
        return BillBank.getFromRow(new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM bill_bank WHERE asoc_code = ?1 AND asoc_type = ?2 AND asoc_account = ?3").setParam(1, code).setParam(2, type).setParam(3, account).getRecord(conn));
    }

    public static int insert(BillBank pobj, Connection conn) throws Exception {
        BillBank obj = (BillBank) pobj;
        int nId = new MySQLQuery(BillBank.getInsertQuery(obj)).executeInsert(conn);
        obj.id = nId;
        return nId;
    }

    public static void update(BillBank pobj, Connection conn) throws Exception {
        new MySQLQuery(BillBank.getUpdateQuery((BillBank) pobj)).executeUpdate(conn);
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM bill_bank WHERE id = " + id;
    }

    public static String getInsertQuery(BillBank obj) {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public static String getUpdateQuery(BillBank obj) {
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MySQLQuery("DELETE FROM bill_bank WHERE id = " + id).executeDelete(conn);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumAccount() {
        return numAccount;
    }

    public void setNumAccount(String numAccount) {
        this.numAccount = numAccount;
    }

    public Integer getAsocCode() {
        return asocCode;
    }

    public void setAsocCode(Integer asocCode) {
        this.asocCode = asocCode;
    }

    public Integer getAsocType() {
        return asocType;
    }

    public void setAsocType(Integer asocType) {
        this.asocType = asocType;
    }

    public String getAsocAccount() {
        return asocAccount;
    }

    public void setAsocAccount(String asocAccount) {
        this.asocAccount = asocAccount;
    }

}
