package api.ord.api;

import api.BaseAPI;
import api.Params;
import api.ord.model.OrdActivityPqr;
import api.ord.model.OrdCfg;
import api.ord.model.OrdPqrCyl;
import api.ord.model.OrdPqrOther;
import api.ord.model.OrdPqrTank;
import api.ord.model.OrdRepairs;
import api.ord.orfeo.OrfeoResponse;
import api.ord.orfeo.PqrResponseFromOrfeo;
import api.sys.model.Employee;
import api.sys.model.SysMailProcess;
import java.sql.Connection;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import utilities.MySQLQuery;
import utilities.SysTask;
import web.quality.SendMail;
import web.quality.SysMailUtil;
import static web.quality.SendMail.getHtmlMsg;

@Path("/orfeo")
public class OrfeoApi extends BaseAPI {

    @POST
    @Path("/pqrResponse")
    public Response pqrResponse(PqrResponseFromOrfeo orfeoResponse, @Context HttpServletRequest request) {
        String sigmaPassword = request.getHeader("X-Sigma-Password");
        try (Connection con = getConnection()) {
            if (MySQLQuery.isEmpty(sigmaPassword) || !sigmaPassword.equals("9YxsO5Eo0tH04U8b")) {
                throw new Exception("La peticion no contiene la constraseña de seguridad");
            }
            SysTask t = new SysTask(OrfeoApi.class, System.getProperty("user.name"), 1, con);
            try {
                OrdPqrCyl pqrCyl = new OrdPqrCyl().select(new Params("rad_orfeo", orfeoResponse.radNumber), con);
                OrdPqrTank pqrTank = new OrdPqrTank().select(new Params("rad_orfeo", orfeoResponse.radNumber), con);
                OrdPqrOther pqrOther = new OrdPqrOther().select(new Params("rad_orfeo", orfeoResponse.radNumber), con);
                OrdRepairs repair = new OrdRepairs().select(new Params("rad_orfeo", orfeoResponse.radNumber), con);
                Employee employee = null;
                OrdActivityPqr ordActivityPqr = new OrdActivityPqr();

                String mailSubject = "Se requiere su atención en la ";
                if (pqrCyl != null) {
                    employee = new Employee().select(pqrCyl.registBy, con);
                    ordActivityPqr.pqrCylId = pqrCyl.id;
                    mailSubject += "PQR Cilindros No. " + pqrCyl.serial;
                } else if (pqrTank != null) {
                    employee = new Employee().select(pqrTank.registBy, con);
                    ordActivityPqr.pqrTankId = pqrTank.id;
                    mailSubject += "PQR Estacionarios No. " + pqrTank.serial;
                } else if (pqrOther != null) {
                    employee = new Employee().select(pqrOther.registBy, con);
                    ordActivityPqr.pqrOtherId = pqrOther.id;
                    mailSubject += "PQR Reclamante No. " + pqrOther.serial;
                } else if (repair != null) { // repair
                    employee = new Employee().select(repair.registBy, con);
                    ordActivityPqr.repairId = repair.id;
                    mailSubject += "PQR Asistencias No. " + repair.serial;
                } else {
                    OrfeoResponse response = new OrfeoResponse();
                    response.status = false;
                    response.data = "No se encontro un pqr con ese número de radicado.";
                    return createResponse(response);
                }

                Date now = now(con);
                ordActivityPqr.actDate = now;
                ordActivityPqr.activity = "Traza Orfeo";
                ordActivityPqr.actDeveloper = employee.firstName + " " + employee.lastName; // TODO - Nombre empleado
                ordActivityPqr.creationDate = orfeoResponse.date;
                ordActivityPqr.createId = employee.id;
                ordActivityPqr.modDate = now;
                ordActivityPqr.modId = employee.id;
                ordActivityPqr.radOrfeo = orfeoResponse.responseRadNumber;
                ordActivityPqr.observation = orfeoResponse.notes;
                ordActivityPqr.insert(con);

                String content = "El módulo de Orfeo reportó una nueva actividad con radicado No. " + orfeoResponse.responseRadNumber;

                try {
                    OrdCfg cfg = new OrdCfg().select(1, con);
                    if (cfg.orfeoMailUsers && !MySQLQuery.isEmpty(employee.mail)) {
                        SendMail.sendMail(con, employee.mail, mailSubject, getHtmlMsg(con, mailSubject, content), "");
                    }

                    MySQLQuery mq = new MySQLQuery("SELECT " + SysMailProcess.getSelFlds("") + " FROM sys_mail_process WHERE UPPER(constant) = 'ORFEO_MAILS' AND active");
                    SysMailProcess mailProcess = new SysMailProcess().select(mq, con);
                    if (mailProcess != null) {
                        new SysMailUtil().sendMail(mailProcess.id, mailSubject, content, con, false);
                    }
                } catch (Exception ex) {
                    t.error(ex, con);
                    Logger.getLogger(OrfeoApi.class.getName()).log(Level.SEVERE, null, ex);
                }

                OrfeoResponse response = new OrfeoResponse();
                response.status = true;
                response.data = "Ok";
                return createResponse(response);
            } catch (Exception ex) {
                t.error(ex, con);
                Logger.getLogger(OrfeoApi.class.getName()).log(Level.SEVERE, null, ex);
                OrfeoResponse response = new OrfeoResponse();
                response.status = false;
                response.data = ex.getMessage() != null && !ex.getMessage().isEmpty()
                        ? ex.getMessage()
                        : "No se pudo registrar la actividad";
                return createResponse(response);
            }
        } catch (Exception ex) {
            Logger.getLogger(OrfeoApi.class.getName()).log(Level.SEVERE, null, ex);
            OrfeoResponse response = new OrfeoResponse();
            response.status = false;
            response.data = ex.getMessage() != null && !ex.getMessage().isEmpty()
                    ? ex.getMessage()
                    : "No se pudo registrar la actividad";
            return createResponse(response);
        }
    }
}
