package api.per.api.perExtraFlow;

import api.per.model.PerExtraRequest;
import api.per.model.PerLicence;
import api.per.model.PerLog;
import api.sys.model.SysFlowReq;
import java.sql.Connection;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import web.system.flow.interfaces.SysFlowUpdater;

public class AprovLicense implements SysFlowUpdater {

    @Override
    public void update(SysFlowReq req, String mvType, SessionLogin sl, Connection conn) throws Exception {

        if (mvType.equals("neg")) {
            return;
        }

        int perReqId = new MySQLQuery("SELECT id FROM per_extra_request WHERE sys_req_id = " + req.id).getAsInteger(conn);
        PerExtraRequest perReq = new PerExtraRequest().select(perReqId, conn);

        if (perReq.type.equals("license")) {
            PerLicence obj = new PerLicence();
            obj.active = true;
            obj.begDate = perReq.begDate;
            obj.endDate = perReq.endDate;
            obj.empId = perReq.perEmpId;
            obj.causeId = perReq.causeId;
            obj.notes = perReq.motive;
            int licenseId = obj.insert(conn);
            LicenseEventsGate.updateEventsGate(conn, obj, null, true, perReqId);
            String logs = obj.getLogs(null, obj, conn);
            PerLog.createLog(obj.id, PerLog.PER_LICENCE, logs, sl.employeeId, conn);
            perReq.perLicenseId = licenseId;
            perReq.update(conn);
        }

    }
    
}
