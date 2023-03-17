package api.per.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class PerHobby extends BaseModel<PerHobby> {
//inicio zona de reemplazo

    public String name;
    public String type;
    public boolean showInPoll;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "type",
            "show_in_poll"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, type);
        q.setParam(3, showInPoll);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        type = MySQLQuery.getAsString(row[1]);
        showInPoll = MySQLQuery.getAsBoolean(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_hobby";
    }

    public static String getSelFlds(String alias) {
        return new PerHobby().getSelFldsForAlias(alias);
    }

    public static List<PerHobby> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerHobby().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new PerHobby().deleteById(id, conn);
    }

    public static List<PerHobby> getAll(Connection conn) throws Exception {
        return new PerHobby().getAllList(conn);
    }

//fin zona de reemplazo
}
