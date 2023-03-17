package web.billing;

import controller.billing.BillClientTankController;
import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.billing.constants.Accounts;
import service.MySQL.MySQLCommon;
import service.MySQL.MySQLSelect;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "ConsultClient", urlPatterns = {"/ConsultClient"})
public class ConsultClient extends BillingServlet {

    private static final String STATUS_OK = "OK";
    private static final String STATUS_ERROR = "ERROR";
    private static final String CONSULT_TYPE_INSTALLATION_NUMBER = "Número de instalación";
    private static final String CONSULT_TYPE_LAST_NAME = "Apellido";
    private static final String CONSULT_TYPE_DOCUMENT = "Documento";
    private static final String CONSULT_TYPE_METER_NUMBER = "Número de medidor";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            JsonObjectBuilder ob = Json.createObjectBuilder();
            JsonArrayBuilder jar = Json.createArrayBuilder();

            int cityId = Integer.valueOf(request.getParameter("cityId"));
            conn = MySQLCommon.getConnection(getDbName(cityId), null);

            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");
            JsonObject req = Json.createReader(request.getPart("data").getInputStream()).readObject();

            try {
                String param = req.getString("param");
                String consultType = req.getString("consultType");

                Object[][] data = null;

                if (consultType.equals(CONSULT_TYPE_INSTALLATION_NUMBER)) {
                    data = new MySQLQuery("SELECT c.id, b.name, c.num_install, CONCAT(c.first_name, ' ' , IFNULL(c.last_name,'')), c.doc, (SELECT `number` FROM bill_meter WHERE client_id = c.id ORDER BY start_span_id DESC LIMIT 1) FROM bill_client_tank c, bill_building b "
                            + "WHERE c.num_install LIKE '%" + param + "%' AND b.id = c.building_id;").getRecords(conn);
                } else if (consultType.equals(CONSULT_TYPE_LAST_NAME)) {
                    data = new MySQLQuery("SELECT c.id, b.name, c.num_install, CONCAT(c.first_name, ' ' , IFNULL(c.last_name,'')), c.doc, (SELECT `number` FROM bill_meter WHERE client_id = c.id ORDER BY start_span_id DESC LIMIT 1) FROM bill_client_tank c, bill_building b "
                            + "WHERE c.last_name LIKE '%" + param + "%' AND b.id = c.building_id;").getRecords(conn);
                } else if (consultType.equals(CONSULT_TYPE_DOCUMENT)) {
                    data = new MySQLQuery("SELECT c.id, b.name, c.num_install, CONCAT(c.first_name, ' ' , IFNULL(c.last_name,'')), c.doc, (SELECT `number` FROM bill_meter WHERE client_id = c.id ORDER BY start_span_id DESC LIMIT 1) FROM bill_client_tank c, bill_building b "
                            + "WHERE c.document LIKE '%" + param + "%' AND b.id = c.building_id;").getRecords(conn);
                } else if (consultType.equals(CONSULT_TYPE_METER_NUMBER)) {
                    data = new MySQLQuery("SELECT c.id, b.name, c.num_install, CONCAT(c.first_name, ' ' , IFNULL(c.last_name,'')), c.doc, (SELECT `number` FROM bill_meter WHERE client_id = c.id ORDER BY start_span_id DESC LIMIT 1) FROM bill_client_tank c, bill_building b "
                            + "WHERE (SELECT `number` FROM bill_meter WHERE client_id = c.id ORDER BY start_span_id DESC LIMIT 1) LIKE '%" + param + "%' AND b.id = c.building_id;").getRecords(conn);
                }

                for (Object[] row : data) {
                    JsonObjectBuilder ob1 = Json.createObjectBuilder();
                    int clientId = MySQLQuery.getAsInteger(row[0]);
                    ob1.add("id", clientId);
                    ob1.add("building_name", MySQLQuery.getAsString(row[1]));
                    ob1.add("num_install", MySQLQuery.getAsString(row[2]));
                    ob1.add("name", MySQLQuery.getAsString(row[3]));
                    ob1.add("document", MySQLQuery.getAsString(row[4]));
                    ob1.add("num_meter", MySQLQuery.getAsString(row[5]) != null ? MySQLQuery.getAsString(row[5]) : "");
                    ob1.add("months", BillClientTankController.getDebtsMonthsClient(clientId, Accounts.C_CAR_GLP, conn));

                    jar.add(ob1.build());
                }

            } catch (Exception ex) {
                Logger.getLogger(ConsultClient.class.getName()).log(Level.SEVERE, null, ex);
                ob.add("status", STATUS_ERROR);
                ob.add("msg", ex.getMessage());
            } finally {
                w.writeArray(jar.build());
            }
        } catch (Exception ex) {
            Logger.getLogger(ConsultClient.class.getName()).log(Level.SEVERE, null, ex);
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
        return "Short description";
    }

}
