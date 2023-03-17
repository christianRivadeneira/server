package web.system.flow;

import java.sql.Connection;
import java.util.Date;
import utilities.MySQLQuery;

public class SysFlowChk {
//inicio zona de reemplazo

    public int id;
    public Date createDt;
    public Date checkDt;
    public String notes;
    public Integer empId;
    public Integer fromStepId;
    public Integer toStepId;
    public int reqId;
    public boolean firstTime;

    private static final String SEL_FLDS = "`create_dt`, "
            + "`check_dt`, "
            + "`notes`, "
            + "`emp_id`, "
            + "`from_step_id`, "
            + "`to_step_id`, "
            + "`req_id`, "
            + "`first_time`";

    private static final String SET_FLDS = "sys_flow_chk SET "
            + "`create_dt` = ?1, "
            + "`check_dt` = ?2, "
            + "`notes` = ?3, "
            + "`emp_id` = ?4, "
            + "`from_step_id` = ?5, "
            + "`to_step_id` = ?6, "
            + "`req_id` = ?7, "
            + "`first_time` = ?8";

    private static void setFields(SysFlowChk obj, MySQLQuery q) {
        q.setParam(1, obj.createDt);
        q.setParam(2, obj.checkDt);
        q.setParam(3, obj.notes);
        q.setParam(4, obj.empId);
        q.setParam(5, obj.fromStepId);
        q.setParam(6, obj.toStepId);
        q.setParam(7, obj.reqId);
        q.setParam(8, obj.firstTime);

    }

    public static SysFlowChk getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        SysFlowChk obj = new SysFlowChk();
        obj.createDt = MySQLQuery.getAsDate(row[0]);
        obj.checkDt = MySQLQuery.getAsDate(row[1]);
        obj.notes = MySQLQuery.getAsString(row[2]);
        obj.empId = MySQLQuery.getAsInteger(row[3]);
        obj.fromStepId = MySQLQuery.getAsInteger(row[4]);
        obj.toStepId = MySQLQuery.getAsInteger(row[5]);
        obj.reqId = MySQLQuery.getAsInteger(row[6]);
        obj.firstTime = MySQLQuery.getAsBoolean(row[7]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    /*
    public static SysFlowChk[] getAll(Connection ep) throws Exception {
        Object[][] data = new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM sys_flow_chk").getRecords(ep);
        SysFlowChk[] rta = new SysFlowChk[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = getFromRow(data[i]);            
        }
        return rta;
    }*/
    public static SysFlowChk select(int id, Connection ep) throws Exception {
        return SysFlowChk.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(ep));
    }

    public static SysFlowChk getCurByReqId(int reqId, Connection ep) throws Exception {
        return SysFlowChk.getFromRow(new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM sys_flow_chk WHERE id = (SELECT r.cur_chk_id FROM sys_flow_req r WHERE r.id = " + reqId + ")").getRecord(ep));
    }

    public static int insert(SysFlowChk pobj, Connection ep) throws Exception {
        SysFlowChk obj = (SysFlowChk) pobj;
        //parche para que la aprobación de compra solo auttorice Gladis Amanda Cabrera Sanchez
        //Si en algun momento el usuario se inactiva o no va llevar más el control de aprobación
        //se deben borrar las línesas 91 a 93
        /*if(obj.fromStepId==32 || obj.fromStepId==21 || obj.fromStepId==23 || obj.fromStepId==26 || obj.fromStepId==44){
            obj.empId=1430;
        }*/
        int nId = new MySQLQuery(SysFlowChk.getInsertQuery(obj)).executeInsert(ep);
        obj.id = nId;
        return nId;
    }

    public void update(SysFlowChk pobj, Connection ep) throws Exception {
        new MySQLQuery(SysFlowChk.getUpdateQuery((SysFlowChk) pobj)).executeUpdate(ep);
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM sys_flow_chk WHERE id = " + id;
    }

    public static String getInsertQuery(SysFlowChk obj) {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public static String getUpdateQuery(SysFlowChk obj) {
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public void delete(int id, Connection ep) throws Exception {
        new MySQLQuery("DELETE FROM sys_flow_chk WHERE id = " + id).executeDelete(ep);
    }
}
