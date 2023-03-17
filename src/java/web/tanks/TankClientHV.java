package web.tanks;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import utilities.MySQLQuery;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.Table;

public class TankClientHV {

    public void getHv(Connection conn, int clientId) throws Exception {
        Object[][] infoData = new MySQLQuery("SELECT "
                + "CONCAT(otc.document, IFNULL(CONCAT('-', otc.dv), '')), "
                + "otc.name, "
                + "otc.represen_name, "
                + "otc.phones, "
                + "cat.description, "
                + "otc.address, "
                + "COALESCE(otc.neigh, n.name), "
                + "c.name, "
                + "otc.folder_name, "
                + "t.name "
                + "FROM ord_tank_client otc "
                + "INNER JOIN est_tank_category cat ON otc.categ_id = cat.id "
                + "LEFT JOIN neigh n ON otc.neigh_id = n.id "
                + "LEFT JOIN city c ON otc.city_id = c.id "
                + "LEFT JOIN est_price_type t ON otc.price_type_id = t.id "
                + "WHERE otc.id = " + clientId).getRecords(conn);

        List<Column> cols = new ArrayList<>();
        cols.add(new Column("Documento", 30, 1));
        cols.add(new Column("Cliente", 30, 1));
        cols.add(new Column("Representante", 30, 1));
        cols.add(new Column("Teléfonos", 30, 1));
        cols.add(new Column("Descripción", 30, 1));
        cols.add(new Column("Dirección", 30, 1));
        cols.add(new Column("Barrio", 30, 1));
        cols.add(new Column("Ciudad", 30, 1));
        cols.add(new Column("Folder", 30, 1));
        cols.add(new Column("Lista de Precios", 30, 1));

        Object[][] novsData = new MySQLQuery("SELECT "
                + "cm.id, "
                + "cm.place_name, "
                + "cm.detail, "
                + "cm.reg_date, "
                + "cm.chk_date "
                + "FROM com_novelty cm "
                + "WHERE cm.est_client_id = " + clientId).getRecords(conn);

        Object[][] consAlertData = new MySQLQuery("SELECT "
                + "ac.consum, "
                + "ac.capacity, "
                + "ac.created_date, "
                + "ac.rev_date, "
                + "ac.notes "
                + "FROM est_alert_consum ac "
                + "WHERE ac.rev_date IS NOT NULL "
                + "AND ac.client_id = " + clientId).getRecords(conn);

        Object[][] docsData = new MySQLQuery("SELECT "
                + "dt.name, "
                + "doc.notes, "
                + "doc.state "
                + "FROM est_client_doc doc "
                + "INNER JOIN est_doc_type dt ON doc.type_id = dt.id "
                + "WHERE doc.client_id = " + clientId).getRecords(conn);

        Object[][] extData = new MySQLQuery("SELECT "
                + "ext.lbs, "
                + "ext.enterprise, "
                + "ext.expiration_date "
                + "FROM est_ext_client ext "
                + "WHERE ext.active "
                + "AND ext.client_id = " + clientId).getRecords(conn);

        Object[][] progData = new MySQLQuery("SELECT "
                + "ep.prog_date, "
                + "v.plate  "
                + "FROM est_prog_sede eps "
                + "INNER JOIN est_prog ep ON eps.prog_id = ep.id "
                + "INNER JOIN vehicle v ON ep.vh_id = v.id "
                + "WHERE eps.tank_client_id = " + clientId).getRecords(conn);

        Object[][] prospData = new MySQLQuery("SELECT "
                + "ep.reg_dt, "
                + "ep.afil_dt, "
                + "ep.capacity, "
                + "ep.cons_planned "
                + "FROM est_prospect ep "
                + "WHERE ep.client_id = " + clientId).getRecords(conn);

        Object[][] saleData = new MySQLQuery("SELECT "
                + "es.sale_date, "
                + "es.bill_num, "
                + "es.kgs, "
                + "es.total, "
                + "es.unit_price, "
                + "vh.plate, "
                + "et.serial, "
                + "et.capacity "
                + "FROM est_sale es "
                + "LEFT JOIN vehicle vh ON es.vh_id = vh.id "
                + "LEFT JOIN est_tank et ON es.est_tank_id = et.id "
                + "WHERE ON es.client_id = " + clientId).getRecords(conn);

        Object[][] schedData = new MySQLQuery("SELECT "
                + "es.visit_date, "
                + "es.visit_id IS NOT NULL, "
                + "es.`type`, "
                + "es.check_notes, "
                + "es.novs "
                + "FROM est_schedule es "
                + "WHERE es.clie_tank_id = " + clientId).getRecords(conn);

        Object[][] frecData = new MySQLQuery("SELECT "
                + "p.week, "
                + "p.day "
                + "FROM est_sede_frec_path p "
                + "WHERE p.clie_tank_id = " + clientId).getRecords(conn);

        Object[][] sedeNovsData = new MySQLQuery("SELECT "
                + "n.dt, "
                + "n.novs, "
                + "n.active "
                + "FROM est_sede_nov n "
                + "WHERE n.clie_tank_id = " + clientId).getRecords(conn);

        Object[][] tankData = new MySQLQuery("SELECT t.serial, t.ctr_type, l.description, t.unused, t.par_beg, t.tot_beg, t.last_par, t.last_tot, t.capacity, t.factory, t.num_users, t.certificate_date "
                + "FROM est_tank t "
                + "INNER JOIN est_tank_location l ON t.location_id = l.id "
                + "WHERE t.client_id = " + clientId).getRecords(conn);

        Object[][] pqrOtherData = new MySQLQuery("SELECT pqr.serial, CONCAT(pqr.regist_date, ' ', pqr.regist_hour), pqr.confirm_date, r.description "
                + "FROM ord_pqr_other pqr "
                + "INNER JOIN ord_pqr_reason r ON pqr.reason_id = r.id "
                + "WHERE pqr.build_id = " + clientId).getRecords(conn);

        Object[][] pqrTankData = new MySQLQuery("SELECT pqr.bill_num, CONCAT(pqr.regist_date, ' ', pqr.regist_hour), pqr.arrival_date, CONCAT(pqr.attention_date, ' ', pqr.attention_hour), pqr.notes, CONCAT(t.first_name, ' ', t.last_name) "
                + "FROM ord_pqr_tank pqr "
                + "LEFT JOIN ord_technician t ON pqr.technician_id = t.id "
                + "WHERE pqr.build_id = " + clientId).getRecords(conn);

        Object[][] repairData = new MySQLQuery("SELECT r.serial, CONCAT(r.regist_date, ' ', r.regist_hour), r.confirm_date, r.cancel_date, CONCAT(t.first_name, ' ', t.last_name) "
                + "FROM ord_repairs r "
                + "LEFT JOIN ord_technician t ON r.technician_id = t.id "
                + "WHERE r.build_id = " + clientId).getRecords(conn);

        Object[][] orderData = new MySQLQuery("SELECT oto.day, oto.taken_hour, oto.assig_hour, CONCAT(IFNULL(oto.confirm_dt, ''), ' ', oto.confirm_hour), CONCAT(drv.first_name, ' ', drv.last_name), v.plate "
                + "FROM ord_tank_order oto "
                + "LEFT JOIN employee drv ON oto.driver_id = drv.id "
                + "LEFT JOIN vehicle v ON oto.vehicle_id = v.id "
                + "WHERE oto.tank_client_id = " + clientId).getRecords(conn);

        Object[][] visitData = new MySQLQuery("SELECT v.visit_date, v.regist_date, v.notes, CONCAT(man.first_name, ' ', man.last_name), CONCAT(sup.first_name, ' ', sup.last_name), CONCAT(drv.first_name, ' ', drv.last_name), CONCAT(pe.first_name, ' ', pe.last_name) "
                + "FROM ord_tank_visit v "
                + "LEFT JOIN vehicle vh ON v.vehicle_id = vh.id "
                + "LEFT JOIN com_service_manager m ON v.manager_id = m.id"
                + "LEFT JOIN employee man ON m.emp_id = man.id "
                + "LEFT JOIN est_supervisor s ON v.supervisor_id = s.id "
                + "LEFT JOIN per_employee sup ON s.per_emp_id = sup.id "
                + "LEFT JOIN employee drv ON v.driver_id = drv.id "
                + "LEFT JOIN per_employee pe ON v.per_emp_id = pe.id "
                + "WHERE v.client_id = " + clientId).getRecords(conn);

        MySQLReport rep = new MySQLReport("Hoja de Vida Cliente Estacionario", "", "Hoja 1", MySQLQuery.now(conn));

        rep.setVerticalFreeze(6);
        rep.setZoomFactor(80);

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new utilities.mysqlReport.CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));//3
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "HH:mm:ss"));//4
        rep.getFormats().get(1).setWrap(true);

        Table tbl = new Table("Reporte Detallado de Pqrs Fugas Cilindros");
        tbl.setColumns(cols);

        //tbl.setData(newData);
        rep.getTables().add(tbl);
    }

}
