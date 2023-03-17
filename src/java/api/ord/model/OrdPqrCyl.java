package api.ord.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class OrdPqrCyl extends BaseModel<OrdPqrCyl> {
//inicio zona de reemplazo

    public String billNum;
    public Date creationDate;
    public Date registHour;
    public int registBy;
    public Date arrivalDate;
    public Date attentionDate;
    public Date attentionHour;
    public int technicianId;
    public int indexId;
    public Integer pqrAnulCauseId;
    public Integer satisPollId;
    public Integer pqrPollId;
    public int officeId;
    public int serial;
    public Integer enterpriseId;
    public int pqrReason;
    public Integer suiTransactId;
    public Integer suiCausalId;
    public Integer suiRtaId;
    public Integer suiNotifyId;
    public Integer pqrSubreason;
    public Integer channelId;
    public String notes;
    public String radOrfeo;
    public Integer noPollId;
    public String nif;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "bill_num",
            "creation_date",
            "regist_hour",
            "regist_by",
            "arrival_date",
            "attention_date",
            "attention_hour",
            "technician_id",
            "index_id",
            "pqr_anul_cause_id",
            "satis_poll_id",
            "pqr_poll_id",
            "office_id",
            "serial",
            "enterprise_id",
            "pqr_reason",
            "sui_transact_id",
            "sui_causal_id",
            "sui_rta_id",
            "sui_notify_id",
            "pqr_subreason",
            "channel_id",
            "notes",
            "rad_orfeo",
            "no_poll_id",
            "nif"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, billNum);
        q.setParam(2, creationDate);
        q.setParam(3, registHour);
        q.setParam(4, registBy);
        q.setParam(5, arrivalDate);
        q.setParam(6, attentionDate);
        q.setParam(7, attentionHour);
        q.setParam(8, technicianId);
        q.setParam(9, indexId);
        q.setParam(10, pqrAnulCauseId);
        q.setParam(11, satisPollId);
        q.setParam(12, pqrPollId);
        q.setParam(13, officeId);
        q.setParam(14, serial);
        q.setParam(15, enterpriseId);
        q.setParam(16, pqrReason);
        q.setParam(17, suiTransactId);
        q.setParam(18, suiCausalId);
        q.setParam(19, suiRtaId);
        q.setParam(20, suiNotifyId);
        q.setParam(21, pqrSubreason);
        q.setParam(22, channelId);
        q.setParam(23, notes);
        q.setParam(24, radOrfeo);
        q.setParam(25, noPollId);
        q.setParam(26, nif);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        billNum = MySQLQuery.getAsString(row[0]);
        creationDate = MySQLQuery.getAsDate(row[1]);
        registHour = MySQLQuery.getAsDate(row[2]);
        registBy = MySQLQuery.getAsInteger(row[3]);
        arrivalDate = MySQLQuery.getAsDate(row[4]);
        attentionDate = MySQLQuery.getAsDate(row[5]);
        attentionHour = MySQLQuery.getAsDate(row[6]);
        technicianId = MySQLQuery.getAsInteger(row[7]);
        indexId = MySQLQuery.getAsInteger(row[8]);
        pqrAnulCauseId = MySQLQuery.getAsInteger(row[9]);
        satisPollId = MySQLQuery.getAsInteger(row[10]);
        pqrPollId = MySQLQuery.getAsInteger(row[11]);
        officeId = MySQLQuery.getAsInteger(row[12]);
        serial = MySQLQuery.getAsInteger(row[13]);
        enterpriseId = MySQLQuery.getAsInteger(row[14]);
        pqrReason = MySQLQuery.getAsInteger(row[15]);
        suiTransactId = MySQLQuery.getAsInteger(row[16]);
        suiCausalId = MySQLQuery.getAsInteger(row[17]);
        suiRtaId = MySQLQuery.getAsInteger(row[18]);
        suiNotifyId = MySQLQuery.getAsInteger(row[19]);
        pqrSubreason = MySQLQuery.getAsInteger(row[20]);
        channelId = MySQLQuery.getAsInteger(row[21]);
        notes = MySQLQuery.getAsString(row[22]);
        radOrfeo = MySQLQuery.getAsString(row[23]);
        noPollId = MySQLQuery.getAsInteger(row[24]);
        nif = MySQLQuery.getAsString(row[25]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_pqr_cyl";
    }

    public static String getSelFlds(String alias) {
        return new OrdPqrCyl().getSelFldsForAlias(alias);
    }

    public static List<OrdPqrCyl> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdPqrCyl().getListFromQuery(q, conn);
    }

    public static List<OrdPqrCyl> getList(Params p, Connection conn) throws Exception {
        return new OrdPqrCyl().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdPqrCyl().deleteById(id, conn);
    }

    public static List<OrdPqrCyl> getAll(Connection conn) throws Exception {
        return new OrdPqrCyl().getAllList(conn);
    }

//fin zona de reemplazo

}
