package web.marketing.pvs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import service.MySQL.MySQLCommon;
import utilities.Batches;
import utilities.MySQLQuery;
import utilities.SysTask;
import web.ShortException;
import web.quality.SendMail;

@Singleton
@Startup
public class GetBiableInfo {

    @Schedule(hour = "5,9,12,15,18", minute = "40")
    protected void processRequest() {
        syncBiableInfo();
    }

    public static void syncBiableInfo() {
        System.out.println("SINCRONIZACIÓN CON BIABLE");
        try {
            try (Connection con = MySQLCommon.getConnection("sigmads", null)) {
                Boolean biable = new MySQLQuery("SELECT get_from_biable FROM inv_cfg").getAsBoolean(con);
                if (biable == null || !biable) {
                    System.out.println("Retorna, no permiso a biable");
                    return;
                }

                String enterprise = new MySQLQuery("SELECT `name` FROM enterprise WHERE !alternative").getAsString(con);

                SysTask t = new SysTask(GetBiableInfo.class, System.getProperty("user.name"), 1, con);
                try {
                    Connection conn = null;
                    Class.forName("com.mysql.jdbc.Driver").newInstance();
                    Properties properties = new Properties();
                    properties.put("connectTimeout", "2000");
                    conn = DriverManager.getConnection("jdbc:mysql://192.168.39.78:3306/BD_BIABLE01?user=BIABLE01&password=BIABLE01", properties);

                    try (Statement stmt = conn.createStatement()) {

                        //Inserción de terceros
                        List<Object[]> lstBiabData = new ArrayList<>();
                        stmt.executeQuery("SELECT "
                                + "t.CODIGO, "//0
                                + "t.SUCURSAL, "//1
                                + "t.CLI_LIPRE, "//2
                                + "t.CLI_LIDES, "//3
                                + "t.IND_LIQ_IMPTO_C, "//4
                                + "t.CLI_CUPO_CRE, "//5
                                + "t.CLI_D_GRACIA, "//6
                                + "SUBSTRING(t.CIUDAD_CORRESP, 4), "//7
                                + "(t.CLI_FORMA_PAGO = '1' OR t.CLI_FORMA_PAGO = '01'), "//8
                                + "(t.CLI_IND_BLO_CUPO <> '0'),  "//9
                                + "t.CLI_CO  "//10
                                + "FROM TERCEROS t "
                                + "WHERE t.CLI_LIPRE <> ''");
                        ResultSet rs = stmt.getResultSet();
                        int cTer = 0;
                        ResultSetMetaData meta = rs.getMetaData();
                        while (rs.next()) {
                            cTer++;
                            Object[] res = new Object[meta.getColumnCount()];
                            for (int j = 0; j < meta.getColumnCount(); j++) {
                                res[j] = rs.getObject(meta.getColumnLabel(j + 1));
                            }
                            lstBiabData.add(res);
                        }
                        if (cTer == 0) {
                            throw new Exception("Terminado porque la tabla terceros no tiene registros válidos");
                        }

                        StringBuilder count = new StringBuilder("contador : ");
                        count.append(" TERCEROS = ").append(cTer).append(" ");
                        new MySQLQuery("TRUNCATE TABLE bbl_terceros").executeUpdate(con);
                        new MySQLQuery("TRUNCATE TABLE bbl_price_list").executeUpdate(con);
                        new MySQLQuery("TRUNCATE TABLE bbl_discount_list").executeUpdate(con);
                        new MySQLQuery("TRUNCATE TABLE bbl_debt").executeUpdate(con);

                        Pattern pat = Pattern.compile("[0-9]+\\.[0-9]+");
                        Batches batch = new Batches(lstBiabData.size(), 500);
                        try {
                            for (int u = 0; u < batch.getBatches(); u++) {
                                StringBuilder sb = new StringBuilder("INSERT INTO bbl_terceros "
                                        + "(codigo, sucursal, cli_lipre, cli_lides, ind_liq_impto_c, cli_cupo_cre, cli_d_gracia, ciudad_corresp, cli_forma_pago, cli_ind_blo_cupo, cli_co) VALUES ");
                                for (int i = batch.getBeginIndex(u); i <= batch.getEndIndex(u); i++) {
                                    Object[] row = lstBiabData.get(i);
                                    sb.append("(");
                                    for (int j = 0; j < row.length; j++) {
                                        if (row[j] == null) {
                                            sb.append("NULL,");
                                        } else {
                                            Matcher mat = pat.matcher(MySQLQuery.getAsString(row[j]));
                                            sb.append(MySQLQuery.getAsString(row[j]).length() > 0 ? (!mat.matches() ? "'" + MySQLQuery.getAsString(row[j]).replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\\'") + "'" : row[j]) : "NULL").append(",");
                                        }
                                    }
                                    sb.deleteCharAt(sb.length() - 1);
                                    sb.append("),");
                                }
                                sb.deleteCharAt(sb.length() - 1);
                                new MySQLQuery(sb.toString()).executeInsert(con);
                                new MySQLQuery("UPDATE bbl_terceros SET cli_lipre_bin = BINARY cli_lipre , "
                                        + "cli_lides_bin =  BINARY cli_lides ").executeUpdate(con);
                            }
                        } catch (Exception ex) {
                            t.error(ex, con);
                            Logger.getLogger(GetBiableInfo.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        //Inserción de lista de precios
                        lstBiabData = new ArrayList<>();
                        stmt.executeQuery("SELECT "
                                + "IF(STRCMP('\t', pd.ID_LIPRE1)=0,\"\\\\t\",IF(STRCMP('\n', pd.ID_LIPRE1) =0,\"\\\\n\", pd.ID_LIPRE1)), "
                                + "pd.ID_REFERENCIA, "
                                + "pd.FECHA_VIG, "
                                + "pd.PRECIO_MIN_1, "
                                + "pd.IMPOCONSUMO1_1 "
                                + "FROM CMLISTA_PRECIOS_D pd "
                                + "WHERE "
                                + "LENGTH(pd.ID_REFERENCIA) > 0 "
                                + "AND LENGTH(pd.ID_LIPRE1) > 0 "
                                + "AND LENGTH(pd.FECHA_VIG) > 0 ");
                        rs = stmt.getResultSet();
                        int cPri = 0;
                        meta = rs.getMetaData();
                        while (rs.next()) {
                            cPri++;
                            Object[] res = new Object[meta.getColumnCount()];
                            for (int j = 0; j < meta.getColumnCount(); j++) {
                                res[j] = rs.getObject(meta.getColumnLabel(j + 1));
                            }
                            lstBiabData.add(res);
                        }
                        count.append(" CMLISTA_PRECIOS_D = ").append(cPri).append(" ");
                        batch = new Batches(lstBiabData.size(), 500);
                        for (int u = 0; u < batch.getBatches(); u++) {
                            System.out.println("inicio del batch " + u);
                            StringBuilder sb = new StringBuilder("INSERT INTO bbl_price_list (id_lipre1, id_referencia, fecha_vig, precio_min_1, impoconsumo1_1) VALUES ");
                            for (int i = batch.getBeginIndex(u); i <= batch.getEndIndex(u); i++) {
                                Object[] row = lstBiabData.get(i);
                                sb.append("(");
                                for (int j = 0; j < row.length; j++) {
                                    if (row[j] == null) {
                                        sb.append("NULL,");
                                    } else {
                                        Matcher mat = pat.matcher(MySQLQuery.getAsString(row[j]));
                                        sb.append(MySQLQuery.getAsString(row[j]).length() > 0 ? (!mat.matches() ? "'" + MySQLQuery.getAsString(row[j]).replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\\'") + "'" : row[j]) : "NULL").append(",");
                                    }
                                }
                                sb.deleteCharAt(sb.length() - 1);
                                sb.append("),");
                            }
                            sb.deleteCharAt(sb.length() - 1);
                            new MySQLQuery(sb.toString()).executeInsert(con);
                        }

                        new MySQLQuery("UPDATE bbl_price_list SET bin_id = BINARY id_lipre1; ").executeUpdate(con);
                        new MySQLQuery("UPDATE bbl_price_list p1 "
                                + "INNER JOIN ( "
                                + "SELECT bin_id, id_referencia "
                                + "FROM bbl_price_list "
                                + "GROUP BY bin_id,id_referencia "
                                + "having COUNT(*) < 2) p2 "
                                + "SET active = 1 "
                                + "WHERE p2.bin_id = p1.bin_id AND p2.id_referencia = p1.id_referencia ").executeUpdate(con);

                        new MySQLQuery("UPDATE bbl_price_list d1 "
                                + "INNER JOIN ( "
                                + "SELECT "
                                + "d.bin_id, d.id_referencia, "
                                + "(SELECT aux.id_lipre1 FROM bbl_price_list aux "
                                + "WHERE aux.bin_id = d.bin_id AND aux.id_referencia = d.id_referencia "
                                + "AND aux.fecha_vig = SUBSTRING_INDEX(GROUP_CONCAT(d.fecha_vig ORDER BY d.fecha_vig DESC SEPARATOR '|'), '|', 1)LIMIT 1) AS id_lipre1 "
                                + "FROM bbl_price_list d "
                                + "GROUP BY bin_id,id_referencia "
                                + "HAVING COUNT(*)>1) d2 "
                                + "SET active = 1 "
                                + "WHERE d2.bin_id = d1.bin_id AND d2.id_referencia = d1.id_referencia AND d2.id_lipre1 = d1.id_lipre1 ").executeUpdate(con);

                        //fin listado de precios
                        //Inserción de lista de descuentos
                        lstBiabData = new ArrayList<>();
                        stmt.executeQuery("SELECT "
                                + "IF(STRCMP('\t', dd.ID_LIDES1)=0,\"\\\\t\",IF(STRCMP('\n', dd.ID_LIDES1) =0,\"\\\\n\", dd.ID_LIDES1)), "
                                + "dd.FECHA_VIG, "
                                + "dd.VLRS_DES1, "
                                + "dd.ID_REFERENCIA "
                                + "FROM CMLISTA_DESCUENTOS_D dd "
                                + "WHERE "
                                + "LENGTH(dd.ID_REFERENCIA) > 0 "
                                + "AND LENGTH(dd.ID_LIDES1) > 0 "
                                + "AND LENGTH(dd.FECHA_VIG) > 0 "
                        );
                        rs = stmt.getResultSet();
                        int cDis = 0;
                        meta = rs.getMetaData();
                        while (rs.next()) {
                            cDis++;
                            Object[] res = new Object[meta.getColumnCount()];
                            for (int j = 0; j < meta.getColumnCount(); j++) {
                                res[j] = rs.getObject(meta.getColumnLabel(j + 1));
                                if (meta.getColumnLabel(j + 1).equals("FECHA_VIG")) {
                                    String date = MySQLQuery.getAsString(res[j]);
                                    if (date.length() == 8) {
                                        res[j] = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, date.length());
                                    } else {
                                        res[j] = "2000-01-01";
                                    }
                                }
                            }
                            lstBiabData.add(res);
                        }
                        count.append(" CMLISTA_DESCUENTOS_D = ").append(cDis).append(" ");
                        batch = new Batches(lstBiabData.size(), 500);
                        for (int u = 0; u < batch.getBatches(); u++) {
                            StringBuilder sb = new StringBuilder("INSERT INTO bbl_discount_list (id_lides1, fecha_vig, vlrs_des1, id_referencia) VALUES ");
                            for (int i = batch.getBeginIndex(u); i <= batch.getEndIndex(u); i++) {
                                Object[] row = lstBiabData.get(i);
                                sb.append("(");
                                for (int j = 0; j < row.length; j++) {
                                    if (row[j] == null) {
                                        sb.append("NULL,");
                                    } else {
                                        Matcher mat = pat.matcher(MySQLQuery.getAsString(row[j]));
                                        sb.append(MySQLQuery.getAsString(row[j]).length() > 0 ? (!mat.matches() ? "'" + MySQLQuery.getAsString(row[j]).replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\\'") + "'" : row[j]) : "NULL").append(",");
                                    }
                                }
                                sb.deleteCharAt(sb.length() - 1);
                                sb.append("),");
                            }
                            sb.deleteCharAt(sb.length() - 1);
                            new MySQLQuery(sb.toString()).executeInsert(con);
                        }

                        new MySQLQuery("UPDATE bbl_discount_list SET bin_id = BINARY id_lides1; ").executeUpdate(con);
                        new MySQLQuery("UPDATE bbl_discount_list d1 "
                                + "INNER JOIN ( "
                                + "SELECT bin_id, id_referencia "
                                + "FROM bbl_discount_list "
                                + "GROUP BY bin_id,id_referencia "
                                + "having COUNT(*) < 2) d2 "
                                + "SET active = 1 "
                                + "WHERE d2.bin_id = d1.bin_id AND d2.id_referencia = d1.id_referencia ").executeUpdate(con);
                        new MySQLQuery("UPDATE bbl_discount_list d1 "
                                + "INNER JOIN ( "
                                + "SELECT "
                                + "d.bin_id, d.id_referencia, "
                                + "(SELECT aux.vlrs_des1 FROM bbl_discount_list aux "
                                + "WHERE aux.bin_id = d.bin_id AND aux.id_referencia = d.id_referencia "
                                + "AND aux.fecha_vig = SUBSTRING_INDEX(GROUP_CONCAT(d.fecha_vig ORDER BY d.fecha_vig DESC SEPARATOR '|'), '|', 1)LIMIT 1) AS vlrs_des1 "
                                + "FROM bbl_discount_list d "
                                + "GROUP BY bin_id,id_referencia "
                                + "HAVING COUNT(*)>1) d2 "
                                + "SET active = 1 "
                                + "WHERE d2.bin_id = d1.bin_id AND d2.id_referencia = d1.id_referencia AND d2.vlrs_des1 = d1.vlrs_des1 ").executeUpdate(con);

                        //fin listado de descuentos
                        int bblTer = new MySQLQuery("select count(*) from bbl_terceros ").getAsInteger(con);
                        count.append(" bbl_terceros = ").append(bblTer).append(" ");
                        int bblPri = new MySQLQuery("select count(*) from bbl_price_list").getAsInteger(con);
                        count.append(" bbl_price_list = ").append(bblPri).append(" ");
                        int bblDis = new MySQLQuery("select count(*) from bbl_discount_list").getAsInteger(con);
                        count.append(" bbl_discount_list = ").append(bblDis).append(" ");
                        t.exMsg = count.toString();

                        //Inserción de cartera
                        stmt.execute("CREATE TEMPORARY TABLE IF NOT EXISTS tbl_max_dates (SELECT c.ID_TERC as ID_TERC, MAX(STR_TO_DATE(c.LAPSO_DOC , '%Y%m')) AS dt "
                                + "FROM CGRESUMEN_CXC c "
                                + "GROUP BY c.ID_TERC)");

                        lstBiabData = new ArrayList<>();
                        stmt.executeQuery("SELECT "
                                + "r.ID_TERC, "
                                + "SUM(r.SALDOS_TOT_CARTERA_L2), "
                                + "r.ID_SUC "
                                + "FROM CGRESUMEN_CXC r "
                                + "INNER JOIN tbl_max_dates md ON r.ID_TERC = md.ID_TERC AND DATE_FORMAT(md.dt,'%Y%m') = r.LAPSO_DOC  "
                                + "GROUP BY r.ID_TERC, r.ID_SUC");
                        rs = stmt.getResultSet();

                        meta = rs.getMetaData();
                        while (rs.next()) {
                            Object[] res = new Object[meta.getColumnCount()];
                            for (int j = 0; j < meta.getColumnCount(); j++) {
                                res[j] = rs.getObject(meta.getColumnLabel(j + 1));
                                if (meta.getColumnLabel(j + 1).equals("FECHA_VIG")) {
                                    String date = MySQLQuery.getAsString(res[j]);
                                    if (date.length() == 8) {
                                        res[j] = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, date.length());
                                    } else {
                                        res[j] = "2000-01-01";
                                    }
                                }
                            }
                            lstBiabData.add(res);
                        }

                        batch = new Batches(lstBiabData.size(), 500);
                        for (int u = 0; u < batch.getBatches(); u++) {
                            StringBuilder sb = new StringBuilder("INSERT INTO bbl_debt (id_terc, saldos_tot_cartera, id_suc) VALUES ");
                            for (int i = batch.getBeginIndex(u); i <= batch.getEndIndex(u); i++) {
                                Object[] row = lstBiabData.get(i);
                                sb.append("(");
                                for (int j = 0; j < row.length; j++) {
                                    if (row[j] == null) {
                                        sb.append("NULL,");
                                    } else {
                                        Matcher mat = pat.matcher(MySQLQuery.getAsString(row[j]));
                                        sb.append(MySQLQuery.getAsString(row[j]).length() > 0 ? (!mat.matches() ? "'" + MySQLQuery.getAsString(row[j]).replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\\'") + "'" : row[j]) : "NULL").append(",");
                                    }
                                }
                                sb.deleteCharAt(sb.length() - 1);
                                sb.append("),");
                            }
                            sb.deleteCharAt(sb.length() - 1);
                            new MySQLQuery(sb.toString()).executeInsert(con);
                        }
                        //fin cartera

                        saveReportInfo(con);
                        t.success(con);
                        System.out.println("Sincronización exitosa");
                    } catch (com.mysql.jdbc.exceptions.jdbc4.CommunicationsException e) {
                        //Logger.getLogger(GetBiableInfo.class.getName()).log(Level.SEVERE, null, e);
                        t.error(e, con);
                        throw new Exception("No hubo conexión con servidor de Listado de Precios");
                    } catch (com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException e) {
                        t.error(e, con);
                        throw new Exception("Error de sintaxis: " + e);
                    } finally {
                        if (conn != null) {
                            conn.close();
                        }
                    }
                } catch (Exception ex) {
                    t.error(ex, con);
                    Logger.getLogger(GetBiableInfo.class.getName()).log(Level.SEVERE, null, ex);
                    try {
                        SendMail.sendMail(con, "soporte@qualisys.com.co", "Sincronización Biable", "Error en la sincronización de Biable. - " + ex.getMessage(), "Error en sincronización de Biable " + enterprise + " - " + ex.getMessage());
                    } catch (Exception ex1) {
                        Logger.getLogger(GetBiableInfo.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
            }
        } catch (ShortException ex) {
            ex.simplePrint();
        } catch (Exception ex) {
            // para errores de cx a bd
            Logger.getLogger(GetBiableInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void saveReportInfo(Connection conn) throws Exception {
        Object[][] cylTypes = new MySQLQuery("SELECT id, `name` FROM cylinder_type").getRecords(conn);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        new MySQLQuery("TRUNCATE TABLE bbl_report").executeDelete(conn);

        for (int i = 0; i < cylTypes.length; i++) {
            String str = "INSERT INTO bbl_report "
                    + "(`terc_code`, "//0
                    + "`sucursal`, "//1
                    + "`cyl_ref`, "//2
                    + "`date_lp`,  "//3
                    + "`price_lp`,  "//4
                    + "`date_ld`,  "//5
                    + "`price_ld`, "//6
                    + "`idlipre`, "//7
                    + "`idlidesc`) "//8
                    + "VALUES ";
            Object[][] priceData = new MySQLQuery("SELECT DISTINCT "
                    + "t.codigo, "
                    + "t.sucursal, "
                    + "lp.fecha_vig, "
                    + "lp.precio_min_1, "
                    + "dd.fecha_vig, "
                    + "dd.vlrs_des1, "
                    + "lp.id_lipre1, "
                    + "dd.id_lides1 "
                    + "FROM bbl_terceros t "
                    + "INNER JOIN bbl_price_list lp ON lp.bin_id = t.cli_lipre_bin AND lp.active AND lp.id_referencia = '" + MySQLQuery.getAsString(cylTypes[i][1]) + "' "
                    + "LEFT JOIN bbl_discount_list dd ON dd.bin_id = t.cli_lides_bin AND dd.active AND dd.id_referencia = '" + MySQLQuery.getAsString(cylTypes[i][1]) + "' "
                    + "ORDER BY t.codigo").getRecords(conn);

            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < priceData.length; j++) {
                String discDate = priceData[j][4] != null ? "'" + df.format(MySQLQuery.getAsDate(priceData[j][4])) + "'" : "NULL";

                sb.append("('").append(MySQLQuery.getAsString(priceData[j][0])).append("','") //0
                        .append(MySQLQuery.getAsString(priceData[j][1])).append("','") //1
                        .append(MySQLQuery.getAsString(cylTypes[i][1])).append("','") //2
                        .append(df.format(MySQLQuery.getAsDate(priceData[j][2]))).append("',") //3
                        .append(MySQLQuery.getAsDouble(priceData[j][3])).append(",") //4
                        .append(discDate).append(",") //5
                        .append(priceData[j][5] != null ? MySQLQuery.getAsDouble(priceData[j][5]) : "NULL").append(",")
                        .append("'").append(MySQLQuery.getAsString(priceData[j][6]).replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\\'")).append("',")
                        .append("");
                if (priceData[j][7] != null) {
                    sb.append("'").append(MySQLQuery.getAsString(priceData[j][7]).replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\\'")).append("'");
                } else {
                    sb.append("NULL");
                }
                sb.append("),"); //6
            }

            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
                new MySQLQuery(str + sb.toString()).executeInsert(conn);
            }

        }

    }
}
