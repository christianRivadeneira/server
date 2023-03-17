package web.quality;

import java.sql.Connection;
import utilities.MySQLQuery;

public class MailCfg {

    public Boolean mailActive;
    public String mailFrom;
    public Boolean mailAuth;
    public String mailUser;
    public String mailPasswd;
    public Boolean mailTls;
    public String smtpHost;
    public String smtpPort;
    public String mailSignatureUrl;
    public Boolean ssl;

    public static MailCfg select(Connection con) throws Exception {
        MailCfg obj;
        Object[][] data = new MySQLQuery("SELECT "
                + "`mail_active`, "
                + "`mail_from`, "
                + "`mail_auth`, "
                + "`mail_user`, "
                + "`mail_passwd`, "
                + "`mail_tls`, "
                + "`smtp_host`, "
                + "`smtp_port`, "
                + "`mail_signature_url`, "
                + "`ssl`, "
                + "id "
                + "FROM sys_cfg WHERE id = 1").getRecords(con);
        obj = new MailCfg();
        obj.mailActive = MySQLQuery.getAsBoolean(data[0][0]);
        obj.mailFrom = MySQLQuery.getAsString(data[0][1]);
        obj.mailAuth = MySQLQuery.getAsBoolean(data[0][2]);
        obj.mailUser = MySQLQuery.getAsString(data[0][3]);
        obj.mailPasswd = MySQLQuery.getAsString(data[0][4]);
        obj.mailTls = MySQLQuery.getAsBoolean(data[0][5]);
        obj.smtpHost = MySQLQuery.getAsString(data[0][6]);
        obj.smtpPort = MySQLQuery.getAsString(data[0][7]);
        obj.mailSignatureUrl = MySQLQuery.getAsString(data[0][8]);
        obj.ssl = MySQLQuery.getAsBoolean(data[0][9]);
        return obj;
    }
}
