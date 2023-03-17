package api.crm.api;

import api.BaseAPI;
import api.MultiPartRequest;
import api.crm.model.ClientPoll;
import api.crm.model.CrmPollClient;
import api.crm.model.CrmPollMisc;
import api.ord.model.OrdPoll;
import api.ord.model.OrdTextPoll;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;
import utilities.apiClient.StringResponse;
import utilities.json.JSONDecoder;
import web.fileManager;
import web.quality.SendMail;

@Path("/CrmPollClientApi")
public class CrmPollClientApi extends BaseAPI {

    private static final int FORMAT_POLL_SIGN = 136;
    private static final String STATUS_OK = "OK";

    @POST
    @Path("createPoll")
    public Response createPoll(ClientPoll obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            try {
                conn.setAutoCommit(false);

                OrdPoll ordPoll = new OrdPoll();
                ordPoll.answer = obj.answer;
                ordPoll.notes = obj.notes;
                ordPoll.pollVersionId = obj.pollVersionId;
                ordPoll.empId = obj.empId;
                ordPoll.id = ordPoll.insert(conn);
                obj.id = ordPoll.id;

                if (obj.answer != null && !obj.answer.isEmpty()) {
                    ByteArrayInputStream bais = new ByteArrayInputStream(obj.textPoll.getBytes());
                    List<OrdTextPoll> listTP = new JSONDecoder().getList(bais, OrdTextPoll.class);

                    for (OrdTextPoll i : listTP) {
                        OrdTextPoll textPoll = new OrdTextPoll();
                        textPoll.ordinal = i.ordinal;
                        textPoll.text = i.text;
                        textPoll.pollId = ordPoll.id;
                        textPoll.insert(conn);
                    }
                }

                CrmPollClient pollClient = new CrmPollClient();
                pollClient.clientId = obj.clientId;
                pollClient.pollId = ordPoll.id;
                pollClient.tankId = obj.tankId;
                pollClient.createDate = Calendar.getInstance().getTime();
                obj.regPollId = pollClient.insert(conn);

                for (int i = 0; i < obj.dataMisc.size(); i++) {
                    CrmPollMisc row = obj.dataMisc.get(i);
                    row.pollId = ordPoll.id;
                    row.ordinal = i;
                    row.insert(conn);
                }

                conn.commit();
                return createResponse(obj);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception ex) {
            Logger.getLogger(CrmPollClientApi.class.getName()).log(Level.SEVERE, null, ex);
            return createResponse(ex);
        }
    }

    @POST
    @Path("/upSign")
    public Response upSign(@Context HttpServletRequest request) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);
            Map<String, String> pars = mr.params;
            String fileName = MySQLQuery.getAsString(pars.get("fileName"));
            String name = fileName;
            int ownerType = FORMAT_POLL_SIGN;
            String desc;

            try {
                Integer regPollId = MySQLQuery.getAsInteger(pars.get("regPollId"));
                Integer empId = MySQLQuery.getAsInteger(pars.get("empId"));

                desc = "Firma Formato";
                if (regPollId != null) {
                    fileManager.upload(empId, regPollId, ownerType, name, desc, true, null, pi, mr.getFile().file, conn);
                    return createResponse(new StringResponse(STATUS_OK));
                } else {
                    return createResponse(new StringResponse("DONT_DELETE"));
                }
            } catch (Exception ex) {
                Logger.getLogger(CrmPollClientApi.class.getName()).log(Level.SEVERE, null, ex);
                return createResponse(ex);
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/sendCreatedPollMail")
    public Response sendCreatedPollMail(@QueryParam("pollId") int pollId) {
        try (Connection conn = getConnection()) {
            Object[] pollRow = new MySQLQuery("SELECT "
                    + "DATE_FORMAT(c.create_date, '%d/%m/%Y %h:%i'), "
                    + "cl.name, "
                    + "CONCAT(e.first_name, ' ', e.last_name), "
                    + "pt.name, "
                    + "COALESCE(t.serial, 'Tanque no seleccionado') AS tank "
                    + "FROM crm_poll_client c "
                    + "INNER JOIN crm_client cl ON c.client_id = cl.id "
                    + "INNER JOIN ord_poll p ON p.id = c.poll_id "
                    + "INNER JOIN ord_poll_version pv ON pv.id = p.poll_version_id "
                    + "INNER JOIN ord_poll_type pt ON pt.id = pv.ord_poll_type_id "
                    + "LEFT JOIN employee e ON e.id = p.emp_id "
                    + "LEFT JOIN est_tank t ON t.id = c.tank_id "
                    + "WHERE p.id = " + pollId).getRecord(conn);

            String mail = new MySQLQuery("SELECT mail_format_notify FROM est_cfg").getAsString(conn);

            if (mail != null && !mail.isEmpty()) {
                String sub = "Nuevo Formato";
                String msg = "Se ha diligenciado un formato: "
                        + "\n\nFormato: " + pollRow[3]
                        + "\nFecha de diligenciamiento: " + pollRow[0]
                        + "\nCliente: " + pollRow[1]
                        + "\nCreó: " + pollRow[2]
                        + "\nTanque: " + pollRow[4];
                SendMail.sendMail(conn, mail, sub, SendMail.getHtmlMsg(conn, sub, msg), msg);
            }
            return createResponse();
        } catch (Exception e) {
            return createResponse(e);
        }
    }

}
