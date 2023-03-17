package api.bill.rpt;

import api.bill.model.BillBuildFactor;
import api.bill.model.BillBuilding;
import api.bill.model.BillCfg;
import api.bill.model.BillClieCau;
import api.bill.model.BillClieRebill;
import api.bill.model.BillClientTank;
import api.bill.model.BillInstCheck;
import api.bill.model.BillInstance;
import api.bill.model.BillPriceSpan;
import api.bill.model.BillSpan;
import api.bill.model.BillTransaction;
import api.bill.model.dto.RptBillPayRequest;
import api.bill.model.dto.SuiNet;
import api.bill.writers.bill.DtoRangeToPrint;
import api.ord.model.OrdChannel;
import api.sys.model.SysCfg;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.billing.constants.Accounts;
import model.billing.constants.Transactions;
import utilities.Dates;
import utilities.MySQLPreparedQuery;
import utilities.MySQLQuery;
import utilities.ServerNow;
import utilities.cast;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.SummaryRow;
import utilities.mysqlReport.Table;
import web.billing.BillingServlet;

public class BillReport {

    // RPT SUI CLIENTES TANQUES
    public static MySQLReport getSuiTank(BillInstance bi, int spanId, Connection connIns) throws Exception {
        SysCfg sysCfg;
        try (Connection em = BillingServlet.getConnection()) {
            sysCfg = SysCfg.select(em);
        }

        String cityName = bi.name;
        BillSpan span = new BillSpan().select(spanId, connIns);
        Map<Integer, BigDecimal> prices = BillPriceSpan.getPricesMap(connIns, spanId);
        SimpleDateFormat shortDateFormat = new SimpleDateFormat("d MMMM yyyy");
        String periodo = "Periodo de Consumo: " + shortDateFormat.format(span.beginDate) + "  -  " + shortDateFormat.format(span.endDate);
        MySQLReport rep = new MySQLReport("Reporte General SUI - " + cityName, periodo, "SUI", MySQLQuery.now(connIns));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.000"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.000"));//3
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//4
        rep.setZoomFactor(80);
        Table tb = new Table("model");
        tb.getColumns().add(new Column("Núm Instal", 15, 0));//0
        tb.getColumns().add(new Column("Cliente", 30, 0));//1
        tb.getColumns().add(new Column("Factor", 8, 2));//2
        tb.getColumns().add(new Column("Cupón", 15, 0));//3
        tb.getColumns().add(new Column("Fecha", 12, 4));//4
        tb.getColumns().add(new Column("Cons. m3", 14, 1));//5
        tb.getColumns().add(new Column("Cons. gal", 14, 1));//6
        tb.getColumns().add(new Column("Cons. kg", 14, 1));//7
        tb.getColumns().add(new Column("Cons. gal * Fact", 18, 1));//8
        tb.getColumns().add(new Column("Cons. kg * Fact", 18, 1));//9
        tb.getColumns().add(new Column("Valor", 20, 3));//10

        List<BillBuilding> buildings = BillBuilding.getAll(connIns);
        MySQLPreparedQuery factorQ = new MySQLPreparedQuery(BillBuildFactor.FACTOR_QUERY, connIns);
        MySQLPreparedQuery qBill = new MySQLPreparedQuery("SELECT b.bill_num, b.creation_date "
                + "FROM bill_bill as b "
                + "WHERE b.client_tank_id = ?1 AND b.bill_span_id = " + spanId + " "
                + "ORDER BY b.id ASC LIMIT 1 ", connIns);

        for (BillBuilding build : buildings) {
            BigDecimal buildFactor = BillBuildFactor.getFactor(spanId, build.id, factorQ);
            MySQLQuery qClients = new MySQLQuery("SELECT num_install, first_name, COALESCE(c.last_name,''), (r.reading - r.last_reading), c.id "
                    + " FROM bill_client_tank c "
                    + " INNER join bill_reading r on r.client_tank_id = c.id and r.span_id = " + spanId + " "
                    + " WHERE c.building_id = " + build.id + " ");
            Object[][] clientsData = qClients.getRecords(connIns);
            if (clientsData.length > 0) {
                Table bTable = new Table(build.oldId + " " + build.name + " " + build.address + "  Clientes " + clientsData.length);
                bTable.setColumns(tb.getColumns());
                for (Object[] clientsRow : clientsData) {
                    Integer clientId = (Integer) clientsRow[4];
                    Integer listId = BillPriceSpan.getListId(connIns, spanId, clientId);
                    //Integer listId = 1;
                    if (listId != null) {
                        BigDecimal m3Consu = (BigDecimal) (clientsRow[3] != null ? clientsRow[3] : BigDecimal.ZERO);
                        BigDecimal valCons = span.getConsVal(m3Consu, buildFactor, prices.get(MySQLQuery.getAsInteger(listId)));
                        String client = clientsRow[1] + " " + clientsRow[2];
                        Object[] row = new Object[11];
                        row[0] = clientsRow[0]; //Núm Instal
                        row[1] = client;//Cliente
                        row[2] = buildFactor;//Factor
                        qBill.setParameter(1, clientId);
                        Object[] lbill = qBill.getRecord();
                        if (lbill != null && lbill.length > 0) {
                            row[3] = lbill[0];//Cupón
                            row[4] = lbill[1];//Fecha
                        } else {
                            row[3] = null;//Cupón
                            row[4] = null;//Fecha
                        }

                        row[5] = m3Consu;//Cons. m3
                        row[6] = m3Consu.multiply(span.getM3ToGalKte());//Cons. gal
                        row[7] = m3Consu.multiply(span.getM3ToGalKte()).multiply(span.galToKgKte);//Cons. kg
                        row[8] = m3Consu.multiply(span.getM3ToGalKte()).multiply(buildFactor);//gal build                            
                        row[9] = m3Consu.multiply(span.getM3ToGalKte()).multiply(span.galToKgKte).multiply(buildFactor);//kg * fac 

                        row[10] = valCons;//Valor
                        if (!sysCfg.skipMinCons || valCons.compareTo(span.minConsValue) >= 0) {
                            bTable.addRow(row);
                        }
                    }
                }
                bTable.setSummaryRow(new SummaryRow("Totales", 5));
                if (!bTable.isEmpty()) {
                    rep.getTables().add(bTable);
                }
            }
        }
        return rep;
    }

    private static BigDecimal getPercent(BigDecimal val, BigDecimal total) {
        if (total != null && total.compareTo(BigDecimal.ZERO) > 0) {
            return val.divide(total, 2, RoundingMode.HALF_EVEN);
        } else {
            return null;
        }
    }

    // RPT FACTURAS PAGADAS TANQUES
    public static MySQLReport getRptTankBillsPaid(BillInstance bi, RptBillPayRequest req, Connection conn) throws Exception {
        BillCfg cfg = new BillCfg().select(1, conn);
        MySQLReport rep = new MySQLReport("Reporte de Pagos", "", "", MySQLQuery.now(conn));
        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy");
        rep.getSubTitles().add("Consulta Desde: " + sdf.format(req.begDt) + " Hasta: " + sdf.format(req.endDt));

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.000"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "d/MM/yyyy"));//2
        rep.setZoomFactor(80);
        rep.setVerticalFreeze(5);
        Table tb = new Table("model");
        tb.getColumns().add(new Column("Num Inst.", 15, 0));//0
        tb.getColumns().add(new Column("Cliente", 35, 0));//1
        tb.getColumns().add(new Column("Fec. Pago", 15, 2));//2
        tb.getColumns().add(new Column("Fec. Ingreso", 18, 2));//3
        tb.getColumns().add(new Column("Cupón", 15, 0));//4
        if (cfg.showTicket) {
            tb.getColumns().add(new Column("Consecutivo", 20, 0));
        }
        tb.getColumns().add(new Column("Edificio", 35, 0));//5
        tb.getColumns().add(new Column("Pago", 20, 1));//6

        //TODO: para generar reversiones el pago no sera  el credito de la factura + credito no ajuste - notas ajuste debito
        MySQLQuery registrarQ = new MySQLQuery("SELECT DISTINCT "
                + "b.registrar_id FROM bill_bill as b "
                + "WHERE ?1 <= b.payment_date AND b.payment_date <= ?2 ");

        registrarQ.setParam(1, req.begDt);
        registrarQ.setParam(2, req.endDt);
        Object[][] registrarsData = registrarQ.getRecords(conn);

        if (registrarsData == null || registrarsData.length == 0) {
            throw new Exception("No se hallaron datos");
        }

        String registIds = "";
        for (Object[] registrarsData1 : registrarsData) {
            registIds += MySQLQuery.getAsString(registrarsData1[0]);
            registIds += ",";
        }
        registIds = registIds.substring(0, registIds.length() - 1);

        String str = "SELECT "
                + "b.registrar_id, "
                + "b.id, "
                + "c.id, "
                + "c.first_name, "
                + "COALESCE(c.last_name,''), "
                + "e.name, "
                + "c.num_install, "
                + "b.payment_date, "
                + "b.bill_num, "
                + (cfg.showTicket ? "b.ticket, " : " ")
                + "b.regist_date "
                + "FROM bill_bill b "
                + "inner join bill_client_tank c on c.id = b.client_tank_id "
                + "inner join bill_building e on e.id = c.building_id "
                + "where ?1 <= payment_date AND payment_date <= ?2 "
                + "and payment_date is not null "
                + "and b.bank_id=" + req.bankId + " "
                + "and registrar_id IN ( " + registIds + " ) ";

        MySQLQuery billDataQ = new MySQLQuery(str);
        billDataQ.setParam(1, req.begDt);
        billDataQ.setParam(2, req.endDt);

        Object[][] dataBills = billDataQ.getRecords(conn);

        MySQLQuery anticDataQ = new MySQLQuery("SELECT n.id ,n.serial, "
                + "CONCAT(b.first_name,' ',COALESCE(b.last_name,'')),n.bank_date,n.when_notes, bb.name, b.num_install "
                + "FROM bill_antic_note n "
                + "INNER JOIN bill_client_tank b ON b.id = n.client_tank_id "
                + "INNER JOIN bill_building bb ON bb.id = b.building_id "
                + "WHERE n.bank_id IS NOT NULL AND n.bank_date IS NOT NULL AND n.bank_id = " + req.bankId + " "
                + "AND n.bank_date BETWEEN ?1 AND ?2 ");
        anticDataQ.setParam(1, req.begDt);
        anticDataQ.setParam(2, req.endDt);
        Object[][] anticData = anticDataQ.getRecords(conn);

        for (Object[] regitrar : registrarsData) {
            Object[][] payedFacs = getPayFacs(dataBills, regitrar[0], cfg.showTicket);
            if (payedFacs.length > 0) {
                MySQLQuery employeeQ = new MySQLQuery("SELECT CONCAT(first_name, ' ', last_name) FROM sigma.employee WHERE id = ?1");
                employeeQ.setParam(1, regitrar[0]);
                String emp = employeeQ.getAsString(conn);
                Table bTable = new Table(emp + "  <PAGOS REGISTRADOS: " + payedFacs.length + ">");
                rep.getTables().add(bTable);
                bTable.setColumns(tb.getColumns());

                Object[][] data = new Object[payedFacs.length][cfg.showTicket ? 8 : 7];

                for (int i = 0; i < payedFacs.length; i++) {
                    MySQLQuery payQ = new MySQLQuery("SELECT SUM(t.value) FROM bill_transaction as t "
                            + " WHERE t.doc_type='fac' "
                            + "and t.doc_id=?1 "
                            + "and t.cli_tank_id=?2");
                    payQ.setParam(1, payedFacs[i][0]);
                    payQ.setParam(2, payedFacs[i][1]);

                    data[i][0] = payedFacs[i][5] != null ? (String) payedFacs[i][5] : "";
                    String client = payedFacs[i][2].toString();
                    if (payedFacs[i][3] != null) {
                        client += " " + payedFacs[i][3];
                    }
                    BigDecimal b = payQ.getAsBigDecimal(conn, true);
                    data[i][1] = client;
                    data[i][2] = payedFacs[i][6] != null ? (Date) payedFacs[i][6] : null;
                    data[i][3] = payedFacs[i][cfg.showTicket ? 9 : 8] != null ? (Date) payedFacs[i][cfg.showTicket ? 9 : 8] : null;
                    data[i][4] = payedFacs[i][7] != null ? (String) payedFacs[i][7] : "";
                    if (cfg.showTicket) {
                        data[i][5] = payedFacs[i][8] != null ? payedFacs[i][8].toString() : "";
                    }
                    data[i][cfg.showTicket ? 6 : 5] = payedFacs[i][4] != null ? (String) payedFacs[i][4] : "";
                    data[i][cfg.showTicket ? 7 : 6] = b.setScale(3, RoundingMode.HALF_EVEN);
                }
                bTable.setData(data);
                bTable.setSummaryRow(new SummaryRow("Totales", cfg.showTicket ? 7 : 6));
            }
        }

        if (anticData.length > 0) {
            Table bTable = new Table("<DEVOLUCIONES: " + anticData.length + ">");
            rep.getTables().add(bTable);
            bTable.getColumns().add(new Column("Num Inst.", 15, 0));//0
            bTable.getColumns().add(new Column("Cliente", 35, 0));//1
            bTable.getColumns().add(new Column("Fec. Pago", 15, 2));//2
            bTable.getColumns().add(new Column("Fec. Ingreso", 18, 2));//3
            bTable.getColumns().add(new Column("Cupón", 15, 0));//4
            bTable.getColumns().add(new Column("Edificio", 35, 0));//5
            bTable.getColumns().add(new Column("Pago", 20, 1));//6

            Object[][] data = new Object[anticData.length][7];

            for (int i = 0; i < anticData.length; i++) {
                MySQLQuery anticValue = new MySQLQuery("SELECT t.value FROM bill_transaction t where t.doc_id = ?1 AND t.doc_type = 'pag_antic' ");
                anticValue.setParam(1, anticData[0]);

                data[i][0] = anticData[i][6];
                data[i][1] = anticData[i][2];
                data[i][2] = anticData[i][3];
                data[i][3] = anticData[i][4];
                data[i][4] = anticData[i][1];
                data[i][5] = anticData[i][5];
                data[i][6] = anticValue.getAsBigDecimal(conn, false).setScale(3, RoundingMode.HALF_EVEN);
            }

            bTable.setData(data);
            bTable.setSummaryRow(new SummaryRow("Totales", 6));
        }
        return rep;
    }

    // RPT FACTURAS PAGADAS NET
    public static MySQLReport getRptNetBillsPaid(BillInstance bi, RptBillPayRequest req, Connection conn) throws Exception {
        BillCfg cfg = new BillCfg().select(1, conn);
        MySQLReport rep = new MySQLReport("Reporte de Pagos", "", "", MySQLQuery.now(conn));
        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy");
        rep.getSubTitles().add("Consulta Desde: " + sdf.format(req.begDt) + " Hasta: " + sdf.format(req.endDt));

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.000"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "d/MM/yyyy"));//2
        rep.setZoomFactor(80);
        rep.setVerticalFreeze(5);
        Table tb = new Table("model");
        tb.getColumns().add(new Column("Num Inst.", 15, 0));//0
        tb.getColumns().add(new Column("Cliente", 35, 0));//1
        tb.getColumns().add(new Column("Fec. Pago", 15, 2));//2
        tb.getColumns().add(new Column("Fec. Ingreso", 18, 2));//3
        tb.getColumns().add(new Column("Cupón", 15, 0));//4
        if (cfg.showTicket) {
            tb.getColumns().add(new Column("Consecutivo", 20, 0));
        }
        tb.getColumns().add(new Column("Dirección", 35, 0));//5
        tb.getColumns().add(new Column("Pago", 20, 1));//6

        //TODO: para generar reversiones el pago no sera  el credito de la factura + credito no ajuste - notas ajuste debito
        MySQLQuery registrarQ = new MySQLQuery("SELECT DISTINCT "
                + "b.registrar_id FROM bill_bill as b "
                + "WHERE ?1 <= b.payment_date AND b.payment_date <= ?2 ");

        registrarQ.setParam(1, req.begDt);
        registrarQ.setParam(2, req.endDt);
        Object[][] registrarsData = registrarQ.getRecords(conn);

        if (registrarsData == null || registrarsData.length == 0) {
            throw new Exception("No se hallaron datos");
        }

        String registIds = "";
        for (Object[] registrarsData1 : registrarsData) {
            registIds += MySQLQuery.getAsString(registrarsData1[0]);
            registIds += ",";
        }
        registIds = registIds.substring(0, registIds.length() - 1);

        String str = "SELECT "
                + "b.registrar_id, "
                + "b.id, "
                + "c.id, "
                + "c.first_name, "
                + "COALESCE(c.last_name,''), "
                + "c.address, "
                + "c.num_install, "
                + "b.payment_date, "
                + "b.bill_num, "
                + (cfg.showTicket ? "b.ticket, " : " ")
                + "b.regist_date "
                + "FROM bill_bill b "
                + "inner join bill_client_tank c on c.id = b.client_tank_id "
                + "where ?1 <= payment_date AND payment_date <= ?2 "
                + "and payment_date is not null "
                + "and b.bank_id=" + req.bankId + " "
                + "and registrar_id IN ( " + registIds + " ) ";

        MySQLQuery billDataQ = new MySQLQuery(str);
        billDataQ.setParam(1, req.begDt);
        billDataQ.setParam(2, req.endDt);

        Object[][] dataBills = billDataQ.getRecords(conn);

        MySQLQuery anticDataQ = new MySQLQuery("SELECT n.id ,n.serial, "
                + "CONCAT(b.first_name,' ',COALESCE(b.last_name,'')),"
                + "n.bank_date,n.when_notes, b.address, b.num_install "
                + "FROM bill_antic_note n "
                + "INNER JOIN bill_client_tank b ON b.id = n.client_tank_id "
                + "WHERE n.bank_id IS NOT NULL AND n.bank_date IS NOT NULL AND n.bank_id = " + req.bankId + " "
                + "AND n.bank_date BETWEEN ?1 AND ?2 ");
        anticDataQ.setParam(1, req.begDt);
        anticDataQ.setParam(2, req.endDt);
        Object[][] anticData = anticDataQ.getRecords(conn);

        for (Object[] regitrar : registrarsData) {
            Object[][] payedFacs = getPayFacs(dataBills, regitrar[0], cfg.showTicket);
            if (payedFacs.length > 0) {
                MySQLQuery employeeQ = new MySQLQuery("SELECT CONCAT(first_name, ' ', last_name) FROM sigma.employee WHERE id = ?1");
                employeeQ.setParam(1, regitrar[0]);
                String emp = employeeQ.getAsString(conn);
                Table bTable = new Table(emp + "  <PAGOS REGISTRADOS: " + payedFacs.length + ">");
                rep.getTables().add(bTable);
                bTable.setColumns(tb.getColumns());

                Object[][] data = new Object[payedFacs.length][cfg.showTicket ? 8 : 7];

                for (int i = 0; i < payedFacs.length; i++) {
                    MySQLQuery payQ = new MySQLQuery("SELECT SUM(t.value) FROM bill_transaction as t "
                            + " WHERE t.doc_type='fac' "
                            + "and t.doc_id=?1 "
                            + "and t.cli_tank_id=?2");
                    payQ.setParam(1, payedFacs[i][0]);
                    payQ.setParam(2, payedFacs[i][1]);

                    data[i][0] = payedFacs[i][5] != null ? (String) payedFacs[i][5] : "";
                    String client = payedFacs[i][2].toString();
                    if (payedFacs[i][3] != null) {
                        client += " " + payedFacs[i][3];
                    }
                    BigDecimal b = payQ.getAsBigDecimal(conn, true);
                    data[i][1] = client;
                    data[i][2] = payedFacs[i][6] != null ? (Date) payedFacs[i][6] : null;
                    data[i][3] = payedFacs[i][cfg.showTicket ? 9 : 8] != null ? (Date) payedFacs[i][cfg.showTicket ? 9 : 8] : null;
                    data[i][4] = payedFacs[i][7] != null ? (String) payedFacs[i][7] : "";
                    if (cfg.showTicket) {
                        data[i][5] = payedFacs[i][8] != null ? payedFacs[i][8].toString() : "";
                    }
                    data[i][cfg.showTicket ? 6 : 5] = payedFacs[i][4] != null ? (String) payedFacs[i][4] : "";
                    data[i][cfg.showTicket ? 7 : 6] = b.setScale(3, RoundingMode.HALF_EVEN);
                }
                bTable.setData(data);
                bTable.setSummaryRow(new SummaryRow("Totales", cfg.showTicket ? 7 : 6));
            }
        }

        if (anticData.length > 0) {
            Table bTable = new Table("<DEVOLUCIONES: " + anticData.length + ">");
            rep.getTables().add(bTable);
            bTable.getColumns().add(new Column("Num Inst.", 15, 0));//0
            bTable.getColumns().add(new Column("Cliente", 35, 0));//1
            bTable.getColumns().add(new Column("Fec. Pago", 15, 2));//2
            bTable.getColumns().add(new Column("Fec. Ingreso", 18, 2));//3
            bTable.getColumns().add(new Column("Cupón", 15, 0));//4
            bTable.getColumns().add(new Column("Dirección", 35, 0));//5
            bTable.getColumns().add(new Column("Pago", 20, 1));//6

            Object[][] data = new Object[anticData.length][7];

            for (int i = 0; i < anticData.length; i++) {
                MySQLQuery anticValue = new MySQLQuery("SELECT t.value FROM bill_transaction t where t.doc_id = ?1 AND t.doc_type = 'pag_antic' ");
                anticValue.setParam(1, anticData[0]);

                data[i][0] = anticData[i][6];
                data[i][1] = anticData[i][2];
                data[i][2] = anticData[i][3];
                data[i][3] = anticData[i][4];
                data[i][4] = anticData[i][1];
                data[i][5] = anticData[i][5];
                data[i][6] = anticValue.getAsBigDecimal(conn, false).setScale(3, RoundingMode.HALF_EVEN);
            }

            bTable.setData(data);
            bTable.setSummaryRow(new SummaryRow("Totales", 6));
        }
        return rep;
    }

    private static Object[][] getPayFacs(Object[][] dataBills, Object registerId, boolean showTicket) throws Exception {
        List<Object[]> dataFacs = new ArrayList<>();
        for (Object[] dataBill : dataBills) {

            if (dataBill[0].equals(registerId)) {
                Object[] aux = new Object[showTicket ? 10 : 9];
                aux[0] = dataBill[1];
                aux[1] = dataBill[2];
                aux[2] = dataBill[3];
                aux[3] = dataBill[4];
                aux[4] = dataBill[5];
                aux[5] = dataBill[6];
                aux[6] = dataBill[7];
                aux[7] = dataBill[8];
                aux[8] = dataBill[9];
                if (showTicket) {
                    aux[9] = dataBill[10];
                }
                dataFacs.add(aux);
            }
        }
        return dataFacs.toArray(new Object[dataFacs.size()][0]);
    }

    // RPT MOVIMIENTOS DEL CLIENTE
    public static MySQLReport getMovementsReport(BillInstance inst, int clientId, boolean detailed, Connection conn) throws Exception {

        BillClientTank client = new BillClientTank().select(clientId, conn);

        MySQLReport rep = new MySQLReport("Movimientos de Cuentas por Cliente - " + inst.name, client.firstName + " " + client.lastName, "Movimientos", MySQLQuery.now(conn));
        rep.getSubTitles().add("Número de Instalación " + client.numInstall);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.000"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "###"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "d/MM/yyyy"));//3
        rep.setZoomFactor(85);
        Table tb = new Table("model");
        tb.getColumns().add(new Column("Id Mov.", 10, 2));
        tb.getColumns().add(new Column("Periodo.", 10, 2));
        tb.getColumns().add(new Column("Fecha", 20, 3));
        tb.getColumns().add(new Column("Débito", 15, 1));
        tb.getColumns().add(new Column("Crédito", 15, 1));
        tb.getColumns().add(new Column("Descripción", 60, 0));

        MySQLPreparedQuery factQ = new MySQLPreparedQuery("SELECT f.bill_num FROM bill_bill AS f WHERE f.id = ?1 ", conn);
        MySQLPreparedQuery noteQ = new MySQLPreparedQuery("SELECT n.serial FROM bill_note as n WHERE n.id = ?1 ", conn);

        List<Integer> aNames = new ArrayList<>();
        aNames.add(Accounts.C_CONS);
        if (inst.isNetInstance()) {
            aNames.add(Accounts.C_CONS_SUBS);
            aNames.add(Accounts.C_CONTRIB);
            aNames.add(Accounts.C_REBILL);
        }
        aNames.add(Accounts.C_BASI);
        aNames.add(Accounts.C_CUOTA_SER_CLI_GLP);
        aNames.add(Accounts.C_CUOTA_SER_CLI_SRV);
        aNames.add(Accounts.C_CUOTA_FINAN_DEU);
        aNames.add(Accounts.C_CUOTA_INT_CRE);
        aNames.add(Accounts.C_CUOTA_SER_EDI);
        aNames.add(Accounts.C_RECON);
        aNames.add(Accounts.C_CAR_GLP);
        aNames.add(Accounts.C_CAR_SRV);
        aNames.add(Accounts.C_CAR_FINAN_DEU);
        aNames.add(Accounts.C_CAR_CONTRIB);
        aNames.add(Accounts.C_CAR_INTE_CRE);
        aNames.add(Accounts.C_INT_GLP);
        aNames.add(Accounts.C_INT_SRV);
        aNames.add(Accounts.C_INT_FINAN_DEU);
        aNames.add(Accounts.C_INT_CONTRIB);
        aNames.add(Accounts.C_CAR_OLD);
        aNames.add(Accounts.C_INT_OLD);
        aNames.add(Accounts.C_FINAN_DEU_POR_COBRAR);
        aNames.add(Accounts.C_ANTICIP);
        aNames.add(Accounts.BANCOS);

        DecimalFormat moneyFormat = new DecimalFormat("$###,##0.00");

        if (detailed) {
            for (int i = 0; i < aNames.size(); i++) {
                Integer accountId = aNames.get(i);
                String accountName = Accounts.accNames.get(accountId);
                BigDecimal totalDeb = BigDecimal.ZERO;
                BigDecimal totalCred = BigDecimal.ZERO;

                List<BillTransaction> res = BillTransaction.getByAccount(accountId, clientId, conn);
                if (res.size() > 0) {

                    Table bTable = new Table("");
                    rep.getTables().add(bTable);
                    bTable.setColumns(tb.getColumns());
                    for (BillTransaction trans : res) {
                        BigDecimal cred = BigDecimal.ZERO;
                        BigDecimal deb = BigDecimal.ZERO;
                        if (trans.accountCredId == accountId) {
                            cred = trans.value;
                        } else {
                            deb = trans.value;
                        }
                        totalCred = totalCred.add(cred);
                        totalDeb = totalDeb.add(deb);

                        Object[] row = new Object[6];
                        bTable.addRow(row);
                        row[0] = trans.id;
                        row[1] = trans.billSpanId;
                        row[2] = trans.created;
                        row[3] = deb;
                        row[4] = cred;

                        String num = "";
                        int type = trans.transTypeId;
                        switch (type) {
                            case Transactions.N_DEBIT:
                            case Transactions.N_CREDIT:
                            case Transactions.N_AJ_DEBIT:
                            case Transactions.N_AJ_CREDIT:
                            case Transactions.N_DEU_ANTE:
                                noteQ.setParameter(1, trans.docId);
                                num = " No. " + noteQ.getAsString();
                                break;
                            case Transactions.PAGO_BANCO:
                                factQ.setParameter(1, trans.docId);
                                num = " No." + factQ.getAsString();
                                break;
                            default:
                                break;
                        }
                        row[5] = Transactions.tNames.get(type) + num;
                    }
                    bTable.setTitle("Balance cuenta " + accountName + "  Total = " + moneyFormat.format(totalDeb.subtract(totalCred)));
                }
            }
        } else {
            Table bTable = new Table("");
            bTable.getColumns().add(new Column("Cuenta", 30, 0));
            bTable.getColumns().add(new Column("Débitos", 25, 1));
            bTable.getColumns().add(new Column("Créditos", 25, 1));
            bTable.getColumns().add(new Column("Total", 25, 1));

            for (int i = 0; i < aNames.size(); i++) {
                Integer accountId = aNames.get(i);
                String accountName = Accounts.accNames.get(accountId);
                BigDecimal totalDeb = BigDecimal.ZERO;
                BigDecimal totalCred = BigDecimal.ZERO;

                List<BillTransaction> res = BillTransaction.getByAccount(accountId, clientId, conn);
                if (res.size() > 0) {
                    for (BillTransaction trans : res) {
                        if (trans.accountCredId == accountId) {
                            totalCred = totalCred.add(trans.value);
                        } else {
                            totalDeb = totalDeb.add(trans.value);
                        }
                    }
                    bTable.addRow(new Object[]{accountName, totalDeb, totalCred, totalDeb.subtract(totalCred)});
                }
            }
            if (!bTable.isEmpty()) {
                rep.getTables().add(bTable);
            }
        }
        return rep;

    }

    // RPT CLIENTES CON COBRO DE RECONEXION TANQUES-REDES
    public static MySQLReport getRptUserReconn(BillInstance bi, int spanId, Connection conn) throws Exception {
        BillSpan sp = new BillSpan().select(spanId, conn);
        SimpleDateFormat shortDateFormat = new SimpleDateFormat("d MMMM yyyy");
        String periodo = "Periodo de Recaudo: Entrega de Facturas - " + shortDateFormat.format(sp.limitDate);
        MySQLQuery q;
        if (bi.isTankInstance()) {
            q = new MySQLQuery("SELECT DISTINCT "
                    + "bill_building.old_id, "
                    + "bill_building.`name`, "
                    + "c.num_install, "
                    + "CONCAT(c.first_name,' ', COALESCE(c.last_name,'')) as name "
                    + "FROM "
                    + "bill_client_tank AS c "
                    + "INNER JOIN bill_transaction AS t ON c.id = t.cli_tank_id "
                    + "INNER JOIN bill_building ON c.building_id = bill_building.id "
                    + "WHERE "
                    + "t.bill_span_id = " + spanId + " AND "
                    + "t.account_deb_id = " + Accounts.C_RECON + " "
                    + "ORDER BY "
                    + "bill_building.old_id ASC, "
                    + "c.num_install ASC");
        } else {
            q = new MySQLQuery("SELECT DISTINCT "
                    + "n.name, "
                    + "c.address, "
                    + "c.num_install, "
                    + "CONCAT(c.first_name,' ', COALESCE(c.last_name,'')) as name "
                    + "FROM "
                    + "bill_client_tank AS c "
                    + "LEFT JOIN sigma.neigh n ON n.id = c.neigh_id "
                    + "INNER JOIN bill_transaction AS t ON c.id = t.cli_tank_id "
                    + "WHERE "
                    + "t.bill_span_id = " + spanId + " AND "
                    + "t.account_deb_id = " + Accounts.C_RECON + " "
                    + "ORDER BY c.num_install ASC");
        }

        Object[][] data = q.getRecords(conn);

        MySQLReport rep = new MySQLReport("Clientes con Cobro de Reconexión", periodo, "Reconexiones", MySQLQuery.now(conn));
        rep.setVerticalFreeze(5);
        rep.setHorizontalFreeze(0);
        rep.setZoomFactor(85);
        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        //Columnas
        List<Column> cols = new ArrayList<>();
        if (bi.isTankInstance()) {
            cols.add(new Column("Cód. Edificio.", 15, 0));
            cols.add(new Column("Edificio", 30, 0));
        } else {
            cols.add(new Column("Barrio", 25, 0));
            cols.add(new Column("Dirección", 25, 0));
        }
        cols.add(new Column("Num. Inst.", 15, 0));
        cols.add(new Column("Nombre", 45, 0));

        Table tbl = new Table("Clientes con Reconexión");
        tbl.setColumns(cols);
        tbl.setData(data);
        if (!tbl.isEmpty()) {
            rep.getTables().add(tbl);
        }
        return rep;
    }

    // RPT NOTAS DEBITO - CREDITO
    public static MySQLReport getRtpClientNotes(BillInstance bi, Boolean cred, Boolean bank, int spanId, Connection conn) throws Exception {

        MySQLReport rep;
        BillSpan sp = new BillSpan().select(spanId, conn);
        Table tbl;

        if (bank) {
            if (cred) {
                rep = new MySQLReport("Notas Crédito con afectación a Bancos - " + bi.name, sp.getConsLabel(), "Hoja 1", MySQLQuery.now(conn));
            } else {
                rep = new MySQLReport("Notas Débito con afectación a Bancos - " + bi.name, sp.getConsLabel(), "Hoja 1", MySQLQuery.now(conn));
            }
        } else if (cred) {
            rep = new MySQLReport("Notas Crédito sin afectación a Bancos - " + bi.name, sp.getConsLabel(), "Hoja 1", MySQLQuery.now(conn));
        } else {
            rep = new MySQLReport("Notas Débito sin afectación a Bancos - " + bi.name, sp.getConsLabel(), "Hoja 1", MySQLQuery.now(conn));
        }

        if (cred) {
            tbl = new Table("Notas Crédito");
        } else {
            tbl = new Table("Notas Débito");
        }

        rep.setVerticalFreeze(5);
        rep.setZoomFactor(85);

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.00"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "d/MM/yyyy"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//3
        rep.getFormats().get(3).setWrap(true);

        //List<Column> cols = new ArrayList<Column>();
        tbl.getColumns().add(new Column("Doc. Id", 10, 0));//0
        tbl.getColumns().add(new Column("Núm. Nota", 12, 0));//1
        tbl.getColumns().add(new Column("Num. Inst", 13, 0));//2
        tbl.getColumns().add(new Column("Nombre", 30, 0));//3
        tbl.getColumns().add(new Column("Fecha", 15, 2));//4
        tbl.getColumns().add(new Column("Descripción", 45, 3));//5
        tbl.getColumns().add(new Column("Valor", 20, 1));//6

        String qs = "SELECT n.id, n.serial, c.num_install, CONCAT(c.first_name,' ', COALESCE(c.last_name,'')), n.when_notes, n.desc_notes, SUM(t.value) FROM bill_transaction t "
                + "inner join bill_note n on t.doc_id = n.id "
                + "inner join bill_client_tank c on n.client_tank_id = c.id "
                + "where ";

        if (bank) {
            if (cred) {
                qs += "n.type_notes='aj_cred' and ";
            } else {
                qs += "n.type_notes='aj_deb' and ";
            }
        } else if (cred) {
            qs += "n.type_notes='n_cred' and ";
        } else {
            qs += "n.type_notes='n_deb' and ";
        }

        qs += "t.doc_type='not' and "
                + "n.bill_span_id =" + spanId + " "
                + "group by t.doc_id ORDER BY n.when_notes ASC";
        MySQLPreparedQuery q = new MySQLPreparedQuery(qs, conn);
        Object[][] data = q.getRecords();
        if (data.length > 0) {
            rep.getTables().add(tbl);
            tbl.setData(data);
            tbl.setSummaryRow(new SummaryRow("Totales", 6));
        }
        return rep;
    }

    // RPT FORMATOS SUI NET
    public static MySQLReport getBillParams(BillInstance inst, SysCfg sysCfg, Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("Parámetros de Facturación", "", "", MySQLQuery.now(conn));
        rep.setMultiRowTitles(2);
        rep.getSubTitles().add("Instancia: " + inst.name);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.0000"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "MM-yyyy"));//2
        rep.setZoomFactor(80);
        rep.setVerticalFreeze(5);
        Table tb = new Table("Parámetros de Facturación");

        tb.getColumns().add(new Column("Cód", 5, 0));//
        tb.getColumns().add(new Column("Mes", 15, 2));//
        tb.getColumns().add(new Column("IPP", 15, 1));//
        tb.getColumns().add(new Column("IPC", 15, 1));//
        tb.getColumns().add(new Column("PMS $", 18, 1));//
        tb.getColumns().add(new Column("Cglp kg", 18, 1));//
        tb.getColumns().add(new Column("Gm $/kg", 18, 1));//
        tb.getColumns().add(new Column("Td $/kg", 18, 1));//
        tb.getColumns().add(new Column("Tv $/kg", 18, 1));//
        tb.getColumns().add(new Column("T $/kg", 18, 1));//
        tb.getColumns().add(new Column("Pérdidas %", 10, 1));//
        tb.getColumns().add(new Column("D inv Residencial", 15, 1));//
        tb.getColumns().add(new Column("D aom Residencial", 15, 1));//
        tb.getColumns().add(new Column("D Residencial", 15, 1));//
        tb.getColumns().add(new Column("D inv no Residencial", 15, 1));//
        tb.getColumns().add(new Column("D aom no Residencial", 15, 1));//
        tb.getColumns().add(new Column("D No residencial", 15, 1));//
        tb.getColumns().add(new Column("Fpc", 10, 1));//
        tb.getColumns().add(new Column("Fv", 10, 1));//
        tb.getColumns().add(new Column("Cuf", 18, 1));//
        tb.getColumns().add(new Column("Cuv Residencial", 18, 1));//
        tb.getColumns().add(new Column("Cuv No Residencial", 18, 1));//
        tb.getColumns().add(new Column("Costo equiv. Estrato 1", 18, 1));//
        tb.getColumns().add(new Column("Costo equiv. Estrato 2", 18, 1));//
        tb.getColumns().add(new Column("Tarifa Estrato 1", 18, 1));//
        tb.getColumns().add(new Column("Tarifa Estrato 2", 18, 1));//
        tb.getColumns().add(new Column("% Subsidio Estratato 1", 15, 1));//
        tb.getColumns().add(new Column("% Subsidio Estratato 2", 15, 1));//
        tb.getColumns().add(new Column("% Contrib Residencial", 15, 1));//
        tb.getColumns().add(new Column("% Contrib No Residencial", 15, 1));//

        MySQLQuery dataQ = new MySQLQuery("SELECT "
                + "cast(s.id as char), "
                + "s.cons_month, "
                + "i.ipp, "
                + "i.ipc, "
                + "s.pms, "
                + "s.cglp, "
                + "s.pms/s.cglp, "
                + "s.t, "
                + "s.tv, "
                + "s.t + s.tv, "
                + "s.p, "
                + "s.d_inv_r, "
                + "s.d_aom_r, "
                + "s.d_inv_r+ s.d_aom_r, "
                + "s.d_inv_nr, "
                + "s.d_aom_nr, "
                + "s.d_inv_nr+ s.d_aom_nr, "
                + "s.fpc, "
                + "s.fv, "
                + "s.cuf, "
                + "s.cuv_r, "
                + "s.cuv_nr, "
                + "s.c_eq_1, "
                + "s.c_eq_2, "
                + "s.final_tarif_1, "
                + "s.final_tarif_2, "
                + "s.sub_perc_1, "
                + "s.sub_perc_2, "
                + "s.contrib_r, "
                + "s.contrib_nr "
                + "FROM bill_span s "
                + "INNER JOIN sigma.bill_price_index i ON i.month = s.cons_month "
                + "ORDER BY s.cons_month");
        Object[][] data = dataQ.getRecords(conn);
        if (data != null && data.length > 0) {
            rep.getTables().add(tb);
            tb.setData(data);
        }
        return rep;
    }

    // RPT FORMATOS SUI NET
    public static MySQLReport getRptA1Billing(int spanId, BillInstance inst, SysCfg sysCfg, Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("A1 Facturación Usuarios por Redes de Ductos", "", "", MySQLQuery.now(conn));
        BillSpan span = new BillSpan().select(spanId, conn);
        rep.getSubTitles().add("Periodo: " + span.getConsLabel());
        rep.getSubTitles().add("Instancia: " + inst.name);
        if (span.state.equals("cons")) {
            rep.getSubTitles().add("¡ALERTA, INFORMACIÓN PROVISIONAL! CAUSACIÓN EN CURSO");
        }

        String daneCode = new MySQLQuery("SELECT code FROM sigma.dane_poblado WHERE id = " + inst.pobId).getAsString(conn);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.0000"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.0000"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.0000"));//3
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd-MM-yyyy"));//4
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "0.00%"));//5

        rep.setZoomFactor(80);
        rep.setMultiRowTitles(2);
        Table tb = new Table("A1 Facturación Usuarios por Redes de Ductos");

        tb.getColumns().add(new Column("NIU", 14, 0));//0
        tb.getColumns().add(new Column("Código DANE", 14, 0));//1
        tb.getColumns().add(new Column("Ubicación", 10, 0));//5
        tb.getColumns().add(new Column("Dirección", 35, 0));//6
        tb.getColumns().add(new Column("Id factura", 12, 0));//7
        tb.getColumns().add(new Column("Expedición Factura", 18, 4));//8
        tb.getColumns().add(new Column("Inicio Periodo", 18, 4));//9
        tb.getColumns().add(new Column("Terminación Periodo", 18, 4));//10
        tb.getColumns().add(new Column("Sector de Consumo", 15, 0));//11
        tb.getColumns().add(new Column("Tipo de Lectura", 15, 0));//12
        tb.getColumns().add(new Column("Lectura Anterior", 15, 1));//13
        tb.getColumns().add(new Column("Lectura Actual", 15, 1));//14
        tb.getColumns().add(new Column("Factor de Corrección", 15, 1));//15
        tb.getColumns().add(new Column("Consumo m3", 15, 1));//16
        tb.getColumns().add(new Column("Cargo Fijo", 25, 3));//17
        tb.getColumns().add(new Column("Cargo aplicado por Consumo $/m3", 25, 3));//18
        tb.getColumns().add(new Column("Facturación por Consumo", 25, 3));//19
        tb.getColumns().add(new Column("Refacturación m3", 25, 1));//20
        tb.getColumns().add(new Column("Refacturación $", 25, 3));//21
        tb.getColumns().add(new Column("Mora Acumulada", 25, 3));//22     
        tb.getColumns().add(new Column("Intereses por Mora Acumulado", 25, 3));//23
        tb.getColumns().add(new Column("Sanciones", 25, 3));//24
        tb.getColumns().add(new Column("Subsidio o Contribución $", 25, 3));//25
        tb.getColumns().add(new Column("Subsidio o Contribución %", 14, 5));//26
        tb.getColumns().add(new Column("Conexión", 14, 3));//27
        tb.getColumns().add(new Column("Intereses de Financiación Conexión", 14, 3));//28
        tb.getColumns().add(new Column("Suspensión y Reconexion", 14, 3));//29
        tb.getColumns().add(new Column("Corte y Reinstalación", 14, 3));//31
        tb.getColumns().add(new Column("Revisión de Instalación", 14, 3));//30
        tb.getColumns().add(new Column("Fecha de Revisión Instalación", 14, 4));//32
        tb.getColumns().add(new Column("Otros", 14, 3));//33
        tb.getColumns().add(new Column("Límite de Pago", 14, 4));//34
        tb.getColumns().add(new Column("Suspensión", 14, 4));//35
        tb.getColumns().add(new Column("Valor Total Facturado", 20, 3));//36
        tb.getColumns().add(new Column("Inf. Predial Utilizada", 20, 0));
        tb.getColumns().add(new Column("Catastro o Num predial", 40, 0));
        tb.getColumns().add(new Column("Hogar ICFB", 20, 0));
        tb.getColumns().add(new Column("Vivienta Int Prioritario", 20, 0));

        List<BillClieCau> caus = BillClieCau.getBySpan(spanId, conn);

        MySQLPreparedQuery qBill = new MySQLPreparedQuery("SELECT "
                + "b.bill_num, "//0
                + "b.creation_date, "//1
                + "b.id "//2                
                + "FROM bill_bill as b "
                + "WHERE b.total AND b.client_tank_id = ?1 AND b.bill_span_id = " + spanId + " "
                + "ORDER BY b.id ASC LIMIT 1 ", conn);

        // cartera
        MySQLPreparedQuery cartera = new MySQLPreparedQuery("SELECT SUM(p.value) FROM bill_plan p  WHERE p.doc_id = ?1  AND p.doc_type = 'fac' AND p.account_cred_id IN ( "
                + Accounts.C_CAR_GLP + ", "
                + Accounts.C_CAR_SRV + ", "
                + Accounts.C_CAR_FINAN_DEU + ", "
                + Accounts.C_CAR_CONTRIB + ", "
                + Accounts.C_CAR_INTE_CRE + ", "
                + Accounts.C_CAR_OLD + ")", conn);
        // intereses
        MySQLPreparedQuery interest = new MySQLPreparedQuery("SELECT SUM(p.value) FROM bill_plan p  WHERE p.doc_id = ?1  AND p.doc_type = 'fac' AND p.account_cred_id IN ( "
                + Accounts.C_INT_GLP + ", "
                + Accounts.C_INT_SRV + ", "
                + Accounts.C_INT_FINAN_DEU + ", "
                + Accounts.C_INT_CONTRIB + ", "
                + Accounts.C_INT_OLD + " )", conn
        );

        // conexión
        MySQLPreparedQuery conex = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.cli_tank_id = ?1 AND bill_span_id = " + spanId + " AND t.account_deb_id = " + Accounts.C_CUOTA_SER_CLI_SRV + " AND t.trans_type_id = " + Transactions.CAUSA_SERV_CONN, conn);
        // financiación conexión
        MySQLPreparedQuery intFinaConex = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.cli_tank_id = ?1 AND bill_span_id = " + spanId + " AND t.account_deb_id = " + Accounts.C_CUOTA_INT_CRE + " AND t.trans_type_id = " + Transactions.CAUSA_INTE_CRE_CONN, conn);
        // suspensión directa a la acc
        MySQLPreparedQuery suspVal = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.cli_tank_id = ?1 AND bill_span_id = " + spanId + " AND t.account_deb_id = " + Accounts.C_RECON, conn);
        // revisiones
        MySQLPreparedQuery checkVal = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.cli_tank_id = ?1 AND bill_span_id = " + spanId + " AND t.account_deb_id = " + Accounts.C_CUOTA_SER_CLI_SRV + " AND t.trans_type_id = " + Transactions.CAUSA_SERV_CHECK, conn);
        // reconexión por suspensión como servicio usuario
        MySQLPreparedQuery suspReconVal = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.cli_tank_id = ?1 AND bill_span_id = " + spanId + " AND t.account_deb_id = " + Accounts.C_CUOTA_SER_CLI_SRV + " AND t.trans_type_id = " + Transactions.CAUSA_SERV_SUSP_RECONN, conn);
        // reconexión por corte como servicio usuario
        MySQLPreparedQuery cutReconVal = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.cli_tank_id = ?1 AND bill_span_id = " + spanId + " AND t.account_deb_id = " + Accounts.C_CUOTA_SER_CLI_SRV + " AND t.trans_type_id = " + Transactions.CAUSA_SERV_CUT_RECONN, conn);
        //total facturado
        MySQLPreparedQuery totalFac = new MySQLPreparedQuery("SELECT SUM(p.value) FROM bill_plan p  WHERE p.doc_id = ?1  AND p.doc_type = 'fac' AND p.trans_type_id = " + Transactions.PAGO_BANCO, conn);

        MySQLPreparedQuery clientQ = new MySQLPreparedQuery("SELECT "
                + "c.code, "//0
                + "IFNULL(rb.orig_beg_read, r.last_reading), "//1
                + "IFNULL(rb.orig_end_read, r.reading), "//2                
                + "n.type, "//3
                + "TRIM(CONCAT(c.address, ' ', IFNULL(n.name, ''))), "//4
                + "IF(r.critical_reading IS NOT NULL, 'E', IF(cons_type IS NULL, 'R', IF(cons_type = 'AVG', 'E', 'R'))), "//5
                + "cad_info,"//6
                + "cadastral_code, "//7
                + "icfb_home, "//8
                + "priority_home "//9
                + "FROM bill_client_tank c "
                + "INNER JOIN sigma.neigh n ON n.id = c.neigh_id "
                + "INNER JOIN bill_reading r ON r.client_tank_id = c.id and r.span_id = " + spanId + " AND c.id = ?1 "
                + "LEFT JOIN sigma.bill_reading_fault f ON f.id = r.fault_id "
                + "LEFT JOIN bill_clie_rebill rb ON rb.client_id = r.client_tank_id AND rb.error_span_id = r.span_id AND rb.active "
                + "", conn);

        MySQLPreparedQuery checkDateQ = new MySQLPreparedQuery(""
                + "SELECT MAX(chk_date) "
                + "FROM bill_inst_check "
                + "WHERE client_id = ?1 AND chk_date <= ?2"
                + "", conn);

        Map<String, String> neighType = new HashMap<>();
        neighType.put("b_rul", "R");
        neighType.put("b_urb", "U");
        neighType.put("vda", "C");

        if (!caus.isEmpty()) {

            List<SuiNet> dataSui = new ArrayList<>();

            for (int i = 0; i < caus.size(); i++) {
                BillClieCau cau = caus.get(i);
                SuiNet sn = new SuiNet();
                clientQ.setParameter(1, cau.clientId);
                Object[] clieRow = clientQ.getRecord();
                qBill.setParameter(1, cau.clientId);
                Object[] billRow = qBill.getRecord();
                int billId = cast.asInt(billRow, 2);
                totalFac.setParameter(1, billId);
                cartera.setParameter(1, billId);
                interest.setParameter(1, billId);
                List<BillClieRebill> rebills = BillClieRebill.getByClientRebillSpan(cau.clientId, spanId, conn);

                conex.setParameter(1, cau.clientId);
                intFinaConex.setParameter(1, cau.clientId);
                suspVal.setParameter(1, cau.clientId);
                checkVal.setParameter(1, cau.clientId);
                suspReconVal.setParameter(1, cau.clientId);
                cutReconVal.setParameter(1, cau.clientId);

                sn.clieNiu = cast.asString(clieRow, 0);
                sn.daneCode = daneCode;
                sn.locationType = neighType.get(cast.asString(clieRow, 3));
                sn.address = cast.asString(clieRow, 4);
                sn.billNum = cast.asString(billRow, 0);
                sn.billCreation = cast.asDate(billRow, 1);
                sn.spanBeg = span.beginDate;
                sn.spanEnd = span.endDate;
                sn.cadInfo = cast.asString(clieRow, 6);
                sn.cadastralCode = cast.asString(clieRow, 7);
                sn.icfbHome = cast.asBoolean(clieRow, 8);
                sn.priorityHome = cast.asBoolean(clieRow, 9);

                if (cau.sector.equals("r")) {
                    sn.sector = cau.stratum + "";
                } else {
                    sn.sector = cau.sector.toUpperCase();
                }

                sn.readingType = cast.asString(clieRow, 5);
                sn.lastRead = cast.asBigDecimal(clieRow, 1);
                sn.curRead = cast.asBigDecimal(clieRow, 2);

                if (cau.meterFactor.compareTo(BigDecimal.ONE) != 0) {
                    sn.factor = cau.meterFactor;
                } else {
                    sn.factor = span.fadj;
                }

                sn.consumoM3 = cau.m3NoSubs.add(cau.m3Subs);
                sn.facCargoFijo = cau.fixedCharge;

                DtoRangeToPrint[] ranges = DtoRangeToPrint.getRanges(cau, span, inst);

                if (ranges.length == 2) {
                    if (ranges[1].cons.compareTo(BigDecimal.ZERO) != 0) {
                        sn.tariff = ranges[1].vunit;
                    } else {
                        sn.tariff = ranges[0].vunit;
                    }
                } else {
                    sn.tariff = ranges[0].vunit;
                }

                sn.facConsumo = cau.valConsNoSubs.add(cau.valConsSubs);
                sn.rebillM3 = BigDecimal.ZERO;
                sn.rebillVal = BigDecimal.ZERO;

                for (int j = 0; j < rebills.size(); j++) {
                    BillClieRebill rb = rebills.get(j);
                    sn.rebillM3 = sn.rebillM3.add(rb.diffM3NoSubs.add(rb.diffM3Subs));
                    sn.rebillVal = sn.rebillVal.add(rb.diffFixedCharge);
                    sn.rebillVal = sn.rebillVal.add(rb.diffValConsNoSubs);
                    sn.rebillVal = sn.rebillVal.add(rb.diffValConsSubs);
                    sn.rebillVal = sn.rebillVal.add(rb.diffValContrib);
                    sn.rebillVal = sn.rebillVal.add(rb.diffValExcContrib);
                    sn.rebillVal = sn.rebillVal.subtract(rb.diffValSubs);
                }

                sn.moraAcumulada = cartera.getAsBigDecimal(true);
                sn.interesAcumulado = interest.getAsBigDecimal(true);

                sn.subsidyValue = cau.valSubs;
                sn.contribValue = cau.valContrib;
                if (sn.subsidyValue.compareTo(BigDecimal.ZERO) > 0) {
                    sn.subsidyPercent = cau.stratum == 1 ? span.subPerc1 : span.subPerc2;
                }

                if (sn.contribValue.compareTo(BigDecimal.ZERO) > 0) {
                    sn.contribPercent = cau.sector.equals("r") ? span.contribR : span.contribNr;
                }

                sn.conexion = conex.getAsBigDecimal(true);
                sn.interesFinanciacionConexion = intFinaConex.getAsBigDecimal(true);
                sn.suspReconect = suspVal.getAsBigDecimal(true).add(suspReconVal.getAsBigDecimal(true));
                sn.netCheckCost = checkVal.getAsBigDecimal(true);
                sn.cutReconnect = cutReconVal.getAsBigDecimal(true);

                if (sn.netCheckCost.compareTo(BigDecimal.ZERO) != 0) {
                    checkDateQ.setParameter(1, cau.clientId);
                    checkDateQ.setParameter(2, Dates.trimDate(sn.billCreation));
                    sn.netCheckDate = checkDateQ.getAsDate();
                }

                BigDecimal total = totalFac.getAsBigDecimal(true);
                BigDecimal subt = BigDecimal.ZERO;
                subt = subt.add(sn.facCargoFijo);
                subt = subt.add(sn.facConsumo);
                subt = subt.add(sn.rebillVal);
                subt = subt.add(sn.moraAcumulada);
                subt = subt.add(sn.interesAcumulado);
                subt = subt.subtract(sn.subsidyValue);
                subt = subt.add(sn.contribValue);
                subt = subt.add(sn.conexion);
                subt = subt.add(sn.interesFinanciacionConexion);
                subt = subt.add(sn.suspReconect);
                subt = subt.add(sn.netCheckCost);
                subt = subt.add(sn.cutReconnect);

                sn.others = total.subtract(subt);
                sn.limitDt = span.limitDate;
                sn.suspDt = span.suspDate;
                sn.total = total;

                if (!sysCfg.skipMinCons || sn.total.compareTo(span.minConsValue) >= 0) {
                    dataSui.add(sn);
                }

            }
            if (!dataSui.isEmpty()) {
                Object[][] data = new Object[dataSui.size()][38];
                for (int i = 0; i < dataSui.size(); i++) {
                    SuiNet sn = dataSui.get(i);
                    data[i][0] = sn.clieNiu;
                    data[i][1] = sn.daneCode;
                    data[i][2] = sn.locationType;
                    data[i][3] = sn.address;
                    data[i][4] = sn.billNum;
                    data[i][5] = sn.billCreation;
                    data[i][6] = sn.spanBeg;
                    data[i][7] = sn.spanEnd;
                    data[i][8] = sn.sector;
                    data[i][9] = sn.readingType;
                    data[i][10] = sn.lastRead;
                    data[i][11] = sn.curRead;
                    data[i][12] = sn.factor;
                    data[i][13] = sn.consumoM3;
                    data[i][14] = sn.facCargoFijo;
                    data[i][15] = sn.tariff;
                    data[i][16] = sn.facConsumo;
                    data[i][17] = sn.rebillM3;
                    data[i][18] = sn.rebillVal;
                    data[i][19] = sn.moraAcumulada;
                    data[i][20] = sn.interesAcumulado;
                    data[i][21] = null;//sanciones
                    data[i][22] = (sn.subsidyValue != null ? sn.subsidyValue.negate() : BigDecimal.ZERO).add(sn.contribValue != null ? sn.contribValue : BigDecimal.ZERO);
                    data[i][23] = (sn.subsidyPercent != null ? sn.subsidyPercent : BigDecimal.ZERO).add(sn.contribPercent != null ? sn.contribPercent : BigDecimal.ZERO).divide(new BigDecimal(100), 4, RoundingMode.HALF_EVEN);
                    data[i][24] = sn.conexion;
                    data[i][25] = sn.interesFinanciacionConexion;
                    data[i][26] = sn.suspReconect;
                    data[i][27] = sn.cutReconnect;
                    data[i][28] = sn.netCheckCost;
                    data[i][29] = sn.netCheckDate;
                    data[i][30] = sn.others;
                    data[i][31] = sn.limitDt;
                    data[i][32] = sn.suspDt;
                    data[i][33] = sn.total;
                    data[i][34] = sn.cadInfo;
                    data[i][35] = sn.cadastralCode;
                    data[i][36] = sn.icfbHome ? "1" : "2";
                    data[i][37] = sn.priorityHome ? "1" : "2";

                }
                tb.setData(data);
                rep.getTables().add(tb);
            }
        }
        return rep;
    }
    
