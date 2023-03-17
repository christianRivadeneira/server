package api.bill.model;

import api.BaseModel;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLPreparedInsert;
import utilities.MySQLQuery;

public class BillPlan extends BaseModel<BillPlan> {

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
    public BigDecimal credit;
    public boolean prev;

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
            "credit",
            "prev"
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
        q.setParam(13, credit);
        q.setParam(14, prev);
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
        credit = MySQLQuery.getAsBigDecimal(row[12], false);
        prev = MySQLQuery.getAsBoolean(row[13]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_plan";
    }

    public static String getSelFlds(String alias) {
        return new BillPlan().getSelFldsForAlias(alias);
    }

    public static List<BillPlan> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillPlan().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillPlan().deleteById(id, conn);
    }

    public static List<BillPlan> getAll(Connection conn) throws Exception {
        return new BillPlan().getAllList(conn);
    }

//fin zona de reemplazo
    private static String calculateSetFlds() throws Exception {
        StringBuilder sb = new StringBuilder("bill_plan SET ");
        String[] flds = new BillPlan().getFlds();
        for (int i = 0; i < flds.length; i++) {
            String fld = flds[i];
            sb.append("`").append(fld).append("` = ?").append(i + 1).append("");
            if (i < flds.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    //se hace con base en el prepareQuery
    private static void setFields(BillPlan obj, MySQLPreparedInsert q) throws Exception {
        q.setParameter(1, obj.cliTankId);
        q.setParameter(2, obj.billSpanId);
        q.setParameter(3, obj.value);
        q.setParameter(4, obj.accountDebId);
        q.setParameter(5, obj.accountCredId);
        q.setParameter(6, obj.transTypeId);
        q.setParameter(7, obj.docType);
        q.setParameter(8, obj.docId);
        q.setParameter(9, obj.creUsuId);
        q.setParameter(10, obj.modUsuId);
        q.setParameter(11, obj.created);
        q.setParameter(12, obj.modified);
        q.setParameter(13, obj.credit);
        q.setParameter(14, obj.prev);
    }

    public static MySQLPreparedInsert getInsertQuery(Connection conn) throws Exception {        
        System.out.println("INSERT INTO " + calculateSetFlds());        
        return new MySQLPreparedInsert("INSERT INTO " + calculateSetFlds(), false, conn);
    }

    public static void insert(BillPlan plan, MySQLPreparedInsert q) throws Exception {
        setFields(plan, q);
        q.addBatch();
    }

}
