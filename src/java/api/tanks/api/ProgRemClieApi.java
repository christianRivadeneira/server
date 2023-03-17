package api.tanks.api;

import api.BaseAPI;
import api.tanks.model.ClieInfo;
import api.tanks.model.ClieInfoProg;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;

@Path("/progRem")
public class ProgRemClieApi extends BaseAPI {

    @GET
    @Path("/getList")
    public Response getRecent(@QueryParam("limit") int limit) {
        MySQLQuery.PRINT_QUERIES = false;
        try (Connection con = getConnection()) {
            getSession(con);
            List<ClieInfo> clieInfoList = ClieInfo.getClieInfoList(new MySQLQuery("SELECT "
                    + "c.id, " //0
                    + "t.id, " //1
                    + "c.name, " //2
                    + "ct.name, " //3
                    + "c.phones, " //4
                    + "t.serial, " //5
                    + "bct.db, " //6
                    + "t.capacity, " //7
                    + "ct.id, " //8
                    + "c.categ_id, "//9
                    + "c.mirror_id, "//10
                    + "c.critic_level_perc, "//11
                    + "IF(t.ctr_type = 'cli', 'Cliente', 'Empresa') "//12
                    + "FROM ord_tank_client c "
                    + "INNER JOIN est_tank t ON t.client_id = c.id "
                    + "INNER JOIN city ct ON c.city_id = ct.id "
                    + "INNER JOIN bill_instance bct ON bct.id = c.bill_instance_id  "
                    + "LEFT JOIN est_schedule sc ON sc.clie_tank_id = c.id AND visit_date >= CURDATE() "
                    + "WHERE c.`type` = 'build' "
                    + "AND c.active "
                    + "AND t.active "
                    + "AND sc.id IS NULL "
                    + "AND (SELECT COUNT(*) = 0 FROM est_prog ep INNER JOIN est_prog_sede eps ON eps.prog_id = ep.id "
                    + "WHERE ep.prog_date >= CURDATE() AND eps.tank_client_id = c.id) "
                    + "ORDER BY c.name, t.serial").getRecords(con));
            Double kteGalToKg = new MySQLQuery("SELECT kte FROM est_cfg WHERE id = 1").getAsDouble(con);

            for (int i = 0; i < clieInfoList.size(); i++) {
                ClieInfo clie = clieInfoList.get(i);
                int tankId = clie.tankId;
                String query = "SELECT "
                        + "r.percent, "//0
                        + "r.last_percent, "//1
                        + "r.read_date, "//2
                        + "s.id, "//3
                        + "s.begin_date, "//4
                        + "s.end_date "//5
                        + "FROM est_tank_read r "
                        + "INNER JOIN " + clie.cityDbName + ".bill_span s ON r.bill_span_id = s.id "
                        + "WHERE "
                        + "r.percent IS NOT NULL AND "
                        + "r.tank_id = ?1 "
                        + "AND (s.state = 'reca' OR s.state = 'cons') "
                        + "ORDER BY r.id DESC "
                        + "LIMIT 1 ";
                Object[] row = new MySQLQuery(query).setParam(1, clie.tankId).getRecord(con);

                Double ki = null; //kilos iniciales
                Double kt = clie.tankCapa * kteGalToKg; //capacidad total del tanque
                Double km = kt * (clie.criticLevelPerc / 100d);
                clie.kdp = null; //promedio de kilos consumidos por día

                if (row == null) {
                    ki = clie.tankCapa * kteGalToKg;
                    Object[] tankRow = new MySQLQuery("SELECT "
                            + "t.id, "
                            + "cl.mirror_id "
                            + "FROM ord_tank_client cl "
                            + "INNER JOIN est_tank t ON t.client_id = cl.id "
                            + "INNER JOIN est_tank_read r ON r.tank_id = t.id "
                            + "INNER JOIN " + clie.cityDbName + ".bill_span s ON r.bill_span_id = s.id "
                            + "WHERE cl.categ_id = " + clie.categId + " "
                            + "AND t.capacity = " + clie.tankCapa + " "
                            + "AND cl.city_id = " + clie.cityId + " "
                            + "AND cl.active "
                            + "AND t.active "
                            + "AND t.id <> " + clie.tankId + " "
                            + "AND s.state = 'reca' "
                            + "ORDER BY t.capacity ASC "
                            + "LIMIT 1").getRecord(con);

                    if (tankRow != null) {
                        tankId = MySQLQuery.getAsInteger(tankRow[0]);
                        row = new MySQLQuery(query).setParam(1, tankId).getRecord(con);
                    }

                    if (tankRow == null || row == null) {
                        query = "SELECT "
                                + "r.percent, "//0
                                + "r.last_percent, "//1
                                + "r.read_date, "//2
                                + "s.id, "//3
                                + "s.begin_date, "//4
                                + "s.end_date "//5
                                + "FROM est_tank_read r "
                                + "INNER JOIN billing_pasto.bill_span s ON r.bill_span_id = s.id "
                                + "WHERE "
                                + "r.percent IS NOT NULL AND "
                                + "r.tank_id = ?1 "
                                + "AND (s.state = 'reca' OR s.state = 'cons') "
                                + "ORDER BY r.id DESC "
                                + "LIMIT 1 ";
                        tankRow = new MySQLQuery("SELECT "
                                + "t.id, "
                                + "cl.mirror_id "
                                + "FROM ord_tank_client cl "
                                + "INNER JOIN est_tank t ON t.client_id = cl.id "
                                + "INNER JOIN est_tank_read r ON r.tank_id = t.id "
                                + "INNER JOIN billing_pasto.bill_span s ON r.bill_span_id = s.id "
                                + "WHERE cl.categ_id = " + clie.categId + " "
                                + "AND cl.city_id = 3 "
                                + "AND cl.active "
                                + "AND t.active "
                                + "AND t.id <> " + clie.tankId + " "
                                + "AND s.state = 'reca' "
                                + "ORDER BY t.capacity ASC "
                                + "LIMIT 1").getRecord(con);

                        if (tankRow != null) {
                            tankId = MySQLQuery.getAsInteger(tankRow[0]);
                            row = new MySQLQuery(query).setParam(1, tankId).getRecord(con);
                        }

                        if (tankRow == null || row == null) {
                            tankRow = new MySQLQuery("SELECT "
                                    + "t.id, "
                                    + "cl.mirror_id "
                                    + "FROM ord_tank_client cl "
                                    + "INNER JOIN est_tank t ON t.client_id = cl.id "
                                    + "INNER JOIN est_tank_read r ON r.tank_id = t.id "
                                    + "INNER JOIN billing_pasto.bill_span s ON r.bill_span_id = s.id "
                                    + "WHERE cl.city_id = 3 "
                                    + "AND cl.active "
                                    + "AND t.active "
                                    + "AND t.id <> " + clie.tankId + " "
                                    + "AND s.state = 'reca' "
                                    + "ORDER BY t.capacity ASC "
                                    + "LIMIT 1").getRecord(con);

                            tankId = MySQLQuery.getAsInteger(tankRow[0]);
                            row = new MySQLQuery(query).setParam(1, tankId).getRecord(con);
                        }
                    }

                } else {
                    ki = kt * (MySQLQuery.getAsDouble(row[0]) / 100);
                    if (ki == 0) { // lectura da cero
                        ki = clie.tankCapa * kteGalToKg;
                    }
                }

                Double sumRems = new MySQLQuery("SELECT IFNULL(SUM(kgs), 0) "
                        + "FROM est_sale s "
                        + "WHERE s.est_tank_id = " + clie.tankId + " "
                        + "AND s.client_id = " + clie.clieId + " "
                        + "AND s.sale_date BETWEEN '" + new SimpleDateFormat("yyyy-MM-dd").format(MySQLQuery.getAsDate(row[2])) + "' AND NOW()").getAsDouble(con);

                Double sumKgsRemLastSpan = new MySQLQuery("SELECT COALESCE(SUM(kgs), 0) FROM est_sale "
                        + "WHERE client_id = " + clie.clieId + " AND est_tank_id = " + clie.tankId + " AND sale_date BETWEEN '" + row[4] + "' AND '" + row[2] + "'").getAsDouble(con);

                Double lastPerc = row[1] != null ? MySQLQuery.getAsDouble(row[1]) : 0;
                Double curPerc = row[0] != null ? MySQLQuery.getAsDouble(row[0]) : 0;
                Double lastReadKg = kt * (lastPerc / 100d);
                Double curReadKg = kt * (curPerc / 100d);

                clie.kdp = Math.ceil((lastReadKg + sumKgsRemLastSpan - curReadKg) / 30d);
                //clie.kdp = Math.ceil((kgsLastSpan + sumKgsRemLastSpan) / 30d); //Todos los span son de 30 días

                //System.out.println("Cliente " + clie.client + "." + clie.clieId + "." + clie.tankId + " - lastPerc " + lastPerc + " - curPerc " + curPerc + " - ki " + ki + " - kt " + kt + " - sumKgsRemLastSpan " + sumKgsRemLastSpan + " - kdp " + clie.kdp);
                Integer dt = new MySQLQuery("SELECT DATEDIFF(NOW(), r.read_date) "
                        + "FROM est_tank_read r "
                        + "WHERE r.bill_span_id = " + row[3] + " "
                        + "AND r.tank_id = " + tankId).getAsInteger(con); //Días transcurridos desde la última lectura.

                clie.remainCritic = ((ki - km + sumRems) / (clie.kdp > 0 ? clie.kdp : 1)) - dt;
                clie.remainZero = ((ki + sumRems) / (clie.kdp > 0 ? clie.kdp : 1)) - dt;
                if (clie.kdp < 0) { // si la velocidad es negativa entonces, se debe revisar el caso manualmente 
                    clie.remainCritic = 0d;
                    clie.remainZero = 0d;
                }

                Object[] vhRow = new MySQLQuery("SELECT "
                        + "s.vh_id, "
                        + "CONCAT(v.internal, ' ', v.plate), "
                        + "dv.driver_id "
                        + "FROM "
                        + "ord_tank_client c "
                        + "LEFT JOIN est_sale s ON s.client_id = c.id "
                        + "LEFT JOIN vehicle v ON s.vh_id = v.id "
                        + "LEFT JOIN driver_vehicle dv ON dv.vehicle_id = v.id AND dv.end IS NULL "
                        + "WHERE c.id = " + clie.clieId + " "
                        + "ORDER BY s.id DESC "
                        + "LIMIT 1").getRecord(con);

                if (vhRow != null) {
                    clie.lastVhId = MySQLQuery.getAsInteger(vhRow[0]);
                    clie.lastVhPlate = MySQLQuery.getAsString(vhRow[1]);
                    clie.lastEmpId = MySQLQuery.getAsInteger(vhRow[2]);
                }
            }

            Collections.sort(clieInfoList, new Comparator<ClieInfo>() {
                @Override
                public int compare(ClieInfo o1, ClieInfo o2) {
                    return o1.remainCritic.compareTo(o2.remainCritic);
                }
            });

            if (limit > clieInfoList.size()) {
                limit = clieInfoList.size();
            }

            List<ClieInfo> result = new ArrayList<>();
            for (int i = 0; i < limit; i++) {
                result.add(clieInfoList.get(i));
            }

            return Response.ok(result).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/progList")
    public Response progList(ClieInfoProg info) throws Exception {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            List<ClieInfo> attendList = new ArrayList<>();
            List<ClieInfo> unattendList = new ArrayList<>();//venir a borrar la tabla y esta parte porque de cliente vienen solo los que tienen vh

            String estProgIds = new MySQLQuery("SELECT GROUP_CONCAT(id) FROM est_prog WHERE prog_date >= CURDATE()").getAsString(conn);
            StringBuilder sb = new StringBuilder();
            StringBuilder sb1 = new StringBuilder();
            for (int i = 0; i < info.data.size(); i++) {
                sb.append(info.data.get(i).clieId).append(",");
                sb1.append(info.data.get(i).tankId).append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb1.deleteCharAt(sb1.length() - 1);
            new MySQLQuery("DELETE FROM est_schedule WHERE visit_date >= CURDATE() AND clie_tank_id IN(" + sb.toString() + ")").executeDelete(conn);
            new MySQLQuery("DELETE FROM est_prog_sede WHERE prog_id IN (" + estProgIds + ") AND tank_client_id IN(" + sb.toString() + ")").executeDelete(conn);
            new MySQLQuery("DELETE FROM est_sched_ignored WHERE check_date >= CURDATE() AND tank_id IN(" + sb1.toString() + ")").executeDelete(conn);

            for (int i = 0; i < info.data.size(); i++) {
                if (info.data.get(i).lastVhId != null) {
                    boolean exist = false;
                    for (int j = 0; j < attendList.size() && !exist; j++) {
                        if (info.data.get(i).clieId == attendList.get(j).clieId) {
                            exist = true;
                        }
                    }

                    if (!exist) {
                        attendList.add(info.data.get(i));
                    }
                } else {
                    unattendList.add(info.data.get(i));
                }
            }

            List<ClieInfo> toUnattended = new ArrayList<>();
            for (int i = 0; i < unattendList.size(); i++) {
                boolean exist = false;
                for (int j = 0; j < attendList.size() && !exist; j++) {
                    if (attendList.get(j).clieId == unattendList.get(i).clieId) {
                        exist = true;
                    }
                }
                if (!exist) {
                    toUnattended.add(unattendList.get(i));
                }
            }

            if (!attendList.isEmpty()) {
                sb = new StringBuilder();
                sb.append("INSERT INTO est_schedule (visit_date, place, clie_tank_id, type, vh_id, checked, emp_id) VALUES ");
                for (int i = 0; i < attendList.size(); i++) {
                    ClieInfo clie = attendList.get(i);
                    sb.append("('").append(info.progDate).append("',0,").append(clie.clieId).append(",'rem',").append(clie.lastVhId).append(",0,").append(clie.lastEmpId).append("),");
                }

                sb.deleteCharAt(sb.length() - 1);
                new MySQLQuery(sb.toString()).executeInsert(conn);
            }

            if (!unattendList.isEmpty()) {
                sb = new StringBuilder();
                sb.append("INSERT INTO est_sched_ignored (check_date, client_id, tank_id, emp_id) VALUES ");
                for (int i = 0; i < toUnattended.size(); i++) {
                    ClieInfo clie = toUnattended.get(i);
                    sb.append("(NOW(),").append(clie.clieId).append(",").append(clie.tankId).append(",").append(sl.employeeId).append("),");
                }

                sb.deleteCharAt(sb.length() - 1);
                new MySQLQuery(sb.toString()).executeInsert(conn);
            }

            return createResponse("ok");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