/*======================== GRC6 ========================*/
    public static MySQLReport getRptGRC6 (int spanId, BillInstance inst,SysCfg sysCfg, Connection conn)throws Exception{
        MySQLReport rep = new MySQLReport("GRC6  Revisión de Componentes Suministro y Transporte del Mercado" +
"Regulado", "", "", MySQLQuery.now(conn));
        BillSpan span = new BillSpan().select(spanId, conn);
        rep.getSubTitles().add("Periodo: " + span.getConsLabel());
        rep.getSubTitles().add("Instancia: " + inst.name);
        if (span.state.equals("cons")) {
            rep.getSubTitles().add("¡ALERTA, INFORMACIÓN PROVISIONAL! CAUSACIÓN EN CURSO");
        }
        // Definir tipo de dato
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.0000"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.0000"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "0.00"));//3
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.0"));//4
        //Tamaño de columnas
        rep.setZoomFactor(80);
        rep.setMultiRowTitles(3);
        Table tb = new Table("GRC6  Revisión de Componentes Suministro y Transporte del Mercado" +
"Regulado");
        //conexiones de campos formato Ecxel 
        tb.getColumns().add(new Column("ID Mercado", 14, 0));//1
        tb.getColumns().add(new Column("Tipo de Gas", 14, 0));//2
        tb.getColumns().add(new Column("Numero de operación SEGAS", 14,0));//3
        tb.getColumns().add(new Column("Tipo Contrato", 14, 0));//4
        tb.getColumns().add(new Column("Precio Suministro",14,0));//5
        tb.getColumns().add(new Column("Cantidad Contratada Suministro",14,0));//6
        tb.getColumns().add(new Column("Cantidad Facturada Suministro",14,0));//7
        tb.getColumns().add(new Column("Costos de las Compras de Gas -CCG",14,0));//8
        tb.getColumns().add(new Column("Ruta de Transporte", 14, 0));//9
        tb.getColumns().add(new Column("Tarifa", 14, 0));//10
        tb.getColumns().add(new Column("Capacidad Contratada Transporte", 14, 0));//11
        tb.getColumns().add(new Column("Capacidad Facturada Transporte", 14, 0));//12
        tb.getColumns().add(new Column("Costos de Transporte de Gas -CCT", 14, 0));//13
        tb.getColumns().add(new Column("Otros Conceptos", 14, 0));//14
        tb.getColumns().add(new Column("Descripción Otros Conceptos", 14, 0));//15
        tb.getColumns().add(new Column("TRM pactada", 14, 0));//16
        tb.getColumns().add(new Column("Poder Calorífico", 14, 0));//17
        tb.getColumns().add(new Column("Constante de Conversión", 14, 0));//18
        Object[][] data = new MySQLQuery ("Select  "          
                    +"''," //1
                    +"''," //2
                    +"''," //3
                    +"''," //4
                    +"''," //5
                    +"''," //6
                    +"''," //7
                    +"''," //8
                    +"''," //9
                    +"''," //10
                    +"''," //11
                    +"''," //12
                    +"''," //13
                    +"''," //14
                    +"''," //15
                    +"''," //16
                    +"''," //17
                    +"''" //18

                + "from bill_client_tank where id =10" ).getRecords(conn);
        
            tb.setData(data);
            rep.getTables().add(tb);
           
        return rep;
    }
    
    
      
