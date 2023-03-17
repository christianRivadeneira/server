package api.ord.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class OrdRepairs extends BaseModel<OrdRepairs> {
//inicio zona de reemplazo

    public int serial;
    public Date registDate;
    public Date registHour;
    public int registBy;
    public Integer indexId;
    public Integer clientId;
    public Integer buildId;
    public int reasonId;
    public Integer anulCauseId;
    public Integer pqrPollId;
    public int officeId;
    public Integer enterpriseId;
    public String netSuiRespType;
    public Integer netSuiRespMinutes;
    public Date confirmDate;
    public Date confirmTime;
    public Date cancelDate;
    public Integer satisPollId;
    public int technicianId;
    public Integer subreasonId;
    public Integer channelId;
    public boolean toPoll;
    public String notes;
    public String radOrfeo;
    public Integer pqrCylId;
    public Integer pqrTankId;
    public Integer noPollId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "serial",
            "regist_date",
            "regist_hour",
            "regist_by",
            "index_id",
            "client_id",
            "build_id",
            "reason_id",
            "anul_cause_id",
            "pqr_poll_id",
            "office_id",
            "enterprise_id",
            "net_sui_resp_type",
            "net_sui_resp_minutes",
            "confirm_date",
            "confirm_time",
            "cancel_date",
            "satis_poll_id",
            "technician_id",
            "subreason_id",
            "channel_id",
            "to_poll",
            "notes",
            "rad_orfeo",
            "pqr_cyl_id",
            "pqr_tank_id",
            "no_poll_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, serial);
        q.setParam(2, registDate);
        q.setParam(3, registHour);
        q.setParam(4, registBy);
        q.setParam(5, indexId);
        q.setParam(6, clientId);
        q.setParam(7, buildId);
        q.setParam(8, reasonId);
        q.setParam(9, anulCauseId);
        q.setParam(10, pqrPollId);
        q.setParam(11, officeId);
        q.setParam(12, enterpriseId);
        q.setParam(13, netSuiRespType);
        q.setParam(14, netSuiRespMinutes);
        q.setParam(15, confirmDate);
        q.setParam(16, confirmTime);
        q.setParam(17, cancelDate);
        q.setParam(18, satisPollId);
        q.setParam(19, technicianId);
        q.setParam(20, subreasonId);
        q.setParam(21, channelId);
        q.setParam(22, toPoll);
        q.setParam(23, notes);
        q.setParam(24, radOrfeo);
        q.setParam(25, pqrCylId);
        q.setParam(26, pqrTankId);
        q.setParam(27, noPollId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        serial = MySQLQuery.getAsInteger(row[0]);
        registDate = MySQLQuery.getAsDate(row[1]);
        registHour = MySQLQuery.getAsDate(row[2]);
        registBy = MySQLQuery.getAsInteger(row[3]);
        indexId = MySQLQuery.getAsInteger(row[4]);
        clientId = MySQLQuery.getAsInteger(row[5]);
        buildId = MySQLQuery.getAsInteger(row[6]);
        reasonId = MySQLQuery.getAsInteger(row[7]);
        anulCauseId = MySQLQuery.getAsInteger(row[8]);
        pqrPollId = MySQLQuery.getAsInteger(row[9]);
        officeId = MySQLQuery.getAsInteger(row[10]);
        enterpriseId = MySQLQuery.getAsInteger(row[11]);
        netSuiRespType = MySQLQuery.getAsString(row[12]);
        netSuiRespMinutes = MySQLQuery.getAsInteger(row[13]);
        confirmDate = MySQLQuery.getAsDate(row[14]);
        confirmTime = MySQLQuery.getAsDate(row[15]);
        cancelDate = MySQLQuery.getAsDate(row[16]);
        satisPollId = MySQLQuery.getAsInteger(row[17]);
        technicianId = MySQLQuery.getAsInteger(row[18]);
        subreasonId = MySQLQuery.getAsInteger(row[19]);
        channelId = MySQLQuery.getAsInteger(row[20]);
        toPoll = MySQLQuery.getAsBoolean(row[21]);
        notes = MySQLQuery.getAsString(row[22]);
        radOrfeo = MySQLQuery.getAsString(row[23]);
        pqrCylId = MySQLQuery.getAsInteger(row[24]);
        pqrTankId = MySQLQuery.getAsInteger(row[25]);
        noPollId = MySQLQuery.getAsInteger(row[26]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_repairs";
    }

    public static String getSelFlds(String alias) {
        return new OrdRepairs().getSelFldsForAlias(alias);
    }

    public static List<OrdRepairs> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdRepairs().getListFromQuery(q, conn);
    }

    public static List<OrdRepairs> getList(Params p, Connection conn) throws Exception {
        return new OrdRepairs().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdRepairs().deleteById(id, conn);
    }

    public static List<OrdRepairs> getAll(Connection conn) throws Exception {
        return new OrdRepairs().getAllList(conn);
    }

//fin zona de reemplazo

}
