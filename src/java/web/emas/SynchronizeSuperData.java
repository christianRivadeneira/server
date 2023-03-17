package web.emas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
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
import utilities.JsonUtils;
import utilities.MySQLQuery;
import web.fileManager;

@MultipartConfig
@WebServlet(name = "SynchronizeSuperData", urlPatterns = {"/SynchronizeSuperData"})
public class SynchronizeSuperData extends HttpServlet {

    private static final String STATUS_ERROR = "ERROR";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;

        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {

            JsonObjectBuilder ob = Json.createObjectBuilder();
            JsonArrayBuilder jar = Json.createArrayBuilder();

            conn = MySQLCommon.getConnection(request.getParameter("poolName"), request.getParameter("tz"));
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");
            JsonArray req = MySQLQuery.scapeJsonArray(request);

            try {

                JsonObject object = req.getJsonObject(0);
                JsonArray visits = object.getJsonArray("visits");
                JsonArray sedes = object.getJsonArray("sedes");
                JsonArray logs = object.getJsonArray("logs");
                JsonArray signatures = object.getJsonArray("signatures");
                fileManager.PathInfo pInfo = new fileManager.PathInfo(conn);

                for (int i = 0; i < visits.size(); i++) {

                    JsonObject visit = visits.getJsonObject(i);

                    Integer superVisitSedeId = new MySQLQuery("SELECT "
                            + "id "
                            + "FROM emas_super_visit "
                            + "WHERE dt = '" + visit.getString("dt") + "' "
                            + "AND emp_supv_id = " + visit.getInt("empSupvId") + " "
                            + "AND clie_sede_id = " + visit.getInt("clieSedeId")).getAsInteger(conn);

                    Integer superVisitRecolId = new MySQLQuery("SELECT "
                            + "id "
                            + "FROM emas_super_visit "
                            + "WHERE dt = '" + visit.getString("dt") + "' "
                            + "AND nov = " + JsonUtils.getBool(visit, "nov", true) + " "
                            + "AND emp_supv_id = " + visit.getInt("empSupvId") + " "
                            + (visit.containsKey("empRecolId") ? "AND emp_recol_id = " + visit.getInt("empRecolId") : "AND emp_recol_id = NULL")).getAsInteger(conn);

                    if (superVisitSedeId == null && superVisitRecolId == null) {
                        MySQLQuery q = new MySQLQuery("INSERT INTO emas_super_visit SET "
                                + "lat = ?1 "
                                + ", lon = ?2 "
                                + ", dt = '" + visit.getString("dt")
                                + "', beg_date = '" + visit.getString("begDate")
                                + "', end_date = '" + visit.getString("endDate")
                                + "', emp_supv_id = " + visit.getInt("empSupvId")
                                + ", clie_sede_id = " + visit.getInt("clieSedeId")
                                + (visit.containsKey("empRecolId") ? ", emp_recol_id = " + visit.getInt("empRecolId") : "")
                                + (visit.containsKey("notes") ? ", notes = '" + visit.getString("notes") + "'" : "")
                                + ", nov = " + JsonUtils.getBool(visit, "nov", true)).setParam(1, visit.containsKey("lat") ? visit.getJsonNumber("lat").doubleValue() : null).setParam(2, visit.containsKey("lon") ? visit.getJsonNumber("lon").doubleValue() : null);
                        q.executeInsert(conn);
                    } else if (superVisitSedeId != null) {
                        MySQLQuery q = new MySQLQuery("UPDATE emas_super_visit SET "
                                + "lat = ?1 "
                                + ", lon = ?2 "
                                + ", dt = '" + visit.getString("dt")
                                + "', beg_date = '" + visit.getString("begDate")
                                + "', end_date = '" + visit.getString("endDate")
                                + "', emp_supv_id = " + visit.getInt("empSupvId")
                                + ", clie_sede_id = " + visit.getInt("clieSedeId")
                                + (visit.containsKey("notes") ? ", notes = '" + visit.getString("notes") + "'" : "")
                                + ", nov = " + JsonUtils.getBool(visit, "nov", true)
                                + " WHERE id = " + superVisitSedeId).setParam(1, visit.containsKey("lat") ? visit.getJsonNumber("lat").doubleValue() : null).setParam(2, visit.containsKey("lon") ? visit.getJsonNumber("lon").doubleValue() : null);
                        q.executeUpdate(conn);
                    } else if (superVisitRecolId != null) {
                        MySQLQuery q = new MySQLQuery("UPDATE emas_super_visit SET "
                                + "lat = ?1 "
                                + ", lon = ?2 "
                                + ", dt = '" + visit.getString("dt")
                                + "', beg_date = '" + visit.getString("begDate")
                                + "', end_date = '" + visit.getString("endDate")
                                + "', emp_supv_id = " + visit.getInt("empSupvId")
                                + (visit.containsKey("empRecolId") ? ", emp_recol_id = " + visit.getInt("empRecolId") : "")
                                + (visit.containsKey("notes") ? ", notes = '" + visit.getString("notes") + "'" : "")
                                + "WHERE id = " + superVisitRecolId).setParam(1, visit.containsKey("lat") ? visit.getJsonNumber("lat").doubleValue() : null).setParam(2, visit.containsKey("lon") ? visit.getJsonNumber("lon").doubleValue() : null);;
                        q.executeUpdate(conn);
                    }
                }

                for (int i = 0; i < sedes.size(); i++) {
                    JsonObject sede = sedes.getJsonObject(i);
                    MySQLQuery q = new MySQLQuery("UPDATE emas_clie_sede SET lat = " + sede.getJsonNumber("sedeLat").bigDecimalValue()
                            + ", lon = " + sede.getJsonNumber("sedeLon").bigDecimalValue()
                            + (sede.containsKey("sedeBarcode") ? ", bar_code = '" + sede.getString("sedeBarcode") + "'" : "")
                            + " WHERE id = " + sede.getInt("sedeId"));
                    q.executeUpdate(conn);
                }

                for (int i = 0; i < logs.size(); i++) {
                    JsonObject log = logs.getJsonObject(i);

                    MySQLQuery q = new MySQLQuery("INSERT emas_log SET "
                            + "owner_id = " + log.getInt("ownerId")
                            + ", owner_type = " + log.getInt("ownerType")
                            + ", employee_id = " + log.getInt("employeeId") + " "
                            + ", log_date = '" + log.getString("logDate") + "' "
                            + (log.containsKey("notes") ? ", notes = '" + log.getString("notes") + "'" : ""));
                    q.executeInsert(conn);
                }

                for (int i = 0; i < signatures.size(); i++) {

                    JsonObject signature = signatures.getJsonObject(i);

                    int bfileId = 0;
                    int ownerId = signature.getInt("ownerId");
                    int ownerType = signature.getInt("ownerType");

                    boolean exist = new MySQLQuery("SELECT "
                            + "COUNT(*)>0 "
                            + "FROM bfile "
                            + "WHERE owner_id = ?1 AND owner_type = ?2").setParam(1, ownerId).setParam(2, ownerType).getAsBoolean(conn);

                    if (exist) {
                        String query = "UPDATE bfile SET "
                                + "updated_by = " + signature.getInt("employeeId") + " "
                                + ",updated = NOW() "
                                + "WHERE owner_id = " + ownerId + " "
                                + "AND owner_type = " + ownerType;
                        new MySQLQuery(query).executeUpdate(conn);

                        bfileId = new MySQLQuery("SELECT "
                                + "id "
                                + "FROM bfile "
                                + "WHERE owner_id = ?1 AND owner_type = ?2").setParam(1, ownerId).setParam(2, ownerType).getAsInteger(conn);

                    } else {
                        String fileName = "Signature-recolId-" + ownerId + ".jpg";

                        String query = "INSERT INTO bfile SET "
                                + "file_name = '" + fileName + "' "
                                + ",description = '" + signature.getString("notes") + "' "
                                + ",owner_id = " + ownerId + " "
                                + ",owner_type = " + signature.getInt("ownerType") + " "
                                + ",created_by = " + signature.getInt("employeeId") + " "
                                + ",updated_by = " + signature.getInt("employeeId") + " "
                                + ",created = NOW() "
                                + ",updated = NOW() "
                                + ",keywords = ''";
                        bfileId = new MySQLQuery(query).executeInsert(conn);
                    }
                    File file = pInfo.getNewFile(bfileId);
                    //Decodificar String base64 (Automáticamente detecta y descomprime el gzip)
                    byte[] data = Base64.decode(signature.getString("signB64"));
                    FileOutputStream fos = new FileOutputStream(file, false);
                    fos.write(data);
                    fos.close();
                    new MySQLQuery("UPDATE bfile SET size = " + file.length() + " WHERE id = " + bfileId).executeUpdate(conn);
                }

            } catch (Exception ex) {
                Logger.getLogger(SynchronizeSuperData.class.getName()).log(Level.SEVERE, null, ex);
                response.sendError(500);
                ob.add("status", STATUS_ERROR);
                ob.add("msg", ex.getMessage());
            } finally {
                w.writeArray(jar.build());
            }

        } catch (Exception ex) {
            Logger.getLogger(SynchronizeSuperData.class.getName()).log(Level.SEVERE, null, ex);
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
