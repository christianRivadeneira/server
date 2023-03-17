package web.push;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import service.MySQL.MySQLCommon;
import utilities.Batches;
import utilities.MySQLQuery;
import web.fileManager;

public class GCMUtils {

    public static void sendToApp(int appId, JsonObject data, String poolName, String tz, String employeeIds) throws Exception {
        Connection dbConn = null;
        try {
            dbConn = MySQLCommon.getConnection(poolName, tz);
            sendToApp(appId, data, employeeIds, dbConn);
        } finally {
            MySQLCommon.closeConnection(dbConn);
        }
    }

    public static void sendToAppManagers(JsonObject data, String employeeIds, Connection dbConn) throws Exception {
        int appId = new MySQLQuery("SELECT id FROM system_app WHERE package_name = 'com.glp.servicemanagers'").getAsInteger(dbConn);
        String tokensQuery = "SELECT id, token FROM sys_gcm_token WHERE app_id = " + appId + (employeeIds != null ? " AND emp_id IN (" + employeeIds + ")" : "");
        String deleteTokenQuery = "DELETE FROM sys_gcm_token WHERE id = ?1 ";
        sendToApp(appId, "https://fcm.googleapis.com/fcm/send", data, tokensQuery, deleteTokenQuery, dbConn);
    }

    public static void sendToAppReadings(int appId, JsonObject data, String poolName, String tz, String employeeIds) throws Exception {
        Connection dbConn = null;
        try {
            dbConn = MySQLCommon.getConnection(poolName, tz);
            String tokensQuery = "SELECT id, token FROM sys_gcm_token WHERE app_id = " + appId
                    + (employeeIds != null ? " AND emp_id IN (" + employeeIds + ")" : "");
            String deleteTokenQuery = "DELETE FROM sys_gcm_token WHERE id = ?1 ";
            sendToApp(appId, "https://fcm.googleapis.com/fcm/send", data, tokensQuery, deleteTokenQuery, dbConn);
        } finally {
            MySQLCommon.closeConnection(dbConn);
        }
    }

