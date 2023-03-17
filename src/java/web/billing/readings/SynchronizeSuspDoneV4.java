package web.billing.readings;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.system.SessionLogin;
import api.sys.model.SysCfg;
import service.MySQL.MySQLCommon;
import utilities.IO;
import utilities.JsonUtils;
import utilities.MySQLQuery;
import web.billing.BillingServlet;
import static web.billing.BillingServlet.getConnection;

@MultipartConfig
@WebServlet(name = "/readings/SynchronizeSuspDoneV4", urlPatterns = {"/readings/SynchronizeSuspDoneV4"})
public class SynchronizeSuspDoneV4 extends BillingServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long t = System.currentTimeMillis();
        try (GZIPOutputStream goz = new GZIPOutputStream(response.getOutputStream()); JsonWriter w = Json.createWriter(goz)) {
            SysCfg sysCfg;
            try (Connection ep = MySQLCommon.getConnection("sigma", null)) {
                sysCfg = SysCfg.select(ep);
            }

            SessionLogin.validate(IO.convertStreamToString(request.getPart("sessionId").getInputStream()));
            JsonObjectBuilder ob = Json.createObjectBuilder();
            JsonArray req = Json.createReader(request.getPart("data").getInputStream()).readArray();

            try (Connection conn = getConnection(Integer.parseInt(request.getParameter("cityId")))) {

                for (int i = 0; i < req.size(); i++) {
                    JsonObject susp = req.getJsonObject(i);

                    MySQLQuery mq = new MySQLQuery("UPDATE bill_susp SET "
                            + "field_notes = ?1, "
                            + "reading = ?2, "
                            + "susp_type = ?3, "
                            + "susp_notes = ?4, "
                            + "sync_date = NOW() "
                            + "WHERE id = ?5");
                    mq.setParam(1, JsonUtils.getString(susp, "fieldNotes"));
                    String reading = JsonUtils.getString(susp, "reading");
                    mq.setParam(2, reading != null ? new BigDecimal(reading) : null);
                    mq.setParam(3, JsonUtils.getString(susp, "suspType"));
                    mq.setParam(4, JsonUtils.getString(susp, "suspNotes"));
                    mq.setParam(5, JsonUtils.getInt(susp, "id"));
                    mq.executeUpdate(conn);
                }

                //========traer nuevas susp done ========
                Object[][] data = new MySQLQuery("SELECT b.id,"
                        + " TRIM(CONCAT(c.first_name, ' ', IFNULL(c.last_name, ''))),"//1
                        + " concat(" + (sysCfg.showApartment ? "c.apartment" : "c.num_install") + ", IFNULL(CONCAT(' (',(SELECT `number` FROM bill_meter WHERE client_id = c.id ORDER BY start_span_id DESC LIMIT 1),')'), '')), "//2
                        + " bi.name,"//3
                        + " b.susp_order_date,"//4
                        + " b.susp_date,"//5
                        + " b.susp_tec_id," //6                       
                        + " field_notes, "//7
                        + " reading, "//8
                        + " susp_type, "//9
                        + " susp_notes "//10
                        + " FROM bill_susp b "
                        + " INNER JOIN bill_client_tank c ON c.id = b.client_id "
                        + " INNER JOIN bill_building bi ON bi.id = c.building_id "
                        + " WHERE b.recon_order_date IS NULL AND b.recon_date IS NULL"
                        + " AND b.cancelled=0 AND b.susp_date IS NOT NULL ORDER BY b.susp_order_date DESC").getRecords(conn);

                JsonArrayBuilder ab = Json.createArrayBuilder();
                for (Object[] row : data) {
                    JsonObjectBuilder ob2 = Json.createObjectBuilder();
                    ob2.add("id", MySQLQuery.getAsInteger(row[0]));
                    JsonUtils.addString(ob2, "clientName", row[1]);
                    JsonUtils.addString(ob2, "clientSub", row[2]);
                    JsonUtils.addString(ob2, "building", row[3]);
                    JsonUtils.addDate(ob2, "suspOrderDate", row[4]);
                    JsonUtils.addDate(ob2, "suspDate", row[5]);
                    JsonUtils.addInt(ob2, "suspTecId", row[6]);
                    JsonUtils.addString(ob2, "field_notes", row[7]);
                    JsonUtils.addString(ob2, "reading", row[8]);
                    JsonUtils.addString(ob2, "suspType", row[9]);
                    JsonUtils.addString(ob2, "suspNotes", row[10]);
                    ab.add(ob2);
                }

                ob.add("status", "OK");
                ob.add("data", ab);

            } catch (Exception ex) {
                Logger.getLogger(SynchronizeSuspDoneV4.class.getName()).log(Level.SEVERE, null, ex);
                ob.add("status", "ERROR");
                ob.add("msg", ex.getMessage());
            } finally {
                w.writeObject(ob.build());
            }
        } catch (Exception ex) {
            Logger.getLogger(GetMonths.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500, ex.getClass().toString() + ":\n" + (ex.getMessage() != null ? ex.getMessage() : ""));
        } finally {
            System.out.println("SynchronizeSuspDone: " + (System.currentTimeMillis() - t) + "ms");
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
        return "Sincronizar Inf deSuspensiones Efectivas";
    }
}
