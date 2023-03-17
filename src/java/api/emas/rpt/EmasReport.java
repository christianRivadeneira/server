package api.emas.rpt;

import java.sql.Connection;
import utilities.MySQLQuery;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.Table;

public class EmasReport {

    public static MySQLReport getExportSedes(Connection conn) throws Exception {
        String str = "SELECT GROUP_CONCAT(DISTINCT cs.contract), s.`name`, s.contact, s.address, s.neigh, dp.name, s.phone, s.type, s.frec_type, s.intensity, "
                + "(SELECT GROUP_CONCAT(DISTINCT fp.week) FROM emas_sede_frec_path fp WHERE fp.sede_id = s.id), "
                + "(SELECT GROUP_CONCAT(DISTINCT UPPER(fp.day)) FROM emas_sede_frec_path fp WHERE fp.sede_id = s.id), "
                + "IF(s.active, 'ACTIVA', 'INACTIVO') "
                + "FROM emas_clie_sede s "
                + "INNER JOIN emas_client c ON s.client_id = c.id "
                + "INNER JOIN dane_poblado dp ON dp.id = c.dane_pob_id "
                + "LEFT JOIN emas_clie_service cs ON cs.clie_sede_id = s.id "
                + "GROUP BY s.id;";

        MySQLQuery q = new MySQLQuery(str);
        Object[][] data = q.getRecords(conn);

        MySQLReport rep = new MySQLReport("Reporte Exportar Sedes", "", "informacion sede", MySQLQuery.now(conn));

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.ENUM, MySQLReportWriter.LEFT, getEnumOptions("type")));//1 
        rep.getFormats().add(new CellFormat(MySQLReportWriter.ENUM, MySQLReportWriter.LEFT, getEnumOptions("frec_type3")));//2 

        rep.setZoomFactor(85);
        rep.setShowNumbers(true);
        rep.getFormats().get(0).setWrap(true);
        rep.setVerticalFreeze(5);
        rep.setMultiRowTitles(true);
        Table tb = new Table("Sedes");
        tb.getColumns().add(new Column("CONTRATO", 15, 0));
        tb.getColumns().add(new Column("Nombre sede", 40, 0));
        tb.getColumns().add(new Column("contacto sede", 40, 0));
        tb.getColumns().add(new Column("direccion sede", 25, 0));
        tb.getColumns().add(new Column("Barrio sede", 25, 0));
        tb.getColumns().add(new Column("Poblado sede", 25, 0));
        tb.getColumns().add(new Column("telefono sede", 25, 0));
        tb.getColumns().add(new Column("GENERADOR (pequeño, grande, paciente, eventual, peluqieria)", 25, 1));
        tb.getColumns().add(new Column("periodo (mensual semanal)", 25, 2));
        tb.getColumns().add(new Column("Atencion(numero de veces)", 10, 0));
        tb.getColumns().add(new Column("Frecuencia semanas", 15, 0));
        tb.getColumns().add(new Column("Frecuencia dias", 15, 0));
        tb.getColumns().add(new Column("sede activa o inactiva", 15, 0));

        tb.setData(data);
        if (tb.getData().length > 0) {
            rep.getTables().add(tb);
        }
        return rep;
    }

    public static MySQLReport getExportClients(Connection conn) throws Exception {
        String str = "SELECT GROUP_CONCAT(DISTINCT cs.contract), c.renovation_date, c.notes "
                + "FROM emas_clie_sede s "
                + "INNER JOIN emas_client c ON s.client_id = c.id "
                + "INNER JOIN emas_clie_service cs ON cs.clie_sede_id = s.id "
                + "GROUP BY s.id;";

        MySQLQuery q = new MySQLQuery(str);
        Object[][] data = q.getRecords(conn);

        MySQLReport rep = new MySQLReport("Reporte Exportar Clientes", "", "Informacion cliente", MySQLQuery.now(conn));

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//1  

        rep.setZoomFactor(85);
        rep.setShowNumbers(true);
        rep.getFormats().get(0).setWrap(true);
        rep.setVerticalFreeze(5);
        rep.setMultiRowTitles(true);
        Table tb = new Table("Clientes");
        tb.getColumns().add(new Column("contrato", 15, 0));
        tb.getColumns().add(new Column("fecha renovacion", 15, 1));
        tb.getColumns().add(new Column("Notas (observaciones)", 60, 0));

        tb.setData(data);
        if (tb.getData().length > 0) {
            rep.getTables().add(tb);
        }
        return rep;
    }
    
    public static String getEnumOptions(String fieldName) {
        if (fieldName.equals("frec_type")) {
            return "week=Semanal&biweek=Quincenal&month=Mensual";
        }
        if (fieldName.equals("frec_type2")) {
            return "biweek=Quincenal&month=Mensual";
        }
        if (fieldName.equals("frec_type3")) {
            return "week=Semanal&month=Mensual";
        }
        if (fieldName.equals("type")) {
            return "small=Pequeño&big=Grande&pac=Paciente&eve=Eventual&hair=Peluquería";
        }
        return null;
    }

}
