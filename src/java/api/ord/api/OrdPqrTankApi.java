package api.ord.api;

import api.BaseAPI;
import api.ord.dto.AnswerPqrCylPoll;
import api.ord.dto.ConfirmPqrTank;
import api.ord.dto.PqrTankReport;
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
import api.ord.model.OrdPqrTank;
import api.ord.model.OrdTechnician;
import api.ord.model.OrdTextPoll;
import api.ord.orfeo.OrfeoClient;
import api.ord.orfeo.OrfeoClosePqrCommand;
import api.ord.orfeo.OrfeoTechnicalAssistanceCommand;
import api.ord.rpt.OrdPqrsReports;
import api.ord.writers.RptPollPQR;
import api.ord.writers.RptPollTank;
import api.ord.writers.TankPQRWriter;
import api.sys.model.Employee;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilities.MySQLQuery;
import utilities.Reports;
import utilities.SysTask;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;

@Path("/ordPqrTank")
public class OrdPqrTankApi extends BaseAPI {

    @POST
    public Response insert(OrdPqrTank obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/insertComplete")
    public Response insertComplete(OrdPqrTank obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.serial = getSerial(obj.officeId + "", "seq_tank", conn);
            obj.enterpriseId = 3; //id 3 constante id de montagas
            obj.registDate = Calendar.getInstance().getTime();
            obj.registHour = Calendar.getInstance().getTime();
            obj.insert(conn);

            Employee emp = new Employee().select(obj.registBy, conn);

            new MySQLQuery("INSERT INTO ord_activity_pqr "
                    + "SET act_date = NOW(), "
                    + "activity = 'Apertura de la PQR', "
                    + "act_developer = '" + emp.firstName + " " + emp.lastName + "', "
                    + "pqr_cyl_id = NULL, "
                    + "pqr_tank_id = " + obj.id + ", "
                    + "pqr_other_id = NULL, "
                    + "repair_id = NULL, "
                    + "create_id = " + emp.id + ", "
                    + "creation_date = NOW(), "
                    + "mod_id = " + emp.id + ", "
                    + "mod_date = NOW() ").executeInsert(conn);

            if (obj.notes != null && !obj.notes.isEmpty()) {
                new MySQLQuery("INSERT INTO ord_activity_pqr "
                        + "SET act_date = NOW(), "
                        + "activity = 'Notas', "
                        + "act_developer = '" + emp.firstName + " " + emp.lastName + "', "
                        + "observation = '" + obj.notes + "', "
                        + "pqr_cyl_id = NULL, "
                        + "pqr_tank_id = " + obj.id + ", "
                        + "pqr_other_id = NULL, "
                        + "repair_id = NULL, "
                        + "create_id = " + emp.id + ", "
                        + "creation_date = NOW(), "
                        + "mod_id = " + emp.id + ", "
                        + "mod_date = NOW() ").executeInsert(conn);
            }

            new MySQLQuery("INSERT INTO `ord_log` (`owner_id`, `owner_type`, `employee_id`, `log_date`, `notes`) "
                    + "VALUES (" + obj.id + ", 5, " + emp.id + ", NOW(), 'Se creó la PQR.')").executeInsert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(OrdPqrTank obj) {
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
            OrdPqrTank obj = new OrdPqrTank().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            OrdPqrTank.delete(id, conn);
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
            return createResponse(OrdPqrTank.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/registerAttention")
    public Response registerAttention(
            AnswerPqrCylPoll tankPoll
    ) {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            SessionLogin sl = getSession(con);
            try {
                // Update poll info
                OrdPoll poll = tankPoll.poll;
                poll.update(con);
                OrdTextPoll.deletePreviousPollAnswers(con, poll.id);
                OrdTextPoll.registerPollAnswers(con, poll, tankPoll.lstText);

                // Get data                
                Employee emp = new Employee().select(sl.employeeId, con);
                OrdPqrTank pqr = new OrdPqrTank().select(tankPoll.pqrId, con);
                // Insert activity
                OrdActivityPqr.insertAttendTankPqrActivity(con, pqr, emp);

                // Update pqr poll info
                Date now = now(con);
                pqr.pqrPollId = poll.id;
                pqr.attentionDate = now;
                pqr.attentionHour = now;
                pqr.update(con);

                // Technical Assistance - Orfeo request
                OrdCfg ordCfg = new OrdCfg().select(1, con);
                if (ordCfg.orfeo && pqr.radOrfeo != null) {
                    SysTask t = new SysTask(OrdPqrTankApi.class, System.getProperty("user.name"), 1, con);
                    try {
                        OrfeoTechnicalAssistanceCommand command = new OrfeoTechnicalAssistanceCommand();

                        command.file = getPqrFile(pqr.id, con);
                        command.radNumber = pqr.radOrfeo;
                        command.notes = OrdTechnician.getTecnicianLabel(con, pqr.technicianId) + ": " + poll.notes;

                        command.date = now(con);

                        String orfeoMessage = new OrfeoClient().registerAssistance(command).tryGetOkValue();

                        System.out.println(orfeoMessage);
                    } catch (Exception ex) {
                        t.error(ex, con);
                        Logger.getLogger(OrdPqrTankApi.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(OrdPqrTankApi.class.getName()).log(Level.SEVERE, null, ex);
            return createResponse(ex);
        }
    }

    @POST
    @Path("/confirm")
    public Response confirmPqrTank(ConfirmPqrTank confirmPqrTank) {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            SessionLogin sl = getSession(con);
            try {
                OrdPqrTank pqr = new OrdPqrTank().select(confirmPqrTank.pqrTankId, con);
                OrdPoll poll = confirmPqrTank.poll;

                if (!confirmPqrTank.confirmWithApp) {
                    poll.insert(con);
                    OrdTextPoll.deletePreviousPollAnswers(con, poll.id);
                    OrdTextPoll.registerPollAnswers(con, poll, confirmPqrTank.textPollList);

                    pqr.attentionDate = confirmPqrTank.pqrAttentionDate;
                    pqr.attentionHour = confirmPqrTank.pqrAttentionHour;
                    pqr.pqrPollId = poll.id;
                }

                // Update pqr close info
                pqr.suiNotifyId = confirmPqrTank.suiNotifyId;
                pqr.suiRtaId = confirmPqrTank.suiRtaId;
                pqr.update(con);

                // Create ord_activity_pqr
                Employee emp = new Employee().select(sl.employeeId, con);
                OrdActivityPqr.insertCloseTankPqrActivity(con, pqr, emp);

                OrdCfg ordCfg = new OrdCfg().select(1, con);

                SysTask t = new SysTask(OrdPqrTankApi.class, System.getProperty("user.name"), 1, con);
                try {
                    if (!confirmPqrTank.confirmWithApp && ordCfg.orfeo && pqr.radOrfeo != null) {
                        OrfeoTechnicalAssistanceCommand command = new OrfeoTechnicalAssistanceCommand();

                        command.file = getPqrFile(pqr.id, con);
                        command.radNumber = pqr.radOrfeo;
                        command.notes = OrdTechnician.getTecnicianLabel(con, pqr.technicianId) + ": " + poll.notes;
                        command.date = now(con);

                        String orfeoMessage = new OrfeoClient()
                                .registerAssistance(command)
                                .tryGetOkValue();

                        System.out.println(orfeoMessage);
                    }

                    // Close Pqr - Orfeo request
                    if (ordCfg.orfeo && pqr.radOrfeo != null) {

                        OrfeoClosePqrCommand command = new OrfeoClosePqrCommand();
                        command.radNumber = pqr.radOrfeo;
                        command.date = now(con);
                        command.employeeDocument = emp.document;
                        command.dependencyCode = ordCfg.orfeoDependencyCode;

                        String orfeoMessage = new OrfeoClient().closePqr(command).tryGetOkValue();

                        System.out.println(orfeoMessage);
                    }

                    System.out.println("OK - orfeo communication");

                } catch (Exception ex) {
                    t.error(ex, con);
                    Logger.getLogger(OrdPqrTankApi.class.getName()).log(Level.SEVERE, null, ex);
                    throw ex;
                }
                con.commit();

                return createResponse(pqr);
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            Logger.getLogger(OrdPqrTankApi.class.getName()).log(Level.SEVERE, null, ex);
            return createResponse(ex);
        }
    }

    //REPORTES-----------------------------------------------------------------------
    //--------------------------------------------------------------------------------
    @POST
    @Path("/getDetailPqrTank")
    public Response getDetailPqrTank(PqrTankReport obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MySQLReport rep = OrdPqrsReports.getDetailedTankPqrReport(obj.begDate, obj.endDate,
                    obj.operId, obj.state,
                    obj.officeId, obj.techId, obj.channelId, obj.supReasonId, conn
            );
            File file = Reports.createReportFile("Detallado", "xls");
            MySQLReportWriter.write(rep, file, conn);
            return createResponse(file, file.getName());
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public static int getSerial(String officeId, String field, Connection conn) throws Exception {
        if (new MySQLQuery("SELECT global_serials FROM ord_cfg").getAsBoolean(conn)) {
            if (new MySQLQuery("SELECT count(*) = 0 FROM ord_global_serial").getAsBoolean(conn)) {
                new MySQLQuery("INSERT INTO ord_global_serial (id) values (1);").executeInsert(conn);
            }
            int serie = new MySQLQuery("SELECT " + field + " FROM ord_global_serial").getAsInteger(conn);
            serie++;
            new MySQLQuery("UPDATE ord_global_serial SET " + field + " = " + serie).executeUpdate(conn);
            return serie;
        } else {
            int serie = new MySQLQuery("SELECT " + field + " FROM ord_office WHERE id = " + officeId + "").getAsInteger(conn);
            serie++;
            new MySQLQuery("UPDATE ord_office SET " + field + " = " + serie + " WHERE id = " + officeId + "").executeUpdate(conn);
            return serie;
        }
    }

    @POST
    @Path("/pdfPqrTank")
    public Response getPdfPqrCyl(@QueryParam("pqrId") int pqrId) {
        try (Connection con = getConnection()) {
            File pdf = getPqrFile(pqrId, con);
            return createResponse(pdf, "pqr_estacionarios.pdf");
        } catch (Exception ex) {
            Logger.getLogger(OrdPqrCylApi.class.getName()).log(Level.SEVERE, null, ex);
            return createResponse(ex);
        }
    }

    public static File getPqrFile(int pqrId, Connection con) throws Exception {
        Integer pollTypeId = new MySQLQuery(OrdPollType.getTypeQueryByPqr(pqrId, OrdPollType.TYPE_TANK)).getAsInteger(con);
        if (pollTypeId != null && (pollTypeId.equals(RptPollPQR.POLL_TYPE_TANK_APP))) {
            return new RptPollTank().generateReport(con, pollTypeId, pqrId);
        } else {
            OrdPqrTank pqr = new OrdPqrTank().select(pqrId, con);
            OrdCfg cfg = new OrdCfg().select(1, con);
            TankPQRWriter writer = new TankPQRWriter(con);
            writer.beginDocument(pqr, cfg);
            return writer.endDocument();
        }
    }
}
