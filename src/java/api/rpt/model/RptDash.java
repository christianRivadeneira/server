package api.rpt.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class RptDash extends BaseModel<RptDash> {
//inicio zona de reemplazo

    public String name;
    public int views;
    public Date lastView;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "views",
            "last_view"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, views);
        q.setParam(3, lastView);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        views = MySQLQuery.getAsInteger(row[1]);
        lastView = MySQLQuery.getAsDate(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "rpt_dash";
    }

    public static String getSelFlds(String alias) {
        return new RptDash().getSelFldsForAlias(alias);
    }

    public static List<RptDash> getList(MySQLQuery q, Connection conn) throws Exception {
        return new RptDash().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new RptDash().deleteById(id, conn);
    }

    public static List<RptDash> getAll(Connection conn) throws Exception {
        return new RptDash().getAllList(conn);
    }

//fin zona de reemplazo
    
}
