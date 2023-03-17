package web.gates;

import java.sql.Connection;
import utilities.MySQLQuery;
import web.ShortException;
import web.gates.cylTrip.GtCylTrip;

public class CheckCylsTrip {

    private GtTripType tripType = new GtTripType();
    private GtCylTrip trip = new GtCylTrip();
    private final String evType;
    private final Connection ep;
    public boolean passed = true;

    public CheckCylsTrip(String evType, Integer tripId, GtTripType tripType, boolean fromMobile, Connection ep) throws Exception {
        this.ep = ep;
        this.evType = evType;
        this.tripType = tripType;
        trip = new GtCylTrip().select(tripId, ep);
        VerifResult v = getValidation();
        if (!v.passed) {
            passed = v.passed;
            if (!fromMobile) {
                new MySQLQuery("UPDATE gt_cyl_trip SET  " + evType + "dt = NULL, blocked = TRUE WHERE id = " + tripId).executeUpdate(ep);
            }
            throw new ShortException("Hay inconsistencias en el inventario de " + GtGlpInv.getTypes(evType).toLowerCase() + ".\n" + v.msg);
        }
    }

    private Boolean getCheked() {
        if (evType.equals("c")) {
            return trip.cdt != null;
        } else if (evType.equals("s")) {
            return trip.sdt != null;
        } else if (evType.equals("e")) {
            return trip.edt != null;
        } else if (evType.equals("d")) {
            return trip.ddt != null;
        }
        return null;
    }

