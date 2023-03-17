package api.bill.writers.bill;

import api.bill.model.BillBill;
import api.bill.model.BillBuildFactor;
import api.bill.model.BillClieCau;
import api.bill.model.BillClieRebill;
import api.bill.model.BillClientFactor;
import api.bill.model.BillClientTank;
import api.bill.model.BillInstCheck;
import api.bill.model.BillInstance;
import api.bill.model.BillMeter;
import api.bill.model.BillPlan;
import api.bill.model.BillPriceSpan;
import api.bill.model.BillPrintRequest;
import api.bill.model.BillReadingFault;
import static api.bill.model.BillReadingFault.getFaultDescByBillQuery;
import api.bill.model.BillSpan;
import api.sys.model.SysCfg;
import controller.billing.BillClientTankController;
import controller.billing.BillSpanController;
import static controller.billing.BillSpanController.getAdjusment;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.billing.BillBillPres;
import model.billing.constants.Accounts;
import model.billing.constants.Transactions;
import model.system.SessionLogin;
import utilities.MySQLPreparedInsert;
import utilities.MySQLPreparedQuery;
import utilities.MySQLPreparedUpdate;
import utilities.MySQLQuery;
import utilities.cast;
import web.marketing.smsClaro.ClaroSmsSender;
import web.quality.SendMail;

public class GetBills {
    
    private static DecimalFormat factorFormat = new DecimalFormat("0.00");

