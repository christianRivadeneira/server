package controller.billing;

import api.bill.api.BillCovidFinanceApi;
import api.bill.model.BillAnticNoteType;
import api.bill.model.BillBuildFactor;
import api.bill.model.BillClieCau;
import api.bill.model.BillClientFactor;
import api.bill.model.BillClientTank;
import api.bill.model.BillCloseRequest;
import api.bill.model.BillCovidCfg;
import api.bill.model.BillInstance;
import api.bill.model.BillMeter;
import api.bill.model.BillPriceSpan;
import api.bill.model.BillSpan;
import api.bill.model.BillTransaction;
import api.bill.model.dto.BillReadingsCheck;
import api.sys.model.Bfile;
import api.sys.model.SysCfg;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import metadata.model.Table;
import model.billing.constants.Accounts;
import model.billing.constants.Transactions;
import model.system.SessionLogin;
import utilities.DBSettings;
import utilities.MySQLPreparedInsert;
import utilities.MySQLPreparedQuery;
import utilities.MySQLPreparedUpdate;
import utilities.MySQLQuery;
import utilities.SysTask;
import utilities.cast;
import web.fileManager;

public class BillSpanController {

    public static MySQLPreparedQuery getdebTotalQuery(Connection conn) throws SQLException {
        return new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.account_deb_id = ?1 AND t.cli_tank_id = ?2", conn);
    }

    public static MySQLPreparedQuery getcredTotalQuery(Connection conn) throws SQLException {
        return new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.account_cred_id = ?1 AND t.cli_tank_id = ?2", conn);
    }

    public static MySQLPreparedQuery getdebSpanQuery(Connection conn) throws SQLException {
        return new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.account_deb_id = ?1 AND t.cli_tank_id = ?2 AND t.bill_span_id = ?3", conn);
    }

    public static MySQLPreparedQuery getcredSpanQuery(Connection conn) throws SQLException {
        return new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.account_cred_id = ?1 AND t.cli_tank_id = ?2 AND t.bill_span_id = ?3", conn);
    }

    public static BigDecimal getAdjusment(BigDecimal val, BigDecimal unit) {
        if (unit.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        return val.divide(unit, 0, RoundingMode.HALF_EVEN).multiply(unit).subtract(val);
    }

    public static boolean isSpanCaused(BillSpan span, Integer clientId, Connection conn) throws Exception {
        return new MySQLPreparedQuery("SELECT COUNT(*) > 0 FROM  "
                + "bill_transaction t "
                + "WHERE t.trans_type_id = " + Transactions.CAUSA + " "
                + "AND t.bill_span_id = " + span.id + " "
                + (clientId != null ? "AND t.cli_tank_id = " + clientId + " " : ""),
                conn).getAsBoolean();
    }

    public static void revertSpan(int empId, BillInstance inst, Connection conn) throws Exception {
        if (inst.siteBilling) {
            throw new Exception("No disponible para facturación en sitio");
        }

        SysTask t = new SysTask(BillSpanController.class, "RevertSpan", empId, "sigma", conn);
        try {
            conn.setAutoCommit(false);
            BillSpan cons = BillSpan.getByState("cons", conn);
            BillSpan reca = BillSpan.getByState("reca", conn);
            BillSpan cart = new BillSpan().select(reca.id - 1, conn);

            Integer lastTransId = new MySQLQuery("SELECT MAX(id) FROM bill_transaction").getAsInteger(conn);

            if (reca.cauLastId != null) {
                if (!reca.cauLastId.equals(lastTransId)) {
                    throw new Exception("Se han hecho cambios después de la causación.");
                }
            } else if (cart.closeLastId != null) {
                if (!cart.closeLastId.equals(lastTransId)) {
                    throw new Exception("Se han hecho cambios después del cierre.");
                }
            } else {
                throw new Exception("Intenta deshacer un cierre antiguo.");
            }
            //borrando facturas
            new MySQLQuery("DELETE bill_plan.* FROM bill_plan, bill_bill WHERE bill_plan.doc_id = bill_bill.id AND bill_bill.bill_span_id = " + reca.id).executeDelete(conn);
            new MySQLQuery("DELETE bill_bill_pres.* FROM bill_bill_pres, bill_bill WHERE bill_bill_pres.bill_id = bill_bill.id AND bill_bill.bill_span_id = " + reca.id).executeDelete(conn);
            new MySQLQuery("DELETE FROM bill_bill WHERE bill_bill.bill_span_id = " + reca.id).executeDelete(conn);

            //borrando causación
            new MySQLQuery("DELETE FROM bill_price_span WHERE span_id = " + cons.id).executeUpdate(conn);
            new MySQLQuery("DELETE FROM bill_transaction WHERE id > " + cart.closeFirstId).executeUpdate(conn);
            new MySQLQuery("UPDATE bill_susp SET span_id = NULL WHERE span_id = " + reca.id).executeUpdate(conn);

            cart.state = "reca";
            reca.state = "cons";

            reca.cauLastId = null;
            cart.closeLastId = null;
            reca.update(conn);
            cart.update(conn);

            BillSpan.delete(cons.id, conn);
            t.success(conn);

            conn.commit();
            resetAutoIncrement("bill_plan", conn);
            resetAutoIncrement("bill_bill_pres", conn);
            resetAutoIncrement("bill_bill", conn);
            resetAutoIncrement("bill_price_span", conn);
            resetAutoIncrement("bill_transaction", conn);
        } catch (Exception ex) {
            Logger.getLogger(BillSpanController.class.getName()).log(Level.SEVERE, null, ex);
            conn.rollback();
            t.error(ex, conn);
            conn.commit();
            throw ex;
        }
    }

    //balance total de la cuenta de un usuario
    public static BigDecimal getTotalBalance(MySQLPreparedQuery debQ, MySQLPreparedQuery credQ, int accountId, int clientId) throws Exception {
        debQ.setParameter(1, accountId);
        debQ.setParameter(2, clientId);

        credQ.setParameter(1, accountId);
        credQ.setParameter(2, clientId);
        BigDecimal deb = debQ.getAsBigDecimal(true);
        BigDecimal cred = credQ.getAsBigDecimal(true);
        return deb.subtract(cred);
    }

    public static String zeroFill(String str, int length) {
        int l = length - str.length();
        if (l <= 0) {
            return str;
        }
        char[] fill = new char[l];
        for (int j = 0; j < fill.length; j++) {
            fill[j] = '0';
        }
        return new String(fill).concat(str);
    }

    private static void resetAutoIncrement(String tblName, Connection conn) throws Exception {
        Integer nId = new MySQLQuery("SELECT max(id) + 1 from " + tblName + ";").getAsInteger(conn);
        new MySQLQuery("ALTER TABLE " + tblName + " AUTO_INCREMENT = " + nId + ";").executeUpdate(conn);
    }

    public static void closeSpan(BillInstance inst, BillCloseRequest req, SessionLogin sl, Connection billConn, Connection sigmaConn) throws Exception {

        try {
            billConn.setAutoCommit(false);

            BillSpan reca = BillSpan.getByState("reca", billConn);
            BillSpan cons = BillSpan.getByState("cons", billConn);
            if (inst.isNetInstance()) {
                if (cons.cuf == null || cons.cuf.compareTo(BigDecimal.ZERO) == 0 || cons.dAomR == null || cons.dAomR.compareTo(BigDecimal.ZERO) == 0) {
                    throw new Exception("Aún no se ha parametrizado el periodo");
                }
            } else {
                ////////////////???????????????????
            }

            if (!cons.readingsClosed && inst.isTankInstance()) {
                throw new Exception("Debe hacer el cierre de lecturas.");
            }
            reca.closeFirstId = new MySQLQuery("SELECT max(id) FROM bill_transaction").getAsInteger(billConn);

            BillReadingsCheck chk = BillReadingController.checkReadings(cons.id, inst, billConn, sigmaConn);

            if (chk.tankReads != chk.tanks && !Table.DEVEL_MODE && inst.isTankInstance()) {
                throw new Exception("El número de lecturas de % tanques registradas (" + chk.tankReads + ") no coincide con el número de tanques (" + chk.tanks + ").\nImposible continuar.");
            }

            if (chk.clientReads != chk.clients) {
                throw new Exception("El número de lecturas de medidores registradas (" + chk.clientReads + ") no coincide con el número de clientes (" + chk.clients + ").\nImposible continuar.");
            }

            if (inst.isTankInstance()) {
                req.setMessage("Creando Copia de Respaldo...");
                createBk(req, sigmaConn);
                req.setMessage("Copia Exitosa...");
            }

            new MySQLQuery("insert into bill_reading_bk (select id, span_id, client_tank_id, reading, last_reading, fault_id, emp_id from bill_reading WHERE span_id <= " + reca.id + " - 8);").executeUpdate(billConn);
            new MySQLQuery("delete from bill_reading WHERE span_id <= " + reca.id + " - 8;").executeUpdate(billConn);

            if (!inst.siteBilling) {
                closeUsers(null, sl, inst, reca, req, billConn, sigmaConn);
            } else {
                if (new MySQLQuery("SELECT COUNT(*)> 0 FROM bill_client_tank c WHERE c.active AND !c.span_closed").getAsBoolean(billConn)) {
                    throw new Exception("Quedan usuarios sin facturar");
                }
                new MySQLQuery("UPDATE bill_client_tank c SET c.span_closed = 0").executeUpdate(billConn);
            }

            /////////////////////////////////////////////////////////////////////////////////////////////
            //cerrando el periodo(avanzar periodo)
            advanceSpan(reca, cons, inst, billConn, sigmaConn);

            billConn.commit();
        } catch (Exception ex) {
            billConn.rollback();
            throw ex;
        }
    }

    public static void advanceSpan(BillSpan reca, BillSpan cons, BillInstance inst, Connection billConn, Connection sigmaConn) throws Exception {
        //borrando archivos temporales de critica, las lecturas se adjuntan primero al cliente y de ahí se pasan al ord_pqr_request, pero si el request nunca llega hay que eliminarlas
        fileManager.PathInfo pi = new fileManager.PathInfo(sigmaConn);
        MySQLQuery q = new MySQLQuery("SELECT id FROM bfile WHERE owner_type = 144");
        Object[][] data = q.getRecords(sigmaConn);
        if (data != null) {
            for (Object[] row : data) {
                pi.getExistingFile(cast.asInt(row, 0)).delete();
            }
        }
        new MySQLQuery("DELETE FROM bfile WHERE owner_type = 144").executeUpdate(sigmaConn);

        reca.state = "cart";
        cons.interes = reca.interes;
        cons.interesSrv = reca.interesSrv;
        cons.state = "reca";

        cons.update(billConn);
        BillSpan ns = createNewSpan(cons, reca, inst, billConn);
        reca.closeLastId = new MySQLQuery("SELECT MAX(id) FROM bill_transaction").getAsInteger(billConn);
        reca.update(billConn);

        new MySQLQuery("INSERT INTO bill_price_span (lst_id, span_id, price) (SELECT lst_id, " + ns.id + ", price FROM bill_price_span WHERE span_id = " + cons.id + ")").executeUpdate(billConn);
        cons.cauLastId = (new MySQLQuery("SELECT MAX(id) FROM bill_transaction").getAsInteger(billConn));
        cons.update(billConn);
    }

    public static void createBk(BillCloseRequest r, Connection conn) throws Exception {
        fileManager.PathInfo pi = new fileManager.PathInfo(conn);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_H-m-s");
        Bfile bf = new Bfile();
        bf.fileName = df.format(new Date()) + ".sql.gz";
        bf.description = "bk";
        bf.ownerId = r.id;
        bf.ownerType = 140;
        bf.createdBy = r.empId;
        bf.updatedBy = r.empId;
        bf.size = 0;
        bf.created = new Date();
        bf.updated = new Date();
        bf.shrunken = false;
        bf.insert(conn);
        File f = pi.getNewFile(bf.id);
        final DBSettings db = new DBSettings(conn);
        final String dbName = new MySQLQuery("SELECT db FROM bill_instance WHERE id = " + r.instId).getAsString(conn);
        Process exec = Runtime.getRuntime().exec(
                "mysqldump --host=" + db.host + " --port=" + db.port + " "
                + " --user=" + db.user + " --password=" + db.pass + " "
                + " --skip-comments --default-character-set=latin1 --hex-blob " + dbName);
        try (FileOutputStream fos = new FileOutputStream(f); GZIPOutputStream zos = new GZIPOutputStream(fos)) {
            fileManager.copy(exec.getInputStream(), zos);
        }
        exec.waitFor();
        if (exec.exitValue() != 0) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            fileManager.copy(exec.getErrorStream(), baos);
            byte[] bytes = baos.toByteArray();
            if (bytes.length > 0) {
                throw new Exception(new String(bytes));
            } else {
                throw new Exception("Error al generar el backup");
            }
        }
    }

