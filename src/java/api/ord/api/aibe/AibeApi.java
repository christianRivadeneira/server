package api.ord.api.aibe;

import api.BaseAPI;
import api.ord.dto.aibe.AlertsInfo;
import api.ord.dto.aibe.ContractInfo;
import api.ord.dto.aibe.PendOrdersInfo;
import api.ord.model.OrdAibeResponse;
import java.sql.Connection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import utilities.Dates;
import utilities.MySQLQuery;
import utilities.SysTask;

@Path("/aibe")
public class AibeApi extends BaseAPI {

    private final int MINUTES_API_REFILL = 30;
    private Preferences prefs;
    private final String API_KEY = "[3BMvwgMwqp8s]";

    private void validatePermission(ContainerRequestContext request) throws Exception {
        if (request.getHeaders().get("API_KEY") == null || !request.getHeaders().get("API_KEY").toString().equals(API_KEY)) {
            throw new Exception("Usted no tiene permiso para acceder a este recurso");
        }
    }

    @GET
    @Path("/clients")
    public Response getClients(@QueryParam("clieDoc") String clieDoc, ContainerRequestContext request) {
        try (Connection conn = getConnection()) {
            validatePermission(request);

            if (clieDoc == null || clieDoc.isEmpty()) {
                throw new Exception("Falta parámetro documento del cliente.");
            }
            if (!clieDoc.matches("[0-9]+")) {
                throw new Exception("El documento de cliente debe ser numérico");
            }

            Object[][] data = new MySQLQuery("SELECT "
                    + "i.id, "
                    + "CONCAT(i.first_name, ' ', i.last_name), "
                    + "CONCAT(i.address, IFNULL(CONCAT(' ', n.name), '')), "
                    + "COALESCE(c.name, ''), "
                    + "i.phones, "
                    + "i.document "
                    + "FROM ord_contract_index i "
                    + "LEFT JOIN neigh n ON i.neigh_id = n.id "
                    + "LEFT JOIN city c ON i.city_id = c.id "
                    + "WHERE i.document = ?1 "
                    + "AND i.active").setParam(1, clieDoc).getRecords(conn);

            List<ContractInfo> result = new ArrayList<>();
            for (Object[] row : data) {
                ContractInfo client = new ContractInfo();
                client.ctrId = MySQLQuery.getAsInteger(row[0]);
                client.ctrName = MySQLQuery.getAsString(row[1]);
                client.ctrAddress = MySQLQuery.getAsString(row[2]);
                client.ctrCity = MySQLQuery.getAsString(row[3]);
                client.ctrPnones = MySQLQuery.getAsString(row[4]);
                client.ctrDocument = MySQLQuery.getAsString(row[5]);
                result.add(client);
            }

            if (result.isEmpty()) {
                throw new Exception("No se encontraron clientes con ese documento");
            }

            return createResponse(result);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/orderCall")
    public Response setOrderCall(OrdAibeResponse resp) {
        try (Connection conn = getConnection()) {

            resp.cylOrderId = new MySQLQuery("SELECT id FROM ord_cyl_order "
                    + "WHERE index_id = ?1 "
                    + "AND day = CURDATE() "
                    + "AND cancel_cause_id IS NULL "
                    + "ORDER BY id DESC LIMIT 1").setParam(1, resp.ctrId).getAsInteger(conn);

            if (resp.cylOrderId == null) {
                throw new Exception("No se encontró pedido para el contrato enviado");
            }

            resp.insert(conn);

            return createResponse();
        } catch (Exception e) {
            return createResponse(e);
        }
    }

    @GET
    @Path("/toConfirm")
    public Response ordersToConfirm(ContainerRequestContext request) {
        try (Connection conn = getConnection()) {
            validatePermission(request);
            Object[][] ordersData = new MySQLQuery("SELECT "
                    + "o.id, "
                    + "CONCAT(i.first_name, ' ', i.last_name), "
                    + "CONCAT(i.address, IFNULL(CONCAT(' ', n.name), '')), "
                    + "c.name, "
                    + "i.phones, "
                    + "concat(o.day, ' ', o.taken_hour), "
                    + "i.document "
                    + "FROM ord_cyl_order o "
                    + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                    + "LEFT JOIN neigh n ON i.neigh_id = n.id "
                    + "LEFT JOIN city c ON i.city_id = c.id "
                    + "WHERE o.cancel_cause_id IS NULL "
                    + "AND o.confirm_hour IS NOT NULL "
                    + "AND o.assig_hour IS NOT NULL "
                    + "AND CURDATE() = o.day ").getRecords(conn);

            List<PendOrdersInfo> result = new ArrayList<>();
            for (Object[] row : ordersData) {
                PendOrdersInfo ord = new PendOrdersInfo();
                ord.cylOrderId = MySQLQuery.getAsInteger(row[0]);
                ord.clieName = MySQLQuery.getAsString(row[1]);
                ord.clieAddress = MySQLQuery.getAsString(row[2]);
                ord.clieCity = MySQLQuery.getAsString(row[3]);
                ord.cliePhones = MySQLQuery.getAsString(row[4]);
                ord.orderDate = MySQLQuery.getAsDate(row[5]);
                ord.clieDocument = MySQLQuery.getAsString(row[6]);
                result.add(ord);
            }

            if (result.isEmpty()) {
                throw new Exception("No hay pedidos por confirmar");
            }
            return createResponse(result);
        } catch (Exception e) {
            return createResponse(e);
        }
    }

    @POST
    @Path("/confirmOrder")
    public Response confirmOrder(OrdAibeResponse resp) {
        try (Connection conn = getConnection()) {
            if (resp.aibeResp.toLowerCase().equals("si")) {
                new MySQLQuery("UPDATE ord_cyl_order "
                        + "SET confirm_dt = CURDATE(), "
                        + "confirm_hour = CURTIME(), "
                        + "confirmed_by_id = 1 "
                        + "WHERE id = ?1 "
                        + "AND confirm_dt IS NULL").setParam(1, resp.cylOrderId).executeUpdate(conn);
            }

            String res = "Confirmación de pedido, rta: " + resp.aibeResp;
            resp.aibeResp = res;
            resp.insert(conn);
            return createResponse();
        } catch (Exception e) {
            return createResponse(e);
        }
    }

    @GET
    @Path("/alerts")
    public Response getAlerts(ContainerRequestContext request) {
        try (Connection conn = getConnection()) {
            validatePermission(request);

            Object[][] alertsData = new MySQLQuery("SELECT "
                    + "q.id, "
                    + "CONCAT(i.first_name, ' ', i.last_name), "
                    + "CONCAT(i.address, IFNULL(CONCAT(' ', n.name), '')), "
                    + "c.name, "
                    + "i.phones, "
                    + "s.date, "
                    + "GROUP_CONCAT(t.neg_question), "
                    + "i.document "
                    + "FROM com_app_neg_quest q "
                    + "INNER JOIN trk_sale s ON q.trk_sale_id = s.id "
                    + "INNER JOIN ord_contract_index i ON s.index_id = i.id "
                    + "INNER JOIN com_app_answer a ON a.neg_quest_id = q.id "
                    + "INNER JOIN com_app_question t ON t.id = a.question_id "
                    + "LEFT JOIN neigh n ON i.neigh_id = n.id "
                    + "LEFT JOIN city c ON i.city_id = c.id "
                    + "WHERE q.active "
                    + "AND q.cancelled_by IS NULL "
                    + "AND q.converted_by IS NULL "
                    + "AND i.phones IS NOT NULL AND i.phones <> 'null' AND i.phones <> '00' AND i.phones <> '' "
                    + "AND DATE(s.date) = CURDATE() "
                    + "GROUP BY q.id").getRecords(conn);

            List<AlertsInfo> result = new ArrayList<>();
            for (Object[] row : alertsData) {
                AlertsInfo alert = new AlertsInfo();
                alert.negQuestId = MySQLQuery.getAsInteger(row[0]);
                alert.clieName = MySQLQuery.getAsString(row[1]);
                alert.clieAddress = MySQLQuery.getAsString(row[2]);
                alert.clieCity = MySQLQuery.getAsString(row[3]);
                alert.cliePhones = MySQLQuery.getAsString(row[4]);
                alert.saleDate = MySQLQuery.getAsDate(row[5]);
                alert.reason = MySQLQuery.getAsString(row[6]);
                alert.cliDocument = MySQLQuery.getAsString(row[7]);
                result.add(alert);
            }

            if (result.isEmpty()) {
                throw new Exception("No se encontraron resultados.");
            }

            return createResponse(result);
        } catch (Exception e) {
            return createResponse(e);
        }
    }

    @GET
    @Path("/refill")
    public Response refill(ContainerRequestContext request) {
        try (Connection conn = getConnection()) {
            SysTask t = null;
            try {
                validatePermission(request);
                t = new SysTask(AibeApi.class, "refill", 1, conn);

                Date now = MySQLQuery.now(conn);
                prefs = Preferences.userRoot().node("/sigma/aibe");
                String dateTrkSale = prefs.get("dateTempTrkSale", "");
                Date dtTrkSale = null;

                if (dateTrkSale != null && dateTrkSale.length() > 0) {
                    dtTrkSale = Dates.getSQLDateTimeFormat().parse(dateTrkSale);
                }

                if (dtTrkSale == null || Dates.compareMinutes(dtTrkSale, now) > MINUTES_API_REFILL) {
                    prefs.put("dateTempTrkSale", Dates.getSQLDateTimeFormat().format(now));
                    new MySQLQuery("CREATE TEMPORARY TABLE IF NOT EXISTS lst_trk_sale AS (SELECT s.index_id AS index_id "
                            + "FROM trk_sale s "
                            + "WHERE s.prom_last_sales <> 0 AND (s.prom_last_sales - 7) < DATEDIFF(NOW(), s.date))").executeUpdate(conn);

                    Object[][] data = new MySQLQuery("SELECT "
                            + "CONCAT(IFNULL(i.first_name, ''), ' ', IFNULL(i.last_name, '')) AS nm, "
                            + "CONCAT(i.address, IFNULL(CONCAT(' ', n.name), '')), "
                            + "c.name, "
                            + "i.phones, "
                            + "i.document, "
                            + "i.id "
                            + "FROM lst_trk_sale s "
                            + "INNER JOIN ord_contract_index i ON s.index_id = i.id "
                            + "LEFT JOIN neigh n ON i.neigh_id = n.id "
                            + "LEFT JOIN city c ON i.city_id = c.id "
                            + "WHERE i.phones IS NOT NULL AND i.phones <> 'null' AND i.phones <> '00' AND i.phones <> '0' AND i.phones <> ''  "
                            + "ORDER BY nm "
                            + "LIMIT 5000 ").getRecords(conn);

                    List<ContractInfo> res = new ArrayList<>();
                    for (Object[] row : data) {
                        ContractInfo ct = new ContractInfo();
                        ct.ctrName = MySQLQuery.getAsString(row[0]);
                        ct.ctrAddress = MySQLQuery.getAsString(row[1]);
                        ct.ctrCity = MySQLQuery.getAsString(row[2]);
                        ct.ctrPnones = MySQLQuery.getAsString(row[3]);
                        ct.ctrDocument = MySQLQuery.getAsString(row[4]);
                        ct.ctrId = MySQLQuery.getAsInteger(row[5]);
                        res.add(ct);
                    }

                    if (res.isEmpty()) {
                        throw new Exception("No se encontraron resultados.");
                    }
                    t.success(conn);
                    return createResponse(res);
                } else {
                    throw new Exception("No se puede consultar, No ha transcurrido los " + MINUTES_API_REFILL + " minutos de la ultima consulta");
                }
            } catch (Exception e) {
                if (t != null) {
                    t.error(e, conn);
                }
                return createResponse(e);
            }
        } catch (Exception e) {
            return createResponse(e);
        }
    }
}
