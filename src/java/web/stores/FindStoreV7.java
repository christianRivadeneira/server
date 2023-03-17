package web.stores;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import utilities.JsonUtils;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "FindStoreV7", urlPatterns = {"/FindStoreV7"})
public class FindStoreV7 extends HttpServlet {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static final SimpleDateFormat sdfReg = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss aaa");
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

                    case "findEstClient": {
                        int idAccExec = req.getInt("idAccExec");
                        String str = "SELECT o.id, o.name, o.document, o.id, o.address, "
                                + "(SELECT COUNT(*) FROM est_tank t WHERE t.client_id = o.id), "
                                + "IFNULL(c.id, -1), "
                                + "(SELECT COUNT(*) FROM crm_task t WHERE t.client_id = c.id "
                                + "AND t.ejec_date IS NULL) "
                                + "FROM ord_tank_client o "
                                + "LEFT JOIN est_prospect e ON e.client_id = o.id "
                                + "LEFT JOIN crm_client c ON c.prospect_id = e.id "
                                + "WHERE o.active AND IFNULL(e.active,true) AND "
                                + "o.exec_reg_id = " + idAccExec + " ";

                        Object[][] dataEstClient = new MySQLQuery(str).getRecords(conn);
                        JsonArrayBuilder jsonStore = Json.createArrayBuilder();

                        for (Object[] data : dataEstClient) {
                            JsonObjectBuilder row = Json.createObjectBuilder();

                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("name", MySQLQuery.getAsString(data[1]));
                            row.add("document", MySQLQuery.getAsString(data[2]));
                            row.add("cta", MySQLQuery.getAsString(data[3]));
                            row.add("address", (data[4] != null ? MySQLQuery.getAsString(data[4]) : "Sin direccion"));
                            row.add("numTank", MySQLQuery.getAsInteger(data[5]));
                            row.add("clientId", MySQLQuery.getAsInteger(data[6]));
                            row.add("taskAmount", MySQLQuery.getAsBigInteger(data[7]));

                            jsonStore.add(row);
                        }

                        ob.add("dataEstClient", jsonStore);
                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "findStores": {

                        int id = req.getInt("id");
                        boolean isSuper = req.getBoolean("isSuper");
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
                                + "i.update_date, "
                                + "(SELECT COUNT(*) FROM crm_task t WHERE t.store_id = i.id AND t.ejec_date IS NULL) "
                                + "FROM "
                                + "com_service_manager AS sm "
                                + "INNER JOIN com_man_store AS ms ON ms.man_id = sm.id "
                                + "INNER JOIN inv_store i ON i.id = ms.store_id "
                                + "INNER JOIN city d ON i.city_id = d.id "
                                + "LEFT JOIN inv_store_type invt ON invt.id = i.type_id "
                                + "LEFT JOIN inv_center invc ON invc.id = i.center_id "
                                + (isSuper ? "INNER JOIN com_super_manager cm ON cm.id = sm.com_super_id WHERE cm.emp_id = " + id : "WHERE sm.emp_id = " + id + " ");
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
                            row.add("taskAmount", MySQLQuery.getAsInteger(data[12]));
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

                        boolean isSuper = req.getBoolean("isSuper");
                        int id = req.getInt("id");
                        Integer vhId = req.getInt("vhId");
                        String str = "SELECT vh.id, vh.plate, vh.internal, "
                                + "IFNULL(CONCAT(e.first_name, ' ',e.last_name) , 'Sin Conductor'), "
                                + "(SELECT COUNT(*) FROM crm_task t WHERE t.vehicle_id = vh.id AND t.ejec_date IS NULL) "
                                + "FROM com_service_manager AS sm "
                                + "INNER JOIN com_man_veh AS mv ON mv.man_id = sm.id "
                                + "INNER JOIN vehicle AS vh ON vh.id = mv.veh_id "
                                + "LEFT JOIN driver_vehicle AS dv ON dv.vehicle_id = vh.id AND dv.`end` IS NULL "
                                + "LEFT JOIN employee AS e ON e.id = dv.driver_id "
                                + (isSuper ? "INNER JOIN com_super_manager cm ON cm.id = sm.com_super_id WHERE cm.emp_id = " + id
                                        : ("WHERE " + (vhId != 0 ? "vh.id = " + vhId + " " : " ") + (id != 0 ? "sm.emp_id = " + id + " " : " ")));

                        Object[][] dataVehicle = new MySQLQuery(str).getRecords(conn);

                        JsonArrayBuilder jsonVehicle = Json.createArrayBuilder();

                        for (Object[] data : dataVehicle) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("plate", (data[1] != null ? MySQLQuery.getAsString(data[1]) : "Sin placa"));
                            row.add("internal", (data[2] != null ? MySQLQuery.getAsString(data[2]) : "Sin interno"));
                            row.add("driver", (data[3] != null ? MySQLQuery.getAsString(data[3]) : "Sin conductor"));
                            row.add("taskAmount", MySQLQuery.getAsInteger(data[4]));

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
                        Integer estClientId = getInt(req, "estClientId");
                        Integer accExecId = getInt(req, "accExecId");
                        Integer storeId = getInt(req, "storeId");

                        Integer hasCoordinate = req.getInt("hasCoordinate");
                        Double latitude = null;
                        Double longitude = null;
                        if (hasCoordinate == 1) {
                            latitude = req.getJsonNumber("latitude").doubleValue();
                            longitude = req.getJsonNumber("longitude").doubleValue();
                        }

                        String chkDate = (req.getString("chkDate").equals("") ? null : "'" + req.getString("chkDate") + "'");
                        String detail = req.getString("detail");

                        String str = "INSERT INTO com_novelty SET ";
                        if (storeId != null) {
                            str += "`manager_id` = " + managerId + ", "
                                    + "`store_id` = " + storeId + ", "
                                    + "`super_id` = " + superId + ", "
                                    + "`reg_date` = NOW(), "
                                    + "`chk_date` = " + chkDate + ", "
                                    + "`detail` = '" + detail + "', "
                                    + "`lat` = " + latitude + ", "
                                    + "`lon` = " + longitude + ", "
                                    + "`has_coordinate` = " + hasCoordinate + " ";

                        } else {
                            str += "`acc_exec_id` = " + accExecId + ", "
                                    + "`est_client_id` = " + estClientId + ", "
                                    + "`reg_date` = NOW(), "
                                    + "`chk_date` = " + chkDate + ", "
                                    + "`detail` = '" + detail + "', "
                                    + "`lat` = " + latitude + ", "
                                    + "`lon` = " + longitude + ", "
                                    + "`has_coordinate` = " + hasCoordinate + " ";
                        }

                        int id = new MySQLQuery(str).executeInsert(conn);
                        ob.add("msg", "Visita Registrada");
                        ob.add("id", id);
                        break;
                    }

                    case "updateVisit": {
                        Integer visitId = req.getInt("visitId");
                        String detail = req.getString("detail");

                        String str = "UPDATE com_novelty SET "
                                + "detail   = '" + detail + "' "
                                + "WHERE id =  " + visitId + " ";

                        new MySQLQuery(str).executeUpdate(conn);

                        ob.add("msg", "Visita Actualizada");
                        break;
                    }

                    case "getNolvelties": {
                        Integer managerId = getInt(req, "managerId");
                        Integer superId = getInt(req, "superId");
                        Integer accExecId = getInt(req, "accExecId");

                        String str = "SELECT ";

                        if (accExecId != null) {
                            str += "cv.id, "
                                    + "CONCAT(e.first_name,' ',e.last_name), "
                                    + "cv.place_name, "
                                    + "cv.reg_date, "
                                    + "cv.chk_date, "
                                    + "cv.detail "
                                    + "FROM com_novelty cv "
                                    + "INNER JOIN per_employee e ON e.id = cv.acc_exec_id "
                                    + "WHERE cv.acc_exec_id = " + accExecId + " "
                                    + "AND DATE(cv.reg_date) = CURDATE() "
                                    + "AND cv.est_client_id IS NULL";
                        } else {
                            str += "cv.id, "
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
                                    + "AND cv.store_id IS NULL AND cv.vehicle_id IS NULL ";
                        }

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
                        Integer managerId = getInt(req, "managerId");
                        Integer storeId = getInt(req, "storeId");

                        Integer estClientId = getInt(req, "estClientId");
                        Integer accExecId = getInt(req, "accExecId");

                        String q = "SELECT ";

                        if (storeId != null) {
                            q += " cv.id, "
                                    + " cv.reg_date, "
                                    + " cv.chk_date, "
                                    + " cv.detail, "
                                    + " cv.store_id, "
                                    + " cv.est_client_id, "
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
                        } else {
                            q += " cv.id,"
                                    + " cv.reg_date, "
                                    + " cv.chk_date, "
                                    + " cv.detail, "
                                    + " cv.store_id, "
                                    + " cv.est_client_id, "
                                    + "CONCAT(emp.first_name,' ',emp.last_name) "
                                    + "FROM com_novelty cv "
                                    + "INNER JOIN per_employee emp ON emp.id = cv.acc_exec_id "
                                    + "WHERE cv.acc_exec_id = " + accExecId + " "
                                    + "AND cv.est_client_id= " + estClientId + " ";
                        }

                        Object[][] dataVisits = new MySQLQuery(q).getRecords(conn);
                        JsonArrayBuilder jsonVisit = Json.createArrayBuilder();

                        for (Object[] data : dataVisits) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("visitId", MySQLQuery.getAsInteger(data[0]));
                            row.add("regDate", (data[1] != null ? sdfReg.format(MySQLQuery.getAsDate(data[1])) : "Sin fecha de registro"));
                            row.add("chkDate", (data[2] != null ? sdf.format(MySQLQuery.getAsDate(data[2])) : "Sin fecha de revisión"));
                            row.add("detail", (data[3] != null ? MySQLQuery.getAsString(data[3]) : "Sin Notas"));
                            JsonUtils.addInt(row, "storeId", MySQLQuery.getAsInteger(data[4]));
                            JsonUtils.addInt(row, "estClieId", MySQLQuery.getAsInteger(data[5]));
                            row.add("register", MySQLQuery.getAsString(data[6]));

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

                            Double latitude = item.getJsonNumber("latitude").doubleValue();
                            Double longitude = item.getJsonNumber("longitude").doubleValue();
                            Integer accExecId = getInt(item, "accExecId");

                            String str = "INSERT INTO com_novelty SET ";
                            if (accExecId != null) {
                                str += "`acc_exec_id` = " + accExecId + ", "
                                        + "`reg_date` = " + regDate + ", "
                                        + (chkDate != null ? "`chk_date` = " + chkDate + ", " : "")
                                        + "`detail` = '" + detail + "', "
                                        + "`place_name` = '" + placeName + " ', "
                                        + "`has_coordinate` = " + hasCoordinate + ", "
                                        + "`lat` = " + latitude + ", "
                                        + "`lon` = " + longitude + " ";
                            } else {
                                str += "`manager_id` = " + managerId + ", "
                                        + "`reg_date` = " + regDate + ", "
                                        + (chkDate != null ? "`chk_date` = " + chkDate + ", " : "")
                                        + "`detail` = '" + detail + "', "
                                        + "`place_name` = '" + placeName + " ', "
                                        + "`lat` = " + latitude + ", "
                                        + "`lon` = " + longitude + ", "
                                        + "`super_id` = " + superId + ", "
                                        + "`has_coordinate` = " + hasCoordinate + " ";
                            }

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
                        String noveltyType = (req.getString("noveltyType") == null ? " " : req.getString("noveltyType"));

                        Integer hasCoordinate = req.getInt("hasCoordinate");
                        Double latitude = null;
                        Double longitude = null;
                        if (hasCoordinate == 1) {
                            latitude = req.getJsonNumber("latitude").doubleValue();
                            longitude = req.getJsonNumber("longitude").doubleValue();
                        }

                        String str = "INSERT INTO com_novelty SET ";
                        switch (noveltyType) {
                            case "vehicle":
                                Integer vehicleId = req.getInt("vehicleId");

                                str += "`manager_id` = " + managerId + ", "
                                        + "`super_id` = " + superId + ", "
                                        + "`reg_date` = NOW(), "
                                        + (chkDate == null ? "`chk_date` = " + chkDate + ", " : "")
                                        + "`detail` = '" + detail + "', "
                                        + "`vehicle_id` = " + vehicleId + ", "
                                        + "`lat` = " + latitude + ", "
                                        + "`lon` = " + longitude + ", "
                                        + "`has_coordinate` = " + hasCoordinate + "";
                                break;
                            default:
                                Integer accExecId = getInt(req, "accExecId");
                                String placeName = req.getString("placeName");

                                if (accExecId != null) {
                                    str += "`acc_exec_id` = " + accExecId + ", "
                                            + "`reg_date` = NOW(), "
                                            + (chkDate != null ? "`chk_date` = " + chkDate + ", " : "")
                                            + "`detail` = '" + detail + "', "
                                            + "`place_name` = '" + placeName + " ', "
                                            + "`has_coordinate` = " + hasCoordinate + ", "
                                            + "`lat` = " + latitude + ", "
                                            + "`lon` = " + longitude + " ";
                                } else {
                                    str += "`manager_id` = " + managerId + ", "
                                            + "`reg_date` = NOW(), "
                                            + (chkDate != null ? "`chk_date` = " + chkDate + ", " : "")
                                            + "`detail` = '" + detail + "', "
                                            + "`place_name` = '" + placeName + " ', "
                                            + "`lat` = " + latitude + ", "
                                            + "`lon` = " + longitude + ", "
                                            + "`super_id` = " + superId + ", "
                                            + "`has_coordinate` = " + hasCoordinate + " " //                                            + "`hlp_request_id` = " + idCaseHlp
                                            ;
                                }
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
                                + " AND s.phone = '" + imei + "' "
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

                    case "findCrmProspect": {
                        int empId = req.getInt("empId");
                        String type = req.getString("type");
                        boolean isAdmin = req.getBoolean("isAdmin");
                        String str = "SELECT "
                                + "c.id, "
                                + "c.name, "
                                + "c.address, "
                                + "c.phone, "
                                + "CASE c.`type` "
                                + "  WHEN 'client' THEN 'Nueva Obra' "
                                + "  WHEN 'prospect' THEN 'Prospecto' "
                                + "ELSE 'No definido' "
                                + "END, "
                                + "CASE c.`state` "
                                + "  WHEN 'cont' THEN 'Candidato' "
                                + "  WHEN 'eval' THEN 'Evaluación' "
                                + "  WHEN 'opor' THEN 'Oportunidad' "
                                + "  WHEN 'desc' THEN 'Descalificado' "
                                + "  WHEN 'no_cont' THEN 'Evaluación' "
                                + "  WHEN 'opor_cli' THEN 'Oportunidad' "
                                + "ELSE 'No definido' "
                                + "END, "
                                + "(SELECT COUNT(*) FROM crm_task t WHERE t.client_id = c.id "
                                + "AND t.ejec_date IS NULL) as tasks "
                                + "FROM crm_client c "
                                + "LEFT JOIN est_prospect p ON p.id = c.prospect_id "
                                + "LEFT JOIN ord_tank_client o ON o.id = p.client_id ";
                        if (isAdmin) {
                            str += "WHERE c.type = ?1 AND o.id IS NULL "
                                    + "AND c.created_by = " + empId + " "
                                    + "AND c.active "
                                    + "AND IFNULL(p.active,true) AND IFNULL(o.active,true) "
                                    + "ORDER BY tasks DESC, name ASC ";

                        } else {
                            str += "WHERE c.sales_employee_id = ?1 AND c.type = ?2 AND o.id IS NULL "
                                    + "AND c.active "
                                    + "AND IFNULL(p.active,true) AND IFNULL(o.active,true) "
                                    + "ORDER BY tasks DESC, name ASC ";
                        }

                        Object[][] dataEstClient;
                        if (isAdmin) {
                            dataEstClient = new MySQLQuery(str).setParam(1, type).getRecords(conn);
                        } else {
                            dataEstClient = new MySQLQuery(str).setParam(1, empId).setParam(2, type).getRecords(conn);
                        }
                        JsonArrayBuilder jsonStore = Json.createArrayBuilder();

                        for (Object[] data : dataEstClient) {
                            JsonObjectBuilder row = Json.createObjectBuilder();

                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("name", MySQLQuery.getAsString(data[1]));
                            row.add("address", (data[2] == null ? "Sin Dirección" : MySQLQuery.getAsString(data[2])));
                            row.add("phone", (data[3] == null ? "Sin Teléfono" : MySQLQuery.getAsString(data[3])));
                            row.add("type", MySQLQuery.getAsString(data[4]));
                            row.add("state", MySQLQuery.getAsString(data[5]));
                            row.add("taskAmount", MySQLQuery.getAsBigInteger(data[6]));

                            jsonStore.add(row);
                        }

                        ob.add("dataCrmProspect", jsonStore);
                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "findCrmActivity": {
                        int empId = req.getInt("empId");
                        int filter = req.getInt("filter");
                        Integer clientId = (req.containsKey("clientId") ? req.getInt("clientId") : null);
                        Integer vehicleId = (req.containsKey("vehicleId") ? req.getInt("vehicleId") : null);
                        Integer storeId = (req.containsKey("storeId") ? req.getInt("storeId") : null);
                        Integer indexId = (req.containsKey("indexId") ? req.getInt("indexId") : null);
                        String type = (req.containsKey("type") ? req.getString("type") : "activity");
                        String find = (req.containsKey("find") ? req.getString("find") : "accExec");
                        boolean isAdmin = find.contains("Admin");

                        if (find.equals("accExecAdmin")) {
                            find = "accExec";
                        }

                        String str = "";
                        if (find.equals("accExec")) {
                            str = "SELECT "
                                    + "t.id, "
                                    + "c.name, "
                                    + "ct.name, "
                                    + "DATE_FORMAT(t.prog_date, '%d/%m/%Y'), "
                                    + "t.priority, "
                                    + "CASE t.priority WHEN 'l' THEN 0 WHEN 'm' THEN 1 WHEN 'h' THEN 2 WHEN 'u' THEN 3 ELSE 0 END AS orderBy, "
                                    + "COUNT(htr.id), "
                                    + "SUM(IF(htr.active = 1, 1, 0)), "
                                    + "t.desc_short, "
                                    + "'cli', "
                                    + "t.client_id, t.vehicle_id, t.store_id, t.index_id "
                                    + "FROM crm_task t "
                                    + "INNER JOIN crm_client c ON c.id = t.client_id "
                                    + "INNER JOIN crm_type_task ct ON ct.id = t.type_task_id "
                                    + "LEFT JOIN crm_task_activity htr ON htr.task_id = t.id ";
                        } else {
                            str = "SELECT "
                                    + "t.id, "
                                    + "COALESCE(vh.plate, CONCAT(st.first_name, ' ', st.last_name), CONCAT(i.first_name, ' ', i.last_name), 'Actividad Libre'), "
                                    + "ct.name, "
                                    + "DATE_FORMAT(t.prog_date, '%d/%m/%Y'), "
                                    + "t.priority, "
                                    + "CASE t.priority WHEN 'l' THEN 0 WHEN 'm' THEN 1 WHEN 'h' THEN 2 WHEN 'u' THEN 3 ELSE 0 END AS orderBy, "
                                    + "COUNT(htr.id), "
                                    + "SUM(IF(htr.active = 1, 1, 0)), "
                                    + "t.desc_short, "
                                    + "IF(vh.id IS NOT NULL, 'veh', IF(vh.id IS NOT NULL, 'sto', 'ind')), "
                                    + "t.client_id, t.vehicle_id, t.store_id, t.index_id "
                                    + "FROM crm_task t "
                                    + "LEFT JOIN vehicle vh ON vh.id = t.vehicle_id "
                                    + "LEFT JOIN inv_store st ON st.id = t.store_id "
                                    + "LEFT JOIN ord_contract_index i ON i.id = t.index_id AND i.type = 'brand' "
                                    + "INNER JOIN crm_type_task ct ON ct.id = t.type_task_id "
                                    + "LEFT JOIN crm_task_activity htr ON htr.task_id = t.id ";
                        }
                        String where = "";
                        String order = "";
                        switch (filter) {
                            case 1:
                                where = "AND t.prog_date < ?2 ";
                                order = "ORDER BY t.prog_date ASC ";
                                break;
                            case 2:
                                where = "AND t.prog_date < ?2 ";
                                order = "ORDER BY t.prog_date ASC ";
                                break;
                            case 3:
                                where = " ";
                                order = "ORDER BY orderBy ASC ";
                                break;
                            default:
                                break;
                        }

                        if (isAdmin) {
                            str += "WHERE t.ejec_date IS NULL "
                                    + (find.equals("accExec") ? "" : "AND ((vh.id IS NOT NULL OR st.id IS NOT NULL OR i.id IS NOT NULL) OR (t.client_id IS NULL AND vh.id IS NULL AND st.id IS NULL AND i.id IS NULL)) ")
                                    + "AND t.creator_id = " + empId + "  "
                                    + (clientId != null ? "AND c.id = " + clientId + " " : " ")
                                    + (vehicleId != null ? "AND t.vehicle_id = " + vehicleId + " " : " ")
                                    + (storeId != null ? "AND t.store_id = " + storeId + " " : " ")
                                    + (indexId != null ? "AND t.index_id = " + indexId + " " : " ");
                            str += where;
                            str += "GROUP BY t.id " + order;
                        } else {
                            str += "WHERE ";
                            if (type.equals("assigned")) {
                                str += " t.resp_id = ?1  AND t.creator_id <> t.resp_id ";
                            } else {
                                str += (find.equals("accExec") ? " (c.sales_employee_id = ?1 OR t.resp_id = ?1 OR t.creator_id = ?1 ) " : " (t.resp_id = ?1 OR t.creator_id = ?1 ) ");
                            }
                            str += " AND t.ejec_date IS NULL "
                                    + (find.equals("accExec") ? "" : "AND ((vh.id IS NOT NULL OR st.id IS NOT NULL OR i.id IS NOT NULL) OR (t.client_id IS NULL AND vh.id IS NULL AND st.id IS NULL AND i.id IS NULL)) ")
                                    + (clientId != null ? "AND c.id = " + clientId + " " : " ")
                                    + (vehicleId != null ? "AND t.vehicle_id = " + vehicleId + " " : " ")
                                    + (storeId != null ? "AND t.store_id = " + storeId + " " : " ")
                                    + (indexId != null ? "AND t.index_id = " + indexId + " " : " ");
                            str += where;
                            str += "GROUP BY t.id " + order;
                        }

                        MySQLQuery query;
                        if (isAdmin) {
                            query = new MySQLQuery(str);
                        } else {
                            query = new MySQLQuery(str).setParam(1, empId);
                        }

                        if (filter == 1) {
                            Calendar c = Calendar.getInstance();
                            c.set(Calendar.HOUR_OF_DAY, 0);
                            c.set(Calendar.MINUTE, 0);
                            c.set(Calendar.SECOND, 0);
                            c.add(Calendar.DAY_OF_YEAR, 1);
                            query.setParam(2, c.getTime());//Fin del dia (Mañana 00:00:00)
                        } else if (filter == 2) {
                            Calendar c = Calendar.getInstance();
                            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                            c.set(Calendar.HOUR_OF_DAY, 0);
                            c.set(Calendar.MINUTE, 0);
                            c.set(Calendar.SECOND, 0);
                            c.add(Calendar.DAY_OF_YEAR, 7);
                            query.setParam(2, c.getTime());//Fin de semana (Lunes 00:00:00)
                        }

                        Object[][] dataEstClient = query.getRecords(conn);
                        JsonArrayBuilder jsonStore = Json.createArrayBuilder();

                        for (Object[] data : dataEstClient) {
                            JsonObjectBuilder row = Json.createObjectBuilder();

                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("client", MySQLQuery.getAsString(data[1]));
                            row.add("type", (data[2] == null ? "Sin Tipo" : MySQLQuery.getAsString(data[2])));
                            row.add("prog", (data[3] == null ? "Sin Programación" : MySQLQuery.getAsString(data[3])));
                            row.add("priority", MySQLQuery.getAsString(data[4]));
                            JsonUtils.addInt(row, "totalTask", MySQLQuery.getAsInteger(data[6]));
                            JsonUtils.addInt(row, "doneTask", MySQLQuery.getAsInteger(data[7]));
                            row.add("descShort", MySQLQuery.getAsString(data[8]));
                            row.add("typeFind", MySQLQuery.getAsString(data[9]));
                            row.add("clientId", data[10] != null ? MySQLQuery.getAsInteger(data[10]) : 0);
                            row.add("vehicleId", data[11] != null ? MySQLQuery.getAsInteger(data[11]) : 0);
                            row.add("storeId", data[12] != null ? MySQLQuery.getAsInteger(data[12]) : 0);
                            row.add("indexId", data[13] != null ? MySQLQuery.getAsInteger(data[13]) : 0);

                            jsonStore.add(row);
                        }

                        ob.add("dataCrmActivity", jsonStore);
                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "findCrmTask": {
                        int taskId = req.getInt("taskId");
                        String str = "SELECT c.id, c.notes, c.active FROM crm_task_activity c WHERE c.task_id = ?1;";

                        Object[][] dataTask = new MySQLQuery(str).setParam(1, taskId).getRecords(conn);
                        JsonArrayBuilder jsonStore = Json.createArrayBuilder();

                        for (Object[] data : dataTask) {
                            JsonObjectBuilder row = Json.createObjectBuilder();

                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("notes", MySQLQuery.getAsString(data[1]));
                            row.add("chk", MySQLQuery.getAsInteger(data[2]));

                            jsonStore.add(row);
                        }

                        ob.add("dataCrmTask", jsonStore);
                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "findClientAdmin": {

                        String query;
                        if (req.containsKey("documento")) {//borrar en febrero 2020
                            query = req.getString("document");
                        } else {
                            query = req.getString("query");
                        }

                        String str = "SELECT c.id, c.address, c.phone, c.document, c.name, ct.id "
                                + "FROM crm_client c "
                                + "LEFT JOIN city ct ON c.city_id = ct.id "
                                + "WHERE "
                                + "c.document LIKE '%" + query + "%' OR "
                                + "c.name LIKE '%" + query + "%' ";

                        Object[][] dataTask = new MySQLQuery(str).getRecords(conn);
                        JsonArrayBuilder json = Json.createArrayBuilder();

                        for (Object[] data : dataTask) {
                            JsonObjectBuilder row = Json.createObjectBuilder();

                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("address", data[1] != null ? MySQLQuery.getAsString(data[1]) : "Sin dirección");
                            row.add("phone", data[2] != null ? MySQLQuery.getAsString(data[2]) : "Sin teléfono");
                            row.add("document", data[3] != null ? MySQLQuery.getAsString(data[3]) : "Sin documento");
                            row.add("name", data[4] != null ? MySQLQuery.getAsString(data[4]) : "Sin Nombre");
                            row.add("cityId", data[5] != null ? MySQLQuery.getAsInteger(data[5]) : 0);

                            json.add(row);
                        }

                        ob.add("dataClient", json);
                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "findVehicleAdmin": {

                        String query = req.getString("query");

                        String str = "SELECT v.id, v.plate, v.internal, GROUP_CONCAT(CONCAT(e.first_name, ' ', e.last_name)) "
                                + "FROM vehicle v "
                                + "LEFT JOIN driver_vehicle dv ON dv.vehicle_id = v.id AND dv.`end` IS NULL "
                                + "LEFT JOIN employee e ON e.id = dv.driver_id "
                                + "WHERE "
                                + "v.plate LIKE '%" + query + "%' OR "
                                + "v.internal LIKE '%" + query + "%' "
                                + "GROUP BY v.id";

                        Object[][] dataTask = new MySQLQuery(str).getRecords(conn);
                        JsonArrayBuilder json = Json.createArrayBuilder();

                        for (Object[] data : dataTask) {
                            JsonObjectBuilder row = Json.createObjectBuilder();

                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("plate", data[1] != null ? MySQLQuery.getAsString(data[1]) : "Sin Placa");
                            row.add("internal", data[2] != null ? MySQLQuery.getAsString(data[2]) : "Sin Interno");
                            row.add("driver", data[3] != null ? MySQLQuery.getAsString(data[3]) : "Sin Conductor");

                            json.add(row);
                        }

                        ob.add("dataVehicle", json);
                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "findStoreAdmin": {

                        String query = req.getString("query");

                        String str = "SELECT v.id, v.internal, v.address, CONCAT(v.first_name, ' ', v.last_name) "
                                + "FROM inv_store v "
                                + "WHERE "
                                + "v.first_name LIKE '%" + query + "%' OR "
                                + "v.last_name LIKE '%" + query + "%' OR "
                                + "v.internal LIKE '%" + query + "%' ";

                        Object[][] dataTask = new MySQLQuery(str).getRecords(conn);
                        JsonArrayBuilder json = Json.createArrayBuilder();

                        for (Object[] data : dataTask) {
                            JsonObjectBuilder row = Json.createObjectBuilder();

                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("internal", data[1] != null ? MySQLQuery.getAsString(data[1]) : "Sin Interno");
                            row.add("address", data[2] != null ? MySQLQuery.getAsString(data[2]) : "Sin Dirección");
                            row.add("fullName", data[3] != null ? MySQLQuery.getAsString(data[3]) : "Sin Nombre");

                            json.add(row);
                        }

                        ob.add("dataStore", json);
                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "findContractAdmin": {

                        String query = req.getString("query");

                        String str = "SELECT c.id, c.document, CONCAT(c.first_name, ' ', c.last_name), c.contract_num, c.address "
                                + "FROM contract c "
                                + "WHERE "
                                + "c.first_name LIKE '%" + query + "%' OR "
                                + "c.last_name LIKE '%" + query + "%' OR "
                                + "c.document LIKE '%" + query + "%' ";

                        Object[][] dataTask = new MySQLQuery(str).getRecords(conn);
                        JsonArrayBuilder json = Json.createArrayBuilder();

                        for (Object[] data : dataTask) {
                            JsonObjectBuilder row = Json.createObjectBuilder();

                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("doc", data[1] != null ? MySQLQuery.getAsString(data[1]) : "Sin Documento");
                            row.add("fullName", data[2] != null ? MySQLQuery.getAsString(data[2]) : "Sin Nombre");
                            row.add("num", data[3] != null ? MySQLQuery.getAsString(data[3]) : "Sin Número");
                            row.add("address", data[3] != null ? MySQLQuery.getAsString(data[3]) : "Sin Dirección");

                            json.add(row);
                        }

                        ob.add("dataContract", json);
                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "findContract": {

                        boolean isSuper = req.getBoolean("isSuper");
                        int empId = req.getInt("empId");
                        String str = "SELECT ci.id, ci.document, CONCAT(ci.first_name, ' ', ci.last_name), ci.contract_num, ci.address, IF(ci.pref, 'Preferencial', 'Institucional'), "
                                + "(SELECT COUNT(*) FROM crm_task t WHERE t.index_id = ci.id AND t.ejec_date IS NULL) "
                                + "FROM com_service_manager sm "
                                + "inner join com_man_contract cm on cm.man_id = sm.id "
                                + "inner join ord_contract_index ci on ci.contract_id = cm.contract_id  "
                                + (isSuper ? "INNER JOIN com_super_manager csm ON cm.id = sm.com_super_id WHERE csm.emp_id = " + empId : "where (ci.pref OR ci.institutional) AND sm.emp_id = " + empId);

                        Object[][] datas = new MySQLQuery(str).getRecords(conn);

                        JsonArrayBuilder jsonVehicle = Json.createArrayBuilder();

                        for (Object[] data : datas) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("doc", (data[1] != null ? MySQLQuery.getAsString(data[1]) : "Sin documento"));
                            row.add("name", (data[2] != null ? MySQLQuery.getAsString(data[2]) : "Sin nombre"));
                            row.add("num", (data[3] != null ? MySQLQuery.getAsString(data[3]) : "Sin número"));
                            row.add("address", (data[4] != null ? MySQLQuery.getAsString(data[4]) : "Sin dirección"));
                            row.add("type", (data[5] != null ? MySQLQuery.getAsString(data[5]) : "Sin tipo"));
                            row.add("taskAmount", MySQLQuery.getAsInteger(data[6]));

                            jsonVehicle.add(row);
                        }

                        ob.add("dataContracts", jsonVehicle);
                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "getGoalsExec": {
                        int id = req.getInt("empId");
                        String dateMax = req.getString("dateMax");
                        String dateMin = req.getString("dateMin");
                        String qEvent = "SELECT COUNT(*) as prog, SUM(IF(t.ejec_date IS NOT NULL, 1, 0)) as exec "
                                + "FROM crm_task t "
                                + "WHERE t.resp_id = " + id + " "
                                + "AND t.prog_date BETWEEN '" + dateMin + "' AND '" + dateMax + "' "//FECHAS
                                + "GROUP BY t.resp_id ";

                        Object[][] dataNovely = new MySQLQuery(qEvent).getRecords(conn);

                        JsonObjectBuilder row = Json.createObjectBuilder();
                        row.add("progAct", (dataNovely.length > 0 && dataNovely[0][0] != null ? MySQLQuery.getAsInteger(dataNovely[0][0]) : 0));
                        row.add("execAct", (dataNovely.length > 0 && dataNovely[0][1] != null ? MySQLQuery.getAsInteger(dataNovely[0][1]) : 0));

                        ob.add("dataGoals", row);

                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "getGoalsType": {
                        int id = req.getInt("empId");
                        String dateMax = req.getString("dateMax");
                        String dateMin = req.getString("dateMin");
                        int module = req.getInt("module");
                        String qEvent = "SELECT tg.goal as prog, "
                                + "(SELECT COUNT(*) FROM crm_task t WHERE t.type_task_id = tt.id AND t.resp_id = " + id + " AND t.prog_date BETWEEN '" + dateMin + "' AND '" + dateMax + "' AND t.ejec_date IS NOT NULL) as exec, "
                                + "tt.name "
                                + "FROM crm_type_task tt "
                                + "INNER JOIN crm_type_task_goal tg ON tg.type_task_id = tt.id "
                                + "WHERE CURDATE() BETWEEN tg.begin_date AND IF(tg.end_date IS NULL, CURDATE(), tg.end_date) "
                                + "AND tt.module = " + module;

                        Object[][] datas = new MySQLQuery(qEvent).getRecords(conn);

                        JsonArrayBuilder jsonArray = Json.createArrayBuilder();

                        for (Object[] data : datas) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("progAct", (data[0] != null ? MySQLQuery.getAsInteger(data[0]) : 0));
                            row.add("execAct", (data[1] != null ? MySQLQuery.getAsInteger(data[1]) : 0));
                            row.add("name", (data[2] != null ? MySQLQuery.getAsString(data[2]) : "N/A"));
                            jsonArray.add(row);
                        }

                        ob.add("dataGoals", jsonArray);

                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "findCrmPollClient": {
                        int empId = req.getInt("empId");
                        String str = "select pc.id, pc.create_date, pv.since, c.name, p.id, pt.name "
                                + "from crm_poll_client pc "
                                + "inner join ord_poll p on pc.poll_id = p.id "
                                + "inner join ord_poll_version pv on p.poll_version_id = pv.id "
                                + "INNER JOIN ord_poll_type pt ON pt.id = pv.ord_poll_type_id "
                                + "inner join crm_client c on c.id = pc.client_id "
                                + "where p.emp_id = ?1 "
                                + "order by pc.create_date DESC;";

                        Object[][] datas = new MySQLQuery(str).setParam(1, empId).getRecords(conn);
                        JsonArrayBuilder jsonData = Json.createArrayBuilder();

                        for (Object[] data : datas) {
                            JsonObjectBuilder row = Json.createObjectBuilder();

                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("createDate", sdf2.format(MySQLQuery.getAsDate(data[1])));
                            row.add("version", sdf2.format(MySQLQuery.getAsDate(data[2])));
                            row.add("clientName", MySQLQuery.getAsString(data[3]));
                            row.add("pollId", MySQLQuery.getAsInteger(data[4]));
                            row.add("formatName", MySQLQuery.getAsString(data[5]));

                            jsonData.add(row);
                        }

                        ob.add("data", jsonData);
                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "findTankOrders": {
                        SimpleDateFormat sdf_order = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        Integer empId = req.getInt("empId");
                        String str = "SELECT o.id, "
                                + "o.day, "
                                + "o.taken_hour, "
                                + "o.assig_hour, "
                                + "o.office_id, "
                                + "o.taken_by_id, "
                                + "o.tank_client_id, "
                                + "o.channel_id, "
                                + "o.kgs, "
                                + "CONCAT(COALESCE(v.internal, ''), ' ', v.plate) as veh, "
                                + "c.name, "
                                + "c.address "
                                + "FROM ord_tank_order o "
                                + "INNER JOIN ord_tank_client c ON c.id = o.tank_client_id "
                                + "LEFT JOIN vehicle v ON v.id = o.vehicle_id "
                                + "WHERE o.taken_by_id = " + empId + " AND o.confirm_hour IS NULL "
                                + "AND o.cancel_cause_id IS NULL AND o.`day` >= CURDATE() ";

                        Object[][] datas = new MySQLQuery(str).getRecords(conn);

                        JsonArrayBuilder jsonData = Json.createArrayBuilder();

                        for (Object[] data : datas) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("day", sdf_order.format(MySQLQuery.getAsDate(data[1])));
                            if (data[2] != null) {
                                row.add("takenHour", sdf_order.format(MySQLQuery.getAsDate(data[2])));
                            }
                            if (data[3] != null) {
                                row.add("AssigHour", sdf_order.format(MySQLQuery.getAsDate(data[3])));
                            }
                            row.add("officeId", MySQLQuery.getAsInteger(data[4]));
                            row.add("takenById", MySQLQuery.getAsInteger(data[5]));
                            row.add("tankClientId", MySQLQuery.getAsInteger(data[6]));
                            if (data[7] != null) {
                                row.add("channelId", MySQLQuery.getAsInteger(data[7]));
                            }
                            row.add("kgs", MySQLQuery.getAsBigDecimal(data[8], true));
                            row.add("vehicle", (data[9] != null ? MySQLQuery.getAsString(data[9]) : "Sin Vehículo"));
                            row.add("tankClient", (data[10] != null ? MySQLQuery.getAsString(data[10]) : "Sin Cliente"));
                            row.add("clientAddress", (data[11] != null ? MySQLQuery.getAsString(data[11]) : "Sin Dirección"));
                            jsonData.add(row);
                        }

                        ob.add("dataOrdTankOrders", jsonData);
                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "findPqrTank": {
                        SimpleDateFormat sdf_order = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        Integer empId = req.getInt("empId");
                        String str = "SELECT p.id, "
                                + "p.regist_date, "
                                + "p.regist_hour, "
                                + "p.regist_by, "
                                + "p.build_id, "
                                + "p.office_id, "
                                + "p.serial, "
                                + "p.channel_id, "
                                + "p.notes, "
                                + "p.technician_id, "
                                + "b.name, "
                                + "b.address, "
                                + "b.phones "
                                + "FROM ord_pqr_tank AS p "
                                + "INNER JOIN ord_tank_client as b ON b.id = p.build_id "
                                + "WHERE p.attention_date IS NULL AND p.regist_by = " + empId;

                        Object[][] datas = new MySQLQuery(str).getRecords(conn);

                        JsonArrayBuilder jsonData = Json.createArrayBuilder();

                        for (Object[] data : datas) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("registDate", sdf_order.format(MySQLQuery.getAsDate(data[1])));
                            row.add("registHour", sdf_order.format(MySQLQuery.getAsDate(data[2])));
                            row.add("registBy", MySQLQuery.getAsInteger(data[3]));
                            row.add("buildId", MySQLQuery.getAsInteger(data[4]));
                            row.add("officeId", MySQLQuery.getAsInteger(data[5]));
                            row.add("serial", MySQLQuery.getAsInteger(data[6]));
                            if (data[7] != null) {
                                row.add("channelId", MySQLQuery.getAsInteger(data[7]));
                            }
                            row.add("notes", data[8] != null ? MySQLQuery.getAsString(data[8]) : "");
                            if (data[9] != null) {
                                row.add("technicianId", MySQLQuery.getAsInteger(data[9]));
                            }
                            row.add("tankClient", (data[10] != null ? MySQLQuery.getAsString(data[10]) : "Sin Cliente"));
                            row.add("clientAddress", (data[11] != null ? MySQLQuery.getAsString(data[11]) : "Sin Dirección"));
                            row.add("clientPhone", (data[12] != null ? MySQLQuery.getAsString(data[12]) : "Sin Teléfono"));
                            jsonData.add(row);
                        }

                        ob.add("data", jsonData);
                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "findPqrOther": {
                        SimpleDateFormat sdf_order = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        Integer empId = req.getInt("empId");
                        String str = "SELECT p.id, "
                                + "p.serial, "
                                + "p.regist_date, "
                                + "p.regist_hour, "
                                + "p.regist_by, "
                                + "p.build_id, "
                                + "p.sys_center_id, "
                                + "p.zone_id, "
                                + "p.reason_id, "
                                + "p.office_id, "
                                + "p.subject, "
                                + "p.channel_id, "
                                + "b.name, "
                                + "b.address, "
                                + "b.phones "
                                + "FROM ord_pqr_other AS p "
                                + "INNER JOIN ord_tank_client as b ON b.id = p.build_id "
                                + "WHERE p.confirm_date IS NULL AND p.regist_by = " + empId;

                        Object[][] datas = new MySQLQuery(str).getRecords(conn);

                        JsonArrayBuilder jsonData = Json.createArrayBuilder();

                        for (Object[] data : datas) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("serial", MySQLQuery.getAsInteger(data[1]));
                            row.add("registDate", sdf_order.format(MySQLQuery.getAsDate(data[2])));
                            row.add("registHour", sdf_order.format(MySQLQuery.getAsDate(data[3])));
                            row.add("registBy", MySQLQuery.getAsInteger(data[4]));
                            row.add("buildId", MySQLQuery.getAsInteger(data[5]));
                            if (data[6] != null) {
                                row.add("sysCenterId", MySQLQuery.getAsInteger(data[6]));
                            }
                            if (data[7] != null) {
                                row.add("zoneId", MySQLQuery.getAsInteger(data[7]));
                            }
                            if (data[8] != null) {
                                row.add("reasonId", MySQLQuery.getAsInteger(data[8]));
                            }
                            row.add("officeId", MySQLQuery.getAsInteger(data[9]));
                            row.add("subject", data[10] != null ? MySQLQuery.getAsString(data[10]) : "");
                            if (data[11] != null) {
                                row.add("channelId", MySQLQuery.getAsInteger(data[11]));
                            }
                            row.add("tankClient", (data[12] != null ? MySQLQuery.getAsString(data[12]) : "Sin Cliente"));
                            row.add("clientAddress", (data[13] != null ? MySQLQuery.getAsString(data[13]) : "Sin Dirección"));
                            row.add("clientPhone", (data[14] != null ? MySQLQuery.getAsString(data[14]) : "Sin Teléfono"));
                            jsonData.add(row);
                        }

                        ob.add("data", jsonData);
                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "findPqrRequest": {
                        SimpleDateFormat sdf_order = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        Integer empId = req.getInt("empId");
                        String str = "SELECT p.id, "
                                + "p.created_id, "
                                + "p.client_id, "
                                + "p.creation_date, "
                                + "p.notes, "
                                + "b.name, "
                                + "b.address, "
                                + "b.phones "
                                + "FROM ord_pqr_request AS p "
                                + "INNER JOIN ord_tank_client as b ON b.id = p.client_id "
                                + "WHERE p.dt_cancel IS NULL AND p.type IS NULL AND p.created_id = " + empId;

                        Object[][] datas = new MySQLQuery(str).getRecords(conn);

                        JsonArrayBuilder jsonData = Json.createArrayBuilder();

                        for (Object[] data : datas) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("createdId", MySQLQuery.getAsInteger(data[1]));
                            row.add("clientId", MySQLQuery.getAsInteger(data[2]));
                            row.add("creationDate", sdf_order.format(MySQLQuery.getAsDate(data[3])));
                            row.add("notes", data[4] != null ? MySQLQuery.getAsString(data[4]) : "");
                            row.add("tankClient", (data[5] != null ? MySQLQuery.getAsString(data[5]) : "Sin Cliente"));
                            row.add("clientAddress", (data[6] != null ? MySQLQuery.getAsString(data[6]) : "Sin Dirección"));
                            row.add("clientPhone", (data[7] != null ? MySQLQuery.getAsString(data[7]) : "Sin Teléfono"));
                            jsonData.add(row);
                        }

                        ob.add("data", jsonData);
                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    case "findStoreOrders": {
                        SimpleDateFormat sdf_order = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        Integer empId = req.getInt("empId");
                        String str = "SELECT o.id, "
                                + "CONCAT(s.first_name, ' ',  s.last_name) AS store, "
                                + "(SELECT GROUP_CONCAT(i.amount, '-', t.name) FROM com_store_order_inv i INNER JOIN cylinder_type t ON t.id = i.cyl_type_id WHERE order_id = o.id) AS orders, "
                                + "o.taken_dt, "
                                + "IF((o.cancel_id IS NULL AND o.assign_by_id IS NULL AND o.confirm_by_id IS NULL AND s.active AND (ist.from_enum <> 'clo' OR ist.exclude)), 'Capturado', "
                                + "IF((o.cancel_id IS NULL AND o.assign_by_id IS NOT NULL AND o.confirm_by_id IS NULL AND s.active AND (ist.from_enum <> 'clo' OR ist.exclude) AND o.novs IS NULL), 'Asignado', "
                                + "IF((o.cancel_id IS NULL AND o.confirm_by_id IS NOT NULL AND s.active AND (ist.from_enum <> 'clo' OR ist.exclude)), 'Confirmado', "
                                + "IF((o.cancel_id IS NOT NULL AND s.active AND (ist.from_enum <> 'clo' OR ist.exclude)), 'Cancelado', 'Nada')))) AS state, "
                                + "v.plate, "
                                + "(SELECT GROUP_CONCAT(e.first_name, ' ', e.last_name) FROM driver_vehicle dv INNER JOIN employee e ON e.id = dv.driver_id WHERE dv.vehicle_id = v.id AND dv.`end` IS NULL) AS drivers "
                                + "FROM com_store_order o "
                                + "INNER JOIN inv_store s ON s.id = o.store_id "
                                + "INNER JOIN inv_store_state ist ON ist.id = s.state_id "
                                + "INNER JOIN com_man_store cs ON cs.store_id = s.id "
                                + "INNER JOIN com_service_manager man ON man.id = cs.man_id "
                                + "INNER JOIN employee e ON e.id = man.emp_id "
                                + "LEFT JOIN com_super_manager csm ON csm.id=man.com_super_id "
                                + "LEFT JOIN per_employee es ON csm.emp_id = es.id "
                                + "LEFT JOIN vehicle v ON v.id = o.vh_id "
                                + "WHERE o.novs IS NULL AND o.cancel_id IS NULL AND o.confirm_by_id IS NULL AND s.active AND (ist.from_enum <> 'clo' OR ist.exclude) "
                                + "AND (e.id = ?1 OR es.emp_id = ?1) ";

                        Object[][] datas = new MySQLQuery(str).setParam(1, empId).getRecords(conn);

                        JsonArrayBuilder jsonData = Json.createArrayBuilder();

                        for (Object[] data : datas) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("id", MySQLQuery.getAsInteger(data[0]));
                            row.add("store", (data[1] != null ? MySQLQuery.getAsString(data[1]) : "Sin Almacén"));
                            row.add("orders", (data[2] != null ? MySQLQuery.getAsString(data[2]) : "Sin Pedidos"));
                            row.add("takenDt", sdf_order.format(MySQLQuery.getAsDate(data[3])));
                            row.add("state", (data[4] != null ? MySQLQuery.getAsString(data[4]) : "Sin Estado"));
                            if (data[5] != null) {
                                row.add("plate", MySQLQuery.getAsString(data[5]));
                            }
                            if (data[6] != null) {
                                row.add("drivers", MySQLQuery.getAsString(data[6]));
                            }
                            jsonData.add(row);
                        }

                        ob.add("dataOrders", jsonData);
                        ob.add("msg", "consulta realizada con exito");
                        break;
                    }

                    default:
                        break;
                }
                ob.add("result", "OK");
            } catch (Exception ex) {
                Logger.getLogger(FindStoreV7.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(FindStoreV7.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySQLSelect.tryClose(conn);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(FindStoreV7.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(FindStoreV7.class.getName()).log(Level.SEVERE, null, ex);
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

}
