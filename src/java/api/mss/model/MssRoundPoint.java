package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.math.BigDecimal;
import java.util.Date;

public class MssRoundPoint extends BaseModel<MssRoundPoint> {
//inicio zona de reemplazo

    public int roundId;
    public int pointId;
    public int place;
    public Date dt;
    public BigDecimal lat;
    public BigDecimal lon;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "round_id",
            "point_id",
            "place",
            "dt",
            "lat",
            "lon"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, roundId);
        q.setParam(2, pointId);
        q.setParam(3, place);
        q.setParam(4, dt);
        q.setParam(5, lat);
        q.setParam(6, lon);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        roundId = MySQLQuery.getAsInteger(row[0]);
        pointId = MySQLQuery.getAsInteger(row[1]);
        place = MySQLQuery.getAsInteger(row[2]);
        dt = MySQLQuery.getAsDate(row[3]);
        lat = MySQLQuery.getAsBigDecimal(row[4], false);
        lon = MySQLQuery.getAsBigDecimal(row[5], false);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_round_point";
    }

    public static String getSelFlds(String alias) {
        return new MssRoundPoint().getSelFldsForAlias(alias);
    }

    public static List<MssRoundPoint> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssRoundPoint().getListFromQuery(q, conn);
    }

    public static List<MssRoundPoint> getList(Params p, Connection conn) throws Exception {
        return new MssRoundPoint().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssRoundPoint().deleteById(id, conn);
    }

    public static List<MssRoundPoint> getAll(Connection conn) throws Exception {
        return new MssRoundPoint().getAllList(conn);
    }

//fin zona de reemplazo
}