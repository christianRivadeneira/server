package api.sys.api;

import api.BaseAPI;
import api.sys.model.SysFlowReq;
import java.sql.Connection;
import java.util.Date;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;

@Path("/sysFlow")
public class SysFlowApi extends BaseAPI {

    @GET
    @Path("/newRequest/{typeId}")
    public Response newRequest(@PathParam("typeId") int typeId) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            SysFlowReq sysFlowReq = createSysFlowReq(typeId, conn, sl);
            return Response.ok(sysFlowReq).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public static SysFlowReq createSysFlowReq(int typeId, Connection conn, SessionLogin sl) throws Exception {
        boolean thIntegration = new MySQLQuery("SELECT th_integration "
                + "FROM sys_flow_type WHERE id = " + typeId).getAsBoolean(conn);

        SysFlowReq sysFlowReq = new SysFlowReq();

        Object[] empRow = new MySQLQuery("select pe.id, c.office_id, sa.id, sa.area_id from "
                + "employee e "
                + "inner join per_employee pe ON pe.id = e.per_employee_id "
                + "inner join per_contract c on c.emp_id = pe.id and c.`last` and c.active "
                + "inner join per_pos p on c.pos_id = p.id "
                + "inner join per_sbarea sa on p.sarea_id = sa.id "
                + "where e.id = ?1").setParam(1, sl.employeeId).getRecord(conn);

        if (thIntegration && empRow == null) {
            throw new Exception("Debe tener un contrato activo en talento humano");
        }

        sysFlowReq.creaDate = new Date();
        sysFlowReq.typeId = typeId;
        sysFlowReq.employeeId = sl.employeeId;
        sysFlowReq.perEmpId = (thIntegration ? MySQLQuery.getAsInteger(empRow[0]) : null);
        sysFlowReq.perOfficeId = (thIntegration ? MySQLQuery.getAsInteger(empRow[1]) : null);
        sysFlowReq.perSareaId = (thIntegration ? MySQLQuery.getAsInteger(empRow[2]) : null);
        sysFlowReq.perAreaId = (thIntegration ? MySQLQuery.getAsInteger(empRow[3]) : null);

        return sysFlowReq;
    }
}
