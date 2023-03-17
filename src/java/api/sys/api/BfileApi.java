package api.sys.api;

import api.BaseAPI;
import api.GridResult;
import api.MultiPartRequest;
import api.MySQLCol;
import api.Params;
import api.sys.model.Bfile;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.sql.Connection;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import metadata.model.GridRequest;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import utilities.ServerNow;
import web.fileManager;

@Path("/bfile")
public class BfileApi extends BaseAPI {

    @GET
    @Path("/getEnterpriseLogo/{ownerId}")
    public Response getEnterpriseLogo(@PathParam("ownerId") int ownerId) {
        try (Connection con = getConnection()) {
            return getImageFile(con, null, ownerId, 29);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getEntLogoByPoolName/")
    public Response getEntLogoByPoolName(@QueryParam("poolName") String poolName, @QueryParam("ownerId") int ownerId) {
        try (Connection con = MySQLCommon.getConnection(poolName, null);) {
            return getImageFile(con, null, ownerId, 29);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private Response getImageFile(Connection con, Integer bfileId, Integer ownerId, Integer ownerType) throws Exception {
        Object[] data;
        if (bfileId != null) {
            data = new MySQLQuery("SELECT id, file_name FROM bfile WHERE id = " + bfileId).getRecord(con);
        } else {
            data = new MySQLQuery("SELECT id, file_name FROM bfile "
                    + "WHERE owner_type = " + ownerType + " AND owner_id = " + ownerId).getRecord(con);
        }

        if (data != null && data.length > 0) {
            File file = new fileManager.PathInfo(con).getExistingFile(MySQLQuery.getAsInteger(data[0]));
            return createResponse(file, MySQLQuery.getAsString(data[1]));
        } else {
            ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 1, 0, 0, 0, 1, 1, 3,
                0, 0, 0, 37, -37, 86, -54, 0, 0, 0, 3, 80, 76, 84, 69, 0, 0, 0, -89, 122, 61, -38, 0, 0, 0, 1, 116, 82, 78, 83, 0, 64, -26, -40, 102, 0, 0,
                0, 10, 73, 68, 65, 84, 8, -41, 99, 96, 0, 0, 0, 2, 0, 1, -30, 33, -68, 51, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126});

            return Response.ok(bais, MediaType.APPLICATION_OCTET_STREAM)
                    .header("content-disposition", "attachment; filename = empty.png")
                    .build();
        }
    }

    @POST
    @Path("/upload")
    public Response upload(@Context HttpServletRequest request) throws Exception {
        try (Connection conn = getConnection()) {
            SessionLogin sess = getSession(conn);
            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);
            Map<String, String> pars = mr.params;
            int ownerId = MySQLQuery.getAsInteger(pars.get("ownerId"));
            Integer ownerType = MySQLQuery.getAsInteger(pars.containsKey("ownerType") ? pars.get("ownerType") : null);
            String tableName = MySQLQuery.getAsString(pars.containsKey("table") ? pars.get("table") : null);
            String desc = MySQLQuery.getAsString(pars.containsKey("description") ? pars.get("description") : null);
            Integer shrinkType = pars.containsKey("shrinkType") ? MySQLQuery.getAsInteger(pars.get("shrinkType")) : null;
            Boolean unique = (pars.containsKey("unique") ? MySQLQuery.getAsBoolean(pars.get("unique")) : null);
            Bfile bfile = fileManager.upload(sess.employeeId, ownerId, ownerType, tableName, mr.getFile().fileName, desc, unique, shrinkType, pi, mr.getFile().file, conn);
            return createResponse(bfile);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/appUploadEvidence")
    public Response uploadEvidence(@Context HttpServletRequest request) throws Exception {
        try (Connection conn = getConnection()) {
            SessionLogin sess = getSession(conn);
            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);
            Map<String, String> pars = mr.params;
            int ownerId = MySQLQuery.getAsInteger(pars.get("ownerId"));
            Integer ownerType = MySQLQuery.getAsInteger(pars.containsKey("ownerType") ? pars.get("ownerType") : null);
            String tableName = MySQLQuery.getAsString(pars.containsKey("table") ? pars.get("table") : null);
            String desc = MySQLQuery.getAsString(pars.containsKey("description") ? pars.get("description") : null);
            desc = MySQLQuery.isEmpty(desc) ? null : desc;
            Integer shrinkType = pars.containsKey("shrinkType") ? MySQLQuery.getAsInteger(pars.get("shrinkType")) : null;
            Boolean unique = (pars.containsKey("unique") ? MySQLQuery.getAsBoolean(pars.get("unique")) : null);
            Integer sigmaId = MySQLQuery.getAsInteger(pars.containsKey("sigmaId") ? pars.get("sigmaId") : -1);
            Bfile bfile;
            if (sigmaId > 0) {
                bfile = new Bfile().select(new Params("id", sigmaId), conn);
                bfile.description = (desc == null ? "evidencia de turno" : desc);
                bfile.updatedBy = sess.employeeId;
                bfile.updated = new ServerNow();
                bfile.update(conn);
            } else {
                bfile = fileManager.upload(sess.employeeId, ownerId, ownerType, tableName, mr.getFile().fileName, desc, unique, shrinkType, pi, mr.getFile().file, conn);
            }
            return createResponse(bfile);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    //TODO, validar que solo sea con imágenes? mejorar la seguridad
    @GET
    @Path("/resource")
    public Response resource(@QueryParam("id") int id) throws Exception {
        try (Connection conn = getConnection()) {
            //getSession(conn);
            return getImageFile(conn, id, null, null);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    //-------------------------------------------------------------------
    @GET
    @Path("/download")
    public Response resource(@QueryParam("tableName") String tableName, @QueryParam("id") int ownerId) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);

            Object[] data = new MySQLQuery("SELECT b.id, b.file_name FROM bfile b "
                    + "WHERE b.table = '" + tableName + "' AND b.owner_id = " + ownerId).getRecord(conn);

            if (data != null && data.length > 0) {
                File file = new fileManager.PathInfo(conn).getExistingFile(MySQLQuery.getAsInteger(data[0]));
                return createResponse(file, MySQLQuery.getAsString(data[1]));
            } else {
                throw new Exception("No se encontró el archivo");
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/downloadById")
    public Response downloadById(@QueryParam("id") int id) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Object[] data = new MySQLQuery("SELECT b.id, b.file_name FROM bfile b WHERE b.id = " + id).getRecord(conn);
            if (data != null && data.length > 0) {
                File file = new fileManager.PathInfo(conn).getExistingFile(MySQLQuery.getAsInteger(data[0]));
                return createResponse(file, MySQLQuery.getAsString(data[1]));
            } else {
                throw new Exception("No se encontró el archivo");
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);

            Bfile bfile = new Bfile().select(id, conn);
            if (bfile != null) {
                Bfile.delete(bfile.id, conn);
                File file = new fileManager.PathInfo(conn).getExistingFile(bfile.id);
                if (file.exists()) {
                    file.delete();
                }
            } else {
                throw new Exception("No se encontró el archivo");
            }
            return Response.ok().build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/attachments")
    public Response closed(GridRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult r = new GridResult();
            r.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_SS_A, 30, "Creado"),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_SS_A, 30, "Actualizado"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 40, "Descripción")
            };

            MySQLQuery q = new MySQLQuery(
                    "SELECT "
                    + "b.id, "
                    + "b.created, "
                    + "b.updated, "
                    + "b.description "
                    + "FROM bfile b "
                    + "WHERE b.owner_id = ?1 AND b.table = ?2 "
                    + "ORDER BY b.created DESC");
            q.setParam(1, req.ints.get(0));
            q.setParam(2, req.tableName);

            System.out.println(q.getParametrizedQuery());
            r.data = q.getRecords(conn);
            return createResponse(r);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
