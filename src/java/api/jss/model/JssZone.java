package api.jss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class JssZone extends BaseModel<JssZone> {
//inicio zona de reemplazo

    public String name;
    public int goal;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "goal"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, goal);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        goal = MySQLQuery.getAsInteger(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "jss_zone";
    }

    public static String getSelFlds(String alias) {
        return new JssZone().getSelFldsForAlias(alias);
    }

    public static List<JssZone> getList(MySQLQuery q, Connection conn) throws Exception {
        return new JssZone().getListFromQuery(q, conn);
    }

    public static List<JssZone> getList(Params p, Connection conn) throws Exception {
        return new JssZone().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new JssZone().deleteById(id, conn);
    }

    public static List<JssZone> getAll(Connection conn) throws Exception {
        return new JssZone().getAllList(conn);
    }

//fin zona de reemplazo
    public static JssZone selectName(String name, Connection conn) throws Exception {
        Object[] row = new MySQLQuery("SELECT " + getSelFlds("") + ", id FROM jss_zone WHERE name = ?1").setParam(1, name).getRecord(conn);

        if (row != null && row.length > 0) {
            JssZone obj = new JssZone();
            obj.setRow(row);
            return obj;
        } else {
            return null;
        }
    }

    public static List<JssZone> getAll(Connection conn, int empId) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " + getSelFlds("z") + ", z.id "
                + "FROM jss_zone z "
                + "WHERE (SELECT count(*) FROM jss_client_zone WHERE zone_id = z.id) > 0 ");
        return getList(q, conn);
    }

}
