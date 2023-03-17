package api.mto.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class MtoTripDelayedMsg extends BaseModel<MtoTripDelayedMsg> {
//inicio zona de reemplazo

    public int empId;
    public Date sched;
    public String msg;
    public Date event;
    public String subject;
    public String brief;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "emp_id",
            "sched",
            "msg",
            "event",
            "subject",
            "brief"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, empId);
        q.setParam(2, sched);
        q.setParam(3, msg);
        q.setParam(4, event);
        q.setParam(5, subject);
        q.setParam(6, brief);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        empId = MySQLQuery.getAsInteger(row[0]);
        sched = MySQLQuery.getAsDate(row[1]);
        msg = MySQLQuery.getAsString(row[2]);
        event = MySQLQuery.getAsDate(row[3]);
        subject = MySQLQuery.getAsString(row[4]);
        brief = MySQLQuery.getAsString(row[5]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mto_trip_delayed_msg";
    }

    public static String getSelFlds(String alias) {
        return new MtoTripDelayedMsg().getSelFldsForAlias(alias);
    }

    public static List<MtoTripDelayedMsg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MtoTripDelayedMsg().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MtoTripDelayedMsg().deleteById(id, conn);
    }

    public static List<MtoTripDelayedMsg> getAll(Connection conn) throws Exception {
        return new MtoTripDelayedMsg().getAllList(conn);
    }

//fin zona de reemplazo
}
