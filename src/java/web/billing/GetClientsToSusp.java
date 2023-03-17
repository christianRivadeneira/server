package web.billing;

import api.bill.model.BillBuilding;
import api.bill.model.BillClientTank;
import api.bill.model.BillInstance;
import api.sys.model.SysCfg;
import controller.billing.BillClientTankController;
import static controller.billing.BillReportController.getDebtClient;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.billing.constants.Accounts;
import model.system.SessionLogin;
import utilities.MySQLPreparedQuery;
import utilities.MySQLQuery;
import utilities.cast;
import static web.billing.BillingServlet.getConnection;
import web.emas.SynchronizeData;

@WebServlet(name = "GetClientsToSusp", urlPatterns = {"/GetClientsToSusp"})
public class GetClientsToSusp extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObjectBuilder ob = Json.createObjectBuilder();

        Map<String, String> pars = MySQLQuery.scapedParams(request);

        int instId = cast.asInt(pars.get("cityId"));
        String sessionId = pars.get("sessionId");
        long t = System.currentTimeMillis();

        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            BillInstance inst = BillingServlet.getInst(instId);
            try (Connection conn = getConnection(instId); Connection gralConn = BillingServlet.getConnection()) {
                SessionLogin.validate(sessionId, conn, "sigma");
                int curSpan = new MySQLQuery("SELECT s.id FROM bill_span s WHERE s.state = 'reca' ").getAsInteger(conn);
                List<BillBuilding> buildings = BillBuilding.getAll(conn);

                MySQLPreparedQuery credQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_cred_id = ?1 AND t.cli_tank_id = ?2", conn);
                MySQLPreparedQuery debQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_deb_id = ?1 AND t.cli_tank_id = ?2", conn);
                MySQLPreparedQuery credQAux = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_cred_id = ?1 AND t.cli_tank_id = ?2", conn);;
                MySQLPreparedQuery debQAux = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_deb_id = ?1 AND t.cli_tank_id = ?2", conn);;
                //meses en deuda
                int cartAcc = Accounts.C_CAR_GLP;
                int cartAccAux = Accounts.C_CAR_FINAN_DEU;

                SysCfg cfg = SysCfg.select(gralConn);

                //meses en deuda
                MySQLPreparedQuery debtsQ = BillClientTankController.getCartBySpanQuery(cartAcc, cartAccAux, conn);
                //con suspensión

                JsonArrayBuilder ab = Json.createArrayBuilder();
                for (BillBuilding bl : buildings) {
                    BillClientTank[] clients = BillClientTank.getByBuildId(bl.id, false, conn);
                    for (BillClientTank cl : clients) {

                        Integer paidTotalBills = new MySQLQuery("SELECT count(*) "
                                + "FROM bill_bill "
                                + "WHERE bill_span_id = ?1 AND client_tank_id = ?2 AND total AND payment_date IS NOT NULL").setParam(1, cl.id).setParam(2, curSpan).getAsInteger(conn);

                        if (paidTotalBills == 0) {
                            if (!new MySQLQuery("SELECT COUNT(*)>0 FROM bill_susp s WHERE s.recon_date IS NULL AND s.cancelled = 0 AND s.client_id = ?1")
                                    .setParam(1, cl.id).getAsBoolean(conn)) {
                                //cartera
                                credQ.setParameter(1, cartAcc);
                                credQ.setParameter(2, cl.id);

                                debQ.setParameter(1, cartAcc);
                                debQ.setParameter(2, cl.id);
                                BigDecimal debit = debQ.getAsBigDecimal(true);
                                BigDecimal credit = credQ.getAsBigDecimal(true);

                                BigDecimal debitAux;
                                BigDecimal creditAux;
                                credQAux.setParameter(1, cartAccAux);
                                credQAux.setParameter(2, cl.id);

                                debQAux.setParameter(1, cartAccAux);
                                debQAux.setParameter(2, cl.id);
                                debitAux = debQAux.getAsBigDecimal(true);
                                creditAux = credQAux.getAsBigDecimal(true);
                                debit = debit.add(debitAux);
                                credit = credit.add(creditAux);
                                BigDecimal total = debit.subtract(credit);

                                if (total.compareTo(cfg.suspValue) > 0) {
                                    if (total.compareTo(BigDecimal.ZERO) != 0) {
                                        BigDecimal[] debts = getDebtClient(cl.id, debtsQ, total);
                                        Integer months = debts[7].intValue();//meses en mora

                                        BigDecimal deposit = BigDecimal.ZERO;

                                        String docIds = new MySQLQuery("SELECT GROUP_CONCAT(bb.id) "
                                                + "FROM bill_bill bb WHERE "
                                                + "bb.client_tank_id = ?1 AND  bb.active = 1 AND bb.total = 0 AND bb.bill_span_id = " + curSpan + " "
                                                + "AND bb.payment_date IS NOT NULL").setParam(1, cl.id).getAsString(conn);
                                        if (docIds != null && !docIds.isEmpty()) {
                                            deposit = new MySQLQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.bill_span_id = " + curSpan + " AND t.doc_id IN (" + docIds + ")").getAsBigDecimal(conn, true);
                                        }

                                        if (months >= inst.suspDebtMonths) {
                                            JsonObjectBuilder cob = Json.createObjectBuilder();
                                            String numMeter = new MySQLQuery("SELECT `number` FROM bill_meter WHERE client_id = ?1 ORDER BY start_span_id DESC LIMIT 1").setParam(1, cl.id).getAsString(conn);
                                            cob.add("clientId", cl.id);
                                            cob.add("buildingName", bl.name);
                                            cob.add("clientName", BillClientTank.getClientName(cl));
                                            cob.add("numInstall", cl.numInstall);
                                            cob.add("document", cl.doc != null ? cl.doc : "");
                                            cob.add("numMeter", numMeter != null ? numMeter : "");
                                            cob.add("total", MySQLQuery.getAsString(total));
                                            cob.add("months", MySQLQuery.getAsString(months));
                                            cob.add("deposit", MySQLQuery.getAsString(deposit));
                                            ab.add(cob);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ob.add("status", "ok");
                ob.add("data", ab);
            } catch (Exception ex) {
                ob.add("status", "error");
                ob.add("msg", ex.getMessage());
                Logger.getLogger(GetClientsToSusp.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                w.writeObject(ob.build());
                System.out.println("GetClientsToSusp " + (System.currentTimeMillis() - t) + "ms");
            }
        } catch (Exception ex) {
            Logger.getLogger(SynchronizeData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

}
