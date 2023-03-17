package api.mss.api;

import api.BaseAPI;
import api.MultiPartRequest;
import api.mss.model.MssScheduleCode;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
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
import utilities.MySQLQuery;
import utilities.importer.Importer;
import utilities.importer.ImporterCol;
import utilities.xlsReader.XlsReader;
import web.fileManager;

@Path("/mssScheduleCode")
public class MssScheduleCodeApi extends BaseAPI {

    @POST
    public Response insert(MssScheduleCode obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(MssScheduleCode obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssScheduleCode old = new MssScheduleCode().select(obj.id, conn);
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
            MssScheduleCode obj = new MssScheduleCode().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssScheduleCode.delete(id, conn);
            SysCrudLog.deleted(this, MssScheduleCode.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/all")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(MssScheduleCode.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/importSchedule")
    public Response importSchedule(@Context HttpServletRequest request) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);

            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);

            if (!mr.getFile().isXls()) {
                throw new Exception("El archivo no es una hoja de cáculo excel .xls");
            }
            Object[][] data = XlsReader.readExcel(mr.getFile().file.getPath(), 0).getData();

            if (data == null || data.length < 2) {
                throw new Exception("El archivo no contiene registros");
            }

            List<ImporterCol> cols = new ArrayList<>();
            cols.add(new ImporterCol("Código", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("Descripción", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("Entrada", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("Salida", ImporterCol.TYPE_TEXT, false));

            new MySQLQuery("TRUNCATE mss_schedule_code").executeUpdate(conn);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

            Importer importer = new Importer(data, cols);

            for (int i = importer.headRow + 1; i < data.length; i++) {
                importer.row = data[i];
                MssScheduleCode obj = new MssScheduleCode();
                obj.code = MySQLQuery.getAsString(importer.get(0));
                obj.notes = MySQLQuery.getAsString(importer.get(1));
                obj.begTime = sdf.parse(MySQLQuery.getAsString(importer.get(2)));
                obj.endTime = sdf.parse(MySQLQuery.getAsString(importer.get(3)));
                obj.insert(conn);
            }

            return createResponse("ok");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
