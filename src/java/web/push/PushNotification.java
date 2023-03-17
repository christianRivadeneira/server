package web.push;

import api.crm.model.CrmTask;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.ManagedBean;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@ManagedBean
public class PushNotification {

    @Inject
    private TimerPushBean t;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection con = null;
        FileInputStream fis = null;
        BufferedInputStream input = null;
        BufferedOutputStream output = null;
        try {
            Map<String, String> req = MySQLQuery.scapedParams(request);
            String taskId = req.get("taskId");
            int tId = Integer.parseInt(taskId);
            String poolName = req.get("poolName");
            String tz = req.get("tz");
            int employeeId = Integer.parseInt(req.get("employeeId"));
            poolName = poolName != null ? poolName : "sigmads";
            con = MySQLCommon.getConnection(poolName, tz);
//            CrmTask task = new CrmTask(tId, employeeId, poolName, tz, con);
//            t.createTimer(task);
            response.setContentType("text/plain; charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            out.write("ok");
        } catch (Exception e) {
            Logger.getLogger(PushNotification.class.getName()).log(Level.SEVERE, null, e);
            response.sendError(500, e.getMessage());
        } finally {
            MySQLCommon.closeConnection(con);
            close(output);
            close(input);
            close(fis);
        }

    }

    private static void close(Closeable obj) {
        if (obj != null) {
            try {
                obj.close();
            } catch (IOException e) {
            }
        }
    }

}
