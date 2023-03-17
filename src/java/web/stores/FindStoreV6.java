package web.stores;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.math.BigDecimal;
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
import utilities.Dates;
import utilities.MySQLQuery;
import web.helpdesk.GCMHlp;

@MultipartConfig
@WebServlet(name = "FindStoreV6", urlPatterns = {"/FindStoreV6"})
public class FindStoreV6 extends HttpServlet {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat sdfReg = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aaa");
    private static final SimpleDateFormat sdfChk = new SimpleDateFormat("dd-MM-yyyy");
    private static final SimpleDateFormat sdfRegAm = new SimpleDateFormat("yyyy-MM-dd hh:mm aaa");

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
        Connection conn = null;

        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            JsonObjectBuilder ob = Json.createObjectBuilder();
            conn = MySQLCommon.getConnection("sigmads", null);
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");
            JsonObject req = MySQLQuery.scapeJsonObj(request);
            String header = req.getString("header");

            try {
                switch (header) {

                    case "findStores": {

                        int id = req.getInt("id");
                        String str = "SELECT "
                                + "i.id, "
                                + "i.internal, "
                                + "i.address, "
                                + "CONCAT(i.first_name ,' ',i.last_name),"
                                + "i.lat, "
                                + "i.lon, "
                                + "d.name, "
                                + "i.document, "
                                + "invt.name, "
                                + "invc.name, "
                                + "i.phones, "
                                + "i.update_date "
                                + "FROM "
                                + "com_service_manager AS sm "
                                + "INNER JOIN com_man_store AS ms ON ms.man_id = sm.id "
                                + "INNER JOIN inv_store i ON i.id = ms.store_id "
                                + "INNER JOIN city d ON i.city_id = d.id "
                                + "LEFT JOIN inv_store_type invt ON invt.id = i.type_id "
                                + "LEFT JOIN inv_center invc ON invc.id = i.center_id "
                                + "WHERE sm.emp_id = " + id + " ";
                        Object[][] dataStore = new MySQLQuery(str).getRecords(conn);

                        JsonArrayBuilder jsonStore = Json.createArrayBuilder();

                        for (Object[] data : dataStore) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("internal", (data[1] != null ? MySQLQuery.getAsString(data[1]) : ""));
                            row.add("address", (data[2] != null ? MySQLQuery.getAsString(data[2]) : "Sin direccion"));
                            row.add("fullName", (data[3] != null ? MySQLQuery.getAsString(data[3]) : ""));

                            BigDecimal lat = MySQLQuery.getAsBigDecimal(data[4], true);
                            BigDecimal lon = MySQLQuery.getAsBigDecimal(data[5], true);

                            row.add("latitude", (lat.signum() == 0 ? "sin coordenadas" : String.valueOf(MySQLQuery.getAsBigDecimal(data[4], true))));
                            row.add("longitude", (lon.signum() == 0 ? "sin coordenadas" : String.valueOf(MySQLQuery.getAsBigDecimal(data[5], true))));
                            row.add("city", (data[6] != null ? MySQLQuery.getAsString(data[6]) : "Sin ciudad"));
                            row.add("document", (data[7] != null ? MySQLQuery.getAsString(data[7]) : "Sin documento"));
                            row.add("type", (data[8] != null ? MySQLQuery.getAsString(data[8]) : "Sin tipo"));
                            row.add("center", (data[9] != null ? MySQLQuery.getAsString(data[9]) : "No tiene centro"));
                            row.add("phone", (data[10] != null ? MySQLQuery.getAsString(data[10]) : "Sin telefono"));

                            if (data[11] == null) {
                                row.add("legalize_date", "No legalizado");
                            } else {
                                Date date = MySQLQuery.getAsDate(data[11]);
                                row.add("legalize_date", sdf.format(date));
                            }
                            jsonStore.add(row);
                        }

                        ob.add("dataStores", jsonStore);
                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "findCyls": {
                        int id = req.getInt("id");

                        JsonArrayBuilder jsonStore = Json.createArrayBuilder();
                        JsonObjectBuilder row = Json.createObjectBuilder();

                        row.add("stock", getStock(MySQLQuery.getAsInteger(id), conn));

                        jsonStore.add(row);

                        ob.add("dataStores", jsonStore);
                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "findManagers": {
                        int idSuper = req.getInt("idSuper");
                        boolean isCoordinator = req.getBoolean("idCoordinator");

                        String str;

                        if (isCoordinator) {
                            str = "SELECT c.id, c.emp_id, sp.id, c.active, e.first_name, e.last_name, COUNT(v.id), "
                                    + "(SELECT COUNT(*) FROM com_man_store s WHERE s.man_id=c.id)  "
                                    + "FROM com_coor_manager cm  "
                                    + "INNER JOIN com_service_manager c ON c.emp_id = cm.emp_id  "
                                    + "INNER JOIN employee e ON c.emp_id = e.id  "
                                    + "INNER JOIN com_super_manager sp ON sp.id  = cm.super_id "
                                    + "LEFT JOIN com_man_veh v ON v.man_id=c.id  "
                                    + "WHERE sp.emp_id = " + idSuper + " "
                                    + "GROUP BY c.id";
                        } else {
                            str = "SELECT c.id, c.emp_id, c.com_super_id, c.active, e.first_name,e.last_name, COUNT(v.id), "
                                    + "(SELECT COUNT(*) FROM com_man_store s WHERE s.man_id=c.id) "
                                    + "FROM com_service_manager c "
                                    + "INNER JOIN employee e ON c.emp_id=e.id "
                                    + "LEFT JOIN com_man_veh v ON v.man_id=c.id  "
                                    + "INNER JOIN com_super_manager sm ON sm.id = c.com_super_id "
                                    + "WHERE sm.emp_id = " + idSuper + "  "
                                    + "GROUP BY c.id ";
                        }

                        Object[][] dataManagers = new MySQLQuery(str).getRecords(conn);

                        JsonArrayBuilder jsonStore = Json.createArrayBuilder();

                        for (Object[] data : dataManagers) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("idEmp", MySQLQuery.getAsInteger(data[1]));
                            row.add("idSuper", (data[2] != null ? MySQLQuery.getAsInteger(data[2]) : null));
                            row.add("active", (MySQLQuery.getAsString(data[3])));

                            row.add("firstName", (data[4] != null ? MySQLQuery.getAsString(data[4]) : "Sin nombre"));
                            row.add("lastName", (data[5] != null ? MySQLQuery.getAsString(data[5]) : "Sin apellido"));
                            row.add("vehicles", (data[6] != null ? MySQLQuery.getAsInteger(data[6]) : 0));
                            row.add("stores", (data[7] != null ? MySQLQuery.getAsInteger(data[7]) : 0));

                            jsonStore.add(row);
                        }

                        ob.add("dataManagers", jsonStore);
                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "setCoordinates": {

                        int storeId = req.getInt("storeId");
                        Double latitude = req.getJsonNumber("latitude").doubleValue();
                        Double longitude = req.getJsonNumber("longitude").doubleValue();

                        new MySQLQuery("UPDATE inv_store "
                                + "SET "
                                + "lat = " + latitude + ", "
                                + "lon = " + longitude + " "
                                + "WHERE id = " + storeId).executeUpdate(conn);

                        ob.add("msg", "registro actualizado ");
                        break;
                    }

                    case "findVehicles": {

                        int id = req.getInt("id");
                        Integer vhId = req.getInt("vhId");
                        String str = "SELECT vh.id, vh.plate, vh.internal, "
                                + "IFNULL(CONCAT(e.first_name, ' ',e.last_name) , 'Sin Conductor') "
                                + "FROM com_service_manager AS sm "
                                + "INNER JOIN com_man_veh AS mv ON mv.man_id = sm.id "
                                + "INNER JOIN vehicle AS vh ON vh.id = mv.veh_id "
                                + "LEFT JOIN driver_vehicle AS dv ON dv.vehicle_id = vh.id AND dv.`end` IS NULL "
                                + "LEFT JOIN employee AS e ON e.id = dv.driver_id "
                                + "WHERE "
                                + (vhId != 0 ? "vh.id = " + vhId + " " : " ")
                                + (id != 0 ? "sm.emp_id = " + id + " " : " ");

                        Object[][] dataVehicle = new MySQLQuery(str).getRecords(conn);

                        JsonArrayBuilder jsonVehicle = Json.createArrayBuilder();

                        for (Object[] data : dataVehicle) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("plate", (data[1] != null ? MySQLQuery.getAsString(data[1]) : "Sin placa"));
                            row.add("internal", (data[2] != null ? MySQLQuery.getAsString(data[2]) : "Sin interno"));
                            row.add("driver", (data[3] != null ? MySQLQuery.getAsString(data[3]) : "Sin conductor"));

                            jsonVehicle.add(row);
                        }

                        ob.add("dataVehicles", jsonVehicle);
                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "salesDay": {
                        int id = req.getInt("id");
                        String saleSub = "SELECT COUNT(*) FROM trk_sale ts INNER JOIN vehicle v on v.id = ts.vehicle_id where DATE(ts.date) = CURDATE() AND v.id = " + id + " AND ts.sale_type = 'sub'";
                        String saleFull = "SELECT COUNT(*) FROM trk_sale ts INNER JOIN vehicle v on v.id = ts.vehicle_id WHERE DATE(ts.date) = CURDATE() AND v.id = " + id + " AND (ts.sale_type LIKE 'full' OR ts.sale_type LIKE 'cred')";
                        String saleMul = "SELECT COUNT(*) FROM trk_sale ts INNER JOIN vehicle v on v.id = ts.vehicle_id INNER JOIN trk_multi_cyls tm on ts.id = tm.sale_id WHERE DATE(ts.date) = CURDATE() AND v.id = " + id + " AND ts.sale_type = 'mul' AND tm.`type` = 'del'";

                        Object[][] dataSub = new MySQLQuery(saleSub).getRecords(conn);
                        Object[][] dataFull = new MySQLQuery(saleFull).getRecords(conn);
                        Object[][] dataMul = new MySQLQuery(saleMul).getRecords(conn);

                        JsonObjectBuilder row = Json.createObjectBuilder();
                        row.add("subSale", MySQLQuery.getAsInteger(dataSub[0][0]));
                        row.add("fullSale", MySQLQuery.getAsInteger(dataFull[0][0]));
                        row.add("multiSale", MySQLQuery.getAsInteger(dataMul[0][0]));

                        ob.add("dataSales", row);
                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "registerVisit": {

                        Integer managerId = getInt(req, "managerId");
                        Integer superId = getInt(req, "superId");
                        boolean caseHlp = req.getString("caseHlp").equals("true");

                        Integer storeId = req.getInt("storeId");
                        SimpleDateFormat sdh = new SimpleDateFormat("yyyy-MM-dd");
                        String chkDate = (req.getString("chkDate").equals("") ? null : "'" + req.getString("chkDate") + "'");
                        String detail = req.getString("detail");

                        String poolName = req.getString("poolName");
                        String tz = req.getString("tz");

                        Integer idCaseHlp = null;
                        if (caseHlp) {
                            String subject = new MySQLQuery("SELECT CONCAT('Almacen: ', COALESCE(s.first_name,''),' ' ,COALESCE(s.last_name,''),' Interno: ',COALESCE(s.internal,'Sin Interno')) FROM inv_store s WHERE s.id =" + storeId).getAsString(conn);
                            Integer idIncharge = getInt(req, "id_incharge");
                            idCaseHlp = getIdCasehlp(superId, managerId, detail, subject, idIncharge, conn, poolName, tz);

                        }

                        String str = "INSERT INTO com_novelty "
                                + "(manager_id,super_id, store_id,reg_date,chk_date,detail, hlp_request_id) VALUES ( "
                                + managerId + ", "
                                + superId + ", "
                                + storeId + ", "
                                + " NOW(), "
                                + chkDate + ", "
                                + " '" + detail + "', "
                                + " " + idCaseHlp + " "
                                + ")";

                        MySQLQuery q = new MySQLQuery(str);
                        int id = q.executeInsert(conn);

                        ob.add("msg", "Visita Registrada");
                        ob.add("id", id);
                        break;
                    }

                    case "updateVisit": {
                        Integer managerId = req.getInt("managerId");
                        Integer storeId = req.getInt("storeId");
                        Integer visitId = req.getInt("visitId");

                        SimpleDateFormat sdh = new SimpleDateFormat("yyyy-MM-dd");
                        String chkDate = (req.getString("chkDate").equals("") ? null : "'" + req.getString("chkDate") + "'");
                        String detail = req.getString("detail");

                        String q1 = "SELECT n.id, n.hlp_request_id FROM com_novelty n WHERE n.id = " + visitId;
                        Object[] caseHlp = new MySQLQuery(q1).getRecord(conn);

                        if (caseHlp.length > 0 && (caseHlp[1] != null)) {
                            q1 = "UPDATE hlp_request SET notes = '" + detail + "' WHERE id = " + caseHlp[1];
                            new MySQLQuery(q1).executeUpdate(conn);
                        }

                        String str = "UPDATE com_novelty SET "
                                + "chk_date =  " + null + ", "
                                + "detail   = '" + detail + "' "
                                + "WHERE id =  " + visitId + " ";

                        MySQLQuery q = new MySQLQuery(str);
                        q.executeUpdate(conn);

                        ob.add("msg", "Visita Actualizada");
                        break;
                    }

                    case "getNolvelties": {
                        Integer managerId = getInt(req, "managerId");
                        Integer superId = getInt(req, "superId");

                        String str = "SELECT "
                                + "cv.id, "
                                + "CONCAT(e.first_name,' ',e.last_name), "
                                + "IF(cv.store_id IS NULL, cv.place_name,(CONCAT(ivs.first_name, ' ', ivs.last_name))), "
                                + "cv.reg_date, "
                                + "cv.chk_date, "
                                + "cv.detail "
                                + "FROM com_novelty cv "
                                + "LEFT JOIN employee e ON e.id = cv.manager_id "
                                + "LEFT JOIN inv_store ivs ON ivs.id = cv.store_id "
                                + "WHERE "
                                + (superId == null ? " cv.manager_id = " + managerId : "cv.super_id = " + superId) + " "
                                + "AND DATE(cv.reg_date) = CURDATE() "
                                + "AND cv.store_id IS NULL AND cv.vehicle_id IS NULL "
                                + "";

                        MySQLQuery q = new MySQLQuery(str);
                        Object[][] dataNovelties = q.getRecords(conn);

                        JsonArrayBuilder jsonNovelty = Json.createArrayBuilder();

                        for (Object[] data : dataNovelties) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("id", MySQLQuery.getAsInteger(data[0]));

                            row.add("nameEmployee", (data[1] != null ? MySQLQuery.getAsString(data[1]) : "Sin nombre"));
                            row.add("nameStore", (data[2] != null ? MySQLQuery.getAsString(data[2]) : "Sin nombre"));

                            row.add("regDate", (data[3] != null ? sdfReg.format(MySQLQuery.getAsDate(data[3])) : "Sin fecha de registro"));
                            row.add("chkDate", (data[4] != null ? sdf.format(MySQLQuery.getAsDate(data[4])) : "Sin fecha de revisión"));

                            row.add("detail", (data[5] != null ? MySQLQuery.getAsString(data[5]) : "Sin Notas"));

                            jsonNovelty.add(row);
                        }

                        ob.add("dataNovelties", jsonNovelty);
                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "getVisits": {
                        int managerId = req.getInt("managerId");
                        int storeId = req.getInt("storeId");

                        String q = "SELECT "
                                + " cv.id, "
                                + " cv.reg_date, "
                                + " cv.chk_date, "
                                + " cv.detail, "
                                + " cv.store_id, "
                                + " IF(cv.super_id, CONCAT(sp.first_name,' ',sp.last_name),CONCAT(e.first_name,' ',e.last_name)) "
                                + " FROM com_novelty cv "
                                + " INNER JOIN employee e ON e.id = cv.manager_id "
                                + " LEFT JOIN inv_store ins ON ins.id = cv.store_id "
                                + " LEFT JOIN per_employee sp ON sp.id = cv.super_id "
                                + " WHERE IF(cv.store_id IS NULL,cv.lat ,ins.lat) > 0  "
                                + " AND e.id = " + managerId + " "
                                + " AND ins.id = " + storeId + " "
                                + " AND DATE(reg_date) = CURDATE() "
                                + " AND cv.store_id IS NOT NULL";

                        Object[][] dataVisits = new MySQLQuery(q).getRecords(conn);
                        JsonArrayBuilder jsonVisit = Json.createArrayBuilder();

                        for (Object[] data : dataVisits) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("visitId", MySQLQuery.getAsInteger(data[0]));
                            row.add("regDate", (data[1] != null ? sdfReg.format(MySQLQuery.getAsDate(data[1])) : "Sin fecha de registro"));
                            row.add("chkDate", (data[2] != null ? sdf.format(MySQLQuery.getAsDate(data[2])) : "Sin fecha de revisión"));
                            row.add("detail", (data[3] != null ? MySQLQuery.getAsString(data[3]) : "Sin Notas"));
                            row.add("storeId", MySQLQuery.getAsInteger(data[4]));
                            row.add("register", MySQLQuery.getAsString(data[5]));

                            jsonVisit.add(row);
                        }
                        ob.add("dataVisits", jsonVisit);
                        ob.add("msg", "consulta realizada con exito");
                    }
                    break;

                    case "syncNovelty": {
                        JsonArray dataSync = req.getJsonArray("dataSync");

                        for (int i = 0; i < dataSync.size(); i++) {
                            JsonObject item = dataSync.getJsonObject(i);
                            Integer managerId = getInt(item, "managerId");
                            Integer superId = getInt(item, "superId");
                            String regDate = (item.getString("regDate").equals("") ? null : "'" + item.getString("regDate") + "'");
                            String chkDate = (item.getString("chkDate").equals("") ? null : "'" + item.getString("chkDate") + "'");
                            String placeName = item.getString("placeName");
                            String detail = item.getString("detail");
                            Integer hasCoordinate = item.getInt("hasCoordinate");

                            String poolName = item.getString("poolName");
                            String tz = item.getString("tz");

                            Double latitude = item.getJsonNumber("latitude").doubleValue();
                            Double longitude = item.getJsonNumber("longitude").doubleValue();
                            boolean caseHlp = (getInt(item, "caseHlp").equals(1));

                            Integer idCaseHlp = null;
                            if (caseHlp) {
                                Integer idIncharge = getInt(item, "id_incharge");
                                idCaseHlp = getIdCasehlp(superId, managerId, detail, placeName, idIncharge, conn, poolName, tz);
                            }

                            String str = ""
                                    + "INSERT INTO com_novelty ("
                                    + " manager_id,"
                                    + " super_id,"
                                    + " place_name, "
                                    + " reg_date, "
                                    + " chk_date, "
                                    + " detail,lat,lon,has_coordinate,"
                                    + " hlp_request_id) VALUES ( "
                                    + managerId + ", "
                                    + superId + ", "
                                    + "'" + placeName + "', "
                                    + regDate + ", "
                                    + chkDate + ", "
                                    + "'" + detail + "', "
                                    + " " + latitude + ", "
                                    + " " + longitude + ","
                                    + " " + hasCoordinate + ", "
                                    + " " + idCaseHlp + " "
                                    + " )";

                            MySQLQuery q = new MySQLQuery(str);
                            q.executeInsert(conn);
                        }

                        ob.add("msg", "Sincronización exitosa");
                        break;
                    }
                    case "registerNovelty": {
                        Integer managerId = getInt(req, "managerId");
                        Integer superId = getInt(req, "superId");
                        String chkDate = (req.getString("chkDate").equals("") ? null : "'" + req.getString("chkDate") + "'");
                        String detail = req.getString("detail");
                        boolean caseHlp = req.getString("caseHlp").equals("true");

                        String poolName = req.getString("poolName");
                        String tz = req.getString("tz");

                        String noveltyType = (req.getString("noveltyType") == null ? " " : req.getString("noveltyType"));
                        Integer idCaseHlp = null;
                        Integer idIncharge = null;

                        String str;
                        switch (noveltyType) {
                            case "vehicle":
                                Integer vehicleId = req.getInt("vehicleId");

                                if (caseHlp) {
                                    String subject = new MySQLQuery("SELECT CONCAT('Caso Vehículo: ', v.plate, ' - Interno: ' ,COALESCE(v.internal,'Sin Interno')  ) FROM vehicle v WHERE  v.id = " + vehicleId).getAsString(conn);
                                    idIncharge = getInt(req, "id_incharge");
                                    idCaseHlp = getIdCasehlp(superId, managerId, detail, subject, idIncharge, conn, poolName, tz);
                                }

                                str = "INSERT INTO com_novelty "
                                        + "(manager_id, super_id , reg_date, "
                                        + (chkDate == null ? " " : "chk_date, ")
                                        + "detail,vehicle_id,hlp_request_id ) VALUES ( "
                                        + managerId + ", "
                                        + superId + ", "
                                        + " NOW(), "
                                        + (chkDate == null ? "" : chkDate + ", ")
                                        + " '" + detail + "', "
                                        + " " + vehicleId + ", "
                                        + " " + idCaseHlp + ""
                                        + " )";
                                break;
                            default:
                                Integer hasCoordinate = req.getInt("hasCoordinate");
                                String placeName = req.getString("placeName");
                                Double latitude = null;
                                Double longitude = null;

                                if (hasCoordinate == 1) {
                                    latitude = req.getJsonNumber("latitude").doubleValue();
                                    longitude = req.getJsonNumber("longitude").doubleValue();
                                }

                                if (caseHlp) {
                                    idIncharge = getInt(req, "id_incharge");
                                    idCaseHlp = getIdCasehlp(superId, managerId, detail, placeName, idIncharge, conn, poolName, tz);
                                }

                                str = "INSERT INTO com_novelty "
                                        + "(manager_id,super_id,place_name, "
                                        + "reg_date, "
                                        + (chkDate == null ? " " : "chk_date, ")
                                        + "detail,lat,lon,has_coordinate, hlp_request_id ) VALUES ( "
                                        + managerId + ", "
                                        + superId + ", "
                                        + " '" + placeName + "', "
                                        + " NOW(), "
                                        + (chkDate == null ? "" : chkDate + ", ")
                                        + " '" + detail + "', "
                                        + " " + latitude + ", "
                                        + " " + longitude + ", "
                                        + " " + hasCoordinate + ", "
                                        + " " + idCaseHlp + ""
                                        + " )";
                                break;
                        }

                        MySQLQuery q = new MySQLQuery(str);
                        q.executeInsert(conn);

                        ob.add("msg", "Novedad registrada");
                        break;
                    }

                    case "getVehicleDocuments": {
                        int id = req.getInt("id");

                        String q = "SELECT "
                                + " dv.id, "
                                + " d.description, "
                                + " dv.fecha, "
                                + " IF(TIMESTAMPDIFF(DAY,dv.fecha,CURRENT_TIMESTAMP())>0, 1 , 0 ) "
                                + " FROM document AS d "
                                + " INNER JOIN document_vehicle AS dv ON dv.doc_id = d.id "
                                + " WHERE dv.vehicle_id = " + id + " GROUP BY dv.id ";

                        Object[][] dataStore = new MySQLQuery(q).getRecords(conn);

                        JsonArrayBuilder jsonDocument = Json.createArrayBuilder();

                        for (Object[] data : dataStore) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("description", (data[1] != null ? MySQLQuery.getAsString(data[1]) : "Sin descripción"));
                            row.add("date", (data[2] != null ? MySQLQuery.getAsString(data[2]) : "No vence"));
                            row.add("expire", (MySQLQuery.getAsInteger(data[3])));

                            jsonDocument.add(row);
                        }
                        ob.add("dataDocument", jsonDocument);

                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "getDocuments": {
                        int id = req.getInt("id");
                        String q = "SELECT "
                                + "v.id, "
                                + "v.plate, "
                                + "SUM(IF(TIMESTAMPDIFF(DAY,dv.fecha, CURRENT_TIMESTAMP())>0, 1, 0)) AS docs "
                                + "FROM com_service_manager AS sm "
                                + "INNER JOIN com_man_veh AS mv ON mv.man_id = sm.id "
                                + "INNER JOIN vehicle AS v ON v.id = mv.veh_id "
                                + "INNER JOIN document_vehicle AS dv ON dv.vehicle_id = v.id "
                                + "WHERE sm.id = " + id + " "
                                + "GROUP BY v.id";

                        Object[][] dataStore = new MySQLQuery(q).getRecords(conn);

                        JsonArrayBuilder jsonDocument = Json.createArrayBuilder();

                        for (Object[] data : dataStore) {
                            if (MySQLQuery.getAsInteger(data[2]) > 0) {
                                JsonObjectBuilder row = Json.createObjectBuilder();
                                row.add("id", MySQLQuery.getAsInteger(data[0]));
                                row.add("plate", (data[1] != null ? MySQLQuery.getAsString(data[1]) : "Sin Placa"));
                                row.add("numDocs", (MySQLQuery.getAsInteger(data[2])));

                                jsonDocument.add(row);
                            }
                        }
                        ob.add("dataDocument", jsonDocument);

                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "getVehicleNovelty": {
                        int id = req.getInt("id");
                        int managerId = req.getInt("managerId");
                        String qEvent = "SELECT "
                                + " cn.id, cn.detail, cn.reg_date , cn.chk_date, "
                                + " IF(cn.super_id, CONCAT(sp.first_name,' ',sp.last_name),CONCAT(e.first_name,' ',e.last_name)) "
                                + " FROM com_novelty cn "
                                + " INNER JOIN employee e ON e.id = cn.manager_id "
                                + " LEFT JOIN per_employee sp ON sp.id = cn.super_id "
                                + " WHERE "
                                + " cn.vehicle_id = " + id + " "
                                + " AND cn.manager_id = " + managerId + " AND cn.chk_date IS NULL ";

                        Object[][] dataNovely = new MySQLQuery(qEvent).getRecords(conn);

                        JsonArrayBuilder jsonNovelty = Json.createArrayBuilder();

                        for (Object[] data : dataNovely) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("note", MySQLQuery.getAsString(data[1]));
                            row.add("regDate", (data[2] != null ? sdfRegAm.format(MySQLQuery.getAsDate(data[2])) : "Sin fecha de registro"));
                            row.add("chkDate", (data[3] != null ? sdfChk.format(MySQLQuery.getAsDate(data[3])) : "Sin fecha de revisión"));
                            row.add("register", MySQLQuery.getAsString(data[4]));
                            jsonNovelty.add(row);
                        }
                        ob.add("dataNovelty", jsonNovelty);

                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "getGoalsMonth": {
                        int id = req.getInt("vehicleId");
                        String dateMax = req.getString("dateMax");
                        String dateMin = req.getString("dateMin");
                        String qEvent = "SELECT v.id, v.internal, v.plate, v.zone_type, z.name, "
                                + "IF(v.zone_type = 'rul', z.perc_rul, z.perc_urb), SUM(ct.kg) AS kg_total, "
                                + "(SELECT SUM(ct1.kg) FROM trk_sale ts1 LEFT JOIN trk_multi_cyls tmc1 ON tmc1.sale_id = ts1.id AND tmc1.`type` = 'del' LEFT JOIN trk_cyl tc1 ON tc1.id = IF(ts1.sale_type = 'mul',tmc1.cyl_id, ts1.cylinder_id) INNER JOIN cylinder_type ct1 ON ct1.id = tc1.cyl_type_id  WHERE ts1.vehicle_id = v.id AND DATE(ts1.date) = CURDATE()) "
                                + "AS kg_day, "
                                + "vt.cap, "
                                + "IF(cmv.road_veh, NULL , vt.goals_month) "
                                + "FROM trk_sale ts "
                                + "LEFT JOIN trk_multi_cyls tmc ON tmc.sale_id = ts.id AND tmc.`type` = 'del' "
                                + "LEFT JOIN trk_cyl tc ON tc.id = IF(ts.sale_type = 'mul',tmc.cyl_id, ts.cylinder_id) "
                                + "INNER JOIN cylinder_type ct ON ct.id = tc.cyl_type_id "
                                + "INNER JOIN vehicle v ON v.id = ts.vehicle_id "
                                + "INNER JOIN vehicle_type vt ON vt.id = v.vehicle_type_id "
                                + "INNER JOIN agency a ON a.id = v.agency_id "
                                + "INNER JOIN city c ON c.id = a.city_id "
                                + "INNER JOIN zone z ON z.id = c.zone_id "
                                + "INNER JOIN com_man_veh cmv ON cmv.veh_id = v.id "
                                + "WHERE v.id = " + id + " "
                                + "AND ts.date BETWEEN '" + dateMin + "' AND '" + dateMax + "' ";//FECHAS

                        Object[][] dataNovely = new MySQLQuery(qEvent).getRecords(conn);

                        JsonObjectBuilder row = Json.createObjectBuilder();
                        row.add("vhId", (dataNovely[0][0] != null ? MySQLQuery.getAsInteger(dataNovely[0][0]) : 0));
                        row.add("internal", (dataNovely[0][1] != null ? MySQLQuery.getAsString(dataNovely[0][1]) : "Sin Interno"));
                        row.add("plate", (dataNovely[0][2] != null ? MySQLQuery.getAsString(dataNovely[0][2]) : "0"));
                        row.add("zoneType", (dataNovely[0][3] != null ? (MySQLQuery.getAsString(dataNovely[0][3]).equals("urb") ? "Urbano" : "Rural") : "Sin tipo"));
                        row.add("zoneName", (dataNovely[0][4] != null ? MySQLQuery.getAsString(dataNovely[0][4]) : "Sin zona"));
                        row.add("percZone", (dataNovely[0][5] != null ? MySQLQuery.getAsInteger(dataNovely[0][5]) : 0));
                        row.add("kgMonth", (dataNovely[0][6] != null ? MySQLQuery.getAsInteger(dataNovely[0][6]) : 0));
                        row.add("kgDay", (dataNovely[0][7] != null ? MySQLQuery.getAsInteger(dataNovely[0][7]) : 0));
                        row.add("capacity", MySQLQuery.getAsBigDecimal(dataNovely[0][8], true));
                        row.add("goalsMonth", MySQLQuery.getAsBigDecimal(dataNovely[0][9], true));

                        ob.add("dataGoals", row);

                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "getManagerGoals": {
                        int id = req.getInt("managerId");
                        String dateMax = MySQLQuery.scape(req.getString("dateMax"));
                        String dateMin = MySQLQuery.scape(req.getString("dateMin"));

                        String qEvent = "SELECT v.id, v.internal, v.plate, v.zone_type, z.name, "
                                + "IF(v.zone_type = 'rul', z.perc_rul, z.perc_urb), SUM(ct.kg) AS kg_month, "
                                + "(SELECT SUM(ct1.kg) FROM trk_sale ts1 LEFT JOIN trk_multi_cyls tmc1 ON "
                                + "tmc1.sale_id = ts1.id AND tmc1.`type` = 'del' LEFT JOIN trk_cyl tc1 ON "
                                + "tc1.id = IF(ts1.sale_type = 'mul',tmc1.cyl_id, ts1.cylinder_id) INNER JOIN "
                                + "cylinder_type ct1 ON ct1.id = tc1.cyl_type_id  WHERE ts1.vehicle_id = cmv.veh_id "
                                + "AND DATE(ts1.date) = CURDATE()) "
                                + "AS kg_day, "
                                + "vt.cap, "
                                + "vt.goals_month "
                                + "FROM com_man_veh cmv "
                                + "LEFT JOIN vehicle v ON v.id = cmv.veh_id "
                                + "LEFT JOIN trk_sale ts ON ts.vehicle_id = v.id "
                                + "AND ts.date BETWEEN '" + dateMin + "' AND '" + dateMax + "' "//FECHAS
                                + "LEFT JOIN trk_multi_cyls tmc ON tmc.sale_id = ts.id AND tmc.`type` = 'del' "
                                + "LEFT JOIN trk_cyl tc ON tc.id = IF(ts.sale_type = 'mul',tmc.cyl_id, ts.cylinder_id) "
                                + "LEFT JOIN cylinder_type ct ON ct.id = tc.cyl_type_id "
                                + "LEFT JOIN vehicle_type vt ON vt.id = v.vehicle_type_id "
                                + "INNER JOIN agency a ON a.id = v.agency_id "
                                + "INNER JOIN city c ON c.id = a.city_id "
                                + "INNER JOIN zone z ON z.id = c.zone_id "
                                + "WHERE cmv.man_id = " + id + " "
                                + "AND !cmv.road_veh "
                                + "GROUP BY v.id";

                        Object[][] dataGoals = new MySQLQuery(qEvent).getRecords(conn);

                        JsonArrayBuilder jsonGoals = Json.createArrayBuilder();

                        for (Object[] data : dataGoals) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("vhId", (data[0] != null ? MySQLQuery.getAsInteger(data[0]) : 0));
                            row.add("internal", (data[1] != null ? MySQLQuery.getAsString(data[1]) : "Sin Interno"));
                            row.add("plate", (data[2] != null ? MySQLQuery.getAsString(data[2]) : "0"));
                            row.add("zoneType", (data[3] != null ? (MySQLQuery.getAsString(data[3]).equals("urb") ? "Urbano" : "Rural") : "Sin tipo"));
                            row.add("zoneName", (data[4] != null ? MySQLQuery.getAsString(data[4]) : "Sin zona"));
                            row.add("percZone", (data[5] != null ? MySQLQuery.getAsInteger(data[5]) : 0));
                            row.add("kgMonth", (data[6] != null ? MySQLQuery.getAsInteger(data[6]) : 0));
                            row.add("kgDay", (data[7] != null ? MySQLQuery.getAsInteger(data[7]) : 0));
                            row.add("capacity", MySQLQuery.getAsBigDecimal(data[8], true));
                            row.add("goalsMonth", MySQLQuery.getAsBigDecimal(data[9], true));
                            jsonGoals.add(row);
                        }
                        ob.add("dataGoals", jsonGoals);

                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "getSuperGoals": {
                        int id = req.getInt("superId");
                        String dateMax = MySQLQuery.scape(req.getString("dateMax"));
                        String dateMin = MySQLQuery.scape(req.getString("dateMin"));

                        String qEvent = "SELECT v.id, v.internal, v.plate, v.zone_type, z.name, "
                                + "IF(v.zone_type = 'rul', z.perc_rul, z.perc_urb), SUM(ct.kg) AS kg_total, "
                                + "(SELECT SUM(ct1.kg) FROM trk_sale ts1 LEFT JOIN trk_multi_cyls tmc1 ON "
                                + "tmc1.sale_id = ts1.id AND tmc1.`type` = 'del' LEFT JOIN trk_cyl tc1 ON "
                                + "tc1.id = IF(ts1.sale_type = 'mul',tmc1.cyl_id, ts1.cylinder_id) INNER JOIN "
                                + "cylinder_type ct1 ON ct1.id = tc1.cyl_type_id  WHERE ts1.vehicle_id = cmv.veh_id "
                                + "AND DATE(ts1.date) = CURDATE()) "
                                + "AS kg_day, "
                                + "vt.cap, "
                                + "vt.goals_month "
                                + "FROM com_man_veh cmv "
                                + "INNER JOIN com_service_manager csm ON cmv.man_id = csm.id "
                                + "INNER JOIN com_super_manager csup ON csm.com_super_id = csup.id AND csup.is_coordinator = 0 "
                                + "INNER JOIN vehicle v ON v.id = cmv.veh_id "
                                + "LEFT JOIN trk_sale ts ON ts.vehicle_id = v.id "
                                + "AND ts.date BETWEEN '" + dateMin + "' AND '" + dateMax + "' " //FECHAS
                                + "LEFT JOIN trk_multi_cyls tmc ON tmc.sale_id = ts.id AND tmc.`type` = 'del' "
                                + "LEFT JOIN trk_cyl tc ON tc.id = IF(ts.sale_type = 'mul',tmc.cyl_id, ts.cylinder_id) "
                                + "LEFT JOIN cylinder_type ct ON ct.id = tc.cyl_type_id "
                                + "LEFT JOIN vehicle_type vt ON vt.id = v.vehicle_type_id "
                                + "INNER JOIN agency a ON a.id = v.agency_id "
                                + "INNER JOIN city c ON c.id = a.city_id "
                                + "INNER JOIN zone z ON z.id = c.zone_id "
                                + "WHERE csup.id = " + id + " "
                                + "AND !cmv.road_veh "
                                + "GROUP BY v.id";

                        Object[][] dataGoals = new MySQLQuery(qEvent).getRecords(conn);

                        JsonArrayBuilder jsonGoals = Json.createArrayBuilder();

                        for (Object[] data : dataGoals) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("vhId", (data[0] != null ? MySQLQuery.getAsInteger(data[0]) : 0));
                            row.add("internal", (data[1] != null ? MySQLQuery.getAsString(data[1]) : "Sin Interno"));
                            row.add("plate", (data[2] != null ? MySQLQuery.getAsString(data[2]) : "0"));
                            row.add("zoneType", (data[3] != null ? (MySQLQuery.getAsString(data[3]).equals("urb") ? "Urbano" : "Rural") : "Sin tipo"));
                            row.add("zoneName", (data[4] != null ? MySQLQuery.getAsString(data[4]) : "Sin zona"));
                            row.add("percZone", (data[5] != null ? MySQLQuery.getAsInteger(data[5]) : 0));
                            row.add("kgMonth", (data[6] != null ? MySQLQuery.getAsInteger(data[6]) : 0));
                            row.add("kgDay", (data[7] != null ? MySQLQuery.getAsInteger(data[7]) : 0));
                            row.add("capacity", MySQLQuery.getAsBigDecimal(data[8], true));
                            row.add("goalsMonth", MySQLQuery.getAsBigDecimal(data[9], true));
                            jsonGoals.add(row);
                        }
                        ob.add("dataGoals", jsonGoals);

                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "getMgrCoordinates": {
                        int id = req.getInt("id");
                        String imei = MySQLQuery.scape(req.getString("imei"));
                        String date = MySQLQuery.scape(req.getString("date"));

                        String q = "SELECT "
                                + " IF(cv.store_id IS NULL,cv.lat ,ins.lat), "
                                + " IF(cv.store_id IS NULL,cv.lon ,ins.lon), "
                                + " cv.reg_date, "
                                + " IF(cv.store_id IS NULL, cv.place_name,(CONCAT(ins.first_name, ' ', ins.last_name))), "
                                + " cv.detail, "
                                + " cv.chk_date, "
                                + " ins.address, "
                                + " ins.internal, "
                                + " ct.id "
                                + " FROM com_novelty cv "
                                + " INNER JOIN employee e ON e.id = cv.manager_id "
                                + " LEFT JOIN inv_store ins ON ins.id = cv.store_id "
                                + " LEFT JOIN inv_center AS ct ON ct.id = ins.center_id "
                                + " WHERE IF(cv.store_id IS NULL,cv.lat ,ins.lat) > 0  "
                                + " AND e.id = " + id + " "
                                + " AND DATE(reg_date) = CURDATE() ";

                        String visit = " AND cv.store_id IS NOT NULL";
                        String novelty = " AND cv.store_id IS NULL AND cv.has_coordinate = 1";

                        String pointsCoord = " SELECT DISTINCT "
                                + " latitude, "
                                + " longitude, "
                                + " date "
                                + " FROM gps_coordinate g "
                                + " INNER JOIN session_login s ON s.id = g.session_id WHERE accuracy <= 150 "
                                + " AND g.employee_id = " + id + " "
                                + "AND s.employee_id = " + id + "  "
                                + " AND phone = '" + imei + "' "
                                + " AND s.app_id = (SELECT id FROM system_app WHERE package_name = 'com.glp.servicemanagers') "
                                + " AND g.`date` BETWEEN '" + date + " 00:00:00' AND '" + date + " 23:59:59' "
                                + " AND g.latitude != 0 "
                                + " ORDER BY date ";

                        Object[][] dataVisitis = new MySQLQuery(q + visit).getRecords(conn);
                        Object[][] dataNovelties = new MySQLQuery(q + novelty).getRecords(conn);
                        Object[][] dataPoints = new MySQLQuery(pointsCoord).getRecords(conn);

                        JsonArrayBuilder jsonVisit = Json.createArrayBuilder();
                        JsonArrayBuilder jsonNovelty = Json.createArrayBuilder();
                        JsonArrayBuilder jsonPoint = Json.createArrayBuilder();

                        //para validar coor
                        //BigDecimal lat = MySQLQuery.getAsBigDecimal(data[0], true);//para 
                        //BigDecimal lon = MySQLQuery.getAsBigDecimal(data[1], true);
                        for (Object[] data : dataVisitis) {
                            JsonObjectBuilder row = Json.createObjectBuilder();

                            row.add("lat", String.valueOf(MySQLQuery.getAsBigDecimal(data[0], true)));
                            row.add("lon", String.valueOf(MySQLQuery.getAsBigDecimal(data[1], true)));
                            row.add("regDate", (data[2] != null ? MySQLQuery.getAsString(data[2]) : ""));
                            row.add("storeName", (data[3] != null ? MySQLQuery.getAsString(data[3]) : ""));
                            row.add("notes", (data[4] != null ? MySQLQuery.getAsString(data[4]) : ""));
                            row.add("chkDate", (data[5] != null ? MySQLQuery.getAsString(data[5]) : "Sin revisión"));
                            row.add("address", (data[6] != null ? MySQLQuery.getAsString(data[6]) : ""));
                            row.add("internal", (data[7] != null ? MySQLQuery.getAsString(data[7]) : ""));
                            jsonVisit.add(row);
                        }

                        for (Object[] data : dataNovelties) {
                            JsonObjectBuilder row = Json.createObjectBuilder();

                            row.add("lat", String.valueOf(MySQLQuery.getAsBigDecimal(data[0], true)));
                            row.add("lon", String.valueOf(MySQLQuery.getAsBigDecimal(data[1], true)));
                            row.add("regDate", (data[2] != null ? MySQLQuery.getAsString(data[2]) : ""));
                            row.add("storeName", (data[3] != null ? MySQLQuery.getAsString(data[3]) : ""));
                            row.add("notes", (data[4] != null ? MySQLQuery.getAsString(data[4]) : ""));
                            row.add("chkDate", (data[5] != null ? MySQLQuery.getAsString(data[5]) : "Sin revisión"));
                            row.add("address", (data[6] != null ? MySQLQuery.getAsString(data[6]) : ""));
                            row.add("internal", (data[7] != null ? MySQLQuery.getAsString(data[7]) : ""));
                            jsonNovelty.add(row);
                        }

                        for (Object[] data : dataPoints) {
                            JsonObjectBuilder row = Json.createObjectBuilder();

                            row.add("lat", String.valueOf(MySQLQuery.getAsBigDecimal(data[0], true)));
                            row.add("lon", String.valueOf(MySQLQuery.getAsBigDecimal(data[1], true)));
                            row.add("regDate", (data[2] != null ? MySQLQuery.getAsString(data[2]) : ""));
                            jsonPoint.add(row);
                        }

                        ob.add("visitCoordinates", jsonVisit);
                        ob.add("noveltyCoordinates", jsonNovelty);
                        ob.add("points", jsonPoint);

                        ob.add("msg", "coordenadas obtenidas");
                        break;
                    }

                    case "registEvent": {
                        Integer event = req.getInt("event");
                        Integer employee = req.getInt("employee");

                        String str = "INSERT INTO com_man_history_event ( "
                                + (event == 0 ? " " : " event_type_id, ")
                                + " employee_id, "
                                + " reg_date "
                                + " ) VALUES ( "
                                + (event == 0 ? " " : event + ", ")
                                + employee + ", "
                                + " NOW() )";

                        new MySQLQuery(str).executeInsert(conn);

                        ob.add("msg", "Desconexion exitosa");
                        break;
                    }

                    case "validateSaleInZone": {
                        Integer vehicleId = req.getInt("vehicleId");
                        int outZone = 0;

                        MySQLQuery sale = new MySQLQuery("SELECT "
                                + "ts.lat, ts.lon "
                                + "FROM trk_sale ts "
                                + "INNER JOIN driver_vehicle dv ON dv.driver_id = ts.emp_id AND dv.`end` IS NULL "
                                + "WHERE DATE(ts.date) = CURDATE() AND dv.vehicle_id = " + vehicleId + " ");
                        Object[][] dataSale = sale.getRecords(conn);

                        if (dataSale != null) {
                            MySQLQuery q = new MySQLQuery("SELECT "
                                    + "g.lat, g.lon FROM gps_polygon g "
                                    + "INNER JOIN sector s ON s.id = g.owner_id "
                                    + "INNER JOIN ord_vehicle_office vo ON vo.sector_id = s.id "
                                    + "INNER JOIN vehicle v ON v.id = vo.vehicle_id "
                                    + "WHERE v.id = " + vehicleId + " ");

                            Object[][] dataPoints = q.getRecords(conn);

                            if (dataPoints != null && dataPoints.length > 0) {
                                double[] xPoints = new double[dataPoints.length];
                                double[] yPoints = new double[dataPoints.length];

                                for (int i = 0; i < dataPoints.length; i++) {
                                    xPoints[i] = MySQLQuery.getAsDouble(dataPoints[i][0]);
                                    yPoints[i] = MySQLQuery.getAsDouble(dataPoints[i][1]);
                                }

                                Path2D.Double d = new Path2D.Double();
                                d.moveTo(xPoints[0], yPoints[0]);
                                for (int i = 1; i < dataPoints.length; i++) {
                                    d.lineTo(xPoints[i], yPoints[i]);
                                }
                                d.closePath();

                                Rectangle2D enveloped = d.getBounds2D();

                                for (Object[] dataSale1 : dataSale) {
                                    if (enveloped.contains(MySQLQuery.getAsDouble(dataSale1[0]),
                                            MySQLQuery.getAsDouble(dataSale1[1]))) {
                                        if (!d.contains(MySQLQuery.getAsDouble(dataSale1[0]),
                                                MySQLQuery.getAsDouble(dataSale1[1]))) {
                                            outZone += 1;
                                        }
                                    } else {
                                        outZone += 1;
                                    }
                                }
                            }
                        }
                        ob.add("outZone", outZone);
                        ob.add("msg", "Consulta exitosa");
                        break;
                    }

                    default:
                        break;
                }
                ob.add("result", "OK");
            } catch (Exception ex) {
                Logger.getLogger(FindStoreV6.class.getName()).log(Level.SEVERE, null, ex);
                String m = ex.getMessage();
                ob.add("result", "ERROR, " + m);
                if (m != null && !m.isEmpty()) {
                    ob.add("errorMsg", m);
                } else {
                    ob.add("errorMsg", "Error desconocido.");
                }
            } finally {
                w.writeObject(ob.build());
                MySQLCommon.closeConnection(conn);
            }
        } catch (Exception ex) {
            Logger.getLogger(FindStoreV6.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySQLSelect.tryClose(conn);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(FindStoreV6.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(FindStoreV6.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "Find Contract";
    }

    private JsonArrayBuilder getStock(Integer id, Connection conn) throws Exception {

        JsonArrayBuilder jsonStock = Json.createArrayBuilder();

        Object[][] stock = new MySQLQuery("SELECT "
                + "ct.name,SUM(a.amount) " //0
                + "FROM inv_movement m "
                + "INNER JOIN inv_mov_amount a ON a.mov_id = m.id "
                + "INNER JOIN inv_mv_type t ON t.id = m.type_id "
                + "INNER JOIN cylinder_type  ct ON ct.id = a.capa_id "
                + "WHERE m.cancel = 0 AND m.store_id = " + id + " "
                + "AND m.mv_date <= NOW() "
                + "AND t.ajeno = 0 AND t.provi = 0 "
                + "GROUP BY a.capa_id, a.type_id "
                + "HAVING SUM(a.amount) <> 0 ").getRecords(conn);

        for (Object[] data : stock) {
            JsonObjectBuilder row = Json.createObjectBuilder();
            row.add("name", MySQLQuery.getAsString(data[0]));
            row.add("amount", (MySQLQuery.getAsInteger(data[1]) < 0 ? (MySQLQuery.getAsInteger(data[1]) * -1) : MySQLQuery.getAsInteger(data[1])));
            jsonStock.add(row);
        }

        return jsonStock;
    }

    private Integer getInt(JsonObject obj, String fieldName) {
        if (!obj.containsKey(fieldName)) {
            return null;
        }
        if (obj.isNull(fieldName)) {
            return null;
        }
        return obj.getJsonNumber(fieldName).intValue();
    }

    private int getIdCasehlp(Integer superId, Integer managerId, String detail, String subject,
            Integer idIncharge, Connection conn, String poolName, String tz) throws Exception {

        Integer typeId = new MySQLQuery("SELECT t.id  FROM hlp_type t WHERE t.is_com_manager LIMIT 1").getAsInteger(conn);
        Integer idInchargeEmp = new MySQLQuery("SELECT e.id FROM employee e WHERE e.per_employee_id = " + idIncharge + " AND e.active = 1 ").getAsInteger(conn);

        if (idInchargeEmp == null) {
            throw new Exception("El encargado debe tener un usuario en el sistema");
        }

        if (typeId == null) {
            throw new Exception("Se debe definir un Tipo de Caso Comercial en Mesa de Ayuda");
        }

        if (superId != null) {
            superId = new MySQLQuery("SELECT e.id FROM employee e WHERE e.per_employee_id = " + superId + " AND e.active").getAsInteger(conn);
        }

        int idCaseHlp = new MySQLQuery("INSERT INTO hlp_request "
                + "SET `employee_id` = " + (superId != null ? superId : managerId) + ", "
                + "`created_by` = " + (superId != null ? superId : managerId) + ", "
                + "`type_id` = " + typeId + ", "
                + "`reg_date` = NOW(), "
                + "`beg_date` = NOW(), "
                + "`state` = 'prog', "
                + "`user_type` = 'emp', "
                + "`priority` = 'hig', "
                + "`notes` = '" + MySQLQuery.scape(detail) + "', "
                + "`subject` = '" + MySQLQuery.scape(subject) + "',  "
                + "in_charge = " + idIncharge + ", "
                + "in_charge_emp= " + idInchargeEmp + "").executeInsert(conn);

        String query = "INSERT INTO hlp_suscriptor (case_id, sup_emp_id, active) "
                + "VALUES (" + idCaseHlp + ", " + (superId != null ? superId : managerId) + ", 1);";
        new MySQLQuery(query).executeInsert(conn);//suscriptor

        int appId = new MySQLQuery("SELECT id FROM system_app WHERE package_name = 'com.qualisys.helpdesk'").getAsInteger(conn);

        JsonObjectBuilder ob = Json.createObjectBuilder();
        ob.add("subject", subject);
        ob.add("message", "Le fue asignado el caso No. " + idCaseHlp + " con las siguientes caracteristicas:\n\n" + detail);
        ob.add("user", "HelpDesk");
        ob.add("dt", Dates.getCheckFormat().format(new Date()));
        GCMHlp.sendToHlpApp(appId, ob.build(), poolName, tz, MySQLQuery.getAsString(idInchargeEmp));

        return idCaseHlp;
    }
}
