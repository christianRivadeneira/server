package api.ord.api;

import api.BaseAPI;
import api.ord.dto.AnswerPqrCylPoll;
import api.ord.dto.ClosePqrCyl;
import api.ord.dto.PqrCylReport;
import api.ord.model.OrdActivityPqr;
import api.ord.model.OrdCfg;
import api.ord.model.OrdPoll;
import api.ord.model.OrdPollType;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import api.ord.model.OrdPqrCyl;
import api.ord.model.OrdTechnician;
import api.ord.model.OrdTextPoll;
import api.ord.orfeo.OrfeoClient;
import api.ord.orfeo.OrfeoClosePqrCommand;
import api.ord.orfeo.OrfeoTechnicalAssistanceCommand;
import api.ord.rpt.OrdPqrsReports;
import api.ord.writers.CylPQRWriter;
import api.ord.writers.RptPollCyl;
import api.ord.writers.RptPollPQR;
import api.sys.model.Employee;
import java.io.File;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilities.MySQLQuery;
import utilities.Reports;
import utilities.SysTask;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;

@Path("/ordPqrCyl")
public class OrdPqrCylApi extends BaseAPI {

    @POST
    public Response insert(OrdPqrCyl obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(OrdPqrCyl obj) {
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
            OrdPqrCyl obj = new OrdPqrCyl().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            OrdPqrCyl.delete(id, conn);
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
            return createResponse(OrdPqrCyl.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/registerAttention")
    public Response registerAttention(AnswerPqrCylPoll cylPoll) {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            SessionLogin sl = getSession(con);
            try {
                // Update poll info
                OrdPoll poll = cylPoll.poll;
                poll.update(con);

                OrdTextPoll.deletePreviousPollAnswers(con, poll.id);
                OrdTextPoll.registerPollAnswers(con, poll, cylPoll.lstText);

                // Get data
                Employee emp = new Employee().select(sl.employeeId, con);
                OrdPqrCyl pqr = new OrdPqrCyl().select(cylPoll.pqrId, con);

                // Insert activity
                OrdActivityPqr.insertAttendCylPqrActivity(con, pqr, emp);

                // Update pqr poll info
                Date now = new Date();
                pqr.pqrPollId = poll.id;
                pqr.attentionDate = now;
                pqr.attentionHour = now;
                pqr.update(con);

                // Technical Assistance - Orfeo request
                OrdCfg ordCfg = new OrdCfg().select(1, con);
                if (ordCfg.orfeo && pqr.radOrfeo != null) {

                    SysTask t = new SysTask(OrdPqrCylApi.class, System.getProperty("user.name"), 1, con);
                    try {
                        OrfeoTechnicalAssistanceCommand command = new OrfeoTechnicalAssistanceCommand();

                        command.file = getPqrFile(pqr.id, con);
                        command.radNumber = pqr.radOrfeo;
                        command.notes = OrdTechnician.getTecnicianLabel(con, pqr.technicianId) + ": " + poll.notes;
                        command.date = new Date();

                        String orfeoMessage = new OrfeoClient().registerAssistance(command).tryGetOkValue();
                        System.out.println(orfeoMessage);
                    } catch (Exception ex) {
                        t.error(ex, con);
                        Logger.getLogger(OrdPqrCylApi.class.getName()).log(Level.SEVERE, null, ex);
                        throw ex;
                    }
                }

                con.commit();
                return createResponse(pqr);
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        } catch (Exception ex) {
            Logger.getLogger(OrdPqrCylApi.class.getName()).log(Level.SEVERE, null, ex);
            return createResponse(ex);
        }
    }

    @POST
    @Path("/confirm")
    public Response closePqr(ClosePqrCyl closePqrCyl) {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            SessionLogin sl = getSession(con);
            try {
                OrdPqrCyl pqr = new OrdPqrCyl().select(closePqrCyl.ordPqrCylId, con);

                OrdPoll poll = closePqrCyl.poll;
                if (!closePqrCyl.confirmWithApp) {
                    poll.insert(con);
                    OrdTextPoll.deletePreviousPollAnswers(con, poll.id);
                    OrdTextPoll.registerPollAnswers(con, poll, closePqrCyl.textPollList);

                    // Update pqr poll info
                    pqr.attentionDate = closePqrCyl.pqrCylAttentionDate;
                    pqr.attentionHour = closePqrCyl.pqrCylAttentionHour;
                    pqr.pqrPollId = poll.id;
                }

                // Update pqr close info
                pqr.suiNotifyId = closePqrCyl.suiNotifyId;
                pqr.suiRtaId = closePqrCyl.suiRtaId;
                pqr.update(con);

                // Create ord_activity_pqr
                Employee emp = new Employee().select(sl.employeeId, con);
                OrdActivityPqr.insertCloseCylPqrActivity(con, pqr, emp);

                OrdCfg ordCfg = new OrdCfg().select(1, con);

                // Technical Assistance - Orfeo request
                SysTask t = new SysTask(OrdPqrCylApi.class, System.getProperty("user.name"), 1, con);
                try {
                    if (!closePqrCyl.confirmWithApp && ordCfg.orfeo && pqr.radOrfeo != null) {
                        OrfeoTechnicalAssistanceCommand command = new OrfeoTechnicalAssistanceCommand();

                        command.file = getPqrFile(pqr.id, con);
                        command.radNumber = pqr.radOrfeo;
                        command.notes = OrdTechnician.getTecnicianLabel(con, pqr.technicianId) + ": " + poll.notes;
                        command.date = now(con);

                        String orfeoMessage = new OrfeoClient().registerAssistance(command).tryGetOkValue();
                        System.out.println(orfeoMessage);
                    }

                    // Close Pqr - Orfeo request
                    if (ordCfg.orfeo && pqr.radOrfeo != null) {

                        OrfeoClosePqrCommand command = new OrfeoClosePqrCommand();
                        command.radNumber = pqr.radOrfeo;
                        command.date = new Date();
                        command.employeeDocument = emp.document;
                        command.dependencyCode = ordCfg.orfeoDependencyCode;

                        String orfeoMessage = new OrfeoClient().closePqr(command).tryGetOkValue();

                        System.out.println(orfeoMessage);
                    }
                } catch (Exception ex) {
                    t.error(ex, con);
                    Logger.getLogger(OrdPqrCylApi.class.getName()).log(Level.SEVERE, null, ex);
                    throw ex;
                }
                con.commit();
                return createResponse(pqr);
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        } catch (Exception ex) {
            Logger.getLogger(OrdPqrCylApi.class.getName()).log(Level.SEVERE, null, ex);
            return createResponse(ex);
        }
    }

//    REPORTES-----------------------------------------------------------------------
//     --------------------------------------------------------------------------------
    @POST
    @Path("/getDetailPqrCyl")
    public Response getDetailPqrCyl(PqrCylReport obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MySQLReport rep = OrdPqrsReports.getDetailsPqrsCylRep(obj.enterpriseId, obj.begDate, obj.endDate,
                    obj.clientType, obj.operId, obj.state,
                    obj.officeId, obj.techId, obj.channelId, obj.supReasonId, conn
            );
            File file = Reports.createReportFile("Detallado", "xls");
            MySQLReportWriter.write(rep, file, conn);
            return createResponse(file, file.getName());
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/pdfPqrCyl")
    public Response getPdfPqrCyl(@QueryParam("pqrId") int pqrId) {
        try (Connection con = getConnection()) {
            File pdf = getPqrFile(pqrId, con);
            return createResponse(pdf, "pqr_cilindros.pdf");
        } catch (Exception ex) {
            Logger.getLogger(OrdPqrCylApi.class.getName()).log(Level.SEVERE, null, ex);
            return createResponse(ex);
        }
    }

    public static File getPqrFile(int pqrId, Connection con) throws Exception {
        Integer pollTypeId = new MySQLQuery(OrdPollType.getTypeQueryByPqr(pqrId, OrdPollType.TYPE_CYL)).getAsInteger(con);
        if (pollTypeId != null && pollTypeId.equals(RptPollPQR.POLL_TYPE_CYL_APP)) {
            return new RptPollCyl().generateReport(con, pollTypeId, pqrId);
        } else {
            OrdPqrCyl pqr = new OrdPqrCyl().select(pqrId, con);
            OrdCfg cfg = new OrdCfg().select(1, con);
            CylPQRWriter writer = new CylPQRWriter(con);
            writer.beginDocument(pqr, cfg);
            return writer.endDocument();
        }
    }
}
