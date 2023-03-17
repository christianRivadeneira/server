package web.tanks;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import utilities.MySQLQuery;

public class EstSale {

    /**
     * @return the driverId
     */
    public int getDriverId() {
        return driverId;
    }

    /**
     * @param driverId the driverId to set
     */
    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    //----------------- Fuera de BD ------------------
    public int movId;

//inicio zona de reemplazo
    public Integer id;
    public Date saleDate;
    public int clientId;
    public String billNum;
    public BigDecimal kgs;
    public BigDecimal gls;
    public int createdId;
    public Integer driverId;
    public int modifiedId;
    public Date createdDate;
    public Date modifiedDate;
    public BigDecimal total;
    public BigDecimal unitPrice;
    public BigDecimal vlrNeto;
    public BigDecimal unitKgPrice;
    public BigDecimal kte;
    public String consec1;
    public String consec2;
    public Integer vhId;
    public Boolean lastImport;
    public Integer estTankId;
    public String source;
    public String billType;
    public boolean isCredit;
    public String notes;
    public Integer tripId;
    public Boolean cancel;
    public Integer salePayId;
    public Date saleClose;
    public BigDecimal lat;
    public BigDecimal lon;
    public Boolean hasPhotos;
    public Integer execId;
    public Integer percBegin;
    public Integer percEnd;

    private static final String SEL_FLDS = "`sale_date`, "
            + "`client_id`, "
            + "`bill_num`, "
            + "`kgs`, "
            + "`gls`, "
            + "`created_id`, "
            + "`driver_id`, "
            + "`modified_id`, "
            + "`created_date`, "
            + "`modified_date`, "
            + "`total`, "
            + "`unit_price`, "
            + "`vlr_neto`, "
            + "`unit_kg_price`, "
            + "`kte`, "
            + "`consec1`, "
            + "`consec2`, "
            + "`vh_id`, "
            + "`last_import`, "
            + "`est_tank_id`, "
            + "`source`, "
            + "`bill_type`, "
            + "`is_credit`, "
            + "`notes`, "
            + "`trip_id`, "
            + "`cancel`, "
            + "`sale_pay_id`, "
            + "`sale_close`, "
            + "`lat`, "
            + "`lon`, "
            + "`has_photos`, "
            + "`exec_id`, "
            + "`perc_begin`, "
            + "`perc_end`";

    private static final String SET_FLDS = "est_sale SET "
            + "`sale_date` = ?1, "
            + "`client_id` = ?2, "
            + "`bill_num` = ?3, "
            + "`kgs` = ?4, "
            + "`gls` = ?5, "
            + "`created_id` = ?6, "
            + "`driver_id` = ?7, "
            + "`modified_id` = ?8, "
            + "`created_date` = ?9, "
            + "`modified_date` = ?10, "
            + "`total` = ?11, "
            + "`unit_price` = ?12, "
            + "`vlr_neto` = ?13, "
            + "`unit_kg_price` = ?14, "
            + "`kte` = ?15, "
            + "`consec1` = ?16, "
            + "`consec2` = ?17, "
            + "`vh_id` = ?18, "
            + "`last_import` = ?19, "
            + "`est_tank_id` = ?20, "
            + "`source` = ?21, "
            + "`bill_type` = ?22, "
            + "`is_credit` = ?23, "
            + "`notes` = ?24, "
            + "`trip_id` = ?25, "
            + "`cancel` = ?26, "
            + "`sale_pay_id` = ?27, "
            + "`sale_close` = ?28, "
            + "`lat` = ?29, "
            + "`lon` = ?30, "
            + "`has_photos` = ?31, "
            + "`exec_id` = ?32, "
            + "`perc_begin` = ?33, "
            + "`perc_end` = ?34";

    private static void setFields(EstSale obj, MySQLQuery q) {
        q.setParam(1, obj.saleDate);
        q.setParam(2, obj.clientId);
        q.setParam(3, obj.billNum);
        q.setParam(4, obj.kgs);
        q.setParam(5, obj.gls);
        q.setParam(6, obj.createdId);
        q.setParam(7, obj.getDriverId());
        q.setParam(8, obj.modifiedId);
        q.setParam(9, obj.createdDate);
        q.setParam(10, obj.modifiedDate);
        q.setParam(11, obj.total);
        q.setParam(12, obj.unitPrice);
        q.setParam(13, obj.vlrNeto);
        q.setParam(14, obj.unitKgPrice);
        q.setParam(15, obj.kte);
        q.setParam(16, obj.consec1);
        q.setParam(17, obj.consec2);
        q.setParam(18, obj.vhId);
        q.setParam(19, obj.lastImport);
        q.setParam(20, obj.estTankId);
        q.setParam(21, obj.source);
        q.setParam(22, obj.billType);
        q.setParam(23, obj.isCredit);
        q.setParam(24, obj.notes);
        q.setParam(25, obj.tripId);
        q.setParam(26, obj.cancel);
        q.setParam(27, obj.salePayId);
        q.setParam(28, obj.saleClose);
        q.setParam(29, obj.lat);
        q.setParam(30, obj.lon);
        q.setParam(31, obj.hasPhotos);
        q.setParam(32, obj.execId);
        q.setParam(33, obj.percBegin);
        q.setParam(34, obj.percEnd);

    }

