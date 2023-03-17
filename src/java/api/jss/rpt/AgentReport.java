package api.jss.rpt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import utilities.Dates;
import utilities.MySQLQuery;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.HeaderColumn;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.Table;
import utilities.mysqlReport.TableHeader;

public class AgentReport {

    public static MySQLReport getRptAverageTime(Date begin, Date end, Integer clientId, Integer agentId, Integer greater, Integer smaller, Connection conn) throws Exception {

        SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");

        MySQLQuery qClients = new MySQLQuery("SELECT c.id, c.document, c.acc, c.name, z.name "
                + "FROM jss_alarm_client c "
                + "INNER JOIN jss_client_zone cz ON cz.client_id = c.id "
                + "INNER JOIN jss_zone z ON z.id = cz.zone_id "
                + "WHERE TRUE "
                + (clientId != null ? "AND c.id = ?1 " : ""));
        MySQLQuery qAgents = new MySQLQuery("SELECT g.id, CONCAT(g.first_name, ' ', g.last_name) AS agent FROM mss_guard g WHERE g.`type` = 'agent' "
                + (agentId != null ? "AND g.id = ?1 " : ""));
        if (clientId != null) {
            qClients.setParam(1, clientId);
        }
        if (agentId != null) {
            qAgents.setParam(1, agentId);
        }

        Object[][] dataClients = qClients.getRecords(conn);
        Object[][] dataAgents = qAgents.getRecords(conn);
        int lenRow = 4 + (dataAgents.length * 3);
        List<Object[]> data = new ArrayList<>();

        for (int i = 0; i < dataClients.length; i++) {
            Object[] rowClient = dataClients[i];

            Object[] rowData = new Object[lenRow];
            rowData[0] = rowClient[1];
            rowData[1] = rowClient[2];
            rowData[2] = rowClient[3];
            rowData[3] = rowClient[4];
            int ind = 0;
            boolean add = false;
            for (int j = 0; j < dataAgents.length; j++) {
                Object[] rowAgent = dataAgents[j];
                String str = "SELECT COUNT(*) AS visit, "
                        + "z.goal, "
                        + "SUM(TIMESTAMPDIFF(MINUTE, v.beg_dt, v.end_dt)) "
                        + "FROM jss_visit v "
                        + "INNER JOIN jss_client_zone cz ON cz.client_id = v.client_id "
                        + "INNER JOIN jss_zone z ON z.id = cz.zone_id "
                        + "WHERE v.end_dt IS NOT NULL AND v.beg_dt BETWEEN ?1 AND ?2 AND v.client_id = ?3 AND v.agent_id = ?4 "
                        + (greater != null ? "AND TIMESTAMPDIFF(MINUTE, v.beg_dt, v.end_dt) >= ?5" : "")
                        + (smaller != null ? "AND TIMESTAMPDIFF(MINUTE, v.beg_dt, v.end_dt) < ?6" : "");

                MySQLQuery q = new MySQLQuery(str);
                q.setParam(1, Dates.getMinHours(begin));
                q.setParam(2, Dates.getMaxHours(end));
                q.setParam(3, MySQLQuery.getAsInteger(rowClient[0]));
                q.setParam(4, MySQLQuery.getAsInteger(rowAgent[0]));
                if (greater != null) {
                    q.setParam(5, greater);
                }
                if (smaller != null) {
                    q.setParam(6, smaller);
                }

                Object[] values = q.getRecord(conn);
                Integer numVisits = MySQLQuery.getAsInteger(values[0]);
                BigDecimal numMinutes = MySQLQuery.getAsBigDecimal(values[2], true);
                rowData[4 + ind] = numVisits;
                rowData[5 + ind] = MySQLQuery.getAsInteger(values[1]);
                if (numVisits != 0 && numVisits > 0) {
                    rowData[6 + ind] = numMinutes.divide(new BigDecimal(numVisits), 2, RoundingMode.HALF_UP);
                }
                ind = ind + 3;
                if (numVisits > 0) {
                    add = true;
                }
            }
            if (add) {
                data.add(rowData);
            }
        }

        MySQLReport rep = new MySQLReport("Reporte Tiempo Promedio Agentes", "", "Tiempo Promedio Agentes", MySQLQuery.now(conn));
        rep.getSubTitles().add("Periodo " + dt.format(begin) + " - " + dt.format(end));

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "##0.00"));//2

        rep.setMultiRowTitles(true);
        rep.setZoomFactor(85);
        rep.setShowNumbers(true);
        rep.getFormats().get(0).setWrap(true);
        rep.setVerticalFreeze(6);
        Table tb = new Table("Promedio Horas Agentes");
        tb.getColumns().add(new Column("Documento", 20, 0));
        tb.getColumns().add(new Column("Código", 15, 0));
        tb.getColumns().add(new Column("Nombre", 40, 0));
        tb.getColumns().add(new Column("Zona", 30, 0));
        for (Object[] rowAgent : dataAgents) {
            tb.getColumns().add(new Column("Visitas Ejec", 15, 1));
            tb.getColumns().add(new Column("Visitas Mes Estimadas", 15, 1));
            tb.getColumns().add(new Column("Promedio Minutos", 15, 2));
        }

        TableHeader header = new TableHeader();
        tb.getHeaders().add(header);
        header.getColums().add(new HeaderColumn("Cliente", 4, 1));
        for (Object[] rowAgent : dataAgents) {
            header.getColums().add(new HeaderColumn(MySQLQuery.getAsString(rowAgent[1]), 3, 1));
        }

        tb.setData(data);
        if (tb.getData().length > 0) {
            rep.getTables().add(tb);
        }
        return rep;
    }

    public static MySQLReport getRptVisitsZones(Date begin, Date end, Integer zoneId, Integer agentId, Connection conn) throws Exception {

        SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");

        MySQLQuery qZones = new MySQLQuery("SELECT z.id, z.name, z.goal FROM jss_zone z WHERE TRUE "
                + (zoneId != null ? "AND z.id = ?1 " : ""));
        if (zoneId != null) {
            qZones.setParam(1, zoneId);
        }

        Object[][] dataZones = qZones.getRecords(conn);

        MySQLReport rep = new MySQLReport("Reporte Visitas Agentes por Zona", "", "Visitas Agentes por Zona", MySQLQuery.now(conn));
        rep.getSubTitles().add("Periodo " + dt.format(begin) + " - " + dt.format(end));

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "HH:mm:ss"));//3

        rep.setZoomFactor(85);
        rep.setShowNumbers(true);
        rep.getFormats().get(0).setWrap(true);
        rep.setVerticalFreeze(4);

        for (Object[] rowZone : dataZones) {
            Table tb = new Table(MySQLQuery.getAsString(rowZone[1]));
            tb.getColumns().add(new Column("Código", 20, 0));
            tb.getColumns().add(new Column("Documento", 20, 0));
            tb.getColumns().add(new Column("Cliente", 40, 0));
            tb.getColumns().add(new Column("Agente", 40, 0));
            tb.getColumns().add(new Column("Fecha", 15, 2));
            tb.getColumns().add(new Column("Llegada", 15, 3));
            tb.getColumns().add(new Column("Salida", 15, 3));
            tb.getColumns().add(new Column("Tiene Foto", 15, 0));
            tb.getColumns().add(new Column("Notas", 40, 0));

            String str = "SELECT c.acc, c.document, c.name, CONCAT(ag.first_name, ' ', ag.last_name), v.beg_dt, v.beg_dt, v.end_dt, "
                    + "(SELECT IF(COUNT(*) > 0, 'Si', 'No') FROM bfile b WHERE b.`table` = 'jss_visit' AND b.owner_id = v.id),"
                    + "v.notes "
                    + "FROM jss_visit v "
                    + "INNER JOIN jss_alarm_client c ON c.id = v.client_id "
                    + "INNER JOIN jss_agent_zone az ON az.span_id = v.span_id AND az.agent_id = v.agent_id "
                    + "INNER JOIN mss_guard ag ON ag.id = v.agent_id "
                    + "WHERE v.beg_dt BETWEEN ?1 AND ?2 AND az.zone_id = ?3 "
                    + (agentId != null ? "AND v.agent_id = ?4 " : "");

            MySQLQuery q = new MySQLQuery(str);
            q.setParam(1, Dates.getMinHours(begin));
            q.setParam(2, Dates.getMaxHours(end));
            q.setParam(3, MySQLQuery.getAsInteger(rowZone[0]));
            if (agentId != null) {
                q.setParam(4, agentId);
            }

            tb.setData(q.getRecords(conn));
            if (tb.getData() != null && tb.getData().length > 0) {
                rep.getTables().add(tb);
            }
        }

        return rep;
    }

    public static MySQLReport getRptCompVisitsZones(Date begin, Date end, Integer zoneId, Integer agentId, Connection conn) throws Exception {

        SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");

        MySQLQuery qZones = new MySQLQuery("SELECT z.id, z.name, z.goal FROM jss_zone z WHERE TRUE "
                + (zoneId != null ? "AND z.id = ?1 " : ""));
        MySQLQuery qAgents = new MySQLQuery("SELECT g.id, CONCAT(g.first_name, ' ', g.last_name) AS agent FROM mss_guard g WHERE g.`type` = 'agent' "
                + (agentId != null ? "AND g.id = ?1 " : ""));
        if (zoneId != null) {
            qZones.setParam(1, zoneId);
        }
        if (agentId != null) {
            qAgents.setParam(1, agentId);
        }

        Object[][] dataZones = qZones.getRecords(conn);
        Object[][] dataAgents = qAgents.getRecords(conn);
        int lenRow = 2 + dataAgents.length;
        Object[][] data = new Object[dataZones.length][lenRow];

        for (int i = 0; i < dataZones.length; i++) {
            Object[] rowZone = dataZones[i];

            Object[] rowData = new Object[lenRow];
            rowData[0] = rowZone[1];
            rowData[1] = rowZone[2];
            for (int j = 0; j < dataAgents.length; j++) {
                Object[] rowAgent = dataAgents[j];
                String str = "SELECT COUNT(*) visits "
                        + "FROM jss_visit v "
                        + "INNER JOIN jss_agent_zone az ON az.agent_id = v.agent_id AND az.span_id = v.span_id "
                        + "WHERE v.beg_dt BETWEEN ?1 AND ?2 AND az.zone_id = ?3 AND v.agent_id = ?4";

                MySQLQuery q = new MySQLQuery(str);
                q.setParam(1, Dates.getMinHours(begin));
                q.setParam(2, Dates.getMaxHours(end));
                q.setParam(3, MySQLQuery.getAsInteger(rowZone[0]));
                q.setParam(4, MySQLQuery.getAsInteger(rowAgent[0]));

                rowData[2 + j] = q.getAsInteger(conn);
            }
            data[i] = rowData;
        }

        MySQLReport rep = new MySQLReport("Reporte Comparación Visitas entre Zonas", "", "Comparación Visitas entre Zonas", MySQLQuery.now(conn));
        rep.getSubTitles().add("Periodo " + dt.format(begin) + " - " + dt.format(end));

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//1

        rep.setZoomFactor(85);
        rep.setShowNumbers(true);
        rep.getFormats().get(0).setWrap(true);
        rep.setVerticalFreeze(6);
        Table tb = new Table("Visitas");
        tb.getColumns().add(new Column("Nombre", 40, 0));
        tb.getColumns().add(new Column("Meta", 15, 1));
        for (Object[] rowAgent : dataAgents) {
            tb.getColumns().add(new Column(MySQLQuery.getAsString(rowAgent[1]), 25, 1));
        }

        TableHeader header = new TableHeader();
        tb.getHeaders().add(header);
        header.getColums().add(new HeaderColumn("Zona", 2, 1));
        header.getColums().add(new HeaderColumn("Agentes", dataAgents.length, 1));

        tb.setData(data);
        if (tb.getData().length > 0) {
            rep.getTables().add(tb);
        }
        return rep;
    }

    public static MySQLReport getRptOneMinute(Date begin, Date end, Integer clientId, Integer agentId, Integer greater, Integer smaller, Connection conn) throws Exception {

        SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");

        MySQLQuery q = new MySQLQuery("SELECT c.acc, c.document, c.name, CONCAT(ag.first_name, ' ', ag.last_name), v.beg_dt, v.end_dt, TIMESTAMPDIFF(MINUTE, v.beg_dt, v.end_dt) "
                + "FROM jss_visit v "
                + "INNER JOIN mss_guard ag ON ag.id = v.agent_id "
                + "INNER JOIN jss_alarm_client c ON c.id = v.client_id "
                + "WHERE v.beg_dt BETWEEN ?1 AND ?2 "
                + (clientId != null ? "AND c.id = ?3 " : "")
                + (agentId != null ? "AND ag.id = ?4 " : "")
                + (greater != null ? "AND TIMESTAMPDIFF(MINUTE, v.beg_dt, v.end_dt) >= ?5" : "")
                + (smaller != null ? "AND (TIMESTAMPDIFF(MINUTE, v.beg_dt, v.end_dt) < ?6 OR v.end_dt IS NULL)" : ""));
        q.setParam(1, Dates.getMinHours(begin));
        q.setParam(2, Dates.getMaxHours(end));
        if (clientId != null) {
            q.setParam(3, clientId);
        }
        if (agentId != null) {
            q.setParam(4, agentId);
        }
        if (greater != null) {
            q.setParam(5, greater);
        }
        if (smaller != null) {
            q.setParam(6, smaller);
        }
        Object[][] data = q.getRecords(conn);

        MySQLReport rep = new MySQLReport("Reporte Visitas Detalladas de Agentes", "", "Visitas Detalladas de Agentes", MySQLQuery.now(conn));
        rep.getSubTitles().add("Periodo " + dt.format(begin) + " - " + dt.format(end));

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy HH:mm:ss"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//2

        rep.setZoomFactor(85);
        rep.setShowNumbers(true);
        rep.getFormats().get(0).setWrap(true);
        rep.setVerticalFreeze(5);
        Table tb = new Table("Visitas Detalladas de Agentes");
        tb.getColumns().add(new Column("Código", 20, 0));
        tb.getColumns().add(new Column("Documento", 20, 0));
        tb.getColumns().add(new Column("Cliente", 45, 0));
        tb.getColumns().add(new Column("Agente", 45, 0));
        tb.getColumns().add(new Column("Fecha Inicio", 20, 1));
        tb.getColumns().add(new Column("Fecha Fin", 20, 1));
        tb.getColumns().add(new Column("Minutos", 15, 2));
        tb.setData(data);
        if (tb.getData().length > 0) {
            rep.getTables().add(tb);
        }
        return rep;
    }

    public static MySQLReport getRptVisits(Date begin, Date end, Integer zoneId, Connection conn) throws Exception {
        SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");
        MySQLReport rep = new MySQLReport("Reporte Visitas", "", "Visitas Agentes por Zona", MySQLQuery.now(conn));
        rep.getSubTitles().add("Periodo " + dt.format(begin) + " - " + dt.format(end));

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//1

        rep.setZoomFactor(85);
        rep.setShowNumbers(true);
        rep.getFormats().get(0).setWrap(true);
        rep.setVerticalFreeze(4);

        Table tb = new Table("Reporte Visitas");
        tb.getColumns().add(new Column("Código", 20, 0));
        tb.getColumns().add(new Column("Documento", 20, 0));
        tb.getColumns().add(new Column("Cliente", 40, 0));
        tb.getColumns().add(new Column("Zona", 40, 0));
        tb.getColumns().add(new Column("Visitas", 15, 1));

        String str = "SELECT c.acc, c.document, c.name, z.name, "
                + "(SELECT COUNT(*) FROM jss_visit v WHERE v.client_id = c.id AND v.beg_dt BETWEEN ?1 AND ?2) "
                + "FROM jss_alarm_client c "
                + "LEFT JOIN jss_client_zone cz ON cz.client_id = c.id "
                + "LEFT JOIN jss_zone z ON z.id = cz.zone_id "
                + "WHERE 1 = 1 "
                + (zoneId != null ? "AND z.id = ?3 " : "")
                + "ORDER BY c.name";

        MySQLQuery q = new MySQLQuery(str);
        q.setParam(1, Dates.getMinHours(begin));
        q.setParam(2, Dates.getMaxHours(end));
        if (zoneId != null) {
            q.setParam(3, zoneId);
        }

        tb.setData(q.getRecords(conn));
        if (tb.getData() != null && tb.getData().length > 0) {
            rep.getTables().add(tb);
        }

        return rep;
    }

}
