package api.ord.api;

import api.BaseAPI;
import api.ord.dto.ConfirmPqrOther;
import api.ord.dto.CreatePqrOtherRequest;
import api.ord.model.OrdActivityPqr;
import api.ord.model.OrdCfg;
import api.ord.model.OrdPoll;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import api.ord.model.OrdPqrOther;
import api.ord.orfeo.OrfeoClient;
import api.ord.orfeo.OrfeoClosePqrCommand;
import api.ord.orfeo.OrfeoResponse;
import api.sys.model.Employee;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilities.SysTask;

@Path("/ordPqrOther")
public class OrdPqrOtherApi extends BaseAPI {

    @POST
    public Response insert(OrdPqrOther obj) {
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
    public Response insertComplete(CreatePqrOtherRequest obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.pqr.registDate = Calendar.getInstance().getTime();
            obj.pqr.registHour = Calendar.getInstance().getTime();
            OrdCfg ordCfg = new OrdCfg().select(1, conn);
            // register Pqr
            if (ordCfg.orfeo) {
                System.out.println("---- dependencia" + obj.command.dependencyCode);
                System.out.println("---- descripcion" + obj.command.description);
                System.out.println("---- emp doc" + obj.command.employeeDocument);
                System.out.println("---- remitente" + obj.command.remittentAddress);
                OrfeoResponse orfeoResponse = new OrfeoClient().registerPqr(obj.command);
                obj.pqr.radOrfeo = orfeoResponse.tryGetOkValue();
            }
            obj.pqr.insert(conn);
            if (ordCfg.orfeo) {
                obj.pqr.update(conn);
            }
            return Response.ok(obj.pqr).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(OrdPqrOther obj) {
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
            OrdPqrOther obj = new OrdPqrOther().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            OrdPqrOther.delete(id, conn);
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
            return createResponse(OrdPqrOther.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/confirm")
    public Response confirmPqr(ConfirmPqrOther confirmPqrOther) {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            SessionLogin sl = getSession(con);
            try {
                OrdPqrOther pqr = new OrdPqrOther().select(confirmPqrOther.pqrOtherId, con);
                OrdPoll poll = confirmPqrOther.poll;
                poll.insert(con);

                // Update pqr poll info
                Date now = now(con);
                pqr.pqrPollId = poll.id;
                pqr.confirmDate = now;

                OrdCfg ordCfg = new OrdCfg().select(1, con);
                if (ordCfg.pqrOtherHourConf) {
                    pqr.confirmHour = now;
                }
                boolean hasSuiCausal = pqr.suiCausalId != null;

                if (hasSuiCausal) {
                    pqr.suiRtaId = confirmPqrOther.suiRtaId;
                    pqr.suiNotifyId = confirmPqrOther.suiNotifyId;
                }

                if (ordCfg.pqrAdmissible) {
                    pqr.isAdmissible = confirmPqrOther.isAdmissible;
                }

                pqr.update(con);

                Employee emp = new Employee().select(sl.employeeId, con);
                // Create ord_activity_pqr
                if (ordCfg.hasObsvOtherAct && !confirmPqrOther.notes.trim().isEmpty()) {
                    OrdActivityPqr.insertClosePqrOtherActivity(con, pqr, emp, confirmPqrOther.notes);
                } else {
                    OrdActivityPqr.insertClosePqrOtherActivity(con, pqr, emp);
                }

                // Close Pqr - Orfeo request
                if (ordCfg.orfeo && pqr.radOrfeo != null) {
                    SysTask t = new SysTask(OrdPqrOtherApi.class, System.getProperty("user.name"), 1, con);
                    try {
                        OrfeoClosePqrCommand command = new OrfeoClosePqrCommand();
                        command.radNumber = pqr.radOrfeo;
                        command.date = now;
                        command.employeeDocument = emp.document;
                        command.dependencyCode = ordCfg.orfeoDependencyCode;

                        String orfeoMessage = new OrfeoClient().closePqr(command).tryGetOkValue();

                        System.out.println(orfeoMessage);
                        System.out.println("OK - orfeo communication");
                    } catch (Exception ex) {
                        t.error(ex, con);
                        Logger.getLogger(OrdPqrOtherApi.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(OrdPqrOtherApi.class.getName()).log(Level.SEVERE, null, ex);
            return createResponse(ex);
        }
    }

}
