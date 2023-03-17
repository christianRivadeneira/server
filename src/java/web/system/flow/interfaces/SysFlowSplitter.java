package web.system.flow.interfaces;

import java.sql.Connection;
import api.sys.model.SysFlowReq;
import model.system.SessionLogin;

public interface SysFlowSplitter {

    public int[] split(SysFlowReq req, SessionLogin sl, Connection conn) throws Exception;
}
