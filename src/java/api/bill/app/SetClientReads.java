package api.bill.app;

import api.bill.model.BillInstance;
import api.bill.model.BillReading;
import api.bill.model.BillReadingFault;
import api.bill.model.BillSpan;
import api.ord.model.OrdPqrRequest;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import model.billing.BillAlertReadings;
import utilities.MySQLPreparedQuery;
import utilities.MySQLQuery;

public class SetClientReads {

    public static void SetClientReadsV1(BillInstance bi, int reqSpanId, String data, Integer empId, Connection instConn, Connection sigmaConn) throws Exception {
        Map<Integer, BillReadingFault> faults = BillReadingFault.getActiveAsMap(sigmaConn);

        BillInstance billInstance = bi;
        String[] lines = data.split("B");

        int spanId = new MySQLQuery("SELECT id FROM bill_span WHERE state = 'cons'").getAsInteger(instConn);
        if (spanId != reqSpanId) {
            throw new Exception("La lectura no corresponde al periodo actual.");
        }

        if (billInstance.isTankInstance()) {
            if (BillSpan.getByState("cons", instConn).readingsClosed) {
                throw new Exception("Ya se hizo el cierre de lecturas.");
            }
        }

        StringBuilder sbInsert = new StringBuilder();
        StringBuilder sbDelete = new StringBuilder();

        List<BillReading> lstReadings = new ArrayList<>();
        MySQLPreparedQuery existsQ = new MySQLPreparedQuery("SELECT id FROM bill_reading WHERE client_tank_id = ?1 AND span_id = ?2", instConn);

        for (String line : lines) {
            String[] parts = line.split("A");
            BillReading r = new BillReading();
            r.spanId = spanId;
            r.clientTankId = Integer.valueOf(parts[0]);
            r.lastReading = new BigDecimal(parts[1]);
            r.reading = new BigDecimal(parts[2]);
            r.lat = parts[3].equals("null") ? null : new BigDecimal(parts[3]);
            r.lon = parts[4].equals("null") ? null : new BigDecimal(parts[4]);
            r.inRadius = parts[5].equals("1");
            r.fromScan = parts[6].equals("1");
            r.faultId = parts[7].isEmpty() ? null : Integer.valueOf(parts[7]);
            r.empId = empId;
            if (parts[8].equals("1")) {                
                OrdPqrRequest.createCritical(r, bi, instConn, sigmaConn);
            }
            sbDelete.append(r.clientTankId).append(","); // delets

            existsQ.setParameter(1, r.clientTankId);
            existsQ.setParameter(2, r.spanId);
            Integer rInt = existsQ.getAsInteger();
            if (rInt != null) {
                r.id = rInt;
                r.update(instConn);
            } else {
                sbInsert.append("(").append(r.reading).append(", ").append(r.lastReading).append(", ").append(r.lat).
                        append(", ").append(r.lon).append(", ").append(r.inRadius ? "1" : "0").append(", ").
                        append(r.clientTankId).append(", ").append(spanId).append(",").append(r.fromScan ? "1" : "0")
                        .append(", ").append(r.faultId).append(", ").append(r.empId).append("),"); // inserts
            }
            lstReadings.add(r);
        }

        //String deletes = sbDelete.toString();
        if (sbInsert.length() > 0) {
            sbInsert.deleteCharAt(sbInsert.length() - 1);
            //deletes = deletes.substring(0, deletes.length() - 1);
            //new MySQLQuery("DELETE FROM bill_reading WHERE client_tank_id IN (" + deletes + ") AND span_id = " + spanId).executeDelete(instConn);
            new MySQLQuery("INSERT INTO bill_reading (reading, last_reading, lat, lon, in_radius, client_tank_id , span_id, from_scan, fault_id, emp_id) VALUES " + sbInsert.toString() + ";").executeInsert(instConn);
        }
        setAlerts(lstReadings, billInstance.id, faults, instConn);
    }

    private static void setAlerts(List<BillReading> lstReadings, int instanceId, Map<Integer, BillReadingFault> map, Connection instConn) throws Exception {
        long t = System.currentTimeMillis();
        List<BillAlertReadings> alerts = new ArrayList<>();

        for (int i = 0; i < lstReadings.size(); i++) {
            BillReading r = lstReadings.get(i);

            BillAlertReadings al = new BillAlertReadings();
            Object[] row = new MySQLQuery("SELECT "
                    + "(SELECT `number` FROM bill_meter WHERE client_id = c.id ORDER BY start_span_id DESC LIMIT 1), "
                    + "c.discon, "
                    + "c.building_id "
                    + "FROM bill_client_tank c "
                    + "WHERE c.id = " + r.clientTankId).getRecord(instConn);

            String numMeter = MySQLQuery.getAsString(row[0]);
            boolean noMeter = (numMeter == null || numMeter.isEmpty());
            boolean discon = MySQLQuery.getAsBoolean(row[1]);
            al.clientId = r.clientTankId;
            al.buildingId = MySQLQuery.getAsInteger(row[2]);
            al.cityId = null;
            al.billInstanceId = instanceId;
            al.spanId = r.spanId;

            boolean alert = false;

            al.discon = false;
            al.noMeter = false;
            // la primera parte nos dice del app la segunda del desktop 
            //+ "con_sm=Se halló lectura, debe estar sin medidor&"
            //+ "con_ss=Se halló lectura, debe estar suspendido&"
            //+ "ss_con=Se halló suspendido, debe tener lectura&"
            //+ "sm_con=No se halló medidor, debe tener lectura&"
            //+ "sm_ss=No se halló medidor, debe estar suspendido&"
            //+ "ss_sm=Se halló suspendido, debe estar sin medidor";   

            BillReadingFault f = r.faultId != null ? map.get(r.faultId) : null;

            boolean appDiscon = f != null && f.type.equals("discon");
            boolean appNoMeter = f != null && f.type.equals("no_meter");
            boolean appNormal = f == null || (r.faultId != null && f.type.equals("normal"));

            boolean sigmaNormal = (!discon && !noMeter);

            if (appDiscon && sigmaNormal) {//app:sin servicio - sigma:con servicio
                al.discon = true;
                al.motive = "ss_con";
                alert = true;
            } else if (appNoMeter && sigmaNormal) {//app:sin medidor - sigma:con medidor
                al.noMeter = true;
                al.motive = "sm_con";
                alert = true;
            } else if (appNormal && discon) {//app:con servicio - sigma:sin servicio
                al.discon = true;
                al.motive = "con_ss";
                alert = true;
            } else if (appNormal && noMeter) {//app:con medidor - sigma:sin medidor
                al.noMeter = true;
                al.motive = "con_sm";
                alert = true;
            } else if (appDiscon && noMeter) {//app:desconectado - sigma:con medidor
                al.discon = true;
                al.motive = "ss_sm";
                alert = true;
            } else if (appNoMeter && discon) {//app:sin medidor - sigma:desconectado
                al.noMeter = true;
                al.motive = "sm_ss";
                alert = true;
            }

            if (alert) {
                al.regDate = new Date();
                alerts.add(al);
            }
        }

        if (alerts.size() > 0) {
            new MySQLQuery(BillAlertReadings.getMultiInsertQuery(alerts)).print().executeInsert(instConn);
        }
        System.out.println("Creacion de Alertas lecturas: " + (System.currentTimeMillis() - t) + "ms");
    }

}