/*====================================*/

    public static MySQLReport getRptGRC1(int spanId, BillInstance inst,SysCfg sysCfg, Connection conn)throws Exception{
        MySQLReport rep = new MySQLReport("GRC1 Información Comercial de Usuarios Regulados", "", "", MySQLQuery.now(conn));
        BillSpan span = new BillSpan().select(spanId, conn);
        rep.getSubTitles().add("Periodo: " + span.getConsLabel());
        rep.getSubTitles().add("Instancia: " + inst.name);
        if (span.state.equals("cons")) {
            rep.getSubTitles().add("¡ALERTA, INFORMACIÓN PROVISIONAL! CAUSACIÓN EN CURSO");
        }

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.0000"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.0000"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "0.00"));//3
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.0"));//4

        rep.setZoomFactor(80);
        rep.setMultiRowTitles(2);
        Table tb = new Table("GRC1 Información Comercial de Usuarios Regulados");
        //conexiones de campos formato Ecxel 
        tb.getColumns().add(new Column("NIU", 14, 0));//1
        tb.getColumns().add(new Column("Tipo de Gass", 10, 0));//2
        tb.getColumns().add(new Column("ID Factura", 14,0));//3
        tb.getColumns().add(new Column("Tipo de Factura", 10, 0));//4
        tb.getColumns().add(new Column("Fecha de Expedición de la Factura",14,0));//5
        tb.getColumns().add(new Column("Fecha de Inicio Periodo de Facturación",14,0));//6
        tb.getColumns().add(new Column("Fecha de Terminación del Periodo de Facturación",14,0));//7
        tb.getColumns().add(new Column("Predios en Condiciones Especiales", 14, 0));//8
        tb.getColumns().add(new Column("Tipo de Lectura", 10, 0));//9
        tb.getColumns().add(new Column("Factor De Poder Calorífico - Fpc", 14, 1));//10
        tb.getColumns().add(new Column("Lectura Anterior", 14, 1));//11
        tb.getColumns().add(new Column("Fecha Lectura Anterior", 14, 0));//12
        tb.getColumns().add(new Column("Lectura Actual", 14, 1));//13
        tb.getColumns().add(new Column("Fecha Lectura Actual", 14, 0));//14
        tb.getColumns().add(new Column("Número de Días Facturados", 14, 0));//15
        tb.getColumns().add(new Column("Factor de corrección utilizado", 10, 4));//16
        tb.getColumns().add(new Column("Consumo", 10, 1));//17
        tb.getColumns().add(new Column("Cuv Cargo Aplicado por Consumo", 14, 1));//18
        tb.getColumns().add(new Column("Facturación por Consumo", 14, 1));//19
        tb.getColumns().add(new Column("Facturación por Cargo Fijo", 14, 1));//20
        tb.getColumns().add(new Column("Valor por Mora Acumulado", 14, 1));//21
        tb.getColumns().add(new Column("Intereses por Mora Acumulado", 14, 1));//22
        tb.getColumns().add(new Column("Valor del Subsidio o Contribución", 14, 1));//23
        tb.getColumns().add(new Column("Porcentaje de Subsidio o Contribución Aplicado", 10, 3));//24
        tb.getColumns().add(new Column("Valor Cuota de Conexión", 14, 1));//25
        tb.getColumns().add(new Column("Intereses Financiación Conexión", 14, 1));//26
        tb.getColumns().add(new Column("Suspensión y Reconexión", 14, 1));//27
        tb.getColumns().add(new Column("Corte y Reinstalación", 14, 1));//28
        tb.getColumns().add(new Column("Tipo Revisión Instalación Interna", 14, 0));//29
        tb.getColumns().add(new Column("Fecha de la Revisión", 14, 0));//30
        tb.getColumns().add(new Column("Valor Otros Conceptos", 14, 1));//31
        tb.getColumns().add(new Column("Valor Intereses Otros Conceptos", 14, 1));//32
        tb.getColumns().add(new Column("Descripción Otros", 20, 0));//33
        tb.getColumns().add(new Column("Refacturación de Consumos", 14, 1));//34
        tb.getColumns().add(new Column("Valor Refacturación", 14, 1));//35
        tb.getColumns().add(new Column("Valor Refacturación Subsidio o Contribución", 14, 1));//36
        tb.getColumns().add(new Column("Fecha Límite de Pago", 14, 0));//37
        tb.getColumns().add(new Column("Fecha de Suspensión", 14, 0));//38
        tb.getColumns().add(new Column("Valor Total Facturado", 14, 1));//39
        Object[][] data = new MySQLQuery("SELECT "
                + "c.code AS 'NIU', "//1
                + "3 AS 'Tipo de Gas', "//2
                + "f.bill_num AS 'ID Factura', "//3
                + "(CASE WHEN (bcc.val_cons_no_subs + bcc.val_cons_subs)>= 0 AND (bcr.diff_val_cons_no_subs is null) THEN 1 WHEN (bcc.val_cons_no_subs + bcc.val_cons_subs)>=0 AND (bcr.diff_val_cons_no_subs is not null) THEN 3 WHEN (bcc.val_cons_no_subs + bcc.val_cons_subs) is null AND (bcr.diff_val_cons_no_subs is not null) THEN 2 ELSE 3 END) AS 'Tipo de Factura', "//4
                + "DATE_FORMAT(f.creation_date, '%d-%m-%Y') AS 'Fecha de Expedición de la Factura', "//5
                + "DATE_FORMAT(s.begin_date, '%d-%m-%Y') AS 'Fecha de Inicio Periodo de Facturación', "//6
                + "DATE_FORMAT(s.end_date, '%d-%m-%Y') AS 'Fecha de Terminación del Periodo de Facturación', "//7
                + "(CASE WHEN c.icfb_home = 1 THEN 1 WHEN c.priority_home = 1 THEN 2 WHEN c.asent_indigena = 1 THEN 3 ELSE 4 END) AS 'Predios en Condiciones Especiales', "//8
                + "(SELECT IF(br.critical_reading IS NOT NULL, '2', IF(cons_type IS NULL, '1', IF(cons_type = 'AVG', '2', '1'))) FROM bill_client_tank c2 INNER JOIN sigma.neigh sn ON sn.id = c2.neigh_id INNER JOIN bill_reading br ON br.client_tank_id = c2.id LEFT JOIN sigma.bill_reading_fault brf ON brf.id = br.fault_id LEFT JOIN bill_clie_rebill rcb ON rcb.client_id = br.client_tank_id AND rcb.error_span_id = br.span_id AND rcb.active WHERE br.span_id = s.id AND c2.id = c.id) AS 'Tipo de Lectura', "//9
                + "s.power AS 'Factor De Poder Calorífico -Fpc', "//10
                + "IFNULL(bcr2.orig_beg_read, br4.last_reading) AS 'Lectura Anterior', "//11
                + "(SELECT DATE_FORMAT(f2.creation_date,'%d-%m-%Y') FROM bill_bill f2 WHERE f2.bill_span_id = s.id-1 AND f2.client_tank_id = c.id LIMIT 1) AS 'Fecha Lectura Anterior', "//12
                + "IFNULL(bcr2.orig_end_read,br4.reading) AS 'Lectura Actual', "//13
                + "DATE_FORMAT(f.creation_date, '%d-%m-%Y') AS 'Fecha Lectura Actual', "//14
                + "DATEDIFF(s.end_date, s.begin_date)+1 AS 'Número de días facturados', "//15
                + "TRUNCATE(s.fadj,1) AS 'Factor de corrección utilizado', "//16
                + "bcc.m3_subs+ bcc.m3_no_subs AS 'Consumo', "//17
                + "s.cuv_r AS 'Cuv Cargo Aplicado por Consumo', "//18
                + "(bcc.val_cons_subs + bcc.val_cons_no_subs) AS 'Facturación por Consumo', "//19
                + "bcc.fixed_charge AS 'Facturación por Cargo Fijo', "//20
                + "(SELECT IFNULL(SUM(bp.value),0) FROM bill_plan bp  WHERE bp.doc_id =f.id  AND bp.doc_type = 'fac' AND bp.account_cred_id IN (7,17,28,30,21,8)) AS 'Valor por Mora Acumulado', "//21
                + "(SELECT IFNULL(SUM(bp2.value),0) FROM bill_plan bp2  WHERE bp2.doc_id = f.id  AND bp2.doc_type = 'fac' AND bp2.account_cred_id IN (6,18, 29,31,9)) AS 'Intereses por Mora Acumulado',"//22
                + "(bcc.val_subs+bcc.val_contrib) AS 'Valor del Subsidio o Contribución', "//23
                + "(CASE WHEN bcc.stratum = 1 THEN TRUNCATE((s.sub_perc_1/100),4) WHEN bcc.stratum = 2 THEN TRUNCATE((s.sub_perc_2/100),4) ELSE 0 END) AS 'Porcentaje de Subsidio o Contribución Aplicado', "//24
                + "(SELECT IFNULL(SUM(t.value),0) FROM bill_transaction t WHERE t.cli_tank_id = c.id AND bill_span_id = s.id AND t.account_deb_id = 19 AND t.trans_type_id = 18) AS 'Valor Cuota de Conexión', "//25
                + "(SELECT IFNULL(SUM(t.value),0) FROM bill_transaction t WHERE t.cli_tank_id = c.id AND bill_span_id = s.id AND t.account_deb_id = 20 AND t.trans_type_id = 21) AS 'Intereses Financiación Conexión', "//26
                + "(IFNULL((SELECT SUM(t.value) FROM bill_transaction t WHERE t.cli_tank_id = c.id AND bill_span_id = s.id AND t.account_deb_id = 5),0)+(IFNULL((SELECT SUM(t.value) FROM bill_transaction t WHERE t.cli_tank_id = c.id AND bill_span_id = s.id AND t.account_deb_id = 19 AND t.trans_type_id = 19),0))) AS 'Suspensión y Reconexión', "//27
                + "(SELECT IFNULL(SUM(t.value),0) FROM bill_transaction t WHERE t.cli_tank_id = c.id AND bill_span_id = s.id AND t.account_deb_id = 19 AND t.trans_type_id = 26) AS 'Corte y Reinstalación', "//28
                + "(SELECT MAX(sbict.code) FROM bill_inst_check bic INNER JOIN sigma.bill_inst_check_type sbict ON bic.type_id = sbict.id INNER JOIN sigma.bill_inst_inspector bii ON bic.inspector_id = bii.id WHERE bic.client_id = c.id LIMIT 1) AS 'Tipo Revisión Instalación Interna', "//29
                + "(SELECT DATE_FORMAT(bic2.chk_date, '%d-%m-%Y') FROM bill_inst_check bic2 LEFT JOIN sigma.bill_inst_check_type bict2 on  bict2.id =bic2.type_id WHERE bic2.client_id =c.id and bict2.code=(SELECT MAX(sbict2.code) FROM bill_inst_check bic3 INNER JOIN sigma.bill_inst_check_type sbict2 ON bic3.type_id = sbict2.id INNER JOIN sigma.bill_inst_inspector bii2 ON bic3.inspector_id = bii2.id WHERE bic3.client_id = c.id LIMIT 1) LIMIT 1) AS 'Fecha de la Revisión', "//30
                + "NULLIF(((IFNULL(CAST((SELECT (SUM(busf.value - busf.ext_pay)) FROM bill_user_service us INNER JOIN bill_service_type bst ON bst.id = us.type_id INNER JOIN bill_user_service_fee busf ON us.id = busf.service_id WHERE busf.value - busf.ext_pay > 0 AND us.bill_client_tank_id = c.id AND us.bill_span_id + busf.place = s.id AND bst.trans_type <> 'conn')AS DECIMAL(40,4)),0))+(IFNULL((SELECT value FROM bill_bill_pres WHERE bill_id =f.id and label LIKE 'Ajuste a la decena'),0)+(IFNULL((SELECT value FROM bill_plan p  WHERE p.doc_type = 'fac' AND p.trans_type_id =8 AND bill_span_id =s.id AND account_cred_id =27 AND cli_tank_id =c.id LIMIT 1),0)))),0) AS 'Valor Otros Conceptos', "//31
                + "(SELECT IF (SUM(busf.inter - busf.ext_inter + IFNULL(busf.inter_tax, 0) - IFNULL(busf.ext_inter_tax, 0))IS NULL, 0, SUM(busf.inter - busf.ext_inter + IFNULL(busf.inter_tax, 0) - IFNULL(busf.ext_inter_tax, 0))) FROM bill_user_service us INNER JOIN bill_service_type bst ON bst.id = us.type_id INNER JOIN bill_user_service_fee busf ON us.id = busf.service_id WHERE busf.value - busf.ext_pay > 0 AND us.bill_client_tank_id = c.id AND us.bill_span_id + busf.place = s.id AND bst.trans_type<>'conn') AS 'Valor Intereses Otros Conceptos', "//32
                + "CONCAT(IF((IFNULL((SELECT value FROM bill_bill_pres WHERE bill_id =f.id and label LIKE 'Ajuste a la decena'),0))<>0,'Ajuste a la decena',''),"
                + "IF((IFNULL((SELECT value FROM bill_plan p  WHERE p.doc_type = 'fac' AND p.trans_type_id =8 AND bill_span_id =s.id AND account_cred_id =27 AND cli_tank_id =c.id LIMIT 1),0))<>0,'-Financiación',''),"
                + "IF(((IFNULL(CAST((SELECT (SUM(busf.value - busf.ext_pay)) FROM bill_user_service us INNER JOIN bill_service_type bst ON bst.id = us.type_id INNER JOIN bill_user_service_fee busf ON us.id = busf.service_id WHERE busf.value - busf.ext_pay > 0 AND us.bill_client_tank_id = c.id AND us.bill_span_id + busf.place = s.id AND bst.trans_type <> 'conn')AS DECIMAL(40,4)),0)))<>0,CONCAT('-',(SELECT GROUP_CONCAT(CAST(bst.name AS CHAR CHARACTER SET utf8) SEPARATOR '-') FROM bill_user_service us INNER JOIN bill_service_type bst ON bst.id = us.type_id INNER JOIN bill_user_service_fee busf ON us.id = busf.service_id WHERE busf.value - busf.ext_pay > 0 AND us.bill_client_tank_id = c.id AND us.bill_span_id + busf.place = s.id AND bst.trans_type<>'conn')),'')) AS 'Descripción Otros',"//33
                + "(SELECT IF(SUM(diff_m3_subs+diff_m3_no_subs) IS NULL,0,diff_m3_subs+diff_m3_no_subs) FROM bill_clie_rebill WHERE rebill_span_id = s.id AND active = 1 AND client_id = c.id) AS 'Refacturación de Consumos', "//34
                + "bcr.diff_val_cons_subs+bcr.diff_val_cons_no_subs AS 'Valor Refacturación', "//35
                + "-bcr.diff_val_subs AS 'Valor Refacturación Subsidio o Contribución', "//36
                + "DATE_FORMAT(s.limit_date,'%d-%m-%Y')  AS 'Fecha Límite de Pago', "//37
                + "DATE_FORMAT(s.susp_date, '%d-%m-%Y') AS 'Fecha de Suspensión', "//38
                + "(SELECT IFNULL(SUM(bp3.value),0) FROM bill_plan bp3  WHERE bp3.doc_id = f.id  AND bp3.doc_type = 'fac' AND bp3.trans_type_id = 8) AS 'Valor Total Facturado' "//39
                + "FROM bill_bill f "
                + "LEFT JOIN bill_span s ON s.id = f.bill_span_id "
                + "LEFT JOIN bill_client_tank c ON f.client_tank_id =c.id "
                + "LEFT JOIN bill_clie_cau bcc ON bcc.client_id =f.client_tank_id AND bcc.span_id =f.bill_span_id "
                + "LEFT JOIN bill_clie_rebill bcr ON bcr.client_id =f.client_tank_id AND bcr.rebill_span_id =f.bill_span_id AND bcr.active =1 "
                + "LEFT JOIN bill_clie_rebill bcr2 ON bcr2.client_id =f.client_tank_id AND bcr2.error_span_id=f.bill_span_id AND bcr2.active =1 "
                + "LEFT JOIN bill_reading br4 ON br4.client_tank_id =f.client_tank_id AND br4.span_id =f.bill_span_id "
                + "WHERE f.bill_span_id ="+spanId+" "
                + "AND f.creation_date =(SELECT MIN(f2.creation_date) FROM bill_bill f2 WHERE f2.client_tank_id =c.id AND f2.bill_span_id=s.id)"
                + "ORDER BY f.bill_num").getRecords(conn);
        if(data.length>0){
            tb.setData(data);
            rep.getTables().add(tb);
        }   
        return rep;
    }
    
    public static MySQLReport getRptGRT1(int spanId, BillInstance inst,SysCfg sysCfg, Connection conn)throws Exception{
        MySQLReport rep = new MySQLReport("GRT1 Estructura Tarifaria de Gas Combustible por Redes", "", "", MySQLQuery.now(conn));
        BillSpan span = new BillSpan().select(spanId, conn);
        rep.getSubTitles().add("Periodo: " + span.getConsLabel());
        rep.getSubTitles().add("Instancia: " + inst.name);
        if (span.state.equals("cons")) {
            rep.getSubTitles().add("¡ALERTA, INFORMACIÓN PROVISIONAL! CAUSACIÓN EN CURSO");
        }

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.0000"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.0000"));//3
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "0.00"));//4
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.0"));//5

        rep.setZoomFactor(80);
        rep.setMultiRowTitles(2);
        Table tb = new Table("GRT1 Estructura Tarifaria de Gas Combustible por Redes");

        tb.getColumns().add(new Column("ID Mercado", 8, 0));//1
        tb.getColumns().add(new Column("Metodología",8,0));//2
        tb.getColumns().add(new Column("Tipo de gas", 8, 0));//3
        tb.getColumns().add(new Column("Tipo de usuario", 8, 0));//4
        tb.getColumns().add(new Column("Rango", 8, 0));//5
        tb.getColumns().add(new Column("Piso rango", 8, 0));//6
        tb.getColumns().add(new Column("Techo rango", 8, 0));//7
        tb.getColumns().add(new Column("CUv", 12, 1));//8
        tb.getColumns().add(new Column("Cuf", 12, 1));//9
        tb.getColumns().add(new Column("G", 12, 1));//10
        tb.getColumns().add(new Column("T o Tm ponderado", 15, 2));//11
        tb.getColumns().add(new Column("D", 12, 1));//12
        tb.getColumns().add(new Column("fPC", 12, 2));//13
        tb.getColumns().add(new Column("Cv", 8, 0));//14
        tb.getColumns().add(new Column("Cc", 8, 0));//15
        tb.getColumns().add(new Column("Cf", 12, 1));//16
        tb.getColumns().add(new Column("P de perdidas", 10, 1));//17
        tb.getColumns().add(new Column("CCG", 5, 0));//18
        tb.getColumns().add(new Column("V", 5, 0));//19
        tb.getColumns().add(new Column("TRM", 5, 0));//20
        tb.getColumns().add(new Column("d", 5, 0));//21
        tb.getColumns().add(new Column("Qreal", 5, 0));//22
        tb.getColumns().add(new Column("CTCG", 5, 0));//23
        tb.getColumns().add(new Column("IVE aplicado al gestor en suministro", 5, 0));//24
        tb.getColumns().add(new Column("IVE aplicado al comercializador en suministro", 5, 0));//25
        tb.getColumns().add(new Column("Vendedor de excedente de suministro", 5, 0));//26
        tb.getColumns().add(new Column("PMS", 15, 1));//27
        tb.getColumns().add(new Column("Cglp", 15, 1));//28
        tb.getColumns().add(new Column("CTT", 5, 0));//29
        tb.getColumns().add(new Column("CP", 5, 0));//30
        tb.getColumns().add(new Column("CTTG", 5, 0));//31
        tb.getColumns().add(new Column("IVE aplicado al gestor en transporte", 5, 0));//32
        tb.getColumns().add(new Column("IVE aplicado al comercializador en transporte", 5, 0));//33
        tb.getColumns().add(new Column("Vendedor de excedente de transporte", 5, 0));//34
        tb.getColumns().add(new Column("T de GLP por ductos", 5, 0));//35
        tb.getColumns().add(new Column("TVm de GLP", 5, 0));//36
        tb.getColumns().add(new Column("A", 5, 0));//37
        tb.getColumns().add(new Column("Costo Transporte", 5, 0));//38
        tb.getColumns().add(new Column("Tmo", 5, 0));//39
        tb.getColumns().add(new Column("Qo", 5, 0));//40
        tb.getColumns().add(new Column("TVm de GNC", 5, 0));//41
        tb.getColumns().add(new Column("Pm", 5, 0));//42
        tb.getColumns().add(new Column("QGNC", 5, 0));//43
        tb.getColumns().add(new Column("P de densidad", 5, 0));//44
        tb.getColumns().add(new Column("Fv", 12, 2));//45
        tb.getColumns().add(new Column("Qc", 5, 0));//46
        tb.getColumns().add(new Column("Im-1", 5, 0));//47
        tb.getColumns().add(new Column("Im-2", 5, 0));//48
        tb.getColumns().add(new Column("Qf", 5, 0));//49
        tb.getColumns().add(new Column("Dm", 5, 0));//50
        tb.getColumns().add(new Column("Q por rango", 5, 0));//51
        tb.getColumns().add(new Column("DAUNR", 8, 0));//52
        tb.getColumns().add(new Column("DAUR", 15, 2));//53
        tb.getColumns().add(new Column("Cons1", 15, 2));//54
        tb.getColumns().add(new Column("Cons2", 15, 2));//55
        tb.getColumns().add(new Column("CUEq1", 15, 2));//56
        tb.getColumns().add(new Column("CUEq2", 15, 2));//57
        tb.getColumns().add(new Column("Tarifa 1", 15, 2));//58
        tb.getColumns().add(new Column("Tarifa 2", 15, 2));//59
        tb.getColumns().add(new Column("%S1", 10, 4));//60
        tb.getColumns().add(new Column("%S2", 10, 4));//61
        tb.getColumns().add(new Column("IPC 0", 12, 2));//62
        tb.getColumns().add(new Column("IPC m-1", 12, 2));//63
        tb.getColumns().add(new Column("IPP 0", 12, 2));//64
        tb.getColumns().add(new Column("IPP m-1", 12, 2));//65
        tb.getColumns().add(new Column("D(AUR),k,m,j", 5, 0));//66
       

        Object[][] data = new MySQLQuery("SELECT "
                + "(SELECT bm.id_market FROM sigma.bill_market bm WHERE bm.id="+inst.marketId+") AS 'ID Mercado', "
                + "(SELECT gl.id FROM sigma.gt_law gl WHERE gl.active=1 LIMIT 1) AS 'Metodología', "//2
                + "3 AS 'Tipo de gas', "
                + "(CASE c.sector_type WHEN 'r' THEN 1 WHEN  'c' THEN 2 WHEN 'i' THEN 3 ELSE 8 END) AS 'Tipo de usuario', "//4
                + "(CASE WHEN c.sector_type='r' AND (SELECT gl2.id FROM sigma.gt_law gl2 WHERE gl2.active= 1 LIMIT 1)=2 THEN '' ELSE DATEDIFF(s.end_date, s.begin_date) END) AS 'Rango', "//5
                + "(CASE WHEN c.sector_type='r' AND (SELECT gl2.id FROM sigma.gt_law gl2 WHERE gl2.active= 1 LIMIT 1)=2 THEN '' ELSE ROUND(s.vital_cons,0) END) AS 'Piso rango', "//6
                + "(CASE WHEN c.sector_type='r' AND (SELECT gl2.id FROM sigma.gt_law gl2 WHERE gl2.active= 1 LIMIT 1)=2 THEN '' ELSE ROUND(s.vital_cons,0) END) AS 'Techo rango', "//7
                + "(CASE c.sector_type WHEN 'r' THEN s.cuv_r ELSE s.cuv_nr END) AS 'CUv', "//8
                + "s.cuf AS 'Cuf', "//9
                + "CAST((s.pms/s.cglp)*s.fv AS DECIMAL(40,5)) AS 'G', "//10
                + "(s.tv*s.fv) AS 'T o Tm ponderado', "//11
                + "(CASE c.sector_type WHEN 'r' THEN (s.d_inv_r+s.d_aom_r) ELSE (s.d_inv_nr+s.d_aom_nr) END) AS 'D', "//12
                + "s.fpc AS 'fPC', "//13
                + "'' AS 'Cv', "//14
                + "'' AS 'Cc', "//15
                + "s.cuf AS 'CF', "//16
                + "0 AS 'P de perdidas', "//17
                + "'' AS 'CCG', "//18
                + "'' AS 'V', "//19
                + "'' AS 'TRM', "//20
                + "'' AS 'd', "//21
                + "'' AS 'Qreal', "//22
                + "'' AS 'CTCG', "//23
                + "'' AS 'IVE aplicado al gestor en suministro', "//24
                + "'' AS 'IVE aplicado al comercializador en suministro', "//25
                + "'' AS 'Vendedor de excedente de suministro', "//26
                + "s.pms AS 'PMS', "//27
                + "s.cglp AS 'Cglp', "//28
                + "'' AS 'CTT', "//29
                + "'' AS 'CP', "//30
                + "'' AS 'CTTG', "//31
                + "'' AS 'IVE aplicado al gestor en transporte', "//32
                + "'' AS 'IVE aplicado al comercializador en transporte', "//33
                + "'' AS 'Vendedor de excedentre de transporte', "//34
                + "'' AS 'T de GLP por ductos', "//35
                + "'' AS 'TVm de GLP', "//36
                + "'' AS 'A', "//37
                + "'' AS 'Costo Transporte', "//38
                + "'' AS 'Tmo', "//39
                + "'' AS 'Qo', "//40
                + "'' AS 'TVm de GNC', "//41
                + "'' AS 'Pm', "//42
                + "'' AS 'QGNC', "//43
                + "'' AS 'P de densidad', "//44
                + "s.fv AS 'Fv', "//45
                + "'' AS 'Qc', "//46
                + "'' AS 'lm-1', "//47
                + "'' AS 'lm-2', "//48
                + "'' AS 'Qf', "//49
                + "'' AS 'Dm', "//50
                + "'' AS 'Q por rango', "//51
                + "(CASE c.sector_type WHEN 'r' THEN '' ELSE (s.d_inv_nr+s.d_aom_nr) END) AS 'DAUNR', "//52
                + "(CASE c.sector_type WHEN 'r' THEN (s.d_inv_r+s.d_aom_r) END) AS 'DAUR', "//53
                + "IF((SELECT COUNT(*) FROM bill_clie_cau WHERE span_id = s.id-1 AND stratum = 1 AND m3_subs > 0)=0,s.vital_cons,((SELECT SUM(m3_subs) FROM bill_clie_cau WHERE span_id = s.id-1 AND stratum = 1 AND m3_subs > 0)/(SELECT COUNT(*) FROM bill_clie_cau WHERE span_id = s.id-1 AND stratum = 1 AND m3_subs > 0))) AS 'Cons1', "//54
                + "IF((SELECT COUNT(*) FROM bill_clie_cau WHERE span_id = s.id-1 AND stratum = 2 AND m3_subs > 0)=0,s.vital_cons,((SELECT SUM(m3_subs) FROM bill_clie_cau WHERE span_id = s.id-1 AND stratum = 2 AND m3_subs > 0)/(SELECT COUNT(*) FROM bill_clie_cau WHERE span_id = s.id-1 AND stratum = 2 AND m3_subs > 0))) AS 'Cons2', "//55
                + "s.c_eq_1 AS 'CUEq1', "//56
                + "s.c_eq_2 AS 'CUEq2', "//57
                + "s.raw_tarif_1 AS 'Tarifa 1', "//58
                + "s.raw_tarif_2 AS 'Tarifa 2', "//59
                + "ROUND((s.sub_perc_1/100),2) AS '%S1', "//60
                + "ROUND((s.sub_perc_2/100),2) AS '%S2',"//61
                + "(SELECT bpi.ipc FROM sigma.bill_price_index bpi WHERE bpi.`month`=(SELECT bm2.cf_prod_base_month FROM sigma.bill_market bm2 WHERE bm2.id="+inst.marketId+")) AS 'IPC 0', "//62
                + "(SELECT bpi2.ipc FROM sigma.bill_price_index bpi2 WHERE YEAR(bpi2.`month`)=YEAR(s.begin_date) AND MONTH(bpi2.`month`)=MONTH(s.begin_date) AND DAY(bpi2.`month`)='01') AS 'IPC m-1', "//63
                + "(SELECT bpi.ipp FROM sigma.bill_price_index bpi WHERE bpi.`month`=(SELECT bm2.cf_prod_base_month FROM sigma.bill_market bm2 WHERE bm2.id="+inst.marketId+")) AS 'IPP 0', "//64
                + "(SELECT bpi2.ipp FROM sigma.bill_price_index bpi2 WHERE YEAR(bpi2.`month`)=YEAR(s.begin_date) AND MONTH(bpi2.`month`)=MONTH(s.begin_date) AND DAY(bpi2.`month`)='01') AS 'IPP m-1', "//65
                + "'' AS 'D(AUR),k,m,j' "//66                
                + "FROM bill_bill f "
                        + "LEFT JOIN bill_span s ON s.id = f.bill_span_id "
                        + "LEFT JOIN bill_client_tank c ON f.client_tank_id =c.id "
                        + "WHERE f.bill_span_id = "+spanId+" "
                        + "AND f.creation_date =(SELECT MIN(f2.creation_date) FROM bill_bill f2 WHERE f2.client_tank_id =c.id AND f2.bill_span_id=s.id) "
                        + "ORDER BY f.bill_num").getRecords(conn);
        
        //Object[][] data = dataQ.getRecords(conn);
        if (data != null && data.length > 0) {
            rep.getTables().add(tb);
            tb.setData(data);
        }
        return rep;
    }
    
    public static MySQLReport getRptGRCS2(int spanId, BillInstance inst,SysCfg sysCfg, Connection conn)throws Exception{
        MySQLReport rep = new MySQLReport("GRT1 Estructura Tarifaria de Gas Combustible por Redes", "", "", MySQLQuery.now(conn));
        BillSpan span = new BillSpan().select(spanId, conn);
        rep.getSubTitles().add("Periodo: " + span.getConsLabel());
        rep.getSubTitles().add("Instancia: " + inst.name);
        if (span.state.equals("cons")) {
            rep.getSubTitles().add("¡ALERTA, INFORMACIÓN PROVISIONAL! CAUSACIÓN EN CURSO");
        }

        rep.setZoomFactor(80);
        rep.setMultiRowTitles(2);
        Table tb = new Table("GRT1 Estructura Tarifaria de Gas Combustible por Redes");

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        
        tb.getColumns().add(new Column("ID Mercado", 8, 0));//1
        tb.getColumns().add(new Column("Metodología",8,0));//2
        
        Object[][] data=new MySQLQuery("select id, code from bill_client_tank").getRecords(conn);
        
        
        
        if (data != null && data.length > 0) {
            rep.getTables().add(tb);
            tb.setData(data);
        }
        return rep;
    }
    
    public static MySQLReport getRptGRS1(int spanId, BillInstance inst,SysCfg sysCfg, Connection conn)throws Exception{
        MySQLReport rep = new MySQLReport("GRS1 Información de Suspensiones", "", "", MySQLQuery.now(conn));
        BillSpan span = new BillSpan().select(spanId, conn);
        rep.getSubTitles().add("Periodo: " + span.getConsLabel());
        rep.getSubTitles().add("Instancia: " + inst.name);
        if (span.state.equals("cons")) {
            rep.getSubTitles().add("¡ALERTA, INFORMACIÓN PROVISIONAL! CAUSACIÓN EN CURSO");
        }

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0

        rep.setZoomFactor(80);
        rep.setMultiRowTitles(2);
        Table tb = new Table("GRS1 Información de Suspensiones");
        
        tb.getColumns().add(new Column("ID Mercado", 10, 0));//1
        tb.getColumns().add(new Column("Código DANE", 14, 0));//2
        tb.getColumns().add(new Column("¿Suspensión fue programada?", 10, 0));//2
        tb.getColumns().add(new Column("Código de evento", 10, 0));//2
        tb.getColumns().add(new Column("Tipo de Gas", 10, 0));//2
        tb.getColumns().add(new Column("Fecha de inicio de la suspensión", 14, 0));//2
        tb.getColumns().add(new Column("Hora de inicio de la suspensión", 14, 0));//2
        tb.getColumns().add(new Column("Fecha final de la suspensión", 14, 0));//2
        tb.getColumns().add(new Column("Hora final de la suspensión", 14, 0));//2
        tb.getColumns().add(new Column("Tipo de suspensión", 10, 0));//2
        tb.getColumns().add(new Column("Origen de la suspensión", 10, 0));//2
        tb.getColumns().add(new Column("Generó compensación", 10, 0));//2
        tb.getColumns().add(new Column("Número de suscriptores afectados", 10, 0));//2
        tb.getColumns().add(new Column("Medio de Comunicación", 10, 0));//2
        tb.getColumns().add(new Column("Fecha de publicación", 14, 0));//2
        tb.getColumns().add(new Column("Observaciones", 40, 0));//2
        
        Object[] dateSpan=new MySQLQuery("SELECT s.begin_date, s.end_date "
                + "FROM bill_span s WHERE s.id="+spanId+" LIMIT 1").getRecord(conn);
            
        Object[][] noSched=new MySQLQuery("SELECT "
                + "(SELECT bm.id_market FROM sigma.bill_market bm WHERE bm.id="+inst.marketId+") AS 'ID Mercado', "
                + "(SELECT dp.code FROM sigma.bill_instance bi LEFT JOIN sigma.dane_poblado dp on dp.id=bi.pob_id WHERE bi.id="+inst.id+") AS 'Código DANE', "
                + "2 AS '¿Suspensión fue programada?', "
                + "CONCAT(bsf.id,'2') AS 'Código de evento', "
                + "3 AS 'Tipo de Gas', "
                + "DATE_FORMAT(bsf.beg_dt,'%d-%m-%Y') AS 'Fecha de inicio de la suspensión', "
                + "DATE_FORMAT(bsf.beg_dt ,'%H:%i') AS 'Hora de inicio de la suspensión', "
                + "DATE_FORMAT(bsf.end_dt, '%d-%m-%Y') AS 'Fecha final de la suspensión', "
                + "DATE_FORMAT(bsf.end_dt, '%H:%i') AS 'Hora final de la suspensión', "
                + "bsf.tipo_susp AS 'Tipo de suspensión', "
                + "bsf.org_susp AS 'Origen de la suspensión',"
                + "(CASE WHEN bsf.cost>=0 THEN 1 WHEN bsf.cost IS NULL THEN 2 ELSE 2 END) AS 'Generó compensación', "
                + "1 AS 'Número de suscriptores afectados', "
                + "bsf.medio AS 'Medio de Comunicación', "
                + "DATE_FORMAT(bsf.date_medio, '%d-%m-%Y') AS 'Fecha de publicación', "
                + "(CASE bsf.org_susp WHEN '6' THEN 'Otros' WHEN '7' THEN 'Falencias en las presiones de suministro' WHEN '8' THEN 'Atención de emergencias' WHEN '9' THEN 'Fuerza mayor o caso fortuito' WHEN '10' THEN 'Desabastecimiento de gas' ELSE '' END) AS 'Observaciones' "
                + "FROM bill_service_fail bsf "
                + "WHERE bsf.span_id="+spanId).getRecords(conn);
        
        Object[][] sched= new MySQLQuery("SELECT "
                + "(SELECT bm.id_market FROM sigma.bill_market bm WHERE bm.id="+inst.marketId+") AS 'ID Mercado', "
                + "(SELECT dp.code FROM sigma.bill_instance bi LEFT JOIN sigma.dane_poblado dp on dp.id=bi.pob_id WHERE bi.id="+inst.id+") AS 'Código DANE', "
                + "1 AS '¿Suspensión fue programada?', "
                + "CONCAT(bssf.id,'1') AS 'Código de evento', "
                + "3 AS 'Tipo de Gas', "
                + "DATE_FORMAT(bssf.sched_start,'%d-%m-%Y') AS 'Fecha de inicio de la suspensión', "
                + "DATE_FORMAT(bssf.sched_start,'%H:%i') AS 'Hora de inicio de la suspensión', "
                + "DATE_FORMAT(bssf.sched_end, '%d-%m-%Y') AS 'Fecha final de la suspensión', "
                + "DATE_FORMAT(bssf.sched_end , '%H:%i') AS 'Hora final de la suspensión', "
                + "bssf.tipo_susp AS 'Tipo de suspensión', "
                + "(CASE bssf.src WHEN 'mp' THEN 1 WHEN 'md' THEN 2 WHEN 'tr' THEN 3 WHEN 'un' THEN 4 WHEN 'r' THEN 5 WHEN 'o' THEN 6 ELSE '' END) AS 'Origen de la suspensión', "
                + "1 AS 'Generó compensación', "
                + "bssf.users AS 'Número de suscriptores afectados', "
                + "(CASE bssf.media WHEN 'm' THEN 1 WHEN 'e' THEN 2 WHEN 'p' THEN 3 WHEN 'a' THEN 4 WHEN 'r' THEN 5 ELSE 1 END) AS 'Medio de Comunicación', "
                + "DATE_FORMAT(bssf.date_media, '%d-%m-%Y') AS 'Fecha de publicación', "
                + "(CASE bssf.src WHEN 'mp' THEN 'Mejoras en el sistema de distribución' WHEN 'md' THEN 'Modificaciones en el sistema de distribución' WHEN 'tr' THEN 'Actividades de mantenimiento o reparación técnica' WHEN 'un' THEN 'Conexión nuevos usuarios' WHEN 'r' THEN 'Racionamientos' WHEN 'o' THEN 'Otros' ELSE '' END) AS 'Obseservaciones' "
                + "FROM bill_sched_service_fail bssf "
                + "WHERE bssf.sched_start BETWEEN '"+dateSpan[0].toString()+" 00:00:00' AND '"+dateSpan[1].toString()+" 23:59:59'").getRecords(conn);
        
        Object[][] schedAndNosched= new Object[noSched.length+sched.length][16];
        int cont=0;
        for(int i =0; i<=noSched.length-1;i++){
            cont++;
            for(int j=0;j<=15;j++){
                schedAndNosched[i][j]=noSched[i][j]; 
            }
        }
        for(int m =0; m<=sched.length-1;m++){
            for(int k=0;k<=15;k++){
                schedAndNosched[cont][k]=sched[m][k];
            }
            cont++;
        }
         
        if(schedAndNosched.length>0){
            tb.setData(schedAndNosched);
            rep.getTables().add(tb);
        }   
        return rep;
    }
    
    public static MySQLReport getRptGRCS1(String pBegin, String pEnd, Connection conn)throws Exception{
        MySQLReport rep = new MySQLReport("GRCS1 Información de Respuesta a Servicio Técnico", "", "", MySQLQuery.now(conn));
        rep.getSubTitles().add("Desde: "+pBegin+" Hasta: "+pEnd );
        
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.0000"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.0000"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.0000"));//3
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd-MM-yyyy"));//4
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "0.00%"));//5

        rep.setZoomFactor(80);
        rep.setMultiRowTitles(2);
        Table tb = new Table("GRCS1 Información de Respuesta a Servicio Técnico");

        tb.getColumns().add(new Column("Radicado Recibido", 10, 0));//1
        tb.getColumns().add(new Column("Tipo de Gas", 10, 0));//2
        tb.getColumns().add(new Column("NIU", 10, 0));//2
        tb.getColumns().add(new Column("Tipo Evento", 10, 0));//2
        tb.getColumns().add(new Column("Tipo Solicitud", 10, 0));//2
        tb.getColumns().add(new Column("Fecha Solicitud", 14, 0));//2
        tb.getColumns().add(new Column("Hora Solicitud", 10, 0));//2
        tb.getColumns().add(new Column("Fecha de llegada del servicio técnico", 14, 0));//2
        tb.getColumns().add(new Column("Hora de llegada del servicio técnico", 10, 0));//2
        tb.getColumns().add(new Column("Observaciones", 50, 0));//2
        
        Object[][] data = new MySQLQuery("SELECT "
                + "pqr.serial AS 'Radicado Recibido', "
                + "3 AS 'Tipo de gas', "
                + "bi.db AS 'NIU', "
                + "(CASE WHEN opr.id=15 THEN 1 WHEN opr.description LIKE '%Incendio%' THEN 2 WHEN opr.id = 172 THEN 3 WHEN opr.id =74 THEN 4 ELSE 5 END) AS 'Tipo Evento', "
                + "(CASE WHEN oc.id=1 THEN 2 WHEN oc.name LIKE '%Escrito%' THEN 1 WHEN oc.id=4 THEN 3 WHEN oc.id=5 OR oc.id=8 OR oc.id=3 OR oc.id=2 THEN 4 ELSE 5 END) AS 'Tipo Solicitud', "
                + "DATE_FORMAT(pqr.`regist_date`, '%d-%m-%Y') AS 'Fecha Solicitud', "
                + "DATE_FORMAT(pqr.regist_hour,'%H:%i') AS 'Hora Solicitud', "
                + "DATE_FORMAT(pqr.arrival_date, '%d-%m-%Y') AS 'Fecha de llegada del servicio técnico', "
                + "DATE_FORMAT(pqr.arrival_date, '%H:%i') AS 'Hora de llegada del servicio técnico', "
                + "CONCAT(opr.description,' - Canal Recepción ',oc.name)  AS 'Observaciones' "
                + "FROM sigma.ord_pqr_tank AS pqr "
                + "LEFT JOIN sigma.ord_pqr_client_tank as client ON client.id=pqr.client_id "
                + "INNER JOIN sigma.ord_pqr_reason AS opr ON pqr.reason_id=opr.id "
                + "LEFT JOIN sigma.bill_instance bi ON bi.id =client.bill_instance_id "
                + "LEFT JOIN sigma.ord_channel oc ON pqr.channel_id = oc.id "
                + "WHERE bi.`type` LIKE 'net' AND pqr.attention_date IS NOT NULL "
                + "AND pqr.regist_date BETWEEN "+"'"+pBegin+"'"+" AND "+"'"+pEnd+"'"+" "
                + "ORDER BY pqr.`regist_date` ASC").getRecords(conn);
        
        //Traer nombre base de datos de instancia e id cliente (mirror id)
        Object[][] clientAndInstance= new MySQLQuery("SELECT "
                + "bi.db, "
                + "client.mirror_id "
                + "FROM sigma.ord_pqr_tank AS pqr "
                + "LEFT JOIN sigma.ord_pqr_client_tank as client ON client.id=pqr.client_id "
                + "INNER JOIN sigma.ord_pqr_reason AS opr ON pqr.reason_id=opr.id "
                + "LEFT JOIN sigma.bill_instance bi ON bi.id =client.bill_instance_id "
                + "LEFT JOIN sigma.ord_channel oc ON pqr.channel_id = oc.id "
                + "WHERE bi.`type` LIKE 'net' AND pqr.attention_date IS NOT NULL "
                + "AND pqr.regist_date BETWEEN "+"'"+pBegin+"'"+" AND "+"'"+pEnd+"'"+" "
                + "ORDER BY pqr.`regist_date` ASC").getRecords(conn);
        
        
        //Traer nombre de instancia
        Object[][] nameInstance= new MySQLQuery("SELECT "
                + "DISTINCT(bi.id), "
                + "bi.name "
                + "FROM sigma.ord_pqr_tank AS pqr "
                + "LEFT JOIN sigma.ord_pqr_client_tank as client ON client.id=pqr.client_id "
                + "INNER JOIN sigma.ord_pqr_reason AS opr ON pqr.reason_id=opr.id "
                + "LEFT JOIN sigma.bill_instance bi ON bi.id =client.bill_instance_id "
                + "LEFT JOIN sigma.ord_channel oc ON pqr.channel_id = oc.id "
                + "WHERE bi.`type` LIKE 'net' AND pqr.attention_date IS NOT NULL "
                + "AND pqr.regist_date BETWEEN "+"'"+pBegin+"'"+" AND "+"'"+pEnd+"'"+" "
                + "ORDER BY pqr.`regist_date` ASC").getRecords(conn);
        String instancias="";
        for(int j=0; j<=nameInstance.length-1;j++){
            instancias+=nameInstance[j][1].toString()+" ";
        }
        rep.getSubTitles().add("Instancias: " +instancias);
        
        if(data.length>0){
            //Remplaza la columna NIU 
            Object[] code=null;
            for(int i=0; i<=clientAndInstance.length-1;i++){
                code=new MySQLQuery("SELECT bct.code "
                + "FROM "+clientAndInstance[i][0].toString()+".bill_client_tank bct "
                + "WHERE bct.id="+clientAndInstance[i][1].toString()).getRecord(conn);
                data[i][2]=code[0];
            }       
            tb.setData(data);
            rep.getTables().add(tb);
        }
        
        return rep;
    }
    
    public static MySQLReport getRptC1ServiceFaults(int spanId, BillInstance inst, Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("C1 Información de Suspensiones no Programadas", "", "", MySQLQuery.now(conn));
        BillSpan span = new BillSpan().select(spanId, conn);
        rep.getSubTitles().add("Periodo: " + span.getConsLabel());
        rep.getSubTitles().add("Instancia: " + inst.name);
        if (span.state.equals("cons")) {
            rep.getSubTitles().add("¡ALERTA, INFORMACIÓN PROVISIONAL! CAUSACIÓN EN CURSO");
        }

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "dd-MM-yyyy"));//1
        rep.setZoomFactor(80);
        rep.setVerticalFreeze(5);
        Table tb = new Table("C1 Información de Suspensiones no Programadas");
        tb.getColumns().add(new Column("Fecha Inicio", 20, 1));//0
        tb.getColumns().add(new Column("Duración", 20, 0));//1
        tb.getColumns().add(new Column("Usuarios Afectados", 30, 0));//3
        tb.getColumns().add(new Column("Justificación", 50, 0));//4

        MySQLQuery dataQ = new MySQLQuery("SELECT "
                + "s.beg_dt, "
                + "TIME_FORMAT(TIMEDIFF(s.end_dt, s.beg_dt), '%H:%i'), "
                + "1, "
                + "CONCAT('NIU: ', c.code ,'. ',s.notes)"
                + "FROM bill_service_fail s "
                + "INNER JOIN bill_client_tank c ON c.id = s.client_id "
                + "INNER JOIN bill_span p ON s.span_id = p.id "
                + "WHERE s.span_id = ?1");
        Object[][] data = dataQ.setParam(1, spanId).getRecords(conn);
        if (data != null && data.length > 0) {
            rep.getTables().add(tb);
            tb.setData(data);
        }
        return rep;
    }

    // RPT COMPENSACIONES NET
    public static MySQLReport getRptC2Compensations(int spanId, BillInstance inst, Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("C2 Información de Compensaciones", "", "", MySQLQuery.now(conn));
        BillSpan span = new BillSpan().select(spanId, conn);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(span.consMonth);

        int month = gc.get(GregorianCalendar.MONTH) + 1;
        int year = gc.get(GregorianCalendar.YEAR);

        rep.getSubTitles().add("Periodo: " + span.getConsLabel());
        rep.getSubTitles().add("Instancia: " + inst.name);
        if (span.state.equals("cons")) {
            rep.getSubTitles().add("¡ALERTA, INFORMACIÓN PROVISIONAL! CAUSACIÓN EN CURSO");
        }

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.0000"));//1
        rep.setZoomFactor(80);
        rep.setVerticalFreeze(5);
        Table tb = new Table("C2 Información de Compensaciones");
        tb.getColumns().add(new Column("NIU", 20, 0));//0
        tb.getColumns().add(new Column("Sector (N/R)", 25, 0));//1
        tb.getColumns().add(new Column("Factura", 30, 0));//2
        tb.getColumns().add(new Column("Periodo", 15, 0));//3
        tb.getColumns().add(new Column("Año", 15, 0));//4
        tb.getColumns().add(new Column("DES", 15, 0));//5
        tb.getColumns().add(new Column("CI $/m3", 25, 1));//6
        tb.getColumns().add(new Column("Demanda Promedio m3/hr", 25, 1));//7
        tb.getColumns().add(new Column("Valor Compensado", 25, 1));//8

        MySQLQuery dataQ = new MySQLQuery("SELECT c.code, "
                + "IF(c.sector_type = 'r', 'Residencial', 'No Residencial'), "
                + "(SELECT b.bill_num FROM "
                + "bill_bill b "
                + "INNER JOIN bill_antic_note n ON b.bill_span_id = n.bill_span_id AND b.client_tank_id = n.client_tank_id "
                + "WHERE n.srv_fail_id = s.id "
                + "ORDER BY b.creation_date ASC LIMIT 1), "
                + month + ", "
                + year + ", "
                + "TIME_FORMAT(TIMEDIFF(s.end_dt, s.beg_dt), '%H:%i'), "
                + "s.creg_cost, "
                + "s.avg_cons, "
                + "s.cost "
                + "FROM bill_service_fail s "
                + "INNER JOIN bill_client_tank c ON c.id = s.client_id "
                + "INNER JOIN bill_span p ON s.span_id = p.id WHERE s.span_id = ?1");
        dataQ.setParam(1, spanId);

        Object[][] data = dataQ.getRecords(conn);

        if (data != null && data.length > 0) {
            rep.getTables().add(tb);
            tb.setData(data);
        }
        return rep;
    }

    public static MySQLReport getRptC3SchedFaults(int spanId, BillInstance inst, Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("C3 Información de Suspensiones Programadas", "", "", MySQLQuery.now(conn));
        BillSpan span = new BillSpan().select(spanId, conn);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(span.consMonth);

        int month = gc.get(GregorianCalendar.MONTH) + 1;
        int year = gc.get(GregorianCalendar.YEAR);

        rep.getSubTitles().add("Periodo: " + span.getConsLabel());
        rep.getSubTitles().add("Instancia: " + inst.name);
        if (span.state.equals("cons")) {
            rep.getSubTitles().add("¡ALERTA, INFORMACIÓN PROVISIONAL! CAUSACIÓN EN CURSO");
        }

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "dd-MM-yyyy"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "HH:mm"));//3
        rep.setZoomFactor(80);
        rep.setVerticalFreeze(5);
        Table tb = new Table("C3 Información de Suspensiones Programadas");

        Date[] dates = Dates.getForBetween(span.beginDate, span.endDate);

        tb.getColumns().add(new Column("Ubicación", 30, 0));//0
        tb.getColumns().add(new Column("Origen", 25, 0));//1
        tb.getColumns().add(new Column("Medio de Comunicación", 25, 0));//2
        tb.getColumns().add(new Column("Fecha Programada", 20, 2));//3
        tb.getColumns().add(new Column("Hora Programada", 20, 3));//4
        tb.getColumns().add(new Column("Duración Programada", 20, 0));//5
        tb.getColumns().add(new Column("Afectados", 25, 1));//6

        MySQLQuery dataQ = new MySQLQuery("SELECT "
                + "f.location, "
                + "UPPER(f.src), "
                + "UPPER(f.media), "
                + "DATE(f.sched_start), "
                + "TIME(f.sched_end), "
                + "TIME_FORMAT(TIMEDIFF(f.sched_end, f.sched_start), '%H:%i'), "
                + "f.users "
                + "FROM "
                + "bill_sched_service_fail f "
                + "WHERE f.sched_start BETWEEN ?1 AND ?2");
        dataQ.setParam(1, dates[0]);
        dataQ.setParam(2, dates[1]);

        Object[][] data = dataQ.getRecords(conn);

        if (data != null && data.length > 0) {
            rep.getTables().add(tb);
            tb.setData(data);
        }
        return rep;
    }

    public static MySQLReport getRptT1Stations(Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("T1 Información de Estaciones y Tanques Almacenamiento", "", "", MySQLQuery.now(conn));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.0000"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "dd-MM-yyyy"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "HH:mm"));//3
        rep.setZoomFactor(80);
        rep.setVerticalFreeze(5);
        Table tb = new Table("T1 Información de Estaciones y Tanques Almacenamiento");

        tb.getColumns().add(new Column("Código DANE", 25, 0));
        tb.getColumns().add(new Column("Longitud", 25, 1));
        tb.getColumns().add(new Column("Latitud", 20, 1));
        tb.getColumns().add(new Column("Altitud", 20, 1));
        tb.getColumns().add(new Column("Tipo de Estación", 20, 0));
        tb.getColumns().add(new Column("Código", 20, 0));
        tb.getColumns().add(new Column("Entrada en Operación", 20, 2));

        MySQLQuery dataQ = new MySQLQuery("SELECT p.code, s.lon, s.lat, s.alt, UPPER(s.`type`), s.code, s.beg_date "
                + "FROM "
                + "bill_station s "
                + "INNER JOIN bill_instance i ON s.inst_id = i.id "
                + "INNER JOIN dane_poblado p ON p.id = i.pob_id "
                + "ORDER BY p.code, s.`type`, s.code ");

        Object[][] data = dataQ.getRecords(conn);

        if (data != null && data.length > 0) {
            rep.getTables().add(tb);
            tb.setData(data);
        }
        return rep;
    }

    public static MySQLReport getRptT2TechSrvResponse(int spanId, BillInstance inst, Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("T2 Información Respuesta Servicio Técnico", "", "", MySQLQuery.now(conn));
        BillSpan span = new BillSpan().select(spanId, conn);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(span.consMonth);

        rep.getSubTitles().add("Periodo: " + span.getConsLabel());
        rep.getSubTitles().add("Instancia: " + inst.name);
        if (span.state.equals("cons")) {
            rep.getSubTitles().add("¡ALERTA, INFORMACIÓN PROVISIONAL! CAUSACIÓN EN CURSO");
        }

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "dd-MM-yyyy"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "HH:mm"));//3
        rep.setZoomFactor(80);
        rep.setVerticalFreeze(5);
        Table tb = new Table("T2 Información Respuesta Servicio Técnico");

        Date[] dates = Dates.getForBetween(span.beginDate, span.endDate);

        tb.getColumns().add(new Column("Radicado", 25, 0));
        tb.getColumns().add(new Column("NIU", 25, 0));
        tb.getColumns().add(new Column("Tipo de Evento", 20, 0));
        tb.getColumns().add(new Column("Tipo de Solicitud", 20, 0));
        tb.getColumns().add(new Column("Fecha de Solicitud", 20, 2));
        tb.getColumns().add(new Column("Hora de Solicitud", 20, 3));
        tb.getColumns().add(new Column("Fecha de Atención", 20, 2));
        tb.getColumns().add(new Column("Hora de Atención", 20, 3));
        tb.getColumns().add(new Column("Observaciones", 40, 0));

        MySQLQuery dataQ = new MySQLQuery("SELECT "
                + "r.serial, "
                + "bc.code, "
                + "snt.code, "
                + "ch.net_sui, "
                + "r.regist_date, "
                + "r.regist_hour, "
                + "r.confirm_date, "
                + "r.confirm_time, "
                + "r.notes "
                + "FROM sigma.ord_repairs r "
                + "INNER JOIN sigma.ord_pqr_client_tank c ON r.client_id = c.id "
                + "INNER JOIN bill_client_tank bc ON bc.id = c.mirror_id "
                + "INNER JOIN sigma.ord_pqr_reason pr ON pr.id = r.reason_id "
                + "INNER JOIN sigma.ord_pqr_sui_net_type snt ON snt.id = pr.sui_net_type_id "
                + "INNER JOIN sigma.ord_channel ch ON ch.id = r.channel_id "
                + "WHERE "
                + "c.bill_instance_id = ?1 AND r.cancel_date IS NULL AND r.regist_date BETWEEN ?2 AND ?3");
        dataQ.setParam(1, inst.id);
        dataQ.setParam(2, dates[0]);
        dataQ.setParam(3, dates[1]);

        Object[][] data = dataQ.getRecords(conn);

        Map<String, String> channels = MySQLQuery.getEnumOptAsMap(new OrdChannel().getEnumOpts("net_sui"));

        for (Object[] row : data) {
            row[3] = channels.get(cast.asString(row, 3));
        }

        if (data != null && data.length > 0) {
            rep.getTables().add(tb);
            tb.setData(data);
        }
        return rep;
    }

    public static MySQLReport getRptT4Measurements(int spanId, BillInstance inst, Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("T4 Suspención del Servicio", "", "", MySQLQuery.now(conn));
        BillSpan span = new BillSpan().select(spanId, conn);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(span.consMonth);

        rep.getSubTitles().add("Periodo: " + span.getConsLabel());
        rep.getSubTitles().add("Instancia: " + inst.name);
        if (span.state.equals("cons")) {
            rep.getSubTitles().add("¡ALERTA, INFORMACIÓN PROVISIONAL! CAUSACIÓN EN CURSO");
        }

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "dd-MM-yyyy"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "HH:mm:ss"));//3
        rep.setZoomFactor(80);
        rep.setVerticalFreeze(5);
        Table tb = new Table("T4 Suspención del Servicio");
