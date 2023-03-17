package web.tanks;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
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
public class CalculateIndicators {

    @Schedule(hour = "5,12,18", minute = "5")

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
            t = new SysTask(CalculateIndicators.class, 1, conn);
            if (!new MySQLQuery("SELECT gen_est_bi FROM sys_cfg").getAsBoolean(conn)) {
                System.out.println("Retorna. No tiene permiso de generación");
                return;
            } 

            Object[][] saleData = new MySQLQuery("SELECT "
                    + "s.exec_id, "
                    + "IFNULL(SUM(s.kgs), 0) "
                    + "FROM est_sale s "
                    + "WHERE "
                    + "s.bill_type != 'rem' "
                    + "AND s.exec_id IS NOT NULL "
                    + "AND s.sale_date BETWEEN '" + sdf.format(begDate) + " 00:00:00' AND '" + sdf.format(endDate) + " 23:59:59' "
                    + "GROUP BY s.exec_id").getRecords(conn);

            Object[][] goalsData = new MySQLQuery("SELECT "
                    + "pe.id, "
                    + "IFNULL(SUM(e.month_goal), 0), "
                    + "IFNULL(SUM(e.afil_goal), 0) "
                    + "FROM est_exec_reg e "
                    + "INNER JOIN per_employee pe ON e.per_emp_id = pe.id "
                    + "GROUP BY pe.id").getRecords(conn);

            Object[][] prospEfData = new MySQLQuery("SELECT "
                    + "reg_exec_id, "
                    + "COUNT(*) "
                    + "FROM est_prospect "
                    + "WHERE reg_dt BETWEEN '" + sdf.format(begDate) + " 00:00:00' AND '" + sdf.format(endDate) + " 23:59:59' "
                    + "AND client_id IS NOT NULL "
                    + "GROUP BY reg_exec_id").getRecords(conn);

            Object[][] prospData = new MySQLQuery("SELECT "
                    + "reg_exec_id, "
                    + "COUNT(*) "
                    + "FROM est_prospect "
                    + "WHERE reg_dt BETWEEN '" + sdf.format(begDate) + " 00:00:00' AND '" + sdf.format(endDate) + " 23:59:59' "
                    + "GROUP BY reg_exec_id").getRecords(conn);

            Object[][] customerData = new MySQLQuery("SELECT "
                    + "cl.exec_reg_id, "
                    + "COUNT(*), "
                    + "IFNULL((SELECT COUNT(DISTINCT c.id) "
                    + "FROM ord_tank_client c "
                    + "INNER JOIN est_sale s ON s.client_id = c.id "
                    + "WHERE "
                    + "c.type != 'build' AND s.sale_date BETWEEN '" + sdf.format(begDate) + " 00:00:00' AND '" + sdf.format(endDate) + " 23:59:59' "
                    + "AND c.exec_reg_id = cl.exec_reg_id), 0) "
                    + "FROM ord_tank_client cl "
                    + "WHERE cl.type != 'build' AND cl.active "
                    + "AND cl.exec_reg_id IS NOT NULL "
                    + "GROUP BY cl.exec_reg_id").getRecords(conn);

            Object[][] remsData = new MySQLQuery("SELECT "
                    + "s.exec_id, "
                    + "IFNULL(SUM(s.kgs), 0) "
                    + "FROM est_sale s "
                    + "WHERE "
                    + "s.bill_type = 'rem' "
                    + "AND s.exec_id IS NOT NULL "
                    + "AND s.sale_date BETWEEN '" + sdf.format(begDate) + " 00:00:00' AND '" + sdf.format(endDate) + " 23:59:59' "
                    + "GROUP BY s.exec_id").getRecords(conn);

            Object[][] instCapa = new MySQLQuery("SELECT "
                    + "c.exec_reg_id, "
                    + "(SUM(t.capacity) * (SELECT cfg.kte FROM est_cfg cfg WHERE cfg.id = 1) * 0.85) "
                    + "FROM ord_tank_client c "
                    + "INNER JOIN est_tank t ON t.client_id = c.id "
                    + "WHERE c.exec_reg_id IS NOT NULL "
                    + "GROUP BY c.exec_reg_id").getRecords(conn);

