package web.billing.readings;

import controller.billing.BillClientTankController;
import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.billing.constants.Accounts;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import web.billing.BillingServlet;

@MultipartConfig
@WebServlet(name = "/readings/getMonths", urlPatterns = {"/readings/getMonths"})
public class GetMonths extends BillingServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            SessionLogin.validate(request.getParameter("sessionId"));
            JsonObjectBuilder ob = Json.createObjectBuilder();
            try (Connection conn = getConnection(Integer.parseInt(request.getParameter("cityId")))) {

                Integer clientId;
                if (request.getParameter("suspId") != null) {
                    clientId = new MySQLQuery("SELECT client_id FROM bill_susp WHERE id = " + request.getParameter("suspId")).getAsInteger(conn);
                } else if (request.getParameter("clientId") != null) {
                    clientId = Integer.valueOf(request.getParameter("clientId"));
                } else {
                    throw new Exception("Debe indicar suspId o clientId");
                }

                BillClientTankController.getDebtsMonthsClient(clientId, Accounts.C_CAR_GLP, conn);
                Integer months = BillClientTankController.getDebtsMonthsClient(clientId, Accounts.C_CAR_GLP, conn);
                ob.add("status", "OK");
                ob.add("months", months);
            } catch (Exception ex) {
                Logger.getLogger(GetMonths.class.getName()).log(Level.SEVERE, null, ex);
                ob.add("status", "ERROR");
                ob.add("msg", ex.getMessage());
            } finally {
                w.writeObject(ob.build());
            }
        } catch (Exception ex) {
            Logger.getLogger(GetMonths.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500, ex.getMessage() != null ? ex.getMessage() : ex.getClass().toString());
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
        return "Consulta de Meses en Mora";
    }

}
