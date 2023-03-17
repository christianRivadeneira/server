package web.gates.cylTrip;

import javax.ejb.Stateless;
import utilities.MySQLQuery;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import web.gates.CheckCylsTrip;
import web.gates.GtTripLog;
import web.gates.GtTripType;

@Stateless
public class GtTripManager {

    public static int createCylTrip(GtCylTrip obj, String notes, boolean fromMobile, Connection conn) throws Exception {
        obj.id = obj.insert(obj, conn);

        Object[] row = new MySQLQuery("SELECT inv_mv_type_id, inv_mv_type_entry FROM gt_trip_type WHERE id = " + obj.typeId).getRecord(conn);
        if (row != null && row.length > 0) {
            Integer invMvTypeId = MySQLQuery.getAsInteger(row[0]);
            Integer invMvTypeEntry = MySQLQuery.getAsInteger(row[1]);
            if (invMvTypeId != null) {
                createInvMovement(invMvTypeId, obj, conn);
            }
            if (invMvTypeEntry != null) {
                createInvMovementEntry(invMvTypeEntry, obj, conn);
            }
        }

        if (notes != null) {
            createTripLog(true, obj.id, obj.employeeId, notes, fromMobile, null, conn);
        }
        return obj.id;
    }

    public static void editCylTrip(GtCylTrip obj, String notes, boolean fromMobile, Connection conn) throws Exception {
        obj.update(obj, conn);
        createTripLog(false, obj.id, obj.employeeId, notes, fromMobile, null, conn);

        Integer invMvTypeId = new MySQLQuery("SELECT inv_mv_type_id FROM gt_trip_type WHERE id = " + obj.typeId).getAsInteger(conn);
        if (invMvTypeId != null) {
            editInvMovement(obj, notes, conn);
        }
    }

    public static void createCylTripLog(int tripId, int empId, String logType, String logNotes, String evType, Connection conn) throws Exception {
        new MySQLQuery("INSERT INTO gt_trip_log SET "
                + "`trip_id` = " + tripId + ", "
                + "`employee_id` = " + empId + ", "
                + "`log_date` = NOW(), "
                + "`type` = '" + logType + "', "
                + "`notes` = '" + logNotes + "', "
                + "cyl_inv_ev_type = " + (evType != null ? "'" + evType + "'" : "NULL")).executeInsert(conn);
    }

    public static void createCylTripInv(List<GtCylInv> inv, Connection conn) throws Exception {
        StringBuilder sb = new StringBuilder("INSERT INTO gt_cyl_inv (trip_id, capa_id, type_id, amount, state, type) VALUES ");
        for (int i = 0; i < inv.size(); i++) {
            GtCylInv item = inv.get(i);
            sb.append("(").append(item.tripId).append(",").append(item.capaId).append(",").append(item.typeId).append(",").append(item.amount).append(",'").append(item.state).append("','").append(item.type).append("'),");
        }
        sb.deleteCharAt(sb.length() - 1);
        new MySQLQuery(sb.toString()).executeInsert(conn);
    }

    private static void createInvMovement(int invMvTypeId, GtCylTrip obj, Connection conn) throws Exception {
        InvMovement mov = new InvMovement();
        mov.typeId = invMvTypeId;
        mov.cancel = false;

        Object[] vhInfo = getInvVhId(obj.vhId, obj.plate, conn);
        mov.vh = MySQLQuery.getAsInteger(vhInfo[0]);
        if (obj.factoryId != null) {
            mov.factoryId = obj.factoryId;
        } else if (obj.centerDestId != null) {
            mov.centerDesId = new MySQLQuery("SELECT sc.inv_center_id FROM gt_center gc INNER JOIN sys_center sc ON sc.gt_center_id = gc.id WHERE gc.id = " + obj.centerDestId).getAsInteger(conn);
        }
        mov.centerId = new MySQLQuery("SELECT sc.inv_center_id FROM gt_center gc INNER JOIN sys_center sc ON sc.gt_center_id = gc.id WHERE gc.id = " + obj.centerOrigId).getAsInteger(conn);
        mov.mvDate = obj.tripDate;
        mov.notes = "Creado desde porterías - Aut: " + obj.authDoc;
        mov.gtCylTripId = obj.id;
        mov.id = mov.insert(mov, conn);

        String logNotes = "Tipo: Nuevo Movimiento.";
        logNotes += "Vehículo: " + (obj.plate != null ? obj.plate : MySQLQuery.getAsString(vhInfo[1]));
        logNotes += mov.notes;
        new MySQLQuery(InvLog.getLogQuery(mov.id, InvLog.INV_MOV, logNotes, obj.employeeId, conn)).executeInsert(conn);
    }

