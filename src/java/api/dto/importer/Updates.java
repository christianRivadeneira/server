package api.dto.importer;

import api.com.model.ComCfg;
import api.trk.model.TrkAnulSale;
import api.trk.model.TrkSale;
import api.trk.model.TrkSaleWarning;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilities.Dates;
import utilities.MySQLQuery;
import web.marketing.cylSales.Contract;
import web.marketing.cylSales.CylSales;
import web.marketing.cylSales.Cylinder;

public class Updates {

    public int inserts = 0;
    public int overwrites = 0;
    public int misses = 0;
    public int error = 0;
    public int warns = 0;
    public int noPayment = 0;

    private static void forgeTrkSale(MinMinas m, int dtoSaleId, ComCfg cfg, Connection conn) throws Exception {
        Integer trkSaleId = new MySQLQuery("SELECT id FROM trk_sale WHERE auth = ?1").setParam(1, m.aprovNumber + "").getAsInteger(conn);
        if (m.state != MinMinas.STATE_ERROR && trkSaleId == null) {
            TrkSale s = new TrkSale();
            s.empId = new MySQLQuery("SELECT id FROM employee WHERE document = ?1 AND active").setParam(1, m.salesman.document).getAsInteger(conn);
            TrkSaleWarning cylW = null;
            try {
                Cylinder.getCylinder(m.nif, true, true, false, null, s.empId, conn);
            } catch (Exception ex) {
                cylW = new TrkSaleWarning(0, true, ex);
            }

            CylSales.lockCyl(cfg.lockCylSale, m.cyl.id, conn);

            Contract contract = CylSales.getContract(m.clieDoc + "", null, null, conn);

            s.auth = m.aprovNumber + "";
            s.bill = m.bill.toString();
            s.cubeCylTypeId = m.cyl.cylTypeId;
            s.cubeNifF = m.cyl.nifF;
            s.cubeNifS = m.cyl.nifS;
            s.cubeNifY = m.cyl.nifY;
            s.cylinderId = m.cyl.id;
            s.date = m.dt;
            s.smanId = m.salesman.id;

            web.marketing.cylSales.TrkSale tmp = new web.marketing.cylSales.TrkSale();
            tmp.empId = s.empId;
            CylSales.setVhAndManager(tmp, conn);
            s.vehicleId = tmp.vehicleId;
            s.manId = tmp.manId;

            //s.gtTripId = 
            s.indexId = contract.indexId;

            s.lat = m.lat;
            s.lon = m.lon;
            CylSales.ZoneInfo zi = CylSales.getZoneInfo(s.lat, s.lon, true, conn);
            s.discount = 0;
            s.credit = false;
            s.courtesy = false;
            s.isSowing = false;
            s.danePobId = zi.danePobId;
            s.price = m.valueTotal;
            s.saleType = "sub";
            s.stratum = m.stratum;
            s.subsidy = m.subsidy;
            s.training = false;
            s.zone = zi.zone;
            s.insert(conn);
            CylSales.updateTripSale(s.empId, s.id, s.date, conn);

            new TrkSaleWarning(s.id, false, "Creado con datos de mme").insert(conn);
            if (cylW != null) {
                cylW.saleId = s.id;
                cylW.insert(conn);
            }

            try {
                CylSales.validatePrice(m.valueTotal, m.salesmanDoc, m.cyl.cylTypeId, m.cylType.name, false, null, conn);
            } catch (Exception ex) {
                Logger.getLogger(Updates.class.getName()).log(Level.INFO, "message", ex);
                new TrkSaleWarning(s.id, true, ex).insert(conn);
            }

            trkSaleId = s.id;
        }
        new MySQLQuery("UPDATE dto_sale SET trk_sale_id = ?1 WHERE id = ?2").setParam(1, trkSaleId).setParam(2, dtoSaleId).executeUpdate(conn);
    }

    private static int insertDtoSale(MinMinas m, int logId, Connection conn) throws Exception {
        int id = new MySQLQuery(m.getInsert(logId, conn)).executeInsert(conn);
        if (m.anulNotes != null && m.anulNotes.equals("ANULADO")) {
            TrkAnulSale as = new TrkAnulSale();
            as.anulDt = new Date();
            as.dtoSaleId = id;
            as.empId = 1;
            as.notes = m.anulNotes;
            as.insert(conn);
        }
        return id;
    }

