package api.bill.rpt.fssri;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import model.billing.constants.Accounts;
import utilities.MySQLQuery;
import utilities.cast;

public class ContribRecNoRec {

    public Map<String, BigDecimal> noRec = new HashMap<>();
    public Map<String, BigDecimal> rec = new HashMap<>();

    public List<Contrib> noRecDetail = new ArrayList<>();
    public List<Contrib> recDetail = new ArrayList<>();

    public ContribRecNoRec() {
        zeroFill(noRec);
        zeroFill(rec);
    }

    private static void zeroFill(Map<String, BigDecimal> m) {
        m.put("5", BigDecimal.ZERO);
        m.put("6", BigDecimal.ZERO);
        m.put("c", BigDecimal.ZERO);
        m.put("i", BigDecimal.ZERO);
    }

    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.jdbc.Driver").newInstance();

        try (Connection instConn = DriverManager.getConnection("jdbc:mysql://localhost:54545/billing_capacitacion_pruebas?user=root&password=root")) {
            int spanId = 9;
            ContribRecNoRec calc = calc(spanId, instConn);
            Map<String, BigDecimal> noRec = calc.noRec;
            Iterator<Map.Entry<String, BigDecimal>> it = noRec.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, BigDecimal> e = it.next();
                System.out.println(e.getKey() + " " + e.getValue());
            }

        }
    }

    public static ContribRecNoRec calc(int spanId, Connection conn) throws Exception {

        ContribRecNoRec rta = new ContribRecNoRec();

        Object[][] clieData = new MySQLQuery("SELECT DISTINCT cli_tank_id FROM bill_transaction WHERE account_deb_id = ?1 OR account_cred_id = ?1").setParam(1, Accounts.C_CAR_CONTRIB).getRecords(conn);

        for (Object[] clieRow : clieData) {
            List<Debt> debts = new ArrayList<>();
            List<Pay> pays = new ArrayList<>();
            int clieId = cast.asInt(clieRow, 0);
            Object[][] debtData = new MySQLQuery("SELECT bill_span_id, SUM(value) FROM bill_transaction WHERE bill_span_id <= ?3 AND account_deb_id = ?1 AND cli_tank_id = ?2 GROUP BY bill_span_id ORDER BY bill_span_id ASC").setParam(1, Accounts.C_CAR_CONTRIB).setParam(2, clieId).setParam(3, spanId).getRecords(conn);
            for (Object[] debtRow : debtData) {
                debts.add(new Debt(debtRow));
            }
            Object[][] payData = new MySQLQuery("SELECT bill_span_id, SUM(value) FROM bill_transaction WHERE bill_span_id <= ?3 AND account_cred_id = ?1 AND cli_tank_id = ?2 GROUP BY bill_span_id ORDER BY bill_span_id ASC").setParam(1, Accounts.C_CAR_CONTRIB).setParam(2, clieId).setParam(3, spanId).getRecords(conn);
            for (Object[] payRow : payData) {
                pays.add(new Pay(payRow));
            }
            for (int j = 0; j < pays.size(); j++) {
                Pay pay = pays.get(j);
                for (int l = 0; l < debts.size(); l++) {
                    Debt debt = debts.get(l);
                    if (debt.endSpan == null) {
                        BigDecimal taken = min(pay.leftValue, debt.leftValue);
                        pay.leftValue = pay.leftValue.subtract(taken);
                        debt.leftValue = debt.leftValue.subtract(taken);
                        if (debt.leftValue.compareTo(BigDecimal.ZERO) == 0) {
                            debt.endSpan = pay.span;
                        }
                        if (pay.leftValue.compareTo(BigDecimal.ZERO) == 0) {
                            break;
                        }
                    }
                }
            }

            System.out.println("spanId " + spanId);
            for (int i = 0; i < debts.size(); i++) {
                Debt d = debts.get(i);
                System.out.println(d.startSpan + " " + d.endSpan + " " + d.leftValue);
            }

            for (int j = 0; j < debts.size(); j++) {
                Debt d = debts.get(j);
                if (d.endSpan == null && spanId - 6 == d.startSpan) {
                    String s = new MySQLQuery("SELECT if(c.sector = 'r', CAST(c.stratum AS CHAR), c.sector)\n"
                            + "FROM bill_clie_cau c WHERE c.span_id = ?1 AND c.client_id = ?2").setParam(1, d.startSpan).setParam(2, clieId).getAsString(conn);
                    rta.noRec.replace(s, rta.noRec.get(s).add(d.origValue));
                }

                if (d.endSpan != null && spanId == d.endSpan && d.endSpan - d.startSpan - 1 >= 6) {
                    String s = new MySQLQuery("SELECT if(c.sector = 'r', CAST(c.stratum AS CHAR), c.sector)\n"
                            + "FROM bill_clie_cau c WHERE c.span_id = ?1 AND c.client_id = ?2").setParam(1, d.startSpan).setParam(2, clieId).getAsString(conn);
                    rta.rec.replace(s, rta.rec.get(s).add(d.origValue));
                }
            }
        }
        return rta;
    }

    public static BigDecimal min(BigDecimal n1, BigDecimal n2) {
        if (n1.compareTo(n2) < 0) {
            return n1;
        } else {
            return n2;
        }
    }

    static class Debt {

        public int startSpan;
        public Integer endSpan;
        public BigDecimal origValue;
        public BigDecimal leftValue;

        public Debt(Object[] row) {
            this(cast.asInt(row, 0), cast.asBigDecimal(row, 1));
        }

        public Debt(int startSpan, BigDecimal value) {
            this.startSpan = startSpan;
            this.leftValue = value;
            this.origValue = value.add(BigDecimal.ZERO);
        }

    }

    static class Pay {

        public int span;
        public BigDecimal origValue;
        public BigDecimal leftValue;

        public Pay(Object[] row) {
            this(cast.asInt(row, 0), cast.asBigDecimal(row, 1));
        }

        public Pay(int span, BigDecimal value) {
            this.span = span;
            this.leftValue = value;
            this.origValue = value.add(BigDecimal.ZERO);
        }
    }

    public static class Contrib {

        public int clientId;
        public int spanid;

        public Contrib(int clientId, int spanid) {
            this.clientId = clientId;
            this.spanid = spanid;
        }

    }
}
