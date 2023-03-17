package api.gt.api;

import api.BaseAPI;
import api.gt.model.GtPreLoad;
import api.gt.model.GtPreLoadInv;
import api.trk.model.TrkCyl;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import web.ShortException;

@Path("/gtPreLoad")
public class GtPreLoadApi extends BaseAPI {

    @POST
    public Response insert(GtPreLoad obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            insert(obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(GtPreLoad obj) {
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
            GtPreLoad obj = new GtPreLoad().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GtPreLoad.delete(id, conn);
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
            return createResponse(GtPreLoad.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getByCenter")
    public Response getByCenter(@QueryParam("centerId") int centerId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            List<GtPreLoad> list = new ArrayList<>();

            Object[][] preLoadData = new MySQLQuery("SELECT " + GtPreLoad.getSelFlds("p") + ", CONCAT(v.plate, ' ', v.internal) FROM gt_pre_load p LEFT JOIN vehicle v ON p.vh_id = v.id WHERE available AND p.center_id = " + centerId).getRecords(conn);
            for (int i = 0; i < preLoadData.length; i++) {
                list.add(new GtPreLoad(preLoadData[i]));
            }

            for (int i = 0; i < list.size(); i++) {
                int preLoadId = list.get(i).id;
                Object[][] invData = new MySQLQuery("SELECT " + GtPreLoadInv.getSelFlds("i") + ", "
                        + "t.name, "
                        + "(SELECT COUNT(*) FROM trk_cyl_load l INNER JOIN trk_cyl c ON l.cyl_id = c.id WHERE l.pre_load_id = " + preLoadId + " AND l.date_del IS NULL AND c.cyl_type_id = t.id) "
                        + "FROM gt_pre_load_inv i "
                        + "INNER JOIN cylinder_type t ON i.capa_id = t.id "
                        + "WHERE i.pre_load_id = " + preLoadId).getRecords(conn);
                List<GtPreLoadInv> lst = new ArrayList<>();
                for (int j = 0; j < invData.length; j++) {
                    lst.add(new GtPreLoadInv(invData[j]));
                }
                list.get(i).load = lst;
            }

            return createResponse(list);
        } catch (Exception e) {
            return createResponse(e);
        }
    }

    @GET
    @Path("/assignToTrip")
    public Response assignToTrip(@QueryParam("tripId") int tripId, @QueryParam("preLoadId") int preLoadId) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            Object[][] invData = new MySQLQuery("SELECT " + GtPreLoadInv.getSelFlds("i") + ", "
                    + "t.name, "
                    + "(SELECT COUNT(*) FROM trk_cyl_load l INNER JOIN trk_cyl c ON l.cyl_id = c.id WHERE l.pre_load_id = " + preLoadId + " AND l.date_del IS NULL AND c.cyl_type_id = t.id) "
                    + "FROM gt_pre_load_inv i "
                    + "INNER JOIN cylinder_type t ON i.capa_id = t.id "
                    + "WHERE i.pre_load_id = " + preLoadId).getRecords(conn);

            List<GtPreLoadInv> lstPreLoadInv = new ArrayList<>();
            for (int j = 0; j < invData.length; j++) {
                lstPreLoadInv.add(new GtPreLoadInv(invData[j]));
            }

            Object[][] tripInv = new MySQLQuery("SELECT "
                    + "i.capa_id, "//0
                    + "i.amount "//1
                    + "FROM gt_cyl_inv i "
                    + "WHERE i.trip_id = " + tripId + " "
                    + "AND i.type_id = 2 "
                    + "AND i.state = 'l' "
                    + "AND i.`type` = 'c' ").getRecords(conn);

            for (int i = 0; i < lstPreLoadInv.size(); i++) {
                GtPreLoadInv get = lstPreLoadInv.get(i);
                boolean invOk = false;
                for (int j = 0; j < tripInv.length; j++) {
                    Object[] row = tripInv[j];
                    if (MySQLQuery.getAsInteger(row[0]) == get.capaId) {
                        if (MySQLQuery.getAsInteger(row[1]) >= get.amount) {
                            invOk = true;
                            break;
                        }
                    }
                }

                if (!invOk) {
                    throw new Exception("El pre-cargue no coincide con el inventario de su viaje");
                }
            }

            Object[][] preLoadScnCyls = new MySQLQuery("SELECT "
                    + "c.cyl_type_id, "
                    + "COUNT(*) "
                    + "FROM trk_cyl_load l "
                    + "INNER JOIN trk_cyl c ON l.cyl_id = c.id "
                    + "WHERE l.pre_load_id = " + preLoadId + " "
                    + "AND l.date_del IS NULL "
                    + "GROUP BY c.cyl_type_id").getRecords(conn);

            Object[][] tripScnCyls = new MySQLQuery("SELECT "
                    + "c.cyl_type_id, "
                    + "COUNT(*) "
                    + "FROM trk_cyl_load l "
                    + "INNER JOIN trk_cyl c ON l.cyl_id = c.id "
                    + "WHERE l.cyl_trip_id = " + tripId + " "
                    + "AND l.date_del IS NULL "
                    + "GROUP BY c.cyl_type_id").getRecords(conn);

            for (int j = 0; j < preLoadScnCyls.length; j++) {
                Object[] preLoadCyl = preLoadScnCyls[j];

                int invAmount = 0;
                for (int i = 0; i < tripInv.length; i++) {
                    Object[] tInv = tripInv[i];
                    if (tInv[0].equals(preLoadCyl[0])) {
                        invAmount = MySQLQuery.getAsInteger(tInv[1]);
                        break;
                    }
                }

                int sumAmount = 0;
                for (int i = 0; i < tripScnCyls.length; i++) {
                    Object[] tripCyl = tripScnCyls[i];
                    if (tripCyl[0].equals(preLoadCyl[0])) {
                        sumAmount = MySQLQuery.getAsInteger(tripCyl[1]) + MySQLQuery.getAsInteger(preLoadCyl[1]);

                    }
                }

                if (invAmount < sumAmount) {
                    String cylName = new MySQLQuery("SELECT name FROM cylinder_type WHERE id = " + preLoadCyl[0]).getAsString(conn);
                    throw new Exception("El número de cilindros de " + cylName + " escaneados excede el inventario.");
                }
            }

            new MySQLQuery("UPDATE trk_cyl_load SET cyl_trip_id = " + tripId + " WHERE pre_load_id = " + preLoadId).executeUpdate(conn);
            new MySQLQuery("UPDATE gt_pre_load SET available = 0 WHERE id = " + preLoadId).executeUpdate(conn);

            return createResponse();
        } catch (Exception e) {
            return createResponse(e);
        }
    }

    @GET
    @Path("/getByEmp")
    public Response getByEmp(@QueryParam("empId") int empId, @QueryParam("tripId") int tripId) {
        try (Connection conn = getConnection()) {

            getSession(conn);
            if (new MySQLQuery("SELECT type_id <> 141 FROM gt_cyl_trip WHERE id = " + tripId).getAsBoolean(conn)) {
                throw new Exception("Opción habilitada solo para viajes de ventas.");
            }

            List<GtPreLoad> list = new ArrayList<>();

            Object[][] preLoadData = new MySQLQuery("SELECT " + GtPreLoad.getSelFlds("p") + ", "
                    + "CONCAT(v.plate, ' ', v.internal) "
                    + "FROM gt_pre_load p "
                    + "INNER JOIN vehicle v ON p.vh_id = v.id "
                    + "INNER JOIN driver_vehicle dv ON dv.vehicle_id = v.id "
                    + "WHERE available "
                    + "AND dv.end IS NULL "
                    + "AND dv.driver_id = " + empId).getRecords(conn);

            for (int i = 0; i < preLoadData.length; i++) {
                list.add(new GtPreLoad(preLoadData[i]));
            }

            for (int i = 0; i < list.size(); i++) {
                int preLoadId = list.get(i).id;
                Object[][] invData = new MySQLQuery("SELECT " + GtPreLoadInv.getSelFlds("i") + ", "
                        + "t.name, "
                        + "(SELECT COUNT(*) FROM trk_cyl_load l INNER JOIN trk_cyl c ON l.cyl_id = c.id WHERE l.pre_load_id = " + preLoadId + " AND l.date_del IS NULL AND c.cyl_type_id = t.id) "
                        + "FROM gt_pre_load_inv i "
                        + "INNER JOIN cylinder_type t ON i.capa_id = t.id "
                        + "WHERE i.pre_load_id = " + preLoadId).getRecords(conn);
                List<GtPreLoadInv> lst = new ArrayList<>();
                for (int j = 0; j < invData.length; j++) {
                    lst.add(new GtPreLoadInv(invData[j]));
                }
                list.get(i).load = lst;
            }

            return createResponse(list);
        } catch (Exception e) {
            return createResponse(e);
        }
    }

    /**
     * Llamar desde el insert del api
     *
     * @param obj
     * @param conn
     * @return
     * @throws Exception
     */
    private GtPreLoad insert(GtPreLoad obj, Connection conn) throws Exception {
        return insert(obj, true, conn);
    }

    private GtPreLoad insert(GtPreLoad obj, boolean validateUnique, Connection conn) throws Exception {
        if (validateUnique && new MySQLQuery("SELECT COUNT(*) > 0 FROM gt_pre_load WHERE available AND vh_id = " + obj.vhId).getAsBoolean(conn)) {
            throw new Exception("Ya existe un pre-cargue disponible para el vehículo " + obj.vehicle);
        }
        obj.available = true;
        obj.dt = new Date();
        obj.id = obj.insert(conn);

        List<GtPreLoadInv> load = obj.load;
        for (int i = 0; i < load.size(); i++) {
            GtPreLoadInv item = load.get(i);
            item.preLoadId = obj.id;
            item.insert(conn);
        }

        return obj;
    }

    @GET
    @Path("/cancelPreload")
    public Response cancelPreload(@QueryParam("preLoadId") int preLoadId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            new MySQLQuery("UPDATE trk_cyl_load tcl "
                    + "INNER JOIN trk_cyl tc ON tcl.cyl_id = tc.id "
                    + "SET tcl.date_del = NOW(), "
                    + "tc.resp_id = NULL, "
                    + "tc.salable = 1 "
                    + "WHERE tcl.pre_load_id = " + preLoadId + " "
                    + "AND tcl.date_del IS NULL").executeUpdate(conn);
            new MySQLQuery("UPDATE gt_pre_load SET available = 0 WHERE id = " + preLoadId).executeUpdate(conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getScanned")
    public Response getScanned(@QueryParam("preLoadId") int preLoadId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(TrkCyl.getList(new MySQLQuery("SELECT " + TrkCyl.getSelFlds("tc") + " FROM trk_cyl tc INNER JOIN trk_cyl_load l ON l.cyl_id = tc.id WHERE l.pre_load_id = " + preLoadId + " AND l.date_del IS NULL"), conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/deleteFromLoad")
    public Response deleteFromLoad(@QueryParam("cylId") int cylId, @QueryParam("preLoadId") int preLoadId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            new MySQLQuery("UPDATE trk_cyl_load SET date_del = NOW() WHERE pre_load_id = " + preLoadId + " AND cyl_id = " + cylId).executeUpdate(conn);
            new MySQLQuery("UPDATE trk_cyl SET resp_id = NULL, salable = 1 WHERE id = " + cylId).executeUpdate(conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/assignPreload")
    public Response assignPreLoad(@QueryParam("preLoadId") int preLoadId, @QueryParam("cylTripId") int cylTripId, @QueryParam("empId") int empId) {
        try (Connection conn = getConnection()) {
            SessionLogin sess = getSession(conn);

            if (empId == 0) {
                throw new Exception("No se puede asignar el pre-cargue. Hay un problema con el responsable. Comuníquese con Sistemas");
            }

            Integer tripType = new MySQLQuery("SELECT type_id FROM gt_cyl_trip WHERE id = " + cylTripId).getAsInteger(conn);
            new MySQLQuery("UPDATE gt_pre_load SET available = 0 WHERE id = " + preLoadId).executeUpdate(conn);
            new MySQLQuery("UPDATE trk_cyl_load SET cyl_trip_id = " + cylTripId + (tripType != 141 ? ", type = 'trip' " : "") + " WHERE pre_load_id = " + preLoadId).executeUpdate(conn);//cambia el tipo para viajes que no son de ventas
            new MySQLQuery("UPDATE trk_cyl c INNER JOIN trk_cyl_load l ON l.cyl_id = c.id  SET c.resp_id = " + empId + ", c.salable = 0 WHERE l.pre_load_id = " + preLoadId + " AND l.date_del IS NULL").executeUpdate(conn);

            //provisional, remover cuando se sepa por qué luego de dejar un cilindro en un expendio correctamtne, luego aparece a nombre del trasportador
            new MySQLQuery("INSERT INTO trk_cyl_nov "
                    + "SELECT NULL, c.id, NOW(), " + sess.employeeId + ", 'Cambio de responsable por un precargue preLoad: " + preLoadId + " cylTrip: " + cylTripId + " ', 'other' FROM trk_cyl c INNER JOIN trk_cyl_load l ON l.cyl_id = c.id WHERE l.pre_load_id = " + preLoadId + " AND l.date_del IS NULL").executeUpdate(conn);

            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/validateTripAndPreload")
    public Response validateTripAndPreload(@QueryParam("preLoadId") int preLoadId, @QueryParam("tripId") int tripId) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            Object[][] preLoadInv = new MySQLQuery("SELECT i.capa_id, i.amount, t.name FROM gt_pre_load_inv i INNER JOIN cylinder_type t ON i.capa_id = t.id WHERE i.pre_load_id = " + preLoadId).getRecords(conn);
            Object[][] tripInv = new MySQLQuery("SELECT i.capa_id, i.amount, t.name FROM gt_cyl_inv i INNER JOIN cylinder_type t ON i.capa_id = t.id WHERE i.state = 'l' AND i.type = 'c' AND i.trip_id = " + tripId).getRecords(conn);

            String type = null;
            for (Object[] pli : preLoadInv) {
                type = MySQLQuery.getAsString(pli[2]);
                for (Object[] ti : tripInv) {
                    if (ti[0].equals(pli[0])) {
                        type = null;
                        break;
                    }
                }
                if (type != null) {
                    throw new Exception("El cargue no tiene cilindros de tipo " + type);
                }
            }

            type = null;
            for (int j = 0; j < tripInv.length; j++) {
                Object[] ti = tripInv[j];
                for (int i = 0; i < preLoadInv.length; i++) {
                    Object[] pli = preLoadInv[i];
                    if (ti[0].equals(pli[0]) && MySQLQuery.getAsInteger(ti[1]) < MySQLQuery.getAsInteger(pli[1])) {
                        type = MySQLQuery.getAsString(pli[2]);
                        break;
                    }
                }

                if (type != null) {
                    throw new Exception("No coinciden las cantidades para el tipo " + type);
                }
            }

            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/createFromFull")
    public Response createFromFull(GtPreLoad obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            if (new MySQLQuery("SELECT COUNT(*) = 0 FROM trk_cyl_load l WHERE (l.type = 'trip' OR l.type = 'load') AND l.date_del IS NULL AND cyl_trip_id = " + obj.tripId).getAsBoolean(conn)) {
                throw new ShortException("No se creará previaje con cilindros llenos. Vendedor sin autoescaneo");
            }

            Integer preLoadId = new MySQLQuery("SELECT id FROM gt_pre_load WHERE available AND vh_id = " + obj.vhId).getAsInteger(conn);

            if (preLoadId == null) {
                GtPreLoad insert = insert(obj, false, conn);
                preLoadId = insert.id;
            }

            Object[][] cylIds = new MySQLQuery("SELECT "
                    + "ld.cyl_id "
                    + "FROM trk_cyl_load ld "
                    + "INNER JOIN trk_cyl c ON ld.cyl_id = c.id "
                    + "INNER JOIN gt_cyl_trip t ON ld.cyl_trip_id = t.id "
                    + "WHERE "
                    + "ld.cyl_trip_id = " + obj.tripId + " "
                    + "AND ld.date_del IS NULL "
                    + "AND c.resp_id = t.driver_id "
                    + "AND !c.salable "
                    + "AND ld.type <> 'unload' "
                    + "AND (SELECT COUNT(*) = 0 FROM trk_cyl_load unld WHERE unld.pre_load_id = " + preLoadId + " AND unld.cyl_id = c.id AND unld.date_del IS NULL)").getRecords(conn);

            for (int i = 0; i < cylIds.length; i++) {
                Object[] cylId = cylIds[i];
                new MySQLQuery("INSERT INTO trk_cyl_load SET "
                        + "cyl_id = " + MySQLQuery.getAsInteger(cylId[0]) + ", "
                        + "pre_load_id = " + preLoadId + ", "
                        + "type = 'load', "
                        + "date_out = NOW()").executeInsert(conn);
            }

            return createResponse();
        } catch (Exception e) {
            return createResponse(e);
        }
    }
}
