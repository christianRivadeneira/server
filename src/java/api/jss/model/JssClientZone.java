package api.jss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class JssClientZone extends BaseModel<JssClientZone> {
//inicio zona de reemplazo

    public int zoneId;
    public int clientId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "zone_id",
            "client_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, zoneId);
        q.setParam(2, clientId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        zoneId = MySQLQuery.getAsInteger(row[0]);
        clientId = MySQLQuery.getAsInteger(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "jss_client_zone";
    }

    public static String getSelFlds(String alias) {
        return new JssClientZone().getSelFldsForAlias(alias);
    }

    public static List<JssClientZone> getList(MySQLQuery q, Connection conn) throws Exception {
        return new JssClientZone().getListFromQuery(q, conn);
    }

    public static List<JssClientZone> getList(Params p, Connection conn) throws Exception {
        return new JssClientZone().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new JssClientZone().deleteById(id, conn);
    }

    public static List<JssClientZone> getAll(Connection conn) throws Exception {
        return new JssClientZone().getAllList(conn);
    }

//fin zona de reemplazo
    public static JssClientZone selectClient(int clientId, Connection conn) throws Exception {
        Object[] row = new MySQLQuery("SELECT " + getSelFlds("") + ", id FROM jss_client_zone WHERE client_id = ?1").setParam(1, clientId).getRecord(conn);

        if (row != null && row.length > 0) {
            JssClientZone obj = new JssClientZone();
            obj.setRow(row);
            return obj;
        } else {
            return null;
        }
    }     

}
