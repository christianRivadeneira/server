package utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySQLPreparedInsert extends MySQLParametrizable {

    private final PreparedStatement ps;
    private final boolean returnKeys;
    private int batches = 0;
    private int lastBatches = 0;

    public MySQLPreparedInsert(String query, boolean returnKeys, Connection conn) throws SQLException {
        super(query);
        this.returnKeys = returnKeys;
        if (returnKeys) {
            ps = conn.prepareStatement(getNormalizeQuery(), PreparedStatement.RETURN_GENERATED_KEYS);
        } else {
            ps = conn.prepareStatement(getNormalizeQuery());
        }
    }

    public void addBatch() throws Exception {
        ps.addBatch();
        batches++;
    }

    public void executeBatch() throws SQLException {
        if (returnKeys) {
            throw new RuntimeException("Return keys está habilitado, debe llamar a getKeys.");
        }
        long t = System.currentTimeMillis();
        ps.executeBatch();
        totalTime = System.currentTimeMillis() - t;
        lastBatches = batches;
        batches = 0;
    }

    public Integer[] executeBatchWithKeys() throws SQLException {
        if (!returnKeys) {
            throw new RuntimeException("Return keys no está habilitado, debe llamar a commit.");
        }
        List<Integer> ids = new ArrayList<>();
        long t = System.currentTimeMillis();
        ps.executeBatch();
        ResultSet rs = ps.getGeneratedKeys();
        while (rs.next()) {
            ids.add(rs.getInt(1));
        }
        totalTime = System.currentTimeMillis() - t;
        lastBatches = batches;
        batches = 0;
        return ids.toArray(new Integer[ids.size()]);
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
