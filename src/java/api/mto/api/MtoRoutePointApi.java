package api.mto.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.mto.model.MtoRoutePoint;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;

@Path("/mtoRoutePoint")
public class MtoRoutePointApi extends BaseAPI {

    @POST
    public Response insert(MtoRoutePoint obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            Object[][] counts = new MySQLQuery(""
                    + "SELECT 'going_start', COUNT(*) FROM mto_route_point p WHERE p.`type` = 'going_start' AND p.route_id = ?1 UNION  "
                    + "SELECT 'comming_start', COUNT(*) FROM mto_route_point p WHERE p.`type`  = 'comming_start' AND p.route_id = ?1 UNION "
                    + "SELECT 'end', COUNT(*) FROM mto_route_point p WHERE p.`type`  = 'end' AND p.route_id = ?1 ").setParam(1, obj.routeId).getRecords(conn);

            Map<String, Integer> map = new HashMap<>();
            if (counts != null && counts.length > 0) {
                for (Object[] count : counts) {
                    map.put(MySQLQuery.getAsString(count[0]), MySQLQuery.getAsInteger(count[1]));
                }
            }

            if (map.get("going_start") == 0 && !obj.type.equals("going_start")) {
                throw new Exception("primero debe establecer el punto de inicio");
            }

            validateTime(obj);

            Integer place;
            switch (obj.type) {
                case "going_start":
                    if (map.get("going_start") == 1) {
                        throw new Exception("La ruta ya tiene un inicio definido");
                    }
                    obj.place = 0;
                    break;
                case "going":
                    place = new MySQLQuery("SELECT MAX(r.place) FROM mto_route_point r WHERE r.route_id = ?1 "
                            + "AND r.type IN('going_start','going') ").setParam(1, obj.routeId).getAsInteger(conn);
                    new MySQLQuery("UPDATE mto_route_point SET place = (place + 1) WHERE place > " + place + "").executeUpdate(conn);
                    obj.place = place + 1;
                    break;
                case "comming_start":
                    if (map.get("comming_start") == 1) {
                        throw new Exception("La ruta ya tiene un retorno definido");
                    }
                    place = new MySQLQuery("SELECT MAX(r.place) FROM mto_route_point r WHERE r.route_id = ?1 "
                            + "AND r.type IN('going_start','going') ").setParam(1, obj.routeId).getAsInteger(conn);
                    new MySQLQuery("UPDATE mto_route_point SET place = (place + 1) WHERE place > " + place + "").executeUpdate(conn);
                    obj.place = place + 1;
                    break;
                case "comming":
                    place = new MySQLQuery("SELECT MAX(r.place) FROM mto_route_point r WHERE r.route_id = ?1 "
                            + "AND r.type IN('going_start','going','comming_start','comming') ").setParam(1, obj.routeId).getAsInteger(conn);
                    new MySQLQuery("UPDATE mto_route_point SET place = (place + 1) WHERE place > " + place + "").executeUpdate(conn);
                    obj.place = place + 1;
                    break;
                case "end":
                    if (map.get("end") == 1) {
                        throw new Exception("La ruta ya tiene un fin definido");
                    }
                    place = new MySQLQuery("SELECT MAX(r.place) FROM mto_route_point r WHERE r.route_id = ?1 "
                            + "AND r.type IN('going_start','going','comming_start','comming') ").setParam(1, obj.routeId).getAsInteger(conn);
                    new MySQLQuery("UPDATE mto_route_point SET place = (place + 1) WHERE place > " + place + "").executeUpdate(conn);
                    obj.place = place + 1;
                    break;
                default:
                    break;
            }

            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(MtoRoutePoint obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            validateTime(obj);
            MtoRoutePoint old = new MtoRoutePoint().select(obj.id, conn);
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MtoRoutePoint obj = new MtoRoutePoint().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("byRoute")
    public Response getByRoute(@QueryParam("routeId") int routeId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            List<MtoRoutePoint> lst = MtoRoutePoint.getList(new MySQLQuery("SELECT " + MtoRoutePoint.getSelFlds("") + " FROM mto_route_point WHERE route_id = ?1").setParam(1, routeId), conn);
            return Response.ok(lst).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MtoRoutePoint obj = new MtoRoutePoint().select(id, conn);
            new MySQLQuery("UPDATE mto_route_point SET place = (place - 1) WHERE place > " + obj.place + "").executeUpdate(conn);
            MtoRoutePoint.delete(id, conn);
            SysCrudLog.deleted(this, MtoRoutePoint.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/all")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(MtoRoutePoint.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/grid")
    public Response getGrid(@QueryParam("routeId") int routeId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult gr = new GridResult();
            gr.data = new MySQLQuery(""
                    + " SELECT id, place, type, name, type, "
                    + " IF(type = 'going_start','',CONCAT(IFNULL(LPAD(h_full, 2, 0),'00'),':',IFNULL(LPAD(m_full, 2, 0),'00'))), "
                    + " IF(h_part IS NOT NULL OR m_part IS NOT NULL, CONCAT(IFNULL(LPAD(h_part, 2, 0),'00'),':',IFNULL(LPAD(m_part, 2, 0),'00')),''), "
                    + " IF(lat IS NOT NULL AND lon IS NOT NULL, 1,0)"
                    + " FROM mto_route_point "
                    + " WHERE route_id = ?1 "
                    + " ORDER BY place asc "
            ).setParam(1, routeId).getRecords(conn);

            gr.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 50, "Nombre"),
                new MySQLCol(MySQLCol.TYPE_ENUM, 30, "Tipo", MySQLQuery.getEnumOptionsAsMatrix(MtoRoutePoint.getEnumOptions("type"))),
                new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Horas"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Cumplido"),
                new MySQLCol(MySQLCol.TYPE_BOOLEAN, 15, "Coord")
            };

            gr.sortType = GridResult.SORT_NONE;

            return createResponse(gr);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/movePoint")
    public Response movePoint(@QueryParam("currPlace") int currPlace, @QueryParam("newPlace") int newPlace,
            @QueryParam("newId") int newId, @QueryParam("currId") int currId,
            @QueryParam("currType") String currType, @QueryParam("newType") String newType) {
        try (Connection conn = getConnection()) {

            if (!currType.equals(newType)) {
                throw new Exception("No se puede mover este punto");
            }

            new MySQLQuery("UPDATE mto_route_point SET place = " + newPlace + " WHERE id = " + currId + " ").executeUpdate(conn);
            new MySQLQuery("UPDATE mto_route_point SET place = " + currPlace + " WHERE id = " + newId + " ").executeUpdate(conn);
            return Response.ok().build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public void validateTime(MtoRoutePoint obj) throws Exception {
        if (!obj.type.equals("going_start")) {
            if ((obj.hFull == null && obj.mFull == null)) {
                throw new Exception("Debe especificar el tiempo de guia para el punto actual");
            }
            if (obj.hPart != null || obj.mPart != null) {
                obj.hPart = (obj.hPart == null ? 0 : obj.hPart);
                obj.mPart = (obj.mPart == null ? 0 : obj.mPart);
            }
            if (obj.hFull != null || obj.mFull != null) {
                obj.hFull = (obj.hFull == null ? 0 : obj.hFull);
                obj.mFull = (obj.mFull == null ? 0 : obj.mFull);
            }
        }
    }
}
