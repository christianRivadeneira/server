package web.system.flow;

import java.sql.Connection;
import utilities.MySQLQuery;

public class SysFlowStepSubs {
//inicio zona de reemplazo

    public int id;
    public String name;
    public int stepId;
    public String usrType;
    public String type;
    public Integer empId;
    public String empQ;
    public boolean disposable;
    public String rol;

    private static final String SEL_FLDS = "`name`, "
            + "`step_id`, "
            + "`usr_type`, "
            + "`type`, "
            + "`emp_id`, "
            + "`emp_q`, "
            + "`disposable`, "
            + "`rol`";

    public static SysFlowStepSubs getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        SysFlowStepSubs obj = new SysFlowStepSubs();
        obj.name = MySQLQuery.getAsString(row[0]);
        obj.stepId = MySQLQuery.getAsInteger(row[1]);
        obj.usrType = MySQLQuery.getAsString(row[2]);
        obj.type = MySQLQuery.getAsString(row[3]);
        obj.empId = MySQLQuery.getAsInteger(row[4]);
        obj.empQ = MySQLQuery.getAsString(row[5]);
        obj.disposable = MySQLQuery.getAsBoolean(row[6]);
        obj.rol = MySQLQuery.getAsString(row[7]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public static SysFlowStepSubs[] getByStep(int stepId, String type, Connection ep) throws Exception {
        Object[][] data = new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM sys_flow_step_subs WHERE step_id = " + stepId + " AND type = ?1").setParam(1, type).getRecords(ep);
        SysFlowStepSubs[] rta = new SysFlowStepSubs[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = getFromRow(data[i]);
        }
        return rta;
    }

    public SysFlowStepSubs select(int id, Connection ep) throws Exception {
        return SysFlowStepSubs.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(ep));
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM sys_flow_step_subs WHERE id = " + id;
    }
}
