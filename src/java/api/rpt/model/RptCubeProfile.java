package api.rpt.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class RptCubeProfile extends BaseModel<RptCubeProfile> {
//inicio zona de reemplazo

    public int cubeId;
    public int profileId;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "cube_id",
            "profile_id",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, cubeId);
        q.setParam(2, profileId);
        q.setParam(3, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        cubeId = MySQLQuery.getAsInteger(row[0]);
        profileId = MySQLQuery.getAsInteger(row[1]);
        active = MySQLQuery.getAsBoolean(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "rpt_cube_profile";
    }

    public static String getSelFlds(String alias) {
        return new RptCubeProfile().getSelFldsForAlias(alias);
    }

    public static List<RptCubeProfile> getList(MySQLQuery q, Connection conn) throws Exception {
        return new RptCubeProfile().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new RptCubeProfile().deleteById(id, conn);
    }

    public static List<RptCubeProfile> getAll(Connection conn) throws Exception {
        return new RptCubeProfile().getAllList(conn);
    }

//fin zona de reemplazo
   
}