    private VerifResult getValidation() throws Exception {
        TripItem raw = new TripItem();
        raw.id = trip.id;
        raw.name = tripType.name;
        raw.authDoc = trip.authDoc;
        raw.checked = getCheked();
        raw.sameState = tripType.sameState;
        raw.needsValidation = GtTripType.getNeedsVal(tripType, evType);
        raw.pul = tripType.pul;

        String query = null;
        if (evType.equals("c")) {
            // no se requieren validaciones
        } else if (evType.equals("s")) {
            if (raw.needsValidation) {
                query = "SELECT CONCAT(CAST(sum(amount*IF(i.type = 'c',-1,1)) AS CHAR),' X ',capa.`name`,' ',type.short_name,' ',IF(state='l','Llenos',IF(state='v','Vacíos',IF(state='f','Fugas','')))) "
                        + "FROM gt_cyl_inv i "//0
                        + "INNER JOIN inv_cyl_type AS type ON i.type_id = type.id "//1
                        + "INNER JOIN cylinder_type AS capa ON i.capa_id = capa.id "//2
                        + "WHERE i.trip_id = " + raw.id + " AND (i.type = 'c' OR i.type = 's') "//3
                        + "GROUP BY i.capa_id, i.type_id, state "//4
                        + "HAVING sum(amount*IF(i.type = 'c',-1,1)) <> 0 "
                        + "ORDER BY CAST(capa.`name` AS SIGNED) ASC ";
            }
        } else if (evType.equals("e")) {
            if (raw.needsValidation) {
                if (raw.sameState) {
                    query = "SELECT CONCAT(CAST(SUM(c) AS CHAR),' X ',capa.`name`,' ',type.short_name,' ',IF(state='l','Llenos',IF(state='v','Vacíos',IF(state='f','Fugas','')))) "
                            + "FROM ( "//0
                            + "SELECT i.capa_id, i.type_id, state, SUM(amount * IF(i.type = 'e',-1,1)) as c "//1
                            + "FROM gt_cyl_inv i "//2
                            + "WHERE i.trip_id = " + raw.id + "  AND (i.type = 'e' OR i.type = 's') "//3
                            + "AND (state = 'l' OR state = 'f') "//4
                            + "GROUP BY i.capa_id, i.type_id HAVING SUM(amount * IF(i.type = 'e',-1,1)) <> 0 "//5
                            + "UNION ALL "//6
                            + "SELECT i.capa_id, i.type_id, state, amount * IF(i.type = 'e',-1,1) as c "//7
                            + "FROM gt_cyl_inv i "//8
                            + "WHERE i.trip_id = " + raw.id + "  AND (i.type = 'e' OR i.type = 's') "//9
                            + "AND state='v' "//10
                            + "UNION ALL "//11
                            + "SELECT capa_id, type_id, state, amount AS c "//12
                            + "FROM gt_cyl_nov n "
                            + "INNER JOIN gt_nov_type nt ON nt.id = n.nov_type_id AND nt.affects_cyls = 1 "
                            + "WHERE trip_id =  " + raw.id + "  "//13
                            + ") AS l1 "//14
                            + "INNER JOIN inv_cyl_type AS type ON l1.type_id = type.id "//15
                            + "INNER JOIN cylinder_type AS capa ON l1.capa_id = capa.id "//16
                            + "GROUP BY l1.capa_id, l1.type_id, state HAVING sum(c) <> 0 "//17
                            + "ORDER BY CAST(capa.`name` AS SIGNED) ASC";
                } else {
                    query = "SELECT CONCAT(CAST(SUM(c) AS CHAR),' X ',capa.`name`,' ',type.short_name,' ') "
                            + "FROM ( "//0
                            + "SELECT i.capa_id, i.type_id, amount*IF(i.type = 'e',-1,1) as c "//1
                            + "FROM gt_cyl_inv i "//2
                            + "WHERE i.trip_id = " + raw.id + " AND (i.type = 'e' OR i.type = 's') "//3
                            + "UNION ALL "//4
                            + "SELECT capa_id, type_id, amount AS c "
                            + "FROM gt_cyl_nov n "
                            + "INNER JOIN gt_nov_type nt ON nt.id = n.nov_type_id AND nt.affects_cyls = 1 "
                            + "WHERE trip_id =  " + raw.id + " "//5
                            + ") AS l1 "//6
                            + "INNER JOIN inv_cyl_type AS type ON l1.type_id = type.id "//7
                            + "INNER JOIN cylinder_type AS capa ON l1.capa_id = capa.id "//8
                            + "GROUP BY l1.capa_id, l1.type_id HAVING sum(c) <> 0 ORDER BY CAST(capa.`name` AS SIGNED) ASC";
                }
            }
        } else if (evType.equals("d")) {
            if (raw.needsValidation) {
                query = "SELECT CONCAT(CAST(SUM(amount*IF(i.type = 'd', -1,1)) AS CHAR),' X ',capa.`name`,' ',type.short_name,' ',IF(state='l','Llenos',IF(state='v','Vacíos',IF(state='f','Fugas','')))) "
                        + "FROM gt_cyl_inv i "//0
                        + "INNER JOIN inv_cyl_type AS type ON i.type_id = type.id "//1
                        + "INNER JOIN cylinder_type AS capa ON i.capa_id = capa.id "//2
                        + "WHERE i.trip_id = " + raw.id + " AND (i.type = 'e' OR i.type = 'd') "//3
                        + "GROUP BY i.capa_id, i.type_id, state "//4
                        + "HAVING sum(amount*IF(i.type = 'd', -1, 1)) <> 0 "//5
                        + "ORDER BY CAST(capa.`name` AS SIGNED) ASC";
            }
        }
        VerifResult v = new VerifResult();
        v.passed = true;
        if (!evType.equals("c")) {
            if (raw.needsValidation) {
                Object[][] data = new MySQLQuery(query).getRecords(ep);
                if (data.length > 0) {
                    v.msg = getResults(data);
                    v.passed = false;
                }
                if (raw.pul && evType.equals("e")) {
                    Object[][] dataP = new MySQLQuery("SELECT CONCAT(CAST(SUM(c) AS CHAR),' X ',capa.`name`,' ',type.short_name,' ','Vacíos') "
                            + "FROM "
                            + "( "
                            + "SELECT rc.capa_id AS capa_id, rc.type_id AS type_id, SUM(rc.amount)*-1 AS c "
                            + "FROM gt_reload AS r "
                            + "INNER JOIN gt_reload_cyls AS rc ON rc.reload_id = r.id "
                            + "WHERE r.pulm_trip_id = " + raw.id + " AND r.cancel = 0 "
                            + "GROUP BY rc.capa_id,rc.type_id "
                            + " "
                            + "UNION ALL "
                            + " "
                            + "SELECT capa_id, type_id, SUM(amount)  AS c "
                            + "FROM gt_cyl_inv WHERE trip_id = " + raw.id + " AND state = 'v' AND type = 'e' "
                            + "GROUP BY capa_id,type_id "
                            + " "
                            + "UNION ALL "
                            + " "
                            + "SELECT capa_id, type_id, SUM(amount)*-1 AS c "
                            + "FROM gt_cyl_nov n "
                            + "INNER JOIN gt_nov_type nt ON nt.id = n.nov_type_id AND nt.affects_cyls = 1 "
                            + "WHERE trip_id = " + raw.id + " "
                            + "AND state = 'v' "
                            + "GROUP BY capa_id,type_id "
                            + " "
                            + ") AS l1 "
                            + "INNER JOIN inv_cyl_type AS type ON l1.type_id = type.id "
                            + "INNER JOIN cylinder_type AS capa ON l1.capa_id = capa.id "
                            + "GROUP BY capa_id, type_id HAVING SUM(c) <> 0 ORDER BY CAST(capa.`name` AS SIGNED) ASC").getRecords(ep);
                    if (dataP.length > 0) {
                        v.msg = getResults(dataP);
                        v.passed = false;
                    }
                }
                return v;
            }
        }
        return v;
    }

    public String getResults(Object[][] data) {
        String msg = "";
        for (Object[] row : data) {
            msg += MySQLQuery.getAsString(row[0]) + "\n";
        }
        return msg;
    }

    class VerifResult {
        String msg = "";
        boolean passed;
    }

    class TripItem {

        public int id;
        public String name;
        public String authDoc;
        public boolean checked;
        public boolean sameState;
        public boolean needsValidation;
        public boolean pul;
    }
}