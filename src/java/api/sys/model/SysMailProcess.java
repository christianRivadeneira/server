package api.sys.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class SysMailProcess extends BaseModel<SysMailProcess> {
//inicio zona de reemplazo

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

    @Override
    protected String[] getFlds() {
        return new String[]{
            "module_id",
            "name",
            "notes",
            "constant",
            "active",
            "auto",
            "start_time",
            "period_type",
            "period",
            "message",
            "subject",
            "columns",
            "query_content",
            "query_action"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, moduleId);
        q.setParam(2, name);
        q.setParam(3, notes);
        q.setParam(4, constant);
        q.setParam(5, active);
        q.setParam(6, auto);
        q.setParam(7, startTime);
        q.setParam(8, periodType);
        q.setParam(9, period);
        q.setParam(10, message);
        q.setParam(11, subject);
        q.setParam(12, columns);
        q.setParam(13, queryContent);
        q.setParam(14, queryAction);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        moduleId = MySQLQuery.getAsInteger(row[0]);
        name = MySQLQuery.getAsString(row[1]);
        notes = MySQLQuery.getAsString(row[2]);
        constant = MySQLQuery.getAsString(row[3]);
        active = MySQLQuery.getAsBoolean(row[4]);
        auto = MySQLQuery.getAsBoolean(row[5]);
        startTime = MySQLQuery.getAsDate(row[6]);
        periodType = MySQLQuery.getAsString(row[7]);
        period = MySQLQuery.getAsInteger(row[8]);
        message = MySQLQuery.getAsString(row[9]);
        subject = MySQLQuery.getAsString(row[10]);
        columns = MySQLQuery.getAsString(row[11]);
        queryContent = MySQLQuery.getAsString(row[12]);
        queryAction = MySQLQuery.getAsString(row[13]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "sys_mail_process";
    }

    public static String getSelFlds(String alias) {
        return new SysMailProcess().getSelFldsForAlias(alias);
    }

    public static List<SysMailProcess> getList(MySQLQuery q, Connection conn) throws Exception {
        return new SysMailProcess().getListFromQuery(q, conn);
    }

    public static List<SysMailProcess> getList(Params p, Connection conn) throws Exception {
        return new SysMailProcess().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new SysMailProcess().deleteById(id, conn);
    }

    public static List<SysMailProcess> getAll(Connection conn) throws Exception {
        return new SysMailProcess().getAllList(conn);
    }

//fin zona de reemplazo
}