    public static EstSale getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        EstSale obj = new EstSale();
        obj.saleDate = MySQLQuery.getAsDate(row[0]);
        obj.clientId = MySQLQuery.getAsInteger(row[1]);
        obj.billNum = MySQLQuery.getAsString(row[2]);
        obj.kgs = MySQLQuery.getAsBigDecimal(row[3], false);
        obj.gls = MySQLQuery.getAsBigDecimal(row[4], false);
        obj.createdId = MySQLQuery.getAsInteger(row[5]);
        obj.setDriverId((int) MySQLQuery.getAsInteger(row[6]));
        obj.modifiedId = MySQLQuery.getAsInteger(row[7]);
        obj.createdDate = MySQLQuery.getAsDate(row[8]);
        obj.modifiedDate = MySQLQuery.getAsDate(row[9]);
        obj.total = MySQLQuery.getAsBigDecimal(row[10], false);
        obj.unitPrice = MySQLQuery.getAsBigDecimal(row[11], false);
        obj.vlrNeto = MySQLQuery.getAsBigDecimal(row[12], false);
        obj.unitKgPrice = MySQLQuery.getAsBigDecimal(row[13], false);
        obj.kte = MySQLQuery.getAsBigDecimal(row[14], false);
        obj.consec1 = MySQLQuery.getAsString(row[15]);
        obj.consec2 = MySQLQuery.getAsString(row[16]);
        obj.vhId = MySQLQuery.getAsInteger(row[17]);
        obj.lastImport = MySQLQuery.getAsBoolean(row[18]);
        obj.estTankId = MySQLQuery.getAsInteger(row[19]);
        obj.source = MySQLQuery.getAsString(row[20]);
        obj.billType = MySQLQuery.getAsString(row[21]);
        obj.isCredit = MySQLQuery.getAsBoolean(row[22]);
        obj.notes = MySQLQuery.getAsString(row[23]);
        obj.tripId = MySQLQuery.getAsInteger(row[24]);
        obj.cancel = MySQLQuery.getAsBoolean(row[25]);
        obj.salePayId = MySQLQuery.getAsInteger(row[26]);
        obj.saleClose = MySQLQuery.getAsDate(row[27]);
        obj.lat = MySQLQuery.getAsBigDecimal(row[28], false);
        obj.lon = MySQLQuery.getAsBigDecimal(row[29], false);
        obj.hasPhotos = MySQLQuery.getAsBoolean(row[30]);
        obj.execId = MySQLQuery.getAsInteger(row[31]);
        obj.percBegin = MySQLQuery.getAsInteger(row[32]);
        obj.percEnd = MySQLQuery.getAsInteger(row[33]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public EstSale select(int id, Connection ep) throws Exception {
        return EstSale.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(ep));
    }

    public int insert(EstSale obj, Connection ep) throws Exception {
        int nId = new MySQLQuery(EstSale.getInsertQuery(obj)).executeInsert(ep);
        obj.id = nId;
        return nId;
    }

    public void update(EstSale pobj, Connection ep) throws Exception {
        new MySQLQuery(EstSale.getUpdateQuery(pobj)).executeUpdate(ep);
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM est_sale WHERE id = " + id;
    }

    public static String getInsertQuery(EstSale obj) {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public static String getUpdateQuery(EstSale obj) {
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public void delete(int id, Connection ep) throws Exception {
        new MySQLQuery("DELETE FROM est_sale WHERE id = " + id).executeDelete(ep);
    }

    public String getEnumOptions(String fieldName) {
        if (fieldName.equals("source")) {
            return "mob=mob&dig=dig&imp=imp";
        }
        if (fieldName.equals("bill_type")) {
            return "cort=cort&fac=fac&rem=rem&cntr=cntr";
        }
        return null;
    }
}
