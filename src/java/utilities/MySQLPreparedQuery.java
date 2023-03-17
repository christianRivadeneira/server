package utilities;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MySQLPreparedQuery extends MySQLParametrizable {

    private final PreparedStatement ps;

    public MySQLPreparedQuery(String query, Connection conn) throws SQLException {
        super(query);
        ps = conn.prepareStatement(getNormalizeQuery());
    }

    public Double getAsDouble() throws Exception {
        return MySQLQuery.getAsDouble(getSingleValue());
    }

    public Integer getAsInteger() throws Exception {
        return MySQLQuery.getAsInteger(getSingleValue());
    }

    public Long getAsLong() throws Exception {
        return MySQLQuery.getAsLong(getSingleValue());
    }

    public Boolean getAsBoolean() throws Exception {
        return MySQLQuery.getAsBoolean(getSingleValue());
    }

    public BigDecimal getAsBigDecimal(boolean nullAsZero) throws Exception {
        return MySQLQuery.getAsBigDecimal(getSingleValue(), nullAsZero);
    }

    public String getAsString() throws Exception {
        return MySQLQuery.getAsString(getSingleValue());
    }

    public Date getAsDate() throws Exception {
        return MySQLQuery.getAsDate(getSingleValue());
    }

    public Object getSingleValue() throws Exception {
        Object[] rec = getRecord();
        if (rec != null) {
            switch (rec.length) {
                case 0:
                    return null;
                case 1:
                    return rec[0];
                default:
                    throw new Exception("La consulta retorna más de una columna");
            }
        } else {
            return null;
        }
    }

    /**
     * Devuelve la primera fila del resultado
     *
     * @return la primera fila del resultado, o null si no hubo resultados
     * @throws Exception
     */
    public Object[] getRecord() throws Exception {
        Object[][] tab = getRecords();
        if (tab != null) {
            switch (tab.length) {
                case 0:
                    return null;
                case 1:
                    return tab[0];
                default:
                    throw new Exception("La consulta retorna más de una fila");
            }
        } else {
            return null;
        }
    }

    private int sentQueries = 0;
    private long totalTime = 0;

    public void printStats(String desc) {
        System.out.println("\t" + desc + "\tTotal Time\t" + totalTime + "\tQueries\t" + sentQueries + "\tAvg Time\t" + (sentQueries > 0 ? (totalTime / (double) sentQueries) : 0));

    }

    public Object[][] getRecords() throws Exception {
        sentQueries++;
        long t = System.currentTimeMillis();
        ResultSet rs = null;
        try {
            rs = ps.executeQuery();
            int cols = rs.getMetaData().getColumnCount();
            List<Object[]> res = new ArrayList<>();

            while (rs.next()) {
                Object[] row = new Object[cols];
                res.add(row);
                for (int i = 0; i < cols; i++) {
                    Object obj = rs.getObject(i + 1);
                    row[i] = obj;
                }
            }
            return detectEmptyResult(res.toArray(new Object[0][]));
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception ex) {
                }
            }
            totalTime += (System.currentTimeMillis() - t);
        }
    }

    public static Object[][] detectEmptyResult(Object[][] data) {
        if (data != null && data.length == 1) {
            Object[] row = data[0];
            boolean allNull = true;
            for (int i = 0; i < row.length && allNull; i++) {
                allNull = (row[i] == null);
            }
            if (allNull) {
                return new Object[0][row.length];
            } else {
                return data;
            }
        } else {
            return data;
        }
    }

    @Override
    public PreparedStatement getPs() {
        return ps;
    }

    public MySQLPreparedQuery print() {
        System.out.println(ps);
        return this;
    }
}
