package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MssMinuteValue extends BaseModel<MssMinuteValue> {   
//inicio zona de reemplazo

    public int minuteFieldId;
    public int minuteEventId;
    public String value;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "minute_field_id",
            "minute_event_id",
            "value"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, minuteFieldId);
        q.setParam(2, minuteEventId);
        q.setParam(3, value);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        minuteFieldId = MySQLQuery.getAsInteger(row[0]);
        minuteEventId = MySQLQuery.getAsInteger(row[1]);
        value = MySQLQuery.getAsString(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_minute_value";
    }

    public static String getSelFlds(String alias) {
        return new MssMinuteValue().getSelFldsForAlias(alias);
    }

    public static List<MssMinuteValue> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssMinuteValue().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssMinuteValue().deleteById(id, conn);
    }

    public static List<MssMinuteValue> getAll(Connection conn) throws Exception {
        return new MssMinuteValue().getAllList(conn);
    }

//fin zona de reemplazo
    public static String getValue(int minuteFieldId, int minuteEventId, Connection conn) throws Exception {
        Params p = new Params("minute_field_id", minuteFieldId).param("minute_event_id", minuteEventId);
        MssMinuteValue v = new MssMinuteValue().select(p, conn);
        return v != null ? v.value : null;
    }
}
