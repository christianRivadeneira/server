package web.emasUrbano;

import java.awt.geom.Path2D;
import java.io.IOException;
import java.sql.Connection;
import java.util.GregorianCalendar;
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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.JsonUtils;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "EmasUrbRequest", urlPatterns = {"/EmasUrbRequest"})
public class EmasUrbRequest extends HttpServlet {

    private final String STATUS_OK = "OK";
    private final String STATUS_ERROR = "ERROR";
    private final int NEIGH_TYPE = 2;
    private final int GREEN_ZONE_TYPE = 3;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;

        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            JsonObjectBuilder ob = Json.createObjectBuilder();
            JsonObject req = MySQLQuery.scapeJsonObj(request);

            String header = req.getString("header");
            String poolName = req.getString("poolName");
            conn = MySQLCommon.getConnection(poolName, null);

            try {
                switch (header) {

                    case "getSchedule": {
                        Integer neighId = getInt(req, "neighId");
                        Integer zoneId = getInt(req, "zoneId");

                        Object[][] data = null;

                        if (neighId != null) {
                            data = new MySQLQuery(" SELECT "
                                    + " p.start_time, "
                                    + " p.end_time, "
                                    + " p.notes, "
                                    + " CONCAT(r.l,',',r.m,',',r.x,',',r.j,',',r.v,',',r.s,',',r.d) "
                                    + " FROM urb_path_recol p  "
                                    + " INNER JOIN urb_route r ON r.id = p.route_id "
                                    + " INNER JOIN urb_recol_schedule s ON s.id = r.schedule_id AND s.active = 1 "
                                    + " WHERE p.neigh_id = " + neighId).getRecords(conn);
                            if (data != null && data.length > 0 && data[0] != null) {
                                JsonArrayBuilder dataScheduleRecol = Json.createArrayBuilder();
                                for (Object[] data1 : data) {
                                    JsonObjectBuilder row = Json.createObjectBuilder();
                                    row.add("startTime", MySQLQuery.getAsString(data1[0]));
                                    row.add("endTime", MySQLQuery.getAsString(data1[1]));
                                    row.add("notes", data1[2] != null ? MySQLQuery.getAsString(data1[2]) : "");
                                    row.add("schedule", MySQLQuery.getAsString(data1[3]));
                                    dataScheduleRecol.add(row);
                                }
                                ob.add("dataScheduleRecol", dataScheduleRecol);
                            } else {
                                ob.addNull("dataScheduleRecol");
                            }

                            //SweepSchedule          
                            data = new MySQLQuery(" SELECT "
                                    + " p.route, "
                                    + " CONCAT(p.l,',',p.m,',',p.x,',',p.j,',',p.v,',',p.s,',',p.d), "
                                    + " p.`schedule` "
                                    + " FROM urb_sweep p "
                                    + " WHERE p.neigh_id = " + neighId + " ").getRecords(conn);

                            if (data != null && data.length > 0 && data[0] != null) {
                                JsonArrayBuilder dataScheduleSweep = Json.createArrayBuilder();
                                for (Object[] data1 : data) {
                                    JsonObjectBuilder row = Json.createObjectBuilder();
                                    row.add("route", data1[0] != null ? MySQLQuery.getAsString(data1[0]) : "");
                                    row.add("schedule", data1[1] != null ? MySQLQuery.getAsString(data1[1]) : "");
                                    row.add("hours", MySQLQuery.getAsString(data1[2]));
                                    dataScheduleSweep.add(row);
                                }
                                ob.add("dataScheduleSweep", dataScheduleSweep);
                            } else {
                                ob.addNull("dataScheduleSweep");
                            }
                        }

                        if (zoneId != null) {
                            data = new MySQLQuery(" SELECT "
                                    + " s.beg_date, "
                                    + " s.end_date, "
                                    + " s.done "
                                    + " FROM urb_green_span s "
                                    + " WHERE "
                                    + " s.active = 1 AND s.green_zone_id = " + zoneId + " ORDER BY s.beg_date ASC ").getRecords(conn);

                            if (data != null && data.length > 0 && data[0] != null) {
                                JsonArrayBuilder dataScheduleZone = Json.createArrayBuilder();
                                for (Object[] data1 : data) {
                                    JsonObjectBuilder row = Json.createObjectBuilder();
                                    row.add("begDate", MySQLQuery.getAsString(data1[0]));
                                    row.add("endDate", MySQLQuery.getAsString(data1[1]));
                                    row.add("done", MySQLQuery.getAsBoolean(data1[2]));
                                    dataScheduleZone.add(row);
                                }
                                ob.add("dataScheduleZone", dataScheduleZone);
                            } else {
                                ob.addNull("dataScheduleZone");
                            }

                        }
                        ob.add("msg", "éxito");
                        break;
                    }

                    case "getSchedule2": {
                        Integer neighId = getInt(req, "neighId");
                        Double lat = req.getJsonNumber("lat").doubleValue();
                        Double lon = req.getJsonNumber("lon").doubleValue();

                        Object[][] data = null;

                        if (neighId != null) {
                            data = new MySQLQuery(" SELECT "
                                    + " p.start_time, "
                                    + " p.end_time, "
                                    + " p.notes, "
                                    + " CONCAT(r.l,',',r.m,',',r.x,',',r.j,',',r.v,',',r.s,',',r.d) "
                                    + " FROM urb_path_recol p  "
                                    + " INNER JOIN urb_route r ON r.id = p.route_id "
                                    + " INNER JOIN urb_recol_schedule s ON s.id = r.schedule_id AND s.active = 1 "
                                    + " WHERE p.neigh_id = " + neighId).getRecords(conn);
                            if (data != null && data.length > 0 && data[0] != null) {
                                JsonArrayBuilder dataScheduleRecol = Json.createArrayBuilder();
                                for (Object[] data1 : data) {
                                    JsonObjectBuilder row = Json.createObjectBuilder();
                                    row.add("startTime", MySQLQuery.getAsString(data1[0]));
                                    row.add("endTime", MySQLQuery.getAsString(data1[1]));
                                    row.add("notes", data1[2] != null ? MySQLQuery.getAsString(data1[2]) : "");
                                    row.add("schedule", MySQLQuery.getAsString(data1[3]));
                                    dataScheduleRecol.add(row);
                                }
                                ob.add("dataScheduleRecol", dataScheduleRecol);
                            } else {
                                ob.addNull("dataScheduleRecol");
                            }

                            data = new MySQLQuery(" SELECT "
                                    + " p.route, "
                                    + " CONCAT(p.l,',',p.m,',',p.x,',',p.j,',',p.v,',',p.s,',',p.d), "
                                    + " p.`schedule` "
                                    + " FROM urb_sweep p "
                                    + " WHERE p.neigh_id = " + neighId + " ").getRecords(conn);

                            if (data != null && data.length > 0 && data[0] != null) {
                                JsonArrayBuilder dataScheduleSweep = Json.createArrayBuilder();
                                for (Object[] data1 : data) {
                                    JsonObjectBuilder row = Json.createObjectBuilder();
                                    row.add("route", data1[0] != null ? MySQLQuery.getAsString(data1[0]) : "");
                                    row.add("schedule", data1[1] != null ? MySQLQuery.getAsString(data1[1]) : "");
                                    row.add("hours", MySQLQuery.getAsString(data1[2]));
                                    dataScheduleSweep.add(row);
                                }
                                ob.add("dataScheduleSweep", dataScheduleSweep);
                            } else {
                                ob.addNull("dataScheduleSweep");
                            }
                        }

                        //ZoneSchedule                        
                        Object zoneData[] = new MySQLQuery("SELECT g.id, g.name FROM neigh n INNER JOIN urb_green_zone g ON g.id = n.urb_zone_id WHERE n.id = " + neighId).getRecord(conn);
                        Integer zoneId = MySQLQuery.getAsInteger(zoneData[0]);
                        String zoneName = MySQLQuery.getAsString(zoneData[1]);

                        if (zoneId != null) {
                            data = new MySQLQuery(" SELECT "
                                    + " s.beg_date, "
                                    + " s.end_date, "
                                    + " s.done "
                                    + " FROM urb_green_span s "
                                    + " WHERE "
                                    + " s.active = 1 AND s.green_zone_id = " + zoneId + " ORDER BY s.beg_date ASC ").getRecords(conn);

                            if (data != null && data.length > 0 && data[0] != null) {
                                JsonArrayBuilder dataScheduleZone = Json.createArrayBuilder();
                                for (Object[] data1 : data) {
                                    JsonObjectBuilder row = Json.createObjectBuilder();
                                    row.add("begDate", data1[0] != null ? MySQLQuery.getAsString(data1[0]) : "");
                                    row.add("endDate", data1[1] != null ? MySQLQuery.getAsString(data1[1]) : "");
                                    row.add("done", data1[2] != null ? MySQLQuery.getAsBoolean(data1[2]) : false);
                                    dataScheduleZone.add(row);
                                }
                                ob.add("dataScheduleZone", dataScheduleZone);
                            } else {
                                ob.addNull("dataScheduleZone");
                            }

                        }
                        JsonUtils.addString(ob, "zoneName", zoneName != null ? zoneName : "Su zona no esta disponible");
                        ob.add("msg", "éxito");
                        break;
                    }

                    case "getTariff": {
                        int year = req.getJsonNumber("year").intValue();

                        JsonArrayBuilder dataTariff = null;
                        Object[][] data = new MySQLQuery(" SELECT "
                                + " t.year, "
                                + " t.month, "
                                + " t.lvl1,t.lvl2,t.lvl3,t.lvl4,t.lvl5 "
                                + " FROM urb_tariff t "
                                + " WHERE "
                                + " t.year = " + year + " ORDER BY t.month DESC ").getRecords(conn);

                        if (data != null && data.length > 0 && data[0] != null) {
                            dataTariff = Json.createArrayBuilder();
                            for (Object[] data1 : data) {
                                JsonObjectBuilder row = Json.createObjectBuilder();
                                row.add("year", MySQLQuery.getAsInteger(data1[0]));
                                row.add("month", MySQLQuery.getAsInteger(data1[1]));
                                row.add("lvl1", MySQLQuery.getAsBigDecimal(data1[2], true));
                                row.add("lvl2", MySQLQuery.getAsBigDecimal(data1[3], true));
                                row.add("lvl3", MySQLQuery.getAsBigDecimal(data1[4], true));
                                row.add("lvl4", MySQLQuery.getAsBigDecimal(data1[5], true));
                                row.add("lvl5", MySQLQuery.getAsBigDecimal(data1[6], true));
                                dataTariff.add(row);
                            }
                            ob.add("dataTariff", dataTariff);
                        } else {
                            ob.addNull("dataTariff");
                        }

                        ob.add("msg", "éxito");
                        break;
                    }

                    case "getQuery": {
                        String str = req.getString("query");
                        String restriction = getString(req, "restriction");
                        String restricRecol = "";
                        String restricSweep = "";

                        String TYPE_RECOL = "TYPE_RECOL";
                        String TYPE_SWEEP = "TYPE_SWEEP";
                        String TYPE_UNDEFINED = "UNDEFINED";

                        if (restriction != null) {
                            String[] dataString = restriction.split(",");
                            restricRecol = " AND( "
                                    + "p.notes like '%" + dataString[0] + "%' OR "
                                    + "p.notes like '%" + dataString[1] + "%' ) ";

                            restricSweep = " AND( "
                                    + "p.route like '%" + dataString[0] + "%' OR "
                                    + "p.route like '%" + dataString[1] + "%' ) ";

                        }

                        Object[][] data = new MySQLQuery(" SELECT "
                                + " p.notes, "
                                + " CONCAT(r.l,',',r.m,',',r.x,',',r.j,',',r.v,',',r.s,',',r.d), "
                                + " CONCAT(p.start_time,',',p.end_time), p.id , 0 "
                                + " FROM urb_path_recol p  "
                                + " INNER JOIN urb_route r ON r.id = p.route_id "
                                + " INNER JOIN urb_recol_schedule s ON s.id = r.schedule_id AND s.active = 1 "
                                + " INNER JOIN neigh n ON n.id = p.neigh_id "
                                + " WHERE "
                                + " (p.notes like '%" + str + "%' " + restricRecol + ") OR n.name LIKE '%" + str + "%' "
                                + " UNION ALL "
                                + " SELECT "
                                + " p.route, "
                                + " CONCAT(p.l,',',p.m,',',p.x,',',p.j,',',p.v,',',p.s,',',p.d), "
                                + " p.`schedule` ,0 , p.id  "
                                + " FROM urb_sweep p "
                                + " INNER JOIN neigh n ON n.id = p.neigh_id "
                                + " WHERE "
                                + " (p.route like '%" + str + "%' " + restricSweep + ") OR n.name LIKE '%" + str + "%' ").getRecords(conn);

                        if (data != null && data.length > 0 && data[0] != null) {
                            JsonArrayBuilder dataSearch = Json.createArrayBuilder();
                            for (Object[] data1 : data) {
                                JsonObjectBuilder row = Json.createObjectBuilder();
                                row.add("route", data1[0] != null ? MySQLQuery.getAsString(data1[0]) : " ");
                                row.add("schedule", MySQLQuery.getAsString(data1[1]));
                                row.add("hours", MySQLQuery.getAsString(data1[2]));

                                if (MySQLQuery.getAsInteger(data1[3]) != 0) {
                                    row.add("type", TYPE_RECOL);
                                } else if (MySQLQuery.getAsInteger(data1[4]) != 0) {
                                    row.add("type", TYPE_SWEEP);
                                } else {
                                    row.add("type", TYPE_UNDEFINED);
                                }

                                dataSearch.add(row);
                            }
                            ob.add("dataSearch", dataSearch);
                        } else {
                            ob.addNull("dataSearch");
                        }

                        ob.add("msg", "éxito");
                        break;
                    }

                    case "searchNeigh": {
                        String str = req.getString("query");

                        Object[][] data = new MySQLQuery(
                                " SELECT "
                                + " n.id, n.name, n.lat, n.lon, p.id, w.id "
                                + " FROM neigh n "
                                + " LEFT JOIN urb_path_recol p ON p.neigh_id = n.id "
                                + " LEFT JOIN urb_route r ON r.id = p.route_id "
                                + " LEFT JOIN urb_recol_schedule s ON s.id = r.schedule_id AND s.active = 1 "
                                + " LEFT JOIN urb_sweep w ON w.neigh_id = n.id "
                                + " WHERE n.name LIKE '%" + str + "%'  AND (p.id IS NOT NULL OR w.id IS NOT NULL) "
                                + " AND (n.lat IS NOT NULL AND n.lon IS NOT NULL) "
                                + " GROUP BY n.id; ").getRecords(conn);

                        if (data != null && data.length > 0 && data[0] != null) {
                            JsonArrayBuilder dataSearch = Json.createArrayBuilder();
                            for (Object[] data1 : data) {
                                JsonObjectBuilder row = Json.createObjectBuilder();
                                row.add("neighId", MySQLQuery.getAsInteger(data1[0]));
                                row.add("neighName", MySQLQuery.getAsString(data1[1]));
                                row.add("lat", MySQLQuery.getAsBigDecimal(data1[2], true));
                                row.add("lon", MySQLQuery.getAsBigDecimal(data1[3], true));
                                row.add("recolInfo", (data1[4] != null));
                                row.add("sweepInfo", (data1[5] != null));
                                dataSearch.add(row);
                            }
                            ob.add("dataSearch", dataSearch);
                        } else {
                            ob.addNull("dataSearch");
                        }

                        ob.add("msg", "éxito");
                        break;
                    }

                    case "getNeighAddress": {
                        Integer sectorId = getInt(req, "sectorId");
                        String str = req.getString("query");

                        Object[][] data = new MySQLQuery(" SELECT "
                                + " n.name, "
                                + " n.lat, "
                                + " n.lon, "
                                + " n.id "
                                + " FROM neigh n "
                                + " WHERE "
                                + (sectorId != null ? " n.sector_id = " + sectorId : " true ") + " "
                                + (str != null ? " AND n.name like '%" + str + "%' " : " ")
                                + " ORDER BY n.name ASC ").getRecords(conn);

                        if (data != null && data.length > 0 && data[0] != null) {
                            JsonArrayBuilder dataNeighs = Json.createArrayBuilder();
                            for (Object[] data1 : data) {
                                JsonObjectBuilder row = Json.createObjectBuilder();
                                row.add("name", MySQLQuery.getAsString(data1[0]));
                                row.add("lat", MySQLQuery.getAsBigDecimal(data1[1], true));
                                row.add("lon", MySQLQuery.getAsBigDecimal(data1[2], true));
                                row.add("id", MySQLQuery.getAsInteger(data1[3]));
                                dataNeighs.add(row);
                            }
                            ob.add("dataNeighs", dataNeighs);
                        } else {
                            ob.addNull("dataNeighs");
                        }

                        ob.add("msg", "éxito");
                        break;
                    }

                    case "RegistToken": {
                        String token = getString(req, "token");
                        String imei = getString(req, "imei");
                        Integer neighId = getInt(req, "neighId");
                        Integer zoneId = getInt(req, "zoneId");

                        if (imei != null) {
                            Object[] tokenRow = new MySQLQuery("SELECT id, imei FROM urb_gcm_token c WHERE c.token = '" + token + "'").getRecord(conn);
                            if (tokenRow != null && tokenRow.length > 0) {
                                String imeiId = MySQLQuery.getAsString(tokenRow[1]);
                                if (imeiId.equals(imei)) {
                                    int id = MySQLQuery.getAsInteger(tokenRow[0]);
                                    new MySQLQuery("UPDATE urb_gcm_token SET "
                                            + "neigh_id = " + neighId + ", "
                                            + "zone_id = " + zoneId + " "
                                            + "WHERE id = " + id + "").executeUpdate(conn);
                                }
                            } else {
                                new MySQLQuery("DELETE FROM urb_gcm_token WHERE imei = '" + imei + "' ").executeDelete(conn);
                                new MySQLQuery("INSERT INTO urb_gcm_token  SET "
                                        + " imei=" + imei + ", "
                                        + " neigh_id=" + neighId + ", "
                                        + " zone_id=" + zoneId + ", "
                                        + " token= '" + token + "'").executeInsert(conn);
                            }
                        }
                        break;
                    }

                    case "getAllData": {
                        getAllData(req, conn, ob);
                        break;
                    }

                    default:
                        break;
                }
                ob.add("status", STATUS_OK);
            } catch (Exception ex) {
                Logger.getLogger(EmasUrbRequest.class.getName()).log(Level.SEVERE, null, ex);
                ob.add("status", STATUS_ERROR);
                String msg = ex.getMessage();
                if (msg != null && !msg.isEmpty()) {
                    ob.add("msg", msg);
                } else {
                    ob.add("msg", "Error desconocido.");
                }
            } finally {
                w.writeObject(ob.build());
            }

        } catch (Exception ex) {
            Logger.getLogger(EmasUrbRequest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySQLCommon.closeConnection(conn);
        }
    }

    private void getAllData(JsonObject req, Connection conn, JsonObjectBuilder ob) throws Exception {
        Integer neighId = getInt(req, "neighId");
        Double lat = req.getJsonNumber("lat").doubleValue();
        Double lon = req.getJsonNumber("lon").doubleValue();
        String token = getString(req, "token");
        String imei = getString(req, "imei");

        Object[][] data = null;

        if (neighId != null) {
            data = new MySQLQuery(" SELECT "
                    + " p.start_time, "
                    + " p.end_time, "
                    + " p.notes, "
                    + " CONCAT(r.l,',',r.m,',',r.x,',',r.j,',',r.v,',',r.s,',',r.d) "
                    + " FROM urb_path_recol p  "
                    + " INNER JOIN urb_route r ON r.id = p.route_id "
                    + " INNER JOIN urb_recol_schedule s ON s.id = r.schedule_id AND s.active = 1 "
                    + " WHERE p.neigh_id = " + neighId).getRecords(conn);
            if (data != null && data.length > 0 && data[0] != null) {
                JsonArrayBuilder dataScheduleRecol = Json.createArrayBuilder();
                for (Object[] data1 : data) {
                    JsonObjectBuilder row = Json.createObjectBuilder();
                    row.add("startTime", MySQLQuery.getAsString(data1[0]));
                    row.add("endTime", MySQLQuery.getAsString(data1[1]));
                    row.add("notes", data1[2] != null ? MySQLQuery.getAsString(data1[2]) : "");
                    row.add("schedule", MySQLQuery.getAsString(data1[3]));
                    dataScheduleRecol.add(row);
                }
                ob.add("dataScheduleRecol", dataScheduleRecol);
            } else {
                ob.addNull("dataScheduleRecol");
            }

            data = new MySQLQuery(" SELECT "
                    + " p.route, "
                    + " CONCAT(p.l,',',p.m,',',p.x,',',p.j,',',p.v,',',p.s,',',p.d), "
                    + " p.`schedule` "
                    + " FROM urb_sweep p "
                    + " WHERE p.neigh_id = " + neighId + " ").getRecords(conn);

            if (data != null && data.length > 0 && data[0] != null) {
                JsonArrayBuilder dataScheduleSweep = Json.createArrayBuilder();
                for (Object[] data1 : data) {
                    JsonObjectBuilder row = Json.createObjectBuilder();
                    row.add("route", data1[0] != null ? MySQLQuery.getAsString(data1[0]) : "");
                    row.add("schedule", data1[1] != null ? MySQLQuery.getAsString(data1[1]) : "");
                    row.add("hours", MySQLQuery.getAsString(data1[2]));
                    dataScheduleSweep.add(row);
                }
                ob.add("dataScheduleSweep", dataScheduleSweep);
            } else {
                ob.addNull("dataScheduleSweep");
            }
        }

        //ZoneSchedule
        Object zoneData[] = new MySQLQuery("SELECT g.id, g.name FROM neigh n INNER JOIN urb_green_zone g ON g.id = n.urb_zone_id WHERE n.id = " + neighId).getRecord(conn);
        Integer zoneId = MySQLQuery.getAsInteger(zoneData[0]);
        String zoneName = MySQLQuery.getAsString(zoneData[1]);

        if (zoneId != null) {
            data = new MySQLQuery(" SELECT "
                    + " s.beg_date, "
                    + " s.end_date, "
                    + " s.done "
                    + " FROM urb_green_span s "
                    + " WHERE "
                    + " s.active = 1 AND s.green_zone_id = " + zoneId + " ORDER BY s.beg_date ASC ").getRecords(conn);

            if (data != null && data.length > 0 && data[0] != null) {
                JsonArrayBuilder dataScheduleZone = Json.createArrayBuilder();
                for (Object[] data1 : data) {
                    JsonObjectBuilder row = Json.createObjectBuilder();
                    row.add("begDate", data1[0] != null ? MySQLQuery.getAsString(data1[0]) : "");
                    row.add("endDate", data1[1] != null ? MySQLQuery.getAsString(data1[1]) : "");
                    row.add("done", data1[2] != null ? MySQLQuery.getAsBoolean(data1[2]) : false);
                    dataScheduleZone.add(row);
                }
                ob.add("dataScheduleZone", dataScheduleZone);
            } else {
                ob.addNull("dataScheduleZone");
            }

        }
        JsonUtils.addString(ob, "zoneName", zoneName != null ? zoneName : "Su zona no esta disponible");
        JsonUtils.addInt(ob, "zoneId", zoneId);

        //tariff
        GregorianCalendar gc = new GregorianCalendar();
        int year = gc.get(GregorianCalendar.YEAR);
        JsonArrayBuilder dataTariff = null;
        data = new MySQLQuery(" SELECT "
                + " t.year, "
                + " t.month, "
                + " t.lvl1,t.lvl2,t.lvl3,t.lvl4,t.lvl5 "
                + " FROM urb_tariff t "
                + " WHERE "
                + " t.year = " + year + " ORDER BY t.month DESC ").getRecords(conn);

        if (data != null && data.length > 0 && data[0] != null) {
            dataTariff = Json.createArrayBuilder();
            for (Object[] data1 : data) {
                JsonObjectBuilder row = Json.createObjectBuilder();
                row.add("year", MySQLQuery.getAsInteger(data1[0]));
                row.add("month", MySQLQuery.getAsInteger(data1[1]));
                row.add("lvl1", MySQLQuery.getAsBigDecimal(data1[2], true));
                row.add("lvl2", MySQLQuery.getAsBigDecimal(data1[3], true));
                row.add("lvl3", MySQLQuery.getAsBigDecimal(data1[4], true));
                row.add("lvl4", MySQLQuery.getAsBigDecimal(data1[5], true));
                row.add("lvl5", MySQLQuery.getAsBigDecimal(data1[6], true));
                dataTariff.add(row);
            }
            ob.add("dataTariff", dataTariff);
        } else {
            ob.addNull("dataTariff");
        }

        //token
        if (imei != null) {
            Object[] tokenRow = new MySQLQuery("SELECT id, imei FROM urb_gcm_token c WHERE c.token = '" + token + "'").getRecord(conn);
            if (tokenRow != null && tokenRow.length > 0) {
                String imeiId = MySQLQuery.getAsString(tokenRow[1]);
                if (imeiId.equals(imei)) {
                    int id = MySQLQuery.getAsInteger(tokenRow[0]);
                    new MySQLQuery("UPDATE urb_gcm_token SET "
                            + "neigh_id = " + neighId + ", "
                            + "zone_id = " + zoneId + " "
                            + "WHERE id = " + id + "").executeUpdate(conn);
                }
            } else {
                new MySQLQuery("DELETE FROM urb_gcm_token WHERE imei = '" + imei + "' ").executeDelete(conn);
                new MySQLQuery("INSERT INTO urb_gcm_token  SET "
                        + " imei=" + imei + ", "
                        + " neigh_id=" + neighId + ", "
                        + " zone_id=" + zoneId + ", "
                        + " token= '" + token + "'").executeInsert(conn);
            }
        }

        ob.add("msg", "éxito");

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
        return "UrbanEmas";
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

    private String getString(JsonObject obj, String fieldName) {
        if (!obj.containsKey(fieldName)) {
            return null;
        }
        if (obj.isNull(fieldName)) {
            return null;
        }
        return obj.getString(fieldName);
    }

    public static Integer getCordHit(double lon, double lat, int typeId, String tableName, Connection conn) throws Exception {
        Object[][] sectData = new MySQLQuery("SELECT id FROM " + tableName).getRecords(conn);
        for (Object[] sectData1 : sectData) {
            int secId = MySQLQuery.getAsInteger(sectData1[0]);
            Object[] boxRow = new MySQLQuery("SELECT MAX(g.lat), MAX(g.lon), MIN(g.lat), MIN(g.lon) FROM gps_polygon g WHERE g.owner_type =" + typeId + " AND g.owner_id = " + secId).getRecord(conn);

            if (boxRow != null) {

                Double maxLat = MySQLQuery.getAsDouble(boxRow[0]);
                Double maxLon = MySQLQuery.getAsDouble(boxRow[1]);
                Double minLat = MySQLQuery.getAsDouble(boxRow[2]);
                Double minLon = MySQLQuery.getAsDouble(boxRow[3]);

                if (minLat < lat && maxLat > lat && minLon < lon && maxLon > lon) {
                    Object[][] polData = new MySQLQuery("SELECT g.lat, g.lon FROM gps_polygon g WHERE g.owner_type = " + typeId + " AND g.owner_id = " + secId + " ORDER BY place").getRecords(conn);
                    if (polData != null && polData.length > 2) {
                        Path2D path = new Path2D.Double();
                        path.moveTo(MySQLQuery.getAsDouble(polData[0][1]), MySQLQuery.getAsDouble(polData[0][0]));
                        for (int j = 1; j < polData.length; j++) {
                            Object[] polRow = polData[j];
                            path.lineTo(MySQLQuery.getAsDouble(polRow[1]), MySQLQuery.getAsDouble(polRow[0]));
                        }
                        path.closePath();
                        if (path.contains(lon, lat)) {
                            return secId;
                        }
                    }
                }
            }
        }

        return null;
    }
}
