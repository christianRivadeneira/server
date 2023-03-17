package api.trk.model;

import api.BaseModel;
import api.dto.model.DtoSale;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import utilities.MySQLQuery;
import web.marketing.cylSales.CylSales;

public class TrkSale extends BaseModel<TrkSale> {
//inicio zona de reemplazo

    public Date date;
    public Integer indexId;
    public Integer cylinderId;
    public Integer empId;
    public Integer smanId;
    public String bill;
    public Integer price;
    public Integer subsidy;
    public String auth;
    public String saleType;
    public BigDecimal lat;
    public BigDecimal lon;
    public Integer danePobId;
    public Integer cylReceivedId;
    public Integer stratum;
    public String zone;
    public Integer liqId;
    public boolean isSowing;
    public Integer vehicleId;
    public Integer manId;
    public boolean credit;
    public boolean courtesy;
    public Integer discount;
    public Integer cubeCylTypeId;
    public boolean training;
    public Integer cubeNifY;
    public Integer cubeNifF;
    public Integer cubeNifS;
    public Date hideDt;
    public Integer gtTripId;
    public String bonusCode;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "date",
            "index_id",
            "cylinder_id",
            "emp_id",
            "sman_id",
            "bill",
            "price",
            "subsidy",
            "auth",
            "sale_type",
            "lat",
            "lon",
            "dane_pob_id",
            "cyl_received_id",
            "stratum",
            "zone",
            "liq_id",
            "is_sowing",
            "vehicle_id",
            "man_id",
            "credit",
            "courtesy",
            "discount",
            "cube_cyl_type_id",
            "training",
            "cube_nif_y",
            "cube_nif_f",
            "cube_nif_s",
            "hide_dt",
            "gt_trip_id",
            "bonus_code"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, date);
        q.setParam(2, indexId);
        q.setParam(3, cylinderId);
        q.setParam(4, empId);
        q.setParam(5, smanId);
        q.setParam(6, bill);
        q.setParam(7, price);
        q.setParam(8, subsidy);
        q.setParam(9, auth);
        q.setParam(10, saleType);
        q.setParam(11, lat);
        q.setParam(12, lon);
        q.setParam(13, danePobId);
        q.setParam(14, cylReceivedId);
        q.setParam(15, stratum);
        q.setParam(16, zone);
        q.setParam(17, liqId);
        q.setParam(18, isSowing);
        q.setParam(19, vehicleId);
        q.setParam(20, manId);
        q.setParam(21, credit);
        q.setParam(22, courtesy);
        q.setParam(23, discount);
        q.setParam(24, cubeCylTypeId);
        q.setParam(25, training);
        q.setParam(26, cubeNifY);
        q.setParam(27, cubeNifF);
        q.setParam(28, cubeNifS);
        q.setParam(29, hideDt);
        q.setParam(30, gtTripId);
        q.setParam(31, bonusCode);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        date = MySQLQuery.getAsDate(row[0]);
        indexId = MySQLQuery.getAsInteger(row[1]);
        cylinderId = MySQLQuery.getAsInteger(row[2]);
        empId = MySQLQuery.getAsInteger(row[3]);
        smanId = MySQLQuery.getAsInteger(row[4]);
        bill = MySQLQuery.getAsString(row[5]);
        price = MySQLQuery.getAsInteger(row[6]);
        subsidy = MySQLQuery.getAsInteger(row[7]);
        auth = MySQLQuery.getAsString(row[8]);
        saleType = MySQLQuery.getAsString(row[9]);
        lat = MySQLQuery.getAsBigDecimal(row[10], false);
        lon = MySQLQuery.getAsBigDecimal(row[11], false);
        danePobId = MySQLQuery.getAsInteger(row[12]);
        cylReceivedId = MySQLQuery.getAsInteger(row[13]);
        stratum = MySQLQuery.getAsInteger(row[14]);
        zone = MySQLQuery.getAsString(row[15]);
        liqId = MySQLQuery.getAsInteger(row[16]);
        isSowing = MySQLQuery.getAsBoolean(row[17]);
        vehicleId = MySQLQuery.getAsInteger(row[18]);
        manId = MySQLQuery.getAsInteger(row[19]);
        credit = MySQLQuery.getAsBoolean(row[20]);
        courtesy = MySQLQuery.getAsBoolean(row[21]);
        discount = MySQLQuery.getAsInteger(row[22]);
        cubeCylTypeId = MySQLQuery.getAsInteger(row[23]);
        training = MySQLQuery.getAsBoolean(row[24]);
        cubeNifY = MySQLQuery.getAsInteger(row[25]);
        cubeNifF = MySQLQuery.getAsInteger(row[26]);
        cubeNifS = MySQLQuery.getAsInteger(row[27]);
        hideDt = MySQLQuery.getAsDate(row[28]);
        gtTripId = MySQLQuery.getAsInteger(row[29]);
        bonusCode = MySQLQuery.getAsString(row[30]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "trk_sale";
    }

    public static String getSelFlds(String alias) {
        return new TrkSale().getSelFldsForAlias(alias);
    }

    public static List<TrkSale> getList(MySQLQuery q, Connection conn) throws Exception {
        return new TrkSale().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new TrkSale().deleteById(id, conn);
    }

    public static List<TrkSale> getAll(Connection conn) throws Exception {
        return new TrkSale().getAllList(conn);
    }

