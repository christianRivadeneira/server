package controller.billing;

import api.bill.model.BillBill;
import controller.billing.BillImportAsoc2001.Asob2001;
import controller.billing.BillImportAsoc2001.BillInfo;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Connection;
import model.billing.BillBank;
import model.billing.constants.Accounts;
import model.system.SessionLogin;
import web.billing.BillingServlet;
import utilities.MySQLPreparedUpdate;
import utilities.MySQLQuery;

public class BillChangeBankAsob2001 {

    static class BillInfoFinder {

        private final List<Integer> instanceIds = new ArrayList<>();
        private final Map<Integer, Integer> recaSpanIds = new HashMap<>();
        private final Connection sigmaConn;

        public BillInfoFinder(Connection sigmaConn) throws Exception {
            this.sigmaConn = sigmaConn;

            Object[][] records = new MySQLQuery("SELECT id FROM sigma.bill_instance;").getRecords(sigmaConn);
            for (Object[] record : records) {
                instanceIds.add(MySQLQuery.getAsInteger(record[0]));
            }

            for (int i = 0; i < instanceIds.size(); i++) {
                Integer cId = instanceIds.get(i);
                Integer recaId = new MySQLQuery("SELECT s.id FROM " + BillingServlet.getDbName(cId) + ".bill_span s WHERE s.state = 'reca';").getAsInteger(sigmaConn);
                recaSpanIds.put(cId, recaId);
            }
        }

        public BillInfo getBillInfo(String ref, BigDecimal val) throws Exception {
            int instLen = Integer.valueOf(ref.substring(0, 1));
            boolean user = false;
            if (instLen >= 6) {
                user = true;
                instLen -= 5;
            }
            if (instLen < 1 || instLen >= ref.length()) {
                throw new Exception("La longitud de la ciudad en inválida. Cupón: " + ref);
            }

            Integer instanceId = Integer.valueOf(ref.substring(1, instLen + 1));
            Integer id = Integer.valueOf(ref.substring(instLen + 1));
            Integer billId;

            if (instanceIds.contains(instanceId)) {
                if (user) {
                    billId = new MySQLQuery("SELECT b.id FROM " + BillingServlet.getDbName(instanceId) + ".bill_bill b WHERE b.active = 1 AND b.client_tank_id = " + id + " AND b.bill_span_id = " + recaSpanIds.get(instanceId) + " AND b.total").getAsInteger(sigmaConn);
                } else {
                    billId = id;
                }
                BigDecimal total = new MySQLQuery("SELECT SUM(p.value) FROM " + BillingServlet.getDbName(instanceId) + ".bill_plan p WHERE p.account_deb_id = " + Accounts.BANCOS + " AND p.doc_id = " + billId + " AND p.doc_type = 'fac'").getAsBigDecimal(sigmaConn, true);
                Object[] billRow = new MySQLQuery("SELECT b.bill_span_id, b.payment_date, b.client_tank_id, b.active FROM " + BillingServlet.getDbName(instanceId) + ".bill_bill b WHERE b.id = " + billId + ";").getRecord(sigmaConn);
                 if (total != val) {
                            
                              throw new Exception("El valor de la factura no coincide " + val);
                            
                        }else {
                            return new BillInfo(instanceId, billId);
                        }
                    } else {
                        String instNum = new MySQLQuery("SELECT num_install FROM " + BillingServlet.getDbName(instanceId) + ".bill_client_tank WHERE id = " + clientId).getAsString(sigmaConn);
                        throw new Exception("El cupón " + ref + " del cliente " + instNum + "  no tiene el valor correcto");
                    }
                }

            } else {
                throw new Exception("Cupón " + ref + ": " + instanceId + " no es un código de ciudad válido.");
            }
        }
    }

    public static String changeBank2001(File f, String asocData) throws Exception {
        StringBuilder sb = new StringBuilder("<font face = 'tahoma' size = 3>");
        Asob2001 asoc = Asob2001.readFile(f);

        try (Connection sigmaConn = BillingServlet.getConnection()) {
            BillInfoFinder bif = new BillInfoFinder(sigmaConn);
            Map<Integer, List<Integer>> cInfo = new HashMap<>();
            for (int i = 0; i < asoc.getRefs().size(); i++) {
                String ref = asoc.getRefs().get(i);
                BigDecimal val = asoc.getValues().get(i);
                try {
                    BillInfo bi = bif.getBillInfo(ref, val);
                    int cityId = bi.instanceId;
                    if (!cInfo.containsKey(cityId)) {
                        cInfo.put(cityId, new ArrayList<Integer>());
                    }
                    cInfo.get(cityId).add(bi.billId);
                } catch (Exception ex) {
                    sb.append("<font color = '#FF0000'> ").append(ex.getMessage()).append("</font><br/>");
                }
            }

            Integer[] cities = cInfo.keySet().toArray(new Integer[0]);
            for (Integer cityId : cities) {
                List<Integer> lBi = cInfo.get(cityId);
                try (Connection conn = BillingServlet.getConnection(cityId)) {
                    conn.setAutoCommit(false);
                    MySQLPreparedUpdate ps = BillBill.getUpdateQuery(conn);
                    sb.append("Ciudad ").append(BillingServlet.getInstName(cityId)).append("<br/>");

                    Object[] obj = asocData.split(",");
                    int code = MySQLQuery.getAsInteger(obj[0]);
                    int type = MySQLQuery.getAsInteger(obj[1]);
                    String account = MySQLQuery.getAsString(obj[2]);
                    BillBank bank = BillBank.getByAsobData(code, type, account, conn);
                    if (bank == null) {
                        throw new Exception("No se encuentra el banco con cuenta " + account + " de tipo " + type + " y código " + code);
                    }

                    sb.append("Se cambiaron ").append(lBi.size()).append(" registros al banco ").append(bank.getName()).append(" ").append(bank.getNumAccount()).append("\n");

                    for (int j = 0; j < lBi.size(); j++) {
                        BillBill bill = BillBill.getById(lBi.get(j), conn);
                        if (bill != null && (bill.paymentDate != null && bill.active)) {
                            bill.bankId = bank.getId();
                            BillBill.update(bill, ps);
                        }
                    }
                    ps.executeBatch();
                    conn.commit();
                }
            }
        }
        return sb.toString();
    }

}
