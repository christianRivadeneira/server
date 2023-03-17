package web.marketing.cylSales;

import api.trk.model.TrkSaleWarning;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class TrkSale implements WarningList {

    public String dtoPrice; //valor del bono de descuento, para efectos de la notificación a un ganador.
    public String phones;
    public List<String> lstWarns;

//inicio zona de reemplazo
    public int id;
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
    public Integer discount; //Este descuento no se maneja como el normal de promociones. Éste es solo valor monetario, el otro va como premio, éste afecta la liquidación.
    public Integer cubeCylTypeId;
    public boolean training;
    public Integer cubeNifY;
    public Integer cubeNifF;
    public Integer cubeNifS;
    public Date hideDt;
    public Integer gtTripId;
    public String bonusCode;
    public int promLastSales;

    private static final String SEL_FLDS = "`date`, "
            + "`index_id`, "
            + "`cylinder_id`, "
            + "`emp_id`, "
            + "`sman_id`, "
            + "`bill`, "
            + "`price`, "
            + "`subsidy`, "
            + "`auth`, "
            + "`sale_type`, "
            + "`lat`, "
            + "`lon`, "
            + "`dane_pob_id`, "
            + "`cyl_received_id`, "
            + "`stratum`, "
            + "`zone`, "
            + "`liq_id`, "
            + "`is_sowing`, "
            + "`vehicle_id`, "
            + "`man_id`, "
            + "`credit`, "
            + "`courtesy`, "
            + "`discount`, "
            + "`cube_cyl_type_id`, "
            + "`training`, "
            + "`cube_nif_y`, "
            + "`cube_nif_f`, "
            + "`cube_nif_s`, "
            + "`hide_dt`, "
            + "`gt_trip_id`, "
            + "`bonus_code`, "
            + "`prom_last_sales`";

    private static final String SET_FLDS = "trk_sale SET "
            + "`date` = ?1, "
            + "`index_id` = ?2, "
            + "`cylinder_id` = ?3, "
            + "`emp_id` = ?4, "
            + "`sman_id` = ?5, "
            + "`bill` = ?6, "
            + "`price` = ?7, "
            + "`subsidy` = ?8, "
            + "`auth` = ?9, "
            + "`sale_type` = ?10, "
            + "`lat` = ?11, "
            + "`lon` = ?12, "
            + "`dane_pob_id` = ?13, "
            + "`cyl_received_id` = ?14, "
            + "`stratum` = ?15, "
            + "`zone` = ?16, "
            + "`liq_id` = ?17, "
            + "`is_sowing` = ?18, "
            + "`vehicle_id` = ?19, "
            + "`man_id` = ?20, "
            + "`credit` = ?21, "
            + "`courtesy` = ?22, "
            + "`discount` = ?23, "
            + "`cube_cyl_type_id` = ?24, "
            + "`training` = ?25, "
            + "`cube_nif_y` = ?26, "
            + "`cube_nif_f` = ?27, "
            + "`cube_nif_s` = ?28, "
            + "`hide_dt` = ?29, "
            + "`gt_trip_id` = ?30, "
            + "`bonus_code` = ?31, "
            + "`prom_last_sales` = ?32";

    private static void setFields(TrkSale obj, MySQLQuery q) {
        q.setParam(1, obj.date);
        q.setParam(2, obj.indexId);
        q.setParam(3, obj.cylinderId);
        q.setParam(4, obj.empId);
        q.setParam(5, obj.smanId);
        q.setParam(6, obj.bill);
        q.setParam(7, obj.price);
        q.setParam(8, obj.subsidy);
        q.setParam(9, obj.auth);
        q.setParam(10, obj.saleType);
        q.setParam(11, obj.lat);
        q.setParam(12, obj.lon);
        q.setParam(13, obj.danePobId);
        q.setParam(14, obj.cylReceivedId);
        q.setParam(15, obj.stratum);
        q.setParam(16, obj.zone);
        q.setParam(17, obj.liqId);
        q.setParam(18, obj.isSowing);
        q.setParam(19, obj.vehicleId);
        q.setParam(20, obj.manId);
        q.setParam(21, obj.credit);
        q.setParam(22, obj.courtesy);
        q.setParam(23, obj.discount);
        q.setParam(24, obj.cubeCylTypeId);
        q.setParam(25, obj.training);
        q.setParam(26, obj.cubeNifY);
        q.setParam(27, obj.cubeNifF);
        q.setParam(28, obj.cubeNifS);
        q.setParam(29, obj.hideDt);
        q.setParam(30, obj.gtTripId);
        q.setParam(31, obj.bonusCode);
        q.setParam(32, obj.promLastSales);

    }

    public TrkSale select(int id, Connection ep) throws Exception {
        TrkSale obj = new TrkSale();
        MySQLQuery q = new MySQLQuery("SELECT " + SEL_FLDS + " FROM trk_sale WHERE id = " + id);
        Object[] row = q.getRecord(ep);
        obj.date = MySQLQuery.getAsDate(row[0]);
        obj.indexId = MySQLQuery.getAsInteger(row[1]);
        obj.cylinderId = MySQLQuery.getAsInteger(row[2]);
        obj.empId = MySQLQuery.getAsInteger(row[3]);
        obj.smanId = MySQLQuery.getAsInteger(row[4]);
        obj.bill = MySQLQuery.getAsString(row[5]);
        obj.price = MySQLQuery.getAsInteger(row[6]);
        obj.subsidy = MySQLQuery.getAsInteger(row[7]);
        obj.auth = MySQLQuery.getAsString(row[8]);
        obj.saleType = MySQLQuery.getAsString(row[9]);
        obj.lat = MySQLQuery.getAsBigDecimal(row[10], false);
        obj.lon = MySQLQuery.getAsBigDecimal(row[11], false);
        obj.danePobId = MySQLQuery.getAsInteger(row[12]);
        obj.cylReceivedId = MySQLQuery.getAsInteger(row[13]);
        obj.stratum = MySQLQuery.getAsInteger(row[14]);
        obj.zone = MySQLQuery.getAsString(row[15]);
        obj.liqId = MySQLQuery.getAsInteger(row[16]);
        obj.isSowing = MySQLQuery.getAsBoolean(row[17]);
        obj.vehicleId = MySQLQuery.getAsInteger(row[18]);
        obj.manId = MySQLQuery.getAsInteger(row[19]);
        obj.credit = MySQLQuery.getAsBoolean(row[20]);
        obj.courtesy = MySQLQuery.getAsBoolean(row[21]);
        obj.discount = MySQLQuery.getAsInteger(row[22]);
        obj.cubeCylTypeId = MySQLQuery.getAsInteger(row[23]);
        obj.training = MySQLQuery.getAsBoolean(row[24]);
        obj.cubeNifY = MySQLQuery.getAsInteger(row[25]);
        obj.cubeNifF = MySQLQuery.getAsInteger(row[26]);
        obj.cubeNifS = MySQLQuery.getAsInteger(row[27]);
        obj.hideDt = MySQLQuery.getAsDate(row[28]);
        obj.gtTripId = MySQLQuery.getAsInteger(row[29]);
        obj.bonusCode = MySQLQuery.getAsString(row[30]);
        obj.promLastSales = MySQLQuery.getAsInteger(row[31]);

        obj.id = id;
        return obj;
    }

//fin zona de reemplazo
    public int insert(TrkSale pobj, Connection ep) throws Exception {
        TrkSale obj = (TrkSale) pobj;
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.executeInsert(ep);
    }

    public void update(TrkSale pobj, Connection ep) throws Exception {
        TrkSale obj = (TrkSale) pobj;
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        q.executeUpdate(ep);
    }

    public void delete(int id, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("DELETE FROM trk_sale WHERE id = " + id);
        q.executeDelete(ep);
    }

    @Override
    public void addWarn(String warn) {
        this.lstWarns.add(warn);
    }

    public void saveWarns(Connection conn) throws Exception {
        if (lstWarns != null && lstWarns.size() > 0) {
            for (int i = 0; i < lstWarns.size(); i++) {
                TrkSaleWarning warn = new TrkSaleWarning();
                warn.saleId = id;
                warn.lockLiq = true;
                warn.dt = new Date();
                warn.warning = lstWarns.get(i);
                warn.insert(conn);
            }
        }
    }
}
