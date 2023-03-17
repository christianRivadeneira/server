package web.emas;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import service.MySQL.MySQLSelect;
import utilities.Base64;
import utilities.MySQLQuery;
import web.fileManager;

@MultipartConfig
@WebServlet(name = "uploadEmasVisits", urlPatterns = {"/uploadEmasVisits"})
public class SynchronizeData extends HttpServlet {

    private static final String STATUS_ERROR = "ERROR";

    private String getBool(JsonObject o, String n, boolean nullAsFalse) {
        if (o.containsKey(n)) {
            return o.getBoolean(n) ? "1" : "0";
        } else {
            return nullAsFalse ? "0" : "NULL";
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;

        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {

            JsonObjectBuilder ob = Json.createObjectBuilder();
            JsonArrayBuilder jar = Json.createArrayBuilder();

            conn = MySQLCommon.getConnection(request.getParameter("poolName"), request.getParameter("tz"));
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");
            JsonObject root = MySQLQuery.scapeJsonObj(request);

            try {
                if (root.containsKey("novs")) {
                    JsonArray novs = root.getJsonArray("novs");
                    for (int j = 0; j < novs.size(); j++) {
                        JsonObject nov = novs.getJsonObject(j);
                        MySQLQuery sq = new MySQLQuery("UPDATE emas_schedule SET novs = '" + nov.getString("schedNovs") + "' WHERE id = " + nov.getInt("schedId"));
                        sq.executeUpdate(conn);
                    }
                }

                JsonArray visits = root.getJsonArray("visits");
                fileManager.PathInfo pInfo = new fileManager.PathInfo(conn);

                for (int i = 0; i < visits.size(); i++) {
                    JsonObject visit = visits.getJsonObject(i);
                    int schedId = visit.getInt("schedId");
                    Integer visitId = new MySQLQuery("SELECT "
                            + "id "
                            + "FROM emas_recol_visit "
                            + "WHERE dt = '" + visit.getString("dt") + "' "
                            + "AND clie_sede_id = " + visit.getInt("clieSedeId") + " "
                            + "AND vehicle_id = " + visit.getInt("vehicleId")).getAsInteger(conn);

                    MySQLQuery q = new MySQLQuery((visitId == null ? "INSERT INTO" : "UPDATE")
                            + " emas_recol_visit SET "
                            + "  lat = ?1 "
                            + ", lon = ?2 "
                            + ", dt = '" + visit.getString("dt")
                            + "', beg_date = '" + visit.getString("begDate") + "'"
                            + (visit.containsKey("endDate") ? ", end_date = '" + visit.getString("endDate") + "'" : "")
                            + (visit.containsKey("notes") ? ", notes = '" + visit.getString("notes") + "'" : "")
                            + ", clie_sede_id = " + visit.getInt("clieSedeId")
                            + ", emp_recol_id = " + visit.getInt("empRecolId")
                            + ", vehicle_id = " + visit.getInt("vehicleId")
                            + ", has_manifest = " + getBool(visit, "hasManifest", true)
                            + ", has_photos = " + getBool(visit, "hasPhotos", true)
                            + ", has_signature = " + getBool(visit, "hasSignature", true)
                            + ", has_printed = " + getBool(visit, "hasPrinted", true)
                            + ", man_num = " + visit.getInt("manNum") + " "
                            + (visitId != null ? "WHERE id = " + visitId : "")).setParam(1, visit.containsKey("lat") ? visit.getJsonNumber("lat").doubleValue() : null).setParam(2, visit.containsKey("lon") ? visit.getJsonNumber("lon").doubleValue() : null);

                    if (visitId == null) {
                        visitId = q.executeInsert(conn);
                    } else {
                        q.executeUpdate(conn);
                    }

                    MySQLQuery sq = new MySQLQuery("UPDATE emas_schedule SET visit_id = " + visitId + " WHERE id = " + schedId);
                    sq.executeUpdate(conn);

                    JsonArray visitAmounts = visit.getJsonArray("visitAmounts");
                    if (visitAmounts != null && visitAmounts.size() > 0) {
                        new MySQLQuery("DELETE FROM emas_amount WHERE recol_visit_id = " + visitId).executeDelete(conn);
                        for (int j = 0; j < visitAmounts.size(); j++) {
                            JsonObject amount = visitAmounts.getJsonObject(j);
                            new MySQLQuery("INSERT INTO emas_amount SET "
                                    + "amount = ?1 "
                                    + ", res_type_id = " + amount.getInt("resTypeId")
                                    + ", container_id = " + amount.getInt("containerId")
                                    + ", color = " + amount.getInt("color")
                                    + ", num_container = " + amount.getInt("numContainer")
                                    + ", recol_visit_id = " + visitId).setParam(1, amount.getJsonNumber("amount").doubleValue()).executeInsert(conn);
                        }
                    }

                    if (visit.containsKey("recolLogs")) {
                        JsonObject log = visit.getJsonObject("recolLogs");
                        new MySQLQuery("INSERT INTO emas_log SET owner_id = " + visitId
                                + ", owner_type = " + log.getInt("ownerType")
                                + ", employee_id = " + log.getInt("employeeId")
                                + ", log_date = '" + log.getString("logDate") + "'"
                                + ", notes = '" + log.getString("notes") + "'").executeUpdate(conn);
                    }

                    if (visit.containsKey("signature")) {
                        JsonObject signature = visit.getJsonObject("signature");
                        int ownerId = visit.getInt("manNum");
                        int ownerType = signature.getInt("ownerType");

                        Integer bfileId = new MySQLQuery("SELECT id "
                                + "FROM bfile "
                                + "WHERE owner_id = ?1 AND owner_type = ?2").setParam(1, ownerId).setParam(2, ownerType).getAsInteger(conn);

                        if (bfileId == null) {
                            String fileName = "date-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date().getTime())
                                    + "-visit-" + ownerId + "-sedeSignature.jpg";
                            String query = "INSERT INTO bfile SET "
                                    + "file_name = '" + fileName + "' "
                                    + ",description = '" + signature.getString("notes") + "' "
                                    + ",owner_id = " + visit.getInt("manNum") + " "
                                    + ",owner_type = " + signature.getInt("ownerType") + " "
                                    + ",created_by = " + signature.getInt("employeeId") + " "
                                    + ",updated_by = " + signature.getInt("employeeId") + " "
                                    + ",created = NOW() "
                                    + ",updated = NOW() "
                                    + ",keywords = ''";
                            bfileId = new MySQLQuery(query).executeInsert(conn);
                        }

                        File file = pInfo.getNewFile(bfileId);
                        byte[] data = Base64.decode(signature.getString("signB64"));
                        try (FileOutputStream fos = new FileOutputStream(file, false); ByteArrayInputStream in = new ByteArrayInputStream(data)) {
                            fileManager.copy(in, fos);
                        }
                        new MySQLQuery("UPDATE bfile SET size = " + file.length() + " WHERE id = " + bfileId).executeUpdate(conn);
                    }

                    MySQLQuery mq = new MySQLQuery("UPDATE emas_cons_man_hist SET used = 1 AND send_mail = 0 WHERE man_num = " + visit.getInt("manNum"));
                    mq.executeUpdate(conn);
                }
            } catch (Exception ex) {
                Logger.getLogger(SynchronizeData.class.getName()).log(Level.SEVERE, null, ex);
                response.sendError(500);
                ob.add("status", STATUS_ERROR);
                ob.add("msg", ex.getMessage());
            } finally {
                w.writeArray(jar.build());
            }
        } catch (Exception ex) {
            Logger.getLogger(SynchronizeData.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500);
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
