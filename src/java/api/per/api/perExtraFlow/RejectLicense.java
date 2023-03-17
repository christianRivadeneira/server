package api.per.api.perExtraFlow;

import api.per.model.PerExtraRequest;
import api.per.model.PerLicence;
import api.sys.model.SysFlowReq;
import java.sql.Connection;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import web.system.flow.interfaces.SysFlowUpdater;

public class RejectLicense implements SysFlowUpdater {

    @Override
    public void update(SysFlowReq req, String mvType, SessionLogin sl, Connection conn) throws Exception {

        int perReqId = new MySQLQuery("SELECT id FROM per_extra_request WHERE sys_req_id = " + req.id).getAsInteger(conn);
        PerExtraRequest perReq = new PerExtraRequest().select(perReqId, conn);

        if (mvType.equals("pos")) {
            perReq.checked = true;
            perReq.update(conn);
        } else {
            if (perReq.type.equals("license")) {
                if (perReq.perLicenseId != null) {
                    PerLicence perLicence = new PerLicence().select(perReq.perLicenseId, conn);
                    perLicence.active = false;
                    perLicence.update(conn);
                }
            }
        }
    }

}