    private static void createInvMovementEntry(int invMvTypeId, GtCylTrip obj, Connection conn) throws Exception {
        InvMovement mov = new InvMovement();
        mov.typeId = invMvTypeId;
        mov.cancel = false;

        Object[] vhInfo = getInvVhId(obj.vhId, obj.plate, conn);
        mov.vh = MySQLQuery.getAsInteger(vhInfo[0]);
        if (obj.factoryId != null) {
            mov.factoryId = obj.factoryId;
        } else if (obj.centerDestId != null) {
            mov.centerDesId = new MySQLQuery("SELECT sc.inv_center_id FROM gt_center gc INNER JOIN sys_center sc ON sc.gt_center_id = gc.id WHERE gc.id = " + obj.centerOrigId).getAsInteger(conn);
        }
        mov.centerId = new MySQLQuery("SELECT sc.inv_center_id FROM gt_center gc INNER JOIN sys_center sc ON sc.gt_center_id = gc.id WHERE gc.id = " + obj.centerDestId).getAsInteger(conn);
        mov.mvDate = obj.tripDate;
        mov.notes = "Creado desde porterías - Aut: " + obj.authDoc;
        mov.gtCylTripId = obj.id;
        mov.id = mov.insert(mov, conn);

        String logNotes = "Tipo: Nuevo Movimiento.";
        logNotes += "Vehículo: " + (obj.plate != null ? obj.plate : MySQLQuery.getAsString(vhInfo[1]));
        logNotes += mov.notes;
        new MySQLQuery(InvLog.getLogQuery(mov.id, InvLog.INV_MOV, logNotes, obj.employeeId, conn)).executeInsert(conn);
    }

    private static void editInvMovement(GtCylTrip trip, String notes, Connection ep) throws Exception {
        Object[][] data = new MySQLQuery("SELECT id FROM inv_movement WHERE gt_cyl_trip_id = " + trip.id).getRecords(ep);
        if (data != null && data.length > 0) {

            for (int i = 0; i < data.length; i++) {
                InvMovement invMov = new InvMovement().select(MySQLQuery.getAsInteger(data[i][0]), ep);

                Integer infVhId = MySQLQuery.getAsInteger(getInvVhId(trip.vhId, trip.plate, ep)[0]);
                if (!infVhId.equals(invMov.vh)) {
                    invMov.vh = infVhId;
                }

                if ((invMov.factoryId != null && trip.factoryId != null) && !invMov.factoryId.equals(trip.factoryId)) {
                    invMov.factoryId = trip.factoryId;
                }

                String boxes = new MySQLQuery("SELECT boxes FROM inv_mv_type WHERE id = " + invMov.typeId).getAsString(ep);
                if (boxes.equals("input")) {
                    if (!Objects.equals(invMov.centerDesId, trip.centerOrigId)) {
                        invMov.centerDesId = new MySQLQuery("SELECT sc.inv_center_id FROM gt_center gc INNER JOIN sys_center sc ON sc.gt_center_id = gc.id WHERE gc.id = " + trip.centerOrigId).getAsInteger(ep);
                    }
                    if (!Objects.equals(invMov.centerId, trip.centerDestId)) {
                        invMov.centerId = new MySQLQuery("SELECT sc.inv_center_id FROM gt_center gc INNER JOIN sys_center sc ON sc.gt_center_id = gc.id WHERE gc.id = " + trip.centerDestId).getAsInteger(ep);
                    }
                } else if (boxes.equals("output")) {
                    if (!Objects.equals(invMov.centerDesId, trip.centerDestId)) {
                        invMov.centerDesId = new MySQLQuery("SELECT sc.inv_center_id FROM gt_center gc INNER JOIN sys_center sc ON sc.gt_center_id = gc.id WHERE gc.id = " + trip.centerDestId).getAsInteger(ep);
                    }
                    if (!Objects.equals(invMov.centerId, trip.centerOrigId)) {
                        invMov.centerId = new MySQLQuery("SELECT sc.inv_center_id FROM gt_center gc INNER JOIN sys_center sc ON sc.gt_center_id = gc.id WHERE gc.id = " + trip.centerOrigId).getAsInteger(ep);
                    }
                }
                invMov.mvDate = trip.tripDate;
                invMov.gtCylTripId = trip.id;
                invMov.update(invMov, ep);

                String logNotes = "Tipo: Edición de Movimiento.\nAnteriores: ";
                logNotes += notes;
                new MySQLQuery(InvLog.getLogQuery(invMov.id, InvLog.INV_MOV, logNotes, trip.employeeId, ep)).executeInsert(ep);
            }
        }
    }

