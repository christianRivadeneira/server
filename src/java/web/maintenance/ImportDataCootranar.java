package web.maintenance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.Dates;
import utilities.MySQLQuery;
import web.personal.GateAndExtrasCalculus;
import web.quality.SendMail;

@WebServlet(name = "ImportDataCootranar", urlPatterns = {"/ImportDataCootranar"})
public class ImportDataCootranar extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
        try {
            final Date today = Dates.trimDate(new Date());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            final SimpleDateFormat df = new SimpleDateFormat("yyMMdd");
            
            Map<String, String> req = MySQLQuery.scapedParams(request);
            if (req.get("all") != null && req.get("dt") != null) {
                throw new Exception("all y dt no deben usarse al mismo tiempo.");
            }

            conn = MySQLCommon.getConnection("sigmads", null);
            final boolean all = req.get("all") != null ? req.get("all").equals("1") : false;
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(req.get("dt") != null ? sdf.parse(req.get("dt")) : today);
            gc.add(GregorianCalendar.DAY_OF_MONTH, -1);
            final String namePatt = all ? "P_[0-9]{8,8}_[0-9]{2,2}\\.txt" : "P_" + df.format(gc.getTime()) + "[0-9]{2,2}_[0-9]{2,2}\\.txt";

            String path = "/media/ventasd/";
            String cfgPath = new MySQLQuery("SELECT c.fuel_import_path FROM mto_cfg c").getAsString(conn);

            File folder = new File(cfgPath != null && !cfgPath.isEmpty() ? cfgPath : path);
            File[] files = null;
            for (int i = 0; i < 3; i++) {
                try {
                    files = folder.listFiles(new FilenameFilter() {

                        @Override
                        public boolean accept(File dir, String name) {
                            if (!name.matches(namePatt)) {
                                return false;
                            }
                            if (all) {
                                try {
                                    return name.matches(namePatt) && Dates.trimDate(df.parse(name.substring(2, 8))).compareTo(today) != 0;
                                } catch (Exception ex) {
                                    Logger.getLogger(ImportDataCootranar.class.getName()).log(Level.SEVERE, null, ex);
                                    return false;
                                }
                            }
                            return true;
                        }
                    });
                    Thread.sleep(1000);
                } catch (Exception ex) {
                }
            }

            if (files == null) {
                throw new Exception("No se pudo listar el directorio.\nRuta Actual: " + (cfgPath != null && !cfgPath.isEmpty() ? cfgPath : path));
            }

            List<DataComb> lstData = new ArrayList<>();
            int done = 0, notFound = 0;
            for (File file : files) {
                for (int i = 0; i < 5; i++) {
                    try (BufferedReader in = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
                        String line;
                        while ((line = in.readLine()) != null) {
                            if (line.length() == 77) {
                                lstData.add(new DataComb(line));
                            }
                        }
                        done++;
                        break;
                    } catch (FileNotFoundException ffe) {
                        notFound++;
                        Thread.sleep(1000);
                    }
                }
            }
            System.out.println("Total files: " + files.length + ". Readed = " + done + ". Not Found = " + notFound);

            if (files.length == 0) {
                throw new Exception("No se encontrarón archivos a importar\nRuta Actual: " + (cfgPath != null && !cfgPath.isEmpty() ? cfgPath : path));
            }

            Object[][] fTypeData = new MySQLQuery("SELECT id, puc_code FROM fuel_type").getRecords(conn);
            Map<String, Integer> fTypes = new HashMap<>();
            for (Object[] fTypeRow : fTypeData) {
                fTypes.put(clearType(fTypeRow[1].toString()), MySQLQuery.getAsInteger(fTypeRow[0]));
            }

            for (DataComb data : lstData) {
                Object[][] vhData = new MySQLQuery("SELECT id,agency_id  FROM vehicle WHERE REPLACE(plate,' ','') = '" + data.plate + "'").getRecords(conn);
                if (!fTypes.containsKey(data.fType)) {
                    throw new Exception("No se encontró el tipo de combustible " + data.fType);
                }

                data.vhId = (vhData == null || vhData.length == 0 ? null : MySQLQuery.getAsInteger(vhData[0][0]));
                data.agencyId = (vhData == null || vhData.length == 0 ? null : MySQLQuery.getAsInteger(vhData[0][1]));
                data.fuellTypeid = fTypes.get(data.fType);
            }

            SimpleDateFormat mySqlDf = new SimpleDateFormat("yyyy-MM-dd");

            for (DataComb data : lstData) {
                if (data.vhId != null) {
                    String d = mySqlDf.format(data.date);
                    Boolean isNew = new MySQLQuery("SELECT count(*) = 0 FROM fuel_load WHERE `days` = '" + d + "' AND "
                            + "`amount` = " + data.gls + " AND  "
                            + "`fuel_type_id` = " + data.fuellTypeid + " AND  "
                            + "`vehicle_id` = " + data.vhId + " AND  "
                            + "`agency_id` = " + data.agencyId + " AND  "
                            + "`provider_id` = 837 AND "//POR LO PRONTO HARD CODE COOTRANAR
                            + "`cost` = " + data.tota + " AND  "
                            + "`fact_num` = " + data.serial + " ").getAsBoolean(conn);

                    if (isNew) {
                        String insert = "INSERT INTO fuel_load SET "
                                + "`days` = '" + d + "', "
                                + "`amount` = " + data.gls + ", "
                                + "`fuel_type_id` = " + data.fuellTypeid + ", "
                                + "`vehicle_id` = " + data.vhId + ", "
                                + "`agency_id` = " + data.agencyId + ", "
                                + "`provider_id` = 837, "//POR LO PRONTO HARD CODE COOTRANAR
                                + "`cost` = " + data.tota + ", "
                                + "`fact_num` = " + data.serial + ", "
                                + "`mileage_cur` = 0.0000 ";//PUESTO PARA QUE SALGAN LOS DATOS EN EL REPORTE COMB RECO
                        new MySQLQuery(insert).executeUpdate(conn);
                    }
                }
            }
            System.out.println("Import finished");
            response.getOutputStream().write("OK".getBytes());
        } catch (Exception e) {
            Logger.getLogger(ImportDataCootranar.class.getName()).log(Level.SEVERE, null, e);
            try {
                SendMail.sendMail(conn, "karol.mendoza@montagas.com.co", "Error en Importacion Combustibles COOTRANAR", "Los datos no se importaron correctamente.\n" + e.getMessage(), "Los datos no se importaron correctamente. \n" + e.getMessage());
            } catch (Exception ex1) {
                Logger.getLogger(GateAndExtrasCalculus.class.getName()).log(Level.SEVERE, null, ex1);
            }
            response.sendError(500, e.getMessage());
        } finally {
            MySQLCommon.closeConnection(conn);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Import data";
    }

    private String clearType(String type) {
        return type.toUpperCase().replaceAll("[^A-Z]", "").trim();
    }

    class DataComb {

        String fType;
        Date date;
        String serial;
        String XXX;
        String p;
        BigDecimal gls;
        BigDecimal tota;
        BigDecimal vunit;
        String nit;
        String plate;
        Integer vhId;
        Integer agencyId;
        Integer fuellTypeid;

        public DataComb(String line) throws Exception {
            fType = clearType(line.substring(0, 12));
            date = new SimpleDateFormat("yyyyMMdd").parse(line.substring(12, 20));
            serial = line.substring(20, 28);
            XXX = line.substring(28, 30);
            p = line.substring(30, 31);
            gls = new BigDecimal(line.substring(31, 41)).divide(new BigDecimal(1000));
            tota = new BigDecimal(line.substring(41, 51)).divide(new BigDecimal(1000));
            vunit = new BigDecimal(line.substring(51, 60)).divide(new BigDecimal(100));
            nit = line.substring(60, 71);
            plate = line.substring(71, 77);
        }
    }

}
