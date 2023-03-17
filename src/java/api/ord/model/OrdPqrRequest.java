package api.ord.model;

import api.BaseModel;
import api.Params;
import api.bill.model.BillInstance;
import api.bill.model.BillReading;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class OrdPqrRequest extends BaseModel<OrdPqrRequest> {
//inicio zona de reemplazo

    public int createdId;
    public Integer clientId;
    public Integer clientTankId;
    public Date creationDate;
    public String notes;
    public String cancelNote;
    public String type;
    public Integer typeId;
    public Date dtCancel;
    public Integer cancelledBy;
    public Integer convertedBy;
    public Integer spanId;
    public Integer instanceId;
    public Integer billMeasureId;
    public String numMeter;
    public Integer cancelId;
    public String billReqType;
    public BigDecimal average;
    public BigDecimal consu;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "created_id",
            "client_id",
            "client_tank_id",
            "creation_date",
            "notes",
            "cancel_note",
            "type",
            "type_id",
            "dt_cancel",
            "cancelled_by",
            "converted_by",
            "span_id",
            "instance_id",
            "bill_measure_id",
            "num_meter",
            "cancel_id",
            "bill_req_type",
            "average",
            "consu"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, createdId);
        q.setParam(2, clientId);
        q.setParam(3, clientTankId);
        q.setParam(4, creationDate);
        q.setParam(5, notes);
        q.setParam(6, cancelNote);
        q.setParam(7, type);
        q.setParam(8, typeId);
        q.setParam(9, dtCancel);
        q.setParam(10, cancelledBy);
        q.setParam(11, convertedBy);
        q.setParam(12, spanId);
        q.setParam(13, instanceId);
        q.setParam(14, billMeasureId);
        q.setParam(15, numMeter);
        q.setParam(16, cancelId);
        q.setParam(17, billReqType);
        q.setParam(18, average);
        q.setParam(19, consu);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        createdId = MySQLQuery.getAsInteger(row[0]);
        clientId = MySQLQuery.getAsInteger(row[1]);
        clientTankId = MySQLQuery.getAsInteger(row[2]);
        creationDate = MySQLQuery.getAsDate(row[3]);
        notes = MySQLQuery.getAsString(row[4]);
        cancelNote = MySQLQuery.getAsString(row[5]);
        type = MySQLQuery.getAsString(row[6]);
        typeId = MySQLQuery.getAsInteger(row[7]);
        dtCancel = MySQLQuery.getAsDate(row[8]);
        cancelledBy = MySQLQuery.getAsInteger(row[9]);
        convertedBy = MySQLQuery.getAsInteger(row[10]);
        spanId = MySQLQuery.getAsInteger(row[11]);
        instanceId = MySQLQuery.getAsInteger(row[12]);
        billMeasureId = MySQLQuery.getAsInteger(row[13]);
        numMeter = MySQLQuery.getAsString(row[14]);
        cancelId = MySQLQuery.getAsInteger(row[15]);
        billReqType = MySQLQuery.getAsString(row[16]);
        average = MySQLQuery.getAsBigDecimal(row[17], false);
        consu = MySQLQuery.getAsBigDecimal(row[18], false);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_pqr_request";
    }

    public static String getSelFlds(String alias) {
        return new OrdPqrRequest().getSelFldsForAlias(alias);
    }

    public static List<OrdPqrRequest> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdPqrRequest().getListFromQuery(q, conn);
    }

    public static List<OrdPqrRequest> getList(Params p, Connection conn) throws Exception {
        return new OrdPqrRequest().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdPqrRequest().deleteById(id, conn);
    }

    public static List<OrdPqrRequest> getAll(Connection conn) throws Exception {
        return new OrdPqrRequest().getAllList(conn);
    }

