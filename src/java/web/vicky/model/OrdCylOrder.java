package web.vicky.model;

import java.sql.Connection;
import java.util.Date;
import utilities.MySQLQuery;

public class OrdCylOrder {
//inicio zona de reemplazo

    public int id;
    public Date day;
    public Date origDay;
    public Date origTime;
    public Date takenHour;
    public Date assigHour;
    public Date confirmDt;
    public Date confirmHour;
    public Date callDt;
    public int officeId;
    public int takenById;
    public Integer assigById;
    public Integer confirmedById;
    public Integer cancelledBy;
    public Integer enterpriseId;
    public int neighId;
    public Integer brandContractId;
    public Integer univContractId;
    public int indexId;
    public Integer cancelCauseId;
    public String justif;
    public String complain;
    public Integer driverId;
    public Integer vehicleId;
    public Integer pollId;
    public int dist;
    public boolean lost;
    public boolean sameSect;
    public boolean toPoll;
    public boolean called;
    public boolean delivered;
    public Integer channelId;
    public Integer priceSuggested;
    public Boolean waitToApp;
    public boolean appConfirmed;
    public boolean clieConfirmed;
    public boolean rate;
    public Integer complainId;

    private static final String SEL_FLDS = "`day`, "
            + "`orig_day`, "
            + "`orig_time`, "
            + "`taken_hour`, "
            + "`assig_hour`, "
            + "`confirm_dt`, "
            + "`confirm_hour`, "
            + "`call_dt`, "
            + "`office_id`, "
            + "`taken_by_id`, "
            + "`assig_by_id`, "
            + "`confirmed_by_id`, "
            + "`cancelled_by`, "
            + "`enterprise_id`, "
            + "`neigh_id`, "
            + "`brand_contract_id`, "
            + "`univ_contract_id`, "
            + "`index_id`, "
            + "`cancel_cause_id`, "
            + "`justif`, "
            + "`complain`, "
            + "`driver_id`, "
            + "`vehicle_id`, "
            + "`poll_id`, "
            + "`dist`, "
            + "`lost`, "
            + "`same_sect`, "
            + "`to_poll`, "
            + "`called`, "
            + "`delivered`, "
            + "`channel_id`, "
            + "`price_suggested`, "
            + "`wait_to_app`, "
            + "`app_confirmed`, "
            + "`clie_confirmed`, "
            + "`rate`, "
            + "`complain_id`";

    private static final String SET_FLDS = "ord_cyl_order SET "
            + "`day` = ?1, "
            + "`orig_day` = ?2, "
            + "`orig_time` = ?3, "
            + "`taken_hour` = ?4, "
            + "`assig_hour` = ?5, "
            + "`confirm_dt` = ?6, "
            + "`confirm_hour` = ?7, "
            + "`call_dt` = ?8, "
            + "`office_id` = ?9, "
            + "`taken_by_id` = ?10, "
            + "`assig_by_id` = ?11, "
            + "`confirmed_by_id` = ?12, "
            + "`cancelled_by` = ?13, "
            + "`enterprise_id` = ?14, "
            + "`neigh_id` = ?15, "
            + "`brand_contract_id` = ?16, "
            + "`univ_contract_id` = ?17, "
            + "`index_id` = ?18, "
            + "`cancel_cause_id` = ?19, "
            + "`justif` = ?20, "
            + "`complain` = ?21, "
            + "`driver_id` = ?22, "
            + "`vehicle_id` = ?23, "
            + "`poll_id` = ?24, "
            + "`dist` = ?25, "
            + "`lost` = ?26, "
            + "`same_sect` = ?27, "
            + "`to_poll` = ?28, "
            + "`called` = ?29, "
            + "`delivered` = ?30, "
            + "`channel_id` = ?31, "
            + "`price_suggested` = ?32, "
            + "`wait_to_app` = ?33, "
            + "`app_confirmed` = ?34, "
            + "`clie_confirmed` = ?35, "
            + "`rate` = ?36, "
            + "`complain_id` = ?37";

