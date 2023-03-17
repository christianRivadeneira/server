package api.mss.rpt;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import utilities.Dates;
import utilities.MySQLQuery;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.Table;

public class MinutesReport {

    public static MySQLReport getDetailArrivals(Date begin, Date end, Integer clientId, Integer postId, Integer guardId, Integer type, Connection conn) throws Exception {

        SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");
        String str = "SELECT "
                + "c.name, p.name, "
                + "CONCAT(g.first_name, ' ', g.last_name), "
                + "s.exp_beg, s.reg_beg, "
                + "s.exp_end, s.reg_end, "
                + " IF( "
                + "(s.exp_beg < NOW() AND s.reg_beg IS NULL) OR "
                + "(s.exp_end < NOW() AND s.reg_end IS NULL) OR "
                + "(TO_SECONDS(s.reg_end) - TO_SECONDS(s.exp_end)) < 0 OR "
                + "(TO_SECONDS(s.reg_beg) NOT BETWEEN (TO_SECONDS(DATE_SUB( DATE_SUB(s.exp_beg, INTERVAL (s.anticipation *60) SECOND) , INTERVAL (s.in_tolerance * 60) SECOND))) AND "
                + "(TO_SECONDS(DATE_ADD( DATE_SUB(s.exp_beg, INTERVAL (s.anticipation *60) SECOND) , INTERVAL (s.in_tolerance * 60) SECOND)))), 'Irregularidad', 'A Tiempo') "
                + "FROM mss_shift s "
                + "INNER JOIN mss_post p ON p.id = s.post_id " + (postId != null ? "AND p.id = ?4 " : "")
                + "INNER JOIN mss_client c ON c.id = p.client_id " + (clientId != null ? "AND c.id = ?3 " : "")
                + "INNER JOIN mss_guard g ON g.id = s.guard_id " + (guardId != null ? "AND g.id = ?5 " : "")
                + "WHERE "
                + "s.rev_dt IS NULL AND p.begin_dt <= DATE(s.exp_beg) AND p.active AND s.active AND s.exp_beg BETWEEN ?1 AND ?2 "
                + (type == 2 || type == 3 ? " AND IF( "
                + "(s.exp_beg < NOW() AND s.reg_beg IS NULL) OR "
                + "(s.exp_end < NOW() AND s.reg_end IS NULL) OR "
                + "(TO_SECONDS(s.reg_end) - TO_SECONDS(s.exp_end)) < 0 OR "
                + "(TO_SECONDS(s.reg_beg) NOT BETWEEN (TO_SECONDS(DATE_SUB( DATE_SUB(s.exp_beg, INTERVAL (s.anticipation *60) SECOND) , INTERVAL (s.in_tolerance * 60) SECOND))) AND "
                + "(TO_SECONDS(DATE_ADD( DATE_SUB(s.exp_beg, INTERVAL (s.anticipation *60) SECOND) , INTERVAL (s.in_tolerance * 60) SECOND)))), 1, 0) = " + (type == 2 ? "1" : "0") : "");

        MySQLQuery q = new MySQLQuery(str);
        q.setParam(1, Dates.getMinHours(begin));
        q.setParam(2, Dates.getMaxHours(end));
        if (clientId != null) {
            q.setParam(3, clientId);
        }
        if (postId != null) {
            q.setParam(4, postId);
        }
        if (guardId != null) {
            q.setParam(5, guardId);
        }
        Object[][] data = q.getRecords(conn);

        MySQLReport rep = new MySQLReport("Reporte Detallado de Marcación", "", "Marcación", MySQLQuery.now(conn));
        rep.getSubTitles().add("Periodo " + dt.format(begin) + " - " + dt.format(end));

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy hh:mm a"));//1  

        rep.setZoomFactor(85);
        rep.setShowNumbers(true);
        rep.getFormats().get(0).setWrap(true);
        rep.setVerticalFreeze(5);
        Table tb = new Table("Marcación");
        tb.getColumns().add(new Column("Cliente", 35, 0));
        tb.getColumns().add(new Column("Puesto", 40, 0));
        tb.getColumns().add(new Column("Guarda", 35, 0));
        tb.getColumns().add(new Column("Inicio Esperado", 25, 1));
        tb.getColumns().add(new Column("Inicio Registrado", 25, 1));
        tb.getColumns().add(new Column("Fin Esperado", 25, 1));
        tb.getColumns().add(new Column("Fin Registrado", 25, 1));
        tb.getColumns().add(new Column("Tipo", 25, 0));

        tb.setData(data);
        if (tb.getData().length > 0) {
            rep.getTables().add(tb);
        }
        return rep;
    }

