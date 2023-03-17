package web.tanks;

import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import web.quality.MailCfg;
import web.quality.SendMail;

@Singleton
@Startup
public class SendMailSummary {

    @Schedule(dayOfWeek = "Sat", hour = "10")
    protected void processRequest() {
        try {
            System.out.println("Llamado envío de correos estacionarios.");
            try (Connection conn = MySQLCommon.getConnection("sigmads", null)) {
                Object[] cfg = new MySQLQuery("SELECT mail_alert_logo_url, mail_sale_tk FROM sys_cfg").getRecord(conn);
                if (MySQLQuery.getAsBoolean(cfg[1])) {
                    String logo = (cfg[0] != null ? MySQLQuery.getAsString(cfg[0]) : "http://qualisys.com.co/ent_logos/qualisys_new.png");
                    Object[][] data = new MySQLQuery("SELECT "
                            + "c.id, "//0
                            + "c.contact_mail, "//1
                            + "s.sale_date, "//2
                            + "t.serial, "//3
                            + "s.kgs, "//4
                            + "s.total, "//5
                            + "IF(pc.id IS NOT NULL, COALESCE(p.prize_value, (s.total * p.prize_percent / 100), (SELECT prize FROM com_promo_prize WHERE id = p.prize_id)), NULL) "//6
                            + "FROM ord_tank_client c "
                            + "INNER JOIN est_sale s ON s.client_id = c.id "
                            + "INNER JOIN est_tank t ON s.est_tank_id = t.id "
                            + "LEFT JOIN com_promo_claim pc ON pc.est_sale_id = s.id "
                            + "LEFT JOIN com_promo p ON pc.promo_id = p.id "
                            + "WHERE "
                            + "s.sale_date BETWEEN DATE_SUB(NOW(), INTERVAL 7 DAY) AND NOW() "
                            + "ORDER BY c.id ASC").getRecords(conn);

                    Integer curClie = null;
                    String curMail = null;
                    String ls = System.lineSeparator();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    DecimalFormat df = new DecimalFormat("#,###.00");
                    MailCfg mcfg = MailCfg.select(conn);

                    StringBuilder sb = new StringBuilder();
                    StringBuilder errSb = new StringBuilder();
                    for (int i = 0; i < data.length; i++) {
                        Object[] row = data[i];
                        if (curClie == null || curClie.equals(MySQLQuery.getAsInteger(row[0]))) {
                            curClie = MySQLQuery.getAsInteger(row[0]);
                            curMail = MySQLQuery.getAsString(row[1]);
                            setMsg(sb, row, sdf, df, ls);
                        } else {
                            sendMail(sb, errSb, logo, mcfg, curMail, MySQLQuery.getAsString(row[3]));

                            sb = new StringBuilder();
                            curClie = MySQLQuery.getAsInteger(row[0]);
                            curMail = MySQLQuery.getAsString(row[1]);
                            setMsg(sb, row, sdf, df, ls);
                        }

                        if (i == data.length - 1) {
                            sendMail(sb, errSb, logo, mcfg, curMail, MySQLQuery.getAsString(row[3]));
                        }
                    }

                    if (!errSb.toString().isEmpty()) {
                        new MySQLQuery("INSERT INTO system_log "
                                + "SET owner_id = 1, "
                                + "owner_type = 25, " //Tipo definido en cliente, SystemLog. No mover.
                                + "employee_id = 1, "
                                + "log_date = NOW(), "
                                + "notes = '" + errSb.toString() + "'").executeInsert(conn);
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(SendMailSummary.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void sendMail(StringBuilder sb, StringBuilder errorSb, String logo, MailCfg cfg, String curMail, String tankSerial) {
        try {
            if (curMail == null || curMail.isEmpty()) {
                throw new Exception("Cliente sin correo de contacto");
            }
            String plain = sb.toString();
            plain = Matcher.quoteReplacement(plain);
            String html = SendMail.readTemplate("/web/template.html");
            html = html.replaceAll("\\{headerTitle\\}", "");
            html = html.replaceAll("\\{titleAlerts\\}", "Venta Estacionario");
            html = html.replaceAll("\\{ent_logo\\}", logo);
            html = html.replaceAll("[\r\n\t]", "");
            html = html.replaceAll("\\{rows\\}", plain);
            SendMail.sendMail(cfg, curMail, "Resumen semanal", html, plain);
        } catch (Exception e) {
            Logger.getLogger(SendMailSummary.class.getName()).log(Level.SEVERE, null, e);
            errorSb.append(tankSerial).append(System.lineSeparator()).append(e.getMessage()).append(System.lineSeparator());
        }
    }

    private void setMsg(StringBuilder sb, Object[] row, SimpleDateFormat sdf, DecimalFormat df, String ls) throws Exception {
        sb.append("Serial: ").append(row[3]).append(ls)
                .append("Fecha: ").append(sdf.format(MySQLQuery.getAsDate(row[2]))).append(ls)
                .append("Kilogramos: ").append(row[4]).append(ls)
                .append("Valor: ").append(df.format(row[5])).append(ls);

        if (row[6] != null) {
            sb.append("En ésta venta se ganó un bono promocional correspondiente a: ");
            String prize = row[6].toString();
            sb.append(prize.matches("[^0-9\\.,]+") ? prize : df.format(df.parse(prize))).append(ls).append(ls);
        } else {
            sb.append(ls);
        }
    }

}
