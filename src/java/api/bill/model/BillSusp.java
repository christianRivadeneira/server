package api.bill.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.math.BigDecimal;
import java.util.Date;

public class BillSusp extends BaseModel<BillSusp> {
//inicio zona de reemplazo

    public int clientId;
    public Date suspOrderDate;
    public Date suspDate;
    public Integer suspCreatorId;
    public Integer suspTecId;
    public Date reconOrderDate;
    public Date reconDate;
    public Integer reconCreatorId;
    public Integer reconTecId;
    public Integer spanId;
    public boolean cancelled;
    public Integer cancelledBy;
    public String cancelNotes;
    public String fieldNotes;
    public Integer payBillId;
    public Date syncDate;
    public BigDecimal reading;
    public String suspType;
    public String suspNotes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "client_id",
            "susp_order_date",
            "susp_date",
            "susp_creator_id",
            "susp_tec_id",
            "recon_order_date",
            "recon_date",
            "recon_creator_id",
            "recon_tec_id",
            "span_id",
            "cancelled",
            "cancelled_by",
            "cancel_notes",
            "field_notes",
            "pay_bill_id",
            "sync_date",
            "reading",
            "susp_type",
            "susp_notes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, clientId);
        q.setParam(2, suspOrderDate);
        q.setParam(3, suspDate);
        q.setParam(4, suspCreatorId);
        q.setParam(5, suspTecId);
        q.setParam(6, reconOrderDate);
        q.setParam(7, reconDate);
        q.setParam(8, reconCreatorId);
        q.setParam(9, reconTecId);
        q.setParam(10, spanId);
        q.setParam(11, cancelled);
        q.setParam(12, cancelledBy);
        q.setParam(13, cancelNotes);
        q.setParam(14, fieldNotes);
        q.setParam(15, payBillId);
        q.setParam(16, syncDate);
        q.setParam(17, reading);
        q.setParam(18, suspType);
        q.setParam(19, suspNotes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        clientId = MySQLQuery.getAsInteger(row[0]);
        suspOrderDate = MySQLQuery.getAsDate(row[1]);
        suspDate = MySQLQuery.getAsDate(row[2]);
        suspCreatorId = MySQLQuery.getAsInteger(row[3]);
        suspTecId = MySQLQuery.getAsInteger(row[4]);
        reconOrderDate = MySQLQuery.getAsDate(row[5]);
        reconDate = MySQLQuery.getAsDate(row[6]);
        reconCreatorId = MySQLQuery.getAsInteger(row[7]);
        reconTecId = MySQLQuery.getAsInteger(row[8]);
        spanId = MySQLQuery.getAsInteger(row[9]);
        cancelled = MySQLQuery.getAsBoolean(row[10]);
        cancelledBy = MySQLQuery.getAsInteger(row[11]);
        cancelNotes = MySQLQuery.getAsString(row[12]);
        fieldNotes = MySQLQuery.getAsString(row[13]);
        payBillId = MySQLQuery.getAsInteger(row[14]);
        syncDate = MySQLQuery.getAsDate(row[15]);
        reading = MySQLQuery.getAsBigDecimal(row[16], false);
        suspType = MySQLQuery.getAsString(row[17]);
        suspNotes = MySQLQuery.getAsString(row[18]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_susp";
    }

    public static String getSelFlds(String alias) {
        return new BillSusp().getSelFldsForAlias(alias);
    }

    public static List<BillSusp> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillSusp().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillSusp().deleteById(id, conn);
    }

    public static List<BillSusp> getAll(Connection conn) throws Exception {
        return new BillSusp().getAllList(conn);
    }

//fin zona de reemplazo
}