package web.billing.readings;

import controller.billing.BillReportController;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.billing.constants.Accounts;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import service.MySQL.MySQLSelect;
import utilities.MySQLQuery;
import web.billing.BillingServlet;

@WebServlet(name = "/readings/listDebt", urlPatterns = {"/readings/listDebt"})
public class listDebt extends BillingServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Connection conn = null;
        try (Connection cityConn = getConnection(Integer.valueOf(request.getParameter("cityId")))) {
            conn = MySQLCommon.getConnection("sigmads", null);
            SessionLogin.validate(request.getParameter("sessionId"));

            Object[][] clieData = new MySQLQuery("SELECT "
                    + "cli.id "
                    + "FROM bill_client_tank AS cli "
                    + "WHERE active = 1 "
                    + "ORDER BY cli.id ASC").getRecords(cityConn);
            MySQLQuery credQ = new MySQLQuery("SELECT t.cli_tank_id, SUM(t.value) FROM bill_transaction as t WHERE t.account_cred_id = ?1 GROUP BY t.cli_tank_id");
            MySQLQuery debQ = new MySQLQuery("SELECT t.cli_tank_id, SUM(t.value) FROM bill_transaction as t WHERE t.account_deb_id = ?1 GROUP BY t.cli_tank_id");
            Object[][] debtsData = new MySQLQuery("SELECT t.bill_span_id, sum(t.value), t.cli_tank_id FROM bill_transaction t WHERE account_deb_id = " + Accounts.C_CAR_GLP + " group by t.bill_span_id, t.cli_tank_id ORDER BY t.cli_tank_id DESC, t.bill_span_id DESC").getRecords(cityConn);

            debQ.setParam(1, Accounts.C_CAR_GLP);
            credQ.setParam(1, Accounts.C_CAR_GLP);
            Object[][] credDataCarGlp = credQ.getRecords(cityConn);
            Object[][] debDataCarGlp = debQ.getRecords(cityConn);

            debQ.setParam(1, Accounts.C_CAR_OLD);
            credQ.setParam(1, Accounts.C_CAR_OLD);
            Object[][] credDataCarOld = credQ.getRecords(cityConn);
            Object[][] debDataCarOld = debQ.getRecords(cityConn);

            debQ.setParam(1, Accounts.C_INT_OLD);
            credQ.setParam(1, Accounts.C_INT_OLD);
            Object[][] credDataIntOld = credQ.getRecords(cityConn);
            Object[][] debDataIntOld = debQ.getRecords(cityConn);

            List<DebtInfo> lstData = new ArrayList<>();
            for (int i = 0; i < clieData.length; i++) {
                DebtInfo it = new DebtInfo();
                it.tankClieId = MySQLQuery.getAsInteger(clieData[i][0]);

                BigDecimal dq = BigDecimal.ZERO;
                for (int j = 0; j < debDataCarGlp.length; j++) {
                    if (it.tankClieId == MySQLQuery.getAsInteger(debDataCarGlp[j][0])) {
                        dq = MySQLQuery.getAsBigDecimal(debDataCarGlp[j][1], true);
                        break;
                    }
                }

                BigDecimal cq = BigDecimal.ZERO;
                for (int j = 0; j < credDataCarGlp.length; j++) {
                    if (it.tankClieId == MySQLQuery.getAsInteger(credDataCarGlp[j][0])) {
                        cq = MySQLQuery.getAsBigDecimal(credDataCarGlp[j][1], true);
                        break;
                    }
                }
                it.sigmaDebt = dq.subtract(cq);

                dq = BigDecimal.ZERO;
                for (int j = 0; j < debDataCarOld.length; j++) {
                    if (it.tankClieId == MySQLQuery.getAsInteger(debDataCarOld[j][0])) {
                        dq = MySQLQuery.getAsBigDecimal(debDataCarOld[j][1], true);
                        break;
                    }
                }

                cq = BigDecimal.ZERO;
                for (int j = 0; j < credDataCarOld.length; j++) {
                    if (it.tankClieId == MySQLQuery.getAsInteger(credDataCarOld[j][0])) {
                        cq = MySQLQuery.getAsBigDecimal(credDataCarOld[j][1], true);
                        break;
                    }
                }
                it.oldDebt = dq.subtract(cq);

                dq = BigDecimal.ZERO;
                for (int j = 0; j < debDataIntOld.length; j++) {
                    if (it.tankClieId == MySQLQuery.getAsInteger(debDataIntOld[j][0])) {
                        dq = MySQLQuery.getAsBigDecimal(debDataIntOld[j][1], true);
                        break;
                    }
                }

                cq = BigDecimal.ZERO;
                for (int j = 0; j < credDataIntOld.length; j++) {
                    if (it.tankClieId == MySQLQuery.getAsInteger(credDataIntOld[j][0])) {
                        cq = MySQLQuery.getAsBigDecimal(credDataIntOld[j][1], true);
                        break;
                    }
                }

                it.oldDebt = it.oldDebt.add(dq.subtract(cq));

                List<Object[]> lst = new ArrayList<>();
                for (int j = 0; j < debtsData.length; j++) {
                    if (it.tankClieId == MySQLQuery.getAsInteger(debtsData[j][2])) {
                        lst.add(debtsData[j]);
                    }
                }
                it.debtsData = new Object[lst.size()][];
                for (int j = 0; j < lst.size(); j++) {
                    it.debtsData[j] = lst.get(j);
                }

                it.months = 0;
                if (it.oldDebt.compareTo(BigDecimal.ZERO) > 0) {
                    it.months++;
                }

                if (((BigDecimal) it.sigmaDebt).compareTo(BigDecimal.ZERO) > 0) {
                    it.months += BillReportController.getDebtClient(it.debtsData, it.sigmaDebt)[7].intValue();
                }

                if (it.months > 0) {
                    lstData.add(it);
                }
            }

            try (GZIPOutputStream goz = new GZIPOutputStream(response.getOutputStream()); OutputStreamWriter osw = new OutputStreamWriter(goz, "UTF8"); PrintWriter w = new PrintWriter(osw, true)) {
                w.write(String.valueOf(lstData.size()));
                for (int i = 0; i < lstData.size(); i++) {
                    DebtInfo it = lstData.get(i);

                    response.setContentType("application/octet-stream");
                    w.write(13);
                    w.write(10);
                    w.write(String.valueOf(it.tankClieId));//cliente
                    w.write(9);
                    w.write(String.valueOf(it.months));//meses
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(listDebt.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500, ex.getMessage());
        } finally {
            MySQLSelect.tryClose(conn);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "";
    }

    private class DebtInfo {

        public int tankClieId;
        public BigDecimal sigmaDebt;
        public BigDecimal oldDebt;
        public int months;
        public Object[][] debtsData;

    }
}
