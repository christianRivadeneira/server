package web.vicky;

import web.vicky.beans.CheckOrderStatus;
import web.vicky.model.OrdCylOrderTimer;
import chat.ChatWebSocket;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import service.MySQL.MySQLCommon;

public class ContextListener implements ServletContextListener {

    @Inject
    private CheckOrderStatus statusChecker;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try (Connection conn = MySQLCommon.getConnection("sigmads", null)) {
            OrdCylOrderTimer[] timers = OrdCylOrderTimer.getAllHandles(conn);
            for (OrdCylOrderTimer t : timers) {
                try {
                    t.getHandle().getTimer();
                } catch (javax.ejb.NoSuchObjectLocalException nte) {
                    OrdCylOrderTimer.delete(t.id, conn);
                    statusChecker.scheduleCheck(t.orderId);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ChatWebSocket.class.getName()).log(Level.INFO, null, ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
