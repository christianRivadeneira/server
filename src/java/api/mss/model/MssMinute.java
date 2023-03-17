package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;
import utilities.ServerCurdate;

public class MssMinute extends BaseModel<MssMinute> {
//inicio zona de reemplazo

    public int postId;
    public int typeId;
    public Date dt;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "post_id",
            "type_id",
            "dt"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, postId);
        q.setParam(2, typeId);
        q.setParam(3, dt);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        postId = MySQLQuery.getAsInteger(row[0]);
        typeId = MySQLQuery.getAsInteger(row[1]);
        dt = MySQLQuery.getAsDate(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_minute";
    }

    public static String getSelFlds(String alias) {
        return new MssMinute().getSelFldsForAlias(alias);
    }

    public static List<MssMinute> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssMinute().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssMinute().deleteById(id, conn);
    }

    public static List<MssMinute> getAll(Connection conn) throws Exception {
        return new MssMinute().getAllList(conn);
    }

//fin zona de reemplazo
    public static MssMinute getByPostToday(int postId, int typeId, Connection conn) throws Exception {
        Params p = new Params();
        p.param("post_id", postId);
        p.param("type_id", typeId);
        p.param("dt", new ServerCurdate());
        return new MssMinute().select(p, conn);
    }
}