    private static Object[] getInvVhId(Integer gtVhId, String gtPlate, Connection conn) throws Exception {
        Integer invVhId;
        String plate = null;
        if (gtVhId != null) {
            invVhId = new MySQLQuery("SELECT id, placa FROM inv_car WHERE vehicle_id = " + gtVhId).getAsInteger(conn);
            plate = new MySQLQuery("SELECT plate FROM vehicle WHERE id = " + gtVhId).getAsString(conn);

            if (invVhId == null) {
                invVhId = new MySQLQuery("SELECT id FROM inv_car WHERE placa LIKE ('" + plate.replace(" ", "%") + "') LIMIT 1").getAsInteger(conn);
            }
            if (invVhId == null) {
                invVhId = new MySQLQuery("INSERT INTO inv_car SET active = 1, vehicle_id = " + gtVhId).executeInsert(conn);
            }
        } else if (gtPlate != null) {
            invVhId = new MySQLQuery("SELECT ic.id FROM inv_car ic INNER JOIN vehicle v ON ic.vehicle_id = v.id WHERE v.plate LIKE ('" + gtPlate.replace(" ", "%") + "')").getAsInteger(conn);
            if (invVhId == null) {
                invVhId = new MySQLQuery("SELECT ic.id FROM inv_car ic WHERE ic.placa LIKE ('" + gtPlate.replace(" ", "%") + "')").getAsInteger(conn);
            }
            if (invVhId == null) {
                invVhId = new MySQLQuery("INSERT INTO inv_car SET placa = '" + gtPlate + "', active = 1, vehicle_id = NULL").executeInsert(conn);
            }
        } else {
            return null;
        }

        return new Object[]{invVhId, plate};
    }

    public static void createTripLog(boolean isNew, int tripId, int empId, String notes, boolean fromMobile, String evType, Connection conn) throws Exception {
        GtTripLog log = new GtTripLog();
        log.type = (isNew ? "new" : "edit");
        log.notes = notes;
        log.tripId = tripId;
        if (evType != null) {
            log.cylInvEvType = evType;
        }
        GtTripLog insert = new GtTripLog();
        if (log.notes != null) {
            insert.insert(log, conn, empId, fromMobile);
        }
    }

