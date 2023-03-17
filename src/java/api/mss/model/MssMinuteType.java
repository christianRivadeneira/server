package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MssMinuteType extends BaseModel<MssMinuteType> {
//inicio zona de reemplazo

    public String name;
    public int clientId;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "client_id",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, clientId);
        q.setParam(3, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        clientId = MySQLQuery.getAsInteger(row[1]);
        active = MySQLQuery.getAsBoolean(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_minute_type";
    }

    public static String getSelFlds(String alias) {
        return new MssMinuteType().getSelFldsForAlias(alias);
    }

    public static List<MssMinuteType> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssMinuteType().getListFromQuery(q, conn);
    }

    public static List<MssMinuteType> getList(Params p, Connection conn) throws Exception {
        return new MssMinuteType().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssMinuteType().deleteById(id, conn);
    }

    public static List<MssMinuteType> getAll(Connection conn) throws Exception {
        return new MssMinuteType().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<MssMinuteType> getByClient(int clientId, Connection conn) throws Exception {
        return new MssMinuteType().getListFromParams(new Params("client_id", clientId).param("active", true), conn);
    }
}
