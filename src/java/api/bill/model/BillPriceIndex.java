package api.bill.model;

import api.BaseModel;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class BillPriceIndex extends BaseModel<BillPriceIndex> {
//inicio zona de reemplazo

    public BigDecimal ipp;
    public BigDecimal ipc;
    public boolean active;
    public Date month;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "ipp",
            "ipc",
            "active",
            "month"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, ipp);
        q.setParam(2, ipc);
        q.setParam(3, active);
        q.setParam(4, month);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        ipp = MySQLQuery.getAsBigDecimal(row[0], false);
        ipc = MySQLQuery.getAsBigDecimal(row[1], false);
        active = MySQLQuery.getAsBoolean(row[2]);
        month = MySQLQuery.getAsDate(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_price_index";
    }

    public static String getSelFlds(String alias) {
        return new BillPriceIndex().getSelFldsForAlias(alias);
    }

    public static List<BillPriceIndex> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillPriceIndex().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillPriceIndex().deleteById(id, conn);
    }

    public static List<BillPriceIndex> getAll(Connection conn) throws Exception {
        return new BillPriceIndex().getAllList(conn);
    }

//fin zona de reemplazo
    public static BillPriceIndex getByMonth(Date month, Connection conn) throws Exception {
        BillPriceIndex ind = new BillPriceIndex().select(new MySQLQuery("SELECT " + getSelFlds("") + " FROM bill_price_index WHERE month = ?1 AND active").setParam(1, month), conn);
        if (ind == null) {
            throw new Exception("No se han definido índices para " + new SimpleDateFormat("MMMM yyyy").format(month));
        }
        return ind;
    }
}
