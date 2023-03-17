package web.system.flow;

import java.sql.Connection;
import utilities.MySQLQuery;

public class SysFlowQuery {
//inicio zona de reemplazo

    public int id;
    public int stepId;
    public String type;
    public boolean java;
    public String query;
    public String msg;

    private static final String SEL_FLDS = "`step_id`, "
            + "`type`, "
            + "`java`, "
            + "`query`, "
            + "`msg`";

    public static SysFlowQuery getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        SysFlowQuery obj = new SysFlowQuery();
        obj.stepId = MySQLQuery.getAsInteger(row[0]);
        obj.type = MySQLQuery.getAsString(row[1]);
        obj.java = MySQLQuery.getAsBoolean(row[2]);
        obj.query = MySQLQuery.getAsString(row[3]);
        obj.msg = MySQLQuery.getAsString(row[4]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public static SysFlowQuery[] getByStepId(int stepId, String type, Connection ep) throws Exception {
        Object[][] data = new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM sys_flow_query WHERE step_id = " + stepId + " AND type = ?1").setParam(1, type).getRecords(ep);
        SysFlowQuery[] rta = new SysFlowQuery[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = getFromRow(data[i]);
        }
        return rta;
    }

    public static SysFlowQuery select(int id, Connection ep) throws Exception {
        return SysFlowQuery.getFromRow(new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM sys_flow_query WHERE id = " + id).getRecord(ep));
    }

    public String getEnumOptions(String fieldName) {
        if (fieldName.equals("type")) {
            return "val=Validación&upd=Update";
        }
        return null;
    }
}
