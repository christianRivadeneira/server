package api.ord.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class OrdPollOption extends BaseModel<OrdPollOption> {
//inicio zona de reemplazo

    public int pollQuestionId;
    public String text;
    public int ordinal;
    public boolean lineBreak;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "poll_question_id",
            "text",
            "ordinal",
            "line_break"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, pollQuestionId);
        q.setParam(2, text);
        q.setParam(3, ordinal);
        q.setParam(4, lineBreak);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        pollQuestionId = MySQLQuery.getAsInteger(row[0]);
        text = MySQLQuery.getAsString(row[1]);
        ordinal = MySQLQuery.getAsInteger(row[2]);
        lineBreak = MySQLQuery.getAsBoolean(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_poll_option";
    }

    public static String getSelFlds(String alias) {
        return new OrdPollOption().getSelFldsForAlias(alias);
    }

    public static List<OrdPollOption> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdPollOption().getListFromQuery(q, conn);
    }

    public static List<OrdPollOption> getListQuestionsId(String questionsId, Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " + getSelFlds("") + " FROM ord_poll_option WHERE poll_question_id IN (" + questionsId + ") ORDER BY ordinal");
        return new OrdPollOption().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdPollOption().deleteById(id, conn);
    }

    public static List<OrdPollOption> getAll(Connection conn) throws Exception {
        return new OrdPollOption().getAllList(conn);
    }

//fin zona de reemplazo
}
