package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MssSuperPostPath extends BaseModel<MssSuperPostPath> {
//inicio zona de reemplazo

    public int place;
    public int pathId;
    public int postId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "place",
            "path_id",
            "post_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, place);
        q.setParam(2, pathId);
        q.setParam(3, postId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        place = MySQLQuery.getAsInteger(row[0]);
        pathId = MySQLQuery.getAsInteger(row[1]);
        postId = MySQLQuery.getAsInteger(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_super_post_path";
    }

    public static String getSelFlds(String alias) {
        return new MssSuperPostPath().getSelFldsForAlias(alias);
    }

    public static List<MssSuperPostPath> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssSuperPostPath().getListFromQuery(q, conn);
    }

    public static List<MssSuperPostPath> getList(Params p, Connection conn) throws Exception {
        return new MssSuperPostPath().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssSuperPostPath().deleteById(id, conn);
    }

    public static List<MssSuperPostPath> getAll(Connection conn) throws Exception {
        return new MssSuperPostPath().getAllList(conn);
    }

//fin zona de reemplazo
}
