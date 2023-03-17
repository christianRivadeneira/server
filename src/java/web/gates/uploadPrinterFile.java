package web.gates;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import web.fileManager;
import web.gates.cylTrip.CylsAmount;
import web.gates.cylTrip.GtCylTrip;
import web.gates.cylTrip.GtTripManager;
import web.gates.cylTrip.ReloadTrip;

@MultipartConfig
@WebServlet(name = "uploadPrinterFile", urlPatterns = {"/uploadPrinterFile"})
public class uploadPrinterFile extends HttpServlet {

    private static final int MAX_SIZE = 1048575;

    protected synchronized void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (Connection con = MySQLCommon.getConnection("sigmads", null)) {

            GZIPInputStream gin = new GZIPInputStream(request.getPart("data").getInputStream());
            ByteArrayOutputStream bais = new ByteArrayOutputStream();

            fileManager.copy(gin, bais, true, true);
            byte[] data = bais.toByteArray();

            PrintData pd = new PrintData(data, request.getRemoteAddr(), con);
            //PrintData pd = new PrintData(request.getInputStream(), request.getRemoteAddr(), con);
            if (pd.isPc) {
                try {
                    new Cargue(pd).processPC();
                    new MySQLQuery("UPDATE gt_print SET is_trip = 1 WHERE id = " + pd.gtPrintId).executeUpdate(con);
                } catch (Exception ex) {
                    Logger.getLogger(uploadPrinterFile.class.getName()).log(Level.SEVERE, null, ex);
                    MySQLQuery q = new MySQLQuery("UPDATE gt_print SET is_trip = 0, log = ?1 WHERE id = ?2");
                    q.setParam(1, ex.getClass().getCanonicalName() + " - " + ex.getMessage());
                    q.setParam(2, pd.gtPrintId);
                    q.executeUpdate(con);
                }
            }
            response.getOutputStream().print("ok");
        } catch (Exception ex) {
            Logger.getLogger(uploadPrinterFile.class.getName()).log(Level.SEVERE, null, ex);
            response.getOutputStream().print("error");
        } finally {
            response.getOutputStream().close();
        }
    }

    class PrintData {

        byte[] rawData;
        private byte[] zipData = null;

        boolean isPc;
        int gtPrintId;
        Connection con;

        public PrintData(byte[] rawData, String remoteAddr, Connection con) throws Exception {
            this.con = con;
            this.rawData = rawData;
            this.isPc = isPC();
            saveGtPrint(remoteAddr);
        }

        public byte[] zipData() throws Exception {
            if (zipData == null) {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); GZIPOutputStream goz = new GZIPOutputStream(baos); ByteArrayInputStream bais = new ByteArrayInputStream(rawData);) {
                    fileManager.copy(bais, goz, true, true);
                    zipData = baos.toByteArray();
                }
            }
            return zipData;
        }

        private void saveGtPrint(String remoteAddr) throws Exception {
            if (rawData.length <= MAX_SIZE) {
                MySQLQuery q = new MySQLQuery("INSERT INTO gt_print SET data = ?1, dt = NOW(), ip = ?2, should_be_trip = ?3");
                q.setParam(1, zipData());
                q.setParam(2, remoteAddr);
                q.setParam(3, isPc);
                gtPrintId = q.executeInsert(con);
            } else {
                MySQLQuery q = new MySQLQuery("INSERT INTO gt_print SET data = null, dt = NOW(), ip = ?1, should_be_trip = 0, log = 'Archivo demasiado grande.'");
                q.setParam(1, remoteAddr);
                gtPrintId = q.executeInsert(con);
            }
        }

        private boolean isPC() throws IOException {
            if (rawData.length > MAX_SIZE) {
                return false;
            }
            String[] pcEG = new String[6];
            pcEG[0] = "";
            pcEG[1] = "                                                                    +-----------------------+";
            pcEG[2] = "               ENERGAS SA ESP                                       |  PLANILLA DE CARGUE   |";
            pcEG[3] = "               ENERGAS SA ESP";
            pcEG[4] = "                                                                       PLANILLA DE CARGUE";
            pcEG[5] = "  NIT.: 800182395-6                                                 +-----------------------+";

            String[] pcMG = new String[6];
            pcMG[0] = "";
            pcMG[1] = "                                                                    +-----------------------+";
            pcMG[2] = "             MONTAGAS S A E S P                                     |  PLANILLA DE CARGUE   |";
            pcMG[3] = "             MONTAGAS S A E S P";
            pcMG[4] = "                                                                       PLANILLA DE CARGUE";
            pcMG[5] = "  NIT.: 891202203-9                                                 +-----------------------+";
            return checkPattern(pcMG) || checkPattern(pcEG);
        }

        private boolean checkPattern(String[] pattern) throws IOException {
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(rawData)));
            boolean isEG = true;
            for (int i = 0; (line = br.readLine()) != null && isEG && i < pattern.length; i++) {
                isEG = line.equals(pattern[i]);
            }
            return isEG;
        }
    }

    class Cargue {

        public String plate;
        public String number;
        public String driver;
        public String location;
        public boolean GLPQI;
        public List<Product> prods = new ArrayList<>();
        private final PrintData pd;
        public boolean autocomp = false;
        public boolean reload = false;

        public Cargue(PrintData pd) throws Exception {
            this.pd = pd;
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(pd.rawData)));
            while ((line = br.readLine()) != null) {
                if (line.contains("| Numero: ")) {
                    number = line.substring(78, line.length() - 1).trim();
                } else if (line.startsWith("| Vehiculo    :")) {
                    plate = line.substring(33, 44).trim();
                } else if (line.startsWith("| Conductor   : ")) {
                    driver = line.substring(16, 33).trim();
                } else if (line.startsWith("| Localizacion: ")) {
                    location = line.substring(16, 33).trim();
                } else if (line.equals("|Referencia  D e s c r i p c i o n  |   Cant.Sale |  Dev.Llenos | Dev.Vacios | Cant a Llenar|")) {
                    br.readLine();
                    while ((line = br.readLine()) != null) {
                        if (line.equals("+-------------------------------------------------------------------------------------------+")) {
                            br.readLine();
                            line = br.readLine();
                            if (line.contains("Observacion:")) {
                                autocomp = line.toLowerCase().contains("#comp");
                                reload = line.toLowerCase().contains("#rec");
                            }
                            return;
                        }
                        Product p = new Product(line);
                        if (line.contains("GLPQI")) {
                            if (!p.strAmountGlp.isEmpty()) {
                                prods.add(p);
                                GLPQI = true;
                            }
                        } else if (!p.strAmount.isEmpty() && !p.ref.isEmpty()) {
                            prods.add(p);
                            GLPQI = false;
                        }
                    }
                }
            }
        }

        private void processPC() throws Exception {
            Integer vhId = new MySQLQuery("SELECT v.id FROM vehicle AS v WHERE v.active AND REPLACE(v.plate,' ','') = '" + plate.replace(" ", "").replace("-", "") + "'").getAsInteger(pd.con);

            Integer driverId = new MySQLQuery("SELECT driver_id FROM driver_vehicle WHERE vehicle_id = " + vhId + " AND end IS NULL").getAsInteger(pd.con);
            if (driverId == null) {
                driverId = new MySQLQuery("SELECT e.id FROM employee AS e WHERE e.document = '" + driver + "'").getAsInteger(pd.con);
            }

            CenterCfg centCfg = new CenterCfg(pd.con, location);
            if (GLPQI && centCfg.saveTankTrip) {
                createGlpTrip(vhId, driverId, centCfg);
            } else if (!GLPQI && centCfg.saveCylTrip) {
                if (reload) {
                    createReload();
                } else {
                    createSaleTrip(vhId, driverId, centCfg);
                }
            }
        }

        private void createSaleTrip(Integer vhId, Integer driverId, CenterCfg centCfg) throws Exception {
            for (Product prod : prods) {
                Object[] row = new MySQLQuery("SELECT capa_id, type_id FROM gt_siesa_cyltype WHERE code = '" + prod.ref + "'").getRecord(pd.con);
                if (row != null) {
                    prod.capaId = MySQLQuery.getAsInteger(row[0]);
                    prod.typeId = MySQLQuery.getAsInteger(row[1]);
                } else {
                    throw new Exception("No se encontró el producto código: " + prod.ref);
                }
            }

            //////////////////PARA VENTA DIRECTA////////////////////
            //ver si el ya hay un viaje con el mismo número de autorización y si es así consultar el número de pasos realizados
            boolean isReprint = false;
            Object[] tripInfo = new MySQLQuery("SELECT steps, id FROM gt_cyl_trip WHERE auth_doc LIKE '" + number + "' AND cancel = 0").getRecord(pd.con);
            Integer oldTripId = null;
            if (tripInfo != null) {
                Integer oldTripSteps = MySQLQuery.getAsInteger(tripInfo[0]);
                oldTripId = MySQLQuery.getAsInteger(tripInfo[1]);
                if (oldTripSteps > 1) {
                    throw new Exception("Un viaje con el mismo número (" + number + ") ya está en curso. No se puede crear.");
                } else {
                    isReprint = true;
                }
            }

            //Para saber si el vehículo tiene una entrada de venta directa pendiente
            Integer directSellId = new MySQLQuery("SELECT "
                    + "ct.id "
                    + "FROM gt_cyl_trip AS ct "
                    + "INNER JOIN gt_trip_type AS tt ON tt.id = ct.type_id "
                    + "WHERE "
                    + "ct.cancel = 0 "
                    + "AND tt.direct_sell = 1 "
                    + "AND tt.e = 1 "
                    + "AND ct.center_orig_id = " + centCfg.centerOrigId + " "
                    + "AND ct.req_steps = ct.steps "
                    + "AND (ct.next_trip_id IS NULL OR ct.next_trip_id = " + oldTripId + ") "
                    + "AND " + (vhId != null ? "ct.vh_id = " + vhId + " " : "ct.plate = '" + plate.replace(" ", "") + "' ")
                    + "ORDER BY ct.trip_date DESC LIMIT 1 ").getAsInteger(pd.con);
            int tripTypeId = (directSellId != null ? centCfg.dsTripTypeId : (autocomp ? centCfg.autoTripTypeId : centCfg.sTripTypeId));

            ///////////////
            int reqSteps = new MySQLQuery("SELECT steps FROM gt_trip_type WHERE id = " + tripTypeId).getAsInteger(pd.con);

            GtCylTrip trip = new GtCylTrip();
            trip.tripDate = new Date();
            trip.authDoc = number;
            trip.cdt = new Date();
            trip.employeeId = 1;
            if (vhId != null) {
                trip.driverId = driverId;
                trip.driver = driver;
                trip.vhId = vhId;
                trip.plate = plate.replace(" ", "");
            }
            trip.enterpriseId = centCfg.enterpriseId;
            trip.typeId = tripTypeId;
            trip.centerOrigId = centCfg.centerOrigId;
            trip.centerDestId = centCfg.centerOrigId;
            trip.blocked = false;
            trip.cancel = false;
            trip.textData = pd.zipData();
            trip.steps = 1;
            trip.reqSteps = reqSteps;
            List<CylsAmount> invLst = new ArrayList<>();
            for (Product prod : prods) {
                CylsAmount item = new CylsAmount();
                item.capa = prod.capaId;
                item.typeId = prod.typeId;
                item.amount = MySQLQuery.getAsInteger(prod.strAmount);
                item.state = "l";
                invLst.add(item);
            }

            if (!isReprint) {
                Integer tripId = GtTripManager.createCylTrip(trip, null, false, pd.con);

                if (directSellId != null) {
                    new MySQLQuery("UPDATE gt_cyl_trip SET next_trip_id = " + tripId + " WHERE id = " + directSellId).executeUpdate(pd.con);
                }

                GtTripManager.fillTripInventory(tripId, 1, "c", new Date(), invLst, "Creado desde Impresión", false, pd.con);
                GtTripManager.createCylTripLog(tripId, 1, "new", "Creado desde Impresión.", null, pd.con);
            } else {
                trip.id = oldTripId;

                GtTripManager.editCylTrip(trip, "Editado por reimpresión", false, pd.con);
                new MySQLQuery("DELETE FROM gt_cyl_inv WHERE trip_id = " + oldTripId).executeDelete(pd.con);
                GtTripManager.fillTripInventory(oldTripId, 1, "c", new Date(), invLst, "Editado por reimpresión", false, pd.con);
            }
        }

        private void createReload() throws Exception {
            for (Product prod : prods) {
                Object[] row = new MySQLQuery("SELECT capa_id, type_id FROM gt_siesa_cyltype WHERE code = '" + prod.ref + "'").getRecord(pd.con);
                if (row != null) {
                    prod.capaId = MySQLQuery.getAsInteger(row[0]);
                    prod.typeId = MySQLQuery.getAsInteger(row[1]);
                } else {
                    throw new Exception("No se encontró el producto código: " + prod.ref);
                }
            }

            Integer tripId = new MySQLQuery("SELECT id FROM gt_cyl_trip WHERE auth_doc = '" + number + "' AND !blocked AND !cancel AND edt IS NULL").getAsInteger(pd.con);
            if (tripId == null) {
                throw new Exception("El viaje " + number + " no está activo para realizar recargues.");
            }

            Integer relId = new MySQLQuery("SELECT r.id "
                    + "FROM gt_trip_reload r "
                    + "WHERE "
                    + "!r.cancelled "
                    + "AND r.edt IS NOT NULL "
                    + "AND r.cdt IS NULL "
                    + "AND (SELECT COUNT(*) > 0 FROM gt_trip_reload_inv i WHERE i.trip_rel_id = r.id AND i.`type` = 'e') "
                    + "AND r.trip_id = " + tripId).getAsInteger(pd.con);

            if (relId == null) {
                throw new Exception("El viaje no está habilitado para un evento de recargue");
            }
            
            List<CylsAmount> lstAmount = new ArrayList<>();
            
            for (int i = 0; i < prods.size(); i++) {
                CylsAmount it = new CylsAmount();
                it.capa = prods.get(i).capaId;
                it.typeId = prods.get(i).typeId;
                it.state = "l";
                it.amount = MySQLQuery.getAsInteger(prods.get(i).strAmount);
                lstAmount.add(it);
            }

            ReloadTrip.createReloadInv(relId, tripId, lstAmount, pd.con);
        }

        private void createGlpTrip(Integer vhId, Integer driverId, CenterCfg centCfg) throws Exception {
            //Carrontaque GLPQI
            if (new MySQLQuery("SELECT COUNT(*)>0 FROM gt_glp_trip WHERE auth_doc LIKE '" + number + "' AND steps > 1").getAsBoolean(pd.con)) {
                throw new Exception("No se crea el viaje. Porque hay uno que ya está en movimiento con este número de planilla. [" + number + "]");
            }

            BigDecimal capaFull = new MySQLQuery("(SELECT capa_full FROM gt_glp_trip WHERE vh_id = " + vhId + " AND trip_date >= DATE_SUB(NOW(),INTERVAL 15 DAY) AND cancel = 0 GROUP BY capa_full ORDER BY count(*) DESC limit 0,1)").getAsBigDecimal(pd.con, true);
            Integer reqSteps = new MySQLQuery("SELECT steps FROM gt_trip_type WHERE id = " + centCfg.glpTripTypeId).getAsInteger(pd.con);
            //////CANCELANDO viajes anteriores sin pasos o con la creación
            new MySQLQuery("UPDATE gt_glp_trip SET cancel = 1, cancel_notes = 'Cancelado desde captura de impresión' "
                    + "WHERE type_id = " + centCfg.glpTripTypeId + " "
                    + "AND cancel = 0 "
                    + "AND (steps = 0 OR steps = 1) "
                    + "AND " + (vhId != null ? "vh_id = " + vhId : "plate = '" + plate + "' ") + " "
                    + "AND auth_doc LIKE '" + number + "'").executeUpdate(pd.con);
            //////////////////
            String insetTrip = "INSERT INTO "
                    + "gt_glp_trip SET "
                    + "`trip_date` = NOW(), "
                    + "`type_id` = " + centCfg.glpTripTypeId + ", "
                    + (vhId != null ? "`vh_id` = " + vhId : "`plate` = '" + plate + "' ") + ", "
                    + (vhId != null ? "`driver_id` = " + driverId : "`driver` = '" + driver + "'") + ", "
                    + "`auth_doc` = '" + number + "', "
                    + "`capa_full` = " + (capaFull != null ? capaFull : BigDecimal.ZERO) + ","
                    + "`center_orig_id` = " + centCfg.centerOrigId + ", "
                    + "`center_dest_id` = " + centCfg.centerOrigId + ", "
                    + "`enterprise_id` = " + centCfg.enterpriseId + ", "
                    + "`employee_id` = 1, "
                    + "`cancel` = 0, "
                    + "text_data = ?1,"
                    + "req_steps = " + reqSteps;
            MySQLQuery q = new MySQLQuery(insetTrip);
            q.setParam(1, pd.zipData());
            Integer tripId = q.executeInsert(pd.con);

            new MySQLQuery("INSERT INTO gt_glp_trip_log SET "
                    + "`trip_id` = " + tripId + ", "
                    + "`employee_id` = 1, "
                    + "`log_date` = NOW(), "
                    + "`type` = 'new', "
                    + "`notes` = 'Creación de viaje desde Impresión capturada.'").executeInsert(pd.con);
        }
    }

    class Product {

        public String ref;
        public String strAmount;
        public String strAmountGlp;
        public int capaId;
        public int typeId;

        public Product() {
        }

        public Product(String line) {
            ref = line.substring(1, 13).trim();
            if (ref.contains("GLPQI")) {//carrotanques
                strAmountGlp = line.substring(38, 50).trim();
            } else {
                strAmount = line.substring(38, 50).trim();
            }
        }

        public Product(Object[] row) {
            this.capaId = MySQLQuery.getAsInteger(row[0]);
            this.typeId = MySQLQuery.getAsInteger(row[1]);
            this.strAmount = MySQLQuery.getAsString(row[2]);
        }
    }

    class CenterCfg {

        Integer centerOrigId;
        Integer enterpriseId;
        Integer sTripTypeId;
        Integer glpTripTypeId;
        Integer dsTripTypeId;
        Integer autoTripTypeId;
        boolean saveCylTrip;
        boolean saveTankTrip;

        public CenterCfg(Connection con, String location) throws Exception {
            Object[] data = new MySQLQuery("SELECT gts.gt_center_id, "
                    + "gts.enterprise_id, "
                    + "gts.trip_type_id, "
                    + "gts.glp_trip_type_id, "
                    + "gts.ds_trip_type_id, "
                    + "gts.auto_trip_type_id, "
                    + "gt.save_cyl_trip, "
                    + "gt.save_tank_trip "
                    + "FROM gt_siesa_center gts "
                    + "INNER JOIN gt_center gt ON gts.gt_center_id = gt.id "
                    + "WHERE siesa = '" + location + "'").getRecord(con);
            if (data == null || data.length == 0) {
                throw new Exception("La localización " + location + " no existe.");
            }
            for (Object basic : data) {
                if (basic == null) {
                    throw new Exception("Configuración de localización " + location + " incompleta.");
                }
            }
            centerOrigId = MySQLQuery.getAsInteger(data[0]);
            enterpriseId = MySQLQuery.getAsInteger(data[1]);
            sTripTypeId = MySQLQuery.getAsInteger(data[2]);
            glpTripTypeId = MySQLQuery.getAsInteger(data[3]);
            dsTripTypeId = MySQLQuery.getAsInteger(data[4]);
            autoTripTypeId = MySQLQuery.getAsInteger(data[5]);
            saveCylTrip = MySQLQuery.getAsBoolean(data[6]);
            saveTankTrip = MySQLQuery.getAsBoolean(data[7]);
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
        return "Short description";
    }
}
