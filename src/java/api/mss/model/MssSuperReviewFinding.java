package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MssSuperReviewFinding extends BaseModel<MssSuperReviewFinding> {
//inicio zona de reemplazo

    public String name;
    public Boolean active;
    public int place;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "active",
            "place"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, active);
        q.setParam(3, place);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        active = MySQLQuery.getAsBoolean(row[1]);
        place = MySQLQuery.getAsInteger(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_super_review_finding";
    }

    public static String getSelFlds(String alias) {
        return new MssSuperReviewFinding().getSelFldsForAlias(alias);
    }

    public static List<MssSuperReviewFinding> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssSuperReviewFinding().getListFromQuery(q, conn);
    }

    public static List<MssSuperReviewFinding> getList(Params p, Connection conn) throws Exception {
        return new MssSuperReviewFinding().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssSuperReviewFinding().deleteById(id, conn);
    }

    public static List<MssSuperReviewFinding> getAll(Connection conn) throws Exception {
        return new MssSuperReviewFinding().getAllList(conn);
    }

//fin zona de reemplazo
}