    public static MySQLReport getShiftNolvelty(Date begin, Date end, Integer clientId, Integer postId, Integer state, Connection conn) throws Exception {
        SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");
        String str = "SELECT c.name, p.name, CONCAT(g.first_name, ' ', g.last_name), si.reg_dt, sit.name, si.notes, si.close_dt, si.close_notes "
                + "FROM mss_shift s "
                + "INNER JOIN mss_shift_incident si ON si.shift_id = s.id "
                + "INNER JOIN mss_shift_incident_type sit ON sit.id = si.shift_incident_type_id "
                + "INNER JOIN mss_post p ON p.id = s.post_id " + (postId != null ? "AND p.id = ?4 " : "")
                + "INNER JOIN mss_guard g ON g.id = s.guard_id "
                + "INNER JOIN mss_client c ON c.id = p.client_id " + (clientId != null ? "AND c.id = ?3 " : "")
                + "WHERE si.reg_dt BETWEEN ?1 AND ?2 "
                + ((state == null || state == 3) ? " " : (state == 1 ? "AND si.close_dt IS NOT NULL " : "AND si.close_dt IS NULL "));//state (1 = Cerradas), (2 = Abiertas), 

        MySQLQuery q = new MySQLQuery(str);
        q.setParam(1, Dates.getMinHours(begin));
        q.setParam(2, Dates.getMaxHours(end));

        if (clientId != null) {
            q.setParam(3, clientId);
        }
        if (postId != null) {
            q.setParam(4, postId);
        }
        Object[][] data = q.getRecords(conn);

        MySQLReport rep = new MySQLReport("Reporte Novedades de Turno", "", "Novedades", MySQLQuery.now(conn));
        rep.getSubTitles().add("Periodo " + dt.format(begin) + " - " + dt.format(end));

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy hh:mm a"));//1 

        rep.setZoomFactor(85);
        rep.setShowNumbers(true);
        rep.getFormats().get(0).setWrap(true);
        rep.setVerticalFreeze(5);
        Table tb = new Table("Novedades de Turno");
        tb.getColumns().add(new Column("Cliente", 35, 0));
        tb.getColumns().add(new Column("Puesto", 35, 0));
        tb.getColumns().add(new Column("Guarda", 35, 0));
        tb.getColumns().add(new Column("Fecha", 25, 1));
        tb.getColumns().add(new Column("Tipo de Novedad", 30, 0));
        tb.getColumns().add(new Column("Novedad", 30, 0));
        tb.getColumns().add(new Column("Fecha de Cierre", 25, 1));
        tb.getColumns().add(new Column("Notas de Cierre", 40, 0));

        tb.setData(data);
        if (tb.getData().length > 0) {
            rep.getTables().add(tb);
        }
        return rep;
    }

