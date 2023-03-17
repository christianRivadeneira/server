package api.sys.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class SysChatMsg extends BaseModel<SysChatMsg> {
//inicio zona de reemplazo

    public Date dt;
    public int fromId;
    public Integer toId;
    public String content;
    public boolean seen;
    public int attachId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "dt",
            "from_id",
            "to_id",
            "content",
            "seen",
            "attach_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, dt);
        q.setParam(2, fromId);
        q.setParam(3, toId);
        q.setParam(4, content);
        q.setParam(5, seen);
        q.setParam(6, attachId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        dt = MySQLQuery.getAsDate(row[0]);
        fromId = MySQLQuery.getAsInteger(row[1]);
        toId = MySQLQuery.getAsInteger(row[2]);
        content = MySQLQuery.getAsString(row[3]);
        seen = MySQLQuery.getAsBoolean(row[4]);
        attachId = MySQLQuery.getAsInteger(row[5]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "sys_chat_msg";
    }

    public static String getSelFlds(String alias) {
        return new SysChatMsg().getSelFldsForAlias(alias);
    }

    public static List<SysChatMsg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new SysChatMsg().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new SysChatMsg().deleteById(id, conn);
    }

    public static List<SysChatMsg> getAll(Connection conn) throws Exception {
        return new SysChatMsg().getAllList(conn);
    }

//fin zona de reemplazo
}