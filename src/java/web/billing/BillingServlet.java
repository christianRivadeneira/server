package web.billing;

import api.bill.model.BillInstance;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import service.MySQL.MySQLCommon;

public class BillingServlet extends HttpServlet {

    private static final Map<Integer, BillInstance> INSTANCES = new HashMap<>();

    public static BillInstance getInst(int instId) throws Exception {
        if (instId > 9999) {
            throw new RuntimeException("El código de la instancia no es válido");
        }

        if (!INSTANCES.containsKey(instId)) {
            try (Connection conn = BillingServlet.getConnection()) {
                BillInstance inst = new BillInstance().select(instId, conn);
                if (inst == null) {
                    throw new Exception("No existe la instancia id: " + instId);
                }
                INSTANCES.put(instId, inst);
            }
        }
        return INSTANCES.get(instId);
    }

    public static void clearCache() {
        INSTANCES.clear();
    }

    public static String getInstName(int instId) throws Exception {
        return getInst(instId).name;
    }

    public static String getDbName(int instId) throws Exception {
        return getInst(instId).db;
    }

    public static Connection getConnection(int instId) throws Exception {
        return MySQLCommon.getConnection(getDbName(instId), null);
    }

    public static Connection getConnection() throws Exception {
        return MySQLCommon.getConnection("sigmads", null);
    }

    public static void getInst() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
