package web.push;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;
import service.MySQL.MySQLCommon;
import static web.push.GCMUtils.sendToApp;

public class FCMClient {

    public static void sendToCliApp(int appId, JsonObject data, String poolName, String tz, String cliId) throws Exception {
        Connection dbConn = null;
        try {
            dbConn = MySQLCommon.getConnection(poolName, tz);
            FCMClient.sendToCliApp(appId, data, cliId, dbConn);
        } finally {
            MySQLCommon.closeConnection(dbConn);
        }
    }

    public static void sendToCliAppAsync(final int appId, final JsonObject data, final String clieIds) throws Exception {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try (Connection conn = MySQLCommon.getDefaultConnection()) {
                    sendToCliApp(appId, data, clieIds, conn);
                } catch (Exception ex) {
                    Logger.getLogger(GCMUtils.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        t.start();
    }

    public static void sendToCliApp(int appId, JsonObject data, String clieIds, Connection conn) throws Exception {
        String tokensQuery = "SELECT id, token FROM clie_gcm_token WHERE "
                + (clieIds != null ? " usr_id IN (" + clieIds + ")" : "");
        String deleteTokenQuery = "DELETE FROM clie_gcm_token WHERE id = ?1 ";
        sendToApp(appId, "https://fcm.googleapis.com/fcm/send", data, tokensQuery, deleteTokenQuery, conn);
    }
}
