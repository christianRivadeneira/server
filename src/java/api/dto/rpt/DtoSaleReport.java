package api.dto.rpt;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import utilities.Dates;
import utilities.MySQLQuery;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.SummaryRow;
import utilities.mysqlReport.Table;

public class DtoSaleReport {

    public synchronized static MySQLReport getConsolidation(Date day, Integer centerId, BigDecimal salCont, Date dDay, boolean chkDetail, String centerLabel, Connection con) throws Exception {
        Date[] begAndEnd = Dates.getDatesBegEnd(day);
        Date begMonth = begAndEnd[0];
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(begMonth);
        gc.add(GregorianCalendar.DAY_OF_MONTH, -1);
        Date lastMonthEnd = gc.getTime();
        SimpleDateFormat sqlFormat = new SimpleDateFormat("yyyy-MM-dd");

        saveSaldo(centerId, day, salCont, con);

        //fras que al inicio del mes estaban sin liquidar y que al día D ya estaban liquidadas
        BigDecimal saldoInicialPrev = new MySQLQuery("SELECT initial_balance_prev FROM dto_center WHERE id = " + centerId).getAsBigDecimal(con, true);
        //fras importadas del minminas desde el día 1 hasta el día D
        BigDecimal saldoInicialMonth = new MySQLQuery("SELECT initial_balance_month FROM dto_center WHERE id = " + centerId).getAsBigDecimal(con, true);
        //saldo del ministerio de minas del mes anterior
        MySQLQuery saldoMinQ = new MySQLQuery("SELECT SUM(subsidy) "
                + "FROM dto_sale s "
                + "LEFT JOIN dto_liquidation l ON s.dto_liq_id = l.id "
                + "LEFT JOIN trk_anul_sale an ON an.dto_sale_id = s.id "
                + "WHERE "
                + "s.dt < '" + sqlFormat.format(lastMonthEnd) + " 23:59:59' "
                + "AND an.id IS NULL "
                + "AND hide_dt IS NULL "
                + "AND (l.id IS NULL OR (l.id IS NOT NULL AND l.dt > '" + sqlFormat.format(lastMonthEnd) + " 00:00:00')) "
                + "AND s.center_id = ?1 ");
        saldoMinQ.setParam(1, centerId);
        BigDecimal saldoMinMinasAnt = saldoMinQ.getAsBigDecimal(con, true);
        //ventas con subidisio nuevas del mes del día D en adelante, las otras están en la kte.
        MySQLQuery saldoMinMesQ = new MySQLQuery("SELECT SUM(s.subsidy) "
                + "FROM dto_sale s "
                + "LEFT JOIN trk_anul_sale an ON an.dto_sale_id = s.id "
                + "WHERE "
                //                    + condicionCausal
                + "s.dt > '" + sqlFormat.format(dDay) + " 00:00:00' "
                + "AND s.dt BETWEEN '" + sqlFormat.format(begMonth) + " 00:00:00' AND '" + sqlFormat.format(day) + " 23:59:59' "
                + "AND an.id IS NULL "
                + "AND s.center_id = ?1");
        saldoMinMesQ.setParam(1, centerId);
        BigDecimal saldoMinMinasMes = saldoMinMesQ.getAsBigDecimal(con, true);

        //fras con causal del no pago que estaban incluidas en la kte.
        //mq.addQuery("SELECT 0");
        //suma de los pendientes sin contar ocultas
        MySQLQuery sumPendQ = new MySQLQuery("SELECT SUM(subsidy) "
                + "FROM dto_sale s "
                + "LEFT JOIN dto_liquidation l ON s.dto_liq_id = l.id "
                + "LEFT JOIN trk_anul_sale an ON an.dto_sale_id = s.id "
                + "WHERE "
                + "s.dt < '" + sqlFormat.format(day) + " 23:59:59' "
                + "AND an.id IS NULL "
                + "AND s.state <> 'error' "
                + "AND s.hide_dt IS NULL "
                + "AND (l.id IS NULL OR (l.id IS NOT NULL AND l.dt > '" + sqlFormat.format(day) + " 00:00:00')) "
                + "AND s.center_id = ?1 ");
        sumPendQ.setParam(1, centerId);
        BigDecimal sumPendng = sumPendQ.getAsBigDecimal(con, true);

        //suma de ocultas
        MySQLQuery sumHideQ = new MySQLQuery("SELECT SUM(subsidy) "
                + "FROM dto_sale s "
                + "LEFT JOIN dto_liquidation l ON s.dto_liq_id = l.id "
                + "LEFT JOIN trk_anul_sale an ON an.dto_sale_id = s.id "
                + "WHERE "
                + "s.dt BETWEEN '" + sqlFormat.format(begMonth) + " 00:00:00' AND '" + sqlFormat.format(day) + " 23:59:59' "
                + "AND an.id IS NULL "
                + "AND s.hide_dt IS NOT NULL "
                + "AND s.center_id = ?1 ");
        sumHideQ.setParam(1, centerId);
        BigDecimal sumHide = sumHideQ.getAsBigDecimal(con, true);

        //detalle de los pendientes
        /**
         * Se usan left join para evitar que haga filtros adicionales, para
         * saber que los datos están completos se usa ds.state <> 'error'
         */
        MySQLQuery dataPendQ;
        if (chkDetail) {
            dataPendQ = new MySQLQuery("SELECT "
                    + "ds.dt,"
                    + "ds.clie_doc,"
                    + "COALESCE(ct.minas_name, ds.orig_capa), "
                    + "ds.value_total, "
                    + "ds.subsidy, "
                    + "sal.document,"
                    + "ds.nif, "
                    + "ds.stratum, "
                    + "ds.aprov_number, "
                    + "ds.bill "
                    + "FROM dto_sale AS ds "
                    + "LEFT JOIN dto_cyl_type AS ct ON ct.id = ds.cyl_type_id "
                    + "LEFT JOIN dto_salesman AS sal ON sal.id = ds.salesman_id "
                    + "LEFT JOIN dto_liquidation l ON ds.dto_liq_id = l.id "
                    + "LEFT JOIN trk_anul_sale an ON an.dto_sale_id = ds.id "
                    + "WHERE ds.state <> 'error' "
                    + "AND an.id IS NULL "
                    + "AND ds.dt < '" + sqlFormat.format(day) + " 23:59:59' "
                    + "AND ds.hide_dt IS NULL "
                    + "AND (l.id IS NULL OR (l.id IS NOT NULL AND l.dt > '" + sqlFormat.format(day) + " 00:00:00')) "
                    + "AND ds.center_id = ?1 "
                    + "ORDER BY ds.dt ");
        } else {
            dataPendQ = new MySQLQuery("SELECT "
                    + "SUM(ds.value_total), "
                    + "SUM(ds.subsidy) "
                    + "FROM dto_sale AS ds "
                    + "LEFT JOIN dto_liquidation l ON ds.dto_liq_id = l.id AND l.center_id = ?1 "
                    + "LEFT JOIN trk_anul_sale an ON an.dto_sale_id = ds.id "
                    + "WHERE ds.state <> 'error' "
                    + "AND an.id IS NULL "
                    + "AND ds.hide_dt IS NULL "
                    + "AND ds.dt < '" + sqlFormat.format(day) + " 23:59:59' "
                    + "AND (l.id IS NULL OR (l.id IS NOT NULL AND l.dt > '" + sqlFormat.format(day) + " 00:00:00')) "
                    + "AND ds.center_id = ?1 ");
        }

        dataPendQ.setParam(1, centerId);
        Object[][] dataPend = dataPendQ.getRecords(con);
        BigDecimal saldoCont = salCont;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        MySQLReport rep = new MySQLReport("Conciliación de Subsidios " + centerLabel, "Fecha: " + sdf.format(day), "Conciliacion", MySQLQuery.now(con));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy hh:mm:ss a"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//3
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.RIGHT));//4
        rep.getFormats().get(0).setWrap(true);
        //rep.setVerticalFreeze(13);
        rep.setZoomFactor(85);
        Table tbl = new Table("Datos conciliación");
        tbl.getColumns().add(new Column("Concepto", 30, 0));
        tbl.getColumns().add(new Column("Valor", 18, 1));
        Object[][] dataTbl = new Object[8][2];

        dataTbl[0][0] = "Saldo Inicial Ministerio de Minas";
        dataTbl[0][1] = saldoMinMinasAnt;
        dataTbl[1][0] = "Ministerio de Minas Mes";
        dataTbl[1][1] = saldoMinMinasMes;

        boolean firstMonth = sameMonth(dDay, day);
        BigDecimal totalMinas;

        if (firstMonth) {
            dataTbl[0][1] = ((BigDecimal) dataTbl[0][1]).add(saldoInicialPrev);
            dataTbl[1][1] = ((BigDecimal) dataTbl[1][1]).add(saldoInicialMonth);
        }

        totalMinas = ((BigDecimal) dataTbl[0][1]).add((BigDecimal) dataTbl[1][1]);

        dataTbl[2][0] = "Total Saldo Ministerio";
        dataTbl[2][1] = totalMinas;

        dataTbl[3][0] = "Saldo Contable";
        dataTbl[3][1] = saldoCont;

        dataTbl[4][0] = "Diferencia";
        dataTbl[4][1] = totalMinas.subtract(saldoCont);

        dataTbl[5][0] = "Suma Pendientes";
        dataTbl[5][1] = sumPendng;

        dataTbl[6][0] = "Suma Depurados";
        dataTbl[6][1] = sumHide;

        dataTbl[7][0] = "Saldo Sigma";
        dataTbl[7][1] = sumPendng.add(sumHide);

        tbl.setRowColor(4, MySQLReportWriter.COLOR_GRAY);
        tbl.setRowColor(7, MySQLReportWriter.COLOR_GRAY);

        tbl.setData(dataTbl);
        rep.getTables().add(tbl);

        if (chkDetail) {
            Table tblD = new Table("Facturas Pendientes");
            tblD.getColumns().add(new Column("Fecha", 0, 2));
            tblD.getColumns().add(new Column("Doc. Cliente", 0, 0));
            tblD.getColumns().add(new Column("Capa.", 10, 4));
            tblD.getColumns().add(new Column("Valor", 15, 1));
            tblD.getColumns().add(new Column("Subsidio", 15, 1));
            tblD.getColumns().add(new Column("Vendedor", 15, 4));
            tblD.getColumns().add(new Column("Nif", 15, 0));
            tblD.getColumns().add(new Column("Estrato", 10, 0));
            tblD.getColumns().add(new Column("No. Aprobación", 20, 0));
            tblD.getColumns().add(new Column("Factura", 20, 0));
            SummaryRow sr = new SummaryRow("Suma Pendientes", 3);
            sr.disableForColumn(5);
            tblD.setSummaryRow(sr);

            tblD.setData(dataPend);
            if (dataPend.length != 0) {
                rep.getTables().add(tblD);
            }
        } else {
            Table tblD = new Table("Facturas Pendientes");
            tblD.getColumns().add(new Column("Valor", 15, 1));
            tblD.getColumns().add(new Column("Subsidio", 15, 1));
            tblD.setData(dataPend);
            rep.getTables().add(tblD);
        }

        return rep;
    }

    private static void saveSaldo(int centerId, Date d, BigDecimal saldo, Connection conn) throws Exception {
        if (saldo != null) {
            MySQLQuery q = new MySQLQuery("DELETE FROM dto_account WHERE center_id = ?1 AND dt = ?2");
            q.setParam(1, centerId);
            q.setParam(2, d);
            q.executeDelete(conn);
            MySQLQuery q2 = new MySQLQuery("INSERT INTO dto_account SET center_id = ?1, dt = ?2, saldo = ?3");
            q2.setParam(1, centerId);
            q2.setParam(2, d);
            q2.setParam(3, saldo);
            q2.executeInsert(conn);
        }
    }

    private static boolean sameMonth(Date d1, Date d2) {
        GregorianCalendar gc1 = new GregorianCalendar();
        gc1.setTime(d1);
        GregorianCalendar gc2 = new GregorianCalendar();
        gc2.setTime(d2);
        return gc1.get(GregorianCalendar.YEAR) == gc2.get(GregorianCalendar.YEAR) && gc1.get(GregorianCalendar.MONTH) == gc2.get(GregorianCalendar.MONTH);
    }
}
