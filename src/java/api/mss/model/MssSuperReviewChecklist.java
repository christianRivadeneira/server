package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MssSuperReviewChecklist extends BaseModel<MssSuperReviewChecklist> {
//inicio zona de reemplazo

    public int reviewId;
    public int findingId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "review_id",
            "finding_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, reviewId);
        q.setParam(2, findingId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        reviewId = MySQLQuery.getAsInteger(row[0]);
        findingId = MySQLQuery.getAsInteger(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_super_review_checklist";
    }

    public static String getSelFlds(String alias) {
        return new MssSuperReviewChecklist().getSelFldsForAlias(alias);
    }

    public static List<MssSuperReviewChecklist> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssSuperReviewChecklist().getListFromQuery(q, conn);
    }

    public static List<MssSuperReviewChecklist> getList(Params p, Connection conn) throws Exception {
        return new MssSuperReviewChecklist().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssSuperReviewChecklist().deleteById(id, conn);
    }

    public static List<MssSuperReviewChecklist> getAll(Connection conn) throws Exception {
        return new MssSuperReviewChecklist().getAllList(conn);
    }

//fin zona de reemplazo
}
