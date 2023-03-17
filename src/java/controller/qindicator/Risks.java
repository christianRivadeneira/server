package controller.qindicator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import web.billing.BillingServlet;

public class Risks extends BillingServlet {

    private static Integer getProcIdByIndicator(Integer indicatorId) {
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            con = ((DataSource) new InitialContext().lookup("sigmads")).getConnection();
            st = con.createStatement();
            rs = st.executeQuery("SELECT c.proc_id FROM cal_indicator AS c WHERE c.id = " + indicatorId);
            if (rs.next()) {
                return MySQLQuery.getAsInteger(rs.getObject(1));
            } else {
                return -1;
            }
        } catch (NamingException | SQLException ex) {
            Logger.getLogger(Risks.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        } finally {
            MySQLCommon.closeConnection(con, st);
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    public static double getTotalRisks(Date begDate, Date endDate, Integer indicatorId) {
        Connection con = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            con = ((DataSource) new InitialContext().lookup("sigmads")).getConnection();
            st = con.prepareStatement("SELECT COUNT(*) "
                    + "FROM rsk_eval AS re "
                    + "INNER JOIN rsk_risk AS rr ON rr.id = re.risk_id  "
                    + "WHERE "
                    + "rr.cal_proc_id = " + getProcIdByIndicator(indicatorId) + " "
                    + "AND DATE(re.eval_date) BETWEEN ? AND ?");
            st.setObject(1, begDate);
            st.setObject(2, endDate);
            rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return -1;
            }
        } catch (NamingException | SQLException ex) {
            Logger.getLogger(Risks.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                }
            }
            MySQLCommon.closeConnection(con, st);
        }
    }

    public static double getRiskTolerables(Date begDate, Date endDate, Integer indicatorId) {
        Connection con = null;
        PreparedStatement st = null;
        Statement sta = null;
        ResultSet rst = null;
        try {
            con = ((DataSource) new InitialContext().lookup("sigmads")).getConnection();
            sta = con.createStatement();
            st = con.prepareStatement("SELECT COUNT(*) "
                    + "FROM rsk_eval AS re "
                    + "INNER JOIN rsk_risk AS rr ON rr.id = re.risk_id "
                    + "WHERE "
                    + "rr.cal_proc_id = " + getProcIdByIndicator(indicatorId) + " "
                    + "AND re.miss_action = FALSE "
                    + "AND DATE(re.eval_date) BETWEEN ? AND ?");
            st.setObject(1, begDate);
            st.setObject(2, endDate);
            ResultSet rs = st.executeQuery();
            int countTolerables = -1;
            if (rs.next()) {
                countTolerables = rs.getInt(1);
            }
            return countTolerables;
        } catch (Exception ex) {
            Logger.getLogger(Risks.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        } finally {
            if (rst != null) {
                try {
                    rst.close();
                } catch (SQLException ex) {
                }
            }
            if (sta != null) {
                try {
                    sta.close();
                } catch (SQLException ex) {
                }
            }
            MySQLCommon.closeConnection(con, st);
        }
    }

}
