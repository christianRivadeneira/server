package web.system.flow;

import java.sql.Connection;
import java.util.Date;
import utilities.MySQLQuery;

public class SysFlowStep {

//inicio zona de reemplazo
    public int id;
    public String name;
    public int typeId;
    public String nodeType;
    public String respType;
    public Integer stageId;
    public Integer respId;
    public String respQ;
    public String gateQ;
    public String splitter;
    public Integer x;
    public Integer y;
    public boolean reject;
    public boolean active;

    private static final String SEL_FLDS = "`name`, "
            + "`type_id`, "
            + "`node_type`, "
            + "`resp_type`, "
            + "`stage_id`, "
            + "`resp_id`, "
            + "`resp_q`, "
            + "`gate_q`, "
            + "`splitter`, "
            + "`x`, "
            + "`y`, "
            + "`reject`, "
            + "`active`";

    public static SysFlowStep getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        SysFlowStep obj = new SysFlowStep();
        obj.name = MySQLQuery.getAsString(row[0]);
        obj.typeId = MySQLQuery.getAsInteger(row[1]);
        obj.nodeType = MySQLQuery.getAsString(row[2]);
        obj.respType = MySQLQuery.getAsString(row[3]);
        obj.stageId = MySQLQuery.getAsInteger(row[4]);
        obj.respId = MySQLQuery.getAsInteger(row[5]);
        obj.respQ = MySQLQuery.getAsString(row[6]);
        obj.gateQ = MySQLQuery.getAsString(row[7]);
        obj.splitter = MySQLQuery.getAsString(row[8]);
        obj.x = MySQLQuery.getAsInteger(row[9]);
        obj.y = MySQLQuery.getAsInteger(row[10]);
        obj.reject = MySQLQuery.getAsBoolean(row[11]);
        obj.active = MySQLQuery.getAsBoolean(row[12]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public static SysFlowStep[] getAll(Connection ep, int typeId) throws Exception {
        Object[][] data = new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM sys_flow_step WHERE type_id = " + typeId).getRecords(ep);
        SysFlowStep[] rta = new SysFlowStep[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = getFromRow(data[i]);
        }
        return rta;
    }

    public static SysFlowStep[] getByHist(int reqId, Connection ep) throws Exception {
        Date lastReject = new MySQLQuery("select max(c.create_dt) from \n"
                + "sys_flow_chk c \n"
                + "inner join sys_flow_step s on s.id = c.from_step_id\n"
                + "where c.req_id = " + reqId + " and s.reject and c.check_dt is not null").getAsDate(ep);

        Object[][] data = new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM sys_flow_step WHERE id IN (select c.from_step_id from sys_flow_chk c where "
                + "c.emp_id IS NOT NULL "
                + "AND c.req_id = " + reqId + " "
                + (lastReject != null ? "AND c.create_dt >= ?1 " : " ")
                + "order by c.id)").setParam(1, lastReject).getRecords(ep);
        SysFlowStep[] rta = new SysFlowStep[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = getFromRow(data[i]);
        }
        return rta;
    }

    public static SysFlowStep select(int id, Connection ep) throws Exception {
        return SysFlowStep.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(ep));
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM sys_flow_step WHERE id = " + id;
    }

    public String getEnumOptions(String fieldName) {
        if (fieldName.equals("node_type")) {
            return "start=Inicio&end=Fin&task=Actividad&gate=Decisión";
        }
        if (fieldName.equals("resp_type")) {
            return "fixed=Fijo&crea=Usuario Creador&sql=Definido por SQL";
        }
        return null;
    }

}
