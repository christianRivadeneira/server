package api.bill.writers.bill;

import java.math.BigDecimal;
import java.sql.Connection;
import utilities.MySQLPreparedQuery;
import utilities.cast;

public class DtoSrvToPrint {

    public String label;
    public BigDecimal feeCapital;
    public BigDecimal feeInterest;

    public BigDecimal pendCapital;
    public BigDecimal pendInterest;
    
    public BigDecimal creditInter;

    public int fee;
    public int totalFees;

    public static MySQLPreparedQuery getQuery(Connection conn) throws Exception {
        return new MySQLPreparedQuery("SELECT "
                + "t.name, "
                + "f.value - f.ext_pay, "
                + "f.inter - f.ext_inter + IFNULL(f.inter_tax, 0) - IFNULL(f.ext_inter_tax, 0), "
                + "(SELECT SUM(fi.value - fi.ext_pay) FROM bill_user_service_fee fi WHERE fi.service_id = f.service_id AND fi.place > f.place), "
                + "(SELECT SUM(fi.inter - fi.ext_inter + IFNULL(fi.inter_tax, 0) - IFNULL(fi.ext_inter_tax, 0)) FROM bill_user_service_fee fi WHERE fi.service_id = f.service_id AND fi.place > f.place), "
                + "f.place + 1, "
                + "(SELECT max(place) + 1 FROM bill_user_service_fee f1 WHERE f1.service_id = s.id),"
                + "credit_inter "
                + "FROM "
                + "bill_user_service s "
                + "INNER JOIN bill_service_type t ON t.id = s.type_id "
                + "INNER JOIN bill_user_service_fee f ON s.id = f.service_id "
                + "WHERE f.value - f.ext_pay > 0 AND s.bill_client_tank_id = ?1 AND s.bill_span_id + f.place = ?2", conn);

    }

    public static DtoSrvToPrint[] getData(int clientId, int spanId, MySQLPreparedQuery q) throws Exception {
        q.setParameter(1, clientId);
        q.setParameter(2, spanId);
        Object[][] data = q.getRecords();
        DtoSrvToPrint[] rta = new DtoSrvToPrint[data.length];
        for (int i = 0; i < rta.length; i++) {
            Object[] row = data[i];
            rta[i] = new DtoSrvToPrint();
            rta[i].label = cast.asString(row, 0);
            rta[i].feeCapital = cast.asBigDecimal(row, 1, true);
            rta[i].feeInterest = cast.asBigDecimal(row, 2, true);
            rta[i].pendCapital = cast.asBigDecimal(row, 3, true);
            rta[i].pendInterest = cast.asBigDecimal(row, 4, true);
            rta[i].fee = cast.asInt(row, 5);
            rta[i].totalFees = cast.asInt(row, 6);
            rta[i].creditInter = cast.asBigDecimal(row, 7);
        }
        return rta;
    }

}
