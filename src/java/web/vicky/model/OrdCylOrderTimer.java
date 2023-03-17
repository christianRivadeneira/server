package web.vicky.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import javax.ejb.TimerHandle;
import utilities.MySQLQuery;

public class OrdCylOrderTimer {
//inicio zona de reemplazo

    public int id;
    public int orderId;
    private byte[] handle;
    public int round;
    public double lastRadius;

    private static final String SEL_FLDS = "`order_id`, "
            + "`handle`, "
            + "`round`, "
            + "`last_radius`";

    private static final String SET_FLDS = "ord_cyl_order_timer SET "
            + "`order_id` = ?1, "
            + "`handle` = ?2, "
            + "`round` = ?3, "
            + "`last_radius` = ?4";

    private static void setFields(OrdCylOrderTimer obj, MySQLQuery q) {
        q.setParam(1, obj.orderId);
        q.setParam(2, obj.handle);
        q.setParam(3, obj.round);
        q.setParam(4, obj.lastRadius);

    }

    public static OrdCylOrderTimer getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        OrdCylOrderTimer obj = new OrdCylOrderTimer();
        obj.orderId = MySQLQuery.getAsInteger(row[0]);
        obj.handle = (row[1] != null ? (byte[]) row[1] : null);
        obj.round = MySQLQuery.getAsInteger(row[2]);
        obj.lastRadius = MySQLQuery.getAsDouble(row[3]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }
//fin zona de reemplazo

    public static OrdCylOrderTimer[] getAllHandles(Connection ep) throws Exception {
        Object[][] data = new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM ord_cyl_order_timer WHERE handle IS NOT NULL").getRecords(ep);
        OrdCylOrderTimer[] rta = new OrdCylOrderTimer[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = getFromRow(data[i]);
        }
        return rta;
    }

    public static OrdCylOrderTimer[] getAllHandles(int orderId, Connection ep) throws Exception {
        Object[][] data = new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM ord_cyl_order_timer WHERE handle IS NOT NULL AND order_id = ?1").setParam(1, orderId).getRecords(ep);
        OrdCylOrderTimer[] rta = new OrdCylOrderTimer[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = getFromRow(data[i]);
        }
        return rta;
    }

    public static OrdCylOrderTimer select(int id, Connection ep) throws Exception {
        return OrdCylOrderTimer.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(ep));
    }

    public static OrdCylOrderTimer getActiveByOrderId(int orderId, Connection ep) throws Exception {
        //El limit se pone para que deje de reportar query returns more than one row, pero se desconoce del todo su efecto y no se estudia 
        return OrdCylOrderTimer.getFromRow(new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM ord_cyl_order_timer WHERE handle IS NOT NULL AND order_id = " + orderId+" ORDER BY round DESC LIMIT 1").getRecord(ep));
    }

    public static int insert(OrdCylOrderTimer pobj, Connection ep) throws Exception {
        OrdCylOrderTimer[] handles = getAllHandles(pobj.orderId, ep);
        for (OrdCylOrderTimer handle : handles) {
            handle.cancelTimer(ep);
        }
        OrdCylOrderTimer obj = (OrdCylOrderTimer) pobj;
        int nId = new MySQLQuery(OrdCylOrderTimer.getInsertQuery(obj)).executeInsert(ep);
        obj.id = nId;
        return nId;
    }

    public static void update(OrdCylOrderTimer pobj, Connection ep) throws Exception {
        new MySQLQuery(OrdCylOrderTimer.getUpdateQuery((OrdCylOrderTimer) pobj)).executeUpdate(ep);
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM ord_cyl_order_timer WHERE id = " + id;
    }

    public static String getInsertQuery(OrdCylOrderTimer obj) {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public static String getUpdateQuery(OrdCylOrderTimer obj) {
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public static void delete(int id, Connection ep) throws Exception {
        new MySQLQuery("DELETE FROM ord_cyl_order_timer WHERE id = " + id).executeDelete(ep);
    }

    public void setHandle(TimerHandle h) throws IOException {
        if (h == null) {
            handle = null;
            return;
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(h);
            oos.close();
            baos.close();
            handle = baos.toByteArray();
        }
    }

    public TimerHandle getHandle() throws Exception {
        if (handle != null) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(handle); ObjectInputStream oos = new ObjectInputStream(bais)) {
                TimerHandle h = (TimerHandle) oos.readObject();
                oos.close();
                bais.close();
                return h;
            }
        } else {
            throw new Exception("El handle está vacío");
        }
    }

    public void cancelTimer(Connection conn) throws Exception {
        if (handle != null) {
            TimerHandle h = getHandle();
            try {
                h.getTimer().cancel();
            } catch (javax.ejb.NoSuchObjectLocalException nte) {
            }
            handle = null;
            OrdCylOrderTimer.update(this, conn);
        }
    }
}
