package controller.billing;

import api.bill.Locking;
import api.bill.model.BillBill;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;
import model.billing.BillBank;
import model.billing.BillSusp;
import model.billing.constants.Accounts;
import utilities.Dates;
import utilities.MySQLPreparedUpdate;
import utilities.MySQLQuery;
import utilities.SysTask;
import web.billing.BillingServlet;

public class BillImportAsoc2001 {

    private static final int WAITING_EA = 1;
    private static final int WAITING_EL = 2;
    private static final int WAITING_RD_CL = 3;
    private static final int WAITING_EL_CA = 4;

    public static class RefInfo {

        public int instId;
        public int id;
        public boolean isUser;

        public static RefInfo getInfo(String ref) throws Exception {
            ref = removeLeftZeroes(ref);
            RefInfo rta = new RefInfo();
            int cityLen = Integer.valueOf(ref.substring(0, 1));
            rta.isUser = false;
            if (cityLen >= 6) {
                rta.isUser = true;
                cityLen -= 5;
            }
            if (cityLen < 1 || cityLen >= ref.length()) {
                throw new Exception("La longitud de la ciudad en inválida. Cupón: " + ref);
            }

            rta.instId = Integer.valueOf(ref.substring(1, cityLen + 1));
            rta.id = Integer.valueOf(ref.substring(cityLen + 1));
            return rta;
        }

    }

    public static class BillInfo {

        public int instanceId;
        public int billId;

        public BillInfo(int refId, int billId) {
            this.instanceId = refId;
            this.billId = billId;
        }
    }

    public static class BillInfoFinder {

        private final List<Integer> instanceIds = new ArrayList<>();
        private final Map<Integer, Integer> recaSpanIds = new HashMap<>();//no usar directamente, llamar a getRecaSpanId
        private final Connection sigmaConn;

        public BillInfoFinder(Connection sigmaConn) throws Exception {
            this.sigmaConn = sigmaConn;

            Object[][] records = new MySQLQuery("SELECT id FROM sigma.bill_instance;").getRecords(sigmaConn);
            for (Object[] record : records) {
                instanceIds.add(MySQLQuery.getAsInteger(record[0]));
            }
        }

        private int getRecaSpanId(int instId, int clientId) throws Exception {
            int recaId;
            if (recaSpanIds.containsKey(instId)) {
                recaId = recaSpanIds.get(instId);
            } else {
                recaId = new MySQLQuery("SELECT s.id FROM " + BillingServlet.getDbName(instId) + ".bill_span s WHERE s.state = 'reca';").getAsInteger(sigmaConn);
                recaSpanIds.put(instId, recaId);
            }

            if (BillingServlet.getInst(instId).siteBilling) {
                boolean closed = new MySQLQuery("SELECT span_closed FROM " + BillingServlet.getDbName(instId) + ".bill_client_tank WHERE id = ?1").setParam(1, clientId).getAsBoolean(sigmaConn);
                return closed ? recaId + 1 : recaId;
            } else {
                return recaId;
            }
        }

        public int getBillId(String ref, BigDecimal val) throws Exception {
            RefInfo refInfo = RefInfo.getInfo(ref);
            Integer billId;

            if (instanceIds.contains(refInfo.instId)) {
                if (refInfo.isUser) {
                    if (BillingServlet.getInst(refInfo.instId).isNetInstance()) {
                        //en redes el id con el que se genera el código del cliente es el del prospecto
                        //no se puede crear clientes sin que sean prospectos primero
                        refInfo.id = new MySQLQuery("SELECT id FROM bill_client_tank c WHERE c.prospect_id = ?1").setParam(1, refInfo.id).getAsInteger(sigmaConn);
                    }

                    billId = new MySQLQuery(""
                            + "SELECT b.id "
                            + "FROM " + BillingServlet.getDbName(refInfo.instId) + ".bill_bill b "
                            + "WHERE b.active = 1 "
                            + "AND b.client_tank_id = " + refInfo.id + " "
                            + "AND b.bill_span_id = " + getRecaSpanId(refInfo.instId, refInfo.id) + " "
                            + "AND b.total "
                            + "ORDER BY id ASC LIMIT 1").getAsInteger(sigmaConn);
                    if (billId == null) {
                        throw new Exception("No se encontró una factura sin pago para el cliente: " + ref);
                    }
                } else {
                    billId = refInfo.id;
                }
                return billId;
            } else {
                throw new Exception("Cupón " + ref + ": " + refInfo.instId + " no es un código de ciudad válido.");
            }
        }