    private static void chargeAdjust(int acc, BillSpan reca, BillClientTank client, int empId, BigDecimal adjust, MySQLPreparedInsert insertTransPs) throws Exception {

        BillTransaction t;
        if (adjust.compareTo(BigDecimal.ZERO) > 0) {
            t = createTransaction(acc, Accounts.E_AJUST, Transactions.CAUSA_CART, reca.id, client.id, empId, adjust, 0, "cart");
        } else {
            t = createTransaction(Accounts.E_AJUST, acc, Transactions.CAUSA_CART, reca.id, client.id, empId, adjust.abs(), 0, "cart");
        }

        BillTransaction.insert(t, insertTransPs);
    }

    public static void closeUsers(Integer clientId, SessionLogin sl, BillInstance inst, BillSpan reca, BillCloseRequest req, Connection billConn, Connection sigmaConn) throws Exception {

        int empId = sl.employeeId;
        BigDecimal realCarteIni;
        if (clientId != null) {
            realCarteIni = new MySQLPreparedQuery("SELECT SUM(value) FROM bill_transaction t WHERE t.account_deb_id IN (" + Accounts.C_CAR_GLP + ", " + Accounts.C_CAR_SRV + ", " + Accounts.C_CAR_INTE_CRE + ", " + Accounts.C_CAR_FINAN_DEU + ", " + Accounts.C_CAR_CONTRIB + ") AND t.bill_span_id = " + reca.id + " AND t.cli_tank_id = " + clientId, billConn).getAsBigDecimal(true);
        } else {
            realCarteIni = new MySQLPreparedQuery("SELECT SUM(value) FROM bill_transaction t WHERE t.account_deb_id IN (" + Accounts.C_CAR_GLP + ", " + Accounts.C_CAR_SRV + ", " + Accounts.C_CAR_INTE_CRE + ", " + Accounts.C_CAR_FINAN_DEU + ", " + Accounts.C_CAR_CONTRIB + ") AND t.bill_span_id = " + reca.id, billConn).getAsBigDecimal(true);
        }

        //fin verificación de lecturas
        MySQLPreparedQuery debCurQ = getdebSpanQuery(billConn);
        MySQLPreparedQuery credCurQ = getcredSpanQuery(billConn);

        //enviado servicios causados en el mes a cerrar a la cartera
        //balance positivos significan deudas
        BillClientTank[] clients;
        if (clientId != null && inst.siteBilling) {
            BillClientTank c = new BillClientTank().select(clientId, billConn);
            if (c == null) {
                throw new Exception("El cliente no existe.");
            }
            if (c.spanClosed) {
                throw new Exception("Ya se hizo el cierre.");
            }
            c.spanClosed = true;
            c.update(billConn);
            clients = new BillClientTank[]{c};
        } else if (clientId == null && !inst.siteBilling) {
            clients = BillClientTank.getAll(true, billConn);
            req.setTotalClicks(clients.length * 3);
        } else {
            throw new Exception("Parámetros inconsistentes");
        }

        BigDecimal addedCarte = BigDecimal.ZERO;

        MySQLPreparedInsert insertTransPs = BillTransaction.getInsertQuery(false, billConn);
        BillCovidCfg covidCfg = new BillCovidCfg().select(1, sigmaConn);
        boolean covidFinanSpan = BillCovidFinanceApi.canBeFinanced(covidCfg, reca.id, billConn) && covidCfg.enabled;

        if (covidFinanSpan && covidCfg.timelyPaymentBonusRate == null) {
            throw new Exception("Debe definir el porcentaje de descuento por pago oportuno por emergencia Covid.");
        }

        MySQLPreparedQuery covidTimelyBillPs = null;
        MySQLPreparedQuery billConsAmountPs = null;

        MySQLPreparedQuery timelyBonusByCategPs = null;
        if (inst.isTankInstance()) {
            timelyBonusByCategPs = new MySQLPreparedQuery("SELECT cat.timely_payment_bonus FROM bill_client_tank c "
                    + "INNER JOIN bill_building b ON b.id = c.building_id "
                    + "INNER JOIN sigma.ord_tank_client sc ON sc.mirror_id = b.id AND sc.bill_instance_id = " + inst.id + " "
                    + "INNER JOIN sigma.est_tank_category cat ON cat.id = sc.categ_id "
                    + "WHERE c.id = ?1", billConn);
        }
        BillAnticNoteType covidNoteType = null;

        if (covidFinanSpan) {
            covidNoteType = BillAnticNoteType.getCovidType(sigmaConn);
            if (covidNoteType == null) {
                throw new Exception("Debe definir un tipo de nota de saldo a favor para covid.");
            }

            covidTimelyBillPs = new MySQLPreparedQuery("SELECT b.id FROM bill_bill b "
                    + "WHERE "
                    + "b.client_tank_id = ?1 AND b.bill_span_id = " + reca.id + " "
                    + "AND b.total AND b.payment_date IS NOT NULL "
                    + "AND b.months = 0 "
                    + "AND DAY(b.payment_date) <= " + covidCfg.timelyPaymentMaxDay + " "
                    + "LIMIT 1", billConn);

            billConsAmountPs = new MySQLPreparedQuery("SELECT SUM(p.value) FROM bill_plan p "
                    + "WHERE p.doc_id = ?1 "
                    + "AND p.account_cred_id IN (" + Accounts.C_CONS + ", " + Accounts.C_CONS_SUBS + ")", billConn);
        }

        MySQLPreparedQuery debTotalQ = getdebTotalQuery(billConn);
        MySQLPreparedQuery credTotalQ = getcredTotalQuery(billConn);

        //notas de financiación automática covid
        /*
        if (inst.isTankInstance() && timelyBonusByCategPs != null) {
            for (BillClientTank client : clients) {
                timelyBonusByCategPs.setParameter(1, client.id);
                if (timelyBonusByCategPs.getAsBoolean()) {
                    BigDecimal cartGlp = getTotalBalance(debTotalQ, credTotalQ, Accounts.C_CAR_GLP, client.id);
                    BigDecimal inteGlp = getTotalBalance(debTotalQ, credTotalQ, Accounts.C_INT_GLP, client.id);
                    BigDecimal consGlp = getSpanBalance(debCurQ, credCurQ, Accounts.C_CONS, client.id, reca.id);

                    if (cartGlp.compareTo(BigDecimal.ZERO) > 0 || inteGlp.compareTo(BigDecimal.ZERO) > 0 || consGlp.compareTo(BigDecimal.ZERO) > 0) {
                        //insertTransPs.executeBatch();
                        BillFinanceNote obj = new BillFinanceNote();
                        obj.clientId = client.id;
                        obj.description = "Generada automáticamente";
                        obj.typeId = 1;//emergencia covid;
                        obj.interestRate = BigDecimal.ZERO;
                        obj.consSpanId = reca.id + 1;
                        obj.payments = 5;
                        obj.delayPaymentSpans = 0;

                        BillFinanInsertRequest r = new BillFinanInsertRequest();
                        r.note = obj;
                        r.accs = new ArrayList<>();
                        if (cartGlp.compareTo(BigDecimal.ZERO) > 0) {
                            BillAccBalance balCar = new BillAccBalance();
                            balCar.accId = Accounts.C_CAR_GLP;
                            balCar.curBalance = cartGlp;
                            balCar.value = cartGlp;
                            r.accs.add(balCar);
                        }

                        if (inteGlp.compareTo(BigDecimal.ZERO) > 0) {
                            BillAccBalance balInte = new BillAccBalance();
                            balInte.accId = Accounts.C_INT_GLP;
                            balInte.curBalance = inteGlp;
                            balInte.value = inteGlp;
                            r.accs.add(balInte);
                        }

                        if (consGlp.compareTo(BigDecimal.ZERO) > 0) {
                            BillAccBalance balCons = new BillAccBalance();
                            balCons.accId = Accounts.C_CONS;
                            balCons.curBalance = consGlp;
                            balCons.value = consGlp;
                            r.accs.add(balCons);
                        }
                        BillFinanceNoteApi.createFinanNote(r, inst, reca.id + 1, sl, billConn, sigmaConn);
                    }
                }
            }
        }*/
        for (BillClientTank client : clients) {
            if (req != null) {
                req.tick();
            }

            //bonificación por pago oportuno covid
            /*if (covidFinanSpan && covidTimelyBillPs != null && billConsAmountPs != null && covidNoteType != null && timelyBonusByCategPs != null) {
                timelyBonusByCategPs.setParameter(1, client.id);
                if (timelyBonusByCategPs.getAsBoolean()) {
                    covidTimelyBillPs.setParameter(1, client.id);
                    Integer billId = covidTimelyBillPs.getAsInteger();
                    if (billId != null) {
                        billConsAmountPs.setParameter(1, billId);
                        BigDecimal amount = billConsAmountPs.getAsBigDecimal(true);
                        amount = amount.multiply(covidCfg.timelyPaymentBonusRate.divide(new BigDecimal(100), 4, RoundingMode.HALF_EVEN));
                        if (amount.compareTo(BigDecimal.ZERO) > 0) {
                            BillAnticNoteRequest r = new BillAnticNoteRequest();
                            r.value = amount;
                            r.note = new BillAnticNote();
                            r.note.billSpanId = reca.id + 1;
                            r.note.clientTankId = client.id;
                            r.note.typeId = covidNoteType.id;
                            r.note.descNotes = covidNoteType.name;
                            r.note.label = covidNoteType.name;
                            BillAnticNote.createNote(r, sl, inst, billConn, sigmaConn);
                        }
                    }
                }
            }*/
            BigDecimal consBal = getSpanBalance(debCurQ, credCurQ, Accounts.C_CONS, client.id, reca.id);
            if (consBal.compareTo(BigDecimal.ZERO) > 0) {
                addedCarte = addedCarte.add(consBal);
                BillTransaction trans = createTransaction(Accounts.C_CAR_GLP, Accounts.C_CONS, Transactions.CAUSA_CART, reca.id, client.id, empId, consBal, 0, "cart");
                BillTransaction.insert(trans, insertTransPs);
            }

            BigDecimal consSubsBal = getSpanBalance(debCurQ, credCurQ, Accounts.C_CONS_SUBS, client.id, reca.id);
            if (consSubsBal.compareTo(BigDecimal.ZERO) > 0) {
                addedCarte = addedCarte.add(consSubsBal);
                BillTransaction trans = createTransaction(Accounts.C_CAR_GLP, Accounts.C_CONS_SUBS, Transactions.CAUSA_CART, reca.id, client.id, empId, consSubsBal, 0, "cart");
                BillTransaction.insert(trans, insertTransPs);
            }

            BigDecimal contriBal = getSpanBalance(debCurQ, credCurQ, Accounts.C_CONTRIB, client.id, reca.id);
            if (contriBal.compareTo(BigDecimal.ZERO) > 0) {
                addedCarte = addedCarte.add(contriBal);
                BillTransaction trans = createTransaction(Accounts.C_CAR_CONTRIB, Accounts.C_CONTRIB, Transactions.CAUSA_CART, reca.id, client.id, empId, contriBal, 0, "cart");
                BillTransaction.insert(trans, insertTransPs);
            }

            BigDecimal rebillBal = getSpanBalance(debCurQ, credCurQ, Accounts.C_REBILL, client.id, reca.id);
            if (rebillBal.compareTo(BigDecimal.ZERO) > 0) {
                addedCarte = addedCarte.add(rebillBal);
                BillTransaction trans = createTransaction(Accounts.C_CAR_GLP, Accounts.C_REBILL, Transactions.CAUSA_CART, reca.id, client.id, empId, rebillBal, 0, "cart");
                BillTransaction.insert(trans, insertTransPs);
            }

            BigDecimal basicBal = getSpanBalance(debCurQ, credCurQ, Accounts.C_BASI, client.id, reca.id);
            if (basicBal.compareTo(BigDecimal.ZERO) > 0) {
                addedCarte = addedCarte.add(basicBal);
                BillTransaction trans = createTransaction(Accounts.C_CAR_GLP, Accounts.C_BASI, Transactions.CAUSA_CART, reca.id, client.id, empId, basicBal, 0, "cart");
                BillTransaction.insert(trans, insertTransPs);
            }
            BigDecimal servEdifBal = getSpanBalance(debCurQ, credCurQ, Accounts.C_CUOTA_SER_EDI, client.id, reca.id);
            if (servEdifBal.compareTo(BigDecimal.ZERO) > 0) {
                addedCarte = addedCarte.add(servEdifBal);
                BillTransaction trans = createTransaction(Accounts.C_CAR_SRV, Accounts.C_CUOTA_SER_EDI, Transactions.CAUSA_CART, reca.id, client.id, empId, servEdifBal, 0, "cart");
                BillTransaction.insert(trans, insertTransPs);
            }

            BigDecimal servUsuGlpBal = getSpanBalance(debCurQ, credCurQ, Accounts.C_CUOTA_SER_CLI_GLP, client.id, reca.id);
            if (servUsuGlpBal.compareTo(BigDecimal.ZERO) > 0) {
                addedCarte = addedCarte.add(servUsuGlpBal);
                BillTransaction trans = createTransaction(Accounts.C_CAR_GLP, Accounts.C_CUOTA_SER_CLI_GLP, Transactions.CAUSA_CART, reca.id, client.id, empId, servUsuGlpBal, 0, "cart");
                BillTransaction.insert(trans, insertTransPs);
            }

            BigDecimal servUsuSrvBal = getSpanBalance(debCurQ, credCurQ, Accounts.C_CUOTA_SER_CLI_SRV, client.id, reca.id);
            if (servUsuSrvBal.compareTo(BigDecimal.ZERO) > 0) {
                addedCarte = addedCarte.add(servUsuSrvBal);
                BillTransaction trans = createTransaction(Accounts.C_CAR_SRV, Accounts.C_CUOTA_SER_CLI_SRV, Transactions.CAUSA_CART, reca.id, client.id, empId, servUsuSrvBal, 0, "cart");
                BillTransaction.insert(trans, insertTransPs);
            }

            BigDecimal finanFeeMonthBal = getSpanBalance(debCurQ, credCurQ, Accounts.C_CUOTA_FINAN_DEU, client.id, reca.id);
            if (finanFeeMonthBal.compareTo(BigDecimal.ZERO) > 0) {
                addedCarte = addedCarte.add(finanFeeMonthBal);
                BillTransaction trans = createTransaction(Accounts.C_CAR_FINAN_DEU, Accounts.C_CUOTA_FINAN_DEU, Transactions.CAUSA_CART, reca.id, client.id, empId, finanFeeMonthBal, 0, "cart");
                BillTransaction.insert(trans, insertTransPs);
            }

            BigDecimal reconexBal = getSpanBalance(debCurQ, credCurQ, Accounts.C_RECON, client.id, reca.id);
            if (reconexBal.compareTo(BigDecimal.ZERO) > 0) {
                addedCarte = addedCarte.add(reconexBal);
                BillTransaction trans = createTransaction(Accounts.C_CAR_SRV, Accounts.C_RECON, Transactions.CAUSA_CART, reca.id, client.id, empId, reconexBal, 0, "cart");
                BillTransaction.insert(trans, insertTransPs);
            }

            BigDecimal inteCredBal = getSpanBalance(debCurQ, credCurQ, Accounts.C_CUOTA_INT_CRE, client.id, reca.id);
            if (inteCredBal.compareTo(BigDecimal.ZERO) > 0) {
                addedCarte = addedCarte.add(inteCredBal);
                BillTransaction trans = createTransaction(Accounts.C_CAR_INTE_CRE, Accounts.C_CUOTA_INT_CRE, Transactions.CAUSA_CART, reca.id, client.id, empId, inteCredBal, 0, "cart");
                BillTransaction.insert(trans, insertTransPs);
            }
        }

        insertTransPs.executeBatch();
        BigDecimal realCarteNew;
        if (clientId != null) {
            realCarteNew = new MySQLPreparedQuery("SELECT SUM(value) FROM bill_transaction t WHERE t.account_deb_id in (" + Accounts.C_CAR_FINAN_DEU + ", " + Accounts.C_CAR_CONTRIB + ", " + Accounts.C_CAR_GLP + ", " + Accounts.C_CAR_SRV + ", " + Accounts.C_CAR_INTE_CRE + ") AND t.bill_span_id = " + reca.id + " AND t.cli_tank_id = " + clientId, billConn).getAsBigDecimal(true);
        } else {
            realCarteNew = new MySQLPreparedQuery("SELECT SUM(value) FROM bill_transaction t WHERE t.account_deb_id in (" + Accounts.C_CAR_FINAN_DEU + ", " + Accounts.C_CAR_CONTRIB + ", " + Accounts.C_CAR_GLP + ", " + Accounts.C_CAR_SRV + ", " + Accounts.C_CAR_INTE_CRE + ") AND t.bill_span_id = " + reca.id, billConn).getAsBigDecimal(true);
        }

        if (realCarteNew.subtract(realCarteIni).compareTo(addedCarte) != 0) {
            throw new Exception("Error, la cartera no coincide, esperada = " + addedCarte + ", encontrada = " + realCarteNew.subtract(realCarteIni));
        }

        //ajuste a la unidad
        BigDecimal spanAdjustMult = new BigDecimal(reca.adjust);

        Map<Integer, BigDecimal> contribInterests = new HashMap<>();
        for (BillClientTank client : clients) {
            if (req != null) {
                req.tick();
            }
            BigDecimal inteGlp = getTotalBalance(debTotalQ, credTotalQ, Accounts.C_INT_GLP, client.id);
            BigDecimal inteSrv = getTotalBalance(debTotalQ, credTotalQ, Accounts.C_INT_SRV, client.id);
            BigDecimal inteFinanDeu = getTotalBalance(debTotalQ, credTotalQ, Accounts.C_INT_FINAN_DEU, client.id);
            BigDecimal inteContrib = getTotalBalance(debTotalQ, credTotalQ, Accounts.C_INT_CONTRIB, client.id);
            BigDecimal inteOld = getTotalBalance(debTotalQ, credTotalQ, Accounts.C_INT_OLD, client.id);
            BigDecimal cartGlp = getTotalBalance(debTotalQ, credTotalQ, Accounts.C_CAR_GLP, client.id);
            BigDecimal cartSrv = getTotalBalance(debTotalQ, credTotalQ, Accounts.C_CAR_SRV, client.id);
            BigDecimal cartFinanDeu = getTotalBalance(debTotalQ, credTotalQ, Accounts.C_CAR_FINAN_DEU, client.id);
            BigDecimal cartContrib = getTotalBalance(debTotalQ, credTotalQ, Accounts.C_CAR_CONTRIB, client.id);
            BigDecimal cartInteCre = getTotalBalance(debTotalQ, credTotalQ, Accounts.C_CAR_INTE_CRE, client.id);
            BigDecimal cartOld = getTotalBalance(debTotalQ, credTotalQ, Accounts.C_CAR_OLD, client.id);

            //ajuste a la decena de las deudas, requerimiento valor exacto de la factura anterior
            BigDecimal adjust = getAdjusment(cartFinanDeu.add(cartContrib).add(cartGlp).add(cartInteCre).add(cartOld.add(inteGlp).add(inteOld)).add(cartSrv).add(inteSrv).add(inteFinanDeu).add(inteContrib), spanAdjustMult);
            //cobro ajuste, se trata de cobrar el ajuste en la cuenta que tenga capacidad de hacer, para no generar cuentas con saldos negativos
            if (adjust.compareTo(BigDecimal.ZERO) != 0) {
                if (cartFinanDeu.add(adjust).compareTo(BigDecimal.ZERO) >= 0) {
                    chargeAdjust(Accounts.C_CAR_FINAN_DEU, reca, client, empId, adjust, insertTransPs);
                    cartFinanDeu = cartFinanDeu.add(adjust);
                } else if (cartContrib.add(adjust).compareTo(BigDecimal.ZERO) >= 0) {
                    chargeAdjust(Accounts.C_CAR_CONTRIB, reca, client, empId, adjust, insertTransPs);
                    cartContrib = cartContrib.add(adjust);
                } else if (cartGlp.add(adjust).compareTo(BigDecimal.ZERO) >= 0) {
                    chargeAdjust(Accounts.C_CAR_GLP, reca, client, empId, adjust, insertTransPs);
                    cartGlp = cartGlp.add(adjust);
                } else if (cartInteCre.add(adjust).compareTo(BigDecimal.ZERO) >= 0) {
                    chargeAdjust(Accounts.C_CAR_INTE_CRE, reca, client, empId, adjust, insertTransPs);
                    cartInteCre = cartInteCre.add(adjust);
                } else if (cartOld.add(adjust).compareTo(BigDecimal.ZERO) >= 0) {
                    chargeAdjust(Accounts.C_CAR_OLD, reca, client, empId, adjust, insertTransPs);
                    cartOld = cartOld.add(adjust);
                } else if (inteGlp.add(adjust).compareTo(BigDecimal.ZERO) >= 0) {
                    chargeAdjust(Accounts.C_INT_GLP, reca, client, empId, adjust, insertTransPs);
                    inteGlp = inteGlp.add(adjust);
                } else if (inteOld.add(adjust).compareTo(BigDecimal.ZERO) >= 0) {
                    chargeAdjust(Accounts.C_INT_OLD, reca, client, empId, adjust, insertTransPs);
                    inteOld = inteOld.add(adjust);
                } else if (cartSrv.add(adjust).compareTo(BigDecimal.ZERO) >= 0) {
                    chargeAdjust(Accounts.C_CAR_SRV, reca, client, empId, adjust, insertTransPs);
                    cartSrv = cartSrv.add(adjust);
                } else if (inteSrv.add(adjust).compareTo(BigDecimal.ZERO) >= 0) {
                    chargeAdjust(Accounts.C_INT_SRV, reca, client, empId, adjust, insertTransPs);
                    inteSrv = inteSrv.add(adjust);
                } else if (inteFinanDeu.add(adjust).compareTo(BigDecimal.ZERO) >= 0) {
                    chargeAdjust(Accounts.C_INT_FINAN_DEU, reca, client, empId, adjust, insertTransPs);
                    inteFinanDeu = inteFinanDeu.add(adjust);
                } else if (inteContrib.add(adjust).compareTo(BigDecimal.ZERO) >= 0) {
                    chargeAdjust(Accounts.C_INT_CONTRIB, reca, client, empId, adjust, insertTransPs);
                    inteContrib = inteContrib.add(adjust);
                } else {
                    //no hay una cuenta que pueda recibir el ajuste a la decena, no se registra.
                }
            }

            if (!client.skipInterest) {
                //cobro intereses                    
                if (cartGlp.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal newInte = cartGlp.multiply(reca.interes).setScale(2, RoundingMode.HALF_EVEN);
                    BillTransaction trans = createTransaction(Accounts.C_INT_GLP, Accounts.E_INTER, Transactions.CAUSA_INTE_GLP, reca.id, client.id, empId, newInte, 0, "cart");
                    BillTransaction.insert(trans, insertTransPs);
                    inteGlp = inteGlp.add(newInte);
                }

                if (cartSrv.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal newInte = cartSrv.multiply(reca.interesSrv).setScale(2, RoundingMode.HALF_EVEN);
                    BillTransaction trans = createTransaction(Accounts.C_INT_SRV, Accounts.E_INTER, Transactions.CAUSA_INTE_SRV, reca.id, client.id, empId, newInte, 0, "cart");
                    BillTransaction.insert(trans, insertTransPs);
                    inteSrv = inteSrv.add(newInte);
                }

                if (cartFinanDeu.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal newInte = cartFinanDeu.multiply(reca.interesSrv).setScale(2, RoundingMode.HALF_EVEN);
                    BillTransaction trans = createTransaction(Accounts.C_INT_FINAN_DEU, Accounts.E_INTER, Transactions.CAUSA_INTE_SRV, reca.id, client.id, empId, newInte, 0, "cart");
                    BillTransaction.insert(trans, insertTransPs);
                    inteFinanDeu = inteFinanDeu.add(newInte);
                }

                if (cartContrib.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal newInte = cartContrib.multiply(reca.interes).setScale(2, RoundingMode.HALF_EVEN);
                    BillTransaction trans = createTransaction(Accounts.C_INT_CONTRIB, Accounts.E_INTER, Transactions.CAUSA_INTE_SRV, reca.id, client.id, empId, newInte, 0, "cart");
                    BillTransaction.insert(trans, insertTransPs);
                    inteContrib = inteContrib.add(newInte);
                    contribInterests.put(clientId, newInte);
                }

                //cobro intereses viejos
                if (cartOld.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal newAliInte = cartOld.multiply(reca.interes).setScale(2, RoundingMode.HALF_EVEN);
                    BillTransaction trans = createTransaction(Accounts.C_INT_OLD, Accounts.E_INTER, Transactions.CAUSA_INTE_GLP, reca.id, client.id, empId, newAliInte, 0, "cart");
                    BillTransaction.insert(trans, insertTransPs);
                    inteOld = inteOld.add(newAliInte);
                }
            }

            //pagos por anticipado
            //balance general de pagos x anticipado
            BigDecimal anticBalance = getTotalBalance(debTotalQ, credTotalQ, Accounts.C_ANTICIP, client.id).multiply(new BigDecimal(-1));

            BigDecimal cartOldAntic = min(anticBalance, cartOld);
            if (cartOldAntic.compareTo(BigDecimal.ZERO) > 0) {
                BillTransaction.insert(createTransaction(Accounts.C_ANTICIP, Accounts.C_CAR_OLD, Transactions.PAGO_ANTICIP, reca.id + 1, client.id, empId, cartOldAntic, 0, "cart"), insertTransPs);
                anticBalance = anticBalance.subtract(cartOldAntic);
            }
            BigDecimal cartGlpAntic = min(anticBalance, cartGlp);
            if (cartGlpAntic.compareTo(BigDecimal.ZERO) > 0) {
                BillTransaction.insert(createTransaction(Accounts.C_ANTICIP, Accounts.C_CAR_GLP, Transactions.PAGO_ANTICIP, reca.id + 1, client.id, empId, cartGlpAntic, 0, "cart"), insertTransPs);
                anticBalance = anticBalance.subtract(cartGlpAntic);
            }

            BigDecimal cartCreAntic = min(anticBalance, cartInteCre);
            if (cartCreAntic.compareTo(BigDecimal.ZERO) > 0) {
                BillTransaction.insert(createTransaction(Accounts.C_ANTICIP, Accounts.C_CAR_INTE_CRE, Transactions.PAGO_ANTICIP, reca.id + 1, client.id, empId, cartCreAntic, 0, "cart"), insertTransPs);
                anticBalance = anticBalance.subtract(cartCreAntic);
            }

            BigDecimal cartSrvAntic = min(anticBalance, cartSrv);
            if (cartSrvAntic.compareTo(BigDecimal.ZERO) > 0) {
                BillTransaction.insert(createTransaction(Accounts.C_ANTICIP, Accounts.C_CAR_SRV, Transactions.PAGO_ANTICIP, reca.id + 1, client.id, empId, cartSrvAntic, 0, "cart"), insertTransPs);
                anticBalance = anticBalance.subtract(cartSrvAntic);
            }

            BigDecimal cartFinanDeuAntic = min(anticBalance, cartFinanDeu);
            if (cartFinanDeuAntic.compareTo(BigDecimal.ZERO) > 0) {
                BillTransaction.insert(createTransaction(Accounts.C_ANTICIP, Accounts.C_CAR_FINAN_DEU, Transactions.PAGO_ANTICIP, reca.id + 1, client.id, empId, cartFinanDeuAntic, 0, "cart"), insertTransPs);
                anticBalance = anticBalance.subtract(cartFinanDeuAntic);
            }

            BigDecimal cartContribAntic = min(anticBalance, cartContrib);
            if (cartContribAntic.compareTo(BigDecimal.ZERO) > 0) {
                BillTransaction.insert(createTransaction(Accounts.C_ANTICIP, Accounts.C_CAR_CONTRIB, Transactions.PAGO_ANTICIP, reca.id + 1, client.id, empId, cartContribAntic, 0, "cart"), insertTransPs);
                anticBalance = anticBalance.subtract(cartContribAntic);
            }

            BigDecimal intOldAntic = min(anticBalance, inteOld);
            if (intOldAntic.compareTo(BigDecimal.ZERO) > 0) {
                BillTransaction.insert(createTransaction(Accounts.C_ANTICIP, Accounts.C_INT_OLD, Transactions.PAGO_ANTICIP, reca.id + 1, client.id, empId, intOldAntic, 0, "cart"), insertTransPs);
                anticBalance = anticBalance.subtract(intOldAntic);
            }

            BigDecimal intGlpAntic = min(anticBalance, inteGlp);
            if (intGlpAntic.compareTo(BigDecimal.ZERO) > 0) {
                BillTransaction.insert(createTransaction(Accounts.C_ANTICIP, Accounts.C_INT_GLP, Transactions.PAGO_ANTICIP, reca.id + 1, client.id, empId, intGlpAntic, 0, "cart"), insertTransPs);
                anticBalance = anticBalance.subtract(intGlpAntic);
            }

            BigDecimal intSrvAntic = min(anticBalance, inteSrv);
            if (intSrvAntic.compareTo(BigDecimal.ZERO) > 0) {
                BillTransaction.insert(createTransaction(Accounts.C_ANTICIP, Accounts.C_INT_SRV, Transactions.PAGO_ANTICIP, reca.id + 1, client.id, empId, intSrvAntic, 0, "cart"), insertTransPs);
                //anticBalance = anticBalance.subtract(intAntic);//la ultima resta no es necesaria a menos que agregue otro rubro.
            }

            BigDecimal inteFinanDeuAntic = min(anticBalance, inteFinanDeu);
            if (inteFinanDeuAntic.compareTo(BigDecimal.ZERO) > 0) {
                BillTransaction.insert(createTransaction(Accounts.C_ANTICIP, Accounts.C_INT_FINAN_DEU, Transactions.PAGO_ANTICIP, reca.id + 1, client.id, empId, inteFinanDeuAntic, 0, "cart"), insertTransPs);
                anticBalance = anticBalance.subtract(inteFinanDeuAntic);
            }

            BigDecimal inteContribAntic = min(anticBalance, inteContrib);
            if (inteContribAntic.compareTo(BigDecimal.ZERO) > 0) {
                BillTransaction.insert(createTransaction(Accounts.C_ANTICIP, Accounts.C_INT_CONTRIB, Transactions.PAGO_ANTICIP, reca.id + 1, client.id, empId, inteContribAntic, 0, "cart"), insertTransPs);
                //la ultima resta no es necesaria a menos que agregue otro rubro.
                //anticBalance = anticBalance.subtract(inteContribAntic);
            }
        }
        insertTransPs.executeBatch();

        //CAUSACIÓN//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        SysCfg sysCfg = SysCfg.select(sigmaConn);

        if (inst.isNetInstance() && sysCfg.skipMinCons) {
            throw new Exception("La omisión del consumo mínimo no aplica a instancias de redes");
        }

        BillSpan cons = BillSpan.getByState("cons", billConn);

        if ((cons.pms == null || cons.pms.equals(BigDecimal.ZERO)) && inst.isNetInstance()) {
            throw new Exception("Debe definir los parámetros del periodo.");
        }

        if (sysCfg.skipMinCons && cons.minConsValue == null) {
            throw new Exception("Debe indicar el valor mínimo de consumo.");
        }

        //cuenta causaciones en el periodo
        if (inst.siteBilling) {
            if (isSpanCaused(cons, clientId, billConn)) {
                throw new Exception("Ya se han causado los servicios para este periodo.\nImposible continuar.");
            }
        } else {
            if (isSpanCaused(cons, null, billConn)) {
                throw new Exception("Ya se han causado los servicios para este periodo.\nImposible continuar.");
            }
        }

        if (inst.isTankInstance()) {
            String pend = new MySQLPreparedQuery("SELECT "
                    + "GROUP_CONCAT(c.num_install) "
                    + "FROM "
                    + "bill_client_tank AS c "
                    + "WHERE "
                    + "c.active = 1 AND  "
                    + "(SELECT count(*) = 0 FROM bill_client_list cl WHERE cl.client_id = c.id AND span_id <= " + cons.id + ")", billConn).getAsString();

            if (pend != null && !pend.isEmpty()) {
                throw new Exception("Algunos usuarios no tienen lista de precios. Imposible continuar.\n" + pend);
            }
        }

        Map<Integer, BigDecimal> prices = BillPriceSpan.getPricesMap(billConn, cons.id);

        //saldo en servicios pagados por anticipado
        MySQLPreparedQuery credAntic = getcredTotalQuery(billConn);
        credAntic.setParameter(1, Accounts.C_ANTICIP);
        MySQLPreparedQuery debAntic = getdebTotalQuery(billConn);
        debAntic.setParameter(1, Accounts.C_ANTICIP);
        //descuento para el edificio
        MySQLPreparedQuery dtoQ = new MySQLPreparedQuery("SELECT amount FROM bill_dto WHERE span_id = " + cons.id + " AND build_id = ?1", billConn);

        //factor vigente para el edificio en el periodo
        MySQLPreparedQuery billFactorQ = BillBuildFactor.getFactorQuery(billConn);
        MySQLPreparedQuery clientFactorQ = BillClientFactor.getFactorQuery(billConn);
        MySQLPreparedQuery meterFactorQ = BillMeter.getMeterQuery(billConn);

        //lectura por cliente y periodo
        MySQLPreparedQuery readingQ = new MySQLPreparedQuery("SELECT r.reading - r.last_reading FROM bill_reading AS r where r.span_id = ?1 AND r.client_tank_id = ?2", billConn);
        MySQLPreparedQuery userUsuQ = new MySQLPreparedQuery("SELECT f.value - f.ext_pay, f.inter - f.ext_inter + IFNULL(f.inter_tax, 0) - IFNULL(f.ext_inter_tax, 0), inte_type, CONCAT(t.name, ' (',(f.place+1),'/', (SELECT max(place) +1 FROM bill_user_service_fee f1 WHERE f1.service_id = s.id),')'), t.trans_type FROM \n"
                + "bill_user_service s "
                + "INNER JOIN bill_service_type t ON t.id = s.type_id "
                + "INNER JOIN bill_user_service_fee f ON s.id = f.service_id "
                + "WHERE f.value - f.ext_pay > 0 AND s.bill_client_tank_id = ?1 AND s.bill_span_id + f.place = " + cons.id, billConn);

        MySQLPreparedQuery finanFeesQ = new MySQLPreparedQuery("SELECT f.id, f.note_id, f.capital, f.interest, CONCAT('Finan. por ', t.name, ' (',(f.place+1),'/', (SELECT max(place) +1 FROM bill_finance_note_fee f1 WHERE f1.note_id = s.id),')') FROM "
                + "bill_finance_note s "
                + "INNER JOIN sigma.bill_finance_note_type t ON t.id = s.type_id "
                + "INNER JOIN bill_finance_note_fee f ON s.id = f.note_id "
                + "WHERE s.client_id = ?1 AND s.cons_span_id + f.place + s.delay_payment_spans = " + cons.id, billConn);

        MySQLPreparedQuery servEdiQ = new MySQLPreparedQuery("SELECT s.total, s.payments, CONCAT(t.name, '(', CAST(" + cons.id + " - s.bill_span_id + 1 AS CHAR), '/', CAST(s.payments AS CHAR),')') FROM bill_build_service s, bill_service_type t WHERE t.id = s.type_id AND s.bill_span_id <= " + cons.id + " AND s.bill_span_id + s.payments - 1 >= " + cons.id + " AND s.bill_building_id = ?1", billConn);
        MySQLPreparedQuery userEdiQ = new MySQLPreparedQuery("SELECT count(*) FROM bill_client_tank WHERE building_id = ?1 AND active = true AND num_install IS NOT NULL ", billConn);

        MySQLPreparedQuery reconQ = new MySQLPreparedQuery(""
                + "SELECT count(*) > 0 FROM bill_susp s "
                + "WHERE s.client_id = ?1 AND s.cancelled = 0 AND s.recon_date IS NOT NULL AND s.span_id IS NULL", billConn);

        BigDecimal totalConsum = BigDecimal.ZERO;//para verificar que se hizo correctamente la causación

        MySQLPreparedUpdate reconUpdateQ = new MySQLPreparedUpdate("UPDATE bill_susp SET span_id = ?2 WHERE client_id = ?1 AND cancelled = 0 AND recon_date IS NOT NULL AND span_id IS NULL", billConn);

        for (BillClientTank client : clients) {
            if (req != null) {
                req.tick();
            }
            //balance general de pagos x anticipado
            BigDecimal anticBalance = getTotalBalance(debAntic, credAntic, Accounts.C_ANTICIP, client.id).multiply(new BigDecimal(-1));
            //CONSUMO
            //lecturas
            readingQ.setParameter(1, cons.id);
            readingQ.setParameter(2, client.id);
            BigDecimal m3Amount = readingQ.getAsBigDecimal(true);
            BillMeter meter = BillMeter.getMeter(cons.id, client.id, meterFactorQ);
            //20/10/2020 se comenta por petición de mg
            /*if (meter == null) {
                throw new Exception("El usuario " + client.numInstall + " no tiene medidor");
            }*/

            if (inst.isTankInstance()) {
                //factor del edificio
                BigDecimal factor = BillBuildFactor.getFactor(cons.id, client.buildingId, billFactorQ);
                BigDecimal clientFac = BillClientFactor.getFactor(cons.id, client.id, clientFactorQ);
                
                factor = (clientFac == BigDecimal.ZERO ? factor : clientFac);
                if (meter != null) {
                    factor = factor.multiply(meter.factor);
                }

                //CARGO FIJO
                if (cons.fixedCharge.compareTo(BigDecimal.ZERO) > 0) {
                    BillTransaction transFijo = createTransaction(Accounts.C_BASI, Accounts.E_ING_OP, Transactions.CAUSA, cons.id, client.id, empId, cons.fixedCharge, 0, "cau");
                    BillTransaction.insert(transFijo, insertTransPs);
                    //--antipado
                    BigDecimal fixedChargeAntic = min(anticBalance, cons.fixedCharge);
                    if (fixedChargeAntic.compareTo(BigDecimal.ZERO) > 0) {
                        BillTransaction.insert(createTransaction(Accounts.C_ANTICIP, Accounts.C_BASI, Transactions.PAGO_ANTICIP, cons.id, client.id, empId, fixedChargeAntic, 0, "cau"), insertTransPs);
                        anticBalance = anticBalance.subtract(fixedChargeAntic);
                    }
                }

                //CONSUMO
                //calculo final y transacción
                int listId = BillPriceSpan.getListId(billConn, cons.id, client.id);
                BigDecimal monthlyCost = cons.getConsVal(m3Amount, factor, prices.get(listId));

                if (sysCfg.skipMinCons && monthlyCost.compareTo(cons.minConsValue) < 0) {
                    monthlyCost = BigDecimal.ZERO;
                }

                totalConsum = totalConsum.add(monthlyCost);
                BillTransaction transMen = createTransaction(Accounts.C_CONS, Accounts.E_ING_OP, Transactions.CAUSA, cons.id, client.id, empId, monthlyCost, 0, "cau");
                BillTransaction.insert(transMen, insertTransPs);
                //aplicación de dto en el valor por edificio            
                //descuento para el edificio
                dtoQ.setParameter(1, client.buildingId);

                BigDecimal dto = cons.getConsVal(m3Amount, factor, dtoQ.getAsBigDecimal(true));
                if (dto.compareTo(BigDecimal.ZERO) > 0) {
                    BillTransaction.insert(createTransaction(Accounts.E_ING_OP, Accounts.C_CONS, Transactions.DTO_EDIF, cons.id, client.id, empId, dto, 0, "cau"), insertTransPs);
                    monthlyCost = monthlyCost.subtract(dto);
                }

                //pago anticipado
                BigDecimal monthlyCostAntic = min(anticBalance, monthlyCost);
                if (monthlyCostAntic.compareTo(BigDecimal.ZERO) > 0) {
                    BillTransaction.insert(createTransaction(Accounts.C_ANTICIP, Accounts.C_CONS, Transactions.PAGO_ANTICIP, cons.id, client.id, empId, monthlyCostAntic, 0, "cau"), insertTransPs);
                    anticBalance = anticBalance.subtract(monthlyCostAntic);
                }
            } else if (inst.isNetInstance()) {
                BillClieCau cau = BillClieCau.calc(client, cons, meter, m3Amount);
                if (contribInterests.containsKey(clientId)) {
                    cau.contribInterest = contribInterests.get(clientId);
                }

                if (cau.fixedCharge.compareTo(BigDecimal.ZERO) > 0) {
                    BillTransaction transFijo = createTransaction(Accounts.C_BASI, Accounts.E_ING_OP, Transactions.CAUSA, cons.id, client.id, empId, cau.fixedCharge, 0, "cau");
                    BillTransaction.insert(transFijo, insertTransPs);
                    //--antipado
                    BigDecimal fixedChargeAntic = min(anticBalance, cau.fixedCharge);
                    if (fixedChargeAntic.compareTo(BigDecimal.ZERO) > 0) {
                        BillTransaction.insert(createTransaction(Accounts.C_ANTICIP, Accounts.C_BASI, Transactions.PAGO_ANTICIP, cons.id, client.id, empId, fixedChargeAntic, 0, "cau"), insertTransPs);
                        anticBalance = anticBalance.subtract(fixedChargeAntic);
                    }
                }

                if (cau.valConsSubs.compareTo(BigDecimal.ZERO) > 0) {
                    totalConsum = totalConsum.add(cau.valConsSubs);
                    BillTransaction transMen = createTransaction(Accounts.C_CONS_SUBS, Accounts.E_ING_OP, Transactions.CAUSA, cons.id, client.id, empId, cau.valConsSubs, 0, "cau");
                    BillTransaction.insert(transMen, insertTransPs);
                    BillTransaction.insert(createTransaction(Accounts.E_SUBS, Accounts.C_CONS_SUBS, Transactions.CAUSA_SUBSIDY, cons.id, client.id, empId, cau.valSubs, 0, "cau"), insertTransPs);

                    //pago anticipado
                    BigDecimal monthlyCostAntic = min(anticBalance, cau.valConsSubs.subtract(cau.valSubs));
                    if (monthlyCostAntic.compareTo(BigDecimal.ZERO) > 0) {
                        BillTransaction.insert(createTransaction(Accounts.C_ANTICIP, Accounts.C_CONS_SUBS, Transactions.PAGO_ANTICIP, cons.id, client.id, empId, monthlyCostAntic, 0, "cau"), insertTransPs);
                        anticBalance = anticBalance.subtract(monthlyCostAntic);
                    }
                }

                if (cau.valConsNoSubs.compareTo(BigDecimal.ZERO) > 0) {
                    totalConsum = totalConsum.add(cau.valConsNoSubs);
                    BillTransaction transMen = createTransaction(Accounts.C_CONS, Accounts.E_ING_OP, Transactions.CAUSA, cons.id, client.id, empId, cau.valConsNoSubs, 0, "cau");
                    BillTransaction.insert(transMen, insertTransPs);

                    //pago anticipado
                    BigDecimal monthlyCostAntic = min(anticBalance, cau.valConsNoSubs);
                    if (monthlyCostAntic.compareTo(BigDecimal.ZERO) > 0) {
                        BillTransaction.insert(createTransaction(Accounts.C_ANTICIP, Accounts.C_CONS, Transactions.PAGO_ANTICIP, cons.id, client.id, empId, monthlyCostAntic, 0, "cau"), insertTransPs);
                        anticBalance = anticBalance.subtract(monthlyCostAntic);
                    }
                }

                if (cau.valConsNoSubs.add(cau.valConsSubs).compareTo(BigDecimal.ZERO) == 0) {
                    BillTransaction transBlank = createTransaction(Accounts.C_CONS, Accounts.E_ING_OP, Transactions.CAUSA, cons.id, client.id, empId, BigDecimal.ZERO, 0, "cau");
                    BillTransaction.insert(transBlank, insertTransPs);
                }

                if (cau.valContrib.compareTo(BigDecimal.ZERO) > 0) {
                    BillTransaction transContrib = createTransaction(Accounts.C_CONTRIB, Accounts.E_CONTRIB, Transactions.CAUSA, cons.id, client.id, empId, cau.valContrib, 0, "cau");
                    BillTransaction.insert(transContrib, insertTransPs);
                    BigDecimal contribAntic = min(anticBalance, cau.valContrib);
                    if (contribAntic.compareTo(BigDecimal.ZERO) > 0) {
                        BillTransaction.insert(createTransaction(Accounts.C_ANTICIP, Accounts.C_CONTRIB, Transactions.PAGO_ANTICIP, cons.id, client.id, empId, contribAntic, 0, "cau"), insertTransPs);
                        anticBalance = anticBalance.subtract(contribAntic);
                    }
                }
                cau.insert(billConn);
            } else {
                throw new RuntimeException();
            }

            //FINANCIACIÓN 
            finanFeesQ.setParameter(1, client.id);
            Object[][] finanFees = finanFeesQ.getRecords();
            for (Object[] finanFee : finanFees) {
                int feeId = MySQLQuery.getAsInteger(finanFee[0]);
                int noteId = MySQLQuery.getAsInteger(finanFee[1]);
                BigDecimal capital = MySQLQuery.getAsBigDecimal(finanFee[2], true);
                BigDecimal inter = MySQLQuery.getAsBigDecimal(finanFee[3], true);
                String label = MySQLQuery.getAsString(finanFee[4]);

                new MySQLQuery("UPDATE bill_finance_note_fee f SET f.caused = 1 WHERE f.id = ?1").setParam(1, feeId).executeUpdate(billConn);
                new MySQLQuery("UPDATE bill_finance_note n SET n.caused = "
                        + "(SELECT COUNT(IF(f.caused = 0, 1, NULL)) = 0 FROM bill_finance_note_fee f WHERE f.note_id = n.id) "
                        + "WHERE n.id = ?1").setParam(1, noteId).executeUpdate(billConn);

                BillTransaction transCapi = createTransaction(Accounts.C_CUOTA_FINAN_DEU, Accounts.C_FINAN_DEU_POR_COBRAR, Transactions.CAUSA_FINAN_FEE, cons.id, client.id, empId, capital, 0, "cau", label);
                BillTransaction.insert(transCapi, insertTransPs);

                //pago anticipado
                BigDecimal capitalAntic = min(anticBalance, capital);
                if (capitalAntic.compareTo(BigDecimal.ZERO) > 0) {
                    BillTransaction.insert(createTransaction(Accounts.C_ANTICIP, Accounts.C_CUOTA_FINAN_DEU, Transactions.PAGO_ANTICIP, cons.id, client.id, empId, capitalAntic, 0, "cau"), insertTransPs);
                    anticBalance = anticBalance.subtract(capitalAntic);
                }

                //INTERESES DE FINANCIACIÓN
                BillTransaction transFinanFeeInt = createTransaction(Accounts.C_CUOTA_INT_CRE, Accounts.E_INTER, Transactions.CAUSA_FINAN_FEE, cons.id, client.id, empId, inter, 0, "cau");
                BillTransaction.insert(transFinanFeeInt, insertTransPs);
                //pago anticipado
                BigDecimal inteAnti = min(anticBalance, inter);
                if (inteAnti.compareTo(BigDecimal.ZERO) > 0) {
                    BillTransaction.insert(createTransaction(Accounts.C_ANTICIP, Accounts.C_CUOTA_INT_CRE, Transactions.PAGO_ANTICIP, cons.id, client.id, empId, inteAnti, 0, "cau"), insertTransPs);
                    anticBalance = anticBalance.subtract(inteAnti);
                }
            }

            //SERVICIOS X USUARIO 
            userUsuQ.setParameter(1, client.id);
            Object[][] usuServs = userUsuQ.getRecords();
            for (Object[] usuServ : usuServs) {
                BigDecimal servUsuCost = MySQLQuery.getAsBigDecimal(usuServ[0], true);
                BigDecimal servUsuInte = MySQLQuery.getAsBigDecimal(usuServ[1], true);
                String srvType = MySQLQuery.getAsString(usuServ[2]);
                String extra = usuServ[3].toString();
                String transType = usuServ[4].toString();

                int account;
                switch (srvType) {
                    case "glp":
                        account = Accounts.C_CUOTA_SER_CLI_GLP;
                        break;
                    case "srv":
                        account = Accounts.C_CUOTA_SER_CLI_SRV;
                        break;
                    default:
                        throw new Exception("Tipo de interés no reconocido: " + srvType);
                }

                //CUOTA A CAPITAL
                int serUsuCapiTransType;
                int serUsuInteTransType;

                if (inst.isNetInstance()) {
                    switch (transType) {
                        case "conn":
                            serUsuCapiTransType = Transactions.CAUSA_SERV_CONN;
                            serUsuInteTransType = Transactions.CAUSA_INTE_CRE_CONN;
                            break;
                        case "susp_reconn":
                            serUsuCapiTransType = Transactions.CAUSA_SERV_SUSP_RECONN;
                            serUsuInteTransType = Transactions.CAUSA_INTE_CRE_SUSP_RECONN;
                            break;
                        case "cut_reconn":
                            serUsuCapiTransType = Transactions.CAUSA_SERV_CUT_RECONN;
                            serUsuInteTransType = Transactions.CAUSA_INTE_CRE_CUT_RECONN;
                            break;
                        case "check":
                            serUsuCapiTransType = Transactions.CAUSA_SERV_CHECK;
                            serUsuInteTransType = Transactions.CAUSA_INTE_CRE_CHECK;
                            break;
                        case "other":
                            serUsuCapiTransType = Transactions.CAUSA_SERV_OTHER;
                            serUsuInteTransType = Transactions.CAUSA_INTE_CRE_OTHER;
                            break;
                        default:
                            throw new RuntimeException();
                    }
                } else {
                    serUsuCapiTransType = Transactions.CAUSA_SERV_OTHER;
                    serUsuInteTransType = Transactions.CAUSA_INTE_CRE_OTHER;
                }

                BillTransaction transSerUsu = createTransaction(account, Accounts.E_ING_OP, serUsuCapiTransType, cons.id, client.id, empId, servUsuCost, 0, "cau", extra);
                BillTransaction.insert(transSerUsu, insertTransPs);
                //pago anticipado
                BigDecimal servUsuCapiAntic = min(anticBalance, servUsuCost);
                if (servUsuCapiAntic.compareTo(BigDecimal.ZERO) > 0) {
                    BillTransaction.insert(createTransaction(Accounts.C_ANTICIP, account, Transactions.PAGO_ANTICIP, cons.id, client.id, empId, servUsuCapiAntic, 0, "cau"), insertTransPs);
                    anticBalance = anticBalance.subtract(servUsuCapiAntic);
                }

                //INTERESES DE FINANCIACIÓN
                BillTransaction transSerUsuInt = createTransaction(Accounts.C_CUOTA_INT_CRE, Accounts.E_INTER, serUsuInteTransType, cons.id, client.id, empId, servUsuInte, 0, "cau");
                BillTransaction.insert(transSerUsuInt, insertTransPs);
                //pago anticipado
                BigDecimal servUsuInteAntic = min(anticBalance, servUsuInte);
                if (servUsuInteAntic.compareTo(BigDecimal.ZERO) > 0) {
                    BillTransaction.insert(createTransaction(Accounts.C_ANTICIP, Accounts.C_CUOTA_INT_CRE, Transactions.PAGO_ANTICIP, cons.id, client.id, empId, servUsuInteAntic, 0, "cau"), insertTransPs);
                    anticBalance = anticBalance.subtract(servUsuInteAntic);
                }
            }

            if (inst.isTankInstance()) {
                //SERVICIOS X EDIFICIO
                servEdiQ.setParameter(1, client.buildingId);
                Object[][] buildServs = servEdiQ.getRecords();
                if (buildServs.length > 0) {
                    userEdiQ.setParameter(1, client.buildingId);
                    int buildUsuCont = userEdiQ.getAsInteger();
                    for (Object[] buildServ : buildServs) {
                        BigDecimal servTotal = MySQLQuery.getAsBigDecimal(buildServ[0], true);
                        BigDecimal servPayments = MySQLQuery.getAsBigDecimal(buildServ[1], true);
                        String extra = buildServ[2].toString();
                        BigDecimal servCost = servTotal.divide(servPayments, RoundingMode.HALF_EVEN).divide(new BigDecimal(buildUsuCont), RoundingMode.HALF_EVEN).setScale(2, RoundingMode.HALF_EVEN);
                        BillTransaction transSerEdi = createTransaction(Accounts.C_CUOTA_SER_EDI, Accounts.E_ING_OP, Transactions.CAUSA, cons.id, client.id, empId, servCost, 0, "cau", extra);
                        BillTransaction.insert(transSerEdi, insertTransPs);
                        //pago anticipado
                        BigDecimal servCostAntic = min(anticBalance, servCost);
                        if (servCostAntic.compareTo(BigDecimal.ZERO) > 0) {
                            BillTransaction.insert(createTransaction(Accounts.C_ANTICIP, Accounts.C_CUOTA_SER_EDI, Transactions.PAGO_ANTICIP, cons.id, client.id, empId, servCostAntic, 0, "cau"), insertTransPs);
                            anticBalance = anticBalance.subtract(servCostAntic);
                        }
                    }
                }
            }

            //RECONEXIONES
            reconQ.setParameter(1, client.id);
            Boolean recon = reconQ.getAsBoolean();
            if (recon != null && recon) {
                reconUpdateQ.setParameter(1, client.id);
                reconUpdateQ.setParameter(2, cons.id);
                reconUpdateQ.addBatch();
                if (!client.skipReconnect) {
                    BillTransaction transRecon = createTransaction(Accounts.C_RECON, Accounts.E_ING_OP, Transactions.CAUSA, cons.id, client.id, empId, cons.reconnect, 0, "cau");
                    BillTransaction.insert(transRecon, insertTransPs);
                    //pago anticipado
                    BigDecimal reconCostAntic = min(anticBalance, cons.reconnect);
                    if (reconCostAntic.compareTo(BigDecimal.ZERO) > 0) {
                        BillTransaction.insert(createTransaction(Accounts.C_ANTICIP, Accounts.C_RECON, Transactions.PAGO_ANTICIP, cons.id, client.id, empId, reconCostAntic, 0, "cau"), insertTransPs);
                        /*
                                se comenta porque desde este punto en adelante no se vuelve a usar la variable
                                pero si luego se aumentan rubros que puedan descontar del pago anticipado se deberá descomentar,
                                por eso no se borra.
                         */
                        //anticBalance = anticBalance.subtract(reconCostAntic);
                    }
                }
            }
        }
        reconUpdateQ.executeBatch();
        insertTransPs.executeBatch();
        // em.commit();

        //verificación de la suma de lo causado por consumo
        BigDecimal realConsum;
        if (clientId != null) {
            realConsum = new MySQLPreparedQuery("SELECT SUM(value) FROM bill_transaction t WHERE (t.account_deb_id = " + Accounts.C_CONS + " OR t.account_deb_id = " + Accounts.C_CONS_SUBS + ") AND t.bill_span_id = " + cons.id + " AND t.cli_tank_id = " + clientId, billConn).getAsBigDecimal(true);
        } else {
            realConsum = new MySQLPreparedQuery("SELECT SUM(value) FROM bill_transaction t WHERE (t.account_deb_id = " + Accounts.C_CONS + " OR t.account_deb_id = " + Accounts.C_CONS_SUBS + ") AND t.bill_span_id = " + cons.id, billConn).getAsBigDecimal(true);
        }

        if (realConsum.compareTo(totalConsum) != 0) {
            throw new Exception("Error, los consumos causados no coinciden, esperada = " + totalConsum + ", encontrada = " + realConsum);
        }
    }

