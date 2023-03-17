package api.mto.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class MtoTripCheck extends BaseModel<MtoTripCheck> {
//inicio zona de reemplazo

    public int place;
    public String type;
    public Integer tripId;
    public Integer pointId;
    public Date expFull;
    public Date expPart;
    public Date reg;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "place",
            "type",
            "trip_id",
            "point_id",
            "exp_full",
            "exp_part",
            "reg"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, place);
        q.setParam(2, type);
        q.setParam(3, tripId);
        q.setParam(4, pointId);
        q.setParam(5, expFull);
        q.setParam(6, expPart);
        q.setParam(7, reg);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        place = MySQLQuery.getAsInteger(row[0]);
        type = MySQLQuery.getAsString(row[1]);
        tripId = MySQLQuery.getAsInteger(row[2]);
        pointId = MySQLQuery.getAsInteger(row[3]);
        expFull = MySQLQuery.getAsDate(row[4]);
        expPart = MySQLQuery.getAsDate(row[5]);
        reg = MySQLQuery.getAsDate(row[6]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mto_trip_check";
    }

    public static String getSelFlds(String alias) {
        return new MtoTripCheck().getSelFldsForAlias(alias);
    }

    public static List<MtoTripCheck> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MtoTripCheck().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MtoTripCheck().deleteById(id, conn);
    }

    public static List<MtoTripCheck> getAll(Connection conn) throws Exception {
        return new MtoTripCheck().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<MtoTripCheck> getByTrip(int tripId, Connection conn) throws Exception {
        return MtoTripCheck.getList(new MySQLQuery("SELECT " + MtoTripCheck.getSelFlds("") + " FROM mto_trip_check WHERE trip_id = ?1 ORDER BY place").setParam(1, tripId), conn);
    }
}
