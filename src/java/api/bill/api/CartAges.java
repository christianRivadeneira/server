package api.bill.api;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import utilities.MySQLQuery;
import utilities.cast;

public class CartAges {

    public int startSpan;
    public Integer endSpan;
    public BigDecimal origValue;
    public BigDecimal leftValue;

    public CartAges(Object[] row) {
        this(cast.asInt(row, 0), cast.asBigDecimal(row, 1));
    }

    public CartAges(int startSpan, BigDecimal value) {
        this.startSpan = startSpan;
        this.leftValue = value;
        this.origValue = value.add(BigDecimal.ZERO);
    }

    public static List<CartAges> calc(int clieId, int spanId, String accs, Connection conn) throws Exception {

        List<CartAges> debts = new ArrayList<>();
        List<Pay> pays = new ArrayList<>();

        Object[][] debtData = new MySQLQuery("SELECT bill_span_id, SUM(value) FROM bill_transaction WHERE bill_span_id <= ?3 AND account_deb_id IN (" + accs + ") AND cli_tank_id = ?2 GROUP BY bill_span_id ORDER BY bill_span_id ASC").setParam(2, clieId).setParam(3, spanId).getRecords(conn);
        for (Object[] debtRow : debtData) {
            debts.add(new CartAges(debtRow));
        }
        Object[][] payData = new MySQLQuery("SELECT bill_span_id, SUM(value) FROM bill_transaction WHERE bill_span_id <= ?3 AND account_cred_id IN (" + accs + ") AND cli_tank_id = ?2 GROUP BY bill_span_id ORDER BY bill_span_id ASC").setParam(2, clieId).setParam(3, spanId).getRecords(conn);
        for (Object[] payRow : payData) {
            pays.add(new Pay(payRow));
        }
        for (int j = 0; j < pays.size(); j++) {
            Pay pay = pays.get(j);
            for (int l = 0; l < debts.size(); l++) {
                CartAges debt = debts.get(l);
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
        return debts;
    }

    public static BigDecimal min(BigDecimal n1, BigDecimal n2) {
        if (n1.compareTo(n2) < 0) {
            return n1;
        } else {
            return n2;
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
}
