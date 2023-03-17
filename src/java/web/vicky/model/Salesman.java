package web.vicky.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class Salesman {

    public int vhId;
    public int empId;
    public int entId;
    public double d;
    public Double lat;
    public Double lon;
    public Date lastOffered;

    public static List<Salesman> getSalesmen(Integer sectorId, OrdCylOrderTimer t, VickyCfg cfg, Connection conn) throws Exception {
        return getSalesmen(null, null, null, sectorId, t, cfg, conn);
    }

    public static List<Salesman> getSalesmen(Double lat, Double lon, Integer officeId, OrdCylOrderTimer t, VickyCfg cfg, Connection conn) throws Exception {
        return getSalesmen(lat, lon, officeId, null, t, cfg, conn);
    }

    private static List<Salesman> getSalesmen(Double lat, Double lon, Integer officeId, Integer sectorId, OrdCylOrderTimer t, VickyCfg cfg, Connection conn) throws Exception {
        List<Salesman> rta = new ArrayList<>();
        if (cfg.enabled) {
            MySQLQuery q = new MySQLQuery("SELECT DISTINCT "
                    + "v.id, "//0
                    + "m.employee_id, "//1
                    + "en.id, "//2
                    + "m.latitude, "//3
                    + "m.longitude,"//4
                    + "TIMESTAMPDIFF(MINUTE, m.date, now()) <= 20 "//5
                    + "FROM driver_vehicle dv "
                    + "LEFT JOIN gps_last_coord m ON dv.driver_id = m.employee_id "
                    + "INNER JOIN vehicle v ON dv.vehicle_id = v.id "
                    + "INNER JOIN agency a ON v.agency_id = a.id "
                    + "INNER JOIN enterprise en ON a.enterprise_id = en.id "
                    + "INNER JOIN ord_vehicle_office vo ON vo.vehicle_id = v.id "
                    + "INNER JOIN ord_office off ON off.id = vo.office_id "
                    + "INNER JOIN employee e ON dv.driver_id = e.id AND e.active "
                    + "INNER JOIN dto_salesman sm ON sm.document = e.document AND sm.offer_orders "
                    + "WHERE v.active  AND dv.`end` is null AND off.sales_app "
                    + (officeId != null ? "AND vo.office_id = " + officeId : "AND vo.sector_id = " + sectorId));

            Object[][] data = q.getRecords(conn);
            rta = getSalesmenList(data, lat, lon, t, cfg, conn);
        }
        return rta;
    }

    public static List<Salesman> getSalesmenByCity(int cityId, OrdCylOrderTimer t, VickyCfg cfg, Connection conn) throws Exception {
        List<Salesman> rta = new ArrayList<>();
        if (cfg.enabled) {
            MySQLQuery q = new MySQLQuery("SELECT "
                    + "v.id, "
                    + "e.id, "
                    + "a.enterprise_id, "
                    + "NULL, "
                    + "NULL,"
                    + "NULL "
                    + "FROM "
                    + "employee e "
                    + "INNER JOIN driver_vehicle dv ON dv.driver_id = e.id and dv.`end` is null "
                    + "INNER JOIN vehicle v ON dv.vehicle_id = v.id "
                    + "INNER JOIN agency a ON v.agency_id = a.id "
                    + "INNER JOIN ord_vehicle_office vo ON vo.vehicle_id = v.id "
                    + "INNER JOIN dto_salesman sm ON sm.document = e.document AND sm.offer_orders "
                    + "WHERE v.active "
                    + "AND a.city_id = " + cityId);

            Object[][] data = q.getRecords(conn);
            rta = getSalesmenList(data, null, null, t, cfg, conn);
        }
        return rta;
    }

    private static List<Salesman> getSalesmenList(Object[][] data, Double lat, Double lon, OrdCylOrderTimer t, VickyCfg cfg, Connection conn) throws Exception {
        List<Salesman> rta = new ArrayList<>();
        OrdCylOrderOffer[] offers = OrdCylOrderOffer.getByOrderId(t.orderId, conn);
        for (Object[] sRow : data) {
            Salesman s = new Salesman();
            s.vhId = MySQLQuery.getAsInteger(sRow[0]);
            s.empId = MySQLQuery.getAsInteger(sRow[1]);
            s.entId = MySQLQuery.getAsInteger(sRow[2]);
            s.lat = MySQLQuery.getAsDouble(sRow[3]);
            s.lon = MySQLQuery.getAsDouble(sRow[4]);
            Boolean freshCoord = MySQLQuery.getAsBoolean(sRow[5]);
            if (freshCoord == null) {
                freshCoord = false;
            }

            if (lat != null) {
                s.d = distFrom(lat, lon, s.lat, s.lon);
            }
            for (OrdCylOrderOffer offer : offers) {
                if (offer.empId == s.empId) {
                    if (s.lastOffered == null || offer.offerDt.compareTo(s.lastOffered) > 0) {
                        s.lastOffered = offer.offerDt;
                    }
                }
            }
            if ((lat == null || freshCoord) && OrdCylOrderOffer.isDriverFree(s.empId, cfg, conn)) {
                rta.add(s);
            }
        }
        return rta;
    }

    private static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371.0; // miles 3958.75 (or 6371.0 kilometers)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;
        return dist;
    }
}
