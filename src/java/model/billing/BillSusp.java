package model.billing;

import api.bill.model.BillBill;
import java.sql.Connection;
import java.sql.SQLException;
import utilities.MySQLPreparedUpdate;

public class BillSusp {

    /**
     * Para detiener automáticamente una orden de corte por el pago de una fra.
     *
     * @param conn
     * @return
     * @throws SQLException
     */
    public static MySQLPreparedUpdate getStopSuspQ(Connection conn) throws SQLException {
        return new MySQLPreparedUpdate("UPDATE bill_susp s "
                + "SET s.cancelled = 1, "
                + "s.cancelled_by = 1, "
                + "s.cancel_notes = ?2, "
                + "s.pay_bill_id = ?3 "
                + "WHERE "
                + "s.client_id = ?1 "
                + "AND s.susp_order_date is not null "
                + "AND s.susp_date IS NULL "
                + "AND s.cancelled = 0", conn);
    }

    /**
     * Para programar automáticamente una reconexión por el pago de de una factura
     *
     * @param conn
     * @return
     * @throws SQLException
     */
    public static MySQLPreparedUpdate getProgReconQ(Connection conn) throws SQLException {
        return new MySQLPreparedUpdate("UPDATE bill_susp s "
                + "SET s.recon_order_date = now(), "
                + "s.recon_creator_id = 1, "
                + "s.cancel_notes = ?2, "
                + "s.pay_bill_id = ?3 "
                + "WHERE "
                + "s.client_id = ?1 "
                + "AND s.susp_date IS NOT NULL "
                + "AND s.recon_order_date IS NULL "
                + "AND s.recon_date IS NULL "
                + "AND s.cancelled = 0", conn);
    }

    /**
     * Coloca los datos de una factura pagada en un corte, para detener el corte
     * o programar una reconexión automáticamente con el pago de una factura
     *
     * @param q
     * @param bill
     * @throws Exception
     */
    public static void setParams(MySQLPreparedUpdate q, BillBill bill) throws Exception {
        q.setParameter(1, bill.clientTankId);
        q.setParameter(2, "Se registró pago de fra " + bill.billNum);
        q.setParameter(3, bill.id);
        q.addBatch();
    }

}
