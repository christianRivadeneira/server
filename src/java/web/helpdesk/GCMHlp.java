package web.helpdesk;

import java.sql.Connection;
import javax.json.JsonObject;
import service.MySQL.MySQLCommon;
import static web.push.GCMUtils.sendToApp;

public class GCMHlp {

    public static void sendToHlpApp(int appId, JsonObject data, String poolName, String tz, String employee) throws Exception {
        Connection dbConn = null;
        try {
            dbConn = MySQLCommon.getConnection(poolName, tz);
            GCMHlp.sendToCliApp(appId, data, dbConn, employee);
        } finally {
            MySQLCommon.closeConnection(dbConn);
        }
    }

    public static void sendToCliApp(int appId, JsonObject data, Connection conn, String employee) throws Exception {
        String tokensQuery = "SELECT id, token FROM sys_gcm_token WHERE app_id = " + appId + " AND emp_id = " + employee;
        String deleteTokenQuery = "DELETE FROM sys_gcm_token WHERE id = ?1 ";
        sendToApp(appId, "https://fcm.googleapis.com/fcm/send", data, tokensQuery, deleteTokenQuery, conn);
    }
}
