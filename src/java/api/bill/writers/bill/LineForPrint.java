package api.bill.writers.bill;

import java.math.BigDecimal;
import java.util.List;

public class LineForPrint {

    public String label;
    public BigDecimal value;
    public boolean bold;

    public LineForPrint() {
    }

    public LineForPrint(String label, BigDecimal value, boolean bold) {
        this.label = label;
        this.value = value;
        this.bold = bold;
    }
    
    public static BigDecimal sum(List<LineForPrint> bfp) {
        BigDecimal sum = new BigDecimal(0);
        for (int i = 0; i < bfp.size(); i++) {
            sum = sum.add(bfp.get(i).value);
        }
        return sum;
    }
}
