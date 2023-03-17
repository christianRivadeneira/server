package api.ord.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class OrdPqrTank extends BaseModel<OrdPqrTank> {
//inicio zona de reemplazo

    public String billNum;
    public Date registDate;
    public Date registHour;
    public int registBy;
    public Date arrivalDate;
    public Date attentionDate;
    public Date attentionHour;
    public Integer technicianId;
    public Integer clientId;
    public Integer buildId;
    public Integer reasonId;
    public Integer anulCauseId;
    public Integer satisPollId;
    public Integer pqrPollId;
    public int officeId;
    public int serial;
    public Integer enterpriseId;
    public Integer suiTransactId;
    public Integer suiCausalId;
    public Integer suiRtaId;
    public Integer suiNotifyId;
    public Integer subreasonId;
    public Integer channelId;
    public String notes;
    public String radOrfeo;
    public Integer noPollId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "bill_num",
            "regist_date",
            "regist_hour",
            "regist_by",
            "arrival_date",
            "attention_date",
            "attention_hour",
            "technician_id",
            "client_id",
            "build_id",
            "reason_id",
            "anul_cause_id",
            "satis_poll_id",
            "pqr_poll_id",
            "office_id",
            "serial",
            "enterprise_id",
            "sui_transact_id",
            "sui_causal_id",
            "sui_rta_id",
            "sui_notify_id",
            "subreason_id",
            "channel_id",
            "notes",
            "rad_orfeo",
            "no_poll_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, billNum);
        q.setParam(2, registDate);
        q.setParam(3, registHour);
        q.setParam(4, registBy);
        q.setParam(5, arrivalDate);
        q.setParam(6, attentionDate);
        q.setParam(7, attentionHour);
        q.setParam(8, technicianId);
        q.setParam(9, clientId);
        q.setParam(10, buildId);
        q.setParam(11, reasonId);
        q.setParam(12, anulCauseId);
        q.setParam(13, satisPollId);
        q.setParam(14, pqrPollId);
        q.setParam(15, officeId);
        q.setParam(16, serial);
        q.setParam(17, enterpriseId);
        q.setParam(18, suiTransactId);
        q.setParam(19, suiCausalId);
        q.setParam(20, suiRtaId);
        q.setParam(21, suiNotifyId);
        q.setParam(22, subreasonId);
        q.setParam(23, channelId);
        q.setParam(24, notes);
        q.setParam(25, radOrfeo);
        q.setParam(26, noPollId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        billNum = MySQLQuery.getAsString(row[0]);
        registDate = MySQLQuery.getAsDate(row[1]);
        registHour = MySQLQuery.getAsDate(row[2]);
        registBy = MySQLQuery.getAsInteger(row[3]);
        arrivalDate = MySQLQuery.getAsDate(row[4]);
        attentionDate = MySQLQuery.getAsDate(row[5]);
        attentionHour = MySQLQuery.getAsDate(row[6]);
        technicianId = MySQLQuery.getAsInteger(row[7]);
        clientId = MySQLQuery.getAsInteger(row[8]);
        buildId = MySQLQuery.getAsInteger(row[9]);
        reasonId = MySQLQuery.getAsInteger(row[10]);
        anulCauseId = MySQLQuery.getAsInteger(row[11]);
        satisPollId = MySQLQuery.getAsInteger(row[12]);
        pqrPollId = MySQLQuery.getAsInteger(row[13]);
        officeId = MySQLQuery.getAsInteger(row[14]);
        serial = MySQLQuery.getAsInteger(row[15]);
        enterpriseId = MySQLQuery.getAsInteger(row[16]);
        suiTransactId = MySQLQuery.getAsInteger(row[17]);
        suiCausalId = MySQLQuery.getAsInteger(row[18]);
        suiRtaId = MySQLQuery.getAsInteger(row[19]);
        suiNotifyId = MySQLQuery.getAsInteger(row[20]);
        subreasonId = MySQLQuery.getAsInteger(row[21]);
        channelId = MySQLQuery.getAsInteger(row[22]);
        notes = MySQLQuery.getAsString(row[23]);
        radOrfeo = MySQLQuery.getAsString(row[24]);
        noPollId = MySQLQuery.getAsInteger(row[25]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_pqr_tank";
    }

    public static String getSelFlds(String alias) {
        return new OrdPqrTank().getSelFldsForAlias(alias);
    }

    public static List<OrdPqrTank> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdPqrTank().getListFromQuery(q, conn);
    }

    public static List<OrdPqrTank> getList(Params p, Connection conn) throws Exception {
        return new OrdPqrTank().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdPqrTank().deleteById(id, conn);
    }

    public static List<OrdPqrTank> getAll(Connection conn) throws Exception {
        return new OrdPqrTank().getAllList(conn);
    }

//fin zona de reemplazo

}
