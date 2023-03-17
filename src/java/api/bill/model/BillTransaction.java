package api.bill.model;

import api.BaseModel;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import utilities.MySQLParametrizable;
import utilities.MySQLPreparedInsert;
import utilities.MySQLPreparedUpdate;
import utilities.MySQLQuery;

public class BillTransaction extends BaseModel<BillTransaction> {

    //debe ser el número de campos más 1
    private static final int ID_PARAM = 16;

//inicio zona de reemplazo
    public int cliTankId;
    public int billSpanId;
    public BigDecimal value;
    public int accountDebId;
    public int accountCredId;
    public int transTypeId;
    public String docType;
    public Integer docId;
    public int creUsuId;
    public Integer modUsuId;
    public Date created;
    public Date modified;
    public Integer billBankId;
    public Integer extraId;
    public String extra;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "cli_tank_id",
            "bill_span_id",
            "value",
            "account_deb_id",
            "account_cred_id",
            "trans_type_id",
            "doc_type",
            "doc_id",
            "cre_usu_id",
            "mod_usu_id",
            "created",
            "modified",
            "bill_bank_id",
            "extra_id",
            "extra"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, cliTankId);
        q.setParam(2, billSpanId);
        q.setParam(3, value);
        q.setParam(4, accountDebId);
        q.setParam(5, accountCredId);
        q.setParam(6, transTypeId);
        q.setParam(7, docType);
        q.setParam(8, docId);
        q.setParam(9, creUsuId);
        q.setParam(10, modUsuId);
        q.setParam(11, created);
        q.setParam(12, modified);
        q.setParam(13, billBankId);
        q.setParam(14, extraId);
        q.setParam(15, extra);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        cliTankId = MySQLQuery.getAsInteger(row[0]);
        billSpanId = MySQLQuery.getAsInteger(row[1]);
        value = MySQLQuery.getAsBigDecimal(row[2], false);
        accountDebId = MySQLQuery.getAsInteger(row[3]);
        accountCredId = MySQLQuery.getAsInteger(row[4]);
        transTypeId = MySQLQuery.getAsInteger(row[5]);
        docType = MySQLQuery.getAsString(row[6]);
        docId = MySQLQuery.getAsInteger(row[7]);
        creUsuId = MySQLQuery.getAsInteger(row[8]);
        modUsuId = MySQLQuery.getAsInteger(row[9]);
        created = MySQLQuery.getAsDate(row[10]);
        modified = MySQLQuery.getAsDate(row[11]);
        billBankId = MySQLQuery.getAsInteger(row[12]);
        extraId = MySQLQuery.getAsInteger(row[13]);
        extra = MySQLQuery.getAsString(row[14]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_transaction";
    }

    public static String getSelFlds(String alias) {
        return new BillTransaction().getSelFldsForAlias(alias);
    }

    public static List<BillTransaction> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillTransaction().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillTransaction().deleteById(id, conn);
    }

    public static List<BillTransaction> getAll(Connection conn) throws Exception {
        return new BillTransaction().getAllList(conn);
    }

//fin zona de reemplazo
    //se hace con base en el prepareQuery
    private static void setFields(BillTransaction bill, MySQLParametrizable q) throws SQLException {
        q.setParameter(1, bill.cliTankId);
        q.setParameter(2, bill.billSpanId);
        q.setParameter(3, bill.value);
        q.setParameter(4, bill.accountDebId);
        q.setParameter(5, bill.accountCredId);
        q.setParameter(6, bill.transTypeId);
        q.setParameter(7, bill.docType);
        q.setParameter(8, bill.docId);
        q.setParameter(9, bill.creUsuId);
        q.setParameter(10, bill.modUsuId);
        q.setParameter(11, bill.created);
        q.setParameter(12, bill.modified);
        q.setParameter(13, bill.billBankId);
        q.setParameter(14, bill.extraId);
        q.setParameter(15, bill.extra);
    }

    public static String calculateSetFlds() throws Exception {
        StringBuilder sb = new StringBuilder("bill_transaction SET ");
        String[] flds = new BillTransaction().getFlds();
        for (int i = 0; i < flds.length; i++) {
            String fld = flds[i];
            sb.append("`").append(fld).append("` = ?").append(i + 1).append("");
            if (i < flds.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public static MySQLPreparedInsert getInsertQuery(boolean returnKeys, Connection conn) throws Exception {
        String SET_FLDS = calculateSetFlds();
        return new MySQLPreparedInsert("INSERT INTO " + SET_FLDS, returnKeys, conn);
    }

    public static MySQLPreparedUpdate getUpdateQuery(Connection conn) throws Exception {
        String SET_FLDS = calculateSetFlds();
        return new MySQLPreparedUpdate("UPDATE " + SET_FLDS + " WHERE id = ?" + ID_PARAM, conn);
    }

    public static void insert(BillTransaction bill, MySQLPreparedInsert q) throws Exception {
        setFields(bill, q);
        q.addBatch();
    }

    public static void update(BillTransaction bill, MySQLPreparedUpdate q) throws Exception {
        setFields(bill, q);
        q.setParameter(ID_PARAM, bill.id);
        q.addBatch();
    }

    public static List<BillTransaction> getByDoc(int docId, String docType, Connection conn) throws Exception {
        return getList(new MySQLQuery("SELECT " + getSelFlds("") + ", id FROM bill_transaction WHERE doc_type = '" + docType + "' AND doc_id = " + docId), conn);
    }

    public static List< BillTransaction> getByAccount(int account, int clientId, Connection conn) throws Exception {
        return getList(new MySQLQuery("SELECT " + getSelFlds("") + ", id FROM bill_transaction as t WHERE (t.account_deb_id = ?1 OR t.account_cred_id = ?1) AND t.cli_tank_id = " + clientId + " ORDER BY t.bill_span_id DESC, t.created DESC, t.id DESC").setParam(1, account), conn);
    }

}
