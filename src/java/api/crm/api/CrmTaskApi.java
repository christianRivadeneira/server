package api.crm.api;

import api.BaseAPI;
import api.crm.model.CrmLog;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import api.crm.model.CrmTask;
import api.crm.model.CrmTaskRev;
import api.crm.rpt.FormatReport;
import api.sys.model.SysGcmMessage;
import java.io.File;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import utilities.Dates;
import utilities.MySQLQuery;
import utilities.apiClient.IntegerResponse;
import web.push.GCMUtils;
import web.quality.SendMail;

@Path("/crmTask")
public class CrmTaskApi extends BaseAPI {

    @POST
    public Response insert(CrmTask obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            conn.setAutoCommit(false);

            //tarea
            if (obj.creatorId == null) {
                obj.creatorId = sl.employeeId;
            }

            if (obj.progDate != null && obj.ejecDate != null) {
                obj.satisfactory = (obj.ejecDate.compareTo(obj.progDate) < 1);
            }

            int insertId = obj.insert(conn);

            //revision
            CrmTaskRev ctr = new CrmTaskRev();
            ctr.taskId = insertId;
            ctr.modifyDate = new Date();
            ctr.insert(conn);

            //recordatorio           
            if (obj.remDate != null && obj.remDate.compareTo(obj.progDate) > 0 && obj.respId != null && obj.ejecDate == null) {
                SysGcmMessage sgm = new SysGcmMessage();
                sgm.appId = new MySQLQuery("SELECT id FROM system_app WHERE package_name = 'com.glp.servicemanagers'").getAsInteger(conn);
                sgm.crmTaskId = insertId;
                sgm.regDate = new Date();
                sgm.empId = obj.respId;
                sgm.toSendDate = obj.remDate;
                sgm.sendDate = null;

                String clientName = new MySQLQuery("SELECT name FROM crm_client c WHERE c.id = " + obj.clientId).getAsString(conn);
                String creatorName = new MySQLQuery("SELECT CONCAT(first_name, ' ', last_name ) FROM employee where id = " + obj.creatorId).getAsString(conn);
                String execMsg = "El día de hoy tiene pendiente una tarea del cliente: " + clientName;

                JsonObjectBuilder rob = Json.createObjectBuilder();
                rob.add("subject", "Recordatorio de Actividad");
                rob.add("brief", obj.descShort);
                rob.add("type", "reminderActivity");
                rob.add("message", execMsg + ":\n\n" + "Actividad: " + obj.descShort);
                rob.add("user", creatorName);
                rob.add("dt", Dates.getCheckFormat().format(obj.remDate));
                sgm.data = rob.build().toString();

                sgm.insert(conn);

            }

            String logs = obj.getLogs(null, obj, conn);
            CrmLog.createLog(obj.id, CrmLog.CL_TASK, logs, sl.employeeId, conn);
            conn.setAutoCommit(true);

            Boolean pushOnTask = new MySQLQuery("SELECT push_on_task FROM crm_cfg;").getAsBoolean(conn);
            if (pushOnTask) {
                if (obj.creatorId != null && obj.respId != null
                        && !obj.creatorId.equals(obj.respId)) {
                    sendPush(obj, conn);
                }
            }

            if (obj.newProgDate != null) {
                obj.id = 0;
                obj.progDate = obj.newProgDate;
                obj.ejecDate = null;
                obj.description = null;
                obj.satisfactory = false;

                obj.insert(conn);

                CrmLog.createLog(obj.id, CrmLog.CL_TASK, logs, sl.employeeId, conn);
                if (pushOnTask) {
                    if (obj.creatorId != null && obj.respId != null
                            && !obj.creatorId.equals(obj.respId)) {
                        sendPush(obj, conn);
                    }
                }

            }
            return Response.ok(obj).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(CrmTask obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            CrmTask orig = new CrmTask().select(obj.id, conn);
            conn.setAutoCommit(false);

            if (obj.progDate != null && obj.ejecDate != null) {
                obj.satisfactory = (obj.ejecDate.compareTo(obj.progDate) < 1);
            }

            obj.update(conn);

            if (obj.ejecDate != null || obj.remDate == null) {
                SysGcmMessage.deletePushByTask(obj.id, conn);
            }

            CrmTaskRev.updateDateByTask(obj.id, conn);
            String logs = obj.getLogs(orig, obj, conn);
            CrmLog.createLog(obj.id, CrmLog.CL_TASK, logs, sl.employeeId, conn);

            if (obj.newProgDate != null) {
                obj.id = 0;
                obj.progDate = obj.newProgDate;
                obj.ejecDate = null;
                obj.description = null;
                obj.satisfactory = false;

                obj.insert(conn);

                CrmLog.createLog(obj.id, CrmLog.CL_TASK, logs, sl.employeeId, conn);

                Boolean pushOnTask = new MySQLQuery("SELECT push_on_task FROM crm_cfg;").getAsBoolean(conn);
                if (pushOnTask) {
                    if (obj.creatorId != null && obj.respId != null
                            && !obj.creatorId.equals(obj.respId)) {
                        sendPush(obj, conn);
                    }
                }

            }
            conn.setAutoCommit(true);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            CrmTask obj = new CrmTask().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            SysGcmMessage.deletePushByTask(id, conn);
            CrmTaskRev.deleteByTask(id, conn);
            CrmTask.delete(id, conn);
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
            return createResponse(CrmTask.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getCountTask")
    public Response getCountProspects(
            @QueryParam("empId") int empId,
            @QueryParam("isAdmin") boolean isAdmin,
            @QueryParam("type") String type
    ) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            String str = "SELECT COUNT(*) "
                    + "FROM crm_task t "
                    + "INNER JOIN crm_client c ON c.id = t.client_id "
                    + "INNER JOIN crm_type_task ct ON ct.id = t.type_task_id ";

            if (isAdmin) {
                str += "WHERE t.ejec_date IS NULL "
                        + "AND t.creator_id = " + empId + "  "
                        + "GROUP BY t.id ";
            } else {
                str += "WHERE ";
                if (type.equals("assigned")) {
                    str += " t.resp_id = ?1 AND t.creator_id <> t.resp_id ";
                } else {
                    str += " (c.sales_employee_id = ?1 OR t.resp_id = ?1 OR t.creator_id = ?1 ) ";
                }
                str += " AND t.ejec_date IS NULL ";
            }
            Integer count;
            if (isAdmin) {
                count = new MySQLQuery(str).getAsInteger(conn);
            } else {
                count = new MySQLQuery(str).setParam(1, empId).getAsInteger(conn);
            }

            count = count != null ? count : 0;

            return createResponse(new IntegerResponse(count));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/sendMailFormat")
    public Response sendMailFormat(@QueryParam("pollId") int pollId, @QueryParam("pollClientId") int pollClientId,
            @QueryParam("name") String name, @QueryParam("document") String document, @QueryParam("mail") String mail) {
        try (Connection conn = getConnection()) {
            new MySQLQuery("UPDATE crm_poll_client SET send_date = NOW() WHERE id = " + pollClientId).executeUpdate(conn);            
            File file = FormatReport.generateReport(conn, pollId, pollClientId);
            String sub = "Formato";
            String msg = "Se ha diligenciado un formato: "
                    + "\n\nNombre: " + name
                    + "\nCédula: " + document;
            SendMail.sendMail(conn, mail, sub, SendMail.getHtmlMsg(conn, sub, msg), msg, new String[]{file.getName()}, new File[]{file}, null, null);
            return createResponse(new IntegerResponse(1));
        } catch (Exception ex) {
            Logger.getLogger(CrmTaskApi.class.getName()).log(Level.SEVERE, null, ex);
            return createResponse(ex);
        }
    }

    @POST
    @Path("/generateReport")
    public Response generateReport(@QueryParam("pollId") int pollId, @QueryParam("pollClientId") int pollClientId) {
        try (Connection conn = getConnection()) {
            File f = FormatReport.generateReport(conn, pollId, pollClientId);
            return createResponse(f, f.getName());
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private void sendPush(CrmTask task, Connection conn) throws Exception {

        String execMsg = "";
        if (task.clientId != null) {
            String clientName = new MySQLQuery("SELECT name FROM crm_client c where c.id = " + task.clientId).getAsString(conn);
            execMsg = "Le fue asignada una actividad del cliente " + clientName.toUpperCase();
        }
        if (task.vehicleId != null) {
            String vehName = new MySQLQuery("SELECT plate FROM vehicle where id = " + task.vehicleId).getAsString(conn);
            execMsg = "Le fue asignada una actividad al vehículo " + vehName.toUpperCase();
        }
        if (task.storeId != null) {
            String vehName = new MySQLQuery("SELECT CONCAT(first_name, ' ', last_name ) FROM inv_store where id = " + task.storeId).getAsString(conn);
            execMsg = "Le fue asignada una actividad al almacen " + vehName.toUpperCase();
        }
        String creatorName = new MySQLQuery("SELECT CONCAT(first_name, ' ', last_name ) FROM employee where id = " + task.creatorId).getAsString(conn);

        JsonObjectBuilder ob1 = Json.createObjectBuilder();
        ob1.add("type", "newActivity");
        ob1.add("subject", "Nueva Actividad");
        ob1.add("brief", execMsg);
        ob1.add("message", execMsg + ":\n\n" + "Actividad: " + task.descShort);
        ob1.add("user", creatorName);
        ob1.add("dt", Dates.getCheckFormat().format(new Date()));

        GCMUtils.sendToAppManagers(ob1.build(), String.valueOf(task.respId), conn);

    }

}
