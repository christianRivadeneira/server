package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class MssRoundProg extends BaseModel<MssRoundProg> {
//inicio zona de reemplazo

    public int postId;
    public String name;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "post_id",
            "name"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, postId);
        q.setParam(2, name);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        postId = MySQLQuery.getAsInteger(row[0]);
        name = MySQLQuery.getAsString(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_round_prog";
    }

    public static String getSelFlds(String alias) {
        return new MssRoundProg().getSelFldsForAlias(alias);
    }

    public static List<MssRoundProg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssRoundProg().getListFromQuery(q, conn);
    }

    public static List<MssRoundProg> getList(Params p, Connection conn) throws Exception {
        return new MssRoundProg().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssRoundProg().deleteById(id, conn);
    }

    public static List<MssRoundProg> getAll(Connection conn) throws Exception {
        return new MssRoundProg().getAllList(conn);
    }

//fin zona de reemplazo
}
