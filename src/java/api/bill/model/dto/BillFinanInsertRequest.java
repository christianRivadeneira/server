package api.bill.model.dto;

import api.bill.model.BillAccBalance;
import api.bill.model.BillFinanceNote;
import java.util.List;

public class BillFinanInsertRequest {

    public BillFinanceNote note;
    public List<BillAccBalance> accs;
}
