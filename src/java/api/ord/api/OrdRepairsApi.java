package api.ord.api;

import api.BaseAPI;
import api.ord.dto.AnswerPqrCylPoll;
import api.ord.dto.ConfirmRepair;
import api.ord.model.OrdActivityPqr;
import api.ord.model.OrdCfg;
import api.ord.model.OrdOffice;
import api.ord.model.OrdPoll;
import api.ord.model.OrdPollType;
import api.ord.model.OrdPqrClientTank;
import api.ord.model.OrdPqrCyl;
import api.ord.model.OrdPqrReason;
import api.ord.model.OrdPqrSuiNetType;
import api.ord.model.OrdPqrTank;
import api.ord.model.OrdRepairs;
import api.ord.model.OrdTechnician;
import api.ord.model.OrdTextPoll;
import api.ord.orfeo.OrfeoClient;
import api.ord.orfeo.OrfeoClosePqrCommand;
import api.ord.orfeo.OrfeoTechnicalAssistanceCommand;
import api.ord.writers.RepairsWriter;
import api.ord.writers.RptPollPQR;
import api.ord.writers.RptPollRepair;
import api.sys.model.Employee;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import utilities.SysTask;
import web.billing.BillingServlet;

@Path("/ordRepairs")
public class OrdRepairsApi extends BaseAPI {

    public static void setNetRespTime(OrdRepairs obj, Connection conn) throws Exception {
        if (obj.clientId != null) {
            OrdPqrClientTank c = new OrdPqrClientTank().select(obj.clientId, conn);
            if (c.billInstanceId != null) {
                if (BillingServlet.getInst(c.billInstanceId).isNetInstance()) {
                    OrdPqrReason r = new OrdPqrReason().select(obj.reasonId, conn);
                    if (r.suiNetTypeId == null) {
                        throw new Exception("Debe configurar el tipo SUI de redes para el motivo.");
                    }
                    OrdPqrSuiNetType s = new OrdPqrSuiNetType().select(r.suiNetTypeId, conn);
                    obj.netSuiRespMinutes = s.respMinutes;
                    obj.netSuiRespType = s.respType;
                }
            }
        }
    }