    public static void sendToAppAsync(final int appId, final JsonObject data, final String employeeIds) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try (Connection conn = MySQLCommon.getDefaultConnection()) {
                    sendToApp(appId, data, employeeIds, conn);
                } catch (Exception ex) {
                    Logger.getLogger(GCMUtils.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        t.start();
    }

    public static void sendToApp(int appId, JsonObject data, String employeeIds, Connection conn) throws Exception {
        String tokensQuery = "SELECT id, token FROM sys_gcm_token WHERE app_id = " + appId
                + (employeeIds != null ? " AND emp_id IN (" + employeeIds + ")" : "");
        String deleteTokenQuery = "DELETE FROM sys_gcm_token WHERE id = ?1 ";
        sendToApp(appId, "https://fcm.googleapis.com/fcm/send", data, tokensQuery, deleteTokenQuery, conn);
    }

    public static void sendToApp(int appId, String address, JsonObject data, String tokensQuery, String deleteTokenQuery, Connection conn) throws Exception {
        String serverKey = new MySQLQuery("SELECT google_server_key FROM system_app WHERE id = " + appId).getAsString(conn);
        Object[][] tokens = new MySQLQuery(tokensQuery).getRecords(conn);
        Batches bs = new Batches(tokens.length, 900);
        for (int i = 0; i < bs.getBatches(); i++) {
            int tries = 0;
            do {
                HttpURLConnection httpConn;
                StringBuilder log = new StringBuilder();
                try {
                    httpConn = (HttpURLConnection) new URL(address).openConnection();
                    httpConn.setRequestMethod("POST");
                    httpConn.setRequestProperty("Content-Type", "application/json");
                    httpConn.setRequestProperty("Authorization", "key=" + serverKey);
                    httpConn.setConnectTimeout(5000);
                    httpConn.setReadTimeout(30000);
                    httpConn.setDoOutput(true);

                    List<Integer> tokenIds = new ArrayList<>();
                    JsonArrayBuilder ab = Json.createArrayBuilder();
                    for (int j = bs.getBeginIndex(i); j <= bs.getEndIndex(i); j++) {
                        ab.add(MySQLQuery.getAsString(tokens[j][1]));
                        tokenIds.add(MySQLQuery.getAsInteger(tokens[j][0]));
                    }
                    JsonArray jTokens = ab.build();
                    OutputStream os = httpConn.getOutputStream();

                    try (JsonWriter w = Json.createWriter(os)) {
                        JsonObjectBuilder ob = Json.createObjectBuilder()
                                .add("registration_ids", jTokens)
                                .add("data", data);
                        JsonObject o = ob.build();
                        w.writeObject(o);
                        log.append(o.toString()).append(System.lineSeparator());
                    }

                    int rta = httpConn.getResponseCode();
                    if (rta == 200) {
                        JsonObject robj = Json.createReader(new InputStreamReader(httpConn.getInputStream())).readObject();
                        int success = robj.getInt("success");
                        int failure = robj.getInt("failure");
                        if (failure > 0) {
                            log.append("success: ").append(success).append("\t. ").append("failure: ").append(failure).append(System.lineSeparator());
                        }
                        if (robj.containsKey("results")) {
                            JsonArray results = robj.getJsonArray("results");
                            for (int j = 0; j < results.size(); j++) {
                                JsonObject result = results.getJsonObject(j);
                                if (result.containsKey("error")) {
                                    String error = result.getString("error");
                                    if (error.equals("NotRegistered") || error.equals("InvalidRegistration")) {
                                        new MySQLQuery(deleteTokenQuery).setParam(1, tokenIds.get(j)).executeDelete(conn);
                                    }
                                    log.append(result.getString("error")).append(", ");
                                }
                            }
                            log.append(System.lineSeparator());
                        }
                        break;
                        //Boolean failure = robj.getInt("failure") == 1;
                    } else if (rta == 400) {
                        //Indicates that the request could not be parsed as JSON, or it contained invalid fields
                        log.append(getError(httpConn)).append(System.lineSeparator());
                        throw new Exception("Error 400, la solicitud no está bien formada. ");
                    } else if (rta == 401) {
                        //There was an error authenticating the sender account.
                        String error = getError(httpConn);
                        log.append(error).append(System.lineSeparator());
                        throw new Exception("Error 401, la solicitud no está bien formada.");
                    } else if (rta >= 500 && rta <= 599) {
                        log.append(getError(httpConn)).append(System.lineSeparator());
                        throw new Exception("Internal Server Error.");
                        //internal error in the GCM connection server 
                    } else {
                        throw new Exception("Unexpected Response");
                    }
                } catch (Exception e) {
                    log.append("Retry").append(System.lineSeparator());
                    System.out.println(log);
                    java.util.logging.Logger.getLogger(GCMUtils.class.getName()).log(java.util.logging.Level.SEVERE, e.getMessage(), e);
                    tries++;
                    if (tries == 3) {
                        throw e;
                    } else {
                        try {
                            Thread.sleep(1500);
                        } catch (Exception ex) {
                        }
                    }
                }
            } while (true);
        }
    }

    public static void sendToServirApp(int appId, String address, JsonObjectBuilder data, String tokensQuery, String deleteTokenQuery, Connection conn) throws Exception {
        String serverKey = new MySQLQuery("SELECT google_server_key FROM system_app WHERE id = " + appId).getAsString(conn);
        Object[][] tokens = new MySQLQuery(tokensQuery).getRecords(conn);
        Batches bs = new Batches(tokens.length, 900);

        for (int i = 0; i < bs.getBatches(); i++) {
            int tries = 0;
            JsonObjectBuilder sendObj = data;
            do {
                HttpURLConnection httpConn;
                StringBuilder log = new StringBuilder();
                try {
                    httpConn = (HttpURLConnection) new URL(address).openConnection();
                    httpConn.setRequestMethod("POST");
                    httpConn.setRequestProperty("Content-Type", "application/json");
                    httpConn.setRequestProperty("Authorization", "key=" + serverKey);
                    httpConn.setDoOutput(true);

                    List<Integer> tokenIds = new ArrayList<>();
                    JsonArrayBuilder ab = Json.createArrayBuilder();
                    for (int j = bs.getBeginIndex(i); j <= bs.getEndIndex(i); j++) {
                        ab.add(MySQLQuery.getAsString(tokens[j][1]));
                        tokenIds.add(MySQLQuery.getAsInteger(tokens[j][0]));
                    }
                    JsonArray jTokens = ab.build();
                    OutputStream os = httpConn.getOutputStream();

                    try (JsonWriter w = Json.createWriter(os)) {
                        sendObj.add("registration_ids", jTokens)
                                .add("priority", "high")
                                .add("restricted_package_name", "");
                        JsonObject send = sendObj.build();
                        w.writeObject(send);
                        log.append(send.toString()).append(System.lineSeparator());
                    }

                    int rta = httpConn.getResponseCode();
                    if (rta == 200) {
                        JsonObject robj = Json.createReader(new InputStreamReader(httpConn.getInputStream())).readObject();
                        int success = robj.getInt("success");
                        int failure = robj.getInt("failure");
                        if (failure > 0) {
                            log.append("success: ").append(success).append("\t. ").append("failure: ").append(failure).append(System.lineSeparator());
                        }
                        if (robj.containsKey("results")) {
                            JsonArray results = robj.getJsonArray("results");
                            for (int j = 0; j < results.size(); j++) {
                                JsonObject result = results.getJsonObject(j);
                                if (result.containsKey("error")) {
                                    String error = result.getString("error");
                                    if (error.equals("NotRegistered") || error.equals("InvalidRegistration")) {
                                        new MySQLQuery(deleteTokenQuery).setParam(1, tokenIds.get(j)).executeDelete(conn);
                                    }
                                    log.append(result.getString("error")).append(", ");
                                }
                            }
                            log.append(System.lineSeparator());
                        }
                        break;
                        //Boolean failure = robj.getInt("failure") == 1;
                    } else if (rta == 400) {
                        //Indicates that the request could not be parsed as JSON, or it contained invalid fields
                        log.append(getError(httpConn)).append(System.lineSeparator());
                        throw new Exception("Error 400, la solicitud no está bien formada. ");
                    } else if (rta == 401) {
                        //There was an error authenticating the sender account.
                        String error = getError(httpConn);
                        log.append(error).append(System.lineSeparator());
                        throw new Exception("Error 401, la solicitud no está bien formada.");
                    } else if (rta >= 500 && rta <= 599) {
                        log.append(getError(httpConn)).append(System.lineSeparator());
                        throw new Exception("Internal Server Error.");
                        //internal error in the GCM connection server 
                    } else {
                        throw new Exception("Unexpected Response");
                    }
                } catch (Exception e) {
                    log.append("Retry").append(System.lineSeparator());
                    System.out.println(log);
                    java.util.logging.Logger.getLogger(GCMUtils.class.getName()).log(java.util.logging.Level.SEVERE, e.getMessage(), e);
                    tries++;
                    if (tries == 3) {
                        throw e;
                    } else {
                        try {
                            Thread.sleep(1500);
                        } catch (Exception ex) {
                        }
                    }
                }
            } while (true);
        }
    }

    public static String getError(HttpURLConnection conn) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fileManager.copy(conn.getErrorStream(), baos, true, true);
        return new String(baos.toByteArray());
    }
}
