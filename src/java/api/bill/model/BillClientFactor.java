package api.bill.model;

import api.BaseModel;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import utilities.MySQLPreparedQuery;
import utilities.MySQLQuery;

public class BillClientFactor extends BaseModel<BillClientFactor> {

//inicio zona de reemplazo
    public int clientId;
    public BigDecimal factor;
    public int billSpanId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "client_id",
            "factor",
            "bill_span_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, clientId);
        q.setParam(2, factor);
        q.setParam(3, billSpanId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        clientId = MySQLQuery.getAsInteger(row[0]);
        factor = MySQLQuery.getAsBigDecimal(row[1], false);
        billSpanId = MySQLQuery.getAsInteger(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_client_factor";
    }

    public static String getSelFlds(String alias) {
        return new BillClientFactor().getSelFldsForAlias(alias);
    }

    public static List<BillClientFactor> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillClientFactor().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillClientFactor().deleteById(id, conn);
    }

    public static List<BillClientFactor> getAll(Connection conn) throws Exception {
        return new BillClientFactor().getAllList(conn);
    }

//fin zona de reemplazo
    public static String FACTOR_QUERY = "SELECT b.factor "
            + "FROM bill_client_factor AS b where b.bill_span_id <= ?1 AND b.client_id = ?2 "
            + "ORDER BY b.bill_span_id DESC LIMIT 1";

    public static MySQLPreparedQuery getFactorQuery(Connection conn) throws SQLException {
        return new MySQLPreparedQuery(FACTOR_QUERY, conn);
    }

    public static BigDecimal getFactor(int spanId, int buildId, MySQLPreparedQuery factorQ) throws Exception {
        factorQ.setParameter(1, spanId);
        factorQ.setParameter(2, buildId);
        BigDecimal factor = factorQ.getAsBigDecimal(false);
        return factor == null ? BigDecimal.ZERO : factor;
    }
}