        private BillInfo getBillInfo(String ref, BigDecimal val) throws Exception {
            RefInfo refInfo = RefInfo.getInfo(ref);
            int billId = getBillId(ref, val);
            BigDecimal total = new MySQLQuery("SELECT SUM(p.value) FROM " + BillingServlet.getDbName(refInfo.instId) + ".bill_plan p WHERE p.account_deb_id = " + Accounts.BANCOS + " AND p.doc_id = " + billId + " AND p.doc_type = 'fac'").getAsBigDecimal(sigmaConn, true);
            Object[] billRow = new MySQLQuery("SELECT b.bill_span_id, b.payment_date, b.client_tank_id, b.active FROM " + BillingServlet.getDbName(refInfo.instId) + ".bill_bill b WHERE b.id = " + billId + ";").getRecord(sigmaConn);
            if (billRow == null) {
                throw new Exception("La factura " + ref + " no se encontró en el sistema.");
            } else {
                int clientId = MySQLQuery.getAsInteger(billRow[2]);
                if (val.compareTo(total) == 0) {
                    if (MySQLQuery.getAsInteger(billRow[0]) != getRecaSpanId(refInfo.instId, clientId)) {
                        String instNum = new MySQLQuery("SELECT num_install FROM " + BillingServlet.getDbName(refInfo.instId) + ".bill_client_tank WHERE id = " + clientId).getAsString(sigmaConn);
                        throw new Exception("El cupón " + ref + " del cliente " + instNum + " no pertenece al periodo de recaudo actual");
                    } else if (billRow[1] != null) {
                        String instNum = new MySQLQuery("SELECT num_install FROM " + BillingServlet.getDbName(refInfo.instId) + ".bill_client_tank WHERE id = " + clientId).getAsString(sigmaConn);
                        throw new Exception("El cupón " + ref + " del cliente " + instNum + " ya está pagado");
                    } else if (!MySQLQuery.getAsBoolean(billRow[3])) {
                        String instNum = new MySQLQuery("SELECT num_install FROM " + BillingServlet.getDbName(refInfo.instId) + ".bill_client_tank WHERE id = " + clientId).getAsString(sigmaConn);
                        throw new Exception("El cupón " + ref + " del cliente " + instNum + " ya no es válido por cambios en la cuenta del cliente");
                    } else {
                        return new BillInfo(refInfo.instId, billId);
                    }
                } else {
                    String instNum = new MySQLQuery("SELECT num_install FROM " + BillingServlet.getDbName(refInfo.instId) + ".bill_client_tank WHERE id = " + clientId).getAsString(sigmaConn);
                    throw new Exception("El cupón " + ref + " del cliente " + instNum + "  no tiene el valor correcto. Vlr: " + val + " - esperado " + total);
                }
            }
        }
    }

    public static String analyseAsob2001(File f) throws Exception {

        StringBuilder sb = new StringBuilder("<font face = 'tahoma' size = 3>");
        Asob2001 asoc = Asob2001.readFile(f);

        sb.append("Recaudos del ").append(new SimpleDateFormat("EEEE',' d 'de' MMMM 'del' yyyy").format(asoc.getRecDate())).append("<br />");
        sb.append("Archivo generado el ").append(new SimpleDateFormat("EEEE',' d 'de' MMMM 'del' yyyy hh:mm a").format(asoc.getFileDate())).append("<br />");

        try (Connection sigmaConn = BillingServlet.getConnection()) {
            BillInfoFinder bif = new BillInfoFinder(sigmaConn);

            Map<Integer, Integer> iRefs = new HashMap<>();

            int ok = 0;
            for (int i = 0; i < asoc.getRefs().size(); i++) {
                String ref = asoc.getRefs().get(i);
                BigDecimal val = asoc.getValues().get(i);
                try {
                    BillInfo bi = bif.getBillInfo(ref, val);
                    int instId = bi.instanceId;
                    if (!iRefs.containsKey(instId)) {
                        iRefs.put(instId, 1);
                    } else {
                        iRefs.replace(instId, iRefs.get(instId) + 1);
                    }
                    ok++;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    sb.append("<font color = '#FF0000'> ").append(ex.getMessage()).append("</font><br/>");
                }
            }

            Integer[] cities = iRefs.keySet().toArray(new Integer[0]);
            for (Integer cityId : cities) {
                try (Connection conn = BillingServlet.getConnection(cityId)) {
                    sb.append("<br /><b>Instancia ").append(BillingServlet.getInstName(cityId)).append("</b>, se hallaron <b>").append(iRefs.get(cityId)).append("</b> registros.").append("<br />");
                    BillBank bank = BillBank.getByAsobData(asoc.ent, asoc.type, asoc.account, conn);
                    if (bank == null) {
                        throw new Exception("No se encuentra la cuenta " + asoc.getAccount() + " de tipo " + asoc.getType() + " del banco " + asoc.getEnt());
                    }
                    sb.append(bank.getName()).append(" ").append(bank.getNumAccount()).append("<br />");
                }
            }

            if (ok == asoc.getRefs().size()) {
                sb.append("<b><font color = '#0000FF'>Se importarán todos los registros encontrados</font></b><br />");
            } else if (ok > 0) {
                sb.append("<font color = '#0000FF'>Se importarán <b>").append(ok).append("</b> de los <b>").append(asoc.getRefs().size()).append("</b> registros encontrados</font><br />");
            } else {
                sb.append("<b><font color = '#FF0000'>No se importará ningún registro</font></b><br />");
            }
        }
        return sb.toString();
    }