    public static MySQLReport getSuperReviewFinding(Date begin, Date end, Integer guardId, Integer findingId, Integer clientId, Integer postId, Connection conn) throws Exception {
        SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");
        String str = "SELECT r.reg_date, CONCAT(gu.first_name, ' ', gu.last_name) AS guar, p.name, IF(r.feedback IS NULL, 'No', 'Si') AS fb, CONCAT(sup.first_name, ' ', sup.last_name) AS supe, r.notes, "
                + "CONCAT(' - ', GROUP_CONCAT(f.name SEPARATOR '\\n - ')) "
                + "FROM mss_super_review r "
                + "INNER JOIN mss_super_review_checklist c ON c.review_id = r.id "
                + "INNER JOIN mss_super_review_finding f ON f.id = c.finding_id " + (findingId != null ? "AND f.id = ?4 " : "")
                + "INNER JOIN mss_guard sup ON r.super_id = sup.id "
                + "INNER JOIN mss_guard gu ON gu.id = r.guard_id " + (guardId != null ? "AND gu.id = ?3 " : "")
                + "INNER JOIN mss_post p ON p.id = r.post_id "
                + "WHERE r.reg_date BETWEEN ?1 AND ?2 "
                + (clientId != null ? " AND p.client_id = ?5 " : "")
                + (postId != null ? " AND p.id = ?6 " : "")
                + "GROUP BY r.id ";

        MySQLQuery q = new MySQLQuery(str);
        q.setParam(1, Dates.getMinHours(begin));
        q.setParam(2, Dates.getMaxHours(end));
        if (guardId != null) {
            q.setParam(3, guardId);
        }
        if (findingId != null) {
            q.setParam(4, findingId);
        }
        if (clientId != null) {
            q.setParam(5, clientId);
        }
        if (postId != null) {
            q.setParam(6, postId);
        }
        Object[][] data = q.getRecords(conn);

        MySQLReport rep = new MySQLReport("Reporte Irregularidades en Supervisión", "", "Irregularidades", MySQLQuery.now(conn));
        rep.getSubTitles().add("Periodo " + dt.format(begin) + " - " + dt.format(end));

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy hh:mm a"));//1

        rep.setZoomFactor(85);
        rep.setShowNumbers(true);
        rep.getFormats().get(0).setWrap(true);
        rep.setVerticalFreeze(5);
        Table tb = new Table("Irregularidades en Supervisión");
        tb.getColumns().add(new Column("Fecha", 25, 1));
        tb.getColumns().add(new Column("Guarda", 35, 0));
        tb.getColumns().add(new Column("Puesto", 55, 0));
        tb.getColumns().add(new Column("Retroalimentacion", 20, 0));
        tb.getColumns().add(new Column("Supervisor", 35, 0));
        tb.getColumns().add(new Column("Notas", 40, 0));
        tb.getColumns().add(new Column("Irregularidades", 35, 0));

        tb.setData(data);
        if (tb.getData().length > 0) {
            rep.getTables().add(tb);
        }
        return rep;
    }

    public static MySQLReport getSuperProgCompliance(Date begin, Date end, Integer superId, Integer state, Integer clientId, Integer postId, Connection conn) throws Exception {
        SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");
        String str
                = "SELECT prog.beg_dt, CONCAT(sup.first_name, ' ', sup.last_name) AS sup, p.name, IF(prog.path_id IS NULL, 'Eventual', 'Ruta') AS fb, "
                + "arrival_dt, CONCAT(gu.first_name, ' ', gu.last_name) AS guar, IF(r.signed = 1 , 'Si', IF(r.signed = 0, 'No',null)) "
                + "FROM mss_super_prog prog "
                + "INNER JOIN mss_guard sup ON sup.id = prog.super_id " + (superId != null ? "AND sup.id = ?3 " : "")
                + "INNER JOIN mss_post p ON p.id = prog.post_id "
                + "LEFT JOIN mss_super_review r ON r.prog_id = prog.id "
                + "LEFT JOIN mss_guard gu ON gu.id = r.guard_id "
                + "WHERE prog.beg_dt BETWEEN ?1 AND ?2 "
                + (state == null || state == 3 ? " " : (state == 1 ? "AND prog.arrival_dt IS NOT NULL " : "AND prog.arrival_dt IS NULL "))//state (1 = visitadas), (2 = sin visitar), 
                + (clientId != null ? " AND p.client_id = ?4 " : "")
                + (postId != null ? " AND p.id = ?5 " : "")
                + "ORDER BY prog.beg_dt ";

        MySQLQuery q = new MySQLQuery(str);
        q.setParam(1, Dates.getMinHours(begin));
        q.setParam(2, Dates.getMaxHours(end));
        if (superId != null) {
            q.setParam(3, superId);
        }
        if (clientId != null) {
            q.setParam(4, clientId);
        }
        if (postId != null) {
            q.setParam(5, postId);
        }

        Object[][] data = q.getRecords(conn);

        MySQLReport rep = new MySQLReport("Reporte Cumplimiento en Supervisiones", "", "Programación", MySQLQuery.now(conn));
        rep.getSubTitles().add("Periodo " + dt.format(begin) + " - " + dt.format(end));

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy hh:mm a"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//2

        rep.setZoomFactor(85);
        rep.setShowNumbers(true);
        rep.getFormats().get(0).setWrap(true);
        rep.setVerticalFreeze(5);
        Table tb = new Table("Cumplimiento en Supervisiones");
        tb.getColumns().add(new Column("Fecha", 15, 2));
        tb.getColumns().add(new Column("Supervisor", 30, 0));
        tb.getColumns().add(new Column("Puesto", 55, 0));
        tb.getColumns().add(new Column("Tipo", 15, 0));
        tb.getColumns().add(new Column("Llegada", 25, 1));
        tb.getColumns().add(new Column("Guarda", 30, 0));
        tb.getColumns().add(new Column("Firma", 10, 0));

        tb.setData(data);
        if (tb.getData().length > 0) {
            rep.getTables().add(tb);
        }
        return rep;
    }

