package utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQLPreparedUpdate extends MySQLParametrizable {

    private final PreparedStatement ps;
    private int batches = 0;
    private int lastBatches = 0;

    public MySQLPreparedUpdate(String query, Connection conn) throws SQLException {
        super(query);
        ps = conn.prepareStatement(getNormalizeQuery(), PreparedStatement.RETURN_GENERATED_KEYS);
    }

    public void addBatch() throws Exception {
        batches++;
        ps.addBatch();
    }

    public void executeBatch() throws SQLException {
        long t = System.currentTimeMillis();
        ps.executeBatch();
        totalTime = System.currentTimeMillis() - t;
        lastBatches = batches;
        batches = 0;
    }

    @Override
    public PreparedStatement getPs() {
        return ps;
    }

    private long totalTime = 0;

    public void printStats(String desc) {
        System.out.println("\t" + desc + "\tTotal Time\t" + totalTime + "\tQueries\t" + lastBatches + "\tAvg Time\t" + (lastBatches > 0 ? (totalTime / (double) lastBatches) : 0));
    }
}
