package api.ord.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class OrdPollQuestion extends BaseModel<OrdPollQuestion> {
//inicio zona de reemplazo

    public int ordPollVersionId;
    public String text;
    public boolean multiple;
    public int ordinal;
    public boolean mandatory;
    public int boxWidth;
    public boolean showInSep;
    public boolean shortText;
    public boolean longText;
    public boolean dateTime;
    public boolean misc;
    public String miscTitle;
    public String miscMeta;
    public Integer questionTypeId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "ord_poll_version_id",
            "text",
            "multiple",
            "ordinal",
            "mandatory",
            "box_width",
            "show_in_sep",
            "short_text",
            "long_text",
            "date_time",
            "misc",
            "misc_title",
            "misc_meta",
            "question_type_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, ordPollVersionId);
        q.setParam(2, text);
        q.setParam(3, multiple);
        q.setParam(4, ordinal);
        q.setParam(5, mandatory);
        q.setParam(6, boxWidth);
        q.setParam(7, showInSep);
        q.setParam(8, shortText);
        q.setParam(9, longText);
        q.setParam(10, dateTime);
        q.setParam(11, misc);
        q.setParam(12, miscTitle);
        q.setParam(13, miscMeta);
        q.setParam(14, questionTypeId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        ordPollVersionId = MySQLQuery.getAsInteger(row[0]);
        text = MySQLQuery.getAsString(row[1]);
        multiple = MySQLQuery.getAsBoolean(row[2]);
        ordinal = MySQLQuery.getAsInteger(row[3]);
        mandatory = MySQLQuery.getAsBoolean(row[4]);
        boxWidth = MySQLQuery.getAsInteger(row[5]);
        showInSep = MySQLQuery.getAsBoolean(row[6]);
        shortText = MySQLQuery.getAsBoolean(row[7]);
        longText = MySQLQuery.getAsBoolean(row[8]);
        dateTime = MySQLQuery.getAsBoolean(row[9]);
        misc = MySQLQuery.getAsBoolean(row[10]);
        miscTitle = MySQLQuery.getAsString(row[11]);
        miscMeta = MySQLQuery.getAsString(row[12]);
        questionTypeId = MySQLQuery.getAsInteger(row[13]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_poll_question";
    }

    public static String getSelFlds(String alias) {
        return new OrdPollQuestion().getSelFldsForAlias(alias);
    }

    public static List<OrdPollQuestion> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdPollQuestion().getListFromQuery(q, conn);
    }

    public static List<OrdPollQuestion> getListPollTypeId(int versionId, Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " + getSelFlds("") + " FROM ord_poll_question WHERE ord_poll_version_id = " + versionId + " ORDER BY ordinal");
        return new OrdPollQuestion().getListFromQuery(q, conn);
    }

    public static List<OrdPollQuestion> getPollQuestionsByVersion(int pollVersionId, Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " + getSelFlds("") + " FROM ord_poll_question WHERE ord_poll_version_id = ?1 ORDER BY ordinal").setParam(1, pollVersionId);
        return new OrdPollQuestion().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdPollQuestion().deleteById(id, conn);
    }

    public static List<OrdPollQuestion> getAll(Connection conn) throws Exception {
        return new OrdPollQuestion().getAllList(conn);
    }

//fin zona de reemplazo
    public static MySQLQuery getByTypeQuery(int pollTypeId) {
        String query = "SELECT "
                + OrdPollQuestion.getSelFlds("opq")
                + "FROM ord_poll_question opq "
                + "WHERE "
                + "opq.ord_poll_version_id = "
                + "( "
                + "SELECT id "
                + "FROM "
                + "ord_poll_version opv "
                + "WHERE "
                + "LAST = 1 "
                + "AND opv.ord_poll_type_id  = ?1 "
                + ") "
                + "ORDER BY ordinal ";

        return new MySQLQuery(query).setParam(1, pollTypeId);
    }

    public static List<OrdPollQuestion> getByTypeQuery(Connection con, int pollTypeId, Integer versionId) throws Exception {
        MySQLQuery mq;
        if (versionId != null) {
            String query = "SELECT " + getSelFlds("opq") + " "
                    + "FROM ord_poll_question opq "
                    + "WHERE opq.ord_poll_version_id = ?1 "
                    + "ORDER BY opq.ordinal ";
            mq = new MySQLQuery(query).setParam(1, versionId);
        } else {
            String query = "SELECT " + getSelFlds("opq") + " "
                    + "FROM ord_poll_question opq "
                    + "WHERE opq.ord_poll_version_id = "
                    + "("
                    + "SELECT opv.id FROM ord_poll_version opv "
                    + "WHERE opv.last = 1 "
                    + "AND opv.ord_poll_type_id = ?1 "
                    + ") "
                    + "ORDER BY opq.ordinal";
            mq = new MySQLQuery(query).setParam(1, pollTypeId);
        }
        return getList(mq, con);
    }
}