//fin zona de reemplazo
    public static OrdPqrRequest getByMeasure(int measureId, int instanceId, Connection conn) throws Exception {
        Params p = new Params();
        p.param("billMeasureId", measureId);
        p.param("instanceId", instanceId);
        return new OrdPqrRequest().select(p, conn);
    }

    public static void createZeroOrSame(BillReading r, BillInstance inst, Connection instConn, Connection sigmaConn) throws Exception {
        BigDecimal consu;
        if (inst.isNetInstance() && r.criticalReading != null) {
            consu = r.criticalReading.subtract(r.lastReading);
        } else {
            consu = r.reading.subtract(r.lastReading);
        }

        String reason = null;
        if (consu.compareTo(BigDecimal.ZERO) == 0) {
            reason = "Consumo en ceros";
        } else {
            BillReading lr = BillReading.getByClientSpan(r.clientTankId, r.spanId - 1, instConn);
            if (lr != null) {
                BigDecimal lconsu;
                if (inst.isNetInstance() && lr.criticalReading != null) {
                    lconsu = lr.criticalReading.subtract(lr.lastReading);
                } else {
                    lconsu = lr.reading.subtract(lr.lastReading);
                }
                if (lconsu.compareTo(consu) == 0) {
                    reason = "Consumos consecutivos iguales";
                }
            }
        }

        if (reason != null) {
            int sigmaClientId = new MySQLQuery("SELECT c.id "
                    + "FROM sigma.ord_pqr_client_tank c "
                    + "WHERE c.mirror_id = ?1 AND c.bill_instance_id = ?2;")
                    .setParam(1, r.clientTankId).setParam(2, inst.id).getAsInteger(instConn);

            Integer reqId = new MySQLQuery("SELECT r.id FROM "
                    + "sigma.ord_pqr_request r "
                    + "WHERE r.bill_req_type = 'zero' AND r.span_id = ?1 AND r.client_tank_id = ?2")
                    .setParam(1, r.spanId).setParam(2, sigmaClientId).getAsInteger(instConn);

            if (reqId == null) {
                OrdPqrRequest rq = new OrdPqrRequest();
                rq.createdId = r.empId;
                rq.clientTankId = sigmaClientId;
                rq.creationDate = new Date();
                rq.notes = reason;
                rq.spanId = r.spanId;
                rq.instanceId = inst.id;
                rq.billReqType = "zero";
                rq.consu = consu;
                rq.average = new MySQLQuery("SELECT AVG(r.reading - r.last_reading) "
                        + "FROM bill_reading r "
                        + "WHERE "
                        + "r.client_tank_id = ?1 AND "
                        + "r.span_id >=  (?2 - 6) AND "
                        + "r.span_id < ?2 ").setParam(1, r.clientTankId).setParam(2, r.spanId).getAsBigDecimal(instConn, true);
                rq.numMeter = new MySQLQuery("SELECT number "
                        + "FROM bill_meter m "
                        + "WHERE m.client_id = ?1 ORDER BY m.start_span_id DESC LIMIT 1").setParam(1, r.clientTankId).getAsString(instConn);
                rq.insert(sigmaConn);
            }
        }
    }

    public static void createCritical(BillReading r, BillInstance inst, Connection instConn, Connection sigmaConn) throws Exception {
        if (inst.isNetInstance() && r.criticalReading == null) {
            return;
        }

        int sigmaClientId = new MySQLQuery("SELECT c.id "
                + "FROM sigma.ord_pqr_client_tank c "
                + "WHERE c.mirror_id = ?1 AND c.bill_instance_id = ?2;")
                .setParam(1, r.clientTankId).setParam(2, inst.id).getAsInteger(instConn);

        Integer reqId = new MySQLQuery("SELECT r.id FROM "
                + "sigma.ord_pqr_request r "
                + "WHERE r.bill_req_type = 'reading' AND r.span_id = ?1 AND r.client_tank_id = ?2")
                .setParam(1, r.spanId).setParam(2, sigmaClientId).getAsInteger(instConn);

        if (reqId == null) {
            OrdPqrRequest rq = new OrdPqrRequest();
            rq.createdId = r.empId;
            rq.clientTankId = sigmaClientId;
            rq.creationDate = new Date();
            rq.notes = "Solicitud por lectura critica";
            rq.spanId = r.spanId;
            rq.instanceId = inst.id;
            rq.billReqType = "reading";
            if (inst.isNetInstance()) {
                rq.consu = r.criticalReading.subtract(r.lastReading); //consumo
            } else {
                rq.consu = r.reading.subtract(r.lastReading); //consumo
            }
            rq.average = new MySQLQuery("SELECT AVG(r.reading - r.last_reading) "
                    + "FROM bill_reading r "
                    + "WHERE "
                    + "r.client_tank_id = ?1 AND "
                    + "r.span_id >=  (?2 - 6) AND "
                    + "r.span_id < ?2 ").setParam(1, r.clientTankId).setParam(2, r.spanId).getAsBigDecimal(instConn, true);
            rq.numMeter = new MySQLQuery("SELECT number "
                    + "FROM bill_meter m "
                    + "WHERE m.client_id = ?1 ORDER BY m.start_span_id DESC LIMIT 1").setParam(1, r.clientTankId).getAsString(instConn);
            rq.insert(sigmaConn);

            MySQLQuery mvQ = new MySQLQuery("UPDATE sigma.bfile SET owner_id = ?1, owner_type = ?2 WHERE owner_id = ?3 AND owner_type = ?4");
            mvQ.setParam(1, rq.id);
            mvQ.setParam(2, 143);
            mvQ.setParam(3, sigmaClientId);
            mvQ.setParam(4, 144);
            mvQ.executeUpdate(instConn);
        }
    }

}
