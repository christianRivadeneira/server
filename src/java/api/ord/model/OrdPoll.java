package api.ord.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class OrdPoll extends BaseModel<OrdPoll> {
//inicio zona de reemplazo

    public int pollVersionId;
    public String answer;
    public String notes;
    public Integer empId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "poll_version_id",
            "answer",
            "notes",
            "emp_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, pollVersionId);
        q.setParam(2, answer);
        q.setParam(3, notes);
        q.setParam(4, empId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        pollVersionId = MySQLQuery.getAsInteger(row[0]);
        answer = MySQLQuery.getAsString(row[1]);
        notes = MySQLQuery.getAsString(row[2]);
        empId = MySQLQuery.getAsInteger(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_poll";
    }

    public static String getSelFlds(String alias) {
        return new OrdPoll().getSelFldsForAlias(alias);
    }

    public static List<OrdPoll> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdPoll().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdPoll().deleteById(id, conn);
    }

    public static List<OrdPoll> getAll(Connection conn) throws Exception {
        return new OrdPoll().getAllList(conn);
    }

//fin zona de reemplazo
}