    private static BigDecimal min(BigDecimal a, BigDecimal b) {
        if (a.compareTo(b) < 0) {
            return a;
        }
        return b;
    }

    //balance total de la cuenta de un usuario en un periodo, para cuentas que van a cartera.
    private static BigDecimal getSpanBalance(MySQLPreparedQuery debQ, MySQLPreparedQuery credQ, int accountId, int clientId, int spanId) throws Exception {
        debQ.setParameter(1, accountId);
        debQ.setParameter(2, clientId);
        debQ.setParameter(3, spanId);

        credQ.setParameter(1, accountId);
        credQ.setParameter(2, clientId);
        credQ.setParameter(3, spanId);
        return debQ.getAsBigDecimal(true).subtract(credQ.getAsBigDecimal(true));
    }

    //crea un nuevo periodo de facturación con base en el periodo de consumo actual
    private static BillSpan createNewSpan(BillSpan cons, BillSpan reca, BillInstance inst, Connection conn) throws Exception {

        BillSpan newSpan = new BillSpan().select(cons.id, conn);
        newSpan.interes = reca.interes;
        newSpan.interesSrv = reca.interesSrv;
        newSpan.covidEmergency = reca.covidEmergency;

        GregorianCalendar end = new GregorianCalendar();
        end.setTime(newSpan.endDate);
        end.add(GregorianCalendar.DAY_OF_MONTH, 1);
        newSpan.beginDate = end.getTime();

        end.setTime(newSpan.endDate);
        end.add(GregorianCalendar.MONTH, 1);
        newSpan.endDate = end.getTime();

        //VALIDADO PARA EL 29 DE FEBRERO
        end.setTime(newSpan.limitDate);
        int day = end.get(GregorianCalendar.DAY_OF_MONTH);
        end.set(GregorianCalendar.DAY_OF_MONTH, 1);
        end.add(GregorianCalendar.MONTH, 1);
        int mDay = end.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
        end.set(GregorianCalendar.DAY_OF_MONTH, Math.min(mDay, day));
        newSpan.limitDate = end.getTime();

        if (inst.isNetInstance()) {
            end.setTime(newSpan.suspDate);
            day = end.get(GregorianCalendar.DAY_OF_MONTH);
            end.set(GregorianCalendar.DAY_OF_MONTH, 1);
            end.add(GregorianCalendar.MONTH, 1);
            mDay = end.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
            end.set(GregorianCalendar.DAY_OF_MONTH, Math.min(mDay, day));
            newSpan.suspDate = end.getTime();            
            
            newSpan.pms = null;
            newSpan.cglp = null;
            newSpan.t = null;
            newSpan.tv = null;
            
            newSpan.subPerc1 = null;
            newSpan.subPerc2 = null;
            newSpan.finalTarif1 = null;
            newSpan.finalTarif2 = null;
            newSpan.rawTarif1 = null;
            newSpan.rawTarif2 = null;
            newSpan.cEq1 = null;
            newSpan.cEq2 = null;
            newSpan.cuvR = null;
            newSpan.cuvNr = null;
            newSpan.cuf = null;
            
        }

        BillSpan.setConsMonth(newSpan);

        newSpan.oldCodPer = 0;
        newSpan.state = "cons";
        newSpan.readingsClosed = false;
        newSpan.costsSet = false;
        newSpan.paramsDone = false;
        newSpan.id = cons.id + 1;
        newSpan.insertWithId(conn);
        return newSpan;
    }

    private static BillTransaction createTransaction(int ctaDeb, int ctaCred, int transTypeId, int spanId, int clientId, int userId, BigDecimal value, Integer docId, String docType) throws Exception {
        return createTransaction(ctaDeb, ctaCred, transTypeId, spanId, clientId, userId, value, docId, docType, null);
    }

    private static BillTransaction createTransaction(int ctaDeb, int ctaCred, int transTypeId, int spanId, int clientId, int userId, BigDecimal value, Integer docId, String docType, String extra) throws Exception {
        BillTransaction trans = new BillTransaction();
        trans.accountCredId = ctaCred;
        trans.accountDebId = ctaDeb;
        trans.billSpanId = spanId;
        trans.cliTankId = clientId;
        trans.creUsuId = userId;
        trans.created = new Date();
        trans.modified = trans.created;
        trans.modUsuId = userId;
        trans.transTypeId = transTypeId;
        trans.value = value;
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new Exception("El valor de la transacción es negativo");
        }
        trans.docId = docId;
        trans.docType = docType;
        trans.extra = extra;
        return trans;
    }

}
