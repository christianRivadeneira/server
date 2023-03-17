package controller.qindicator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import service.MySQL.MySQLCommon;
import web.billing.BillingServlet;

public class TankInds extends BillingServlet {

    public static double progDate(Date begDate, Date endDate) {
        Connection con = null;
        PreparedStatement st = null;
        try {
            String q = "SELECT "
                    + "COUNT(*) "//0
                    + "FROM "
                    + "est_mto "
                    + "WHERE "
                    + "prog_date BETWEEN ? AND ? ";
            con = MySQLCommon.getConnection("sigmads", null);
            st = con.prepareStatement(q);
            st.setObject(1, begDate);
            st.setObject(2, endDate);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                return -1;
            }
        } catch (Exception ex) {
            Logger.getLogger(TankInds.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        } finally {
            MySQLCommon.closeConnection(con, st);
        }
    }

    public static double execDate(Date begDate, Date endDate) {
        Connection con = null;
        PreparedStatement st = null;
        try {
            String q = "SELECT "
                    + "COUNT(*) "//0
                    + "FROM "
                    + "est_mto "
                    + "WHERE "
                    + "exec_date BETWEEN ? AND ?   ";
            con = ((DataSource) new InitialContext().lookup("sigmads")).getConnection();
            st = con.prepareStatement(q);
            st.setObject(1, begDate);
            st.setObject(2, endDate);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                return -1;
            }
        } catch (Exception ex) {
            Logger.getLogger(TankInds.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        } finally {
            MySQLCommon.closeConnection(con, st);
        }
    }
}
