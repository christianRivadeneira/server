package api.mss.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class MssGuardPost extends BaseModel<MssGuardPost> {
//inicio zona de reemplazo

    public int guardId;
    public int postId;
    public Date begDt;
    public Date endDt;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "guard_id",
            "post_id",
            "beg_dt",
            "end_dt"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, guardId);
        q.setParam(2, postId);
        q.setParam(3, begDt);
        q.setParam(4, endDt);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        guardId = MySQLQuery.getAsInteger(row[0]);
        postId = MySQLQuery.getAsInteger(row[1]);
        begDt = MySQLQuery.getAsDate(row[2]);
        endDt = MySQLQuery.getAsDate(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_guard_post";
    }

    public static String getSelFlds(String alias) {
        return new MssGuardPost().getSelFldsForAlias(alias);
    }

    public static List<MssGuardPost> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssGuardPost().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssGuardPost().deleteById(id, conn);
    }

    public static List<MssGuardPost> getAll(Connection conn) throws Exception {
        return new MssGuardPost().getAllList(conn);
    }

//fin zona de reemplazo
}
