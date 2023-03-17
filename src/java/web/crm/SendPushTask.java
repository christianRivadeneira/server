package web.crm;

import api.sys.model.SysGcmMessage;
import java.io.StringReader;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import metadata.model.Table;
import service.MySQL.MySQLCommon;
import utilities.SysTask;
import web.push.GCMUtils;
import web.quality.SendMail;

@Singleton
public class SendPushTask {

    public static void main(String[] args) {

    }

    @Schedule(minute = "*/5", second = "0", hour = "*")
    protected void processRequest() {
        try {
            try (Connection con = MySQLCommon.getConnection("sigmads", null)) {
                SysTask t = new SysTask(SendPushTask.class, System.getProperty("user.name"), 1, con);
                try {
                    List<SysGcmMessage> messages = SysGcmMessage.getPendingMessages(con, 11);

                    if (messages == null || messages.isEmpty()) {
                        return;
                    }
                    System.out.println("push de recordatorio: " + messages.size());
                    for (SysGcmMessage msg : messages) {

                        JsonObject dataMsg;
                        try (JsonReader jsonReader = Json.createReader(new StringReader(msg.data))) {
                            dataMsg = jsonReader.readObject();
                            GCMUtils.sendToApp(msg.appId, dataMsg, msg.empId + "", con);
                            msg.sendDate = new Date();
                            msg.update(con);
                        }
                    }
                    t.success(con);
                } catch (Exception ex) {
                    t.error(ex, con);
                    Logger.getLogger(SendPushTask.class.getName()).log(Level.SEVERE, null, ex);
                    try {
                        String user = "";
                        try {
                            user = System.getProperty("user.name");
                        } catch (Exception ex1) {
                        }
                        String env = Table.DEVEL_MODE ? "Desarrollador " : "Producción";
                        SendMail.sendMail(con, "soporte@qualisys.com.co", "Recordarios de tareas", "Usuario: " + user + " Ambiente: " + env + ". Error en envio de recordatorios " + ex.getMessage(), "Erroren envio correo automaticos " + ex.getMessage());
                    } catch (Exception ex1) {
                        Logger.getLogger(SendPushTask.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
            }
        } catch (Exception ex) {
            // para errores de cx a bd
            Logger.getLogger(SendPushTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
