package web.gates;

import java.sql.Connection;
import utilities.MySQLQuery;

public class GtTripLog {
//inicio zona de reemplazo

    public Integer tripId;
    public String type;
    public String notes;
    public String cylInvEvType;

    public GtTripLog select(int id, Connection ep) throws Exception {
        return null;
    }
//fin zona de reemplazo

    public int insert(GtTripLog obj, Connection ep, int employeeId, boolean fromMobile) throws Exception {
        MySQLQuery q = new MySQLQuery("INSERT INTO gt_trip_log SET "
                + "`trip_id` = ?1, "
                + "`employee_id` = " + employeeId + ", "
                + "`log_date` = NOW(), "
                + "`type` = ?2, "
                + "`notes` = '" + "Dispositivo: "+ (fromMobile ? "Móvil" : "PC") +  " \\n" + obj.notes + "',"
                + "`cyl_inv_ev_type` = ?3 ");
        q.setParam(1, obj.tripId);
        q.setParam(2, obj.type);
        q.setParam(3, obj.cylInvEvType);
        return q.executeInsert(ep);
    }

    public String getInsertQuery(GtTripLog obj, Connection ep, int employeeId, boolean fromMobile) throws Exception {
        MySQLQuery q = new MySQLQuery("INSERT INTO gt_trip_log SET "
                + "`trip_id` = ?1, "
                + "`employee_id` = " + employeeId + ", "
                + "`log_date` = NOW(), "
                + "`type` = ?2, "
                + "`notes` = '" + "Dispositivo:"+ (fromMobile ? "Móvil" : "PC") +  " \\n" + obj.notes + "' ,"
                + "`cyl_inv_ev_type` = ?3 ");
        q.setParam(1, obj.tripId);
        q.setParam(2, obj.type);
        q.setParam(3, obj.cylInvEvType);
        return q.getQuery();
    }

    public String getEnumOptions(String fieldName) {
        if (fieldName.equals("type")) {
            return "edit=Edición&new=Nuevo";
        }
        return null;
    }
}
