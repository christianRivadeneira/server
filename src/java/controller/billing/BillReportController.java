package controller.billing;

import api.bill.model.BillBuilding;
import api.bill.model.BillClientTank;
import api.bill.model.BillInstance;
import api.bill.model.BillSpan;
import api.sys.model.SysCfg;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.billing.constants.Accounts;
import utilities.MySQLPreparedQuery;
import utilities.MySQLQuery;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.HeaderColumn;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.SummaryRow;
import utilities.mysqlReport.Table;
import utilities.mysqlReport.TableHeader;

public class BillReportController {

    private static BigDecimal getClientsDebtsBalance(MySQLPreparedQuery debQ, MySQLPreparedQuery credQ, int clieId, int accId) throws SQLException, Exception {
        credQ.setParameter(1, accId);
        credQ.setParameter(2, clieId);
        debQ.setParameter(1, accId);
        debQ.setParameter(2, clieId);
        return debQ.getAsBigDecimal(true).subtract(credQ.getAsBigDecimal(true));
    }

    public static MySQLReport getRptCart(BillInstance inst, Connection conn) throws Exception {

        MySQLPreparedQuery credQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_cred_id = ?1 AND t.cli_tank_id = ?2", conn);
        MySQLPreparedQuery debQ = new MySQLPreparedQuery(" SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_deb_id  = ?1 AND t.cli_tank_id = ?2", conn);

        MySQLReport rep = new MySQLReport("Clientes en Cartera - " + inst.name, "", "Hoja 1", MySQLQuery.now(conn));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.00"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//2
        rep.setZoomFactor(85);

        Table tb = new Table("model");
        int colw = 18;

        TableHeader th = new TableHeader();

        th.getColums().add(new HeaderColumn("Num. Inst", 1, 2));
        th.getColums().add(new HeaderColumn("Nombre", 1, 2));
        th.getColums().add(new HeaderColumn("Meses", 3, 1));
        th.getColums().add(new HeaderColumn("Cartera", 4, 1));
        th.getColums().add(new HeaderColumn("Intereses", 3, 1));
        th.getColums().add(new HeaderColumn("Totales", 2, 1));

        String old = "Anteriores";
        String srv = "Otros Serv.";
        String glp = "Consumo";
        tb.getColumns().add(new Column("Num. Inst", 12, 0));//0
        tb.getColumns().add(new Column("Nombre", 35, 0));//1
        tb.getColumns().add(new Column(glp, 12, 2));//2
        tb.getColumns().add(new Column(old, 12, 2));//3
        tb.getColumns().add(new Column(srv, 12, 2));//4
        tb.getColumns().add(new Column(glp, colw, 1));//5
        tb.getColumns().add(new Column(old, colw, 1));//7
        tb.getColumns().add(new Column(srv, colw, 1));//9
        tb.getColumns().add(new Column("Inter. Financ.", colw, 1));//10
        tb.getColumns().add(new Column(glp, colw, 1));//6
        tb.getColumns().add(new Column(old, colw, 1));//8
        tb.getColumns().add(new Column(srv, colw, 1));//11
        tb.getColumns().add(new Column("Cartera", colw, 1));//11
        tb.getColumns().add(new Column("Interes", colw, 1));//11
        tb.setSummaryRow(new SummaryRow("Totales", 2));

        tb.getHeaders().add(th);

        //meses en deuda
        MySQLPreparedQuery debtsConsQ = BillClientTankController.getCartBySpanQuery(Accounts.C_CAR_GLP, conn);
        MySQLPreparedQuery debtsSrvQ = BillClientTankController.getCartBySpanQuery(Accounts.C_CAR_SRV, Accounts.C_CAR_INTE_CRE, conn);

        if (inst.isTankInstance()) {
            List<BillBuilding> buildings = BillBuilding.getAll(conn);
            for (int i = 0; i < buildings.size(); i++) {
                BillBuilding billB = buildings.get(i);
                BillClientTank[] clients = BillClientTank.getByBuildId(billB.id, false, conn);
                String title = billB.oldId + " " + billB.name + " " + billB.address + " " + clients.length + " Clientes";
                getClientsDebts(rep, tb, title, clients, credQ, debQ, debtsConsQ, debtsSrvQ, conn);
            }
        } else {
            BillClientTank[] clients = BillClientTank.getAll(true, conn);
            String title = "Clientes";
            getClientsDebts(rep, tb, title, clients, credQ, debQ, debtsConsQ, debtsSrvQ, conn);
        }
        return rep;

    }

