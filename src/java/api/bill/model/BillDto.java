package api.bill.model;

import api.BaseModel;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class BillDto extends BaseModel<BillDto> {
//inicio zona de reemplazo

    public int buildId;
    public int spanId;
    public BigDecimal amount;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "build_id",
            "span_id",
            "amount"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, buildId);
        q.setParam(2, spanId);
        q.setParam(3, amount);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        buildId = MySQLQuery.getAsInteger(row[0]);
        spanId = MySQLQuery.getAsInteger(row[1]);
        amount = MySQLQuery.getAsBigDecimal(row[2], false);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_dto";
    }

    public static String getSelFlds(String alias) {
        return new BillDto().getSelFldsForAlias(alias);
    }

    public static List<BillDto> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillDto().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillDto().deleteById(id, conn);
    }

    public static List<BillDto> getAll(Connection conn) throws Exception {
        return new BillDto().getAllList(conn);
    }

//fin zona de reemplazo
}
