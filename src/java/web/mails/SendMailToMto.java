package web.mails;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import utilities.SysTask;
import web.quality.SendMail;
import static web.quality.SendMail.LINE_BREAK;

@Singleton
@Startup
public class SendMailToMto {

    @Schedule(hour = "9")
    protected void processRequest() {
        System.out.println("ENVIANDO CORREOS DE MTO");
        try {
            try (Connection con = MySQLCommon.getConnection("sigmads", null)) {
                SysTask t = new SysTask(SendMailToMto.class, System.getProperty("user.name"), 1, con);
                try {
                    Object[] sysCfg = new MySQLQuery("SELECT mail_alert_logo_url, send_mto_mail FROM sys_cfg").getRecord(con);

                    if (!MySQLQuery.getAsBoolean(sysCfg[1])) {
                        return;
                    }

                    String logo = MySQLQuery.getAsString(sysCfg[0]);
                    logo = (logo != null ? logo : "http://qualisys.com.co/ent_logos/qualisys_new.png");
                    Object[][] encargados = new MySQLQuery("SELECT DISTINCT "
                            + "e.id, "
                            + "CONCAT(e.first_name,' ',e.last_name), "
                            + "e.mail,"
                            + "IF(mce.city_id IS NOT NULL, (SELECT GROUP_CONCAT(mto_city_employee.city_id) FROM mto_city_employee WHERE mto_city_employee.emp_id = e.id), (SELECT GROUP_CONCAT(agency.city_id) FROM agency)) "
                            + "FROM mto_city_employee mce "
                            + "INNER JOIN employee e ON e.id = mce.emp_id "
                            + "WHERE e.active = 1 AND e.mail IS NOT NULL AND e.mail <> ''").getRecords(con);
                    MtoCfg cfg = new MtoCfg(con);
                    for (Object[] encargado : encargados) {
                        ManagerMto enc = new ManagerMto(con, MySQLQuery.getAsInteger(encargado[0]), MySQLQuery.getAsString(encargado[1]), MySQLQuery.getAsString(encargado[2]), MySQLQuery.getAsString(encargado[3]), cfg);
                        String plain = "Vehículos" + LINE_BREAK;
                        plain += "Alertas de Flota" + LINE_BREAK;
                        plain += "Documentos de Vencidos:          " + enc.alDocsVh;
                        plain += "TOTAL ALERTAS:          " + enc.totalAlert;
                        String modelRow = "<tr><td style=\"mso-table-lspace: 0pt;mso-table-rspace: 0pt;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%;\">?1</td><td style=\"mso-table-lspace: 0pt;mso-table-rspace: 0pt;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%;\">?2</td></tr>";
                        String titleRow = "<tr><td style=\"font-size: 18px;mso-table-lspace: 0pt;mso-table-rspace: 0pt;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%;\"><b>?1</b></td></tr>";
                        String rows
                                = (format(titleRow, "VEHÍCULOS"))
                                + (format(modelRow, "Documentos Vencidos", enc.alDocsVh))
                                + (format(modelRow, "Documentos Faltantes", enc.alDocsMandatoryVh))
                                + (format(modelRow, "Revisión de Dotación", enc.alElementsRevVh))
                                + (format(modelRow, "Dotación Faltante", enc.alElementsMandatoryVh))
                                + (format(modelRow, "Fuera de Servicio", enc.alOutServiceVh))
                                + (format(modelRow, "Novedades", enc.alNovsVh))
                                + (format(titleRow, "CONDUCTORES"))
                                + (format(modelRow, "Documentos Vencidos", enc.alDocsDrv))
                                + (format(modelRow, "Documentos Faltantes", enc.alDocsDrvMandatory))
                                + (format(titleRow, "MANTENIMIENTO"))
                                + (format(modelRow, "Preventivos", enc.alMtoPrev))
                                + (format(modelRow, "Ordenes Programadas", enc.alMtoProg));
                        if (cfg.pnlFormats) {
                            rows += (format(modelRow, "Próxima Revisión", enc.alProxRevisions))
                                    + (format(modelRow, "Producto no Conforme", enc.alProductNoConform));
                        }
                        String html = SendMail.readTemplate("./templateMto.html");
                        html = html.replaceAll("\\{headerTitle\\}", "Resumen Diario de Gesti&oacute;n");
                        html = html.replaceAll("\\{titleAlerts\\}", "Alertas de Flota");
                        html = html.replaceAll("[\r\n\t]", "");
                        html = html.replaceAll("\\{total\\}", enc.totalAlert + "");
                        html = html.replaceAll("\\{ent_logo\\}", logo);
                        html = html.replaceAll("\\{rows\\}", rows);
                        //out.write(html);
                        SendMail.sendMail(con, enc.mail, "Alertas de Flota", html, plain);
                        //sendMail.sendMail(con, "cristiangif@hotmail.com", "Alertas de Flota", html, plain);//para pruebas

                    }
                    t.success(con);
                } catch (Exception ex) {
                    t.error(ex, con);
                    Logger.getLogger(SendMailToMto.class.getName()).log(Level.SEVERE, null, ex);
                    try {
                        SendMail.sendMail(con, "karol.mendoza@montagas.com.co", "Alertas de Flota", "Error en envio de correos. MTO " + ex.getMessage(), "Error en envio de correos. MTO " + ex.getMessage());
                    } catch (Exception ex1) {
                        Logger.getLogger(SendMailToMto.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
            }
        } catch (Exception ex) {
            // para errores de cx a bd
            Logger.getLogger(SendMailToMto.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String format(String base, Object... args) {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            base = base.replaceAll("\\?" + (i + 1), arg.toString());
        }
        return base;
    }

    class ManagerMto {

        Integer id;
        String name;
        String mail;
        String citiesId;
        Connection con;

        Integer alDocsVh;
        Integer alDocsMandatoryVh;
        Integer alElementsMandatoryVh;
        Integer alElementsRevVh;
        Integer alOutServiceVh;
        Integer alNovsVh;
        Integer alDocsDrv;
        Integer alDocsDrvMandatory;

        Integer alMtoPrev;
        Integer alMtoProg;

        Integer alProxRevisions = 0;
        Integer alProductNoConform = 0;

        Integer totalAlert = 0;
        MtoCfg cfg;

        public ManagerMto(Connection con, Integer id, String name, String mail, String cities, MtoCfg cfg) throws Exception {
            this.con = con;
            this.id = id;
            this.name = name;
            this.mail = mail;
            this.citiesId = cities;
            this.cfg = cfg;
            getDocsVh();
            getDocsMandatoryVh();
            getElementsVh();
            getRevisionElementsVh();
            getOutServiceVh();
            getNovsVh();
            getDocsDrv();
            getMandatoryDocsDrv();
            getMtoPrev();
            getMtoProgMto();
            if (cfg.pnlFormats) {
                getArrivingFormats();
                getProductNoConform();
            }
            this.totalAlert = alDocsVh + alDocsMandatoryVh + alElementsMandatoryVh + alElementsRevVh + alOutServiceVh + alNovsVh + alDocsDrv + alDocsDrvMandatory + alMtoPrev + alMtoProg + alProxRevisions + alProductNoConform;
        }

        ///////////////DRIVERS/////////////////////////
        private void getDocsVh() throws Exception {
            //ESTE QUERY ESTA EN EL CLIENTE EN AlertDocsVehs.java
            Integer c = new MySQLQuery("SELECT "
                    + "COUNT(*) "
                    + "FROM "
                    + "document AS d "
                    + "INNER JOIN document_vehicle AS dv ON dv.doc_id = d.id "
                    + "INNER JOIN vehicle AS v ON dv.vehicle_id = v.id "
                    + "INNER JOIN agency AS a ON a.id = v.agency_id "
                    + "WHERE dv.apply = true "
                    + "AND a.city_id IN (" + citiesId + ") "
                    + "AND v.visible = 1 AND "
                    + "v.active = 1 AND "
                    + "dv.fecha IS NOT NULL AND "
                    + "dv.apply = 1 AND "
                    + "DATEDIFF(dv.fecha,CURDATE())<=(Select cfg.alert_docs FROM mto_cfg AS cfg) ").getAsInteger(con);
            alDocsVh = c != null ? c : 0;
        }

        private void getDocsMandatoryVh() throws Exception {
            //ESTE QUERY ESTA EN EL CLIENTE EN AlertDocsVehsMandatory.java
            Integer c = new MySQLQuery("SELECT COUNT(*) "
                    + "FROM vehicle AS v "
                    + "INNER JOIN agency AS a ON a.id = v.agency_id "
                    + "INNER JOIN enterprise AS e ON e.id = a.enterprise_id "
                    + "WHERE v.active = 1 "
                    + "AND v.visible = 1 "
                    + "AND (SELECT COUNT(*)>0 FROM document AS d WHERE d.active = 1 AND d.mandatory = 1 AND d.id NOT IN (SELECT dv.doc_id FROM document_vehicle AS dv WHERE dv.vehicle_id = v.id AND dv.apply = 1)) "
                    + "AND a.city_id IN (" + citiesId + ") ").getAsInteger(con);
            alDocsMandatoryVh = c != null ? c : 0;
        }

        private void getElementsVh() throws Exception {
            //ESTE QUERY ESTA EN EL CLIENTE EN AlertElementsVehsMandatory.java
            Integer c = new MySQLQuery("SELECT COUNT(*) "
                    + "FROM vehicle AS v "
                    + "INNER JOIN agency AS a ON a.id = v.agency_id "
                    + "INNER JOIN enterprise AS e ON e.id = a.enterprise_id "
                    + "WHERE v.active = 1 AND "
                    + "(SELECT COUNT(*)>0 "
                    + "FROM mto_element AS e "
                    + "WHERE e.mandatory = 1 "
                    + "AND e.id NOT IN (SELECT ev.element_id FROM mto_veh_element AS ev WHERE ev.vehicle_id = v.id)) "
                    + "AND a.city_id IN (" + citiesId + ") ").getAsInteger(con);
            alElementsMandatoryVh = c != null ? c : 0;
        }

        private void getRevisionElementsVh() throws Exception {
            //ESTE QUERY ESTA EN EL CLIENTE EN AlertElements.java
            Integer c = new MySQLQuery("SELECT "
                    + "COUNT(*) "
                    + "FROM "
                    + "mto_element AS e "
                    + "INNER JOIN mto_veh_element AS ve ON ve.element_id = e.id "
                    + "INNER JOIN vehicle AS v ON v.id = ve.vehicle_id "
                    + "INNER JOIN agency AS a ON a.id = v.agency_id "
                    + "INNER JOIN city ct ON a.city_id = ct.id "
                    + "WHERE NOW()>= DATE_SUB(ve.dt_rev_date,INTERVAL (Select element_alert_days from mto_cfg) DAY)"
                    + "AND a.city_id IN (" + citiesId + ")  ").getAsInteger(con);
            alElementsRevVh = c != null ? c : 0;
        }

        private void getOutServiceVh() throws Exception {
            //ESTE QUERY ESTA EN EL CLIENTE EN AlertOutService.java
            Integer c = new MySQLQuery("SELECT "
                    + "COUNT(*) "//
                    + "FROM "
                    + "mto_out_service AS s "
                    + "INNER JOIN vehicle AS v ON s.vehicle_id = v.id "
                    + "INNER JOIN agency AS a ON a.id = v.agency_id "
                    + "INNER JOIN city ct ON a.city_id = ct.id "
                    + "INNER JOIN mto_out_type ot ON s.out_id = ot.id "
                    + "WHERE "
                    + "s.end is null "
                    + "AND a.city_id IN (" + citiesId + ") ").getAsInteger(con);
            alOutServiceVh = c != null ? c : 0;
        }

        private void getNovsVh() throws Exception {
            //ESTE QUERY ESTA EN EL CLIENTE EN AlertNovs.java
            Integer c = new MySQLQuery("SELECT "
                    + "COUNT(*) "
                    + "FROM mto_vh_note AS dn "
                    + "INNER JOIN mto_note_type AS t ON t.id = dn.type_id "
                    + "INNER JOIN vehicle AS v ON v.id = dn.vehicle_id "
                    + "INNER JOIN agency AS a ON a.id = v.agency_id "
                    + "INNER JOIN enterprise AS e ON e.id = a.enterprise_id "
                    + "WHERE dn.rev_date IS NULL AND "
                    + "a.city_id IN (" + citiesId + ") "
                    + "AND v.visible = 1 AND v.active= 1 ").getAsInteger(con);
            alNovsVh = c != null ? c : 0;
        }

        ///////////////DRIVERS/////////////////////////
        private void getDocsDrv() throws Exception {
            //ESTE QUERY ESTA EN EL CLIENTE EN AlertDocsDrivers.java
            Integer c = new MySQLQuery("SELECT "
                    + "COUNT(*) "
                    + "FROM "
                    + "mto_driver_doc_type AS d "
                    + "INNER JOIN mto_driver_doc AS dd ON dd.doc_id = d.id "
                    + "INNER JOIN employee AS e ON dd.driver_id = e.id "
                    + "INNER JOIN agency AS a ON a.id = e.agency_id "
                    + "WHERE dd.apply = 1 "
                    + "AND a.city_id IN (" + citiesId + ") "
                    + "AND e.active = 1 "
                    + "AND dd.fecha IS NOT NULL "
                    + "AND DATEDIFF(dd.fecha,CURDATE())<=(Select cfg.alert_docs FROM mto_cfg AS cfg) ").getAsInteger(con);
            alDocsDrv = c != null ? c : 0;
        }

        private void getMandatoryDocsDrv() throws Exception {
            //ESTE QUERY ESTA EN EL CLIENTE EN AlertDocsMandatoryDriver.java
            Integer c = new MySQLQuery("SELECT "
                    + "COUNT(*) "
                    + "FROM driver_vehicle AS dv "
                    + "INNER JOIN employee AS em ON em.id = dv.driver_id "
                    + "INNER JOIN vehicle v ON v.id = dv.vehicle_id "
                    + "INNER JOIN agency AS a ON a.id = v.agency_id "
                    + "WHERE dv.`end` IS NULL AND em.active = 1 "
                    + "AND (SELECT COUNT(*)>0 "
                    + "FROM mto_driver_doc_type AS dt "
                    + "WHERE dt.mandatory = 1 AND dt.active = 1 "
                    + "AND dt.id NOT IN (SELECT md.doc_id FROM mto_driver_doc AS md WHERE md.driver_id = dv.driver_id AND md.apply = 1)) "
                    + "AND a.city_id IN (" + citiesId + ") ").getAsInteger(con);
            alDocsDrvMandatory = c != null ? c : 0;
        }

        private void getMtoPrev() throws Exception {
            //////////////este query es igual al de AlertMtoPrevNew.java
            Integer c = new MySQLQuery("SELECT "
                    + "COUNT(*) "
                    + "FROM "
                    + "mto_pend_task AS pends "
                    + "INNER JOIN vehicle AS v ON pends.vehicle_id = v.id "
                    + "INNER JOIN agency AS a ON a.id = v.agency_id "
                    + "WHERE "
                    + "prev_mto = 1 AND a.city_id IN (" + citiesId + ") "
                    + "AND IF(" + getIf(false) + " IS NOT NULL, CAST(" + getIf(false) + " AS SIGNED) <= " + cfg.mileage + ", FALSE) OR "
                    + "IF(" + getIf(true) + " IS NOT NULL, DATE_SUB(" + getIf(true) + ", INTERVAL " + cfg.weeks + " WEEK), FALSE)").getAsInteger(con);
            alMtoPrev = c != null ? c : 0;
        }

        private void getMtoProgMto() throws Exception {
            ////////////Este query ya esta en AlertProgramMto.java
            Integer c = new MySQLQuery("SELECT "
                    + "COUNT(*) "//
                    + "FROM work_order wo "
                    + "INNER JOIN vehicle v ON wo.vehicle_id=v.id "
                    + "INNER JOIN agency AS a ON a.id = v.agency_id "
                    + "INNER JOIN vehicle_type vt ON v.vehicle_type_id=vt.id "
                    + "INNER JOIN prov_provider p ON wo.provider_id = p.id "
                    + "WHERE "
                    + "wo.canceled = 0 AND flow_status <> 'planning' AND "
                    + "wo.kind = 'pro' AND "
                    + "wo.done_date IS NULL "
                    + "AND a.city_id IN (" + citiesId + ")  "
                    + "ORDER by wo.begin").getAsInteger(con);
            alMtoProg = c != null ? c : 0;
        }

        private String getIf(boolean days) {
            String[] mat = new String[]{"route", "days", "none", "fueload", "chk"};
            String cond = "";
            boolean first = true;
            for (String type : mat) {
                if (((days && type.equals("days")) || (!days && !type.equals("days"))) && !type.equals("none")) {
                    String fld = "km_left";
                    if (first) {
                        first = false;
                        cond = "IF(mto_type = '" + type + "', " + fld + " , @)";
                    } else {
                        cond = cond.replaceAll("@", "IF(mto_type = '" + type + "', " + fld + " , @)");
                    }
                }
            }
            return cond.replaceAll("@", "NULL");
        }

        private void getArrivingFormats() throws Exception {
            ///es query esta en el cliente en AlertArrivingFormats
            Integer c = new MySQLQuery("SELECT COUNT(*) "
                    + "FROM mto_chk_lst AS lst "
                    + "INNER JOIN mto_chk_version AS ver ON ver.id = lst.version_id "
                    + "INNER JOIN mto_chk_type AS t ON ver.type_id = t.id "
                    + "WHERE t.has_program = 1 "
                    + "AND lst.next_id IS NULL "
                    + "AND SUBDATE(DATE(lst.next_date),INTERVAL (SELECT chk_alert_days FROM mto_cfg) DAY) <= DATE(NOW()) ").getAsInteger(con);
            alProxRevisions = c != null ? c : 0;
        }

        private void getProductNoConform() throws Exception {
            ///es query esta en el cliente en AlertErrorFormat
            Integer c = new MySQLQuery("SELECT COUNT(*) FROM "
                    + "(SELECT "
                    + "lst.id "
                    + "FROM mto_chk_lst AS lst "
                    + "WHERE "
                    + "(SELECT COUNT(*)>0 "
                    + "FROM mto_chk_val AS val "
                    + "INNER JOIN mto_chk_row AS r ON r.id = val.row_id "
                    + "WHERE val.lst_id = lst.id AND r.mandatory = 1 "
                    + "AND val.state <> 'ok' AND r.type <> 'tit' AND val.corr_date IS NULL AND work_order_id IS NULL) OR "
                    + "(SELECT COUNT(*)>0 "
                    + "FROM mto_chk_element AS el "
                    + "WHERE el.lst_id = lst.id "
                    + "AND IF(el.need_review,el.rev_date IS NULL,el.checked = FALSE) AND (el.work_order_id IS NULL AND el.corr_date IS NULL)) "
                    + "GROUP BY lst.id) AS l").getAsInteger(con);
            alProductNoConform = c != null ? c : 0;
        }
    }

    class MtoCfg {

        Integer mileage;
        Boolean weeks;
        Boolean pnlFormats;

        public MtoCfg(Connection con) throws Exception {
            Object[] cfg = new MySQLQuery("SELECT c.mileage, c.weeks, c.pnl_formats FROM mto_cfg AS c WHERE id = 1").getRecord(con);
            mileage = MySQLQuery.getAsInteger(cfg[0]);
            weeks = MySQLQuery.getAsBoolean(cfg[1]);
            pnlFormats = MySQLQuery.getAsBoolean(cfg[2]);
        }
    }

}
