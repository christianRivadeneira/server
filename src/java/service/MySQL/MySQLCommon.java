package service.MySQL;

import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.sql.DataSource;
import web.ShortException;

public class MySQLCommon {

    public static Connection getDefaultConnection() throws Exception {
        return getConnection("sigmads", null);
    }

    public static Connection getConnection(String poolName, String tz) throws Exception {
        DataSource ds;
        try {
            ds = (DataSource) new InitialContext().lookup(poolName);
        } catch (NameNotFoundException ex) {
            throw new ShortException("pool " + poolName + " not found");
        } catch (javax.naming.NamingException ex) {
            throw new ShortException("pool " + poolName + " not found");
        }
        MysqlDataSource mysqlDs = ds.unwrap(MysqlDataSource.class);
        mysqlDs.setRewriteBatchedStatements(true);
        Connection con = mysqlDs.getConnection();
        con.setAutoCommit(true);

        //Connection con = ((DataSource) new InitialContext().lookup(poolName)).getConnection();
        setTz(con, poolName, tz);
        setLocale(con);
        MySQLConnection mCon = con.unwrap(MySQLConnection.class);
        mCon.setRewriteBatchedStatements(true);
        return con;
    }

    public static void setLocale(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeQuery("SET lc_time_names = 'es_CO';");
        }
    }

    public static void setTz(Connection conn, String poolName, String tz) throws SQLException {
        //nada de MG solo para Qualisys
        if (!poolName.equals("sigmads") && !poolName.startsWith("billing_") && tz != null) {
            try (Statement st = conn.createStatement()) {
                if (tz.toLowerCase().startsWith("gmt") || tz.toLowerCase().startsWith("utc")) {
                    tz = tz.substring(3);
                }
                st.executeQuery("SET time_zone = '" + tz + "';");
            }
        }
    }

    public static void closeConnection(Connection con, Statement st) {
        if (st != null) {
            try {
                st.close();
            } catch (Exception ex) {
            }
        }

        if (con != null) {
            try {
                con.close();
            } catch (Exception ex) {
            }
        }
    }

    public static void closeConnection(Connection con) {
        closeConnection(con, null);
    }
}
