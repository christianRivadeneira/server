package model.billing;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import utilities.MySQLParametrizable;
import utilities.MySQLPreparedInsert;
import utilities.MySQLQuery;

/**
 * *
 * El id debe ser Integer y no int
 *
 * @author Mario
 */
public class BillBillPres {
    
    public BillBillPres() {

    }

    public BillBillPres(int billId, String label, BigDecimal value, int pos, String type) {
        this.id = null;
        this.billId = billId;
        this.label = label;
        this.value = value;
        this.pos = pos;
        this.bold = false;
        this.type = type;
    }

    public BillBillPres(int billId, String label, BigDecimal value, int pos, boolean bold) {
        this.id = null;
        this.billId = billId;
        this.label = label;
        this.value = value;
        this.pos = pos;
        this.bold = bold;
        this.type = "det";
    }

//inicio zona de reemplazo
    public Integer id;
    public int billId;
    public String label;
    public BigDecimal value;
    public int pos;
    public boolean bold;
    public String type;

    private static final String SEL_FLDS = "`bill_id`, "
            + "`label`, "
            + "`value`, "
            + "`pos`, "
            + "`bold`,"
            + "`type`";

    private static final String SET_FLDS = "bill_bill_pres SET "
            + "`bill_id` = ?1, "
            + "`label` = ?2, "
            + "`value` = ?3, "
            + "`pos` = ?4, "
            + "`bold` = ?5,"
            + "`type` = ?6";

    private static void setFields(BillBillPres obj, MySQLParametrizable q) throws SQLException {
        q.setParameter(1, obj.billId);
        q.setParameter(2, obj.label);
        q.setParameter(3, obj.value);
        q.setParameter(4, obj.pos);
        q.setParameter(5, obj.bold);
        q.setParameter(6, obj.type);

    }

    public static BillBillPres getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        BillBillPres obj = new BillBillPres();
        obj.billId = MySQLQuery.getAsInteger(row[0]);
        obj.label = MySQLQuery.getAsString(row[1]);
        obj.value = MySQLQuery.getAsBigDecimal(row[2], false);
        obj.pos = MySQLQuery.getAsInteger(row[3]);
        obj.bold = MySQLQuery.getAsBoolean(row[4]);
        obj.type = MySQLQuery.getAsString(row[5]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public static BillBillPres[] getByBillId(int billId, Connection conn) throws Exception {
        Object[][] data = new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM bill_bill_pres WHERE bill_id = " + billId + " ORDER BY pos ASC").getRecords(conn);
        BillBillPres[] rta = new BillBillPres[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = getFromRow(data[i]);
        }
        return rta;
    }

    public static MySQLPreparedInsert getInsertQuery(Connection conn) throws SQLException {
        return new MySQLPreparedInsert("INSERT INTO " + SET_FLDS, false, conn);
    }

    public static void insert(BillBillPres bill, MySQLPreparedInsert q) throws Exception {
        setFields(bill, q);
        q.addBatch();
    }

}
