package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MssPointProg extends BaseModel<MssPointProg> {
//inicio zona de reemplazo

    public int pointId;
    public int progId;
    public Integer place;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "point_id",
            "prog_id",
            "place"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, pointId);
        q.setParam(2, progId);
        q.setParam(3, place);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        pointId = MySQLQuery.getAsInteger(row[0]);
        progId = MySQLQuery.getAsInteger(row[1]);
        place = MySQLQuery.getAsInteger(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_point_prog";
    }

    public static String getSelFlds(String alias) {
        return new MssPointProg().getSelFldsForAlias(alias);
    }

    public static List<MssPointProg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssPointProg().getListFromQuery(q, conn);
    }

    public static List<MssPointProg> getList(Params p, Connection conn) throws Exception {
        return new MssPointProg().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssPointProg().deleteById(id, conn);
    }

    public static List<MssPointProg> getAll(Connection conn) throws Exception {
        return new MssPointProg().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<MssPointProg> getByProg(int progId, Connection conn) throws Exception {
        Params pars = new Params();
        pars.param("prog_id", progId);
        pars.sort("place", Params.ASC);        
        return new MssPointProg().getListFromParams(pars, conn);
    }

}
