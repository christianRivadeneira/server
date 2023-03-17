package api.ess.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class EssEventType extends BaseModel<EssEventType> {
//inicio zona de reemplazo

    public String name;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        active = MySQLQuery.getAsBoolean(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ess_event_type";
    }

    public static String getSelFlds(String alias) {
        return new EssEventType().getSelFldsForAlias(alias);
    }

    public static List<EssEventType> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EssEventType().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EssEventType().deleteById(id, conn);
    }

    public static List<EssEventType> getAll(Connection conn) throws Exception {
        return new EssEventType().getAllList(conn);
    }

//fin zona de reemplazo
}
