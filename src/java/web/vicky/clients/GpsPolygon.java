package web.vicky.clients;

import api.sys.model.Sector;
import java.awt.geom.Path2D;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

public class GpsPolygon {
    
    public static final int TYPE_SECTOR = 1;
    public static final int TYPE_NEIGH = 2;
    public static final int TYPE_CITY = 4;
    public static final int TYPE_ZONE = 5; //Zonas comerciales
    public static final int TYPE_ZONE_SALE = 6; //GeoCerca de cobertura de ventas (Nariño - Cauca - Putumarzo)
    public static final int TYPE_EMAS_GREEN_ZONE = 3;

    public static Integer hit(String idsQuery, int ownerType, double lat, double lon, Connection conn) throws Exception {
        Object[][] idsData = new MySQLQuery(idsQuery).getRecords(conn);
        for (Object[] idsRow : idsData) {
            int id = MySQLQuery.getAsInteger(idsRow[0]);
            Object[] boxRow = new MySQLQuery("SELECT MAX(g.lat), MAX(g.lon), MIN(g.lat), MIN(g.lon) FROM gps_polygon g WHERE g.owner_type = " + ownerType + " AND g.owner_id = " + id).getRecord(conn);
            if (boxRow != null) {
                double maxLat = MySQLQuery.getAsDouble(boxRow[0]);
                double maxLon = MySQLQuery.getAsDouble(boxRow[1]);
                double minLat = MySQLQuery.getAsDouble(boxRow[2]);
                double minLon = MySQLQuery.getAsDouble(boxRow[3]);
                if (minLat < lat && maxLat > lat && minLon < lon && maxLon > lon) {
                    Object[][] polData = new MySQLQuery("SELECT g.lat, g.lon FROM gps_polygon g WHERE g.owner_type = " + ownerType + " AND g.owner_id = " + id + " ORDER BY place").getRecords(conn);
                    if (polData != null && polData.length > 2) {
                        Path2D path = new Path2D.Double();
                        path.moveTo(MySQLQuery.getAsDouble(polData[0][1]), MySQLQuery.getAsDouble(polData[0][0]));
                        for (int j = 1; j < polData.length; j++) {
                            Object[] polRow = polData[j];
                            path.lineTo(MySQLQuery.getAsDouble(polRow[1]), MySQLQuery.getAsDouble(polRow[0]));
                        }
                        path.closePath();
                        if (path.contains(lon, lat)) {
                            return id;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static Sector hitSector(double lat, double lon) {
        Connection conn = null;
        try {
            conn = MySQLCommon.getConnection("sigmads", null);
            Integer id = hit("SELECT id FROM sector s", TYPE_SECTOR, lat, lon, conn);
            if (id != null) {
                return new Sector().select(id, conn);
            }
            return null;
        } catch (Exception ex) {
            Logger.getLogger(GpsPolygon.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            MySQLCommon.closeConnection(conn);
        }
    }

    public static Integer hitNeigh(double lat, double lon, int cityId) {
        Connection conn = null;
        try {
            conn = MySQLCommon.getConnection("sigmads", null);
            return hit("SELECT n.id FROM "
                    + "neigh n "
                    + "INNER JOIN sector s ON s.id = n.sector_id "
                    + "WHERE s.city_id = " + cityId, 2, lat, lon, conn);
        } catch (Exception ex) {
            Logger.getLogger(GpsPolygon.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            MySQLCommon.closeConnection(conn);
        }
    }
}
