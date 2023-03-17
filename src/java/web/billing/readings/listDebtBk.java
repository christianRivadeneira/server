package web.billing.readings;

import controller.billing.BillClientTankController;
import controller.billing.BillReportController;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
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
import utilities.MySQLPreparedQuery;
import utilities.MySQLQuery;
import web.billing.BillingServlet;

@WebServlet(name = "/readings/listDebtBk", urlPatterns = {"/readings/listDebtBk"})
public class listDebtBk extends BillingServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Connection conn = null;
        try (Connection cityConn = getConnection(Integer.valueOf(request.getParameter("cityId")))) {
            conn = MySQLCommon.getConnection("sigmads", null);
            SessionLogin.validate(request.getParameter("sessionId"));
            long t = System.currentTimeMillis();
            String[] clieIds = request.getParameter("clieIds").split(",");

            System.out.println("Inicio " + (System.currentTimeMillis() - t));
            try (GZIPOutputStream goz = new GZIPOutputStream(response.getOutputStream()); OutputStreamWriter osw = new OutputStreamWriter(goz, "UTF8"); PrintWriter w = new PrintWriter(osw, true)) {
                w.write(String.valueOf(clieIds.length));
                for (int i = 0; i < clieIds.length; i++) {
                    MySQLQuery credQ = new MySQLQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_cred_id = ?1 AND t.cli_tank_id = ?2");
                    MySQLQuery debQ = new MySQLQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_deb_id = ?1 AND t.cli_tank_id = ?2");
                    MySQLPreparedQuery debtsQ = BillClientTankController.getCartBySpanQuery(Accounts.C_CAR_GLP, cityConn);

                    int clientId = Integer.valueOf(clieIds[i]);
                    debQ.setParam(2, clientId);
                    credQ.setParam(2, clientId);

                    debQ.setParam(1, Accounts.C_CAR_GLP);
                    credQ.setParam(1, Accounts.C_CAR_GLP);
                    BigDecimal sigmaDebt = debQ.getAsBigDecimal(cityConn, true).subtract(credQ.getAsBigDecimal(cityConn, true));

                    debQ.setParam(1, Accounts.C_CAR_OLD);
                    credQ.setParam(1, Accounts.C_CAR_OLD);
                    BigDecimal oldDebt = debQ.getAsBigDecimal(cityConn, true).subtract(credQ.getAsBigDecimal(cityConn, true));

                    debQ.setParam(1, Accounts.C_INT_OLD);
                    credQ.setParam(1, Accounts.C_INT_OLD);
                    oldDebt = oldDebt.add(debQ.getAsBigDecimal(cityConn, true).subtract(credQ.getAsBigDecimal(cityConn, true)));

                    int months = 0;
                    if (oldDebt.compareTo(BigDecimal.ZERO) > 0) {
                        months++;
                    }

                    if (((BigDecimal) sigmaDebt).compareTo(BigDecimal.ZERO) > 0) {
                        months += BillReportController.getDebtClient(clientId, debtsQ, sigmaDebt)[7].intValue();
                    }

                    response.setContentType("application/octet-stream");
                    w.write(13);
                    w.write(10);
                    System.out.println("clientId " + clientId);
                    w.write(String.valueOf(clientId));//cliente
                    w.write(9);
                    System.out.println("months " + months);
                    w.write(String.valueOf(months));//meses
                }
            }

            System.out.println("Fin " + (System.currentTimeMillis() - t));
        } catch (Exception ex) {
            Logger.getLogger(listDebtBk.class.getName()).log(Level.SEVERE, null, ex);
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
}
