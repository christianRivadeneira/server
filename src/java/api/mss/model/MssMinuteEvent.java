package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class MssMinuteEvent extends BaseModel<MssMinuteEvent> {
//inicio zona de reemplazo

    public Date regDate;
    public String type;
    public String notes;
    public int guardId;
    public int minuteId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "reg_date",
            "type",
            "notes",
            "guard_id",
            "minute_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, regDate);
        q.setParam(2, type);
        q.setParam(3, notes);
        q.setParam(4, guardId);
        q.setParam(5, minuteId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        regDate = MySQLQuery.getAsDate(row[0]);
        type = MySQLQuery.getAsString(row[1]);
        notes = MySQLQuery.getAsString(row[2]);
        guardId = MySQLQuery.getAsInteger(row[3]);
        minuteId = MySQLQuery.getAsInteger(row[4]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_minute_event";
    }

    public static String getSelFlds(String alias) {
        return new MssMinuteEvent().getSelFldsForAlias(alias);
    }

    public static List<MssMinuteEvent> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssMinuteEvent().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssMinuteEvent().deleteById(id, conn);
    }

    public static List<MssMinuteEvent> getAll(Connection conn) throws Exception {
        return new MssMinuteEvent().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<MssMinuteEvent> getAll(int minuteId, Connection conn) throws Exception {
        return new MssMinuteEvent().getListFromParams(new Params("minute_id", minuteId).sort("reg_date"), conn);
    }

}
