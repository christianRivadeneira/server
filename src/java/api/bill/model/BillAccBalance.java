package api.bill.model;

import java.math.BigDecimal;

public class BillAccBalance {

    public int accId;
    public int oppAccId;
    public boolean anticip;
    public String accName;
    public BigDecimal curBalance;
    public BigDecimal value = BigDecimal.ZERO;
}
