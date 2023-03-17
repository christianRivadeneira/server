package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MssMinuteIncident extends BaseModel<MssMinuteIncident> {
//inicio zona de reemplazo

    public String notes;
    public int eventId;
    public int typeId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "notes",
            "event_id",
            "type_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, notes);
        q.setParam(2, eventId);
        q.setParam(3, typeId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        notes = MySQLQuery.getAsString(row[0]);
        eventId = MySQLQuery.getAsInteger(row[1]);
        typeId = MySQLQuery.getAsInteger(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_minute_incident";
    }

    public static String getSelFlds(String alias) {
        return new MssMinuteIncident().getSelFldsForAlias(alias);
    }

    public static List<MssMinuteIncident> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssMinuteIncident().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssMinuteIncident().deleteById(id, conn);
    }

    public static List<MssMinuteIncident> getAll(Connection conn) throws Exception {
        return new MssMinuteIncident().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<MssMinuteIncident> getByEvent(int eventId, Connection conn) throws Exception {
        Params p = new Params("event_id", eventId);        
        return new MssMinuteIncident().getListFromParams(p, conn);
    }

}
