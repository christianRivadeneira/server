package api.mto.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MtoRoute extends BaseModel<MtoRoute> {
//inicio zona de reemplazo

    public String name;
    public int len;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "len",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, len);
        q.setParam(3, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        len = MySQLQuery.getAsInteger(row[1]);
        active = MySQLQuery.getAsBoolean(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mto_route";
    }

    public static String getSelFlds(String alias) {
        return new MtoRoute().getSelFldsForAlias(alias);
    }

    public static List<MtoRoute> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MtoRoute().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MtoRoute().deleteById(id, conn);
    }

    public static List<MtoRoute> getAll(Connection conn) throws Exception {
        return new MtoRoute().getAllList(conn);
    }

//fin zona de reemplazo
    public static boolean hasPoints(int routeId, Connection conn) throws Exception {
        return new MySQLQuery("SELECT COUNT(*) > 0 FROM mto_route_point p WHERE p.route_id = ?1").setParam(1, routeId).getAsBoolean(conn);
    }

    public static void validateRoutePoints(int routeId, Connection conn) throws Exception {

        if (new MySQLQuery("SELECT COUNT(*) > 4 "
                + "FROM ( "
                + "SELECT * FROM mto_route_point p "
                + "WHERE p.route_id = ?1 AND p.type IN ('going_start','comming_start','comming','end') "
                + "GROUP BY p.type) AS agrup ").setParam(1, routeId).getAsBoolean(conn)) {
            throw new Exception("La ruta seleccionada no tiene configurado todos los puntos para el viaje.");
        }

        boolean hasAllCoords = new MySQLQuery("SELECT COUNT(*)=0 "
                + " FROM mto_route_point r "
                + " WHERE (r.lat IS NULL OR r.lon IS NULL) AND r.route_id = ?1").setParam(1, routeId).getAsBoolean(conn);

        if (!hasAllCoords) {
            throw new Exception("Falta coordenadas en algunos puntos");
        }

        Object[][] orderPoints = new MySQLQuery("SELECT "
                + " CASE r.type "
                + " WHEN 'going_start' THEN 1 "
                + " WHEN 'going' THEN 2 "
                + " WHEN 'comming_start' THEN 3 "
                + " WHEN 'comming' THEN 4 "
                + " WHEN 'end' THEN 5 "
                + " END "
                + " FROM mto_route_point r WHERE r.route_id = ?1 "
                + " ORDER BY r.place ASC "
        ).setParam(1, routeId).getRecords(conn);

        for (int i = 0; i < orderPoints.length - 1; i++) {
            int value = MySQLQuery.getAsInteger(orderPoints[i][0]);
            int nextValue = MySQLQuery.getAsInteger(orderPoints[i + 1][0]);
            if (value > nextValue) {
                throw new Exception("Los puntos no estan ordenados correctamente");
            }
        }

        Object[][] goingFulls = new MySQLQuery(" SELECT (r.h_full * 60) + r.m_full "
                + " FROM mto_route_point r WHERE r.route_id = ?1 AND  "
                + " r.`type` IN ('going', 'comming_start') "
                + " ORDER BY r.place ASC; ").setParam(1, routeId).getRecords(conn);

        for (int i = 0; i < goingFulls.length - 1; i++) {
            int value = MySQLQuery.getAsInteger(goingFulls[i][0]);
            int nextValue = MySQLQuery.getAsInteger(goingFulls[i + 1][0]);
            if (value > nextValue) {
                throw new Exception("Los tiempos de ida no estan ordenados correctamente");
            }
        }

        Object[][] commingFulls = new MySQLQuery("SELECT (r.h_full * 60) + r.m_full "
                + " FROM mto_route_point r WHERE r.route_id = ?1 AND "
                + " r.`type` IN ('comming', 'end') "
                + " ORDER BY r.place ASC; ").setParam(1, routeId).getRecords(conn);

        for (int i = 0; i < commingFulls.length - 1; i++) {
            int value = MySQLQuery.getAsInteger(commingFulls[i][0]);
            int nextValue = MySQLQuery.getAsInteger(commingFulls[i + 1][0]);
            if (value > nextValue) {
                throw new Exception("Los tiempos de regreso no estan ordenados correctamente");
            }
        }

        Boolean hasCompliment = new MySQLQuery("SELECT COUNT(*)>0 FROM mto_route_point r WHERE r.route_id = ?1 AND (h_part IS NOT NULL OR m_part IS NOT NULL);").setParam(1, routeId).getAsBoolean(conn);
        if (hasCompliment) {
            Boolean hasStart = new MySQLQuery("SELECT COUNT(*)>0 FROM mto_route_point r WHERE r.route_id = ?1 AND (h_part = 0 AND m_part = 0);").setParam(1, routeId).getAsBoolean(conn);
            if (!hasStart) {
                throw new Exception("No se ha establecido el inicio del cumplido");
            } else {
                Boolean hasEnd = new MySQLQuery("SELECT COUNT(*)>0 FROM mto_route_point r WHERE r.route_id = ?1 AND r.`type`= 'end' AND (h_part > 0 OR m_part > 0);").setParam(1, routeId).getAsBoolean(conn);
                if (!hasEnd) {
                    throw new Exception("No se ha establecido el fin del cumplido");
                }
            }
            Object[][] partTimes = new MySQLQuery("SELECT (r.h_part * 60) + r.m_part "
                    + "FROM mto_route_point r WHERE r.route_id = ?1 AND "
                    + "r.`type` IN ('comming', 'end') AND h_part IS NOT NULL "
                    + "ORDER BY r.place ASC; ").setParam(1, routeId).getRecords(conn);

            for (int i = 0; i < partTimes.length - 1; i++) {
                Integer value = MySQLQuery.getAsInteger(partTimes[i][0]);
                Integer nextValue = MySQLQuery.getAsInteger(partTimes[i + 1][0]);
                if (value > nextValue) {
                    throw new Exception("Los tiempos del cumplido no estan ordenados correctamente");
                }
            }
        }

    }

}
