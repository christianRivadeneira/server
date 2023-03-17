package api.dto.api;

import api.BaseAPI;
import api.MultiPartRequest;
import api.dto.dto.DtoSaleRequest;
import api.dto.importer.DtoSaleImportError;
import api.dto.importer.Importer;
import api.dto.importer.Importer.Content;
import api.dto.importer.MinasCsvAnalisys;
import api.dto.model.DtoImportLog;
import api.dto.rpt.DtoSaleReport;
import java.io.File;
import java.sql.Connection;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import utilities.mysqlReport.MySQLReport;
import utilities.shrinkfiles.FileShrinker;
import web.fileManager;

@Path("/dtoSale")
public class DtoSaleApi extends BaseAPI {

    public synchronized void checkLock(SessionLogin sl, Connection conn) throws Exception {
        if (new MySQLQuery("SELECT locked FROM dto_lock_import WHERE id = 1 FOR UPDATE").getAsBoolean(conn)) {
            throw new Exception("Otro usuario está importando en éste momento.\nLa ventana se habilitará cuando el usuario termine su importación.");
        } else {
            new MySQLQuery("UPDATE dto_lock_import SET locked = 1, last_emp_id = " + sl.employeeId + ", beg_time = NOW(), end_time = NULL").executeUpdate(conn);
        }
    }

    public synchronized void releaseLock(Connection conn) throws Exception {
        new MySQLQuery("UPDATE dto_lock_import SET locked = 0, end_time = NOW()").executeUpdate(conn);
    }

    public static final int DTO_IMPORT_LOG = 132;//ultimo

    @POST
    @Path("/minasCsvAnalisys")
    public synchronized Response minasCsvAnalisys(@Context HttpServletRequest request) {
        try (Connection conn = getConnection()) {
            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);
            File f = Importer.fromZip(mr.getFile().file);
            try {
                SessionLogin sl = getSession(conn);
                checkLock(sl, conn);
                Content c = Importer.getContent(f, conn);
                DtoImportLog log = new DtoImportLog();
                log.fileName = mr.params.get("fileName");
                log.notes = (c.otherDates ? "Contiene datos de otras fechas. " : "");
                log.fileRows = c.allRows.size();
                log.employeeId = sl.employeeId;
                log.insert(conn);
                fileManager.upload(sl.employeeId, log.id, DTO_IMPORT_LOG, log.fileName, log.fileName, false, FileShrinker.TYPE_NONE, pi, mr.getFile().file, conn);
                MinasCsvAnalisys rta = new MinasCsvAnalisys();
                rta.errors = DtoSaleImportError.fromMap(c.errors);
                rta.importLogId = log.id;
                return Response.ok(rta).build();
            } finally {
                releaseLock(conn);
                mr.getFile().file.delete();
                f.delete();
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/importMinasCsv")
    public Response importCsv(@QueryParam("importId") int importId) {
        try (Connection conn = getConnection()) {
            try {
                SessionLogin sl = getSession(conn);
                checkLock(sl, conn);
                return Response.ok(Importer.importCsv(importId, conn)).build();
            } finally {
                releaseLock(conn);
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/importMinasAnulCsv")
    public Response importAnulCsv(@QueryParam("importId") int importId) {
        try (Connection conn = getConnection()) {
            try {
                SessionLogin sl = getSession(conn);
                checkLock(sl, conn);
                MySQLReport rep = Importer.importCsvAnul(importId, conn);
                return createResponse(rep.write(conn), "exp_cts_como.xls");
            } finally {
                releaseLock(conn);
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/getConsolidation")
    public synchronized Response getConsolidation(DtoSaleRequest req) {        
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep = DtoSaleReport.getConsolidation(req.day, req.centerId, req.salCont, req.dDay, req.chkDetail, req.centerLabel, conn);
            return createResponse(rep.write(conn), "subsidios2.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