//fin zona de reemplazo

    public static Date[] getDayLimits(Date d) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(d);
        gc.set(GregorianCalendar.HOUR_OF_DAY, 0);
        gc.set(GregorianCalendar.MINUTE, 0);
        gc.set(GregorianCalendar.SECOND, 0);
        gc.set(GregorianCalendar.MILLISECOND, 0);
        Date beg = gc.getTime();
        gc.add(GregorianCalendar.DAY_OF_MONTH, 1);
        Date end = gc.getTime();
        return new Date[]{beg, end};
    }

    public static void createFromDtoSale(Connection conn, int year, int month) throws Exception {
        GregorianCalendar gc = new GregorianCalendar();
        gc.set(GregorianCalendar.YEAR, year);
        gc.set(GregorianCalendar.MONTH, month - 1);
        gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
        gc.set(GregorianCalendar.HOUR_OF_DAY, 0);
        gc.set(GregorianCalendar.MINUTE, 0);
        gc.set(GregorianCalendar.SECOND, 0);
        gc.set(GregorianCalendar.MILLISECOND, 0);
        Date beg = gc.getTime();
        gc.add(GregorianCalendar.MONTH, 1);
        Date end = gc.getTime();

        //MySQLQuery.DUMP_QUERIES = true;
        //qué otros campos colocar aquí?
        MySQLQuery q = new MySQLQuery("SELECT " + DtoSale.getSelFlds("s") + " FROM "
                + "dto_sale s "
                + "LEFT JOIN trk_anul_sale ans ON ans.dto_sale_id = s.id "
                + "WHERE "
                //+ "s.id = 8607602 and "
                + "ans.id IS NULL AND s.dt BETWEEN ?1 AND ?2 AND s.trk_sale_id IS NULL AND hide_dt IS NULL "
        //  + "LIMIT 1;"
        ).setParam(1, beg).setParam(2, end);

        List<DtoSale> dSales = DtoSale.getList(q, conn);

        for (int i = 0; i < dSales.size(); i++) {
            DtoSale ds = dSales.get(i);

            gc.setTime(ds.dt);
            gc.add(GregorianCalendar.DAY_OF_MONTH, -5);
            beg = gc.getTime();

            // gc.setTime(ds.dt);
            gc.add(GregorianCalendar.DAY_OF_MONTH, 10);
            end = gc.getTime();

            MySQLQuery qt = new MySQLQuery("SELECT " + TrkTransaction.getSelFlds("t") + " FROM "
                    + "trk_transaction t "
                    + "INNER JOIN trk_cyl c ON t.cyl_id = c.id AND c.cyl_type_id = ?3 "
                    + "INNER JOIN employee e ON e.id = t.emp_id "
                    + "WHERE "
                    + "t.bill  = ?1 AND "
                    + "t.document = ?2 AND "
                    + "t.dt BETWEEN ?4 AND ?5 AND "
                    + "e.document = (SELECT document FROM dto_salesman WHERE id = ?6) AND "
                    + "t.val_sub = ?7 AND "
                    + "t.price = ?8 AND error1 IS NULL "
                    //+ "AND error2 IS NULL "
                    + "AND lat IS NOT NULL ORDER BY t.dt DESC limit 1");
            qt.setParam(1, ds.bill + "");
            qt.setParam(2, ds.clieDoc);
            qt.setParam(3, ds.cylTypeId);
            qt.setParam(4, beg);
            qt.setParam(5, end);
            qt.setParam(6, ds.salesmanId);
            qt.setParam(7, ds.subsidy);
            qt.setParam(8, ds.valueTotal);

            List<TrkTransaction> tsl = TrkTransaction.getList(qt, conn);

            if (tsl != null && tsl.size() == 1) {
                TrkTransaction ts = tsl.get(0);
                TrkSale s = new TrkSale();
                TrkCyl cyl = new TrkCyl().select(ts.cylId, conn);

                s.auth = ds.aprovNumber + "";
                s.bill = ts.bill;
                s.cubeCylTypeId = cyl.cylTypeId;
                s.cubeNifF = cyl.nifF;
                s.cubeNifS = cyl.nifS;
                s.cubeNifY = cyl.nifY;
                s.cylinderId = cyl.id;
                s.date = ds.dt;
                s.empId = ts.empId;
                s.liqId = ds.dtoLiqId;
                //s.gtTripId = 
                s.indexId = ts.indexId;

                s.lat = new BigDecimal(ts.lat);
                s.lon = new BigDecimal(ts.lon);
                CylSales.ZoneInfo zi = CylSales.getZoneInfo(s.lat, s.lon, true, conn);
                if (zi != null) {

                    s.danePobId = zi.danePobId;
                    s.price = ts.price;
                    s.saleType = "sub";
                    s.stratum = 1;
                    s.subsidy = ds.subsidy;
                    s.training = false;

                    MySQLQuery qv = new MySQLQuery("SELECT vehicle_id FROM trk_sale WHERE date BETWEEN ?1 AND ?2 AND emp_id = ?3 AND vehicle_id IS NOT NULL LIMIT 1");
                    Date[] dayLimits = getDayLimits(s.date);
                    qv.setParam(1, dayLimits[0]);
                    qv.setParam(2, dayLimits[1]);
                    qv.setParam(3, s.empId);

                    s.vehicleId = qv.getAsInteger(conn);

                    s.zone = zi.zone;

                    MySQLQuery sq = new MySQLQuery("SELECT id FROM trk_sale WHERE auth = ?1 AND bill = ?2 AND cube_cyl_type_id = ?3 AND emp_id = ?4 and index_id = ?5 and subsidy = ?6");
                    sq.setParam(1, s.auth);
                    sq.setParam(2, s.bill);
                    sq.setParam(3, s.cubeCylTypeId);
                    sq.setParam(4, s.empId);
                    sq.setParam(5, s.indexId);
                    sq.setParam(6, s.subsidy);

                    Integer saleId = sq.getAsInteger(conn);
                    if (saleId == null) {
                        s.insert(conn);
                        TrkSaleWarning w = new TrkSaleWarning();
                        w.dt = new Date();
                        w.lockLiq = false;
                        w.saleId = s.id;
                        w.warning = "Creado usando la transacción";
                        w.insert(conn);
                        ds.trkSaleId = s.id;
                        ds.update(conn);
                    } else {
                        ds.trkSaleId = saleId;
                        ds.update(conn);
                    }
                }
            }
        }
    }
}
