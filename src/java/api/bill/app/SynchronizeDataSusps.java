package api.bill.app;

import api.bill.model.BillInstance;
import api.bill.model.dto.BillSuspRequest;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import api.sys.model.SysCfg;
import utilities.MySQLQuery;

public class SynchronizeDataSusps {

    public static List<BillSuspRequest> syncSuspDoneV1(List<BillSuspRequest> dataSusp, BillInstance billInstance, SysCfg sysCfg, Connection conn) throws Exception {
        for (BillSuspRequest obj : dataSusp) {
            MySQLQuery mq = new MySQLQuery("UPDATE bill_susp SET "
                    + "field_notes = ?1, "
                    + "reading = ?2, "
                    + "susp_type = ?3, "
                    + "susp_notes = ?4, "
                    + "sync_date = NOW() "
                    + "WHERE id = ?5");
            mq.setParam(1, obj.fieldNotes);
            mq.setParam(2, obj.reading);
            mq.setParam(3, obj.suspType);
            mq.setParam(4, obj.suspNotes);
            mq.setParam(5, obj.id);
            mq.executeUpdate(conn);
        }

        //========traer nuevas susp done ========
        Object[][] data = new MySQLQuery("SELECT b.id,"
                + " TRIM(CONCAT(c.first_name, ' ', IFNULL(c.last_name, ''))),"//1
                + " concat(" + (sysCfg.showApartment ? "c.apartment" : "c.num_install") + ", IFNULL(CONCAT(' (',(SELECT `number` FROM bill_meter WHERE client_id = c.id ORDER BY start_span_id DESC LIMIT 1),')'), '') ),"//2
                + " bi.name,"//3
                + " b.susp_order_date,"//4
                + " b.susp_date,"//5
                + " b.susp_tec_id," //6                       
                + " field_notes, "//7
                + " reading, "//8
                + " susp_type, "//9
                + " susp_notes, "//10
                + " c.neigh_id "//11                             
                + " FROM bill_susp b "
                + "INNER JOIN bill_client_tank c ON c.id = b.client_id "
                + " LEFT JOIN bill_building bi ON bi.id = c.building_id "
                + " WHERE b.recon_order_date IS NULL AND b.recon_date IS NULL"
                + " AND b.cancelled=0 AND b.susp_date IS NOT NULL ORDER BY b.susp_order_date DESC").getRecords(conn);

        List<BillSuspRequest> rta = new ArrayList<>();
        for (Object[] row : data) {
            BillSuspRequest obj = new BillSuspRequest();
            obj.id = MySQLQuery.getAsInteger(row[0]);

            obj.clientName = MySQLQuery.getAsString(row[1]);
            obj.clientSub = MySQLQuery.getAsString(row[2]);
            obj.building = MySQLQuery.getAsString(row[3]);
            obj.suspOrderDate = MySQLQuery.getAsDate(row[4]);
            obj.suspDate = MySQLQuery.getAsDate(row[5]);
            obj.suspTecId = MySQLQuery.getAsInteger(row[6]);
            obj.fieldNotes = MySQLQuery.getAsString(row[7]);
            obj.reading = MySQLQuery.getAsBigDecimal(row[8], false);
            obj.suspType = MySQLQuery.getAsString(row[9]);
            obj.suspNotes = MySQLQuery.getAsString(row[10]);
            obj.neighId = MySQLQuery.getAsInteger(row[11]);
            rta.add(obj);
        }
        return rta;
    }

