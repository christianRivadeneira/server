package web.quality;

import java.sql.Connection;
import utilities.MySQLQuery;
import static web.quality.SendMail.getHtmlMsg;

public class SysMailUtil {

    public static final int PER_FINISH_CLINIC_HIST = 1;
    public static final int PER_CANDIDATE_TO_EMP = 2;
    public static final int EST_NEW_CLIENT_EST = 3;
    public static final int COM_PROMO_AUTH = 4;
    public static final int MTO_SWITCH_AGENCY = 5;
    public static final int BILL_ALERT_READINGS = 6;

    public SysMailUtil() {
    }

    public void sendMail(Integer processId, String subject, String message, Connection conn, boolean isAuto) throws Exception {
        if (!isAuto) {
            boolean isActive = new MySQLQuery("SELECT active FROM sys_mail_process WHERE id = " + processId).getAsBoolean(conn);
            if (!isActive) {
                return;
            }
        }
        String dest = new MySQLQuery("SELECT GROUP_CONCAT(DISTINCT mail SEPARATOR ',') "
                + " FROM sys_mail_process p "
                + " INNER JOIN sys_mail_emp me ON me.process_id = p.id "
                + " INNER JOIN employee e ON e.id = me.emp_id AND e.mail IS NOT NULL AND e.mail <> '' AND e.active "
                + " WHERE p.id = " + processId).getAsString(conn);
        if (dest != null && !dest.isEmpty()) {
            SendMail.sendMail(conn, dest, subject, getHtmlMsg(conn, subject, message), "");
        }
    }

}
