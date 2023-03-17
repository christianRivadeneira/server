package web.system.flow;

import java.sql.Connection;
import utilities.MySQLQuery;

public class SysFlowSubs {

    public Boolean disposable;
    public String rol;

//inicio zona de reemplazo
    public int id;
    public int reqId;
    public int empId;
    public Integer subId;

    private static final String SEL_FLDS = "`req_id`, "
            + "`emp_id`, "
            + "`sub_id`";

    private static final String SET_FLDS = "sys_flow_subs SET "
            + "`req_id` = ?1, "
            + "`emp_id` = ?2, "
            + "`sub_id` = ?3";

    private static void setFields(SysFlowSubs obj, MySQLQuery q) {
        q.setParam(1, obj.reqId);
        q.setParam(2, obj.empId);
        q.setParam(3, obj.subId);

    }

    public static SysFlowSubs getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        SysFlowSubs obj = new SysFlowSubs();
        obj.reqId = MySQLQuery.getAsInteger(row[0]);
        obj.empId = MySQLQuery.getAsInteger(row[1]);
        obj.subId = MySQLQuery.getAsInteger(row[2]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    /*
    public static SysFlowSubs[] getAll(Connection ep) throws Exception {
        Object[][] data = new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM sys_flow_subs").getRecords(ep);
        SysFlowSubs[] rta = new SysFlowSubs[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = getFromRow(data[i]);
        }
        return rta;
    }*/
    public static SysFlowSubs getByIds(int reqId, int empId, Connection ep) throws Exception {
        Object[] data = new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM sys_flow_subs WHERE req_id = " + reqId + " AND emp_id = " + empId).getRecord(ep);
        SysFlowSubs sub = getFromRow(data);
        if (sub != null && sub.subId != null) {
            SysFlowStepSubs ssub = new SysFlowStepSubs().select(sub.subId, ep);
            sub.rol = ssub.rol;
            sub.disposable = ssub.disposable;
        }
        return sub;
    }

    public SysFlowSubs select(int id, Connection ep) throws Exception {
        return SysFlowSubs.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(ep));
    }

    public int insert(SysFlowSubs pobj, Connection ep) throws Exception {
        SysFlowSubs obj = (SysFlowSubs) pobj;
        int nId = new MySQLQuery(SysFlowSubs.getInsertQuery(obj)).executeInsert(ep);
        obj.id = nId;
        return nId;
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM sys_flow_subs WHERE id = " + id;
    }

    public static String getInsertQuery(SysFlowSubs obj) {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public static String getUpdateQuery(SysFlowSubs obj) {
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public void delete(int id, Connection ep) throws Exception {
        new MySQLQuery("DELETE FROM sys_flow_subs WHERE id = " + id).executeDelete(ep);
    }
}
