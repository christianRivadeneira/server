package api.rpt.api;

import api.BaseAPI;
import api.MultiPartRequest;
import api.rpt.model.CubeInfo;
import api.rpt.model.Fix;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import api.rpt.model.RptCube;
import api.rpt.model.cubeImport.RptCubeImporter;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import utilities.MySQLQuery;
import web.fileManager;

@Path("/rptCube")
public class RptCubeApi extends BaseAPI {

    @POST
    public Response insert(RptCube obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(RptCube obj) {
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
            RptCube obj = new RptCube().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/cubeInfo")
    public Response cubeInfo(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(new CubeInfo(id, conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            RptCube.delete(id, conn);
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
            return createResponse(RptCube.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/fix")
    public Response fix() {
        try (Connection conn = getConnection()) {
            Fix.filtLists(conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/import")
    public Response importRequest(@Context HttpServletRequest request) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);
            Integer id = MySQLQuery.getAsInteger(mr.params.get("id"));
            RptCubeImporter.importSQL(mr.getFile().file, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/test")
    public Response test(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Integer count = new MySQLQuery(RptCubeImporter.createQuery(false, id, true, conn)).getAsInteger(conn);
            new MySQLQuery(RptCubeImporter.createQuery(false, id, false, conn).trim() + " LIMIT 0").getRecords(conn);
            return createResponse(new Count(count));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/query")
    public Response query(@QueryParam("id") int id, @QueryParam("html") boolean html) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            String q = RptCubeImporter.createQuery(html, id, false, conn);
            return createResponse(new Query(q));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public class Query {

        public String q;

        public Query(String q) {
            this.q = q;
        }
    }

    public class Count {

        public Integer c;

        public Count(Integer c) {
            this.c = c;
        }
    }
}
