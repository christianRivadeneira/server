package printout.basics;

import java.sql.Connection;
import java.util.Date;
import printout.basics.rows.MtoChkRow;
import utilities.MySQLQuery;

public class MtoChkVal {
//inicio zona de reemplazo

    public Integer id;
    public Integer lstId;
    public Integer colId;
    public Integer rowId;
    public Integer workOrderId;
    public String val;
    public String state;
    public String correction;
    public Date corrDate;
    public String corrProv;
    public Integer corrEmployeeId;

    private static final String selFlds = "`lst_id`, "
            + "`col_id`, "
            + "`row_id`, "
            + "`work_order_id`, "
            + "`val`, "
            + "`state`, "
            + "`correction`, "
            + "`corr_date`, "
            + "`corr_prov`,"
            + "corr_employee_id";

    private static final String setFlds = "mto_chk_val SET "
            + "`lst_id` = ?1, "
            + "`col_id` = ?2, "
            + "`row_id` = ?3, "
            + "`work_order_id` = ?4, "
            + "`val` = ?5, "
            + "`state` = ?6, "
            + "`correction` = ?7, "
            + "`corr_date` = ?8, "
            + "`corr_prov` = ?9,"
            + "corr_employee_id = ?10";

    private void setFields(MtoChkVal obj, MySQLQuery q) {
        q.setParam(1, obj.lstId);
        q.setParam(2, obj.colId);
        q.setParam(3, obj.rowId);
        q.setParam(4, obj.workOrderId);
        q.setParam(5, obj.val);
        q.setParam(6, obj.state);
        q.setParam(7, obj.correction);
        q.setParam(8, obj.corrDate);
        q.setParam(9, obj.corrProv);
        q.setParam(10, obj.corrEmployeeId);
    }

    public MtoChkVal select(int id, Connection ep) throws Exception {
        return getFromRow(new MySQLQuery("SELECT " + selFlds + ", id FROM mto_chk_val WHERE id = " + id).getRecord(ep));
    }

    public static MtoChkVal getFromRow(Object[] row) throws Exception {
        MtoChkVal obj = new MtoChkVal();
        obj.lstId = (row[0] != null ? (Integer) row[0] : null);
        obj.colId = (row[1] != null ? (Integer) row[1] : null);
        obj.rowId = (row[2] != null ? (Integer) row[2] : null);
        obj.workOrderId = (row[3] != null ? (Integer) row[3] : null);
        obj.val = (row[4] != null ? (String) row[4] : null);
        obj.state = (row[5] != null ? row[5].toString() : null);
        obj.correction = (row[6] != null ? row[6].toString() : null);
        obj.corrDate = (row[7] != null ? (Date) row[7] : null);
        obj.corrProv = (row[8] != null ? row[8].toString() : null);
        obj.corrProv = (row[8] != null ? row[8].toString() : null);
        obj.corrEmployeeId = (row[9] != null ? (Integer) row[9] : null);
        obj.id = (Integer) row[10];
        return obj;
    }

//fin zona de reemplazo
    public static MtoChkVal[][] getAnswers(MtoChkRow[][] rowMat, int lstId, Connection ep) throws Exception {
        MtoChkVal[][] rta = new MtoChkVal[rowMat.length][];
        for (int i = 0; i < rowMat.length; i++) {
            MtoChkRow[] rows = rowMat[i];
            rta[i] = new MtoChkVal[rows.length];
            for (int j = 0; j < rows.length; j++) {
                Object[][] data = new MySQLQuery("SELECT " + selFlds + ", id FROM mto_chk_val WHERE row_id = " + rows[j].id + " AND lst_id = " + lstId).getRecords(ep);
                rta[i][j] = (data != null && data.length > 0 ? getFromRow(data[0]) : null);
            }
        }
        return rta;
    }

    public int insert(MtoChkVal obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + setFlds);
        setFields(obj, q);
        return q.executeInsert(ep);
    }

    public void update(MtoChkVal obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("UPDATE " + setFlds + " WHERE id = " + obj.id);
        setFields(obj, q);
        q.executeUpdate(ep);
    }

    public void delete(int id, Connection ep) throws Exception {
        new MySQLQuery("DELETE FROM mto_chk_val WHERE id = " + id).executeDelete(ep);
    }

    public String getEnumOptions(String fieldName) {
        if (fieldName.equals("state")) {
            return "error=error&warn=warn&ok=ok";
        }
        return null;
    }
}