//
        tb.getColumns().add(new Column("NIU", 25, 0));
        tb.getColumns().add(new Column("Fecha", 25, 2));
        tb.getColumns().add(new Column("Hora", 25, 3));
        tb.getColumns().add(new Column("Presión Medida", 25, 1));
        tb.getColumns().add(new Column("Sustancia odorante", 25, 0));
        tb.getColumns().add(new Column("Método", 25, 0));
        tb.getColumns().add(new Column("Nivel de concentración", 25, 1));
        tb.getColumns().add(new Column("Observaciones", 25, 0));

        MySQLQuery dataQ = new MySQLQuery("SELECT "
                + "c.code, "
                + "date(m.taken_dt), "
                + "time(m.taken_dt), "
                + "m.pressure, "
                + "o.name, "
                + "\"CN\", "
                + "m.odorant_amount, "
                + "m.notes "
                + "FROM  "
                + "bill_measure m "
                + "INNER JOIN bill_client_tank c ON c.id = m.client_id "
                + "INNER JOIN sigma.bill_odorant o ON o.id = m.odorant_id "
                + "WHERE  "
                + "m.span_id = ?1 AND m.taken_dt IS NOT NULL;");
        dataQ.setParam(1, span.id);
        Object[][] data = dataQ.getRecords(conn);

        if (data != null && data.length > 0) {
            rep.getTables().add(tb);
            tb.setData(data);
        }
        return rep;
    }

    public static MySQLReport getRptT3Indicators(int spanId, BillInstance inst, Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("T3 Consolidado de Indicadores", "", "", MySQLQuery.now(conn));
        BillSpan span = new BillSpan().select(spanId, conn);
        Date[] dates = Dates.getForBetween(span.beginDate, span.endDate);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(span.consMonth);

        rep.getSubTitles().add("Periodo: " + span.getConsLabel());
        rep.getSubTitles().add("Instancia: " + inst.name);
        if (span.state.equals("cons")) {
            rep.getSubTitles().add("¡ALERTA, INFORMACIÓN PROVISIONAL! CAUSACIÓN EN CURSO");
        }

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "dd-MM-yyyy"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "HH:mm:ss"));//3
        rep.setZoomFactor(80);
        rep.setVerticalFreeze(5);
        Table tb = new Table("T3 Consolidado de Indicadores");

        tb.getColumns().add(new Column("IPLI", 25, 1));
        tb.getColumns().add(new Column("IO", 25, 1));
        tb.getColumns().add(new Column("IRST – EG", 25, 1));
        tb.getColumns().add(new Column("IRST – IN", 25, 1));
        tb.getColumns().add(new Column("IRST – CL", 25, 1));
        tb.getColumns().add(new Column("IRST – IS", 25, 1));

        int measurements = new MySQLQuery("SELECT COUNT(*) FROM bill_measure WHERE taken_dt IS NOT NULL AND span_id = ?1").setParam(1, spanId).getAsInteger(conn);

        int pressureOk = new MySQLQuery("SELECT COUNT(*) FROM bill_measure WHERE taken_dt IS NOT NULL AND span_id = ?1 AND pressure_ok").setParam(1, spanId).getAsInteger(conn);
        int odorantOk = new MySQLQuery("SELECT COUNT(*) FROM bill_measure WHERE taken_dt IS NOT NULL AND span_id = ?1 AND odorant_ok").setParam(1, spanId).getAsInteger(conn);

        String repsQ = "SELECT TIMESTAMP(r.confirm_date, r.confirm_time), TIMESTAMP(r.regist_date, r.regist_hour), r.net_sui_resp_minutes, r.net_sui_resp_type  FROM "
                + "ord_repairs r "
                + "INNER JOIN ord_pqr_client_tank c ON r.client_id = c.id "
                + "INNER JOIN ord_pqr_reason pr ON r.reason_id = pr.id "
                + "INNER JOIN ord_pqr_sui_net_type nt ON pr.sui_net_type_id = nt.id "
                + "WHERE "
                + "r.confirm_date IS NOT NULL "
                + "AND nt.code = ?4 AND r.cancel_date IS NULL "
                + "AND r.regist_date BETWEEN ?1 AND ?2 AND c.bill_instance_id = ?3;";

        String[] types = new String[]{"eg", "in", "cl", "is"};
        int[] total = new int[types.length];
        int[] onTime = new int[types.length];

        new MySQLQuery("USE sigma;").executeUpdate(conn);

        for (int i = 0; i < types.length; i++) {
            String type = types[i];
            Object[][] records = new MySQLQuery(repsQ).setParam(4, type).setParam(3, inst.id).setParam(1, dates[0]).setParam(2, dates[1]).getRecords(conn);
            total[i] = records.length;
            for (Object[] record : records) {
                Date confirm = cast.asDate(record, 0);
                Date regist = cast.asDate(record, 1);
                int respTime = cast.asInt(record, 2);
                String respType = cast.asString(record, 3);

                switch (respType) {
                    case "min":
                        double min = (regist.getTime() - confirm.getTime()) / 3600000d;
                        if (min <= respTime) {
                            onTime[i]++;
                        }
                        break;
                    case "days":
                        Date[] days = Dates.getDatesBetween(Dates.trimDate(regist), Dates.trimDate(confirm));
                        GregorianCalendar gcDays = new GregorianCalendar();
                        int workDays = 0;
                        for (Date day : days) {
                            gcDays.setTime(day);
                            int wd = gcDays.get(GregorianCalendar.DAY_OF_WEEK);
                            if (wd != GregorianCalendar.SATURDAY && wd != GregorianCalendar.SUNDAY) {
                                if (new MySQLQuery("SELECT COUNT(*) = 0 FROM per_holiday WHERE holi_date = ?1").setParam(1, Dates.trimDate(day)).getAsBoolean(conn)) {
                                    workDays++;
                                }
                            }
                        }
                        if (workDays <= respTime) {
                            onTime[i]++;
                        }
                        break;
                    default:
                        throw new RuntimeException();
                }
            }
        }

        Object[][] data = new Object[1][6];
        tb.setData(data);
        rep.getTables().add(tb);

        data[0][0] = measurements > 0 ? pressureOk / ((double) measurements) * 100d : 0;
        data[0][1] = measurements > 0 ? odorantOk / ((double) measurements) * 100d : 0;
        data[0][2] = total[0] > 0 ? onTime[0] / ((double) total[0]) * 100d : 0;
        data[0][3] = total[1] > 0 ? onTime[1] / ((double) total[1]) * 100d : 0;
        data[0][4] = total[2] > 0 ? onTime[2] / ((double) total[2]) * 100d : 0;
        data[0][5] = total[3] > 0 ? onTime[3] / ((double) total[3]) * 100d : 0;

        return rep;
    }

    public static MySQLReport getRptPendingMeasures(Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("Mediciones de Calidad Pendientes", "", "", MySQLQuery.now(conn));

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.setZoomFactor(80);

        List<BillInstance> instances = BillInstance.getAllNet(conn);
        for (int i = 0; i < instances.size(); i++) {
            BillInstance instance = instances.get(i);
            instance.useInstance(conn);
            Object[][] data = new MySQLQuery("SELECT "
                    + "c.code, "
                    + "c.address, "
                    + "n.name, "
                    + "c.first_name, "
                    + "c.last_name "
                    + "FROM "
                    + "bill_measure m "
                    + "INNER JOIN bill_client_tank c ON c.id = m.client_id "
                    + "LEFT JOIN sigma.neigh n ON n.id = c.neigh_id "
                    + "WHERE m.taken_dt IS NULL "
                    + "ORDER BY c.code ASC").getRecords(conn);

            Table tb = new Table(instance.name);

            tb.getColumns().add(new Column("Código", 20, 0));
            tb.getColumns().add(new Column("Dirección", 35, 0));
            tb.getColumns().add(new Column("Barrio", 25, 0));
            tb.getColumns().add(new Column("Nombres", 25, 0));
            tb.getColumns().add(new Column("Apellidos", 25, 0));

            if (data.length > 0) {
                tb.setData(data);
                rep.getTables().add(tb);
            }
        }
        return rep;
    }
    
    // RPT GRC3
    public static MySQLReport getRptGRC3(int spanId, BillInstance inst, Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("GRC3 Información de Compensación Sector Residencial y No Residencial Usuarios Regulados", "", "", MySQLQuery.now(conn));
        BillSpan span = new BillSpan().select(spanId, conn);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(span.consMonth);

        rep.getSubTitles().add("Periodo: " + span.getConsLabel());
        rep.getSubTitles().add("Instancia: " + inst.name);
        if (span.state.equals("cons")) {
            rep.getSubTitles().add("¡ALERTA, INFORMACIÓN PROVISIONAL! CAUSACIÓN EN CURSO");
        }

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "###0.00"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "####"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "dd-MM-yyyy"));//3
        rep.setZoomFactor(80);
        rep.setVerticalFreeze(5);
        Table tb = new Table("GRC3 Información de Compensaciones");
        tb.getColumns().add(new Column("NIU", 15, 0));//0
        tb.getColumns().add(new Column("Tipo de gas", 20, 0));//1
        tb.getColumns().add(new Column("Tipo sector de consumo", 30, 0));//2
        tb.getColumns().add(new Column("ID Factura", 15, 0));//3
        tb.getColumns().add(new Column("Periodo Compensado", 25, 0));//4
        tb.getColumns().add(new Column("Año", 15, 2));//5
        tb.getColumns().add(new Column("DES", 20, 0));//6
        tb.getColumns().add(new Column("CI", 20, 2));//7
        tb.getColumns().add(new Column("Demanda Promedio", 25, 1));//8
        tb.getColumns().add(new Column("Valor Compensado", 25, 2));//9
        tb.getColumns().add(new Column("Código Causal", 20, 0));//10

        MySQLQuery dataQ = new MySQLQuery("SELECT c.code AS 'NIU', " 
                + "3 AS 'Tipo de gas', " 
                + "IF(c.sector_type = 'r', 1, 2) AS 'Tipo sector de consumo', " 
                + "(SELECT b.bill_num FROM " 
                + "bill_bill b " 
                + "INNER JOIN bill_antic_note n ON b.bill_span_id = n.bill_span_id AND b.client_tank_id = n.client_tank_id " 
                + "WHERE n.srv_fail_id = s.id " 
                + "ORDER BY b.creation_date ASC LIMIT 1) AS 'ID Factura', " 
                + "MONTH (p.cons_month) AS 'Periodo Compensado', " 
                + "YEAR (p.cons_month) AS 'Año', " 
                + "TIME_FORMAT(TIMEDIFF(s.end_dt, s.beg_dt), '%H:%i') AS 'DES', " 
                + "TRUNCATE(s.creg_cost, 0) AS 'CI', " 
                + "TRUNCATE((s.creg_cost * s.avg_cons), 2) AS 'Demanda Promedio', " 
                + "TRUNCATE(s.cost, 0) AS 'Valor Compensado', " 
                + "CAST(s.causal_type AS UNSIGNED) AS 'Código Causal' " 
                + "FROM bill_service_fail s " 
                + "INNER JOIN bill_client_tank c ON c.id = s.client_id " 
                + "INNER JOIN bill_span p ON s.span_id = p.id WHERE s.span_id = ?1");
        dataQ.setParam(1, spanId);
        
        Object[][] data = dataQ.getRecords(conn);

        if (data != null && data.length > 0) {
            rep.getTables().add(tb);
            tb.setData(data);
        }
        return rep;
    }
    
    // RPT GRC4
    public static MySQLReport getRptGRC4(int spanId, BillInstance inst, Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("GRC4 Servicios Adicionales", "", "", MySQLQuery.now(conn));
        BillSpan span = new BillSpan().select(spanId, conn);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(span.consMonth);

        rep.getSubTitles().add("Periodo: " + span.getConsLabel());
        rep.getSubTitles().add("Instancia: " + inst.name);
        if (span.state.equals("cons")) {
            rep.getSubTitles().add("¡ALERTA, INFORMACIÓN PROVISIONAL! CAUSACIÓN EN CURSO");
        }

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "####"));//1
        rep.setZoomFactor(80);
        rep.setVerticalFreeze(5);
        Table tb = new Table("Servicios Adicionales");
        
        tb.getColumns().add(new Column("Concepto", 15, 0));//0
        tb.getColumns().add(new Column("Otro", 50, 0));//1
        tb.getColumns().add(new Column("Valor Unitario", 20, 1));//2
        tb.getColumns().add(new Column("Valor Facturado", 20, 1));//3
        tb.getColumns().add(new Column("IVA", 20, 1));//4

        MySQLQuery dataQ = new MySQLQuery("SELECT (CASE WHEN bst.name = 'Reconexion' THEN 1 WHEN bst.name = 'Instalación de Red Interna' THEN 22 WHEN bst.name = 'Certificacion Previa' AND bct.sector_type = 'r' THEN 4 WHEN bst.name = 'Certificacion Previa' AND bct.sector_type != 'r' THEN 5 WHEN bst.name = 'Certificacion Periodica' AND bct.sector_type = 'r' THEN 6 WHEN bst.name = 'Certificacion Periodica' AND bct.sector_type != 'r' THEN 7 WHEN bst.name != 'Conexion' AND bst.name != 'Reconexion' AND bst.name != 'Instalación de Red Interna' AND bst.name != 'Certificación Previa' AND bst.name != 'Certificación Periodica' AND bst.name != 'Derechos de Conexion' THEN 29 END) AS Concepto, " 
                + "IF(bst.name != 'Conexion' AND bst.name != 'Reconexion' AND bst.name != 'Instalación de Red Interna' AND bst.name != 'Certificación Previa' AND bst.name != 'Certificación Periodica' AND bst.name != 'Derechos de Conexion', bst.name, NULL) AS Otro, " 
                + "(SELECT CAST(AVG(bs.total) AS SIGNED) FROM bill_user_service AS bs WHERE bs.type_id = bst.id ORDER BY bs.id DESC LIMIT 20) AS ValorUnitario, " 
                + "(SELECT (CAST(AVG(bs.total) AS SIGNED) - CAST((CAST(AVG(bs.total) AS SIGNED)) * 0.19 AS SIGNED)) FROM bill_user_service AS bs WHERE bs.type_id = bst.id ORDER BY bs.id DESC LIMIT 20) AS ValorFacturado, " 
                + "(SELECT CAST((CAST(AVG(bs.total) AS SIGNED)) * 0.19 AS SIGNED) FROM bill_user_service AS bs WHERE bs.type_id = bst.id ORDER BY bs.id DESC LIMIT 20) AS IVA " 
                + "FROM bill_user_service AS bus " 
                + "INNER JOIN bill_service_type AS bst ON bus.type_id = bst.id " 
                + "INNER JOIN bill_client_tank AS bct ON bus.bill_client_tank_id = bct.id WHERE bst.name != 'Derechos de Conexion' AND bus.bill_span_id = " + spanId + " GROUP BY bst.name");
        
        Integer valor = new MySQLQuery("SELECT CAST(AVG(bbp.value) AS SIGNED) FROM bill_bill_pres AS bbp " 
                + "INNER JOIN bill_bill AS bb ON bbp.bill_id = bb.id " 
                + "WHERE bbp.label = 'Reconexiones' AND bb.bill_span_id = " + spanId + " " 
                + "ORDER BY bbp.id DESC LIMIT 10 ").getAsInteger(conn);
        
        Object[][] data = dataQ.getRecords(conn);
        Object[][] data2 = new Object[0][0];
        if(valor != null){
            MySQLQuery reconexion = new MySQLQuery("SELECT 1 AS Concepto, NULL, CAST(AVG(bbp.value) AS SIGNED) AS ValorUnitario, CAST(AVG(bbp.value) AS SIGNED) AS ValorFacturado, 0 As Iva FROM bill_bill_pres AS bbp " 
                    + "INNER JOIN bill_bill AS bb ON bbp.bill_id = bb.id " 
                    + "WHERE bbp.label = 'Reconexiones' AND bb.bill_span_id = " + spanId + " " 
                    + "ORDER BY bbp.id DESC LIMIT 10");
            data2 = reconexion.getRecords(conn);
        }
        
        List<Item> items = new ArrayList<>();
        
        for (Object[] row : data2) {
            items.add(getData(row));
        }
        
        for (Object[] row : data) {
            items.add(getData(row));
        }
        
        Object[][] defData = new Object[items.size()][5];
        for (int i = 0; i < items.size(); i++) {
            defData[i][0] = items.get(i).concept;
            defData[i][1] = items.get(i).other;
            defData[i][2] = items.get(i).valUni;
            defData[i][3] = items.get(i).valFac;
            defData[i][4] = items.get(i).iva;
        }
        
        if (defData != null && defData.length > 0) {
            rep.getTables().add(tb);
            tb.setData(defData);
        }
        return rep;
    }
    
    // RPT GGRTT2
    public static MySQLReport getRptGRTT2(int spanId, BillInstance inst, Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("GRTT2 Inventario de Suscriptores", "", "", MySQLQuery.now(conn));
        BillSpan span = new BillSpan().select(spanId, conn);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(span.consMonth);

        rep.getSubTitles().add("Periodo: " + span.getConsLabel());
        rep.getSubTitles().add("Instancia: " + inst.name);
        if (span.state.equals("cons")) {
            rep.getSubTitles().add("¡ALERTA, INFORMACIÓN PROVISIONAL! CAUSACIÓN EN CURSO");
        }

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "###0.000000"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "####"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "dd-MM-yyyy"));//3
        rep.setZoomFactor(80);
        rep.setVerticalFreeze(5);
        Table tb = new Table("GRTT2 Suscriptores");
        tb.getColumns().add(new Column("NIU", 15, 0));//1
        tb.getColumns().add(new Column("Tipo de Usuario", 20, 0));//2
        tb.getColumns().add(new Column("ID Comercializador", 20, 2));//3
        tb.getColumns().add(new Column("ID Mercado", 15, 2));//4
        tb.getColumns().add(new Column("Código DANE", 15, 0));//5
        tb.getColumns().add(new Column("Ubicación", 15, 0));//6
        tb.getColumns().add(new Column("Dirección", 30, 0));//7
        tb.getColumns().add(new Column("Información Predial Utilizada", 30, 0));//8
        tb.getColumns().add(new Column("Cédula Catastral", 45, 0));//9
        tb.getColumns().add(new Column("Estrato/Sector", 20, 0));//10
        tb.getColumns().add(new Column("Altitud (usuario)", 20, 2));//11
        tb.getColumns().add(new Column("Longitud (usuario)", 20, 1));//12
        tb.getColumns().add(new Column("Latitud (usuario)", 20, 1));//13
        tb.getColumns().add(new Column("Estado", 10, 0));//14
        tb.getColumns().add(new Column("Fecha ajuste", 25, 3));//15
        
        MySQLQuery dataQ = new MySQLQuery("SELECT c.code AS 'NIU', " 
                + "2 AS 'Tipo de Usuario', " 
                + "6026 AS 'ID Comercializador', " 
                + "(SELECT bm.id_market FROM sigma.bill_instance AS bi INNER JOIN sigma.bill_market AS bm ON bm.id = bi.market_id WHERE bi.id = " + inst.id + ") AS 'ID Mercado', " 
                + "(SELECT CAST(code AS UNSIGNED) FROM sigma.dane_poblado WHERE code = " + inst.pobId + ") AS 'Código DANE', " 
                + "IF(c.location IS NOT NULL, CAST(c.location AS UNSIGNED), CAST(2 AS UNSIGNED)) AS 'Ubicación', " 
                + "c.address AS 'Dirección', " 
                + "CAST(c.cad_info AS UNSIGNED) AS 'Información Predial Utilizada', " 
                + "IF(c.cadastral_code IS NULL OR c.cadastral_code = 'No Registra' OR c.cadastral_code = 'Sin Registrar' OR c.cadastral_code = 'No Registrado' OR c.cadastral_code = 'Cod: Sin Registrar' OR c.cadastral_code = 'Cod: No Registra', 0, c.cadastral_code) AS 'Cédula Catastral', " 
                + "(CASE WHEN c.sector_type = 'r' AND c.stratum = 1 THEN 1 WHEN c.sector_type = 'r' AND c.stratum = 2 THEN 2 WHEN c.sector_type = 'r' AND c.stratum = 3 THEN 3 WHEN c.sector_type = 'r' AND c.stratum = 4 THEN 4 WHEN c.sector_type = 'r' AND c.stratum = 5 THEN 5 WHEN c.sector_type = 'r' AND c.stratum = 6 THEN 6 WHEN c.sector_type = 'c' THEN 7 WHEN c.sector_type = 'i' THEN 8 WHEN c.sector_type = 'o' THEN 9 WHEN c.sector_type = 'ea' THEN 10 WHEN c.sector_type = 'ed' THEN 11 END) AS 'Estrato / Sector', " 
                + "(SELECT (CASE WHEN bi.id = 205 THEN 3013 WHEN bi.id = 207 THEN 2985 WHEN bi.id = 208 THEN 2566 WHEN bi.id = 211 THEN 1912 WHEN bi.id = 212 THEN 2103 WHEN bi.id = 213 THEN 1515 WHEN bi.id = 214 THEN 2970 WHEN bi.id = 215 THEN 2881 END) FROM sigma.bill_instance AS bi WHERE bi.id = 207 AND bi.`type`= 'net') AS 'Altitud', " 
                + "(SELECT br.lon FROM bill_reading AS br WHERE br.client_tank_id = c.id ORDER BY br.id DESC LIMIT 1) AS 'Longitud', " 
                + "(SELECT br.lat FROM bill_reading AS br WHERE br.client_tank_id = c.id ORDER BY br.id DESC LIMIT 1) AS 'Latitud', " 
                + "IF(c.active = 1, 1, 2) AS 'Estado', " 
                + "(CASE WHEN sys.`type` AND sys.dt > '" + span.consMonth + "' AND sys.dt IS NOT NULL THEN sys.dt WHEN c.creation_date > '" + span.consMonth + "' THEN c.creation_date ELSE c.creation_date END) AS 'Fecha ajuste' " 
                + "FROM bill_client_tank AS c " 
                + "LEFT JOIN sigma.sys_crud_log AS sys ON c.id = sys.owner_serial AND sys.bill_inst_id = " + inst.id + " GROUP BY c.id");
        
        Object[][] data = dataQ.getRecords(conn);

        if (data != null && data.length > 0) {
            rep.getTables().add(tb);
            tb.setData(data);
        }
        return rep;
    }
    
    // RPT GRCS3
    public static MySQLReport getRptGRCS3(int spanId, BillInstance inst, Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("GRCS3 Información de Presión en Líneas Individuales y Nivel de Odorización", "", "", MySQLQuery.now(conn));
        BillSpan span = new BillSpan().select(spanId, conn);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(span.consMonth);

        rep.getSubTitles().add("Periodo: " + span.getConsLabel());
        rep.getSubTitles().add("Instancia: " + inst.name);
        if (span.state.equals("cons")) {
            rep.getSubTitles().add("¡ALERTA, INFORMACIÓN PROVISIONAL! CAUSACIÓN EN CURSO");
        }

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "###0.00"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "####"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "dd-MM-yyyy"));//3
        rep.setZoomFactor(85);
        rep.setVerticalFreeze(5);
        Table tb = new Table("GRCS3 Información de Presión en Líneas Individuales y Nivel de Odorización");
        tb.getColumns().add(new Column("NIU", 15, 0));//0
        tb.getColumns().add(new Column("Fecha de medición", 20, 3));//1
        tb.getColumns().add(new Column("Hora de medición", 20, 0));//2
        tb.getColumns().add(new Column("Tipo de Gas", 15, 0));//3
        tb.getColumns().add(new Column("Presión Medida", 20, 1));//4
        tb.getColumns().add(new Column("Método", 15, 0));//5
        tb.getColumns().add(new Column("Sustancia Odorante", 20, 0));//6
        tb.getColumns().add(new Column("Nivel de Concentración Mínimo", 30, 1));//7
        tb.getColumns().add(new Column("Nivel de Concentración Medido", 30, 1));//8
        tb.getColumns().add(new Column("Observaciones", 30, 0));//9

        MySQLQuery dataQ = new MySQLQuery("SELECT c.code AS 'NIU', " 
                + "m.taken_dt AS 'Fecha de medición', " 
                + "DATE_FORMAT(m.taken_dt, '%H:%i') AS 'Hora de medición', " 
                + "(SELECT id FROM sigma.gt_type_gas WHERE active = 1) AS 'Tipo de Gas', " 
                + "TRUNCATE(m.pressure, 2) AS 'Presión Medida', " 
                + "1 AS 'Método', " 
                + "o.id AS 'Sustancia Odorante', " 
                + "TRUNCATE(o.min, 2) AS 'Nivel de concentración mínimo', " 
                + "TRUNCATE(m.odorant_amount, 2) AS 'Nivel de Concentración Medido', " 
                + "'No Aplica' "  
                + "FROM bill_measure m " 
                + "INNER JOIN bill_client_tank c ON c.id = m.client_id " 
                + "INNER JOIN sigma.bill_odorant o ON o.id = m.odorant_id " 
                + "WHERE m.span_id = ?1 AND m.taken_dt IS NOT NULL");
        dataQ.setParam(1, spanId);

        Object[][] data = dataQ.getRecords(conn);

        if (data != null && data.length > 0) {
            rep.getTables().add(tb);
            tb.setData(data);
        }
        return rep;
    }
    
    // RPT GRCS7
    public static MySQLReport getRptGRCS7(int spanId, BillInstance inst, Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("GRCS7 Revisiones Previas y Revisiones Periódicas Obligatorias – RPO", "", "", MySQLQuery.now(conn));
        BillSpan span = new BillSpan().select(spanId, conn);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(span.consMonth);

        rep.getSubTitles().add("Periodo: " + span.getConsLabel());
        rep.getSubTitles().add("Instancia: " + inst.name);
        if (span.state.equals("cons")) {
            rep.getSubTitles().add("¡ALERTA, INFORMACIÓN PROVISIONAL! CAUSACIÓN EN CURSO");
        }

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "###0.00"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "####"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "dd-MM-yyyy"));//3
        rep.setZoomFactor(80);
        rep.setVerticalFreeze(5);
        Table tb = new Table("GRCS7 Información de Revisiones Previas y Revisiones Periódicas Obligatorias");
        
        tb.getColumns().add(new Column("NIU", 15, 0));//0
        tb.getColumns().add(new Column("Grupo", 10, 0));//1
        tb.getColumns().add(new Column("Número de medidor", 25, 0));//2
        tb.getColumns().add(new Column("Tipo Revisión", 15, 0));//3
        tb.getColumns().add(new Column("Fecha de Revisión", 25, 3));//4
        tb.getColumns().add(new Column("Número de Certificado", 30, 2));//5
        tb.getColumns().add(new Column("Organismo de Certificación", 30, 0));//6
        tb.getColumns().add(new Column("Código de acreditación ONAC", 30, 0));//7
        tb.getColumns().add(new Column());

        MySQLQuery dataQ = new MySQLQuery("SELECT bct.code AS NIU, " 
                + "NULL, " 
                + "bm.`number` AS NumeroDeMedidor, " 
                + "bict.code AS TipoRevision, " 
                + "bic.chk_date AS FechaRevision, " 
                + "bic.cert_num AS NumeroCertificado, " 
                + "bii.name AS OrganismoDeCertificacion, " 
                + "bic.cod_onac AS CodigoDeAcreditacionOnac, "
                + "bct.id " 
                + "FROM bill_client_tank AS bct " 
                + "INNER JOIN bill_meter AS bm ON bct.id = bm.client_id " 
                + "INNER JOIN bill_inst_check AS bic ON bct.id = bic.client_id " 
                + "INNER JOIN sigma.bill_inst_check_type AS bict ON bict.id = bic.type_id " 
                + "INNER JOIN sigma.bill_inst_inspector AS bii ON bic.inspector_id = bii.id WHERE bic.chk_date >= '" + span.consMonth + "'");

        Object[][] data = dataQ.getRecords(conn);
        
        List<Object[]> ldata = new ArrayList();
        for (Object[] row : data) {
            int id = MySQLQuery.getAsInteger(row[row.length - 1]);
            BillInstCheck.InstCheckInfo ds = BillInstCheck.getNextDates(id, inst, null, conn);
            Date now = new ServerNow();
            Integer group = 0;
            if(now.after(ds.minDate) && now.before(ds.maxDate)){
                group = 1;
            }
            else if(((Date) row[row.length - 5]).before(ds.minDate) && ((int) row[row.length - 6]) == 2 ){
                group = 2;
            }
            else if(((Date) row[row.length - 5]).after(ds.maxDate) && ((int) row[row.length - 6]) == 2){
                group = 3;
            }
            else if (((Date) row[row.length - 5]).after(span.consMonth) && ((int) row[row.length - 6]) == 1){
                group = 4;
            }
            
            row[row.length - 8] = group;
            ldata.add(row);
        }
        if (!ldata.isEmpty()) {
            tb.setData(ldata);
            rep.getTables().add(tb);
        }
        return rep;
    }
    
    // RPT GRCS8
    public static MySQLReport getRptGRCS8(String initialDate, String finalDate, BillInstance inst, Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("GRCS8 Datos Consolidados – Esquema de Revisiones Periódicas", "", "", MySQLQuery.now(conn));
        GregorianCalendar gc = new GregorianCalendar();

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "###0.000000"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "####"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "dd-MM-yyyy"));//3
        rep.setZoomFactor(80);
        rep.setVerticalFreeze(5);
        Table tb = new Table("GRCS8 Datos Consolidados");
        tb.getColumns().add(new Column("Cuentas activas", 20, 2));//0
        tb.getColumns().add(new Column("Cuentas con certificado vigente", 35, 2));//1
        tb.getColumns().add(new Column("Cuentas suspendidas por esquema de revisiones periódicas", 65, 2));//2
        tb.getColumns().add(new Column("Cuentas suspendidas por causales diferentes al esquema de revisiones periódicas", 65, 2));//3
        tb.getColumns().add(new Column("Otras cuentas", 20, 2));//4
        tb.getColumns().add(new Column("Observaciones", 30, 0));//5
        
        MySQLQuery dataN = new MySQLQuery("SELECT c.id FROM bill_client_tank AS c WHERE c.active = 1");

        Object[][] myData = dataN.getRecords(conn);
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date myDate = formatter.parse(finalDate);
        
        int cant = 0;
        for (Object[] row : myData) {
            int id = MySQLQuery.getAsInteger(row[row.length-1]);
            BillInstCheck.InstCheckInfo ds = BillInstCheck.getNextDates(id, inst, null, conn);
            if(myDate.before(ds.maxDate)){
                cant++;
            }
        }
        
        MySQLQuery dataQ = new MySQLQuery("SELECT " 
                + "(SELECT COUNT(*) FROM bill_client_tank AS c WHERE c.active = 1) AS 'Cuentas Activas', " 
                + "NULL AS 'Cuentas con certificado vigente', " 
                + "(SELECT COUNT(*) FROM bill_susp AS bs2 WHERE bs2.susp_cause = 'review_expiration' AND bs2.susp_date BETWEEN '" + initialDate + " 00:00:00' AND '" + finalDate + " 23:59:59') AS 'Cuentas suspendidas por esquema de revisiones periódicas', " 
                + "(SELECT COUNT(*) FROM bill_susp AS bs3 WHERE bs3.susp_cause != 'review_expiration' AND bs3.susp_cause != 'outage' AND bs3.susp_date BETWEEN '" + initialDate + " 00:00:00' AND '" + finalDate + " 23:59:59') + (SELECT COUNT(*) FROM bill_susp bs WHERE bs.susp_cause IS NULL AND bs.susp_date BETWEEN '" + initialDate + " 00:00:00' AND '" + finalDate + " 23:59:59') AS 'Cuentas suspendidas por causales diferentes al esquema de revisiones periódicas', " 
                + "(SELECT COUNT(*) FROM bill_susp AS bs4 WHERE bs4.susp_cause = 'outage' AND bs4.susp_date BETWEEN '" + initialDate + " 00:00:00' AND '" + finalDate + " 23:59:59') AS 'Otras cuentas', " 
                + "IF((SELECT COUNT(*) FROM bill_susp AS bs4 WHERE bs4.susp_cause = 'outage' AND bs4.susp_date BETWEEN '" + initialDate + " 00:00:00' AND '" + finalDate + " 23:59:59') > 0, 'Cortes de Servicios', 'No aplica') AS Observaciones " 
                + "FROM bill_susp AS bs GROUP BY 'Cuentas Activas'");

        Object[][] data = dataQ.getRecords(conn);
        
        List<Object[]> defData = new ArrayList();
        for (Object[] row : data) {
            row[row.length - 5] = cant;
            defData.add(row);
        }
        
        if (!defData.isEmpty()) {
            tb.setData(defData);
            rep.getTables().add(tb);
        }
        return rep;
    }
    
    // RPT GRCS9
    public static MySQLReport getRptGRCS9(int spanId, BillInstance inst, Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("GRCS9 Revisiones Periódicas Obligatorias y Revisiones Previas - Cuentas No Normalizadas", "", "", MySQLQuery.now(conn));
        BillSpan span = new BillSpan().select(spanId, conn);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(span.consMonth);

        rep.getSubTitles().add("Periodo: " + span.getConsLabel());
        rep.getSubTitles().add("Instancia: " + inst.name);
        if (span.state.equals("cons")) {
            rep.getSubTitles().add("¡ALERTA, INFORMACIÓN PROVISIONAL! CAUSACIÓN EN CURSO");
        }

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "###0.000000"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "####"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "dd-MM-yyyy"));//3
        rep.setZoomFactor(80);
        rep.setVerticalFreeze(5);
        Table tb = new Table("Cuentas No Normalizadas");
        
        tb.getColumns().add(new Column("NIU", 15, 0));//0
        tb.getColumns().add(new Column("Caso", 15, 0));//1
        tb.getColumns().add(new Column("Número de medidor", 30, 0));//2
        tb.getColumns().add(new Column("Fecha de la última revisión", 30, 0));//3
        tb.getColumns().add(new Column("Número de Certificado", 30, 0));//4
        tb.getColumns().add(new Column("Organismo de Certificación", 30, 0));//4
        tb.getColumns().add(new Column("Código de acreditación ONAC", 30, 0));//4
        tb.getColumns().add(new Column("Observaciones", 50, 0));//4

        MySQLQuery dataC1 = new MySQLQuery("SELECT " 
                + "c.code AS 'NIU', " 
                + "1 AS 'Caso', " 
                + "bm.`number` AS 'Número de medidor', " 
                + "IF ((SELECT DATE_FORMAT(bic.chk_date, '%d-%m-%Y') FROM bill_inst_check AS bic WHERE bic.client_id = c.id ORDER BY id DESC LIMIT 1) IS NULL, DATE_FORMAT(c.net_building, '%d-%m-%Y'), (SELECT DATE_FORMAT(bic.chk_date, '%d-%m-%Y') FROM bill_inst_check AS bic WHERE bic.client_id = c.id ORDER BY id DESC LIMIT 1)) AS 'Fecha de la última revisión', " 
                + "(SELECT bic.cert_num FROM bill_inst_check AS bic WHERE bic.client_id = c.id ORDER BY id DESC LIMIT 1) AS 'Número de Certificado', " 
                + "(SELECT bii.name FROM bill_inst_check AS bic INNER JOIN sigma.bill_inst_inspector AS bii ON bic.inspector_id = bii.id WHERE bic.client_id = c.id ORDER BY bic.id DESC LIMIT 1) AS 'Organismo de Certificación', " 
                + "(SELECT bic.cod_onac FROM bill_inst_check AS bic WHERE bic.client_id = c.id ORDER BY id DESC LIMIT 1) AS 'Código de acreditación ONAC', " 
                + "'Ninguna' AS 'Observaciones' " 
                + "FROM bill_client_tank AS c " 
                + "INNER JOIN bill_susp AS bs ON bs.client_id = c.id " 
                + "INNER JOIN bill_meter AS bm ON c.id = bm.client_id " 
                + "INNER JOIN bill_reading AS br ON c.id = br.client_tank_id " 
                + "WHERE c.active = 1 " 
                + "AND bs.susp_date BETWEEN '" + span.consMonth + "' AND LAST_DAY('" + span.consMonth + "') " 
                + "AND bs.susp_cause = 'review_expiration' " 
                + "AND bs.recon_date IS NOT NULL " 
                + "AND (br.reading - br.last_reading) > 0 " 
                + "AND br.span_id = " + spanId + " GROUP BY c.id");
        
        Object[][] c1Data = dataC1.getRecords(conn);
        
        MySQLQuery dataC2 = new MySQLQuery("SELECT " 
                + "c.code AS 'NIU', " 
                + "2 AS 'Caso', " 
                + "bm.`number` AS 'Número de medidor', " 
                + "IF ((SELECT DATE_FORMAT(bic.chk_date, '%d-%m-%Y') FROM bill_inst_check AS bic WHERE bic.client_id = c.id ORDER BY id DESC LIMIT 1) IS NULL, DATE_FORMAT(c.net_building, '%d-%m-%Y'), (SELECT DATE_FORMAT(bic.chk_date, '%d-%m-%Y') FROM bill_inst_check AS bic WHERE bic.client_id = c.id ORDER BY id DESC LIMIT 1)) AS 'Fecha de la última revisión', " 
                + "(SELECT bic.cert_num FROM bill_inst_check AS bic WHERE bic.client_id = c.id ORDER BY id DESC LIMIT 1) AS 'Número de Certificado', " 
                + "(SELECT bii.name FROM bill_inst_check AS bic INNER JOIN sigma.bill_inst_inspector AS bii ON bic.inspector_id = bii.id WHERE bic.client_id = c.id ORDER BY bic.id DESC LIMIT 1) AS 'Organismo de Certificación', " 
                + "(SELECT bic.cod_onac FROM bill_inst_check AS bic WHERE bic.client_id = c.id ORDER BY id DESC LIMIT 1) AS 'Código de acreditación ONAC', " 
                + "'Cuentas no normalizadas debido a que se reconectaron de forma irregular' AS 'Observaciones' " 
                + "FROM bill_susp AS bs " 
                + "INNER JOIN bill_client_tank AS c ON bs.client_id = c.id " 
                + "INNER JOIN bill_meter AS bm ON c.id = bm.client_id " 
                + "INNER JOIN bill_meter_check AS bmc ON bm.id = bmc.meter_id " 
                + "WHERE c.active = 1 " 
                + "AND bs.recon_date IS NULL " 
                + "AND bmc.chk_date BETWEEN '" + span.consMonth + "' AND LAST_DAY('" + span.consMonth + "') " 
                + "AND bmc.novelty = 'irregularly' GROUP BY bmc.id");
        
        Object[][] c2Data = dataC2.getRecords(conn);
        
        MySQLQuery dataC3 = new MySQLQuery("SELECT " 
                + "c.code AS 'NIU', " 
                + "3 AS 'Caso', " 
                + "bm.`number` AS 'Número de medidor', " 
                + "IF ((SELECT DATE_FORMAT(bic.chk_date, '%d-%m-%Y') FROM bill_inst_check AS bic WHERE bic.client_id = c.id ORDER BY id DESC LIMIT 1) IS NULL, DATE_FORMAT(c.net_building, '%d-%m-%Y'), (SELECT DATE_FORMAT(bic.chk_date, '%d-%m-%Y') FROM bill_inst_check AS bic WHERE bic.client_id = c.id ORDER BY id DESC LIMIT 1)) AS 'Fecha de la última revisión', " 
                + "(SELECT bic.cert_num FROM bill_inst_check AS bic WHERE bic.client_id = c.id ORDER BY id DESC LIMIT 1) AS 'Número de Certificado', " 
                + "(SELECT bii.name FROM bill_inst_check AS bic INNER JOIN sigma.bill_inst_inspector AS bii ON bic.inspector_id = bii.id WHERE bic.client_id = c.id ORDER BY bic.id DESC LIMIT 1) AS 'Organismo de Certificación', " 
                + "(SELECT bic.cod_onac FROM bill_inst_check AS bic WHERE bic.client_id = c.id ORDER BY id DESC LIMIT 1) AS 'Código de acreditación ONAC', " 
                + "'Cuentas no normalizadas debido a que se conectaron sin autorización del distribuidor' AS 'Observaciones' " 
                + "FROM bill_client_tank AS c " 
                + "LEFT JOIN bill_inst_check AS bic ON c.id = bic.client_id " 
                + "INNER JOIN bill_meter AS bm ON c.id = bm.client_id " 
                + "INNER JOIN bill_meter_check AS bmc ON bm.id = bmc.meter_id " 
                + "WHERE bic.client_id IS NULL " 
                + "AND c.active = 1 " 
                + "AND bmc.novelty = 'no_authorization' " 
                + "AND bmc.chk_date BETWEEN '" + span.consMonth + "' AND LAST_DAY('" + span.consMonth + "')");
        
        Object[][] c3Data = dataC3.getRecords(conn);

        List<Item2> items = new ArrayList<>();
        
        for (Object[] row : c1Data) {
            items.add(getData2(row));
        }
        
        for (Object[] row : c2Data) {
            items.add(getData2(row));
        }
        
        for (Object[] row : c3Data) {
            items.add(getData2(row));
        }

        Object[][] defData = new Object[items.size()][8];
        for (int i = 0; i < items.size(); i++) {
            defData[i][0] = items.get(i).niu;
            defData[i][1] = items.get(i).caso;
            defData[i][2] = items.get(i).numMeter;
            defData[i][3] = items.get(i).dateReview;
            defData[i][4] = items.get(i).numCert;
            defData[i][5] = items.get(i).orgCert;
            defData[i][6] = items.get(i).onac;
            defData[i][7] = items.get(i).observ;
        }
        
        if (defData != null && defData.length > 0) {
            rep.getTables().add(tb);
            tb.setData(defData);
        }
        return rep;
    }
    
    // RPT GRI1
    public static MySQLReport getRptGRI1(Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("GRI1 Estaciones de Regulación de Gas Natural y Almacenamientos de GLP, dispuestos para la distribución de gas combustible por redes de tubería", "", "", MySQLQuery.now(conn));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "###0.000000"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "####"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "dd-MM-yyyy"));//3
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "HH:mm"));//4
        rep.setZoomFactor(80);
        rep.setVerticalFreeze(5);
        Table tb = new Table("Estaciones de Regulación de Gas Natural y Almacenamientos de GLP, dispuestos para la distribución de gas combustible por redes de tubería");

        tb.getColumns().add(new Column("Código DANE", 25, 0));
        tb.getColumns().add(new Column("Longitud", 25, 1));
        tb.getColumns().add(new Column("Latitud", 20, 1));
        tb.getColumns().add(new Column("Altitud", 20, 2));
        tb.getColumns().add(new Column("Tipo de Estación", 20, 0));
        tb.getColumns().add(new Column("Código Estación", 20, 0));
        tb.getColumns().add(new Column("Fecha Entrada en Operación", 30, 3));
        tb.getColumns().add(new Column("Fecha Cumplimiento VUN", 30, 0));
        tb.getColumns().add(new Column("Resolución", 30, 0));
        tb.getColumns().add(new Column("Capacidad regulación/almacenamiento", 40, 2));
        tb.getColumns().add(new Column("Código del Certificado de conformidad del almacenamiento de GLP", 60, 0));
        tb.getColumns().add(new Column("Nombre del organismo de inspección que certificó el almacenamiento de GLP", 60, 0));

        MySQLQuery dataQ = new MySQLQuery("SELECT " 
                + "p.code AS 'Código DANE', " 
                + "TRUNCATE(bs.lon, 6) AS 'Longitud', " 
                + "TRUNCATE(bs.lat, 6) AS 'Latitud', " 
                + "TRUNCATE(bs.alt, 0) AS 'Altitud', " 
                + "3 AS 'Tipo de Estación', " 
                + "bs.code AS 'Código Estación', " 
                + "bs.beg_date AS 'Fecha Entrada en Operación', " 
                + "NULL AS 'Fecha Cumplimiento VUN', " 
                + "bm.resolution AS 'Resolución', " 
                + "bs.capacity AS 'Capacidad regulación/almacenamiento', " 
                + "bs.cod_cert AS 'Código del Certificado de conformidad del almacenamiento de GLP', " 
                + "bii.name AS 'Nombre del organismo de inspección que certificó el almacenamiento de GLP' " 
                + "FROM sigma.bill_station AS bs " 
                + "INNER JOIN sigma.bill_instance AS i ON bs.inst_id = i.id " 
                + "INNER JOIN sigma.dane_poblado AS p ON p.id = i.pob_id " 
                + "INNER JOIN sigma.bill_market AS bm ON i.market_id = bm.id " 
                + "LEFT JOIN sigma.bill_inst_inspector AS bii ON bs.inspector_id = bii.id");

        Object[][] data = dataQ.getRecords(conn);

        if (data != null && data.length > 0) {
            rep.getTables().add(tb);
            tb.setData(data);
        }
        return rep;
    }
    
    private static class Item {
        Integer concept;
        String other;
        Integer valUni;
        Integer valFac;
        Integer iva;
    }

    private static Item getData(Object[] row) {
        Item item = new Item();
        item.concept = MySQLQuery.getAsInteger(row[0]);
        item.other = MySQLQuery.getAsString(row[1]);
        item.valUni = MySQLQuery.getAsInteger(row[2]);
        item.valFac = MySQLQuery.getAsInteger(row[3]);
        item.iva = MySQLQuery.getAsInteger(row[4]);
        return item;
    }
    
    private static class Item2 {
        String niu;
        Integer caso;
        String numMeter;
        String dateReview;
        Integer numCert;
        String orgCert;
        String onac;
        String observ;
    }

    private static Item2 getData2(Object[] row) {
        Item2 item = new Item2();
        item.niu = MySQLQuery.getAsString(row[0]);
        item.caso = MySQLQuery.getAsInteger(row[1]);
        item.numMeter = MySQLQuery.getAsString(row[2]);
        item.dateReview = MySQLQuery.getAsString(row[3]);
        item.numCert = MySQLQuery.getAsInteger(row[4]);
        item.orgCert = MySQLQuery.getAsString(row[5]);
        item.onac = MySQLQuery.getAsString(row[6]);
        item.observ = MySQLQuery.getAsString(row[7]);
        return item;
    }
}
