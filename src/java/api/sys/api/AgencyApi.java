package api.sys.api;

import api.BaseAPI;
import api.MultiPartRequest;
import api.sys.model.Agency;
import controller.system.BackupController;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import web.fileManager;

@Path("/agency")
public class AgencyApi extends BaseAPI {

    @POST
    public Response insert(Agency obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(Agency obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.update(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            Agency obj = new Agency().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            Agency.delete(id, conn);
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
            return createResponse(Agency.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/executor")
    public Response execute(@Context HttpServletRequest request) throws Exception {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);
            String dbPar = mr.params.get("dbPar");

            byte[] data = BackupController.executeSQLFile(conn, mr.getFile().file, dbPar);

            File f = File.createTempFile("txt", ".txt");
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fileManager.copy(new ByteArrayInputStream(data), fos, true, true);
            }
            return createResponse(f, "response.txt");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