    public static List<BillSuspRequest> syncSuspReconV1(List<BillSuspRequest> dataSusp, BillInstance billInstance, SysCfg sysCfg, Connection conn) throws Exception {

        for (BillSuspRequest obj : dataSusp) {
            Object[] row = new MySQLQuery("SELECT susp_date, recon_date, cancelled FROM bill_susp WHERE id = ?1")
                    .setParam(1, obj.id).getRecord(conn);
            Date origSuspDate = MySQLQuery.getAsDate(row[0]);
            Date origReconDate = MySQLQuery.getAsDate(row[1]);
            boolean cancelled = MySQLQuery.getAsBoolean(row[2]);

            Date newSuspDate = obj.suspDate;
            Date newReconDate = obj.reconDate;
            Integer suspId = obj.id;

            if (cancelled && (!Objects.equals(origSuspDate, newSuspDate) || !Objects.equals(origReconDate, newReconDate))) {
                cancelled = false;
            }

            MySQLQuery mq = new MySQLQuery("UPDATE bill_susp SET "
                    + "susp_date = ?1, "
                    + "susp_tec_id = ?2, "
                    + "recon_date = ?3, "
                    + "recon_tec_id = ?4, "
                    + "cancelled = ?5, "
                    + "field_notes = ?6, "
                    + "reading = ?7, "
                    + "susp_type = ?8, "
                    + "susp_notes = ?9, "
                    + "sync_date = NOW() "
                    + "WHERE id = ?10");
            mq.setParam(1, newSuspDate);
            mq.setParam(2, obj.suspTecId);
            mq.setParam(3, newReconDate);
            mq.setParam(4, obj.reconTecId);
            mq.setParam(5, cancelled);
            mq.setParam(6, obj.fieldNotes);
            mq.setParam(7, MySQLQuery.getAsBigDecimal(obj.reading, false));
            mq.setParam(8, obj.suspType);
            mq.setParam(9, obj.suspNotes);
            mq.setParam(10, suspId);

            mq.executeUpdate(conn);

            if (!cancelled && newReconDate == null && newSuspDate != null) {
                new MySQLQuery("UPDATE bill_client_tank SET discon = 1 "
                        + "WHERE id = (SELECT client_id FROM bill_susp WHERE id = " + suspId + ")").executeUpdate(conn);
            } else if (!cancelled && newReconDate != null && newSuspDate != null) {
                new MySQLQuery("UPDATE bill_client_tank SET discon = 0 "
                        + "WHERE id = (SELECT client_id FROM bill_susp WHERE id = " + suspId + ")").executeUpdate(conn);
            }

        }
        //========traer nuevas ordenes de suspensión y reconexión========
        Object[][] data = new MySQLQuery("SELECT "
                + "s.id, "//0
                + "trim(concat(c.first_name, ' ', ifnull(c.last_name, ''))), "//1
                + "concat(" + (sysCfg.showApartment ? "c.apartment" : "c.num_install") + ", IFNULL(CONCAT(' (',(SELECT `number` FROM bill_meter WHERE client_id = c.id ORDER BY start_span_id DESC LIMIT 1),')'), '')), "//2
                + "b.name, "//3
                + "susp_order_date,"//4
                + "susp_date,"//5
                + "susp_tec_id,"//6
                + "recon_order_date,"//7
                + "recon_date,"//8
                + "recon_tec_id, "//8
                + "field_notes, "//10
                + "reading, "//11
                + "susp_type, "//12
                + "b.id, "//13                                   
                + "cc.lat, "//14   
                + "cc.lon, "//15                                                                  
                + "b.address, "//16
                + "s.susp_notes, "//17
                + "c.neigh_id "//18                             
                + "FROM bill_susp s "
                + "INNER JOIN bill_client_tank c ON c.id = s.client_id "
                + "LEFT JOIN bill_building b ON b.id = c.building_id "
                + "LEFT JOIN sigma.ord_tank_client cc ON cc.mirror_id=b.id AND cc.`type`='build' "
                + "WHERE cancelled = 0 AND "
                + "((susp_order_date IS NOT NULL AND susp_date IS NULL) OR (recon_order_date IS NOT NULL AND recon_date IS NULL)"
                + " OR (recon_order_date IS NULL AND recon_date IS NULL AND susp_date IS NOT NULL)) GROUP BY s.id").getRecords(conn);

        List<BillSuspRequest> rta = new ArrayList<>();
        for (Object[] row : data) {
            BillSuspRequest obj = new BillSuspRequest();
            obj.id = MySQLQuery.getAsInteger(row[0]);

            obj.clientName = MySQLQuery.getAsString(row[1]);
            obj.clientSub = MySQLQuery.getAsString(row[2]);
            obj.building = MySQLQuery.getAsString(row[3]);
            obj.suspOrderDate = MySQLQuery.getAsDate(row[4]);
            obj.suspDate = MySQLQuery.getAsDate(row[5]);
            obj.suspTecId = MySQLQuery.getAsInteger(row[6]);

            obj.reconOrderDate = MySQLQuery.getAsDate(row[7]);
            obj.reconDate = MySQLQuery.getAsDate(row[8]);
            obj.reconTecId = MySQLQuery.getAsInteger(row[9]);
            obj.fieldNotes = MySQLQuery.getAsString(row[10]);
            obj.reading = MySQLQuery.getAsBigDecimal(row[11], false);
            obj.suspType = MySQLQuery.getAsString(row[12]);

            obj.buildingId = MySQLQuery.getAsInteger(row[13]);

            obj.lat = MySQLQuery.getAsBigDecimal(row[14], false);
            obj.lon = MySQLQuery.getAsBigDecimal(row[15], false);

            obj.address = MySQLQuery.getAsString(row[16]);
            obj.suspNotes = MySQLQuery.getAsString(row[17]);
            obj.neighId = MySQLQuery.getAsInteger(row[18]);

            rta.add(obj);
        }

        return rta;

    }

}
