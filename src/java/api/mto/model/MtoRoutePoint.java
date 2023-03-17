package api.mto.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MtoRoutePoint extends BaseModel<MtoRoutePoint> {
//inicio zona de reemplazo

    public String name;
    public String type;
    public int routeId;
    public Double lat;
    public Double lon;
    public Integer hFull;
    public Integer mFull;
    public Integer hPart;
    public Integer mPart;
    public int place;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "type",
            "route_id",
            "lat",
            "lon",
            "h_full",
            "m_full",
            "h_part",
            "m_part",
            "place"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, type);
        q.setParam(3, routeId);
        q.setParam(4, lat);
        q.setParam(5, lon);
        q.setParam(6, hFull);
        q.setParam(7, mFull);
        q.setParam(8, hPart);
        q.setParam(9, mPart);
        q.setParam(10, place);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        type = MySQLQuery.getAsString(row[1]);
        routeId = MySQLQuery.getAsInteger(row[2]);
        lat = MySQLQuery.getAsDouble(row[3]);
        lon = MySQLQuery.getAsDouble(row[4]);
        hFull = MySQLQuery.getAsInteger(row[5]);
        mFull = MySQLQuery.getAsInteger(row[6]);
        hPart = MySQLQuery.getAsInteger(row[7]);
        mPart = MySQLQuery.getAsInteger(row[8]);
        place = MySQLQuery.getAsInteger(row[9]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mto_route_point";
    }

    public static String getSelFlds(String alias) {
        return new MtoRoutePoint().getSelFldsForAlias(alias);
    }

    public static List<MtoRoutePoint> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MtoRoutePoint().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MtoRoutePoint().deleteById(id, conn);
    }

    public static List<MtoRoutePoint> getAll(Connection conn) throws Exception {
        return new MtoRoutePoint().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<MtoRoutePoint> getByRoute(int routeId, Connection conn) throws Exception {
        return getList(new MySQLQuery("SELECT " + getSelFlds("p") + " FROM mto_route_point p WHERE p.route_id = ?1 ORDER BY p.place").setParam(1, routeId), conn);
    }

    public static String getEnumOptions(String fieldName) {
        if (fieldName.equals("type")) {
            return "going_start=Inicio de Viaje&"
                    + "going=Control Ida&"
                    + "comming_start=Almacenadora&"
                    + "comming=Control Regreso&"
                    + "end=Fin de viaje";
        }
        return null;
    }
}