    @POST
    public Response insert(OrdRepairs obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            setNetRespTime(obj, conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(OrdRepairs obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            setNetRespTime(obj, conn);
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
            OrdRepairs obj = new OrdRepairs().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            OrdRepairs.delete(id, conn);
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
            return createResponse(OrdRepairs.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/registerAttention")
    public Response registerAttention(AnswerPqrCylPoll repairPoll) {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            SessionLogin sl = getSession(con);

            try {
                // Update poll info
                OrdPoll poll = repairPoll.poll;
                poll.insert(con);
                OrdTextPoll.deletePreviousPollAnswers(con, repairPoll.pqrId);
                OrdTextPoll.registerPollAnswers(con, poll, repairPoll.lstText);

                // Get data
                Employee emp = new Employee().select(sl.employeeId, con);
                OrdRepairs repair = new OrdRepairs().select(repairPoll.pqrId, con);

                //Insert activity
                OrdActivityPqr.insertAttendRepairActivity(con, repair, emp);

                // Update pqr poll info
                Date now = new Date();
                repair.pqrPollId = poll.id;
                repair.confirmDate = now;
                repair.confirmTime = now;
                repair.update(con);

                OrdCfg ordCfg = new OrdCfg().select(1, con);

                if (ordCfg.showAssiPoll) {
                    realizeSelectionAssistance(con, repair, ordCfg.assisPollRatio);
                }

                // TODO - include Log
                // Technical Assistance - Orfeo request
                if (ordCfg.orfeo && repair.radOrfeo != null) {
                    SysTask t = new SysTask(OrdRepairsApi.class, System.getProperty("user.name"), 1, con);
                    try {
                        OrfeoTechnicalAssistanceCommand command = new OrfeoTechnicalAssistanceCommand();

                        command.file = getPqrFile(repair.id, con);
                        command.radNumber = repair.radOrfeo;
                        command.notes = OrdTechnician.getTecnicianLabel(con, repair.technicianId) + ": " + poll.notes;
                        command.date = now;

                        String orfeoMessage = new OrfeoClient().registerAssistance(command).tryGetOkValue();
                        System.out.println(orfeoMessage);

                    } catch (Exception ex) {
                        t.error(ex, con);
                        Logger.getLogger(OrdRepairsApi.class.getName()).log(Level.SEVERE, null, ex);
                        throw ex;
                    }
                }

                con.commit();

                return createResponse(repair);
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        } catch (Exception ex) {
            Logger.getLogger(OrdRepairsApi.class.getName()).log(Level.SEVERE, null, ex);
            return createResponse(ex);
        }
    }

    private static void realizeSelectionAssistance(Connection con, OrdRepairs repair, int assisPollRatio) throws Exception {
        BigDecimal rnd = BigDecimal.valueOf(new Random().nextDouble());
        rnd = rnd.setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal percent = new BigDecimal(assisPollRatio / 100d);
        percent = percent.setScale(2, RoundingMode.HALF_EVEN);

        if (percent.compareTo(rnd) >= 0) {
            repair.toPoll = true;
            repair.update(con);
        }
    }

    @POST
    @Path("/confirm")
    public Response confirmPqr(ConfirmRepair confirmRepair) {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            SessionLogin sl = getSession(con);
            try {
                OrdRepairs pqr = new OrdRepairs().select(confirmRepair.repairId, con);
                OrdPoll poll = confirmRepair.poll;
                poll.insert(con);
                OrdTextPoll.deletePreviousPollAnswers(con, poll.id);
                OrdTextPoll.registerPollAnswers(con, poll, confirmRepair.textPollList);

                pqr.confirmDate = now(con);
                pqr.pqrPollId = poll.id;
                pqr.update(con);

                Employee emp = new Employee().select(sl.employeeId, con);

                OrdActivityPqr.insertCloseRepairPqrActivity(con, pqr, emp);

                OrdCfg ordCfg = new OrdCfg().select(1, con);

                // Technical Assistance - Orfeo request
                if (ordCfg.orfeo && pqr.radOrfeo != null) {
                    SysTask t = new SysTask(OrdRepairsApi.class, System.getProperty("user.name"), 1, con);
                    try {
                        OrfeoTechnicalAssistanceCommand command = new OrfeoTechnicalAssistanceCommand();

                        command.file = getPqrFile(pqr.id, con);
                        command.radNumber = pqr.radOrfeo;
                        command.notes = OrdTechnician.getTecnicianLabel(con, pqr.technicianId) + ": " + poll.notes;
                        command.date = MySQLQuery.now(con);

                        String technicalAssistanceMessage = new OrfeoClient()
                                .registerAssistance(command)
                                .tryGetOkValue();

                        System.out.println(technicalAssistanceMessage);

                        OrfeoClosePqrCommand closeCommand = new OrfeoClosePqrCommand();
                        closeCommand.radNumber = pqr.radOrfeo;
                        closeCommand.date = new Date();
                        closeCommand.employeeDocument = emp.document;
                        closeCommand.dependencyCode = ordCfg.orfeoDependencyCode;

                        String closeMessage = new OrfeoClient().closePqr(closeCommand).tryGetOkValue();

                        System.out.println(closeMessage);
                    } catch (Exception ex) {
                        t.error(ex, con);
                        Logger.getLogger(OrdRepairsApi.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(OrdRepairsApi.class.getName()).log(Level.SEVERE, null, ex);
            return createResponse(ex);
        }
    }

    @POST
    @Path("/convertPQR")
    public Response convertPQR(OrdRepairs obj) {
        try (Connection conn = getConnection()) {
            if (obj.pqrCylId == null && obj.pqrTankId == null) {
                throw new Exception("No se puede convertir, porque no hay una PQR relacionada");
            }

            if (obj.pqrCylId != null) {
                OrdPqrCyl pqr = new OrdPqrCyl().select(obj.pqrCylId, conn);

                Integer idCancelCause = new MySQLQuery("SELECT id FROM ord_pqr_anul_cause p WHERE p.type = 'cyl' AND p.cancel_repair LIMIT 1").getAsInteger(conn);

                if (idCancelCause == null) {
                    throw new Exception("No existe una causa de cancelación de asistencia técnica, para este tipo de pqr");
                }

                pqr.pqrAnulCauseId = idCancelCause;
                pqr.update(conn);

                obj.registDate = pqr.creationDate;
                obj.registHour = pqr.registHour;
                obj.registBy = pqr.registBy;
                obj.indexId = pqr.indexId;
                obj.technicianId = pqr.technicianId;
                obj.reasonId = pqr.pqrReason;
                obj.officeId = pqr.officeId;
                obj.enterpriseId = pqr.enterpriseId;
                obj.channelId = pqr.channelId;

            } else {
                OrdPqrTank pqr = new OrdPqrTank().select(obj.pqrTankId, conn);

                Integer idCancelCause = new MySQLQuery("SELECT id FROM ord_pqr_anul_cause p WHERE p.type = 'tank' AND p.cancel_repair LIMIT 1").getAsInteger(conn);

                if (idCancelCause == null) {
                    throw new Exception("No existe una causa de cancelación de asistencia técnica, para este tipo de pqr");
                }

                pqr.anulCauseId = idCancelCause;
                pqr.update(conn);

                obj.registDate = pqr.registDate;
                obj.registHour = pqr.registHour;
                obj.registBy = pqr.registBy;
                obj.clientId = pqr.clientId;
                obj.buildId = pqr.buildId;
                obj.technicianId = pqr.technicianId;
                obj.reasonId = pqr.reasonId;
                obj.officeId = pqr.officeId;
                obj.enterpriseId = pqr.enterpriseId;
                obj.channelId = pqr.channelId;
            }

            obj.serial = getSerial(obj.officeId + "", "seq_repairs", conn);

            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
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
    @Path("/pdfPqrRepairs")
    public Response getPdfPqr(@QueryParam("pqrId") int pqrId) {
        try (Connection con = getConnection()) {
            File pdf = getPqrFile(pqrId, con);
            return createResponse(pdf, "pqr_asistencia.pdf");
        } catch (Exception ex) {
            Logger.getLogger(OrdPqrCylApi.class.getName()).log(Level.SEVERE, null, ex);
            return createResponse(ex);
        }
    }

    public static File getPqrFile(int pqrId, Connection con) throws Exception {

        Integer pollTypeId = new MySQLQuery(OrdPollType.getTypeQueryByPqr(pqrId, OrdPollType.TYPE_REPAIR)).getAsInteger(con);
        OrdRepairs pqr = new OrdRepairs().select(pqrId, con);
        OrdOffice office = new OrdOffice().select(pqr.officeId, con);
        Integer pollId = new OrdRepairs().select(pqrId, con).pqrPollId;

        if (pollId != null && office.pqrsApp) {
            return new RptPollRepair().generateReport(con, RptPollPQR.POLL_TYPE_REPAIR_APP, pqrId);
        } else if (pollTypeId != null && pollTypeId.equals(RptPollPQR.POLL_TYPE_REPAIR_APP)) {
            return new RptPollRepair().generateReport(con, pollTypeId, pqrId);
        } else {
            OrdCfg cfg = new OrdCfg().select(1, con);
            RepairsWriter writer = new RepairsWriter(con);
            writer.beginDocument(pqr, cfg);
            return writer.endDocument();
        }

    }
}
