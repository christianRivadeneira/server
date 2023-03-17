package api.ord.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class OrdTankOrder extends BaseModel<OrdTankOrder> {
//inicio zona de reemplazo

    public Date day;
    public Date origDay;
    public Date origTime;
    public Date takenHour;
    public Date assigHour;
    public Date confirmDt;
    public Date confirmHour;
    public Date callDt;
    public Integer officeId;
    public int takenById;
    public Integer assigById;
    public Integer confirmedById;
    public Integer cancelledBy;
    public Integer enterpriseId;
    public Integer tankClientId;
    public Integer cancelCauseId;
    public String justif;
    public String complain;
    public Integer driverId;
    public Integer vehicleId;
    public Integer pollId;
    public boolean lost;
    public Integer channelId;
    public BigDecimal kgs;
    public Integer noPollId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "day",
            "orig_day",
            "orig_time",
            "taken_hour",
            "assig_hour",
            "confirm_dt",
            "confirm_hour",
            "call_dt",
            "office_id",
            "taken_by_id",
            "assig_by_id",
            "confirmed_by_id",
            "cancelled_by",
            "enterprise_id",
            "tank_client_id",
            "cancel_cause_id",
            "justif",
            "complain",
            "driver_id",
            "vehicle_id",
            "poll_id",
            "lost",
            "channel_id",
            "kgs",
            "no_poll_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, day);
        q.setParam(2, origDay);
        q.setParam(3, origTime);
        q.setParam(4, takenHour);
        q.setParam(5, assigHour);
        q.setParam(6, confirmDt);
        q.setParam(7, confirmHour);
        q.setParam(8, callDt);
        q.setParam(9, officeId);
        q.setParam(10, takenById);
        q.setParam(11, assigById);
        q.setParam(12, confirmedById);
        q.setParam(13, cancelledBy);
        q.setParam(14, enterpriseId);
        q.setParam(15, tankClientId);
        q.setParam(16, cancelCauseId);
        q.setParam(17, justif);
        q.setParam(18, complain);
        q.setParam(19, driverId);
        q.setParam(20, vehicleId);
        q.setParam(21, pollId);
        q.setParam(22, lost);
        q.setParam(23, channelId);
        q.setParam(24, kgs);
        q.setParam(25, noPollId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        day = MySQLQuery.getAsDate(row[0]);
        origDay = MySQLQuery.getAsDate(row[1]);
        origTime = MySQLQuery.getAsDate(row[2]);
        takenHour = MySQLQuery.getAsDate(row[3]);
        assigHour = MySQLQuery.getAsDate(row[4]);
        confirmDt = MySQLQuery.getAsDate(row[5]);
        confirmHour = MySQLQuery.getAsDate(row[6]);
        callDt = MySQLQuery.getAsDate(row[7]);
        officeId = MySQLQuery.getAsInteger(row[8]);
        takenById = MySQLQuery.getAsInteger(row[9]);
        assigById = MySQLQuery.getAsInteger(row[10]);
        confirmedById = MySQLQuery.getAsInteger(row[11]);
        cancelledBy = MySQLQuery.getAsInteger(row[12]);
        enterpriseId = MySQLQuery.getAsInteger(row[13]);
        tankClientId = MySQLQuery.getAsInteger(row[14]);
        cancelCauseId = MySQLQuery.getAsInteger(row[15]);
        justif = MySQLQuery.getAsString(row[16]);
        complain = MySQLQuery.getAsString(row[17]);
        driverId = MySQLQuery.getAsInteger(row[18]);
        vehicleId = MySQLQuery.getAsInteger(row[19]);
        pollId = MySQLQuery.getAsInteger(row[20]);
        lost = MySQLQuery.getAsBoolean(row[21]);
        channelId = MySQLQuery.getAsInteger(row[22]);
        kgs = MySQLQuery.getAsBigDecimal(row[23], false);
        noPollId = MySQLQuery.getAsInteger(row[24]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_tank_order";
    }

    public static String getSelFlds(String alias) {
        return new OrdTankOrder().getSelFldsForAlias(alias);
    }

    public static List<OrdTankOrder> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdTankOrder().getListFromQuery(q, conn);
    }

    public static List<OrdTankOrder> getList(Params p, Connection conn) throws Exception {
        return new OrdTankOrder().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdTankOrder().deleteById(id, conn);
    }

    public static List<OrdTankOrder> getAll(Connection conn) throws Exception {
        return new OrdTankOrder().getAllList(conn);
    }

//fin zona de reemplazo

}
