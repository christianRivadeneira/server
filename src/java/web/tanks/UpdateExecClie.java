package web.tanks;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import utilities.SysTask;

@Singleton
@Startup
public class UpdateExecClie {

    @Schedule(hour = "10,16,23", minute = "5")
    protected void processRequest() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(new Date());
        gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
        Date begDate = gc.getTime();

        gc.set(GregorianCalendar.DAY_OF_MONTH, gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
        Date endDate = gc.getTime();

        Connection conn = null;
        SysTask t = null;
        try {
            conn = MySQLCommon.getConnection("sigmads", null);
            t = new SysTask(UpdateExecClie.class, 1, conn);
            if (!new MySQLQuery("SELECT gen_est_bi FROM sys_cfg").getAsBoolean(conn)) {
                System.out.println("Retorna. No tiene permiso de generación");
                return;
            }

            Object[][] data = new MySQLQuery("SELECT DISTINCT "
                    + "c.exec_reg_id, "
                    + "c.id, "
                    + "s.id IS NOT NULL "
                    + "FROM ord_tank_client c "
                    + "LEFT JOIN est_sale s ON s.client_id = c.id AND s.sale_date BETWEEN '" + sdf.format(begDate) + "' AND '" + sdf.format(endDate) + "' "
                    + "WHERE c.exec_reg_id IS NOT NULL "
                    + "ORDER BY c.exec_reg_id, c.id").getRecords(conn);

            new MySQLQuery("DELETE FROM est_exec_clie WHERE date = '" + sdf.format(begDate) + "'").executeDelete(conn);

            int pages = (int) Math.ceil(data.length / 500d);
            for (int i = 0; i < pages; i++) {
                int index = 500 * i;
                StringBuilder sb = new StringBuilder();
                for (int j = index; j < (index + 500) && j < data.length; j++) {
                    sb.append("(").append(data[j][0]).append(",").append(data[j][1]).append(",'").append(sdf.format(begDate)).append("',").append(MySQLQuery.getAsBoolean(data[j][2]) ? "1" : "0").append("),");
                }

                if (sb.length() > 0) {
                    sb.deleteCharAt(sb.length() - 1);
                    new MySQLQuery("INSERT INTO est_exec_clie (exec_reg_id, tank_client_id, date, has_sale) VALUES " + sb.toString()).executeUpdate(conn);
                }
            }
            t.success(conn);
        } catch (Exception e) {
            if (t != null) {
                try {
                    t.error(e, conn);
                } catch (Exception ex) {
                    Logger.getLogger(UpdateExecClie.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            Logger.getLogger(UpdateExecClie.class.getName()).log(Level.SEVERE, null, e);
        }
    }

}
