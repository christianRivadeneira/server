package api.sys.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class SysGcmMessage extends BaseModel<SysGcmMessage> {

//inicio zona de reemplazo

    public int appId;
    public Date regDate;
    public Date toSendDate;
    public Date sendDate;
    public String data;
    public int empId;
    public String tableName;
    public Integer ownerId;
    public Integer crmTaskId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "app_id",
            "reg_date",
            "to_send_date",
            "send_date",
            "data",
            "emp_id",
            "table_name",
            "owner_id",
            "crm_task_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, appId);
        q.setParam(2, regDate);
        q.setParam(3, toSendDate);
        q.setParam(4, sendDate);
        q.setParam(5, data);
        q.setParam(6, empId);
        q.setParam(7, tableName);
        q.setParam(8, ownerId);
        q.setParam(9, crmTaskId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        appId = MySQLQuery.getAsInteger(row[0]);
        regDate = MySQLQuery.getAsDate(row[1]);
        toSendDate = MySQLQuery.getAsDate(row[2]);
        sendDate = MySQLQuery.getAsDate(row[3]);
        data = MySQLQuery.getAsString(row[4]);
        empId = MySQLQuery.getAsInteger(row[5]);
        tableName = MySQLQuery.getAsString(row[6]);
        ownerId = MySQLQuery.getAsInteger(row[7]);
        crmTaskId = MySQLQuery.getAsInteger(row[8]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "sys_gcm_message";
    }

    public static String getSelFlds(String alias) {
        return new SysGcmMessage().getSelFldsForAlias(alias);
    }

    public static List<SysGcmMessage> getList(MySQLQuery q, Connection conn) throws Exception {
        return new SysGcmMessage().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new SysGcmMessage().deleteById(id, conn);
    }

    public static List<SysGcmMessage> getAll(Connection conn) throws Exception {
        return new SysGcmMessage().getAllList(conn);
    }

//fin zona de reemplazo
    public static void deletePushByTask(int taskId, Connection conn) throws Exception {
        new MySQLQuery("DELETE FROM sys_gcm_message WHERE crm_task_id = " + taskId).executeDelete(conn);
    }

    public static List<SysGcmMessage> getPendingMessages(Connection con, int appId) throws Exception {
        MySQLQuery mq = new MySQLQuery("SELECT " + getSelFlds("") + " FROM sys_gcm_message g "
                + " WHERE "
                + " g.send_date IS NULL "
                + " AND g.app_id = " + appId
                + " AND g.to_send_date <  DATE_ADD(NOW(),INTERVAL 5 MINUTE) ");

        List<SysGcmMessage> list = getList(mq, con);
        return list;
    }
}
