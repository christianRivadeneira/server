package api.gt.api;

import api.BaseAPI;
import api.com.model.ComCfg;
import api.gt.model.GtCylInv;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import api.gt.model.GtCylTrip;
import api.gt.model.GtCylTripInfo;
import api.trk.model.CylinderType;
import api.trk.model.TrkCyl;
import api.trk.model.TrkCylLoad;
import api.trk.model.TrkMto;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import web.ShortException;
import web.marketing.cylSales.CylValidations;

@Path("/gtCylTrip")
public class GtCylTripApi extends BaseAPI {

    @POST
    public Response insert(GtCylTrip obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(GtCylTrip obj) {
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
            GtCylTrip obj = new GtCylTrip().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GtCylTrip.delete(id, conn);
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
            return createResponse(GtCylTrip.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    //Cuando el cilindro regresa del mantenimiento el cargue queda en la trk_mto en lugar de la trk_cyl_load
    //la trk_cyl_load tiene el cargue de salida y la información del mantenimiento
    @POST
    @Path("/registerDepartureOnMtoTrip")
    public Response registerDepartureOnMtoTrip(@QueryParam("nif") String nif, @QueryParam("tripId") int tripId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return Response.ok(registerDeparture(nif, tripId, false, null, false, true, conn)).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/removeDepartureOnMtoTrip")
    public Response removeDepartureOnMtoTrip(@QueryParam("cylId") int cylId, @QueryParam("tripId") int tripId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            removeDeparture(cylId, tripId, false, null, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    //14/11/2019 se deja por compatibilidad, se puede quitar luego de lanzar la versión
    @GET
    @Path("/departedCylsOnMtoTrip")
    public Response departedCylsOnMtoTrip(@QueryParam("tripId") int tripId) {
        try (Connection conn = getConnection()) {
            return createResponse(TrkCyl.getList(new MySQLQuery("SELECT "
                    + TrkCyl.getSelFlds("c")
                    + "FROM trk_cyl_load l "
                    + "INNER JOIN trk_cyl c ON c.id = l.cyl_id "
                    + "WHERE l.cyl_trip_id = ?1 AND l.`type` = 'trip' "
                    + "AND l.date_del IS NULL"
            ).setParam(1, tripId), conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/departedCylsOnMtoTripV1")
    public Response departedCylsOnMtoTripV1(@QueryParam("tripId") int tripId) {
        try (Connection conn = getConnection()) {
            LoadInfo rta = new LoadInfo();
            rta.cyls = TrkCyl.getList(new MySQLQuery("SELECT "
                    + TrkCyl.getSelFlds("c")
                    + "FROM trk_cyl_load l "
                    + "INNER JOIN trk_cyl c ON c.id = l.cyl_id "
                    + "WHERE l.cyl_trip_id = ?1 AND l.`type` = 'trip' "
                    + "AND l.date_del IS NULL"
            ).setParam(1, tripId), conn);
            rta.invs = GtCylInv.getInv(tripId, "c", conn);
            return createResponse(rta);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/unloadedCylsOnTrip")
    public Response unloadedCylsOnTrip(@QueryParam("tripId") int tripId) {
        try (Connection conn = getConnection()) {
            LoadInfo rta = new LoadInfo();
            rta.cyls = TrkCyl.getList(new MySQLQuery("SELECT "
                    + TrkCyl.getSelFlds("c")
                    + "FROM trk_cyl_load l "
                    + "INNER JOIN trk_cyl c ON c.id = l.cyl_id "
                    + "WHERE l.cyl_trip_id = ?1 AND l.`type` = 'unload' "
                    + "AND l.date_del IS NULL"
            ).setParam(1, tripId), conn);
            rta.invs = GtCylInv.getInv(tripId, "e", conn);
            rta.noReadableNifs = new MySQLQuery("SELECT COUNT(*) FROM trk_sale s WHERE s.gt_trip_id = " + tripId + " AND s.cyl_received_id IS NULL AND s.sale_type <> 'mul'").getAsInteger(conn);
            rta.pqrCyls = new MySQLQuery("SELECT COUNT(*) FROM trk_cyl_novelty WHERE trip_id = " + tripId).getAsInteger(conn);
            return createResponse(rta);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    //Cuando el cilindro regresa del mantenimiento el cargue queda en la trk_mto en lugar de la trk_cyl_load
    //la trk_cyl_load tiene el cargue de salida y la información del mantenimiento
    @POST
    @Path("/registerArrivalOnMtoTrip")
    public Response registerArrivalOnMtoTrip(@QueryParam("nif") String nif, @QueryParam("tripId") int tripId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            TrkCyl cyl = TrkCyl.selectByNif(nif, conn);
            if (cyl == null) {
                throw new Exception("El cilindro no está registrado");
            }
            boolean rep = new MySQLQuery("SELECT COUNT(*) > 0 FROM trk_mto m WHERE m.cyl_trip_id = " + tripId + " AND m.trk_cyl_id = " + cyl.id).getAsBoolean(conn);
            if (!rep) {
                new MySQLQuery("UPDATE trk_cyl_load SET date_entry = NOW() WHERE cyl_id = " + cyl.id + " AND date_entry IS NULL AND `type` = 'trip'").executeUpdate(conn);
                TrkMto mto = new TrkMto();
                mto.cylTripId = tripId;
                mto.date = new Date();
                mto.mtoType = 1;
                mto.trkCylId = cyl.id;
                mto.insert(conn);
            } else {
                throw new ShortException("El cilindro ya fue escaneado");
            }
            return Response.ok(cyl).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/removeArrivalOnMtoTrip")
    public Response removeArrivalOnMtoTrip(@QueryParam("cylId") int cylId, @QueryParam("tripId") int tripId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            new MySQLQuery("DELETE FROM trk_mto WHERE cyl_trip_id = " + tripId + " AND trk_cyl_id = " + cylId).executeDelete(conn);
            new MySQLQuery("UPDATE trk_cyl_load SET date_entry = NULL WHERE cyl_id = " + cylId + " AND `type` = 'trip'").executeUpdate(conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    //14/11/2019 se deja por compatibilidad, se puede quitar luego de lanzar la versión
    @GET
    @Path("/arrivedCylsOnMtoTrip")
    public Response arrivedCylsOnMtoTrip(@QueryParam("tripId") int tripId) {
        try (Connection conn = getConnection()) {
            return createResponse(TrkCyl.getList(new MySQLQuery("SELECT "
                    + TrkCyl.getSelFlds("c")
                    + "FROM trk_mto m "
                    + "INNER JOIN trk_cyl c ON c.id = m.trk_cyl_id "
                    + "WHERE m.cyl_trip_id = " + tripId), conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/arrivedCylsOnMtoTripV1")
    public Response arrivedCylsOnMtoTripV1(@QueryParam("tripId") int tripId) {
        try (Connection conn = getConnection()) {
            LoadInfo info = new LoadInfo();
            info.cyls = TrkCyl.getList(new MySQLQuery("SELECT "
                    + TrkCyl.getSelFlds("c")
                    + "FROM trk_mto m "
                    + "INNER JOIN trk_cyl c ON c.id = m.trk_cyl_id "
                    + "WHERE m.cyl_trip_id = " + tripId), conn);
            info.invs = GtCylInv.getInv(tripId, "e", conn);
            return createResponse(info);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    ////////////////////////////////////////////////
    @POST
    @Path("/registerDepartureOnTrip")
    public Response registerDepartureOnTrip(@QueryParam("nif") String nif, @QueryParam("tripId") int tripId, @QueryParam("reserve") boolean reserve, @QueryParam("invCenterId") Integer invCenterId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return Response.ok(registerDeparture(nif, tripId, reserve, invCenterId, true, false, conn)).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/removeDepartureOnTrip")
    public Response removeDepartureOnTrip(@QueryParam("cylId") int cylId, @QueryParam("tripId") int tripId, @QueryParam("reserve") boolean reserve, @QueryParam("invCenterId") Integer invCenterId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            removeDeparture(cylId, tripId, reserve, invCenterId, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/departedCylsInfo")
    public Response departedCylsInfo(@QueryParam("tripId") int tripId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            LoadInfo rta = new LoadInfo();
            rta.cyls = TrkCyl.getList(new MySQLQuery("SELECT "
                    + TrkCyl.getSelFlds("c")
                    + "FROM trk_cyl_load l "
                    + "INNER JOIN trk_cyl c ON c.id = l.cyl_id "
                    + "WHERE l.cyl_trip_id = ?1 AND l.`type` = 'trip' "
                    + "AND l.date_del IS NULL"
            ).setParam(1, tripId), conn);
            rta.invs = GtCylInv.getInv(tripId, "c", conn);
            return createResponse(rta);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getVhTrips")
    public Response getVhTrips(@QueryParam("vhIdOrInternal") String vhIdOrInternal) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            int daysTripClosed = new MySQLQuery("SELECT days_trip_closed FROM inv_cfg").getAsInteger(conn);
            Object[][] trips = new MySQLQuery("SELECT "
                    + "t.auth_doc, "
                    + "tt.name, "
                    + "CONCAT(e.first_name, ' ', e.last_name), "
                    + "v.plate, "
                    + "v.internal, "
                    + "t.id, "
                    + "t.ddt, "
                    + "v.id, "
                    + "e.id "
                    + "FROM gt_cyl_trip t "
                    + "INNER JOIN gt_trip_type tt ON t.type_id = tt.id "
                    + "INNER JOIN vehicle v ON t.vh_id = v.id "
                    + "LEFT JOIN driver_vehicle dv ON t.vh_id = dv.vehicle_id AND dv.`end` IS NULL "
                    + "LEFT JOIN employee e ON dv.driver_id = e.id "
                    + "WHERE (v.plate like '" + vhIdOrInternal + "' OR v.internal like '" + vhIdOrInternal + "') "
                    + "AND DATE(DATE_ADD(t.ddt, INTERVAL " + daysTripClosed + " DAY)) >= CURDATE() "
                    + "AND t.req_steps = t.steps "
                    + "AND !t.cancel "
                    + "AND t.edt IS NOT NULL "
                    + "AND t.ddt IS NOT NULL "
                    + "ORDER BY t.ddt DESC").getRecords(conn);

            List<GtCylTripInfo> lst = new ArrayList<>();
            for (int i = 0; i < trips.length; i++) {
                lst.add(new GtCylTripInfo(trips[i]));
            }

            return createResponse(lst);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/registerUnload")
    public Response registerUnload(@QueryParam("nif") String nif, @QueryParam("tripId") int tripId, @QueryParam("centerId") int centerId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(registerUnload(nif, tripId, centerId, conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/deleteFromInitialClasify")
    public Response deleteFromInitialClasify(@QueryParam("cylId") int cylId, @QueryParam("tripId") int tripId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            new MySQLQuery("UPDATE trk_cyl_load l SET l.date_del = NOW() WHERE l.cyl_trip_id =  " + tripId + " AND l.cyl_id = " + cylId + " AND l.date_del IS NULL AND l.type = 'unload'").executeUpdate(conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/registerPreload")
    public Response registerPreload(@QueryParam("preLoadId") int preLoadId, @QueryParam("cylId") int cylId) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            if (new MySQLQuery("SELECT COUNT(*) > 0 FROM trk_cyl_load l WHERE l.pre_load_id = " + preLoadId + " AND l.cyl_id = " + cylId + " AND l.`type` = 'load' AND l.date_del IS NULL").getAsBoolean(conn)) {
                throw new ShortException("El cilindro ya fue registrado");
            }

            Object[] cylRow = new MySQLQuery("SELECT "
                    + "c.cyl_type_id, "
                    + "ct.name "
                    + "FROM trk_cyl c "
                    + "INNER JOIN cylinder_type ct ON c.cyl_type_id = ct.id "
                    + "WHERE c.id = " + cylId).getRecord(conn);
            int cylTypeId = MySQLQuery.getAsInteger(cylRow[0]);
            String cylName = MySQLQuery.getAsString(cylRow[1]);

            TrkCyl cyl = new TrkCyl().select(cylId, conn);
            boolean chkPlatfBeforeSale = new MySQLQuery("SELECT chk_platf_before_sale FROM com_cfg WHERE id = 1 ").getAsBoolean(conn);
            if (chkPlatfBeforeSale && !cyl.salable && cyl.respId == null) {
                throw new ShortException("El cilindro " + cyl.toString() + " fue vendido y no ha pasado por plataforma.");
            }

            Object[][] amountData = new MySQLQuery("SELECT "
                    + "gpli.capa_id, "
                    + "gpli.amount, "
                    + "(SELECT COUNT(*) "
                    + "	FROM trk_cyl_load tcl "
                    + "	INNER JOIN trk_cyl tc ON tcl.cyl_id = tc.id "
                    + "	WHERE tc.cyl_type_id = gpli.capa_id "
                    + "	AND tcl.pre_load_id = gpli.pre_load_id "
                    + " AND tcl.date_del IS NULL)"
                    + "FROM gt_pre_load_inv gpli "
                    + "WHERE gpli.pre_load_id = " + preLoadId).getRecords(conn);

            boolean inInventory = false;
            for (int i = 0; i < amountData.length; i++) {
                Object[] row = amountData[i];
                int invAmount = MySQLQuery.getAsInteger(row[1]);
                int scnAmount = MySQLQuery.getAsInteger(row[2]);
                if (cylTypeId == MySQLQuery.getAsInteger(row[0])) {
                    if (invAmount <= scnAmount) {
                        throw new Exception("Ha alcanzado el máximo de cilindros de " + cylName + ". El cilindro no se agregará a su carga");
                    }
                    inInventory = true;
                    break;
                }
            }

            if (!inInventory) {
                throw new Exception("No hay cilindros de " + cylName + " en el pre-cargue.");
            }

            if (new MySQLQuery("SELECT COUNT(*) > 0 "
                    + "FROM trk_cyl_load l "
                    + "INNER JOIN gt_cyl_trip t ON l.cyl_trip_id = t.id "
                    + "WHERE l.cyl_id = " + cylId + " "
                    + "AND l.date_del is null "
                    + "AND !t.cancel "
                    + "AND t.req_steps > t.steps").getAsBoolean(conn)) {
                throw new Exception("El cilindro reservado para otro viaje. No se agregará a éste inventario.");
            }

            if (new MySQLQuery("SELECT COUNT(*) > 0  "
                    + "FROM trk_cyl_load l "
                    + "WHERE l.cyl_id = " + cylId + " "
                    + "AND l.date_del IS NULL "
                    + "AND l.pre_load_id IS NOT NULL "
                    + "AND l.cyl_trip_id IS NULL").getAsBoolean(conn)) {
                throw new Exception("El cilindro está reservado para otro pre-cargue");
            }

            TrkCylLoad l = new TrkCylLoad();
            l.cylId = cylId;
            l.preLoadId = preLoadId;
            l.dateOut = new Date();
            l.type = "load";
            l.insert(conn);

            Integer driverId = new MySQLQuery("SELECT dv.driver_id "
                    + "FROM driver_vehicle dv "
                    + "INNER JOIN gt_pre_load p ON p.vh_id = dv.vehicle_id "
                    + "WHERE dv.end IS NULL AND p.id = " + preLoadId).getAsInteger(conn);
            new MySQLQuery("UPDATE trk_cyl SET resp_id = " + (driverId != null ? driverId : sl.employeeId) + ", salable = 0 WHERE id = " + cylId).executeUpdate(conn);

            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private TrkCyl registerDeparture(String nif, int tripId, boolean reserve, Integer invCenterId, boolean validateCyl, boolean mtoTrip, Connection conn) throws Exception {
        getSession(conn);
        try {
            conn.setAutoCommit(false);
            TrkCyl cyl = TrkCyl.selectByNif(nif, conn);
            if (cyl == null) {
                throw new Exception("El cilindro no está registrado");
            }
            Integer empId = null;
            boolean rep = new MySQLQuery("SELECT COUNT(*) > 0 FROM trk_cyl_load l WHERE l.cyl_trip_id = " + tripId + " AND l.cyl_id = " + cyl.id + " AND l.`type` = 'trip' AND l.date_del IS NULL").getAsBoolean(conn);

            if (rep) {
                throw new ShortException("El cilindro ya fue escaneado");
            } else {
                //cargue porterías

                if (validateCyl) {
                    empId = new MySQLQuery("SELECT "
                            + "COALESCE(t.driver_id, dv.driver_id) "
                            + "FROM gt_cyl_trip t "
                            + "LEFT JOIN driver_vehicle dv ON t.vh_id = dv.vehicle_id AND dv.`end` IS NULL "
                            + "WHERE t.id = " + tripId).getAsInteger(conn);
                    if (empId == null) {
                        throw new Exception("No fue posible conseguir el conductor para éste viaje");
                    }
                    CylValidations val = CylValidations.getValidations(nif, empId, false, CylValidations.SALES, invCenterId, conn);
                    if (val.cylError != null || val.saleError != null) {
                        throw new ShortException((val.cylError != null ? val.cylError : "") + (val.saleError != null ? val.saleError : ""));
                    }
                } else if (mtoTrip) {
                    if (new MySQLQuery("SELECT COUNT(*) > 0 FROM trk_cyl_wanted WHERE cyl_id = " + cyl.id + " AND aprov_dt IS NULL").getAsBoolean(conn)) {
                        new MySQLQuery("UPDATE trk_cyl_wanted SET find_dt = NOW(), find_id = " + empId + " WHERE cyl_id = " + cyl.id + " AND aprov_dt IS NULL").executeUpdate(conn);
                        throw new ShortException("Se ha ordenado la recolección del nif " + nif);
                    }
                }

                GtCylInv inv = GtCylInv.getInv(tripId, cyl.cylTypeId, "c", conn);
                int expected = inv != null ? inv.amount : 0;

                //consolidado del escaneo
                Integer amount = TrkCylLoad.getAmount(tripId, "trip", cyl.cylTypeId, conn);
                amount = amount != null ? amount : 0;

                if (amount >= expected) {
                    String typeName = new CylinderType().select(cyl.cylTypeId, conn).name;
                    throw new ShortException("El número de cilindros de " + typeName + "lb no corresponde al inventario de cargue.");
                }

                if (reserve) {
                    boolean chkPlatfBeforeSale = new MySQLQuery("SELECT chk_platf_before_sale FROM com_cfg WHERE id = 1 ").getAsBoolean(conn);
                    if (chkPlatfBeforeSale && !cyl.salable && cyl.respId == null) {
                        throw new ShortException("El cilindro " + nif + " fue vendido y no ha pasado por plataforma.");
                    }
                    cyl.salable = false;
                    cyl.respId = new GtCylTrip().select(tripId, conn).employeeId;
                    cyl.update(conn);
                }

                TrkCylLoad l = new TrkCylLoad();
                l.cylId = cyl.id;
                l.cylTripId = tripId;
                l.dateOut = new Date();
                l.type = "trip";
                l.insert(conn);

                if (validateCyl) {
                    new MySQLQuery("UPDATE trk_cyl SET inv_center_id = NULL, resp_id = " + empId + ", salable = 0 WHERE id = " + cyl.id).executeUpdate(conn);
                }
            }
            conn.commit();
            return cyl;
        } catch (Exception ex) {
            conn.rollback();
            throw ex;
        }
    }

    private CylValidations registerUnload(String nif, int tripId, Integer invCenterId, Connection conn) throws Exception {
        SessionLogin sl = getSession(conn);
        TrkCyl cyl = TrkCyl.selectByNif(nif, conn);
        if (cyl == null) {
            throw new Exception("El cilindro no está registrado");
        }
        boolean rep = new MySQLQuery("SELECT COUNT(*) > 0 FROM trk_cyl_load l WHERE l.cyl_trip_id = " + tripId + " AND l.cyl_id = " + cyl.id + " AND l.`type` = 'unload' AND l.date_del IS NULL").getAsBoolean(conn);

        if (rep) {
            throw new ShortException("El cilindro ya fue escaneado");
        }

        CylValidations val = CylValidations.getValidations(nif, sl.employeeId, false, CylValidations.TRACKING, invCenterId, conn);
        if (val.cylError != null) {
            throw new ShortException((val.cylError != null ? val.cylError : ""));
        }

        GtCylInv inv = GtCylInv.getInv(tripId, cyl.cylTypeId, "e", "v", conn);
        int expected = inv != null ? inv.amount : 0;

        //consolidado del escaneo
        Integer amount = TrkCylLoad.getAmount(tripId, "unload", cyl.cylTypeId, conn);
        amount = amount != null ? amount : 0;

        if (amount >= expected) {
            String typeName = new CylinderType().select(cyl.cylTypeId, conn).name;
            throw new ShortException("El número de cilindros de " + typeName + "lb no corresponde al inventario de entrada.");
        }

        TrkCylLoad l = new TrkCylLoad();
        l.cylId = cyl.id;
        l.cylTripId = tripId;
        l.dateOut = new Date();
        l.type = "unload";
        l.insert(conn);

        return val;
    }

    private void removeDeparture(int cylId, int tripId, boolean reserve, Integer invCenterId, Connection conn) throws Exception {
        if (new MySQLQuery("SELECT "
                + "IF(tt.s, t.sdt IS NOT NULL, FALSE) "
                + "FROM gt_cyl_trip t "
                + "INNER JOIN gt_trip_type tt ON t.type_id = tt.id "
                + "WHERE t.id = " + tripId).getAsBoolean(conn)) {
            throw new Exception("El viaje ya salió por portería. No es posible modificar el inventario");
        }

        if (invCenterId != null && new MySQLQuery("SELECT lock_cyls FROM inv_center WHERE id = " + invCenterId).getAsBoolean(conn)) {
            new MySQLQuery("UPDATE trk_cyl SET inv_center_id = " + invCenterId + " WHERE id = " + cylId).executeUpdate(conn);
        }

        new MySQLQuery("DELETE FROM trk_cyl_load WHERE cyl_trip_id = " + tripId + " AND cyl_id = " + cylId + " AND `type` = 'trip'").executeDelete(conn);
        if (reserve) {
            TrkCyl cyl = new TrkCyl().select(cylId, conn);
            cyl.salable = true;
            cyl.respId = null;
            cyl.update(conn);
        }
    }

}
