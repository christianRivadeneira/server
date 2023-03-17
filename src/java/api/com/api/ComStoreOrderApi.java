package api.com.api;

import api.BaseAPI;
import java.sql.Connection;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import api.com.model.ComCfg;
import api.com.model.ComLog;
import api.com.model.ComStoreOrder;
import api.com.model.ComStoreOrderInv;
import api.com.model.PvOrderInv;
import api.com.model.PvOrderState;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.ws.rs.POST;
import model.system.SessionLogin;
import utilities.MySQLQuery;

@Path("/comStoreOrder")
public class ComStoreOrderApi extends BaseAPI {

    @POST
    public Response insert(ComStoreOrder obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            if (new MySQLQuery("SELECT COUNT(*) > 0 "
                    + "FROM com_store_order "
                    + "WHERE store_id = " + obj.storeId + " "
                    + "AND prog_dt = '" + new SimpleDateFormat("yyyy-MM-dd").format(obj.progDt) + "' "
                    + "AND cancel_dt IS NULL").getAsBoolean(conn)) {
                throw new Exception("Ya existe un pedido programado para la misma fecha.");
            }

            Calendar dateNow = Calendar.getInstance();

            int hour = dateNow.get(Calendar.HOUR);

            dateNow.set(Calendar.HOUR, 0);
            dateNow.set(Calendar.MINUTE, 0);
            dateNow.set(Calendar.SECOND, 0);
            dateNow.set(Calendar.MILLISECOND, 0);
            int day = dateNow.get(Calendar.DAY_OF_WEEK);

            Calendar dateProg = Calendar.getInstance();
            dateProg.setTime(obj.progDt);
            dateProg.set(Calendar.HOUR, 0);
            dateProg.set(Calendar.MINUTE, 0);
            dateProg.set(Calendar.SECOND, 0);
            dateProg.set(Calendar.MILLISECOND, 0);

            int orderSaturdayHour = new MySQLQuery("SELECT order_saturday_hour FROM inv_cfg").getAsInteger(conn);

            if (dateNow.getTime().after(dateProg.getTime())) {
                throw new Exception("La fecha de programación debe ser posterior a la actual");
            } else if (dateNow.getTime().compareTo(dateProg.getTime()) == 0) { //no se puede programar para el mismo dia
                throw new Exception("El pedido no se puede programar para el mismo día");
            } else if (dateProg.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                throw new Exception("El día Domingo no se realizan entregas.");
            } else if (day == Calendar.SATURDAY || hour >= orderSaturdayHour) { //sabado no se puede programar para el domingo o despues de las 4 no se puede programar para el otro dia
                dateNow.add(Calendar.DAY_OF_YEAR, 2);

                if (dateNow.getTime().after(dateProg.getTime())) {
                    throw new Exception("No es posible programarlo para esa fecha. Seleccione el día siguiente.");
                }
            }

            obj.takenDt = new Date();
            obj.insert(conn);

            for (PvOrderInv row : obj.load) {
                ComStoreOrderInv inv = new ComStoreOrderInv();
                inv.orderId = obj.id;
                inv.cylTypeId = row.capaId;
                inv.amount = row.amount;

                inv.insert(conn);
            }

            String logs = "Se creó el registro.";
            ComLog.createLog(obj.id, ComLog.STORE_ORDER, logs, obj.takenById, conn);

            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(ComCfg obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            obj.update(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            ComCfg obj = new ComCfg().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getOrderState")
    public Response getOrderState() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            Object[][] data = new MySQLQuery("SELECT "
                    + "o.taken_dt, "//0
                    + "o.prog_dt, "//1
                    + "(SELECT GROUP_CONCAT(conf.first_name, ' ', conf.last_name) FROM driver_vehicle dv LEFT JOIN employee conf ON dv.driver_id = conf.id WHERE dv.vehicle_id = v.id AND dv.end IS NULL), "//2
                    + "CONCAT(v.plate, '-', v.internal), "//3
                    + "(SELECT GROUP_CONCAT(i.amount, ' x ', ct.name, 'Lb') FROM com_store_order_inv i INNER JOIN cylinder_type ct ON i.cyl_type_id = ct.id WHERE i.order_id = o.id), "//4
                    + "IF(o.cancel_dt IS NOT NULL, '0', "
                    + "	IF(o.confirm_dt IS NOT NULL, '1', "
                    + "	IF(o.assign_dt IS NOT NULL, '2', '3'))), "//5
                    + "o.pv_sale_id "//6
                    + "FROM com_store_order o "
                    + "INNER JOIN employee sto ON o.store_id = sto.store_id "
                    + "LEFT JOIN vehicle v ON o.vh_id = v.id "
                    + "WHERE "
                    + "sto.id = " + sl.employeeId + " AND "
                    + "o.confirm_dt IS NULL AND "
                    + "o.cancel_dt IS NULL "
                    + "ORDER BY o.prog_dt DESC ").getRecords(conn);

            List<PvOrderState> lst = new ArrayList<>();
            for (int i = 0; i < data.length; i++) {
                Object[] row = data[i];
                PvOrderState item = new PvOrderState(row);
                if (row[6] != null) {
                    item.inventory = new MySQLQuery("SELECT GROUP_CONCAT(cnt) "
                            + "FROM (SELECT "
                            + "CONCAT(COUNT(tc.id), ' x ', ct.name, 'Lb') AS cnt "
                            + "FROM trk_pv_cyls tpc "
                            + "INNER JOIN trk_cyl tc ON tpc.cyl_id = tc.id "
                            + "INNER JOIN cylinder_type ct ON tc.cyl_type_id = ct.id "
                            + "WHERE tpc.`type` = 'del' "
                            + "AND tpc.pv_sale_id = " + MySQLQuery.getAsInteger(row[6]) + " "
                            + "GROUP BY ct.id) AS l").getAsString(conn);
                }
                lst.add(item);
            }

            return createResponse(lst);
        } catch (Exception e) {
            return createResponse(e);
        }
    }
}
