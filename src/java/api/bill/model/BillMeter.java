package api.bill.model;

import api.BaseModel;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import utilities.MySQLPreparedQuery;
import utilities.MySQLQuery;

public class BillMeter extends BaseModel<BillMeter> {
//inicio zona de reemplazo

    public int clientId;
    public String number;
    public Date start;
    public int startSpanId;
    public BigDecimal startReading;
    public BigDecimal factor;
    public String notes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "client_id",
            "number",
            "start",
            "start_span_id",
            "start_reading",
            "factor",
            "notes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, clientId);
        q.setParam(2, number);
        q.setParam(3, start);
        q.setParam(4, startSpanId);
        q.setParam(5, startReading);
        q.setParam(6, factor);
        q.setParam(7, notes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        clientId = MySQLQuery.getAsInteger(row[0]);
        number = MySQLQuery.getAsString(row[1]);
        start = MySQLQuery.getAsDate(row[2]);
        startSpanId = MySQLQuery.getAsInteger(row[3]);
        startReading = MySQLQuery.getAsBigDecimal(row[4], false);
        factor = MySQLQuery.getAsBigDecimal(row[5], false);
        notes = MySQLQuery.getAsString(row[6]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_meter";
    }

    public static String getSelFlds(String alias) {
        return new BillMeter().getSelFldsForAlias(alias);
    }

    public static List<BillMeter> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillMeter().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillMeter().deleteById(id, conn);
    }

    public static List<BillMeter> getAll(Connection conn) throws Exception {
        return new BillMeter().getAllList(conn);
    }

//fin zona de reemplazo
    public static String METER_QUERY = "SELECT b.number, b.factor "
            + "FROM bill_meter AS b where b.start_span_id <= ?1 AND b.client_id = ?2 "
            + "ORDER BY b.start_span_id DESC LIMIT 1";

    public static MySQLPreparedQuery getMeterQuery(Connection conn) throws SQLException {
        return new MySQLPreparedQuery(METER_QUERY, conn);
    }

    public static BillMeter getMeter(int spanId, int clientId, MySQLPreparedQuery meterQ) throws Exception {
        meterQ.setParameter(1, spanId);
        meterQ.setParameter(2, clientId);
        Object[] row = meterQ.getRecord();
        if (row != null) {
            BillMeter m = new BillMeter();
            m.clientId = clientId;
            m.factor = MySQLQuery.getAsBigDecimal(row[1], true);
            m.number = MySQLQuery.getAsString(row[0]);
            return m;
        }
        return null;
    }

    /**
     * Query para migrar los medidores de client a meter
     * TRUNCATE bill_meter; INSERT INTO
     * bill_meter(client_id,NUMBER,START,start_span_id,start_reading,factor)(
     * SELECT c.id, c.num_meter, DATE((SELECT MIN(t.created) FROM
     * bill_transaction t WHERE t.cli_tank_id = c.id)), (SELECT
     * MIN(t.bill_span_id) FROM bill_transaction t WHERE t.cli_tank_id = c.id),
     * (SELECT MIN(reading) FROM bill_reading_bk r WHERE r.client_tank_id =
     * c.id), 1 FROM bill_client_tank c WHERE c.num_meter IS NOT NULL AND
     * (SELECT MIN(t.created) FROM bill_transaction t WHERE t.cli_tank_id =
     * c.id) IS NOT NULL);
     */
}
