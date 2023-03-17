package web.mails;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import web.quality.SendMail;
import static web.quality.SendMail.LINE_BREAK;

@WebServlet(name = "SendMailToTh", urlPatterns = {"/SendMailToTh"})
public class SendMailToTh extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        Map<String, String> req = MySQLQuery.scapedParams(request);
        String poolName = req.get("poolName");
        String tz = req.get("tz");
        if (poolName == null) {
            poolName = "sigmads";
            tz = null;
        }
        Connection con = null;
        PrintWriter out = response.getWriter();
        try {
            con = MySQLCommon.getConnection(poolName, tz);
            String logo = new MySQLQuery("SELECT mail_alert_logo_url FROM sys_cfg").getAsString(con);
            logo = (logo != null ? logo : "http://qualisys.com.co/ent_logos/qualisys_new.png");
            Object[][] encargados = new MySQLQuery("SELECT "
                    + "DISTINCT "
                    + "CONCAT(e.first_name,' ',e.last_name), "
                    + "e.mail, "
                    + "IF(pma.area_id IS NOT NULL, (SELECT GROUP_CONCAT(per_manager_area.area_id) FROM per_manager_area WHERE per_manager_area.emp_id = e.id), (SELECT GROUP_CONCAT(per_area.id) FROM per_area)) "
                    + "FROM per_manager_area pma "
                    + "INNER JOIN employee e ON e.id = pma.emp_id "
                    + "WHERE e.active = 1 "
                    + "AND e.mail IS NOT NULL").getRecords(con);
            for (Object[] encargado : encargados) {
                ManagerPer enc = new ManagerPer(con, MySQLQuery.getAsString(encargado[0]), MySQLQuery.getAsString(encargado[1]), MySQLQuery.getAsString(encargado[2]));
                String plain = "Vehículos" + LINE_BREAK;
                plain += "Inconsistencias De Asistencia" + LINE_BREAK;
                plain += "TOTAL ALERTAS:          " + enc.totalAlert;
                String modelRow = "<tr><td style=\"mso-table-lspace: 0pt;mso-table-rspace: 0pt;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%;\">?1</td><td style=\"mso-table-lspace: 0pt;mso-table-rspace: 0pt;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%;\">?2</td></tr>";
                String titleRow = "<tr><td style=\"font-size: 18px;mso-table-lspace: 0pt;mso-table-rspace: 0pt;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%;\"><b>?1</b></td></tr>";
                String rows = format(modelRow, "Advertencias de Inconsistencias", enc.warnPuntualidad)
                        + format(modelRow, "Advertencias de Entradas", enc.warnIn)
                        + format(modelRow, "Advertencias de Salidas", enc.warnOut)
                        + format(modelRow, "No Registrados", enc.noRegistrados)
                        + format(modelRow, "Revisados", enc.warnRevision);
                String html = SendMail.readTemplate("./template.html");
                html = html.replaceAll("\\{headerTitle\\}", "Resumen Diario de Talento Humano");
                html = html.replaceAll("\\{titleAlerts\\}", "Talento Humano");
                html = html.replaceAll("[\r\n\t]", "");
                html = html.replaceAll("\\{total\\}", enc.totalAlert + "");
                html = html.replaceAll("\\{ent_logo\\}", logo);
                html = html.replaceAll("\\{rows\\}", rows);
                // out.write(html);
                SendMail.sendMail(con, enc.mail, "Talento Humano", html, plain);
                //sendMail.sendMail(con, "karol.mendoza@montagas.com.co", "Talento Humano", html, plain);//para pruebas
                out.write("ok");
            }
        } catch (Exception ex) {
            response.sendError(500, ex.getMessage());
            try {
                SendMail.sendMail(con, "karol.mendoza@montagas.com.co", "Alertas de Talento", "Error en envio de correos. Talento " + ex.getMessage(), "Error en envio de correos. Talento " + ex.getMessage());
            } catch (Exception ex1) {
                Logger.getLogger(SendMailToTh.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(SendMailToTh.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySQLCommon.closeConnection(con);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Correos Talento Humano";
    }

    private static String format(String base, Object... args) {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            base = base.replaceAll("\\?" + (i + 1), arg.toString());
        }
        return base;
    }

    class ManagerPer {

        String name;
        String mail;
        String areasIds;
        Connection con;

        Integer warnPuntualidad;
        Integer warnOut;
        Integer warnIn;
        Integer noRegistrados;
        Integer warnRevision;
        Integer totalAlert = 0;

        public ManagerPer(Connection con, String name, String mail, String areasIds) throws Exception {
            this.con = con;
            this.name = name;
            this.mail = mail;
            this.areasIds = areasIds;
            getInconsistencias();
            getWanrEntradas();
            getWanrSalidas();
            getNoRegistred();
            getWarnRevision();
            this.totalAlert = warnPuntualidad + warnOut + warnIn + noRegistrados + warnRevision;
        }

        private void getWanrEntradas() throws Exception {
            ///////////////WARNING ENTRADAAS /////////////////////////
            String qStr = "SELECT COUNT(*) "
                    + "FROM per_employee AS pe "
                    + "INNER JOIN per_gate_event AS ev ON ev.emp_id = pe.id "
                    + "INNER JOIN per_contract ON pe.id = per_contract.emp_id AND per_contract.active= 1 AND IF(per_contract.leave_date IS NULL, TRUE,ev.event_day <= per_contract.leave_date) AND per_contract.beg_date <= ev.event_day "
                    + "INNER JOIN per_pos AS pp ON pp.id = per_contract.pos_id "
                    + "INNER JOIN per_sbarea AS ps ON ps.id = pp.sarea_id "
                    + "WHERE DATE(ev.event_day) = DATE(SUBDATE(NOW(),INTERVAL 1 DAY)) "
                    + "AND ev.warning = 1 "
                    + "AND ps.area_id IN (" + areasIds + ") AND ev.type = 'in' ";
            Integer val = new MySQLQuery(qStr).getAsInteger(con);
            warnIn = val != null ? val : 0;
        }

        private void getWanrSalidas() throws Exception {
            ///////////////WARNING ENTRADAAS /////////////////////////
            String qStr = "SELECT COUNT(*) "
                    + "FROM per_employee AS pe "
                    + "INNER JOIN per_gate_event AS ev ON ev.emp_id = pe.id "
                    + "INNER JOIN per_contract ON pe.id = per_contract.emp_id AND per_contract.active= 1 AND IF(per_contract.leave_date IS NULL, TRUE,ev.event_day <= per_contract.leave_date) AND per_contract.beg_date <= ev.event_day "
                    + "INNER JOIN per_pos AS pp ON pp.id = per_contract.pos_id "
                    + "INNER JOIN per_sbarea AS ps ON ps.id = pp.sarea_id "
                    + "WHERE DATE(ev.event_day) = DATE(SUBDATE(NOW(),INTERVAL 1 DAY)) "
                    + "AND ev.warning = 1 "
                    + "AND ps.area_id IN (" + areasIds + ") AND ev.type = 'out' ";
            Integer val = new MySQLQuery(qStr).getAsInteger(con);
            warnOut = val != null ? val : 0;
        }

        private void getInconsistencias() throws Exception {
            ///////////////TOTAL INCONCISTENCIAS AttendPanel en el cliente////////
            String str = "SELECT COUNT(*) "
                    + "FROM per_gate_event ev "
                    + "INNER JOIN per_employee emp ON emp.id = ev.emp_id "
                    + "INNER JOIN per_contract ON emp.id = per_contract.emp_id AND per_contract.active= 1 AND IF(per_contract.leave_date IS NULL, TRUE,ev.event_day <= per_contract.leave_date) AND per_contract.beg_date <= ev.event_day "
                    + "INNER JOIN per_pos AS pp ON pp.id = per_contract.pos_id "
                    + "INNER JOIN per_sbarea AS ps ON ps.id = pp.sarea_id "
                    + "WHERE DATE(ev.event_day) = DATE(SUBDATE(NOW(),INTERVAL 1 DAY)) "
                    + "AND ((warning = 1 OR reg_hour IS NULL) AND checked = 0) "
                    + "AND ps.area_id IN (" + areasIds + ")";
            Integer val = new MySQLQuery(str).getAsInteger(con);
            warnPuntualidad = val != null ? val : 0;
        }

        private void getWarnRevision() throws Exception {
            ///////////////TOTAL en el cliente////////
            String str = "SELECT COUNT(*) "
                    + "FROM per_gate_event ev "
                    + "INNER JOIN per_employee emp ON emp.id = ev.emp_id "
                    + "INNER JOIN per_contract ON emp.id = per_contract.emp_id AND per_contract.active= 1 AND IF(per_contract.leave_date IS NULL, TRUE,ev.event_day <= per_contract.leave_date) AND per_contract.beg_date <= ev.event_day "
                    + "INNER JOIN per_pos AS pp ON pp.id = per_contract.pos_id "
                    + "INNER JOIN per_sbarea AS ps ON ps.id = pp.sarea_id "
                    + "WHERE DATE(ev.event_day) = DATE(SUBDATE(NOW(),INTERVAL 1 DAY)) "
                    + "AND ((warning = 1 OR reg_hour IS NULL) AND checked = 1) "
                    + "AND ps.area_id IN (" + areasIds + ")";
            Integer val = new MySQLQuery(str).getAsInteger(con);
            warnRevision = val != null ? val : 0;
        }

        private void getNoRegistred() throws Exception {
            //////////WARNING EVENTOS NO REGISTRADOS///////////////
            String str = "SELECT COUNT(*) "
                    + "FROM per_employee AS pe "
                    + "INNER JOIN per_gate_event AS ev ON ev.emp_id = pe.id "
                    + "INNER JOIN per_contract ON pe.id = per_contract.emp_id AND per_contract.active = 1 AND IF(per_contract.leave_date IS NULL, TRUE,ev.event_day <= per_contract.leave_date) AND per_contract.beg_date <= ev.event_day "
                    + "INNER JOIN per_pos AS pp ON pp.id = per_contract.pos_id "
                    + "INNER JOIN per_sbarea AS ps ON ps.id = pp.sarea_id "
                    + "WHERE DATE(ev.event_day) = DATE(SUBDATE(NOW(),INTERVAL 1 DAY)) "
                    + "AND ev.reg_hour IS NULL "
                    + "AND ps.area_id IN (" + areasIds + ") ";
            Integer val = new MySQLQuery(str).getAsInteger(con);
            noRegistrados = val != null ? val : 0;
        }

    }
}
