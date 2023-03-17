package api.jss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class JssAgentZone extends BaseModel<JssAgentZone> {
//inicio zona de reemplazo

    public int zoneId;
    public int agentId;
    public int spanId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "zone_id",
            "agent_id",
            "span_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, zoneId);
        q.setParam(2, agentId);
        q.setParam(3, spanId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        zoneId = MySQLQuery.getAsInteger(row[0]);
        agentId = MySQLQuery.getAsInteger(row[1]);
        spanId = MySQLQuery.getAsInteger(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "jss_agent_zone";
    }

    public static String getSelFlds(String alias) {
        return new JssAgentZone().getSelFldsForAlias(alias);
    }

    public static List<JssAgentZone> getList(MySQLQuery q, Connection conn) throws Exception {
        return new JssAgentZone().getListFromQuery(q, conn);
    }

    public static List<JssAgentZone> getList(Params p, Connection conn) throws Exception {
        return new JssAgentZone().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new JssAgentZone().deleteById(id, conn);
    }

    public static List<JssAgentZone> getAll(Connection conn) throws Exception {
        return new JssAgentZone().getAllList(conn);
    }

//fin zona de reemplazo
    
    public static JssAgentZone getByAgentId(Connection conn, int agentId, int spanId) throws Exception {
        Object[] row = new MySQLQuery("SELECT " + getSelFlds("") + ", id "
                + "FROM jss_agent_zone "
                + "WHERE agent_id = ?1 AND span_id = ?2 LIMIT 1").setParam(1, agentId).setParam(2, spanId).getRecord(conn);

        if (row != null && row.length > 0) {
            JssAgentZone obj = new JssAgentZone();
            obj.setRow(row);
            return obj;
        } else {
            return null;
        }
    } 

}