package api.sys.dto;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import web.push.GCMUtils;

public class Notify {

    public String title;
    public String message;
    public String empIds;
    public String imgUrl;

    public static void sendNotification(Connection conn, Notify obj) throws Exception {

        JsonObjectBuilder not = Json.createObjectBuilder();
        JsonObjectBuilder ob = Json.createObjectBuilder();
        ob.add("title", obj.title);
        ob.add("body", obj.message);
        ob.add("sound", "default");
        ob.add("click_actoin", "FCM_PLUGIN_ACTIVITY");
        ob.add("icon", "fcm_push_icon");

        not.add("notificacion", ob);

        ob = Json.createObjectBuilder();
        ob.add("page", "secure/notification");
        ob.add("id", 1);
        ob.add("title", obj.title);
        ob.add("description", obj.message);
        if (obj.imgUrl != null) {
            ob.add("img", obj.imgUrl);
        }
        ob.add("datetime", new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(new Date()));
        ob.add("time", 1);
        not.add("data", ob);

        String tokensQuery = "SELECT id, token FROM ess_fcm_token WHERE TRUE "
                + (obj.empIds != null ? "AND usr_id IN (" + obj.empIds + ")" : "");
        String deleteTokenQuery = "DELETE FROM ess_fcm_token WHERE usr_id = ?1";

        GCMUtils.sendToServirApp(19, "https://fcm.googleapis.com/fcm/send", not, tokensQuery, deleteTokenQuery, conn);

    }

}