    public static void create(Integer clientTankId, BillInstance inst, SysCfg sysCfg, BillPrintRequest req, Connection sigmaConn, Connection billConn, SessionLogin sl, BillWriter writer) throws Exception {
        DecimalFormat twoDecPlacesF = new DecimalFormat("#,###.00");
        int userId = sl.employeeId;
        billConn.setAutoCommit(false);
        if (req != null) {
            req.setStatus("running", "Iniciando...");
        }
        new MySQLQuery("SET foreign_key_checks = 0;").executeUpdate(billConn);

        BillSpan reca;
        BillClientTank[] clients;
        if (clientTankId == null) {
            reca = BillSpan.getByState("reca", billConn);
            clients = BillClientTank.getAll(true, billConn);
            new MySQLQuery("UPDATE bill_bill b SET b.active = 0 WHERE b.bill_span_id = " + reca.id + " AND b.payment_date IS NULL").executeUpdate(billConn);
        } else {
            reca = BillSpan.getByClient("reca", clientTankId, inst, billConn);
            clients = new BillClientTank[1];
            clients[0] = new BillClientTank().select(clientTankId, billConn);
            BillBill.anullActiveBills(clientTankId, reca.id, billConn);
        }

        //cuenta causaciones en el periodo
        Integer contCau = new MySQLQuery("SELECT COUNT(t.id) FROM  bill_transaction t WHERE t.trans_type_id = " + Transactions.CAUSA + " AND t.bill_span_id = " + reca.id).getAsInteger(billConn);
        if (contCau == null || contCau == 0) {
            throw new Exception("Aun no ha causado los servicios para este periodo.\nImposible continuar.");
        }

        if (req != null) {
            req.requestedBills = clients.length;
            req.update();
        }

        BillBill[] bills = new BillBill[clients.length];
        BillForPrint[] billsFp = new BillForPrint[clients.length];
        MySQLPreparedInsert insertBill = BillBill.getInsertQuery(billConn);

        for (int i = 0; i < clients.length; i++) {
            BillClientTank client = clients[i];
            BillBill bill = new BillBill();
            bill.active = true;
            bill.billSpanId = reca.id;
            bill.creationDate = new Date();
            bill.creatorId = userId;
            bill.clientTankId = client.id;
            bill.total = true;
            BillBill.insert(bill, insertBill);
            bills[i] = bill;
        }
        //BillClieCau cau=new BillClieCau();        

        Integer[] ids = insertBill.executeBatchWithKeys();
        for (int i = 0; i < clients.length; i++) {
            bills[i].id = (ids[i]);
        }
        Map<Integer, BigDecimal> prices = BillPriceSpan.getPricesMap(billConn, reca.id);

        int[] currAccs = new int[]{Accounts.C_CONS, Accounts.C_CONS_SUBS, Accounts.C_CONTRIB, Accounts.C_REBILL, Accounts.C_BASI, Accounts.C_CUOTA_SER_EDI, Accounts.C_CUOTA_SER_CLI_GLP, Accounts.C_CUOTA_SER_CLI_SRV, Accounts.C_CUOTA_FINAN_DEU, Accounts.C_CUOTA_INT_CRE, Accounts.C_RECON};
        int[] cartAccs = new int[]{Accounts.C_CAR_GLP, Accounts.C_CAR_SRV, Accounts.C_CAR_FINAN_DEU, Accounts.C_CAR_CONTRIB, Accounts.C_CAR_INTE_CRE, Accounts.C_CAR_OLD, Accounts.C_INT_GLP, Accounts.C_INT_SRV, Accounts.C_INT_FINAN_DEU, Accounts.C_INT_CONTRIB, Accounts.C_INT_OLD};

        MySQLPreparedUpdate updateBillPs = BillBill.getUpdateQuery(billConn);
        MySQLPreparedInsert insertPlanPs = BillPlan.getInsertQuery(billConn);
        MySQLPreparedInsert insertPresPs = BillBillPres.getInsertQuery(billConn);

        MySQLPreparedQuery neighQ = new MySQLPreparedQuery("SELECT name FROM sigma.neigh WHERE id = ?1", billConn);

        MySQLPreparedQuery addressQ = new MySQLPreparedQuery("SELECT "
                + "b.address, b.name, IFNULL(t.short_name,'Apto'), b.id "
                + "FROM bill_building AS b "
                + "LEFT JOIN bill_build_type AS t ON t.id = b.build_type_id "
                + "WHERE b.id = ?1", billConn);

        MySQLPreparedQuery buildFactorQ = BillBuildFactor.getFactorQuery(billConn);
        MySQLPreparedQuery clientFactorQ = BillClientFactor.getFactorQuery(billConn);
        MySQLPreparedQuery meterQ = BillMeter.getMeterQuery(billConn);

        MySQLPreparedQuery consQ = new MySQLPreparedQuery("SELECT reading - last_reading FROM bill_reading AS r WHERE " + reca.id + " >= r.span_id and r.client_tank_id = ?1 ORDER BY r.span_id DESC", billConn);
        MySQLPreparedQuery currReadQ = new MySQLPreparedQuery("SELECT r.reading      FROM bill_reading AS r where r.span_id = ?1 AND r.client_tank_id = ?2", billConn);
        MySQLPreparedQuery lastReadQ = new MySQLPreparedQuery("SELECT r.last_reading FROM bill_reading AS r where r.span_id = ?1 AND r.client_tank_id = ?2", billConn);
        MySQLPreparedQuery faultDescQ = getFaultDescByBillQuery(billConn);

        MySQLPreparedQuery cartBySpanQ = BillClientTankController.getCartBySpanQuery(Accounts.C_CAR_GLP, billConn);

        MySQLPreparedQuery currCredQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.account_cred_id = ?1 AND t.cli_tank_id = ?2 AND t.bill_span_id = " + reca.id, billConn);
        MySQLPreparedQuery currDebQ = new MySQLPreparedQuery(" SELECT SUM(t.value) FROM bill_transaction t WHERE t.account_deb_id = ?1  AND t.cli_tank_id = ?2 AND t.bill_span_id = " + reca.id, billConn);

        MySQLPreparedQuery prevCredQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.account_cred_id = ?1 AND t.cli_tank_id = ?2 AND t.bill_span_id < " + reca.id, billConn);
        MySQLPreparedQuery prevDebQ = new MySQLPreparedQuery(" SELECT SUM(t.value) FROM bill_transaction t WHERE t.account_deb_id = ?1  AND t.cli_tank_id = ?2 AND t.bill_span_id < " + reca.id, billConn);

        MySQLPreparedQuery totalCredQ = new MySQLPreparedQuery("SELECT sum(t.value) from bill_transaction t where t.account_cred_id = ?1 AND t.cli_tank_id = ?2", billConn);
        MySQLPreparedQuery totalDebQ = new MySQLPreparedQuery(" SELECT sum(t.value) from bill_transaction t where t.account_deb_id = ?1  AND t.cli_tank_id = ?2", billConn);

        MySQLPreparedQuery currDebDetQ = new MySQLPreparedQuery("SELECT t.value, t.extra FROM bill_transaction t WHERE t.account_deb_id = ?1 AND t.cli_tank_id = ?2 AND t.bill_span_id = " + reca.id, billConn);

        //ultima causación de intereses
        MySQLPreparedQuery inteCauGlpQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.cli_tank_id = ?1 AND t.trans_type_id = " + Transactions.CAUSA_INTE_GLP + " AND t.bill_span_id = " + (reca.id - 1), billConn);
        MySQLPreparedQuery inteCauSrvQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.cli_tank_id = ?1 AND t.trans_type_id = " + Transactions.CAUSA_INTE_SRV + " AND t.bill_span_id = " + (reca.id - 1), billConn);

        //para los creditos a favor del cliente en el periodo.
        MySQLPreparedQuery transQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.trans_type_id  = ?1 AND t.cli_tank_id = ?2 AND t.bill_span_id = " + reca.id, billConn);
        MySQLPreparedQuery ajustQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.trans_type_id  = " + Transactions.PAGO_BANCO + " AND t.account_cred_id = " + Accounts.E_AJUST + " AND t.bill_span_id = " + reca.id + " AND t.cli_tank_id = ?2", billConn);
        MySQLPreparedQuery anticLabelQ = new MySQLPreparedQuery("SELECT n.label, t.value FROM "
                + "bill_antic_note n "
                + "INNER JOIN bill_transaction t ON t.doc_id = n.id AND t.doc_type = 'pag_antic' "
                + "WHERE n.client_tank_id = ?1 AND n.bill_span_id = " + reca.id, billConn);
        MySQLPreparedQuery lastPayLabelQ = new MySQLPreparedQuery("SELECT DATE_FORMAT(b.payment_date, \"%d/%m/%Y\"), (SELECT name FROM bill_bank WHERE id = b.bank_id), b.id FROM bill_bill b WHERE b.bank_id IS NOT NULL AND b.client_tank_id = ?1 ORDER BY id DESC LIMIT 1", billConn);
        MySQLPreparedQuery lastPayValueQ = new MySQLPreparedQuery("SELECT SUM(value) FROM bill_plan WHERE doc_id = ?1", billConn);

        MySQLPreparedQuery srvLabelsQ = DtoSrvToPrint.getQuery(billConn);

        HashMap<Integer, String> neighData = new HashMap<>();
        HashMap<Integer, Object[]> buildData = new HashMap<>();
        HashMap<Integer, BigDecimal> factors = new HashMap<>();
        Map<Integer, String> priceListNames = BillPriceSpan.getPriceListNames(billConn);
        if (req != null) {
            req.setTotalClicks(clients.length * 3);
        }

        String consInterLbl = twoDecPlacesF.format(reca.interes.multiply(new BigDecimal(100))) + "%";
        String srvInterLbl = twoDecPlacesF.format(reca.interesSrv.multiply(new BigDecimal(100))) + "%";

        for (int i = 0; i < clients.length; i++) {
            if (req != null) {
                req.tick();
            }
            //solo los debitos de cada cuenta en el periodo actual
            HashMap<Integer, BigDecimal> currDebits = new HashMap<>();
            //los balances de las cuentas hasta el periodo anterior
            HashMap<Integer, BigDecimal> lastBals = new HashMap<>();
            //las deudas de cada cta
            HashMap<Integer, BigDecimal> currBals = new HashMap<>();

            BillClientTank client = clients[i];
            int clientId = client.id;

            currDebQ.setParameter(2, clientId);
            currCredQ.setParameter(2, clientId);
            prevCredQ.setParameter(2, clientId);
            prevDebQ.setParameter(2, clientId);
            transQ.setParameter(2, clientId);
            totalDebQ.setParameter(2, clientId);
            totalCredQ.setParameter(2, clientId);
            ajustQ.setParameter(2, clientId);
            currDebDetQ.setParameter(2, clientId);
//                causeNoteQ.setParam(1, clientId);
            inteCauGlpQ.setParameter(1, clientId);
            inteCauSrvQ.setParameter(1, clientId);
            anticLabelQ.setParameter(1, clientId);
            lastPayLabelQ.setParameter(1, clientId);

            //colocando el número de factura
            BillBill bill = bills[i];
            bill.billNum = BillBill.getPaymentReference(inst.id, bill);
            BillForPrint billFP = new BillForPrint();
            billsFp[i] = billFP;
            BillMeter meter = BillMeter.getMeter(reca.id, clientId, meterQ);
            setData(billFP, client, meter, bill, inst, billConn, true);
            Object[] row = lastPayLabelQ.getRecord();
            billFP.lastPayDate = (row != null && row.length > 0 ? MySQLQuery.getAsString(row[0]) : "");
            billFP.lastPayBank = (row != null && row.length > 0 ? MySQLQuery.getAsString(row[1]) : "");
            if (row != null && row.length > 0) {
                lastPayValueQ.setParameter(1, MySQLQuery.getAsInteger(row[2]));
                Object[] value = lastPayValueQ.getRecord();
                billFP.lastPayValue = (value != null && value.length > 0 ? MySQLQuery.getAsBigDecimal(value[0], true) : BigDecimal.ZERO);
            } else {
                billFP.lastPayValue = (BigDecimal.ZERO);
            }

            //para pagos desde el banco
            BigDecimal fromBank = BigDecimal.ZERO;
            //creditos a favor del cliente, unos de los metodos de calculo que luego se usa para verificación
            BigDecimal credits = BigDecimal.ZERO;

            //deudas del periodo actual
            for (int j = 0; j < currAccs.length; j++) {
                int accId = currAccs[j];
                currDebQ.setParameter(1, accId);
                BigDecimal curDeb = currDebQ.getAsBigDecimal(true);
                currDebits.put(accId, curDeb);

                currCredQ.setParameter(1, accId);
                BigDecimal curCred = currCredQ.getAsBigDecimal(true);
                credits = credits.add(curCred);

                lastBals.put(accId, BigDecimal.ZERO);
                BigDecimal accBal = curDeb.subtract(curCred);
                currBals.put(accId, accBal);
                if (accBal.compareTo(BigDecimal.ZERO) > 0) {
                    fromBank = payDebtFromBank(fromBank, bill.id, insertPlanPs, accBal, accId, Transactions.PAGO_BANCO, reca.id, clientId, userId);
                }
            }

            for (int j = 0; j < cartAccs.length; j++) {
                int accId = cartAccs[j];
                currDebQ.setParameter(1, accId);
                BigDecimal curDeb = currDebQ.getAsBigDecimal(true);
                currDebits.put(accId, curDeb);

                currCredQ.setParameter(1, accId);
                BigDecimal curCred = currCredQ.getAsBigDecimal(true);
                credits = credits.add(curCred);

                prevCredQ.setParameter(1, accId);
                prevDebQ.setParameter(1, accId);
                BigDecimal lastBal = prevDebQ.getAsBigDecimal(true).subtract(prevCredQ.getAsBigDecimal(true));
                lastBals.put(accId, lastBal);
                BigDecimal accBal = lastBal.add(curDeb).subtract(curCred);
                currBals.put(accId, accBal);
                if (accBal.compareTo(BigDecimal.ZERO) != 0) {
                    fromBank = payDebtFromBank(fromBank, bill.id, insertPlanPs, accBal, accId, Transactions.PAGO_BANCO, reca.id, clientId, userId);
                }
            }

            //contar los meses en deuda
            int debMonts = BillClientTankController.getDebtMonths(clientId, cartBySpanQ, billConn, currBals.get(Accounts.C_CAR_GLP));
            //si tiene cartera alicanto meses en deuda suma +1;
            if (currBals.get(Accounts.C_CAR_OLD).add(currBals.get(Accounts.C_INT_OLD)).compareTo(BigDecimal.ZERO) != 0) {
                debMonts++;
            }
            bill.months = (debMonts);

            //ajustes a la unidad cobrados en el mes
            BigDecimal paidAdjust = ajustQ.getAsBigDecimal(true);

            //ajuste a la unidad de esta factura
            BigDecimal spanMult = new BigDecimal(reca.adjust);
            BigDecimal adjust = getAdjusment(fromBank, spanMult);
            if (adjust.compareTo(BigDecimal.ZERO) != 0) {
                fromBank = payDebtFromBank(fromBank, bill.id, insertPlanPs, adjust, Accounts.E_AJUST, Transactions.PAGO_BANCO, reca.id, clientId, userId);
            }

            //////////////////PRINTING/////////////////////
            //dirección
            billFP.priceList = (priceListNames.get(BillPriceSpan.getListId(billConn, reca.id, clientId)));
            if (inst.isTankInstance()) {
                Object[] buildRow;
                if (buildData.containsKey(client.buildingId)) {
                    buildRow = buildData.get(client.buildingId);
                } else {
                    addressQ.setParameter(1, client.buildingId);
                    buildRow = (Object[]) addressQ.getRecords()[0];
                    buildData.put(client.buildingId, buildRow);
                }
                billFP.address = buildRow[0].toString();
                billFP.buildingName = buildRow[1].toString();
                billFP.buildingType = MySQLQuery.getAsString(buildRow[2]);
                billFP.billBuildingId = MySQLQuery.getAsInteger(buildRow[3]);
                billFP.clieApto = (client.apartment);
            } else {
                if (client.neighId != null) {
                    if (neighData.containsKey(client.neighId)) {
                        billFP.buildingName = neighData.get(client.neighId);
                    } else {
                        neighQ.setParameter(1, client.neighId);
                        String neigh = neighQ.getAsString();
                        neighData.put(client.buildingId, neigh);
                        billFP.buildingName = neigh;
                    }
                } else {
                    billFP.buildingName = "";
                }
                billFP.clieApto = "";
                billFP.address = (client.address != null ? client.address : "");
                billFP.buildingType = "";
                billFP.billBuildingId = null;
                billFP.sectorType = client.sectorType;
            }

            billFP.months = debMonts;
            //fin dirección           
            //precio por lista
            Integer listId = BillPriceSpan.getListId(billConn, reca.id, client.id);
            if (listId != null) {
                billFP.gplPrice = (prices.get(listId));
            } else {
                billFP.gplPrice = (BigDecimal.ZERO);
            }

            //lecturas
            if (billFP.rebill != null) {
                billFP.lastRead = billFP.rebill.origBegRead;
                billFP.currRead = billFP.rebill.origEndRead;
            } else {
                lastReadQ.setParameter(1, reca.id);
                lastReadQ.setParameter(2, client.id);
                currReadQ.setParameter(1, reca.id);
                currReadQ.setParameter(2, client.id);
                billFP.lastRead = lastReadQ.getAsBigDecimal(true);
                billFP.currRead = currReadQ.getAsBigDecimal(true);
            }

            faultDescQ.setParameter(1, reca.id);
            faultDescQ.setParameter(2, client.id);
            billFP.readingFaultDescription = faultDescQ.getAsString();

            if (inst.isTankInstance()) {
                //factor
                if (factors.containsKey(client.buildingId)) {
                    billFP.factor = factors.get(client.buildingId);
                } else {
                    BigDecimal factor = BillBuildFactor.getFactor(reca.id, client.buildingId, buildFactorQ);
                    BigDecimal clientFac = BillClientFactor.getFactor(reca.id, client.id, clientFactorQ);
                    billFP.factor = (clientFac == BigDecimal.ZERO ? factor : clientFac);
                    factors.put(client.buildingId, factor);
                }
                if (meter != null) {
                    billFP.factor = billFP.factor.multiply(meter.factor);
                }
            } else {
                if (meter != null && (meter.factor != null && meter.factor.compareTo(BigDecimal.ONE) != 0)) {
                    billFP.factor = meter.factor;
                } else {
                    billFP.factor = reca.fadj;
                }
            }

            //ultimos consumos
            ArrayList<Double> consAnt = new ArrayList<>();
            consQ.setParameter(1, client.id);
            Object[][] consums = consQ.getRecords();
            for (int j = 0; j < Math.min(consums.length, 6); j++) {
                consAnt.add(MySQLQuery.getAsBigDecimal(consums[j][0], true).multiply(billFP.factor).doubleValue());
            }
            billFP.consumos = consAnt;
            if (billFP.rebill != null && !billFP.consumos.isEmpty()) {
                billFP.consumos.remove(0);
                billFP.consumos.add(0, billFP.currRead.subtract(billFP.lastRead).multiply(billFP.factor).doubleValue());
            }

            //total a pagar
            billFP.total = fromBank;
            //items
            List<BillBillPres> lines = new ArrayList<>();
            //suma de comprobación de los valores facturados.

            BigDecimal sum = BigDecimal.ZERO;
            //lines.add(new BillBillPres( bill.id, "Cargos del mes", null, lines.size(), true));

            BigDecimal val;
            
            if (inst.isNetInstance()) {
                val = currDebits.get(Accounts.C_CONS).add(currDebits.get(Accounts.C_CONS_SUBS));
                //Val2=currDebits.get(Accounts.C_CONSTP).add(currDebits.get(Accounts.C_CONS_SUBSTP));
                //System.out.println("DEBEN SER LO QUE NECESITO "+val);
                if (val.compareTo(BigDecimal.ZERO) != 0) {
                    lines.add(new BillBillPres(bill.id, "Consumo", val, lines.size(), "glp"));
                    sum = sum.add(val);
                }
                /*if (val2.compareTo(BigDecimal.ZERO) != 0) {
                    lines.add(new BillBillPres(bill.id, "ConsumoTP", val2, lines.size(), "glp"));
                    sum = sum.add(val);
                }*/
                
            } else {
                val = currDebits.get(Accounts.C_CONS);
                if (val.compareTo(BigDecimal.ZERO) != 0) {
                    lines.add(new BillBillPres(bill.id, "Consumo", val, lines.size(), false));
                    sum = sum.add(val);
                }
            }
            val = currDebits.get(Accounts.C_BASI);
            if (val.compareTo(BigDecimal.ZERO) != 0) {
                lines.add(new BillBillPres(bill.id, "Cargo Fijo", val, lines.size(), "glp"));
                sum = sum.add(val);
            }

            val = currDebits.get(Accounts.C_CONTRIB);
            if (val.compareTo(BigDecimal.ZERO) != 0) {
                lines.add(new BillBillPres(bill.id, "Contribución", val, lines.size(), "glp"));
                sum = sum.add(val);
            }

            val = currDebits.get(Accounts.C_REBILL);
            if (val.compareTo(BigDecimal.ZERO) != 0) {
                lines.add(new BillBillPres(bill.id, "Ajuste Refacturación Meses Ant.", val, lines.size(), false));
                sum = sum.add(val);
            }

            val = currDebits.get(Accounts.C_CUOTA_SER_EDI);
            if (val.compareTo(BigDecimal.ZERO) != 0) {
                currDebDetQ.setParameter(1, Accounts.C_CUOTA_SER_EDI);
                Object[][] detData = currDebDetQ.getRecords();
                for (Object[] detRow : detData) {
                    lines.add(new BillBillPres(bill.id, detRow[1].toString(), MySQLQuery.getAsBigDecimal(detRow[0], true), lines.size(), false));
                }
                sum = sum.add(val);
            }

            if (inst.isNetInstance()) {
                val = currDebits.get(Accounts.C_CUOTA_SER_CLI_GLP).add(currDebits.get(Accounts.C_CUOTA_SER_CLI_SRV)).add(currDebits.get(Accounts.C_CUOTA_INT_CRE));
                if (val.compareTo(BigDecimal.ZERO) != 0) {
                    lines.add(new BillBillPres(bill.id, "Otros servicios y Financiaciones en el Mes", val, lines.size(), "det"));
                    sum = sum.add(val);
                }
            } else {
                val = currDebits.get(Accounts.C_CUOTA_SER_CLI_GLP);
                if (val.compareTo(BigDecimal.ZERO) != 0) {
                    currDebDetQ.setParameter(1, Accounts.C_CUOTA_SER_CLI_GLP);
                    Object[][] detData = currDebDetQ.getRecords();
                    for (Object[] detRow : detData) {
                        lines.add(new BillBillPres(bill.id, detRow[1] != null ? detRow[1].toString() : "Servicios Usuario", MySQLQuery.getAsBigDecimal(detRow[0], true), lines.size(), false));
                    }
                    sum = sum.add(val);
                }
                val = currDebits.get(Accounts.C_CUOTA_SER_CLI_SRV);
                if (val.compareTo(BigDecimal.ZERO) != 0) {
                    currDebDetQ.setParameter(1, Accounts.C_CUOTA_SER_CLI_SRV);
                    Object[][] detData = currDebDetQ.getRecords();
                    for (Object[] detRow : detData) {
                        lines.add(new BillBillPres(bill.id, detRow[1] != null ? detRow[1].toString() : "Servicios Usuario", MySQLQuery.getAsBigDecimal(detRow[0], true), lines.size(), false));
                    }
                    sum = sum.add(val);
                }
            }

            val = currDebits.get(Accounts.C_CUOTA_FINAN_DEU);
            if (val.compareTo(BigDecimal.ZERO) != 0) {
                currDebDetQ.setParameter(1, Accounts.C_CUOTA_FINAN_DEU);
                Object[][] detData = currDebDetQ.getRecords();
                for (Object[] detRow : detData) {
                    lines.add(new BillBillPres(bill.id, detRow[1] != null ? detRow[1].toString() : "Cuota de Financiación", MySQLQuery.getAsBigDecimal(detRow[0], true), lines.size(), "no_glp"));
                }
                sum = sum.add(val);
            }

            if (inst.isTankInstance()) {
                val = currDebits.get(Accounts.C_CUOTA_INT_CRE);
                if (val.compareTo(BigDecimal.ZERO) != 0) {
                    lines.add(new BillBillPres(bill.id, "Intereses de Financiación", val, lines.size(), "no_glp"));
                    sum = sum.add(val);
                }
            }

            val = currDebits.get(Accounts.C_RECON);
            if (val.compareTo(BigDecimal.ZERO) != 0) {
                lines.add(new BillBillPres(bill.id, "Reconexiones", val, lines.size(), "no_glp"));
                sum = sum.add(val);
            }

            billFP.srvs = DtoSrvToPrint.getData(bill.clientTankId, bill.billSpanId, srvLabelsQ);

            //
            //causación de intereses del periodo anterior
            BigDecimal inteCauGlp = inteCauGlpQ.getAsBigDecimal(true);
            BigDecimal inteCauSrv = inteCauSrvQ.getAsBigDecimal(true);

            BigDecimal saldAnte = currDebits.get(Accounts.C_CAR_GLP).add(currDebits.get(Accounts.C_CAR_INTE_CRE)).add(currDebits.get(Accounts.C_CAR_OLD)).add(currDebits.get(Accounts.C_CAR_SRV)).add(currDebits.get(Accounts.C_CAR_FINAN_DEU)).add(currDebits.get(Accounts.C_CAR_CONTRIB));
            saldAnte = saldAnte.add(currDebits.get(Accounts.C_INT_GLP)).add(currDebits.get(Accounts.C_INT_OLD)).add(currDebits.get(Accounts.C_INT_SRV)).add(currDebits.get(Accounts.C_INT_FINAN_DEU)).add(currDebits.get(Accounts.C_INT_CONTRIB));
            saldAnte = saldAnte.add(lastBals.get(Accounts.C_CAR_GLP)).add(lastBals.get(Accounts.C_CAR_INTE_CRE)).add(lastBals.get(Accounts.C_CAR_OLD)).add(lastBals.get(Accounts.C_CAR_SRV)).add(lastBals.get(Accounts.C_CAR_FINAN_DEU)).add(lastBals.get(Accounts.C_CAR_CONTRIB));
            saldAnte = saldAnte.add(lastBals.get(Accounts.C_INT_GLP)).add(lastBals.get(Accounts.C_INT_OLD)).add(lastBals.get(Accounts.C_INT_SRV)).add(lastBals.get(Accounts.C_INT_FINAN_DEU)).add(lastBals.get(Accounts.C_INT_CONTRIB));
            saldAnte = saldAnte.subtract(inteCauGlp).subtract(inteCauSrv);

            if (saldAnte.compareTo(BigDecimal.ZERO) != 0) {
                lines.add(new BillBillPres(bill.id, "Saldo Anterior", saldAnte, lines.size(), false));
                sum = sum.add(saldAnte);
            }

            if (reca.interes.equals(reca.interesSrv)) {
                BigDecimal totalInte = inteCauGlp.add(inteCauSrv);
                if (totalInte.compareTo(BigDecimal.ZERO) != 0) {
                    lines.add(new BillBillPres(bill.id, "Intereses de Mora Periodo " + consInterLbl, totalInte, lines.size(), "no_glp"));
                    sum = sum.add(totalInte);
                }
            } else {
                if (inteCauGlp.compareTo(BigDecimal.ZERO) != 0) {
                    lines.add(new BillBillPres(bill.id, "Int. de Mora por Consumo Periodo " + consInterLbl, inteCauGlp, lines.size(), "no_glp"));
                    sum = sum.add(inteCauGlp);
                }

                if (inteCauSrv.compareTo(BigDecimal.ZERO) != 0) {
                    lines.add(new BillBillPres(bill.id, "Int. de Mora por Otros Srvs. Periodo " + srvInterLbl, inteCauSrv, lines.size(), "no_glp"));
                    sum = sum.add(inteCauSrv);
                }
            }

            if (paidAdjust.compareTo(BigDecimal.ZERO) != 0) {
                lines.add(new BillBillPres(bill.id, "Ajustes Cobrados Periodo", paidAdjust, lines.size(), "no_glp"));
                sum = sum.add(paidAdjust);
            }

            if (inst.isTankInstance()) {
                lines.add(new BillBillPres(bill.id, "Subtotal", sum, lines.size(), true));
            }

            transQ.setParameter(1, Transactions.CAUSA_SUBSIDY);
            BigDecimal subsidy = transQ.getAsBigDecimal(true).negate();

            transQ.setParameter(1, Transactions.PAGO_BANCO);
            BigDecimal paidInBank = transQ.getAsBigDecimal(true).negate();

            transQ.setParameter(1, Transactions.N_CREDIT);
            BigDecimal paidWithNotes = transQ.getAsBigDecimal(true);

            transQ.setParameter(1, Transactions.N_AJ_CREDIT);
            paidWithNotes = paidWithNotes.add(transQ.getAsBigDecimal(true)).negate();

            transQ.setParameter(1, Transactions.PAGO_ANTICIP);
            BigDecimal paidAntic = transQ.getAsBigDecimal(true).negate();

            transQ.setParameter(1, Transactions.DTO_EDIF);
            BigDecimal dtoEdit = transQ.getAsBigDecimal(true).negate();

            transQ.setParameter(1, Transactions.N_FINAN);
            BigDecimal nFinan = transQ.getAsBigDecimal(true).negate();

            if (subsidy.add(dtoEdit.add(paidInBank).add(paidWithNotes).add(paidAntic).add(nFinan).add(credits.add(paidAdjust))).compareTo(BigDecimal.ZERO) != 0) {
                throw new Exception("Error inesperado al generar la factura para el cliente " + client.numInstall + ".\nLa sección informativa de creditos no es consistente con las etiquetas.\nCreditos Bal " + credits + " Creditos Info " + paidInBank.add(paidWithNotes).add(paidAntic));
            }

            if (subsidy.compareTo(BigDecimal.ZERO) < 0) {
                lines.add(new BillBillPres(bill.id, "Subsidio", subsidy, lines.size(), "glp"));
                sum = sum.add(subsidy);
            }

            if (paidInBank.compareTo(BigDecimal.ZERO) < 0) {
                lines.add(new BillBillPres(bill.id, "Pagado en Bancos", paidInBank, lines.size(), false));
                sum = sum.add(paidInBank);
            }

            if (paidWithNotes.compareTo(BigDecimal.ZERO) < 0) {
                lines.add(new BillBillPres(bill.id, "Saldo a Favor", paidWithNotes, lines.size(), false));
                sum = sum.add(paidWithNotes);
            }

            if (nFinan.compareTo(BigDecimal.ZERO) < 0) {
                lines.add(new BillBillPres(bill.id, "Acuerdos de Pago", nFinan, lines.size(), false));
                sum = sum.add(nFinan);
            }

            if (dtoEdit.compareTo(BigDecimal.ZERO) < 0) {
                lines.add(new BillBillPres(bill.id, "Descuento en Precio", dtoEdit, lines.size(), false));
                sum = sum.add(dtoEdit);
            }

            //información devoluciones
            transQ.setParameter(1, Transactions.N_ANTICIP);
            BigDecimal assignedAnti = transQ.getAsBigDecimal(true);

            totalDebQ.setParameter(1, Accounts.C_ANTICIP);
            totalCredQ.setParameter(1, Accounts.C_ANTICIP);
            BigDecimal anticipBal = totalDebQ.getAsBigDecimal(true).subtract(totalCredQ.getAsBigDecimal(true)).multiply(new BigDecimal(-1));
            if (paidAntic.negate().compareTo(assignedAnti) == 0 && anticipBal.compareTo(BigDecimal.ZERO) == 0) {
                //los saldos a favor se consumieron en el mismo mes, se muestra el detalle
                if (assignedAnti.compareTo(BigDecimal.ZERO) > 0) {
                    Object[][] anticNotes = anticLabelQ.getRecords();
                    BigDecimal total = BigDecimal.ZERO;
                    for (Object[] anticNote : anticNotes) {
                        String noteLabel = MySQLQuery.getAsString(anticNote[0]);
                        BigDecimal noteVal = MySQLQuery.getAsBigDecimal(anticNote[1], true);
                        total = total.add(noteVal);
                        lines.add(new BillBillPres(bill.id, noteLabel, noteVal.negate(), lines.size(), false));
                    }
                    if (total.compareTo(assignedAnti) != 0) {
                        throw new Exception("Los detalles de los saldos a favor no coinciden con el total");
                    }
                    sum = sum.add(paidAntic);
                }
            } else {
                //los saldos a favor se seguirán consumiendo en los meses que vienen, se muestra consolidado lo que se usó este mes
                if (paidAntic.compareTo(BigDecimal.ZERO) < 0) {
                    lines.add(new BillBillPres(bill.id, "Saldo a Favor", paidAntic, lines.size(), false));
                    sum = sum.add(paidAntic);
                }
            }

            if (adjust.compareTo(BigDecimal.ZERO) != 0) {
                lines.add(new BillBillPres(bill.id, "Ajuste a la decena", adjust, lines.size(), "no_glp"));
                sum = sum.add(adjust);
            }

            lines.add(new BillBillPres(bill.id, "TOTAL A PAGAR", fromBank, lines.size(), true));

            //si quedaron saldos a favor para seguir consumiendo en las próximas facturas
            //se muestran los detalles de esos saldos, de lo contrario ya se habrán mostrado arriba
            if (paidAntic.negate().compareTo(assignedAnti) != 0 || anticipBal.compareTo(BigDecimal.ZERO) != 0) {
                Object[][] anticNotes = anticLabelQ.getRecords();
                if (anticNotes.length > 0) {
                    lines.add(new BillBillPres(bill.id, "Saldos a Favor Registrados", null, lines.size(), true));
                    if (assignedAnti.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal total = BigDecimal.ZERO;
                        for (Object[] anticNote : anticNotes) {
                            String noteLabel = MySQLQuery.getAsString(anticNote[0]);
                            BigDecimal noteVal = MySQLQuery.getAsBigDecimal(anticNote[1], true);
                            total = total.add(noteVal);
                            lines.add(new BillBillPres(bill.id, noteLabel, noteVal, lines.size(), false));
                        }
                        if (total.compareTo(assignedAnti) != 0) {
                            throw new Exception("Los detalles de los saldos a favor no coinciden con el total");
                        }
                    }
                }
            }

            /*if (anticipBal.compareTo(BigDecimal.ZERO) > 0) {
                lines.add(new BillBillPres( bill.id, "Saldo a Favor Disponible", anticipBal, lines.size(), false));
            }*/
            //System.out.println("Estamos aquí 1");
            setLines(billFP, lines, inst.isNetInstance());
            
            for (int j = 0; j < lines.size(); j++) {
                BillBillPres.insert(lines.get(j), insertPresPs);
            }

            BillBill.update(bill, updateBillPs);

            if (sum.compareTo(fromBank) != 0) {
                throw new Exception("Error inesperado al generar la factura para el cliente " + client.numInstall + ".\nLas sumas de valores a imprimir no coinciden. " + sum.toString() + " - " + fromBank.toString());
            }
            DtoRangeToPrint.setRanges(billFP, reca, inst);

            if (clientTankId != null) {
                writer.addBill(billFP);
            } else {
                if (!sysCfg.skipZeroBills || (sysCfg.skipZeroBills && fromBank.compareTo(BigDecimal.ZERO) > 0)) {
                    writer.addBill(billFP);
                }
            }
        }

        insertPlanPs.executeBatch();
        insertPresPs.executeBatch();
        updateBillPs.executeBatch();

        insertPlanPs.printStats("insertPlanPs");
        insertPresPs.printStats("insertPresPs");
        updateBillPs.printStats("updateBillPs");

        addressQ.printStats("addressQ");
        buildFactorQ.printStats("buildFactorQ");
        clientFactorQ.printStats("clientFactorQ");
        consQ.printStats("galsQ");
        currReadQ.printStats("currReadQ");
        lastReadQ.printStats("lastReadQ");
        cartBySpanQ.printStats("cartBySpanQ");
        currCredQ.printStats("currCredQ");
        currDebQ.printStats("currDebQ");
        prevCredQ.printStats("prevCredQ");
        prevDebQ.printStats("prevDebQ");
        totalCredQ.printStats("totalCredQ");
        totalDebQ.printStats("totalDebQ");
        currDebDetQ.printStats("currDebDetQ");
        inteCauGlpQ.printStats("inteCauGlpQ");
        inteCauSrvQ.printStats("inteCauSrvQ");
        transQ.printStats("transQ");
        ajustQ.printStats("ajustQ");
        anticLabelQ.printStats("anticLabelQ");

        billConn.commit();

        ///COMPROBANDO LOS PLANES CONTRA LAS DEUDAS///
        MySQLPreparedQuery creditPlanQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_plan t WHERE t.account_cred_id = ?1 AND t.cli_tank_id = ?2 AND t.doc_id = ?3", billConn);
        if (req != null) {
            req.setStatus("testing", "Iniciando Comprobaciones...");
        }

        for (int j = 0; j < bills.length; j++) {
            if (req != null) {
                req.tick();
            }
            BillBill bill = bills[j];

            for (int i = 0; i < currAccs.length; i++) {
                int acc = currAccs[i];
                BigDecimal debt = BillSpanController.getTotalBalance(currDebQ, currCredQ, acc, bill.clientTankId);
                creditPlanQ.setParameter(1, acc);
                creditPlanQ.setParameter(2, bill.clientTankId);
                creditPlanQ.setParameter(3, bill.id);
                BigDecimal plan = creditPlanQ.getAsBigDecimal(true);
                if (plan.compareTo(BigDecimal.ZERO) < 0) {
                    throw new Exception("Error inesperado al generar la factura para el cliente " + clients[j].numInstall + ".\n" + acc + " " + plan.toString() + " No se permiten créditos negativos");
                }
                if (debt.compareTo(plan) != 0) {
                    throw new Exception("Error inesperado al generar la factura para el cliente " + clients[j].numInstall + ".\n" + acc + " " + debt.toString() + " " + plan.toString());
                }
            }

            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < cartAccs.length; i++) {
                int acc = cartAccs[i];
                BigDecimal debt = BillSpanController.getTotalBalance(totalDebQ, totalCredQ, acc, bill.clientTankId);
                creditPlanQ.setParameter(1, acc);
                creditPlanQ.setParameter(2, bill.clientTankId);
                creditPlanQ.setParameter(3, bill.id);
                BigDecimal plan = creditPlanQ.getAsBigDecimal(true);
                total = total.add(plan);

                if (acc == Accounts.C_CAR_GLP) {
                    if (plan.compareTo(new BigDecimal(reca.adjust).multiply(new BigDecimal(12)).negate()) < 0) {
                        throw new Exception("Error inesperado al generar la factura para el cliente " + clients[j].numInstall + ".\n" + acc + " " + plan.toString() + " No se permiten créditos negativos");
                    }
                } else {
                    if (plan.compareTo(BigDecimal.ZERO) < 0) {
                        throw new Exception("Error inesperado al generar la factura para el cliente " + clients[j].numInstall + ".\n" + acc + " " + plan.toString() + " No se permiten créditos negativos");
                    }
                }

                if (debt.compareTo(plan) != 0) {
                    throw new Exception("Error inesperado al generar la factura para el cliente " + clients[j].numInstall + ".\n" + acc + " " + debt.toString() + " " + plan.toString());
                }
            }
            if (total.compareTo(BigDecimal.ZERO) < 0) {
                throw new Exception("Error inesperado al generar la factura para el cliente " + clients[j].numInstall + ".\nEl total es negativo");
            }
        }
        billConn.commit();

