package web.emas;

import java.io.File;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import web.ShortException;
import web.quality.SendMail;
import static web.quality.SendMail.LINE_BREAK;

@Singleton
@Startup
public class SendMailTimer {

    private static final String TITLE = "Manifiesto de Recolección.";
    private static final String EMAS_LOGO = "http://qualisys.com.co/ent_logos/emas.png"; //RUTA LOGO    
    private static final Logger LOG = Logger.getLogger(SendMailTimer.class.getName());

    @PostConstruct
    public void reset() {
        try {
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Schedule(hour = "10,11,12,13,14,15,16,17,18,19,20", minute = "*/30", dayOfWeek = "*", timezone = "UTC-5")
    public void programedEmails() {
        try (Connection conn = MySQLCommon.getConnection("emasds", null)) {
            Object[][] manNums = new MySQLQuery("SELECT man_num FROM emas_cons_man_hist WHERE used = 1 AND send_mail = 0").getRecords(conn);
            if (manNums != null && manNums.length > 0) {
                for (int i = 0; i < manNums.length; i++) {
                    Integer manNum = MySQLQuery.getAsInteger(manNums[i][0]);
                    Object[] sede = new MySQLQuery("SELECT "
                            + "v.id,"
                            + "s.name,"
                            + "s.email,"
                            + "v.dt,"
                            + "s.address "
                            + "FROM emas_clie_sede s "
                            + "INNER JOIN emas_recol_visit v ON s.id = v.clie_sede_id  "
                            + "INNER JOIN emas_cons_man_hist m ON v.man_num = m.man_num "
                            + "WHERE m.man_num = ?1").setParam(1, manNum).getRecord(conn);

                    if (sede != null && sede.length > 0 && sede[2] != null) {
                        File manifest = new RecolManifest(conn, MySQLQuery.getAsInteger(sede[0]), manNum).generateReport();
                        String date = new SimpleDateFormat("dd/MM/yyyy").format(MySQLQuery.getAsDate(sede[3]));

                        String plain = "Señor(es) " + sede[1] + ". " + LINE_BREAK + LINE_BREAK;
                        plain += "El servicio de recolección de residuos peligrosos de "
                                + "la Empresa Metropolitana de Aseo EMAS, ha llevado a cabo "
                                + "una recolección en su establecimiento el día " + date + ", "
                                + (MySQLQuery.getAsString(sede[4]) != null ? "ubicado en la siguiente dirección: " + MySQLQuery.getAsString(sede[4]) + ", " : "")
                                + "por lo tanto, adjuntamos a este correo el respectivo manifiesto "
                                + "de recolección." + LINE_BREAK;
                        plain = Matcher.quoteReplacement(plain);
                        String html;
                        html = SendMail.readTemplate("./template.html");
                        html = html.replaceAll("\\{headerTitle\\}", "Información");
                        html = html.replaceAll("\\{titleAlerts\\}", TITLE);
                        html = html.replaceAll("\\{ent_logo\\}", EMAS_LOGO);
                        html = html.replaceAll("[\r\n\t]", "");
                        html = html.replaceAll("\\{rows\\}", plain);
                        SendMail.sendMail(conn, MySQLQuery.getAsString(sede[2]), "Manifiesto de Recolección.", html, plain, new String[]{"Manifiesto_Visita-" + MySQLQuery.getAsDate(sede[3]) + ".pdf"}, new File[]{manifest});
                    } else {
                        System.err.println("No Hay registro de " + manNum);
                        i++;
                    }

                    new MySQLQuery("UPDATE emas_cons_man_hist SET send_mail = 1 WHERE man_num = " + manNum).executeUpdate(conn);
                }
            }
        } catch (ShortException ex) {
            ex.simplePrint();
        } catch (Exception ex) {
            Logger.getLogger(SendMailTimer.class.getName()).log(Level.SEVERE, "message", ex);
        }
    }

}
