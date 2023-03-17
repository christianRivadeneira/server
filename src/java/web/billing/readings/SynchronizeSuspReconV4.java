package web.billing.readings;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.Objects;
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

@MultipartConfig
@WebServlet(name = "/readings/SynchronizeSuspV4", urlPatterns = {"/readings/SynchronizeSuspV4"})
public class SynchronizeSuspReconV4 extends BillingServlet {

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
                    Object[] row = new MySQLQuery("SELECT susp_date, recon_date, cancelled FROM bill_susp WHERE id = ?1")
                            .setParam(1, JsonUtils.getInt(susp, "id")).getRecord(conn);
                    Date origSuspDate = MySQLQuery.getAsDate(row[0]);
                    Date origReconDate = MySQLQuery.getAsDate(row[1]);
                    boolean cancelled = MySQLQuery.getAsBoolean(row[2]);

                    Date newSuspDate = JsonUtils.getDate(susp, "suspDate");
                    Date newReconDate = JsonUtils.getDate(susp, "reconDate");
                    Integer suspId = JsonUtils.getInt(susp, "id");

                    if (cancelled && (!Objects.equals(origSuspDate, newSuspDate) || !Objects.equals(origReconDate, newReconDate))) {
                        cancelled = false;
                    }

                    MySQLQuery mq = new MySQLQuery("UPDATE bill_susp SET "
                            + "susp_date = ?1, "
                            + "susp_tec_id = ?2, "
                            + "recon_date = ?3, "
                            + "recon_tec_id = ?4, "
                            + "cancelled = ?5, "
                            + "field_notes = ?6, "
                            + "reading = ?7, "
                            + "susp_type = ?8, "
                            + "susp_notes = ?9, "
                            + "sync_date = NOW() "
                            + "WHERE id = ?10");
                    mq.setParam(1, newSuspDate);
                    mq.setParam(2, JsonUtils.getInt(susp, "suspTecId"));
                    mq.setParam(3, newReconDate);
                    mq.setParam(4, JsonUtils.getInt(susp, "reconTecId"));
                    mq.setParam(5, cancelled);
                    mq.setParam(6, JsonUtils.getString(susp, "fieldNotes"));
                    String reading = JsonUtils.getString(susp, "reading");
                    mq.setParam(7, reading != null ? new BigDecimal(reading) : null);
                    mq.setParam(8, JsonUtils.getString(susp, "suspType"));
                    mq.setParam(9, JsonUtils.getString(susp, "suspNotes"));
                    mq.setParam(10, suspId);

                    mq.executeUpdate(conn);

                    if (!cancelled && newReconDate == null && newSuspDate != null) {
                        new MySQLQuery("UPDATE bill_client_tank SET discon = 1 "
                                + "WHERE id = (SELECT client_id FROM bill_susp WHERE id = " + suspId + ")").executeUpdate(conn);
                    } else if (!cancelled && newReconDate != null && newSuspDate != null) {
                        new MySQLQuery("UPDATE bill_client_tank SET discon = 0 "
                                + "WHERE id = (SELECT client_id FROM bill_susp WHERE id = " + suspId + ")").executeUpdate(conn);
                    }

                }
                //========traer nuevas ordenes de suspensión y reconexión========

                Object[][] data = new MySQLQuery("SELECT "
                        + "s.id, "//0
                        + "trim(concat(c.first_name, ' ', ifnull(c.last_name, ''))), "//1
                        + "concat(" + (sysCfg.showApartment ? "c.apartment" : "c.num_install") + ", IFNULL(CONCAT(' (',(SELECT `number` FROM bill_meter WHERE client_id = c.id ORDER BY start_span_id DESC LIMIT 1),')'), '')), "//2
                        + "b.name, "//3
                        + "susp_order_date,"//4
                        + "susp_date,"//5
                        + "susp_tec_id,"//6
                        + "recon_order_date,"//7
                        + "recon_date,"//8
                        + "recon_tec_id, "//8
                        + "field_notes, "//10
                        + "reading, "//11
                        + "susp_type, "//12
                        + "b.id, "//13                                   
                        + "cc.lat, "//14   
                        + "cc.lon, "//15                                                                  
                        + "b.address, "//16
                        + "s.susp_notes "//17
                        + "FROM bill_susp s "
                        + "INNER JOIN bill_client_tank c ON c.id = s.client_id "
                        + "INNER JOIN bill_building b ON b.id = c.building_id "
                        + "LEFT JOIN sigma.ord_tank_client cc ON cc.mirror_id=b.id AND cc.`type`='build' "
                        + "WHERE cancelled = 0 AND "
                        + "((susp_order_date IS NOT NULL AND susp_date IS NULL) OR (recon_order_date IS NOT NULL AND recon_date IS NULL)) GROUP BY s.id").getRecords(conn);

                JsonArrayBuilder ab = Json.createArrayBuilder();
                //se comenta para forzar a que actualicen
                /*
                for (Object[] row : data) {
                    JsonObjectBuilder ob2 = Json.createObjectBuilder();
                    ob2.add("id", MySQLQuery.getAsInteger(row[0]));
                    JsonUtils.addString(ob2, "clientName", row[1]);
                    JsonUtils.addString(ob2, "clientSub", row[2]);
                    JsonUtils.addString(ob2, "building", row[3]);
                    JsonUtils.addDate(ob2, "suspOrderDate", row[4]);
                    JsonUtils.addDate(ob2, "suspDate", row[5]);
                    JsonUtils.addInt(ob2, "suspTecId", row[6]);
                    JsonUtils.addDate(ob2, "reconOrderDate", row[7]);
                    JsonUtils.addDate(ob2, "reconDate", row[8]);
                    JsonUtils.addInt(ob2, "reconTecId", row[9]);
                    JsonUtils.addString(ob2, "field_notes", row[10]);
                    JsonUtils.addString(ob2, "reading", row[11]);
                    JsonUtils.addString(ob2, "suspType", row[12]);
                    ob2.add("buildingId", MySQLQuery.getAsInteger(row[13]));
                    JsonUtils.addBigDecimal(ob2, "lat", row[14], false);
                    JsonUtils.addBigDecimal(ob2, "lon", row[15], false);
                    JsonUtils.addString(ob2, "address", row[16]);
                    JsonUtils.addString(ob2, "suspNotes", row[17]);
                    ab.add(ob2);
                }*/

                ob.add("status", "OK");
                ob.add("data", ab);

            } catch (Exception ex) {
                Logger.getLogger(SynchronizeSuspReconV4.class.getName()).log(Level.SEVERE, null, ex);
                ob.add("status", "ERROR");
                ob.add("msg", ex.getMessage());
            } finally {
                w.writeObject(ob.build());
            }
        } catch (Exception ex) {
            Logger.getLogger(GetMonths.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500, ex.getClass().toString() + ":\n" + (ex.getMessage() != null ? ex.getMessage() : ""));
        } finally {
            System.out.println("SynchronizeSuspRecon: " + (System.currentTimeMillis() - t) + "ms");
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
        return "Sincronizar Suspensiones y Reconexiones";
    }
}
