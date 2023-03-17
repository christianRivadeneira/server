package api.rpt.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class RptCube extends BaseModel<RptCube> {
//inicio zona de reemplazo

    public String name;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "rpt_cube";
    }

    public static String getSelFlds(String alias) {
        return new RptCube().getSelFldsForAlias(alias);
    }

    public static List<RptCube> getList(MySQLQuery q, Connection conn) throws Exception {
        return new RptCube().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new RptCube().deleteById(id, conn);
    }

    public static List<RptCube> getAll(Connection conn) throws Exception {
        return new RptCube().getAllList(conn);
    }

//fin zona de reemplazo
   
    public static RptCube getSelectByRptIdQuery(int rptId, Connection conn) throws Exception {
        return new RptCube().select(new MySQLQuery("SELECT " + getSelFlds("c") + " FROM rpt_cube c WHERE c.id = (SELECT cube_id FROM rpt_rpt WHERE id = " + rptId + ")"), conn);
    }

}
