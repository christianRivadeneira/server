package web.marketing.cylSales;

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
import service.MySQL.MySQLSelect;
import utilities.MySQLQuery;
import utilities.SysTask;
import web.quality.SendMail;

@Singleton
@Startup
public class GetCylsNoRotation {

    @Schedule(hour = "20", minute = "35", dayOfWeek = "4")
    protected void processRequest() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(new Date());
        gc.set(GregorianCalendar.MONTH, -18);
        Date limitDate = gc.getTime();

        gc.setTime(new Date());
        int curYear = gc.get(GregorianCalendar.YEAR);

        Connection conn = null;
        SysTask t = null;
        try {
            conn = MySQLCommon.getConnection("sigmads", null);
            if (!new MySQLQuery("SELECT phantom_nif FROM com_cfg WHERE id = 1").getAsBoolean(conn)) {
                return;
            }
            
            t = new SysTask(GetCylsNoRotation.class, 1, conn);
            conn.setAutoCommit(false);

            new MySQLQuery("DELETE FROM trk_no_rot_cyls WHERE assign_date < DATE_SUB(NOW(), INTERVAL 3 DAY) AND sale_date IS NULL").executeDelete(conn);
            new MySQLQuery("DELETE FROM trk_no_rot_cyls WHERE resp_id IS NULL OR sale_date < DATE_SUB(NOW(), INTERVAL (SELECT days_sale FROM dto_cfg WHERE id = 1) DAY)").executeDelete(conn);
            new MySQLQuery("INSERT INTO trk_no_rot_cyls (cyl_id) "
                    + "(SELECT c.id  "
                    + "FROM trk_cyl c "
                    + "LEFT JOIN trk_no_rot_cyls nr ON nr.cyl_id = c.id "
                    + "LEFT JOIN trk_sale s ON s.cylinder_id = c.id AND s.date > '" + sdf.format(limitDate) + "' "
                    + "LEFT JOIN trk_multi_cyls tmc ON tmc.cyl_id = c.id AND tmc.`type` = 'del' "
                    + "LEFT JOIN trk_sale m ON tmc.sale_id = m.id AND m.date > '" + sdf.format(limitDate) + "' "
                    + "LEFT JOIN trk_check chk ON chk.trk_cyl_id = c.id AND dt > DATE_SUB(NOW(), INTERVAL 1 MONTH) "
                    + "WHERE "
                    + "c.nif_y <> " + (curYear - 2000) + " " //nos da el año en dos dígitos
                    + "AND chk.id IS NULL "
                    + "AND s.id IS NULL "
                    + "AND tmc.id IS NULL "
                    + "AND nr.id IS NULL "
                    + "AND c.cyl_type_id <> 7 "
                    + "AND (SELECT COUNT(*) > 0 FROM dto_minas_cyl mc WHERE mc.`y` = c.nif_y AND mc.f = c.nif_f AND mc.s = c.nif_s))").executeInsert(conn);

            t.success(conn);
            conn.commit();
        } catch (Exception e) {
            Logger.getLogger(GetCylsNoRotation.class.getName()).log(Level.SEVERE, null, e);
            try {
                if (conn != null) {
                    SendMail.sendMail(conn, "soporte@qualisys.com.co", "Nifs Alternos", "Error en la generación de alternos. - " + e.getMessage(), "Error en la generación de alternos. - " + e.getMessage());
                    conn.rollback();
                }
                if (t != null) {
                    t.error(e, conn);
                }
            } catch (Exception ex) {
            }
        } finally {
            MySQLSelect.tryClose(conn);
        }
    }

}
