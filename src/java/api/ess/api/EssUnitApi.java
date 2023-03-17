package api.ess.api;

import api.BaseAPI;
import api.GridResult;
import api.MultiPartRequest;
import api.MySQLCol;
import api.ess.model.EssUnit;
import api.sys.dto.Notify;
import api.sys.model.Bfile;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import metadata.model.GridRequest;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import utilities.shrinkfiles.FileShrinker;
import web.fileManager;

@Path("/essUnit")
public class EssUnitApi extends BaseAPI {

    @POST
    public Response insert(EssUnit obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.cache = "temp";
            obj.insert(conn);
            EssUnit.updateCache(obj.id, conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(EssUnit obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            EssUnit old = new EssUnit().select(obj.id, conn);
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
            EssUnit obj = new EssUnit().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            EssUnit.delete(id, conn);
            SysCrudLog.deleted(this, EssUnit.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getAll")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(EssUnit.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/notifyMultipart")
    public Response notifyMultipart(@Context HttpServletRequest request) throws Exception {
        try (Connection conn = getConnection()) {
            SessionLogin sess = getSession(conn);

            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);
            Map<String, String> pars = mr.params;
            Integer bfileId = null;
            Integer buildId = MySQLQuery.getAsInteger(pars.get("buildId"));
            Integer unitId = MySQLQuery.getAsInteger(pars.get("unitId"));
            String title = MySQLQuery.getAsString(pars.get("title"));
            String msg = MySQLQuery.getAsString(pars.get("msg"));

            if (buildId == null && unitId == null) {
                throw new Exception("Seleccione un Edificio ó Unidad");
            }

            if (mr.getFile() != null) {
                if (!mr.getFile().isImage()){                   
                    throw new Exception("Archivo de imagen no soportado");
                }
                Bfile upload = fileManager.upload(sess.employeeId, sess.employeeId, 139, mr.getFile().fileName, "imagen de notificacion", null, FileShrinker.TYPE_JPG_COLOR, pi, mr.getFile().file, conn);
                bfileId = upload.id;
            }

            String imageUrl = request.getScheme() + "://" + request.getRemoteAddr() + ":"
                    + request.getLocalPort() + request.getContextPath() + "/api/bfile/resource?id=" + bfileId;
            sendNotify(buildId, unitId, title, msg, imageUrl, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/notifyParams")
    public Response notifyParams(@QueryParam("buildId") Integer buildId, @QueryParam("unitId") Integer unitId, @QueryParam("title") String title,
            @QueryParam("msg") String msg, @QueryParam("imageUrl") String imageUrl) throws Exception {
        try (Connection conn = getConnection()) {
            SessionLogin sess = getSession(conn);
            if (buildId == null && unitId == null) {
                throw new Exception("Seleccione un Edificio ó Unidad");
            }
            sendNotify(buildId, unitId, title, msg, imageUrl, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private void sendNotify(Integer buildId, Integer unitId, String title, String msg, String imageUrl, Connection conn) throws Exception {

        String empIds = new MySQLQuery("SELECT GROUP_CONCAT(DISTINCT p.emp_id) "
                + " FROM ess_person p "
                + "INNER JOIN ess_person_unit pu on  pu.person_id = p.id "
                + "INNER JOIN ess_unit u on u.id = pu.unit_id "
                + "WHERE p.active "
                + (buildId != null ? " AND u.build_id = " + buildId : " AND u.id = " + unitId)).getAsString(conn);

        if (MySQLQuery.isEmpty(empIds)) {
            throw new Exception("No se encontró destinatarios en " + (buildId != null ? "este edificio" : "esta unidad"));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"notes\":\"").append(msg).append("\"");
        sb.append("}");
        String total = sb.toString();

        Notify n = new Notify();
        n.empIds = empIds;
        n.title = title;
        n.message = total;
        if (!MySQLQuery.isEmpty(imageUrl)) {
            n.imgUrl = imageUrl;
        }
        Notify.sendNotification(conn, n);
    }

    @POST
    @Path("/eventGrid")
    public Response eventGrid(GridRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult r = new GridResult();
            r.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_INTEGER, 50, "Torre"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 50, "Apartamento"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 150, "Habitantes")
            };

            if (req.ints.isEmpty()) {
                r.data = new Object[0][0];

            } else {
                int buildId = req.ints.get(0);
                String query = !req.strings.isEmpty() ? req.strings.get(0) : "";

                String[] parts = query.split(" ");
                String q = "SELECT u.id, u.tower, u.code, GROUP_CONCAT(CONCAT(p.first_name, ' ', p.last_name) SEPARATOR ', ') "
                        + "FROM ess_unit u "
                        + "INNER JOIN ess_person_unit pu ON pu.unit_id = u.id "
                        + "INNER JOIN ess_person p ON p.id = pu.person_id "
                        + "WHERE u.active AND u.build_id = " + buildId + " ";
                for (String part : parts) {
                    q += " AND u.cache LIKE '%" + part + "%' ";
                }
                q += " GROUP BY u.id ";
                r.data = new MySQLQuery(q).getRecords(conn);
                r.sortType = GridResult.SORT_ASC;
                r.sortColIndex = 0;
            }
            return createResponse(r);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
