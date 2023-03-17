package api.ord.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class OrdPqrOther extends BaseModel<OrdPqrOther> {
//inicio zona de reemplazo

    public Integer serial;
    public Date registDate;
    public Date registHour;
    public int registBy;
    public Integer indexId;
    public Integer clientId;
    public Integer buildId;
    public Integer reasonId;
    public Integer anulCauseId;
    public Integer pqrPollId;
    public int officeId;
    public Integer enterpriseId;
    public Date confirmDate;
    public Date cancelDate;
    public Integer suiTransactId;
    public Integer suiCausalId;
    public Integer suiRtaId;
    public Integer suiNotifyId;
    public Integer satisPollId;
    public String respName;
    public Integer subreasonId;
    public Integer storeId;
    public Integer respId;
    public String subject;
    public String description;
    public Integer channelId;
    public Date confirmHour;
    public Integer detCauseSui;
    public String subjectReason;
    public Boolean isAdmissible;
    public String radOrfeo;

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
            "confirm_date",
            "cancel_date",
            "sui_transact_id",
            "sui_causal_id",
            "sui_rta_id",
            "sui_notify_id",
            "satis_poll_id",
            "resp_name",
            "subreason_id",
            "store_id",
            "resp_id",
            "subject",
            "description",
            "channel_id",
            "confirm_hour",
            "det_cause_sui",
            "subject_reason",
            "is_admissible",
            "rad_orfeo"
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
        q.setParam(13, confirmDate);
        q.setParam(14, cancelDate);
        q.setParam(15, suiTransactId);
        q.setParam(16, suiCausalId);
        q.setParam(17, suiRtaId);
        q.setParam(18, suiNotifyId);
        q.setParam(19, satisPollId);
        q.setParam(20, respName);
        q.setParam(21, subreasonId);
        q.setParam(22, storeId);
        q.setParam(23, respId);
        q.setParam(24, subject);
        q.setParam(25, description);
        q.setParam(26, channelId);
        q.setParam(27, confirmHour);
        q.setParam(28, detCauseSui);
        q.setParam(29, subjectReason);
        q.setParam(30, isAdmissible);
        q.setParam(31, radOrfeo);
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
        confirmDate = MySQLQuery.getAsDate(row[12]);
        cancelDate = MySQLQuery.getAsDate(row[13]);
        suiTransactId = MySQLQuery.getAsInteger(row[14]);
        suiCausalId = MySQLQuery.getAsInteger(row[15]);
        suiRtaId = MySQLQuery.getAsInteger(row[16]);
        suiNotifyId = MySQLQuery.getAsInteger(row[17]);
        satisPollId = MySQLQuery.getAsInteger(row[18]);
        respName = MySQLQuery.getAsString(row[19]);
        subreasonId = MySQLQuery.getAsInteger(row[20]);
        storeId = MySQLQuery.getAsInteger(row[21]);
        respId = MySQLQuery.getAsInteger(row[22]);
        subject = MySQLQuery.getAsString(row[23]);
        description = MySQLQuery.getAsString(row[24]);
        channelId = MySQLQuery.getAsInteger(row[25]);
        confirmHour = MySQLQuery.getAsDate(row[26]);
        detCauseSui = MySQLQuery.getAsInteger(row[27]);
        subjectReason = MySQLQuery.getAsString(row[28]);
        isAdmissible = MySQLQuery.getAsBoolean(row[29]);
        radOrfeo = MySQLQuery.getAsString(row[30]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_pqr_other";
    }

    public static String getSelFlds(String alias) {
        return new OrdPqrOther().getSelFldsForAlias(alias);
    }

    public static List<OrdPqrOther> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdPqrOther().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdPqrOther().deleteById(id, conn);
    }

    public static List<OrdPqrOther> getAll(Connection conn) throws Exception {
        return new OrdPqrOther().getAllList(conn);
    }

//fin zona de reemplazo

}