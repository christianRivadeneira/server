package api.trk.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class TrkNegAns extends BaseModel<TrkNegAns> {
//inicio zona de reemplazo

    public Integer questionId;
    public Integer checkId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "question_id",
            "check_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, questionId);
        q.setParam(2, checkId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        questionId = MySQLQuery.getAsInteger(row[0]);
        checkId = MySQLQuery.getAsInteger(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "trk_neg_ans";
    }

    public static String getSelFlds(String alias) {
        return new TrkNegAns().getSelFldsForAlias(alias);
    }

    public static List<TrkNegAns> getList(MySQLQuery q, Connection conn) throws Exception {
        return new TrkNegAns().getListFromQuery(q, conn);
    }

//fin zona de reemplazo
    
}