        ClaroSmsSender sender = new ClaroSmsSender();
        String month = new SimpleDateFormat("MMMM").format(reca.consMonth);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        for (int j = 0; j < bills.length; j++) {
            BillClientTank c = clients[j];
            BillForPrint bp = billsFp[j];
            BillBill bill = bills[j];
            String paydate = bill.months >= inst.suspDebtMonths - 1 ? "Pago inmediato" : "Pague oportunamente hasta " + df.format(reca.limitDate);
            String msg = "Se ha emitido su factura Montagas S.A E.S.P de " + month + " por " + twoDecPlacesF.format(bp.total) + ". " + paydate + " con Ref. " + c.code;

            if (inst.sendSms && c.smsBill) {
                List<String> phones = c.getPhonesForSMS();
                for (int k = 0; k < phones.size(); k++) {
                    sender.sendMsg(msg, "1", phones.get(k));
                }
            }

            if (inst.sendMail && c.mailBill) {
                SendMail.sendBillMail(sysCfg, c.mail, "Se ha emitido su factura Montagas S.A E.S.P", msg, msg, null, null, null, null);
            }
        }

    }

    public static BillForPrint getById(int billId, BillInstance inst, Connection conn) throws Exception {
        return getBillInfo(billId, inst, conn);
    }
   
    public static BillForPrint getBillInfo(int billId, BillInstance inst, Connection conn) throws Exception {
        BillBill bill = new BillBill().select(billId, conn);

        BillSpan span = new BillSpan().select(bill.billSpanId, conn);
        if (bill == null) {
            throw new Exception("No se encontró el cupón " + billId);
        }
        BillClientTank client = new BillClientTank().select(bill.clientTankId, conn);
        MySQLPreparedQuery neighQ = new MySQLPreparedQuery("SELECT name FROM sigma.neigh WHERE id = ?1", conn);
        MySQLPreparedQuery addressQ = new MySQLPreparedQuery("SELECT "
                + "b.address, b.name, IFNULL(t.short_name,'Apto'), b.id "
                + "FROM bill_building AS b "
                + "LEFT JOIN bill_build_type AS t ON t.id = b.build_type_id "
                + "WHERE b.id = ?1", conn);
        //cuanto acreditará la factura a una cuenta en particular
        MySQLPreparedQuery credPlanQ = new MySQLPreparedQuery("SELECT SUM(p.value + p.credit) FROM bill_plan p WHERE p.account_cred_id = ?1 AND p.cli_tank_id = " + client.id + " AND p.doc_id = " + billId + " AND p.doc_type = 'fac'", conn);
        //
//        MySQLPreparedQuery credPlanQPrev = new MySQLPreparedQuery("SELECT SUM(p.value + p.credit) FROM bill_plan p WHERE p.account_cred_id = ?1 AND p.cli_tank_id = " + client.id + " AND p.doc_id = " + billId + " AND p.doc_type = 'fac' AND p.prev = 1", conn);
//        MySQLPreparedQuery credPlanQCurr = new MySQLPreparedQuery("SELECT SUM(p.value + p.credit) FROM bill_plan p WHERE p.account_cred_id = ?1 AND p.cli_tank_id = " + client.id + " AND p.doc_id = " + billId + " AND p.doc_type = 'fac' AND p.prev = 0", conn);

        //cuanto se pagará en bancos para una cuenta en particular
        MySQLPreparedQuery debBank = new MySQLPreparedQuery("SELECT SUM(p.value) FROM bill_plan p WHERE p.account_deb_id = " + Accounts.BANCOS + " AND p.cli_tank_id = " + client.id + " AND p.doc_id = " + billId + " AND p.doc_type = 'fac'", conn);
        //cuanto hay de crédito para una cuenta en particular.
//        MySQLPreparedQuery creditQ = new MySQLPreparedQuery("SELECT SUM(p.credit) FROM bill_plan p WHERE p.account_deb_id = " + Accounts.BANCOS + " AND p.cli_tank_id = " + client.id + " AND p.doc_id = " + billId + " AND p.doc_type = 'fac'", conn);

        MySQLPreparedQuery buildFactorQ = BillBuildFactor.getFactorQuery(conn);
        MySQLPreparedQuery clientFactorQ = BillClientFactor.getFactorQuery(conn);
        MySQLPreparedQuery meterQ = BillMeter.getMeterQuery(conn);

        BillMeter meter = BillMeter.getMeter(bill.billSpanId, bill.clientTankId, meterQ);
        MySQLPreparedQuery consQ = new MySQLPreparedQuery("SELECT c FROM "
                + "(SELECT r.reading - r.last_reading AS c, r.span_id FROM bill_reading AS r WHERE ?1 <= r.span_id AND  ?2 >= r.span_id  and r.client_tank_id = ?3 "
                + "UNION "
                + "SELECT r.reading - r.last_reading AS c, r.span_id FROM bill_reading_bk AS r WHERE ?1 <= r.span_id AND  ?2 >= r.span_id  and r.client_tank_id = ?3) "
                + "AS l ORDER BY span_id DESC", conn
        );
        MySQLPreparedQuery readingQ = new MySQLPreparedQuery(""
                + "SELECT r.reading, r.last_reading FROM bill_reading AS r where r.span_id = ?1 AND r.client_tank_id = ?2 "
                + "UNION "
                + "SELECT r.reading, r.last_reading FROM bill_reading_bk AS r where r.span_id = ?1 AND r.client_tank_id = ?2", conn);
        MySQLPreparedQuery lastPayLabelQ = new MySQLPreparedQuery("SELECT DATE_FORMAT(b.payment_date, \"%d/%m/%Y\"), (SELECT name FROM bill_bank WHERE id = b.bank_id), b.id FROM bill_bill b WHERE b.bank_id IS NOT NULL AND b.client_tank_id = ?1 ORDER BY id DESC LIMIT 1", conn);
        MySQLPreparedQuery lastPayValueQ = new MySQLPreparedQuery("SELECT SUM(value) FROM bill_plan WHERE doc_id = ?1", conn);
        BillForPrint billFP = new BillForPrint();

        MySQLPreparedQuery faultDescQ = BillReadingFault.getFaultDescByBillQuery(conn);
        faultDescQ.setParameter(1, span.id);
        faultDescQ.setParameter(2, client.id);
        billFP.readingFaultDescription = faultDescQ.getAsString();
        setData(billFP, client, meter, bill, inst, conn, false);
        lastPayLabelQ.setParameter(1, client.id);
        Object[] row = lastPayLabelQ.getRecord();
        billFP.lastPayDate = (row != null && row.length > 0 ? MySQLQuery.getAsString(row[0]) : "");
        billFP.lastPayBank = (row != null && row.length > 0 ? MySQLQuery.getAsString(row[1]) : "");
        if (row != null && row.length > 0) {
            lastPayValueQ.setParameter(1, MySQLQuery.getAsInteger(row[2]));
            Object[] value = lastPayValueQ.getRecord();
            billFP.lastPayValue = (value != null && value.length > 0 ? MySQLQuery.getAsBigDecimal(value[0], true) : BigDecimal.ZERO);
        } else {
            billFP.lastPayValue = (BigDecimal.ZERO);
        }

        //precio por lista
        Integer listId = BillPriceSpan.getListId(conn, bill.billSpanId, client.id);
        if (listId != null) {
            billFP.priceList = (BillPriceSpan.getPriceListName(conn, listId));
            billFP.gplPrice = (BillPriceSpan.getPrice(conn, bill.billSpanId, listId));
        } else {
            billFP.gplPrice = (BigDecimal.ZERO);
        }

        billFP.srvs = DtoSrvToPrint.getData(bill.clientTankId, bill.billSpanId, DtoSrvToPrint.getQuery(conn));
        //lecturas
        readingQ.setParameter(1, bill.billSpanId);
        readingQ.setParameter(2, client.id);

        Object[][] curReadList = readingQ.getRecords();
        if (curReadList.length > 0) {
            if (billFP.rebill != null) {
                billFP.lastRead = billFP.rebill.origBegRead;
                billFP.currRead = billFP.rebill.origEndRead;
            } else {
                Object[] curReadRow = curReadList[0];
                billFP.currRead = MySQLQuery.getAsBigDecimal(curReadRow[0], true);
                billFP.lastRead = MySQLQuery.getAsBigDecimal(curReadRow[1], true);
            }
        } else {
            billFP.currRead = BigDecimal.ZERO;
            billFP.lastRead = BigDecimal.ZERO;
        }

        //factor
        if (inst.isTankInstance()) {
            BigDecimal factor = BillBuildFactor.getFactor(span.id, client.buildingId, buildFactorQ);
            BigDecimal clientFac = BillClientFactor.getFactor(span.id, client.id, clientFactorQ);
            billFP.factor = (clientFac == BigDecimal.ZERO ? factor : clientFac);
            if (meter != null) {
                billFP.factor = billFP.factor.multiply(meter.factor);
            }
        } else {
            if (meter != null && (meter.factor != null && meter.factor.compareTo(BigDecimal.ONE) != 0)) {
                billFP.factor = meter.factor;
            } else {
                billFP.factor = span.fadj;
            }
        }
        //ultimos consumos
        billFP.consumos = new ArrayList<>();
        consQ.setParameter(1, bill.billSpanId - 5);
        consQ.setParameter(2, bill.billSpanId);
        consQ.setParameter(3, client.id);

        Object[][] consums = consQ.getRecords();
        for (Object[] consRow : consums) {
            billFP.consumos.add(MySQLQuery.getAsBigDecimal(consRow[0], true).multiply(billFP.factor).doubleValue());
        }
        if (billFP.rebill != null && !billFP.consumos.isEmpty()) {
            billFP.consumos.remove(0);
            billFP.consumos.add(0, billFP.currRead.subtract(billFP.lastRead).multiply(billFP.factor).doubleValue());
        }
        //dirección
        ///////////////////////////////////////////////////////////////////
        if (inst.isTankInstance()) {
            addressQ.setParameter(1, client.buildingId);
            Object[] buildRow = addressQ.getRecords()[0];
            billFP.address = (buildRow[0].toString());
            billFP.buildingName = (buildRow[1].toString());
            billFP.buildingType = (MySQLQuery.getAsString(buildRow[2]));
            billFP.billBuildingId = (MySQLQuery.getAsInteger(buildRow[3]));
            billFP.clieApto = (client.apartment);
        } else {
            if (client.neighId != null) {
                neighQ.setParameter(1, client.neighId);
                billFP.buildingName = (neighQ.getAsString());
            } else {
                billFP.buildingName = ("");
            }
            billFP.clieApto = "";
            billFP.address = client.address;
            billFP.buildingType = "";
            billFP.billBuildingId = null;
            billFP.sectorType = client.sectorType;
        }

        //////////////////////////////////////////////////////////////////
        //costo consumo
        credPlanQ.setParameter(1, Accounts.C_CONS);//cuenta        
        //total a pagar        
        billFP.total = (debBank.getAsBigDecimal(true));
        //items    
        List<BillBillPres> lines = new ArrayList<>();
        Collections.addAll(lines, BillBillPres.getByBillId(bill.id, conn));
        //System.out.println("Estamos aquí 2");
        setLines(billFP, lines, inst.isNetInstance());
        DtoRangeToPrint.setRanges(billFP, span, inst);
        return billFP;
    }

    private static void setData(BillForPrint p, BillClientTank client, BillMeter m, BillBill bill, BillInstance inst, Connection conn, boolean newBill) throws Exception {
        p.instId = inst.id;
        p.billNum = bill.billNum;
        p.isTotal = bill.total;
        p.creationDate = bill.creationDate;
        p.months = bill.months;
        p.meter = m != null ? m.number : "";
        
        p.billId = bill.id;
        p.spanId = bill.billSpanId;

        BillInstCheck.InstCheckInfo checkInfo = BillInstCheck.getNextDates(client.id, inst, !newBill ? bill.creationDate : null, conn);
        if (inst.isNetInstance()) {
            p.cau = BillClieCau.getByClientSpan(client.id, bill.billSpanId, conn);
            p.rebill = BillClieRebill.getByClientErrorSpan(client.id, bill.billSpanId, conn);

            Object[] measureRow = new MySQLQuery("SELECT "
                    + "odorant_amount, "
                    + "pressure "
                    + "FROM bill_measure m WHERE m.taken_dt IS NOT NULL AND m.span_id = ?1 AND m.client_id = ?2").setParam(1, bill.billSpanId).setParam(2, bill.clientTankId).getRecord(conn);
            if (measureRow != null) {
                p.io = factorFormat.format(cast.asBigDecimal(measureRow, 0, false)) + "mg/m3";
                p.ipli = factorFormat.format(cast.asBigDecimal(measureRow, 1, false)) + "mbar";
            } else {
                p.io = "";
                p.ipli = "";
            }
        }

        p.lastInstCheck = checkInfo.lastCheck;
        p.minInstCheck = checkInfo.minDate;
        p.maxInstCheck = checkInfo.maxDate;
        p.setClient(client);
        if(inst.isNetInstance()){
            boolean esTarifa=new MySQLQuery("SELECT tarifa_plena FROM sigma.bill_market WHERE id=?1").setParam(1, inst.marketId).getAsBoolean(conn);
            p.setTarifaPlena(esTarifa);
            if(esTarifa){
                BigDecimal resultado=BillClieCau.resultadoTarifaPlena(p.clieDoc,p.cau.id,conn);
                p.setResultFinal(resultado);
                
            }
        }
        else if(inst.isTankInstance()){
            Integer idQuality = null;
            String fecha = "2022-09-20";
            SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
            Date inicioCalidad = date.parse(fecha);
            System.out.println("##### cual es la fecha de creación "+bill.creationDate);
            if(bill.creationDate.after(inicioCalidad)){
                Date periodo=new MySQLQuery("SELECT r.read_date "
                    + "FROM sigma.est_tank AS t INNER JOIN sigma.est_tank_read AS r ON r.tank_id = t.id "
                    + "WHERE r.bill_span_id = "+bill.billSpanId+" ORDER BY r.read_date ASC LIMIT 0,1").getAsDate(conn);
            
            //Traer la ultima calidad en un periodo
            
                idQuality = new MySQLQuery("SELECT s.id_quality FROM sigma.ord_tank_client o " 
                    + "INNER JOIN bill_building b on b.id = o.mirror_id " 
                    + "INNER JOIN sigma.est_sale s ON s.client_id = o.id " 
                    + "INNER JOIN bill_client_tank c ON c.building_id = b.id " 
                    + "WHERE c.id = "+client.id
                    + " AND s.created_date >= "+"'"+periodo+"'"
                    + " AND s.id_quality IS NOT NULL "
                    + "ORDER BY s.created_date DESC LIMIT 1").getAsInteger(conn);


                //Si no hay calidad en el periodo trae la ultima calidad si hubiere
                if(idQuality==null){
                    idQuality = new MySQLQuery("SELECT s.id_quality FROM sigma.ord_tank_client o " 
                        + "INNER JOIN bill_building b on b.id = o.mirror_id " 
                        + "INNER JOIN sigma.est_sale s ON s.client_id = o.id " 
                        + "INNER JOIN bill_client_tank c ON c.building_id = b.id " 
                        + "WHERE c.id = "+client.id
                        + " AND s.id_quality IS NOT NULL "
                        + "ORDER BY s.created_date DESC LIMIT 1").getAsInteger(conn);
                }
                System.out.println("############ El id de calidad es "+idQuality);
                if(idQuality != null){
                    Object[] qualitys = new MySQLQuery("SELECT c_3_propano, c_4_butano, c_5_olefinas, agua FROM sigma.est_sale_quality s WHERE s.id = "+idQuality).getRecord(conn);
                    p.setCalidad((BigDecimal)qualitys[0], (BigDecimal)qualitys[1], (BigDecimal)qualitys[2], (BigDecimal)qualitys[3]);
                }   
                
            }
            //Si hay información de revisión la muestra
            Integer codeReview= new MySQLQuery("SELECT MAX(sbict.code) "
                    + "FROM bill_inst_check bic "
                    + "INNER JOIN sigma.bill_inst_check_type sbict ON bic.type_id = sbict.id "
                    + "INNER JOIN sigma.bill_inst_inspector bii ON bic.inspector_id = bii.id "
                    + "WHERE bic.client_id = "+client.id+" LIMIT 1").getAsInteger(conn);
            
            if(codeReview!=null){
                Object[] review=new MySQLQuery("SELECT sbict.code, bic.chk_date, sbict.name "
                        + "FROM bill_inst_check bic "
                        + "INNER JOIN sigma.bill_inst_check_type sbict ON bic.type_id = sbict.id "
                        + "INNER JOIN sigma.bill_inst_inspector bii ON bic.inspector_id = bii.id "
                        + "WHERE bic.client_id = "+client.id+" AND sbict.code="+codeReview+" LIMIT 1").getRecord(conn);
                p.setReview((Date)review[1], review[2].toString(), true);
            }
        }
    }

    private static void setLines(BillForPrint bill, List<BillBillPres> press, boolean isNet) {
        if (isNet && bill.isTotal) {
            List<LineForPrint> glp = new ArrayList<>();
            List<LineForPrint> noGlp = new ArrayList<>();
            List<LineForPrint> det = new ArrayList<>();
            
            for (int i = 0; i < press.size(); i++) {
                BillBillPres bp = press.get(i);
                LineForPrint lp = new LineForPrint();
                lp.label = bp.label;
                lp.value = bp.value;
                lp.bold = bp.bold;
                switch (bp.type) {
                    case "glp":
                        glp.add(lp);
                        break;
                    case "no_glp":
                        noGlp.add(lp);
                        break;
                    case "det":
                        det.add(lp);
                        break;
                    default:
                        throw new RuntimeException();
                }
            }
            
            bill.glpLines = glp;
            bill.noGlpLines = noGlp;
            bill.detLines = det;

            BigDecimal totalGlp = LineForPrint.sum(bill.glpLines);
            //System.out.println("11- Total glp "+totalGlp);
            bill.glpLines.add(new LineForPrint("Total Servicio Gas", totalGlp, true));
            BigDecimal totalNoGlp = LineForPrint.sum(bill.noGlpLines);
            //System.out.println("11.1- Total glp "+totalNoGlp);
            bill.noGlpLines.add(new LineForPrint("Total Otros Cargos", totalNoGlp, true));

            bill.detLines.add(0, new LineForPrint("Valor Facturado de Gas", totalGlp, false));
            bill.detLines.add(1, new LineForPrint("Facturación Otros Cargos", totalNoGlp, false));
            bill.detLines.add(2, new LineForPrint("Valor Facturado en el Periodo", totalNoGlp.add(totalGlp), true));
        } else {
            List<LineForPrint> det = new ArrayList<>();
            for (int i = 0; i < press.size(); i++) {
                BillBillPres bp = press.get(i);
                LineForPrint lp = new LineForPrint();
                lp.label = bp.label;
                lp.value = bp.value;
                lp.bold = bp.bold;
                det.add(lp);
            }
            bill.detLines = det;
        }
    }

    public static BigDecimal payDebtFromBank(BigDecimal fromBank, int docId, MySQLPreparedInsert insertPlanQ, BigDecimal deuda, int credAccount, int transTypeId, int spanId, int clientId, int userId) throws Exception {
        if (deuda.compareTo(BigDecimal.ZERO) != 0) {
            BillPlan plan = new BillPlan();
            plan.accountCredId = credAccount;
            plan.accountDebId = Accounts.BANCOS;
            plan.billSpanId = spanId;
            plan.cliTankId = clientId;
            plan.creUsuId = userId;
            plan.created = new Date();
            plan.modified = plan.created;
            plan.modUsuId = userId;
            plan.transTypeId = transTypeId;
            plan.value = deuda;
            plan.credit = BigDecimal.ZERO;
            plan.docId = docId;
            plan.docType = "fac";
            plan.prev = true;
            BillPlan.insert(plan, insertPlanQ);
            return fromBank.add(deuda);
        }
        return fromBank;
    }
}
