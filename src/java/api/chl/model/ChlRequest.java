package api.chl.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class ChlRequest extends BaseModel<ChlRequest> {
//inicio zona de reemplazo

    public String reqSerial;
    public int sysReqId;
    public Integer providerId;
    public Integer clientId;
    public String kind;
    public String notes;
    public Date requestDt;
    public Date orderDt;
    public Date arrivalDt;
    public Date deliveryDt;
    public Date expectedDt;
    public Date compromiseDt;
    public Integer importId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "req_serial",
            "sys_req_id",
            "provider_id",
            "client_id",
            "kind",
            "notes",
            "request_dt",
            "order_dt",
            "arrival_dt",
            "delivery_dt",
            "expected_dt",
            "compromise_dt",
            "import_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, reqSerial);
        q.setParam(2, sysReqId);
        q.setParam(3, providerId);
        q.setParam(4, clientId);
        q.setParam(5, kind);
        q.setParam(6, notes);
        q.setParam(7, requestDt);
        q.setParam(8, orderDt);
        q.setParam(9, arrivalDt);
        q.setParam(10, deliveryDt);
        q.setParam(11, expectedDt);
        q.setParam(12, compromiseDt);
        q.setParam(13, importId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        reqSerial = MySQLQuery.getAsString(row[0]);
        sysReqId = MySQLQuery.getAsInteger(row[1]);
        providerId = MySQLQuery.getAsInteger(row[2]);
        clientId = MySQLQuery.getAsInteger(row[3]);
        kind = MySQLQuery.getAsString(row[4]);
        notes = MySQLQuery.getAsString(row[5]);
        requestDt = MySQLQuery.getAsDate(row[6]);
        orderDt = MySQLQuery.getAsDate(row[7]);
        arrivalDt = MySQLQuery.getAsDate(row[8]);
        deliveryDt = MySQLQuery.getAsDate(row[9]);
        expectedDt = MySQLQuery.getAsDate(row[10]);
        compromiseDt = MySQLQuery.getAsDate(row[11]);
        importId = MySQLQuery.getAsInteger(row[12]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "chl_request";
    }

    public static String getSelFlds(String alias) {
        return new ChlRequest().getSelFldsForAlias(alias);
    }

    public static List<ChlRequest> getList(MySQLQuery q, Connection conn) throws Exception {
        return new ChlRequest().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new ChlRequest().deleteById(id, conn);
    }

    public static List<ChlRequest> getAll(Connection conn) throws Exception {
        return new ChlRequest().getAllList(conn);
    }

//fin zona de reemplazo

}
