package web.system.flow.interfaces;

import java.sql.Connection;
import api.sys.model.SysFlowReq;

public interface SysFlowValidator {

    public boolean validate(SysFlowReq req, Connection conn) throws Exception;
}