    public static boolean fillTripInventory(int tripId, int empId, String evType, Date evDate, List<CylsAmount> newInvCyl, String notes, boolean fromMobile, Connection ep) throws Exception {
        GtTripType tripType = new GtTripType().select(new MySQLQuery("SELECT type_id FROM gt_cyl_trip WHERE id = " + tripId).getAsInteger(ep), ep);

        Object[][] origInvCylData = new MySQLQuery("SELECT amount, capa_id, state, type_id FROM gt_cyl_inv "
                + "WHERE trip_id = " + tripId + " AND type = '" + evType + "'").getRecords(ep);

        List<CylsAmount> origInvCyl = CylsAmount.getListCylsAmount(origInvCylData);

        if (evType.equals("c") && !origInvCyl.isEmpty()) {
            boolean hasDeparture = new MySQLQuery("SELECT sdt IS NOT NULL FROM gt_cyl_trip WHERE id = " + tripId).getAsBoolean(ep);
            if (hasDeparture) {
                throw new Exception("No es posible modificar el inventario. Ya salió por porteria");
            }

            Object[][] dataLoad = new MySQLQuery(" SELECT COUNT(c.id), c.cyl_type_id, '', '-1', t.name "
                    + " FROM trk_cyl_load l "
                    + " INNER JOIN trk_cyl c ON c.id = l.cyl_id "
                    + " INNER JOIN cylinder_type t ON t.id = c.cyl_type_id "
                    + " WHERE l.cyl_trip_id = " + tripId + " AND l.date_del IS NULL "
                    + " GROUP BY c.cyl_type_id ").getRecords(ep);

            List<CylsAmount> lstLoad = CylsAmount.getListCylsAmount(dataLoad);

            if (dataLoad != null && !lstLoad.isEmpty()) { //****** HUBO UN CAMBIO *****

                Map<String, CylsAmount> map = new HashMap<>();
                for (CylsAmount obj : origInvCyl) {
                    String code = "c" + obj.capa + "t" + obj.typeId;
                    obj.operation = CylsAmount.OPER_DELETE;
                    map.put(code, obj);
                }
                for (CylsAmount obj : newInvCyl) {
                    String code = "c" + obj.capa + "t" + obj.typeId;
                    CylsAmount item = map.get(code);
                    if (item != null) {
                        if (item.amount == obj.amount) {
                            item.operation = CylsAmount.OPER_NONE;
                        } else {
                            item.amount = obj.amount;
                            item.operation = (item.amount == 0 ? CylsAmount.OPER_DELETE : CylsAmount.OPER_UPDATE);
                        }
                        map.put(code, item);
                    } else {
                        obj.operation = CylsAmount.OPER_CREATE;
                        map.put(code, obj);
                    }
                }

                Integer brandId = new MySQLQuery("SELECT id FROM inv_cyl_type WHERE brand = 1").getAsInteger(ep);

                for (Map.Entry<String, CylsAmount> entry : map.entrySet()) {
                    CylsAmount obj = entry.getValue();

                    if (obj.typeId == brandId) {
                        CylsAmount loadItems = CylsAmount.getByCapa(lstLoad, obj.capa);
                        if (obj.operation != CylsAmount.OPER_DELETE && (loadItems == null || loadItems.amount <= obj.amount)) {//no tenia ó es menor
                            makeCylOperation(tripId, empId, notes, fromMobile, evType, obj, ep);
                        } else {
                            throw new Exception("No es posible disminuir el inventario.\nYa existe " + loadItems.amount + " cilindros escaneados de " + loadItems.name + "");
                        }
                    } else {
                        makeCylOperation(tripId, empId, notes, fromMobile, evType, obj, ep);
                    }
                }
                fillInvMvInventory(tripId, ep, newInvCyl);
                return true;
            }
        }

        new MySQLQuery("DELETE FROM gt_cyl_inv WHERE trip_id = " + tripId + " AND type = '" + evType + "'").executeDelete(ep);
        if (newInvCyl != null && newInvCyl.size() > 0) {
            StringBuilder sb = new StringBuilder("INSERT INTO gt_cyl_inv (trip_id, capa_id, type_id, amount, state, type) VALUES ");

            for (int i = 0; i < newInvCyl.size(); i++) {
                CylsAmount it = newInvCyl.get(i);
                sb.append("(").append(tripId).append(",").append(it.capa).append(",").append(it.typeId).append(",").append(it.amount).append(",'").append(it.state).append("','").append(evType).append("'),");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(";");
            new MySQLQuery(sb.toString()).executeInsert(ep);
        }

        boolean closeFrm = true;
        if (GtTripType.getNeedsVal(tripType, evType)) {
            if (new CheckCylsTrip(evType, tripId, tripType, fromMobile, ep).passed) {
                new MySQLQuery("UPDATE gt_cyl_trip SET  " + evType + "dt = ?1, blocked = FALSE WHERE id = " + tripId).setParam(1, evDate).executeUpdate(ep);
            } else {
                closeFrm = false;
            }
        } else {
            new MySQLQuery("UPDATE gt_cyl_trip SET  " + evType + "dt = ?1 WHERE id = " + tripId).setParam(1, evDate).executeUpdate(ep);
        }
        //los cdt, sdt, edt y cdt son las fechas y horas de verificación, 
        //al editar una etapa se pierde la verificación 
        //de las siguientes
        if (evType.equals("c")) {
            if (tripType.s || tripType.e || tripType.d) {
                new MySQLQuery("UPDATE gt_cyl_trip SET " + (tripType.s ? "sdt = null " : " ") + (tripType.e ? " ,edt = null " : " ") + (tripType.d ? " ,ddt = null " : " ") + " WHERE id = " + tripId).executeUpdate(ep);
            }
        } else if (evType.equals("s")) {
            if (!fromMobile) {
                new MySQLQuery("UPDATE gt_cyl_trip SET has_ssign = 1 WHERE id = " + tripId).executeUpdate(ep);
            }
            if (tripType.e || tripType.d) {
                new MySQLQuery("UPDATE gt_cyl_trip SET edt = null ,ddt = null  WHERE id = " + tripId).executeUpdate(ep);
            }
        } else if (evType.equals("e")) {
            new MySQLQuery("UPDATE gt_cyl_trip "
                    + "SET " + (tripType.d ? "ddt = null " : " ") + " "
                    + (!fromMobile ? " ,has_esign = 1 " : " ") + " "
                    + "WHERE id = " + tripId).executeUpdate(ep);
        }

        if (evType.equals("c") || (evType.equals("e") && tripType.steps == 2)) {
            fillInvMvInventory(tripId, ep, newInvCyl);
        }

        new MySQLQuery("UPDATE gt_cyl_trip SET steps = IF(cdt IS NOT NULL,1,0)+IF(sdt IS NOT NULL,1,0)+IF(edt IS NOT NULL,1,0)+IF(ddt IS NOT NULL,1,0)  WHERE id=" + tripId).executeUpdate(ep);
        createTripLog(false, tripId, empId, notes, fromMobile, evType, ep);
        return closeFrm;
    }

    private static void fillInvMvInventory(int tripId, Connection conn, List<CylsAmount> lstAmount) throws Exception {
        Object[][] movData = new MySQLQuery("SELECT id, type_id FROM inv_movement WHERE gt_cyl_trip_id = " + tripId).getRecords(conn);
        if (movData != null && movData.length > 0) {
            for (int i = 0; i < movData.length; i++) {
                Object[] movInfo = movData[i];

                Integer movId = MySQLQuery.getAsInteger(movInfo[0]);
                String boxes = new MySQLQuery("SELECT boxes FROM inv_mv_type WHERE id = " + MySQLQuery.getAsInteger(movInfo[1])).getAsString(conn);

                List<CylsAmount> resLstAmount = new ArrayList<>();
                getAmount(lstAmount, resLstAmount);
                new MySQLQuery("DELETE FROM inv_mov_amount WHERE mov_id = " + movId).executeDelete(conn);
                for (int j = 0; j < resLstAmount.size(); j++) {
                    int sign = 1;
                    if (boxes.equals("output")) {
                        sign = -1;
                    }
                    new MySQLQuery("INSERT "
                            + "INTO inv_mov_amount SET "
                            + "capa_id=" + resLstAmount.get(j).capa + ","
                            + "type_id=" + resLstAmount.get(j).typeId + ","
                            + "mov_id=" + movId + ","
                            + "amount=" + (resLstAmount.get(j).amount * sign)).executeInsert(conn);
                }
            }
        }
    }

    private static void getAmount(List<CylsAmount> origLstAmount, List<CylsAmount> resLstAmount) {
        for (CylsAmount item : origLstAmount) {
            boolean exist = false;
            for (int j = 0; j < resLstAmount.size(); j++) {
                if (item.capa == resLstAmount.get(j).capa && item.typeId == resLstAmount.get(j).typeId) {
                    exist = true;
                    resLstAmount.get(j).amount = resLstAmount.get(j).amount + item.amount;
                    break;
                }
            }

            if (!exist) {
                CylsAmount ca = new CylsAmount();
                ca.capa = item.capa;
                ca.typeId = item.typeId;
                ca.amount = item.amount;
                ca.typeId = item.typeId;
                resLstAmount.add(ca);
            }
        }
    }

    private static void makeCylOperation(int tripId, int empId, String notes, boolean fromMobile, String evType, CylsAmount obj, Connection ep) throws Exception {
        switch (obj.operation) {
            case CylsAmount.OPER_CREATE:
                StringBuilder sb = new StringBuilder("INSERT INTO gt_cyl_inv (trip_id, capa_id, type_id, amount, state, type) VALUES ");
                sb.append("(").append(tripId).append(",").append(obj.capa).append(",").append(obj.typeId).append(",").append(obj.amount).append(",'").append(obj.state).append("','").append(evType).append("');");
                new MySQLQuery(sb.toString()).executeInsert(ep);
                break;
            case CylsAmount.OPER_DELETE:
                new MySQLQuery("DELETE FROM gt_cyl_inv "
                        + "WHERE trip_id = ?1 AND type = ?2 AND capa_id = ?3 AND type_id = ?4 "
                ).setParam(1, tripId).setParam(2, evType).setParam(3, obj.capa).setParam(4, obj.typeId).executeDelete(ep);
                break;
            case CylsAmount.OPER_UPDATE:
                new MySQLQuery("UPDATE gt_cyl_inv "
                        + "SET amount = " + obj.amount + " "
                        + "WHERE trip_id = ?1 AND type = ?2 AND capa_id = ?3 AND type_id = ?4 "
                ).setParam(1, tripId).setParam(2, evType).setParam(3, obj.capa).setParam(4, obj.typeId).executeUpdate(ep);
                break;
        }

        if (obj.operation != CylsAmount.OPER_NONE) {
            createTripLog(false, tripId, empId, notes, fromMobile, evType, ep);
        }

    }
}