    public static synchronized String importAsob2001(int empId, File f, String asocData) throws Exception {
        StringBuilder sb = new StringBuilder();
        Asob2001 asoc = Asob2001.readFile(f);
        try (Connection conn = BillingServlet.getConnection()) {
            SysTask t = new SysTask(BillImportAsoc2001.class, empId, conn);
            try {
                conn.setAutoCommit(false);

                BillInfoFinder bif = new BillInfoFinder(conn);

                Map<Integer, List<BigDecimal>> instVals = new HashMap<>();
                Map<Integer, List<Integer>> instBills = new HashMap<>();

                for (int i = 0; i < asoc.getRefs().size(); i++) {
                    String ref = asoc.getRefs().get(i);
                    BigDecimal val = asoc.getValues().get(i);
                    try {
                        BillInfo bi = bif.getBillInfo(ref, val);
                        int instId = bi.instanceId;
                        BillingServlet.getInstName(instId);
                        if (!instVals.containsKey(instId)) {
                            instVals.put(instId, new ArrayList<BigDecimal>());
                            instBills.put(instId, new ArrayList<Integer>());

                        }
                        if (!instBills.get(instId).contains(bi.billId)) {
                            instVals.get(instId).add(val);
                            instBills.get(instId).add(bi.billId);
                        }
                    } catch (Exception ex) {
                        sb.append("<font color = '#FF0000'>El cupón ").append(ref).append(" no pertenece a clientes de remisión.</font><br/>");
                    }
                }

                Integer[] instances = instBills.keySet().toArray(new Integer[0]);
                for (Integer instId : instances) {
                    try {
                        Locking.lock(instId);
                        List<Integer> lBi = instBills.get(instId);
                        BillingServlet.getInst(instId).useInstance(conn);
                        sb.append("Ciudad ").append(BillingServlet.getInstName(instId)).append(", se hallaron ").append(lBi.size()).append(" registros.\n");

                        BillBank bank;
                        if (asocData != null && !asocData.isEmpty()) {
                            Object[] obj = asocData.split(",");
                            int code = MySQLQuery.getAsInteger(obj[0]);
                            int type = MySQLQuery.getAsInteger(obj[1]);
                            String account = MySQLQuery.getAsString(obj[2]);
                            bank = BillBank.getByAsobData(code, type, account, conn);
                            if (bank == null) {
                                throw new Exception("No se encuentra el banco con cuenta " + account + " de tipo " + type + " y código " + code);
                            }
                        } else {
                            bank = BillBank.getByAsobData(asoc.ent, asoc.type, asoc.account, conn);
                            if (bank == null) {
                                throw new Exception("No se encuentra la cuenta " + asoc.getAccount() + " de tipo " + asoc.getType() + " del banco " + asoc.getEnt());
                            }
                        }

                        sb.append(bank.getName()).append(" ").append(bank.getNumAccount()).append("\n");

                        MySQLPreparedUpdate moveQ = new MySQLPreparedUpdate("INSERT INTO bill_transaction (cli_tank_id, bill_span_id, value, account_deb_id, account_cred_id, trans_type_id, doc_type, doc_id, cre_usu_id, mod_usu_id, created, modified, bill_bank_id) (SELECT cli_tank_id, bill_span_id, SUM(value), account_deb_id, account_cred_id, trans_type_id, doc_type, doc_id, " + empId + ", " + empId + ", NOW(), NOW(), " + bank.getId() + " FROM bill_plan p WHERE p.doc_id = ?1 AND p.doc_type = 'fac' group by account_cred_id)", conn);
                        BillBill[] bills = new BillBill[lBi.size()];
                        for (int j = 0; j < lBi.size(); j++) {
                            bills[j] = new BillBill().select(lBi.get(j), conn);
                            if (bills[j] != null && bills[j].bankId == null) {
                                moveQ.setParameter(1, bills[j].id);
                                moveQ.addBatch();
                            }
                        }
                        moveQ.executeBatch();

                        MySQLPreparedUpdate updateBillQ = BillBill.getUpdateQuery(conn);
                        MySQLPreparedUpdate stopSuspQ = BillSusp.getStopSuspQ(conn);
                        MySQLPreparedUpdate progReconQ = BillSusp.getProgReconQ(conn);

                        for (int j = 0; j < lBi.size(); j++) {
                            BillBill bill = bills[j];
                            if (bill != null) {
                                bill.lastTransId = BillTransactionController.getLastTrasactionIdByClient(bill.clientTankId, conn);
                                bill.paymentDate = (Date) asoc.getRecDate();
                                bill.registDate = new Date();
                                bill.registrarId = empId;
                                bill.bankId = bank.getId();
                                BillSusp.setParams(stopSuspQ, bill);
                                BillSusp.setParams(progReconQ, bill);
                                BillBill.update(bill, updateBillQ);
                            }
                        }
                        stopSuspQ.executeBatch();
                        progReconQ.executeBatch();
                        updateBillQ.executeBatch();
                    } finally {
                        Locking.unlock(instId);
                    }
                }
                new MySQLQuery("USE sigma").executeUpdate(conn);
                t.success(conn);
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        }
        return sb.toString();
    }

    //quita los ceros y no imprimibles del inicio de la referencia
    public static String removeLeftZeroes(String ref) {
        for (int j = 0; j < ref.length(); j++) {
            if (Character.getNumericValue(ref.charAt(j)) > 0) {
                ref = ref.substring(j);
                break;
            }
        }
        return ref.trim();
    }

    static class Asob2001 {

        public Date recDate;
        public Date fileDate;
        public String fileMod;
        public int ent;
        public String account;
        public int type;
        public List<String> refs;
        public List<BigDecimal> values;

        public Asob2001() {
            refs = new ArrayList<>();
            values = new ArrayList<>();
        }

        public Date getRecDate() {
            return recDate;
        }

        public void setRecDate(Date recDate) {
            this.recDate = recDate;
        }

        public Date getFileDate() {
            return fileDate;
        }

        public void setFileDate(Date fileDate) {
            this.fileDate = fileDate;
        }

        public String getFileMod() {
            return fileMod;
        }

        public void setFileMod(String fileMod) {
            this.fileMod = fileMod;
        }

        public int getEnt() {
            return ent;
        }

        public void setEnt(int ent) {
            this.ent = ent;
        }

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public List<String> getRefs() {
            return refs;
        }

        public void setRefs(List<String> refs) {
            this.refs = refs;
        }

        public List<BigDecimal> getValues() {
            return values;
        }

        public void setValues(List<BigDecimal> values) {
            this.values = values;
        }

        public static Asob2001 readFile(File f) throws Exception {
            int BUFFER = 10485760;
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(f)));
            BufferedReader br = new BufferedReader(new InputStreamReader(zis));
            BufferedReader entrada;
            if (zis.getNextEntry() != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(baos));
                int count;
                char[] cbuf = new char[BUFFER];
                while ((count = br.read(cbuf, 0, BUFFER)) != -1) {
                    bw.write(cbuf, 0, count);
                }
                bw.close();
                baos.close();
                entrada = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(baos.toByteArray())));
            } else {
                throw new Exception("El archivo está vacio.");
            }

            Asob2001 res = new Asob2001();

            int state = WAITING_EA;
            String lote = null;
            int bloq_reg = 0;
            int file_reg = 0;
            BigDecimal bloq_sum = BigDecimal.ZERO;
            BigDecimal file_sum = BigDecimal.ZERO;

            String reg;
            for (int i = 0; (reg = entrada.readLine()) != null; i++) {
                if (reg.length() != 162) {
                    throw new Exception("Error, fila " + i + " no es de 162 caracteres");
                }
                String type = reg.substring(0, 2);
                switch (state) {
                    case WAITING_EA:
                        if (type.equals("01")) {
                            //fecha de recaudo
                            String str_date = reg.substring(12, 20);
                            int year = Integer.valueOf(str_date.substring(0, 4));
                            int month = Integer.valueOf(str_date.substring(4, 6));
                            int day = Integer.valueOf(str_date.substring(6, 8));
                            GregorianCalendar gc = new GregorianCalendar(year, month - 1, day);
                            res.setRecDate(Dates.trimDate(gc.getTime()));

                            //fecha del archivo
                            year = Integer.valueOf(reg.substring(40, 44));
                            month = Integer.valueOf(reg.substring(44, 46));
                            day = Integer.valueOf(reg.substring(46, 48));
                            int hour = Integer.valueOf(reg.substring(48, 50));
                            int min = Integer.valueOf(reg.substring(50, 52));
                            gc = new GregorianCalendar(year, month - 1, day);
                            gc.set(GregorianCalendar.HOUR_OF_DAY, hour);
                            gc.set(GregorianCalendar.MINUTE, min);
                            gc.set(GregorianCalendar.SECOND, 0);
                            res.setFileDate(gc.getTime());
                            res.setFileMod(reg.substring(52, 53));
                            res.setEnt(Integer.valueOf(removeLeftZeroes(reg.substring(20, 23))));
                            res.setAccount(removeLeftZeroes(reg.substring(23, 40)));
                            res.setType(Integer.valueOf(removeLeftZeroes(reg.substring(53, 55))));
                            state = WAITING_EL;
                        } else {
                            throw new Exception("El archivo debe iniciar con el registo de encabezado.");
                        }
                        break;
                    case WAITING_EL:
                        if (type.equals("05")) {
                            lote = reg.substring(15, 19);
                            state = WAITING_RD_CL;
                        } else {
                            throw new Exception("Se esperaba un encabezado de lote." + type + " " + (i));
                        }
                        break;
                    case WAITING_RD_CL:
                        if (type.equals("06")) {//detalle
                            String ref = removeLeftZeroes(reg.substring(2, 50));
                            BigDecimal val = new BigDecimal(reg.substring(50, 62) + "." + reg.substring(62, 64));
                            bloq_sum = bloq_sum.add(val);
                            bloq_reg++;
                            res.getRefs().add(ref);
                            res.getValues().add(val);
                        } else if (type.equals("08")) {//control de lote
                            int total_reg = Integer.valueOf(reg.substring(2, 11));
                            if (total_reg != bloq_reg) {
                                throw new Exception("Error lote " + lote + ", se esperaban " + total_reg + " detalles y se hallaron " + bloq_reg);
                            }
                            BigDecimal total_val = new BigDecimal(reg.substring(11, 27) + "." + reg.substring(27, 29));
                            if (bloq_sum.compareTo(total_val) != 0) {
                                throw new Exception("Error lote " + lote + ", se esperaban " + total_val + " y se halló " + bloq_sum);
                            }

                            String bloq = reg.substring(29, 33);
                            if (!bloq.equals(lote)) {
                                throw new Exception("Error lote " + lote + ", se intentó cerrar el bloque " + bloq);
                            }

                            file_sum = file_sum.add(bloq_sum);
                            file_reg += bloq_reg;

                            bloq_reg = 0;
                            bloq_sum = BigDecimal.ZERO;
                            state = WAITING_EL_CA;
                        } else {
                            throw new Exception("Se esperaba un registro de detalle o un cotrol de lote.");
                        }
                        break;
                    case WAITING_EL_CA:
                        if (type.equals("05")) {
                            lote = reg.substring(15, 19);
                            state = WAITING_RD_CL;
                        } else if (type.equals("09")) {
                            int total_reg = Integer.valueOf(reg.substring(2, 11));
                            if (total_reg != file_reg) {
                                throw new Exception("Error archivo, se esperaban " + total_reg + " detalles y se hallaron " + file_reg);
                            }
                            BigDecimal total_val = new BigDecimal(reg.substring(11, 27) + "." + reg.substring(27, 29));
                            if (file_sum.compareTo(total_val) != 0) {
                                throw new Exception("Error archivo, se esperaban " + total_val + " y se halló " + file_sum);
                            }
                        }
                        break;
                }
            }
            entrada.close();
            return res;
        }
    }
}