    public static Updates processData(List<MinMinas> data, int logId, boolean otherDates, Connection conn) throws Exception {
        ComCfg cfg = new ComCfg().select(1, conn);
        //Verifica que no haya filas duplicadas en el archivo.
        for (int i = 0; i < data.size(); i++) {
            MinMinas d1 = data.get(i);
            for (int j = (i + 1); j < data.size(); j++) {
                MinMinas d2 = data.get(j);
                if (d1.clieDoc == d2.clieDoc && d1.dt.equals(d2.dt) && d1.valueTotal.equals(d2.valueTotal) && d1.subsidy.equals(d2.subsidy)) {
                    throw new Exception("El registro " + Dates.getDefaultFormat().format(d1.dt) + ", cliente " + d1.clieDoc + " está duplicado.");
                }
            }
        }

        Updates rta = new Updates();
        boolean origAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            PreparedStatement psSelect = conn.prepareStatement("SELECT id, state <> 'ok' AND auth_notes IS NULL AND causal_id IS NULL AND dto_liq_id IS NULL FROM dto_sale WHERE aprov_number = ?");

            for (int i = 0; i < data.size(); i++) {
                MinMinas m = data.get(i);
                psSelect.setInt(1, m.aprovNumber);
                Object[][] rowData = MySQLQuery.getRecords(psSelect.executeQuery());

                if (rowData.length == 0) {
                    int minasId = insertDtoSale(m, logId, conn);
                    forgeTrkSale(m, minasId, cfg, conn);
                    rta.inserts++;
                    rta.error += (m.state == MinMinas.STATE_ERROR ? 1 : 0);
                    rta.warns += (m.state == MinMinas.STATE_WARN ? 1 : 0);
                    rta.noPayment += (m.payment ? 0 : 1);
                } else {
                    int rowId = MySQLQuery.getAsInteger(rowData[0][0]);
                    boolean rowReplace = MySQLQuery.getAsBoolean(rowData[0][1]);
                    if (rowReplace) {
                        List<TrkAnulSale> as = TrkAnulSale.getList(new MySQLQuery("SELECT " + TrkAnulSale.getSelFlds("a") + " FROM trk_anul_sale a WHERE a.dto_sale_id = " + rowId), conn);
                        new MySQLQuery("DELETE FROM trk_anul_sale WHERE dto_sale_id = " + rowId).executeUpdate(conn);
                        new MySQLQuery("DELETE FROM dto_sale WHERE id = " + rowId).executeUpdate(conn);
                        int minasId = insertDtoSale(m, logId, conn);
                        for (int j = 0; j < as.size(); j++) {
                            TrkAnulSale a = as.get(j);
                            a.dtoSaleId = minasId;
                            a.insert(conn);
                        }
                        forgeTrkSale(m, minasId, cfg, conn);
                        rta.overwrites++;
                        rta.error += (m.state == MinMinas.STATE_ERROR ? 1 : 0);
                        rta.warns += (m.state == MinMinas.STATE_WARN ? 1 : 0);
                        rta.noPayment += (m.payment ? 0 : 1);
                    } else {
                        forgeTrkSale(m, rowId, cfg, conn);
                        rta.misses++;
                    }
                }
            }

            //por si algun vendedor cambia de centro durante el proceso de importación, pasa las ventas al nuevo vendedor y actualiza el centro
            new MySQLQuery("UPDATE "
                    + "dto_sale sale "
                    + "INNER JOIN dto_salesman old ON old.id = sale.salesman_id AND !old.active "
                    + "INNER JOIN dto_salesman new ON new.document = old.document AND new.active "
                    + "LEFT JOIN trk_anul_sale anul ON sale.id = anul.dto_sale_id "
                    + "SET sale.salesman_id = new.id "
                    + "WHERE "
                    + "sale.dto_liq_id IS NULL AND sale.hide_dt IS NULL "
                    + "AND anul.id IS NULL;").executeUpdate(conn);

            new MySQLQuery("UPDATE "
                    + "dto_sale sale  "
                    + "INNER JOIN dto_salesman sm ON sm.id = sale.salesman_id  "
                    + "LEFT JOIN trk_anul_sale anul ON sale.id = anul.dto_sale_id "
                    + "SET sale.center_id = sm.center_id "
                    + "WHERE  "
                    + "sale.dto_liq_id IS NULL AND sale.hide_dt IS NULL AND anul.id IS NULL "
                    + "AND sm.center_id <> sale.center_id").executeUpdate(conn);

            conn.commit();
            return rta;
        } catch (Exception ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(origAutoCommit);
        }
    }

    public static String getCountType(Object[][] row, Date dateMinMinas) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (row == null || row.length == 0) {
            return "nodb";
        }
        Date usedDate = MySQLQuery.getAsDate(row[0][0]);
        if (usedDate != null) {
            if (Dates.trimDate(usedDate).compareTo(Dates.trimDate(sdf.parse(sdf.format(dateMinMinas)))) == 0) {
                return MySQLQuery.getAsString(row[0][1]);
            } else {
                return "unlocked";
            }
        } else {
            return "unlocked";
        }
    }
}