            for (int i = 0; i < goalsData.length; i++) {
                EstBiIndicator ind = new EstBiIndicator();
                ind.execId = MySQLQuery.getAsInteger(goalsData[i][0]);
                ind.goal = MySQLQuery.getAsInteger(goalsData[i][1]);
                ind.goalAfil = MySQLQuery.getAsInteger(goalsData[i][2]);
                Integer curId = new MySQLQuery("SELECT id FROM est_bi_indicator WHERE exec_id = " + ind.execId + " AND cur_month = '" + sdf.format(begDate) + "'").getAsInteger(conn);

                for (int j = 0; j < saleData.length; j++) {
                    if (MySQLQuery.getAsInteger(saleData[j][0]) == ind.execId) {
                        ind.sale = MySQLQuery.getAsInteger(saleData[j][1]);
                        break;
                    }
                }

                for (int j = 0; j < prospEfData.length; j++) {
                    if (Objects.equals(MySQLQuery.getAsInteger(prospEfData[j][0]), ind.execId)) {
                        ind.prospEf = MySQLQuery.getAsInteger(prospEfData[j][1]);
                        break;
                    }
                }

                for (int j = 0; j < prospData.length; j++) {
                    if (MySQLQuery.getAsInteger(prospData[j][0]) == ind.execId) {
                        ind.prosp = MySQLQuery.getAsInteger(prospData[j][1]);
                        break;
                    }
                }

                for (int j = 0; j < customerData.length; j++) {
                    if (MySQLQuery.getAsInteger(customerData[j][0]).equals(ind.execId)) {
                        ind.customers = MySQLQuery.getAsInteger(customerData[j][1]) + MySQLQuery.getAsInteger(customerData[j][2]);
                        ind.zeroSale = ind.customers - MySQLQuery.getAsInteger(customerData[j][2]);
                        if (ind.zeroSale < 0) {
                            ind.zeroSale = 0;
                        }
                        break;
                    }

                    for (int k = 0; k < remsData.length; k++) {
                        if (MySQLQuery.getAsInteger(remsData[k][0]).equals(ind.execId)) {
                            ind.rems = MySQLQuery.getAsInteger(remsData[k][1]);
                            break;
                        }
                    }

                    for (int k = 0; k < instCapa.length; k++) {
                        if (MySQLQuery.getAsInteger(instCapa[k][0]).equals(ind.execId)) {
                            ind.installedCapa = MySQLQuery.getAsInteger(instCapa[k][1]);
                            break;
                        }
                    }
                }

                if (curId == null && ind.customers > 0) {
                    new MySQLQuery("INSERT INTO est_bi_indicator SET "
                            + "exec_id = " + ind.execId + ", "
                            + "prosp_goal = '" + ind.goalAfil + "', "
                            + "cur_month = '" + sdf.format(begDate) + "', "
                            + "goal = " + ind.goal + ", "
                            + "sale = " + ind.sale + ", "
                            + "prosp = " + ind.prosp + ", "
                            + "prosp_ef = " + ind.prospEf + ", "
                            + "zero_sale = " + ind.zeroSale + ", "
                            + "customers = " + ind.customers + ", "
                            + "rems = " + ind.rems + ", "
                            + "installed_capa = " + ind.installedCapa).executeInsert(conn);
                } else {
                    if (ind.customers > 0) {
                        new MySQLQuery("UPDATE est_bi_indicator SET "
                                + "exec_id = " + ind.execId + ", "
                                + "prosp_goal = '" + ind.goalAfil + "', "
                                + "cur_month = '" + sdf.format(begDate) + "', "
                                + "goal = " + ind.goal + ", "
                                + "sale = " + ind.sale + ", "
                                + "prosp = " + ind.prosp + ", "
                                + "prosp_ef = " + ind.prospEf + ", "
                                + "zero_sale = " + ind.zeroSale + ", "
                                + "customers = " + ind.customers + ", "
                                + "rems = " + ind.rems + ", "
                                + "installed_capa = " + ind.installedCapa + " "
                                + "WHERE id = " + curId).executeUpdate(conn);
                    }
                }
            }
            t.success(conn);
        } catch (Exception e) {
            if (t != null) {
                try {
                    t.error(e, conn);
                } catch (Exception ex) {
                    Logger.getLogger(CalculateIndicators.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            Logger.getLogger(CalculateIndicators.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private class EstBiIndicator {

        public int execId;
        public int goal;
        public int goalAfil;
        public int sale;
        public int prosp;
        public int prospEf;
        public int zeroSale;
        public int customers;
        public int rems;
        public int installedCapa;

    }

}
