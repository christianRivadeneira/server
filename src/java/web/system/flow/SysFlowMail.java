package web.system.flow;

import java.sql.Connection;
import utilities.MySQLQuery;

public class SysFlowMail {
//inicio zona de reemplazo

    public int id;
    public int stepId;
    public String type;
    public Integer empId;
    public String query;
    public String msg;

    private static final String SEL_FLDS = "`step_id`, "
            + "`type`, "
            + "`emp_id`, "
            + "`query`, "
            + "`msg`";

    public static SysFlowMail getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        SysFlowMail obj = new SysFlowMail();
        obj.stepId = MySQLQuery.getAsInteger(row[0]);
        obj.type = MySQLQuery.getAsString(row[1]);
        obj.empId = MySQLQuery.getAsInteger(row[2]);
        obj.query = MySQLQuery.getAsString(row[3]);
        obj.msg = MySQLQuery.getAsString(row[4]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public static SysFlowMail[] getByStepId(int stepId, Connection conn) throws Exception {
        Object[][] data = new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM sys_flow_mail WHERE `step_id` = " + stepId).getRecords(conn);
        SysFlowMail[] rta = new SysFlowMail[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = getFromRow(data[i]);
        }
        return rta;
    }

    public SysFlowMail select(int id, Connection conn) throws Exception {
        return SysFlowMail.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(conn));
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM sys_flow_mail WHERE id = " + id;
    }
}
