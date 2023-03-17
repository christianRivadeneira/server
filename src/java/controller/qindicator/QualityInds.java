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

public class QualityInds {

    public static double totalIndicadores(Date begDate, Date endDate) {
        Connection con = null;
        PreparedStatement st = null;
        try {
            String q = "SELECT COUNT(*) FROM cal_ind_values v INNER JOIN cal_indicator i ON i.id = v.ind_id WHERE (v.a IS NOT NULL OR v.miss_values = 0) AND (i.qb IS NULL OR i.qb <> 'totalIndicadores') AND month BETWEEN ? AND ? ";
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
            Logger.getLogger(QualityInds.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        } finally {
            MySQLCommon.closeConnection(con, st);
        }
    }

    public static double indicadoresOk(Date begDate, Date endDate) {
        Connection con = null;
        PreparedStatement st = null;
        try {
            String q = "SELECT COUNT(*) FROM cal_ind_values v INNER JOIN cal_indicator i ON i.id = v.ind_id WHERE (i.qb IS NULL OR i.qa <> 'indicadoresOk')  AND level = 'ok' AND month BETWEEN ? AND ? ";

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
            Logger.getLogger(QualityInds.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        } finally {
            MySQLCommon.closeConnection(con, st);
        }
    }
}
