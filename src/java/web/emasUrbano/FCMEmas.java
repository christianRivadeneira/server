package web.emasUrbano;

import java.sql.Connection;
import javax.json.JsonObject;
import service.MySQL.MySQLCommon;
import static web.push.GCMUtils.sendToApp;

public class FCMEmas {

    public static void sendToUrbApp(int appId, JsonObject data, String poolName, String tz, String neighId, String zoneId, String type) throws Exception {
        Connection dbConn = null;
        try {
            dbConn = MySQLCommon.getConnection(poolName, tz);
            FCMEmas.sendToCliApp(appId, data, neighId, zoneId, dbConn, type);
        } finally {
            MySQLCommon.closeConnection(dbConn);
        }
    }

    public static void sendToCliApp(int appId, JsonObject data, String neighId, String zoneId, Connection conn, String type) throws Exception {

        String tokensQuery = null;
        String deleteTokenQuery = null;

        switch (type) {
            case "recol":
                tokensQuery = "SELECT id, token FROM urb_gcm_token  "
                        + (neighId != null ? " WHERE neigh_id IN (" + neighId + ")" : "");
                deleteTokenQuery = "DELETE FROM urb_gcm_token WHERE id = ?1 ";
                break;
            case "cut":
                if (zoneId != null) {
                    tokensQuery = "SELECT id, token FROM urb_gcm_token WHERE zone_id = " + zoneId;
                    deleteTokenQuery = "DELETE FROM urb_gcm_token WHERE id = ?1 ";
                }
                break;
        }

        if (tokensQuery != null || deleteTokenQuery != null) {
            sendToApp(appId, "https://fcm.googleapis.com/fcm/send", data, tokensQuery, deleteTokenQuery, conn);
        }

    }
}
