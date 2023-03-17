package api.trk.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class TrkSimerRequest extends BaseModel<TrkSimerRequest> {
//inicio zona de reemplazo

    public Date dt;
    public String request;
    public Integer status;
    public String msg;
    public String sigmaEvent;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "dt",
            "request",
            "status",
            "msg",
            "sigma_event"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, dt);
        q.setParam(2, request);
        q.setParam(3, status);
        q.setParam(4, msg);
        q.setParam(5, sigmaEvent);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        dt = MySQLQuery.getAsDate(row[0]);
        request = MySQLQuery.getAsString(row[1]);
        status = MySQLQuery.getAsInteger(row[2]);
        msg = MySQLQuery.getAsString(row[3]);
        sigmaEvent = MySQLQuery.getAsString(row[4]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "trk_simer_request";
    }

    public static String getSelFlds(String alias) {
        return new TrkSimerRequest().getSelFldsForAlias(alias);
    }

    public static List<TrkSimerRequest> getList(MySQLQuery q, Connection conn) throws Exception {
        return new TrkSimerRequest().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new TrkSimerRequest().deleteById(id, conn);
    }

    public static List<TrkSimerRequest> getAll(Connection conn) throws Exception {
        return new TrkSimerRequest().getAllList(conn);
    }

//fin zona de reemplazo
}