    public static MySQLReport getFailRounds(Date begin, Date end, Integer clientId, Integer postId, Integer guardId, Integer state, Connection conn) throws Exception {

        SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");
        String str = "SELECT "
                + "c.name, p.name, CONCAT(g.first_name, ' ', g.last_name), prog.name, r.reg_dt, r.beg_dt, r.end_dt, r.tolerance, r.close_dt, r.close_notes "
                + "FROM mss_round r "
                + "INNER JOIN mss_round_prog prog ON prog.id = r.round_prog_id "
                + "INNER JOIN mss_post p ON p.id = prog.post_id " + (postId != null ? "AND p.id = ?4 " : "")
                + "INNER JOIN mss_client c ON c.id = p.client_id " + (clientId != null ? "AND c.id = ?3 " : "")
                + "INNER JOIN mss_guard g ON g.id = r.guard_id " + (guardId != null ? "AND g.id = ?5 " : "")
                + "LEFT JOIN mss_round_point rp ON rp.round_id = r.id "
                + "WHERE "
                + "(r.end_dt IS NULL "
                + "OR ((SELECT COUNT(*)>0 FROM mss_round_point rpaux WHERE rpaux.round_id = r.id AND rpaux.dt IS NOT NULL) = 0) "
                + "AND (TRUE = IF(r.tolerance IS NOT NULL AND r.tolerance > 0 , (SELECT TIMESTAMPDIFF(MINUTE, r.reg_dt,r.end_dt)) > r.tolerance, 0))) "
                + "AND r.reg_dt BETWEEN ?1 AND ?2 "
                + (state == null || state == 3 ? " " : (state == 1 ? "AND r.close_dt IS NOT NULL " : "AND r.close_dt IS NULL "))//state (1 = Cerradas), (2 = Abiertas), 
                + "GROUP BY r.id ";

        MySQLQuery q = new MySQLQuery(str);
        q.setParam(1, Dates.getMinHours(begin));
        q.setParam(2, Dates.getMaxHours(end));
        if (clientId != null) {
            q.setParam(3, clientId);
        }
        if (postId != null) {
            q.setParam(4, postId);
        }
        if (guardId != null) {
            q.setParam(5, guardId);
        }
        Object[][] data = q.getRecords(conn);

        MySQLReport rep = new MySQLReport("Reporte Rondas sin Cumplir", "", "Rondas", MySQLQuery.now(conn));
        rep.getSubTitles().add("Periodo " + dt.format(begin) + " - " + dt.format(end));

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy hh:mm a"));//1

        rep.setZoomFactor(85);
        rep.setShowNumbers(true);
        rep.getFormats().get(0).setWrap(true);
        rep.setVerticalFreeze(5);
        Table tb = new Table("Rondas sin Cumplir");
        tb.getColumns().add(new Column("Cliente", 35, 0));
        tb.getColumns().add(new Column("Puesto", 40, 0));
        tb.getColumns().add(new Column("Guarda", 35, 0));
        tb.getColumns().add(new Column("Ronda", 35, 0));
        tb.getColumns().add(new Column("Fecha", 25, 1));
        tb.getColumns().add(new Column("Inicio", 25, 1));
        tb.getColumns().add(new Column("Fin", 25, 1));
        tb.getColumns().add(new Column("Tolerancia", 15, 0));
        tb.getColumns().add(new Column("Fecha Cierre", 25, 1));
        tb.getColumns().add(new Column("Notas Cierre", 40, 0));

        tb.setData(data);
        if (tb.getData().length > 0) {
            rep.getTables().add(tb);
        }
        return rep;
    }
}