    private static void getClientsDebts(MySQLReport rep, Table model, String title, BillClientTank[] clients, MySQLPreparedQuery credQ, MySQLPreparedQuery debQ, MySQLPreparedQuery debtsConsQ, MySQLPreparedQuery debtsSrvQ, Connection conn) throws Exception {

        List<Object[]> data = new ArrayList<>();
        for (BillClientTank cl : clients) {
            BigDecimal cartGlp = getClientsDebtsBalance(debQ, credQ, cl.id, Accounts.C_CAR_GLP);
            BigDecimal inteGlp = getClientsDebtsBalance(debQ, credQ, cl.id, Accounts.C_INT_GLP);
            BigDecimal cartOld = getClientsDebtsBalance(debQ, credQ, cl.id, Accounts.C_CAR_OLD);
            BigDecimal inteOld = getClientsDebtsBalance(debQ, credQ, cl.id, Accounts.C_INT_OLD);

            BigDecimal cartSrv = getClientsDebtsBalance(debQ, credQ, cl.id, Accounts.C_CAR_SRV);
            BigDecimal inteSrv = getClientsDebtsBalance(debQ, credQ, cl.id, Accounts.C_INT_SRV);
            BigDecimal cartCre = getClientsDebtsBalance(debQ, credQ, cl.id, Accounts.C_CAR_INTE_CRE);

            int monthsCons;
            int monthsSrv;
            int monthsOld = 0;

            if (cartGlp.add(inteGlp).add(cartOld).add(inteOld).add(cartSrv).add(inteSrv).add(cartCre).compareTo(BigDecimal.ZERO) > 0) {
                monthsCons = BillClientTankController.getDebtMonths(cl.id, debtsConsQ, conn, cartGlp);
                monthsSrv = BillClientTankController.getDebtMonths(cl.id, debtsSrvQ, conn, cartSrv.add(cartCre));

                if (cartOld.add(inteOld).compareTo(BigDecimal.ZERO) != 0) {
                    monthsOld = 1;
                }
                Object[] row = new Object[14];
                row[0] = (cl.numInstall);
                row[1] = cl.firstName + (cl.lastName != null ? " " + cl.lastName : "");
                row[2] = (monthsCons);
                row[3] = (monthsOld);
                row[4] = (monthsSrv);
                row[5] = (cartGlp);
                row[6] = (cartOld);
                row[7] = (cartSrv);
                row[8] = (cartCre);
                row[9] = (inteGlp);
                row[10] = (inteOld);
                row[11] = (inteSrv);
                row[12] = (cartGlp.add(cartOld).add(cartSrv).add(cartCre));
                row[13] = (inteGlp.add(inteOld).add(inteSrv));
                data.add(row);
            }
        }
        if (!data.isEmpty()) {
            Table tbl = new Table(model);
            tbl.setData(data);
            tbl.setTitle(title);
            //tbl.setColumns(model.getColumns());
            //tbl.setHeaders(model.getHeaders());
            rep.getTables().add(tbl);
        }
    }

