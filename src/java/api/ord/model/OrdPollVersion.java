package api.ord.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class OrdPollVersion extends BaseModel<OrdPollVersion> {
//inicio zona de reemplazo

    public int ordPollTypeId;
    public boolean last;
    public Date since;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "ord_poll_type_id",
            "last",
            "since"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, ordPollTypeId);
        q.setParam(2, last);
        q.setParam(3, since);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        ordPollTypeId = MySQLQuery.getAsInteger(row[0]);
        last = MySQLQuery.getAsBoolean(row[1]);
        since = MySQLQuery.getAsDate(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_poll_version";
    }

    public static String getSelFlds(String alias) {
        return new OrdPollVersion().getSelFldsForAlias(alias);
    }

    public static List<OrdPollVersion> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdPollVersion().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdPollVersion().deleteById(id, conn);
    }

    public static List<OrdPollVersion> getAll(Connection conn) throws Exception {
        return new OrdPollVersion().getAllList(conn);
    }

//fin zona de reemplazo

}