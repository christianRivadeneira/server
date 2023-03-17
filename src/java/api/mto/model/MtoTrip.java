package api.mto.model;

import api.BaseModel;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import utilities.Dates;
import utilities.MySQLQuery;

public class MtoTrip extends BaseModel<MtoTrip> {
//inicio zona de reemplazo

    public Date tripDate;
    public BigDecimal gals;
    public BigDecimal price;
    public String notes;
    public int routeId;
    public Integer employeeId;
    public Integer driverId;
    public int vehId;
    public String bill;
    public Integer len;
    public BigDecimal expGals;
    public Date expDeparture;
    public boolean notified;
    public String guide;
    public String compliment;
    public boolean canceled;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "trip_date",
            "gals",
            "price",
            "notes",
            "route_id",
            "employee_id",
            "driver_id",
            "veh_id",
            "bill",
            "len",
            "exp_gals",
            "exp_departure",
            "notified",
            "guide",
            "compliment",
            "canceled"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, tripDate);
        q.setParam(2, gals);
        q.setParam(3, price);
        q.setParam(4, notes);
        q.setParam(5, routeId);
        q.setParam(6, employeeId);
        q.setParam(7, driverId);
        q.setParam(8, vehId);
        q.setParam(9, bill);
        q.setParam(10, len);
        q.setParam(11, expGals);
        q.setParam(12, expDeparture);
        q.setParam(13, notified);
        q.setParam(14, guide);
        q.setParam(15, compliment);
        q.setParam(16, canceled);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        tripDate = MySQLQuery.getAsDate(row[0]);
        gals = MySQLQuery.getAsBigDecimal(row[1], false);
        price = MySQLQuery.getAsBigDecimal(row[2], false);
        notes = MySQLQuery.getAsString(row[3]);
        routeId = MySQLQuery.getAsInteger(row[4]);
        employeeId = MySQLQuery.getAsInteger(row[5]);
        driverId = MySQLQuery.getAsInteger(row[6]);
        vehId = MySQLQuery.getAsInteger(row[7]);
        bill = MySQLQuery.getAsString(row[8]);
        len = MySQLQuery.getAsInteger(row[9]);
        expGals = MySQLQuery.getAsBigDecimal(row[10], false);
        expDeparture = MySQLQuery.getAsDate(row[11]);
        notified = MySQLQuery.getAsBoolean(row[12]);
        guide = MySQLQuery.getAsString(row[13]);
        compliment = MySQLQuery.getAsString(row[14]);
        canceled = MySQLQuery.getAsBoolean(row[15]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mto_trip";
    }

    public static String getSelFlds(String alias) {
        return new MtoTrip().getSelFldsForAlias(alias);
    }

    public static List<MtoTrip> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MtoTrip().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MtoTrip().deleteById(id, conn);
    }

    public static List<MtoTrip> getAll(Connection conn) throws Exception {
        return new MtoTrip().getAllList(conn);
    }

//fin zona de reemplazo
    public static BigDecimal getPrice(int routeId, BigDecimal gals, Date date, Connection conn) throws Exception {
        String priceQ = "SELECT price "
                + "FROM mto_route_price "//0
                + "WHERE "//1
                + "mto_route_price.route_id = " + routeId + "  AND "//2
                + "mto_route_price.since_date = "//3
                + "(SELECT "//4
                + "MAX(p.since_date) "//5
                + "FROM "
                + "mto_route_price AS p "
                + "WHERE "
                + "p.route_id = " + routeId + " AND "
                + "p.since_date <= ?1 )";
        MySQLQuery q = new MySQLQuery(priceQ);
        q.setParam(1, Dates.trimDate(date));
        if (gals != null) {
            BigDecimal curPrice = q.getAsBigDecimal(conn, false);
            if (curPrice != null) {
                return gals.multiply(curPrice);
            }
        }
        return null;
    }

    public static boolean isTripAdmin(int empId, Connection conn) throws Exception {
        if (empId == 1) {
            return true;
        } else {
            MySQLQuery mq = new MySQLQuery("SELECT IF(SUM(cfg.app_view_all_trips) > 0, 1, 0) "
                    + "FROM profile p "
                    + "INNER JOIN login l ON l.profile_id = p.id "
                    + "INNER JOIN mto_prof_cfg cfg ON cfg.prof_id = p.id "
                    + "WHERE p.active AND l.employee_id = " + empId + " AND p.is_mobile = TRUE AND p.menu_id = " + MtoProfCfg.MODULE_ID + " ");

            Boolean isAdmin = mq.getAsBoolean(conn);
            return (isAdmin == true);
        }
    }

    public static List<Integer> getAdminIds(Connection conn) throws Exception {
        List<Integer> adminIds = new ArrayList<>();        
        Object[][] data = new MySQLQuery("SELECT DISTINCT l.employee_id "
                + " FROM profile p "
                + " INNER JOIN login l ON l.profile_id = p.id "
                + " INNER JOIN mto_prof_cfg cfg ON cfg.prof_id = p.id AND cfg.app_view_all_trips "
                + " WHERE p.active AND p.is_mobile = TRUE AND p.menu_id = " + MtoProfCfg.MODULE_ID + " ").getRecords(conn);

        if (data != null && data.length > 0) {
            for (Object[] obj : data) {
                adminIds.add(MySQLQuery.getAsInteger(obj[0]));
            }
        }
        return adminIds;

    }

}