    public static MySQLReport getCartAgesTank(String type, String typeCli, boolean minValue, boolean months, BillInstance inst, Connection conn, Connection gralConn) throws Exception {

        MySQLReport rep = new MySQLReport("Cartera por Edades - " + inst.name, "", "Hoja 1", MySQLQuery.now(conn));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.00"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.000"));//3

        SysCfg cfg = SysCfg.select(gralConn);

        rep.setZoomFactor(80);
        rep.setVerticalFreeze(4);
        rep.setHorizontalFreeze(3);

        Table tb = new Table("Clientes");
        tb.getColumns().add(new Column("Cliente", 35, 0));//0
        tb.getColumns().add(new Column("Dirección", 35, 0));//1
        tb.getColumns().add(new Column("Barrio", 30, 0));//2
        tb.getColumns().add(new Column("Zona", 30, 0));//3
        tb.getColumns().add(new Column("Tipo", 30, 0));//4
        tb.getColumns().add(new Column("Corte Efectivo", 15, 0));//5
        tb.getColumns().add(new Column("Num. Inst", 12, 0));//6
        tb.getColumns().add(new Column("Nombre", 30, 0));//7
        tb.getColumns().add(new Column("Meses", 9, 2));//8
        tb.getColumns().add(new Column("1 Mes", 16, 1));//9
        tb.getColumns().add(new Column("2 Meses", 16, 1));//10
        tb.getColumns().add(new Column("3 Meses", 16, 1));//11
        tb.getColumns().add(new Column("4 Meses", 16, 1));//12
        tb.getColumns().add(new Column("5 Meses", 16, 1));//13
        tb.getColumns().add(new Column("6 Meses", 16, 1));//14
        tb.getColumns().add(new Column("> 6 Meses", 16, 1));//15
        tb.getColumns().add(new Column("Total Cartera", 18, 1));//16
        tb.getColumns().add(new Column("Total Interés", 18, 1));//17
        tb.getColumns().add(new Column("Lec. Periodo", 18, 3));//18
        tb.getColumns().add(new Column("Lec. Anterior", 18, 3));//19
        tb.getColumns().add(new Column("Cons. en Recaudo", 20, 3));//20
        tb.setSummaryRow(new SummaryRow("Totales", 7));

        BillSpan reca = BillSpan.getByState("reca", conn);

        List<BillBuilding> buildings = BillBuilding.getAll(conn);

        MySQLPreparedQuery credQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_cred_id = ?1 AND t.cli_tank_id = ?2", conn);
        MySQLPreparedQuery debQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_deb_id = ?1 AND t.cli_tank_id = ?2", conn);
        MySQLPreparedQuery consQ = new MySQLPreparedQuery("SELECT reading, last_reading FROM bill_reading WHERE span_id = " + reca.id + " AND client_tank_id = ?1", conn);
        MySQLPreparedQuery credQAux = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_cred_id = ?1 AND t.cli_tank_id = ?2", conn);;
        MySQLPreparedQuery debQAux = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_deb_id = ?1 AND t.cli_tank_id = ?2", conn);;
        //meses en deuda
        int cartAcc;
        int inteAcc;
        int cartAccAux = 0;
        int inteAccAux = 0;

        switch (type) {
            case "glp":
                cartAcc = Accounts.C_CAR_GLP;
                inteAcc = Accounts.C_INT_GLP;
                break;
            case "srv":
                cartAcc = Accounts.C_CAR_SRV;
                inteAcc = Accounts.C_INT_SRV;
                break;
            case "finan":
                cartAcc = Accounts.C_CAR_FINAN_DEU;
                inteAcc = Accounts.C_INT_FINAN_DEU;
                break;
            case "glpfin":
                cartAcc = Accounts.C_CAR_GLP;
                inteAcc = Accounts.C_INT_GLP;
                cartAccAux = Accounts.C_CAR_FINAN_DEU;
                inteAccAux = Accounts.C_INT_FINAN_DEU;
                break;
            case "contrib":
                cartAcc = Accounts.C_CAR_CONTRIB;
                inteAcc = Accounts.C_INT_CONTRIB;
                break;
            default:
                throw new Exception("Tipo desconocido: " + type);
        }

        MySQLPreparedQuery debtsQ;
        if (type.equals("glpfin")) {
            debtsQ = BillClientTankController.getCartBySpanQuery(cartAcc, cartAccAux, conn);
        } else {
            debtsQ = BillClientTankController.getCartBySpanQuery(cartAcc, conn);
        }

        List<Object[]> data = new ArrayList<>();
        for (BillBuilding bl : buildings) {
            Object[][] buildData = new MySQLQuery("SELECT n.`name`, s.`name`, ct.description "
                    + "FROM ord_tank_client AS tc "
                    + "INNER JOIN neigh AS n ON n.id = tc.neigh_id "
                    + "INNER JOIN sector AS s ON s.id = n.sector_id "
                    + "LEFT JOIN est_tank_category etc ON etc.id = tc.categ_id "
                    + "LEFT JOIN est_categ_type ct ON ct.id = etc.type_id "
                    + "WHERE tc.mirror_id = " + bl.id + " "
                    + (typeCli.equals("all") ? "" : (typeCli.equals("res") ? "AND ct.residential = 1 " : "AND ct.residential = 0 "))
                    + "AND tc.bill_instance_id = " + inst.id).getRecords(gralConn);
            String neigh = "";
            String sector = "";
            String clientType = null;
            if (buildData.length > 0) {
                neigh = MySQLQuery.getAsString(buildData[0][0]);
                sector = MySQLQuery.getAsString(buildData[0][1]);
                clientType = MySQLQuery.getAsString(buildData[0][2]);
            }
            BillClientTank[] clients = BillClientTank.getByBuildId(bl.id, false, conn);

            if (typeCli.equals("all") || (!typeCli.equals("all") && clientType != null)) {
                for (BillClientTank cl : clients) {
                    Object[] row = new Object[21];
                    row[0] = bl.name;
                    row[1] = bl.address;
                    row[2] = neigh;
                    row[3] = sector;
                    row[4] = clientType;
                    row[5] = (new MySQLQuery("SELECT COUNT(*)>0 FROM bill_susp s WHERE s.recon_date IS NULL AND s.cancelled = 0 AND s.client_id = ?1")
                            .setParam(1, cl.id).getAsBoolean(conn) ? "Si" : "No");
                    row[6] = cl.numInstall;
                    row[7] = cl.firstName + (cl.lastName != null ? " " + cl.lastName : "");
                    //consumos

                    BigDecimal read = BigDecimal.ZERO;
                    BigDecimal lastRead = BigDecimal.ZERO;

                    consQ.setParameter(1, cl.id);
                    Object[] consRow = consQ.getRecord();
                    if (consRow != null) {
                        read = MySQLQuery.getAsBigDecimal(consRow[0], true);
                        lastRead = MySQLQuery.getAsBigDecimal(consRow[1], true);
                    }
                    row[18] = read;
                    row[19] = lastRead;
                    row[20] = read.subtract(lastRead);

                    //cartera
                    credQ.setParameter(1, cartAcc);
                    credQ.setParameter(2, cl.id);

                    debQ.setParameter(1, cartAcc);
                    debQ.setParameter(2, cl.id);
                    BigDecimal debit = debQ.getAsBigDecimal(true);
                    BigDecimal credit = credQ.getAsBigDecimal(true);

                    BigDecimal debitAux = BigDecimal.ZERO;
                    BigDecimal creditAux = BigDecimal.ZERO;
                    if (type.equals("glpfin")) {
                        credQAux.setParameter(1, cartAccAux);
                        credQAux.setParameter(2, cl.id);

                        debQAux.setParameter(1, cartAccAux);
                        debQAux.setParameter(2, cl.id);
                        debitAux = debQAux.getAsBigDecimal(true);
                        creditAux = credQAux.getAsBigDecimal(true);
                    }
                    debit = debit.add(debitAux);
                    credit = credit.add(creditAux);
                    BigDecimal total = debit.subtract(credit);
                    row[16] = total;

                    //intereses
                    credQ.setParameter(1, inteAcc);
                    credQ.setParameter(2, cl.id);

                    debQ.setParameter(1, inteAcc);
                    debQ.setParameter(2, cl.id);

                    if (type.equals("glpfin")) {
                        credQAux.setParameter(1, inteAccAux);
                        credQAux.setParameter(2, cl.id);

                        debQAux.setParameter(1, inteAccAux);
                        debQAux.setParameter(2, cl.id);
                        row[17] = debQ.getAsBigDecimal(true).add(debQAux.getAsBigDecimal(true)).subtract(credQ.getAsBigDecimal(true)).subtract(credQAux.getAsBigDecimal(true));
                    } else {
                        row[17] = debQ.getAsBigDecimal(true).subtract(credQ.getAsBigDecimal(true));

                    }

                    if (bl.id == 89) {
                        System.err.println("************** total: " + total.doubleValue());
                        System.err.println("************** numINstall: " + cl.numInstall);
                        System.err.println("************** total: " + cl.firstName + (cl.lastName != null ? " " + cl.lastName : ""));
                    }

                    if ((total).compareTo(BigDecimal.ZERO) != 0) {
                        BigDecimal[] debts = getDebtClient(cl.id, debtsQ, total);
                        Integer monthsAc = debts[7].intValue();//meses en mora
                        row[8] = monthsAc;
                        System.arraycopy(debts, 0, row, 9, 7); //7 es la longitud 
                        if (bl.id == 89) {
                            System.err.println("************** months: " + monthsAc);
                        }
                        if (minValue && total.compareTo(cfg.suspValue) > 0) {
                            if (months && monthsAc >= inst.suspDebtMonths) {
                                data.add(row);
                            } else if (!months) {
                                data.add(row);
                            }
                        } else if (!minValue) {
                            data.add(row);
                        }
                    }
                }
            }
        }
        if (!data.isEmpty()) {
            tb.setData(data);
            rep.getTables().add(tb);
        }
        return rep;
    }

    public static MySQLReport getCartAgesNet(String type, BillInstance inst, Connection conn, Connection gralConn) throws Exception {
        MySQLReport rep = new MySQLReport("Cartera por Edades - " + inst.name, "", "Hoja 1", MySQLQuery.now(conn));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.00"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.000"));//3

        rep.setZoomFactor(80);
        rep.setVerticalFreeze(4);
        rep.setHorizontalFreeze(3);

        Table tb = new Table("Clientes");
        tb.getColumns().add(new Column("Dirección", 35, 0));//0
        tb.getColumns().add(new Column("Código", 12, 0));//1
        tb.getColumns().add(new Column("Nombre", 30, 0));//2
        tb.getColumns().add(new Column("Meses", 9, 2));//3
        tb.getColumns().add(new Column("1 Mes", 16, 1));//4
        tb.getColumns().add(new Column("2 Meses", 16, 1));//5
        tb.getColumns().add(new Column("3 Meses", 16, 1));//6
        tb.getColumns().add(new Column("4 Meses", 16, 1));//7
        tb.getColumns().add(new Column("5 Meses", 16, 1));//8
        tb.getColumns().add(new Column("6 Meses", 16, 1));//9
        tb.getColumns().add(new Column("> 6 Meses", 16, 1));//10
        tb.getColumns().add(new Column("Total Cartera", 18, 1));//11
        tb.getColumns().add(new Column("Total Interés", 18, 1));//12

        if (type.equals("glp")) {
            tb.getColumns().add(new Column("Lec. Periodo", 18, 3));//13
            tb.getColumns().add(new Column("Lec. Anterior", 18, 3));//14
            tb.getColumns().add(new Column("Cons. en Recaudo", 20, 3));//15
        }

        tb.setSummaryRow(new SummaryRow("Totales", 4));

        BillSpan reca = BillSpan.getByState("reca", conn);

        MySQLPreparedQuery credQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_cred_id = ?1 AND t.cli_tank_id = ?2", conn);
        MySQLPreparedQuery debQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_deb_id = ?1 AND t.cli_tank_id = ?2", conn);
        MySQLPreparedQuery consQ = new MySQLPreparedQuery("SELECT reading, last_reading FROM bill_reading WHERE span_id = " + reca.id + " AND client_tank_id = ?1", conn);

        int cartAcc;
        int inteAcc;

        switch (type) {
            case "glp":
                cartAcc = Accounts.C_CAR_GLP;
                inteAcc = Accounts.C_INT_GLP;
                break;
            case "srv":
                cartAcc = Accounts.C_CAR_SRV;
                inteAcc = Accounts.C_INT_SRV;
                break;
            case "finan":
                cartAcc = Accounts.C_CAR_FINAN_DEU;
                inteAcc = Accounts.C_INT_FINAN_DEU;
                break;
            case "contrib":
                cartAcc = Accounts.C_CAR_CONTRIB;
                inteAcc = Accounts.C_INT_CONTRIB;
                break;
            default:
                throw new Exception("Tipo desconocido: " + type);
        }

        MySQLPreparedQuery debtsQ = BillClientTankController.getCartBySpanQuery(cartAcc, conn);

        List<Object[]> data = new ArrayList<>();
        BillClientTank[] clients = BillClientTank.getAll(true, conn);

        for (BillClientTank cl : clients) {
            Object[] row = new Object[type.equals("glp") ? 16 : 13];
            row[0] = cl.address;
            row[1] = cl.numInstall;
            row[2] = cl.firstName + (cl.lastName != null ? " " + cl.lastName : "");

            //consumos
            BigDecimal read = BigDecimal.ZERO;
            BigDecimal lastRead = BigDecimal.ZERO;
            if (type.equals("glp")) {
                consQ.setParameter(1, cl.id);
                Object[] consRow = consQ.getRecord();
                if (consRow != null) {
                    read = MySQLQuery.getAsBigDecimal(consRow[0], true);
                    lastRead = MySQLQuery.getAsBigDecimal(consRow[1], true);
                }
            }

            //cartera
            credQ.setParameter(1, cartAcc);
            credQ.setParameter(2, cl.id);

            debQ.setParameter(1, cartAcc);
            debQ.setParameter(2, cl.id);
            BigDecimal debit = debQ.getAsBigDecimal(true);
            BigDecimal credit = credQ.getAsBigDecimal(true);
            row[11] = debit.subtract(credit);

            //intereses
            credQ.setParameter(1, inteAcc);
            credQ.setParameter(2, cl.id);

            debQ.setParameter(1, inteAcc);
            debQ.setParameter(2, cl.id);
            row[12] = debQ.getAsBigDecimal(true).subtract(credQ.getAsBigDecimal(true));
            if(type.equals("glp")){
            row[13] = read;
            row[14] = lastRead;
            row[15] = read.subtract(lastRead);

            if (((BigDecimal) row[14]).compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal[] debts = getDebtClient(cl.id, debtsQ, (BigDecimal) row[14]);
                row[3] = debts[7].intValue();//meses en mora
                System.arraycopy(debts, 0, row, 4, 7); //7 es la longitud 
                data.add(row);
            }
            }
            else{
                data.add(row);
                //TODO
                //Aquí implementación para tipos diferentes a consumo (srv, finan, contrib)
            }
        }

        if (!data.isEmpty()) {
            tb.setData(data);
            rep.getTables().add(tb);
        }
        return rep;
    }

    public static BigDecimal[] getDebtClient(int clientId, MySQLPreparedQuery debtsQ, BigDecimal totalDebt) throws Exception {
        if (totalDebt.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        debtsQ.setParameter(1, clientId);
        Object[][] debts = debtsQ.getRecords();
        List<BigDecimal> debtsCli = new ArrayList<>();
        BigDecimal sum = BigDecimal.ZERO;
        int months = 0;

        for (int i = 0; i < debts.length && totalDebt.compareTo(BigDecimal.ZERO) > 0; i++) {
            Object[] debtRow = debts[i];
            BigDecimal debt = min((BigDecimal) debtRow[1], totalDebt);
            totalDebt = totalDebt.subtract(debt);
            if (debtsCli.size() > 5) {
                sum = sum.add(debt);
            } else {
                debtsCli.add(debt);
            }
            months++;
        }

        if (sum.compareTo(BigDecimal.ZERO) > 0) {
            debtsCli.add(sum);
        } else {
            for (int i = debtsCli.size(); i < 7; i++) {
                debtsCli.add(BigDecimal.ZERO);
            }
        }
        debtsCli.add(new BigDecimal(months));
        BigDecimal[] debs = new BigDecimal[debtsCli.size()];
        return debtsCli.toArray(debs);
    }

    public static BigDecimal[] getDebtClient(Object[][] debts, BigDecimal totalDebt) throws Exception {
        if (totalDebt.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        List<BigDecimal> debtsCli = new ArrayList<>();
        BigDecimal sum = BigDecimal.ZERO;
        int months = 0;

        for (int i = 0; i < debts.length && totalDebt.compareTo(BigDecimal.ZERO) > 0; i++) {
            Object[] debtRow = debts[i];
            BigDecimal debt = min((BigDecimal) debtRow[1], totalDebt);
            totalDebt = totalDebt.subtract(debt);
            if (debtsCli.size() > 5) {
                sum = sum.add(debt);
            } else {
                debtsCli.add(debt);
            }
            months++;
        }

        if (sum.compareTo(BigDecimal.ZERO) > 0) {
            debtsCli.add(sum);
        } else {
            for (int i = debtsCli.size(); i < 7; i++) {
                debtsCli.add(BigDecimal.ZERO);
            }
        }
        debtsCli.add(new BigDecimal(months));
        BigDecimal[] debs = new BigDecimal[debtsCli.size()];
        return debtsCli.toArray(debs);
    }

    private static BigDecimal min(BigDecimal n1, BigDecimal n2) {
        if (n1.compareTo(n2) < 0) {
            return n1;
        }
        return n2;
    }
}
