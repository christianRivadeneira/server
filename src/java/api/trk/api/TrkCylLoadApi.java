package api.trk.api;

import api.BaseAPI;
import api.com.model.ComCfg;
import api.trk.dto.ExpInventory;
import api.trk.dto.Load;
import api.trk.model.TrkCyl;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import api.trk.model.TrkCylLoad;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import utilities.MySQLQuery;
import web.ShortException;
import web.gates.cylTrip.GtCylTrip;

@Path("/trkCylLoad")
public class TrkCylLoadApi extends BaseAPI {

    @POST
    public Response insert(TrkCylLoad obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(TrkCylLoad obj) {
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
            TrkCylLoad obj = new TrkCylLoad().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            TrkCylLoad.delete(id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getAll")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(TrkCylLoad.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private static void checkReload(GtCylTrip trip, ComCfg comCfg, Connection conn) throws Exception {
        if (comCfg.chkDelCylLoad) {
            Date maxDate = new MySQLQuery("SELECT MAX(l.d) FROM "
                    + "(SELECT MAX(reload_date) AS d FROM gt_reload WHERE vh_trip_id = ?1 AND !cancel "
                    + "UNION "
                    + "SELECT MAX(cdt) AS d FROM gt_trip_reload WHERE trip_id = ?1 AND !cancelled) "
                    + "AS l").setParam(1, trip.id).getAsDate(conn);

            if (maxDate == null) {
                throw new ShortException("No se ubicó el recargue");
            }

            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(maxDate);

            gc.add(GregorianCalendar.MINUTE, comCfg.delCylLoadLimitMins);
            Date limitDate = gc.getTime();
            if (new Date().compareTo(limitDate) > 0) {
                throw new ShortException("No se puede modificar el recargue, ya pasaron mas de " + comCfg.delCylLoadLimitMins + " minutos");
            }
        }
    }

    private static void checkSold(ComCfg comCfg, TrkCyl cyl, GtCylTrip trip, SessionLogin sl, Connection conn) throws ShortException, Exception {
        if (comCfg.lockCylSale) {
            if (!cyl.salable && cyl.respId == null) {
                throw new ShortException("El NIF " + cyl.toString() + " fue vendido y no ha pasado por plataforma.");
            }
        } else {
            boolean sold = new MySQLQuery("SELECT COUNT(*) > 0 "
                    + "FROM trk_sale s "
                    + "LEFT JOIN trk_multi_cyls tmc ON tmc.sale_id = s.id "
                    + "WHERE (s.cylinder_id = " + cyl.id + " OR (tmc.cyl_id = " + cyl.id + " AND tmc.`type` = 'del')) "
                    + "AND s.emp_id = " + sl.employeeId + " "
                    + "AND DATE(s.date) >= (SELECT t.trip_date FROM gt_cyl_trip t WHERE id = " + trip.id + ")").getAsBoolean(conn);
            if (!sold) {
                sold = new MySQLQuery("SELECT COUNT(*) > 0 "
                        + "FROM trk_pv_sale s "
                        + "INNER JOIN trk_pv_cyls tmc ON tmc.pv_sale_id = s.id "
                        + "WHERE tmc.cyl_id = " + cyl.id + " AND tmc.`type` = 'del' "
                        + "AND s.emp_id = " + sl.employeeId + " "
                        + "AND DATE(s.dt) >= (SELECT t.trip_date FROM gt_cyl_trip t WHERE id = " + trip.id + ")").getAsBoolean(conn);
            }
            if (sold) {
                throw new ShortException("El NIF " + cyl.toString() + " fue vendido y no ha pasado por plataforma.");
            }
        }
    }

    @POST
    @Path("/addToSaleTrip")
    public Response addToSaleTrip(@QueryParam("cylId") int cylId, @QueryParam("tripId") int tripId, @QueryParam("type") String type, @QueryParam("altCylId") Integer altCylId) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            ComCfg comCfg = new ComCfg().select(1, conn);
            TrkCyl cyl = new TrkCyl().select(cylId, conn);
            GtCylTrip trip = new GtCylTrip().select(tripId, conn);
            checkSold(comCfg, cyl, trip, sl, conn);
            if (type.equals("load")) {
                if (trip.sdt != null && comCfg.chkDelCylLoad) {
                    throw new ShortException("No se pueden escanear cilindros de cargue, ya salió por portería");
                }
            } else if (type.equals("reload")) {
                checkReload(trip, comCfg, conn);
            }

            TrkCylLoad load = new TrkCylLoad();

            load.cylTripId = tripId;
            load.cylId = cylId;
            load.type = type;
            load.dateOut = new Date();
            load.altCylId = altCylId;
            if (new MySQLQuery("SELECT COUNT(*) = 0 FROM trk_cyl_load WHERE cyl_trip_id = " + tripId + " AND cyl_id = " + cylId + " AND date_del IS NULL FOR UPDATE").getAsBoolean(conn)) {
                load.insert(conn);

                if (comCfg.lockCylSale) {
                    cyl.salable = false;
                    cyl.respId = sl.employeeId;
                    cyl.update(conn);
                }
            }

            return Response.ok().build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    @Path("/deleteFromSaleTrip")
    public Response deleteFromTrip(@QueryParam("cylId") int cylId, @QueryParam("tripId") int tripId) {
        try (Connection conn = getConnection()) {
            //MySQLQuery.DUMP_QUERIES = true;
            SessionLogin sl = getSession(conn);
            ComCfg comCfg = new ComCfg().select(1, conn);
            TrkCyl cyl = new TrkCyl().select(cylId, conn);
            TrkCylLoad load = new TrkCylLoad().select(new MySQLQuery("SELECT " + TrkCylLoad.getSelFlds("") + " "
                    + "FROM trk_cyl_load "
                    + "WHERE cyl_trip_id = ?1 AND cyl_id = ?2 "
                    + "AND date_del IS NULL").setParam(1, tripId).setParam(2, cylId), conn);

            GtCylTrip trip = new GtCylTrip().select(tripId, conn);
            checkSold(comCfg, cyl, trip, sl, conn);
            if (load.type.equals("load")) {
                if (trip.sdt != null && comCfg.chkDelCylLoad) {
                    throw new ShortException("El NIF " + cyl.toString() + " no se puede eliminar del cargue, ya salió por portería");
                }
            } else if (load.type.equals("reload")) {
                checkReload(trip, comCfg, conn);
            }

            load.dateDel = new Date();
            load.update(conn);

            if (comCfg.lockCylSale) {
                cyl.salable = true;
                cyl.respId = null;
                cyl.update(conn);
            }

            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    @Path("/deleteAllScan")
    public Response deleteAllScan(@QueryParam("tripId") int tripId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            if (new MySQLQuery("SELECT sdt IS NULL FROM gt_cyl_trip WHERE id = " + tripId).getAsBoolean(conn)) {
                new MySQLQuery("DELETE FROM trk_cyl_load WHERE cyl_trip_id = " + tripId).executeDelete(conn);
            } else {
                throw new ShortException("El cargue no se puede borrar porque ya salió por portería.");
            }

            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getExpInventory")
    public Response getExpInventory(@QueryParam("smanId") int smanId) {
        try (Connection conn = getConnection()) {

            Integer empId = new MySQLQuery("SELECT IFNULL(sto.id, empLiq.id) "
                    + "FROM dto_salesman ds "
                    + "LEFT JOIN employee sto ON ds.store_id = sto.store_id "
                    + "LEFT JOIN dto_salesman liq ON ds.liquidator_id = liq.id "
                    + "LEFT JOIN employee empLiq ON empLiq.store_id = liq.store_id "
                    + "WHERE ds.id = " + smanId).getAsInteger(conn);

            if (empId == null) {
                throw new Exception("El vendedor no está asociado a un Almacén o a un liquidador, comuníquese con Sistemas");
            }

            Object[] row = new MySQLQuery("SELECT "
                    + "e.store_id, "
                    + "IFNULL((SELECT DATE(MAX(pvs.dt)) FROM trk_pv_sale pvs WHERE pvs.store_id = e.store_id), '2020-01-01') "
                    + "FROM "
                    + "employee e "
                    + "WHERE e.id = " + empId).getRecord(conn);

            Object[][] trasInv = null;
            Object[][] trasCyls = null;
            if (row != null && row.length > 0) {
                Integer stoDeliverId = new MySQLQuery("SELECT "
                        + "dv.driver_id "
                        + "FROM "
                        + "com_store_order o "
                        + "INNER JOIN driver_vehicle dv ON o.vh_id = dv.vehicle_id "
                        + "WHERE "
                        + "o.store_id = " + MySQLQuery.getAsInteger(row[0]) + " "
                        + "AND o.cancel_dt IS NULL "
                        + "AND dv.`end` IS NULL "
                        + "AND o.taken_dt > ?1 "
                        + "ORDER BY o.taken_dt DESC "
                        + "LIMIT 1").setParam(1, MySQLQuery.getAsDate(row[1])).getAsInteger(conn);

                if (stoDeliverId != null) {
                    trasInv = new MySQLQuery("SELECT "
                            + "ct.id AS typ, "
                            + "COUNT(tc.id) AS amo, "
                            + "ct.name AS nm "
                            + "FROM trk_cyl tc "
                            + "INNER JOIN cylinder_type ct ON tc.cyl_type_id = ct.id "
                            + "WHERE "
                            + "tc.resp_id = " + stoDeliverId + " "
                            + "GROUP BY ct.id").getRecords(conn);

                    trasCyls = new MySQLQuery("SELECT "
                            + "tc.id, "
                            + "CONCAT(LPAD(tc.nif_y, 2, 0), LPAD(tc.nif_f, 4, 0), LPAD(tc.nif_s, 6, 0)), "
                            + "ct.`name` "
                            + "FROM trk_cyl tc "
                            + "INNER JOIN cylinder_type ct ON tc.cyl_type_id = ct.id "
                            + "WHERE tc.resp_id = " + stoDeliverId).getRecords(conn);
                }
            }

            Object[][] invData = new MySQLQuery("SELECT "
                    + "ct.id AS typ, "
                    + "COUNT(tc.id) AS amo, "
                    + "ct.name AS nm "
                    + "FROM trk_cyl tc "
                    + "INNER JOIN cylinder_type ct ON tc.cyl_type_id = ct.id "
                    + "WHERE "
                    + "tc.resp_id = " + empId + " "
                    + "GROUP BY ct.id").getRecords(conn);

            List<Load> inv = new ArrayList<>();
            for (int i = 0; i < invData.length; i++) {
                Object[] obj = invData[i];
                Load item = new Load();
                item.amount = MySQLQuery.getAsInteger(obj[1]);
                item.scnAmount = MySQLQuery.getAsInteger(obj[1]);
                item.cylTypeId = MySQLQuery.getAsInteger(obj[0]);
                item.mgName = MySQLQuery.getAsString(obj[2]);
                inv.add(item);
            }

            Object[][] cylsScanData = new MySQLQuery("SELECT "
                    + "tc.id, "
                    + "CONCAT(LPAD(tc.nif_y, 2, 0), LPAD(tc.nif_f, 4, 0), LPAD(tc.nif_s, 6, 0)), "
                    + "ct.`name` "
                    + "FROM trk_cyl tc "
                    + "INNER JOIN cylinder_type ct ON tc.cyl_type_id = ct.id "
                    + "WHERE tc.resp_id = " + empId).getRecords(conn);

            List<TrkCylLoad> lstCyls = new ArrayList<>();
            Random r = new Random();
            ExpInventory res = new ExpInventory();
            res.virtualTripId = r.nextInt(1000000) + 1;
            for (int i = 0; i < cylsScanData.length; i++) {
                Object[] obj = cylsScanData[i];
                TrkCylLoad item = new TrkCylLoad();
                item.cylId = MySQLQuery.getAsInteger(obj[0]);
                item.type = "load";
                item.nif = MySQLQuery.getAsString(obj[1]);
                item.cylCap = MySQLQuery.getAsString(obj[2]);
                item.cylTripId = res.virtualTripId;
                lstCyls.add(item);
            }

            res.inventory = inv;
            res.lstCyls = lstCyls;

            List<Load> lstTrasInv = new ArrayList<>();
            if (trasInv != null && trasInv.length > 0) {
                for (int i = 0; i < trasInv.length; i++) {
                    Object[] obj = trasInv[i];
                    Load item = new Load();
                    item.amount = MySQLQuery.getAsInteger(obj[1]);
                    item.scnAmount = MySQLQuery.getAsInteger(obj[1]);
                    item.cylTypeId = MySQLQuery.getAsInteger(obj[0]);
                    item.mgName = MySQLQuery.getAsString(obj[2]);
                    lstTrasInv.add(item);
                }
            }

            List<TrkCylLoad> lstTrasCyls = new ArrayList<>();
            if (trasCyls != null && trasCyls.length > 0) {
                for (int i = 0; i < trasCyls.length; i++) {
                    Object[] obj = trasCyls[i];
                    TrkCylLoad item = new TrkCylLoad();
                    item.cylId = MySQLQuery.getAsInteger(obj[0]);
                    item.type = "reload";
                    item.nif = MySQLQuery.getAsString(obj[1]);
                    item.cylCap = MySQLQuery.getAsString(obj[2]);
                    item.cylTripId = res.virtualTripId;
                    lstTrasCyls.add(item);
                }
            }

            res.trasInventory = lstTrasInv;
            res.lstTrasCyls = lstTrasCyls;

            return createResponse(res);
        } catch (Exception e) {
            return createResponse(e);
        }
    }

}
