package api.trk.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TrkCylLoad extends BaseModel<TrkCylLoad> {
    
    //fuera de la zona de reemplazo
    public String nif;
    public String cylCap;
    //
    
//inicio zona de reemplazo

    public Integer cylTripId;
    public Integer preLoadId;
    public int cylId;
    public Integer altCylId;
    public String type;
    public Date dateOut;
    public Date dateImpMto;
    public Date dateEntry;
    public Date dateDel;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "cyl_trip_id",
            "pre_load_id",
            "cyl_id",
            "alt_cyl_id",
            "type",
            "date_out",
            "date_imp_mto",
            "date_entry",
            "date_del"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, cylTripId);
        q.setParam(2, preLoadId);
        q.setParam(3, cylId);
        q.setParam(4, altCylId);
        q.setParam(5, type);
        q.setParam(6, dateOut);
        q.setParam(7, dateImpMto);
        q.setParam(8, dateEntry);
        q.setParam(9, dateDel);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        cylTripId = MySQLQuery.getAsInteger(row[0]);
        preLoadId = MySQLQuery.getAsInteger(row[1]);
        cylId = MySQLQuery.getAsInteger(row[2]);
        altCylId = MySQLQuery.getAsInteger(row[3]);
        type = MySQLQuery.getAsString(row[4]);
        dateOut = MySQLQuery.getAsDate(row[5]);
        dateImpMto = MySQLQuery.getAsDate(row[6]);
        dateEntry = MySQLQuery.getAsDate(row[7]);
        dateDel = MySQLQuery.getAsDate(row[8]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "trk_cyl_load";
    }

    public static String getSelFlds(String alias) {
        return new TrkCylLoad().getSelFldsForAlias(alias);
    }

    public static List<TrkCylLoad> getList(MySQLQuery q, Connection conn) throws Exception {
        return new TrkCylLoad().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new TrkCylLoad().deleteById(id, conn);
    }

    public static List<TrkCylLoad> getAll(Connection conn) throws Exception {
        return new TrkCylLoad().getAllList(conn);
    }

//fin zona de reemplazo

    public static Map<Integer, Integer> getAmounts(int tripId, String type, Connection conn) throws Exception {
        Object[][] data = new MySQLQuery("SELECT c.cyl_type_id, COUNT(*) "
                + "FROM trk_cyl_load l "
                + "INNER JOIN trk_cyl c ON l.cyl_id = c.id "
                + "WHERE l.cyl_trip_id = " + tripId + " AND l.date_del IS NULL AND type = ?1 "
                + "GROUP BY c.cyl_type_id").setParam(1, type).getRecords(conn);

        Map<Integer, Integer> map = new HashMap();
        for (Object[] row : data) {
            map.put(MySQLQuery.getAsInteger(row[0]), MySQLQuery.getAsInteger(row[1]));
        }
        return map;
    }

    //'load','reload','trip'
    public static Integer getAmount(int tripId, String type, int cylTypeId, Connection conn) throws Exception {
        return new MySQLQuery("SELECT COUNT(*) "
                + "FROM trk_cyl_load l "
                + "INNER JOIN trk_cyl c ON l.cyl_id = c.id "
                + "WHERE l.cyl_trip_id = " + tripId + " "
                + "AND l.date_del IS NULL "
                + "AND type = ?1 "
                + "AND c.cyl_type_id = " + cylTypeId + " "
                + "GROUP BY c.cyl_type_id").setParam(1, type).getAsInteger(conn);
    }
}
