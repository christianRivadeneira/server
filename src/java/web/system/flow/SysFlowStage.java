package web.system.flow;

import java.sql.Connection;
import utilities.MySQLQuery;

public class SysFlowStage {
//inicio zona de reemplazo

    public int id;
    public String name;
    public String sname;
    public int typeId;
    public String extra;
    public int place;
    public String serialQuery;

    private static final String SEL_FLDS = "`name`, "
            + "`sname`, "
            + "`type_id`, "
            + "`extra`, "
            + "`place`, "
            + "`serial_query`";

    private static final String SET_FLDS = "sys_flow_stage SET "
            + "`name` = ?1, "
            + "`sname` = ?2, "
            + "`type_id` = ?3, "
            + "`extra` = ?4, "
            + "`place` = ?5, "
            + "`serial_query` = ?6";

    private static void setFields(SysFlowStage obj, MySQLQuery q) {
        q.setParam(1, obj.name);
        q.setParam(2, obj.sname);
        q.setParam(3, obj.typeId);
        q.setParam(4, obj.extra);
        q.setParam(5, obj.place);
        q.setParam(6, obj.serialQuery);

    }

    public static SysFlowStage getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        SysFlowStage obj = new SysFlowStage();
        obj.name = MySQLQuery.getAsString(row[0]);
        obj.sname = MySQLQuery.getAsString(row[1]);
        obj.typeId = MySQLQuery.getAsInteger(row[2]);
        obj.extra = MySQLQuery.getAsString(row[3]);
        obj.place = MySQLQuery.getAsInteger(row[4]);
        obj.serialQuery = MySQLQuery.getAsString(row[5]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public static String getSerialQueryByStepId(int stepId, Connection conn) throws Exception {
        String rta = new MySQLQuery("SELECT serial_query  "
                + "FROM sys_flow_stage stage "
                + "INNER JOIN sys_flow_step step ON step.stage_id = stage.id "
                + "WHERE "
                + "step.id = " + stepId).getAsString(conn);
        return rta;
    }

    public SysFlowStage select(int id, Connection conn) throws Exception {
        return SysFlowStage.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(conn));
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM sys_flow_mail WHERE id = " + id;
    }
}
