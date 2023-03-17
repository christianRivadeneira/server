package api.ess.utils;

import api.ess.model.EssPerson;
import java.sql.Connection;
import java.util.regex.Matcher;
import utilities.MySQLQuery;
import web.quality.SendMail;
import static web.quality.SendMail.LINE_BREAK;

public class SendRecoveryMail {

    private static final String TITLE = "Recuperación de contraseña.";

    public static void sendMail(EssPerson usr, Connection con) throws Exception {
        String logo = new MySQLQuery("SELECT mail_alert_logo_url FROM sys_cfg").getAsString(con);
        logo = (logo != null ? logo : "http://qualisys.com.co/ent_logos/qualisys_new.png");
        String plain = "Saludos " + usr.firstName + "!" + LINE_BREAK + LINE_BREAK;
        plain += "Hemos enviado este correo porque ha solicitado el restablecimiento de " + LINE_BREAK
                + "su contraseña, debe ingresar este PIN en su App." + LINE_BREAK + LINE_BREAK;
        plain += "PIN DE SEGURIDAD: <b>" + usr.recoveryPin + "</b>" + LINE_BREAK + LINE_BREAK;
        plain = Matcher.quoteReplacement(plain);
        String html = SendMail.readTemplate("/web/template.html");
        html = html.replaceAll("\\{headerTitle\\}", "Información");
        html = html.replaceAll("\\{titleAlerts\\}", TITLE);
        html = html.replaceAll("\\{total\\}", "");
        html = html.replaceAll("\\{ent_logo\\}", logo);
        html = html.replaceAll("[\r\n\t]", "");
        html = html.replaceAll("\\{rows\\}", plain);
        SendMail.sendMail(con, usr.mail, "Recuperación de contraseña", html, plain);
    }
}