    private static void setFields(OrdCylOrder obj, MySQLQuery q) {
        q.setParam(1, obj.day);
        q.setParam(2, obj.origDay);
        q.setParam(3, obj.origTime);
        q.setParam(4, obj.takenHour);
        q.setParam(5, obj.assigHour);
        q.setParam(6, obj.confirmDt);
        q.setParam(7, obj.confirmHour);
        q.setParam(8, obj.callDt);
        q.setParam(9, obj.officeId);
        q.setParam(10, obj.takenById);
        q.setParam(11, obj.assigById);
        q.setParam(12, obj.confirmedById);
        q.setParam(13, obj.cancelledBy);
        q.setParam(14, obj.enterpriseId);
        q.setParam(15, obj.neighId);
        q.setParam(16, obj.brandContractId);
        q.setParam(17, obj.univContractId);
        q.setParam(18, obj.indexId);
        q.setParam(19, obj.cancelCauseId);
        q.setParam(20, obj.justif);
        q.setParam(21, obj.complain);
        q.setParam(22, obj.driverId);
        q.setParam(23, obj.vehicleId);
        q.setParam(24, obj.pollId);
        q.setParam(25, obj.dist);
        q.setParam(26, obj.lost);
        q.setParam(27, obj.sameSect);
        q.setParam(28, obj.toPoll);
        q.setParam(29, obj.called);
        q.setParam(30, obj.delivered);
        q.setParam(31, obj.channelId);
        q.setParam(32, obj.priceSuggested);
        q.setParam(33, obj.waitToApp);
        q.setParam(34, obj.appConfirmed);
        q.setParam(35, obj.clieConfirmed);
        q.setParam(36, obj.rate);
        q.setParam(37, obj.complainId);

    }

    public static OrdCylOrder getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        OrdCylOrder obj = new OrdCylOrder();
        obj.day = MySQLQuery.getAsDate(row[0]);
        obj.origDay = MySQLQuery.getAsDate(row[1]);
        obj.origTime = MySQLQuery.getAsDate(row[2]);
        obj.takenHour = MySQLQuery.getAsDate(row[3]);
        obj.assigHour = MySQLQuery.getAsDate(row[4]);
        obj.confirmDt = MySQLQuery.getAsDate(row[5]);
        obj.confirmHour = MySQLQuery.getAsDate(row[6]);
        obj.callDt = MySQLQuery.getAsDate(row[7]);
        obj.officeId = MySQLQuery.getAsInteger(row[8]);
        obj.takenById = MySQLQuery.getAsInteger(row[9]);
        obj.assigById = MySQLQuery.getAsInteger(row[10]);
        obj.confirmedById = MySQLQuery.getAsInteger(row[11]);
        obj.cancelledBy = MySQLQuery.getAsInteger(row[12]);
        obj.enterpriseId = MySQLQuery.getAsInteger(row[13]);
        obj.neighId = MySQLQuery.getAsInteger(row[14]);
        obj.brandContractId = MySQLQuery.getAsInteger(row[15]);
        obj.univContractId = MySQLQuery.getAsInteger(row[16]);
        obj.indexId = MySQLQuery.getAsInteger(row[17]);
        obj.cancelCauseId = MySQLQuery.getAsInteger(row[18]);
        obj.justif = MySQLQuery.getAsString(row[19]);
        obj.complain = MySQLQuery.getAsString(row[20]);
        obj.driverId = MySQLQuery.getAsInteger(row[21]);
        obj.vehicleId = MySQLQuery.getAsInteger(row[22]);
        obj.pollId = MySQLQuery.getAsInteger(row[23]);
        obj.dist = MySQLQuery.getAsInteger(row[24]);
        obj.lost = MySQLQuery.getAsBoolean(row[25]);
        obj.sameSect = MySQLQuery.getAsBoolean(row[26]);
        obj.toPoll = MySQLQuery.getAsBoolean(row[27]);
        obj.called = MySQLQuery.getAsBoolean(row[28]);
        obj.delivered = MySQLQuery.getAsBoolean(row[29]);
        obj.channelId = MySQLQuery.getAsInteger(row[30]);
        obj.priceSuggested = MySQLQuery.getAsInteger(row[31]);
        obj.waitToApp = MySQLQuery.getAsBoolean(row[32]);
        obj.appConfirmed = MySQLQuery.getAsBoolean(row[33]);
        obj.clieConfirmed = MySQLQuery.getAsBoolean(row[34]);
        obj.rate = MySQLQuery.getAsBoolean(row[35]);
        obj.complainId = MySQLQuery.getAsInteger(row[36]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public static OrdCylOrder select(int id, Connection ep) throws Exception {
        return OrdCylOrder.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(ep));
    }

    public int insert(OrdCylOrder pobj, Connection ep) throws Exception {
        OrdCylOrder obj = pobj;
        int nId = new MySQLQuery(OrdCylOrder.getInsertQuery(obj)).executeInsert(ep);
        obj.id = nId;
        return nId;
    }

    public void update(OrdCylOrder pobj, Connection ep) throws Exception {
        new MySQLQuery(OrdCylOrder.getUpdateQuery(pobj)).executeUpdate(ep);
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM ord_cyl_order WHERE id = " + id;
    }

    public static String getInsertQuery(OrdCylOrder obj) {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public static String getUpdateQuery(OrdCylOrder obj) {
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public void delete(int id, Connection ep) throws Exception {
        new MySQLQuery("DELETE FROM ord_cyl_order WHERE id = " + id).executeDelete(ep);
    }

}
