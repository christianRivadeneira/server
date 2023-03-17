package web.system.flow;

import java.sql.Connection;
import utilities.MySQLQuery;

public class SysFlowConn {
//inicio zona de reemplazo

    public int id;
    public String name;
    public int fromStepId;
    public int toStepId;
    public boolean reqComment;
    public boolean draw;
    public String type;
    public String fromDir;
    public String toDir;
    public Integer fromSlot;
    public Integer toSlot;

    private static final String SEL_FLDS = "`name`, "
            + "`from_step_id`, "
            + "`to_step_id`, "
            + "`req_comment`, "
            + "`draw`, "
            + "`type`, "
            + "`from_dir`, "
            + "`to_dir`, "
            + "`from_slot`, "
            + "`to_slot`";

    public static SysFlowConn getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        SysFlowConn obj = new SysFlowConn();
        obj.name = MySQLQuery.getAsString(row[0]);
        obj.fromStepId = MySQLQuery.getAsInteger(row[1]);
        obj.toStepId = MySQLQuery.getAsInteger(row[2]);
        obj.reqComment = MySQLQuery.getAsBoolean(row[3]);
        obj.draw = MySQLQuery.getAsBoolean(row[4]);
        obj.type = MySQLQuery.getAsString(row[5]);
        obj.fromDir = MySQLQuery.getAsString(row[6]);
        obj.toDir = MySQLQuery.getAsString(row[7]);
        obj.fromSlot = MySQLQuery.getAsInteger(row[8]);
        obj.toSlot = MySQLQuery.getAsInteger(row[9]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public static SysFlowConn[] getFromData(Object[][] data) throws Exception {
        SysFlowConn[] rta = new SysFlowConn[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = getFromRow(data[i]);
        }
        return rta;
    }

    public static SysFlowConn getFromQuery(String q, Connection ep) throws Exception {
        return getFromRow(new MySQLQuery(q).getRecord(ep));
    }

    public static SysFlowConn select(int id, Connection con) throws Exception {
        return getFromQuery("SELECT " + SEL_FLDS + ", id FROM sys_flow_conn WHERE id = " + id, con);
    }

    public static SysFlowConn[] getByStep(int parId, Connection con) throws Exception {
        return getFromData(new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM sys_flow_conn WHERE from_step_id = " + parId + " ORDER BY `name`").getRecords(con));
    }

    public String getEnumOptions(String fieldName) {
        if (fieldName.equals("type")) {
            return "pos=Positiva&neg=Negativa&mult=Múltiple";
        }
        return null;
    }

    public String getEnumOptions(String fieldName, String parNodeType) {
        if (fieldName.equals("type")) {
            switch (parNodeType) {
                case "start":
                    return "pos=Positiva";
                case "gate":
                    return "pos=Positiva&neg=Negativa";
                case "task":
                    return "pos=Positiva&neg=Negativa&mult=Múltiple";
                case "end":
                    return "pos=Positiva&neg=Negativa&mult=Múltiple";
                default:
                    throw new RuntimeException("Tipo no reconocido: " + parNodeType);
            }
        }
        return null;
    }
}
