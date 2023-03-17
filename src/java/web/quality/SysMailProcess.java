package web.quality;

import java.sql.Connection;
import java.util.Date;
import utilities.MySQLQuery;

public class SysMailProcess {
//inicio zona de reemplazo

    public int id;
    public int moduleId;
    public String name;
    public String notes;
    public String constant;
    public boolean active;
    public boolean auto;
    public Date startTime;
    public String periodType;
    public Integer period;
    public String message;
    public String subject;
    public String columns;
    public String queryContent;
    public String queryAction;

    private static final String SEL_FLDS = "`module_id`, "
            + "`name`, "
            + "`notes`, "
            + "`constant`, "
            + "`active`, "
            + "`auto`, "
            + "`start_time`, "
            + "`period_type`, "
            + "`period`, "
            + "`message`, "
            + "`subject`, "
            + "`columns`, "
            + "`query_content`, "
            + "`query_action`";

    private static final String SET_FLDS = "sys_mail_process SET "
            + "`module_id` = ?1, "
            + "`name` = ?2, "
            + "`notes` = ?3, "
            + "`constant` = ?4, "
            + "`active` = ?5, "
            + "`auto` = ?6, "
            + "`start_time` = ?7, "
            + "`period_type` = ?8, "
            + "`period` = ?9, "
            + "`message` = ?10, "
            + "`subject` = ?11, "
            + "`columns` = ?12, "
            + "`query_content` = ?13, "
            + "`query_action` = ?14";

    private static void setFields(SysMailProcess obj, MySQLQuery q) {
        q.setParam(1, obj.moduleId);
        q.setParam(2, obj.name);
        q.setParam(3, obj.notes);
        q.setParam(4, obj.constant);
        q.setParam(5, obj.active);
        q.setParam(6, obj.auto);
        q.setParam(7, obj.startTime);
        q.setParam(8, obj.periodType);
        q.setParam(9, obj.period);
        q.setParam(10, obj.message);
        q.setParam(11, obj.subject);
        q.setParam(12, obj.columns);
        q.setParam(13, obj.queryContent);
        q.setParam(14, obj.queryAction);

    }

    public static SysMailProcess getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        SysMailProcess obj = new SysMailProcess();
        obj.moduleId = MySQLQuery.getAsInteger(row[0]);
        obj.name = MySQLQuery.getAsString(row[1]);
        obj.notes = MySQLQuery.getAsString(row[2]);
        obj.constant = MySQLQuery.getAsString(row[3]);
        obj.active = MySQLQuery.getAsBoolean(row[4]);
        obj.auto = MySQLQuery.getAsBoolean(row[5]);
        obj.startTime = MySQLQuery.getAsDate(row[6]);
        obj.periodType = MySQLQuery.getAsString(row[7]);
        obj.period = MySQLQuery.getAsInteger(row[8]);
        obj.message = MySQLQuery.getAsString(row[9]);
        obj.subject = MySQLQuery.getAsString(row[10]);
        obj.columns = MySQLQuery.getAsString(row[11]);
        obj.queryContent = MySQLQuery.getAsString(row[12]);
        obj.queryAction = MySQLQuery.getAsString(row[13]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    private static final String TO_STR_FLDS = ")";

    public static SysMailProcess[] getAllActiveAndAuto(Connection ep) throws Exception {
        Object[][] data = new MySQLQuery("SELECT " + SEL_FLDS + ", id "
                + "FROM sys_mail_process "
                + "WHERE active AND auto ").getRecords(ep);
        SysMailProcess[] rta = new SysMailProcess[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = getFromRow(data[i]);
        }
        return rta;
    }

    public SysMailProcess select(int id, Connection ep) throws Exception {
        return SysMailProcess.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(ep));
    }

    public void update(SysMailProcess pobj, Connection ep) throws Exception {
        new MySQLQuery(SysMailProcess.getUpdateQuery(pobj)).executeUpdate(ep);
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM sys_mail_process "
                + "WHERE id = " + id + " "
                + "AND auto = 1 ";
    }

    public static String getUpdateQuery(SysMailProcess obj) {
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

}
