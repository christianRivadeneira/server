package api.ord.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class OrdAibeResponse extends BaseModel<OrdAibeResponse> {

    //Fuera de la zona de reemplazo
    public Integer ctrId;

    //inicio zona de reemplazo
    public String callId;
    public Integer cylOrderId;
    public Integer negQestId;
    public String aibeResp;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "call_id",
            "cyl_order_id",
            "neg_qest_id",
            "aibe_resp"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, callId);
        q.setParam(2, cylOrderId);
        q.setParam(3, negQestId);
        q.setParam(4, aibeResp);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        callId = MySQLQuery.getAsString(row[0]);
        cylOrderId = MySQLQuery.getAsInteger(row[1]);
        negQestId = MySQLQuery.getAsInteger(row[2]);
        aibeResp = MySQLQuery.getAsString(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_aibe_response";
    }

    public static String getSelFlds(String alias) {
        return new OrdAibeResponse().getSelFldsForAlias(alias);
    }

    public static List<OrdAibeResponse> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdAibeResponse().getListFromQuery(q, conn);
    }

    public static List<OrdAibeResponse> getList(Params p, Connection conn) throws Exception {
        return new OrdAibeResponse().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdAibeResponse().deleteById(id, conn);
    }

    public static List<OrdAibeResponse> getAll(Connection conn) throws Exception {
        return new OrdAibeResponse().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<OrdAibeResponse> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/
}
