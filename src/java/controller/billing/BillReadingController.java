package controller.billing;

import api.bill.model.BillInstance;
import api.bill.model.dto.BillReadingsCheck;
import java.sql.Connection;
import utilities.MySQLQuery;

public class BillReadingController {
    

    public static BillReadingsCheck checkReadings(int spanId, BillInstance bi, Connection billConn, Connection sigmaConn) throws Exception {
        int tClients;
        if (bi.isTankInstance()) {
            tClients = new MySQLQuery("SELECT COUNT(*) "
                    + "FROM bill_building AS b "
                    + "INNER JOIN bill_client_tank AS c ON c.building_id = b.id AND c.active = 1 ").getAsInteger(billConn);
        } else {
            tClients = new MySQLQuery("SELECT COUNT(*) "
                    + "FROM bill_client_tank c WHERE c.active = 1 ").getAsInteger(billConn);
        }

        int tClientReads = new MySQLQuery("SELECT COUNT(*) FROM bill_reading AS r "
                + "INNER JOIN bill_client_tank AS c ON c.id = r.client_tank_id AND c.active = 1 "
                + "WHERE r.span_id = " + spanId).getAsInteger(billConn);

        MySQLQuery tanksQ = new MySQLQuery("SELECT COUNT(*) "
                + "FROM ord_tank_client AS c "
                + "INNER JOIN est_tank AS t ON t.client_id = c.id "
                + "WHERE c.mirror_id = ?1 AND c.bill_instance_id = " + bi.id + " ");
        MySQLQuery tankReadsQ = new MySQLQuery("SELECT COUNT(*) "
                + "FROM ord_tank_client AS c "
                + "INNER JOIN est_tank AS t ON t.client_id = c.id "
                + "INNER JOIN est_tank_read AS r ON r.tank_id = t.id AND r.bill_span_id = " + spanId + " "
                + "WHERE c.mirror_id = ?1 AND c.bill_instance_id = " + bi.id + " ");

        int tTanks = 0;
        int tTankReads = 0;

        if (bi.isTankInstance()) {
            Object[][] res = new MySQLQuery("SELECT b.id, COUNT(c.id) "
                    + "FROM bill_building AS b "
                    + "INNER JOIN bill_client_tank AS c ON c.building_id = b.id AND c.active = 1 "//0
                    + "GROUP BY b.id ORDER BY b.old_id ASC").getRecords(billConn);


            for (Object[] row : res) {
                tankReadsQ.setParam(1, row[0]);
                Integer tankReads = tankReadsQ.getAsInteger(sigmaConn);
                if (tankReads != null) {
                    tTankReads += tankReads;
                }

                tanksQ.setParam(1, row[0]);
                Integer tanks = tanksQ.getAsInteger(sigmaConn);
                if (tanks != null) {
                    tTanks += tanks;
                }
            }
        }

        return new BillReadingsCheck(tClients, tTanks, tClientReads, tTankReads);
    }
}
