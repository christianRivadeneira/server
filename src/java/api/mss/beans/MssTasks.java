package api.mss.beans;

import api.GridResult;
import api.mss.model.MssCfg;
import api.mss.model.MssChallenge;
import api.mss.model.MssRound;
import api.mss.model.MssShift;
import api.sys.model.SystemApp;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import utilities.cast;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.Table;
import web.ShortException;
import web.quality.SendMail;

@Singleton
@Startup
public class MssTasks {

    //IMPORTANTE, cambiar aqui cuando se instale en MINVIR *************
    public static final String POOL_NAME = "ssds";
    public static final String TZ = "GMT-05:00";

    @Schedule(minute = "30", hour = "*/1")
    protected void sendChallenges() {
        try (Connection conn = MySQLCommon.getConnection(POOL_NAME, TZ)) {
            Object[][] data = new MySQLQuery("SELECT s.id, g.emp_id FROM "
                    + "mss_shift s "
                    + "INNER JOIN mss_guard g ON g.id = s.guard_id "
                    + "WHERE "
                    + "NOW() BETWEEN s.exp_beg AND s.exp_end "
                    + "AND s.reg_beg IS NOT NULL AND RAND() < 0.25").getRecords(conn);
            SystemApp app = SystemApp.getByPkgName("com.qualisys.minutas", conn);
            if (app == null) {
                return;
            }

            for (Object[] row : data) {
                int shiftId = MySQLQuery.getAsInteger(row[0]);
                int empId = MySQLQuery.getAsInteger(row[1]);
                MssChallenge.sendChallenge(app.id, shiftId, empId, conn);
            }
        } catch (ShortException ex) {
            ex.simplePrint();
        } catch (Exception e) {
            Logger.getLogger(MssTasks.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @Schedule(minute = "*/5", hour = "*")
    protected void sendRounds() {
        try (Connection conn = MySQLCommon.getConnection(POOL_NAME, TZ)) {
            SystemApp app = SystemApp.getByPkgName("com.qualisys.minutas", conn);
            if (app == null) {
                return;
            }
            System.out.println("xxxxxxxxxxxxxxx inicio tarea rondas");
            int roundTolerance = new MssCfg().select(1, conn).roundTolerance;

            Object[][] progs = new MySQLQuery("SELECT p.id, p.post_id "
                    + "FROM mss_round_prog p "
                    + "INNER JOIN mss_round_prog_time t ON t.prog_id = p.id "
                    + "WHERE (HOUR(t.`begin`)*60) + MINUTE(t.`begin`) = "
                    + "(HOUR(NOW())*60) + MINUTE(NOW());").getRecords(conn);
            System.out.println("RONDAS POR REALIZAR: " + (progs != null ? progs.length : 0));
            for (Object[] prog : progs) {
                int progId = cast.asInt(prog, 0);
                int postId = cast.asInt(prog, 1);
                System.out.println("progId: "+ progId);
                System.out.println("postId: "+ postId);
                System.out.println("BUSCANDO PROPIOS.....");
                Object[][] dataShift = new MySQLQuery("SELECT "
                        + "s.guard_id, "//0
                        + "g.emp_id, "//1
                        + "s.make_round  "//2
                        + "FROM mss_shift s "
                        + "INNER JOIN mss_guard g ON g.id = s.guard_id "
                        + "WHERE  "
                        + "s.reg_beg IS NOT NULL AND s.reg_end IS NULL  "
                        + "AND NOW() BETWEEN s.exp_beg AND s.exp_end AND s.post_id = ?1 "
                        + "ORDER BY s.reg_beg DESC ")
                        .setParam(1, postId).getRecords(conn);

                System.out.println("GURDAS PROPIOS: " + (dataShift != null ? dataShift.length : 0));
                if (dataShift != null && dataShift.length > 0 ) {
                    getGuardsAvailable(dataShift, progId, app.id, roundTolerance, conn);

                } else {
                    System.out.println("BUSCANDO ALTERNATIVOS.....");
                    Object[][] alternatives = new MySQLQuery("SELECT "
                            + "s.guard_id, g.emp_id, s.make_round "                            
                            + "FROM mss_point p "
                            + "INNER JOIN mss_point p2 ON p2.code = p.code AND p2.post_id <> p.post_id "
                            + "INNER JOIN mss_post post ON post.id = p.post_id AND post.id = ?1 "
                            + "INNER JOIN mss_post post2 ON post2.id = p2.post_id "
                            + "INNER JOIN mss_shift s ON s.post_id = post2.id "
                            + "INNER JOIN mss_guard g ON g.id = s.guard_id "
                            + "WHERE "
                            + "post.client_id = post2.client_id "
                            + "AND s.reg_beg IS NOT NULL AND s.reg_end IS NULL "
                            + "AND NOW() BETWEEN s.exp_beg AND s.exp_end "
                            + "GROUP BY s.id "
                            + "ORDER BY s.reg_beg DESC ").setParam(1, postId).getRecords(conn);
                    System.out.println("GURDAS ALETERNOS: " + (alternatives != null ? alternatives.length : 0));
                    if (alternatives != null && alternatives.length > 0) {
                        getGuardsAvailable(alternatives, progId, app.id, roundTolerance, conn);
                    }
                }
            }

        } catch (ShortException ex) {
            ex.simplePrint();
        } catch (Exception e) {
            Logger.getLogger(MssTasks.class.getName()).log(Level.SEVERE, null, e);
        }

    }

    private void getGuardsAvailable(Object[][] dataShift, int progId, int appId, Integer roundTolerance, Connection conn) throws Exception {

        if (dataShift.length == 1) {
            int guardId = cast.asInt(dataShift[0][0]);
            int empId = cast.asInt(dataShift[0][1]);            
            MssRound.sendRound(progId, guardId, empId, appId, roundTolerance, conn);
        } else {
            List<Object[]> guardsRound = new ArrayList<>();
            List<Object[]> guardsNone = new ArrayList<>();            

            for (Object[] obj : dataShift) {
                if (cast.asBoolean(obj, 2)) {
                    guardsRound.add(obj);
                } else {
                    guardsNone.add(obj);
                }
            }
            int index = 0;
            if (!guardsRound.isEmpty()) {               
                index = 0;//(int) (Math.ceil(Math.random() * guardsRound.size()) - 1);
                MssRound.sendRound(progId, cast.asInt(guardsRound.get(index)[0]), cast.asInt(guardsRound.get(index)[1]), appId, roundTolerance, conn);
            } else {
                index = 0;// (int) (Math.ceil(Math.random() * guardsRound.size()) - 1);
                MssRound.sendRound(progId, cast.asInt(guardsNone.get(index)[0]), cast.asInt(guardsNone.get(index)[1]), appId, roundTolerance, conn);
            }
        }
    }

    @Schedule(minute = "0", hour = "3")//10 DE LA NOCHE COLOMBIA
    protected void lateChekIn() {
        try (Connection conn = MySQLCommon.getConnection(POOL_NAME, TZ)) {
            GridResult r = MssShift.getLateCheckInReport(conn);
            MssCfg cfg = new MssCfg().select(1, conn);

            if (r.data != null && r.data.length > 0 && cfg != null && !MySQLQuery.isEmpty(cfg.adminMail)) {
                MySQLReport rep = new MySQLReport("Llegadas Tarde", null, "hoja1", MySQLQuery.now(conn));
                rep.setZoomFactor(80);
                rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
                rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "d/MM/yyyy hh:mm a"));//1            
                Table tbl = new Table("Llegadas Tarde");
                tbl.getColumns().add(new Column("Cedula", 15, 0));
                tbl.getColumns().add(new Column("Nombre", 40, 0));
                tbl.getColumns().add(new Column("Puesto", 60, 0));
                tbl.getColumns().add(new Column("Tipo", 25, 0));
                tbl.getColumns().add(new Column("Esperada", 25, 1));
                tbl.getColumns().add(new Column("Registrada", 25, 1));
                tbl.setData(r.data);
                rep.getTables().add(tbl);
                File file = rep.write(conn);
                String htmlMsg = SendMail.getHtmlMsg(conn, "Llegadas Tarde en Turnos", "A continuación se presenta el reporte de llegadas tarde:");
                SendMail.sendMail(conn, cfg.adminMail, "Llegadas tarde", htmlMsg, TZ, new String[]{"Llegadas_tarde.xls"}, new File[]{file});
                MssShift.updateLateShiftNotified(conn);
            }

        } catch (ShortException ex) {
            ex.simplePrint();
        } catch (Exception e) {
            Logger.getLogger(MssTasks.class.getName()).log(Level.SEVERE, null, e);
        }

    }

}
