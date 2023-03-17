package api.est.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;
import java.util.GregorianCalendar;

public class EstProg extends BaseModel<EstProg> {
//inicio zona de reemplazo

    public Date progDate;
    public Date endDate;
    public Integer vhId;
    public Integer pathId;
    public Integer emploId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "prog_date",
            "end_date",
            "vh_id",
            "path_id",
            "emplo_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, progDate);
        q.setParam(2, endDate);
        q.setParam(3, vhId);
        q.setParam(4, pathId);
        q.setParam(5, emploId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        progDate = MySQLQuery.getAsDate(row[0]);
        endDate = MySQLQuery.getAsDate(row[1]);
        vhId = MySQLQuery.getAsInteger(row[2]);
        pathId = MySQLQuery.getAsInteger(row[3]);
        emploId = MySQLQuery.getAsInteger(row[4]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "est_prog";
    }

    public static String getSelFlds(String alias) {
        return new EstProg().getSelFldsForAlias(alias);
    }

    public static List<EstProg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EstProg().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EstProg().deleteById(id, conn);
    }

    public static List<EstProg> getAll(Connection conn) throws Exception {
        return new EstProg().getAllList(conn);
    }

//fin zona de reemplazo
    
    public static String getWeekday(Date date) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        int d = gc.get(GregorianCalendar.DAY_OF_WEEK);
        switch (d) {
            case GregorianCalendar.MONDAY:
                return "l";
            case GregorianCalendar.TUESDAY:
                return "m";
            case GregorianCalendar.WEDNESDAY:
                return "x";
            case GregorianCalendar.THURSDAY:
                return "j";
            case GregorianCalendar.FRIDAY:
                return "v";
            case GregorianCalendar.SATURDAY:
                return "s";
            case GregorianCalendar.SUNDAY:
                return "d";
            default:
                break;
        }
        throw new RuntimeException("Día inesperado: " + d);
    }
    
    public static void validateSchedule(int clientId, int week, String day, String date, Connection ep) throws Exception {
        String vh = new MySQLQuery("SELECT "
                + "CONCAT(v.plate, ' - ', v.internal, ' ', COALESCE(CONCAT(e.first_name, ' ', e.last_name), '')) "
                + "FROM est_sede_frec_path fp "
                + "INNER JOIN est_path_route pr ON fp.path_route_id = pr.id "
                + "INNER JOIN est_prog ep ON ep.path_id = pr.id "
                + "INNER JOIN vehicle v ON ep.vh_id = v.id "
                + "LEFT JOIN driver_vehicle dv ON dv.vehicle_id = v.id AND dv.`end` IS NULL "
                + "LEFT JOIN employee e ON dv.driver_id = e.id "
                + "WHERE fp.week = " + week + " "
                + "AND fp.clie_tank_id = " + clientId + " "
                + "AND fp.day = '" + day + "' "
                + "AND ep.prog_date = '" + date + "' "
                + ""
                + "UNION "
                + ""
                + "SELECT "
                + "COALESCE(CONCAT(v.plate, ' - ', v.internal, ' ', COALESCE(CONCAT(e.first_name, ' ', e.last_name), '')), 'Ya está programado') "
                + "FROM est_prog ep "
                + "INNER JOIN est_prog_sede s ON s.prog_id = ep.id "
                + "LEFT JOIN vehicle v ON ep.vh_id = v.id "
                + "LEFT JOIN driver_vehicle dv ON dv.vehicle_id = v.id AND dv.`end` IS NULL "
                + "LEFT JOIN employee e ON dv.driver_id = e.id "
                + "WHERE "
                + "ep.prog_date = '" + date + "' "
                + "AND s.tank_client_id = " + clientId + " "
                + ""
                + "UNION "
                + ""
                + "SELECT COALESCE(CONCAT(v.plate, ' - ', v.internal, ' ', COALESCE(CONCAT(e.first_name, ' ', e.last_name), '')), 'Ya está programado') "
                + "FROM est_schedule srem "
                + "INNER JOIN vehicle v ON v.id = srem.vh_id "
                + "LEFT JOIN driver_vehicle dv ON dv.vehicle_id = v.id AND dv.`end` IS NULL "
                + "LEFT JOIN employee e ON dv.driver_id = e.id "
                + "WHERE srem.visit_date = '" + date + "' AND srem.clie_tank_id = " + clientId + " AND srem.type = 'rem' "
        ).getAsString(ep);

        if (vh != null) {
            throw new Exception("El cliente ya tiene una visita programada para la fecha.\nVendedor: " + vh);
        }
    }
}