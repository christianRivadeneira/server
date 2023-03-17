package api.com.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class ComAppQuestion extends BaseModel<ComAppQuestion> {
//inicio zona de reemplazo

    public String question;
    public String negQuestion;
    public String type;
    public Integer place;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "question",
            "neg_question",
            "type",
            "place",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, question);
        q.setParam(2, negQuestion);
        q.setParam(3, type);
        q.setParam(4, place);
        q.setParam(5, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        question = MySQLQuery.getAsString(row[0]);
        negQuestion = MySQLQuery.getAsString(row[1]);
        type = MySQLQuery.getAsString(row[2]);
        place = MySQLQuery.getAsInteger(row[3]);
        active = MySQLQuery.getAsBoolean(row[4]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "com_app_question";
    }

    public static String getSelFlds(String alias) {
        return new ComAppQuestion().getSelFldsForAlias(alias);
    }

    public static List<ComAppQuestion> getList(MySQLQuery q, Connection conn) throws Exception {
        return new ComAppQuestion().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new ComAppQuestion().deleteById(id, conn);
    }

    public static List<ComAppQuestion> getAll(Connection conn) throws Exception {
        return new ComAppQuestion().getAllList(conn);
    }

//fin zona de reemplazo
}