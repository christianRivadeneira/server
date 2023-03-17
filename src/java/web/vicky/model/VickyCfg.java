package web.vicky.model;

import java.sql.Connection;
import utilities.MySQLQuery;

public class VickyCfg {

    public int appId;

//inicio zona de reemplazo
     public int id;
    public double maxRadiousKm;
    public double radiousDeltaKm;
    public int maxSectorRounds;
    public int maxGpsRounds;
    public int roundTimeSecs;
    public int pauseTimeSecs;
    public boolean editOrders;
    public Integer testEmpId;
    public boolean enabled;
    public double maxRadioTksKm;

    private static final String SEL_FLDS = "`max_radious_km`, "
            + "`radious_delta_km`, "
            + "`max_sector_rounds`, "
            + "`max_gps_rounds`, "
            + "`round_time_secs`, "
            + "`pause_time_secs`, "
            + "`edit_orders`, "
            + "`test_emp_id`, "
            + "`enabled`, "
            + "`max_radio_tks_km`";

    /**
     * ******IMPORTANTE!********** TENER CUIDADO CON appId, CUALQUIER DUDA
     * CONSULTAR CON MARIO
     *
     */
    private static final String SET_FLDS = "vicky_cfg SET "
            + "`max_radious_km` = ?1, "
            + "`radious_delta_km` = ?2, "
            + "`max_sector_rounds` = ?3, "
            + "`max_gps_rounds` = ?4, "
            + "`round_time_secs` = ?5, "
            + "`pause_time_secs` = ?6, "
            + "`edit_orders` = ?7, "
            + "`test_emp_id` = ?8, "
            + "`enabled` = ?9, "
            + "`max_radio_tks_km` = ?10";

    private static void setFields(VickyCfg obj, MySQLQuery q) {
        q.setParam(1, obj.maxRadiousKm);
        q.setParam(2, obj.radiousDeltaKm);
        q.setParam(3, obj.maxSectorRounds);
        q.setParam(4, obj.maxGpsRounds);
        q.setParam(5, obj.roundTimeSecs);
        q.setParam(6, obj.pauseTimeSecs);
        q.setParam(7, obj.editOrders);
        q.setParam(8, obj.testEmpId);
        q.setParam(9, obj.enabled);
        q.setParam(10, obj.maxRadioTksKm);

    }

    public static VickyCfg getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        VickyCfg obj = new VickyCfg();
        obj.maxRadiousKm = MySQLQuery.getAsDouble(row[0]);
        obj.radiousDeltaKm = MySQLQuery.getAsDouble(row[1]);
        obj.maxSectorRounds = MySQLQuery.getAsInteger(row[2]);
        obj.maxGpsRounds = MySQLQuery.getAsInteger(row[3]);
        obj.roundTimeSecs = MySQLQuery.getAsInteger(row[4]);
        obj.pauseTimeSecs = MySQLQuery.getAsInteger(row[5]);
        obj.editOrders = MySQLQuery.getAsBoolean(row[6]);
        obj.testEmpId = MySQLQuery.getAsInteger(row[7]);
        obj.enabled = MySQLQuery.getAsBoolean(row[8]);
        obj.maxRadioTksKm = MySQLQuery.getAsDouble(row[9]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public static VickyCfg select(int id, Connection conn) throws Exception {
        VickyCfg cfg = VickyCfg.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(conn));
        cfg.appId = new MySQLQuery("SELECT id FROM system_app WHERE package_name = 'com.glp.subsidiosonline'").getAsInteger(conn);
        return cfg;
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM vicky_cfg WHERE id = " + id;
    }

    public static String getUpdateQuery(VickyCfg obj) {
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }
}
