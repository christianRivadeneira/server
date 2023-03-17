package web.system.flow.interfaces;

import java.sql.Connection;
import api.sys.model.SysFlowReq;
import model.system.SessionLogin;

public interface SysFlowUpdater {
    public void update(SysFlowReq req, String mvType, SessionLogin sl, Connection conn) throws Exception;
}
