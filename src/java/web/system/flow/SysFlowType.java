package web.system.flow;

import java.sql.Connection;
import utilities.MySQLQuery;

public class SysFlowType {
//inicio zona de reemplazo

    public int id;
    public String name;
    public String sname;
    public String roles;
    public String article;
    public String subject;
    public String module;

    private static final String SEL_FLDS = "`name`, "
            + "`sname`, "
            + "`roles`, "
            + "`article`, "
            + "`subject`, "
            + "`module`";

    public static SysFlowType getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        SysFlowType obj = new SysFlowType();
        obj.name = MySQLQuery.getAsString(row[0]);
        obj.sname = MySQLQuery.getAsString(row[1]);
        obj.roles = MySQLQuery.getAsString(row[2]);
        obj.article = MySQLQuery.getAsString(row[3]);
        obj.subject = MySQLQuery.getAsString(row[4]);
        obj.module = MySQLQuery.getAsString(row[5]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public static SysFlowType select(int id, Connection ep) throws Exception {
        return SysFlowType.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(ep));
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM sys_flow_type WHERE id = " + id;
    }
}
