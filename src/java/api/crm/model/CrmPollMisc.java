package api.crm.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class CrmPollMisc extends BaseModel<CrmPollMisc> {
//inicio zona de reemplazo

    public Integer pollId;
    public Integer questionId;
    public Integer ordinal;
    public String text1;
    public String text2;
    public String text3;
    public String text4;
    public String text5;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "poll_id",
            "question_id",
            "ordinal",
            "text1",
            "text2",
            "text3",
            "text4",
            "text5",
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, pollId);
        q.setParam(2, questionId);
        q.setParam(3, ordinal);
        q.setParam(4, text1);
        q.setParam(5, text2);
        q.setParam(6, text3);
        q.setParam(7, text4);
        q.setParam(8, text5);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        pollId = MySQLQuery.getAsInteger(row[0]);
        questionId = MySQLQuery.getAsInteger(row[1]);
        ordinal = MySQLQuery.getAsInteger(row[2]);
        text1 = MySQLQuery.getAsString(row[3]);
        text2 = MySQLQuery.getAsString(row[4]);
        text3 = MySQLQuery.getAsString(row[5]);
        text4 = MySQLQuery.getAsString(row[6]);
        text5 = MySQLQuery.getAsString(row[7]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_poll_misc";
    }

    public static String getSelFlds(String alias) {
        return new CrmPollMisc().getSelFldsForAlias(alias);
    }

    public static List<CrmPollMisc> getList(MySQLQuery q, Connection conn) throws Exception {
        return new CrmPollMisc().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new CrmPollMisc().deleteById(id, conn);
    }

    public static List<CrmPollMisc> getAll(Connection conn) throws Exception {
        return new CrmPollMisc().getAllList(conn);
    }

//fin zona de reemplazo
    
}
